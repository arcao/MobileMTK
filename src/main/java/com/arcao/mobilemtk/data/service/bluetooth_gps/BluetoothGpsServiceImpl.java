package com.arcao.mobilemtk.data.service.bluetooth_gps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by Arcao on 27. 7. 2014.
 */
public class BluetoothGpsServiceImpl implements BluetoothGpsService {
	private static final char NMEA_START = '$';
	private static final char NMEA_FIELD_SEP = ',';
	private static final char CHECKSUM_START = '*';
	private static final char[] EOL_BYTES = { '\r', '\n' };
	private static final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static final int TIMEOUT_COMMAND = 5000;

	private CopyOnWriteArrayList<NmeaListener> listeners;
	private BluetoothSocket socket;
	private ReceiveThread thread;
	private InputStream inputStream;
	private OutputStream outputStream;

	@Override
	public void connect(String deviceId) throws IOException {
		if (isConnected())
			disconnect();

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = adapter.getRemoteDevice(deviceId);

		socket = device.createInsecureRfcommSocketToServiceRecord(UUID_SERIAL);

		// speed up connection
		adapter.cancelDiscovery();

		try {
			socket.connect();

			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);

			try { socket.close(); } catch (IOException ex) { Timber.e(ex, e.getMessage());}

			throw e;
		}

		thread = new ReceiveThread(inputStream);
		thread.start();
	}

	@Override
	public void disconnect() throws IOException {
		thread.interrupt();
		thread = null;

		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
		socket.close();
	}

	@Override
	public boolean isConnected() {
		return thread != null;
	}

	@Override
	public void addNmeaListener(NmeaListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeNmeaListener(NmeaListener listener) {
	listeners.remove(listener);
	}

	@Override
	public void postNmea(String command, Object... params) throws IOException {
		StringBuffer sb = new StringBuffer(command);
		for (Object param : params) {
			sb.append(NMEA_FIELD_SEP);
			sb.append(param);
		}

		postNmea(sb.toString());
	}

	@Override
	public Matcher postNmeaWaitForPattern(Pattern pattern, String command, Object... params) throws IOException {
		final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

		final NmeaListener listener = new NmeaListener() {
			@Override
			public void onNmeaReceived(String line, String command, String[] params) {
				queue.add(line);
			}
		};

		try {
			addNmeaListener(listener);

			postNmea(command, params);

			String line;
			while ((line = queue.poll(TIMEOUT_COMMAND, TimeUnit.MILLISECONDS)) != null) {
				Matcher m = pattern.matcher(line);
				if (m.find()) {
					return m;
				}
			}
		} catch (InterruptedException e) {
			return null;
		} finally {
			removeNmeaListener(listener);
		}
		return null;
	}


	private void postNmea(String line) throws IOException {
		if (!isConnected())
			throw new IOException("GPS Bluetooth device is not connected.");

		int checksum = computeChecksum(line);

		StringBuffer sb = new StringBuffer();
		sb.append(NMEA_START);
		sb.append(line);
		sb.append(CHECKSUM_START);
		if (checksum < 0x10)
			sb.append('0');
		sb.append(Integer.toHexString(checksum).toUpperCase());
		sb.append(EOL_BYTES);

		outputStream.write(sb.toString().getBytes("UTF-8"));
		outputStream.flush();
	}

	private byte computeChecksum(String line) {
		int index = line.length();
		byte checksum = 0;
		while (--index >= 0) {
			checksum ^= line.charAt(index);
		}

		return checksum;
	}

	private boolean verifyChecksum(String line) {
		if (line == null || line.length() == 0) return false;
		if (line.charAt(0) != NMEA_START) return false;

		int checkSumCharPosition = line.lastIndexOf(CHECKSUM_START);
		if (checkSumCharPosition == -1) return false;

		try {
			byte checksum = Byte.parseByte(line.substring(checkSumCharPosition + 1), 16);
			return checksum == computeChecksum(line.substring(1, checkSumCharPosition));
		} catch (Throwable e) {
			Timber.e(e, e.getMessage());
			return false;
		}
	}

	public void processNmea(final String line) {
		if (!verifyChecksum(line)) {
			Timber.e("Checksum is invalid for line: " + line);
			return;
		}

		// remove the NMEA start symbol '$' and checksum part
		int checkSumCharPosition = line.lastIndexOf(CHECKSUM_START);
		String message = line.substring(1, checkSumCharPosition);

		String[] messageParts = StringUtils.splitPreserveAllTokens(message, NMEA_FIELD_SEP);

		// exclude the command from parameters
		String command = messageParts[0];

		String[] params = new String[messageParts.length - 1];
		if (messageParts.length > 1) {
			System.arraycopy(messageParts, 1, params, 0, messageParts.length - 1);
		}

		for (NmeaListener listener : listeners) {
			listener.onNmeaReceived(line, command, params);
		}
	}

	private class ReceiveThread extends Thread {
		private final InputStream inputStream;
		private boolean running = true;

		public ReceiveThread(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public void interrupt() {
			running = false;
			super.interrupt();
		}

		public void run() {
			final StringBuffer sb = new StringBuffer(256);

			try {
				while(running) {
					sb.setLength(0);
					char ch;
					while ((ch = (char) inputStream.read()) != '\n') {
						sb.append(ch);
					}

					processNmea(sb.toString().trim());
				}

			} catch (Throwable e) {
				Timber.e(e, e.getMessage());
			} finally {
				IOUtils.closeQuietly(inputStream);
				thread = null;
			}
		}
	}
}
