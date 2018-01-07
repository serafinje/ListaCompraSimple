package com.sera.android.shoplist;

import java.math.BigDecimal;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ShopListWebDAO;

public class CheckoutProductDialog extends Dialog
	implements View.OnClickListener, TextWatcher
{
	private ShopListActivity parentActivity;

	private TextView lblProductName;
	private EditText txtUnitPrice;
	private EditText txtQuantity;
	private TextView txtTotal;
	private Product prodAnterior;
	private Product prodNuevo;
    private final String TAG="CheckoutProductDialog";



	public CheckoutProductDialog(ShopListActivity context,Product prod) {
		super(context);
		this.parentActivity = context;
		this.prodAnterior = prod;
		this.prodNuevo = new Product(prod);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        Log.d(TAG,"onCreate "+parentActivity.currentItem);
		setContentView(R.layout.checkout_product);
	    getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Gestión de eventos
        Button b = (Button)findViewById(R.id.btnCheckoutAccept);
        b.setOnClickListener( this );

        Button bCancel = (Button)findViewById(R.id.btnCheckoutCancel);
        bCancel.setOnClickListener( new View.OnClickListener() {
			public void onClick(View arg0) {
                Log.d(TAG,"onClick Cancel ");
				// Esconder teclado
				InputMethodManager imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(txtUnitPrice.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(txtQuantity.getWindowToken(), 0);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				
				parentActivity.updateItem(prodAnterior, false,false);
				
				dismiss();
			}
		});
        
        txtUnitPrice = (EditText)findViewById(R.id.txtUnitPrice);
        txtUnitPrice.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			public void afterTextChanged(Editable price) {
				if (txtTotal!=null && price!=null && !price.toString().equals("")) {
					try {
						BigDecimal newPrice=new BigDecimal(price.toString());
						prodNuevo.setPrice(newPrice);
						txtTotal.setText(prodNuevo.getTotalPriceCurrency());
					} catch (NumberFormatException nfe) {
						prodNuevo.setPrice(new BigDecimal(0));
						txtTotal.setText(prodNuevo.getTotalPriceCurrency());
					}
				}
			}
		});
        
        txtQuantity = (EditText)findViewById(R.id.txtQuantity);
        txtQuantity.addTextChangedListener(this);
        
        Button bMas = (Button)findViewById(R.id.btnCheckoutMore);
        bMas.setOnClickListener(this);

        Button bMenos = (Button)findViewById(R.id.btnCheckoutLess);
        bMenos.setOnClickListener(this);
	}
	
	public void onStart()
    {
		Log.d(TAG,"onStart "+parentActivity.currentItem);
		if (parentActivity.currentItem!=null) {
			setTitle(parentActivity.getResources().getString(R.string.strCheckOut) + ": " + parentActivity.currentItem.getName());

	    	this.prodAnterior = parentActivity.currentItem;
    		this.prodNuevo =new Product(prodAnterior);
		
		    lblProductName = (TextView)findViewById(R.id.lblProductName);
	    	lblProductName.setText(parentActivity.currentItem.getName());

    		txtUnitPrice = (EditText)findViewById(R.id.txtUnitPrice);
            txtUnitPrice.setText(parentActivity.currentItem.getPrice().toString());
            txtUnitPrice.setSelection(txtUnitPrice.getText().length());
            txtUnitPrice.requestFocus();

            txtQuantity = (EditText)findViewById(R.id.txtQuantity);
		    txtQuantity.setText(parentActivity.currentItem.getQuantity().toString());

		    txtTotal = (TextView)findViewById(R.id.txtTotal);
		    txtTotal.setText(parentActivity.currentItem.getTotalPriceCurrency());
		} else {
            Log.d(TAG,"Dismiss "+parentActivity.currentItem.getName());
			dismiss(); // Esto parece que pasa cuando abrimos otro programa mientras estamos en esta ventana. Al recuperar el estado abre esta ventana pero sin datos.
		}
	}
	
	


	/****************** EVENTOS *********************/
	
	/**
	 * Boton->onClick
	 */
	public void onClick(View btn)
	{
        Log.d(TAG,"onClick OK ");

		// Esconder teclado
		InputMethodManager imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtUnitPrice.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(txtQuantity.getWindowToken(), 0);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if (btn.getId() == R.id.btnCheckoutAccept) {
			String unitPrice = txtUnitPrice.getText().toString(); 
			if (unitPrice!=null && !"".equals(unitPrice)) {
				prodNuevo.setPrice(txtUnitPrice.getText().toString());
			}
			try {
				BigDecimal quantity=null;
				quantity = new BigDecimal(txtQuantity.getText().toString());
				prodNuevo.setQuantity(quantity);
			} catch (Exception pe) {
				//Log.w(this.getClass().getName(),pe);
			}
			boolean addHistory = !prodAnterior.getPrice().equals(prodNuevo.getPrice());
			//Log.d(this.getClass().getName(),"Tienda: "+prodNuevo.getShopId()+" / Precio anterior: "+prodAnterior.getPrice()+" / Prod nuevo: "+prodNuevo.getPrice()+" -> Hist?rico: "+addHistory);
			parentActivity.updateItem(prodNuevo, true,addHistory);

			ShopListWebDAO webDAO = new ShopListWebDAO();
			webDAO.addProduct(prodNuevo);
			
			dismiss();
		}
		if (btn.getId() == R.id.btnCheckoutLess) {
			String quantity = txtQuantity.getText().toString();
			if ("".equals(quantity)) quantity="1";
			BigDecimal bdNewQuantity=new BigDecimal(quantity);
			BigDecimal bdUno = new BigDecimal(1);
			if (bdNewQuantity.intValue()<=1) {
				bdNewQuantity=bdUno;
			} else {
				bdNewQuantity=bdNewQuantity.subtract(bdUno);
			}
			prodNuevo.setQuantity(bdNewQuantity);
			txtQuantity.setText(bdNewQuantity.toString());
			txtTotal.setText(prodNuevo.getTotalPriceCurrency());
		}
		if (btn.getId() == R.id.btnCheckoutMore) {
			String quantity = txtQuantity.getText().toString();
			if ("".equals(quantity)) quantity="0";
			BigDecimal bdNewQuantity=new BigDecimal(quantity);
			BigDecimal bdUno = new BigDecimal(1);
			bdNewQuantity=bdNewQuantity.add(bdUno);
			prodNuevo.setQuantity(bdNewQuantity);
			txtQuantity.setText(bdNewQuantity.toString());
			txtTotal.setText(prodNuevo.getTotalPriceCurrency());
		}
	}
	

	 
	public void afterTextChanged(Editable texto) {
		if (txtTotal!=null && texto!=null && !texto.toString().equals("")) {
			try {
				BigDecimal newQuantity=new BigDecimal(texto.toString());
				prodNuevo.setQuantity(newQuantity);
				txtTotal.setText(prodNuevo.getTotalPriceCurrency());
			} catch (NumberFormatException nfe) {
				prodNuevo.setQuantity(new BigDecimal(0));
				txtTotal.setText(prodNuevo.getTotalPriceCurrency());
			}
		}
	}

	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// @todo Auto-generated method stub
	}

	public void onTextChanged(CharSequence texto, int inicio, int longanterior, int longnueva) {
		// @todo Auto-generated method stub
		
	}
}
