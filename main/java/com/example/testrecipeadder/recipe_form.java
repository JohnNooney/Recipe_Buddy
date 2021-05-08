package com.example.testrecipeadder;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;
import com.example.testrecipeadder.Utilities.PermissionUtils;
import com.example.testrecipeadder.Utilities.Utils;
import com.example.testrecipeadder.recipeController.DownloadImageTask;
import com.example.testrecipeadder.recipeController.HTTPBroadcastReceiver;
import com.example.testrecipeadder.recipeController.RecipeCheckBoradcastReceiver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

//Holds the functionality for the screen that adds
//a recipe. User can either manually fill in the recipe
//or retrive one via a link from BBCGoodFood or Food.com
public class recipe_form extends AppCompatActivity {


    private static final int REQUEST_CODE = 200;
    private ImageView image;
    private TextView httpRecipeName;
    private TextView httpRecipeIngredients;
    private TextView httpRecipeDirections;
    private TextView imageHintText;
    private View saveRecipeButton;
    private TextView recipeFormHint;

    //tool suite
    private Utils utils = new Utils();
    private PermissionUtils pUtils;

    // Construct a map of key-value pairs
    private Map<String, Object> recipeMap = new HashMap<>();

    private String webUrl;
    private String site;
    private String imgSrc;
    private boolean recipeReceived = false;
    private boolean sharedRecipe = false;
    private FirestoreRecipe recipeToShare;

    private HTTPBroadcastReceiver httpReceiver;
    private RecipeCheckBoradcastReceiver recipeCheckReceiver;

