package com.joinacf.acf.petitions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.WindowManager;


import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityPetetionBinding;

import java.util.ArrayList;

public class MyPetitionListActivity extends AppCompatActivity {

    ArrayList<PetitionModel> model;
    ActivityPetetionBinding dataBiding;
    String strCategoryID = "";
    String strName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_petetion);
        init();
        LoadAdapter();
    }

    private void LoadAdapter() {
        model= new ArrayList<>();

        model.add(new PetitionModel("1","RK Reddy","Not Verified","Complain on cheating","12th Aug 2019 , 7:20PM" ,"0"));
        model.add(new PetitionModel("2","RK Reddy","Not Verified","Complain on cheating","14th Aug 2019 , 8:20PM" ,"0"));
        model.add(new PetitionModel("3","RK Reddy","Verified","Complain on cheating","18th Aug 2019 , 2:20PM" ,"0"));
        model.add(new PetitionModel("4","RK Reddy","Not Verified","Complain on cheating","19th Aug 2019 , 1:20PM" ,"0"));
        model.add(new PetitionModel("5","RK Reddy","Verified","Complain on cheating","22th Aug 2019 , 9:20PM" ,"0"));

        PetitionsListAdapter adaper = new PetitionsListAdapter(MyPetitionListActivity.this,model);
        dataBiding.lvPetitions.setAdapter(adaper);
    }

    private void  init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        /*Bundle b = getIntent().getExtras();
        if (b != null)
        {
            strCategoryID = b.getString("CatergoryID").toString();
            strName = b.getString("Name").toString();
        }*/
        getSupportActionBar().setTitle("My Petition List");

    }

}
