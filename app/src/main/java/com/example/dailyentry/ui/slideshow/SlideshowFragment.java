package com.example.dailyentry.ui.slideshow;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.example.dailyentry.FinancesEntry;
import com.example.dailyentry.R;
import com.example.dailyentry.RVFinancesEntryAdapter;
import com.example.dailyentry.SessionManagement;
import com.example.dailyentry.ui.gallery.PersonEntry;
import com.example.dailyentry.ui.gallery.TourDetailActivity;
import com.google.android.gms.ads.AdListener;
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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    ArrayList<Object> FinanceEntry = new ArrayList<>();
    RVFinancesEntryAdapter adapter;
    EditText DateRang;
    String SelectedUser, SelectedFromDate, SelectedToDate = "";
    AutoCompleteTextView user;
    ArrayList<String> UserList=new ArrayList<String>();
    String UserId;
    int CurrentItems, TotalItem, ScrollOutItem, PageIndex = 0, FinanceEntryLength = 0;
    Boolean isScrolled = false, isFilter = false;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    public static final int ITEM_PER_AD = 4;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    ProgressDialog progressDialog;
    Button PopupYesDeletePerson, PopupNoDeletePerson;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        recyclerView = root.findViewById(R.id.RV_FinanceEntryList);
        user = root.findViewById(R.id.UserAutoCompleteTextView);
        DateRang = root.findViewById(R.id.DateRangEditText);
        swipeRefreshLayout = root.findViewById(R.id.refreshFinancesList);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        SessionManagement sessionManagement = new SessionManagement(getActivity());
        UserId = sessionManagement.getSession("id");

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        progressDialog = new ProgressDialog(getActivity(), R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String URL = "http://restrictionsolution.com/ci/DailyEntry/user/toUser?user_id="+UserId+" ";

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
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, UserList);
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

        requestQueue = Volley.newRequestQueue(getActivity());
        URL = "http://restrictionsolution.com/ci/DailyEntry/finances/getFinanceList";

        stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    JSONObject resData = new JSONObject(response);
                    JSONArray FinancesData = resData.getJSONArray("data");
                    Log.e("Res", FinancesData + "");
                    for (int i = 0; i < FinancesData.length(); i++) {
                        JSONObject FinanceEntryObject = FinancesData.getJSONObject(i);
                        FinancesEntry financeEntry = new FinancesEntry();
                        financeEntry.setId(FinanceEntryObject.getString("id"));
                        financeEntry.setUser(FinanceEntryObject.getString("to_username").toString());
                        financeEntry.setDescription(FinanceEntryObject.getString("description").toString());
                        financeEntry.setAmount(FinanceEntryObject.getString("amount").toString());
                        financeEntry.setDate(FinanceEntryObject.getString("date").toString());
                        financeEntry.setFinanceType(FinanceEntryObject.getString("finance_type").toString());
                        financeEntry.setPaymentType(FinanceEntryObject.getString("type").toString());
                        FinanceEntry.add(financeEntry);
                    }
                    getBannerAds();
                    adapter = new RVFinancesEntryAdapter(FinanceEntry, getActivity());
                    manager = new LinearLayoutManager(getActivity());
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(manager);

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

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.showDropDown();
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
                if(SelectedUser == null){
                    SelectedUser = "";
                }
                SelectedFromDate = String.valueOf(DateFormat.format("dd MMMM yyyy", startDate));
                SelectedToDate = String.valueOf(DateFormat.format("dd MMMM yyyy", endDate));
                isFilter = true;
                DateRang.setText(materialDatePicker.getHeaderText());
                FinanceEntryLength = 0;
                PageIndex = 0;
                fetchData();
            }
        });

        user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SelectedUser = String.valueOf(editable);
                FinanceEntryLength = 0;
                PageIndex = 0;
                isFilter = true;
                fetchData();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PageIndex = 0;
                FinanceEntryLength = 0;
                isFilter = true;
                fetchData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return root;
    }

    private Void fetchData() {
        if (isFilter.equals(true)) {
            progressDialog.show();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String URL = "http://restrictionsolution.com/ci/DailyEntry/finances/getFinanceList";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject resData = new JSONObject(response);
                    JSONArray FinancesData = resData.getJSONArray("data");
                    if (resData.getString("isFilter").equals("true")) {
                        FinanceEntry.clear();
                        isFilter = false;
                        progressDialog.dismiss();
                    }
                    for (int i = 0; i < FinancesData.length(); i++) {
                        JSONObject FinanceEntryObject = FinancesData.getJSONObject(i);
                        FinancesEntry financeEntry = new FinancesEntry();
                        financeEntry.setId(FinanceEntryObject.getString("id"));
                        financeEntry.setUser(FinanceEntryObject.getString("to_username").toString());
                        financeEntry.setDescription(FinanceEntryObject.getString("description").toString());
                        financeEntry.setAmount(FinanceEntryObject.getString("amount").toString());
                        financeEntry.setDate(FinanceEntryObject.getString("date").toString());
                        financeEntry.setFinanceType(FinanceEntryObject.getString("finance_type").toString());
                        financeEntry.setPaymentType(FinanceEntryObject.getString("type").toString());
                        FinanceEntry.add(financeEntry);
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

                if (SelectedUser != null) {
                    params.put("user", SelectedUser);
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
        for (int i = FinanceEntryLength; i < FinanceEntry.size(); i++ ) {
            if (i%SlideshowFragment.ITEM_PER_AD == 0 && i != 0) {
                final AdView adView = new AdView(getActivity());
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getResources().getString(R.string.banner_unit));
                FinanceEntry.add(i, adView);
            }
        }
        FinanceEntryLength = FinanceEntry.size();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        loadBannerAds();
    }

    private void loadBannerAds() {
        loadBannerAd(ITEM_PER_AD);
    }

    private void loadBannerAd(final int index)
    {
        if (index >= FinanceEntry.size())
        {
            return;
        }

        Object item = FinanceEntry.get(index);
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
        for (Object item : FinanceEntry)
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
        for (Object item : FinanceEntry)
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
        for (Object item : FinanceEntry)
        {
            if (item instanceof AdView)
            {
                AdView adView = (AdView) item;
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Object item = FinanceEntry.get(position);
            if (!(item instanceof AdView))
            {
                DeleteUserPopup(position);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24_white)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24_white)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    public void DeleteUserPopup(int position) {
        dialogBuilder = new AlertDialog.Builder(getActivity());
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
                    RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                    String URL = "http://restrictionsolution.com/ci/DailyEntry/finances/deleteFinanceEntry";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject responseData = new JSONObject(response);
                                if (responseData.getBoolean("success")) {
                                    PageIndex = 0;
                                    FinanceEntryLength = 0;
                                    isFilter = true;
                                    fetchData();
                                    dialog.hide();

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
                            FinancesEntry financesEntry = (FinancesEntry) FinanceEntry.get(position);
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("finances_id", financesEntry.getId());
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
}