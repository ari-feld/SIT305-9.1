package com.example.a71.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.a71.R;
import com.example.a71.database.AppDatabase;
import com.example.a71.database.ItemEntity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback {

    GoogleMap mMap;

    AppDatabase db;

    FusedLocationProviderClient fusedLocationClient;

    double userLat;
    double userLng;

    // Radius in KM
    final double SEARCH_RADIUS_KM = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = AppDatabase.getInstance(this);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                        LatLng userLocation =
                                new LatLng(userLat, userLng);
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        userLocation,
                                        12
                                )
                        );
                        loadMarkers();
                    }
                });
    }
    private void loadMarkers() {

        new Thread(() -> {

            List<ItemEntity> items =
                    db.itemDao().getAll();

            runOnUiThread(() -> {

                for (ItemEntity item : items) {

                    double distance =
                            calculateDistanceKm(
                                    userLat,
                                    userLng,
                                    item.latitude,
                                    item.longitude
                            );

                    // Radius filter
                    if (distance <= SEARCH_RADIUS_KM) {

                        LatLng position =
                                new LatLng(
                                        item.latitude,
                                        item.longitude
                                );

                        BitmapDescriptor color =
                                item.type.equals("Lost")
                                        ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                        : BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(position)
                                        .title(item.name)
                                        .icon(color)
                                        .snippet(item.type + " • " + distance + " km away")
                        );
                    }
                }
            });

        }).start();
    }

    // =========================
    // DISTANCE CALCULATION
    // =========================

    private double calculateDistanceKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        float[] results = new float[1];

        Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                results
        );

        return results[0] / 1000.0;
    }
}