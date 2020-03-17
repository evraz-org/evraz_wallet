package com.ngse.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bitshares.bitshareswallet.BaseFragment;
import com.bitshares.bitshareswallet.market.MarketTicker;
import com.bitshares.bitshareswallet.room.BitsharesMarketTicker;
import com.bitshares.bitshareswallet.viewmodel.QuotationViewModel;
import com.good.code.starts.here.pairs.PairsRecyclerAdapter;
import com.ngse.ui.NewMainActivity;

import org.evrazcoin.evrazwallet.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExchangeFragment extends BaseFragment {
    private static final String TAG = "QuotationFragment";

    private QuotationCurrencyPairAdapter quotationCurrencyPairAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private FloatingActionButton fabAddPair;
    private PairsRecyclerAdapter adapter;

    public ExchangeFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static ExchangeFragment newInstance() {
        ExchangeFragment fragment = new ExchangeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_fragment_exchange, container, false);
        ButterKnife.bind(this, view);

        quotationCurrencyPairAdapter = new QuotationCurrencyPairAdapter(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(quotationCurrencyPairAdapter);
        mRecyclerView.setItemAnimator(null);


        QuotationViewModel viewModel = ViewModelProviders.of(getActivity()).get(QuotationViewModel.class);
        quotationCurrencyPairAdapter.setOnItemClickListenr(new QuotationCurrencyPairAdapter.OnItemClickListner() {
            @Override
            public void onItemClick(View view, int position) {
                //mListener.notifyCurrencyPairChange();
                if (quotationCurrencyPairAdapter.getSelectedMarketTicker() != null) {
                    MarketTicker marketTicker = quotationCurrencyPairAdapter.getSelectedMarketTicker().marketTicker;
                    viewModel.selectedMarketTicker(new Pair(marketTicker.base, marketTicker.quote));
                    ((NewMainActivity) getActivity()).showTradingScheduleFragment();
                }

            }
        });

        fabAddPair = view.findViewById(R.id.addServer);

        fabAddPair.setOnClickListener(v -> {
            View dialogView = inflater.inflate(R.layout.dialog_add_pair, null);
            EditText first = dialogView.findViewById(R.id.editText);
            EditText second = dialogView.findViewById(R.id.editText2);

            new AlertDialog.Builder(ExchangeFragment.this.getActivity())
                    .setTitle(R.string.add_pair)
                    .setView(dialogView)
                    .setPositiveButton(R.string.add, (dialogInterface, i) -> {
                        String firstStr = first.getText().toString();
                        String secondStr = second.getText().toString();
                        if (!check(firstStr) || !check(secondStr) || first.equals(second)) {
                            Toast.makeText(getActivity(), R.string.add_pair_err, Toast.LENGTH_SHORT).show();
                        } else {
                            if (adapter.add(firstStr.toUpperCase() + ":" + secondStr.toUpperCase())) {
                                onShow();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        });
        adapter = new PairsRecyclerAdapter(getActivity());


        return view;
    }

    private boolean check(String name) {
        if (name.length() == 0) return false;
        if (name.charAt(0) == '.' || name.charAt(name.length() - 1) == '.') return false;
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if (!Character.isLetter(c) && c != '.') {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onShow() {
        super.onShow();

        QuotationViewModel viewModel = ViewModelProviders.of(getActivity()).get(QuotationViewModel.class);
        viewModel.getMarketTicker().observe(
                this,
                marketTickerListResource -> {
                    switch (marketTickerListResource.status) {
                        case ERROR:
                            break;
                        case LOADING:
                            if (marketTickerListResource.data != null && marketTickerListResource.data.size() != 0) {
                                ArrayList<BitsharesMarketTicker> container = new ArrayList<>();
                                for (int i = 0; i < marketTickerListResource.data.size(); i++) {
                                    BitsharesMarketTicker item = marketTickerListResource.data.get(i);
                                    if (
                                            item.marketTicker.base.equals("BTC") ||
                                                    item.marketTicker.base.equals("BTS") ||
                                                    item.marketTicker.base.equals("EVRAZ") ||
                                                    item.marketTicker.quote.equals("BTC") ||
                                                    item.marketTicker.quote.equals("BTS") ||
                                                    item.marketTicker.quote.equals("EVRAZ")) {
                                        container.add(item);
                                    }
                                }
                                quotationCurrencyPairAdapter.notifyDataUpdated(container);
                                MarketTicker marketTicker = quotationCurrencyPairAdapter.getSelectedMarketTicker().marketTicker;
                                viewModel.selectedMarketTicker(new Pair(marketTicker.base, marketTicker.quote));
                            }
                            break;
                        case SUCCESS:
                            ArrayList<BitsharesMarketTicker> container = new ArrayList<>();
                            for (int i = 0; i < marketTickerListResource.data.size(); i++) {
                                BitsharesMarketTicker item = marketTickerListResource.data.get(i);
                                if (
                                        item.marketTicker.base.equals("BTC") ||
                                                item.marketTicker.base.equals("BTS") ||
                                                item.marketTicker.base.equals("EVRAZ") ||
                                                item.marketTicker.quote.equals("BTC") ||
                                                item.marketTicker.quote.equals("BTS") ||
                                                item.marketTicker.quote.equals("EVRAZ")) {
                                    container.add(item);
                                }
                            }
                            quotationCurrencyPairAdapter.notifyDataUpdated(container);
                            MarketTicker marketTicker = quotationCurrencyPairAdapter.getSelectedMarketTicker().marketTicker;
                            viewModel.selectedMarketTicker(new Pair(marketTicker.base, marketTicker.quote));
                            break;
                    }
                });


        viewModel.getSelectedMarketTicker().observe(this, currencyPair -> quotationCurrencyPairAdapter.notifyDataSetChanged());
    }

    @Override
    public void onHide() {
        super.onHide();
    }


}
