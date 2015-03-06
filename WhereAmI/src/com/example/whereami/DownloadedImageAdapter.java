package com.example.whereami;

import java.util.ArrayList;

import com.example.whereami.Utils.BitmapInfo;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class DownloadedImageAdapter extends BaseAdapter{
	private Context mContext;

	private ArrayList<BitmapInfo> workerList = new ArrayList<BitmapInfo>();

	public DownloadedImageAdapter(Context context, ArrayList<BitmapInfo> list) {
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
		//Bitmap scaledImage;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				
				GridView.LayoutParams gridLayout = new GridView.LayoutParams(
						200, 200, Gravity.CENTER);
				imageView.setLayoutParams(gridLayout);
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setPadding(5, 5, 5, 5);
				imageView.setVisibility(View.VISIBLE); 
				//scaledImage = Bitmap.createScaledBitmap(workerList.get(position).getFlickrImage(), 200, 200, false);
				
			} else if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				GridView.LayoutParams gridLayout = new GridView.LayoutParams(
						120, 120, Gravity.CENTER);
				imageView.setLayoutParams(gridLayout);
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setPadding(2, 2, 2, 2);
				imageView.setVisibility(View.VISIBLE); 
				//scaledImage = Bitmap.createScaledBitmap(workerList.get(position).getFlickrImage(), 120, 120, false);
			}
			

		} else {
			imageView = (ImageView) convertView;
		}
		
		imageView.setImageBitmap(workerList.get(position).getFlickrImage());
		return imageView;
	}
}
