package com.example.testrecipeadder.Utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;

import com.example.testrecipeadder.Firestore.FirebaseQueries;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//class for various methods used through the program
public class Utils {

    //for the Jsoup Tag selectors Map
    private Map<String, Map<String, String>> siteClassData;
    public Map<String, Map<String, String>> getSiteData(){return siteClassData;}

    FirebaseQueries fb = new FirebaseQueries();

    public Utils(){
        InitSiteClassData();
    }

    //returns the location of where a temp file has been downloaded
    public String TempFileLocator(String filename)
    {
        return System.getProperty("java.io.tmpdir")+ filename;
    }

    public void GetImage(String imgName, ImageView imgView){
        //Check first if the image exists local storage
        File localImage = new File(TempFileLocator(imgName));
        if (localImage.exists())
        {
            Bitmap imgBitmap = BitmapFactory.decodeFile(localImage.getAbsolutePath());
            imgView.setImageBitmap(imgBitmap);
        }
        //otherwise download image from database
        else
            fb.DownloadImageTemp(imgName, imgView);
    }

    //returns true if email supplied is valid
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    //returns true if password supplied is valid. more than 5 characters
    public boolean isValidPassword(CharSequence pass) {
        return pass.length() > 5;
    }


    //generate the maps that hold html class element information for BBCGoodFood and Food.com
    public void InitSiteClassData() {
        Map<String, String> foodSiteClasses = new HashMap<String, String>() {{
            put("Title", "recipe-title");
            put("Ingredients", ".recipe-ingredients__item");
            put("Steps", ".recipe-directions__step");
            put("Image", "meta[name='og:image']");
        }};

        Map<String, String> bbcFoodSiteClasses = new HashMap<String, String>() {{
            put("Title", "post-header__title");
            put("Ingredients", ".recipe__ingredients ul li");
            put("Steps", ".recipe__method-steps div ul li div p");
            put("Image", ".post-header__image .image__picture");
        }};

        //create a collection of maps
        siteClassData = new HashMap<String, Map<String, String>>() {{
            put("food", foodSiteClasses);
            put("bbcgoodfood", bbcFoodSiteClasses);
        }};

    }


}
