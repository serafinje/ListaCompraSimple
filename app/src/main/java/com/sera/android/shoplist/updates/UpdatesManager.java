package com.sera.android.shoplist.updates;


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import com.sera.android.shoplist.R;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * En cada nueva versi?n, debo a?adir un bloque en la parte de arriba del constructor, a?adiendo un VersionUpdates
 * @author Sera
 *
 */
public class UpdatesManager
{
	public final static String firstVersion = "Inicial";
	private class VersionUpdates {
		String versionNumber;
		ArrayList<String> updates;
		
		public VersionUpdates() { this.updates = new ArrayList<String>(); }
	}
	
	private Context context;
	ArrayList<VersionUpdates> appUpdates = new ArrayList<VersionUpdates>();
	
	public UpdatesManager(Context context)
	{
		this.context=context;
		
		/***************************************************************************************
		// Inicializamos la lista de updates
		// 1) Leer el fichero de updates 
		Configuration config = context.getResources().getConfiguration();
		String name = "updates-" + config.locale.getLanguage() + ".txt";

		InputStream stream = null;
		try {
		    stream = context.getAssets().open(name);
	    } catch (IOException exception) {
			name = "updates.txt";
	    	try {
			    stream = context.getAssets().open(name);
	    	} catch (IOException ex2) {
	    		
	    	}
	    }
		
        if (stream != null) {
        	try {
        		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        	
        		String linea="";
        		VersionUpdates currentVersion=null;
        		while ((linea=reader.readLine())!=null) {
        			byte[] arrByteForSpanish = linea.getBytes("UTF-8");  
        			linea = new String(arrByteForSpanish);
        			if (linea.startsWith("Version")) {
        				currentVersion = new VersionUpdates();
        				currentVersion.versionNumber = linea.substring("Version ".length());
        				appUpdates.add(currentVersion);
        				// Cambio de version: Nuevo objeto VersionUpdates
        			} else {
        				// A?adimos string a VersionUpdates actual
        				currentVersion.updates.add(linea);
        			}
        		}
        	} catch (IOException ioe) {
        		
        	} finally {
        		try { stream.close(); } catch (IOException ioe) { Log.d(this.getClass().getName(), ioe.getMessage(),ioe); }
        	}
        }
        **********************************************************************************/
		try {
			VersionUpdates currentVersion;
			BufferedReader br;
			String linea;
			
			currentVersion = new VersionUpdates();
			currentVersion.versionNumber="1.2";
			br = new BufferedReader(new StringReader(context.getResources().getString(R.string.strVersion1_2)));
			linea="";
			while ( (linea = br.readLine())!=null ) {
				currentVersion.updates.add(linea);
			}
			appUpdates.add(currentVersion);

			currentVersion = new VersionUpdates();
			currentVersion.versionNumber="1.1";
			br = new BufferedReader(new StringReader(context.getResources().getString(R.string.strVersion1_1)));
			linea="";
			while ( (linea = br.readLine())!=null ) {
				currentVersion.updates.add(linea);
			}
			appUpdates.add(currentVersion);
	
			currentVersion = new VersionUpdates();
			currentVersion.versionNumber="1.0";
			br = new BufferedReader(new StringReader(context.getResources().getString(R.string.strVersion1_0)));
			linea="";
			while ( (linea = br.readLine())!=null ) {
				currentVersion.updates.add(linea);
			}
			appUpdates.add(currentVersion);
		} catch (IOException ioe) {
		}
	}
		
	public ArrayList<String> getAppUpdates( String prefsFileName, String appName)
	{
		ArrayList<String> ret=null;
		
	   SharedPreferences settings = context.getSharedPreferences(prefsFileName, 0);
	   
	   try {
		   String currentVersion = settings.getString(appName, UpdatesManager.firstVersion);
		   String newVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		   
		   if (currentVersion.equals(UpdatesManager.firstVersion)) {
			   ret=getFirstInstall();
		   } else {
    	   // Miramos cu?l es la nueva versi?n que se acaba de instalar
	           float fCurrentVersion = Float.parseFloat(currentVersion);
	           float fNewVersion = Float.parseFloat(newVersion);
	           if (fNewVersion>fCurrentVersion) {
	        	   // Sacar actualizaciones desde la ultima vez
	        	   ret = getUpdatesSince(currentVersion);
	           }
		   }

		   // 2) Actualizar versi?n instalada
	 	   if (ret!=null) {
				//ret.add("");
				//ret.add(context.getResources().getString(R.string.strUpdatesMsg));  Espero que blablabla
		   
	 		   // TODO Comentar esto cuando quiera hacer debug
	 		   SharedPreferences.Editor editor = settings.edit();
	    	   editor.putString(appName, newVersion);
	    	   editor.commit();
	 	   }
	   } catch (Exception e) {
	   }
	   
	   
	   return ret;
	}
	
	
	
	
	public ArrayList<String> getFirstInstall() {
		VersionUpdates vu = appUpdates.get(appUpdates.size()-1);
		return vu.updates;
	}
	
	public ArrayList<String> getUpdates(String version) {
		ArrayList<String> ret = null;
		Iterator<VersionUpdates> it = appUpdates.iterator();
		boolean salir=false;
		while (it.hasNext() && !salir) {
			VersionUpdates next = it.next();
			if (next.versionNumber.equals(version)) {
				ret = next.updates;
				salir=true;
			}
		}
		return ret;
	}
	
	
	public ArrayList<String> getUpdatesSince(String version) {
		ArrayList<String> ret = new ArrayList<String>();
		String s = ""; for (int i=0; i<appUpdates.size(); i++) s+=appUpdates.get(i).versionNumber+",";
		Iterator<VersionUpdates> it = appUpdates.iterator();
		boolean salir=false;
		while (it.hasNext() && !salir) {
			VersionUpdates next = it.next();
			salir = next.versionNumber.equals(firstVersion) || next.versionNumber.equals(version);

			if (!salir) {
				ret.addAll(next.updates);
			}
		}
		
		return ret;
	}
	
}
