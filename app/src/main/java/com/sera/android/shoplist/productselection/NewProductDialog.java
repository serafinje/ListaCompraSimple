package com.sera.android.shoplist.productselection;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCaptureActivity;
import com.sera.android.shoplist.DAO.ShopListWebDAO;
import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Dialogo para crear un nuevo producto.
 * Se muestra en la pantalla de añadir/quitar productos.
 * @author Sera
 */
public class NewProductDialog extends Dialog
	implements View.OnClickListener
{
    private Product productoCrear;
	private SelectProductActivity parentActivity;
	private TextView newProductName;
	private EditText newProductPrice;
	EditText newProductBarcode;
	private AutoCompleteTextView newProductShop;
	private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG="NewProductDialog";

	NewProductDialog(SelectProductActivity context) {
		super(context);
		this.parentActivity = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.new_product);
	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    setTitle(this.parentActivity.getResources().getString(R.string.strNewProductTitle));

        // Preparamos los componentes donde meter datos
        newProductName = (TextView)findViewById(R.id.newProduct_Nombre);
        newProductPrice = (EditText)findViewById(R.id.newProduct_Precio);
        newProductBarcode = (EditText)findViewById(R.id.newProduct_Barcode);
        newProductShop = (AutoCompleteTextView)findViewById(R.id.newProduct_Shop);

        // Inicializamos objeto producto, y si tenemos barcode lanzamos la consulta
        productoCrear = new Product("");
/*
        if (parentActivity.getIntent().hasExtra("BARCODE")) {
            productoCrear.setBarcode(parentActivity.getIntent().getExtras().getString("BARCODE"));
            new BusquedaProductoTask().execute(productoCrear);
        }
*/
        // Rellenamos lista de tiendas y preseleccionamos la que tengamos de filtro
        ArrayAdapter<String> adapterShops = new ArrayAdapter<String>(this.getContext(),R.layout.list_item,this.parentActivity.listaProductos.getTiendasTotal());
        newProductShop.setAdapter(adapterShops);

        // EventListeners
        Button b = (Button)findViewById(R.id.btnNewProduct_Crear);
        b.setOnClickListener( this );

        b = (Button)findViewById(R.id.btnNewProduct_Cancel);
        b.setOnClickListener( this );
        
        ImageButton ib = (ImageButton)findViewById(R.id.btnScan);
        ib.setOnClickListener(this);

        // Cuando salimos del nombre, lanzamos consulta por nombre (si no hay barcode)
        newProductName.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus && productoCrear.getBarcode().equals("") && !newProductName.getText().equals("")) {
                            productoCrear.setName(newProductName.getText().toString());
							if (parentActivity.strShopFilter!=null && !parentActivity.strShopFilter.equals(parentActivity.strAllShopsLiteral)) {
								productoCrear.setShop(parentActivity.strShopFilter);
							}
                            Log.d(TAG, "Ejecutando consulta por " + productoCrear.getName()+"/"+productoCrear.getShop());
                            new BusquedaProductoTask().execute(productoCrear);
                        }
                    }
                }
        );
	}

	@Override
	public void onStart()
	{
        // Mostramos teclado
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	    ((InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(newProductName, 0);

        // Si recibimos barcode, lo usamos para lanzar una consulta
        if (parentActivity.getIntent().hasExtra("BARCODE")) {
            productoCrear.setBarcode(parentActivity.getIntent().getExtras().getString("BARCODE"));
            new BusquedaProductoTask().execute(productoCrear);
        }

        // Rellenamos componentes
        rellenaCampos();
    }

    private void rellenaCampos() {
        newProductBarcode.setText(productoCrear.getBarcode());
        newProductName.setText(productoCrear.getName());

        newProductPrice.setText(productoCrear.getPrice().toString());
        // fixme Hacer que se use formato local para los precios (con coma decimal)
        //newProductPrice.setText(String.format(Locale.getDefault(),"%1$.2f",productoCrear.getPrice()));
        newProductShop.setText(productoCrear.getShop());

        if (parentActivity.strShopFilter!=null && !parentActivity.strShopFilter.equals(parentActivity.strAllShopsLiteral)) {
            productoCrear.setShop(parentActivity.strShopFilter);
            newProductShop.setText(parentActivity.strShopFilter);
        } else {
            newProductShop.setText("");
        }
    }

	/****************** EVENTOS *********************/
	
	/**
	 * Boton->onClick
	 */
	public void onClick(View btn) {
		switch (btn.getId()) {
		case R.id.btnNewProduct_Crear:
			String name = newProductName.getText().toString().trim();
			String price = newProductPrice.getText().toString();
			String barcode = newProductBarcode.getText().toString();
			String shop = newProductShop.getText().toString();
			
			if (name.equals("")) {
				return;
			}		
			if (price.equals("")) price="0";
			//if (quantity.equals("")) quantity="1";
	
			
			// Esconder teclado
			InputMethodManager imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(newProductName.getWindowToken(), 0);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			
			Product p = new Product(name,"",price,"1",barcode,shop,true,true);
			parentActivity.addProducto(p);
			dismiss();
			break;
		case R.id.btnNewProduct_Cancel:
			// Esconder teclado
			imm = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(newProductName.getWindowToken(), 0);
			dismiss();
			break;
		case R.id.btnScan:
			Intent intent = new Intent(parentActivity, BarcodeCaptureActivity.class);
			parentActivity.startActivityForResult(intent, RC_BARCODE_CAPTURE);
		}
	}



	/**
	 * Tarea asíncrona para buscar en la web.
	 * Me gustaría que estuviese en el DAO, pero por ahora lo veo más viable aquí.
	 * fixme Quizás al final no necesite la clase DAO y la función de inserción también vaya aquí...
	 */
	private class BusquedaProductoTask extends AsyncTask<Product,Void,JSONObject>
	{
        @Override
        protected void onPreExecute() {
            // Deshabilitar campos mientras se lanza la busqueda
            newProductBarcode.setEnabled(false);
            newProductName.setEnabled(false);
            newProductPrice.setEnabled(false);
            newProductShop.setEnabled(false);
            InputMethodManager keyboard = (InputMethodManager)parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(newProductName, 0);
        }

		@Override
		protected JSONObject doInBackground(Product... params)
		{
			Product p = params[0];
			try {
				String dest = "http://serafinje.ddns.net/ListaCompra/";
				if (!p.getBarcode().equals("")) {
					dest += "barcode/"+ URLEncoder.encode(p.getBarcode(), "UTF-8");
				} else {
					dest += "nombre/"+URLEncoder.encode(p.getName(),"UTF-8")+"/"+URLEncoder.encode(p.getShop(),"UTF-8");
				}

				Log.d(TAG,"Invocando "+dest);
				URL servidor = new URL(dest);
				HttpURLConnection httpclient = (HttpURLConnection) servidor.openConnection();
				httpclient.setReadTimeout(5000);
				httpclient.setConnectTimeout(5000);
				httpclient.setRequestMethod("GET");
				//httpclient.setDoInput(true);
				//httpclient.setDoOutput(true);
				httpclient.connect();

				// Recibimos respuesta
                Log.d(TAG,"Respuesta: Estado="+httpclient.getResponseCode());
                Log.d(TAG,"Respuesta: Mensaje="+httpclient.getResponseMessage());

                // Leemos respuesta
                BufferedReader br=new BufferedReader(new InputStreamReader(servidor.openStream()));
                // Este reader tiene que leer algo como:
                // [
                //   { "estado": 200,
                //     "resultado": ""
                //   },
                //   {
                //     "barcode": "55555",
                //     "nombre": "Prueba5",
                //     "tienda": "",
                //     "precio": 60
                //   }
                // ]

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                httpclient.disconnect();
                Log.d(TAG,"Respuesta: "+sb.toString());

                JSONArray obj = new JSONArray(sb.toString());
                JSONObject respuesta = obj.getJSONObject(0);
                int estado=respuesta.getInt("estado");
                String mensaje=respuesta.getString("resultado");
                Log.d(TAG,"Respuesta: Contenido[0]=["+estado+","+mensaje+"]");
                Log.d(TAG,"Respuesta: Contenido[1]=");

                if (estado==200 && obj.length()>1) {
                    JSONObject datos = obj.getJSONObject(1);
                    return datos;
                }

			} catch(Exception e) {
				Log.e(TAG,e.getMessage(),e);
			}
			return null;
		}

		@Override
        protected void onPostExecute(JSONObject result) {
            if (result!=null) {
                Log.d(TAG, "onPostExecute " + result.toString());
                try {
                    productoCrear.setBarcode(result.getString("barcode"));
                    productoCrear.setName(result.getString("nombre"));
                    productoCrear.setShop(result.getString("tienda"));
                    productoCrear.setPrice(result.getString("precio"));
                    rellenaCampos();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            // Habilitar campos del diálogo
            newProductBarcode.setEnabled(true);
            newProductName.setEnabled(true);
            newProductPrice.setEnabled(true);
            if (newProductPrice.isFocused()) {
                newProductPrice.setSelection(newProductPrice.getText().length());
            }
            newProductShop.setEnabled(true);
            if (newProductShop.isFocused()) {
                newProductShop.setSelection(newProductShop.getText().length());
            }
        }


	}

}
