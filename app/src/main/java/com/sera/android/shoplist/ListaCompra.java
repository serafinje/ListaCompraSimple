package com.sera.android.shoplist;

import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.util.Log;

import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ProductsSet;
import com.sera.android.shoplist.DAO.ShopListDAO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Contiene todo lo relativo al tratamiento de la lista de la compra.
 * Con esto pretendo separarlo del funcionamiento de la app, que se maneja en
 * la clase ShopListActivity, donde todo esto estaba antes.
 * Created by Sera on 23/05/2017.
 */

public class ListaCompra
{
    private final String TAG="ListaCompra";

    // Objeto de acceso a la BD.
    private ShopListDAO productsDAO;

    // Lista completa de productos. No debería ser muy útil ahora que vamos por tienda
    //private ProductsSet shopListItems = new ProductsSet();

    // Lista de tiendas y de productos por tienda.
    //private List<String> tiendas;
    private HashMap<String,ProductsSet> productosPorTienda;
    private HashMap<String,ProductsSet> listaCompraPorTienda;


    public ListaCompra(ExpandableListActivity parentActivity)
    {
        productsDAO = new ShopListDAO(parentActivity);
        this.refresh();
        //productsDAO.showTables();
    }

    /*========================================================
      FUNCIONES PARA DEVOLVER DATOS
     =========================================================*/
    /*public Product getItem(String nombre) {
        return shopListItems.getItem(nombre);
    }*/

    /**
     * fixme ¿Sobra esto por poder usar un getProductosTotal().getItem(String)?
     * ¿Rehacemos esto para usar ese mismo método?
     * fixme ¿Y si tenemos el mismo nombre en diferentes tiendas?
     */
    public Product buscaProducto(String nombre) {
        Product ret=null;
        Iterator<ProductsSet> it = this.productosPorTienda.values().iterator();
        while (it.hasNext() && ret==null) {
            ret = it.next().getItem(nombre);
        }
        return ret;
    }

    // Lista de tiendas (total y en lista de compra)
    public List<String> getTiendasTotal() {
        ArrayList<String> ret = new ArrayList<String>();
        ret.addAll(productosPorTienda.keySet());
        return ret;
    }
    public List<String> getTiendasShopList() {
        ArrayList<String> ret = new ArrayList<String>();
        ret.addAll(listaCompraPorTienda.keySet());
        return ret;
    }
    // Lista de productos (total, en tienda, y en lista de compra)
    public ProductsSet getProductosTotal() {
        ProductsSet ret= new ProductsSet();
        Iterator<ProductsSet> it = productosPorTienda.values().iterator();
        while (it.hasNext())
            ret.addAll(it.next());

        return ret;
    }
    public ProductsSet getProductosDeTienda(String tienda) {
        //return productsDAO.readByMarketProductsList(tienda);
        return this.productosPorTienda.get(tienda);
    }
    public ProductsSet getListaCompraDeTienda(String tienda) {
        //return productsDAO.readByMarketProductsList(tienda);
        return this.listaCompraPorTienda.get(tienda);
    }

    // Sin código de barras / con determinado código de barras
    public ProductsSet getProductsWithoutBarCode() {
        return productsDAO.getProductsWithoutBarCode();
    }
    public ProductsSet getProductByBarCode(String barCode) {
        return productsDAO.getProductByBarCode(barCode);
    }

    // Suma de precios comprados
    public BigDecimal getCheckedPrice() {
        BigDecimal ret = new BigDecimal(0);
        Iterator<String> it = this.listaCompraPorTienda.keySet().iterator();
        while (it.hasNext()) {
            String tienda = it.next();
            ProductsSet productos = this.listaCompraPorTienda.get(tienda);
            ret = ret.add(productos.getCheckedPrice());
        }
        return ret;
    }

    // Suma de precios en lista de compra
    public BigDecimal getTotalPrice() {
        BigDecimal ret = new BigDecimal(0);
        Iterator<String> it = this.listaCompraPorTienda.keySet().iterator();
        while (it.hasNext()) {
            String tienda = it.next();
            ProductsSet productos = this.listaCompraPorTienda.get(tienda);
            ret = ret.add(productos.getTotalPrice());
        }
        return ret;
    }



    /*========================================================
      FUNCIONES PARA MODIFICAR ESTADOS
     =========================================================*/

    /**
     * Actualiza las listas de tiendas y productos.
     */
    public void refresh() {
        productsDAO.deleteEmptyShops();
        List<String> tiendas = productsDAO.getShops();
        this.productosPorTienda = new HashMap<>();
        this.listaCompraPorTienda = new HashMap<>();

        ListIterator<String> it = tiendas.listIterator();
        while (it.hasNext()) {
            String nombre = it.next();
            ProductsSet products = productsDAO.readByMarketProductsList(nombre);
            if (products!=null && products.size()>0)
                productosPorTienda.put(nombre,products);
            products = productsDAO.readByMarketShopList(nombre);
            if (products!=null && products.size()>0)
                listaCompraPorTienda.put(nombre,products);
        }
    }

    /**
     * Desmarca todos los productos y actualiza.
     */
    public void uncheckAll() {
        productsDAO.uncheckAll();
        this.refresh();
    }

    /**
     * Cambia el estado del producto en la lista de la compra.
     * @param p El producto
     * @param checked Marcar/desmarcar
     * @param addHistory Añadir al historial
    // fixme Dejar de usar.
     */
    public void actualizaProducto(Product p,boolean checked,boolean addHistory)
    {
        Log.d(TAG,"actualizaProducto "+p);
        String tienda = p.getShop();
        ProductsSet listaCompraTienda = listaCompraPorTienda.get(tienda);
        if (listaCompraTienda!=null) {
            listaCompraTienda.removeItem(p);
            p.setChecked(checked);
            listaCompraTienda.addItem(p);
        } else {
            // Esto para cuando actualizamos un producto de una tienda que no está en la lista de la compra.
            this.modificaProducto(p,checked,true);
        }
        productsDAO.updateProductChecks(p);
        if (addHistory) {
            productsDAO.addHistory(p);
        }
    }


    // Modifica el producto. Debería poder unir este método y el anterior
    public void modificaProducto(Product p,boolean checked,boolean inShoppingList)
    {
        Log.d(TAG,"modificaProducto "+p);
        //shopListItems.remove(p);
        p.setChecked(checked);
        p.setInShoppingList(inShoppingList);
        //shopListItems.addItem(p);
        productsDAO.updateProduct(p);
        //this.updateShopsAdapter();
        //this.adapter.notifyDataSetChanged();
        // fixme Optimizar. Modificar listas, no releer todo.
        refresh();
    }

    /**
     * Inserta un producto nuevo en la BD
     * @param product
     */
    public void creaProducto(Product product) {
        this.productsDAO.addProduct(product);
        this.productsDAO.addHistory(product);
        // fixme Optimizar. Modificar listas, no releer todo.
        this.refresh();
    }

    /**
     * Borra producto
     * @param product
     */
    public void borraProducto(Product product) {
        this.productsDAO.removeProduct(product);
        // fixme Optimizar. Modificar listas, no releer todo.
        this.refresh();
    }
}
