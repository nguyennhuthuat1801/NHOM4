package com.example.fruitmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fruitmanager.database.AppDatabase;
import com.example.fruitmanager.model.Order;
import com.example.fruitmanager.model.OrderDetail;
import com.example.fruitmanager.model.Product;
import com.example.fruitmanager.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvDesc;
    private EditText etQuantity;
    private Button btnAddToCart;
    private AppDatabase db;
    private SessionManager session;
    private int productId;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        tvName = findViewById(R.id.tvDetailName);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvDesc = findViewById(R.id.tvDetailDesc);
        etQuantity = findViewById(R.id.etQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        product = db.productDao().getProductById(productId);

        if (product != null) {
            tvName.setText(product.name);
            tvPrice.setText(String.format("$%.2f", product.price));
            tvDesc.setText(product.description);
        }

        btnAddToCart.setOnClickListener(v -> {
            if (!session.isLoggedIn()) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            addToCart();
        });
    }

    private void addToCart() {
        int userId = session.getUserId();
        int quantity = Integer.parseInt(etQuantity.getText().toString());

        Order pendingOrder = db.orderDao().getPendingOrderByUser(userId);
        long orderId;

        if (pendingOrder == null) {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            Order newOrder = new Order(userId, date, 0, "Pending");
            orderId = db.orderDao().insert(newOrder);
        } else {
            orderId = pendingOrder.id;
        }

        OrderDetail detail = new OrderDetail((int) orderId, productId, quantity, product.price);
        db.orderDetailDao().insert(detail);

        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        
        // Show Checkout Option
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("ORDER_ID", (int) orderId);
        startActivity(intent);
        finish();
    }
}
