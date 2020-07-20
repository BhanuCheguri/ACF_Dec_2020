package com.joinacf.acf.network;

import com.google.gson.JsonObject;
import com.joinacf.acf.modelclasses.AddMemberResult;
import com.joinacf.acf.modelclasses.AddPetitionRequest;
import com.joinacf.acf.modelclasses.AddPetitionResult;
import com.joinacf.acf.modelclasses.DashboardCategories;
import com.joinacf.acf.modelclasses.ModeratorListModel;
import com.joinacf.acf.modelclasses.ModeratorStatusModel;
import com.joinacf.acf.modelclasses.MyPostingModel;
import com.joinacf.acf.modelclasses.MyProfileModel;
import com.joinacf.acf.modelclasses.NewComplaintDataRequest;
import com.joinacf.acf.modelclasses.OfficesModel;
import com.joinacf.acf.modelclasses.ProviderModel;
import com.joinacf.acf.modelclasses.ResultModel;
import com.joinacf.acf.modelclasses.SPLoginModel;
import com.joinacf.acf.modelclasses.SectionsModel;
import com.joinacf.acf.modelclasses.StatusModel;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.modelclasses.PetitionModel;

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
    Call<ResponseBody> getValidateMember(@Query("email") String email);

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
    Call<ResponseBody> getSMSOTP(@Query("mobile") String mobile);

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

    @Multipart
    @POST("petitions/upload")
    Call<ResponseBody> uploadPetitionImages(@Query("item") String item,@PartMap Map<String, RequestBody> Files);

    @POST("posts/upload")
    Call<JSONObject> uploadMultiFile(@Header("item") String authorization, @Body RequestBody file);

    @POST("posts/upload")
    Call<JSONObject> uploadFiles(@Header("item") String authorization, @Part MultipartBody.Part file);

    @Multipart
    @POST("posts/upload")
    Call<JSONObject> postFile(@PartMap Map<String,RequestBody> Files, @Header("item") String authorization);

    @Multipart
    @POST("posts/upload")
    Call<Response> uploadAlbum(@Part List<MultipartBody.Part> image);

}
