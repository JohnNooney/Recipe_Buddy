package com.example.testrecipeadder.recipeController;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;
import com.example.testrecipeadder.R;
import com.example.testrecipeadder.Utilities.Utils;

import java.util.ArrayList;

public class FoodAdapter extends ArrayAdapter<FirestoreRecipe> {

    Context mContext;
    int mLayoutResourceId;
    ArrayList<FirestoreRecipe> mData = null;
    FirebaseQueries fb = new FirebaseQueries();
    Utils utils = new Utils();

    public FoodAdapter(Context context, int resource, ArrayList<FirestoreRecipe> data) {
        super(context, resource, data);

        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mData = data;
    }

    @Override
    public FirestoreRecipe getItem(int position) {
        return super.getItem(position);
    }


    //gets called for every item in the ListView
    //with increased efficiency by only loading in the list items when
    //the appear on the screen
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder = null;

        //if we dont have a row view to reuse..
        if (row == null)
        {
            //inflate the layout for a single row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mLayoutResourceId, parent, false);

            holder = new PlaceHolder();

            //get reference to the different view elements to update
            //and add them to the temp holder
            holder.nameView = (TextView) row.findViewById(R.id.nameTextView);
            holder.imageView = (ImageView) row.findViewById(R.id.imageView);

            //allow to set ref to holder class
            row.setTag(holder);
        }else{
            //otherwise overwrite an existing row view.
            holder = (PlaceHolder) row.getTag();
        }


        //get the data from the data array
        FirestoreRecipe food = mData.get(position);

        //setting the view to reflect the data we need to display
        holder.nameView.setText(food.getName());

        //download image file from the database or use the local image fil
        utils.GetImage(food.getImage(), holder.imageView);
        return row;
    }

//    @Override
//    public void addAll(FirestoreRecipe... items) {
//        super.addAll(items);
//    }

    public void downloadImages() {
    }

    //add on click listener for each recipe
//    View.OnClickListener PopupListener = new View.OnClickListener(){
//        @Override
//        public void onClick(View view) {
//            Integer viewPosition = (Integer) view.getTag();
//            FirestoreRecipe p = mData.get(viewPosition);
//            Toast.makeText(getContext(),viewPosition.toString(), Toast.LENGTH_SHORT).show();
//        }
//    };

    private static class PlaceHolder{
        TextView nameView;
        ImageView imageView;
    }
}
