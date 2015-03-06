package com.example.whereami;

import java.util.ArrayList;
import java.util.Random;

import com.example.whereami.Utils.BitmapInfo;
import com.example.whereami.Utils.GameState;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameMainActivity extends Activity {

	private TextView mResult;
	private GridView mGrid;
	private ImageView mQuestionImage;
	private TextView mScoreCard;
	private DownLoadTask mDownLoadTask;
	public GridAdaptor mGridAdaptor;

	private WakeLock mWakeLock;
	private PowerManager mPowerManager;

	private Button mStartButton;
	private Button mClearButton;
	private String TAG = "GameMainActivity";
	private ArrayList<Drawable> imageList;
	private static ArrayList<BitmapInfo> mDownLoadedImages;
	private static int mUnusedCount = 0;
	private Chronometer myChronometer;

	private static String mQimageTitle;
	private static int mScore = 0;
	private SharedPreferences pref;
	private static boolean isBGExecuting = false;
	private static long mCountUp = (long) 0;
	private static long mStopValue = (long) 15;
	private static long mBaseTime = (long) 0;
	private static boolean mTimerEnd = false;
	private ProgressDialog progress;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			flickImages();
			progress.dismiss();
		}
	};

	private View.OnClickListener clickStart = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mMobileData = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mWifi.isConnected()) {
				startLevel();
			} else if (mMobileData.isConnected()) {
				startLevel();
			} else {
				Toast.makeText(getApplicationContext(),
						"Turn on wifi or mobile data to play.",
						Toast.LENGTH_SHORT).show();
				resetGame();
			}

			Log.i(TAG, "Start Button clicked.");
		}

	};

	private View.OnClickListener clickClear = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			resetGame();
			Log.i(TAG, "Clear Button clicked.");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_main);
		initializeView();
		Object retained = getLastNonConfigurationInstance();
		if (retained instanceof DownLoadTask) {
			mDownLoadTask = (DownLoadTask) retained;
			mDownLoadTask.setActivity(GameMainActivity.this);
		} else {
			mDownLoadTask = new DownLoadTask(GameMainActivity.this);
			GameState.setCurrentState(GameState.NOT_STARTED);
		}
		if (savedInstanceState != null) {
			mQimageTitle = savedInstanceState.getString("QImageTitle");
			GameState.setCurrentState(savedInstanceState.getInt("GameState"));
			if (!mTimerEnd && myChronometer != null) {
				mBaseTime = savedInstanceState.getLong("myChrono");
				savedInstanceState.clear();
				myChronometer.setBase(mBaseTime);
				myChronometer.start();
			}
		} else {
			mBaseTime = (long) 0;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownLoadTask != null)
			mDownLoadTask.setActivity(null);
		return mDownLoadTask;

	}

	private void initializeView() {

		mGrid = (GridView) findViewById(R.id.gridView);
		imageList = new ArrayList<Drawable>();
		mDownLoadedImages = new ArrayList<BitmapInfo>();
		for (int i = 0; i < 9; i++) {
			imageList.add(getResources().getDrawable(
					R.drawable.playing_card_back));
		}
		Log.e(TAG, "list count" + imageList.size());
		setOldAdapter();

		mStartButton = (Button) findViewById(R.id.start);
		mClearButton = (Button) findViewById(R.id.clear);
		mStartButton.setOnClickListener(clickStart);
		mClearButton.setOnClickListener(clickClear);
		mQuestionImage = (ImageView) findViewById(R.id.questionImage);

		mResult = (TextView) findViewById(R.id.result_text);
		mScoreCard = (TextView) findViewById(R.id.Score);
		pref = GameMainActivity.this.getPreferences(Context.MODE_PRIVATE);
		String savedScore = pref.getString("ScoreCard", "0");
		mScoreCard.setText(savedScore);
		mScore = Integer.valueOf(savedScore);
		myChronometer = (Chronometer) findViewById(R.id.chrono1);
		myChronometer.setText("00:00");
		myChronometer
				.setOnChronometerTickListener(new OnChronometerTickListener() {

					@Override
					public void onChronometerTick(Chronometer chronometer) {
						mCountUp = (SystemClock.elapsedRealtime() - chronometer
								.getBase()) / 1000;

						if (mCountUp >= mStopValue) {
							chronometer.stop();
							chronometer.setText("00:00");
							mTimerEnd = true;
							setOldAdapter();
							startGuessing();
						}
					}
				});
	}

	public void notifyActivityTaskCompleted(
			ArrayList<BitmapInfo> downLoadedImages) {
		Thread.dumpStack();
		Log.i(TAG, "Level started ");

		if (GameState.getCurrentState() != GameState.GUESSING) {
			Log.e("suraj", " i not guess");
			if (!checkAllImagesPresent()) {
				Log.e("suraj", " if all images present");
				// invalidate new adapter
				mDownLoadedImages.clear();
				mDownLoadedImages = downLoadedImages;
				setNewAdapter();
				GameState.setCurrentState(GameState.STARTED_LEVEL);
				// Start timer
				startTimer();
			} else {
				Log.e("suraj", " else all images present");
				resetGame();
				Toast.makeText(getApplicationContext(),
						"Failed to load Images. Please try again.",
						Toast.LENGTH_SHORT).show();
			}
		} else if (GameState.getCurrentState() == GameState.GUESSING) {
			Log.e("suraj", " else GUESSING");
			mDownLoadedImages.clear();
			mDownLoadedImages = downLoadedImages;
			restartCurrentLevel();
		}

	}

	private void resetGame() {
		/*if(mWakeLock != null)
		mWakeLock.release();*/
		mDownLoadTask = null;
		if (imageList != null)
			imageList.clear();
		if (mDownLoadedImages != null)
			mDownLoadedImages.clear();
		for (int i = 0; i < 9; i++) {
			imageList.add(getResources().getDrawable(
					R.drawable.playing_card_back));
		}
		setOldAdapter();

		mResult.setText(R.string.result_oncreate);
		mQuestionImage.setImageBitmap(null);
		mQuestionImage.setVisibility(View.VISIBLE);
		// save scores and question ImageView settings
	}

	private void setOldAdapter() {
		mGridAdaptor = new GridAdaptor(GameMainActivity.this, imageList);
		mGrid.setAdapter(mGridAdaptor);
		mGrid.invalidate();
		mGrid.invalidateViews();
		Log.e("suraj", "child count" + mGrid.getChildCount());
		mGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < mDownLoadedImages.size()) {
					if (mDownLoadedImages.get(position).getFlickrTitle()
							.equals(mQimageTitle)) {
						ImageView imageView = (ImageView) view;
						imageView.setImageBitmap(mDownLoadedImages
								.get(position).getFlickrImage());
						mDownLoadedImages.get(position).setCorrectlyGuessed(
								true);
						startGuessing();
					}
				}
			}
		});
	}

	private void setNewAdapter() {
		DownloadedImageAdapter mDownloadedImageAdapter = new DownloadedImageAdapter(
				GameMainActivity.this, mDownLoadedImages);
		mGrid.setAdapter(mDownloadedImageAdapter);
		mGrid.invalidate();
		mGrid.invalidateViews();
	}

	private void startTimer() {
		if (!mTimerEnd) {
			GameState.setCurrentState(GameState.TIMER_START);
			mBaseTime = SystemClock.elapsedRealtime();
			if (myChronometer != null) {
				myChronometer.setBase(mBaseTime);
				myChronometer.start();
				mResult.setText(R.string.timer_started);
			}
		}
	}

	private void startGuessing() {
		GameState.setCurrentState(GameState.GUESSING);
		if (!generateQuestionImage()
				&& GameState.getCurrentState() == GameState.LEVEL_END) {
			endLevel();
		}
	}

	private boolean generateQuestionImage() {
		Log.i(TAG, "generateQuestionImage() ");
		boolean imageFound = false;

		while (!imageFound && !BitmapInfo.mAllVisited) { // + while
			BitmapInfo.mAllVisited = checkAllImagesUsed();

			if (BitmapInfo.mAllVisited) {
				GameState.setCurrentState(GameState.LEVEL_END);
				saveScore();
				imageFound = false;
			} else {
				int randomIndex = randomNumber();
				if (randomIndex > 0 && randomIndex < mDownLoadedImages.size()) {
					if (mUnusedCount <= 3) {
						for (BitmapInfo bitmapInfo : mDownLoadedImages) {
							if (!bitmapInfo.isUsedForQuestion()) {
								bitmapInfo.setUsedForQuestion(true);
								mQimageTitle = bitmapInfo.getFlickrTitle();
								mQuestionImage.setVisibility(View.VISIBLE);
								mQuestionImage.setImageBitmap(bitmapInfo
										.getFlickrImage());
								mQuestionImage.invalidate();
								imageFound = true;
								break;
							}
						}

					} else if (!mDownLoadedImages.get(randomIndex)
							.isUsedForQuestion()
							&& !mDownLoadedImages.get(randomIndex)
									.isCorrectlyGuessed()) {// + if
						mDownLoadedImages.get(randomIndex).setUsedForQuestion(
								true);
						mQimageTitle = mDownLoadedImages.get(randomIndex)
								.getFlickrTitle();
						mQuestionImage.setVisibility(View.VISIBLE);
						mQuestionImage.setImageBitmap(mDownLoadedImages.get(
								randomIndex).getFlickrImage());
						mQuestionImage.invalidate();
						imageFound = true;
						Log.i(TAG, "imageFound ");
						imageFound = true;
					} // - if
				} // - if randomIndex
			}// - if-else
		} // - while

		return imageFound;
	}

	private void startLevel() {
		/*mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"MyWakelockTag");
		mWakeLock.acquire();*/
		
		GameState.setCurrentState(GameState.DOWNLOADING);
		if (mDownLoadTask == null) {
			mDownLoadTask = new DownLoadTask(GameMainActivity.this);
		}
		if (!mDownLoadTask.isTaskCompleted()) {
			initializeView();
			if (!isBGExecuting) {
				isBGExecuting = true;
				mDownLoadTask.execute("Download images from flickr");
			}
			Log.i(TAG, "Start Button clicked. Download Task started");
		}
	}

	private int randomNumber() {
		Random random = new Random();
		return random.nextInt(mDownLoadedImages.size());
	}

	private void endLevel() {
		GameState.setCurrentState(GameState.LEVEL_END);
		BitmapInfo.mAllVisited = false;
		mTimerEnd = false;
		mBaseTime = (long) 0;
		mCountUp = (long) 0;
		myChronometer.setText("00:00");
		
		resetGame();
		return;
	}

	public boolean checkAllImagesUsed() {
		boolean allUsed = true;
		mUnusedCount = 0;
		for (BitmapInfo bitmapInfo : mDownLoadedImages) {
			if (!bitmapInfo.isUsedForQuestion()) {
				mUnusedCount++;
				allUsed = false;
			}
		}

		return allUsed;
	}

	private void saveScore() {
		mScore = mScore + 10;
		mScoreCard.setText(String.valueOf(mScore));
		Editor editor = pref.edit();
		editor.putString("ScoreCard", String.valueOf(mScore));
		editor.clear();
		editor.commit();
		mResult.setText(R.string.result_level_complete);
	}

	public static boolean isBGExecuting() {
		return isBGExecuting;
	}

	public static void setBGExecuting(boolean isBGExecuting) {
		GameMainActivity.isBGExecuting = isBGExecuting;
	}

	public boolean checkAllImagesPresent() {
		boolean noImages = false;
		mUnusedCount = 0;
		for (BitmapInfo bitmapInfo : mDownLoadedImages) {
			if (bitmapInfo.getFlickrImage() == null) {

				noImages = true;
				break;
			}
		}

		return noImages;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("QImageTitle", mQimageTitle);
		savedInstanceState.putInt("GameState", GameState.getCurrentState());
		if (!mTimerEnd) {
			savedInstanceState.putLong("myChrono", mBaseTime);
			myChronometer.stop();
		}
		super.onSaveInstanceState(savedInstanceState);
	}

	public void restartCurrentLevel(){
		Log.e("suraj","inside restartCurrentLevel ");
		// use question image view and show it
		Bitmap bitmap = findQuestionImage();
		if(bitmap != null){
		Log.e("suraj","bitmap is not null");
		mQuestionImage.setImageBitmap(bitmap);
		mQuestionImage.setVisibility(View.VISIBLE);
		mQuestionImage.invalidate();
		//show bitmaps for which identification has been done 
		setOldAdapter();
		//use handler to send message
		progress = new ProgressDialog(GameMainActivity.this);
		progress.setMessage("Refreshing View. Please wait..");
		progress.show();
		mHandler.sendMessageDelayed(Message.obtain(), 2000);
		}
	}

	private Bitmap findQuestionImage() {
		Bitmap bitmap = null;
		for (BitmapInfo bitmapinfo : mDownLoadedImages) {
			if (bitmapinfo.getFlickrTitle().equals(mQimageTitle)) {
				bitmap = Bitmap.createBitmap(bitmapinfo.getFlickrImage());
				mQimageTitle = bitmapinfo.getFlickrTitle();
				bitmapinfo.setUsedForQuestion(true);
			}

		}
		return bitmap;
	}

	private void flickImages() {
		Log.e("suraj", "inside flickImages");
		for (int i = 0; i < mDownLoadedImages.size(); i++) {
			if (mDownLoadedImages.get(i).isCorrectlyGuessed() == true) {
				Log.e("suraj", "child count" + mGrid.getChildCount());
				ImageView imageView = (ImageView) mGrid.getChildAt(i);
				if (imageView != null) {
					imageView.setImageBitmap(mDownLoadedImages.get(i)
							.getFlickrImage());
					imageView.invalidate();
				} else {
					Log.e("suraj", "imageView is null");
				}
			}
		}
	}

}
