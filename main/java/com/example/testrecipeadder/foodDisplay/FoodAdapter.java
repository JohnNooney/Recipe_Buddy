package com.example.testrecipeadder.foodDisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testrecipeadder.Firestore.FirebaseQueries;
import com.example.testrecipeadder.Firestore.FirestoreRecipe;
import com.example.testrecipeadder.R;
import com.example.testrecipeadder.Utils.Utils;

import java.util.ArrayList;

public class FoodAdapter extends ArrayAdapter<FirestoreRecipe> {

    Context mContext;
    int mLayoutResourceId;
    ArrayList<FirestoreRecipe> mData = null;
    FirebaseQueries fb = new FirebaseQueries();
    Utils utils = new Utils();

    public FoodAdapter(Context context, int resource, ArrayList<FirestoreRecipe> data) {
        super(context, resource, data);

        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mData = data;
    }

    @Override
    public FirestoreRecipe getItem(int position) {
        return super.getItem(position);
    }


    //gets called for every item in the ListView
    //with increased efficiency by only loading in the list items when
    //the appear on the screen
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PlaceHolder holder = null;

        //if we dont have a row view to reuse..
        if (row == null)
        {
            //inflate the layout for a single row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mLayoutResourceId, parent, false);

            holder = new PlaceHolder();

            //get reference to the different view elements to update
            //and add them to the temp holder
            holder.nameView = (TextView) row.findViewById(R.id.nameTextView);
            holder.imageView = (ImageView) row.findViewById(R.id.imageView);

            //allow to set ref to holder class
            row.setTag(holder);
        }else{
            //otherwise overwrite an existing row view.
            holder = (PlaceHolder) row.getTag();
        }


        //get the data from the data array
        FirestoreRecipe food = mData.get(position);

        //setup and reuse the same listener for each row
        //holder.imageView.setOnClickListener(PopupListener);
       // Integer rowPosition = position;
        //holder.imageView.setTag(rowPosition);

        //setting the view to reflect the data we need to display
        holder.nameView.setText(food.getName());

        //download image file from the database and retrieve its file location from tmpdir
        fb.DownloadImage(food.getImage(), holder.imageView);
        //String imgPath = fb.DownloadImage(food.getImage(), holder.imageView);

        //int resId = mContext.getResources().getIdentifier(food.getImage().toLowerCase(), "drawable",mContext.getPackageName());
        //holder.imageView.setImageResource(resId);

        //returning the row view (because this is called getView)
        return row;
    }

    //add on click listener for each recipe
//    View.OnClickListener PopupListener = new View.OnClickListener(){
//        @Override
//        public void onClick(View view) {
//            Integer viewPosition = (Integer) view.getTag();
//            FirestoreRecipe p = mData.get(viewPosition);
//            Toast.makeText(getContext(),viewPosition.toString(), Toast.LENGTH_SHORT).show();
//        }
//    };

    private static class PlaceHolder{
        TextView nameView;
        ImageView imageView;
    }
}
