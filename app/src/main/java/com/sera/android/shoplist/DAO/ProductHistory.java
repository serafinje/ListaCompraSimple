package com.sera.android.shoplist.DAO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProductHistory extends Product 
{
	private static final long serialVersionUID = 6588070482483516081L;
	
	private Date priceDate;
	
	public ProductHistory(Product p) {
		super(p);
	}

	
	public Date getPriceDate() {
		return priceDate;
	}

	public void setPriceDate(Date priceDate) {
		this.priceDate = priceDate;
	}
	
	public void setPriceDate(String priceDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date d = null;
		try {
			d = sdf.parse(priceDate);
		} catch(Exception pe)  {
			d=null;
		}
		
		if (d!=null) setPriceDate(d);
	}
}
