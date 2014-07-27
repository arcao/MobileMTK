package com.arcao.mobilemtk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import javax.inject.Inject;

import dagger.ObjectGraph;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by Arcao on 27. 7. 2014.
 */
public class App extends Application {
	private ObjectGraph objectGraph;
	@Inject
	SharedPreferences prefs;
	private String deviceId;

	@Override
	public void onCreate() {
		super.onCreate();

		//if (BuildConfig.DEBUG) {
		Timber.plant(new Timber.DebugTree());
		//}

		buildObjectGraphAndInject();
	}

	@DebugLog
	public void buildObjectGraphAndInject() {
		objectGraph = ObjectGraph.create(Modules.list(this));
		objectGraph.inject(this);
	}

	public void inject(Object o) {
		objectGraph.inject(o);
	}

	public static App get(Context context) {
		return (App) context.getApplicationContext();
	}

	public String getDeviceId() {
		if (deviceId == null) {
			deviceId = prefs.getString("device_id", null);

			if (deviceId == null) {
				deviceId = UUID.randomUUID().toString();
				prefs.edit().putString("device_id", deviceId).apply();
			}
		}

		return deviceId;
	}


}
