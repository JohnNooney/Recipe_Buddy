package com.example.testrecipeadder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;
import com.example.testrecipeadder.recipeController.FoodAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class recipe_list extends Fragment implements EventListener<QuerySnapshot>{

    GridView mGridView;
    FoodAdapter mArrayAdapter;
    ArrayList<FirestoreRecipe> myFoodArray = new ArrayList<>();
    FirebaseQueries fb = new FirebaseQueries();

    CollectionReference recipes = FirebaseFirestore.getInstance().collection(FirestoreRecipe.COLLECTION_PATH);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Add a listener to the entire collection of recipes which will notify of changes */
        //TODO: May be a problem since it will update everytime any user changes the collection
        recipes.addSnapshotListener(this);

        /* Populate the recipe list from Firestore data */
        populateList();
    }

    public void InitArrayAdapter()
    {
        //setup vars
        mGridView = (GridView) getView().findViewById(R.id.foodGridView);
        mArrayAdapter = new FoodAdapter(getActivity().getApplicationContext(), R.layout.row, myFoodArray);

        //assign adapter to listview
        if(mGridView != null){
            mGridView.setAdapter(mArrayAdapter);
        }

        //set click to open recipe card
        mGridView.setOnItemClickListener((adapterView, view2, i, l) -> {

            Intent myIntent = new Intent(getActivity(), recipe_entry.class);
            myIntent.putExtra("recipe", myFoodArray.get(i)); //Optional parameters
            getActivity().startActivity(myIntent);

            //Toast.makeText(getActivity().getApplicationContext(), myFoodArray.get(i).getName(), Toast.LENGTH_SHORT).show();
            Log.v("FOOD", myFoodArray.get(i).getName());
        });

        //set long click to delete function for each recipe card
        mGridView.setOnItemLongClickListener((adapterView, view2, i, l) -> {
            CreateDeleteDialoge(myFoodArray.get(i).getID(), i);
            //Toast.makeText(getActivity().getApplicationContext(), myFoodArray.get(i).getName(), Toast.LENGTH_SHORT).show();
            Log.v("FOOD", myFoodArray.get(i).getName());
            return true;
        });


//        getView().findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(RecipeListFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });
    }

    private void CreateDeleteDialoge(String id, Integer arrListIndex) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        fb.DeleteRecipe(id);
                        myFoodArray.remove(arrListIndex);
                        Toast.makeText(getActivity().getApplicationContext(), "Recipe Deleted", Toast.LENGTH_SHORT).show();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void populateList(){
        //Get all recipe documents in collection
        fb.GetAllRecipes(myFoodArray, recipe_list.this, recipes);

    }

    //Process updated to any of the recipes in the Firestore "recipes collection"
    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        /* If exception occurs, don't try to do anything else, just display the error and return */
        if (e != null) {
            Log.e("DB", "Listen failed.", e);
            return;
        }

        /* Update data from Firestore, then list via adapter if data changes*/
        if(myFoodArray != null && mArrayAdapter != null){ /* important checks to avoid crashes */
            /* If there is a change these should not be bull, it may be empty if
             * all documents have been deleted form the collection. */
            if(queryDocumentSnapshots!=null && !queryDocumentSnapshots.isEmpty()) {
                //TODO: clearing an entire list and repopulating it is not efficient
                mArrayAdapter.clear();
                myFoodArray = new ArrayList<>();

                /* Gets all documents in the affected collection */
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    FirestoreRecipe recipe = document.toObject(FirestoreRecipe.class);

                    //only add the recipe if it belongs to the user
                    if (recipe.getRoles().containsKey(fb.auth.getUid())){
                        recipe.setID(document.getId());

                        //only add recipe if it was not in local recipe list
                        //if (!myFoodArray.contains(recipe))
                        myFoodArray.add(recipe);
                    }

                }

                /* Add all data to adapter */
                //mArrayAdapter.addAll(myFoodArray);
                InitArrayAdapter();


                /* Tell the adapter to redraw the listView with new dataset */
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mArrayAdapter.notifyDataSetChanged();
                    }
                });

            }
        }
    }
}