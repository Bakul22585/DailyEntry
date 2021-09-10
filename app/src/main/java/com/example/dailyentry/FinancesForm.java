package com.example.dailyentry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FinancesForm extends AppCompatActivity {

    EditText purpose, amount, transcationDate, chequeDate, chequeNumber;
    AutoCompleteTextView user;
    ArrayList<String> UserList=new ArrayList<String>();
    RadioGroup financeType, paymentType;
    private RadioButton FinanceRadioButton, PaymentRadioButton;
    String FinanceType, PaymentType, user_id;
    CheckBox TransactionComplete;
    Button Submit, Add_image;
    ImageView TransactionImage;
    int year, month, day;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    private AdView adView;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finances_form);

        Toolbar toolbar = findViewById(R.id.toolbar_finance_form);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Finances Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        user = findViewById(R.id.text_username);
        purpose = findViewById(R.id.text_purpose);
        amount = findViewById(R.id.text_description);
        transcationDate = findViewById(R.id.text_date);
        financeType = findViewById(R.id.radiogroup_type);
        paymentType = findViewById(R.id.radiogroup_payment_type);
        chequeDate = findViewById(R.id.text_cheque_date);
        chequeNumber = findViewById(R.id.text_cheque_number);
        TransactionComplete = findViewById(R.id.checkbox_complate);
        TransactionImage = findViewById(R.id.img_transaction_img);
        Add_image = findViewById(R.id.btn_add_img);
        Submit = findViewById(R.id.btn_submit);

        SessionManagement sessionManagement = new SessionManagement(FinancesForm.this);
        user_id = sessionManagement.getSession("id");

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.banner_unit));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.financesFormAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select DATE");
        final MaterialDatePicker TransactionMaterialDatePicker = builder.build();
        final MaterialDatePicker ChequeDateMaterialDatePicker = builder.build();

        final Calendar calendar = Calendar.getInstance();
        transcationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransactionMaterialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        TransactionMaterialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                Long SelectDate = (Long) selection;
                CharSequence DateCharSequence = DateFormat.format("dd MMMM yyyy", SelectDate);
                transcationDate.setText(DateCharSequence);
            }
        });

        chequeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChequeDateMaterialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        ChequeDateMaterialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                Long SelectDate = (Long) selection;
                CharSequence DateCharSequence = DateFormat.format("dd MMMM yyyy", SelectDate);
                chequeDate.setText(DateCharSequence);
            }
        });

        paymentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radio_cheque) {
                    chequeDate.setVisibility(View.VISIBLE);
                    chequeNumber.setVisibility(View.VISIBLE);
                    TransactionComplete.setChecked(false);
                } else {
                    chequeDate.setVisibility(View.GONE);
                    chequeNumber.setVisibility(View.GONE);
                    TransactionComplete.setChecked(true);
                }
            }
        });

        chequeDate.setVisibility(View.GONE);
        chequeNumber.setVisibility(View.GONE);

        Add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMultiTouchEnabled(true)
                        .start(FinancesForm.this);
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://restrictionsolution.com/ci/DailyEntry/user/toUser?user_id="+user_id+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject responseData = new JSONObject(response);
                    JSONArray UserArray = responseData.getJSONArray("data");
                    for (int i = 0; i < UserArray.length(); i++) {
                        UserList.add(UserArray.get(i).toString());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(FinancesForm.this, android.R.layout.simple_expandable_list_item_1, UserList);
                    user.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        });
        requestQueue.add(stringRequest);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user = findViewById(R.id.text_username);
                purpose = findViewById(R.id.text_purpose);
                amount = findViewById(R.id.text_description);
                transcationDate = findViewById(R.id.text_date);
                financeType = findViewById(R.id.radiogroup_type);
                int financesTypeId = financeType.getCheckedRadioButtonId();
                FinanceRadioButton = findViewById(financesTypeId);

                paymentType = findViewById(R.id.radiogroup_payment_type);
                int paymentTypeId = paymentType.getCheckedRadioButtonId();
                PaymentRadioButton = findViewById(paymentTypeId);

                chequeDate = findViewById(R.id.text_cheque_date);
                chequeNumber = findViewById(R.id.text_cheque_number);
                TransactionComplete = findViewById(R.id.checkbox_complate);

                if (user.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Or Select User Name", Toast.LENGTH_SHORT).show();
                } else if (purpose.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter purpose", Toast.LENGTH_SHORT).show();
                } else if (amount.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Amount", Toast.LENGTH_SHORT).show();
                } else if (transcationDate.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),"Please Select Transaction Date", Toast.LENGTH_SHORT).show();
                } else if (FinanceRadioButton == null) {
                    Toast.makeText(getApplicationContext(), "Please Select Credit Or Debit Option", Toast.LENGTH_SHORT).show();
                } else if (PaymentRadioButton == null) {
                    Toast.makeText(getApplicationContext(), "Please Select Payment Option Cash, Cheque And Bank", Toast.LENGTH_SHORT).show();
                } else {
                    FinanceType = FinanceRadioButton.getText().toString();
                    PaymentType = PaymentRadioButton.getText().toString();

                    if (PaymentType.equals("Cheque") && chequeNumber.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Please Enter Cheque Number", Toast.LENGTH_SHORT).show();
                    } else if (PaymentType.equals("Cheque") && chequeDate.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Please Select Cheque Date", Toast.LENGTH_SHORT).show();
                    } else {
                        final ProgressDialog progressDialog = new ProgressDialog(view.getContext(), R.style.DialogTheme);
                        progressDialog.setCancelable(false); // set cancelable to false
                        progressDialog.setMessage("Please Wait"); // set message
                        progressDialog.show(); // show progress dialog

                        RequestQueue requestQueue = Volley.newRequestQueue(view.getContext());
                        String URL = "http://restrictionsolution.com/ci/DailyEntry/finances/add";

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getBoolean("success") == true) {
                                        user.setText("");               purpose.setText("");
                                        amount.setText("");             transcationDate.setText("");
                                        financeType.clearCheck();       paymentType.clearCheck();
                                        chequeNumber.setText("");       chequeDate.setText("");
                                        bitmap = null;
                                        TransactionComplete.setChecked(false);
                                        TransactionImage.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

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
                                params.put("user_id", user_id.trim());
                                params.put("to_user", user.getText().toString().trim());
                                params.put("description", purpose.getText().toString().trim());
                                params.put("amount", amount.getText().toString().trim());
                                switch (FinanceType) {
                                    case "Credit":
                                        params.put("finance_type", "1");
                                        break;
                                    case "Debit":
                                        params.put("finance_type", "2");
                                        break;
                                }
                                switch (PaymentType) {
                                    case "Cash":
                                        params.put("type", "1");
                                        break;
                                    case "Cheque":
                                        params.put("type", "2");
                                        break;
                                    case "Bank":
                                        params.put("type", "3");
                                        break;
                                }
                                params.put("date", transcationDate.getText().toString().trim());
                                params.put("cheque_number", chequeNumber.getText().toString().trim());
                                params.put("cheque_date", chequeDate.getText().toString().trim());

                                if (TransactionComplete.isChecked()) {
                                    params.put("is_complate", "1");
                                } else {
                                    params.put("is_complate", "0");
                                }

                                if (bitmap != null) {
                                    params.put("image", imageToString(bitmap));
                                }
                                return params;
                            }
                        };
                        requestQueue.add(stringRequest);
                    }
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
                    TransactionImage.setImageBitmap(bitmap);
                    TransactionImage.setVisibility(View.VISIBLE);
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