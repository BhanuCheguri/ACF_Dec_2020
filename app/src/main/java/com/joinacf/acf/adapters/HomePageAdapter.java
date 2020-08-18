package com.joinacf.acf.adapters;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.joinacf.acf.modelclasses.PetitionModel;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.R;
import com.joinacf.acf.utilities.ImageLoader;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.ViewHolder> implements Filterable {
    public ArrayList<WallPostsModel.Result> dataSet;
    Context context;
    private static LayoutInflater inflater = null;
    ImageLoader imageLoader;
    private ArrayList<WallPostsModel.Result> dataList;
    private ArrayList<WallPostsModel.Result> dataListFiltered;

    public HomePageAdapter(Context context, ArrayList<WallPostsModel.Result> data) {
        this.dataSet = data;
        this.context = context;
        this.dataListFiltered = data;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    dataListFiltered = dataSet;
                } else {

                    ArrayList<WallPostsModel.Result> list = new ArrayList<>();

                    for (WallPostsModel.Result result : dataSet) {

                        if (result.getTitle().toLowerCase().contains(charString)) {

                            list.add(result);
                        }
                    }
                    dataListFiltered = list;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                dataListFiltered = (ArrayList<WallPostsModel.Result>) filterResults.values;
                HomePageAdapter.this.notifyDataSetChanged();
            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtLocation;
        TextView txtDescription;
        TextView txtDateTime;
        ImageView imgFilePath;
        LinearLayout linearLayout;
        LinearLayout linearImages;

        public ViewHolder(View rowView) {
            super(rowView);
            this.txtTitle = (TextView) rowView.findViewById(R.id.tv_title);
            this.txtLocation = (TextView) rowView.findViewById(R.id.tv_location);
            this.txtDescription = (TextView) rowView.findViewById(R.id.tv_description);
            this.txtDateTime = (TextView) rowView.findViewById(R.id.tv_DateTime);
            this.imgFilePath = (ImageView) rowView.findViewById(R.id.imgFilePath);
            this.linearLayout = (LinearLayout) rowView.findViewById(R.id.linear);
            this.linearImages = (LinearLayout) rowView.findViewById(R.id.linearImages);
        }
    }

    @Override
    public int getItemCount() {
        return dataListFiltered.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_home_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final WallPostsModel.Result dataModel = dataListFiltered.get(position);
        holder.imgFilePath.setBackgroundResource(R.drawable.rippleeffect);
        holder.txtTitle.setText(dataModel.getTitle());
        holder.txtLocation.setText(dataModel.getLocation());
        holder.txtDateTime.setText(getPostedDate(dataModel.getPostedDate()));
        holder.txtDescription.setText(dataModel.getDescription());

        ArrayList<String> lstFilepaths = new ArrayList<>();
        String strFilePaths = dataModel.getFilePath();
        if (strFilePaths != null && !strFilePaths.equalsIgnoreCase("")) {
            holder.linearImages.setVisibility(View.VISIBLE);
            String[] strFilepathArray = strFilePaths.split(",");
            if (strFilepathArray.length > 0) {
                if (strFilepathArray.length > 1) {

                    holder.linearLayout.setVisibility(View.VISIBLE);
                    if(holder.linearLayout.getChildCount() > 0)
                        holder.linearLayout.removeAllViews();

                    for (int i = 0; i < strFilepathArray.length; i++) {
                        System.out.println("Image URL ::: " + strFilepathArray[i].trim().toString());
                        final String strFile = strFilepathArray[i].trim().toString();
                        if (strFile != null && !strFile.equalsIgnoreCase("")) {
                            lstFilepaths.add(strFilepathArray[i]);
                            final ImageView imageView = new ImageView(context);
                            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(350, 350);
                            imageView.setLayoutParams(parms);
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView.setBackgroundResource(R.drawable.rippleeffect);
                            imageView.setId(i);
                            imageView.setImageResource(R.mipmap.ic_refresh);
                            imageView.setContentDescription(strFilepathArray[i].trim().toString());
                            imageView.setPadding(5, 5, 5, 5);
                            if (i == 0)
                                loadImages(strFile, imageView, holder.linearLayout, true, holder.imgFilePath, strFilepathArray.length);
                            else
                                loadImages(strFile, imageView, holder.linearLayout, false, holder.imgFilePath, strFilepathArray.length);
                            final ImageView img = holder.imgFilePath;

                            final ViewHolder finalHolder1 = holder;
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = imageView.getContentDescription().toString();
                                    loadImagePath(url, finalHolder1.imgFilePath);
                                }
                            });
                        }
                    }
                } else {
                    String strFile = strFilepathArray[0].trim().toString();
                    try {
                        if (strFile != null && !strFile.equalsIgnoreCase("")) {
                            loadImages(strFile, null, holder.linearLayout, true, holder.imgFilePath, strFilepathArray.length);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            } else
                holder.linearImages.setVisibility(View.GONE);
        } else {
            holder.linearImages.setVisibility(View.GONE);
        }

        holder.imgFilePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = v.getContentDescription().toString();
                try {
                    openFile(Uri.parse(content), content, context);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "No Activity found to handle this file. Need to install supported Application", Toast.LENGTH_LONG).show();
                }
            }
        });
        //Toast.makeText(context,"Clicked :" + finalHolder.txtTitle.getText().toString(),Toast.LENGTH_LONG).show();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void loadImagePath(String url, ImageView imgFilePath) {
        String strMimeType = getExtensionType(Uri.parse(url), context);
        imgFilePath.setContentDescription(url);
        if (strMimeType != null && !strMimeType.equalsIgnoreCase("")) {
            if (strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("jpeg") || strMimeType.equalsIgnoreCase("png")) {
                Glide.with(context).load(url).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("mp4")) {
                Glide.with(context).load(R.drawable.mp4).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("application/pdf") || strMimeType.equalsIgnoreCase("pdf")) {
                Glide.with(context).load(R.drawable.pdf).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("docx") || strMimeType.equalsIgnoreCase("doc") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                Glide.with(context).load(R.drawable.doc).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("xlsx") || strMimeType.equalsIgnoreCase("xls") || strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                Glide.with(context).load(R.drawable.excel).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("ppt") || strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                Glide.with(context).load(R.drawable.powerpoint).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("txt") || strMimeType.equalsIgnoreCase("text/plain")) {
                Glide.with(context).load(R.drawable.txt).into(imgFilePath);
            } else if (strMimeType.equalsIgnoreCase("mp3") || strMimeType.equalsIgnoreCase("audio/mpeg")) {
                Glide.with(context).load(R.drawable.mp3).into(imgFilePath);
            }
        }
    }

    public void loadImages(String url, final ImageView img, LinearLayout linearLayout, boolean first, final ImageView imgFilePath, int length) {
        String strMimeType = getExtensionType(Uri.parse(url), context);
        if (strMimeType != null && !strMimeType.equalsIgnoreCase("")) {
            if (strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("jpeg")  || strMimeType.equalsIgnoreCase("png")) {
                //imageLoader.DisplayImage(url, img);
                if (first) {
                    imgFilePath.setContentDescription(url);
                    //imageLoader.DisplayImage(url, imgFilePath);
                    Glide.with(context).load(url).into(imgFilePath);
                }else
                    Glide.with(context).load(url).override(600, 300).into(img);

                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(url).override(600, 300).into(img);
                }
            } else if (strMimeType.equalsIgnoreCase("mp4")) {
                if (first) {
                    Glide.with(context).load(R.drawable.mp4).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(R.drawable.mp4).override(600, 300).into(img);
                }
            } else if (strMimeType.equalsIgnoreCase("application/pdf") || strMimeType.equalsIgnoreCase("pdf")) {
                if (first) {
                    Glide.with(context).load(R.drawable.pdf).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(R.drawable.pdf).override(600, 300).into(img);
                }
            } else if (strMimeType.equalsIgnoreCase("docx") || strMimeType.equalsIgnoreCase("doc") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                if (first) {
                    Glide.with(context).load(R.drawable.doc).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    Glide.with(context).load(R.drawable.doc).override(600, 300).into(img);
                    linearLayout.setVisibility(View.VISIBLE);
                }
            } else if (strMimeType.equalsIgnoreCase("xlsx") || strMimeType.equalsIgnoreCase("xls") || strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                if (first) {
                    Glide.with(context).load(R.drawable.excel).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(R.drawable.excel).override(600, 300).into(img);
                }
            } else if (strMimeType.equalsIgnoreCase("ppt") || strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                if (first) {
                    Glide.with(context).load(R.drawable.powerpoint).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(R.drawable.powerpoint).override(600, 300).into(img);
                }
            } else if (strMimeType.equalsIgnoreCase("txt") || strMimeType.equalsIgnoreCase("text/plain")) {
                if (first) {
                    Glide.with(context).load(R.drawable.txt).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(R.drawable.txt).override(600, 300).into(img);
                }
            } else if (strMimeType.equalsIgnoreCase("mp3") || strMimeType.equalsIgnoreCase("audio/mpeg")) {
                if (first) {
                    Glide.with(context).load(R.drawable.mp3).into(imgFilePath);
                    imgFilePath.setContentDescription(url);
                }
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                    Glide.with(context).load(R.drawable.mp3).override(600, 300).into(img);
                }
            }
            if (linearLayout != null) {
                linearLayout.addView(img);
                if (length == 1)
                    linearLayout.setVisibility(View.GONE);
                else {
                    linearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void openFile(Uri uri, String url, Context ctx) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            // Other files
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }


    public static String getExtensionType(Uri contentURI, Context context) {
        String extension;
        if (contentURI.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(contentURI));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(contentURI.getPath())).toString());
        }
        return extension;
    }

    public boolean isImageExists(String url) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            System.out.println(" code :: " + con.getResponseCode());
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private String getPostedDate(String postedDate) {
        if (postedDate.contains("T")) {
            String[] strDateZ = null;
            String date = "";
            Duration diff = null;

            String[] strPostedDate = postedDate.split("T");
            String strDate = strPostedDate[0] + " " + strPostedDate[1];
            if (strDate.contains("Z")) {
                strDateZ = strDate.split("Z");
                date = strDateZ[0];
            }
            try {
                if (strDateZ != null && !date.equalsIgnoreCase("")) {
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


                        if (mm <= 60 && hh != 0) {
                            //if (hh <= 60 && day != 0) {
                            if (hh <= 60 && day != 0) {
                                if (day > 5) {
                                    spf = new SimpleDateFormat("dd MMM yyyy");
                                    date = spf.format(oldDate);
                                    return date;
                                } else
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return postedDate;
    }
}
