package com.example.recipebuddycardlist;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ListView mListView;
    FoodAdapter mArrayAdapter;
    Food[] myFoodArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitStringArr();

        //setup vars
        mListView = (ListView) findViewById(R.id.testListView);
        mArrayAdapter = new FoodAdapter(getApplicationContext(), R.layout.row, myFoodArray);

        //assign adapter to listview
        if(mListView != null){
            mListView.setAdapter(mArrayAdapter);
        }

        //when a list item is clicked
        mListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Toast.makeText(getApplicationContext(),myFoodArray[i].mNameOfFood, Toast.LENGTH_SHORT).show();
            Log.v("FOOD", myFoodArray[i].mNameOfFood);
        });
    }

    private void InitStringArr(){
        myFoodArray = new Food[]{
                new Food("Clam Chowder", "ClamChowder"),
                new Food("Lobster Roll", "LobsterRoll"),
                new Food("Grilled Shark", "GrilledShark"),
                new Food("Fried Alligator", "FriedAlligator"),
                new Food("CrawFish Nibbles", "CrawFishNibbles"),
                new Food("Cajun Chicken", "CajunChicken"),
                new Food("Sourdough Loaf", "SourdoughLoaf"),
                new Food("Cheddar Biscuits", "CheddarBiscuits")
        };
    }
}