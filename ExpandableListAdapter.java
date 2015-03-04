package com.sg_bros.weatherforcast;
///This is the adapter class for expandable list
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint("ViewHolder")
public class ExpandableListAdapter extends BaseExpandableListAdapter {
	MainActivity ma;
	ArrayList<city> data;
	HashMap<String, Integer> imageMap;
	
	ExpandableListAdapter(MainActivity a,ArrayList<city> data){
		ma = a;
		this.data = data;
		imageMap = new HashMap<String,Integer>();
		createMap();
	}
	
	private void createMap(){
		imageMap.put("01d",  Integer.valueOf(R.raw.d01));
		imageMap.put("02d",  Integer.valueOf(R.raw.d02));
		imageMap.put("03d",  Integer.valueOf(R.raw.d03));
		imageMap.put("04d",  Integer.valueOf(R.raw.d04));
		imageMap.put("09d",  Integer.valueOf(R.raw.d09));
		imageMap.put("10d",  Integer.valueOf(R.raw.d10));
		imageMap.put("11d",  Integer.valueOf(R.raw.d11));
		imageMap.put("13d",  Integer.valueOf(R.raw.d13));
		imageMap.put("50d",  Integer.valueOf(R.raw.d50));
		imageMap.put("01n",  Integer.valueOf(R.raw.n01));
		imageMap.put("02n",  Integer.valueOf(R.raw.n02));
		imageMap.put("03n",  Integer.valueOf(R.raw.n03));
		imageMap.put("04n",  Integer.valueOf(R.raw.n04));
		imageMap.put("09n",  Integer.valueOf(R.raw.n09));
		imageMap.put("10n",  Integer.valueOf(R.raw.n10));
		imageMap.put("11n",  Integer.valueOf(R.raw.n11));
		imageMap.put("13n",  Integer.valueOf(R.raw.n13));
		imageMap.put("50n",  Integer.valueOf(R.raw.n50));
		imageMap.put("1dd",  Integer.valueOf(R.raw.d01));
		
	}
	@Override
	public Object getChild(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
			ViewGroup arg4) {  //This method loads data corresponding to one day in one child of the list
		TextView weatherCodeTextView,humidityTextView,pressureWindTextView,rainTextView,minMaxTm,mornEveTm,dayTm,dateTextView;
		// TODO Auto-generated method stu
		LayoutInflater inflater = ma.getLayoutInflater();
		View v = inflater.inflate(R.layout.child_item_layout, arg4,false);
		
		LayoutInflater mInflater = (LayoutInflater)ma.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mV = arg3;
		v = mInflater.inflate(R.layout.child_item_layout, arg4,false);
		dayForcast day = (dayForcast) data.get(arg0).day.get(arg1); //here a day for child is choosen from data based on the position of the child 
		int a = 0;
		for(String s:day.forecastValues){
			if(s == null)
				day.forecastValues[a] = "NA";
			a++;
		}
		dateTextView = (TextView)v.findViewById(R.id.date_text_view);
		weatherCodeTextView = (TextView)v.findViewById(R.id.weathConditionTextView);
		humidityTextView =(TextView)v.findViewById(R.id.humidity_text_view);
		pressureWindTextView =(TextView)v.findViewById(R.id.pressure_wind_text_view);
		rainTextView =(TextView)v.findViewById(R.id.rain_text_view);
		minMaxTm =(TextView)v.findViewById(R.id.min_max_temperature_text_view);
		mornEveTm =(TextView)v.findViewById(R.id.morn_eve_night_temperature_text_view);
		//dayTm =(TextView)v.findViewById(R.id.day_temperature_text_view);
		
		dateTextView.setText(day.forecastValues[22]);
		weatherCodeTextView.setText(day.forecastValues[1]);
		humidityTextView.setText("Humidiy : "+day.forecastValues[17]+day.forecastValues[18]);
		pressureWindTextView.setText("Pressure : "+day.forecastValues[16]+day.forecastValues[15]+"\n"
				+"Wind speed : "+day.forecastValues[7]+"mps"
				+"\nWind direction : "+day.forecastValues[6]);
		
		rainTextView.setText("Rain: : "+day.forecastValues[3]+"mm");
		
		minMaxTm.setText("Min : "+day.forecastValues[10]+ "\u00b0"+"C"+"\nMax : "+day.forecastValues[11]+"\u00b0"+"C");
		mornEveTm.setText("Morn : "+day.forecastValues[14]+"\nDay : "+day.forecastValues[9]+"\u00b0"+"C"+"\u00b0"+"C"+"\nEve : "+day.forecastValues[13]+"\u00b0"+"C"+"\nNight : "+day.forecastValues[12]+"\u00b0"+"C");
		///dayTm.setText("Day : "+day.forecastValues[9]+"\u00b0"+"C");
		
		ImageView o = (ImageView)v.findViewById(R.id.weatherImage);
		if(imageMap.get(day.forecastValues[2])!= null && !imageMap.get(day.forecastValues[2]).equals("Not available"))
			o.setImageResource(imageMap.get(day.forecastValues[2]).intValue());
		else 
			o.setImageResource(R.drawable.ic_launcher);
		
		return v;
	}

	@Override
	public int getChildrenCount(int arg0) {
		// TODO Auto-generated method stub
		return data.get(arg0).day.size();
	}

	@Override
	public Object getGroup(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public long getGroupId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {
		// TODO Auto-generated method stub
		city c = data.get(arg0);
		LayoutInflater inflater = ma.getLayoutInflater();
		View v = inflater.inflate(R.layout.parent_item_layout, arg3,false);
		
		TextView t = (TextView)v.findViewById(R.id.textView1);
		t.setText(c.name+","+c.country);
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	
	private void downloadImage(final ImageView xx, final String location){
		new Thread(new Runnable() {
			Bitmap m;
			URL bUrl;
			dayForcast f;
			InputStream in;
			@Override
			public void run() {
				// TODO Auto-generated method stub
					//final ImageView o = (ImageView)ma.findViewById(R.id.imageView);
				try {
					bUrl = new URL("http://openweathermap.org/img/w/"+location+".png");
					HttpURLConnection urlConn = (HttpURLConnection) bUrl.openConnection();
					urlConn.setRequestMethod("GET");
					urlConn.connect();
					//Log.e("weatherListAdapter","Downloading Image....");
					in = urlConn.getInputStream();
					m = BitmapFactory.decodeStream(in);
				}	catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					BitmapFactory bFactory = new BitmapFactory();
					m = bFactory.decodeResource(ma.getResources(), R.drawable.ic_launcher);
				}
					xx.post(new Runnable() {
						
						@Override
						public void run() {
								xx.setImageBitmap(m);
						}
					});
			}
		}).start();
		
	}	

}
