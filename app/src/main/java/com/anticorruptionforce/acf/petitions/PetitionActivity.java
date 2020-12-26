package com.anticorruptionforce.acf.petitions;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.anticorruptionforce.acf.activities.AddPetitionActivity;
import com.anticorruptionforce.acf.activities.BaseActivity;
import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.fragments.MyPetitionListFragment;
import com.anticorruptionforce.acf.databinding.ActivityPetitionBinding;

public class PetitionActivity extends BaseActivity {

    ActivityPetitionBinding dataBiding;
    String strCategoryID = "";
    String strName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_petition);
        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_petition);

        init();

        dataBiding.llMyPetitionList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PetitionActivity.this, MyPetitionListFragment.class);
                startActivity(intent);
            }
        });

        dataBiding.llAddNewPetition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PetitionActivity.this, AddPetitionActivity.class);
                startActivity(intent);
            }
        });

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
}
