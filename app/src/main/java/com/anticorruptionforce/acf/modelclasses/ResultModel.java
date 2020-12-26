package com.anticorruptionforce.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResultModel {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("result")
    @Expose
    private ArrayList<ResultModel.Result> result = null;

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

    public ArrayList<ResultModel.Result> getResult() {
        return result;
    }

    public void setResult(ArrayList<ResultModel.Result> result) {
        this.result = result;
    }

    public class Result {

        @SerializedName("RES")
        @Expose
        private Integer RES;

        public Integer getRES() {
            return RES;
        }

        public void setSTATUS(Integer res) {
            this.RES = res;
        }
    }
}
