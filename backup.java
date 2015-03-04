package com.sg_bros.weatherforcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
//The methods in this class are used to store weather info of the cities searched recently
public class backup {
	MainActivity ma;
	city data;
	int cityNo;
	
	backup(MainActivity m){
		ma = m;
		cityNo = ma.citylist.size();
		startBackUp();
	}

	private void startBackUp(){
		if(ma.citylist != null && ma.citylist.size()>0)
			backUpExpandables();
	
	}
	
	private void backUpExpandables(){ //backUpExpandables creates new sharedPreference for each new city and stores name and country
		synchronized (ma.citylist) {  //later it creates 14 unique sharedpreferences  corresponging to a city & each contains weather data of one day 
			ma.getSharedPreferences("city_number", Context.MODE_PRIVATE).edit().putInt("city_mNnumber", cityNo).commit();
			int z = ma.citylist.size()-1;
			city c = ma.citylist.get(z);
			if(c != null){
				String city =c.name+","+c.country;
				ma.getSharedPreferences("city_"+String.valueOf(z), Context.MODE_PRIVATE).edit().putString("country", c.country).commit();
				ma.getSharedPreferences("city_"+String.valueOf(z), Context.MODE_PRIVATE).edit().putString("name", c.name).commit();
				int x = 0;
				for(dayForcast f:c.day){
					SharedPreferences gh = ma.getSharedPreferences("city_"+String.valueOf(z)+"_"+String.valueOf(x), Context.MODE_PRIVATE);
					
					int y = 0;
					for(String s:f.forecastValues){
						gh.edit().putString("city_"+String.valueOf(z)+"_"+String.valueOf(x)+"_"+String.valueOf(y), s).commit();
						y++;
					}
				x++;	
				}
			
			}

				
		}
	
	}
}
