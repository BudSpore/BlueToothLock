package com.ping.myapp32.chengke;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * 蓝牙客户端连接线程,这里开了3个线程
 *
 */
public class BluetoothClientConnThread extends Thread{

	private Handler serviceHandler;		//用于向客户端Service回传消息的handler
	private BluetoothDevice serverDevice;	//服务器设备
	private BluetoothSocket socket;		//通信Socket
	private readThread mreadThread = null;
	/**
	 * 构造函数
	 * @param handler
	 * @param serverDevice
	 */
	public BluetoothClientConnThread(Handler handler, BluetoothDevice serverDevice) {
		this.serviceHandler = handler;
		this.serverDevice = serverDevice;
	}
	//#4CAF50"    #2E7D32
	//读取数据 android:centerColor="#4CAF50"
	//android:endColor="#81C784"
	private class readThread extends Thread {

		public void run() {

			byte[] buffer = new byte[1024];
			int bytes;
			InputStream mmInStream = null;

			try {
				mmInStream = socket.getInputStream();
			} catch (IOException e1) {
				Log.d("error","data receive failed");
				Log.d("read", "不能读取数据");
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (true) {
				try {
					// Read from the InputStream
					if( (bytes = mmInStream.read(buffer)) > 0 )
					{
						byte[] buf_data = new byte[bytes];
						for(int i=0; i<bytes; i++)
						{
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						Message msg = serviceHandler.obtainMessage();
						msg.what = BluetoothTools.MESSAGE_READ_OBJECT;//给CommunThread.sendMessageHandle(data);不断传data

						Bundle bundle = new Bundle();
						bundle.putString("Id", s);
						msg.setData(bundle);
						msg.sendToTarget();
					}
				} catch (IOException e) {
					try {
						Log.d("read", "不能读取数据");

						mmInStream.close();

					} catch (IOException e1) {
						Log.d("error","data receive failed");
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}
	@Override
	public void run() {
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		try {
			Method method = serverDevice.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
			socket = (BluetoothSocket) method.invoke(serverDevice, Integer.valueOf(1));

			//socket = serverDevice.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
			Log.d("ACTION_SELECTED_DEVICE", "选中第一个设备,进入Client_Service,又进入线程处理");
			socket.connect();

			mreadThread = new readThread();
			mreadThread.start();
		} catch (Exception ex) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//发送连接失败消息
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			Log.d("MESSAGE_CONNECT_ERROR", "连接FAIL");
			return;
		}

		//发送连接成功消息，消息的obj参数为连接的socket
		//这一部分用于开启BluetoothCommunThread线程，并且调用sendMessageHandle(data)
		Message msg = serviceHandler.obtainMessage();
		//Message msg = new Message();
		msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
		msg.obj = socket;
		msg.sendToTarget();

		Log.d("MESSAGE_CONNECT_SUCCESS", "socket连接success");
	}
}
