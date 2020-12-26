package com.anticorruptionforce.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddPetitionResult {

    @SerializedName("PID")
    @Expose
    private Integer pID;
    @SerializedName("OTP")
    @Expose
    private String oTP;

    public Integer getPID() {
        return pID;
    }

    public void setPID(Integer pID) {
        this.pID = pID;
    }

    public String getOTP() {
        return oTP;
    }

    public void setOTP(String oTP) {
        this.oTP = oTP;
    }

}
