package com.sera.android.shoplist.producthistory;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sera.android.shoplist.R;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ProductHistoryCursorAdapter extends SimpleCursorAdapter
{
	public ProductHistoryCursorAdapter(Context context,int layout,Cursor c, String[] from, int[] to)
	{
		super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
    	// Fecha
        int dateCol = c.getColumnIndex("pricedate");
        String date = c.getString(dateCol);

        String formattedDate="";
        try {
        	// YYYYMMDD -> Date
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        Date d = sdf.parse(date);
	        
	        // Date -> Fecha en locale
	        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
	        formattedDate = dateFormat.format(d);
	
	        TextView txtDate = (TextView)v.findViewById(R.id.txtProductHistoryDate);
	        if (txtDate != null) {
	            txtDate.setText(formattedDate);
	        }
        } catch (Exception e) {
        	//Log.e(this.getClass().getName(), "Error en generaci?n de l?neas de historia",e);
        }

        // Precio
        int priceCol = c.getColumnIndex("price");
        String price = c.getString(priceCol);
        TextView txtPrice =  (TextView)v.findViewById(R.id.txtProductHistoryPrice);
        if (txtPrice != null) {
            txtPrice.setText(price);
        } 
        
    }

}
