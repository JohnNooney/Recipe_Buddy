package com.example.recipebuddycardlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FoodAdapter extends ArrayAdapter<Food> {

    Context mContext;
    int mLayoutResourceId;
    Food mData[] = null;

    public FoodAdapter(Context context, int resource, Food[] data) {
        super(context, resource, data);
        this.mContext = context;
        this.mLayoutResourceId = resource;
        this.mData = data;
    }

    @Override
    public Food getItem(int position) {
        return super.getItem(position);
    }


    //gets called for every item in the ListView
    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        View row = convertView;

        //inflate the layout for a single row
        LayoutInflater inflater = LayoutInflater.from(mContext);
        row = inflater.inflate(mLayoutResourceId, parent, false);

        //get reference to the different view elements we wish to update
        TextView nameView = (TextView) row.findViewById(R.id.nameTextView);
        ImageView imageView = (ImageView) row.findViewById(R.id.imageView);

        //get the data from the data array
        Food food = mData[position];

        //setting the view to reflect the data we need to display
        nameView.setText(food.mNameOfFood);

        int resId = mContext.getResources().getIdentifier(food.mNameOfImage.toLowerCase(), "drawable",mContext.getPackageName());
        imageView.setImageResource(resId);

        //returning the row view (because th8is is called getView after all)
        return row;
    }
}
