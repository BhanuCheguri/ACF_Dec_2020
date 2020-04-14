package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusModel {
    @SerializedName("RES")
    @Expose
    private Integer rES;

    public Integer getRES() {
        return rES;
    }

    public void setRES(Integer rES) {
        this.rES = rES;
    }

}
