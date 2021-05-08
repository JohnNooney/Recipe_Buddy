package com.example.testrecipeadder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;
import com.example.testrecipeadder.Utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

//Class for holding the functionality to displaying a recipe
//when clicked on from the recipe list
public class recipe_entry extends AppCompatActivity {

    private static final int REQUEST_CODE = 200;

    private ImageView image;
    private TextView recipeName;
    private TextView recipeIngredients;
    private TextView recipeDirections;
    private Button saveChangesBtn;
    private FirestoreRecipe fullRecipe;
    private Map<String, Object> recipeMap = new HashMap<>();

    private boolean changesMade = false;
    private boolean imgChanged = false;

    private Utils utils = new Utils();
    private  FirebaseQueries fb = new FirebaseQueries();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_entry);

        //define Views for global use
        image = (ImageView) findViewById(R.id.imageView);
        recipeName = (TextView) findViewById(R.id.recipeName);
        recipeIngredients = (TextView) findViewById(R.id.recipeIngredients);
        recipeDirections = (TextView) findViewById(R.id.recipeDirections);
        saveChangesBtn = (Button) findViewById(R.id.saveChangesBtn);

        //get the intent from the recipe list activity
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            if (intentExtras.containsKey("recipe")) {
                Log.d("Recipe Entry", "Intent found");
                PopulateRecipeUI((FirestoreRecipe) intentExtras.getSerializable("recipe"));
            }
        }
        else
            Log.d("Recipe Entry", "Intent not found");

        //add text and imageview click listeners to check if user will be entering the recipe manually
        //AddUIListeners();


    }

    public void HandleClicks(View view){
        if (view.getId() == R.id.saveChangesBtn){
            //TODO: add functionality to save the updated recipe. Check if image needs to be updated as well
            if (changesMade){

                //convert recipe to recipe map
                PopulateRecipeMap();

                //If image has been changed then update recipe image as well
                if (imgChanged)
                {
                    Log.d("Image", "Image must be changed");
                }
                //fb.UpdateRecipe(recipeMap, recipe_entry.this);
            }
            else
                Toast.makeText(getApplicationContext(), "No changes made to save.", Toast.LENGTH_LONG).show();


        }
    }

    private void PopulateRecipeMap() {
        recipeMap.put(FirestoreRecipe.KEY_NAME, fullRecipe.getName());
        recipeMap.put(FirestoreRecipe.KEY_INGREDIENTS, fullRecipe.getIngredients());
        recipeMap.put(FirestoreRecipe.KEY_DIRECTIONS, fullRecipe.getDirections());
        recipeMap.put(FirestoreRecipe.KEY_IMAGE, fullRecipe.getImage());
        recipeMap.put(FirestoreRecipe.KEY_ROLES, fullRecipe.getRoles());
        recipeMap.put("id", fullRecipe.getID());
    }

    //when the activity is started this is fired. Get the id
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //check that the recipe adder intent is incoming
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("updatedRecipe")) {
                fullRecipe = (FirestoreRecipe) extras.get("updatedRecipe");
                PopulateRecipeMap();
            }
        }
        else
            Log.d("Recipe Entry", "Intent not found");

    }

    private void PopulateRecipeUI(FirestoreRecipe recipe) {
        //assign the recipe
        fullRecipe = recipe;

        //set the text into the textViews
        recipeName.setText(recipe.getName());
        recipeIngredients.setText(recipe.getIngredients());
        recipeDirections.setText(recipe.getDirections());

        utils.GetImage(recipe.getImage(), image);
    }


    private void AddUIListeners() {
        recipeName.addTextChangedListener(textWatcher);

        recipeIngredients.addTextChangedListener(textWatcher);

        recipeDirections.addTextChangedListener(textWatcher);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open image gallery for user to select one
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    //Textwatcher to be used in each of the text fields
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //make save recipe button visible
            saveChangesBtn.setVisibility(View.VISIBLE);

            //user has edited text
            changesMade = true;
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //When the user has selected an image from the gallery, insert it into the imageview field
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode){
                case REQUEST_CODE:
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
                        //use image uri to create bitmap and set it in drawable
                        Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        image.setImageDrawable(drawable);

                        //make save recipe button visible AND remove option to add image
                        saveChangesBtn.setVisibility(View.VISIBLE);

                        //to signal that recipe image has changed
                        changesMade = true;
                        imgChanged = true;

                        Log.d("Image Gallery", "Image set");
                        break;
                    }else if(resultCode == Activity.RESULT_CANCELED) {
                        Log.e("Image Gallery", "Selecting picture cancelled");
                    }
                    break;
            }
        }
        catch(Exception e){
            Log.e("Image Gallery", "Exception in onActivityResult: " + e.getMessage());
        }
    }

}