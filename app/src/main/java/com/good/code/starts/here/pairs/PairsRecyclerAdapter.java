package com.good.code.starts.here.pairs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bitshares.bitshareswallet.wallet.graphene.chain.utils;

import org.evrazcoin.evrazwallet.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PairsRecyclerAdapter extends RecyclerView.Adapter<PairsRecyclerAdapter.ViewHolder> {

    private int lastSelected;
    private int selected;

    private Context context;
    private SharedPreferences preferences;
    private ArrayList<String> pairs = new ArrayList<>();

    private boolean onBind;

    public PairsRecyclerAdapter(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<String> pairsContainer = new ArrayList<>(preferences.getStringSet("pairs", new HashSet<>()));
        for (int i = 0; i < pairsContainer.size(); i++) {
            String[] pair = pairsContainer.get(i).split(":");
            String pairFirst = pair[0];
            String pairSecond = pair[1];


            if (pairFirst.contains("BTC") ||
                    pairFirst.contains("BTS") ||
                    pairFirst.contains("EVRAZ") ||
                    pairSecond.contains("BTC") ||
                    pairSecond.contains("BTS") ||
                    pairSecond.contains("EVRAZ")) {
                pairs.add(pairsContainer.get(i));
            }
        }
        selected = pairs.indexOf(preferences.getString("quotation_currency_pair", "BTS:USD"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_pair, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        String[] pair = pairs.get(i).split(":");
        if (pair.length == 2) {
            String first = utils.getAssetSymbolDisply(pair[0]);
            String second = utils.getAssetSymbolDisply(pair[1]);
            viewHolder.first.setText(first);
            viewHolder.second.setText(second);
        }

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
            if (isChecked && !onBind) {
                lastSelected = selected;
                selected = viewHolder.getAdapterPosition();
                notifyItemChanged(lastSelected);
                preferences.edit().putString("quotation_currency_pair", pairs.get(selected)).apply();
            }
        };

        viewHolder.radioButton.setOnCheckedChangeListener(onCheckedChangeListener);

        viewHolder.itemView.setOnClickListener(v -> {
            if (!viewHolder.radioButton.isChecked() && !onBind) {
                lastSelected = selected;
                selected = viewHolder.getAdapterPosition();
                viewHolder.radioButton.setOnCheckedChangeListener(null);
                viewHolder.radioButton.setChecked(true);
                viewHolder.radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
                notifyItemChanged(lastSelected);
                preferences.edit().putString("quotation_currency_pair", pairs.get(selected)).apply();
            }
        });

        viewHolder.delete.setOnClickListener(v -> {
            if (viewHolder.getAdapterPosition() == selected) {
                Toast.makeText(context, R.string.cannot_delete_selected_pair, Toast.LENGTH_SHORT).show();
            } else {
                String currPair = pairs.get(viewHolder.getAdapterPosition());
                pairs.remove(viewHolder.getAdapterPosition());
                notifyItemRemoved(viewHolder.getAdapterPosition());
                Set<String> temp = preferences.getStringSet("pairs", new HashSet<>());
                temp.remove(currPair);
                preferences.edit().putStringSet("pairs", temp).apply();
            }
        });

        onBind = true;
        viewHolder.radioButton.setChecked(i == selected);
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return pairs.size();
    }

    public boolean add(String pairStr) {
        Set<String> temp = preferences.getStringSet("pairs", new HashSet<>());
        if (temp.add(pairStr)) {
            pairs.add(pairStr);
            preferences.edit().putStringSet("pairs", temp).apply();
            return true;
        } else {
            Toast.makeText(context, R.string.add_pair_exist, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView first;
        TextView second;
        RadioButton radioButton;
        ImageView delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            first = itemView.findViewById(R.id.first);
            second = itemView.findViewById(R.id.second);

            radioButton = itemView.findViewById(R.id.radioButton);

            delete = itemView.findViewById(R.id.delete);
        }
    }
}
