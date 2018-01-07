package com.sera.android.shoplist.productselection;

import java.text.NumberFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.sera.android.shoplist.R;
import com.sera.android.shoplist.DAO.Product;
import com.sera.android.shoplist.DAO.ProductsSet;

/**
 * Adapter personalizado para manejar un Checklist de productos 
 * @author Serafinje
 */
public class SelectListAdapter extends BaseExpandableListAdapter
{
    private String TAG="SelectListAdapter";

	// Activity en la que está corriendo el adapter. La necesitamos para sacar información del contexto y demás.
	private SelectProductActivity selectProductActivity;
	
	public SelectListAdapter(SelectProductActivity slActivity) {
		this.selectProductActivity=slActivity;
		//this.inflater = (LayoutInflater)slActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    @Override
    public Object getChild(int groupPosition, int childPosition) {
            //Log.d(TAG, "getChild " + groupPosition + "-" + childPosition);
            String nombreSuper = this.selectProductActivity.listaTiendas.get(groupPosition);
            ProductsSet listaSuper = this.selectProductActivity.listaProductos.getProductosDeTienda(nombreSuper);
            return listaSuper.toArray()[childPosition];
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        //Log.d(TAG,"getChildId "+groupPosition);
        return childPosition;
    }


    /**
     * Devuelve la vista de linea del checklist
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param productView
     * @param parent
     * @return
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View productView, ViewGroup parent)
    {
        //Log.d(TAG,"getChildView "+groupPosition+"-"+childPosition);
        if (productView == null) {
            LayoutInflater infalInflater = (LayoutInflater)this.selectProductActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            productView = infalInflater.inflate(R.layout.select_product_line, null);
        }

        // Creamos una objeto especial que almacena todos los datos del producto formateados
        final LineaDeSeleccionHolder lineaHolder = new LineaDeSeleccionHolder();
        lineaHolder.imgbtnDelete = (ImageButton)productView.findViewById(R.id.btnProductDelete);
        lineaHolder.imgbtnEdit = (ImageButton)productView.findViewById(R.id.btnProductEdit);
        lineaHolder.chkItem = (CheckBox)productView.findViewById(R.id.listaProductosNombre);
        lineaHolder.txtPrecio = (TextView)productView.findViewById(R.id.listaProductosPrecio);

        final Product childProduct = (Product)getChild(groupPosition, childPosition);

        // Columna 1: Botón de borrado.
        //	Con el click, sacamos un cuadro de diálogo para confirmar el borrado
        lineaHolder.imgbtnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(selectProductActivity);
                builder.setMessage(selectProductActivity.getResources().getText(R.string.strConfirmaBorrado)+" "+childProduct.getName()+"?");
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.strSi, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectProductActivity.borrarProducto(childProduct);
                        selectProductActivity.adapterGrupos.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(R.string.strNo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        // Columna 2: Botón de edición
        lineaHolder.imgbtnEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectProductActivity.currentItem = childProduct;
                selectProductActivity.showDialog(R.layout.edit_product);
            }
        });


        // Columna 3: Checkbox
        //		Activamos o no, según el campo correspondiente.
        //		Le ponemos el nombre del producto.
        //		Tachamos si está activado.
        //		Asignamos listener para cambio de check.
        //		Asignamos listener para pulsación larga -> Desactivamos menú contextual (usamos el global)
        lineaHolder.chkItem.setChecked(childProduct.inShoppingList());
        lineaHolder.chkItem.setText(childProduct.getName());
        if (childProduct.inShoppingList()) {
            lineaHolder.chkItem.setPaintFlags(lineaHolder.chkItem.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            lineaHolder.chkItem.setPaintFlags(lineaHolder.chkItem.getPaintFlags()&~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //Mirar si puedo usar esto o tengo que usar el OnClickListener
        /* Creo que no funciona, voy a tener que usar el onClick como en la otra pantalla.
        lineaHolder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Product current = (Product)getChild(groupPosition, childPosition);
                selectProductActivity.actualizarProducto(current, false,isChecked);
            }
        });*/
        lineaHolder.chkItem.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                // return false to let list's context menu show
                return false;
            }
        });

         /* [ChkListAdapter] Ahora gestiono el Click en lugar del CheckedChange */
        lineaHolder.chkItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Product current = (Product)getChild(groupPosition, childPosition);
                Log.d(TAG,"SeleccionProducto.onClick "+groupPosition+","+childPosition+" "+"-"+current.toString());
                selectProductActivity.actualizarProducto(current, false,!current.inShoppingList());
            }
        });
        /**/


        // Columna 4: Precio
        //		Formateamos con dos decimales y pintamos
        //String precio = ProductsSet.format(prod.getPrice(),"0.00");
        String precio = NumberFormat.getCurrencyInstance().format(childProduct.getPrice());
        lineaHolder.txtPrecio.setText(precio.toCharArray(),0,precio.length());

        selectProductActivity.registerForContextMenu(productView);
        // Asignamos el objeto especial como "tag" para la View
        productView.setTag(lineaHolder);

        // Done!
        return productView;

    }


    @Override
    public int getChildrenCount(int groupPosition) {
        int ret=0;
        String tienda = this.selectProductActivity.listaTiendas.get(groupPosition);
        ProductsSet listaSuper = this.selectProductActivity.listaProductos.getProductosDeTienda(tienda);
        if (listaSuper!=null) ret=listaSuper.size();
        //Log.d(TAG,"getChildrenCount "+groupPosition+" -> "+tienda+" -> "+listaSuper.size());
        return ret;
    }


    @Override
    public Object getGroup(int groupPosition) {
        //Log.d(TAG,"getGroup "+groupPosition);
        return this.selectProductActivity.listaTiendas.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        Log.d(TAG,"getGroupCount -> "+this.selectProductActivity.listaTiendas.size());
        return this.selectProductActivity.listaTiendas.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        //Log.d(TAG,"getGroupId "+groupPosition);
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        Log.d(TAG,"getGroupView "+groupPosition);
        String headerTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.selectProductActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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


	public static class LineaDeSeleccionHolder {
		public ImageButton imgbtnDelete;
		public ImageButton imgbtnEdit;
		public CheckBox chkItem;
		public TextView txtPrecio;
	}

}
