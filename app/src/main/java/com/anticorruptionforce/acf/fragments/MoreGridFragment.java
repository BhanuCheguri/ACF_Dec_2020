package com.anticorruptionforce.acf.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.anticorruptionforce.acf.activities.KnowYourActsActivity;
import com.anticorruptionforce.acf.activities.MainActivity;
import com.anticorruptionforce.acf.activities.MoreActivity;
import com.anticorruptionforce.acf.activities.NewLoginActivity;
import com.anticorruptionforce.acf.activities.MyProfileActivity;
import com.anticorruptionforce.acf.modelclasses.DashboardCategories;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.databinding.FragmentGridBinding;
import com.anticorruptionforce.acf.utilities.App;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.pd.chocobar.ChocoBar;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MoreGridFragment extends BaseFragment {

    FragmentGridBinding dataBiding;
    int mLastFirstVisibleItem;
    int mLastVisibleItemCount;
    boolean bFirst;
    String strPersonName,strPersonEmail,strLoginType;

    int[] imageId = {
            R.mipmap.ic_adulteration,
            R.mipmap.ic_social_evil,
            R.mipmap.ic_women_saftey,
            R.mipmap.ic_human_trafficking,
            R.mipmap.ic_medical_emergency,
            R.mipmap.ic_law_n_order,
            R.mipmap.ic_information,
            R.mipmap.ic_humanrights
    };
    private APIRetrofitClient apiRetrofitClient;
    ArrayList<String> lstCategories;
    HashMap<String,String> hshMapDashBoardsLst ;
    ArrayList<String> lstExistsCategories;
    ArrayList<DashboardCategories.Result> lstgridCatagories;
    ArrayList<DashboardCategories.Result> lstDashboardCatagories;
    ArrayList<DashboardCategories.Result> lstResltCatagories;

    public static MoreGridFragment newInstance() {
        return new MoreGridFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        dataBiding = DataBindingUtil.inflate(inflater, R.layout.fragment_grid, null, false);
        setActionBarTitle(getString(R.string.title_more));
        if(App.isNetworkAvailable())
            getDashboardCategories();
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }
        loadSharedPrefference();

        dataBiding.grid.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity)getActivity()).hideBottomNavigation();
                } else if (mLastFirstVisibleItem < firstVisibleItem) {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity)getActivity()).showBottomNavigation();
                } else if (mLastVisibleItemCount < visibleItemCount) {
                    Log.e(getClass().toString(), "scrolling down");
                    ((MainActivity)getActivity()).showBottomNavigation();
                } else if (mLastVisibleItemCount > visibleItemCount) {
                    Log.e(getClass().toString(), "scrolling up");
                    ((MainActivity)getActivity()).hideBottomNavigation();
                }
                mLastFirstVisibleItem = firstVisibleItem;
                mLastVisibleItemCount = visibleItemCount;
            }

            public void onScrollStateChanged(AbsListView listView, int scrollState) {
            }
        });

        return dataBiding.getRoot();
    }

    private void loadSharedPrefference() {

        bFirst = getBooleanSharedPreference(getActivity(), "FirstTime");
        strLoginType = getStringSharedPreference(getActivity(), "LoginType");
        strPersonName = getStringSharedPreference(getActivity(), "PersonName");
        //Toast.makeText(getActivity(),strPersonName ,Toast.LENGTH_LONG).show();
        strPersonEmail = getStringSharedPreference(getActivity(), "personEmail");
        if (bFirst) {
            //showWelcomeAlert(strPersonName);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        MenuItem action_search= menu.findItem(R.id.action_search);
        action_search.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:

                break;
            case R.id.myprofile:
                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    intent = new Intent(getContext(), MyProfileActivity.class);
                }
                getActivity().startActivity(intent);
                break;

            case R.id.logout:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut()
    {
        try {
            if (strLoginType.equalsIgnoreCase("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                googleSignInClient.signOut();
                putBooleanSharedPreference(getActivity(), "LoggedIn",false);
                Toast.makeText(getApplicationContext(), "User Logged out successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), NewLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            } else {
                FacebookSdk.sdkInitialize(getApplicationContext());
                AppEventsLogger.activateApp(getActivity());
                LoginManager.getInstance().logOut();
                putBooleanSharedPreference(getActivity(), "LoggedIn",false);

                Intent intent = new Intent(getActivity(), NewLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            putBooleanSharedPreference(getActivity(), "FirstTime", false);
            putStringSharedPreference(getActivity(), "LoginType", "");
            putStringSharedPreference(getActivity(), "personName", "");
            putStringSharedPreference(getActivity(), "personEmail", "");
            putStringSharedPreference(getActivity(), "personId", "");
        }catch (Exception e)
        {
            e.printStackTrace();
            //FirebaseCrashlytics.getInstance().setCustomKey("MoreGridFragment", e.getMessage());
        }
    }

    public class CustomGrid extends BaseAdapter{
        private Context mContext;
        private final ArrayList<String> web;
        private final int[] Imageid;

        public CustomGrid(Context c, ArrayList<String> lstDashboardCatagories, int[] Imageid) {
            mContext = c;
            this.Imageid = Imageid;
            this.web = lstDashboardCatagories;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return web.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {

                grid = new View(mContext);
                grid = inflater.inflate(R.layout.grid_row_layout, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_img);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 300);
                imageView.setLayoutParams(layoutParams);
                textView.setText(web.get(position));
                imageView.setImageResource(Imageid[position]);

            } else {
                grid = (View) convertView;
            }

            return grid;
        }
    }

    private void getDashboardCategories() {
        try {
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Call<DashboardCategories> call = api.getDashboardCategories();

            call.enqueue(new Callback<DashboardCategories>() {
                @Override
                public void onResponse(Call<DashboardCategories> call, Response<DashboardCategories> response) {
                    DashboardCategories myProfileData  = response.body();
                    if(myProfileData != null) {
                        String status = myProfileData.getStatus();
                        String msg = myProfileData.getMessage();
                        if (msg.equalsIgnoreCase("SUCCESS")) {
                            lstResltCatagories = myProfileData.getResult();
                            checkExistingCategories(lstResltCatagories);
                        }
                    }
                }

                @Override
                public void onFailure(Call<DashboardCategories> call, Throwable t) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e)
        {
            e.printStackTrace();
            //FirebaseCrashlytics.getInstance().setCustomKey("MoreGridFragment", e.getMessage());
        }
    }

    private void checkExistingCategories(ArrayList<DashboardCategories.Result> lstData) {
        lstDashboardCatagories = new ArrayList<DashboardCategories.Result>();
        lstExistsCategories = new ArrayList<String>();
        lstCategories = new ArrayList<String>();
        hshMapDashBoardsLst = new HashMap<>();

        lstExistsCategories.add("Home");
        lstExistsCategories.add("Corruption");
        lstExistsCategories.add("Find n Fix");
        lstExistsCategories.add("My Petitions");

        lstDashboardCatagories = lstData;
        for(int i=0; i<lstDashboardCatagories.size(); i++) {
            lstCategories.add(lstDashboardCatagories.get(i).getName().toString().trim());
            hshMapDashBoardsLst.put(lstDashboardCatagories.get(i).getName().toString().trim(),lstDashboardCatagories.get(i).getCategoryID().toString().trim());
        }

        lstCategories.removeAll(lstExistsCategories);

        System.out.println(lstCategories);

        CustomGrid adapter  = new CustomGrid(getActivity(), lstCategories, imageId);
        dataBiding.grid.setAdapter(adapter);
        dataBiding.grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                System.out.println("CatergoryID" +hshMapDashBoardsLst.get(lstCategories.get(position)));

                if(!hshMapDashBoardsLst.get(lstCategories.get(position)).equalsIgnoreCase("10")) {
                    Intent intent = new Intent(getActivity(), MoreActivity.class);
                    intent.putExtra("CatergoryID", hshMapDashBoardsLst.get(lstCategories.get(position)) + "-"+ lstCategories.get(position) );
                    intent.putExtra("Name", lstCategories.get(position));
                    getActivity().startActivity(intent);
                }else
                {
                    Intent intent = new Intent(getActivity(), KnowYourActsActivity.class);
                    intent.putExtra("CatergoryID", hshMapDashBoardsLst.get(lstCategories.get(position))+ "-"+ lstCategories.get(position));
                    intent.putExtra("Name", lstCategories.get(position));
                    getActivity().startActivity(intent);
                }
            }
        });
    }
}
