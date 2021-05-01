package com.example.testrecipeadder.Firestore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.testrecipeadder.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;


public class FirebaseQueries {
    private StorageReference sref = FirebaseStorage.getInstance().getReference();
    private FirebaseStorage dbStorage = FirebaseStorage.getInstance("gs://recipebuddytest.appspot.com");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference myRef;
    private static String TAG = "DB";
    private Utils utils = new Utils();

    public void addRecipeToFirebase(Map<String, Object> recipeMap)
    {
        /* Add a new document with a generated ID (name) */
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

    //retrieve recipe from the db based on id and fill in the necessary views with that information
    public void GetSingleRecipe(String id, TextView recipeName, TextView recipeIngredients, TextView recipeDirections, ImageView image)
    {

        DocumentReference recipe = db.collection(FirestoreRecipe.COLLECTION_PATH).document(id);

        //Query database for recipe
        recipe.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        //turn document into recipe object
                        FirestoreRecipe fullRecipe = doc.toObject(FirestoreRecipe.class);

                        Log.d(TAG, "Recipe found");

                        //set the text into the textViews
                        recipeName.setText(fullRecipe.getName());
                        recipeIngredients.setText(fullRecipe.getIngredients());
                        recipeDirections.setText(fullRecipe.getDirections());

                        //set the image
                        DownloadImage(fullRecipe.getImage(), image);
                    }
                }
                else
                    Log.e(TAG, "Recipe not found");
            }
        });
    }


    public void DeleteRecipe(String id)
    {
        //delete document based on ID
        db.collection(FirestoreRecipe.COLLECTION_PATH).document(id).delete()
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

    public void UploadImage(ImageView image, String imgName)
    {
        if(image != null){
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
    public void DownloadImage(String imgName, ImageView image)
    {
        //make sure image exists before trying to download it
        sref.child("RecipeImages/" + imgName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //get the references to the folder where the image is
                StorageReference imagesRef = sref.child("RecipeImages/" + imgName);

                //Log.d("File Path", localFile.getAbsolutePath());
                imagesRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d("DB Image", "Image has been downloaded successfully");
                        Bitmap imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        image.setImageBitmap(imgBitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DB Image", "Image unable to downloaded");
                    }
                });
            }
        });


        //return the location of the temp image
        //return localFile.getAbsolutePath();
    }
}


