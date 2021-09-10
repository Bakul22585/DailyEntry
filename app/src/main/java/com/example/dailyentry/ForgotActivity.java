package com.example.dailyentry;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotActivity extends AppCompatActivity {

    private AdView mAdView;
    EditText EmailAddress;
    TextView EmailAddressError, AlreadyCode;
    Button GetCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        EmailAddress = findViewById(R.id.editTextForgotEmail);
        EmailAddressError = findViewById(R.id.txtForgotEmailError);
        AlreadyCode = findViewById(R.id.txtAlreadyCode);
        GetCode = findViewById(R.id.btnForgot);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.banner_unit));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adViewForgot);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message

        AlreadyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotActivity.this, RestPasswordActivity.class);
                startActivity(intent);
            }
        });

        GetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetCode.setEnabled(false);
                Boolean successBoolean = true;
                progressDialog.show();

                if (EmailAddress.getText().toString().equals("")) {
                    successBoolean = false;
                    EmailAddressError.setVisibility(View.VISIBLE);
                }

                if (!EmailAddress.getText().toString().equals("")) {
                    EmailAddressError.setVisibility(View.GONE);
                }

                if (successBoolean) {
                    RequestQueue requestQueue = Volley.newRequestQueue(ForgotActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/user/forgotpassword";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                try {
                                    JSONObject responseData = new JSONObject(response);
                                    GetCode.setEnabled(true);
                                    Toast.makeText(getApplicationContext(), responseData.getString("message"), Toast.LENGTH_LONG).show();
                                    if (responseData.getBoolean("success")) {
                                        Intent intent = new Intent(ForgotActivity.this, RestPasswordActivity.class);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                GetCode.setEnabled(true);
                                progressDialog.dismiss();
                            }
                        }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            final Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/x-www-form-urlencoded");

                            return headers;
                        }

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("email", EmailAddress.getText().toString());
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    GetCode.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ForgotActivity.this, LoginActivity.class));
        finish();
        return;
    }
}