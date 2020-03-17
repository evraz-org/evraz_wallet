package com.bitshares.bitshareswallet;


import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitshares.bitshareswallet.room.BitsharesBalanceAsset;
import com.bitshares.bitshareswallet.viewmodel.QuotationViewModel;
import com.good.code.starts.here.TokenHideAdapter;
import com.good.code.starts.here.pairs.PairsFragment;

import com.good.code.starts.here.servers.ServersFragment;

import org.evrazcoin.evrazwallet.R;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends PreferenceFragmentCompat {

    private boolean loaded = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        /*Preference preference = findPreference("currency_setting");
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent intent = new Intent();
                intent.putExtra("setting_changed", "currency_setting");
                getActivity().setResult(Activity.RESULT_OK, intent);
                return true;
            }
        });*/

        Preference pairSelectPreference = findPreference("quotation_currency_pair");
        pairSelectPreference.setOnPreferenceClickListener(p -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PairsFragment.newInstance()).addToBackStack(null).commit();
            return true;
        });

        Preference hidePreference = findPreference("hide");
        hidePreference.setOnPreferenceClickListener(p -> {

            ProgressDialog progressDialog = ProgressDialog.show(getActivity(),getString(R.string.loading_assets), "");
            BitsharesApplication.getInstance().getBitsharesDatabase().getBitsharesDao().queryAvaliableBalances("USD").observe(this, bitsharesBalanceAssets -> {

                if(!loaded) {
                    loaded = true;
                    List<String> symbolList = new ArrayList<>();
                    symbolList.add("FINTEH");
                    for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssets) {
                        if (!bitsharesBalanceAsset.quote.equals("FINTEH"))
                            symbolList.add(bitsharesBalanceAsset.quote);
                    }

                    RecyclerView recyclerView = new RecyclerView(getActivity());

                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    TokenHideAdapter adapter = new TokenHideAdapter(getActivity(), symbolList);
                    recyclerView.setAdapter(adapter);

                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.hide)
                            .setView(recyclerView)
                            .setPositiveButton(R.string.save, (dialog1, which) -> {
                                adapter.save();
                                loaded = false;
                            })
                            .setNegativeButton(R.string.cancel, ((dialog2, which) -> {
                                loaded = false;
                            } ))
                            .create();

                    progressDialog.dismiss();
                    dialog.show();
                }
            });
            return true;
        });

        Preference serverSelectPreference = findPreference("full_node_api_server");
        serverSelectPreference.setOnPreferenceClickListener(p -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ServersFragment.newInstance()).addToBackStack(null).commit();
            return true;
        });
    }
}
