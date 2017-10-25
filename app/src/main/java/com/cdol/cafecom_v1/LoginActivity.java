package com.cdol.cafecom_v1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private LoginButton facebookbtn;
    private SharedPreferences auto;
    private CallbackManager callbackManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)
        setContentView(R.layout.activity_login);

        user = new User();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton googleBtn = (SignInButton) findViewById(R.id.sign_in_google_btn);
        googleBtn.setSize(SignInButton.SIZE_WIDE);
        googleBtn.setScopes(gso.getScopeArray());

        callbackManager = CallbackManager.Factory.create();  //로그인 응답을 처리할 콜백 관리자
        facebookbtn = (LoginButton)findViewById(R.id.sign_in_facebook_btn); //페이스북 로그인 버튼
        //유저 정보, 친구정보, 이메일 정보등을 수집하기 위해서는 허가(퍼미션)를 받아야 합니다.
        facebookbtn.setReadPermissions(Arrays.asList("public_profile", "email"));
        //버튼에 바로 콜백을 등록하는 경우 LoginManager에 콜백을 등록하지 않아도됩니다.
        //반면에 커스텀으로 만든 버튼을 사용할 경우 아래보면 CustomloginButton OnClickListener안에 LoginManager를 이용해서
        //로그인 처리를 해주어야 합니다.
        facebookbtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) { //로그인 성공시 호출되는 메소드
                Log.e("토큰",loginResult.getAccessToken().getToken());
                Log.e("유저아이디",loginResult.getAccessToken().getUserId());
                Log.e("퍼미션 리스트",loginResult.getAccessToken().getPermissions()+"");

                //loginResult.getAccessToken() 정보를 가지고 유저 정보를 가져올수 있습니다.
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken() ,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.e("user profile",object.toString());
                                    String photo = "https://graph.facebook.com/"+object.getString("id")+"/picture";
                                    // facabook channel : 2
                                    user.setId(object.getString("id"));
                                    user.setEmail(object.getString("email"));
                                    user.setName(object.getString("name"));
                                    user.setPhoto(photo);
                                    user.setChannel(2);

                                    logIn(getString(R.string.url), user);

                                    LoginManager.getInstance().logOut();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onError(FacebookException error) { }

            @Override
            public void onCancel() { }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        // Auto Login
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        String loginEmail = auto.getString("inputEmail",null);
        Log.d(TAG, "loginEmail:" + loginEmail);
        //Toast.makeText(this, loginEmail, Toast.LENGTH_LONG).show();
        if(loginEmail != null){
            // MainActivity
            findViewById(R.id.sign_in_google_btn).setVisibility(View.GONE);
            findViewById(R.id.sign_in_facebook_btn).setVisibility(View.GONE);

            user.setEmail(loginEmail);
            //logIn(getString(R.string.url), "logIn", user);

            hideProgressDialog();

            setResult(0);
            finish();
        } else {
            findViewById(R.id.sign_in_google_btn).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_google_btn).setOnClickListener(this);
            findViewById(R.id.sign_in_facebook_btn).setVisibility(View.VISIBLE);
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String userPhoto = "";

            if(acct.getPhotoUrl() != null){
                userPhoto = acct.getPhotoUrl().toString();
            }

            // google channel : 1
            user.setId(acct.getId());
            user.setEmail(acct.getEmail());
            user.setName(acct.getDisplayName());
            user.setPhoto(userPhoto);
            user.setChannel(1);

            logIn(getString(R.string.url), user);

            googleSignOut();

        }
    }
    // [END handleSignInResult]

    private void setSharedPreferences(String id, String email, String name, String userPhoto, String bookmark){
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor autoLogin = auto.edit();

        autoLogin.putString("inputId", id);
        autoLogin.putString("inputEmail", email);
        autoLogin.putString("inputName", name);
        autoLogin.putString("inputPhoto", userPhoto);
        autoLogin.putString("bookmark", bookmark);

        autoLogin.commit();

    }

    // [START signIn]
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    private void googleSignOut(){
        // Logout in Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        ;
                    }
                });
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_google_btn:
                googleSignIn();
                break;
        }
    }

    @Override
    public void onDestroy(){
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    public void logIn(String url, final User user) {
        class GetData extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }
            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];

                try {
                    uri = uri + "getData?fn=userA";
                    uri = uri + "&id=" + user.getId();
                    uri = uri + "&email=" + user.getEmail();
                    uri = uri + "&name=" + URLEncoder.encode(user.getName(), "UTF-8");
                    uri = uri + "&photo=" + user.getPhoto();
                    uri = uri + "&channel=" + user.getChannel();
                    Log.d("URL", uri);

                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    con.setConnectTimeout(1000);

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();
                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) {
                if(result != null) {
                    try {
                        JSONObject resultObject = new JSONObject(result);

                        // 0: 다른채널 중복 Email, 1: Login, 2:SignIn
                        if(resultObject.getInt("result") == 0){
                            String message = "";
                            if(resultObject.getInt("value") == 1){
                                message = "이미 Google로 가입되어있습니다.";
                            } else if(resultObject.getInt("value") == 2){
                                message = "이미 Facebook으로 가입되어있습니다.";
                            }
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        } else {
                            setSharedPreferences(user.getId(), user.getEmail(), user.getName(), user.getPhoto(), resultObject.getString("bookmark"));
                            onStart();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        GetData g = new GetData();
        g.execute(url);

    }
}
