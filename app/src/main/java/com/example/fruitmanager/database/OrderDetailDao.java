package com.example.fruitmanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.fruitmanager.model.OrderDetail;
import java.util.List;

@Dao
public interface OrderDetailDao {
    @Insert
    void insert(OrderDetail orderDetail);

    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    List<OrderDetail> getOrderDetailsByOrder(int orderId);
}
