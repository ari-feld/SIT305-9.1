package com.example.a71.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {

    @Insert
    void insert(ItemEntity item);

    @Query("SELECT * FROM items ORDER BY timestamp DESC")
    List<ItemEntity> getAll();

    @Query("SELECT * FROM items WHERE category = :category ORDER BY timestamp DESC")
    List<ItemEntity> filterByCategory(String category);

    @Query("SELECT * FROM items WHERE id = :id")
    ItemEntity getById(int id);

    @Delete
    void delete(ItemEntity item);
}