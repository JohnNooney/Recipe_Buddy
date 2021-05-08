package com.example.testrecipeadder.recipeController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;

public class RecipeCheckBoradcastReceiver extends BroadcastReceiver {
    //private static final String TAG = "HTTPBroadcastReceiver";
    private FirebaseQueries fb = new FirebaseQueries();
    private FirestoreRecipe recipe;
    public static String html = "";


    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getExtras().get("webUrl").toString();

        Log.d("Check URL","broadcast inside" + url);

        fb.CheckForRecipe(url, context);
    }
}