package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MyPostingModel {

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

        @SerializedName("ItemID")
        @Expose
        private String itemID;
        @SerializedName("Title")
        @Expose
        private String title;
        @SerializedName("Description")
        @Expose
        private String description;
        @SerializedName("CategoryID")
        @Expose
        private Integer categoryID;
        @SerializedName("PostedDate")
        @Expose
        private String postedDate;
        @SerializedName("Location")
        @Expose
        private String location;
        @SerializedName("Latitude")
        @Expose
        private String latitude;
        @SerializedName("Langitude")
        @Expose
        private String langitude;
        @SerializedName("Status")
        @Expose
        private Integer status;

        public String getItemID() {
            return itemID;
        }

        public void setItemID(String itemID) {
            this.itemID = itemID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(Integer categoryID) {
            this.categoryID = categoryID;
        }

        public String getPostedDate() {
            return postedDate;
        }

        public void setPostedDate(String postedDate) {
            this.postedDate = postedDate;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLangitude() {
            return langitude;
        }

        public void setLangitude(String langitude) {
            this.langitude = langitude;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
