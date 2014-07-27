package com.arcao.mobilemtk;

import android.app.Application;

import com.arcao.mobilemtk.data.DataModule;
import com.arcao.mobilemtk.ui.UiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
				includes = {
								UiModule.class,
								DataModule.class
				},
				injects = {
								App.class
				}
)
public final class MainModule {
	private final App app;

	public MainModule(App app) {
		this.app = app;
	}

	@Provides
	@Singleton
	Application provideApplication() {
		return app;
	}

	@Provides
	@Singleton
	App provideApp() {
		return app;
	}

}