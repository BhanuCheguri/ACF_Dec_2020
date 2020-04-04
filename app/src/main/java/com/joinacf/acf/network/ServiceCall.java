package com.joinacf.acf.network;

import android.util.Log;
import android.widget.Toast;

import com.joinacf.acf.modelclasses.WallPostsModel;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ServiceCall {

    String reponse = null;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel> lstWallPost;


    public String sendPost(final String jsonParam)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://api.ainext.in/petitions/addpetition");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();


                    InputStream response = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            response.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    reponse = sb.toString();
                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());
                    conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }});
        thread.start();
        return reponse;
    }

    public ArrayList<WallPostsModel> getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<List<WallPostsModel>> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<List<WallPostsModel>>() {
            @Override
            public void onResponse(Call<List<WallPostsModel>> call, Response<List<WallPostsModel>> response) {
                List<WallPostsModel> myProfileData = response.body();
                lstWallPost = new ArrayList<WallPostsModel>();
                for (Object object : myProfileData) {
                    lstWallPost.add((WallPostsModel) object);
                }
                //populateListView(lstWallPost);
            }

            @Override
            public void onFailure(Call<List<WallPostsModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return lstWallPost;
    }
}
