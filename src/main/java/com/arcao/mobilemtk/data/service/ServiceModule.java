package com.arcao.mobilemtk.data.service;

import com.arcao.mobilemtk.data.service.bluetooth_gps.BluetoothGpsService;
import com.arcao.mobilemtk.data.service.bluetooth_gps.BluetoothGpsServiceImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
				injects = {
					BluetoothGpsServiceImpl.class
				},
				complete = false,
				library = true
)
public final class ServiceModule {
	@Provides
	@Singleton
	public BluetoothGpsService getBluetoothGpsService() {
		return new BluetoothGpsServiceImpl();
	}
}
