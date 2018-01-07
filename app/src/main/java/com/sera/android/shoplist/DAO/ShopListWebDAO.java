package com.sera.android.shoplist.DAO;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ShopListWebDAO
{
    private final String TAG="ShopListWebDAO";
    private Product retProduct=null;

	public Product getProduct(Product p)
	{
        /*
		try {
			String dest = baseURL + "get_product.php3?barcode="+barcode;
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(dest);
			HttpResponse response = httpclient.execute(httppost);		
			Properties props = new Properties();
			props.load(response.getEntity().getContent());
			
			if (!props.isEmpty()) {
				p = new Product(props.getProperty("name"));
				p.setBarcode(barcode);
				p.setType(props.getProperty("type"));
				p.setPrice(props.getProperty("price"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		*/
        new BusquedaProductoTask().execute(p);
		return retProduct;
	}

	public void addProduct(Product p)
	{
		new ProductAdderTask().execute(p);
	}

	
    private class ProductAdderTask extends AsyncTask<Product,Void,Void>
    {
		@Override
		protected Void doInBackground(Product... params)
		{
			Product p = params[0];
			try {
				String dest = "http://serafinje.ddns.net/ListaCompra/";

                URL servidor = new URL(dest);
                HttpURLConnection httpclient = (HttpURLConnection) servidor.openConnection();
                httpclient.setReadTimeout(10000);
                httpclient.setConnectTimeout(15000);
                httpclient.setRequestMethod("POST");
                httpclient.setDoInput(true);
                httpclient.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(httpclient.getOutputStream ());
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("barcode" , p.getBarcode());
                    obj.put("nombre" , p.getName());
                    obj.put("tienda" , p.getShop());
                    obj.put("precio" , p.getPrice());

                    wr.writeBytes(obj.toString());
                    Log.e(TAG, "JSON Input: "+obj.toString());
                    wr.flush();
                    wr.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                httpclient.connect();

                // Recibimos respuesta
                String response="";
                //int responseCode=httpclient.getResponseCode();
                //if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpclient.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                //}
				Log.d(TAG,"Respuesta: Estado="+httpclient.getResponseCode());
                Log.d(TAG,"Respuesta: Mensaje="+httpclient.getResponseMessage());
                Log.d(TAG,"Respuesta: Contenido="+response);
			} catch(Exception e) {
				Log.e(TAG,e.getMessage(),e);
			}		
			return null;
		}
    	
    }


    private class BusquedaProductoTask extends AsyncTask<Product,Void,Void>
    {
        @Override
        protected Void doInBackground(Product... params)
        {
            Product p = params[0];
            try {
                String dest = "http://serafinje.ddns.net/ListaCompra/";
                if (!p.getBarcode().equals("")) {
                    dest += "barcode/"+p.getBarcode();
                } else {
                    dest += "nombre/"+p.getName()+"/tienda/"+p.getShop();
                }

                URL servidor = new URL(dest);
                HttpURLConnection httpclient = (HttpURLConnection) servidor.openConnection();
                httpclient.setReadTimeout(10000);
                httpclient.setConnectTimeout(15000);
                httpclient.setRequestMethod("GET");
                httpclient.setDoInput(true);
                httpclient.setDoOutput(true);
                httpclient.connect();

                // Recibimos respuesta
                JsonReader reader = new JsonReader(new InputStreamReader(httpclient.getInputStream()));
                // Este reader tiene que leer algo como:
                // [
                //   [200,""],
                //   {
                //     "barcode": "55555",
                //     "nombre": "Prueba5",
                //     "tienda": "",
                //     "precio": 60
                //   }
                // ]
                reader.beginArray();
                // Primero leemos el par [estado,mensaje] (que debería ser [200,""])
                reader.beginObject();
                int estado=reader.nextInt();
                String mensaje=reader.nextString();
                reader.endObject();

                // Ahora leemos la tupla del producto, si existe.
                if (reader.hasNext()) {
                    retProduct = new Product("");
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name=reader.nextName();
                        if (name.equals("barcode")) {
                            retProduct.setBarcode(reader.nextString());
                        } else if (name.equals("nombre")) {
                            retProduct.setName(reader.nextString());
                        } else if (name.equals("tienda")) {
                            retProduct.setShop(reader.nextString());
                        } else if (name.equals("precio")) {
                            retProduct.setPrice(reader.nextString());
                        }
                    }
                    reader.endObject();
                }
                reader.endArray();

                //}
                Log.d(TAG,"Respuesta: Estado="+httpclient.getResponseCode());
                Log.d(TAG,"Respuesta: Mensaje="+httpclient.getResponseMessage());
                Log.d(TAG,"Respuesta: Contenido[0]=["+estado+","+mensaje+"]");
                Log.d(TAG,"Respuesta: Contenido[1]=");
            } catch(Exception e) {
                Log.e(TAG,e.getMessage(),e);
            }
            return null;
        }

    }

}


