package com.example.dailyentry.ui.gallery;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyentry.FinancesEntry;
import com.example.dailyentry.R;
import com.example.dailyentry.RVFinancesEntryAdapter;
import com.google.android.gms.ads.AdView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RVTourEntryAdapter extends RecyclerView.Adapter {

    private List<Object> data;
    private Context context;
    private OnItemClickListener Listener;

    private static final int ENTRY_ITEM = 0;
    private static final int ADS_ITEM = 1;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setListener(OnItemClickListener listener) {
        Listener = listener;
    }

    public RVTourEntryAdapter(List<Object> data, Context context){
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ENTRY_ITEM:
                View EntryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_tour_entry_list, parent, false);
                return new RVTourEntryAdapter.TourEntryViewHolder(EntryView, Listener);
            case ADS_ITEM:
            default:
                View BannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_ad, parent, false);
                return new RVTourEntryAdapter.BannerAdViewHolder(BannerView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ENTRY_ITEM:
                if (data.get(position) instanceof TourEntry) {
                    RVTourEntryAdapter.TourEntryViewHolder tourEntryViewHolder = (TourEntryViewHolder) holder;
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

                    TourEntry tourEntry = (TourEntry) data.get(position);
                    tourEntryViewHolder.Description.setText(tourEntry.getDescription());
                    tourEntryViewHolder.Tour.setText(tourEntry.getName());
                    tourEntryViewHolder.Budget.setText(format.format(Double.parseDouble(tourEntry.getBudget())));
                    tourEntryViewHolder.Date.setText(tourEntry.getDate());
                    tourEntryViewHolder.FinishDate.setText(tourEntry.getStatus());

                    SpannableString Amount = new SpannableString(format.format(Double.parseDouble(tourEntry.getBudget())));
                    ForegroundColorSpan fcBalck = new ForegroundColorSpan(Color.parseColor("#8a000000"));
                    Amount.setSpan(fcBalck,0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tourEntryViewHolder.Budget.setText(Amount);
                }
                break;
            case  ADS_ITEM:
            default:
                if (data.get(position) instanceof AdView) {
                    RVFinancesEntryAdapter.BannerAdViewHolder bannerAdViewHolder = (RVFinancesEntryAdapter.BannerAdViewHolder) holder;
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
            if (position%GalleryFragment.ITEM_PER_AD == 0) {
                return ADS_ITEM;
            } else {
                return ENTRY_ITEM;
            }
        }
    }

    public class TourEntryViewHolder extends RecyclerView.ViewHolder {
        TextView Description, Tour, Date, Budget, FinishDate;
        public TourEntryViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            Description = itemView.findViewById(R.id.text_description);
            Tour = itemView.findViewById(R.id.text_TourName);
            Date = itemView.findViewById(R.id.text_TourDate);
            FinishDate = itemView.findViewById(R.id.text_TourFinishDate);
            Budget = itemView.findViewById(R.id.text_budget);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    listener.onItemClick(position);
                }
            });
        }
    }

    public class BannerAdViewHolder extends RecyclerView.ViewHolder {

        public BannerAdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
