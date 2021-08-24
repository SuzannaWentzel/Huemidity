package com.example.huemidity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    // constants
    private static final String TAG = "[DEBUG]";
    private static final String CITY = "city";
    private static final String DEFAULT_CITY = "Enschede";
    private static final int LOCATION_PERMISSION_CODE = 26;

    // screen variables
    public View splashScreen;
    public View mainScreen;
    public View selectCityScreen;

    // view elements
    public TextView tempTxt;
    public TextView cityTxt;
    public TextView maxTempTxt;
    public TextView minTempTxt;
    public TextView humidityTxt;
    public TextView cityLabel;

    // GPS variables
    public LocationManager locationManager;
    public LocationListener locationListener;

    // other global variables
    private GestureDetector gestureDetector;
    private ProgressBar weatherSpinner;
    private SharedPreferences sharedPreferences;
    public String city;
    public String weatherBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // set splash screen
        splashScreen = findViewById(R.id.splash_screen);
        splashScreen.setVisibility(View.VISIBLE);

        findViews();

        // init gesture detection
        gestureDetector = initGestureDetector();

        // wait 2s before starting request, to avoid flashing splash screen
        waitAndRunWeather();

        // init app preferences & city
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        city = sharedPreferences.getString(CITY, DEFAULT_CITY);

        // init location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void findViews() {
        // init screens
        mainScreen = findViewById(R.id.main_container);
        selectCityScreen = findViewById(R.id.select_city_container);

        // init views
        cityTxt = findViewById(R.id.city);
        tempTxt = findViewById(R.id.temp);
        maxTempTxt = findViewById(R.id.max_temp);
        minTempTxt = findViewById(R.id.min_temp);
        humidityTxt = findViewById(R.id.humidity);

        // init spinner
        weatherSpinner = findViewById(R.id.spinner_weather);
    }

    private GestureDetector initGestureDetector() {
        mainScreen.setOnTouchListener(this);

        return new GestureDetector(this, new OnSwipeListener() {
            @Override
            public boolean onSwipe(Direction direction) {
                if (direction == Direction.up) {
                    //TODO: show weekly weather
                    Log.d(TAG, "onSwipe: up");

                }

                if (direction == Direction.down) {
                    // reload weather data
                    weatherSpinner.setVisibility(View.VISIBLE);
                    new WeatherTask().execute();
                }

                return true;
            }
        });
    }

    public void waitAndRunWeather() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // request the weather
                new WeatherTask().execute();
            }
        }, 2000);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void changeCity(View view) {
        // open change city fragment
        SelectCity selectCity = SelectCity.newInstance(city, weatherBackground);
        selectCityScreen.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.select_city_container, selectCity);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void saveCityInput(View view) {
        // change button with loading spinner
        ProgressBar manualSpinner = findViewById(R.id.spinner_manually_entered);
        manualSpinner.setVisibility(View.VISIBLE);
        ImageView btn = findViewById(R.id.btn_save_city);
        btn.setVisibility(View.GONE);

        // retrieve the city that was entered
        EditText cityTxt = findViewById(R.id.input_city);
        city = cityTxt.getText().toString().trim();

        saveCity();
    }

    public void saveCityGPS(View view) {
        // show spinner to indicate it's loading
        ProgressBar gpsSpinner = findViewById(R.id.spinner_gps);
        gpsSpinner.setVisibility(View.VISIBLE);

        getLocation();
        // city is saved in locationhandler
    }

    public void saveCity() {
        // execute weathertask, if city is incorrect, it will throw an error
        new WeatherTask().execute();

        // store city in shared preferences (app preferences)
        sharedPreferences.edit().putString(CITY, city).apply();
    }

    private void getLocation() {
        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // no permission given, ask for permission
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_CODE);
            return;
        }
        // check if GPS is enabled
        if (GPSEnabled()) {
            locationListener = new MyLocationListener();
            locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);
        } else {
            // GPS is disabled, request enabling gps
            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(onGPS);
        }
    }

    // checks if GPS is enabled/disabled
    private boolean GPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void setBackground(String weather, Double temp) {
        switch (weather) {
            case "few clouds":
            case "scattered clouds":
            case "broken clouds":
                weatherBackground = "clouds";
                mainScreen.setBackgroundResource(R.drawable.clouded);
                break;
            case "shower rain":
            case "rain":
                weatherBackground = "rain";
                mainScreen.setBackgroundResource(R.drawable.rain);
                break;
            case "thunderstorm":
                weatherBackground = "thunder";
                mainScreen.setBackgroundResource(R.drawable.thunder);
                break;
            case "snow":
                weatherBackground = "snow";
                mainScreen.setBackgroundResource(R.drawable.snow);
                break;
            case "mist":
                weatherBackground = "fog";
                mainScreen.setBackgroundResource(R.drawable.fog);
                break;
            default:
                if (temp > 25) {
                    weatherBackground = "hot";
                    mainScreen.setBackgroundResource(R.drawable.hot);
                    break;
                }

                // case "sun" included here
                weatherBackground = "sun";
                mainScreen.setBackgroundResource(R.drawable.sun);
                break;
        }
    }

    private String getCityFromCoordinates(double latitude, double longitude) {
        String cityName = null;
        // use geocoder to transform lat & long to address
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                // result was found, we got a city
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String locatedCity = getCityFromCoordinates(latitude, longitude);
            Log.d(TAG, "onLocationChanged: found city: " + locatedCity);

            city = locatedCity != null? locatedCity : city;
            saveCity();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    @SuppressLint("StaticFieldLeak")
    class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // use HttpRequest class to send request to openweathermap API
            return HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + Keys.OPEN_WEATHER_API_KEY);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                String temp = Math.round(main.getDouble("temp")) + " °C";
                String tempMin = Math.round(main.getDouble("temp_min")) + " °C ";
                String tempMax = Math.round(main.getDouble("temp_max")) + " °C";
                String humidity = "Humidity: " + main.getString("humidity") + "%";


                String weatherDescription = weather.getString("description");
                setBackground(weatherDescription, main.getDouble("temp"));

//                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                /* populate data into views */
                cityTxt.setText(city);
                tempTxt.setText(temp);
                Log.d(TAG, "onPostExecute: temp: " + temp);
                maxTempTxt.setText(tempMax);
                minTempTxt.setText(tempMin);
                humidityTxt.setText(humidity);

                // Hide spinner, hide the splash screen, show the main design
                //TODO: add fade-transition between screens
                weatherSpinner.setVisibility(View.GONE);
                splashScreen.setVisibility(View.GONE);
                mainScreen.setVisibility(View.VISIBLE);

                // close fragment if found on stack
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                if (manager.getBackStackEntryCount() > 0) {
                    manager.popBackStack();
                }

                transaction.commit();

            } catch (JSONException e) {
                Log.d("[DEBUG]", "EXEPTION: onPostExecute: " + e.getMessage());
                cityLabel = findViewById(R.id.label_city);
                cityLabel.setTextColor(getResources().getColor(R.color.red));
                cityLabel.setText(getResources().getString(R.string.city_label_input_error));
            }
        }
    }
}