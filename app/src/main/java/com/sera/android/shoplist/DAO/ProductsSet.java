package com.sera.android.shoplist.DAO;

import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * Vamos a hacer una extension de TreeSet que nos permita manejar elementos comparandolos solamente por el nombre, en lugar de los criterios del Comparable
 * GESTION DE LA LISTA DE PRODUCTOS
 * 0) Indicar si contiene el producto
 * 1) Añadir producto
 * 2) Borrar producto
 * 3) Leer un producto
 * 4) Sacar lista de nombres
 * 5) Sacar precio de pendientes / seleccionados / totales
 * @author Sera
 *
 */
public class ProductsSet extends TreeSet<Product>
{
	// Para que sea serializable
	private static final long serialVersionUID = 1L;
	
	/**
     * 0) Indica si existe un producto con el mismo nombre.
     */
    public boolean containsItem(Product item)
	{
		// Buscamos el item por el nombre
		Iterator<Product> it = this.iterator();
		Product p = null;
		boolean encontrado=false;
		while (it.hasNext() && !encontrado) {
			p = it.next();
			if (p.equals(item)) {
				encontrado=true;
			}
		}

		return encontrado;
	}

    /**
     * 1) Añade un Producto a un TreeSet de Productos
	 * Primero mira si ya existe (por el nombre) y lo borra antes de añadirlo.
	 * Es necesario borrar e insertar porque de lo contrario el TreeSet no reordena.
	 * @return 
     */
    public void addItem(Product item)
	{
    	//Log.d(this.getClass().getName(), "AddItem ["+this.toString()+"] + "+item.toString());
		// Primero buscamos el item por el nombre, para borrarlo
		Iterator<Product> it = this.iterator();
		Product p = null;
		boolean encontrado=false;
		while (it.hasNext() && !encontrado) {
			p = it.next();
			if (p.equals(item)) {
                Log.d(this.getClass().getName(),"Detectada igualdad "+p+" <---> "+item);
				encontrado=true;
			}
		}

		if (encontrado) {
			this.remove(p);
		}
		
		// Despues lo añadimos
		this.add(item);
	}

    /**
     * 2) Borra un Producto de un TreeSet de Productos
     */
    public void removeItem(Product item)
	{
    	//Log.d(this.getClass().getName(), "RemoveItem ["+this.toString()+"] - "+item.toString());
		// Primero buscamos el item por el nombre, para borrarlo
		Iterator<Product> it = this.iterator();
		Product p = null;
		boolean encontrado=false;
		while (it.hasNext() && !encontrado) {
			p = it.next();
			if (p.equals(item)) {
				encontrado=true;
			}
		}

		if (encontrado) {
			//Log.d(this.getClass().getName(),"Borrado="+this.remove(p));
		}
	}
    
    
    
    
	/**
	 * 3) Devuelve un producto buscado por el nombre. Si no lo encuentra, devuelve null.
	 * @param name
	 * @return
	 */
	public Product getItem(String name) {
		Iterator<Product> it = this.iterator();
		while (it.hasNext()) {
			Product p = it.next();
			if (p.getName().equalsIgnoreCase(name)) {
				//Log.d(this.getClass().getName(),"GetItem["+name+"] -> "+p.toString());
				return p;
			}
		}
		
		return null;
	}

	public Product getItem(int pos) {
        return (Product)(this.toArray()[pos]);
    }
	
	
	/**
	 * 4) Devuelve un Array ordenado de los nombres de productos.
	 * @return
	 */
	public ArrayList<String> getItemNames()
	{
		ArrayList<String> ret = new ArrayList<String>();
		TreeSet<String> t = new TreeSet<String>();
		Iterator<Product> it = this.iterator();
		while (it.hasNext()) {
			Product p = it.next();
			//ret.add(p.name);
			t.add(p.getName());
		}
		Iterator<String> it2 = t.iterator();
		while (it2.hasNext()) ret.add(it2.next());
		return ret;
	}
	
	/**
	 * 5) Sacar precio de pendientes / seleccionados / totales
	 */
	public BigDecimal getTotalPrice()
	{
		BigDecimal ret=new BigDecimal(0);
		Iterator<Product> it = this.iterator();
		while (it.hasNext()) {
			Product p = it.next();
			//ret.add(p.name);
			ret = ret.add(p.getTotalPrice());
			//Log.d(this.getClass().getName(),p.getName()+"["+p.getQuantity()+"x"+p.getPrice()+"]->"+p.getTotalPrice()+" ["+ret+"]");
		}
		//return format(ret,"0.00");
		return ret;
	}

	public BigDecimal getPendingPrice()
	{
		BigDecimal ret=new BigDecimal(0);
		Iterator<Product> it = this.iterator();
		while (it.hasNext()) {
			Product p = it.next();
			if (p.isPending()) {
				ret = ret.add(p.getTotalPrice());
			}
		}
		//return format(ret,"0.00");
		return ret;
	}

	public BigDecimal getCheckedPrice()
	{
		BigDecimal ret=new BigDecimal(0);
		Iterator<Product> it = this.iterator();
		while (it.hasNext()) {
			Product p = it.next();
			if (p.isChecked()) {
				ret = ret.add(p.getTotalPrice());
			}
		}
		//return format(ret,"0.00");
		return ret;
	}

	/*
	public static String format(BigDecimal f,String frm)
	{
		java.text.DecimalFormat myFormatter = new java.text.DecimalFormat(frm);
		return myFormatter.format(f);
	}*/
}
