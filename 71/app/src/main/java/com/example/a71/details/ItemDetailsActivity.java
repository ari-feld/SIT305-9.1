package com.example.a71.details;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a71.R;
import com.example.a71.database.AppDatabase;
import com.example.a71.database.ItemEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemDetailsActivity extends AppCompatActivity {

    TextView tvName, tvType, tvCategory, tvDate, tvLocation, tvDescription, tvLostFoundDate;
    Button btnDelete;
    ImageView imgItem;

    AppDatabase db;
    ItemEntity item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        tvName = findViewById(R.id.tvName);
        tvType = findViewById(R.id.tvType);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvLostFoundDate = findViewById(R.id.tvLostFoundDate);
        imgItem = findViewById(R.id.imgItem);
        btnDelete = findViewById(R.id.btnDelete);

        db = AppDatabase.getInstance(this);

        int itemId = getIntent().getIntExtra("itemId", -1);

        loadItem(itemId);

        btnDelete.setOnClickListener(v -> {
            if (item == null) return;

            new Thread(() -> {
                db.itemDao().delete(item);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
                    finish();
                });

            }).start();
        });
    }

    private void loadItem(int id) {
        new Thread(() -> {

            item = db.itemDao().getById(id);

            runOnUiThread(() -> {
                if (item == null) return;

                tvName.setText(item.name);
                tvType.setText("Type: " + item.type);
                tvCategory.setText("Category: " + item.category);
                tvLocation.setText("Location: " + item.location);
                tvDescription.setText(item.description);

                // Posted date
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                String postedDate = sdf.format(new Date(item.timestamp));
                tvDate.setText("Posted: " + postedDate);

                // LOST / FOUND DATE (USER INPUT)
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                String lostDate = sdf2.format(new Date(item.lostFoundTimestamp));
                tvLostFoundDate.setText("Found / Lost Date: " + lostDate);

                // IMAGE LOAD
                if (item.imageUri != null && !item.imageUri.isEmpty()) {
                    imgItem.setImageURI(android.net.Uri.parse(item.imageUri));
                } else {
                    imgItem.setImageResource(android.R.color.darker_gray);
                }
            });

        }).start();
    }
}