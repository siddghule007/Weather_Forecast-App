# Weather_Forecast-App
This is the repository for Weather Forecast android app source code files

++========================Introduction==============================================

 This is a project of android app named Weather Forecast. This app shows 14 days forecast
 for most of the cities in the world. This app can accept multiple city names and shows 
 weather forecast. App fetches 14 days weather data from 
 http://api.openweathermap.org/data/2.5/forecast/. The data is present in the xml format 
 as well as JSON format. App uses XML format to fetch data. The forecast data gets updated 
 at regular intervals during a day. This app can also shows 14 days forecast for the current 
 city using GPS.


--------Some important directories of this project--------

1) src: Contains all source files
2) res: Contains all resources 

---------Sub-folders of res ---------------------------------------    

2a) drawable: These are directories where drawable files are present
2b) layouts:  It contains layout for main activity, child list view, parent list view
2c) menu:     contains layout for action bar
2d) raw:      contains weather images
2e) value:    contains string.xml, colors.xml, dimens.xml, styles.xml

3) AndroidManifest.xml file

========Features of the Weather Forecast======

1) Can accept multiple cities
2) Shows 14 days weather forecast for each of the cities
3) Can access user location via GPS and shows weather forecast for the user city

4) ------data fields shown in the weather forecast--------
	
	i)Weather Condition
	ii)Humidity
	iii)Pressure
	iv)Wind Speed and direction
	v)Rainfall
	vi)Min, Max, Morning, Evening, Night, day average temperatures


*========How to use=============*

1) Getting 14 days forecast for multiple cities:
   Enter city names in the EditText placed just below action bar. You can enter either 
   one city or multiple cities, each one separated by comma. Then press button next to it.
   If button shows cross(X) on it, it means you don't have mobile data or wi-fi with working 
   internet connection. Wait till it shows all the cities. But keep in mind you won't 
   get cities in same order as you entered because of parallel running threads. Then 
   click on the city name and you will see 14 days forecast for the city.

2) Getting forecast for user city using GPS: 
   First click on the action button at right top. Enable GPS if navigated to location settings. 
   You won't be navigated if gps is already enabled.  Wait till app collects your location 
   data and you will get forecast for your current location using GPS Enabled Weather Forecast APP. 


