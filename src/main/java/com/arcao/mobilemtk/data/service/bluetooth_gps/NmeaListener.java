package com.arcao.mobilemtk.data.service.bluetooth_gps;

/**
 * Created by Arcao on 27. 7. 2014.
 */
public interface NmeaListener {
	void onNmeaReceived(String line, String command, String[] params);
}
