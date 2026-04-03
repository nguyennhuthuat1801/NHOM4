package com.example.fruitmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fruitmanager.adapter.ProductAdapter;
import com.example.fruitmanager.database.AppDatabase;
import com.example.fruitmanager.model.Category;
import com.example.fruitmanager.model.Product;
import com.example.fruitmanager.util.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    private RecyclerView rvProducts;
    private ChipGroup chipGroupCategories;
    private ProductAdapter adapter;
    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        rvProducts = findViewById(R.id.rvProducts);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        seedDataIfEmpty();
        loadCategories();
        loadProducts(-1); // -1 for all
    }

    private void seedDataIfEmpty() {
        if (db.categoryDao().getAllCategories().isEmpty()) {
            db.categoryDao().insert(new Category("Fruits"));
            db.categoryDao().insert(new Category("Vegetables"));
            db.categoryDao().insert(new Category("Drinks"));

            List<Category> cats = db.categoryDao().getAllCategories();
            db.productDao().insert(new Product("Apple", 2.5, "Fresh Red Apple", cats.get(0).id, ""));
            db.productDao().insert(new Product("Banana", 1.2, "Sweet Banana", cats.get(0).id, ""));
            db.productDao().insert(new Product("Carrot", 0.8, "Organic Carrot", cats.get(1).id, ""));
            db.productDao().insert(new Product("Orange Juice", 3.0, "Natural Juice", cats.get(2).id, ""));
        }
    }

    private void loadCategories() {
        List<Category> categories = db.categoryDao().getAllCategories();
        
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> loadProducts(-1));
        chipGroupCategories.addView(allChip);

        for (Category cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat.name);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> loadProducts(cat.id));
            chipGroupCategories.addView(chip);
        }
    }

    private void loadProducts(int categoryId) {
        List<Product> products;
        if (categoryId == -1) {
            products = db.productDao().getAllProducts();
        } else {
            products = db.productDao().getProductsByCategory(categoryId);
        }

        if (adapter == null) {
            adapter = new ProductAdapter(products, this);
            rvProducts.setAdapter(adapter);
        } else {
            adapter.updateList(products);
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", product.id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.action_login);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        if (session.isLoggedIn()) {
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        } else {
            loginItem.setVisible(true);
            logoutItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_login) {
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            session.logout();
            invalidateOptionsMenu();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }
}
