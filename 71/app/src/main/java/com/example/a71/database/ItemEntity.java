package com.example.a71.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;
    public String name;
    public String phone;
    public String description;
    public String category;
    public String location;

    public long timestamp;
    public long lostFoundDate;

    public String imageUri;

    public long lostFoundTimestamp;

    public double latitude;
    public double longitude;
}
