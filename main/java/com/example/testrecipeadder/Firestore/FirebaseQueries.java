package com.example.testrecipeadder.Firestore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.testrecipeadder.MainActivity;
import com.example.testrecipeadder.recipe_list;
import com.example.testrecipeadder.recipe_form;
import com.example.testrecipeadder.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FirebaseQueries {
    private StorageReference sref = FirebaseStorage.getInstance().getReference();
    private FirebaseStorage dbStorage = FirebaseStorage.getInstance("gs://recipebuddytest.appspot.com");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference myRef;
    private String _userID;
    //private GoogleSignInClient mGoogleSignInClient;
    private static String TAG = "DB";
    //private Utils utils = new Utils();
    public FirebaseAuth auth;

    public String userID() {
        return _userID;
    }

    public void userID(String id) {
        _userID = id;
    }

    public FirebaseQueries() {
        auth = FirebaseAuth.getInstance();
    }

    //if no user is logged in then send them to log in screen
    public boolean CheckUserSignIn(Context context) {
        if (auth.getCurrentUser() == null) {
            //direct to sign in context
            UserSignOut(context);
            return false; //just in case
        }

        return true;
    }


    //sign user out on demand
    public void UserSignOut(Context context) {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            Toast.makeText(context, "Successfully signed out.", Toast.LENGTH_LONG).show();
        }

        //direct to sign in context
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    //method to add a new recipe to the firestore database
    public void addRecipeToFirebase(Map<String, Object> recipeMap) {
        /* Add a new document with a generated ID (name) */
        //add roles map. user who created recipe is added to owner
        if (auth.getCurrentUser() != null) {
            recipeMap.put("roles", new HashMap<String, String>() {{
                put(auth.getCurrentUser().getUid(), "owner");
            }});
        }

        db.collection(FirestoreRecipe.COLLECTION_PATH).add(recipeMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("D", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.w("D", "Error adding document", e);
                    }
                });
    }

    //method to add a user to the roles of a recipe so they can access it as well
    public void AddUserToRecipe(String recipeId, Map<String, String> roles){

        Log.d(TAG, "Recipe id is: " + recipeId);
        //check that the recipe doesn't already exist within the user's list
        db.collection(FirestoreRecipe.COLLECTION_PATH).document(recipeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        //turn document into recipe object
                        FirestoreRecipe recipe = doc.toObject(FirestoreRecipe.class);

                        Log.d(TAG, "Recipe found. Checking roles");

                        //if the logged in user does not already have this recipe then add them to the roles
                        if (!recipe.getRoles().containsKey(auth.getCurrentUser().getUid())){

                            //create a map that will add the new user to the roles map in the document
                            Map<String, Object> allRoles = new HashMap<String, Object>(){{put("roles", new HashMap<String, String>(){{
                                //add existing roles
                                putAll(roles);
                                //add new role
                                put(auth.getCurrentUser().getUid(), "owner");
                            }});
                            }};

                            //replace the old roles map in the document with a new one
                            db.collection(FirestoreRecipe.COLLECTION_PATH).document((String) recipeId).set(allRoles, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "User has been added to recipe");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding User", e);
                                }
                            });
                        }
                        else
                        {
                            Log.d(TAG, "User already has recipe");
                        }
                    }
                }
            }
        });


    }

    //Get all recipes from the database and filter the results based on which recipes belong to which users
    //after that update UI
    public void GetAllRecipes(ArrayList<FirestoreRecipe> myFoodArray, recipe_list recipelist, CollectionReference recipes) {
        recipes.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //foreach document in the list get results
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("DB objects", document.getId() + " => " + document.getData())
                                // Get recipe as FirestoreRecipe object
                                FirestoreRecipe recipe = document.toObject(FirestoreRecipe.class);

                                //only add the recipe if it belongs to the user
                                if (recipe.getRoles().containsKey(auth.getUid())) {
                                    /* Set object's ID from document name (id) */
                                    recipe.setID(document.getId());
                                    /* Add to list of recipes */
                                    myFoodArray.add(recipe);
                                }

                            }
                            /* Initialise the adapter with the list of recipes */
                            //adapter = new ContactsAdapter(ContactsListActivity.this, data);
                            /* Attach the adapter to our list view. */
                            //list.setAdapter(adapter);
                            recipelist.InitArrayAdapter();

                        } else {
                            Log.w("Database Error", "Error getting documents.", task.getException());
                        }
                    }
                });
    }


    //retrieve recipe from the db based on id and fill in the necessary views with that information
    public void GetSingleRecipe(String id) {

        DocumentReference recipe = db.collection(FirestoreRecipe.COLLECTION_PATH).document(id);

        //Query database for recipe
        recipe.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        //turn document into recipe object
                        FirestoreRecipe fullRecipe = doc.toObject(FirestoreRecipe.class);

                        Log.d(TAG, "Recipe found");
                    }
                } else
                    Log.e(TAG, "Recipe not found");
            }
        });
    }

    //TODO: fix update method. currently doesnt update recipe at all
    //Method to update recipe.
