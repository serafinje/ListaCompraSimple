package com.sera.android.shoplist.productselection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;
import com.google.android.gms.vision.barcode.Barcode;
import com.sera.android.shoplist.ListaCompra;
import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ProductsSet;
import com.sera.android.shoplist.DAO.ShopListWebDAO;

/**
 * Activity para la pantalla de selección de productos para la lista.
 * fixme Repositorio de producto/supermercado/precio
 */
public class SelectProductActivity
	extends ExpandableListActivity
	implements View.OnClickListener,AdapterView.OnItemClickListener
{
    final String TAG = "SelectProductActivity";

    ListaCompra listaProductos;     // Todos los productos de la BD
    List<String> listaTiendas;

    protected SelectListAdapter adapterGrupos;
    ExpandableListView mainListView;

	Dialog currentDialog=null;      // Último diálogo abierto (crear o editar)
    Product currentItem=null;		// El producto actual en ventana de edición

    // Datos para componentes superiores de filtro rápido
    String strAllShopsLiteral="";	// Para no leer recursos cada poco, el filtro "Todos..." lo guardamos aquí
    String strShopFilter="";		// Filtro actual (Todos... o nombre de super)
    int  intShopFilter=0;			// ID del filtro en el spinner


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
	
		// ------------------------------------------------- 
		// Inicializacion de datos (layout, adapter, datos)
        // Lectura de la BD
        listaProductos = new ListaCompra(this);
        listaTiendas = listaProductos.getTiendasTotal();

        // Asignación de Layout
		setContentView(R.layout.select_product_list);
		setTitle(getResources().getString(R.string.strSelectProductListTitle));

        // Asignación de Adapter y configuración de ListView
        adapterGrupos = new SelectListAdapter(this);
		mainListView = getExpandableListView();
		mainListView.setAdapter(adapterGrupos);
		mainListView.setItemsCanFocus(false);

        // Expandir grupos del ExpandableListView
        for(int i=0; i < adapterGrupos.getGroupCount(); i++)
            mainListView.expandGroup(i);

		// --------------------- 
		// Listeners de botones
        Button btnAdd = (Button)findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener( this );
        
        Button b = (Button)findViewById(R.id.btnDone);
        b.setOnClickListener( this );
        
        // ----------------------------------------------------------------------------------------------
        // Inicializacion del AutoComplete: Listeners, le asignamos un array con los nombres de productos
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.txtQuickSelectProduct);
        ArrayAdapter<String> adapterNombres = new ArrayAdapter<String>(this,R.layout.list_item,listaProductos.getProductosTotal().getItemNames());
        textView.setAdapter(adapterNombres);
        textView.setOnClickListener(this);
        textView.setOnItemClickListener(this);

        // -------------------------------------------------------------
        // Inicializacion del Spinner
        strAllShopsLiteral = getResources().getString(R.string.strSelectProductListQuickShop);
        Spinner spnMarkets = (Spinner)findViewById(R.id.spnMarkets);
        spnMarkets.setSelection(this.intShopFilter);
        List<String> shops = listaProductos.getTiendasTotal();
        arreglaListaTiendas(shops);

        // Asignamos array de tiendas y asignamos listeners.
        final ArrayAdapter<String> adapterShops = new ArrayAdapter<String>(this,R.layout.list_item,shops);
		spnMarkets.setAdapter(adapterShops);
        spnMarkets.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
			@Override
            public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
                // La idea es que al seleccionar una tienda se expanda esa y contraigan las demás.
                // Se desmarcan todos los productos de las otras tiendas. Puede ser confuso y molesto, consultar.
                // fixme ¿Alguna otra idea?
                //Log.d(TAG,"onItemSelected "+pos+"-"+id);
                intShopFilter = pos-1;
                strShopFilter = parent.getItemAtPosition(pos).toString();
                if (pos==0) {
                    // Se ha seleccionado "Todas las tiendas"
                    intShopFilter=0;
                    for (int i = 0; i < adapterGrupos.getGroupCount(); i++)
                        mainListView.expandGroup(i);
                } else {
                    Log.d(TAG,"Seleccionada entrada "+pos+": ["+strShopFilter+"]");

                    // Comprimimos los grupos que no sean el seleccionado.
                    // Y desmarcamos todos sus productos
                    for (int i = 0; i <= adapterGrupos.getGroupCount(); i++) {
                        if (i!=pos) {
                            String tiendaContraer = parent.getItemAtPosition(i).toString();
                            Log.d(TAG,"Desmarcando elementos de grupo "+i+"->"+tiendaContraer);
                            if (!tiendaContraer.equals(strAllShopsLiteral)) {
                                mainListView.collapseGroup(i-1);
                                ProductsSet productos = listaProductos.getProductosDeTienda(tiendaContraer);
                                Iterator<Product> it = productos.iterator();
                                while (it.hasNext()) {
                                    Product p = it.next();
                                    if (p.inShoppingList()) {
                                        listaProductos.modificaProducto(p, false, false);
                                    }
                                }
                            } else
                                Log.d(TAG,"Este no lo desmarcamos porque es "+tiendaContraer);
                        } else {
                            Log.d(TAG,"Este es el seleccionado: "+i+"-"+parent.getItemAtPosition(i));
                            // Expandir grupos del ExpandableListView
                            mainListView.expandGroup(i-1);
                        }

                    }
                    adapterGrupos.notifyDataSetChanged();
                }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

		// Inicializacion de anuncio
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		// Esconder teclado
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
		
		// Si recibimos como parametro un codigo de barras, abrimos la ventana de creacion de producto
		if (getIntent().hasExtra("BARCODE")) {
			showDialog(R.layout.new_product);
		}

	}

	@Override
	public void onStart() {
        Log.d(TAG,"onStart");
        super.onStart();
	}
	

	public void actualizarSpinnerTiendas() {
        Log.d(TAG,"actualizarSpinnerTiendas "+intShopFilter);
        Spinner spnMarkets = (Spinner)findViewById(R.id.spnMarkets);
        List<String> shops = listaProductos.getTiendasTotal();
        arreglaListaTiendas(shops);

        // Revisamos si un borrado de producto provocó un borrado de tienda. En tal caso, ponemos filtro "todos"
        if (this.intShopFilter>=shops.size() || !shops.get(intShopFilter).equals(strShopFilter)) {
        	this.intShopFilter=0;
        	this.strShopFilter="";
        }
        
        ArrayAdapter<String> adapterShops = new ArrayAdapter<String>(this,R.layout.list_item,shops);
		spnMarkets.setAdapter(adapterShops);
    	spnMarkets.setSelection(this.intShopFilter,true);
   	
    	// Actualizamos también Autocomplete de productos por si se borró uno
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.txtQuickSelectProduct);
        ArrayAdapter<String> adapterNombres = new ArrayAdapter<String>(this,R.layout.list_item,listaProductos.getProductosTotal().getItemNames());
        textView.setAdapter(adapterNombres);
	}

    /**
     * Mete el filtro de todas las tiendas en la posición 0, y borra las que no tengan productos.
     * @param tiendas  La lista a arreglar
     */
	private void arreglaListaTiendas(List<String> tiendas)
    {
        Log.d(TAG,"arreglaListaTiendas");
        tiendas.add(0,strAllShopsLiteral);
        for (int i=0; i<tiendas.size(); i++) {
            if (tiendas.get(i).equals("")) {
                //tiendas.set(i,strAllShopsLiteral );
                tiendas.remove(i);
            }
        }
        Log.d(TAG,tiendas.toString());
    }
	
	
	/********************************* MODIFICACION DE LA LISTA DE PRODUCTOS ******************************
	 * 1) Añadir producto
	 * 2) Borrar producto
	 * 3) Editar producto
	 */
	// 1) Añadir producto
	public void addProducto(Product p) {
        Log.d(TAG,"addProducto "+p);

        // Insertamos en la BD (y actualizamos listas)
        listaProductos.creaProducto(p);

        // fixme Subimos a repositorio web
		ShopListWebDAO webDAO = new ShopListWebDAO();
		webDAO.addProduct(p);

        // Actualizamos componentes
        listaTiendas = listaProductos.getTiendasTotal();
		this.adapterGrupos.notifyDataSetChanged();
		this.actualizarSpinnerTiendas();

		// Al restaurar el estado de la aplicación se restauran los diálogos que haya creado, con los consiguientes problemas.
		// Con el remove, el dialogo dejará de existir y no será restaurado.
		this.removeDialog(R.layout.new_product);
		this.removeDialog(R.layout.edit_product);
		
		// Si venimos de una creación desde la pantalla principal, volvemos.
		// Pero antes, lo marcamos como comprado y con una unidad.
		if (getIntent().hasExtra("BARCODE")) {
        	p.setQuantity(new java.math.BigDecimal(1));
        	p.setInShoppingList(true);
        	p.setChecked(true);
            actualizarProducto(p,true,true);
			
			finish();
		}
	}

	// 2) Borrar producto
	public void borrarProducto(Product p) {
        Log.d(TAG,"borrarProducto "+p);
        // Borramos de la BD
		this.listaProductos.borraProducto(p);

        // Actualizamos componentes
        listaTiendas = listaProductos.getTiendasTotal();
		this.adapterGrupos.notifyDataSetChanged();
		this.actualizarSpinnerTiendas();
	}
	
	// 3) Editar producto
	public void actualizarProducto(Product p,boolean checked,boolean inShoppingList) {
        Log.d(TAG,"actualizarProducto "+p);
        // Actualizamos en BD
		listaProductos.modificaProducto(p,checked,inShoppingList);

        // Actualizamos componentes
        listaTiendas = listaProductos.getTiendasTotal();

        // Si la tienda del producto es diferente del filtro de spinner, limpiamos dicho filtro
        /*
        if (!p.getShop().equals(this.strShopFilter)) {
            Log.d(TAG,"Reseteamos filtro porque "+p+" no es de la tienda "+this.strShopFilter);
            Spinner spnMarkets = (Spinner)findViewById(R.id.spnMarkets);
            spnMarkets.setSelection(0);
            this.strShopFilter="";
            this.intShopFilter=0;
            this.actualizarSpinnerTiendas();
        }*/
		this.adapterGrupos.notifyDataSetChanged();
	}

	

	
	/* ***************** EVENTOS ******************** */
	
	/**
	 * Boton->onClick
	 */
	@Override
	public void onClick(View btnClicked) {
		if (btnClicked.getId()==R.id.btnAdd) {
			showDialog(R.layout.new_product);
		}
		if (btnClicked.getId()==R.id.btnDone) {
			 Toast.makeText(getApplicationContext(),getResources().getString(R.string.strUpdatedSL), Toast.LENGTH_SHORT).show();
			 finish();
		}
		if (btnClicked.getId()==R.id.txtQuickSelectProduct) {
			AutoCompleteTextView tv = (AutoCompleteTextView)btnClicked;
			tv.setText("");
		}

	}

	
	
    /**
     * AdapterView.OnItemClickListener
     * AutoCompleteTextView->ItemClick
     * Campo de selección rápida de productos: Sale una lista de productos y se elige uno de ellos.
     * Automáticamente se marca como ya comprado.
     */
    @Override
    public void onItemClick(AdapterView parent,android.view.View view,int position,long id)
    {
		ArrayAdapter<String> aa = (ArrayAdapter<String>)parent.getAdapter();
		String s = aa.getItem(position);
		Product p = listaProductos.getProductosTotal().getItem(s);
		this.actualizarProducto(p,false,!p.inShoppingList());
		
		// Borrar texto
		AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.txtQuickSelectProduct);
		tv.setText(this.getResources().getString(R.string.strSelectProductListQuickSelect));
		
		// Esconder teclado
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
		
		// fixme Scroll de la lista al producto actualizado
		// ListView.setSelection(int position)
	}



    

    /**
     * Ventanas de creación y modificación de productos.
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
    	Dialog dialog;
    	switch (id) {
    	case R.layout.new_product:
            // Creamos el dialogo para crear productos
            dialog = new NewProductDialog(this);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            currentDialog = dialog;
            break;
    	case R.layout.edit_product:
    		if (this.currentItem==null) {
    			dialog=null;
    		} else {
    			dialog = new EditProductDialog(this,this.currentItem);
    		}
    		currentDialog=dialog;
            break;
    	default:
    		dialog=null;
    		break;
    	}

        adapterGrupos.notifyDataSetChanged();
    	return dialog;
    }
    /**/
    
	
    /***********************************************************************
     * Menú de la aplicación
     *
	final int MENU_DATOS_EJEMPLO=0;
	final int MENU_REMOVE_SELECTED=1;
	
    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu the Menu to which entries may be added
     * @return true
     *
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_DATOS_EJEMPLO, 0, R.string.strMenuDatosEjemplo);
        menu.add(0, MENU_REMOVE_SELECTED, 1, R.string.strMenuRemoveSelected);

        return true;
    }
    
    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     *
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case MENU_DATOS_EJEMPLO:
            	productsDAO.rellenaDatosEjemplo();
                shopListItems = productsDAO.readAllProductsList();
                adapter.notifyDataSetChanged();
                return true;          	
        }

        return false;
    }/**/
    
    
    
	/*********************************************************************************
	 * Menú contextual
	 */

	/**
     * Pulsación larga sobre un producto
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	CheckBox cb = (CheckBox)v.findViewById(R.id.listaProductosNombre);
    	this.currentItem = listaProductos.buscaProducto(cb.getText().toString());
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_context_menu, menu);
    }
    
    /**
     * Selección de un elemento del menú contextual
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
      switch (item.getItemId()) {
      case R.id.cntxProductHistory:
			Intent intent = new Intent("ProductHistory.intent.action.Launch");
			intent.putExtra("PRODUCTID", this.currentItem.getId());
            startActivityForResult(intent, 0);
    	  return true;
      default:
        return super.onContextItemSelected(item);
      }
    }

    /**
     * Recibimos resultado del escaneo en la activity en lugar de en los diálogos de crear/editar.
     * Así que tenemos que reabrir el diálogo que sea, rellenando el campo de barcode.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		Barcode objBarcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
		final String barcode = objBarcode.displayValue;
		if (currentDialog instanceof NewProductDialog) {
			NewProductDialog d = (NewProductDialog)currentDialog;
			if (!barcode.equals("")) {
				d.newProductBarcode.setText(barcode);
			}
			d.show();
		} 
		else if (currentDialog instanceof EditProductDialog) {
			EditProductDialog d = (EditProductDialog)currentDialog;
			if (!barcode.equals("")) {
				d.editProductBarcode.setText(barcode);
			}
			d.show();
		}

    }
}
