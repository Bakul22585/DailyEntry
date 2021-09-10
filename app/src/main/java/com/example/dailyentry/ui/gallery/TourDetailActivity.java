package com.example.dailyentry.ui.gallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailyentry.R;
import com.example.dailyentry.SessionManagement;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TourDetailActivity extends AppCompatActivity {

    String TourId, TourName, UserId, TourStatus, SenderId, ReceiverId;
    Button AddPerson, AddEntry, Finish, PopupSaveNewPerson, PayPerson, PopupPayPerson, PopupYesDeletePerson, PopupNoDeletePerson;
    EditText PopupAddNewPerson, PopupPayPersonAmount;
    TextView PopupAddNewPersonError;
    Spinner Sender, Receiver;
    RadioGroup PopupPaymentType;
    RadioButton PopupSelectedPaymentButton;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    RVPersonEntryAdapter adapter;
    LinearLayoutManager manager;
    ArrayList<Object> PersonEntry = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    public Boolean isScrolled = false, isFilter = false;
    int CurrentItems, TotalItem, ScrollOutItem, PageIndex = 0, PersonEntryLength = 0;
    float MinAmount, MaxAmount;
    public static final int ITEM_PER_AD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        Intent intent = getIntent();
        TourId = intent.getStringExtra("TourId");
        TourName = intent.getStringExtra("TourName");
        TourStatus = intent.getStringExtra("TourStatus");

        Toolbar toolbar = findViewById(R.id.tour_detail_toolbar);
        recyclerView = findViewById(R.id.RV_TourDetailPersonList);
        swipeRefreshLayout = findViewById(R.id.refreshTourDetailList);
        AddPerson = findViewById(R.id.btnAddPerson);
        AddEntry = findViewById(R.id.btnAddEntry);
        Finish = findViewById(R.id.btnTourFinish);
        PayPerson = findViewById(R.id.btnPayPerson);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(TourName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        progressDialog = new ProgressDialog(this, R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        SessionManagement sessionManagement = new SessionManagement(this);
        UserId = sessionManagement.getSession("id");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        if (!TourStatus.equals("Running")) {
            Finish.setEnabled(false);
            AddPerson.setEnabled(false);
            PayPerson.setVisibility(View.VISIBLE);
            AddEntry.setVisibility(View.GONE);
        }

        AddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTourPopup();
            }
        });

        AddEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TourDetailActivity.this, TourFinancesEntryActivity.class);
                intent.putExtra("TourId",TourId);
                startActivity(intent);
            }
        });

        PayPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayTourPersonPopup();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressDialog.show();
                PersonEntry.clear();
                PersonEntryLength = 0;
                getTourData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        Finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Finish.setEnabled(false);
                progressDialog.show();
                RequestQueue requestQueue = Volley.newRequestQueue(TourDetailActivity.this);
                String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/finishTour";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject responseData = new JSONObject(response);
                            if (responseData.getBoolean("success")) {
                                Finish.setEnabled(false);
                                AddPerson.setEnabled(false);
                                PayPerson.setVisibility(View.VISIBLE);
                                AddEntry.setVisibility(View.GONE);
                            }else{
                                Finish.setEnabled(true);
                                Toast.makeText(TourDetailActivity.this, responseData.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Finish.setEnabled(true);
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
                        params.put("tour_id", TourId);
                        return params;
                    }
                };
                requestQueue.add(stringRequest);
            }
        });

        getTourData();
    }

    public void getTourData() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/getTourPerson?tour_id="+ TourId +" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    JSONObject resData = new JSONObject(response);
                    JSONArray PersonData = resData.getJSONArray("data");

                    for (int i = 0; i < PersonData.length(); i++) {
                        JSONObject PersonEntryObject = PersonData.getJSONObject(i);
                        PersonEntry personEntry = new PersonEntry();
                        personEntry.setId(PersonEntryObject.getString("id").toString());
                        personEntry.setName(PersonEntryObject.getString("person_name").toString());
                        personEntry.setPersonBudget(PersonEntryObject.getString("personBudget").toString());
                        personEntry.setPerPersonBudget(PersonEntryObject.getString("perPersonBudget"));
                        personEntry.setPl(PersonEntryObject.getString("pl"));
                        personEntry.setSr(PersonEntryObject.getString("sr"));
                        personEntry.setStatus(PersonEntryObject.getString("status"));
                        personEntry.setExpandable(false);
                        personEntry.setData(PersonEntryObject.getJSONArray("entry"));
                        PersonEntry.add(personEntry);
                    }
                    getBannerAds();
                    adapter = new RVPersonEntryAdapter(PersonEntry, TourDetailActivity.this);
                    manager = new LinearLayoutManager(TourDetailActivity.this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(manager);
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
    }

    private void getBannerAds() {
        for (int i = PersonEntryLength; i < PersonEntry.size(); i++ ) {
            if (i% ITEM_PER_AD == 0 && i != 0) {
                final AdView adView = new AdView(this);
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getResources().getString(R.string.banner_unit));
                PersonEntry.add(i, adView);
            }
        }
        PersonEntryLength = PersonEntry.size();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        loadBannerAds();
    }

    private void loadBannerAds() {
        loadBannerAd(ITEM_PER_AD);
    }

    private void loadBannerAd(final int index)
    {
        if (index >= PersonEntry.size())
        {
            return;
        }

        Object item = PersonEntry.get(index);
        if (!(item instanceof AdView))
        {
            throw new ClassCastException("Expected item at index " + index + " to be a banner ad" + " ad.");
        }

        final AdView adView = (AdView) item;

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener()
        {
            @Override
            public void onAdLoaded()
            {
                super.onAdLoaded();
                // The previous banner ad loaded successfully, call this method again to
                // load the next ad in the items list.
                loadBannerAd(index + ITEM_PER_AD);
            }

            @Override
            public void onAdFailedToLoad(int errorCode)
            {
                // The previous banner ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("MainActivity", "The previous banner ad failed to load. Attempting to"
                        + " load the next banner ad in the items list.");
                loadBannerAd(index + ITEM_PER_AD);
            }
        });

        // Load the banner ad.
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onResume()
    {
        for (Object item : PersonEntry)
        {
            if (item instanceof AdView)
            {
                AdView adView = (AdView) item;
                adView.resume();
            }
        }
        super.onResume();
    }

    @Override
    public void onPause()
    {
        for (Object item : PersonEntry)
        {
            if (item instanceof AdView)
            {
                AdView adView = (AdView) item;
                adView.pause();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        for (Object item : PersonEntry)
        {
            if (item instanceof AdView)
            {
                AdView adView = (AdView) item;
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    public void createNewTourPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View AddPersonPopupView = getLayoutInflater().inflate(R.layout.create_add_tour_person_popup, null);

        PopupAddNewPerson = AddPersonPopupView.findViewById(R.id.editTextTourPersonName);
        PopupAddNewPersonError = AddPersonPopupView.findViewById(R.id.textViewTourPersonNameError);
        PopupSaveNewPerson = AddPersonPopupView.findViewById(R.id.btnAddTourPerson);

        dialogBuilder.setView(AddPersonPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        PopupSaveNewPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupSaveNewPerson.setEnabled(false);
                Boolean successBoolean = true;
                progressDialog.show();

                if (PopupAddNewPerson.getText().toString().equals("")) {
                    successBoolean = false;
                    PopupAddNewPersonError.setVisibility(View.VISIBLE);
                }
                if (!PopupAddNewPerson.getText().toString().equals("")) {
                    PopupAddNewPersonError.setVisibility(View.GONE);
                }

                if(successBoolean){
                    RequestQueue requestQueue = Volley.newRequestQueue(TourDetailActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/addTourPerson";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);
                                if (responseData.getBoolean("success")) {
                                    dialog.hide();
                                    PersonEntryLength = 0;
                                    PersonEntry.clear();
                                    getTourData();
                                }else{
                                    Toast.makeText(TourDetailActivity.this, responseData.getString("message"), Toast.LENGTH_SHORT).show();
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
                            params.put("tour_id", TourId);
                            params.put("person_name", PopupAddNewPerson.getText().toString());
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    PopupSaveNewPerson.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void PayTourPersonPopup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View PayPersonPopupView = getLayoutInflater().inflate(R.layout.create_pay_person_tour_popup, null);

        Sender = PayPersonPopupView.findViewById(R.id.spinnerSender);
        Receiver = PayPersonPopupView.findViewById(R.id.spinnerReceiver);
        PopupPayPersonAmount = PayPersonPopupView.findViewById(R.id.textPayAmount);
        PopupPaymentType = PayPersonPopupView.findViewById(R.id.radioGroupPaymentType);
        PopupPayPerson = PayPersonPopupView.findViewById(R.id.btnPayTourPerson);

        List<String> SenderOption = new ArrayList<>();
        List<String> ReceiverOption = new ArrayList<>();

        ReceiverOption.add("Select Receiver Person");
        SenderOption.add("Select Sender Person");

        for (int i = 0; i < PersonEntry.size(); i++ ) {
            Object item = PersonEntry.get(i);
            if (!(item instanceof AdView))
            {
                Log.e("No AdView", i + "");
                PersonEntry personEntry = (PersonEntry) PersonEntry.get(i);

                float PerPersonBudget = Float.parseFloat(personEntry.getPerPersonBudget());
                float sr = Float.parseFloat(personEntry.getSr());

                if (PerPersonBudget < sr) {
                    ReceiverOption.add(personEntry.getName());
                } else if (PerPersonBudget > sr) {
                    SenderOption.add(personEntry.getName());
                }
            }
        }

        ArrayAdapter<String> SenderDataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, SenderOption);
        SenderDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Sender.setAdapter(SenderDataAdapter);

        ArrayAdapter<String> ReceiverDataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ReceiverOption);
        ReceiverDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Receiver.setAdapter(ReceiverDataAdapter);

        dialogBuilder.setView(PayPersonPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        PopupPayPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupPayPerson.setEnabled(false);
                Boolean successBoolean = true;
                progressDialog.show();

                int PaymentTypeId = PopupPaymentType.getCheckedRadioButtonId();
                PopupSelectedPaymentButton = PayPersonPopupView.findViewById(PaymentTypeId);

                if (Sender.getSelectedItem().toString().equals("Select Sender Person")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please select sender person", Toast.LENGTH_LONG).show();
                } else if (Receiver.getSelectedItem().toString().equals("Select Receiver Person")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please select receiver person", Toast.LENGTH_LONG).show();
                } else if (PopupPayPersonAmount.getText().toString().equals("")) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please enter pay amount", Toast.LENGTH_LONG).show();
                } else if (PopupSelectedPaymentButton == null) {
                    successBoolean = false;
                    Toast.makeText(getApplicationContext(), "Please select payment type", Toast.LENGTH_LONG).show();
                } else {
                    for (int i = 0; i < PersonEntry.size(); i++ ) {
                        Object item = PersonEntry.get(i);
                        if (!(item instanceof AdView)) {
                            PersonEntry personEntry = (PersonEntry) PersonEntry.get(i);
                            String Name = personEntry.getName();
                            if (Name.equals(Receiver.getSelectedItem().toString())) {
                                ReceiverId = personEntry.getId();
                                MinAmount = Float.parseFloat(personEntry.getPl());
                            }

                            if (Name.equals(Sender.getSelectedItem().toString())) {
                                SenderId = personEntry.getId();
                                float pl = Float.parseFloat(personEntry.getPl());
                                float sr = Float.parseFloat(personEntry.getSr());
//                                MaxAmount = pl - sr;
                                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                                MaxAmount = Float.valueOf(decimalFormat.format(pl));
                            }
                        }
                    }
                    /*if (Float.parseFloat(PopupPayPersonAmount.getText().toString()) > MinAmount) {
                        successBoolean = false;
                        Toast.makeText(getApplicationContext(), "Please enter valid pay amount", Toast.LENGTH_LONG).show();
                    }*/

                    if (Float.parseFloat(PopupPayPersonAmount.getText().toString()) > MaxAmount) {
                        successBoolean = false;
                        Toast.makeText(getApplicationContext(), "Insufficient funds, Please enter valid pay amount", Toast.LENGTH_LONG).show();
                    }
                }

                if (successBoolean) {
                    RequestQueue requestQueue = Volley.newRequestQueue(TourDetailActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/PayPersonAmount";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);
                                if (responseData.getBoolean("success")) {
                                    dialog.hide();
                                    PersonEntryLength = 0;
                                    PersonEntry.clear();
                                    getTourData();
                                }else{
                                    Toast.makeText(TourDetailActivity.this, responseData.getString("message"), Toast.LENGTH_SHORT).show();
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
                            params.put("tour_id", TourId);
                            params.put("sender", SenderId);
                            params.put("receiver", ReceiverId);
                            params.put("amount", PopupPayPersonAmount.getText().toString());
                            params.put("type", (PopupSelectedPaymentButton.getText().toString().equals("Cash")? "1": "2" ));
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    PopupPayPerson.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void DeleteUserPopup(int position) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View DeleteConfirmationPopupView = getLayoutInflater().inflate(R.layout.delete_confirmation_popup, null);

        PopupYesDeletePerson = DeleteConfirmationPopupView.findViewById(R.id.btnYesDelete);
        PopupNoDeletePerson = DeleteConfirmationPopupView.findViewById(R.id.btnNoDelete);

        dialogBuilder.setView(DeleteConfirmationPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        PopupYesDeletePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean successBoolean = true;
                progressDialog.show();

                if(successBoolean){
                    RequestQueue requestQueue = Volley.newRequestQueue(TourDetailActivity.this);
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/deleteTourPerson";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);
                                if (responseData.getBoolean("success")) {
                                    PersonEntry.clear();
                                    PersonEntryLength = 0;
                                    getTourData();
                                    dialog.hide();
                                }else{
                                    Toast.makeText(TourDetailActivity.this, responseData.getString("message"), Toast.LENGTH_SHORT).show();
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
                            PersonEntry personEntry = (PersonEntry) PersonEntry.get(position);
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("tour_person_id", personEntry.getId().toString());
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    progressDialog.dismiss();
                }
            }
        });

        PopupNoDeletePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Object item = PersonEntry.get(position);
            if (!(item instanceof AdView))
            {
                DeleteUserPopup(position);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(TourDetailActivity.this, R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24_white)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(TourDetailActivity.this, R.color.red))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24_white)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}