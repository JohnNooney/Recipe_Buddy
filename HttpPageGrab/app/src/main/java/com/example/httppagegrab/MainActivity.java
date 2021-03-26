package com.example.httppagegrab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    TextView httpResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //define textview for global use
        httpResultText = (TextView)findViewById(R.id.httpResult);
        httpResultText.setMovementMethod(new ScrollingMovementMethod()); //set text as scrollable

        //set text of url for testing
        ((TextView)findViewById(R.id.urlText)).setText("https://www.food.com/recipe/best-banana-bread-2886");
    }

    public void HandleClicks(View view)
    {
        if (view.getId() == R.id.btnGetPage)
        {
            String webUrl = ((EditText)findViewById(R.id.urlText)).getText().toString();
            HTTPPageGrab(webUrl);
        }
    }

    //use the HTTPURLConnection to download page
    private void HTTPPageGrab(String webUrl) {
        HTTPRequestTask task = new HTTPRequestTask(this);
        task.execute(webUrl);
    }

    //recieve the background task of downloading the webpage and
    //use JSoup to parse the recipe info from the webpage
    //** Currently ony works with Food.com
    public void AsyncResult(String result)
    {
        //use Jsoup to parse text
        Document doc = Jsoup.parse(result);

        //get title
        Elements title = doc.getElementsByClass("recipe-title");

        //build string
        String fullRecipe = title.text() + "\n\n";

        //get ingredients
        Elements ingredients = doc.getElementsByClass("recipe-ingredients__ingredient-parts");
        Elements ingredientQuantites = doc.getElementsByClass("recipe-ingredients__ingredient-quantity");

        fullRecipe += "Ingredients \n";
        for (int i = 0; i < ingredients.size(); i++) {
            fullRecipe += "- " + ingredientQuantites.get(i).text() + " "; //add quantity
            fullRecipe += ingredients.get(i).text(); //add ingredient
            fullRecipe += "\n";
        }

        //get directions
        Elements steps = doc.getElementsByClass("recipe-directions__step");

        fullRecipe += "\n Directions \n";
        for (int i = 1; i < steps.size() + 1; i++) {
            fullRecipe += i + ". " +  steps.get(i - 1).text() + "\n";
        }

        httpResultText.setText(fullRecipe);
    }
}