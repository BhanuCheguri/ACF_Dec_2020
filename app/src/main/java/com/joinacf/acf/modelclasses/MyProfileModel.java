package com.joinacf.acf.modelclasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyProfileModel {

    @SerializedName("MemberID")
    @Expose
    private String memberID;
    @SerializedName("FullName")
    @Expose
    private String fullName;
    @SerializedName("Mobile")
    @Expose
    private String mobile;
    @SerializedName("Email")
    @Expose
    private String email;
    @SerializedName("Gender")
    @Expose
    private String gender;
    @SerializedName("Photo")
    @Expose
    private String photo;
    @SerializedName("MemberType")
    @Expose
    private String memberType;
    @SerializedName("RegDate")
    @Expose
    private String regDate;
    @SerializedName("Status")
    @Expose
    private Integer status;

    public MyProfileModel(String MemberID, String FullName, String Mobile, String Email, String Gender, String Photo, String MemberType, String RegDate, Integer Status)
    {
        this.memberID = MemberID;
        this.fullName = FullName;
        this.mobile = Mobile;
        this.email = Email;
        this.gender = Gender;
        this.photo  = Photo;
        this.memberType = MemberType;
        this.regDate = RegDate;
        this.status = Status;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
