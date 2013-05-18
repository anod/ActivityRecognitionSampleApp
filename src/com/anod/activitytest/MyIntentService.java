package com.anod.activitytest;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.location.ActivityRecognitionResult;

public class MyIntentService extends IntentService {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
	public MyIntentService() {
		super("ActivityTestIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			Intent broadcast = new Intent(ServiceMessageReceiver.MESSAGE_INTENT);
			broadcast.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, result);
			sendBroadcast(broadcast);
		}
	}

}
