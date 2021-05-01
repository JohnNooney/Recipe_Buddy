package com.example.testrecipeadder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testrecipeadder.Firestore.FirebaseQueries;

public class recipe_entry extends AppCompatActivity {

    ImageView image;
    TextView recipeName;
    TextView recipeIngredients;
    TextView recipeDirections;

    FirebaseQueries fb = new FirebaseQueries();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_entry);

        //define Views for global use
        image = (ImageView) findViewById(R.id.imageView);
        recipeName = (TextView) findViewById(R.id.recipeName);
        recipeIngredients = (TextView) findViewById(R.id.recipeIngredients);
        recipeDirections = (TextView) findViewById(R.id.recipeDirections);

        //get the intent from the recipe list activity
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            if (intentExtras.containsKey("recipeID")) {
                Log.d("Recipe Entry", "Intent found");
                PopulateRecipe(intentExtras.get("recipeID").toString());
            }
        }
        else
            Log.d("Recipe Entry", "Intent not found");

    }

    //when the activity is started this is fired. Get the id
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        //check that the recipe adder intent is incoming
//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            if (extras.containsKey("recipesID")) {
//                PopulateRecipe(extras.get("recipesID").toString());
//            }
//        }
//        else
//            Log.d("Recipe Entry", "Intent not found");
//
//    }

    private void PopulateRecipe(String recipeID) {
        //get the recipe from the database
        //upon query completion fill the necessary views to display data
        fb.GetSingleRecipe(recipeID, recipeName, recipeIngredients, recipeDirections, image);

    }
}