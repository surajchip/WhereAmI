package com.example.whereami.Utils;

public class GameState {

	public static final int NOT_STARTED = 0;
	public static final int DOWNLOADING = 1;
	public static final int DOWNLOAD_COMPLETE = 2;
	public static final int STARTED_LEVEL = 3;
	public static final int TIMER_START = 4;
	public static final int GUESSING = 5;
	public static final int LEVEL_END = 6;
	public static int mCurrentState;
	
	public static int getCurrentState() {
		return mCurrentState;
	}
	public static void setCurrentState(int mCurrentState) {
		GameState.mCurrentState = mCurrentState;
	}
}
