package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SPLoginModel {
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

        @SerializedName("Status")
        @Expose
        private String status;
        @SerializedName("UType")
        @Expose
        private String uType;
        @SerializedName("SPID")
        @Expose
        private String sPID;
        @SerializedName("SectionID")
        @Expose
        private String sectionID;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getUType() {
            return uType;
        }

        public void setUType(String uType) {
            this.uType = uType;
        }

        public String getSPID() {
            return sPID;
        }

        public void setSPID(String sPID) {
            this.sPID = sPID;
        }

        public String getSectionID() {
            return sectionID;
        }

        public void setSectionID(String sectionID) {
            this.sectionID = sectionID;
        }
    }
}
