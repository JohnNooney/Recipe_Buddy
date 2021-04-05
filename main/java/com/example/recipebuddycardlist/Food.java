package com.example.recipebuddycardlist;

//Data class to hold information about food items in ListView
public class Food {
    public String mNameOfFood;
    public String mNameOfImage;
    public String mPopup;

    public Food(String startNameOfFood, String startNameOfImage, String popUp)
    {
        this.mNameOfFood = startNameOfFood;
        this.mNameOfImage = startNameOfImage;
        this.mPopup = popUp;
    }

}
