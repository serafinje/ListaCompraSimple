package com.sera.android.shoplist.backup;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;

import com.sera.android.shoplist.DAO.ShopListDAO;
import com.sera.android.shoplist.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * Created by Sera on 15/05/2017.
 */
public class ShopListBackupAgent extends BackupAgent
{
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException
    {
        PrintWriter pw = new PrintWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/ListaCompraSimple.backup");

        ShopListDAO productsDAO = new ShopListDAO(this.getBaseContext());
        ArrayList<String> export = productsDAO.exportTables();
        Iterator<String> it = export.iterator();
        while (it.hasNext()) {
            String s = it.next();
            pw.write(s);
            // Key
            StringTokenizer st = new StringTokenizer(s,"|");
            String key = st.nextToken()+st.nextToken();
            // Value
            byte[] buffer  = s.getBytes();
            int len = buffer.length;

            // Grabamos ambos
            data.writeEntityHeader(key, len);
            data.writeEntityData(buffer, len);
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.shoppingcart)
                        .setContentTitle("Backup realizado")
                        .setContentText("Copiados "+export.size()+" productos.");

        pw.close();
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException
    {
        PrintWriter pw = new PrintWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/ListaCompraSimple.backup");
        ArrayList<String> datos = new ArrayList<>();

        while (data.readNextHeader()) {
            String key = data.getKey();     // Esto por ahora no lo vamos a usar

            int dataSize = data.getDataSize();
            byte[] dataBuf = new byte[dataSize];
            data.readEntityData(dataBuf, 0, dataSize);
            String producto = new String(dataBuf);
            datos.add(producto);
            pw.write(producto);
        }
        pw.close();

        // Importamos la lista que acabamos de leer
        ShopListDAO productsDAO = new ShopListDAO(this.getBaseContext());
        productsDAO.importTables(datos);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.shoppingcart)
                        .setContentTitle("Restore realizado")
                        .setContentText("Restaurados "+datos.size()+" productos.");

    }
}
