package com.joinacf.acf.fragments;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.joinacf.acf.activities.NewComplaintActivity;
import com.joinacf.acf.activities.ProfileActivity;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.R;
import com.joinacf.acf.databinding.FragmentSocialevilBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * Created by priya.cheguri on 8/14/2019.
 */

public class SocialEvilFragment extends BaseFragment {

    FragmentSocialevilBinding dataBiding;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel> lstWallPost;
    HomePageAdapter adapter;
    List<WallPostsModel> myProfileData;

    public static SocialEvilFragment newInstance() {
        return new SocialEvilFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_socialevil, null, false);

        setActionBarTitle(getString(R.string.title_socialEvil));

        init();
        LoadAdapter();
        return dataBiding.getRoot();
    }

    private void  init() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));


        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void LoadAdapter()
    {
        showProgressDialog(getActivity());
        getWallPostDetails("5","-1");

        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewComplaintActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }
    public ArrayList<WallPostsModel> getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<List<WallPostsModel>> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<List<WallPostsModel>>() {
            @Override
            public void onResponse(Call<List<WallPostsModel>> call, Response<List<WallPostsModel>> response) {
                if(response != null) {
                    myProfileData = response.body();
                    if(myProfileData.size() > 0) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        lstWallPost = new ArrayList<WallPostsModel>();
                        for (Object object : myProfileData) {
                            lstWallPost.add((WallPostsModel) object);
                        }
                        populateListView(lstWallPost);
                    }else {
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(getActivity());
                    }
                }else {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(getActivity());
                }
            }

            @Override
            public void onFailure(Call<List<WallPostsModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(getActivity());
            }
        });
        return lstWallPost;
    }

    private void populateListView(ArrayList<WallPostsModel> wallPostData) {
        adapter = new HomePageAdapter(getActivity(),wallPostData);
        dataBiding.lvSocialEvilFeed.setAdapter(adapter);
        //hideProgressDialog(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu, menu);
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:

                break;
            case R.id.myprofile:
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                getActivity().startActivity(intent);
                break;

            /*case R.id.logout:
                signOut();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }
}