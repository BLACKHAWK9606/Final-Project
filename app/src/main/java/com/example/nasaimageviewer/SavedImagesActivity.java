package com.example.nasaimageviewer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class SavedImagesActivity extends AppCompatActivity {

    private ListView savedImagesListView;
    private TextView noSavedImagesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_images);

        savedImagesListView = findViewById(R.id.savedImagesListView);
        noSavedImagesTextView = findViewById(R.id.noSavedImagesTextView);

        loadSavedImages();
    }

    private void loadSavedImages() {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedImages", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        if (allEntries.isEmpty()) {
            noSavedImagesTextView.setVisibility(TextView.VISIBLE);
            savedImagesListView.setVisibility(ListView.GONE);
        } else {
            noSavedImagesTextView.setVisibility(TextView.GONE);
            savedImagesListView.setVisibility(ListView.VISIBLE);

            ArrayList<String> imageTitles = new ArrayList<>();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                try {
                    JSONObject jsonObject = new JSONObject(entry.getValue().toString());
                    String title = jsonObject.getString("title");
                    imageTitles.add("Date: " + entry.getKey() + "\nTitle: " + title);
                } catch (JSONException e) {
                    Toast.makeText(this, "Error loading saved images", Toast.LENGTH_SHORT).show();
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, imageTitles);
            savedImagesListView.setAdapter(adapter);
        }
    }
}