    //initialize firebase queries
    private FirebaseQueries fb = new FirebaseQueries();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_form);

        //define Views for global use
        image = (ImageView) findViewById(R.id.imageView);
        httpRecipeName = (TextView) findViewById(R.id.recipeName);
        httpRecipeIngredients = (TextView) findViewById(R.id.recipeIngredients);
        httpRecipeDirections = (TextView) findViewById(R.id.recipeDirections);
        imageHintText = (TextView) findViewById(R.id.imageHint);
        recipeFormHint = (TextView) findViewById(R.id.recipeFormHint);
        saveRecipeButton = findViewById(R.id.btnSaveRecipe);

        //set text of url for testing
        //((TextView) findViewById(R.id.urlText)).setText("https://www.bbcgoodfood.com/recipes/greek-roast-lamb");

        //create http receiver
        httpReceiver = new HTTPBroadcastReceiver();
        recipeCheckReceiver = new RecipeCheckBoradcastReceiver();

        //register broadcaset receiver
        registerReceiver(httpReceiver, new IntentFilter("com.example.httppagegrab.HTTPBroadcastReceiver"));
        registerReceiver(recipeCheckReceiver, new IntentFilter("com.example.httppagegrab.RecipeCheckBroadcastReceiver"));


        //add text and imageview click listeners to check if user will be entering the recipe manually
        AddUIListeners();

        //Init permissions tool suite
        pUtils = new PermissionUtils(this, this);

    }

    public void HandleClicks(View view) {
        if (view.getId() == R.id.btnGetPage) {
            //base case: check that internet is available before fetching webpage
            if (!pUtils.CheckInternetStatus()){
                Log.d("Network", "No internet");
                Toast.makeText(getApplicationContext(), "Internet required to fetch webpage.", Toast.LENGTH_LONG).show();
                return;
            }

            webUrl = ((EditText) findViewById(R.id.urlText)).getText().toString();
            if(!TextUtils.isEmpty(webUrl)){
                String[] urlSplit = webUrl.split("\\."); //split the url at each . to get just the name of the site
                site = urlSplit[1];

                //ensure that either a food.com site or bbcgoodfood.com site is being downloaded
                if (site.equals("food") || site.equals("bbcgoodfood")){

                    //Perform check to see if recipe from entered url already exists, if so add user to it
                    RecipeCheck(webUrl);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Only Food.com and BBCGoodFood.com are supported at the moment.", Toast.LENGTH_LONG).show();
                    Log.e("URL", "URL unable to parse");
                }

            }
            else
                Toast.makeText(getApplicationContext(), "Enter a Recipe URL", Toast.LENGTH_LONG).show();

        }
        else if (view.getId() == R.id.btnSaveRecipe){
            //save to firebase store if fields are filled
            if (ValidateFields()){
                //fill the recipe map with data in textViews and imageView
                PopulateRecipeMap();
                try{
                        if(sharedRecipe && recipeToShare != null){
                            Log.d("Recipe", "recipe is shared");
                            fb.AddUserToRecipe(recipeToShare.getID(), recipeToShare.getRoles());
                        }
                        else{
                            fb.addRecipeToFirebase(recipeMap);
                            try {
                                fb.UploadImage(image, imgSrc);
                            }
                            catch(Exception e){
                                Toast.makeText(getApplicationContext(), "Issue Saving Image", Toast.LENGTH_LONG).show();
                            }
                        }

                        Toast.makeText(getApplicationContext(), "Successfully Added Recipe", Toast.LENGTH_LONG).show();

                        Intent myIntent = new Intent(recipe_form.this, MainActivity.class);
                        // myIntent.putExtra("key", value); //Optional parameters
                        recipe_form.this.startActivity(myIntent);

                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.e("Firebase Error", "Unable to save recipe");
                }
            }
        }
    }

    //check with database if recipe exists in it
    private void RecipeCheck(String webUrl){
        Log.d("Check URL","broadcast " + webUrl);

        Intent intent = new Intent();
        intent.setAction("com.example.httppagegrab.RecipeCheckBroadcastReceiver");
        intent.putExtra("webUrl", webUrl);
        sendBroadcast(intent);
    }


    //use the HTTPURLConnection to download page
    private void HTTPPageGrab(String webUrl) {
        Log.d("url", webUrl);

        //create intent for broadcast receiver to pick up on
        Intent intent = new Intent();
        intent.setAction("com.example.httppagegrab.HTTPBroadcastReceiver");
        intent.putExtra("webUrl", webUrl);
        sendBroadcast(intent);

    }

    //Determine if the HTML can be parsed
    //** Currently ony works with Food.com and BBCGoodFood.com
    public void ProcessHTML(String result) {
        //set the url in the recipe map
        recipeMap.put(FirestoreRecipe.KEY_URL, webUrl);
        JsoupParse(result);

        //make save recipe button visible AND remove option to add image
        //saveRecipeButton.setVisibility(View.VISIBLE);
        imageHintText.setVisibility(View.GONE);

        //if this method is reached then recipe is new
        sharedRecipe = false;
    }

    //use JSoup to parse the recipe info from the webpage
    public void JsoupParse(String html) {

        //use Jsoup to parse text
        Document doc = Jsoup.parse(html);

        //get title
        Elements title = doc.getElementsByClass(utils.getSiteData().get(site).get("Title"));

        //set recipe name in TextView and map
        httpRecipeName.setText(title.text());

        //get ingredients (quantities included in each element)
        Elements ingredients = doc.select(utils.getSiteData().get(site).get("Ingredients"));

        String ingredientsText = "";
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientsText += "- " + ingredients.get(i).text() + "\n"; //add ingredient
        }
        httpRecipeIngredients.setText(ingredientsText);


        //get directions
        Elements steps = doc.select(utils.getSiteData().get(site).get("Steps"));

        String directions = "";
        for (int i = 1; i < steps.size() + 1; i++) {
            directions += i + ". " + steps.get(i - 1).text() + "\n\n";
        }
        httpRecipeDirections.setText(directions);

        //get image
        Elements imageElement = doc.select(utils.getSiteData().get(site).get("Image"));

        if (imageElement != null) {
            //for food.com image retrieval
            if (imageElement.first().hasAttr("content"))
                imgSrc = imageElement.first().attr("content");
            //for bbcgoodfood image retrieval
            else
                imgSrc = imageElement.select("img").first().attr("src");  //absolute URL on src

            new DownloadImageTask(image).execute(imgSrc);
        } else
            Log.e("Image", "Path not found");

        //get the last segment of image URL to save in db
        Uri imgUri = Uri.parse(imgSrc);
        imgSrc = imgUri.getLastPathSegment();
    }


    //when the activity is started this is fired. HTTP page grab broadcast will restart this activity also
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            //check that the recipe adder intent is incoming
            if (extras.containsKey("htmlCallback")) {
                String html = extras.get("htmlCallback").toString();
                if (!html.equals("invalid")){
                    recipeReceived = true;
                    ProcessHTML(html);
                }
                else{
                    Toast.makeText(recipe_form.this, "Invalid URL", Toast.LENGTH_LONG).show();
                }
            }
            //check that the recipe database check intent is incoming
            if(extras.containsKey("recipeCheckCallback")){
                String response = extras.get("recipeCheckCallback").toString();
                //the recipe is newly added then proceed with standard recipe retrieval
                if (response.equals("does not exist")){
                    Log.d("Recipe", "Recipe not found in DB.");
                    HTTPPageGrab(webUrl);
                    return;
                }
                //the recipe exists so use it
                recipeToShare = (FirestoreRecipe) extras.get("recipeCheckCallback");

                //fill in the fields with retrieved recipe
                httpRecipeName.setText(recipeToShare.getName());
                httpRecipeIngredients.setText(recipeToShare.getIngredients());
                httpRecipeDirections.setText(recipeToShare.getDirections());
                fb.DownloadImageTemp(recipeToShare.getImage(), image);

                //remove text hint from image view
                imageHintText.setVisibility(View.GONE);

                //flag that this will become a shared recipe if saved
                sharedRecipe = true;
            }
        }

    }

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
                        saveRecipeButton.setVisibility(View.VISIBLE);
                        recipeFormHint.setVisibility(View.GONE);
                        imageHintText.setVisibility(View.GONE);

                        //to signal that recipe image did not come from website
                        //or that this a shared recipe
                        recipeReceived = false;
                        sharedRecipe = false;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregister broadcast receiver
        unregisterReceiver(httpReceiver);
        unregisterReceiver(recipeCheckReceiver);

    }

    //Textwatcher to be used in each of the text fields
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //make save recipe button visible
            saveRecipeButton.setVisibility(View.VISIBLE);
            recipeFormHint.setVisibility(View.GONE);

            //if the user changes the text after it was taken from the DB as a shared recipe then change it to a custom recipe
            if (sharedRecipe)
                sharedRecipe = false;
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //Text listeners to check if user will be entering the recipe manually
    private void AddUIListeners(){
        httpRecipeName.addTextChangedListener(textWatcher);

        httpRecipeIngredients.addTextChangedListener(textWatcher);

        httpRecipeDirections.addTextChangedListener(textWatcher);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if user has enabled permission to allow for storage
                if (pUtils.PermissionCheck()){
                    //Open image gallery for user to select one
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                else
                    pUtils.SelectPermissions();

            }
        });
    }

    //Used to populate recipe map as to properly save to database
    private void PopulateRecipeMap()
    {
        recipeMap.put(FirestoreRecipe.KEY_NAME, httpRecipeName.getText().toString());
        recipeMap.put(FirestoreRecipe.KEY_INGREDIENTS, httpRecipeIngredients.getText().toString());
        recipeMap.put(FirestoreRecipe.KEY_DIRECTIONS, httpRecipeDirections.getText().toString());

        //if user did not get image from url then generate a custom identifier
        if (!recipeReceived){
            imgSrc = fb.auth.getUid() + httpRecipeName.getText().toString();
        }

        //if the recipe is a shared recipe

        recipeMap.put(FirestoreRecipe.KEY_IMAGE, imgSrc);

    }

    //validate that the entered fields are filled
    private Boolean ValidateFields()
    {
        if (!httpRecipeName.getText().toString().trim().equals("") && !httpRecipeIngredients.getText().toString().trim().equals("") && !httpRecipeDirections.getText().toString().trim().equals(""))
            return true;


        Toast.makeText(getApplicationContext(), "Fill out all fields before submitting", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        pUtils.RequestPermissionResult(requestCode, permissions, grantResults);
    }
}