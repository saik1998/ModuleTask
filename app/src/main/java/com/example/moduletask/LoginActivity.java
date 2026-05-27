package com.example.moduletask;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        EditText edtUsename,edtPassword;
        Button btnLogin;
        edtUsename =findViewById(R.id.edtUsername);
        edtPassword =findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v ->{
            String username =
                    edtUsename.getText().toString();
            String password =
                    edtPassword.getText().toString();
            if (username.equals("barcode")
                    && password.equals("password")){
                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        );
                intent.putExtra(
                        "username",
                        username
                );
                startActivity(intent);
                finish();

            }
            else {
                Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });

    }
}