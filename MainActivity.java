package com.sg_bros.weatherforcast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	ImageButton downloadButton;
	TextView webPage;
	ConnectivityManager connectivityManager;
	boolean isNetworkAvailable;
	URL url;
	InputStream in;
	InputStreamReader reader;
	BroadcastReceiver broadcastReceiver;
	static SharedPreferences cityETNameAndProxy;
	SharedPreferences.Editor cityEditor;
	static SharedPreferences.Editor cityETNameAndProxyEditor;
	EditText cityEditText;
	LocationManager locationManger;
	Location goodLocation;
	LocationListener locListener;
	ExpandableListView xListView;
	ArrayList<city> citylist;
	HashSet<String> cityNames;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	String currentCity ="No city";
	ExpandableListAdapter xadapter ;
	static boolean gpsEnable = false;
	ArrayList<Thread> threadList;
	static int fpf = 0;
	int counter = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);                   //onCreate invokes initComponents method to initiate objects for the app
		setContentView(R.layout.activity_main);
		initComponents();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {              
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		
		case R.id.action_gps:
			if(!locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)){                     //When user clicks on action_gps action button, user is navigated to location settings if gps is disabled 
				startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),0);         // or app starts collecting location data if gps is previously enabled
				
			}else{
				Toast.makeText(getApplicationContext(), "GPS is already enabled", Toast.LENGTH_SHORT).show();
				Location oldLocation = locationManger.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(oldLocation != null){
					webPage.setText("Saved location:Longitude:"+String.valueOf(oldLocation.getLongitude())+",Latitude:"+String.valueOf(oldLocation.getLatitude()));	
				}else
					Toast.makeText(getApplicationContext(), "Old location data is unavailable", Toast.LENGTH_SHORT).show();
				counter = 0;
				webPage.setText("Getting your location....");
				cityETNameAndProxyEditor.putBoolean("isLocating", true).commit();
				locationManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
			}
			
			break;
		}
		
		return super.onOptionsItemSelected(item);
		
	}
	
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		
		if(requestCode == 0){                                                              ///when user returns from location setting onActivityResult method is called
			if(locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)){                //user enables gps app starts collecting location data
				Location oldLocation = locationManger.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(oldLocation != null){
					webPage.setText("Saved location:Longitude:"+String.valueOf(oldLocation.getLongitude())+",Latitude:"+String.valueOf(oldLocation.getLatitude()));	
				}else
					Toast.makeText(getApplicationContext(), "Old location data is unavailable", Toast.LENGTH_SHORT).show();
				counter = 0;
				webPage.setText("Getting your location....");
				cityETNameAndProxyEditor.putBoolean("isLocating", true).commit();
				locationManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
			}
		}
	}
	
	private void getGpsWeather(String lat,String lon){
		try {
			webPage.setText("Your Location is: Longitude="+lon+" Latitude="+lat);     //getGpsWeather supplies latitude and longitude collected from gps data for fetching user location weather data
			url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?lat="+lat+"&lon="+lon +"&cnt=14&mode=xml&units=metric&&APPID=e3ddc40fffd3c6459cf89bf6e8d07ad6");
			synchronized (citylist) {
				citylist.removeAll(citylist);
				xListView.setAdapter(xadapter);
			}
			downloadPage("City at "+lat+","+lon,0);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {                              /// isBetteraLocation compares between old location and new location return true if new location is better else returns false
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	private void initComponents(){            //initComponets initiates all views,lists,list adapter,sets,sharedPreferences,connectivity manager,location mananger,location listener,text changed listener and broadcast receivers
		threadList = new ArrayList<Thread>();
		downloadButton = (ImageButton)findViewById(R.id.download_button);
		cityEditText = (EditText)findViewById(R.id.city_name_editText);
		webPage = (TextView)findViewById(R.id.web_page);
		connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		xListView = (ExpandableListView)findViewById(R.id.expandableListView1);
		cityNames = new HashSet<String>();
		citylist = new ArrayList<city>();
		xadapter = new ExpandableListAdapter(this, citylist);
		xListView.setAdapter(xadapter);
		cityETNameAndProxy = getSharedPreferences("city name and proxy backup", Context.MODE_PRIVATE);
		cityETNameAndProxyEditor = cityETNameAndProxy.edit();
		
		locationManger = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locListener = new LocationListener() {                                       
			
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub                     //Called when gps provider is disabled
				Toast.makeText(getApplicationContext(), "GPS is enabled", Toast.LENGTH_SHORT).show();
				if(cityETNameAndProxy.getBoolean("isLocating", false))
					webPage.setText("Getting your location...");
			}
			
			@Override
			public void onProviderDisabled(String arg0) {             //Called when gps provide is enabled
				// TODO Auto-generated method stub
				webPage.setText("inside onProviderDisabled");
				Toast.makeText(getApplicationContext(), "GPS is disabled", Toast.LENGTH_SHORT).show();
				if(cityETNameAndProxy.getBoolean("isLocating", false))
					webPage.setText("Please turn on GPS to get your location");
				
			}
			
			@Override
			public void onLocationChanged(Location arg0) {
				// TODO Auto-generated method stub   
				if(cityETNameAndProxy.getBoolean("isLocating", false)){//Called when user location is updated
					if(counter <10) 									
						webPage.setText("Loading location....");           ///A goodLocation is new location when isBetterLocation return true for that new location
					else
						webPage.setText("Your location found");          ////This method updates goodLocation for 10 times when 10th goodLocation is available it shows the weather of the last goodLocation
				}
				Location oldLocation = locationManger.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(counter == 0)
					Toast.makeText(getApplicationContext(), "Collecting location data", Toast.LENGTH_SHORT).show();
				
				if(counter < 11){
					webPage.setText("Getting your location....");
					if(oldLocation != null){
						goodLocation = isBetterLocation(oldLocation, arg0)?arg0:oldLocation;
						}else{
						oldLocation = arg0;
					}
					if(goodLocation != null){
						webPage.setText("Your Location found:Longitude:"+String.valueOf(goodLocation.getLongitude())+",Latitude:"+String.valueOf(goodLocation.getLatitude())+" getting accurate location");
						cityETNameAndProxyEditor.putInt("counter", counter).commit();
						if(counter == 10){
							Toast.makeText(getApplicationContext(), "Loading complete:Now see your location", Toast.LENGTH_SHORT).show();
							getGpsWeather(String.valueOf(goodLocation.getLatitude()),String.valueOf(goodLocation.getLongitude()));
						}
						counter++;
					}
				}
				
				if(counter > 10){
					cityETNameAndProxyEditor.putBoolean("isLocating", false).commit();
				}else{
					cityETNameAndProxyEditor.putBoolean("isLocating", true).commit();
				}
				
			}
			
		};
		
		cityEditText.addTextChangedListener(new TextWatcher() {   //It stores the user input 
		
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				cityETNameAndProxyEditor.putString("city edit text", arg0.toString());
				cityETNameAndProxyEditor.commit();
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		
		setBackUpFile();
		
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		broadcastReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent arg1) {       ///This is the broadcast receiver for network connectivity change(including wi-fi and mobile data)
				// TODO Auto-generated method stub
				NetworkInfo wifi,mobileData;
				
				if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
					downloadButton.setImageResource(R.drawable.ic_action_send_now);
					isNetworkAvailable = true;
				}else{
					downloadButton.setImageResource(R.drawable.ic_action_cancel);
					isNetworkAvailable = false;
				}
			}
		};
	
		registerReceiver(broadcastReceiver, filter);
		checkNetworkState();
	}
	
	private void setBackUpFile(){
		
		cityEditText.setText(cityETNameAndProxy.getString("city edit text", "pune"));
		if(cityETNameAndProxy.getBoolean("isLocating", false)){                              ///When orientation is changed or user reopens the app setBackUpFile method takes care of restoring the user previous data
			counter = cityETNameAndProxy.getInt("counter", 0);
			webPage.setText("Getting your location...");
			if(!locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER))
				webPage.setText("Please turn on the GPS to get your location!");
			locationManger.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
		}
	
		xListView.requestFocus();
		setUpBackUpExpandables();
		
		
	}
	
	private void setUpBackUpExpandables(){
		String name,country;
		synchronized (citylist) {                   ///setUpExpandables method restore data in expandable list view
			citylist.remove(citylist); 
			ArrayList<dayForcast> dayList; 
			int cities = getSharedPreferences("city_number", Context.MODE_PRIVATE).getInt("city_mNnumber", 1);
			for(int a=0;a<cities;a++){
				dayList = new ArrayList<dayForcast>();
				name = getSharedPreferences("city"+"_"+String.valueOf(a), Context.MODE_PRIVATE).getString("name", "No city");
				country = getSharedPreferences("city"+"_"+String.valueOf(a), Context.MODE_PRIVATE).getString("country", "No country");
				
				for(int d=0;d<14;d++){
					String[] s = new String[23];
					SharedPreferences gh = getSharedPreferences("city_"+String.valueOf(a)+"_"+String.valueOf(d), Context.MODE_PRIVATE);
					for(int e=0;e<23;e++){
						s[e]=gh.getString("city_"+String.valueOf(a)+"_"+String.valueOf(d)+"_"+String.valueOf(e), "--");
					}
					dayList.add(new dayForcast(s));
				}
				citylist.add(new city(name,country,dayList));
			}
		}
		

	}
	
	private void checkNetworkState(){  ///checkNetworkState check ths availability of networks
		NetworkInfo wifi,mobileData;
		if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
			downloadButton.setImageResource(R.drawable.ic_action_send_now);
			isNetworkAvailable = true;
		}else{
			downloadButton.setImageResource(R.drawable.ic_action_cancel);
			isNetworkAvailable = false;
		}
	}
	
	public void getWeatherOfAllCities(View v){
		                                         //getWeatherOfAllCities method takes editText input then finds all city names and invokes downloadPage method to collect weather of city
		synchronized (citylist) {
			cityNames.removeAll(cityNames);
			citylist.removeAll(citylist);
			xListView.setAdapter(xadapter);
		}
		int y = 0;
		for(String x:cityEditText.getText().toString().trim().toLowerCase().split(",")){
			cityNames.add(x.trim().toLowerCase());
			y++;
		}
		
		
		int w = 0;
		for(String s:cityNames){
			try {
				url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q="+s+"&mode=xml&units=metric&cnt=14&APPID=e3ddc40fffd3c6459cf89bf6e8d07ad6");
				downloadPage(s,w);
				currentCity = s;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
			
	}
	
	public void downloadPage(String s,int w){      //If network is available downloadPage opens urlconnection on the url containg xml weather data of the city
		if(isNetworkAvailable){
			try {
	
				//Log.e("downloadPage URL",url.toString());
				HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
				//Log.e("downloadPage METH","Success1");
				urlConn.setRequestMethod("GET");
				urlConn.setReadTimeout(10000);
			    urlConn.setConnectTimeout(15000);    
				urlConn.setDoInput(true);
				//Log.e("downloadPage METH","Success2");
				connect(urlConn,s,w);
				
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				webPage.append("Protocol exception");
			}catch ( IOException e) {
				// TODO: handle exception
				e.printStackTrace();
				webPage.setText("Cannot fetch the weather information of "+s+"\nPlease Check your network settings and retry"); 
				Toast.makeText(getApplicationContext(), "Please Check your network settings and retry" , Toast.LENGTH_LONG).show();
			}
			
		}else{
			Toast.makeText(getApplicationContext(), "Please Check your network settings and retry" , Toast.LENGTH_LONG).show();
			webPage.setText("Please Check your network settings and retry");
		}
	}
	
	private void connect(final HttpURLConnection urlConn,final String s,final int w){   ///connect method starts new thread of network operations for each city & gets inputStreams containing weather data in xml format 
		new Thread(new Runnable(){        

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					urlConn.connect();
					webPage.post(new Runnable(){
					int a = urlConn.getResponseCode();
						@Override
						public void run() {
							webPage.setText("Loading data for :"+s);
						}
						
					});
					in = urlConn.getInputStream();
					parseXml(s,w);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//Log.e("connect METH","IO exception");
					
					webPage.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							webPage.setText("Cannot fetch the weather information of "+s+"\nPlease Check your network settings and retry");
							Toast.makeText(getApplicationContext(), "Please Check your network settings and Retry" , Toast.LENGTH_LONG).show();
						}
					});
				
				}
			}
			
		}).start();
	}
	
	private void parseXml(final String s,int w){
		if(in != null){                               ///parseXml creates new class to parse xml data from input stream
			new ForcastParser(this,in,s);	
		}
			
		else{
			//Log.e("parseXml","InputStream is null");
			webPage.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					webPage.setText("Cannot fetch the weather information of "+s+"\nPlease Check your network settings and retry");
					Toast.makeText(getApplicationContext(), "Please Check your network settings and Retry" , Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	
}

