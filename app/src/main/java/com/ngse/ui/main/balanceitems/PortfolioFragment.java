package com.ngse.ui.main.balanceitems;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitshares.bitshareswallet.BaseFragment;
import com.bitshares.bitshareswallet.room.BitsharesBalanceAsset;
import com.bitshares.bitshareswallet.viewmodel.WalletViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ngse.ui.FragmentContainerActivity;
import com.ngse.utility.Utils;

import org.evrazcoin.evrazwallet.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;


public class PortfolioFragment extends BaseFragment {

    public static final String TAG = PortfolioFragment.class.getName();
    private BalancesAdapter mBalancesAdapter;

    private OnFragmentInteractionListener mListener;
    private MenuItem backMenuItem;

    public interface OnPortfolioAction {
        void makeHideClicked(BitsharesBalanceAsset bitsharesBalanceAsset);

        void makeVisibleClicked(BitsharesBalanceAsset bitsharesBalanceAsset);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    private void addToHide(BitsharesBalanceAsset asset) {
        ArrayList<BitsharesBalanceAsset> hiddenAssets = getHiddenAssetsList();
        hiddenAssets.add(asset);
        getSharedPreferences().edit().putString("hidden_assets", new Gson().toJson(hiddenAssets)).apply();
    }

    private void removeFromHide(BitsharesBalanceAsset asset) {
        ArrayList<BitsharesBalanceAsset> hiddenAssets = getHiddenAssetsList();
        for (int i = 0; i < hiddenAssets.size(); i++) {
            BitsharesBalanceAsset hiddenAsset = hiddenAssets.get(i);
            if (hiddenAsset.quote.equals(asset.quote)){
                hiddenAssets.remove(hiddenAsset);
            }
        }
        getSharedPreferences().edit().putString("hidden_assets", new Gson().toJson(hiddenAssets)).apply();
    }

    public String getHiddenAssets() {
        return getSharedPreferences().getString("hidden_assets", "");
    }

    public ArrayList<BitsharesBalanceAsset> getHiddenAssetsList() {
        Type type = new TypeToken<List<BitsharesBalanceAsset>>() {
        }.getType();
        ArrayList<BitsharesBalanceAsset> hiddenAssets = new Gson().fromJson(getHiddenAssets(), type);
        if (hiddenAssets == null) {
            return new ArrayList<>();
        } else {
            return hiddenAssets;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        backMenuItem = menu.add(R.string.back)
                .setIcon(R.drawable.abc_ic_ab_back_material)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == backMenuItem)
            getActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public static PortfolioFragment newInstance() {
        return new PortfolioFragment();
    }

    public static String IS_HIDDEN_KEY = "PortfolioFragment.isHiddenAssets";

    public static PortfolioFragment newInstance(boolean isHiddenAssets) {
        PortfolioFragment portfolioFragment = new PortfolioFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_HIDDEN_KEY, isHiddenAssets);
        portfolioFragment.setArguments(bundle);
        return portfolioFragment;
    }

