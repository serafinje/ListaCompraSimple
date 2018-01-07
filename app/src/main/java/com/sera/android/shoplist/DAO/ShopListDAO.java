package com.sera.android.shoplist.DAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ShopListDAO
{
	private ShopListOpenHelper helper;
    private final String TAG="ShopListDAO";
	
	public ShopListDAO(Context parent) {
		super();
		this.helper = new ShopListOpenHelper(parent);
	}
	
	/*************************************** GESTION DE LA LISTA DE PRODUCTOS ******************************
	 * 1) Lectura de toda la lista de productos con sus datos de tiendas
	 * 2) Grabacion de la lista de productos (no deberia ser necesaria)
	 * 3) Insertar producto
	 * 4) Actualizar producto
	 * 5) Borrar producto
	 * 6) Desmarcar todos (quitar check de pending)
	 */

	
	
	// 0) Listado de todas las tablas
	public void showTables()
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		String sql = "select * from lista_compra";
		Log.w(this.getClass().getName(),sql);
		Cursor c = productsDB.rawQuery(sql, null);
		c.moveToFirst();
		int col = c.getColumnCount();
		while (!c.isAfterLast()) {
			String ret="";
			for (int i=0; i<col; i++) ret+="["+c.getString(i)+"]";
			c.moveToNext();
			Log.d(this.getClass().getName(), ret);
		}
		c.close();
		productsDB.close();
		
		productsDB = this.helper.getWritableDatabase();
		sql = "select * from producto";
		Log.d(this.getClass().getName(),sql);
		c = productsDB.rawQuery(sql, null);
		c.moveToFirst();
		col = c.getColumnCount();
		while (!c.isAfterLast()) {
			String ret="";
			for (int i=0; i<col; i++) ret+="["+c.getString(i)+"]";
			c.moveToNext();
			Log.d(this.getClass().getName(), ret);
		}
		c.close();
		productsDB.close();
		
		productsDB = this.helper.getWritableDatabase();
		sql = "select * from tienda";
		Log.d(this.getClass().getName(),sql);
		c = productsDB.rawQuery(sql, null);
		c.moveToFirst();
		col = c.getColumnCount();
		while (!c.isAfterLast()) {
			String ret="";
			for (int i=0; i<col; i++) ret+="["+c.getString(i)+"]";
			c.moveToNext();
			Log.d(this.getClass().getName(), ret);
		}
		
		c.close();
		productsDB.close();
/*
		productsDB = this.helper.getWritableDatabase();
		sql = " select sl.inshoppinglist,sl.pending,p.name from lista_compra sl,producto p,tienda s where sl.id_producto=p.id and sl.id_tienda=s.id";
		//Log.w(this.getClass().getName(),sql);
		c = productsDB.rawQuery(sql, null);
		c.moveToFirst();
		col = c.getColumnCount();
		while (!c.isAfterLast()) {
			String ret="";
			for (int i=0; i<col; i++) ret+="["+c.getString(i)+"]";
			c.moveToNext();
			//Log.w(this.getClass().getName(), ret);
		}
		c.close();
		productsDB.close();
		*/
	}

	public void importTables(ArrayList<String> descarga)
    {
        SQLiteDatabase productsDB = this.helper.getWritableDatabase();
        String query;

        Iterator<String> i = descarga.iterator();
        while (i.hasNext()) {
            String linea = i.next();
            StringTokenizer t = new StringTokenizer(linea,"|");
            String tipo = t.nextToken();
            switch(tipo) {
                case "PRODUCTO":
                    query = "insert into PRODUCTO values (";
                    query += t.nextToken();  // id
                    query += ",'"+t.nextToken()+"'"; // nombre
                    //query += ",'"+t.nextToken()+"'"; // tipo (no lo usamos por ahora)
                    query += ")";
                    productsDB.execSQL(query);
                    break;
                case "TIENDA":
                    query = "insert into TIENDA values (";
                    query += t.nextToken();  // id
                    query += ",'"+t.nextToken()+"'"; // nombre
                    // Las coordenadas no las usamos por ahora.
                    productsDB.execSQL(query);
                    break;
                case "LISTA_COMPRA":
                    query = "insert into LISTA_COMPRA values (";
                    query += t.nextToken();  // id lista
                    query += ","+t.nextToken(); // id producto
                    query += ","+t.nextToken(); // id tienda
                    query += ",'"+t.nextToken()+"'"; // codigo barras
                    query += ",'"+t.nextToken()+"'"; // precio
                    query += ",'"+t.nextToken()+"'"; // cantidad
                    query += ","+t.nextToken(); // inShoppingList()
                    query += ","+t.nextToken(); // pending()
                    productsDB.execSQL(query);
                    break;
            }
        }
        productsDB.close();
    }

    public ArrayList<String> exportTables()
    {
        ArrayList<String> descarga = new ArrayList<>();
        SQLiteDatabase productsDB = this.helper.getWritableDatabase();

        String tabla = "PRODUCTO";
        String sql = "select * from producto";
        //Log.d(this.getClass().getName(),sql);
        Cursor c = productsDB.rawQuery(sql, null);
        c.moveToFirst();
        int col = c.getColumnCount();
        while (!c.isAfterLast()) {
            String linea=tabla;
            for (int i=0; i<col; i++) linea+="|"+c.getString(i);
            c.moveToNext();
            descarga.add(linea);
            Log.d(this.getClass().getName(), linea);
        }
        c.close();
        productsDB.close();

        productsDB = this.helper.getWritableDatabase();
        tabla="TIENDA";
        sql = "select * from tienda";
        //Log.d(this.getClass().getName(),sql);
        c = productsDB.rawQuery(sql, null);
        c.moveToFirst();
        col = c.getColumnCount();
        while (!c.isAfterLast()) {
            String linea=tabla;
            for (int i=0; i<col; i++) linea+="|"+c.getString(i);
            c.moveToNext();
            descarga.add(linea);
            Log.d(this.getClass().getName(), linea);
        }

        c.close();
        productsDB.close();

        productsDB = this.helper.getWritableDatabase();
        tabla = "LISTA_COMPRA";
        sql = "select * from lista_compra";
        //Log.w(this.getClass().getName(),sql);
        c = productsDB.rawQuery(sql, null);
        c.moveToFirst();
        col = c.getColumnCount();
        while (!c.isAfterLast()) {
            String linea=tabla;
            for (int i=0; i<col; i++) linea+="|"+c.getString(i);
            c.moveToNext();
            descarga.add(linea);
            Log.d(this.getClass().getName(), linea);
        }
        c.close();
        productsDB.close();

        return descarga;
    }


	// 1) Lectura de BD de todos los productos
	public ProductsSet readAllProductsList()
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		ProductsSet items = new ProductsSet();
		
		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
						+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
						+ ShopListOpenHelper.PRODUCT.TABLENAME  + " p,"
						+ ShopListOpenHelper.SHOP.TABLENAME     +" s "
					  + " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
					    + " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID;
		sql += " ORDER BY p.name";
		
		Cursor c = productsDB.rawQuery(sql,null);
		c.moveToFirst();

		Product	p;
		while (!c.isAfterLast()) {
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);
			
			p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
			items.add(p);
			
			c.moveToNext();
		}
		c.close();

		productsDB.close();
		return items;
	}

	public ProductsSet readShopList()
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		ProductsSet items = new ProductsSet();
		
		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
						+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
						+ ShopListOpenHelper.PRODUCT.TABLENAME + " p,"
						+ ShopListOpenHelper.SHOP.TABLENAME +" s "
					  + " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
					    + " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID
					    + " AND sl."+ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST+"=1 "
					    + " ORDER BY p."+ShopListOpenHelper.PRODUCT.NAME;
		
		Cursor c = productsDB.rawQuery(sql,null);
		c.moveToFirst();

		Product	p;
		while (!c.isAfterLast()) {
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);
			
			p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
			items.addItem(p);
			
			c.moveToNext();
		}
		c.close();

		productsDB.close();
		return items;
	}

	
	// Lectura de BD de todos los productos de un super
	public ProductsSet readByMarketProductsList(String shop)
	{
        //Log.d(TAG,"Leyendo productos de tienda ["+shop+"]");
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		ProductsSet items = new ProductsSet();
		
		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
						+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
						+ ShopListOpenHelper.PRODUCT.TABLENAME  + " p,"
						+ ShopListOpenHelper.SHOP.TABLENAME     +" s "
					  + " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
					    + " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID
					    + " AND s."+ShopListOpenHelper.SHOP.NAME+"='"+shop+"'";
		sql += " ORDER BY p.name";
		
		Cursor c = productsDB.rawQuery(sql,null);
		c.moveToFirst();

		Product	p;
		while (!c.isAfterLast()) {
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);
			
			p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
			items.addItem(p);
            //Log.d(TAG,p.toString());
			
			c.moveToNext();
		}
		c.close();

		productsDB.close();
        //Log.d(TAG,"-----------------------------------");
		return items;
	}

	public ProductsSet readByMarketShopList(String shop)
	{
		//Log.d(TAG,"Leyendo lista compra de tienda ["+shop+"]");
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		ProductsSet items = new ProductsSet();

		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
				+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
				+ ShopListOpenHelper.PRODUCT.TABLENAME  + " p,"
				+ ShopListOpenHelper.SHOP.TABLENAME     +" s "
				+ " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
				+ " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID
				+ " AND s."+ShopListOpenHelper.SHOP.NAME+"='"+shop+"'"
                + " AND sl."+ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST+"=1 ";
		sql += " ORDER BY p.name";

		Cursor c = productsDB.rawQuery(sql,null);
		c.moveToFirst();

		Product	p;
		while (!c.isAfterLast()) {
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);

			p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
			items.addItem(p);
			//Log.d(TAG,p.toString());

			c.moveToNext();
		}
		c.close();

		productsDB.close();
		//Log.d(TAG,"-----------------------------------");
		return items;
	}

	public Product getProduct(int id) {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
						+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
						+ ShopListOpenHelper.PRODUCT.TABLENAME  + " p,"
						+ ShopListOpenHelper.SHOP.TABLENAME     +" s "
					  + " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
					    + " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID
						+ " AND p."+ShopListOpenHelper.PRODUCT._ID+"="+id;

		Cursor c = productsDB.rawQuery(sql,null);
		c.moveToFirst();

		Product	p=null;
		if (c.getCount()==1 && !c.isAfterLast())
		{
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);
			
			p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
		}
		c.close();

		productsDB.close();
		
		return p;
	}
	
	
	public ProductsSet getProductByBarCode(String barCode)
	{
		Log.d(TAG,"Consulta por Barcode");
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
						+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
						+ ShopListOpenHelper.PRODUCT.TABLENAME  + " p,"
						+ ShopListOpenHelper.SHOP.TABLENAME     +" s "
					  + " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
					    + " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID
						+ " AND sl."+ShopListOpenHelper.SHOPLIST.BARCODE+"='"+barCode+"';";

		ProductsSet	ps=new ProductsSet();
		Cursor c = productsDB.rawQuery(sql,null);
		if (c.getCount()>0) {
            Log.d(TAG,"Encontrados "+c.getCount()+" productos.");
		c.moveToFirst();

		//if (c.getCount()==1 && !c.isAfterLast())
        while (!c.isAfterLast())
		{
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);
			
			Product p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
			ps.addItem(p);

            Log.d(TAG,"Añadido "+p);

            c.moveToNext();
		}
		c.close();
		}

		Log.d(TAG,"Productos: "+ps.size()+" - "+ps.getItemNames());
		productsDB.close();
		
		return ps;
	}

	
	public ProductsSet getProductsWithoutBarCode() 
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		ProductsSet items = new ProductsSet();
		
		String sql = "SELECT sl.*,p.name as prodname,p.*,s.*,s.name as shopname FROM "
				+ ShopListOpenHelper.SHOPLIST.TABLENAME + " sl,"
				+ ShopListOpenHelper.PRODUCT.TABLENAME  + " p,"
				+ ShopListOpenHelper.SHOP.TABLENAME     +" s "
			  + " WHERE sl."+ShopListOpenHelper.SHOPLIST._ID_PROD+"=p."+ShopListOpenHelper.PRODUCT._ID
			    + " AND sl."+ShopListOpenHelper.SHOPLIST._ID_SHOP+"=s."+ShopListOpenHelper.SHOP._ID
				+ " AND sl."+ShopListOpenHelper.SHOPLIST.BARCODE+"='';";

		//Log.d(this.getClass().getName(),sql);
		
		Cursor c = productsDB.rawQuery(sql,null);
		c.moveToFirst();

		Product	p;
		while (!c.isAfterLast()) {
			int id_prod = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_PROD));
			int id_mall = c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST._ID_SHOP));
			String name = c.getString(c.getColumnIndex("prodname"));
			String mall = c.getString(c.getColumnIndex("shopname"));
			String type = c.getString(c.getColumnIndex(ShopListOpenHelper.PRODUCT.TYPE));
			String barcode = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.BARCODE));
			String price = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PRICE));
			String quantity = c.getString(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.QUANTITY));
			boolean inShoppingList = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST))==1);
			boolean pending = (c.getInt(c.getColumnIndex(ShopListOpenHelper.SHOPLIST.PENDING))==1);
			
			p = new Product(name);
			p.setId(id_prod);
			p.setShopId(id_mall);
			p.setShop(mall);
			p.setType(type);
			p.setBarcode(barcode);
			p.setPrice(price);
			p.setQuantity(quantity);
			p.setInShoppingList(inShoppingList);
			p.setPending(pending);
			items.addItem(p);
			
			c.moveToNext();
		}
		c.close();

		productsDB.close();
		return items;
	}


	// 2) Guardar en BD todos los productos (de momento, solo tabla shoplist). Ahora mismo no se usa.
	public void saveShopList(ProductsSet p)
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		Iterator<Product> it = p.iterator();
		while (it.hasNext()) {
			Product item = it.next();
			//Log.d(this.getClass().getName(),"Actualizando en BD: "+item.toString());
			ContentValues values = new ContentValues();
				values.put(ShopListOpenHelper.SHOPLIST.PRICE, item.getPrice().toString());
				values.put(ShopListOpenHelper.SHOPLIST.BARCODE, item.getBarcode());
				values.put(ShopListOpenHelper.SHOPLIST.QUANTITY, item.getQuantity().toString());
				values.put(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST, item.inShoppingList());
				values.put(ShopListOpenHelper.SHOPLIST.PENDING, item.isPending());
			String whereClause= ShopListOpenHelper.SHOPLIST._ID_PROD + "="+item.getId();
			productsDB.update(ShopListOpenHelper.SHOPLIST.TABLENAME, values, whereClause, null);
		}
		productsDB.close();
	}
	
	// 3) Insertar producto. Inserta en PRODUCT, SHOPLIST y tambien en SHOP si hace falta
	public void addProduct(Product item)
	{
		//Log.d(this.getClass().getName(), "Insertando producto "+item.toString());
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		// 1) Insertamos producto
		// fixme Comprobar si ya existe, actualizar en tal caso
		ContentValues pValues = new ContentValues();
		pValues.put(ShopListOpenHelper.PRODUCT.NAME,item.getName());
		pValues.put(ShopListOpenHelper.PRODUCT.TYPE,item.getType());
		long pId = productsDB.insert(ShopListOpenHelper.PRODUCT.TABLENAME, null, pValues);
		productsDB.close();
		
		// 2) Miramos si existe ya la tienda. Si existe ponemos el id en el producto, si no existe lo insertamos
		long sId=1;
		if (item.getShop()==null) item.setShop("");
		sId = this.getShopId(item.getShop());
		productsDB = this.helper.getWritableDatabase();
		if (sId==-1) {
			ContentValues sValues = new ContentValues();
			sValues.put(ShopListOpenHelper.SHOP.NAME,item.getShop());
			sId = productsDB.insert(ShopListOpenHelper.SHOP.TABLENAME, null, sValues);
		}
		item.setShopId(sId);
		productsDB.close();
		
		// 3) Insertamos en listacompra 
		productsDB = this.helper.getWritableDatabase();
		if (pId!=-1) {
			item.setId(pId);

			ContentValues slValues = new ContentValues();
			slValues.put(ShopListOpenHelper.SHOPLIST._ID_PROD,item.getId());
			slValues.put(ShopListOpenHelper.SHOPLIST._ID_SHOP, sId);
			slValues.put(ShopListOpenHelper.SHOPLIST.PRICE, item.getPrice().toString());
			slValues.put(ShopListOpenHelper.SHOPLIST.BARCODE, item.getBarcode());
			slValues.put(ShopListOpenHelper.SHOPLIST.QUANTITY, item.getQuantity().toString());
			slValues.put(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST, item.inShoppingList());
			slValues.put(ShopListOpenHelper.SHOPLIST.PENDING, item.isPending());
		
			productsDB.insert(ShopListOpenHelper.SHOPLIST.TABLENAME, null, slValues);
		}
		productsDB.close();
		
		//Log.w(this.getClass().getName(),"Insertado producto "+pId+" en tienda "+sId);
	}
	
	// 4) Actualizar producto 
	public void updateProduct(Product item)
	{
		//Log.d(this.getClass().getName(), "Actualizando producto "+item.toString());
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		String whereClause = ShopListOpenHelper.PRODUCT._ID + "=?";
		String[] whereArgs={""+item.getId()};

		// 1) Actualizar tabla producto 
		ContentValues values = new ContentValues();
			values.put(ShopListOpenHelper.PRODUCT.NAME,item.getName());
			values.put(ShopListOpenHelper.PRODUCT.TYPE, item.getType());
			
		int pcount = productsDB.update(ShopListOpenHelper.PRODUCT.TABLENAME, values, whereClause, whereArgs);
		//Log.d(this.getClass().getName(), "Productos actualizados en tabla producto: "+pcount);
		productsDB.close();

		
		// 2) Miramos si existe ya la tienda. Si existe ponemos el id en el producto, si no existe lo insertamos
		// TODO: Al elegir tienda ya deber?a asignarse un ID (si se elige una existente), y si no, el ID estar?a vac?o y aqu? crear?amos una entrada
		productsDB = this.helper.getWritableDatabase();
		long sId=0;
		if (item.getShop()==null) item.setShop("");
		sId = this.getShopId(item.getShop());
		if (sId==-1) {
			ContentValues sValues = new ContentValues();
			sValues.put(ShopListOpenHelper.SHOP.NAME,item.getShop());
			sId = productsDB.insert(ShopListOpenHelper.SHOP.TABLENAME, null, sValues);
			//Log.d(this.getClass().getName(), "Creada tienda '"+item.getShop()+"' ("+sId+")");
		} 
		item.setShopId(sId);
		productsDB.close();

		// 3) Miramos si la tienda ha quedado huerfana. En tal caso, la borramos tambien
		// fixme Borrar la tienda antigua (si ya no queda ningun producto en esa tienda)
		
		// 4) Actualizar tabla listacompra
		values = new ContentValues();
			values.put(ShopListOpenHelper.SHOPLIST.BARCODE,item.getBarcode());
			values.put(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST, item.inShoppingList());
			values.put(ShopListOpenHelper.SHOPLIST.PENDING, item.isPending());
			values.put(ShopListOpenHelper.SHOPLIST.PRICE, item.getPrice().toString());
			values.put(ShopListOpenHelper.SHOPLIST.QUANTITY, item.getQuantity().toString());
			values.put(ShopListOpenHelper.SHOPLIST._ID_SHOP, item.getShopId());
			
		whereClause = ShopListOpenHelper.PRODUCT._ID + "="+item.getId();

		productsDB = this.helper.getWritableDatabase();	
		pcount = productsDB.update(ShopListOpenHelper.SHOPLIST.TABLENAME, values, whereClause, null);
		//Log.d(this.getClass().getName(), "Productos actualizados en tabla listacompra: "+pcount);
		
		productsDB.close();
	}
	
	public void updateProductChecks(Product item)
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		ContentValues values = new ContentValues();
			values.put(ShopListOpenHelper.SHOPLIST.BARCODE,item.getBarcode());
			values.put(ShopListOpenHelper.SHOPLIST.INSHOPPINGLIST, item.inShoppingList());
			values.put(ShopListOpenHelper.SHOPLIST.PENDING, item.isPending());
			values.put(ShopListOpenHelper.SHOPLIST.PRICE, item.getPrice().toString());
			values.put(ShopListOpenHelper.SHOPLIST.QUANTITY, item.getQuantity().toString());
			
		String whereClause = ShopListOpenHelper.PRODUCT._ID + "="+item.getId();
		int pcount = productsDB.update(ShopListOpenHelper.SHOPLIST.TABLENAME, values, whereClause, null);
		productsDB.close();
	}
	
	

	// 5) Borrar producto 
	public void removeProduct(Product item)
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();

		// 1) Borramos de listacompra
		String whereClause = ShopListOpenHelper.SHOPLIST._ID_PROD + "=?";
		String[] whereArgs={""+item.getId()};
		productsDB.delete(ShopListOpenHelper.SHOPLIST.TABLENAME, whereClause, whereArgs);
		productsDB.close();
		
		// 2) Borramos de producto
		productsDB = this.helper.getWritableDatabase();
		whereClause = ShopListOpenHelper.PRODUCT._ID + "=?";
		productsDB.delete(ShopListOpenHelper.PRODUCT.TABLENAME, whereClause, whereArgs);
		productsDB.close();
		
		// 3) Miramos si la tienda ha quedado huerfana. En tal caso, la borramos tambien
		this.deleteEmptyShops();
	}
	
	public void uncheckAll()
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ShopListOpenHelper.SHOPLIST.PENDING,true);
		productsDB.update(ShopListOpenHelper.SHOPLIST.TABLENAME, values, null, null);
	}
	
	
	/********************************************************************************/
	
	
	/**
	 * Supermercados
	 * 1. Añadir
	 * 2. Leer lista
	 */
	public void addShop(String name)
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("name", name);
		productsDB.insert(ShopListOpenHelper.SHOP.TABLENAME, null, values);
		productsDB.close();
	}

	public String getShop(long id) {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		String sql = "SELECT name from " + ShopListOpenHelper.SHOP.TABLENAME
		+ " WHERE "+ShopListOpenHelper.SHOP._ID+"=?";
		Cursor c = productsDB.rawQuery(sql, new String[]{""+id});
		c.moveToFirst();
		String l = c.getString(0);
		c.close();
		productsDB.close();
		return l;		
	}
	
	public long getShopId(String name) {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		Cursor c = productsDB.query(ShopListOpenHelper.SHOP.TABLENAME,new String[] {ShopListOpenHelper.SHOP._ID},ShopListOpenHelper.SHOP.NAME+"='"+name+"'",null,null,null,null,null  );
		long l = -1;
		if (c.getCount()!=0) {
			c.moveToFirst();
			l = c.getLong(0);
		}
		c.close();
		productsDB.close();
		return l;		
	}
	
	public ArrayList<String> getShops() {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		String sql = "select name from tienda";
		Cursor c = productsDB.rawQuery(sql, null);
		c.moveToFirst();
		ArrayList<String> l = new ArrayList<String>();
		while (!c.isAfterLast()) {
			l.add(c.getString(0));
			c.moveToNext();
		}
		c.close();
		productsDB.close();
		return l;
	}

	public void deleteEmptyShops() {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		String query = "delete from tienda where _id!=1 and _id not in (select id_tienda from lista_compra group by id_tienda)";
		productsDB.execSQL(query);
		productsDB.close();
	}
	
	
	
	/*******************************************************************************
	 * Histórico de precios
	 */
	public void addHistory(Product item) {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
		String date = sdf.format(Calendar.getInstance().getTime());
		ContentValues slValues = new ContentValues();
			slValues.put("id_producto",item.getId());
			slValues.put("id_tienda", item.getShopId());
			slValues.put("pricedate", date);
			slValues.put("price", item.getPrice().toString());
		
		productsDB.insert("hist_precios", null, slValues);
		productsDB.close();
	}
	
	
	public ArrayList<ProductHistory> getPriceHistory(Product item) {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		String sql = "select pricedate,price from hist_precios where id_producto="+item.getId()+" and id_tienda="+item.getShopId()+" order by _id";
		Cursor c = productsDB.rawQuery(sql, null);
		//Log.d(this.getClass().getName(), sql+" -> "+c.getCount());
		c.moveToFirst();
		ArrayList<ProductHistory> l = new ArrayList<ProductHistory>();
		while (!c.isAfterLast()) {
			ProductHistory p = new ProductHistory(item);
			//Log.d(this.getClass().getName(), c.getString(0));
			p.setPriceDate(c.getString(0));
			p.setPrice(c.getString(1));
			l.add(p);
			c.moveToNext();
		}
		c.close();
		productsDB.close();
		
		return l;
	}

	public Cursor getPriceHistoryCursor(Product item) {
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		String sql = "select _id,pricedate,price from hist_precios where id_producto="+item.getId()+" and id_tienda="+item.getShopId();
		//String[] where = new String[] {"id_producto="+item.getId(),"id_tienda="+item.getShopId()};
		Cursor c = productsDB.rawQuery(sql, null);
		//c.moveToFirst();
		//Log.d(this.getClass().getName(), "Consulta hist?rico: "+sql);
		//Log.d(this.getClass().getName(), "Entradas del hist?rico: "+c.getCount());
		return c;
		/*
		ArrayList<ProductHistory> l = new ArrayList<ProductHistory>();
		while (!c.isAfterLast()) {
			ProductHistory p = new ProductHistory(item);
			p.setPriceDate(c.getString(0));
			p.setPrice(c.getString(1));
			l.add(p);
			c.moveToNext();
		}
		c.close();
		productsDB.close();
		
		return l;*/
	}

	
	/*****************************************************************************************/
	
	// Datos de ejemplo
	public void rellenaDatosEjemplo() 
	{
		SQLiteDatabase productsDB = this.helper.getWritableDatabase();
		productsDB.delete(ShopListOpenHelper.SHOP.TABLENAME, null, null);
		productsDB.delete(ShopListOpenHelper.PRODUCT.TABLENAME, null, null);
		productsDB.delete(ShopListOpenHelper.SHOPLIST.TABLENAME, null, null);
		productsDB.close();
		
		//Log.w(this.getClass().getName(),"Rellenando con datos de ejemplo");

		addShop("");
		addShop("Mercadona");
		addShop("Carrefour");
		
		Product a;
		// Productos en lista actual
		//      Product(name,type,price,quantity,barcode,shop,pending,inSL)
        a = new Product("Yogur Bifidus Fibras","","1"   ,"2",  "","Mercadona",true,true);	addProduct(a);	
        a = new Product("Lechuga",             "","1.55","1",  "","Mercadona",true,true);	addProduct(a);	
        a = new Product("Yogur l?quido",       "","1.19","3",  "","Mercadona",true,true);  	addProduct(a);	
        a = new Product("Tomate canario",      "","0.81","1.5","","Mercadona",false,true); 	addProduct(a);	
        a = new Product("Copos avena",         "","1.29","1",  "","Mercadona",true,true);	addProduct(a);	
        a = new Product("Copos arroz/trigo",   "","1.59","1",  "","Mercadona",true,true);   addProduct(a);

        // Resto de productos
        a = new Product("Agua 6l",                "","0.63","1",  "","Mercadona",false,false); 	addProduct(a);
        a = new Product("Agua 5l",                "","0.59","1",  "","Carrefour",false,false); 	addProduct(a);
        a = new Product("Cesta",               "","0.60","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Copos ma?z",          "","1.05","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Espinacas crema",     "","1.60","2",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Horchata",            "","0.89","1",  "","Carrefour",false,false);		addProduct(a);
        a = new Product("Lata sardinas con tomate","","0.95","1","","Mercadona",false,false);	addProduct(a);
        a = new Product("Lata at?n claro aceite oliva","","3.48","1","","Mercadona",false,false);	addProduct(a);
        a = new Product("Lechuga",             "","1.10","1",  "","Mercadona",false,false);		addProduct(a);	
        a = new Product("Naranja Palot",       "","1.55","1.275","","Mercadona",false,false);	addProduct(a);
        a = new Product("N?ctar pi?a",         "","0.65","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Pan tostado",         "","1.39","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Paraguayo",           "","2.03","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Pasta gamba",         "","0.75","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Pera",                "","0.95","1",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Pera ercolina",       "","1.49","1.005","","Mercadona",false,false);	addProduct(a);
        a = new Product("Pisto verduras",      "","1.50","2",  "","Mercadona",false,false);		addProduct(a);
        a = new Product("Tomate ensalada",     "","1.19","0.781","","Mercadona",false,false);	addProduct(a);
        a = new Product("Tortitas ma?z",       "","1.20","1",    "","Mercadona",false,false);	addProduct(a);
        a = new Product("Uva blanca",          "","2.14","1",    "","Mercadona",false,false);	addProduct(a);
        a = new Product("Uva negra",           "","1.79","1.078","","Mercadona",false,false);	addProduct(a);
        a = new Product("Yogur Activia",       "","2"   ,"2"    ,"","Carrefour",false,false);	addProduct(a);
        a = new Product("Yogur l?quido",       "","1.19","2",  "","Carrefour",false,false);  	addProduct(a);
        
        
		productsDB = this.helper.getWritableDatabase();
		ContentValues slValues = new ContentValues();
		slValues.put("id_producto",0);
		slValues.put("id_tienda", 2);
			slValues.put("pricedate", "20110101");	slValues.put("price", "1"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110201");	slValues.put("price", "2"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110301");	slValues.put("price", "4"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110401");	slValues.put("price", "8"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110501");	slValues.put("price", "10"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110601");	slValues.put("price", "21"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110701");	slValues.put("price", "31"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110801");	slValues.put("price", "41"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110901");	slValues.put("price", "61"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111001");	slValues.put("price", "71"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111101");	slValues.put("price", "81"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111201");	slValues.put("price", "91"); 	productsDB.insert("hist_precios", null, slValues);

		slValues.put("id_producto",1);
			slValues.put("pricedate", "20110101");	slValues.put("price", "1"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110201");	slValues.put("price", "2"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110301");	slValues.put("price", "4"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110401");	slValues.put("price", "8"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110501");	slValues.put("price", "10"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110601");	slValues.put("price", "21"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110701");	slValues.put("price", "31"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110801");	slValues.put("price", "41"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110901");	slValues.put("price", "61"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111001");	slValues.put("price", "71"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111101");	slValues.put("price", "81"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111201");	slValues.put("price", "91"); 	productsDB.insert("hist_precios", null, slValues);

		slValues.put("id_producto",2);
			slValues.put("pricedate", "20110101");	slValues.put("price", "1"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110201");	slValues.put("price", "2"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110301");	slValues.put("price", "4"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110401");	slValues.put("price", "8"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110501");	slValues.put("price", "10"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110601");	slValues.put("price", "21"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110701");	slValues.put("price", "31"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110801");	slValues.put("price", "41"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110901");	slValues.put("price", "61"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111001");	slValues.put("price", "71"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111101");	slValues.put("price", "81"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111201");	slValues.put("price", "91"); 	productsDB.insert("hist_precios", null, slValues);

		slValues.put("id_producto",3);
			slValues.put("pricedate", "20110101");	slValues.put("price", "1"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110201");	slValues.put("price", "2"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110301");	slValues.put("price", "4"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110401");	slValues.put("price", "8"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110501");	slValues.put("price", "10"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110601");	slValues.put("price", "21"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110701");	slValues.put("price", "31"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110801");	slValues.put("price", "41"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110901");	slValues.put("price", "61"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111001");	slValues.put("price", "71"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111101");	slValues.put("price", "81"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111201");	slValues.put("price", "91"); 	productsDB.insert("hist_precios", null, slValues);

		slValues.put("id_producto",4);
			slValues.put("pricedate", "20110101");	slValues.put("price", "1"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110201");	slValues.put("price", "2"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110301");	slValues.put("price", "4"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110401");	slValues.put("price", "8"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110501");	slValues.put("price", "10"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110601");	slValues.put("price", "21"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110701");	slValues.put("price", "31"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110801");	slValues.put("price", "41"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110901");	slValues.put("price", "61"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111001");	slValues.put("price", "71"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111101");	slValues.put("price", "81"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111201");	slValues.put("price", "91"); 	productsDB.insert("hist_precios", null, slValues);

		slValues.put("id_producto",5);
			slValues.put("pricedate", "20110101");	slValues.put("price", "1"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110201");	slValues.put("price", "2"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110301");	slValues.put("price", "4"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110401");	slValues.put("price", "8"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110501");	slValues.put("price", "10"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110601");	slValues.put("price", "21"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110701");	slValues.put("price", "31"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110801");	slValues.put("price", "41"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20110901");	slValues.put("price", "61"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111001");	slValues.put("price", "71"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111101");	slValues.put("price", "81"); 	productsDB.insert("hist_precios", null, slValues);
			slValues.put("pricedate", "20111201");	slValues.put("price", "91"); 	productsDB.insert("hist_precios", null, slValues);


		productsDB.close();

	}
}
