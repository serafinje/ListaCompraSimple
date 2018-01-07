package com.sera.android.shoplist.productselection;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;
import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.Product;

class EditProductDialog extends Dialog
	implements View.OnClickListener
{
    private SelectProductActivity parentActivity;
    private EditText editProductName;
	private EditText editProductPrice;
	EditText editProductBarcode;
	private AutoCompleteTextView editProductShop;

	private Product prodAnterior;

	private static final int RC_BARCODE_CAPTURE = 9001;

	EditProductDialog(SelectProductActivity context, Product prod) {
		super(context);
		this.parentActivity = context;
		this.prodAnterior = prod;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		setContentView(R.layout.edit_product);
	    getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	    setTitle(this.parentActivity.getResources().getString(R.string.strEditProductTitle));

        Button b = (Button)findViewById(R.id.btnEditProduct_Guardar);
        b.setOnClickListener( this );

        b = (Button)findViewById(R.id.btnEditProduct_Cancel);
        b.setOnClickListener( this );

        ImageButton ib = (ImageButton)findViewById(R.id.btnScan);
        ib.setOnClickListener(this);
	}
	
	public void onStart() {
		this.prodAnterior = parentActivity.currentItem;
		
		editProductName = (EditText)findViewById(R.id.editProduct_Nombre);
		editProductName.setText(parentActivity.currentItem.getName());
        editProductName.setSelection(editProductName.getText().length());

		editProductPrice = (EditText)findViewById(R.id.editProduct_Precio);
		editProductPrice.setText(""+parentActivity.currentItem.getPrice());

		editProductBarcode = (EditText)findViewById(R.id.editProduct_Barcode);
		editProductBarcode.setText(parentActivity.currentItem.getBarcode());

		editProductShop = (AutoCompleteTextView)findViewById(R.id.editProduct_Shop);
		editProductShop.setText(""+parentActivity.currentItem.getShop());
        ArrayAdapter<String> adapterShops = new ArrayAdapter<>(this.getContext(), R.layout.list_item, this.parentActivity.listaProductos.getTiendasTotal());
		editProductShop.setAdapter(adapterShops);
		
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	    //((InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editProductName, 0);  
	}
	
	
	public void setProductText(String productText)
	{
		if (editProductName!=null) {
			editProductName.setText(productText);
		}
	}


	/****************** EVENTOS *********************/
	
	/**
	 * Boton->onClick
	 */
	public void onClick(View btn) {
		// Esconder teclado
		InputMethodManager imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editProductName.getWindowToken(), 0);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		switch (btn.getId()) {
		case R.id.btnEditProduct_Guardar:
			String name = editProductName.getText().toString().trim();
			String type = this.prodAnterior.getType();
			String price = editProductPrice.getText().toString();
			String quantity =this.prodAnterior.getQuantity().toString();
			String barcode = editProductBarcode.getText().toString();
			String shop = editProductShop.getText().toString();
			boolean inSl = this.prodAnterior.inShoppingList();
			boolean pending = this.prodAnterior.isPending();
			
			if (name.equals("")) {
				return;
			}
			if (price.equals("")) price="0";
			
			Product p = new Product(name,type,price,quantity,barcode,shop,inSl,pending);
	
			parentActivity.borrarProducto(prodAnterior);  // Borramos el anterior porque al cambiar los datos ya no lo encontraríamos
			parentActivity.addProducto(p);
			break;
			
		case R.id.btnEditProduct_Cancel:
			// Esconder teclado
			imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editProductName.getWindowToken(), 0);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			break;
			
		case R.id.btnScan:
			Intent intent = new Intent(parentActivity, BarcodeCaptureActivity.class);
			parentActivity.startActivityForResult(intent, RC_BARCODE_CAPTURE);
		}
		dismiss();
	}
}