    public boolean isShowOnlyHiddenAssets() {
        if (getArguments() != null) {
            return getArguments().getBoolean(IS_HIDDEN_KEY, false);
        } else {
            return false;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        obtainData();
    }

    public void obtainData() {
        if (isShowOnlyHiddenAssets()) {
            ArrayList<BitsharesBalanceAsset> hiddenContainer = getHiddenAssetsList();
            mBalancesAdapter.notifyBalancesDataChanged(hiddenContainer);
        } else {
            WalletViewModel walletViewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
            walletViewModel.getBalanceData().observe(
                    this, resourceBalanceList -> {
                        ArrayList<BitsharesBalanceAsset> container = getNotHidableAsserts(resourceBalanceList.data);
                        switch (resourceBalanceList.status) {
                            case SUCCESS:
                                processShowdata(resourceBalanceList.data);
                                mBalancesAdapter.notifyBalancesDataChanged(container);
                                break;
                            case LOADING:
                                if (resourceBalanceList.data != null) {
                                    mBalancesAdapter.notifyBalancesDataChanged(container);
                                }
                                break;
                        }
                    });
        }
    }

    public ArrayList<BitsharesBalanceAsset> getNotHidableAsserts(List<BitsharesBalanceAsset> bitshares) {
        ArrayList<BitsharesBalanceAsset> filteredContainer = (ArrayList<BitsharesBalanceAsset>) bitshares;
        ArrayList<BitsharesBalanceAsset> hiddenContainer = getHiddenAssetsList();
        for (int i = 0; i < bitshares.size(); i++) {
            BitsharesBalanceAsset bitshareToShow = bitshares.get(i);
            for (int j = 0; j < hiddenContainer.size(); j++) {
                BitsharesBalanceAsset hiddenBitshare = hiddenContainer.get(j);
                if (bitshareToShow.quote.equals(hiddenBitshare.quote)) {
                    filteredContainer.remove(bitshareToShow);
                }
            }
        }
        return filteredContainer;
    }

    @Override
    public void onShow() {
        super.onShow();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.new_fragment_portfolio, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBalancesAdapter = new BalancesAdapter();
        mBalancesAdapter.setOnHideClickListener(new OnPortfolioAction() {
            @Override
            public void makeHideClicked(BitsharesBalanceAsset bitsharesBalanceAsset) {
                addToHide(bitsharesBalanceAsset);
                obtainData();
            }

            @Override
            public void makeVisibleClicked(BitsharesBalanceAsset bitsharesBalanceAsset) {
                removeFromHide(bitsharesBalanceAsset);
                obtainData();
            }
        });
        recyclerView.setAdapter(mBalancesAdapter);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int textResId = isShowOnlyHiddenAssets() ? R.string.hidden_assets : R.string.portfolio;
        int textResIdHiding = isShowOnlyHiddenAssets() ? R.string.opened : R.string.hided;
        ((TextView) view.findViewById(R.id.title)).setText(textResId);
        ((TextView) view.findViewById(R.id.tvHiding)).setText(textResIdHiding);
        view.findViewById(R.id.tvHiding).setOnClickListener(v -> {
            if (isShowOnlyHiddenAssets()){
                getActivity().onBackPressed();
            } else {
                FragmentContainerActivity.startThisActivityAndShowHideAssets(getActivity());
            }
        });
    }

    void processShowdata(List<BitsharesBalanceAsset> bitsharesBalanceAssetList) {
//        long totalBTS = 0;
        long totalBalance = 0;
        for (BitsharesBalanceAsset bitsharesBalanceAsset : bitsharesBalanceAssetList) {
//            totalBTS += bitsharesBalanceAsset.total;
            totalBalance += bitsharesBalanceAsset.balance;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            /*throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void notifyUpdate() {

    }

    class BalanceItemViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView viewUnit;
        public TextView viewAmount;
        public TextView viewOrders;
        public TextView viewAvailable;
        public ImageView ivHide;
        public View vHide;

        public BalanceItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            viewAmount = (TextView) itemView.findViewById(R.id.textViewNumber);
            viewUnit = (TextView) itemView.findViewById(R.id.textViewUnit);
//            viewEqual = (TextView) itemView.findViewById(R.id.textViewEqual);
            viewOrders = (TextView) itemView.findViewById(R.id.textExchangeRate);
            ivHide = (ImageView) itemView.findViewById(R.id.ivHide);
            vHide = (View) itemView.findViewById(R.id.unlockButtonBgView);
            viewAvailable = (TextView) itemView.findViewById(R.id.textUSDBalance);
        }
    }

    class BalancesAdapter extends RecyclerView.Adapter<BalanceItemViewHolder> {
        private List<BitsharesBalanceAsset> bitsharesBalanceAssetList;

        private OnPortfolioAction onPortfolioAction;

        void setOnHideClickListener(OnPortfolioAction onPortfolioAction) {
            this.onPortfolioAction = onPortfolioAction;
        }

        @Override
        public BalanceItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_recyclerview_item_balances, parent, false);
            return new BalanceItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BalanceItemViewHolder holder, int position) {
            BitsharesBalanceAsset bitsharesBalanceAsset = bitsharesBalanceAssetList.get(position);
            float balance = (float) bitsharesBalanceAsset.amount / bitsharesBalanceAsset.quote_precision;
            float orders = (float) bitsharesBalanceAsset.orders / bitsharesBalanceAsset.quote_precision;
            float available = balance - orders;

            holder.viewUnit.setText(bitsharesBalanceAsset.quote);
            holder.viewAmount.setText(Utils.formatDecimal(balance));
            holder.viewOrders.setText(Utils.formatDecimal(orders));
            holder.viewAvailable.setText(Utils.formatDecimal(available));
            int resourceId = isShowOnlyHiddenAssets() ? R.drawable.ic_eye : R.drawable.ic_hide;
            holder.ivHide.setImageResource(resourceId);
            holder.vHide.setOnClickListener(v -> {
                if (isShowOnlyHiddenAssets()) {
                    onPortfolioAction.makeVisibleClicked(bitsharesBalanceAsset);
                } else {
                    onPortfolioAction.makeHideClicked(bitsharesBalanceAsset);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (bitsharesBalanceAssetList == null) {
                return 0;
            } else {
                return bitsharesBalanceAssetList.size();
            }
        }

        public void notifyBalancesDataChanged(List<BitsharesBalanceAsset> bitsharesBalanceAssetList) {
            this.bitsharesBalanceAssetList = bitsharesBalanceAssetList;

            notifyDataSetChanged();
        }
    }

}
