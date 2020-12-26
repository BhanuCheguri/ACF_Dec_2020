package com.anticorruptionforce.acf.adapters;

import android.content.Context;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anticorruptionforce.acf.R;


class CustomGridAdapter extends BaseAdapter {
    String[] dashboardNames;
    int[] dashboardIcons;
    private static LayoutInflater inflater=null;

    public CustomGridAdapter(FragmentActivity context, String[] dashboard_names, int[] dashboard_icons) {
        dashboardNames = dashboard_names;
        dashboardIcons =dashboard_icons ;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
        return dashboardIcons.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    public class Holder
    {
        TextView grid_text;
        ImageView grid_img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.grid_row_layout, null);
        holder.grid_img = (ImageView) rowView.findViewById(R.id.grid_img);
        holder.grid_text = (TextView) rowView.findViewById(R.id.grid_text);

        holder.grid_text.setText(dashboardNames[position]);
        holder.grid_img.setImageResource(dashboardIcons[position]);



        return rowView;
    }
}
