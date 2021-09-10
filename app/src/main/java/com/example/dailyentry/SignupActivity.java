package com.example.dailyentry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private AdView mAdView;
    EditText FirstName, LastName, Mobile, Email, Password, ConfirmPassword, Activation;
    String ActivationCode, token;
    Boolean ActivationStatus = true;
    Button SignUp;
    TextView FirstNameError, LastNameError, MobileError, EmailError, PasswordError, ConfirmPasswordError, ActivationError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirstName = findViewById(R.id.editTextSignUpFirstName);
        FirstNameError = findViewById(R.id.txtSignUpFirstNameError);
        LastName = findViewById(R.id.editTextSignUpLastName);
        LastNameError = findViewById(R.id.txtSignUpLastNameError);
        Mobile = findViewById(R.id.editTextSignUpMobile);
        MobileError = findViewById(R.id.txtSignUpMobileError);
        Email = findViewById(R.id.editTextSignUpEmail);
        EmailError = findViewById(R.id.txtSignUpEmailError);
        Password = findViewById(R.id.editTextSignUpPassword);
        PasswordError = findViewById(R.id.txtSignUpPasswordError);
        ConfirmPassword = findViewById(R.id.editTextSignUpConfirmPassword);
        ConfirmPasswordError = findViewById(R.id.txtSignUpConfirmPasswordError);
        Activation = findViewById(R.id.editTextSignUpVerification);
        ActivationError = findViewById(R.id.txtSignUpVerificationError);
        SignUp = findViewById(R.id.btnSignUp);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.banner_unit));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.SignupadView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                }
            }
        });

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp.setEnabled(false);
                Boolean successBoolean = true;
                progressDialog.show();

                if (FirstName.getText().toString().equals("")) {
                    successBoolean = false;
                    FirstNameError.setVisibility(View.VISIBLE);
                }
                if (!FirstName.getText().toString().equals("")) {
                    FirstNameError.setVisibility(View.GONE);
                }

                if (LastName.getText().toString().equals("")) {
                    successBoolean = false;
                    LastNameError.setVisibility(View.VISIBLE);
                }
                if (!LastName.getText().toString().equals("")) {
                    LastNameError.setVisibility(View.GONE);
                }
//
//                if (Mobile.getText().toString().equals("")) {
//                    successBoolean = false;
//                    MobileError.setVisibility(View.VISIBLE);
//                }
//                if (!Mobile.getText().toString().equals("")) {
//                    MobileError.setVisibility(View.GONE);
//                }
//                if (Mobile.getText().toString().length() < 10) {
//                    successBoolean = false;
//                    MobileError.setText("Please enter valid phone number");
//                    MobileError.setVisibility(View.VISIBLE);
//                }

                if (Email.getText().toString().equals("")) {
                    successBoolean = false;
                    EmailError.setVisibility(View.VISIBLE);
                }
                if (!Email.getText().toString().equals("")) {
                    EmailError.setVisibility(View.GONE);
                }

                if (Password.getText().toString().equals("")) {
                    successBoolean = false;
                    PasswordError.setVisibility(View.VISIBLE);
                }
                if (!Password.getText().toString().equals("")) {
                    PasswordError.setVisibility(View.GONE);
                }

                if (ConfirmPassword.getText().toString().equals("")) {
                    successBoolean = false;
                    ConfirmPasswordError.setText("Enter Confirmation Password");
                    ConfirmPasswordError.setVisibility(View.VISIBLE);
                }
                if (!ConfirmPassword.getText().toString().equals("")) {
                    ConfirmPasswordError.setVisibility(View.GONE);
                }

                if (!ConfirmPassword.getText().toString().equals("") && !ConfirmPassword.getText().toString().equals(Password.getText().toString())) {
                    successBoolean = false;
                    ConfirmPasswordError.setText("Confirmation password must be the same as password");
                    ConfirmPasswordError.setVisibility(View.VISIBLE);
                }

                if (Activation.getText().toString().equals("") && ActivationStatus.equals(false)) {
                    successBoolean = false;
                    ActivationError.setText("Enter your email verification code");
                    ActivationError.setVisibility(View.VISIBLE);
                }

                if (!Activation.getText().toString().equals("") && ActivationStatus.equals(false)) {
                    ActivationError.setVisibility(View.GONE);
                }

                if (!Activation.getText().toString().equals(ActivationCode) && ActivationStatus.equals(false)) {
                    successBoolean = false;
                    ActivationError.setText("Your email code and verification code do not match, Please check and try again");
                    ActivationError.setVisibility(View.VISIBLE);
                }

                if(successBoolean){
                    RequestQueue requestQueue = Volley.newRequestQueue(SignupActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/user/add";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);
                                if (responseData.getBoolean("success")) {
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getApplicationContext(), responseData.getString("message"), Toast.LENGTH_LONG).show();
                                    SignUp.setEnabled(true);
                                    if (responseData.getString("field").equals("activation")) {
                                        if (ActivationStatus.equals(true)) {
                                            ActivationStatus = false;
                                            Activation.setVisibility(View.VISIBLE);
                                            ActivationCode = responseData.getString("activation");
                                            SignUp.setText("Complete");
                                        }
                                    }
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
                            params.put("firstname", FirstName.getText().toString());
                            params.put("lastname", LastName.getText().toString());
                            params.put("email", Email.getText().toString());
                            params.put("password", Password.getText().toString());
                            params.put("activation", String.valueOf(ActivationStatus));
                            params.put("device_token", token);
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    SignUp.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
        return;
    }
}