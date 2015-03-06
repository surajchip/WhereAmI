package com.example.whereami;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.whereami.Utils.BitmapInfo;
import com.example.whereami.Utils.GameState;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.GridView;

class DownLoadTask extends AsyncTask<String, Integer, ArrayList<BitmapInfo>> {
	private ProgressDialog progressDialog;
	public GridView mGrid;
	private GameMainActivity mCallingActivity;
	private boolean mTaskCompleted = false;
	private String TAG = "DownLoadTask";
	private ArrayList<BitmapInfo> mDownLoadedImages;
	private String qResult;
	private String flickrUri = "https://api.flickr.com/services/feeds/photos_public.gne/?&method=flickr.people.getPublicPhotos&tags=nature&format=json&nojsoncallback=1";

	public DownLoadTask(GameMainActivity activity) {
		mCallingActivity = activity;
		mDownLoadedImages = new ArrayList<BitmapInfo>();

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mTaskCompleted = false;
		progressDialog = new ProgressDialog(mCallingActivity);
		progressDialog.setMessage("Loading images from Flickr. Please wait...");
		progressDialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progressDialog.setMessage(String.format(
				"Loading images from Flickr %s/%s. Please wait...", values[0],
				values[1]));
		GameState.setCurrentState(GameState.DOWNLOADING);
	}

	@Override
	protected ArrayList<BitmapInfo> doInBackground(String... params) {

		mDownLoadedImages.clear();

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(flickrUri);
		HttpResponse response;
		try {
			response = (HttpResponse) client.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(
						entity);
				InputStream inputStream = bufferedEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();
				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");
				}

				qResult = stringBuilder.toString();
				inputStream.close();

			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (qResult != null) {
			ParseJSON(qResult);
			preloadBitmap();
			for (BitmapInfo bitmapinfo : mDownLoadedImages) {
				Log.e(TAG, " bitmap objects"
						+ bitmapinfo.getFlickrImage().toString());
			}
		}
		return mDownLoadedImages;
	}

	@Override
	protected void onPostExecute(ArrayList<BitmapInfo> list) {
		Log.i(TAG, "onPostExecute() is called");
		GameState.setCurrentState(GameState.DOWNLOAD_COMPLETE);
		mTaskCompleted = true;
		GameMainActivity.setBGExecuting(false);
		super.onPostExecute(list);
		notifyActivityTaskCompleted();
	}

	public void setActivity(GameMainActivity activity) {
		Log.i(TAG, "New activity is set");
		Log.e(TAG, "current State " + GameState.getCurrentState());
		this.mCallingActivity = activity;

		if (progressDialog != null && mCallingActivity == null)
			progressDialog.dismiss();

		if (this.mCallingActivity != null
				&& GameState.getCurrentState() == GameState.DOWNLOADING) {
			progressDialog = new ProgressDialog(mCallingActivity);
			progressDialog
					.setMessage("Loading images from Flickr. Please wait...");
			progressDialog.show();
		}
		if (mTaskCompleted) {
			notifyActivityTaskCompleted();
		}
	}

	public boolean isTaskCompleted() {
		return mTaskCompleted;
	}

	public void notifyActivityTaskCompleted() {
		Log.i(TAG, "Task is completed");
		if (mCallingActivity != null && progressDialog != null) {
			progressDialog.dismiss();
			mCallingActivity.notifyActivityTaskCompleted(mDownLoadedImages);
		}
	}

	public ArrayList<BitmapInfo> getDownLoadedImages() {
		return mDownLoadedImages;
	}

	private ArrayList<BitmapInfo> ParseJSON(String json) {

		try {
				JSONObject JsonObject = new JSONObject(json);
					JSONArray JsonArray_photo = JsonObject
							.getJSONArray("items");
					int i = 0;
					while (i < 9) {
						JSONObject FlickrPhoto = JsonArray_photo
								.getJSONObject(i);
						BitmapInfo flickrImage = new BitmapInfo();
						flickrImage.setFlickrImagePath(FlickrPhoto
								.getString("media"));
						flickrImage.setFlickrTitle(FlickrPhoto
								.getString("title"));
						flickrImage.setFlickrImagePath(flickrImage
								.getFlickrImagePath()
								.replaceFirst("\"m\":", ""));
						flickrImage.setFlickrImagePath(flickrImage
								.getFlickrImagePath().replaceAll("[{}\"]", ""));
						flickrImage.setFlickrImagePath(flickrImage
								.getFlickrImagePath().replace("\\", ""));
						flickrImage.setFlickrImagePath(flickrImage
								.getFlickrImagePath().replace("http:///",
										"http://"));
						Log.i(TAG,
								"flickrTitle " + flickrImage.getFlickrTitle());
						Log.i(TAG,
								"flickrMedia "
										+ flickrImage.getFlickrImagePath());
						if (mDownLoadedImages != null) {
							if (mDownLoadedImages.size() < 9) {
								mDownLoadedImages.add(flickrImage);
								i++;
								Log.e(TAG, "Size of mDownLoadedImages "
										+ mDownLoadedImages.size());
							} else {
								return mDownLoadedImages;
							}
						}// - 1st if
					}// - while
			
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return mDownLoadedImages;
	}

	private void preloadBitmap() {
		for (BitmapInfo bitmapInfo : mDownLoadedImages) {

			String FlickrPhotoPath = bitmapInfo.getFlickrImagePath();
			URL FlickrPhotoUrl = null;

			try {
				FlickrPhotoUrl = new URL(FlickrPhotoPath);

				HttpURLConnection httpConnection = (HttpURLConnection) FlickrPhotoUrl
						.openConnection();
				httpConnection.setDoInput(true);
				httpConnection.connect();
				InputStream inputStream = httpConnection.getInputStream();
				Bitmap bitmap = (BitmapFactory.decodeStream(inputStream));
				bitmapInfo.setFlickrImage(Bitmap.createScaledBitmap(bitmap,
						200, 200, false));
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

		} // - for
	} // - preloadBitmap
}// - class
