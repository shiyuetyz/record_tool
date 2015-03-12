package com.example.record_tool_yzs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Record_main extends Activity implements OnClickListener {
	private Button btn_start;
	private Button btn_stop;
	private Button btn_before;
	private TextView text_content;
	private Intent intent;
	private String file_name = "";
	private String commandsDir = Environment.getExternalStorageDirectory()
			.getPath() + "/usc_record/";

	private String command_file_name = "";
	private static int bufferSizeInBytes;
	private AudioRecord audioRecord = null;
	protected static int CHANNEL = AudioFormat.CHANNEL_IN_MONO; // .CHANNEL_CONFIGURATION_MONO;
	protected static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	protected static int frequency = 16000; // 16000,8000
	protected static final int k = (16000 * 150) / 1000; // 100ms
	private byte[] buffer = new byte[k >> 1];// 37.5ms 1200bytes
	private boolean isRecord = false;
	private int index = 0;
	private List<String> list = null;
	private ProgressBar bar;
	private int retrue_num = 0;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
		}
	};
	

	private int general = 0;
	private int poi = 0;
	private int wakeup = 0;
	private int general_w = 1;
	private int poi_w = 1;
	private int wakeup_w = 1;
	private boolean isSelect = false;
	private int command_number = 0;
	private boolean isChange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_main);
		intent = getIntent();
		Bundle bundle = intent.getBundleExtra("bundle");
		file_name = bundle.getString("file_name");
		command_file_name = bundle.getString("command_file_name");
		isSelect = bundle.getBoolean("isSelect");
		
		if (isSelect) {
			String general_s = bundle.getString("general").trim().toString();
			String poi_s = bundle.getString("poi").trim().toString();
			String wakeup_s = bundle.getString("wakeup").trim().toString();
			
			if (general_s != null && !general_s.equals("")) {
				general = Integer.valueOf(general_s);
			}
			if (poi_s != null && !poi_s.equals("")) {
				poi = Integer.valueOf(poi_s);
			}
			if (wakeup_s != null && !wakeup_s.equals("")) {
				wakeup = Integer.valueOf(wakeup_s);
			}
			command_number = general + poi + wakeup;
		}

