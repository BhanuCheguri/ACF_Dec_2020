package com.joinacf.acf.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joinacf.acf.activities.AddPetitionActivity;
import com.joinacf.acf.activities.MoreActivity;
import com.joinacf.acf.modelclasses.DashboardCategories;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.R;
import com.joinacf.acf.databinding.FragmentGridBinding;
import com.crashlytics.android.Crashlytics;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MoreGridFragment extends BaseFragment {

    FragmentGridBinding dataBiding;
    /*String[] web = {
            "Corruption",
            "Adulteration",
            "Find N Fix",
            "Social Evil"
    } ;*/
    int[] imageId = {
            R.drawable.ic_adulteration,
            R.mipmap.ic_women_saftey,
            R.drawable.ic_social_evil,
            R.mipmap.ic_medical_emergency,
            R.mipmap.ic_law_n_order,
            R.mipmap.ic_law_n_order,
            R.mipmap.ic_law_n_order,
            R.mipmap.ic_law_n_order,
    };
    private APIRetrofitClient apiRetrofitClient;
    ArrayList<String> lstCategories;
    HashMap<String,String> hshMapDashBoardsLst ;
    ArrayList<String> lstExistsCategories;
    ArrayList<DashboardCategories> lstgridCatagories;
    ArrayList<DashboardCategories> lstDashboardCatagories;

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

        apiRetrofitClient = new APIRetrofitClient();
        getDashboardCategories();

        return dataBiding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 200);
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
            Call<List<DashboardCategories>> call = api.getDashboardCategories();

            call.enqueue(new Callback<List<DashboardCategories>>() {
                @Override
                public void onResponse(Call<List<DashboardCategories>> call, Response<List<DashboardCategories>> response) {
                    List<DashboardCategories> myProfileData = response.body();
                    ArrayList<DashboardCategories> lstgridCatagories = new ArrayList<DashboardCategories>();
                    for (Object object : myProfileData) {
                        lstgridCatagories.add((DashboardCategories) object);
                    }
                    checkExistingCategories(lstgridCatagories);
                }

                @Override
                public void onFailure(Call<List<DashboardCategories>> call, Throwable t) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }

    private void checkExistingCategories(ArrayList<DashboardCategories> lstData) {
        lstDashboardCatagories = new ArrayList<DashboardCategories>();
        lstExistsCategories = new ArrayList<String>();
        lstCategories = new ArrayList<String>();
        hshMapDashBoardsLst = new HashMap<>();

        lstExistsCategories.add("Home");
        lstExistsCategories.add("Corruption");
        lstExistsCategories.add("Find n Fix");
        lstExistsCategories.add("Social Evil");

        lstDashboardCatagories = lstData;
        for(int i=0; i<lstDashboardCatagories.size(); i++) {
            lstCategories.add(lstDashboardCatagories.get(i).getName().toString().trim());
            hshMapDashBoardsLst.put(lstDashboardCatagories.get(i).getName().toString().trim(),lstDashboardCatagories.get(i).getCategoryID().toString().trim());
        }

        lstCategories.removeAll(lstExistsCategories);

        System.out.println(lstCategories);

        CustomGrid adapter = new CustomGrid(getActivity(), lstCategories, imageId);
        dataBiding.grid.setAdapter(adapter);
        dataBiding.grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if(!hshMapDashBoardsLst.get(lstCategories.get(position)).equalsIgnoreCase("11")){
                    Intent intent = new Intent(getActivity(), MoreActivity.class);
                    intent.putExtra("CatergoryID",hshMapDashBoardsLst.get(lstCategories.get(position)));
                    intent.putExtra("Name",lstCategories.get(position));
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(getActivity(), AddPetitionActivity.class);
                    intent.putExtra("CatergoryID",hshMapDashBoardsLst.get(lstCategories.get(position)));
                    intent.putExtra("Name",lstCategories.get(position));
                    startActivity(intent);
                }

               // Toast.makeText(getActivity(), "You Clicked at " + hshMapDashBoardsLst.get(lstCategories.get(position)), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
