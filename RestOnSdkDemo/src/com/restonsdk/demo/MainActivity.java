package com.restonsdk.demo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.medica.jni.SleepAnalysis_Out;
import com.medica.jni.SleepHelperJni;
import com.medica.restonsdk.Constants;
import com.medica.restonsdk.bluetooth.RestOnHelper;
import com.medica.restonsdk.domain.BleDevice;
import com.medica.restonsdk.domain.Detail;
import com.medica.restonsdk.domain.RealTimeData;
import com.medica.restonsdk.domain.Summary;
import com.medica.restonsdk.interfs.BleStateChangedListener;
import com.medica.restonsdk.interfs.Method;
import com.medica.restonsdk.interfs.RawDataCallback;
import com.medica.restonsdk.interfs.RealtimeDataCallback;
import com.medica.restonsdk.interfs.ResultCallback;
import com.medica.restonsdk.interfs.UpgradeCallback;

public class MainActivity extends Activity{
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static  String[] ITEMS ;
	
	private GridView gridView;
	private GridAdapter adapter;
	
	private RestOnHelper restonHelper;
	private BleDevice selectedBleDevice;
	
	private static final int REQCODE_OPEN_BT = 1;
	private static final int REQCODE_SEACH_DEVICE = 2;
	
	public static final int userId = 1;
	
	private Summary summary;
	private Detail detail;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * { "搜索设备", "连接设备", "获取设备ID", "登录设备", "设备状态", "获取电量",  "开始采集", "查看实时数据",
		"查看原始数据", "关闭实时数据", "关闭原始数据", "停止采集", "查询概要信息", "查询详细信息", "睡眠分析", "关闭低电量警告", "设置自动监测", "设置智能闹钟", 
"当前版本", "固件升级", "登出设备", "断开连接"};
		 */
		ITEMS = new String[]{
				this.getString(R.string.device_scan),this.getString(R.string.device_conn),this.getString(R.string.get_device_id),this.getString(R.string.login_device),this.getString(R.string.device_state),this.getString(R.string.device_power),this.getString(R.string.start_collect
				),this.getString(R.string.realtime_data),this.getString(R.string.original_data),this.getString(R.string.stop_realtime_data),this.getString(R.string.stop_original_data),this.getString(R.string.stop_collect),
				this.getString(R.string.summary_data),this.getString(R.string.detail_data
				),this.getString(R.string.analysis),this.getString(R.string.stop_power_warn),this.getString(R.string.auto_monitor_config),this.getString(R.string.smart_clock_config),this.getString(R.string.current_version),this.getString(R.string.device_upgrade),this.getString(R.string.disconnect_device)
			};
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		restonHelper = RestOnHelper.getInstance(this);
		restonHelper.addBleStateChangedListener(bleStateChangedListener);
		
		IntentFilter filter = new IntentFilter(RestOnHelper.ACTION_LOW_POWER);
		registerReceiver(lowPowerReceiver, filter);
		
		gridView = (GridView) findViewById(R.id.gridview);
		adapter = new GridAdapter();
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(itemClickListener);
	}
	
	
	private final OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
