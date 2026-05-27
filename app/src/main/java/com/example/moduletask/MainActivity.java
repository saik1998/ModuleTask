package com.example.moduletask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {

    CardView cardView;
    ImageView imageProfile;
    DrawerLayout drawerLayout;
    private static final int CAMERA_REQUEST_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        cardView = findViewById(R.id.cardView);
        imageProfile = findViewById(R.id.imgProfile);
        drawerLayout = findViewById(R.id.drawerLayout);

        imageProfile.setOnClickListener(v -> {

            drawerLayout.openDrawer(
                    androidx.core.view.GravityCompat.START
            );

        });

        cardView.setOnClickListener(view -> {
            openBottomDialog();
        });




    }

    private void openBottomDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);

        dialog.setContentView(view);
        dialog.show();

        ImageView btnScan = view.findViewById(R.id.btnScan);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        btnScan.setOnClickListener(v ->{
            dialog.dismiss();
            checkCameraPermission();
        });


        btnSearch.setOnClickListener(view1 -> {
            dialog.dismiss();
            Intent intent = new Intent(this,SearchActivity.class);
            startActivity(intent);
        });



    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            openScanner(); // Permission already granted

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }
    }

    private void openScanner() {

        Intent intent = new Intent(this,ScannerActivity.class);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openScanner();

            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}