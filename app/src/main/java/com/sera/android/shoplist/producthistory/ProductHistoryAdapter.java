package com.sera.android.shoplist.producthistory;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.ProductHistory;
import com.sera.android.shoplist.DAO.ProductsSet;

/**
 * Adapter personalizado para manejar un Checklist de productos 
 * @author Sera
 */
public class ProductHistoryAdapter extends BaseAdapter
{
	// Activity en la que est? corriendo la aplicaci?n. La necesitamos para sacar informaci?n del contexto y dem?s.
	private ProductHistoryActivity parentActivity;
	private ArrayList<ProductHistory> productHistory;
	
	// Variable interna, para manejar la ListView
	private LayoutInflater inflater;
	
	public ProductHistoryAdapter(ProductHistoryActivity slActivity,ArrayList<ProductHistory> history) {
		this.parentActivity=slActivity;
		this.productHistory=history;
		this.inflater = (LayoutInflater)slActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public int getCount() {
		return productHistory.size();
	}


	public long getItemId(int pos) {
		return pos;
	}

	/**
	 * Devuelve la vista de linea del CheckList
	 */
	public View getView(final int pos, View productView, ViewGroup parent) 
	{
		productView = inflater.inflate(R.layout.product_history_line, null);
		final ViewHolder holder = new ViewHolder();
		holder.txtDate = (TextView)productView.findViewById(R.id.txtProductHistoryDate);
		holder.txtPrice = (TextView)productView.findViewById(R.id.txtProductHistoryPrice);

		final ProductHistory prod = getItem(pos);
		
		// Columna 1: Fecha
        // Date -> Fecha en locale
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(parentActivity);
        holder.txtDate.setText(dateFormat.format(prod.getPriceDate()));

		// Columna 2: Precio
		//		Formateamos con dos decimales y pintamos
		String precio = NumberFormat.getCurrencyInstance().format(prod.getPrice());
		holder.txtPrice.setText(precio.toCharArray(),0,precio.length());

		// Done!
		productView.setTag(holder);
		return productView;
	}
	
	
	// 3) Leer un item
	public ProductHistory getItem(int pos) {
		ProductHistory ret =(ProductHistory)(productHistory.toArray())[pos]; 
		return ret;
	}

	
	public static class ViewHolder {
		public TextView txtDate;
		public TextView txtPrice;
	}

}
