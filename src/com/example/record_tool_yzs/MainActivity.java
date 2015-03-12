package com.example.record_tool_yzs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	private Button btn_start;
	private EditText edit_name;
	private String commandsPath = Environment.getExternalStorageDirectory().getPath()+"/usc_record/command_path/";
	private Spinner spinner;
	private List<String> command_file;
	private ArrayAdapter<String> adapter;
	private RadioButton btn_yes;
	private RadioButton btn_no;
	private RadioGroup group;
	private String command_file_name = "";
	private LinearLayout linear;
	
	private EditText general;
	private EditText poi;
	private EditText wakeup;
	private boolean isSelect = false;
	private boolean isFileExist = true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn_start = (Button)findViewById(R.id.btn_start);
		btn_start.setOnClickListener(this);
		edit_name = (EditText)findViewById(R.id.edit_name);
		spinner = (Spinner)findViewById(R.id.spinner);
		group = (RadioGroup)findViewById(R.id.group);
		group.setOnCheckedChangeListener(new MyCheckedChangeListener());
		linear = (LinearLayout)findViewById(R.id.command_bi);
		
		general = (EditText)findViewById(R.id.general_num);
		poi = (EditText)findViewById(R.id.poi_num);
		wakeup = (EditText)findViewById(R.id.wakeup_num);
		
		command_file = new ArrayList<String>();
		
		File file = new File(commandsPath);
		String filename = "";
		if (file.exists()) {
			isFileExist = true;
			File[] files = file.listFiles();//得到commands文件下的所有文件
			for (File file2 : files) {
				filename += file2.getName()+";;;;;";
				command_file.add(file2.getName().toString());
			}
//			Toast.makeText(this, "filename : " + filename, Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(this, "command 文件不存在", Toast.LENGTH_LONG).show();
			isFileExist = false;
		}
		if (command_file.size() > 0) {
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,command_file);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}
		if (adapter != null) {
			spinner.setAdapter(adapter);
		}
		spinner.setOnItemSelectedListener(new MyItemListener());
	}

	class MyItemListener implements OnItemSelectedListener{//下拉菜单的监听

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if (isFileExist) {
				command_file_name = command_file.get(arg2);
			}else{
				Toast.makeText(MainActivity.this, "命令文件为空！", Toast.LENGTH_LONG).show();
			}
			
//			Toast.makeText(MainActivity.this, "你选择的是 : " + command_file.get(arg2), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class MyCheckedChangeListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			// TODO Auto-generated method stub
			String result = "";
			switch (arg1) {
			case R.id.radiobutton_1:
				result = "yes";
				linear.setVisibility(View.VISIBLE);
				isSelect = true;
				break;
			case R.id.radiobutton_2:
				result = "no";
				linear.setVisibility(View.INVISIBLE);
				isSelect = false;
				break;

			default:
				break;
			}
			
//			Toast.makeText(MainActivity.this, "ni xuan:" + result, Toast.LENGTH_LONG).show();
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_start:
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			String edit_content = edit_name.getText().toString();
			
			String general_l = general.getText().toString();
			String poi_l = poi.getText().toString();
			String wakeup_l = wakeup.getText().toString();
			if (isSelect) {
				bundle.putString("general", general.getText().toString());
				bundle.putString("poi", poi.getText().toString());
				bundle.putString("wakeup", wakeup.getText().toString());
			}
			
			if (edit_content != null && !edit_content.equals("")) {
				bundle.putBoolean("isSelect", isSelect);
				bundle.putString("file_name",edit_content );
				bundle.putString("command_file_name", command_file_name);
				intent.putExtra("bundle", bundle);
				intent.setClass(MainActivity.this, Record_main.class);
				if (isFileExist) {
					startActivity(intent);
				}else{
					Toast.makeText(this, "命令文件为空！请放入文件在指定路径！", Toast.LENGTH_LONG).show();
				}
				
			}else{
				Toast.makeText(this, "输入的名称为空！请重新输入！", Toast.LENGTH_LONG).show();
			}
				
			break;

		default:
			break;
		}
	}

}
