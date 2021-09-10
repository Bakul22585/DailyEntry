package com.example.dailyentry.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyentry.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RVPersonExpensesEntryAdapter extends RecyclerView.Adapter {

    private List<Object> data;
    private Context context;

    public RVPersonExpensesEntryAdapter(List<Object> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View EntryView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_tour_person_entry_list, parent, false);
        return new PersonExpensesEntryViewHolder(EntryView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PersonExpensesEntryViewHolder personExpensesEntryViewHolder = (PersonExpensesEntryViewHolder) holder;
        PersonExpenseEntry personExpenseEntry = (PersonExpenseEntry) data.get(position);
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

        personExpensesEntryViewHolder.title.setText(personExpenseEntry.getTitle());
        personExpensesEntryViewHolder.description.setText(personExpenseEntry.getDescription());
        personExpensesEntryViewHolder.type.setText(personExpenseEntry.getType());
        personExpensesEntryViewHolder.date.setText(personExpenseEntry.getDate());
        personExpensesEntryViewHolder.amount.setText(format.format(Double.parseDouble(personExpenseEntry.getAmount())));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PersonExpensesEntryViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, amount, date, finance_type, type;
        public PersonExpensesEntryViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.txt_tour_person_expenses_title);
            description = itemView.findViewById(R.id.txt_tour_person_expenses_description);
            amount = itemView.findViewById(R.id.txt_tour_person_expenses_amount);
            date = itemView.findViewById(R.id.txt_tour_person_expenses_date);
            type = itemView.findViewById(R.id.txt_tour_person_expenses_type);
        }
    }
}
