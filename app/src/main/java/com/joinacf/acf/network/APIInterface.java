package com.joinacf.acf.network;

import com.google.gson.JsonObject;
import com.joinacf.acf.modelclasses.AddMemberResult;
import com.joinacf.acf.modelclasses.AddPetitionRequest;
import com.joinacf.acf.modelclasses.AddPetitionResult;
import com.joinacf.acf.modelclasses.DashboardCategories;
import com.joinacf.acf.modelclasses.MyPostsModel;
import com.joinacf.acf.modelclasses.MyProfileModel;
import com.joinacf.acf.modelclasses.WallPostsModel;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIInterface {

    String BASE_URL = "http://api.ainext.in/";
    String ADD_PETITION = BASE_URL + "petitions/addpetition";
    //String GET_POSTS =  http://api.ainext.in/posts/getposts?categoryID=1&days=-1
    //String GET_MEMBERS =  BASE_URL + "members/getmembers?mobile=9032200318/";
    String ADD_MEMBERS =  BASE_URL + "members/addmember";
    //http://api.ainext.in/members/validateotp?mobile=9032200318&otp=74340
    //http://api.ainext.in/members/validateotp?mobile=9032200318&otp=74340

    @GET("members/validatemember?")
    Call<ResponseBody> getValidateMember(@Query("email") String email);


    @GET("members/getmembers?")
    Call<List<MyProfileModel>> getProfileDetails(@Query("mobile") String mobile);

    @GET("members/validateotp?")
    Call<ResponseBody> getValidateOTPStatus(@Query("mobile") String mobile,@Query("otp") String otp);

    @GET("members/sendsms?")
    Call<ResponseBody> getSMSOTP(@Query("mobile") String mobile);

   /*@GET("posts/getposts?categoryID=1&days=-1")
    Call<List<WallPostsModel>> getWallPostDetails();*/

    @GET("posts/getposts?")
    Call<List<WallPostsModel>> getWallPostDetails(@Query("categoryID") String categoryID, @Query("days") String days);

   // @Headers("Content-Type: application/json")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("members/addmember")
    Call<List<AddMemberResult>> postAddMember(@Body JsonObject jsonBody);

    /*@POST("posts")
    Call<AddMemberResult> postData(@Body JsonObject body);*/

    @Multipart
    @POST("posts/upload")
    Call<JsonObject> uploadImage(@Part MultipartBody.Part image);



    @GET("posts/getcategories?")
    Call<List<DashboardCategories>> getDashboardCategories();

    @Headers({"Accept:application/json", "Content-Type:application/json;"})
    @POST("petitions/addpetition")
    Call<AddPetitionResult> postPetitionData(@Body AddPetitionRequest body);

    @GET("petitions/getmypetitions?")
    Call<List<MyPostsModel>> getMyPostings(@Query("memberID") String memberID);


}
