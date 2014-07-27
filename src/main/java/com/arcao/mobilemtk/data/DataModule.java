package com.arcao.mobilemtk.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.mobilemtk.data.service.ServiceModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
				includes = {
								ServiceModule.class
				},
				complete = false,
				library = true
)
public final class DataModule {
	@Provides
	@Singleton
	SharedPreferences provideSharedPreferences(Application app) {
		return PreferenceManager.getDefaultSharedPreferences(app);
	}
}
