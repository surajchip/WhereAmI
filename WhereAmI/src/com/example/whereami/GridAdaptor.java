package com.example.whereami;

import java.util.ArrayList;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridAdaptor extends BaseAdapter {

	private Context mContext;

	private ArrayList<Drawable> workerList = new ArrayList<Drawable>();
	public GridAdaptor(Context context, ArrayList<Drawable> list) {
		mContext = context;
		workerList = list;
	}

	@Override
	public int getCount() {

		return workerList.size();
	}

	@Override
	public Object getItem(int position) {

		return workerList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				
				GridView.LayoutParams gridLayout = new GridView.LayoutParams(
						200, 200, Gravity.CENTER);
				imageView.setLayoutParams(gridLayout);
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setPadding(5, 5, 5, 5);
				imageView.setVisibility(View.VISIBLE); 
			} else if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				GridView.LayoutParams gridLayout = new GridView.LayoutParams(
						100, 100, Gravity.CENTER);
				imageView.setLayoutParams(gridLayout);
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setPadding(2, 2, 2, 2);
				imageView.setVisibility(View.VISIBLE); 
			}
			

		} else {
			imageView = (ImageView) convertView;
		}
		imageView.setImageDrawable(workerList.get(position));
		return imageView;
	}
}
