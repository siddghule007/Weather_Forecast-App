package com.sg_bros.weatherforcast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.R.xml;
import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

///ForcastParser is the class which is being used to fetch xml data from inputstream

public class ForcastParser {
	MainActivity ma;
	InputStream in;
	String s;
	public ForcastParser(MainActivity ma,InputStream in,String s) {
		// TODO Auto-generated constructor stub
		this.in = in;
		this.ma = ma;
		this.s = s;
		 new Thread(new Runnable() {          ////A new thread is started to fetch xml data
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startParsing();
			}
		},"My Thread").start();
		 
		
	}

	protected void startParsing(){         ///startparsing is the starting point for xml parsing which setsup xmlPullParser and starts parsing by calling to nextTag()
				// TODO Auto-generated method stub
						try {
							XmlPullParser mParser = Xml.newPullParser();
							mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
							//in = (InputStream)input();
							mParser.setInput(in,null);
							mParser.nextTag();
							 final city c = readWeatherdata(mParser);
							 ma.xListView.post(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									synchronized (ma.citylist) {
										//Log.e("startParsing",c.name+" adding to cityList");
										if(!isSame(c)){                //Check for duplicate entry
											ma.citylist.add(c);
											ma.xListView.setAdapter(ma.xadapter);
											ma.webPage.setText("Loading for "+s+" is completed successfully");
											if(ma.citylist.size()>0){
												new Thread(new Runnable() {
													public void run() {
														new backup(ma);   //Once data is fetched is stored to restore it back when user restarts the app or orientation of the device is changed
													}
												},"BackUp thread").start();
											}
										}	
									}
									
								}
							});
							 
						} catch (XmlPullParserException e1) {
							//Log.e("XmlPullParserException","Check city names");
							ma.webPage.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									ma.webPage.setText("Please!Check city name:"+s);
									Toast.makeText(ma, "Please!Check city name:"+s, Toast.LENGTH_LONG).show();
								}
							});
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					
				}

	private boolean isSame(final city cx){   //isSame checks for presence of city cx in the citylist
		 synchronized (ma.citylist) {

			 if(ma.citylist.size()>0){
					for(city cc:ma.citylist){
						if(cc.name != null && cx.name != null && cc.country != null && cx.country != null && cx.name.equals(cc.name) && cx.country.equals(cc.country)){
							//Log.e("Parsing",cc.name+" already present in citylist");
							ma.webPage.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									ma.webPage.setText(cx.name+" already present in citylist");
									Toast.makeText(ma, cx.name+" already present in citylist", Toast.LENGTH_LONG).show();
								}
							});
							return true;
							
						}
					
					}
				}
		}
	return false;
	}
	city readWeatherdata(XmlPullParser mParser){       //readWeather reads the weather tag & a city object containing weather info for 14 days
		String name = "",country = "";
		ArrayList<dayForcast> day = null;
		
		try {
			mParser.require(XmlPullParser.START_TAG, null, "weatherdata");
			while(!(mParser.next() == XmlPullParser.START_TAG && mParser.getName().equals("location")))
				continue;
			
			mParser.require(XmlPullParser.START_TAG, null, "location");
				String[] loc = readLocation(mParser);
				name = loc[0];
				country = loc[1];
			
				while(!(mParser.next() == XmlPullParser.START_TAG && mParser.getName().equals("forecast")))
				continue;
			
			
			mParser.require(XmlPullParser.START_TAG, null, "forecast");
			day = readForecast(mParser);
			
			
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("readWeatherdata","Check the city name entered");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//break;
		}
		finally{
			try {
				in.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
		
		return new city(name,country,day);
		
	}
	
	private String[] readLocation(XmlPullParser mParser){   //readLocation reads the location tag & returns string array containg city name and country name
		String[] loc = new String[2];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "location");
			while(!(mParser.next() == XmlPullParser.START_TAG && mParser.getName().equals("name")))
				continue;
			
			mParser.next();
			if(mParser.getEventType() == XmlPullParser.TEXT){
				//Log.e("CITY",mParser.getText());
				loc[0] = mParser.getText();
				
			}
			
			while(!(mParser.next() == XmlPullParser.START_TAG && mParser.getName().equals("country")))
					continue;
			mParser.require(XmlPullParser.START_TAG,null,"country");
			mParser.next();
				if(mParser.getEventType() == XmlPullParser.TEXT){
					//Log.e("readLocation",mParser.getText());
					loc[1] = mParser.getText();
				}
			
			
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loc;
		
	}
	
	private ArrayList<dayForcast> readForecast(XmlPullParser mParser){  //readForecast reads the forecast tag & returns list of dayForecast object
																		//a dayForecast object contains weather info of a day
		ArrayList<dayForcast> day = new ArrayList<dayForcast>();
		try {
			mParser.require(XmlPullParser.START_TAG, null, "forecast");
			while(!(mParser.next() == XmlPullParser.END_TAG && mParser.getName().equals("forecast"))){
				if(mParser.getEventType() == XmlPullParser.START_TAG && mParser.getName().equals("time")){
					day.add(readTime(mParser));
				}
			}
				
				
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return day;
		
	}
	
	private dayForcast readTime(XmlPullParser mParser){ //readTime reads the time tag & return a dayForecast object containing all weather info of a day
		String day;
		String[] forecastValues = null;
		
		try {
			mParser.require(XmlPullParser.START_TAG, null, "time");
			forecastValues = new String[23];
			forecastValues[22] = mParser.getAttributeValue(null, "day");
			
			mParser.nextTag();
			String[] s = readSymbol(mParser);
			forecastValues[0] = s[0];
			forecastValues[1] = s[1];
			forecastValues[2] = s[2];
			
			s = readPrecipitation(mParser);
			forecastValues[3] = s[0];
			forecastValues[4] = s[1];
			
			s = readWindDirection(mParser);
			forecastValues[5] = s[0];
			forecastValues[6] = s[1];
			
			s = readWindSpeed(mParser);
			forecastValues[7] = s[0];
			forecastValues[8] = s[1];
			
			s = readTemperature(mParser);
			for(int a = 0;a < 6 ;a++)
				forecastValues[9+a] = s[a];
			
			s = readPressure(mParser);
			forecastValues[15] = s[0];
			forecastValues[16] = s[1];
			
			s = readHumidity(mParser);
			forecastValues[17] = s[0];
			forecastValues[18] = s[1];
			
			s = readClouds(mParser);
			forecastValues[19] = s[0];
			forecastValues[20] = s[1];
			forecastValues[21] = s[2];
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new dayForcast(forecastValues);
		
	}
	
	private String[] readSymbol(XmlPullParser mParser){ //read reads the symbol tag & returns the string array containing all values from symbole tag
		String[] s = new String[3];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "symbol");
			s[0] = mParser.getAttributeValue(null, "number");
			s[1] = mParser.getAttributeValue(null, "name");
			s[2] = mParser.getAttributeValue(null, "var");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;
	}

	private String[] readPrecipitation(XmlPullParser mParser){ //read reads the precpitation tag & returns the string array containing all values from precipitation tag
		String[] s = new String[2];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "precipitation");
			s[0] = mParser.getAttributeValue(null, "value");
			s[1] = mParser.getAttributeValue(null, "type");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	private String[] readWindDirection(XmlPullParser mParser){ //read reads the windDirection tag & returns the string array containing all values from windDirection tag
		String[] s = new String[2];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "windDirection");
			s[0] = mParser.getAttributeValue(null, "deg");
			s[1] = mParser.getAttributeValue(null, "name");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	private String[] readWindSpeed(XmlPullParser mParser){//read reads the windSpeed tag & returns the string array containing all values from windSpeed tag
		String[] s = new String[2];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "windSpeed");
			s[0] = mParser.getAttributeValue(null, "mps");
			s[1] = mParser.getAttributeValue(null, "name");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	private String[] readTemperature(XmlPullParser mParser){//read reads the temperature tag & returns the string array containing all values from temperature tag
		String[] s = new String[6];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "temperature");
			s[0] = mParser.getAttributeValue(null, "day");
			s[1] = mParser.getAttributeValue(null, "min");
			s[2] = mParser.getAttributeValue(null, "max");
			s[3] = mParser.getAttributeValue(null, "night");
			s[4] = mParser.getAttributeValue(null, "eve");
			s[5] = mParser.getAttributeValue(null, "morn");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	private String[] readPressure(XmlPullParser mParser){//read reads the pressure tag & returns the string array containing all values from pressure tag
		String[] s = new String[2];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "pressure");
			s[0] = mParser.getAttributeValue(null, "unit");
			s[1] = mParser.getAttributeValue(null, "value");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	private String[] readHumidity(XmlPullParser mParser){//read reads the humidity tag & returns the string array containing all values from humidity tag
		String[] s = new String[2];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "humidity");
			s[0] = mParser.getAttributeValue(null, "value");
			s[1] = mParser.getAttributeValue(null, "unit");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	private String[] readClouds(XmlPullParser mParser){ //read reads the cloulds tag & returns the string array containing all values from clouds tag
		String[] s = new String[3];
		try {
			mParser.require(XmlPullParser.START_TAG, null, "clouds");
			s[0] = mParser.getAttributeValue(null, "value");
			s[1] = mParser.getAttributeValue(null, "all");
			s[2] = mParser.getAttributeValue(null, "unit");
			mParser.nextTag();
			mParser.nextTag();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

}

