package com.example.dailyentry.ui.gallery;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyentry.FinancesEntry;
import com.example.dailyentry.R;
import com.example.dailyentry.RVFinancesEntryAdapter;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RVPersonEntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> data;
    private Context context;

    private static final int ENTRY_ITEM = 0;
    private static final int ADS_ITEM = 1;

    public RVPersonEntryAdapter(List<Object> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ENTRY_ITEM:
                View EntryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_person_entry_list, parent, false);
                return new PersonEntryViewHolder(EntryView);
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
                if (data.get(position) instanceof PersonEntry) {
                    PersonEntryViewHolder personEntryViewHolder = (PersonEntryViewHolder) holder;
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

                    PersonEntry personEntry = (PersonEntry) data.get(position);
                    personEntryViewHolder.Name.setText(personEntry.getName());
                    personEntryViewHolder.PersonBudget.setText(format.format(Double.parseDouble(personEntry.getPersonBudget())));
                    personEntryViewHolder.PerPersonBudget.setText(format.format(Double.parseDouble(personEntry.getPerPersonBudget())));
                    personEntryViewHolder.PerPersonPL.setText(format.format(Double.parseDouble(personEntry.getPl())));

                    if (!personEntry.getSr().equals(personEntry.getPerPersonBudget())) {
                        if (personEntry.getStatus().equals("1")) {
                            personEntryViewHolder.PerPersonPL.setTextColor(Color.GREEN);
                        } else {
                            personEntryViewHolder.PerPersonPL.setTextColor(Color.RED);
                        }
                    } else {
                        ColorStateList defaultColor = personEntryViewHolder.PerPersonPL.getTextColors();
                        personEntryViewHolder.PerPersonPL.setTextColor(defaultColor);
                    }

                    Boolean isExpandable = ((PersonEntry) data.get(position)).getExpandable();
                    personEntryViewHolder.expandable_layout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

                    ArrayList<Object> PersonExpenses = new ArrayList<>();
                    for (int i = 0; i < personEntry.getData().length(); i++) {
                        try {
                            JSONObject personEntryObject = personEntry.getData().getJSONObject(i);

                            PersonExpenseEntry personExpenseEntry = new PersonExpenseEntry();
                            personExpenseEntry.setTitle(personEntryObject.getString("title"));
                            personExpenseEntry.setDescription(personEntryObject.getString("description"));
                            personExpenseEntry.setAmount(personEntryObject.getString("amount"));
                            personExpenseEntry.setDate(personEntryObject.getString("date"));
                            personExpenseEntry.setType(personEntryObject.getString("type"));
                            PersonExpenses.add(personExpenseEntry);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    RVPersonExpensesEntryAdapter adapter = new RVPersonExpensesEntryAdapter(PersonExpenses);
                    LinearLayoutManager manager = new LinearLayoutManager(context);
                    personEntryViewHolder.recyclerView.setAdapter(adapter);
                    personEntryViewHolder.recyclerView.setLayoutManager(manager);
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
        if (position == 0 || data.get(position) instanceof PersonEntry) {
            return ENTRY_ITEM;
        } else {
            if (position%TourDetailActivity.ITEM_PER_AD == 0) {
                return ADS_ITEM;
            } else {
                return ENTRY_ITEM;
            }
        }
    }

    public class BannerAdViewHolder extends RecyclerView.ViewHolder {

        public BannerAdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class PersonEntryViewHolder extends RecyclerView.ViewHolder {
        TextView Name, PersonBudget, PerPersonBudget, PerPersonPL;
        RelativeLayout collapse_layout, expandable_layout;
        RecyclerView recyclerView;
        public PersonEntryViewHolder(@NonNull View itemView) {
            super(itemView);

            Name = itemView.findViewById(R.id.txtPersonName);
            PersonBudget = itemView.findViewById(R.id.txtPersonBudget);
            PerPersonBudget = itemView.findViewById(R.id.txtPerPersonBudget);
            PerPersonPL = itemView.findViewById(R.id.txtPerPersonPL);
            collapse_layout = itemView.findViewById(R.id.cardViewHeaderLinearLayout);
            expandable_layout = itemView.findViewById(R.id.expandable_layout);
            recyclerView = itemView.findViewById(R.id.RV_TourDetailsPersonExpensesList);

            collapse_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PersonEntry personEntry = (PersonEntry) data.get(getAdapterPosition());
                    personEntry.setExpandable(!personEntry.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}
