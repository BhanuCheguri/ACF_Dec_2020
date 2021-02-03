package com.anticorruptionforce.acf.network;

import com.google.gson.JsonObject;
import com.anticorruptionforce.acf.modelclasses.AddMemberResult;
import com.anticorruptionforce.acf.modelclasses.AddPetitionRequest;
import com.anticorruptionforce.acf.modelclasses.AddPetitionResult;
import com.anticorruptionforce.acf.modelclasses.DashboardCategories;
import com.anticorruptionforce.acf.modelclasses.KnowYourActModel;
import com.anticorruptionforce.acf.modelclasses.ModeratorListModel;
import com.anticorruptionforce.acf.modelclasses.MyPostingModel;
import com.anticorruptionforce.acf.modelclasses.MyProfileModel;
import com.anticorruptionforce.acf.modelclasses.NewComplaintDataRequest;
import com.anticorruptionforce.acf.modelclasses.OTPResponse;
import com.anticorruptionforce.acf.modelclasses.OfficesModel;
import com.anticorruptionforce.acf.modelclasses.ProviderModel;
import com.anticorruptionforce.acf.modelclasses.ResultModel;
import com.anticorruptionforce.acf.modelclasses.SPLoginModel;
import com.anticorruptionforce.acf.modelclasses.SectionsModel;
import com.anticorruptionforce.acf.modelclasses.StatusModel;
import com.anticorruptionforce.acf.modelclasses.StatusResponse;
import com.anticorruptionforce.acf.modelclasses.WallPostsModel;
import com.anticorruptionforce.acf.modelclasses.PetitionModel;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface APIInterface {

    String BASE_URL = "http://api.ainext.in/";
    String ADD_PETITION = BASE_URL + "petitions/addpetition";
    //String GET_POSTS =  http://api.ainext.in/posts/getposts?categoryID=1&days=-1
    //String GET_MEMBERS =  BASE_URL + "members/getmembers?mobile=9032200318/";
    String ADD_MEMBERS =  BASE_URL + "members/addmember";
    //http://api.ainext.in/members/validateotp?mobile=9032200318&otp=74340
    //http://api.ainext.in/members/validateotp?mobile=9032200318&otp=74340

    @GET("moderation/getvideolink")
    Call<String> getVideoLink();

    @GET("members/validatemember?")
    Call<StatusResponse> getValidateMember(@Query("email") String email);

    @Headers("Content-Type: application/json")
    @POST("members/updatemobile?")
    Call<ResponseBody> postUpdateMobileNumber(@Body JsonObject jsonBody);

    @Headers("Content-Type: application/json")
    @POST("emergency/updatemlocation")
    Call<ResponseBody> updateMemLocation(@Body JsonObject jsonBody);

    @GET("members/getmembers?")
    Call<MyProfileModel> getProfileDetails(@Query("mobile") String mobile);

    @GET("members/getmembersbyemail?")
    Call<MyProfileModel> getProfileDetailsbyEmail(@Query("email") String email);

    @GET("members/validateotp?")
    Call<ResponseBody> getValidateOTPStatus(@Query("mobile") String mobile,@Query("otp") String otp);

    @GET("members/sendsms?")
    Call<OTPResponse> getSMSOTP(@Query("mobile") String mobile,@Query("email") String email);

    @GET("posts/getposts?")
    Call<WallPostsModel> getWallPostDetails(@Query("categoryID") String categoryID, @Query("days") String days);

    @GET("posts/getcategories?")
    Call<DashboardCategories> getDashboardCategories();

    @Headers("Content-Type: application/json")
    @POST("members/addmember")
    Call<List<AddMemberResult>> postAddMember(@Body JsonObject jsonBody);

    @GET("posts/getmposts?")
    Call<MyPostingModel> getMyPostings(@Query("memberID") String memberID);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("petitions/addpetition")
    Call<AddPetitionResult> postPetitionData(@Body AddPetitionRequest body);

    @GET("petitions/getmypetitions?")
    Call<PetitionModel> getMyPetitions(@Query("memberID") String memberID);

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("posts/addpost?")
    Call<ResponseBody> postNewItem(@Body NewComplaintDataRequest jsonBody);

    @GET("petitions/getofficesbygeo?")
    Call<OfficesModel> getOfficesbyGeo(@Query("lat") String lat, @Query("long") String lang);

    @GET("petitions/getspsections?")
    Call<SectionsModel> getSections(@Query("spid") String SPID);

    //Service provider Calls
    @GET("moderation/AuthenticateModSP?")
    Call<SPLoginModel> getAuthenticateModSP(@Query("mobile") String email, @Query("password") String password);

    @GET("petitions/getsppetitions?")
    Call<PetitionModel> getSPpetitions(@Query("spid") String SPID, @Query("sectionID") String sectionID);

    @GET("petitions/verifyotp?")
    Call<StatusModel> getVerifyOTP(@Query("petitionID") String petitionID, @Query("otp") String otp);

    @GET("moderation/getitemsformod?")
    Call<ModeratorListModel> getitemsformod(@Query("status") String status);

    @GET("moderation/getserviceproviders?")
    Call<ProviderModel> getServiceProviders();

    @GET("moderation/getmoderationstatus?")
    Call<JsonObject> getmoderationstatus();

    @POST("moderation/addmoderation?")
    Call<ResultModel> addModeration(@Body JsonObject jsonBody);

    @Multipart
    @POST("posts/upload")
    Call<JsonObject> uploadImage(@Part MultipartBody.Part image);

    //@Multipart
    @POST("posts/upload")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> uploadImages(@Query("item") String item,@Body RequestBody requestBody);

    @POST("posts/upload")
    Call<JSONObject> uploadMultiFile(@Header("item") String authorization, @Body RequestBody file);

    @POST("petitions/upload")
    Call<ResponseBody> uploadPetitionMultiFile(@Query("pid") String authorization, @Body RequestBody file);

    @GET("moderation/getserviceproviders?")
    Call<KnowYourActModel> getKnowYourActs();


    @POST("posts/upload")
    Call<JSONObject> uploadFiles(@Header("item") String authorization, @Part MultipartBody.Part file);

    @Multipart
    @POST("posts/upload")
    Call<JSONObject> postFile(@PartMap Map<String,RequestBody> Files, @Header("item") String authorization);

    @Multipart
    @POST("posts/upload")
    Call<Response> uploadAlbum(@Part List<MultipartBody.Part> image);

}
