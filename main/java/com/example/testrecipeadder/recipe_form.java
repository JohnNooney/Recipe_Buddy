package com.example.testrecipeadder;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;
import com.example.testrecipeadder.Utils.Utils;
import com.example.testrecipeadder.recipeController.DownloadImageTask;
import com.example.testrecipeadder.recipeController.HTTPBroadcastReceiver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class recipe_form extends AppCompatActivity {


    ImageView image;
    TextView httpRecipeName;
    TextView httpRecipeIngredients;
    TextView httpRecipeDirections;
    TextView imageHintText;
    View saveRecipeButton;

    //tool suite
    Utils utils = new Utils();

    // Construct a map of key-value pairs
    Map<String, Object> recipeMap = new HashMap<>();

    String webUrl;
    String site;
    String imgSrc;

    HTTPBroadcastReceiver httpReceiver;

    //initialize firebase queries
    FirebaseQueries firebase = new FirebaseQueries();

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
        saveRecipeButton = findViewById(R.id.btnSaveRecipe);

        //set text of url for testing
        ((TextView) findViewById(R.id.urlText)).setText("https://www.bbcgoodfood.com/recipes/greek-roast-lamb");

        //create http receiver
        httpReceiver = new HTTPBroadcastReceiver();
        //register receiver
        registerReceiver(httpReceiver, new IntentFilter("com.example.httppagegrab.HTTPBroadcastReceiver"));
    }


    public void HandleClicks(View view) {
        if (view.getId() == R.id.btnGetPage) {

            webUrl = ((EditText) findViewById(R.id.urlText)).getText().toString();
            if(!TextUtils.isEmpty(webUrl)){
                String[] urlSplit = webUrl.split("\\."); //split the url at each . to get just the name of the site
                site = urlSplit[1];

                //ensure that either a food.com site or bbcgoodfood.com site is being downloaded
                if (site.equals("food") || site.equals("bbcgoodfood")){
                    HTTPPageGrab(webUrl);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please enter a valid URL", Toast.LENGTH_LONG).show();
                    Log.e("URL", "URL unable to parse");
                }

            }
            else
                Toast.makeText(getApplicationContext(), "Enter a Recipe URL", Toast.LENGTH_LONG).show();

        }
        else if (view.getId() == R.id.btnSaveRecipe){
            //save to firebase store
            if (recipeMap != null){
                try{

                        firebase.addRecipeToFirebase(recipeMap);
                        firebase.UploadImage(image, imgSrc);
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
        saveRecipeButton.setVisibility(View.VISIBLE);
        imageHintText.setVisibility(View.GONE);
    }

    //use JSoup to parse the recipe info from the webpage
    public void JsoupParse(String html) {

        //use Jsoup to parse text
        Document doc = Jsoup.parse(html);

        //get title
        Elements title = doc.getElementsByClass(utils.getSiteData().get(site).get("Title"));

        //set recipe name in TextView and map
        httpRecipeName.setText(title.text());
        recipeMap.put(FirestoreRecipe.KEY_NAME, title.text());

        //get ingredients (quantities included in each element)
        Elements ingredients = doc.select(utils.getSiteData().get(site).get("Ingredients"));

        String ingredientsText = "";
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientsText += "- " + ingredients.get(i).text() + "\n"; //add ingredient
        }
        httpRecipeIngredients.setText(ingredientsText);
        //start building recipe information
        //String fullRecipe=ingredientsText + "/"; //this will act as a delimiter when entered into the database

        //set the finished recipe in the map
        recipeMap.put(FirestoreRecipe.KEY_INGREDIENTS, ingredientsText);


        //get directions
        Elements steps = doc.select(utils.getSiteData().get(site).get("Steps"));

        String directions = "";
        for (int i = 1; i < steps.size() + 1; i++) {
            directions += i + ". " + steps.get(i - 1).text() + "\n\n";
        }
        httpRecipeDirections.setText(directions);
        //fullRecipe+=directions;

        //set the directions in the map
        recipeMap.put(FirestoreRecipe.KEY_DIRECTIONS, directions);

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

        //set the image url in the map
        recipeMap.put(FirestoreRecipe.KEY_IMAGE, imgSrc);

        //image.setImageDrawable(LoadImageFromWebOperations(imageSrc));
        //return fullRecipe;
    }



    //when the activity is started this is fired. HTTP page grab broadcast will restart this activity also
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //check that the recipe adder intent is incoming
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("htmlCallback")) {
                String html = extras.get("htmlCallback").toString();
                ProcessHTML(html);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregister broadcast receiver
        unregisterReceiver(httpReceiver);

    }
}