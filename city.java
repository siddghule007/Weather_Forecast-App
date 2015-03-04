package com.sg_bros.weatherforcast;
//This class represnts a city in the city list
import java.util.ArrayList;
import java.util.List;

public class city {
	String name,country;
	ArrayList<dayForcast> day;
	city(String name, String country, ArrayList<dayForcast>day){
		this.name = name;
		this.country = country;
		this.day = day;
	}
	
}

