package com.example.nasaimageviewer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView resultTextView;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Activity created");

        // Initialize UI elements
        resultTextView = findViewById(R.id.resultTextView);
        Button datePickerButton = findViewById(R.id.datePickerButton);
        Button saveImageButton = findViewById(R.id.saveImageButton);
        Button viewSavedImagesButton = findViewById(R.id.viewSavedImagesButton);

        // Set click listeners
        datePickerButton.setOnClickListener(v -> {
            Log.d(TAG, "Date Picker button clicked");
            openDatePicker();
        });
        saveImageButton.setOnClickListener(v -> {
            Log.d(TAG, "Save Image button clicked");
            saveImage();
        });
        viewSavedImagesButton.setOnClickListener(v -> {
            Log.d(TAG, "View Saved Images button clicked");
            startActivity(new Intent(this, SavedImagesActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");
    }

    private void openDatePicker() {
        Log.d(TAG, "openDatePicker: Opening date picker");

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            Log.d(TAG, "Selected date: " + selectedDate);

            fetchNasaImage(selectedDate);
        }, year, month, day).show();
    }

    private void fetchNasaImage(@NonNull String date) {
        String apiKey = getString(R.string.nasa_api_key);
        String apiUrl = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey + "&date=" + date;

        Log.d(TAG, "fetchNasaImage: Fetching data from URL: " + apiUrl);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "fetchNasaImage: HTTP response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                } else {
                    throw new Exception("HTTP error code: " + responseCode);
                }

                handler.post(() -> processApiResponse(result.toString()));
            } catch (Exception e) {
                Log.e(TAG, "fetchNasaImage: Error fetching data", e);
                handler.post(() -> Toast.makeText(MainActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void processApiResponse(String response) {
        Log.d(TAG, "processApiResponse: Processing response");

        try {
            JSONObject jsonObject = new JSONObject(response);
            String title = jsonObject.getString("title");
            String url = jsonObject.getString("url");
            String hdUrl = jsonObject.optString("hdurl", "No HD URL available");

            resultTextView.setText(getString(R.string.api_result_format, title, url, hdUrl));
            Log.d(TAG, "processApiResponse: API response processed successfully");
        } catch (JSONException e) {
            Log.e(TAG, "processApiResponse: Error parsing JSON", e);
            Toast.makeText(this, "Error parsing data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "No image to save!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "saveImage: No image selected to save");
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("SavedImages", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(selectedDate, resultTextView.getText().toString());
        editor.apply();

        Log.d(TAG, "saveImage: Image saved for date " + selectedDate);
        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
    }
}
