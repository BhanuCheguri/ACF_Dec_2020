package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfficesModel {
    @SerializedName("SPID")
    @Expose
    private String sPID;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Address")
    @Expose
    private String address;
    @SerializedName("Latitude")
    @Expose
    private String latitude;
    @SerializedName("Longitude")
    @Expose
    private String longitude;
    @SerializedName("DISTANCE")
    @Expose
    private String dISTANCE;

    public String getSPID() {
        return sPID;
    }

    public void setSPID(String sPID) {
        this.sPID = sPID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDISTANCE() {
        return dISTANCE;
    }

    public void setDISTANCE(String dISTANCE) {
        this.dISTANCE = dISTANCE;
    }
}
