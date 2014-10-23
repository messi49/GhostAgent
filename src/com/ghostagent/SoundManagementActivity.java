package com.ghostagent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import com.design.DrawCenterView;
import com.design.DrawLeftView;
import com.design.DrawRightView;
import com.ghostagent.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SoundManagementActivity extends Activity implements OnClickListener{
	final static int SAMPLING_RATE = 16000;
	AudioRecord audioRec = null;
	Button btn = null;
	boolean bIsRecording = false;
	boolean bIsTransferring = false;
	int bufSize;

	// file pointer
	FileOutputStream out;

	File recFile;

	//address and port
	String address;
	int port;

	// view size
	public static int viewWidth;
	public static int viewHeight;
	// handler
	Handler  mHandler   = new Handler();
	DrawLeftView drawLeftView;
	DrawRightView drawRightView;
	DrawCenterView drawCenterView;

	public static boolean getSizeFlag = false;

	//image button
	ImageButton vision, voice, s1, s2, p1, p2, p3, p4, autoCruise, normalCruise, pursuit;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.knight_rider);

		// Calculation of the buffer size
		bufSize = AudioRecord.getMinBufferSize(
				SAMPLING_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT) * 2;
		// Creating AudioRecord
		audioRec = new AudioRecord(
				MediaRecorder.AudioSource.MIC, 
				SAMPLING_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				bufSize);

		// get parameters
		address = getIntent().getExtras().getString("address");
		port = getIntent().getExtras().getInt("port");

		Log.v("Log", "address: " + address + ", port: " + port);
		if(address.length() == 0){
			//address = "192.168.2.227";
			address = "192.168.0.3";
		}

		if(port == 0){
			port = 52345;
		}

		//connect server
		SoundManagementNative.connectServer(address, port);
		try{
			Thread.sleep(20); //20ミリ秒Sleepする
		}catch(InterruptedException e){}
		
		//SoundManagementNative.connectServer(address, 30003);

		// center expression
		drawLeftView = (DrawLeftView) findViewById(R.id.leftView);
		drawRightView = (DrawRightView) findViewById(R.id.rightView);
		drawCenterView = (DrawCenterView) findViewById(R.id.centerView);

		bIsRecording = true;

		// set buttons
		vision = (ImageButton)findViewById(R.id.air);
		findViewById(R.id.air).setOnClickListener(this);

		voice = (ImageButton)findViewById(R.id.oil);
		findViewById(R.id.oil).setOnClickListener(this);

		s1 = (ImageButton)findViewById(R.id.s1);
		findViewById(R.id.s1).setOnClickListener(this);

		s2 = (ImageButton)findViewById(R.id.s2);
		findViewById(R.id.s2).setOnClickListener(this);

		p1 = (ImageButton)findViewById(R.id.p1);
		findViewById(R.id.p1).setOnClickListener(this);

		p2 = (ImageButton)findViewById(R.id.p2);
		findViewById(R.id.p2).setOnClickListener(this);

		p3 = (ImageButton)findViewById(R.id.p3);
		findViewById(R.id.p3).setOnClickListener(this);

		p4 = (ImageButton)findViewById(R.id.p4);
		findViewById(R.id.p4).setOnClickListener(this);

		autoCruise = (ImageButton)findViewById(R.id.autoCruise);
		findViewById(R.id.autoCruise).setOnClickListener(this);

		normalCruise = (ImageButton)findViewById(R.id.normalCruise);
		findViewById(R.id.normalCruise).setOnClickListener(this);

		pursuit = (ImageButton)findViewById(R.id.pursuit);
		findViewById(R.id.pursuit).setOnClickListener(this);

		// Start Recoding
		Log.v("AudioRecord", "startRecording");

		// get file path
		recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.raw");
		Log.v("File", Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.raw");

		try {
			recFile.createNewFile();
			// write file
			out = new FileOutputStream(recFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		bIsRecording = true;

		// start recording
		startRecoding();

	}

	public void startRecoding(){
		audioRec.startRecording();
		// Recoding Thread
		new Thread(new Runnable() {
			public void run() {
				byte buf[] = new byte[bufSize];
				int counter = 0;

				while (bIsRecording) {
					// Read recoding data
					audioRec.read(buf, 0, buf.length);							
					//Log.v("AudioRecord", "read " + buf.length + " bytes");

					// change byte order
					//			        ByteBuffer buffer = ByteBuffer.allocate(buf.length);
					//			        buffer.put(buf);
					//
					//			        buffer.order(ByteOrder.BIG_ENDIAN);
					//			        
					//			        buf = buffer.array();

					//send sound data 
					if(bIsTransferring == true)
						SoundManagementNative.sendSoundData(buf, buf.length);

					/*-------design------*/
					//Randomクラスのインスタンス�?
					Random rnd = new Random();

					drawLeftView.drawView(rnd.nextInt(10));
					drawRightView.drawView(rnd.nextInt(10));
					drawCenterView.drawView(rnd.nextInt(14));
					/*-------design------*/

					/*
					try {
						out.write(buf);
					} catch (IOException e) {
						e.printStackTrace();
					}
					 */
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v("AudioRecord", "onDestroy");
		bIsRecording = false;

		// close file pointer
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		try {
			//change the file format (raw -> wav)
			addWavHeader();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 */

		// Stop Recoding
		Log.v("AudioRecord", "stop");
		audioRec.stop();

		// release
		audioRec.release();

		// disconnected
		SoundManagementNative.closeConnect();

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// 表示と同時にウィジェ�?��の高さ�?�?��取得したいとき�?大抵ここで取る�?
		if (hasFocus) {
			viewWidth = drawLeftView.getWidth();
			viewHeight = drawLeftView.getHeight();

			getSizeFlag = true;

			Log.v("View", "width: " + viewWidth + ", height: " + viewHeight);
		}
		super.onWindowFocusChanged(hasFocus);
	}



	//Save as a WAV header
	public void addWavHeader() throws IOException {
		// The recorded file
		File recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.raw");
		// WAV file
		File wavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.wav");

		wavFile.createNewFile();


		// stream
		FileInputStream in = new FileInputStream(recFile);
		FileOutputStream outStream = new FileOutputStream(wavFile);

		// create header
		byte[] header = createHeader(SAMPLING_RATE, (int)recFile.length());
		// write header
		outStream.write(header);

		// read byte data 
		int n = 0,offset = 0;
		byte[] buffer = new byte[(int)recFile.length()];
		while (offset < buffer.length && (n = in.read(buffer, offset, buffer.length - offset)) >= 0) {
			offset += n;
		}
		// write byte data
		outStream.write(buffer);

		// end
		in.close();
		outStream.close();
	}

	//create header of WAV
	public static byte[] createHeader(int sampleRate, int datasize) {
		byte[] byteRIFF = {'R', 'I', 'F', 'F'};
		byte[] byteFilesizeSub8 = intToBytes((datasize + 36));  // file size -8 byte
		byte[] byteWAVE = {'W', 'A', 'V', 'E'};
		byte[] byteFMT_ = {'f', 'm', 't', ' '};
		byte[] byte16bit = intToBytes(16); // fmt chank
		byte[] byteSamplerate = intToBytes(sampleRate);  // sample rate
		byte[] byteBytesPerSec = intToBytes(sampleRate * 2); // byte / seconds = sample rate x 1 channel x 2byte
		byte[] bytePcmMono = {0x01, 0x00, 0x01, 0x00}; // format ID 1 = LPCM , channel 1 = MONORAL
		byte[] byteBlockBit = {0x02, 0x00, 0x10, 0x00}; // Block size 2byte 
		byte[] byteDATA = {'d', 'a', 't', 'a'};
		byte[] byteDatasize = intToBytes(datasize);  // data size

		ByteArrayOutputStream outArray = new ByteArrayOutputStream();
		try {
			outArray.write(byteRIFF);
			outArray.write(byteFilesizeSub8);
			outArray.write(byteWAVE);
			outArray.write(byteFMT_);
			outArray.write(byte16bit);
			outArray.write(bytePcmMono);
			outArray.write(byteSamplerate);
			outArray.write(byteBytesPerSec);
			outArray.write(byteBlockBit);
			outArray.write(byteDATA);
			outArray.write(byteDatasize);
		} catch (IOException e) {
			return outArray.toByteArray();
		}
		return outArray.toByteArray();
	}
	//make change little endian
	public static byte[] intToBytes(int value) {
		byte[] bt = new byte[4];
		bt[0] = (byte)(value & 0x000000ff);
		bt[1] = (byte)((value & 0x0000ff00) >> 8);
		bt[2] = (byte)((value & 0x00ff0000) >> 16);
		bt[3] = (byte)((value & 0xff000000) >> 24);
		return bt;
	}

	/*
	s1: 14
	s2: 15
	p1: 18
	p2: 19
	p3: 20
	p4: 21
	 */

	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		if(v == vision){
			Log.v("Button", "vision");
			SoundManagementNative.sendCommand(1, 5);
		}
		else if(v == voice){
			Log.v("Button", "voice");
			bIsTransferring = !bIsTransferring;
			Log.v("bIsTransferring", "bIsTransferring: " + bIsTransferring);

		}
		else if(v == s1){
			Log.v("Button", "s1");
			SoundManagementNative.sendCommand(1, 14);

		}
		else if(v == s2){
			Log.v("Button", "s2");
			SoundManagementNative.sendCommand(1, 15);

		}
		else if(v == p1){
			Log.v("Button", "p1");
			SoundManagementNative.sendCommand(1, 18);

		}
		else if(v == p2){
			Log.v("Button", "p2");
			SoundManagementNative.sendCommand(1, 19);

		}
		else if(v == p3){
			Log.v("Button", "p3");
			SoundManagementNative.sendCommand(1, 20);

		}
		else if(v == p4){
			Log.v("Button", "p4");
			SoundManagementNative.sendCommand(1, 21);
		}
		else if(v == autoCruise){
			Log.v("Button", "autoCruise");

		}
		else if(v == normalCruise){
			Log.v("Button", "normalCruise");
			// パッケージ名, クラス名をセット
			intent = getIntent();
			intent.setClassName("com.andrive", "com.andrive.StartActivity");
			// Activity以外からActivityを呼ぶためのフラグ
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			// アプリを起動
			startActivity(intent);

		}
		else if(v == pursuit){
			Log.v("Button", "pursuit");

		}

	}
}