package com.example.testrecipeadder.ui.login;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.MainActivity;
import com.example.testrecipeadder.R;
import com.example.testrecipeadder.Utilities.PermissionUtils;
import com.example.testrecipeadder.Utilities.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 200;
    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    Button signupButton;

    FirebaseQueries fb = new FirebaseQueries();
    Utils utils = new Utils();

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private PermissionUtils pUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //get views in vars for class usage
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        signupButton = findViewById(R.id.signup);
        //final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        //init firebase auth instance
        auth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        //init permission checker
        pUtils = new PermissionUtils(this, this);

    }


    @Override
    protected void onStart() {
        super.onStart();
        //Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null)
        {
            Log.d("FB Login", "User is already logged in");
            //TODO: check if user is alread logged in and go to main app
        }

        //if permissions need to be set then request user to set them
        pUtils.SelectPermissions();

    }

    public void HandleClicks(View view)
    {
        //base case: make sure user is connected to internet
        if (!pUtils.CheckInternetStatus())
        {
            Log.d("Network", "No internet");
            Toast.makeText(getApplicationContext(), "Internet required", Toast.LENGTH_LONG).show();
            return;
        }

        if(view.getId() == R.id.login){
            //TODO : perform login sequence
            //authenticate user details
            if (Validate()){
                fb.LoginUser(usernameEditText.getText().toString(), passwordEditText.getText().toString(), auth, LoginActivity.this);
            }
        }
        else if (view.getId() == R.id.GoogleSignIn){
            //trigger google sign in instance
            GoogleSignIn();
        }
        else if(view.getId() == R.id.signup){
            //initiate account creation
            fb.CreateUser(usernameEditText.getText().toString(), passwordEditText.getText().toString(), auth, LoginActivity.this);
        }
    }

    //Method to handle simple authentication before authenticating with firebase
    private boolean Validate() {
        boolean flag = true;
        //check user email input
        if(!utils.isValidEmail(usernameEditText.getText())) {
            usernameEditText.setError("Email entered is invalid.");
            flag = false;
        }
        //check user password input
        if (!utils.isValidPassword(passwordEditText.getText())) {
            passwordEditText.setError("Password length must be greater than 5 characters.");
            flag = false;
        }

        return flag;
    }

    //create google sign-in intent, will be triggered when user clicks sign-in w/ google
    public void GoogleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()){
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d("FB Google", "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("FB Google", "Google sign in failed", e);
                }
            }
            else{
                Log.w("FB Auth", task.getException());
            }

        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FB Login", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Log.d("FB User", user.getDisplayName());

                            //TODO: navigate user to main app with login info
                            Toast.makeText(getApplicationContext(), "Successful Login.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            //intent.putExtra("creds", user);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FB Login", "signInWithCredential:failure", task.getException());
                            //TODO: prompt for invalid login attempt

                        }
                    }
                });
    }

    private void updateUiWithUser(String username) {
        String welcome = getString(R.string.welcome) + username;
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //for each permission granted then enable functionality
        pUtils.RequestPermissionResult(requestCode, permissions, grantResults);
    }
}