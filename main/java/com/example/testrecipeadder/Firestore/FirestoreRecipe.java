package com.example.testrecipeadder.Firestore;

public class FirestoreRecipe {

    public static final String COLLECTION_PATH = "recipes";
    public static final String KEY_URL = "url";
    public static final String KEY_NAME = "name";
    public static final String KEY_DIRECTIONS= "directions";
    public static final String KEY_INGREDIENTS= "ingredients";
    public static final String KEY_IMAGE = "image";

    private String url;
    private String name;
    private String ingredients;
    private String directions;
    private String image;
    private String id;

    public FirestoreRecipe(){}

    public String getUrl(){return url;}
    public String getName(){return name;}
    public String getDirections(){return directions;}
    public String getIngredients(){return ingredients;}
    public String getImage(){return image;}
    public String getID(){return id;}
    public void setID(String id){this.id = id;}

    @Override
    public String toString(){
        return String.format("id: %s; url %s; name %s; ingredients %s; directions %s; image: %s", id, url, name, ingredients, directions, image);
    }
}
