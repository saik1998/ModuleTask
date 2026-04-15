package com.example.moduletask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<String> spinnerData;
    private ArrayList<Model> list;
    public MyAdapter(ArrayList<String> arrayList, ArrayList<Model> modelList) {
        this.spinnerData = arrayList;
        this.list = modelList;
    }


    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        /*Model model = list.get(position);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                spinnerData
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        holder.spinner.setAdapter(adapter);
        holder.batch.setText(model.getBatch());
        holder.quantity.setText(model.getQuantity());

        if (position == list.size() - 1) {
            holder.buttonPlus.setVisibility(View.VISIBLE);
        } else {
            holder.buttonPlus.setVisibility(View.GONE);
        }

        holder.buttonPlus.setOnClickListener(view -> {

            int pos = holder.getAdapterPosition();

            list.add(pos + 1, new Model()); // ✅ add new row below current
            notifyItemInserted(pos + 1);
        });


//        holder.buttonPlus.setOnClickListener(view -> {
//            holder.buttonPlus.setVisibility(View.GONE);
//            holder.buttonMinus.setVisibility(View.VISIBLE);
//            holder.layout.setVisibility(View.VISIBLE);
////            list.add(new Model()); // add empty row
////            notifyItemInserted(list.size() - 1);
//        });

        holder.buttonMinus.setOnClickListener(view -> {

            if (list.size() > 1) {
                int pos = holder.getAdapterPosition();

                list.remove(pos); // ✅ remove row
                notifyItemRemoved(pos);
            }
        });

//        holder.buttonMinus.setOnClickListener(view -> {
////            if (list.size() > 1) { // prevent removing last item
//////                int pos = holder.getAdapterPosition();
//////                list.remove(pos);
//////                notifyItemRemoved(pos);
//////                notifyItemRangeChanged(pos, list.size());
//////            }
//
//            holder.layout.setVisibility(View.GONE);
//        });*/

        Model model = list.get(position);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                spinnerData
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        holder.spinner.setAdapter(adapter);

        holder.batch.setText(model.getBatch());
        holder.quantity.setText(model.getQuantity());

        // ✅ Show + only on last item
        if (position == list.size() - 1) {
            holder.buttonPlus.setVisibility(View.VISIBLE);
            holder.buttonMinus.setVisibility(View.GONE);
        } else {
            holder.buttonPlus.setVisibility(View.GONE);
//            holder.buttonMinus.setVisibility(View.VISIBLE);
        }

        // ✅ Show - only if more than 1 row
//        holder.buttonMinus.setVisibility(
//                list.size() > 1 ? View.VISIBLE : View.VISIBLE
//        );

        // ➕ ADD ROW
        holder.buttonPlus.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();

            Model newModel = new Model();

            // Generate different values
            newModel.setBatch("field " + (list.size() + 1));
            newModel.setQuantity(String.valueOf(list.size() + 1));

            list.add(pos + 1, newModel);
            notifyItemInserted(pos + 1);
            holder.buttonPlus.setVisibility(View.GONE);
            holder.buttonMinus.setVisibility(View.VISIBLE);
//            int pos = holder.getAdapterPosition();
//
//            list.add(pos + 1, new Model());
//            notifyItemInserted(pos + 1);
        });

        // ➖ REMOVE ROW
        holder.buttonMinus.setOnClickListener(v -> {
            if (list.size() > 1) {
                int pos = holder.getAdapterPosition();

                list.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

/*
    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        String value = spinnerData.get(position);

        // Set spinner data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                spinnerData
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        holder.spinner.setAdapter(adapter);

        // Set other fields
        holder.batch.setText("Batch " + position);
        holder.quantity.setText(value);
    }
*/

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Spinner spinner;
        TextView batch, quantity;
        MaterialButton buttonPlus,buttonMinus;
        LinearLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            spinner = itemView.findViewById(R.id.spinnerValues);
            batch = itemView.findViewById(R.id.batch);
            quantity = itemView.findViewById(R.id.quantity);
            buttonMinus = itemView.findViewById(R.id.btnMinus);
            buttonPlus = itemView.findViewById(R.id.btnPlus);
            layout = itemView.findViewById(R.id.tableLayout);
        }
    }
}
