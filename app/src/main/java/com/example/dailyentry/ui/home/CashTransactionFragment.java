package com.example.dailyentry.ui.home;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CashTransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CashTransactionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    LinearLayoutManager manager;
    List<Object> FinanceEntry;
    RVFinancesEntryAdapter adapter;
    ArrayList<String> UserList=new ArrayList<String>();
    TextView totalBalance, totalIncome, totalExpenses;
    BarChart barChart;
    String UserId;
    public static final int ITEM_PER_AD = 4;

    public CashTransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CashTransactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CashTransactionFragment newInstance(String param1, String param2) {
        CashTransactionFragment fragment = new CashTransactionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_cash_transaction, container, false);
        recyclerView = root.findViewById(R.id.homeScreenCashTransaction);
        manager = new LinearLayoutManager(getActivity());
        FinanceEntry = new ArrayList<>();
        barChart = root.findViewById(R.id.HomeCashBarChart);
        totalBalance = root.findViewById(R.id.totalCashBalanceTextView);
        totalIncome = root.findViewById(R.id.totalCashIncome);
        totalExpenses = root.findViewById(R.id.totalCashExpenses);

        recyclerView.setNestedScrollingEnabled(false);
        SessionManagement sessionManagement = new SessionManagement(getActivity());
        UserId = sessionManagement.getSession("id");

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.DialogTheme);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String URL = "http://restrictionsolution.com/ci/DailyEntry/finances/getBarCharData?user_id="+UserId+"&status=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject responseData = new JSONObject(response);
                    JSONObject data = responseData.getJSONObject("data");
                    JSONArray credit = data.getJSONArray("credit");
                    JSONArray debit = data.getJSONArray("debit");
                    JSONArray FinancesData = data.getJSONArray("OriginalData");
                    JSONArray total = data.getJSONArray("total");

                    ArrayList<BarEntry> creditArray = new ArrayList<>();
                    ArrayList<BarEntry> debitArray = new ArrayList<>();

                    for (int i = 0; i < total.length(); i++) {
                        JSONObject totalObject = total.getJSONObject(i);

                        String total_income = totalObject.getString("total_income");
                        String total_expenses = totalObject.getString("total_expenses");
                        String total_balance = totalObject.getString("total_balance");
                        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

                        if (total_income.equals("")) {
                            total_income = "0";
                        }

                        if (total_expenses.equals("")) {
                            total_expenses = "0";
                        }

                        if (total_balance.equals("")) {
                            total_balance = "0";
                        }

                        totalBalance.setText(format.format(Double.parseDouble(total_balance)));
                        totalIncome.setText(format.format(Double.parseDouble(total_income)));
                        totalExpenses.setText(format.format(Double.parseDouble(total_expenses)));
                    }

                    for (int i = 0; i < credit.length(); i++) {
                        JSONObject CreditObject = credit.getJSONObject(i);
                        String id = CreditObject.getString("id");
                        String amount = CreditObject.getString("amount");
                        creditArray.add(new BarEntry(Integer.parseInt(id), Integer.parseInt(amount)));
                    }

                    for (int i = 0; i < debit.length(); i++) {
                        JSONObject DebitObject = debit.getJSONObject(i);
                        String id = DebitObject.getString("id");
                        String amount = DebitObject.getString("amount");
                        debitArray.add(new BarEntry(Integer.parseInt(id), Integer.parseInt(amount)));
                    }

                    BarDataSet barDataSet = new BarDataSet(creditArray, "Credit");
                    BarDataSet barDataSet2 = new BarDataSet(debitArray, "Debit");
                    barDataSet.setColor(Color.parseColor("#add827"));
                    barDataSet2.setColor(Color.RED);
                    barDataSet.setValueTextColor(Color.BLACK);
                    barDataSet.setValueTextSize(10f);
                    barDataSet2.setValueTextColor(Color.BLACK);
                    barDataSet2.setValueTextSize(10f);

                    BarData barData = new BarData();
                    barData.addDataSet(barDataSet);
                    barData.addDataSet(barDataSet2);

                    barChart.setFitBars(true);
                    barChart.setData(barData);
                    barChart.getDescription().setText("");
                    barChart.animateY(2000);
                    barChart.getAxisRight().setEnabled(false);

                    for (int i = 0; i < FinancesData.length(); i++) {
                        JSONObject FinanceEntryObject = FinancesData.getJSONObject(i);
                        FinancesEntry financeEntry = new FinancesEntry();
                        financeEntry.setUser(FinanceEntryObject.getString("to_username").toString());
                        financeEntry.setDescription(FinanceEntryObject.getString("description").toString());
                        financeEntry.setAmount(FinanceEntryObject.getString("amount").toString());
                        financeEntry.setDate(FinanceEntryObject.getString("date").toString());
                        financeEntry.setFinanceType(FinanceEntryObject.getString("finance_type").toString());
                        financeEntry.setPaymentType(FinanceEntryObject.getString("type").toString());
                        FinanceEntry.add(financeEntry);
                    }
                    adapter = new RVFinancesEntryAdapter(FinanceEntry, getActivity());
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(manager);
                    getBannerAds();
                    loadBannerAds();

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

        return root;
    }

    private void getBannerAds() {
        for (int i = ITEM_PER_AD; i < FinanceEntry.size(); i+=ITEM_PER_AD ) {
            final AdView adView = new AdView(getActivity());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getResources().getString(R.string.banner_unit));
            FinanceEntry.add(i, adView);
        }
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
}