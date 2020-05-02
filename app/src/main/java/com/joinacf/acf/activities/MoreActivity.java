package com.joinacf.acf.activities;

import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.joinacf.acf.R;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.databinding.ActivityMoreBinding;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MoreActivity extends BaseActivity {

    ActivityMoreBinding dataBiding;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel.Result> lstWallPost;
    String strCategoryID = "";
    String strName = "";
    HomePageAdapter adapter;
    List<WallPostsModel> myProfileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_more);
        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_more);
        init();
        LoadAdapter();
    }

    private void  init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle b = getIntent().getExtras();
        if (b != null)
        {
            strCategoryID = b.getString("CatergoryID").toString();
            strName = b.getString("Name").toString();
        }
        getSupportActionBar().setTitle(strName);

    }

    private void LoadAdapter()
    {
        showProgressDialog(MoreActivity.this);
        if(App.isNetworkAvailable())
            getWallPostDetails(strCategoryID,"-1");
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }


        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoreActivity.this, NewComplaintActivity.class);
                startActivity(intent);
            }
        });
    }
    public void getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<WallPostsModel> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<WallPostsModel>() {
            @Override
            public void onResponse(Call<WallPostsModel> call, Response<WallPostsModel> response) {
                if(response != null) {
                    WallPostsModel myWallData  = response.body();
                    if(myWallData != null) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        String status = myWallData.getStatus();
                        String msg = myWallData.getMessage();
                        if(msg.equalsIgnoreCase("SUCCESS")) {
                            lstWallPost = myWallData.getResult();
                            populateListView(lstWallPost);
                        }else
                        {
                            dataBiding.llNoData.setVisibility(View.VISIBLE);
                            hideProgressDialog(MoreActivity.this);
                        }
                    }else {
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(MoreActivity.this);
                    }
                }else {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(MoreActivity.this);
                }
            }

            @Override
            public void onFailure(Call<WallPostsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(MoreActivity.this);
            }
        });
        // return lstWallPost;
    }

    private void populateListView(ArrayList<WallPostsModel.Result> wallPostData) {
        adapter = new HomePageAdapter(MoreActivity.this,wallPostData);
        dataBiding.lvMoreFeed.setAdapter(adapter);
        hideProgressDialog(MoreActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem action_search= menu.findItem(R.id.action_search);
        action_search.setVisible(false);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(adapter != null)
                    adapter.getFilter().filter(newText.toString());
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:

                break;
            case R.id.myprofile:
                Intent intent = new Intent(MoreActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

            /*case R.id.logout:
                signOut();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }
}
