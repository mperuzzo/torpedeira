package com.peruzzo.torpedeira;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwoLayoutsArrayAdapter extends ArrayAdapter<String> {
	
	private final List<Mensagem> values;
	private LayoutInflater inflator;

	static class ViewHolder {
		public TextView text;
		public ImageView image;
	}

	public TwoLayoutsArrayAdapter(Context context) {				
		super(context, R.id.TextView01);		
		this.values = new ArrayList<Mensagem>();
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void add(Mensagem mensagem) {
		this.values.add(mensagem);
		add(mensagem.toString());
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return (position % 2 == 0) ? 0 : 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflator.inflate(R.layout.row, null);
			ImageView icon = (ImageView) rowView.findViewById(R.id.ImageView01);
			if (this.values.get(position).getStatus() == Activity.RESULT_OK) {
				 icon.setImageResource(R.drawable.sms_send);
			} else {
				icon.setImageResource(R.drawable.sms_not_send);
			}
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.TextView01);
			viewHolder.image = icon;
			rowView.setTag(viewHolder);
		}
		ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.text.setText(this.values.get(position).toString());
		return rowView;
	}
}

