package com.example.moduletask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();

        fab.setOnClickListener(view -> {

            showCustomDialog();

        });

    }

    private void initViews() {
        try{
            fab = findViewById(R.id.fabAddIcon);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Inside your Activity
    public void showCustomDialog() {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // RecyclerView setup
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Example adapter (replace with your actual adapter)
        ArrayList<Model> modelList = new ArrayList<>();

        Model model = new Model();


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("select item");
        arrayList.add("kothuru");
        arrayList.add("medchal");
        arrayList.add("hyderabad");
        arrayList.add("kakinada");
        arrayList.add("Rajaundry");
        arrayList.add("vizag");
        arrayList.add("jaggampeta");
        model.setSpinnerListData(arrayList);
        model.setBatch("field");
        model.setQuantity("1");
        modelList.add(model);

        MyAdapter adapter = new MyAdapter(arrayList,modelList);
        recyclerView.setAdapter(adapter);

        // Buttons
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnClose = dialogView.findViewById(R.id.btnClose);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnClose.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        btnSubmit.setOnClickListener(v -> {
            // TODO: handle submit logic
            Toast.makeText(this, "Submitted!", Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        });
    }
}