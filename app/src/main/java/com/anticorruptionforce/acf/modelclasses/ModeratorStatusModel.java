package com.anticorruptionforce.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModeratorStatusModel {

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

        @SerializedName("Pending")
        @Expose
        private Integer pending;
        @SerializedName("Department \u2013L1")
        @Expose
        private Integer departmentL1;
        @SerializedName("Department \u2013L2")
        @Expose
        private Integer departmentL2;
        @SerializedName("ACB")
        @Expose
        private Integer aCB;
        @SerializedName("Police")
        @Expose
        private Integer police;
        @SerializedName("Health")
        @Expose
        private Integer health;
        @SerializedName("Expert/Legal")
        @Expose
        private Integer expertLegal;
        @SerializedName("Publish")
        @Expose
        private Integer publish;

        public Integer getPending() {
            return pending;
        }

        public void setPending(Integer pending) {
            this.pending = pending;
        }

        public Integer getDepartmentL1() {
            return departmentL1;
        }

        public void setDepartmentL1(Integer departmentL1) {
            this.departmentL1 = departmentL1;
        }

        public Integer getDepartmentL2() {
            return departmentL2;
        }

        public void setDepartmentL2(Integer departmentL2) {
            this.departmentL2 = departmentL2;
        }

        public Integer getACB() {
            return aCB;
        }

        public void setACB(Integer aCB) {
            this.aCB = aCB;
        }

        public Integer getPolice() {
            return police;
        }

        public void setPolice(Integer police) {
            this.police = police;
        }

        public Integer getHealth() {
            return health;
        }

        public void setHealth(Integer health) {
            this.health = health;
        }

        public Integer getExpertLegal() {
            return expertLegal;
        }

        public void setExpertLegal(Integer expertLegal) {
            this.expertLegal = expertLegal;
        }

        public Integer getPublish() {
            return publish;
        }

        public void setPublish(Integer publish) {
            this.publish = publish;
        }

    }
}