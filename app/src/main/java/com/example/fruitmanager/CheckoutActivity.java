package com.example.fruitmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fruitmanager.database.AppDatabase;
import com.example.fruitmanager.model.Order;
import com.example.fruitmanager.model.OrderDetail;
import com.example.fruitmanager.model.Product;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private RecyclerView rvOrderDetails;
    private TextView tvTotalAmount;
    private Button btnCheckout, btnContinueShopping;
    private AppDatabase db;
    private int orderId;
    private List<OrderDetail> orderDetails;
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = AppDatabase.getInstance(this);
        orderId = getIntent().getIntExtra("ORDER_ID", -1);

        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        
        loadOrderDetails();

        btnCheckout.setOnClickListener(v -> {
            Order order = db.orderDao().getOrderById(orderId);
            if (order != null) {
                order.status = "Paid";
                order.totalAmount = totalAmount;
                db.orderDao().update(order);
                Toast.makeText(this, "Order Paid Successfully!", Toast.LENGTH_SHORT).show();
                
                // Chuyển sang màn hình hiển thị hóa đơn
                Intent intent = new Intent(this, InvoiceActivity.class);
                intent.putExtra("ORDER_ID", orderId);
                startActivity(intent);
                finish();
            }
        });

        btnContinueShopping.setOnClickListener(v -> finish());
    }

    private void loadOrderDetails() {
        orderDetails = db.orderDetailDao().getOrderDetailsByOrder(orderId);
        totalAmount = 0;
        for (OrderDetail detail : orderDetails) {
            totalAmount += detail.unitPrice * detail.quantity;
        }
        tvTotalAmount.setText(String.format("Total: $%.2f", totalAmount));
        
        rvOrderDetails.setAdapter(new RecyclerView.Adapter<DetailViewHolder>() {
            @NonNull
            @Override
            public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
                return new DetailViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
                OrderDetail detail = orderDetails.get(position);
                Product product = db.productDao().getProductById(detail.productId);
                if (product != null) {
                    holder.tvName.setText(product.name);
                }
                holder.tvQuantity.setText("x" + detail.quantity);
                holder.tvPrice.setText(String.format("$%.2f", detail.unitPrice * detail.quantity));
            }

            @Override
            public int getItemCount() {
                return orderDetails.size();
            }
        });
    }

    static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;
        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOrderProductName);
            tvQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
        }
    }
}
