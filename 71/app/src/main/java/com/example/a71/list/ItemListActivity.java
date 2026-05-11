package com.example.a71.list;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a71.R;
import com.example.a71.database.AppDatabase;
import com.example.a71.database.ItemEntity;
import com.example.a71.details.ItemDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etFilter;

    ItemAdapter adapter;
    AppDatabase db;

    List<ItemEntity> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        recyclerView = findViewById(R.id.recyclerView);
        etFilter = findViewById(R.id.etFilter);

        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ItemAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(this, ItemDetailsActivity.class);
            intent.putExtra("itemId", item.id);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadData();

        // FILTER
        etFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterList(s.toString());
            }
        });
    }

    // RELOAD DATA FROM DB
    private void loadData() {
        new Thread(() -> {

            fullList = db.itemDao().getAll();

            runOnUiThread(() -> {
                adapter.updateList(fullList);
            });

        }).start();
    }

    // REFRESH EVERY TIME ACTIVITY COMES BACK
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // FILTER
    private void filterList(String text) {

        if (text.isEmpty()) {
            adapter.updateList(fullList);
            return;
        }

        List<ItemEntity> filtered = new ArrayList<>();

        for (ItemEntity item : fullList) {

            if (item.category != null &&
                    item.category.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }

        adapter.updateList(filtered);
    }
}