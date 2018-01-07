package com.sera.android.shoplist.DAO;

import android.support.annotation.NonNull;

import java.io.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;


public class Product implements Comparable<Product>, Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int COMPARE_NAME=1;
	public static final int COMPARE_PRICE=2;
	public static final int COMPARE_PENDING=3;
    public static final int COMPARE_SHOP=4;
    public static final int COMPARE_BARCODE=5;
	public static ArrayList<Integer> compareFields=new ArrayList<>();
	static {
		compareFields.add(-Product.COMPARE_PENDING);
		compareFields.add(Product.COMPARE_NAME);
        compareFields.add(Product.COMPARE_SHOP);
        compareFields.add(Product.COMPARE_BARCODE);
		//compareFields.add(-Product.COMPARE_PRICE);
	}
	
	private long		id;
	private String 		name;
	private String		type;
	private BigDecimal 	quantity=new BigDecimal(1);	// Cantidad en kg o en unidades (defecto 1)
	private BigDecimal 	price   =new BigDecimal(0);	// Precio en eur/kg o eur/unidad
	private String		barcode="";
	private long		idShop;
	private String		shop;
	private boolean		inShoppingList=false;
	private boolean 	pending=true;
	
	
	/** Constructores **/
	public Product(String name)
	{
		this.setName(name.trim());
	}
	public Product(String name,String type,String price,String quantity,String barcode,String shop,boolean p,boolean sl) {
		this.setName(name.trim());
		this.setType(type);
		this.setQuantity(quantity);
		this.setPrice(price);
		this.setBarcode(barcode);
		this.setShop(shop.trim());
		this.setInShoppingList(sl);
		this.setPending(p);
	}
	public Product(Product p) {
		if (p!=null) {
		this.id			= p.getId();  
		this.name		= p.getName().trim();
		this.price 		= p.getPrice();
		this.quantity 	= p.getQuantity();
		this.setPending(p.isPending());
		this.inShoppingList = p.inShoppingList();
		this.setBarcode(p.getBarcode());
		this.shop = p.getShop().trim();
		this.setShopId(p.getShopId());
		}
	}

	/** Getters **/
	public long getId() {
		return id;
	}
	public String getName() {
		if (name==null) name="";
		return name;
	}
	// La propiedad "checked" encapsulada indica si el checkbox deberia estar activado o no: Es lo opuesto a la propiedad interna "pending".
	public boolean isChecked() {
		return !isPending();
	}
	public BigDecimal getPrice() {
		return price;
	}
	public String getPriceCurrency() {
		if (price!=null)
			return NumberFormat.getCurrencyInstance().format(price);
		else
			return NumberFormat.getCurrencyInstance().format(0);
	}
	public BigDecimal getQuantity() {
		return quantity;
	}
	public BigDecimal getTotalPrice() {
		return price.multiply(quantity);
	}
	public String getTotalPriceCurrency() {
		BigDecimal total = price.multiply(quantity);
		return NumberFormat.getCurrencyInstance().format(total);
	}
	public long getShopId() { return idShop; }
	public String getShop() {
		if (shop==null) shop="";
        return shop;
	}
	public boolean inShoppingList() {
		return this.inShoppingList;
	}
	public String getType() {
		return type;
	}
	public String getBarcode() {
		return barcode;
	}
	public boolean isPending() {
		return pending;
	}
	
	
	/** Setters **/
	public void setId(long id)				{ this.id = id; 	}
	public void setName(String name) {
        if (name==null) name="";
        this.name = name.trim();
	}
	public void setPrice(String p)			{ this.price = new BigDecimal(p); 	}
	public void setPrice(BigDecimal p)		{ this.price = p; 	}
	public void setQuantity(String q)		{ this.quantity = new BigDecimal(q); }
	public void setQuantity(BigDecimal q)	{ this.quantity = q; 	}
	public void setShopId(long shopId)		{ this.idShop = shopId; }
	public void setShop(String shop)		{
        if (shop==null) shop="";
        this.shop = shop.trim();
	}
	public void setInShoppingList(boolean b){ this.inShoppingList = b; }
	public void setType(String type) 		{ this.type = type;	}
	public void setBarcode(String barcode) 	{ this.barcode = barcode;	}
	public void setPending(boolean pending) { this.pending = pending;	}
	public void setChecked(boolean checked) { setPending(!checked); }
	
	
	
	@Override
	public int compareTo(@NonNull Product arg0) {
		int ret=0;
		int i=0;
		for (i=0; i<compareFields.size() && ret==0; i++)
		{
			if (compareFields.get(i)==Product.COMPARE_NAME) ret=getName().compareToIgnoreCase(arg0.getName());
			if (compareFields.get(i)==Product.COMPARE_PRICE) ret=price.compareTo(arg0.price);
			if (compareFields.get(i)==Product.COMPARE_PENDING) ret=Boolean.valueOf(isPending()).compareTo(arg0.isPending());
            if (compareFields.get(i)==Product.COMPARE_SHOP) ret=getShop().compareToIgnoreCase(arg0.getShop());
            if (compareFields.get(i)==Product.COMPARE_BARCODE) ret=getBarcode().compareToIgnoreCase(arg0.getBarcode());
			if (compareFields.get(i)==-Product.COMPARE_NAME) ret=-getName().compareToIgnoreCase(arg0.getName());
			if (compareFields.get(i)==-Product.COMPARE_PRICE) ret=-price.compareTo(arg0.price);
			if (compareFields.get(i)==-Product.COMPARE_PENDING) ret=-Boolean.valueOf(isPending()).compareTo(arg0.isPending());
            if (compareFields.get(i)==-Product.COMPARE_SHOP) ret=-getShop().compareToIgnoreCase(arg0.getShop());
            if (compareFields.get(i)==-Product.COMPARE_BARCODE) ret=-getBarcode().compareToIgnoreCase(arg0.getBarcode());
		}
		return ret;
	}
	
	public boolean equals(Product prod) {
		boolean ret = (this.getId()==prod.getId());
        boolean ret2 = this.getName().equalsIgnoreCase(prod.getName());
        if (!this.getShop().equals("") && !prod.getShop().equals("")) {
            ret2 = ret2 && this.getShop().equalsIgnoreCase(prod.getShop());
        }
        if (!this.getBarcode().equals("") && !prod.getBarcode().equals("")) {
            ret2 = ret2 && this.getBarcode().equals(prod.getBarcode());
        }
		return ret || ret2;

	}
	
	public void sortBy(int orden)
	{
		if (compareFields.indexOf(orden)==0 || compareFields.indexOf(-orden)==0) {
			compareFields.set(0,-orden);
		} else {
			int pos = compareFields.indexOf(orden);
			if (pos==-1) pos = compareFields.indexOf(-orden);
			if (pos!=-1) {
				compareFields.remove(pos);
				compareFields.set(0,orden);
			}
		}
	}
	
	public String toString() {
		return getName() +"["+quantity+"x"+price+"][P="+isPending()+"][SL="+inShoppingList+"]";
	}
}
