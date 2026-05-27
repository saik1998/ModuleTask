package com.example.moduletask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.app.Dialog;

public class ScannerActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    CodeScannerView scannerView;
    ImageView btnBack;
    private boolean isScanned = false;
    private Dialog loadingDialog;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scanner);

        scannerView = findViewById(R.id.scanner_view);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.progressBarLL);

        openCameraScan();
        btnBack.setOnClickListener(v -> {

            finish();

        });
        /*loadingDialog = new Dialog(this);

        loadingDialog.setContentView(
                R.layout.loading_dialog
        );

        loadingDialog.getWindow()
                .setBackgroundDrawableResource(
                        android.R.color.transparent
                );

        loadingDialog.setCancelable(false);*/


    }

    private void openCameraScan() {
        try {
            mCodeScanner = new CodeScanner(this, scannerView);
            mCodeScanner.setDecodeCallback(result -> {

                runOnUiThread(() -> {

                    if(isScanned) {
                        return;
                    }

                    isScanned = true;

                    mCodeScanner.stopPreview();
                    scannerView.setEnabled(false);

                    CallApi(result.getText());

                });

            });
            scannerView.setOnClickListener(view -> mCodeScanner.startPreview());


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void CallApi(String text) {

        if (text == null || text.isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid Barcode",
                    Toast.LENGTH_SHORT
            ).show();

            isScanned = false;

            mCodeScanner.startPreview();

            return;
        }

        if (!isNetworkConnection(this)) {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
            isScanned = false;

            mCodeScanner.startPreview();
            return;
        }

        linearLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);


        OkHttpClient client = new OkHttpClient();

        // Build URL with query param
        HttpUrl url = HttpUrl.parse("http://nsluat.empover.com/fmuat/Api_Aparna/dispatch_tracking")
                .newBuilder()
                .addQueryParameter("barcode", text) // THIS is key
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get() // ✅ GET request
                .addHeader("Authorization", "Basic cmVzdDpzZWVkc0BhZG1pbg==")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(() -> {

                    linearLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);

                    isScanned = false;

                    mCodeScanner.startPreview();

                    Toast.makeText(
                            ScannerActivity.this,
                            "Network Error",
                            Toast.LENGTH_LONG
                    ).show();

                });

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData;

                if(response.body() != null){
                    responseData = response.body().string();
                } else {
                    responseData = "";
                }

                runOnUiThread(() -> {
                    if (response.isSuccessful()
                            && !responseData.isEmpty()) {
                        Log.d("API_RESPONSE", responseData);
                        Toast.makeText(ScannerActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        Gson gson = new Gson();

                        BarcodeModel model =
                                gson.fromJson(responseData, BarcodeModel.class);

                        if(model == null){

                            linearLayout.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);

                            isScanned = false;

                            mCodeScanner.startPreview();

                            Toast.makeText(
                                    ScannerActivity.this,
                                    "Invalid Response",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        ArrayList<TrackerResModel> list =
                                model.getBarcode_data();
                        if(list == null || list.isEmpty()) {

                            linearLayout.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(
                                    ScannerActivity.this,
                                    "No Data Found",
                                    Toast.LENGTH_LONG
                            ).show();

                            isScanned = false;

                            mCodeScanner.startPreview();

                            return;
                        }
//                        loadingDialog.dismiss();

                        linearLayout.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);

                        openDetailsScreen(list);

                    } else {

                        Toast.makeText(
                                ScannerActivity.this,
                                responseData.toString(),Toast.LENGTH_SHORT).show();
                        linearLayout.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);

                        isScanned = false;

                        mCodeScanner.startPreview();

                       
                    }
                });
            }
        });
    }
    public static boolean isNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCodeScanner != null) {
            try{
                mCodeScanner.startPreview();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {

        if(mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();

    }
    private void openDetailsScreen(ArrayList<TrackerResModel> list) {
        Intent intent =
                new Intent(
                        ScannerActivity.this,
                        DetailsActivity.class);
        intent.putExtra("data", list);
        intent.putExtra("source", "scanner");


        startActivity(intent);
        finish();

    }

}