//		Toast.makeText(this, "Bundle content:" + bundle.getString("file_name"),
//				Toast.LENGTH_SHORT).show();
		text_content = (TextView) findViewById(R.id.text_headling);

		btn_start = (Button) findViewById(R.id.btn_record_start);
		btn_stop = (Button) findViewById(R.id.btn_record_stop);
		btn_before = (Button) findViewById(R.id.btn_record_before);
		btn_start.setOnClickListener(this);
		btn_stop.setOnClickListener(this);
		btn_before.setOnClickListener(this);
		bar = (ProgressBar)findViewById(R.id.myProgressBar);
		list = new ArrayList<String>();
		readCommandByFile("");// 读取commands form file
		text_content.setText(list.get(index));
		createRecord();
		writeLinkTextAndPcm(commandsDir + "pcm_path/" + file_name+"/");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_record_start:
			if (!isRecord) {
				if(list.size() <= index){
					
				}else{
					start_record();
					btn_start.setText("说完了！");
				}
				
				
			}else{
				if (list.size() > index) {
					text_content.setText(list.get(index));
					close();
				} else {
					Toast.makeText(this, "已经没有commands了!", Toast.LENGTH_SHORT)
							.show();
					close();
				}
			}

			break;
		case R.id.btn_record_stop:
			if (isRecord && list.size() > index) {
				text_content.setText(list.get(index));
				close();
			} else {
				Toast.makeText(this, "已经没有commands了!", Toast.LENGTH_SHORT)
						.show();
				close();
			}
			break;
		case R.id.btn_record_before:
			do_before();
			break;

		default:
			break;
		}
	}

	private void setProgressForBar(){
		bar.setProgress((int) (Math.random() * 100));
	}
	public class MyAsyncTsak extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}

	public void createRecord() {
		bufferSizeInBytes = (k * 4 * 16 * 1) / 8; // 200ms = 6400bytes
		int bufferSize = AudioRecord.getMinBufferSize(frequency, CHANNEL,
				ENCODING);
		if (bufferSizeInBytes < bufferSize) {
			bufferSizeInBytes = bufferSize;
		}

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
				frequency, CHANNEL, ENCODING, bufferSizeInBytes);

	}

	public void start_record() {

		if (list.size() > index) {
			isRecord = true;
			index++;
			if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				audioRecord.startRecording();
				bar.setVisibility(View.VISIBLE);
				retrue_num = 0;
				
			}
			new Thread(new RecordDataThread()).start();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(true){
						setProgressForBar();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
		} else {
			Toast.makeText(this, "已经没有commands了!", Toast.LENGTH_SHORT).show();
			close();

		}

	}

	public void close() {
		if (audioRecord != null) {

			if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				audioRecord.stop();
				bar.setVisibility(View.INVISIBLE);
			}
			isRecord = false;
			// audioRecord.release();
			// audioRecord = null;
		}
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				btn_start.setText("开始录音");
			}
		});
		
	}

	private void do_before() {
		if (!isSelect) {
			if (index > 0) {
				retrue_num ++;
				if (retrue_num == 1) {
					index--;
					text_content.setText(list.get(index));
				}else{
					Toast.makeText(this, "已经重录的一条了！", Toast.LENGTH_SHORT).show();
				}
				
			}else{
				Toast.makeText(this, "已经是第一条了！", Toast.LENGTH_SHORT).show();
			}
			
		}else{
			if (command_number == list.size()) {
				if (general >= general_w) {
					
					general_w --;
					
				}
				if ( general_w > general  && poi >= poi_w ) {
					if (general + 1 == general_w) {
						general_w --;
					}else{
						if (poi_w > 1) {
							poi_w --;
						}else{
							Toast.makeText(this, "已经是第一条了！", Toast.LENGTH_SHORT).show();
						}
						
					}
					
				}
				if (general_w > general && poi_w > poi && wakeup >= wakeup_w) {
					if (poi + 1 == poi_w) {
						poi_w -- ;
					}else{
						if (wakeup_w > 1) {
							wakeup_w --;
						}else{
							Toast.makeText(this, "已经是第一条了！", Toast.LENGTH_SHORT).show();
						}
					}
					
					
				}
			}else{
				
				if (index > 1) {
					index--;
				}else{
					Toast.makeText(this, "已经是第一条了！", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
	}

	class RecordDataThread implements Runnable {
		@Override
		public void run() {
			if (isSelect) {
				if (command_number == list.size()) {
					if (general >= general_w && general > 0) {
						writePcmFile(general_w,commandsDir + "pcm_path/" + file_name
								+ "/general/");
						
						general_w ++;
						
					}
					if ( general_w > general  && poi >= poi_w && poi > 0) {
						if (general == 0) {
							writePcmFile(poi_w,commandsDir + "pcm_path/" + file_name
									+ "/poi/");
							poi_w ++;
							general = -1;
						}else{
							if (general + 1 == general_w) {
								general_w ++;
							}else{
								writePcmFile(poi_w,commandsDir + "pcm_path/" + file_name
										+ "/poi/");
								poi_w ++;
							}
						}
						
						
					}
					if (general_w > general && poi_w > poi && wakeup >= wakeup_w && wakeup > 0) {
						
						if (poi == 0 || general == 0) {
							writePcmFile(wakeup_w,commandsDir + "pcm_path/" + file_name
									+ "/poi/");
							wakeup_w ++;
							poi_w = -1;
							general_w = -1;
						}else{
							if (poi + 1 == poi_w || general + 1 == general_w) {
								poi_w ++ ;
								general_w ++;
							}else{
								writePcmFile(wakeup_w,commandsDir + "pcm_path/" + file_name
										+ "/wakeup/");
								wakeup_w ++;
							}
						}
						
//						if (general == 0) {
//							writePcmFile(poi_w,commandsDir + "pcm_path/" + file_name
//									+ "/poi/");
//							poi_w ++;
//							general = -1;
//						}else{
//							if (general + 1 == general_w) {
//								general_w ++;
//							}else{
//								writePcmFile(poi_w,commandsDir + "pcm_path/" + file_name
//										+ "/poi/");
//								poi_w ++;
//							}
//						}
						
					}
				}else{
					if (index > 0) {
						writePcmFile(index,commandsDir + "pcm_path/" + file_name + "/");
					}
					
				}
				
			} else {
				if (index > 0) {
					writePcmFile(index,commandsDir + "pcm_path/" + file_name + "/");
					
				}
			}

		}
	}

	public void writePcmFile(int index_w,String recordingDir) {
		File file = null;
		file = new File(recordingDir, generateFileName(index_w,"pcm"));

		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		FileOutputStream os = null;
		BufferedOutputStream bos = null;
		try {
			os = new FileOutputStream(file, false);
			bos = new BufferedOutputStream(os);
			// write data stream
			while (isRecord) {
				int read = audioRecord.read(buffer, 0, buffer.length);
				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					bos.write(buffer, 0, read);
				}
			}
			Log.d("RecognitionThread",
					"writePcmFile: " + file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String generateFileName(int index_,String ex) {
		return file_name + "_" + indexChange(index_) + "." + ex;
	}

	private String indexChange(int index_f) {
		String indexString = "";
		int[] indexs = { 0, 0, 0, 0 };
		if (index_f < 10) {
			indexs[3] = index_f;
		} else if (index_f >= 10 && index_f < 100) {
			int ge = index_f % 10;
			int shi = index_f / 10;
			indexs[3] = ge;
			indexs[2] = shi;
		} else if (index_f >= 100 && index_f < 1000) {
			int ge = index_f % 10;
			int shi = (index_f / 10) % 10;
			int bai = index_f / 100;
			indexs[3] = ge;
			indexs[2] = shi;
			indexs[1] = bai;
		} else if (index_f >= 1000 && index_f < 10000) {
			int ge = index_f % 10;
			int shi = (index_f / 10) % 10;
			int bai = (index_f / 100) % 10;
			int qian = index_f / 1000;
			indexs[3] = ge;
			indexs[2] = shi;
			indexs[1] = bai;
			indexs[0] = qian;
		}
		for (int i : indexs) {
			indexString += i;
		}
		return indexString;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (audioRecord != null) {
			if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				audioRecord.stop();
			}
			isRecord = false;
			audioRecord.release();
			audioRecord = null;
		}
	}

	private void readCommandByFile(String Commands_path) {
		File file = new File(commandsDir + "command_path/" + command_file_name);
		FileReader fr = null;
		BufferedReader bf = null;
		String str = "";
		try {
			if (file.exists()) {
				fr = new FileReader(file);
				bf = new BufferedReader(fr);
				while ((str = bf.readLine()) != null) {
					list.add(str);
				}
			} else {
				Toast.makeText(this, "命令路径不存在,请检查!", Toast.LENGTH_SHORT).show();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fr = null;
			}
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				bf = null;
			}
		}

		// if (file.exists()) {
		// try {
		// intput = new FileInputStream(file);
		//
		// int c;
		// byte buffer[] = new byte[1024];// 定义一个byte数组
		// while ((c = intput.read(buffer)) != -1) {// 从文件的输入流中读取数据
		//
		// list.add(buffer.toString());
		// }
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }else{
		// Toast.makeText(this, "命令路径不存在,请检查!", Toast.LENGTH_SHORT).show();
		// }
	}
	
	private void writeLinkTextAndPcm(String recordingDir){
		File file = null;
		file = new File(recordingDir, "text.txt");

		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		FileOutputStream os = null;
		BufferedOutputStream bos = null;
		try {
			os = new FileOutputStream(file, false);
			bos = new BufferedOutputStream(os);
			// write data stream
			for(int i = 0; i < list.size(); i++){
				bos.write(generateFileName(i+1,"pcm").getBytes());
				bos.write("\t".getBytes());
				bos.write(list.get(i).getBytes());
				bos.write("\n".getBytes());
			}
			Log.d("RecognitionThread",
					"writePcmFile: " + file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}