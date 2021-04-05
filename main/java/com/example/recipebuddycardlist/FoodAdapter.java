package com.example.recipebuddycardlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Placeholder;

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
        PlaceHolder holder = null;

        //if we dont have a row view to reuse..
        if (row == null)
        {
            //inflate the layout for a single row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(mLayoutResourceId, parent, false);

            holder = new PlaceHolder();

            //get reference to the different view elements we wish to update
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
        Food food = mData[position];

        //setup and reuse the same listener for each row
        holder.imageView.setOnClickListener(PopupListener);
        Integer rowPosition = position;
        holder.imageView.setTag(rowPosition);

        //setting the view to reflect the data we need to display
        holder.nameView.setText(food.mNameOfFood);

        int resId = mContext.getResources().getIdentifier(food.mNameOfImage.toLowerCase(), "drawable",mContext.getPackageName());
        holder.imageView.setImageResource(resId);

        //returning the row view (because th8is is called getView after all)
        return row;
    }

    View.OnClickListener PopupListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Integer viewPosition = (Integer) view.getTag();
            Food p = mData[viewPosition];
            Toast.makeText(getContext(),p.mPopup, Toast.LENGTH_SHORT).show();
        }
    };

    private static class PlaceHolder{
        TextView nameView;
        ImageView imageView;
    }
}
