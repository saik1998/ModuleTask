package com.example.moduletask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;


import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {


        RecyclerView recyclerView;
        ImageView btnBack;

    Button btnPlus, btnScan, btnSearch;

    boolean isMenuOpen = false;


        @SuppressLint({"MissingInflatedId", "WrongViewCast"})
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_details);
            btnBack = findViewById(R.id.btnBack);
            btnPlus = findViewById(R.id.btnPlus);

            btnScan = findViewById(R.id.btnScan);

            btnSearch = findViewById(R.id.btnSearch);

            recyclerView = findViewById(R.id.listView);

            btnBack.setOnClickListener(v -> {
                finish();


            });


            btnPlus.setOnClickListener(v -> {

                if(isMenuOpen){

                    btnScan.setVisibility(View.GONE);

                    btnSearch.setVisibility(View.GONE);

                    isMenuOpen = false;

                }else{

                    btnScan.setVisibility(View.VISIBLE);

                    btnSearch.setVisibility(View.VISIBLE);

                    isMenuOpen = true;
                }
            });

            btnScan.setOnClickListener(v -> {
                        Intent intent = new Intent(
                                DetailsActivity.this,
                                ScannerActivity.class
                        );
                        startActivity(intent);
                        finish();
                    });

            btnSearch.setOnClickListener(v ->{
                Intent intent =
                        new Intent(
                                DetailsActivity.this,
                                SearchActivity.class

                        );
                startActivity(intent);
                finish();
            });





            ArrayList<TrackerResModel> list =
                    (ArrayList<TrackerResModel>)
                            getIntent().getSerializableExtra("data");

            if(list == null){
                list = new ArrayList<>();
            }

            recyclerView.setLayoutManager(
                    new LinearLayoutManager(this)
            );

            recyclerView.setAdapter(
                    new TrackerAdapter(list)
            );
        }

    }