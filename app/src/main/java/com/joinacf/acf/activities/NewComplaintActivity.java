package com.joinacf.acf.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import org.apache.commons.io.FileUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.joinacf.acf.R;
import com.joinacf.acf.adapters.ImageListAdapter;
import com.joinacf.acf.databinding.ActivityNewComplaintBinding;
import com.joinacf.acf.modelclasses.DashboardCategories;
import com.joinacf.acf.modelclasses.NewComplaintDataRequest;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.network.AppLocationService;
import com.joinacf.acf.modelclasses.NewComplaintModel;
import com.joinacf.acf.utilities.App;
import com.joinacf.acf.utilities.Utility;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class NewComplaintActivity extends BaseActivity /*implements View.OnFocusChangeListener*/ {

    private static Uri contentUri = null;
    public static String TAG = "NewComplaintActivity.Class";
    ActivityNewComplaintBinding binding;
    //String[] Names = {"Corruption", "Adulteration", "Social Evil", "Find and Fix"};
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList<String> tImagesPath = new ArrayList<String>();
    private int GALLERY = 1, CAMERA = 2;
    private int VIDEO_GALLERY = 3, VIDEO_CAMERA = 4;
    private int PICKFILE_RESULT_CODE = 5;
    private int AUDIO = 6;
    private int LOCATION = 7;
    public static final String complaint_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ACF/";

    NewComplaintModel model;
    ArrayList<NewComplaintModel> lstModelData;
    AppLocationService appLocationService;
    private APIRetrofitClient apiRetrofitClient;
    private FusedLocationProviderClient locationProviderClient;
    private Geocoder geocoder;
    private double latitude,longitude;
    private List<Address> addresses;
    private int locationRequestCode = 1000;
    String strTitle, strDescription,strLocation;
    private int strResult = -1;
    private int category = -1;
    String currentTime= "";
    ArrayList<DashboardCategories.Result> lstCatagories;
    ArrayList<String> lstCatagoriesNames;
    ArrayList<String> lstPathURI;
    ArrayList<Uri> lstPathUri;
    HashMap<String,String> hshMapCategoryLst ;
    String strResponse = "";
    CustomImageAdapter customImageAdapter;
    Bundle b;
    ArrayList<Integer> lstAutoEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(getString(R.string.title_new_Item));


        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_complaint);
        init();
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        locationRequestCode);
            } else {
                 getLocation();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getLocation()
    {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(NewComplaintActivity.this, Locale.getDefault());
        locationProviderClient.getLastLocation().addOnSuccessListener(NewComplaintActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if(addresses.size() > 0) {
                            String addressLine1 = addresses.get(0).getAddressLine(0);
                            Log.e("line1", addressLine1);
                            String city = addresses.get(0).getLocality();
                            Log.e("city", city);
                            String state = addresses.get(0).getAdminArea();
                            Log.e("state", state);
                            String pinCode = addresses.get(0).getPostalCode();
                            Log.e("pinCode", pinCode);

                            String fullAddress = addressLine1 + ",  " + city + ",  " + state + ",  " + pinCode;
                            binding.currentLocation.setText(addressLine1);
                            strLocation = addressLine1;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("MainActivity", e.getMessage());
                    }

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void init() {
        apiRetrofitClient  = new APIRetrofitClient();
        lstModelData = new ArrayList<>();
        lstCatagories = new ArrayList<DashboardCategories.Result>();
        lstCatagoriesNames = new ArrayList<>();
        hshMapCategoryLst = new HashMap<>();
        lstPathURI = new ArrayList<>();
        lstPathUri = new ArrayList<>();
        lstAutoEmpty = new ArrayList<>();
        appLocationService = new AppLocationService(NewComplaintActivity.this);

        currentTime = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
        //binding.currentDate.setText(currentTime);
        showProgressDialog(NewComplaintActivity.this);
        if(App.isNetworkAvailable())
            getDashboardCategories();
        else{
            ChocoBar.builder().setView(binding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }

        b = getIntent().getExtras();
        if(b != null){
            if(!b.getString("Category").equalsIgnoreCase("") && b.getString("Category") != null) {
                binding.spinner.setText(b.getString("Category"));
                binding.etTitle.requestFocus();
            }
        }


        binding.spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String categoryID = hshMapCategoryLst.get(lstCatagoriesNames.get(i).toString());
                category = Integer.valueOf(categoryID);
            }
        });

        binding.fabDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lstModelData.size() < 10) {
                    showAttachementDialog();
                }else
                {
                    Toast.makeText(NewComplaintActivity.this,getString(R.string.error_max_limit_exceeds),Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strTitle = binding.etTitle.getText().toString();
                strDescription = binding.etDescription.getText().toString();
                SubmitDialog();
            }
        });


    }


    private void getDashboardCategories() {
        try {
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Call<DashboardCategories> call = api.getDashboardCategories();

            call.enqueue(new Callback<DashboardCategories>() {
                @Override
                public void onResponse(Call<DashboardCategories> call, Response<DashboardCategories> response) {
                    DashboardCategories myProfileData = response.body();
                    hideProgressDialog(NewComplaintActivity.this);
                    if(myProfileData != null) {
                        String status = myProfileData.getStatus();
                        String msg = myProfileData.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstCatagories = myProfileData.getResult();
                        }
                    }
                    loadCategoriesData(lstCatagories);
                }

                @Override
                public void onFailure(Call<DashboardCategories> call, Throwable t) {
                    Crashlytics.logException(t);
                    hideProgressDialog(NewComplaintActivity.this);
                    Toast.makeText(NewComplaintActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    showAlert(NewComplaintActivity.this,"Error","Unable to get the Categories list","OK");
                }
            });

        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }

    private void loadCategoriesData(ArrayList<DashboardCategories.Result> lstData) {
        if(lstData.size() > 0 ) {
            for (int i = 0; i < lstData.size(); i++) {
                lstCatagoriesNames.add(lstData.get(i).getName().toString().trim());
                hshMapCategoryLst.put(lstData.get(i).getName().toString().trim(), lstData.get(i).getCategoryID().toString().trim());
            }

            if(!b.getString("Category").equalsIgnoreCase("") && b.getString("Category") != null){
                String categoryID = hshMapCategoryLst.get(b.getString("Category"));
                category = Integer.valueOf(categoryID);
            }

            ArrayAdapter adapter = new ArrayAdapter(this,R.layout.custom_textview_layout,R.id.text,lstCatagoriesNames);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
            binding.spinner.setAdapter(adapter);
            binding.spinner.setThreshold(1);
            binding.spinner.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    binding.spinner.showDropDown();
                    return false;
                }
            });
        }else
            showAlert(NewComplaintActivity.this,"Error","Unable to get the Categories list","OK");

    }

    private void showAttachementDialog() {

        Integer[] mThumbIds = {
                R.drawable.camera, R.drawable.video_camera,
                R.drawable.document,R.drawable.audio
        };
        String[] mNames= {"Camera","Video","Document","Audio"};
        List<Integer>  mList = new ArrayList<Integer>();
        for (int i = 1; i < 6; i++) {
            mList.add(i);
        }

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.grid_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        GridView gridView = (GridView) dialog.findViewById(R.id.gridView);
        gridView.setAdapter(new ImageListAdapter(this,mThumbIds,mNames));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();
                switch (position)
                {
                    case 0:
                        try {
                            requestMultiplePermissions();
                            //selectImage();
                            takePhotoFromCamera();
                        }catch(ActivityNotFoundException anfe){
                            String errorMessage = "Whoops – your device doesn’t support capturing images!";
                            Toast toast = Toast.makeText(NewComplaintActivity.this, errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    break;
                    case 1:
                        try {
                            requestMultiplePermissions();
                            takeVideoFromCamera();
                        }catch(ActivityNotFoundException anfe){
                            String errorMessage = "Whoops – your device doesn’t support capturing videos!";
                            Toast toast = Toast.makeText(NewComplaintActivity.this, errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        break;

                    case 2:

                        String[] mimeTypes =
                                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                                        "text/plain",
                                        "application/pdf"};

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
                            if (mimeTypes.length > 0) {
                                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                            }
                        } else {
                            String mimeTypesStr = "";
                            for (String mimeType : mimeTypes) {
                                mimeTypesStr += mimeType + "|";
                            }
                            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
                        }
                        startActivityForResult(intent,PICKFILE_RESULT_CODE);
                        break;

                    case 3:

                        Intent audio_intent = new Intent();
                        audio_intent.setType("audio/*");
                        audio_intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(audio_intent,PICKFILE_RESULT_CODE);

                        break;

                }
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(NewComplaintActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utility.checkPermission(NewComplaintActivity.this);
                if (items[item].equals("Take Photo")) {
                    if(result)
                        takePhotoFromCamera();
                } else if (items[item].equals("Choose from Library")) {
                    if(result)
                        choosePhotoFromGallary();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void choosePhotoFromGallary() {
        /*Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY);*/

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }


    private void takeVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_CAMERA);
    }


    @SuppressLint("LongLogTag")
    public void saveFile(Context context, Bitmap b, String picName){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "file not found");
            e.printStackTrace();
        }
        finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            Uri contentURI = data.getData();
            try {
                if (data != null) {
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    String path = getPathFromUri(NewComplaintActivity.this,contentURI);

                    Cursor cursor = getContentResolver().query(contentURI, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bm = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    //Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));
                    cursor.close();
                    File file = new File(contentURI.getPath().toString());
                    Log.d("", "File : " + file.getName());
                    StringTokenizer tokens = new StringTokenizer(file.getName(), ":");
                    String first = tokens.nextToken();
                    String file_1 = tokens.nextToken().trim();
                    setAttachmentData(path,path,contentURI,getExtensionType(contentURI),bm);

                }
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CAMERA) {
            try {
                Uri contentURI = data.getData();
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                String imgcurTime = dateFormat.format(new Date());
                saveFile(NewComplaintActivity.this,photo,imgcurTime + ".jpg");
                String dir = getFilesDir().getAbsolutePath();
                String imagePath = dir + "/"+imgcurTime + ".jpg";
                File file = new File(imagePath);
                if(file.exists())
                {
                    FileOutputStream out = new FileOutputStream(imagePath);
                    photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();

                    //Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                    setAttachmentData(imagePath,imagePath,contentURI,"jpg",bitmap);
                }else
                    Toast.makeText(this, "Not able to create image path", Toast.LENGTH_SHORT).show();


            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }

        }
       else if (requestCode == VIDEO_CAMERA) {
            Uri contentURI = data.getData();
            try {
                String recordedVideoPath = getPath(contentURI);
                Log.d("frrr", recordedVideoPath);

                String[] filePath = {MediaStore.Video.Media.DATA};
                Cursor cursor = getContentResolver().query(contentURI, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                //Uri uri = FileProvider.getUriForFile(NewComplaintActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));

                setAttachmentData(recordedVideoPath,imagePath,contentURI,getExtensionType(contentURI),null);

            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }

        }else if(requestCode == PICKFILE_RESULT_CODE) {
            Uri contentURI = data.getData();

            try {
                String uriString = contentURI.toString();
                File myFile = new File(uriString);
                //String path = myFile.getAbsolutePath();
                String path = getPathFromUri(NewComplaintActivity.this,contentURI);
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtensionType(contentURI));

                setAttachmentData(path,path,contentURI,mimeType,null);

            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == AUDIO) {
            Uri contentURI = data.getData();
            try {
                String uriString = contentURI.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();

                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtensionType(contentURI));

                setAttachmentData(path,path,contentURI,mimeType,null);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(NewComplaintActivity.this, "Attachment Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setAttachmentData(String Content, String FilePath, Uri contentURI, String mimeType,Bitmap bm) {

        model = new NewComplaintModel();
        model.setContent(Content);
        model.setFilePath(FilePath);
        model.setUri(contentURI);
        model.setExtensionType(mimeType);
        model.setBitmap(bm);
        lstModelData.add(model);

        lstPathURI.add(Content);
        lstPathUri.add(contentURI);
        customImageAdapter = new CustomImageAdapter(this, lstModelData);
        binding.grid.setAdapter(customImageAdapter);
    }


    public String getExtensionType(Uri contentURI)
    {
        String extension = "";
        if(contentURI != null) {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(contentURI));
            System.out.print("extension ::" + extension);
        }

        return extension;
    }


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

   /* @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus) {
            //do job here owhen Edittext lose focus
            if(v instanceof EditText) {
                if (!((EditText)v).getText().equals("")) {
                    if(lstAutoEmpty.contains(v.getId()))
                    {
                        lstAutoEmpty.remove(v.getId());
                    }
                }else{
                    if(!lstAutoEmpty.contains(v.getId()))
                    {
                        lstAutoEmpty.add(v.getId());
                    }
                }
                if(lstAutoEmpty.size() < 0)
                {
                    binding.btnSubmit.setVisibility(View.VISIBLE);
                }
            }else if(v instanceof AutoCompleteTextView)
            {
                if (!((AutoCompleteTextView)v).getText().equals("")) {
                    if(lstAutoEmpty.contains(v.getId()))
                    {
                        lstAutoEmpty.remove(v.getId());
                    }
                }else{
                    if(!lstAutoEmpty.contains(v.getId()))
                    {
                        lstAutoEmpty.add(v.getId());
                    }
                }
                if(lstAutoEmpty.size() < 0)
                {
                    binding.btnSubmit.setVisibility(View.VISIBLE);
                }
            }
        }
    }*/

    public class CustomImageAdapter extends BaseAdapter
    {
        private Context context;
        private ArrayList<NewComplaintModel> lstModelData;
        private LayoutInflater inflater=null;
        //Bitmap bm = null;

        public CustomImageAdapter(NewComplaintActivity c, ArrayList<NewComplaintModel> lstData) {
            context = c;
            lstModelData = lstData;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void clear(){
            lstModelData.clear();
            this.notifyDataSetChanged();
        }

        public int getCount() {
            if(lstModelData != null)
                return lstModelData.size();
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
            ImageView grid_close;
            ImageView grid_img;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder = new Holder();
            View rowView;

            rowView = inflater.inflate(R.layout.new_complaint_grid_row_layout, null);
            holder.grid_img = (ImageView) rowView.findViewById(R.id.grid_img);


            final String strMimeType = lstModelData.get(position).getExtensionType();
            if(strMimeType.equalsIgnoreCase("jpg") || strMimeType.equalsIgnoreCase("png")) {
                if(lstModelData.get(position).getBitmap() !=null) {
                    holder.grid_img.setImageBitmap(lstModelData.get(position).getBitmap());
                    holder.grid_img.setId(position);
                }
            }
            else if(strMimeType.equalsIgnoreCase("mp4")) {
                    holder.grid_img.setImageResource(R.drawable.mp4);
                    holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/pdf")) {
                holder.grid_img.setImageResource(R.drawable.pdf);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || strMimeType.equalsIgnoreCase("application/msword")) {
                holder.grid_img.setImageResource(R.drawable.doc);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/vnd.ms-excel") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                holder.grid_img.setImageResource(R.drawable.excel);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("application/vnd.ms-powerpoint") || strMimeType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                holder.grid_img.setImageResource(R.drawable.powerpoint);
                holder.grid_img.setId(position);
            }
            else if(strMimeType.equalsIgnoreCase("text/plain")) {
                holder.grid_img.setImageResource(R.drawable.txt);
                holder.grid_img.setId(position);
            }else if(strMimeType.equalsIgnoreCase("audio/mpeg")) {
                holder.grid_img.setImageResource(R.drawable.mp3);
                holder.grid_img.setId(position);
            }
            else
            {
                Toast.makeText(context,"This attachment doesn't supported by ACF" ,Toast.LENGTH_LONG).show();
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        
                        if(!strMimeType.equalsIgnoreCase("mp4") && !strMimeType.equalsIgnoreCase("jpg") && !strMimeType.equalsIgnoreCase("png")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(lstModelData.get(position).getUri(), strMimeType);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                        else
                        {
                            if(strMimeType.equalsIgnoreCase("mp4"))
                            {
                                Intent intent = new Intent(context,ViewMediaActivity.class);
                                intent.putExtra("FilePath",lstModelData.get(position).getUri().toString());
                                intent.putExtra("FileType","mp4");
                                context.startActivity(intent);

                            }else  if(strMimeType.equalsIgnoreCase("jpg") ||  strMimeType.equalsIgnoreCase("png")){
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                lstModelData.get(position).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] byteArray = stream.toByteArray();

                                Intent intent = new Intent(context,ViewMediaActivity.class);
                                intent.putExtra("FilePath",byteArray);
                                intent.putExtra("FileType","jpg");
                                context.startActivity(intent);
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(context,"No Activity found to handle this file.Need to install supported Application",Toast.LENGTH_LONG).show();
                    }
                }
            });

            return rowView;
        }
    }

    public void SubmitDialog()
    {
        if (!strDescription.equalsIgnoreCase("") && !strTitle.equalsIgnoreCase("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New Post Upload");
            builder.setMessage("Do you want to upload the complaint?")
                    .setCancelable(false)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if(App.isNetworkAvailable())
                                        new AsyncTaskExample().execute();
                                    else{
                                        ChocoBar.builder().setView(binding.mainLayout)
                                                .setText("No Internet connection")
                                                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                                //.setActionText(android.R.string.ok)
                                                .red()   // in built red ChocoBar
                                                .show();
                                    }
                                }
                            });

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //  Action for 'NO' Button
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }else {
            Toast.makeText(NewComplaintActivity.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
        }
    }


    private class AsyncTaskExample extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewComplaintActivity.this,"Please wait.. We are posting your complaint");
        }
        @Override
        protected Integer doInBackground(String... strings) {
            String result = uploadData(String.valueOf(prepareJSON()),"http://api.ainext.in/posts/addpost");
            try {
                JSONObject jsonObject = new JSONObject(result);
                if(jsonObject.has("message"))
                {
                    if(jsonObject.getString("message").equalsIgnoreCase("SUCCESS")){
                        if(jsonObject.has("result"))
                        {
                            JSONArray jsonarray = new JSONArray(jsonObject.getString("result"));
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                strResult = jsonobject.getInt("RES");
                            }
                        }
                    }
                }

            }catch (Exception e)
            {
                Crashlytics.logException(e);
            }
            return strResult;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            hideProgressDialog(NewComplaintActivity.this);

            if(result != -1 && result != 0) {
                showSuccessAlert(NewComplaintActivity.this,"Success","Data Successfully uploaded","OK");
            }else
                showAlert(NewComplaintActivity.this,"Failed to upload data","Result:"+result,"OK");
        }
    }

    private class AsyncUploadImages extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewComplaintActivity.this,"Please wait.. We are uploading your images");
        }
        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            if(!lstPathURI.isEmpty())
            {
                uploadMultiFile(lstPathURI);
            }

            System.out.println("Upload Images Result::" + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(NewComplaintActivity.this);

            /*if(result != null && !result.equalsIgnoreCase("")) {
                try{
                    JSONObject jobject = new JSONObject(result);
                    if (result.length() > 0) {
                        if (jobject.has("message")) {
                            if (jobject.getString("message").equalsIgnoreCase("SUCCESS")) {
                                if (jobject.has("result")) {
                                    JSONArray jsonArray = new JSONArray(jobject.getString("result"));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        CustomDialog(NewComplaintActivity.this, "Thank You", "Your request has been successfully posted. We will process and keep in touch with you.", "");
                                        *//*if(jsonObject.has("Status")) {
                                            int Status = jsonObject.getInt("Status");
                                        }*//*
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }else {
                showAlert(NewComplaintActivity.this, "Failed to upload data", "Result:" + result, "OK");
            }*/
            //binding.grid.setAdapter(null);
        }
    }

    public void showSuccessAlert(Activity activity, String Title, String strMsg, String Positive)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // Get the layout inflater
        LayoutInflater inflater = (activity).getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        builder.setTitle(Title);
        builder.setMessage(strMsg);
        builder.setCancelable(false);
        builder.setPositiveButton(Positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                if(App.isNetworkAvailable())
                    new AsyncUploadImages().execute(String.valueOf(strResult));
                else{
                    ChocoBar.builder().setView(binding.mainLayout)
                            .setText("No Internet connection")
                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                            .red()
                            .show();
                }
            }
        });
        builder.create();
        builder.show();
    }

    public void CustomDialog(Context context,String title,String msg,String subMsg)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.title);
        text.setText(title);

        TextView message = (TextView) dialog.findViewById(R.id.msg);
        message.setText(msg);

        if(!msg.equalsIgnoreCase("")) {
            message.setVisibility(View.VISIBLE);
            message.setText(msg);
        }else
            message.setVisibility(View.GONE);

        TextView sub_Msg = (TextView) dialog.findViewById(R.id.sub_msg);

        if(!subMsg.equalsIgnoreCase("")) {
            sub_Msg.setVisibility(View.VISIBLE);
            sub_Msg.setText(subMsg);
        }else
            sub_Msg.setVisibility(View.GONE);

        Button dialogButton = (Button) dialog.findViewById(R.id.btnOk);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @SuppressLint("LongLogTag")
    private String uploadData(String jsonParam , String strUrl)
    {
        try{
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(jsonParam);
                writer.close();
                InputStream inputStream;
                int status = urlConnection.getResponseCode();

                if (status != HttpURLConnection.HTTP_OK)  {
                    inputStream = urlConnection.getErrorStream();
                }
                else  {
                    inputStream = urlConnection.getInputStream();
                }
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                String JsonResponse = buffer.toString();
                //response data
                Log.i(TAG,JsonResponse);
                return JsonResponse;
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            /*Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Call<ResponseBody> call = api.postNewItem(prepareJSON());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String  bodyString = new String(response.body().bytes());
                        Log.v("bodyString ::: ",bodyString);
                        if (response.isSuccessful()) {
                            JSONArray jsonarray = new JSONArray(bodyString);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                strResult = jsonobject.getInt("RES");
                            }
                            hideProgressDialog(NewComplaintActivity.this);
                        } else {
                            hideProgressDialog(NewComplaintActivity.this);
                            Toast.makeText(NewComplaintActivity.this, "RESPONSE :: "+ response.body().toString(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        hideProgressDialog(NewComplaintActivity.this);
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    } catch (JSONException e) {
                        hideProgressDialog(NewComplaintActivity.this);
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    hideProgressDialog(NewComplaintActivity.this);
                }
            });*/
        }catch (Exception e)
        {
            Crashlytics.logException(e);
            hideProgressDialog(NewComplaintActivity.this);
        }
        return null;
    }

    private JsonObject prepareJSON() {
        JsonObject jsonParam = null;
        NewComplaintDataRequest request = new NewComplaintDataRequest();
        try {
            String postedBy = getStringSharedPreference(NewComplaintActivity.this,"MemberID");
            /*request.setCategoryID(category);
            request.setDescription(strDescription);
            request.setTitle(strTitle);
            request.setPostedBy(Integer.valueOf(postedBy));
            request.setLangitude(String.valueOf(latitude));
            request.setLangitude(String.valueOf(longitude));
            request.setItemID(-1);
            request.setLocation(strLocation);*/
            jsonParam = prepareAddNewPostJSON(category, strTitle, strDescription, postedBy,strLocation, String.valueOf(latitude),String.valueOf(longitude));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("JSON Data ::"+jsonParam.toString());
        return jsonParam;
        //return request;
    }

    private JsonObject prepareAddNewPostJSON(int strComplaintType, String strTitle, String strDescription, String postedBy, String strLocation, String latitude, String longitude){

        try {
            JsonObject json = new JsonObject();
            json.addProperty("itemID", -1);
            json.addProperty("title", strTitle);
            json.addProperty("description", strDescription);
            json.addProperty("categoryID", category);
            json.addProperty("postedBy", postedBy);
            json.addProperty("location", strLocation);
            json.addProperty("latitude", latitude);
            json.addProperty("langitude", longitude);

            return json;
        }catch (Exception e)
        {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return null;
    }

    /*public void uploadAlbum(ArrayList<String> filePaths ){

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            //MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = null;
            Request request = null ;
            for (int i = 0; i < filePaths.size(); i++) {
                File file = new File(filePaths.get(i));
                body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getAbsolutePath(),
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(file.getAbsolutePath()))).build();
                request  = new Request.Builder()
                        .url("http://api.ainext.in/posts/upload")
                        .method("POST", body)
                        .build();
            }

            okhttp3.Response response = client.newCall(request).execute();
            System.out.println("Response :::: "+response.body());
        } catch (Exception e)
        {
            e.printStackTrace();
        }*/
       /* List<MultipartBody.Part> list = new ArrayList<>();
        int i = 0;
        for (Uri uri : filePaths) {
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), bos.toByteArray()));
            list.add(fileToUpload);
        }
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);

        Call<ResponseBody> call = api.uploadAlbum(list);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("main", "the message is ----> " + response.body());
                Log.e("main", "the error is ----> " + response.body());

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.e("main", "on error is called and the error is  ----> " + throwable.getMessage());

            }
        });*/

    //}

    private void uploadMultiFile(ArrayList<String> filePaths ) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient clientWith30sTimeout = okHttpClient.newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        List<MultipartBody.Part> list = new ArrayList<>();
        // Multiple Images
        for (int i = 0; i < filePaths.size(); i++) {
            File file = new File(filePaths.get(i));
            /*Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 30, bos);*/
            //RequestBody requestBody = RequestBody.create(MediaType.parse("*///*"), file);

            /*MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), bos.toByteArray()));*/
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
            builder.addPart(fileToUpload);
            //list.add(fileToUpload);
            //builder.addFormDataPart("file", file.getName(), RequestBody.create(MultipartBody.FORM, bos.toByteArray()));
        }

        MultipartBody requestBody = builder.build();

        Call<JSONObject> call = api.uploadMultiFile("1",requestBody);
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
               // showAlert(NewComplaintActivity.this, "Uploaded Successfully", "OK", "OK");
                CustomDialog(NewComplaintActivity.this, "Thank You", "Your request has been successfully posted. We will process and keep in touch with you.", "");
                binding.etTitle.setText("");
                binding.etDescription.setText("");
                binding.spinner.setText("");
                getLocation();
                binding.currentDate.setText(currentTime);
                customImageAdapter.notifyDataSetChanged();
                customImageAdapter.clear();
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.d(TAG, "Error " + t.getMessage());
                showAlert(NewComplaintActivity.this, "Failed to upload data", "Result:" + t.getMessage(), "OK");
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri){
        File file = new File(getPath(fileUri));
        RequestBody requestBody = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);
        return MultipartBody.Part.createFormData(partName, file.getName(),requestBody);
    }


    private String getRealPathFromURI(Uri uri) {
        Uri returnUri = uri;
        Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(getFilesDir(), name);
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }



    public static String getPathFromUri(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);
                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));

                         /*   final Uri contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));*/

                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }


                    }

                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final boolean isOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }


            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};


                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            } else if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
        }


        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context);
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                // return getFilePathFromURI(context,uri);
                return getMediaFilePathForN(uri, context);
                // return getRealPathFromURI(context,uri);
            } else {

                return getDataColumn(context, uri, null, null);
            }


        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Check if a file exists on device
     *
     * @param filePath The absolute file path
     */
    private static boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }


    /**
     * Get full file path from external storage
     *
     * @param pathData The storage type and the relative path
     */
    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private static String getMediaFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }


    private static String getDataColumn(Context context, Uri uri,
                                        String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri - The Uri to check.
     * @return - Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Drive.
     */
    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }


    private void uploadImage(String imagePath)
    {

        try {
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);

            RequestBody requestFile = RequestBody.create((MediaType.parse("image/*")), imagePath);

            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imagePath, requestFile);
            Call<JsonObject> call = api.uploadImage(body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    try {
                        if(response.body() != null) {
                            Log.e(" responce => ", response.body().toString());
                            if (response.isSuccessful()) {
                                //hideProgressDialog(NewComplaintActivity.this);
                                Gson gson = new Gson();
                                String successResponse = gson.toJson(response.body());
                                Toast.makeText(NewComplaintActivity.this, "RESPONSE :: "+ successResponse, Toast.LENGTH_SHORT).show();
                                JSONObject jsonObj = new JSONObject(response.body().toString());
                            } else {
                                Toast.makeText(NewComplaintActivity.this, "RESPONSE :: "+ response.body().toString(), Toast.LENGTH_SHORT).show();
                                //hideProgressDialog(NewComplaintActivity.this);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            Log.e("Tag", "error=" + e.toString());
                            Toast.makeText(NewComplaintActivity.this, "ERROR :: "+ e.toString(), Toast.LENGTH_SHORT).show();
                           // hideProgressDialog(NewComplaintActivity.this);
                        } catch (Resources.NotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    try {
                        Log.e("Tag", "error" + t.toString());
                        Toast.makeText(NewComplaintActivity.this, "ERROR :: "+ t.toString(), Toast.LENGTH_SHORT).show();
                        hideProgressDialog(NewComplaintActivity.this);
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}


/*public int uploadFile(String sourceFileUri) {

        String fileName=sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "------hellojosh";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);
        Log.e("joshtag", "Uploading: sourcefileURI, "+fileName);

        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :"+ appSingleton.getInstance().photouri);//FullPath);
            runOnUiThread(new Runnable() {
                public void run() {
                    //messageText.setText("Source File not exist :"
                }
            });
            return 0;  //RETURN #1
        }
        else{
            try{

                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                Log.v("joshtag",url.toString());

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy            s
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("file", fileName);
                conn.setRequestProperty("user", user_id));

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    Log.i("joshtag","->");
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage().toString();
                Log.i("joshtag", "HTTP Response is : "  + serverResponseMessage + ": " + serverResponseCode);

                // ------------------ read the SERVER RESPONSE
                DataInputStream inStream;
                try {
                    inStream = new DataInputStream(conn.getInputStream());
                    String str;
                    while ((str = inStream.readLine()) != null) {
                        Log.e("joshtag", "SOF Server Response" + str);
                    }
                    inStream.close();
                }
                catch (IOException ioex) {
                    Log.e("joshtag", "SOF error: " + ioex.getMessage(), ioex);
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                if(serverResponseCode == 200){
                    //Do something
                }//END IF Response code 200

                dialog.dismiss();
            }//END TRY - FILE READ
            catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.e("joshtag", "UL error: " + ex.getMessage(), ex);
            } //CATCH - URL Exception

            catch (Exception e) {
                e.printStackTrace();
                Log.e("Upload file to server Exception", "Exception : "+ e.getMessage(), e);
            } //

            return serverResponseCode; //after try
        }//END ELSE, if file exists.
    }*/
