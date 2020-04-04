package com.joinacf.acf.petitions;

import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.joinacf.acf.R;

import java.util.ArrayList;

public class PetitionsListAdapter extends ArrayAdapter<PetitionModel> {
    public ArrayList<PetitionModel> dataSet;
    FragmentActivity context;
    private static LayoutInflater inflater=null;


    public PetitionsListAdapter(FragmentActivity context,ArrayList<PetitionModel> data) {
        super(context, R.layout.custom_petition_list_items, data);
        this.dataSet = data;
        this.context = context;
    }

    public class ViewHolder
    {
        TextView txtPetitionNo;
        TextView txtPetitionier;
        TextView txtStatus;
        TextView txtComplaint;
        TextView txtComplaintDate;
        Button btnVerified;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder holder = null; // view lookup cache stored in tag
        View rowView = null;
        PetitionModel dataModel = (PetitionModel) getItem(position);

        if (convertView == null) {

            holder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.custom_petition_list_items, null);

            holder.txtPetitionNo = (TextView) rowView.findViewById(R.id.tv_petitionNo);
            holder.txtPetitionier = (TextView) rowView.findViewById(R.id.tv_petitioner);
            holder.txtStatus = (TextView) rowView.findViewById(R.id.tv_status);
            holder.txtComplaint = (TextView) rowView.findViewById(R.id.tv_complaint);
            holder.txtComplaintDate = (TextView) rowView.findViewById(R.id.tv_complaintDate);
            holder.btnVerified = (Button) rowView.findViewById(R.id.btnVerified);
        }


        holder.txtPetitionNo.setText(dataModel.getPetitionNo());
        holder.txtPetitionier.setText(dataModel.getPetitioner());
        holder.txtComplaint.setText(dataModel.getComplaint());
        holder.txtComplaintDate.setText(dataModel.getComplaintDate());


        if(dataModel.getStatus().equalsIgnoreCase("Verified")) {
            holder.btnVerified.setVisibility(View.INVISIBLE);
            holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + "</font>" + "<font color=\"#47a842\">" + dataModel.getStatus() + "</font>"));
        }
        else {
            holder.btnVerified.setVisibility(View.VISIBLE);
            holder.txtStatus.setText(Html.fromHtml("<font color=\"#000000\">" + "Status : " + dataModel.getStatus()));
        }


        final ViewHolder finalHolder = holder;

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Clicked :" + finalHolder.txtPetitionNo.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });

        return rowView;
    }

}
