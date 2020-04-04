package com.joinacf.acf.petitions;

public class PetitionModel
{
    String PetitionNo;
    String Petitioner;
    String Status;
    String Complaint;
    String ComplaintDate;
    String Attachments;
    
    public PetitionModel(String strPetitionNo, String strPetitioner, String strStatus, String strComplaint, String strComplaintDate, String strAttachments) {
        PetitionNo = strPetitionNo;
        Petitioner = strPetitioner;
        Status = strStatus;
        Complaint = strComplaint;
        ComplaintDate = strComplaintDate;
        Attachments = strAttachments;
    }

    public String getPetitionNo() {
        return PetitionNo;
    }

    public void setPetitionNo(String petitionNo) {
        PetitionNo = petitionNo;
    }

    public String getPetitioner() {
        return Petitioner;
    }

    public void setPetitioner(String petitioner) {
        Petitioner = petitioner;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getComplaint() {
        return Complaint;
    }

    public void setComplaint(String complaint) {
        Complaint = complaint;
    }

    public String getComplaintDate() {
        return ComplaintDate;
    }

    public void setComplaintDate(String complaintDate) {
        ComplaintDate = complaintDate;
    }

    public String getAttachments() {
        return Attachments;
    }

    public void setAttachments(String attachments) {
        Attachments = attachments;
    }
}
