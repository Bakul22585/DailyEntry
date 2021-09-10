package com.example.dailyentry.ui.gallery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailyentry.R;
import com.example.dailyentry.SessionManagement;
//import com.example.dailyentry.ui.slideshow.TourDetailsActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    RVTourEntryAdapter adapter;
    ArrayList<Object> TourEntry = new ArrayList<>();
    LinearLayoutManager manager;
    EditText DateRang,TourName, TourDescription;
    String SelectedTour, SelectedFromDate, SelectedToDate = "";
    AutoCompleteTextView tour;
    String UserId;
    int CurrentItems, TotalItem, ScrollOutItem, PageIndex = 0, TourEntryLength = 0;
    public Boolean isScrolled = false, isFilter = false;
    SwipeRefreshLayout swipeRefreshLayout;
    public static final int ITEM_PER_AD = 4;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private Button SaveTour;
    private TextView TourNameError;
    FloatingActionButton fab;
    ArrayList<String> TourList=new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = root.findViewById(R.id.RV_TourEntryList);
        tour = root.findViewById(R.id.TourAutoCompleteTextView);
        DateRang = root.findViewById(R.id.TourDateRangEditText);
        swipeRefreshLayout = root.findViewById(R.id.tourRefreshFinancesList);
        progressBar = (ProgressBar) root.findViewById(R.id.tourEntryProgressBar);
        fab = root.findViewById(R.id.fabTourPopup);

        progressDialog = new ProgressDialog(getActivity(), R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        SessionManagement sessionManagement = new SessionManagement(getActivity());
        UserId = sessionManagement.getSession("id");

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTourPopup();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PageIndex = 0;
                isFilter = true;
                fetchData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("SELECT A DATE");
        final MaterialDatePicker materialDatePicker = builder.build();

        final Calendar calendar = Calendar.getInstance();
        DateRang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDatePicker.show(getActivity().getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long,Long> selection) {
                Long startDate = selection.first;
                Long endDate = selection.second;
                if(SelectedTour == null){
                    SelectedTour = "";
                }
                SelectedFromDate = String.valueOf(DateFormat.format("dd MMMM yyyy", startDate));
                SelectedToDate = String.valueOf(DateFormat.format("dd MMMM yyyy", endDate));
                PageIndex = 0;
                isFilter = true;
                DateRang.setText(materialDatePicker.getHeaderText());
                fetchData();
            }
        });

        tour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tour.showDropDown();
            }
        });

        tour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SelectedTour = String.valueOf(s);
                PageIndex = 0;
                isFilter = true;
                fetchData();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/getAllTour?user_id="+UserId+" ";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject responseData = new JSONObject(response);
                    JSONArray UserArray = responseData.getJSONArray("data");
                    for (int i = 0; i < UserArray.length(); i++) {
                        TourList.add(UserArray.get(i).toString());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, TourList);
                    tour.setAdapter(adapter);
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

        requestQueue = Volley.newRequestQueue(getActivity());
        URL = "http://restrictionsolution.com/ci/DailyEntry/tour/getTour";

        stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    JSONObject resData = new JSONObject(response);
                    JSONArray TourData = resData.getJSONArray("data");

                    for (int i = 0; i < TourData.length(); i++) {
                        JSONObject FinanceEntryObject = TourData.getJSONObject(i);
                        TourEntry tourEntry = new TourEntry();
                        tourEntry.setId(FinanceEntryObject.getString("id").toString());
                        tourEntry.setName(FinanceEntryObject.getString("name").toString());
                        tourEntry.setDescription(FinanceEntryObject.getString("description").toString());
                        tourEntry.setDate(FinanceEntryObject.getString("date").toString());
                        tourEntry.setStatus(FinanceEntryObject.getString("status").toString());
                        tourEntry.setBudget(FinanceEntryObject.getString("budget").toString());
                        TourEntry.add(tourEntry);
                    }
                    getBannerAds();
                    adapter = new RVTourEntryAdapter(TourEntry, getActivity());
                    manager = new LinearLayoutManager(getActivity());
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(manager);

                    adapter.setListener(new RVTourEntryAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            TourEntry tourEntry = (TourEntry) TourEntry.get(position);
                            Intent intent = new Intent(getActivity(), TourDetailActivity.class);
                            intent.putExtra("TourId",tourEntry.getId());
                            intent.putExtra("TourName",tourEntry.getName());
                            intent.putExtra("TourStatus",tourEntry.getStatus());
                            startActivity(intent);
                        }

                        @Override
                        public void onDeleteClick(int position) {

                        }
                    });

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolled = true;
                            }
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            CurrentItems = manager.getChildCount();
                            TotalItem = manager.getItemCount();
                            ScrollOutItem = manager.findFirstVisibleItemPosition();

                            if (dy > 0) {
                                CurrentItems = manager.getChildCount();
                                TotalItem = manager.getItemCount();
                                ScrollOutItem = manager.findFirstVisibleItemPosition();

                                int Total = CurrentItems + ScrollOutItem;
                                if (isScrolled && (Total == TotalItem)) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    PageIndex++;
                                    isFilter = false;
                                    isScrolled = false;
                                    fetchData();
                                }
                            }
                        }
                    });
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
                params.put("user_id", UserId);
                params.put("PageIndex", String.valueOf(PageIndex));
                return params;
            }
        };
        requestQueue.add(stringRequest);

        return root;
    }

    public Void fetchData() {
        if (isFilter.equals(true)) {
            progressDialog.show();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/getTour";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject resData = new JSONObject(response);
                    JSONArray TourData = resData.getJSONArray("data");
                    if (resData.getString("isFilter").equals("true")) {
                        TourEntry.clear();
                        TourEntryLength = 0;
                        isFilter = false;
                        progressDialog.dismiss();
                    }
                    for (int i = 0; i < TourData.length(); i++) {
                        JSONObject FinanceEntryObject = TourData.getJSONObject(i);
                        TourEntry tourEntry = new TourEntry();
                        tourEntry.setId(FinanceEntryObject.getString("id").toString());
                        tourEntry.setName(FinanceEntryObject.getString("name").toString());
                        tourEntry.setDescription(FinanceEntryObject.getString("description").toString());
                        tourEntry.setDate(FinanceEntryObject.getString("date").toString());
                        tourEntry.setStatus(FinanceEntryObject.getString("status").toString());
                        tourEntry.setBudget(FinanceEntryObject.getString("budget").toString());
                        TourEntry.add(tourEntry);
                    }
                    getBannerAds();
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
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
                params.put("user_id", UserId);
                params.put("PageIndex", String.valueOf(PageIndex));

                if (SelectedTour != null) {
                    params.put("tour", SelectedTour);
                }
                if (SelectedFromDate != null) {
                    params.put("formDate", SelectedFromDate);
                }
                if (SelectedToDate != null) {
                    params.put("toDate", SelectedToDate);
                }

                params.put("isFilter", String.valueOf(isFilter));
                return params;
            }
        };
        requestQueue.add(stringRequest);
        return null;
    }

    private void getBannerAds() {
        for (int i = TourEntryLength; i < TourEntry.size(); i++ ) {
            if (i% GalleryFragment.ITEM_PER_AD == 0 && i != 0) {
                final AdView adView = new AdView(getActivity());
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getResources().getString(R.string.banner_unit));
                TourEntry.add(i, adView);
            }
        }
        TourEntryLength = TourEntry.size();
        loadBannerAds();
    }

    private void loadBannerAds() {
        loadBannerAd(ITEM_PER_AD);
    }

    private void loadBannerAd(final int index)
    {
        if (index >= TourEntry.size())
        {
            return;
        }

        Object item = TourEntry.get(index);
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
        for (Object item : TourEntry)
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
        for (Object item : TourEntry)
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
        for (Object item : TourEntry)
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
        dialogBuilder = new AlertDialog.Builder(getActivity());
        final View tourPopupView = getLayoutInflater().inflate(R.layout.create_tour_popup, null);

        TourName = tourPopupView.findViewById(R.id.editTextTourName);
        TourNameError = tourPopupView.findViewById(R.id.textViewTourNameError);
        TourDescription = tourPopupView.findViewById(R.id.editTextTourDescription);
        SaveTour = tourPopupView.findViewById(R.id.btnCreateTour);

        dialogBuilder.setView(tourPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        SaveTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveTour.setEnabled(false);
                Boolean successBoolean = true;
                progressDialog.show();

                if (TourName.getText().toString().equals("")) {
                    successBoolean = false;
                    TourNameError.setVisibility(View.VISIBLE);
                }
                if (!TourName.getText().toString().equals("")) {
                    TourNameError.setVisibility(View.GONE);
                }

                if(successBoolean){
                    RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/tour/addTour";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);
                                if (responseData.getBoolean("success")) {
                                    isFilter = true;
                                    PageIndex = 0;
                                    dialog.hide();
                                    fetchData();
                                }else{
                                    Toast.makeText(getActivity(), responseData.getString("message"), Toast.LENGTH_SHORT).show();
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
                            params.put("user_id", UserId);
                            params.put("name", TourName.getText().toString());
                            params.put("description", TourDescription.getText().toString());
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                } else {
                    SaveTour.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }
}