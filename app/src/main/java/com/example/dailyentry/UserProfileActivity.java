package com.example.dailyentry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private AdView adView;
    ImageView userProfilePic;
    EditText FirstName, LastName, Email, Password, ConfirmPassword;
    TextView FirstNameError, LastNameError, PasswordError, ConfirmPasswordError, LoginUserName, LoginUserFullName;
    CheckBox ShowPassword;
    Button Update;
    String UserId, UploadImage;
    Bitmap bitmap;
    ImageButton CameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolbarUserProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        userProfilePic = findViewById(R.id.userProfilePic);
        FirstName = findViewById(R.id.editTextUserUpdateFirstName);
        FirstNameError = findViewById(R.id.txtUserUpdateFirstNameError);
        LastName = findViewById(R.id.editTextUserUpdateLastName);
        LastNameError = findViewById(R.id.txtUserUpdateLastNameError);
        Email = findViewById(R.id.editTextUserUpdateEmail);
        ShowPassword = findViewById(R.id.displayPasswordSection);
        Password = findViewById(R.id.editTextUserUpdatePassword);
        PasswordError = findViewById(R.id.txtUserUpdatePasswordError);
        ConfirmPassword = findViewById(R.id.editTextUserUpdateConfirmPassword);
        ConfirmPasswordError = findViewById(R.id.txtUserUpdateConfirmPasswordError);
        Update = findViewById(R.id.btnUserProfileUpdate);
        CameraButton = findViewById(R.id.userProfileCamera);
        LoginUserFullName = findViewById(R.id.UserFullName);
        LoginUserName = findViewById(R.id.UserUserId);

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.banner_unit));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.UserUpdateAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message

        SessionManagement sessionManagement = new SessionManagement(this);
        UserId = sessionManagement.getSession("id");
        String LoginUserImg = sessionManagement.getSession("Img");
        String LoginFullName = sessionManagement.getSession("FirstName") +" "+ sessionManagement.getSession("LastName");

        LoginUserFullName.setText(LoginFullName);
        LoginUserName.setText(sessionManagement.getSession("username"));

        if (!LoginUserImg.equals("")) {
            byte[] decodedString = Base64.decode(LoginUserImg, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            userProfilePic.setImageBitmap(decodedByte);
        }

        FirstName.setText(sessionManagement.getSession("FirstName"));
        LastName.setText(sessionManagement.getSession("LastName"));
        Email.setText(sessionManagement.getSession("email"));

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMultiTouchEnabled(true)
                        .start(UserProfileActivity.this);
            }
        });

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMultiTouchEnabled(true)
                        .start(UserProfileActivity.this);
            }
        });

        ShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Password.setVisibility(View.VISIBLE);
                    ConfirmPassword.setVisibility(View.VISIBLE);
                } else {
                    Password.setVisibility(View.GONE);
                    PasswordError.setVisibility(View.GONE);
                    ConfirmPassword.setVisibility(View.GONE);
                    ConfirmPasswordError.setVisibility(View.GONE);
                }
            }
        });

        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Update.setEnabled(false);
                progressDialog.show();
                Boolean successBoolean = true;

                if (FirstName.getText().toString().equals("")) {
                    FirstNameError.setVisibility(View.VISIBLE);
                    successBoolean = false;
                }
                if (!FirstName.getText().toString().equals("")) {
                    FirstNameError.setVisibility(View.GONE);
                }

                if (LastName.getText().toString().equals("")) {
                    LastNameError.setVisibility(View.VISIBLE);
                    successBoolean = false;
                }
                if (!LastName.getText().toString().equals("")) {
                    LastNameError.setVisibility(View.GONE);
                }

                if (ShowPassword.isChecked()) {
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

                    if (!ConfirmPassword.getText().toString().equals(Password.getText().toString())) {
                        successBoolean = false;
                        ConfirmPasswordError.setText("Confirmation password must be the same as password");
                        ConfirmPasswordError.setVisibility(View.VISIBLE);
                    }
                }

                if (successBoolean) {
                    RequestQueue requestQueue = Volley.newRequestQueue(UserProfileActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/user/updateUser";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                Update.setEnabled(true);
                                try {
                                    JSONObject res = new JSONObject(response);
                                    Toast.makeText(UserProfileActivity.this, res.getString("message"), Toast.LENGTH_LONG).show();
                                    if (res.getBoolean("success")) {
                                        JSONArray UserDetails = res.getJSONArray("data");
                                        JSONObject UserData = (JSONObject) UserDetails.get(0);
                                        String id, FirstName, LastName, username, mobile, img, email;
                                        id = UserData.getString("id");
                                        FirstName = UserData.getString("firstname");
                                        LastName = UserData.getString("lastname");
                                        username = UserData.getString("username");
                                        img = UserData.getString("img");
                                        email = UserData.getString("email");

                                        UserObject user = new UserObject(id, FirstName, LastName, email ,username, img);
                                        SessionManagement sessionManagement = new SessionManagement(UserProfileActivity.this);
                                        sessionManagement.ClearSession();
                                        sessionManagement.saveSession(user);


                                        String LoginUserImg = sessionManagement.getSession("Img");
                                        String LoginFullName = sessionManagement.getSession("FirstName") +" "+ sessionManagement.getSession("LastName");

                                        LoginUserFullName.setText(LoginFullName);

                                        if (!LoginUserImg.equals("")) {
                                            byte[] decodedString = Base64.decode(LoginUserImg, Base64.DEFAULT);
                                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                            userProfilePic.setImageBitmap(decodedByte);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Update.setEnabled(true);
                            }
                        }
                    )
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            final Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/x-www-form-urlencoded");

                            return headers;
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("user_id", UserId);
                            params.put("first_name", FirstName.getText().toString().trim());
                            params.put("last_name", LastName.getText().toString().trim());
                            params.put("user_name", sessionManagement.getSession("username"));
                            if (ShowPassword.isChecked()) {
                                params.put("password", Password.getText().toString().trim());
                            }

                            if (bitmap != null) {
                                params.put("image", imageToString(bitmap));
                            }
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    progressDialog.dismiss();
                    Update.setEnabled(true);
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    userProfilePic.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.NO_WRAP);
    }
}