package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SectionsModel {
    @SerializedName("SectionID")
    @Expose
    private String sectionID;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("ContactNo")
    @Expose
    private String contactNo;
    @SerializedName("ContactDetails")
    @Expose
    private String contactDetails;

    public String getSectionID() {
        return sectionID;
    }

    public void setSectionID(String sectionID) {
        this.sectionID = sectionID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }
}
