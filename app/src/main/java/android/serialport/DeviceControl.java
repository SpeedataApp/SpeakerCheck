package android.serialport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DeviceControl {
	private BufferedWriter CtrlFile;
	
	public DeviceControl(String path) throws IOException
	{
		File DeviceName = new File(path);
		CtrlFile = new BufferedWriter(new FileWriter(DeviceName, false));	//open file
	}
	public void PowerOnDevice() throws IOException        //poweron id device
	{
		if (android.os.Build.VERSION.RELEASE.equals("5.1")){

	//		if((android.os.Build.MODEL.equalsIgnoreCase("kt45q"))||(android.os.Build.MODEL.equalsIgnoreCase("kt50"))){
	//			CtrlFile.write("-wdout93 1");//45q  50
	//		}else if(android.os.Build.MODEL.equalsIgnoreCase("kt80")) {
	//			CtrlFile.write("-wdout94 1");//80
	//		} else {
				CtrlFile.write("-wdout93 1");//临时版本
            CtrlFile.flush();
				CtrlFile.write("-wdout67 1");//临时版本
            CtrlFile.flush();
	//		}
		}
		else if (android.os.Build.VERSION.RELEASE.equals("4.4.2"))
			CtrlFile.write("-wdout67 1");//临时版本
		CtrlFile.flush();
	//		CtrlFile.write("-wdout106 1");
			CtrlFile.write("-wdout93 1");
		CtrlFile.flush();
	}
	
	public void PowerOffDevice() throws IOException        //poweroff id device
	{
		if (android.os.Build.VERSION.RELEASE.equals("5.1")){
	//		if((android.os.Build.MODEL.equalsIgnoreCase("kt45q"))||(android.os.Build.MODEL.equalsIgnoreCase("kt50"))){
	//			CtrlFile.write("-wdout93 0");//45q  50
	//		}else if(android.os.Build.MODEL.equalsIgnoreCase("kt80")) {
	//			CtrlFile.write("-wdout94 0");//80
	//		}else {
				CtrlFile.write("-wdout93 0");//临时版本
            CtrlFile.flush();
				CtrlFile.write("-wdout67 0");//临时版本
            CtrlFile.flush();
	//		}
		}
		else if (android.os.Build.VERSION.RELEASE.equals("4.4.2"))
			CtrlFile.write("-wdout67 0");//临时版本
		CtrlFile.flush();
			CtrlFile.write("-wdout93 0");
	//		CtrlFile.write("-wdout106 0");
  		CtrlFile.flush();
	}
	public void DeviceClose() throws IOException        //close file
	{
		CtrlFile.close();
	}
}