//    public void UpdateRecipe(Map<String, Object>recipeMap, Context context){
//
//        Intent intent = new Intent(context, recipe_entry.class);
//
//        //If recipe is shared then branch off and create new recipe for that specific user
//        if (recipeMap.containsKey("url"))
//        {
//            recipeMap.remove("url");
//            recipeMap.remove("roles");
//
//            recipeMap.put("roles", new HashMap<String, String>() {{
//                put(auth.getCurrentUser().getUid(), "owner");
//            }});
//
//            db.collection(FirestoreRecipe.COLLECTION_PATH).add(recipeMap)
//                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//                            Log.d("D", "Unique Recipe added with ID: " + documentReference.getId());
//
//                            //send the user back to the recipe page with the updated image
//                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if (task.isSuccessful()){
//                                        DocumentSnapshot doc = task.getResult();
//                                        if (doc.exists()) {
//                                            //turn document into recipe object
//                                            FirestoreRecipe updatedRecipe = doc.toObject(FirestoreRecipe.class);
//
//                                            Toast.makeText(context, "Recipe updated.", Toast.LENGTH_LONG).show();
//
//                                            intent.putExtra("updatedRecipe", updatedRecipe);
//
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            e.printStackTrace();
//                            Log.w("D", "Error adding document", e);
//                        }
//                    });
//            return;
//        }
//        else{
//            DocumentReference recipe = db.collection(FirestoreRecipe.COLLECTION_PATH).document(recipeMap.get("id").toString());
//
//            //Otherwise update the recipe
//            recipe.update(recipeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if (task.isSuccessful()){
//                        Log.d(TAG, "Recipe Updated.");
//
//                        //send the user back to the recipe page with the updated image
//                        recipe.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if (task.isSuccessful()){
//                                    DocumentSnapshot doc = task.getResult();
//                                    if (doc.exists()) {
//                                        //turn document into recipe object
//                                        FirestoreRecipe updatedRecipe = doc.toObject(FirestoreRecipe.class);
//
//                                        Toast.makeText(context, "Recipe updated.", Toast.LENGTH_LONG).show();
//
//                                        intent.putExtra("updatedRecipe", updatedRecipe);
//
//                                    }
//                                }
//                            }
//                        });
//                    }
//                    else{
//                        Log.d(TAG, "Unable to update recipe.", task.getException());
//                    }
//                }
//            });
//        }
//
//        context.startActivity(intent);
//
//    }

    //delete document based on ID
    public void DeleteRecipe(String id) {
        //Check if the recipe is a shared recipe. if so only remove user from roles
        DocumentReference recipe = db.collection(FirestoreRecipe.COLLECTION_PATH).document(id);


        recipe.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){
                        FirestoreRecipe recipeToDelete = doc.toObject(FirestoreRecipe.class);
                        Log.d(TAG, "recipe url: " + recipeToDelete.getUrl());
                        //check to see if recipe has a url. If so then it is a shared recipe
                        if (recipeToDelete.urlCheck()){

                            Map<String, String> newRoles = recipeToDelete.getRoles();

                            //remove current user from new roles list
                            if (newRoles.containsKey(auth.getUid())){
                                newRoles.remove(auth.getUid());

                                //create a map that will delete the current user from the recipe roles
                                Map<String, Object> allRoles = new HashMap<String, Object>(){{put("roles", new HashMap<String, Object>(){{
                                    //add existing roles
                                    putAll(newRoles);
                                    put(auth.getUid(), FieldValue.delete()); //reflect removal on server
                                }});
                                }};

                                Log.d(TAG, "Entire Roles list: "+ allRoles);
                                //Log.d(TAG, "Ref key: "+ myRef.child("roles").child(auth.getUid()).getKey());

                                //replace the old roles map in the document with a new one
                                recipe.set(allRoles, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "User has been removed from recipe list");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error removing User", e);
                                    }
                                });

                            }
                        }
                        //otherwise it is a unique recipe and can be deleted from the DB
                        else{
                            Log.d(TAG, "Deleting recipe from DB");
                            recipe.delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Doc deleted successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }
                    }
                }
            }
        });

    }

    public void UploadImage(ImageView image, String imgName) {
        if (image != null) {
            //Get the data from an ImageView as bytes
            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            //check if file already exists
            sref.child("RecipeImages/" + imgName).getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //file not found, add it to the db

                    //get the references to the folder where all the images are
                    //***may need to have recipes saved with a unique image path to use as title for image data in bucket
                    StorageReference imagesRef = sref.child("RecipeImages/" + imgName);

                    UploadTask uploadTask = imagesRef.putBytes(data);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("DB Image", "Image has been saved successfully");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("DB Image", "Image not saved");
                        }
                    });
                }
            });

        }
    }

    //Downloads the image to a local file so user's can use them in offline mode
    //return the file path to where the image is downloaded
    public void DownloadImage(String imgName, ImageView image) {
        //Create file in temp directory
        //File localImgFile = new File(System.getProperty("java.io.tmpdir"), imgName);
        File localImg = null;
        try {
            localImg = File.createTempFile(imgName, "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File finalLocalImg = localImg;
        sref.getFile(localImg).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //Local temp file is created
                try {

                    Log.d("Image", finalLocalImg.getAbsolutePath());
                    //Might need to decode as byte array (save to file as bytearray)
                    Bitmap imgBitmap = BitmapFactory.decodeFile(finalLocalImg.getAbsolutePath());
                    image.setImageBitmap(imgBitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //Download an image for the moment it is being shown on the screen
    //requires constant connection to display images
    public void DownloadImageTemp(String imgName, ImageView image) {
        //make sure image exists before trying to download it
        sref.child("RecipeImages/" + imgName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //get the references to the folder where the image is
                StorageReference imagesRef = sref.child("RecipeImages/" + imgName);

                //Log.d("File Path", localFile.getAbsolutePath());
                imagesRef.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d("DB Image", "Image has been downloaded successfully");
                        Bitmap imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        image.setImageBitmap(imgBitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Image unable to downloaded");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Image not found");
            }
        });


        //return the location of the temp image
        //return localFile.getAbsolutePath();
    }

    //method to attempt a Firebase user login authentication
    public void LoginUser(String email, String password, FirebaseAuth auth, Context context) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userID(task.getResult().getUser().getUid());

                    Toast.makeText(context, "Successful Log In", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Unsuccessful Log In", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //method to create a user
    public void CreateUser(String email, String password, FirebaseAuth auth, Context context) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Successfully Registered", Toast.LENGTH_LONG).show();

                    //make intent to navigate to main activity
                    //Intent intent = new Intent(context, MainActivity.class);
                    //context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Registration Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //method to query database and see recipe already exists in database based on url
    //if recipe does exists then add this user to access it
    public void CheckForRecipe(String webUrl, Context context) {
        if (webUrl == null){
            Log.d(TAG, "Web url is null");
            return;
        }

        //search through all recipes to see if there is a match
        db.collection(FirestoreRecipe.COLLECTION_PATH).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Intent intent = new Intent(context, recipe_form.class);

                        if (task.isSuccessful()) {
                            //foreach document in the list get results
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("DB objects", document.getId() + " => " + document.getData())
                                // Get recipe as FirestoreRecipe object
                                FirestoreRecipe recipe = document.toObject(FirestoreRecipe.class);

                                //set the id of the recipe
                                recipe.setID(document.getId());

                                //only add the recipe if it belongs to the user
                                if (recipe.urlCheck() && recipe.getUrl().equals(webUrl)) {
                                    Log.d("Recipe", "Recipe found in DB");
                                    intent.putExtra("recipeCheckCallback", recipe);
                                    break;
                                }

                                intent.putExtra("recipeCheckCallback", "does not exist");

                            }

                        } else {
                            Log.w("Database Error", "Error getting documents.", task.getException());
                            intent.putExtra("recipeCheckCallback", "does not exist");
                        }


                        Log.d("Recipe", "Sending recipe to activity");
                        //Navigate back to recipe form
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(intent);
                    }
                });
    }

}


