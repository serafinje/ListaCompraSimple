package com.sera.android.shoplist.producthistory;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ProductHistory;
import com.sera.android.shoplist.DAO.ShopListDAO;

public class ProductHistoryActivity extends Activity
{
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.product_history);

	    ShopListDAO productsDAO =new ShopListDAO(this);
	    int itemId = (int)this.getIntent().getLongExtra("PRODUCTID", 0);
	    Product item = productsDAO.getProduct(itemId);
	    ArrayList<ProductHistory> hist = productsDAO.getPriceHistory(item);
	    
	    String title = getResources().getString(R.string.strProductHistory) + " - " + item.getName();
	    this.setTitle(title);
	    
	    ListView gridview = (ListView) findViewById(R.id.gvProductHistory);
	    gridview.setAdapter(new ProductHistoryAdapter(this, hist));
	    
	    Button btnVolver = (Button)findViewById(R.id.btnBack);
	    btnVolver.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
