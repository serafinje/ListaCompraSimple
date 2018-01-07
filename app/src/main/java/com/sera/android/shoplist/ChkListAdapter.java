package com.sera.android.shoplist;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ProductsSet;

import java.util.HashMap;
import java.util.List;

/**
 * Adapter personalizado para manejar un Checklist de productos 
 * @author Serafinje
 */
class ChkListAdapter extends BaseExpandableListAdapter
{
    private String TAG="ChkListAdapter";

	// Activity en la que esta corriendo la aplicación. La necesitamos para sacar información del contexto y demás.
	private ShopListActivity slActivity;
    private Context _contexto;

	public ChkListAdapter(ShopListActivity slActivity)
    {
        this._contexto = slActivity.getBaseContext();
		this.slActivity=slActivity;
		//this.inflater = (LayoutInflater)slActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Log.d(TAG,"Iniciado Adapter con "+this._listHeaderSupermercados.size()+" supermercados y "+this._listChildProductos.size()+" listas de productos.");
	}
	
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        String nombreSuper = this.slActivity.listaCompra.getTiendasShopList().get(groupPosition);
        ProductsSet listaSuper = this.slActivity.listaCompra.getListaCompraDeTienda(nombreSuper);
        return listaSuper.toArray()[childPosititon];
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View productView, ViewGroup parent)
    {
        if (productView == null) {
            LayoutInflater infalInflater = (LayoutInflater)this._contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            productView = infalInflater.inflate(R.layout.productline, null);
        }

        // Creamos una objeto especial que almacena todos los datos del producto formateados
        final LineaDeListaCompraHolder lineaHolder = new LineaDeListaCompraHolder();
        lineaHolder.chkItem = (CheckBox)productView.findViewById(R.id.listaProductosNombre);
        lineaHolder.txtCantidad = (TextView)productView.findViewById(R.id.listaProductosCantidad);
        lineaHolder.txtPrecio = (TextView)productView.findViewById(R.id.listaProductosPrecio);

        //Product prod = getItem(childPosition);
        final Product childProduct = (Product)getChild(groupPosition, childPosition);
        //Log.d(TAG,childProduct.toString());
        String precio = childProduct.getTotalPriceCurrency();
        lineaHolder.txtPrecio.setText(precio.toCharArray(),0,precio.length());
        lineaHolder.txtCantidad.setText("("+new java.text.DecimalFormat("0.##").format(childProduct.getQuantity())+")");
        lineaHolder.chkItem.setChecked(!childProduct.isPending());
        lineaHolder.chkItem.setText(childProduct.getName());

        if (!childProduct.isPending()) {
            Log.d(TAG,"Tachando Elemento "+childPosition+": "+childProduct);
            lineaHolder.chkItem.setPaintFlags(lineaHolder.chkItem.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            lineaHolder.chkItem.setPaintFlags(lineaHolder.chkItem.getPaintFlags()&~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        // Listener para el click sobre el check
        /** fixme Quito esto porque saltaba cada vez que hacía un setChecked y solo lo quiero cuando se pulse.
        lineaHolder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Product current = getItem(childPosition);
                Product current = (Product)getChild(groupPosition, childPosition);
                Log.d(TAG,"onCheckedChanged "+groupPosition+","+childPosition+" "+isChecked+"-"+current.toString());
                if (isChecked) {
                    // Dialogo para elegir unidades/cantidad
                    slActivity.showUpdateItemDialog(current);
                } else {
                    // Desmarcar, sin dialogo
                    slActivity.updateItem(current, isChecked,false);
                }
            }
        });
        /* Ahora gestiono el Click en lugar del CheckedChange */
        lineaHolder.chkItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Product current = (Product)getChild(groupPosition, childPosition);
                Log.d(TAG,"Producto.onClick "+groupPosition+","+childPosition+" "+"-"+current.toString());
                if (current.isPending()) {
                    // Dialogo para elegir unidades/cantidad
                    slActivity.showUpdateItemDialog(current);
                } else {
                    // Desmarcar, sin dialogo
                    slActivity.updateItem(current, false, false);
                    Product.compareFields.clear();
                    Product.compareFields.add(-Product.COMPARE_PENDING);
                    Product.compareFields.add(Product.COMPARE_NAME);

                    // Actualizamos lista de productos
                    slActivity.listaCompra.refresh();
                    slActivity.adapter.notifyDataSetChanged();
                    slActivity.updatePriceTotals();
                }
           }
        });
        /**/

        lineaHolder.chkItem.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                // return false to let list's context menu show
                return false;
            }
        });
        slActivity.registerForContextMenu(productView);
        // Asignamos el objeto especial como "tag" para la View
        productView.setTag(lineaHolder);

        // Para que el click también funcione fuera del check
        /* Este listener no parece funcionar, al menos por ahora */
        productView.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                //Product current = getItem(childPosition);
                Product current = (Product)getChild(groupPosition, childPosition);
                Log.d(TAG,"********************************** producto.onClick "+groupPosition+","+childPosition+" "+current.toString());
                if (!current.isChecked()) {
                    slActivity.showUpdateItemDialog(current);
                } else {
                    slActivity.updateItem(current, false,false);
                }

            }
        });
        /**/
        return productView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        String tienda = this.slActivity.listaCompra.getTiendasShopList().get(groupPosition);
        ProductsSet listaSuper = this.slActivity.listaCompra.getListaCompraDeTienda(tienda);
        return listaSuper.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.slActivity.listaCompra.getTiendasShopList().get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.slActivity.listaCompra.getTiendasShopList().size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        String headerTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView)convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }




	/**
	 * Devuelve la vista de linea del CheckList
	 *
     * METODO COMENTADO: VERSION PRE-AGRUPAMIENTO POR SUPER
	public View getChildView(final int pos, View productView, ViewGroup parent)
	{
		productView = inflater.inflate(R.layout.productline, null);
		final ViewHolder holder = new ViewHolder();
		holder.chkItem = (CheckBox)productView.findViewById(R.id.listaProductosNombre);
		holder.quantity = (TextView)productView.findViewById(R.id.listaProductosCantidad);
		holder.precio = (TextView)productView.findViewById(R.id.listaProductosPrecio);

		Product prod = getItem(pos);
		String precio = prod.getTotalPriceCurrency();
		holder.precio.setText(precio.toCharArray(),0,precio.length());
		holder.quantity.setText("("+new java.text.DecimalFormat("0.##").format(prod.getQuantity())+")");
		holder.chkItem.setChecked(!prod.isPending());
		holder.chkItem.setText(prod.getName());
		if (!prod.isPending()) {
			holder.chkItem.setPaintFlags(holder.chkItem.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
		}
		// Click sobre el check
		holder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Product current = getItem(pos);
				if (isChecked) {
					slActivity.showUpdateItemDialog(current);
				} else {
					slActivity.updateItem(current, isChecked,false);
				}
			}
		});
		holder.chkItem.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                // return false to let list's context menu show
                return false;
            }
        });
		slActivity.registerForContextMenu(productView);
		productView.setTag(holder);
		
		// Para que el click también funcione fuera del check
		productView.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Product current = getItem(pos);
				if (!current.isChecked()) {
					slActivity.showUpdateItemDialog(current);
				} else {
					slActivity.updateItem(current, false,false);
				}
				
			}
		});
		return productView;
	}
    ********************************************

	// 3) Leer un item
	public Product getItem(int pos) {
		//Log.d(this.getClass().getName(),"GetItem["+pos+"]");
		Product ret =(Product)(slActivity.shopListItems.toArray())[pos]; 
		//Log.d(this.getClass().getName(),"GetItem["+pos+"] -> "+ret.toString());
		return ret;
	}

	
	// 4) Leer lista de items
	public ProductsSet getItems() {
		return slActivity.shopListItems;
	}
	
	
	public void setItems(ProductsSet it) {
 		//Log.d(this.getClass().getName(),"SetItems - "+it.size()+" elementos");
		this.slActivity.shopListItems=it;
		notifyDataSetChanged();
	}
	*/

	private static class LineaDeListaCompraHolder {
		CheckBox chkItem;
		TextView txtCantidad;
		TextView txtPrecio;
	}

}
