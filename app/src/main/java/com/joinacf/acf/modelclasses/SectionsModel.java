package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SectionsModel {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("result")
    @Expose
    private ArrayList<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Result> getResult() {
        return result;
    }

    public void setResult(ArrayList<Result> result) {
        this.result = result;
    }

    public class Result {

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
}
