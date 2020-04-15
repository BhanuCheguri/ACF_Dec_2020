package com.joinacf.acf.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HomePageAdapter  extends ArrayAdapter<WallPostsModel> {
    public ArrayList<WallPostsModel> dataSet;
    Activity context;
    private static LayoutInflater inflater=null;
    ArrayList<WallPostsModel> filteredList;
    ValueFilter valueFilter;

    public HomePageAdapter(Activity context, ArrayList<WallPostsModel> data) {
        super(context, R.layout.custom_home_layout, data);
        this.dataSet = data;
        this.context = context;
        this.filteredList = data;
    }

    public class ViewHolder
    {
        TextView txtTitle;
        TextView txtLocation;
        TextView txtDescription;
        TextView txtDateTime;
        ImageView imgFilePath;
        LinearLayout linearLayout;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null){
            valueFilter  = new ValueFilter();
        }
        return valueFilter;
    }


    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public WallPostsModel getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null; // view lookup cache stored in tag
        View rowView = null;

        WallPostsModel dataModel = (WallPostsModel) getItem(position);

        holder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        rowView = inflater.inflate(R.layout.custom_home_layout, null);

        holder.txtTitle = (TextView) rowView.findViewById(R.id.tv_title);
        holder.txtLocation = (TextView) rowView.findViewById(R.id.tv_location);
        holder.txtDescription = (TextView) rowView.findViewById(R.id.tv_description);
        holder.txtDateTime = (TextView) rowView.findViewById(R.id.tv_DateTime);
        holder.imgFilePath = (ImageView)rowView.findViewById(R.id.imgFilePath);
        holder.linearLayout = (LinearLayout)rowView.findViewById(R.id.linear);

        holder.imgFilePath.setBackgroundResource(R.drawable.rippleeffect);
        holder.txtTitle.setText(dataModel.getTitle());
        holder.txtLocation.setText(dataModel.getLocation());
        holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));
        holder.txtDescription.setText(dataModel.getDescription());

        ArrayList<String> lstFilepaths = new ArrayList<>();
        String strFilePaths = dataModel.getFilePath();
        if(strFilePaths != null && !strFilePaths.equalsIgnoreCase(""))
        {
            String[] strFilepathArray = strFilePaths.split(",");
            if(strFilepathArray.length > 0) {
                holder.imgFilePath.setVisibility(View.VISIBLE);
                for (int i = 0; i < strFilepathArray.length; i++) {

                    System.out.println("Image URL ::: "+strFilepathArray[i].trim().toString());
                    String strFile = strFilepathArray[i].trim().toString();
                    lstFilepaths.add(strFilepathArray[i]);
                    final ImageView imageView = new ImageView(context);
                    imageView.setBackgroundResource(R.drawable.rippleeffect);
                    imageView.setId(i);
                    imageView.setContentDescription(strFilepathArray[i].trim().toString());
                    imageView.setPadding(5, 5, 5, 5);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    //LinearLayout.MarginLayoutParams mlp = (LinearLayout.MarginLayoutParams)imageView.getLayoutParams();
                    //mlp.setMargins(5, 0, 5, 0);
                    Glide.with(context).load(strFile).override(500, 500).into(imageView);
                    holder.linearLayout.addView(imageView);
                    final ImageView img = holder.imgFilePath;
                    Glide.with(context).load(strFilepathArray[0].trim().toString()).into(img);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = imageView.getContentDescription().toString();
                            Glide.with(context).load(url).into(img);
                        }
                    });
                }
            }
            else
                holder.imgFilePath.setVisibility(View.GONE);
        }else
            holder.imgFilePath.setVisibility(View.GONE);


        final ViewHolder finalHolder = holder;

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Clicked :" + finalHolder.txtTitle.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });

        return rowView;
    }

    //Date date1 = simpleDateFormat.parse("10/10/2013 11:30:10");
   // Date date2 = simpleDateFormat.parse("13/10/2013 20:35:55");

    private String getPostedDate(String postedDate)
    {
        if(postedDate.contains("T"))
        {
            String[] strDateZ = null;
            String date ="";
            Duration diff = null;

            String[] strPostedDate = postedDate.split("T");
            String strDate = strPostedDate[0] + " " + strPostedDate[1];
            if(strDate.contains("Z"))
            {
                strDateZ = strDate.split("Z");
                date = strDateZ[0];
            }
            try {
                if(strDateZ != null  && !date.equalsIgnoreCase("")) {
                    SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd hh:mm:sss");
                    try {

                        Date oldDate = spf.parse(date);
                        System.out.println(oldDate);

                        int day = 0;
                        int hh = 0;
                        int mm = 0;
                        Date currentDate = new Date();
                        Long timeDiff = currentDate.getTime() - oldDate.getTime();
                        day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
                        hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
                        mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));


                        if (mm <= 60 && hh!= 0) {
                            //if (hh <= 60 && day != 0) {
                            if (hh <= 60 && day != 0) {
                                if(day > 5){
                                    spf = new SimpleDateFormat("dd MMM yyyy");
                                    date = spf.format(oldDate);
                                    return date;
                                }
                                else
                                    return day + " Days";
                            } else {
                                return hh + " Hrs";
                            }
                        } else {
                            return mm + " Min";
                        }

                        // Log.e("toyBornTime", "" + toyBornTime);

                    } catch (ParseException e) {

                        e.printStackTrace();
                    }

                }
                return date;
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return postedDate;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<WallPostsModel> filterList = new ArrayList<>();
                String searchStr = constraint.toString();

                for(WallPostsModel itemsModel:dataSet){
                    if(/*itemsModel.getDescription().contains(searchStr) ||*/ itemsModel.getTitle().toLowerCase().contains(searchStr)){
                        filterList.add(itemsModel);
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                }
            } else {
                //synchronized(this) {
                    results.count = dataSet.size();
                    results.values = dataSet;
               // }
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            /*filteredList = (ArrayList<WallPostsModel>)results.values;
            notifyDataSetChanged();
            clear();
            for(int i = 0, l = filteredList.size(); i < l; i++)
                add(filteredList.get(i));
            notifyDataSetInvalidated();*/

            filteredList = (ArrayList<WallPostsModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
