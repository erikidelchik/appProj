package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Register extends AppCompatActivity {

    private Button register_button;
    private TextView email,username,password,passwordConform;

    private String email_p,username_p,password_p,passwordConform_p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        register_button = findViewById(R.id.register_button);

        email = findViewById(R.id.input_email);
        username = findViewById(R.id.input_username);
        password = findViewById(R.id.input_pass);
        passwordConform = findViewById(R.id.input_passConfrm);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                email_p = email.getText().toString();
                username_p = username.getText().toString();
                password_p = password.getText().toString();
                passwordConform_p = password.getText().toString();

                //insert check parameters function here!!!!!!!!!!!!!!

                executor.execute(()->{
                    StringBuilder result = new StringBuilder();

                    try {

                        URL url = new URL("http://192.168.1.205/LoginRegister/newSignup.php");

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);


                        //check if username and password exist in the database via the php server
                        OutputStream os = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        String data = "username=" + URLEncoder.encode(username_p, "UTF-8") +
                                "&password=" + URLEncoder.encode(password_p, "UTF-8")+
                                "&email=" + URLEncoder.encode(email_p, "UTF-8");
                        writer.write(data);
                        writer.flush();
                        writer.close();
                        os.close();

                        InputStream inputStream = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(),"something went wrong, try again",Toast.LENGTH_SHORT).show();
                    }

                    handler.post(()->{
                        if(result.toString().equals("success")){
                            Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), second.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                });



            }
        });

    }
}