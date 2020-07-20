package com.joinacf.acf.modelclasses;
import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModeratorListModel {

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
        @SerializedName("Category")
        @Expose
        private String category;
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
        @SerializedName("MOD SATUS")
        @Expose
        private String mODSATUS;
        @SerializedName("AssignedTo")
        @Expose
        private String assignedTo;
        @SerializedName("Assigned_Date")
        @Expose
        private String assignedDate;
        @SerializedName("Accepted_Date")
        @Expose
        private String acceptedDate;
        @SerializedName("FilePath")
        @Expose
        private String filePath;

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

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
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

        public String getMODSATUS() {
            return mODSATUS;
        }

        public void setMODSATUS(String mODSATUS) {
            this.mODSATUS = mODSATUS;
        }

        public String getAssignedTo() {
            return assignedTo;
        }

        public void setAssignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
        }

        public String getAssignedDate() {
            return assignedDate;
        }

        public void setAssignedDate(String assignedDate) {
            this.assignedDate = assignedDate;
        }

        public String getAcceptedDate() {
            return acceptedDate;
        }

        public void setAcceptedDate(String acceptedDate) {
            this.acceptedDate = acceptedDate;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
