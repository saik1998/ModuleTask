package com.example.moduletask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.app.Dialog;
import android.app.Dialog;
import android.widget.EditText;
import android.widget.Button;

public class SearchActivity extends AppCompatActivity {
    private Dialog loadingDialog;

    EditText searchTxt;
    Button searchBtn;
    ImageView btnBack;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        searchTxt = findViewById(R.id.barCode);
        searchBtn = findViewById(R.id.search);
        btnBack = findViewById(R.id.btnBack);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingDialog.show();

                CallApi(
                        searchTxt.getText()
                                .toString()
                                .trim()
                );
            }
        });
        btnBack.setOnClickListener(v -> {

            finish();

        });
        loadingDialog = new Dialog(this);

        loadingDialog.setContentView(
                R.layout.loading_dialog
        );

        loadingDialog.getWindow()
                .setBackgroundDrawableResource(
                        android.R.color.transparent
                );

        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);




    }

    private void CallApi(String text) {

        if (text == null || text.isEmpty()) return;

        if (!isNetworkConnection(this)) {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
            return;
        }

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

                    loadingDialog.dismiss();

                    Toast.makeText(
                            SearchActivity.this,
                            "API Failed",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData;

                if(response.body() != null){
                    responseData = response.body().string();
                }else{
                    responseData = "";
                }

                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (response.isSuccessful()) {
                        Log.d("API_RESPONSE", responseData);
                        Toast.makeText(SearchActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        Gson gson = new Gson();

                        BarcodeModel model =
                                gson.fromJson(responseData, BarcodeModel.class);

                        if(model == null){

                            Toast.makeText(
                                    SearchActivity.this,
                                    "Invalid Response",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        ArrayList<TrackerResModel> list =
                                model.getBarcode_data();

                        if(list == null || list.isEmpty()){

                            Toast.makeText(
                                    SearchActivity.this,
                                    "No Data Found",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        openDetailsScreen(list);
                    } else {
                        Toast.makeText(
                                SearchActivity.this,
                                responseData.toString(),Toast.LENGTH_SHORT).show();                    }
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

    private void openDetailsScreen(ArrayList<TrackerResModel> list) {
        Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
        intent.putExtra("data", list);
        intent.putExtra("source", "scanner");
        startActivity(intent);
        finish();
    }

}