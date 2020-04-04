package com.joinacf.acf.modelclasses;

public class AddMemberResult {
    String MemberID;
    String FullName;
    String Mobile;
    String Email;
    String Gender;
    String Photo;
    String MemberType;
    String RegDate;
    String Status;
    String ModifiedBy;
    String ModifiedDate;


    /*public AddMemberResult(String FullName, String Mobile,String Email, String Gender, String Photo) {
        this.FullName = FullName;
        this.Mobile = Mobile;
        this.Email = Email;
        this.Gender = Gender;
        this.Photo = Photo;
    }*/

    public String getMemberID() {
        return MemberID;
    }

    public void setMemberID(String memberID) {
        MemberID = memberID;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getMemberType() {
        return MemberType;
    }

    public void setMemberType(String memberType) {
        MemberType = memberType;
    }

    public String getRegDate() {
        return RegDate;
    }

    public void setRegDate(String regDate) {
        RegDate = regDate;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getModifiedBy() {
        return ModifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        ModifiedBy = modifiedBy;
    }

    public String getModifiedDate() {
        return ModifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        ModifiedDate = modifiedDate;
    }
}
