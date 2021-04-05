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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ListView mListView;
    GridView mGridView;
    FoodAdapter mArrayAdapter;
    Food[] myFoodArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitStringArr();

        //setup vars
        //mListView = (ListView) findViewById(R.id.testListView);
        mGridView = (GridView) findViewById(R.id.testGridView);
        mArrayAdapter = new FoodAdapter(getApplicationContext(), R.layout.row, myFoodArray);

        //assign adapter to listview
        if(mGridView != null){
            //mListView.setAdapter(mArrayAdapter);
            mGridView.setAdapter(mArrayAdapter);
        }

        //when a list item is clicked
        mGridView.setOnItemClickListener((adapterView, view, i, l) -> {
            Toast.makeText(getApplicationContext(),myFoodArray[i].mNameOfFood, Toast.LENGTH_SHORT).show();
            Log.v("FOOD", myFoodArray[i].mNameOfFood);
        });
    }

    private void InitStringArr(){
        myFoodArray = new Food[]{
                new Food("Clam Chowder", "ClamChowder", "Staple of Maine."),
                new Food("Lobster Roll", "LobsterRoll", "Best to get it on the East Coast."),
                new Food("Grilled Shark", "GrilledShark", "A delicacy in South Africa."),
                new Food("Fried Alligator", "FriedAlligator", "Try this in Florida"),
                new Food("CrawFish Nibbles", "CrawFishNibbles", "Best side for cajun cooking."),
                new Food("Cajun Chicken", "CajunChicken", "Easy to make and tastes great."),
                new Food("Sourdough Loaf", "SourdoughLoaf", "Oldest type of bread in the world."),
                new Food("Cheddar Biscuits", "CheddarBiscuits", "Best type of bread in the world.")
        };
    }
}