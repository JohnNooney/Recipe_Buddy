package com.example.testrecipeadder.Utils;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    //for the Jsoup Tag selectors Map
    Map<String, Map<String, String>> siteClassData;
    public Map<String, Map<String, String>> getSiteData(){return siteClassData;}

    public Utils(){
        InitSiteClassData();
    }

    //returns the location of where a temp file has been downloaded
    public String TempFileLocator(String filename)
    {
        return System.getProperty("java.io.tmpdir")+ filename;
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
