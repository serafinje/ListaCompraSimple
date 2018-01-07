package com.sera.android.shoplist.updates;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sera.android.shoplist.R;
import com.sera.android.shoplist.ShopListActivity;

public class UpdatesDialog extends Dialog 
{
	private ShopListActivity parentActivity;

	public UpdatesDialog(ShopListActivity context) {
		super(context);
		this.parentActivity=context;
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.updates);
	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    setTitle("");
	    
	    ListView lvUpdates = (ListView)findViewById(R.id.lvUpdates);
	    String[] lista = parentActivity.updates.toArray(new String[parentActivity.updates.size()]);
	    lvUpdates.setAdapter(
	    		new ArrayAdapter<String>(this.parentActivity,R.layout.updates_line,lista) {
	    			private LayoutInflater mInflater=(LayoutInflater)parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
		    		@Override
		    		public View getView(int position, View convertView, ViewGroup parent) {
		    			View row;
		    	 
		    			if (null == convertView) {
		    				row = mInflater.inflate(R.layout.updates_line, null);
		    			} else {
		    				row = convertView;
		    			}
		    	 
		    			TextView tv = (TextView) row.findViewById(R.id.tvUpdatesLine);
		    			tv.setText(getItem(position));
		    	 
		    			return row;
		    		}
	    		});
	    
	    Button btnOK = (Button)findViewById(R.id.btnOK);
	    btnOK.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
	    });
	}
}



