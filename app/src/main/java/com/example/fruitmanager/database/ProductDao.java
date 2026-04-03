package com.example.fruitmanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.fruitmanager.model.Product;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductById(int productId);
}
