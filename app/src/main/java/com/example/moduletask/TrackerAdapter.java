package com.example.moduletask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrackerAdapter extends RecyclerView.Adapter<TrackerAdapter.ViewHolder> {

    private List<TrackerResModel> list;

    public TrackerAdapter(List<TrackerResModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_items_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackerResModel item = list.get(position);

        holder.tvBarcode.setText(item.getBarcode());
        holder.tvVariety.setText(item.getVariety());
        holder.tvPacketQty.setText(item.getQty());
        holder.txtLotNum.setText(item.getLot());
        holder.fromLocation.setText(item.getFrom_location());
        holder.customerCode.setText(item.getCust_code());
        holder.saleOrderNumber.setText(item.getSale_order_number());
        holder.dispatchDate.setText(item.getDispatch_date());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBarcode, tvVariety, tvPacketQty,txtLotNum,fromLocation,customerCode,saleOrderNumber, dispatchDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarcode = itemView.findViewById(R.id.txtBarcode);
            tvVariety = itemView.findViewById(R.id.txtvariety);
            tvPacketQty = itemView.findViewById(R.id.txtQty);
            txtLotNum = itemView.findViewById(R.id.txtLot);
            fromLocation = itemView.findViewById(R.id.txtfrom_location);
            customerCode = itemView.findViewById(R.id.txtCust_code);
            saleOrderNumber = itemView.findViewById(R.id.txtsale_order_number);
            dispatchDate = itemView.findViewById(R.id.txtdispatch_date);
        }
    }
}
