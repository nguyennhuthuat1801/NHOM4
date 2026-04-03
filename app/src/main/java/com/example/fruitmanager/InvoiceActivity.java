package com.example.fruitmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fruitmanager.database.AppDatabase;
import com.example.fruitmanager.model.Order;
import com.example.fruitmanager.model.OrderDetail;
import com.example.fruitmanager.model.Product;
import com.example.fruitmanager.model.User;
import java.util.List;

public class InvoiceActivity extends AppCompatActivity {
    private TextView tvCustomer, tvDate, tvTotal;
    private RecyclerView rvItems;
    private Button btnBackHome;
    private AppDatabase db;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        db = AppDatabase.getInstance(this);
        orderId = getIntent().getIntExtra("ORDER_ID", -1);

        tvCustomer = findViewById(R.id.tvInvoiceCustomer);
        tvDate = findViewById(R.id.tvInvoiceDate);
        tvTotal = findViewById(R.id.tvInvoiceTotal);
        rvItems = findViewById(R.id.rvInvoiceItems);
        btnBackHome = findViewById(R.id.btnBackToHome);

        rvItems.setLayoutManager(new LinearLayoutManager(this));

        displayInvoice();

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void displayInvoice() {
        Order order = db.orderDao().getOrderById(orderId);
        if (order != null) {
            User user = db.userDao().getUserById(order.userId);
            tvCustomer.setText(user != null ? user.fullName : "Unknown");
            tvDate.setText(order.orderDate);
            tvTotal.setText(String.format("TOTAL: $%.2f", order.totalAmount));

            List<OrderDetail> details = db.orderDetailDao().getOrderDetailsByOrder(orderId);
            rvItems.setAdapter(new RecyclerView.Adapter<InvoiceViewHolder>() {
                @NonNull
                @Override
                public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
                    return new InvoiceViewHolder(view);
                }

                @Override
                public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
                    OrderDetail detail = details.get(position);
                    Product product = db.productDao().getProductById(detail.productId);
                    holder.tvName.setText(product != null ? product.name : "Unknown");
                    holder.tvQty.setText("x" + detail.quantity);
                    holder.tvPrice.setText(String.format("$%.2f", detail.unitPrice * detail.quantity));
                }

                @Override
                public int getItemCount() {
                    return details.size();
                }
            });
        }
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvPrice;
        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOrderProductName);
            tvQty = itemView.findViewById(R.id.tvOrderQuantity);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
        }
    }
}
