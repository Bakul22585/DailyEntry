package com.example.dailyentry.ui.gallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailyentry.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourFinancesEntryActivity extends AppCompatActivity {

    String TourId, PersonId;
    Spinner PersonName;
    EditText Title, Description, Amount, Date;
    RadioGroup FinancesType, PaymentType;
    RadioButton FinancesTypeRadioButton, PaymentTypeRadioButton;
    Button Submit;
    ProgressDialog progressDialog;
    private AdView mAdView;
    ArrayList<Object> PersonEntry = new ArrayList<>();
    List<String> PersonNameOption = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_finances_entry);

        Toolbar toolbar = findViewById(R.id.tourToolbarFinanceForm);
        PersonName = findViewById(R.id.textTourPersonName);
        Title = findViewById(R.id.textTourPurpose);
        Description = findViewById(R.id.textTourDescription);
        Amount = findViewById(R.id.textTourAmount);
        Date = findViewById(R.id.textTourDate);
        PaymentType = findViewById(R.id.tourRadioGroupPaymentType);
        Submit = findViewById(R.id.btnSaveTourExpenseEntry);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tour Entry Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        Intent intent = getIntent();
        TourId = intent.getStringExtra("TourId");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.banner_unit));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.tourFinancesFormAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select DATE");
        final MaterialDatePicker TransactionMaterialDatePicker = builder.build();
        final MaterialDatePicker ChequeDateMaterialDatePicker = builder.build();

        final Calendar calendar = Calendar.getInstance();
        Date.setOnClickListener(new View.OnClickListener() {
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
                Date.setText(DateCharSequence);
            }
        });

        progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        PersonNameOption.add("Select Person Name");

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/getTourPersonName?tour_id="+TourId+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    JSONObject jsonRes = new JSONObject(response);
                    JSONArray PersonData = jsonRes.getJSONArray("data");

                    for (int i = 0; i < PersonData.length(); i++) {
                        JSONObject PersonEntryObject = PersonData.getJSONObject(i);
                        PersonEntry personEntry = new PersonEntry();
                        personEntry.setId(PersonEntryObject.getString("id").toString());
                        personEntry.setName(PersonEntryObject.getString("person_name").toString());
                        PersonEntry.add(personEntry);
                        PersonNameOption.add(PersonEntryObject.getString("person_name"));

                        ArrayAdapter<String> SenderDataAdapter = new ArrayAdapter(TourFinancesEntryActivity.this, android.R.layout.simple_spinner_item, PersonNameOption);
                        SenderDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        PersonName.setAdapter(SenderDataAdapter);
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
        });
        requestQueue.add(stringRequest);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Submit.setEnabled(false);
                Boolean successBoolean = true;
                progressDialog.show();

                int PaymentTypeId = PaymentType.getCheckedRadioButtonId();
                PaymentTypeRadioButton = findViewById(PaymentTypeId);

                if (PersonName.getSelectedItem().toString().equals("Select Person Name")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please select person name", Toast.LENGTH_LONG).show();
                } else if (Title.getText().toString().equals("")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please enter title", Toast.LENGTH_LONG).show();
                } else if (Amount.getText().toString().equals("")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please enter amount", Toast.LENGTH_LONG).show();
                } else if (Date.getText().toString().equals("")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please select date", Toast.LENGTH_LONG).show();
                } else if (PaymentTypeRadioButton == null) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please select payment type", Toast.LENGTH_LONG).show();
                }

                if (successBoolean) {
                    RequestQueue requestQueue1 = Volley.newRequestQueue(TourFinancesEntryActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/addTourEntry";
                    StringRequest stringRequest1 = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Submit.setEnabled(true);
                            try {
                                JSONObject res = new JSONObject(response);

                                if (res.getBoolean("success") == true) {
                                    Title.setText("");          Description.setText("");
                                    Amount.setText("");         Date.setText("");
                                    PaymentType.clearCheck();   PersonName.setSelection(0);
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
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("tour_id", TourId);

                            for (int i = 0; i < PersonEntry.size(); i++) {
                                PersonEntry personEntry = (PersonEntry) PersonEntry.get(i);
                                String Name = personEntry.getName();

                                if (Name.equals(PersonName.getSelectedItem().toString())) {
                                    params.put("tour_person_id", personEntry.getId());
                                }
                            }
                            params.put("title", Title.getText().toString());
                            params.put("description", Description.getText().toString());
                            params.put("amount", Amount.getText().toString());
                            params.put("type", (PaymentTypeRadioButton.getText().toString().equals("Cash") ? "1":"2"));
                            params.put("date", Date.getText().toString());

                            return params;
                        }
                    };

                    requestQueue1.add(stringRequest1);
                } else {
                    Submit.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}