package com.joinacf.acf.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import io.fabric.sdk.android.Fabric;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Person;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.joinacf.acf.R;
import com.joinacf.acf.databinding.ActivityNewLoginBinding;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joinacf.acf.modelclasses.AddMemberResult;
import com.joinacf.acf.modelclasses.DashboardCategories;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.PeopleApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NewLoginActivity extends BaseActivity implements View.OnClickListener ,GoogleApiClient.OnConnectionFailedListener{

    ActivityNewLoginBinding binding;
    private static final String TAG = "NewLoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mSignInButton;

    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    //LoginButton loginButton;
    CallbackManager callbackManager;
    private FirebaseAnalytics mFirebaseAnalytics;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    APIRetrofitClient apiRetrofitClient;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private WeakReference<NewLoginActivity> weakAct = new WeakReference<>(this);

    int status;
    private String personName,personFamilyName,personGivenName,personEmail,personId,personGender,personBday;
    private String image_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiRetrofitClient = new APIRetrofitClient();

        Fabric.with(this, new Crashlytics());
        try {
           // Crashlytics.getInstance().crash();

            boolean bLoggedIn = getBooleanSharedPreference(NewLoginActivity.this, "LoggedIn");
            if (bLoggedIn) {
                putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", false);
                Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                FacebookSdk.sdkInitialize(this.getApplicationContext());

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                binding = DataBindingUtil.setContentView(this, R.layout.activity_new_login);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
                }

                /***** G+ Login *****/

                Scope myScope = new Scope("https://www.googleapis.com/auth/user.birthday.read");
                Scope myScope2 = new Scope(Scopes.PLUS_ME);
                Scope myScope3 = new Scope(Scopes.PROFILE); //get name and id

                mFirebaseAuth = FirebaseAuth.getInstance();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestScopes(myScope , new Scope(Scopes.PLUS_LOGIN), new Scope(Scopes.PROFILE))
                        //.requestScopes(new Scope(PeopleApi.CONTACT_SCOPE), new Scope(PeopleApi.BIRTHDAY_SCOPE))
                        .requestEmail()
                        .requestProfile()
                        .build();
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                        .addApi(Plus.API)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
                mGoogleApiClient.connect(); // <-- move to here.

                mAuth = FirebaseAuth.getInstance();
                mAuthListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // User is signed in
                            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        } else {
                            // User is signed out
                            Log.d(TAG, "onAuthStateChanged:signed_out");
                        }
                    }
                };

                binding.signInButton.setOnClickListener(this);


                /***** Facebook Login *****/
                callbackManager = CallbackManager.Factory.create();

                MediaController mediaController= new MediaController(this);
                mediaController.setAnchorView(binding.videoView);
                //Location of Media File
                Uri vidUri = Uri.parse("http://techslides.com/demos/sample-videos/small.mp4");
                //Starting VideView By Setting MediaController and URI
                binding.videoView.setMediaController(mediaController);
                binding.videoView.setVideoURI(vidUri);
                binding.videoView.requestFocus();
                ///binding.videoView.start();

                //facebookLogin();
            }
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }


    public class FacebookAyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewLoginActivity.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog(NewLoginActivity.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            facebookLogin();
            return null;
        }
    }


    public class GPlusAyncTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewLoginActivity.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog(NewLoginActivity.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            gplusLogin();
            return null;
        }
    }

    private void gplusLogin()
    {
        try {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void facebookLogin()
    {
        try {
            boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

            if (!loggedOut) {
                //Using Graph API
                getUserProfile(AccessToken.getCurrentAccessToken());
            }
            AccessTokenTracker fbTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                    if (accessToken2 == null) {
                        Toast.makeText(getApplicationContext(), "User Logged  out successfully.", Toast.LENGTH_LONG).show();
                    }
                }
            };
            fbTracker.startTracking();

            //binding.loginButton.setReadPermissions(Arrays.asList("email", "public_profile","user_birthday"));
            binding.loginButton.setReadPermissions("user_friends");
            binding.loginButton.setReadPermissions("public_profile");
            binding.loginButton.setReadPermissions("email");
            binding.loginButton.setReadPermissions("user_birthday");

            binding.loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
                    if (!loggedOut) {
                        Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());
                        getUserProfile(AccessToken.getCurrentAccessToken());
                    }
                }

                @Override
                public void onCancel() {
                    System.out.println("onCancel");
                    Crashlytics.log("onCancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    System.out.println("onError");
                    Crashlytics.logException(exception);
                }
            });

            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }catch (Exception e) {
            Crashlytics.logException(e);
            Toast.makeText(NewLoginActivity.this, "Facebook login failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.login_button:
            case R.id.facebook:
                try {
                    /*LoginButton loginButton  = new LoginButton(this);
                    loginButton.performClick();*/
                    if(isAppInstalled(getApplicationContext(), "com.facebook.katana")) {
                        new FacebookAyncTask().execute();
                    }else {
                        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.katana"));
                        startActivity(i);
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
                break;
            case R.id.sign_in_button:
            case R.id.google:
                new GPlusAyncTask().execute();
                break;
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        try {
            GraphRequest request = GraphRequest.newMeRequest(
                    currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.d("TAG", object.toString());
                            try {
                                if(object.has("first_name")) {
                                    String personFirstName = object.getString("first_name");
                                }
                                if(object.has("last_name")) {
                                    String personLastEmail = object.getString("last_name");
                                }
                                if(object.has("first_name") && object.has("last_name")) {
                                    personName = object.getString("first_name") + " " + object.getString("last_name");
                                }
                                if(object.has("email")) {
                                    personEmail = object.getString("email");
                                }
                                if(object.has("birthday")) {
                                    personBday = object.getString("birthday"); // 01/31/1980 format
                                }
                                if(object.has("gender")) {
                                    personGender = object.getString("gender");
                                }
                                if(object.has("id")) {
                                    personId = object.getString("id");
                                    image_url = "https://graph.facebook.com/" + personId + "/picture?type=large";
                                }

                                //Toast.makeText(NewLoginActivity.this, personName + " " + personEmail, Toast.LENGTH_LONG).show();
                                putStringSharedPreference(NewLoginActivity.this, "LoginType", "Facebook");
                                putStringSharedPreference(NewLoginActivity.this, "personName", personName);
                                putStringSharedPreference(NewLoginActivity.this, "personEmail", personEmail);
                                putStringSharedPreference(NewLoginActivity.this, "personId", personId);
                                putStringSharedPreference(NewLoginActivity.this, "mobile", "9177579498");
                                putStringSharedPreference(NewLoginActivity.this, "personPhoto", image_url);

                                //Integer nStatus = getValidateMember(personEmail);
                                String Mobile = "9177579498";
                                String Gender = "F";
                                String Photo = "Photo.jpg";
                                JSONObject jsonParam = prepareAddMemberJSON(personName,Mobile,personEmail,Gender,image_url);

                                postAddMember(jsonParam.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "first_name,last_name,email,id,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();

            /*new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    personId,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            *//* handle the result *//*
                            String result  = response.toString();
                            Log.d("TAG", response.toString());
                        }
                    }
            ).executeAsync();*/
        }catch (Exception e) {
            Crashlytics.logException(e);
            Toast.makeText(NewLoginActivity.this, "Failed to getUserProfile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        Crashlytics.log(connectionResult.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                //handleSignInResult(result);
                if (result.isSuccess()) {
                    // Google Sign-In was successful, authenticate with Firebase
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    if (account != null) {

                        new GetProfileDetails(account, weakAct, TAG).execute();

                        personName = account.getDisplayName();
                        personGivenName = account.getGivenName();
                        personFamilyName = account.getFamilyName();
                        personEmail = account.getEmail();
                        personId = account.getId();
                        image_url = account.getPhotoUrl().toString();

                        //Uri personPhoto = account.getPhotoUrl();
                        putStringSharedPreference(NewLoginActivity.this, "LoginType", "Google");
                        putStringSharedPreference(NewLoginActivity.this, "personName", personName);
                        putStringSharedPreference(NewLoginActivity.this, "personEmail", personEmail);
                        putStringSharedPreference(NewLoginActivity.this, "personId", personId);
                        putStringSharedPreference(NewLoginActivity.this, "mobile", "9177579498");
                        putStringSharedPreference(NewLoginActivity.this, "personId", personId);
                        putStringSharedPreference(NewLoginActivity.this, "personPhoto", image_url);

                        String Mobile = "9177579498";
                        String Gender = "F";
                        String Photo = "Photo.jpg";
                        JSONObject jsonParam = prepareAddMemberJSON(personName,Mobile,personEmail,personGender,image_url);
                        postAddMember(jsonParam.toString());
                    }
                } else {
                    // Google Sign-In failed
                    Toast.makeText(NewLoginActivity.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Google Sign-In failed. resultCode"+resultCode);
                    Log.e(TAG, "Google Sign-In failed result"+result);
                }
            }catch (Exception e) {
                Crashlytics.logException(e);
                Toast.makeText(NewLoginActivity.this, "Google Sign-In Exception", Toast.LENGTH_SHORT).show();

            }
        }
    }

    static class GetProfileDetails extends AsyncTask<Void, Void, Person> {

        private PeopleService ps;
        private int authError = -1;
        private WeakReference<NewLoginActivity> weakAct;
        private String TAG;

        GetProfileDetails(GoogleSignInAccount account, WeakReference<NewLoginActivity> weakAct, String TAG) {
            this.TAG = TAG;
            this.weakAct = weakAct;
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this.weakAct.get(), Collections.singleton(Scopes.PROFILE));
            credential.setSelectedAccount(new Account(account.getEmail(), "com.google"));
            HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            ps = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("Google Sign In Quickstart")
                    .build();
        }

        @Override
        protected Person doInBackground(Void... params) {
            Person meProfile = null;
            try {
                meProfile = ps
                        .people()
                        .get("people/me")
                        .setPersonFields("names,genders,birthdays")
                        .execute();
            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();
                authError = 0;
            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
                authError = 1;
            } catch (IOException e) {
                e.printStackTrace();
                authError = 2;
            }
            return meProfile;
        }

        @Override
        protected void onPostExecute(Person meProfile) {
            NewLoginActivity mainAct = weakAct.get();
            if (mainAct != null) {
                mainAct.printBasic();
                if (authError == 0) { //app has been revoke, re-authenticated required.
                    mainAct.reqPerm();
                } else if (authError == 1) {
                    Log.w(TAG, "People API might not enable at" +
                            " https://console.developers.google.com/apis/library/people.googleapis.com/?project=<project name>");
                } else if (authError == 2) {
                    Log.w(TAG, "API io error");
                } else {
                    if (meProfile != null) {
                        mainAct.saveAdvanced(meProfile);
                        mainAct.printAdvanced();
                    }
                }
            }
        }
    }

    private void saveAdvanced(Person meProfile) {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            List<Gender> genders = meProfile.getGenders();
            if (genders != null && genders.size() > 0) {
                personGender = genders.get(0).getValue();
                Log.d(TAG, "onPostExecute gender: " + personGender);
                putStringSharedPreference(NewLoginActivity.this, "profileGender", personGender);
            } else {
                Log.d(TAG, "onPostExecute no gender if set to private ");
            }
            List<Birthday> birthdays = meProfile.getBirthdays();
            if (birthdays != null && birthdays.size() > 0) {
                for (Birthday b : birthdays) { //birthday still able to get even private, unlike gender
                    Date bdate = b.getDate();
                    if (bdate != null) {
                        String bday, bmonth, byear;
                        if (bdate.getDay() != null) bday = bdate.getDay().toString();
                        else bday = "";
                        if (bdate.getMonth() != null) bmonth = bdate.getMonth().toString();
                        else bmonth = "";
                        if (bdate.getYear() != null) byear = bdate.getYear().toString();
                        else byear = "";
                        System.out.println(bday + " - "+ bmonth + " - "+ byear);
                        personBday = bday + " - "+ bmonth + " - "+ byear;
                    }
                    putStringSharedPreference(NewLoginActivity.this, "profileBday", personBday);
                }
            } else {
                Log.w(TAG, "saveAdvanced no birthday");
            }
            //editor.commit();  //next instruction is print from pref, so don't use apply()
        } else {
            Log.w(TAG, "saveAdvanced no acc");
        }
    }

    private void reqPerm() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void printBasic() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d(TAG, "latest sign in: "
                    + "\n\tPhoto url:" + account.getPhotoUrl()
                    + "\n\tEmail:" + account.getEmail()
                    + "\n\tDisplay name:" + account.getDisplayName()
                    + "\n\tFamily(last) name:" + account.getFamilyName()
                    + "\n\tGiven(first) name:" + account.getGivenName()
                    + "\n\tId:" + account.getId()
                    + "\n\tIdToken:" + account.getIdToken()
            );
        } else {
            Log.w(TAG, "basic info is null");
        }
    }

    private void printAdvanced() {
            account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                SharedPreferences sharedPref = getSharedPreferences(account.getId(), MODE_PRIVATE);
                if (sharedPref.contains("gender")) { //this checking works since null still saved
                    String gender = sharedPref.getString("gender", "");
                    Log.d(TAG, "gender: " + gender);
                    if (sharedPref.contains("bday")) { //this checking works since null still saved
                        String bday = sharedPref.getString("bday", "");
                        String bmonth = sharedPref.getString("bmonth", "");
                        String byear = sharedPref.getString("byear", "");
                        Log.d(TAG, bday + "/" + bmonth + "/" + byear);
                    } else {
                        Log.w(TAG, "failed ot get birthday from pref");
                    }
                    String givenName = sharedPref.getString("givenName", "");
                    String familyName = sharedPref.getString("familyName", "");
                    String id = sharedPref.getString("id", "");
                } else {
                    Log.w(TAG, "failed ot get data from pref -2");
                }

            } else {
                Log.w(TAG, "failed ot get data from pref -1");
            }
        }

    private void postAddMember(String strJson) {
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);

            try {
                showProgressDialog(NewLoginActivity.this,"Please Wait.. We are adding Members");
                JsonObject convertedObject = new Gson().fromJson(strJson, JsonObject.class);
                Call<List<AddMemberResult>> registerCall = api.postAddMember(convertedObject);
                registerCall.enqueue(new Callback<List<AddMemberResult>>() {
                    @Override
                    public void onResponse(Call<List<AddMemberResult>> registerCall, retrofit2.Response<List<AddMemberResult>> response) {
                        try {
                            if(response.body() != null) {
                                Log.e(" responce => ", response.body().toString());
                                List<AddMemberResult> addMemberResult = response.body();
                                if (response.isSuccessful()) {
                                    List<AddMemberResult> lstResult = response.body();
                                    ArrayList<AddMemberResult> lstAddMember = new ArrayList<AddMemberResult>();
                                    for (Object object : lstResult) {
                                        lstResult.add((AddMemberResult) object);
                                    }
                                    hideProgressDialog(NewLoginActivity.this);
                                    status = getValidateMember(personEmail);
                                } else {
                                    hideProgressDialog(NewLoginActivity.this);
                                }
                            }else {
                                hideProgressDialog(NewLoginActivity.this);
                                Toast.makeText(NewLoginActivity.this, "RESPONSE :: " + response.body(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            hideProgressDialog(NewLoginActivity.this);
                            try {
                                Log.e("Tag", "error=" + e.toString());
                                Toast.makeText(NewLoginActivity.this, "ERROR :: "+ e.toString(), Toast.LENGTH_SHORT).show();
                                //hideProgressDialog(OTPVerificationActivity.this);
                            } catch (Resources.NotFoundException e1) {
                                e1.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<List<AddMemberResult>> call, Throwable t) {
                        try {
                            hideProgressDialog(NewLoginActivity.this);
                            Log.e("Tag", "error" + t.toString());
                            Toast.makeText(NewLoginActivity.this, "ERROR :: "+ t.toString(), Toast.LENGTH_SHORT).show();
                            hideProgressDialog(NewLoginActivity.this);
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                });
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
    }

    private int getValidateMember(String Email)
    {
        showProgressDialog(NewLoginActivity.this,"Please wait.. We are validating your Email ID");
        try{
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Call<ResponseBody> call = api.getValidateMember(Email);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String  bodyString = new String(response.body().bytes());
                        Log.v("bodyString ::: ",bodyString);
                        if (response.isSuccessful()) {
                            JSONArray jsonarray = new JSONArray(bodyString);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                status = jsonobject.getInt("Status");
                            }
                            hideProgressDialog(NewLoginActivity.this);
                            if(status == 0) {
                                putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                                putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", true);

                                Toast.makeText(NewLoginActivity.this, "Successfully Registered", Toast.LENGTH_LONG);
                                Intent intent = new Intent(NewLoginActivity.this, OTPVerificationActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }else if(status == 1){
                                putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                                putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", true);

                                Toast.makeText(NewLoginActivity.this, "Already Registered", Toast.LENGTH_LONG);
                                Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }else
                            {
                                showAlert(NewLoginActivity.this,"Error","Something wrong fro our end","OK");
                            }
                        } else {
                            hideProgressDialog(NewLoginActivity.this);
                            Toast.makeText(NewLoginActivity.this, "RESPONSE :: "+ response.body().toString(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        hideProgressDialog(NewLoginActivity.this);
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    } catch (JSONException e) {
                        hideProgressDialog(NewLoginActivity.this);
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    hideProgressDialog(NewLoginActivity.this);
                }
            });
        }catch (Exception e)
        {
            Crashlytics.logException(e);
            hideProgressDialog(NewLoginActivity.this);
        }
        return status;
    }

    private JSONObject prepareAddMemberJSON(String personName, String mobile, String personEmail, String gender, String photo){

        try {
            JSONObject json = new JSONObject();
            json.put("fullname", personName);
            json.put("mobile", mobile);
            json.put("email", personEmail);
            json.put("gender", gender);
            json.put("photo", photo);

            return json;
        }catch (Exception e)
        {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        try{
            if (result.isSuccess()) {

                final GoogleSignInAccount acct = result.getSignInAccount();

                String name = acct.getDisplayName();
                final String mail = acct.getEmail();
                // String photourl = acct.getPhotoUrl().toString();

                final String givenname="",familyname="",displayname="",birthday="";

               /* Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
                        Person person = loadPeopleResult.getPersonBuffer().get(0);

                        Log.d("GivenName ", person.getName().getGivenName());
                        Log.d("FamilyName ",person.getName().getFamilyName());
                        Log.d("DisplayName ",person.getDisplayName());
                        Log.d("gender ", String.valueOf(person.getGender())); //0 = male 1 = female
                        String gender="";
                        if(person.getGender() == 0){
                            gender = "Male";
                        }else {
                            gender = "Female";
                        }
                        Log.d("Gender ",gender);
                        if(person.hasBirthday()) {
                            Log.d("Birthday ", person.getBirthday());
                        }
                    }
                });*/
            } else {

                Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", false);
            putStringSharedPreference(NewLoginActivity.this, "LoginType", "");
            putStringSharedPreference(NewLoginActivity.this, "personName", "");
            putStringSharedPreference(NewLoginActivity.this, "personEmail", "");
            putStringSharedPreference(NewLoginActivity.this, "personId", "");
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }
}
