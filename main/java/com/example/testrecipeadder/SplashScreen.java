package com.example.testrecipeadder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.ui.login.LoginActivity;

public class SplashScreen extends AppCompatActivity {

    private FirebaseQueries fb = new FirebaseQueries();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //wait 1 second to decide where user should go
        (new Handler()).postDelayed(this::UserLoginCheck, 1000);
    }

    private void UserLoginCheck(){
        if (fb.CheckUserSignIn(this)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}