package com.restonsdk.demo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.medica.restonsdk.bluetooth.RestOnHelper;
import com.medica.restonsdk.domain.BleDevice;
import com.medica.restonsdk.interfs.BleScanListener;

public class SearchDeviceActivity extends Activity {
//	private static final String TAG = SearchDeviceActivity.class.getSimpleName();
	private ListView listView;
	private RestOnHelper bleHelper;
	private DeviceAdapter adapter;
	private LayoutInflater inflater;
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		
		inflater = getLayoutInflater();
		bleHelper = RestOnHelper.getInstance(this);
		adapter = new DeviceAdapter();
		
		listView = (ListView) findViewById(R.id.list_devices);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this.mDeviceClickListener);
		
		bleHelper.scanBleDevice(bleScanListener);
	}
	

	protected void onDestroy() {
		super.onDestroy();
//		LogUtil.showMsg(TAG+" onDestroy-----------");
	}
	
	private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapter,View view, int position, long id) {
			bleHelper.stopScan();
			BleDevice device = SearchDeviceActivity.this.adapter.getItem(position);
			Intent data = new Intent();
			data.putExtra("device", device);
			setResult(RESULT_OK, data);
			finish();
		}
	};



	private BleScanListener bleScanListener = new BleScanListener(){
		
		@Override
		public void onBleScanStart() {
			// TODO Auto-generated method stub
			setTitle(SearchDeviceActivity.this.getString(R.string.scaning));
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onBleScan(final BleDevice device) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//if(device.deviceName.startsWith("T"))
						adapter.addItem(device);
				}
			});
		}

		@Override
		public void onBleScanFinish() {
			// TODO Auto-generated method stub
//			LogUtil.showMsg("onBleScanFinish-----------");
			setProgressBarIndeterminateVisibility(false);
			if(adapter.getCount() > 0){
				SearchDeviceActivity.this.setTitle(SearchDeviceActivity.this.getString(R.string.select_device));
			}else{
				SearchDeviceActivity.this.setTitle(SearchDeviceActivity.this.getString(R.string.no_device));
			}
		}
	};
	
	private class DeviceAdapter extends BaseAdapter{

		private ArrayList<BleDevice> data = new ArrayList<BleDevice>();
		
		class ViewHolder{
			TextView tvName;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public BleDevice getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
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
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.device_name, null);
				holder.tvName = (TextView) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			BleDevice device = getItem(position);
			holder.tvName.setText(device.deviceName + "\n" + device.address);
			return convertView;
		}
		
		public void addItem (BleDevice device){
			if(!data.contains(device)){
				data.add(device);
				notifyDataSetChanged();
			}
		}
	}
	
}


























