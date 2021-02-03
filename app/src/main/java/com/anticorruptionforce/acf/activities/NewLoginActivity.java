package com.anticorruptionforce.acf.activities;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.anticorruptionforce.acf.utilities.Utils;
import com.facebook.login.LoginBehavior;
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
import com.anticorruptionforce.acf.R;
import com.anticorruptionforce.acf.databinding.ActivityNewLoginBinding;
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
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.anticorruptionforce.acf.modelclasses.AddMemberResult;
import com.anticorruptionforce.acf.modelclasses.StatusResponse;
import com.anticorruptionforce.acf.network.APIInterface;
import com.anticorruptionforce.acf.network.APIRetrofitClient;
import com.anticorruptionforce.acf.utilities.App;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.pd.chocobar.ChocoBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
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
    //private FirebaseAnalytics mFirebaseAnalytics;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    APIRetrofitClient apiRetrofitClient;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    private WeakReference<NewLoginActivity> weakAct = new WeakReference<>(this);

    private String personName,personFamilyName,personGivenName,personEmail,personId,personGender,personBday;
    private String image_url;
    String Mobile = "";
    int nStatusValue = -1;
    List<StatusResponse.Result> mystatusResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiRetrofitClient = new APIRetrofitClient();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_login);

        try {
            boolean bLoggedIn = getBooleanSharedPreference(NewLoginActivity.this, "LoggedIn");
            boolean bAdminLogIn = getBooleanSharedPreference(NewLoginActivity.this, "AdminLogin");

            if (bLoggedIn) {
                putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", false);
                Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else if(bAdminLogIn){
                boolean bMLogIn = getBooleanSharedPreference(NewLoginActivity.this, "AdminLoginForM");
                boolean bSPLogIn = getBooleanSharedPreference(NewLoginActivity.this, "AdminLoginForSP");
                if(bSPLogIn)
                {
                    Intent intent = new Intent(NewLoginActivity.this,ServiceProviderActivity.class);
                    startActivity(intent);
                    finish();
                }else if(bMLogIn){
                    Intent intent = new Intent(NewLoginActivity.this, ModeratorActivity.class);
                    startActivity(intent);
                    finish();
                }
            }else {
                FacebookSdk.sdkInitialize(this.getApplicationContext());
                callbackManager = CallbackManager.Factory.create();

                //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

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

                if(App.isNetworkAvailable())
                    new AsyncGetVideoLink().execute();
                else{
                    ChocoBar.builder().setView(binding.linearLayout)
                            .setText("No Internet connection")
                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                            //.setActionText(android.R.string.ok)
                            .red()   // in built red ChocoBar
                            .show();
                }
                    //Snackbar.make(coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT).show();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
        }

        binding.notaMember.setOnClickListener(this);

    }


    public class AsyncGetVideoLink extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewLoginActivity.this,"Buffering video please wait...");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(NewLoginActivity.this);
            //Location of Media File
            try {
                Uri vidUri = Uri.parse(result);
                binding.videoView.setVideoURI(vidUri);
                binding.videoView.requestFocus();
                //binding.videoView.start();
                binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                    }
                });

                binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        binding.videoView.start();
                        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                            @Override
                            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                MediaController mediaController = new MediaController(NewLoginActivity.this);
                                binding.videoView.setMediaController(mediaController);
                                mediaController.setAnchorView(binding.videoView);
                            }
                        });
                    }
                });

                binding.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        Log.d("video", "setOnErrorListener ");
                        return true;
                    }
                });
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String strVideoLink = getVideoLink();
            return strVideoLink;
        }
    }

    private String getVideoLink() {
        String result = "";
        String inputLine= "";
        String REQUEST_METHOD = "GET";
        int READ_TIMEOUT = 15000;
        int CONNECTION_TIMEOUT = 15000;
        try {
            URL myUrl = new URL("http://api.ainext.in/moderation/getvideolink");
            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.connect();
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            result = stringBuilder.toString();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public class FacebookAyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewLoginActivity.this,"Login-In to Facebook");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressDialog(NewLoginActivity.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                facebookLogin();
            }catch (Exception e){
                //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                e.printStackTrace();
                Toast.makeText(NewLoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }


    public class GPlusAyncTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewLoginActivity.this,"Login-In to Google");
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
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }catch (Exception e) {
            e.printStackTrace();
           // FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
        }
    }

    private void facebookLogin()
    {
        try {
            /*boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
            if (!loggedOut) {
                return;
            }*/
            AccessTokenTracker fbTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                    if (accessToken2 == null) {
                        Toast.makeText(getApplicationContext(), "User Logged  out successfully.", Toast.LENGTH_LONG).show();
                    }
                }
            };
            fbTracker.startTracking();
            /*binding.loginButton.setReadPermissions("user_friends");
            binding.loginButton.setReadPermissions("public_profile");
            binding.loginButton.setReadPermissions("email");
            binding.loginButton.setReadPermissions("user_birthday");*/

            List<String> permission =
                    new ArrayList<>();
            permission.add("email");
            permission.add("public_profile");
            permission.add("user_friends");


            binding.loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
                    if (!loggedOut) {
                        Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());
                        if(App.isNetworkAvailable())
                            getUserProfile(loginResult.getAccessToken());
                        else{
                            ChocoBar.builder().setView(binding.linearLayout)
                                    .setText("No Internet connection")
                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                    //.setActionText(android.R.string.ok)
                                    .red()   // in built red ChocoBar
                                    .show();
                        }
                    }
                }

                @Override
                public void onCancel() {
                    System.out.println("onCancel");
                    //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", "Oncancel");
                }

                @Override
                public void onError(FacebookException e) {
                    System.out.println("onError");
                   // FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(NewLoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            });
            LoginManager.getInstance().setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK).logInWithReadPermissions(this,permission);
            //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }catch (Exception e) {
            //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
            e.printStackTrace();
            Toast.makeText(NewLoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
            case R.id.notaMember:
                Intent intent = new Intent(NewLoginActivity.this, AdminLogin.class);
                startActivity(intent);
                finish();
                break;
            case R.id.login_button:
            case R.id.facebook:
                try {
                    if(isAppInstalled(getApplicationContext(), "com.facebook.katana")) {
                        try {
                            if(App.isNetworkAvailable())
                                new FacebookAyncTask().execute();
                            else{
                                ChocoBar.builder().setView(binding.linearLayout)
                                        .setText("No Internet connection")
                                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                        //.setActionText(android.R.string.ok)
                                        .red()   // in built red ChocoBar
                                        .show();
                            }
                        }catch (Exception e) {
                            //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(NewLoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                        Toast.makeText(NewLoginActivity.this, "App not installed", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                }
                break;
            case R.id.sign_in_button:
            case R.id.google:
                if(App.isNetworkAvailable())
                    new GPlusAyncTask().execute();
                else{
                    ChocoBar.builder().setView(binding.linearLayout)
                            .setText("No Internet connection")
                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                            //.setActionText(android.R.string.ok)
                            .red()   // in built red ChocoBar
                            .show();
                }
                break;
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        try {
            Toast.makeText(this, "GetUserProfileMethod", Toast.LENGTH_SHORT).show();
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
                                if(object.has("id")) {
                                    personId = object.getString("id");
                                    image_url = "https://graph.facebook.com/" + personId + "/picture?type=large";
                                }
                                putStringSharedPreference(NewLoginActivity.this, "LoginType", "Facebook");
                                putStringSharedPreference(NewLoginActivity.this, "personName", personName);
                                putStringSharedPreference(NewLoginActivity.this, "personEmail", personEmail);
                                putStringSharedPreference(NewLoginActivity.this, "personId", personId);
                                putStringSharedPreference(NewLoginActivity.this, "mobile", "");
                                putStringSharedPreference(NewLoginActivity.this, "personPhoto", image_url);

                                if(App.isNetworkAvailable())
                                    getValidateMember(personEmail);
                                else{
                                    ChocoBar.builder().setView(binding.linearLayout)
                                            .setText("No Internet connection")
                                            .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                            //.setActionText(android.R.string.ok)
                                            .red()   // in built red ChocoBar
                                            .show();
                                }

                            } catch (JSONException e) {
                                //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(NewLoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "first_name,last_name,email,id,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }catch (Exception e) {
            //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
            e.printStackTrace();
            Toast.makeText(NewLoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", connectionResult.toString());
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

                        if(App.isNetworkAvailable())
                            new GetProfileDetails(account, weakAct, TAG).execute();
                        else{
                            ChocoBar.builder().setView(binding.linearLayout)
                                    .setText("No Internet connection")
                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                    //.setActionText(android.R.string.ok)
                                    .red()   // in built red ChocoBar
                                    .show();
                        }

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
                        putStringSharedPreference(NewLoginActivity.this, "mobile", "");
                        putStringSharedPreference(NewLoginActivity.this, "personId", personId);
                        putStringSharedPreference(NewLoginActivity.this, "personPhoto", image_url);

                        //String Gender = "F";
                        //String Photo = "Photo.jpg";
                    }
                } else {
                    // Google Sign-In failed
                    Toast.makeText(NewLoginActivity.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Google Sign-In failed. resultCode"+resultCode);
                    Log.e(TAG, "Google Sign-In failed result"+result);
                    Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                e.printStackTrace();
               // FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                Toast.makeText(NewLoginActivity.this, "Google Sign-In Exception", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public class AsyncTaskAddMember extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(NewLoginActivity.this,"Please wait.. ");
        }
        @Override
        protected String doInBackground(String... strings) {
            String emailId = "";
            try {
                JSONObject jsonParam = prepareAddMemberJSON(personName,Mobile,personEmail,personGender,image_url);
                String result = postAddMember(jsonParam.toString());
                if(!result.equalsIgnoreCase("") && result != null) {
                    emailId = personEmail;
                    JSONObject jobject = new JSONObject(result);
                    if (result.length() > 0) {
                        AddMemberResult addMember = new AddMemberResult();
                        addMember.setMessage(jobject.getString("message"));
                        addMember.setStatus(jobject.getString("status"));
                        AddMemberResult.Result addMemberResult = new AddMemberResult.Result();
                        if (jobject.has("message")) {
                            if (addMember.getMessage().equalsIgnoreCase("SUCCESS"))
                                if (jobject.has("result")) {
                                    JSONArray jsonArray = new JSONArray(jobject.getString("result"));
                                    System.out.println(jobject.toString());
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonobject = jsonArray.getJSONObject(i);
                                        addMemberResult.setMemberID(jsonobject.getString("MemberID"));
                                        addMemberResult.setFullName(jsonobject.getString("FullName"));
                                        addMemberResult.setEmail(jsonobject.getString("Email"));

                                        if (jsonobject.has("Mobile"))
                                            addMemberResult.setMobile(jsonobject.getString("Mobile"));
                                        if (jsonobject.has("Photo"))
                                            addMemberResult.setPhoto(jsonobject.getString("Photo"));

                                        addMemberResult.setMemberType(jsonobject.getString("MemberType"));
                                        addMemberResult.setRegDate(jsonobject.getString("RegDate"));
                                        addMemberResult.setStatus(jsonobject.getInt("Status"));
                                        addMemberResult.setModifiedBy(jsonobject.getString("ModifiedBy"));
                                        addMemberResult.setModifiedDate(jsonobject.getString("ModifiedDate"));
                                        Log.i("MemberID" ,jsonobject.getString("MemberID"));
                                        putStringSharedPreference(NewLoginActivity.this, "MemberID", jsonobject.getString("MemberID"));
                                    }
                                }
                        }
                        else if (addMember.getMessage().equalsIgnoreCase("ERROR")) {
                            showAlert(NewLoginActivity.this, "Error", jobject.getString("result"), "OK");
                        } else if (addMember.getMessage().equalsIgnoreCase("FAILURE")) {
                            showAlert(NewLoginActivity.this, "Failed",jobject.getString("result"), "OK");
                        }
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", ex.getMessage());
                Utils.showErrorAlert(NewLoginActivity.this,"Google Login Failed. Details were not added to Server");
                Toast.makeText(NewLoginActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                return emailId;
            } catch (Exception e)
            {
                e.printStackTrace();
                //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
                //Toast.makeText(NewLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return emailId;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressDialog(NewLoginActivity.this);
            if(!result.equalsIgnoreCase("")) {
                if (nStatusValue == 0) {
                    putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                    putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", false);

                    Toast.makeText(NewLoginActivity.this, "Successfully Registered", Toast.LENGTH_LONG);
                    Intent intent = new Intent(NewLoginActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("email", result);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else if (nStatusValue == 1) {
                    putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                    putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", true);

                    Toast.makeText(NewLoginActivity.this, "Already Registered", Toast.LENGTH_LONG);
                    Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    showAlert(NewLoginActivity.this, "Error", "Something wrong fro our end", "OK");
                }
            }else
                Utils.showErrorAlert(NewLoginActivity.this,"Google Login Failed. Details were not added to Server");
        }
    }

    public class GetProfileDetails extends AsyncTask<Void, Void, Person> {

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
            if(meProfile != null){
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
                }else
                {
                    Log.d(TAG, "String.valueOf(meProfile)" + meProfile);
                }
            }
            if(App.isNetworkAvailable())
                getValidateMember(personEmail);
            else{
                ChocoBar.builder().setView(binding.linearLayout)
                        .setText("No Internet connection")
                        .setDuration(ChocoBar.LENGTH_INDEFINITE)
                        //.setActionText(android.R.string.ok)
                        .red()   // in built red ChocoBar
                        .show();
            }
        }
    }

    private void saveAdvanced(Person meProfile) {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            List<Gender> genders = meProfile.getGenders();
            if (genders != null && genders.size() > 0) {
                if(genders.get(0).getValue() != null) {
                    personGender = genders.get(0).getValue();
                    if (personGender == null)
                        personGender = "";
                }
                else
                    personGender = "";
                Log.d(TAG, "onPostExecute gender: " + personGender);
                putStringSharedPreference(NewLoginActivity.this, "profileGender", personGender);
            } else {
                Log.d(TAG, "onPostExecute no gender if set to private ");
                personGender = "";
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
                personBday = "";
            }
            //editor.commit();  //next instruction is print from pref, so don't use apply()
        } else {
            Log.w(TAG, "saveAdvanced no acc");
            personBday = "";
        }
    }

    private void reqPerm() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

    private String postAddMember(String jsonParam) {

        try {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("http://api.ainext.in/members/addmember");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(jsonParam);
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                String JsonResponse = buffer.toString();
                //response data
                Log.i(TAG, JsonResponse);
                return JsonResponse;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;

            /*Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
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
            }*/
    }

    private void getValidateMember(String Email)
    {
        showProgressDialog(NewLoginActivity.this,"Please wait.. We are validating your Email ID");
        try{
            Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
            APIInterface api = retrofit.create(APIInterface.class);
            Call<StatusResponse> call = api.getValidateMember(Email);

            call.enqueue(new Callback<StatusResponse>() {
                @Override
                public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                    System.out.println("getValidateMember:"+ response);
                    hideProgressDialog(NewLoginActivity.this);
                    if(response.code() == 200)
                    {
                        StatusResponse myResponseData = response.body();
                        if(myResponseData != null) {
                            String status = myResponseData.getstatus();
                            String msg = myResponseData.getMessage();
                            if (msg.equalsIgnoreCase("SUCCESS")) {
                                mystatusResult = myResponseData.getResult();
                                for (int i = 0; i < mystatusResult.size(); i++) {
                                    nStatusValue = mystatusResult.get(i).getResult();

                                    //if (nStatusValue == 0 ) {
                                        if(App.isNetworkAvailable())
                                            new AsyncTaskAddMember().execute();
                                        else{
                                            ChocoBar.builder().setView(binding.linearLayout)
                                                    .setText("No Internet connection")
                                                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                                                    //.setActionText(android.R.string.ok)
                                                    .red()   // in built red ChocoBar
                                                    .show();
                                        }
                                    /*} else if (nStatusValue == 1) {
                                        putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                                        putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", true);

                                        Toast.makeText(NewLoginActivity.this, "Already Registered", Toast.LENGTH_LONG);
                                        Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }else {
                                        showAlert(NewLoginActivity.this, "Error", "Something wrong fro our end", "OK");
                                    }*/
                                }
                            }
                        }


                        else {
                            hideProgressDialog(NewLoginActivity.this);
                            //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", response.body().toString());
                            Toast.makeText(NewLoginActivity.this, "RESPONSE :: " + response.body().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<StatusResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    hideProgressDialog(NewLoginActivity.this);
                }
            });
        }catch (Exception e)
        {
            e.printStackTrace();
            //FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
            hideProgressDialog(NewLoginActivity.this);
        }
    }

    private JSONObject prepareAddMemberJSON(String personName, String mobile, String personEmail, String gender, String photo){

        try {
            JSONObject json = new JSONObject();

            if(personName == null) {
                if (personName.equalsIgnoreCase("")||(personName == null))
                    json.put("fullname", "");
            }
            else
                json.put("fullname", personName);

            if(mobile == null) {
                if (mobile.equalsIgnoreCase("") || mobile == null)
                    json.put("mobile", "");
            }
            else
                json.put("mobile", mobile);

            if(personEmail != null) {
                json.put("email", personEmail);
            }else
                json.put("email", "");

            if(gender == null) {
                if (gender.equalsIgnoreCase("") || gender == null)
                    json.put("gender", "");
            }
            else
                json.put("gender", gender);

            if(photo == null) {
                if (photo.equalsIgnoreCase("") || photo == null)
                    json.put("photo", "");
            }
            else
                json.put("photo", photo);

            return json;
        }catch (Exception e)
        {
            e.printStackTrace();
           // FirebaseCrashlytics.getInstance().setCustomKey("NewLoginActivity", e.getMessage());
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
