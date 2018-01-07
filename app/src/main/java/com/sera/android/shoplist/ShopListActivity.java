package com.sera.android.shoplist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;
import com.google.android.gms.vision.barcode.Barcode;
import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ProductsSet;
import com.sera.android.shoplist.updates.UpdatesDialog;
import com.sera.android.shoplist.updates.UpdatesManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Activity para la vista principal.
 * fixme Histórico precios
 * fixme Al marcar un producto, que lo resalte por un segundo antes de actualizarlo (igual que click sobre precio).
 *
 * @author Serafinje
 */
public class ShopListActivity extends ExpandableListActivity
    implements View.OnClickListener
{
    public ListaCompra listaCompra;             // Los datos

	protected ChkListAdapter adapter;
    Product currentItem=null;       // Para trabajar con el producto seleccionado

	private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "ShopListActivity"; // Para logs


    /**
     * Called when the activity is first created.
     */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        //Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        // Lectura de datos de productos
        listaCompra = new ListaCompra(this);

        InicializaUI();
    }

	private void InicializaUI()
	{
        //Log.d(TAG,"InicializaUI");
        setContentView(R.layout.main);

		// 2) Creacion de un adapter con la informacion de productos
        adapter = new ChkListAdapter(this);

		// Inicializamos el ListView con nuestro adapter, y expandimos los grupos
        ExpandableListView listView = (ExpandableListView)findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        for(int i=0; i < adapter.getGroupCount(); i++)
            listView.expandGroup(i);

        // 3) Inicializacion del resto de componentes
        Button bScan = (Button)findViewById(R.id.btnScan);
        bScan.setOnClickListener(this);
        
        Button btnUncheckAll = (Button)findViewById(R.id.btnUncheckAll);
        btnUncheckAll.setOnClickListener(this);

        Button editList = (Button)findViewById(R.id.btnEditList);
        editList.setOnClickListener(this);

        // Initialize the Mobile Ads SDK.
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Rellenamos con datos iniciales de la suma de la compra
        updatePriceTotals();
        
		// Esconder teclado
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	
	/**
	 * Revisamos si es una nueva instalacion, y en tal caso, mostraremos un mensaje con las novedades
	 */
    public ArrayList<String> updates;
	private void startupMessage()
	{
        //Log.d(TAG,"startupMessage");
 	   UpdatesManager uManager = new UpdatesManager(this);
 	   updates = uManager.getAppUpdates("ListaCompraPrefsFile","ListaCompraVersion");
 	   if (updates!=null) {
	       // Mostrar mensaje en pantalla
           showDialog(R.layout.updates);
 	   }
	}
		
	

	@Override
	public void onStart() {
        //Log.d(TAG,"onStart");
		super.onStart();
		// Esconder teclado
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	@Override
	public void onPause() {
        //Log.d(TAG,"onPause");
		super.onPause();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
        //Log.d(TAG,"onWindowFocusChanged");
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}
	
	public void updateItem(Product p,boolean checked,boolean addHistory) {
        //Log.d(TAG,"updateItem "+p+"->"+checked);
        listaCompra.actualizaProducto(p,checked,addHistory);
		this.adapter.notifyDataSetChanged();
		updatePriceTotals();
	
		// Al restaurar el estado de la aplicacion se restauran los dialogos que haya creado, con los consiguientes problemas
		// Con el remove, el dialogo dejara de existir y no sera restaurado.
		this.removeDialog(R.layout.checkout_product);
		
		// Esconder teclado
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
	}
	
	/**
	 * Muestra el dialogo para elegir la cantidad a comprar
	 */
	public void showUpdateItemDialog(Product p)
	{
        //Log.d(TAG,"showUpdateItemDialog "+p.toString());
		this.currentItem = p;
		showDialog(R.layout.checkout_product);
	}
	
	
	public void updatePriceTotals()
	{
        //Log.d(TAG,"updatePriceTotals");
		TextView tvCompraActual = (TextView)findViewById(R.id.txtCompraActual);
		if (tvCompraActual!=null) {
			tvCompraActual.setText(NumberFormat.getCurrencyInstance().format(listaCompra.getCheckedPrice()));
		}
		
		TextView tvCompraTotal  = (TextView)findViewById(R.id.txtCompraTotal);
		if (tvCompraTotal!=null) {
			tvCompraTotal.setText(NumberFormat.getCurrencyInstance().format(listaCompra.getTotalPrice()));
		}
	}

    /********************************* EVENTOS ******************************/

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
        //Log.d(TAG,"onConfigurationChanged");
	  super.onConfigurationChanged(newConfig);
	  InicializaUI();
	}
	
    /**
     * View.OnClickListener
     * 		AutoCompleteTextView->Click
     * 		BotonAdd->Click
     * 
     * Activity
     * 		Vuelta tras estar en la ventana de seleccion de productos
     */
	public void onClick(View componente)
	{
        //Log.d(TAG,"onClick");
		if (componente.getId()==R.id.btnScan) {
			Intent intent = new Intent(this, BarcodeCaptureActivity.class);
			startActivityForResult(intent, RC_BARCODE_CAPTURE);
		}
		/*
		if (componente.getId()==R.id.btnDatosEjemplo) {
        	productsDAO.rellenaDatosEjemplo();
            shopListItems = productsDAO.readShopList();
            updatePriceTotals();
            adapter.notifyDataSetChanged();
        	componente.setVisibility(View.INVISIBLE);
        }*/
		if (componente.getId()==R.id.btnUncheckAll) {
        	listaCompra.uncheckAll();
            updatePriceTotals();
            adapter.notifyDataSetChanged();
		}

		// SelectProductActivity
		if (componente.getId()==R.id.btnEditList) {
			Intent intent = new Intent("SelectProductActivity.intent.action.Launch");

            // Establecemos orden alfabetico para lista total
            Product.compareFields.clear();
            Product.compareFields.add(Product.COMPARE_NAME);
            
            startActivityForResult(intent, 0);
		}
	}



    
    /**
     * Resultado de abrir la ventana de seleccion de productos.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Log.d(TAG,"onActivityResult");
    	switch(requestCode) {
    	case 0: // Edicion de lista de productos
	        // Primero restauramos orden anterior
	        Product.compareFields.clear();
	        Product.compareFields.add(-Product.COMPARE_PENDING);
	        Product.compareFields.add(Product.COMPARE_NAME);
	
		    // Actualizamos lista de productos
	        listaCompra.refresh();
		    adapter.notifyDataSetChanged();
	        updatePriceTotals();

            ExpandableListView listView = (ExpandableListView)findViewById(android.R.id.list);
            listView.setAdapter(adapter);
            for(int i=0; i < adapter.getGroupCount(); i++)
                listView.expandGroup(i);

			// Al restaurar el estado de la aplicacion se restauran los dialogos que haya creado, con los consiguientes problemas
			// Con el remove, el dialogo dejara de existir y no sera restaurado.
			this.removeDialog(R.layout.select_product_list);
	        break;
    	case RC_BARCODE_CAPTURE: // Barcode Scanner
			if (resultCode == CommonStatusCodes.SUCCESS) {
                Product.compareFields.clear();
                Product.compareFields.add(-Product.COMPARE_PENDING);
                Product.compareFields.add(Product.COMPARE_NAME);
                Product.compareFields.add(Product.COMPARE_SHOP);
                Product.compareFields.add(Product.COMPARE_BARCODE);
				if (data != null) {
                    Barcode objBarcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    final String barcode = objBarcode.displayValue;
                    final ProductsSet ps = listaCompra.getProductByBarCode(barcode);
                    if (ps==null || ps.size()==0) {
                        Log.d(TAG,"**** Código de barras desconocido ****");
                        // Codigo de barras nuevo: Mostramos una lista de productos sin codigo de barras, para elegir uno, o crear
                        final ProductsSet prodsSinBarCode = listaCompra.getProductsWithoutBarCode();
                        ArrayList<String> prodsNames = prodsSinBarCode.getItemNames();
                        prodsNames.add(0, getResources().getString(R.string.strCrearProducto));
                        String[] itemstmp = new String[prodsNames.size()];
                        itemstmp = prodsNames.toArray(itemstmp);
                        final String[] items = itemstmp;

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(barcode);
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item==0) {
                                    // Creamos nuevo producto
                                    // Para ello guardamos la variable BARCODE y arrancamos SelectProductActivity, que abrira NewProductDialog
                                    Intent intent = new Intent("SelectProductActivity.intent.action.Launch");
                                    intent.putExtra("BARCODE", barcode);

                                    // Esto igual ni hace falta aqui
                                    Product.compareFields.clear();
                                    Product.compareFields.add(Product.COMPARE_NAME);

                                    startActivityForResult(intent, 0);
                                } else {
                                    Product current= prodsSinBarCode.getItem(items[item]);
                                    current.setBarcode(barcode);

                                    // Si ya lo teniamos marcado, sumamos una unidad
                                    if (current.inShoppingList() && current.isChecked()) {
                                        current.setQuantity(current.getQuantity().add(new java.math.BigDecimal(1)));
                                    }
                                    // Si no lo teniamos marcado, marcamos con una sola unidad
                                    else if (current.inShoppingList() && !current.isChecked()) {
                                        current.setQuantity(new java.math.BigDecimal(1));
                                    }
                                    // Si no estaba en la lista prevista, lo metemos y marcamos
                                    else if (!current.inShoppingList()) {
                                        current.setInShoppingList(true);
                                        current.setQuantity(new java.math.BigDecimal(1));
                                    }
                                    updateItem(current,true,true);
                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } else if (ps.size()>1) {
                        // La búsqueda devolvió más de un resultado: Mismo producto en diferentes tiendas
                        Log.d(TAG,"**** Código de barras múltiple ****");
                        ArrayList<String> prodsNames = new ArrayList<>();
                        prodsNames.add(0, getResources().getString(R.string.strCrearProducto));
                        Iterator<Product> it = ps.iterator();
                        while (it.hasNext()) {
                            Product pr = it.next();
                            prodsNames.add(pr.getName()+" ("+pr.getShop()+")");
                        }

                        String[] itemstmp = new String[prodsNames.size()];
                        itemstmp = prodsNames.toArray(itemstmp);
                        final String[] items = itemstmp;

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(barcode);
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (item==0) {
                                        // Creamos nuevo producto
                                        // Para ello guardamos la variable BARCODE y arrancamos SelectProductActivity, que abrira NewProductDialog
                                        Intent intent = new Intent("SelectProductActivity.intent.action.Launch");
                                        intent.putExtra("BARCODE", barcode);

                                        // Esto igual ni hace falta aqui
                                        Product.compareFields.clear();
                                        Product.compareFields.add(Product.COMPARE_NAME);

                                        startActivityForResult(intent, 0);
                                    } else {
                                        //Product current= ps.getItem(items[item]);
                                        Product current = ps.getItem(item-1);
                                        //current.setBarcode(barcode);

                                        // Si ya lo teniamos marcado, sumamos una unidad
                                        if (current.inShoppingList() && current.isChecked()) {
                                            current.setQuantity(current.getQuantity().add(new java.math.BigDecimal(1)));
                                        }
                                        // Si no lo teniamos marcado, marcamos con una sola unidad
                                        else if (current.inShoppingList() && !current.isChecked()) {
                                            current.setQuantity(new java.math.BigDecimal(1));
                                        }
                                        // Si no estaba en la lista prevista, lo metemos y marcamos
                                        else if (!current.inShoppingList()) {
                                            current.setInShoppingList(true);
                                            current.setQuantity(new java.math.BigDecimal(1));
                                        }
                                        updateItem(current,true,true);
                                    }
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();

                    } else {
                        Log.d(TAG,"**** Código de barras único ****"+ps.first());
                        // La búsqueda devolvió un solo resultado
                        Product p = ps.first();
                        // Si ya lo teniamos marcado, sumamos una unidad
                        if (p.inShoppingList() && p.isChecked()) {
                            p.setQuantity(p.getQuantity().add(new java.math.BigDecimal(1)));
                        }
                        // Si no lo teniamos marcado, marcamos con una sola unidad
                        else if (p.inShoppingList() && !p.isChecked()) {
                            p.setQuantity(new java.math.BigDecimal(1));
                        }
                        // Si no estaba en la lista prevista, lo metemos y marcamos
                        else if (!p.inShoppingList()) {
                            p.setInShoppingList(true);
                            p.setQuantity(new java.math.BigDecimal(1));
                        }
                        updateItem(p,true,true);

                        // Seguimos escaneando!
                        // TODO Seguimos escaneando? De momento solo cuando no tuvimos que crearlo
                        // Por ahora ni eso.
                        //Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                        //startActivityForResult(intent, RC_BARCODE_CAPTURE);
                    }

				} else {
					Log.d(TAG, "No barcode captured, intent data is null");
				}
			} else {
                String msgError = String.format(getString(R.string.strBarcodeError), CommonStatusCodes.getStatusCodeString(resultCode));
                Log.d(TAG, msgError);
                Toast.makeText(getApplicationContext(),msgError, Toast.LENGTH_SHORT).show();
			}

            break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}

    }
    
    /**
     * Creacion de ventanas de dialogo
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
        //Log.d(TAG,"onCreateDialog");
    	Dialog dialog;
    	switch (id) {
    	case R.layout.checkout_product:
            // Creamos el dialogo para crear productos
            dialog = new CheckoutProductDialog(this,this.currentItem);
            dialog.show();
            break;
    	case R.layout.updates:
            dialog = new UpdatesDialog(this);
            break;
    	default:
    		dialog=null;
    		break;
    	}
        
    	adapter.notifyDataSetChanged();
    	return dialog;
    }
    /**/

    
    
    

    /****************** MENU DE LA APLICACION ******************/
    
	final int MENU_CONTACTME=0;
	final int MENU_UNCHECK_ALL=1;
	final int MENU_FEEDBACK=2;
    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        //menu.add(0, MENU_CONTACTME, 0, R.string.strFeedback);
        menu.add(0, MENU_FEEDBACK, 0, R.string.strRate);
        menu.add(0, MENU_UNCHECK_ALL, 1, R.string.strMenuUncheckAll);

        return true;
    }   
	
	
    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case MENU_FEEDBACK:
        	String appPackageName=getResources().getString(R.string.app_package_name);
        	Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appPackageName));
        	marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        	startActivity(marketIntent);
        	break;
            case MENU_CONTACTME:
    			Intent intent = new Intent("SendFeedbackActivity.intent.action.Launch");
                startActivityForResult(intent, 0);
                return true;
            case MENU_UNCHECK_ALL:
            	listaCompra.uncheckAll();
                updatePriceTotals();
                adapter.notifyDataSetChanged();
                return true;            	
        }

        return false;
    }

    
	/*********************************************************************************
	 * Menu contextual
	 */

	/**
     * Pulsacion larga sobre un Producto
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	CheckBox cb = (CheckBox)v.findViewById(R.id.listaProductosNombre);
    	this.currentItem = listaCompra.buscaProducto(cb.getText().toString());
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_context_menu, menu);
    }
    
    /**
     * Seleccion de un elemento del menu contextual
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
      //Log.d(this.getClass().getName(), "Seleccionado menu "+item.getTitle()+", sobre producto "+this.currentItem.getName());
      switch (item.getItemId()) {
      case R.id.cntxProductHistory:
    	  //showDialog(R.layout.product_history);
			Intent intent = new Intent("ProductHistory.intent.action.Launch");
			intent.putExtra("PRODUCTID", this.currentItem.getId());
            startActivityForResult(intent, 0);
    	  return true;
      default:
        return super.onContextItemSelected(item);
      }
    }
    
}
