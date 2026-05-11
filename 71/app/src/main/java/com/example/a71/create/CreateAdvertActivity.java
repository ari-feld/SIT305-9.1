package com.example.a71.create;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.a71.R;
import com.example.a71.database.AppDatabase;
import com.example.a71.database.ItemEntity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;

import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    EditText etName, etPhone, etDescription, etCategory, etLocation;

    RadioGroup radioGroup;

    Button btnSave, btnSelectImage, btnCurrentLocation;

    TextView btnSelectDate, tvImageStatus;

    Uri imageUri;

    AppDatabase db;

    Calendar selectedCalendar = Calendar.getInstance();

    double itemLatitude = 0;
    double itemLongitude = 0;

    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        db = AppDatabase.getInstance(this);

        // =========================
        // GOOGLE PLACES INIT
        // =========================

        if (!Places.isInitialized()) {
            Places.initialize(
                    getApplicationContext(),
                    getString(R.string.API_KEY)
            );
        }

        // =========================
        // LOCATION CLIENT
        // =========================

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        // =========================
        // RUNTIME PERMISSION
        // =========================

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    200
            );
        }

        // =========================
        // BIND VIEWS
        // =========================

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        etCategory = findViewById(R.id.etCategory);
        etLocation = findViewById(R.id.etLocation);

        radioGroup = findViewById(R.id.radioGroup);

        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);

        btnSelectDate = findViewById(R.id.btnSelectDate);

        tvImageStatus = findViewById(R.id.tvImageStatus);

        // =========================
        // IMAGE PICKER
        // =========================

        btnSelectImage.setOnClickListener(v -> {

            Intent intent =
                    new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.setType("image/*");

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            startActivityForResult(intent, 1);
        });

        // =========================
        // DATE PICKER
        // =========================

        btnSelectDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker =
                    new DatePickerDialog(
                            this,
                            (view, y, m, d) -> {

                                selectedCalendar.set(y, m, d);

                                SimpleDateFormat sdf =
                                        new SimpleDateFormat(
                                                "dd MMM yyyy",
                                                Locale.getDefault()
                                        );

                                btnSelectDate.setText(
                                        sdf.format(selectedCalendar.getTime())
                                );

                            },
                            year,
                            month,
                            day
                    );

            picker.show();
        });

        // =========================
        // PLACE AUTOCOMPLETE
        // =========================

        etLocation.setFocusable(false);

        etLocation.setOnClickListener(v -> {

            List<Place.Field> fields = Arrays.asList(
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
            );

            Intent intent =
                    new Autocomplete.IntentBuilder(
                            AutocompleteActivityMode.OVERLAY,
                            fields
                    ).build(this);

            startActivityForResult(intent, 100);
        });

        // =========================
        // CURRENT LOCATION BUTTON
        // =========================

        btnCurrentLocation.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                        this,
                        "Location permission not granted",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (location != null) {

                            itemLatitude = location.getLatitude();
                            itemLongitude = location.getLongitude();

                            etLocation.setText(
                                    "Current Location Selected"
                            );

                            Toast.makeText(
                                    this,
                                    "Location loaded",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        // =========================
        // SAVE
        // =========================

        btnSave.setOnClickListener(v -> {

            if (imageUri == null) {

                Toast.makeText(
                        this,
                        "Select an image",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            int selectedId =
                    radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {

                Toast.makeText(
                        this,
                        "Select Lost or Found",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String type =
                    (selectedId == R.id.radioLost)
                            ? "Lost"
                            : "Found";

            ItemEntity item = new ItemEntity();

            item.type = type;

            item.name =
                    etName.getText().toString();

            item.phone =
                    etPhone.getText().toString();

            item.description =
                    etDescription.getText().toString();

            item.category =
                    etCategory.getText().toString();

            item.location =
                    etLocation.getText().toString();

            item.timestamp =
                    System.currentTimeMillis();

            item.lostFoundTimestamp =
                    selectedCalendar.getTimeInMillis();

            item.imageUri =
                    imageUri.toString();

            // SAVE COORDINATES
            item.latitude = itemLatitude;
            item.longitude = itemLongitude;

            new Thread(() -> {

                db.itemDao().insert(item);

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Advert Saved!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            }).start();
        });
    }

    // =========================
    // ACTIVITY RESULTS
    // =========================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        // =========================
        // IMAGE RESULT
        // =========================

        if (requestCode == 1
                && resultCode == RESULT_OK
                && data != null) {

            imageUri = data.getData();

            if (imageUri != null) {

                final int takeFlags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION;

                getContentResolver()
                        .takePersistableUriPermission(
                                imageUri,
                                takeFlags
                        );

                tvImageStatus.setText(
                        "Image selected ✓"
                );
            }
        }

        // =========================
        // PLACE RESULT
        // =========================

        if (requestCode == 100
                && resultCode == RESULT_OK
                && data != null) {

            Place place =
                    Autocomplete.getPlaceFromIntent(data);

            etLocation.setText(place.getAddress());

            if (place.getLatLng() != null) {

                itemLatitude =
                        place.getLatLng().latitude;

                itemLongitude =
                        place.getLatLng().longitude;
            }
        }
    }
}