//			LogUtil.showMsg(TAG+" onItemClick pos:"+position);
			if(!restonHelper.isBluetoothOpen()){
				
				Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enabler,REQCODE_OPEN_BT);
				return;
			}
			
			if(position == 0){
				Intent intent = new Intent(MainActivity.this, SearchDeviceActivity.class);
				startActivityForResult(intent, REQCODE_SEACH_DEVICE);
			}else{
				
				/*if(selectedBleDevice == null){
					Toast.makeText(MainActivity.this, "请先选择设备", Toast.LENGTH_SHORT).show();
					return;
				}*/
				
				if(position == 1){
					restonHelper.connDevice(selectedBleDevice, resultCallback);
				}else if(position == 2){
					restonHelper.getDeviceId(resultCallback);
				}else if(position == 3){
					restonHelper.loginDevice(selectedBleDevice, userId, resultCallback);
				}else if(position == 4){
					restonHelper.getDeviceStatus(resultCallback);
				}else if(position == 5){
					restonHelper.getDevicePower(resultCallback);
				}else if(position == 6){
					restonHelper.startCollect(resultCallback);
				}else if(position == 7){
					restonHelper.seeRealtimeData(realtimeDataCallback);
				}else if(position == 8){
					restonHelper.seeRawData(rawDataCallback);
				}else if(position == 9){
					restonHelper.closeRealtimeData(resultCallback);
				}else if(position == 10){
					restonHelper.closeRawData(resultCallback);
				}else if(position == 11){
					restonHelper.stopCollect(resultCallback);
				}else if(position == 12){
					Calendar calendar = Calendar.getInstance();
					int endTime = (int) (calendar.getTimeInMillis() / 1000);
					calendar.add(Calendar.DAY_OF_MONTH, -100);
					int startTime = (int) (calendar.getTimeInMillis() / 1000);
					LogUtil.showMsg(TAG+" queryHistorySummary stime:" + dateFormat.format(new Date(startTime * 1000l))+
							",etime:"+ dateFormat.format(new Date(endTime * 1000l)));
					restonHelper.queryHistorySummary(startTime, endTime, resultCallback);
				}else if(position == 13){
					restonHelper.queryHistoryDetail(summary, resultCallback);
				}else if(position == 14){
					if(summary != null && detail != null){
						SleepAnalysis_Out analysis = SleepHelperJni.analysis(summary.startTime, (int)(summary.timezone * 60 * 60), 1, 60, summary.recordCount, detail.breathRate, detail.heartRate, detail.status, detail.statusValue, Constants.DEVICE_TYPE_RESTON_Z2);
						int score = analysis == null ? -1 : analysis.sleepScore;
						LogUtil.showMsg(TAG+" analysis score:" + score);
					}
				}else if(position == 15){//stop power warning
					restonHelper.closeLowPowerWarn(resultCallback);
				}else if(position == 16){//auto monitor
					byte enable = 1;
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, 1);
					byte hour = (byte) cal.get(Calendar.HOUR_OF_DAY);
					byte minute = (byte) cal.get(Calendar.MINUTE);
					byte repeat = 127;
					LogUtil.showMsg(TAG+" setAutoStart h:" + hour+",m:" + minute);
					restonHelper.setAutoStart(enable, hour, minute, repeat, resultCallback);
				}else if(position == 17){//smart alarm clock
					byte alartOFF = 1;
					byte autoGet = 1;
					byte autoMove = 30;
					byte hourOfDay = 8;
					byte minute = 0;
					byte repeat = 0;
					restonHelper.setAlarmTime(alartOFF, autoGet, autoMove, hourOfDay, minute, repeat, resultCallback);
				}else if(position == 18){//current version
					restonHelper.getDeviceVersion(resultCallback);
				}else if(position == 19){//upgrade
					/*File file = new File("/storage/emulated/0/b.des");
					long crcDes= 2473374035L;
					long crcBin= 404487962;
					restonHelper.upgradeFirmware(Constants.DEVICE_TYPE_RESTON_Z2,1.17f, (int)crcBin, (int)crcDes, file, upgradeCallback);                    
                    
                    */
				}else if(position == 20){//logout
					restonHelper.logout(resultCallback);
				}else if(position == 21){//disconnect
					restonHelper.disconnect();
				}
			}
		}
	};
	
	
	private final ResultCallback resultCallback = new ResultCallback() {
		@Override
		public void onResult(final Method method, final Object result) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				public void run() {
					LogUtil.showMsg(TAG+" onResult m:"+ method+",result:"+ result);
					
					if(method == Method.CONNECT_DEVICE){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.conn_succ), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_ID){
						String deviceId = (String) result;
						if(deviceId == null){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}else{
							selectedBleDevice.deviceId = deviceId;
						}
					}
					
					else if(method == Method.LOGIN){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.login_succ), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_STATUS){
						Integer res = (Integer) result;
						if(res == -1){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}else if(res == 0){
							Toast.makeText(MainActivity.this,  MainActivity.this.getString(R.string.status_uncollect), Toast.LENGTH_SHORT).show();
						}else if(res == 1){
							Toast.makeText(MainActivity.this,  MainActivity.this.getString(R.string.status_collecting), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_POWER){//电量低于10%时，会有低电量警告
						Integer res = (Integer) result;
						if(res == -1){
							Toast.makeText(MainActivity.this,  MainActivity.this.getString(R.string.get_power_fail), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.power_avail) + res + "%", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.START_COLLECT){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.device_collecting), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.CLOSE_REALTIME_DATA){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.realtime_closed), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.CLOSE_RAW_DATA){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.Raw_data_closed), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.STOP_COLLECT){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.collect_stoped), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.opt_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.QUERY_HISTORY_SUMMARY){
						if(result != null){
							ArrayList<Summary> list = (ArrayList<Summary>) result;
							int size = list.size();
							LogUtil.showMsg(TAG+" query summary size:" + size);
							if(size > 0){
								summary = list.get(0);
							}
						}
					}
					
					else if(method == Method.QUERY_HISTORY_DETAIL){
						if(result != null){
							detail = (Detail) result;
							LogUtil.showMsg(TAG+" query detail:" + detail.status.length);
						}
					}
					
					else if(method == Method.CLOSE_LOWPOWER_WARNING){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.power_warning_stoped), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.power_warning_stop_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.SET_AUTO_START){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.config_succ), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.config_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.SET_ALARM_TIME){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.config_succ), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.config_fail), Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_VERSION){
						if(result!=null){
							String ver = result.toString();
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.current_version) + ver, Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this,  MainActivity.this.getString(R.string.get_version_fail), Toast.LENGTH_SHORT).show();
						}
					}else if(method == Method.LOGOUT){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this,  MainActivity.this.getString(R.string.logout_succ), Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.logout_fail), Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
		}
	};
	
	
	private RealtimeDataCallback realtimeDataCallback = new RealtimeDataCallback() {

		@Override
		public void onResult(Method arg0, Object arg1) {
			// TODO Auto-generated method stub
			LogUtil.showMsg(TAG+" handleRealtimeData m:" + arg0+",res:" + arg1);
		}

		@Override
		public void handleRealtimeData(RealTimeData arg0) {
			// TODO Auto-generated method stub
			LogUtil.showMsg(TAG+" handleRealtimeData breath:" + arg0.breathRate+",heart:"+arg0.heartRate+",wakeFlag:" + arg0.wakeFlag+",sleepFlag:" + arg0.sleepFlag);
		}
	};
	
	private RawDataCallback rawDataCallback = new RawDataCallback() {
		
		@Override
		public void onResult(Method arg0, Object arg1) {
			// TODO Auto-generated method stub
			LogUtil.showMsg(TAG+" handleRawData m:" + arg0+",res:" + arg1);
		}
		
		@Override
		public void handleRawData(float[][] arg0) {
			// TODO Auto-generated method stub
			for(float[] data:arg0){
				LogUtil.showMsg(TAG+" handleRawData data:" + Arrays.toString(data));

			}
		}
	};
	
	private UpgradeCallback upgradeCallback = new UpgradeCallback() {
		
		@Override
		public void onResult(Method arg0, Object arg1) {
			// TODO Auto-generated method stub
			LogUtil.showMsg(TAG+" onUpgrade m:" + arg0+",res:" + arg1);
		}
		
		@Override
		public void onUpgrade(int arg0) {
			// TODO Auto-generated method stub
			LogUtil.showMsg(TAG+" onUpgrade progress:" + arg0);
		}
	};
	
	private BleStateChangedListener bleStateChangedListener = new BleStateChangedListener() {
		@Override
		public void onStateChanged(final int state) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				public void run() {
					LogUtil.showMsg(TAG+" onStateChanged state:"+ state);
					switch(state){
					case RestOnHelper.STATE_CONNECTING:{
//						Toast.makeText(MainActivity.this, "设备连接中···", Toast.LENGTH_SHORT).show();
						break;
					}
					case RestOnHelper.STATE_CONNECTED:{
//						Toast.makeText(MainActivity.this, "设备连接成功", Toast.LENGTH_SHORT).show();
						break;
					}
					case RestOnHelper.STATE_DISCONNECTED:{
//						Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
						break;
					}
					}
				}
			});
		}
	};
	
	
	private BroadcastReceiver lowPowerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			LogUtil.showMsg(TAG+" reston low power-----------");
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == REQCODE_SEACH_DEVICE){
				selectedBleDevice = (BleDevice) data.getSerializableExtra("device");
				LogUtil.showMsg(TAG+" onActivityResult device:"+ selectedBleDevice);
			}
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		LogUtil.showMsg(TAG+" onDestroy----------");
		unregisterReceiver(lowPowerReceiver);
		restonHelper.removeBleStateChangedListener(bleStateChangedListener);
		restonHelper.disconnect();
		super.onDestroy();
	}
	
	
	private class GridAdapter extends BaseAdapter{
		
		class ViewHolder{
			Button btn;
		}
		
		private LayoutInflater inflater;
		
		GridAdapter(){
			inflater = getLayoutInflater();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ITEMS.length;
		}

		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return ITEMS[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.grid_item, null);
				holder = new ViewHolder();
				holder.btn = (Button) convertView.findViewById(R.id.btn);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			String item = getItem(position);
			holder.btn.setText(item);
			return convertView;
		}
	}
}








































