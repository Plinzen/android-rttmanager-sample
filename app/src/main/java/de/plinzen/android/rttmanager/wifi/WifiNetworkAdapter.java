package de.plinzen.android.rttmanager.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.continental.android.rttmanager.R;

class WifiNetworkAdapter extends RecyclerView.Adapter<WifiNetworkAdapter.ViewHolder> {

    interface OnClickListener {
        void onItemClicked(final ScanResult wifiNetwork);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.wifi_network_item)
        RelativeLayout wifi_network_item;
        @BindView(R.id.txtSsid)
        TextView txtSsid;
        @BindView(R.id.txtMCResponder)
        TextView txtMcResponder;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private final Context context;
    private List<ScanResult> wifiNetworks;
    private OnClickListener clickListener;

    WifiNetworkAdapter(final Context context) {
        this.context = context;
        wifiNetworks = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_network, parent,
                false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position >= wifiNetworks.size()) {
            return;
        }
        final ScanResult result = wifiNetworks.get(position);
        holder.txtSsid.setText(result.SSID);
        holder.txtMcResponder.setText(String.valueOf(result.is80211mcResponder()));
        bind(holder.wifi_network_item, result, clickListener);
    }

    public void setClickListener(final OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        if (wifiNetworks == null) {
            return 0;
        }
        return wifiNetworks.size();
    }

    void setWifiNetworks(final List<ScanResult> wifiNetworks) {
        if (wifiNetworks == null) {
            return;
        }
        final List<String> duplicates = new ArrayList<>(wifiNetworks.size());
        final List<ScanResult> filteredResults = new ArrayList<>(wifiNetworks.size());
        for (ScanResult result : wifiNetworks) {
            if (!duplicates.contains(result.SSID)) {
                duplicates.add(result.SSID);
                filteredResults.add(result);
            }
        }
        this.wifiNetworks = filteredResults;
        notifyDataSetChanged();
    }


    private void bind(final View wifiNetworkItem, final ScanResult result, final OnClickListener listener) {
        wifiNetworkItem.setOnClickListener(view -> listener.onItemClicked(result));
    }
}
