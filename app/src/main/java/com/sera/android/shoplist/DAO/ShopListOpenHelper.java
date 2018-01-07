package com.sera.android.shoplist.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ShopListOpenHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "listacompra.db";
    private Context context;
    
	ShopListOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context=context;
	}
	
	
	public SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase db = super.getWritableDatabase();
		if (!db.isOpen()) {
			db = context.openOrCreateDatabase(
	                DATABASE_NAME,
	                SQLiteDatabase.OPEN_READWRITE, null);
		}
		return db;
	}
	
	/**
	 * Creamos las tablas
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE "+ShopListOpenHelper.PRODUCT.TABLENAME+" ("
				 + "_id INTEGER PRIMARY KEY,"
				 + "name TEXT,"
				 + "type TEXT"
				 + ");");
		
		db.execSQL("CREATE TABLE "+ShopListOpenHelper.SHOP.TABLENAME+" ("
				 + "_id INTEGER PRIMARY KEY,"
				 + "name TEXT,"
				 + "longitude INTEGER,"
				 + "latitude INTEGER"
				 + ");");
		
		// Insertamos tienda vac?a
		ContentValues sValues = new ContentValues();
		sValues.put("name","");
		db.insert(ShopListOpenHelper.SHOP.TABLENAME, null, sValues);

		
		db.execSQL("CREATE TABLE "+ShopListOpenHelper.SHOPLIST.TABLENAME+" ("
				 + "_id INTEGER PRIMARY KEY,"
				 + "id_producto INTEGER,"
				 + "id_tienda INTEGER,"
				 + "barcode TEXT,"
				 + "price TEXT,"
				 + "quantity TEXT,"
				 + "inshoppinglist INTEGER,"
				 + "pending INTEGER"
				 + ");");		

		db.execSQL("CREATE TABLE hist_precios ("
				+ "_id INTEGER PRIMARY KEY,"
				+ "id_producto INTEGER,"
				+ "id_tienda INTEGER,"
				+ "pricedate TEXT,"
				+ "price TEXT"
				+ ");");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
	}

	
	class PRODUCT {
		final static String TABLENAME="producto";
		final static String _ID="_id";
		final static String NAME="name";
		final static String TYPE="type";
	}
	class SHOP {
		final static String TABLENAME="tienda";
		final static String _ID="_id";
		final static String NAME="name";
		public final static String LONG="longitude";
		public final static String LAT="latitude";
	}
	class SHOPLIST {
		final static String TABLENAME="lista_compra";
		final static String _ID="_id";
		final static String _ID_PROD="id_producto";
		final static String _ID_SHOP="id_tienda";
		final static String BARCODE="barcode";
		final static String PRICE="price";
		final static String QUANTITY="quantity";
		final static String INSHOPPINGLIST="inshoppinglist";
		final static String PENDING="pending";
	}
}
