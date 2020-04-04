package com.joinacf.acf.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joinacf.acf.R;


public class ImageListAdapter extends BaseAdapter
{
    private Context context;
    private Integer[] imgData;
    private String[] mData;
    private static LayoutInflater inflater=null;


    public ImageListAdapter(Context c, Integer[] mThumbIds, String[] mNames) {
        context = c;
        mData = mNames;
        imgData = mThumbIds;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if(mData != null)
            return mData.length;
        else
            return 0;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }


    public class Holder
    {
        ImageView grid_img;
        TextView grid_text;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.grid_row_without_cardview, null);
        holder.grid_img = (ImageView) rowView.findViewById(R.id.grid_img);
        holder.grid_text = (TextView) rowView.findViewById(R.id.grid_text);

        holder.grid_text.setText(mData[position]);
        holder.grid_img.setImageResource(imgData[position]);

        return rowView;
    }
}