package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PetitionModel {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("result")
    @Expose
    private ArrayList<PetitionModel.Result> result = null;

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

    public ArrayList<PetitionModel.Result> getResult() {
        return result;
    }

    public void setResult(ArrayList<PetitionModel.Result> result) {
        this.result = result;
    }

    public class Result {

        @SerializedName("PetitionID")
        @Expose
        private String petitionID;
        @SerializedName("Title")
        @Expose
        private String title;
        @SerializedName("Image1")
        @Expose
        private String image1;
        @SerializedName("Image2")
        @Expose
        private String image2;
        @SerializedName("Image3")
        @Expose
        private String image3;
        @SerializedName("Image4")
        @Expose
        private String image4;
        @SerializedName("Image5")
        @Expose
        private String image5;
        @SerializedName("Image6")
        @Expose
        private String image6;
        @SerializedName("CreatedDate")
        @Expose
        private String createdDate;
        @SerializedName("FullName")
        @Expose
        private String fullName;
        @SerializedName("Mobile")
        @Expose
        private String mobile;
        @SerializedName("VerificationCode")
        @Expose
        private String verificationCode;
        @SerializedName("VerificationDate")
        @Expose
        private String verificationDate;
        @SerializedName("Remarks")
        @Expose
        private String remarks;
        @SerializedName("Status")
        @Expose
        private Integer status;

        public String getPetitionID() {
            return petitionID;
        }

        public void setPetitionID(String petitionID) {
            this.petitionID = petitionID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage1() {
            return image1;
        }

        public void setImage1(String image1) {
            this.image1 = image1;
        }

        public String getImage2() {
            return image2;
        }

        public void setImage2(String image2) {
            this.image2 = image2;
        }

        public String getImage3() {
            return image3;
        }

        public void setImage3(String image3) {
            this.image3 = image3;
        }

        public String getImage4() {
            return image4;
        }

        public void setImage4(String image4) {
            this.image4 = image4;
        }

        public String getImage5() {
            return image5;
        }

        public void setImage5(String image5) {
            this.image5 = image5;
        }

        public String getImage6() {
            return image6;
        }

        public void setImage6(String image6) {
            this.image6 = image6;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getVerificationCode() {
            return verificationCode;
        }

        public void setVerificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
        }

        public String getVerificationDate() {
            return verificationDate;
        }

        public void setVerificationDate(String verificationDate) {
            this.verificationDate = verificationDate;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}

