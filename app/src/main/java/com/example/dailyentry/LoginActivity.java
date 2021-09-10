package com.example.dailyentry;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private AdView mAdView;
    EditText Username, Password;
    Button Login;
    TextView ForgotPassword, NewRegister, UserError, PasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Username = findViewById(R.id.editTextLoginUsername);
        Password = findViewById(R.id.editTextLoginUserPassword);
        Login = findViewById(R.id.btnLogin);
        NewRegister = findViewById(R.id.txtAddNewUser);
        ForgotPassword = findViewById(R.id.txtForgotPassword);
        UserError = findViewById(R.id.txtLoginUserError);
        PasswordError = findViewById(R.id.txtLoginUserPasswordError);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.banner_unit));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message

        NewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotActivity.class);
                startActivity(intent);
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean successBoolean = true;
                if (Username.getText().toString().equals("")) {
                    successBoolean = false;
                    UserError.setVisibility(View.VISIBLE);
                }
                if (!Username.getText().toString().equals("")) {
                    UserError.setVisibility(View.GONE);
                }
                if (Password.getText().toString().equals("")) {
                    successBoolean = false;
                    PasswordError.setVisibility(View.VISIBLE);
                }
                if (!Password.getText().toString().equals("")) {
                    PasswordError.setVisibility(View.GONE);
                }
                if (!Username.getText().toString().equals("") && !Password.getText().toString().equals("")) {
                }
                if(successBoolean){
                    progressDialog.show();
                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/user/login";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);

                                if (responseData.getBoolean("success")) {
                                    JSONArray UserDetails = responseData.getJSONArray("data");
                                    JSONObject UserData = (JSONObject) UserDetails.get(0);

                                    String id, FirstName, LastName, username, mobile, img, email;
                                    id = UserData.getString("id");
                                    FirstName = UserData.getString("firstname");
                                    LastName = UserData.getString("lastname");
                                    username = UserData.getString("username");
                                    img = UserData.getString("img");
                                    email = UserData.getString("email");

                                    UserObject user = new UserObject(id, FirstName, LastName, email ,username, img);
                                    SessionManagement sessionManagement = new SessionManagement(LoginActivity.this);
                                    sessionManagement.saveSession(user);

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getApplicationContext(), responseData.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            final Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/x-www-form-urlencoded");

                            return headers;
                        }

                        @Override
                        protected Map<String,String> getParams(){
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("username", Username.getText().toString());
                            params.put("password", Password.getText().toString());
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        SessionManagement sessionManagement = new SessionManagement(LoginActivity.this);
        String Id = sessionManagement.getSession("id");

        if (Id.length() != 0) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        return;
    }
}