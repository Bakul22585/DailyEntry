package com.example.dailyentry;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyentry.ui.home.AllTarnsactionFragment;
import com.google.android.gms.ads.AdView;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RVFinancesEntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> data;
    private Context context;

    private static final int ENTRY_ITEM = 0;
    private static final int ADS_ITEM = 1;

    public RVFinancesEntryAdapter(List<Object> data, Context context){
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ENTRY_ITEM:
                View EntryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_finance_entry_list_layout, parent, false);
                return new FinancesEntryViewHolder(EntryView);
            case ADS_ITEM:
            default:
                View BannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_ad, parent, false);
                return new BannerAdViewHolder(BannerView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ENTRY_ITEM:
                if (data.get(position) instanceof FinancesEntry) {
                    FinancesEntryViewHolder financesEntryViewHolder = (FinancesEntryViewHolder) holder;
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

                    FinancesEntry financesEntry = (FinancesEntry) data.get(position);
                    financesEntryViewHolder.Description.setText(financesEntry.getDescription());
                    financesEntryViewHolder.User.setText(financesEntry.getUser());
                    financesEntryViewHolder.Amount.setText(format.format(Double.parseDouble(financesEntry.getAmount())));
                    financesEntryViewHolder.Date.setText(financesEntry.getDate());

                    if (financesEntry.getFinanceType().equals("1")) {
                        financesEntryViewHolder.Amount.setTextColor(Color.GREEN);
                    } else {
                        financesEntryViewHolder.Amount.setTextColor(Color.RED);
                    }

                    SpannableString Amount = new SpannableString(format.format(Double.parseDouble(financesEntry.getAmount())));
                    ForegroundColorSpan fcBalck = new ForegroundColorSpan(Color.parseColor("#8a000000"));
                    Amount.setSpan(fcBalck,0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    financesEntryViewHolder.Amount.setText(Amount);

                    if (financesEntry.getPaymentType().equals("1")) {
                        financesEntryViewHolder.PaymentType.setText("Cash");
                    } else if(financesEntry.getPaymentType().equals("2")) {
                        financesEntryViewHolder.PaymentType.setText("Cheque");
                    } else if (financesEntry.getPaymentType().equals("3")) {
                        financesEntryViewHolder.PaymentType.setText("Bank");
                    }
                }
                break;
            case  ADS_ITEM:
            default:
                if (data.get(position) instanceof AdView) {
                    BannerAdViewHolder bannerAdViewHolder = (BannerAdViewHolder) holder;
                    AdView adView = (AdView) data.get(position);
                    ViewGroup adCardView = (ViewGroup) bannerAdViewHolder.itemView;

                    if (adCardView.getChildCount() > 0) {
                        adCardView.removeAllViews();
                    }

                    if (adCardView.getParent() !=null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }

                    adCardView.addView(adView);
                }
                break;
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || data.get(position) instanceof FinancesEntry) {
            return ENTRY_ITEM;
        } else {
            if (position%AllTarnsactionFragment.ITEM_PER_AD == 0) {
                return ADS_ITEM;
            } else {
                return ENTRY_ITEM;
            }
        }
    }

    public class FinancesEntryViewHolder extends RecyclerView.ViewHolder {
        TextView Description, User, Date, Amount, PaymentType;
        public FinancesEntryViewHolder(@NonNull View itemView) {
            super(itemView);

            Description = itemView.findViewById(R.id.tv_description);
            User = itemView.findViewById(R.id.tv_username);
            Date = itemView.findViewById(R.id.tv_date);
            Amount = itemView.findViewById(R.id.tv_amount);
            PaymentType = itemView.findViewById(R.id.tv_type);
        }
    }

    public class BannerAdViewHolder extends RecyclerView.ViewHolder {

        public BannerAdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
