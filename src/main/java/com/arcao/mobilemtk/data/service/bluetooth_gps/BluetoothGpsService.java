package com.arcao.mobilemtk.data.service.bluetooth_gps;

import java.io.IOException;
import java.util.regex.Pattern;

public interface BluetoothGpsService {
	void connect(String deviceId) throws IOException;
	void disconnect() throws IOException;
	boolean isConnected();
	void addNmeaListener(NmeaListener listener);
	void removeNmeaListener(NmeaListener listener);

	void postNmea(String command, Object... params) throws IOException;
	java.util.regex.Matcher postNmeaWaitForPattern(Pattern pattern, String command, Object... params) throws IOException;
}
