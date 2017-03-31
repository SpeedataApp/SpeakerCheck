/*
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android.serialport;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SerialPort {

	private static final String TAG = "SerialPortNative";

	/*
	 * Do not remove or rename the field mFd: it is used by native method
	 * close();
	 */
	private int fdx = -1;
	private int writelen;
	private String str;



	  public SerialPort(String device, int baudrate) throws SecurityException,
              IOException {

	  fdx = openport_easy(device, baudrate);
		  if (fdx < 0) { Log.e(TAG,
	  "native open returns null"); throw new IOException(); }


	  }

	public SerialPort() {
		// openport_easy(dev,brd);
	}


	public void OpenSerial(String dev, int brd) throws SecurityException,
            IOException {
		// int result = 0;
		fdx = openport_easy(dev, brd);
		if (fdx < 0) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
	}

	public void OpenSerial(String device, int baudrate, int databit,
                           int stopbit, int crc) throws SecurityException, IOException {
		// private native int openport(String port,int brd,int bit,int stop,int
		// crc);
		System.out.println("open");
		fdx = openport(device, baudrate, databit, stopbit, crc);
		if (fdx < 0) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
	}

	/*
	 * public int WriteSerialPort(byte[] data) { if(fdx < 0) { return -1; }
	 * return writeport(fdx, data); }
	 */
	/*
	 * public SerialPort() {
	 * 
	 * }
	 * 
	 * public int OpenSerialPort(String port) { if(fd >= 0) { close(fd); } fd =
	 * openport(port); if(fd < 0) { return -1; } return 0; }
	 */
	public int getFd() {
		return fdx;
	}

	public int WriteSerialByte(int fd, byte[] str) {
		writelen = writeport(fd, str);
		Log.d("ID_DEV", "write success writelen = " + writelen);
		if (writelen >= 0) {
			Log.d(TAG, "write success");
		}
		return writelen;
	}

	public int WriteSerialString(int fd, String str, int len) {
		writelen = writestring(fd, str, len);
		return writelen;
	}

	public byte[] ReadSerial(int fd, int len)
			throws UnsupportedEncodingException {
		byte[] tmp;
		tmp = readport(fd, len, 50);
		if (tmp == null) {
			return null;
		}
		/*
		 * for(byte x : tmp) { Log.w("xxxx", String.format("0x%x", x)); }
		 */

		return tmp;
	}

	public String ReadSerialString(int fd, int len)
			throws UnsupportedEncodingException {
		byte[] tmp;
		tmp = readport(fd, len, 50);
		if (tmp == null) {
			return null;
		}
		if (isUTF8(tmp)) {
			str = new String(tmp, "utf8");
			Log.d(TAG, "is a utf8 string");
		} else {
			str = new String(tmp, "gbk");
			Log.d(TAG, "is a gbk string");
		}
		return str;
	}

	public void CloseSerial(int fd) {
		closeport(fd);
	}

	private boolean isUTF8(byte[] sx) {
		Log.d(TAG, "begian to set codeset");
		for (int i = 0; i < sx.length;) {
			if (sx[i] < 0) {
				if ((sx[i] >>> 5) == 0x7FFFFFE) {
					if (((i + 1) < sx.length)
							&& ((sx[i + 1] >>> 6) == 0x3FFFFFE)) {
						i = i + 2;
					} else {
						return false;
					}
				} else if ((sx[i] >>> 4) == 0xFFFFFFE) {
					if (((i + 2) < sx.length)
							&& ((sx[i + 1] >>> 6) == 0x3FFFFFE)
							&& ((sx[i + 2] >>> 6) == 0x3FFFFFE)) {
						i = i + 3;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				i++;
			}
		}
		return true;
	}




	// JNI
	private native int openport_easy(String port, int brd);

	private native int openport(String port, int brd, int bit, int stop, int crc);

	private native void closeport(int fd);

	private native byte[] readport(int fd, int count, int delay);

	private native int writeport(int fd, byte[] buf);

	public native static int writestring(int fd, String wb, int len);

	static {
		System.loadLibrary("serial_port");
	}

}
