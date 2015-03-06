package com.example.whereami.Utils;

import android.graphics.Bitmap;

public class BitmapInfo {
	private String mFlickrTitle;
	private String mFlickrImagePath;
	private Bitmap mFlickrImage;
	private boolean mUsedForQuestion = false;
	private boolean mCorrectlyGuessed = false;
	public static boolean mAllVisited = false ;
	
	public String getFlickrTitle() {
		return mFlickrTitle;
	}
	public void setFlickrTitle(String mFlickrTitle) {
		this.mFlickrTitle = mFlickrTitle;
	}
	public String getFlickrImagePath() {
		return mFlickrImagePath;
	}
	public void setFlickrImagePath(String mFlickrImagePath) {
		this.mFlickrImagePath = mFlickrImagePath;
	}
	public Bitmap getFlickrImage() {
		return mFlickrImage;
	}
	public void setFlickrImage(Bitmap mFlickrImage) {
		this.mFlickrImage = mFlickrImage;
	}
	public boolean isUsedForQuestion() {
		return mUsedForQuestion;
	}
	public void setUsedForQuestion(boolean mUsedForQuestion) {
		this.mUsedForQuestion = mUsedForQuestion;
	}
	public boolean isCorrectlyGuessed() {
		return mCorrectlyGuessed;
	}
	public void setCorrectlyGuessed(boolean mCorrectlyGuessed) {
		this.mCorrectlyGuessed = mCorrectlyGuessed;
	}
	
	
	

}
