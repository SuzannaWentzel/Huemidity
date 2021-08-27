package com.example.huemidity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeeklyWeather#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeeklyWeather extends Fragment {

    // the fragment initialization parameters
    private static final String TAG = "WEEKLY_WEATHER";
    private static final String CITY = "CITY";
    private static final int LOCATION_PERMISSION_CODE = 32;

    private String city;
    private Context context;
    private Activity activity;

    // view parameters
    private RelativeLayout weeklyWeatherContainer;
    private RelativeLayout weeklyWeatherContent;
    private LinearLayout dayContainer;

    private TextView dayView;
    private TextView dateView;
    private TextView tempView;
    private TextView humidityView;
    private ImageView currentWeatherView;
    private ImageView previousWeatherView;
    private ImageView nextWeatherView;

    private ProgressBar spinnerWeather;

    private GestureDetector gestureDetector;

    // location parameters
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;

    // weather parameters
    private ArrayList<Weather> weeklyWeather;
    private int currentIndex;

    public WeeklyWeather() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param city defines the location the weekly weather should be searched on.
     * @return A new instance of fragment WeeklyWeather.
     */
    public static WeeklyWeather newInstance(String city) {
        WeeklyWeather fragment = new WeeklyWeather();
        Bundle args = new Bundle();
        args.putString(CITY, city);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = getArguments().getString(CITY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weekly_weather, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        weeklyWeather = new ArrayList<>();

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        weeklyWeatherContainer = view.findViewById(R.id.weekly_weather_container);
        weeklyWeatherContent = view.findViewById(R.id.weekly_weather_content);
        spinnerWeather = view.findViewById(R.id.spinner_weekly_weather);
        dayView = view.findViewById(R.id.day);
        dateView = view.findViewById(R.id.date);
        tempView = view.findViewById(R.id.degrees);
        humidityView = view.findViewById(R.id.humidity_weekly);
        currentWeatherView = view.findViewById(R.id.current_day);
        previousWeatherView = view.findViewById(R.id.previous_day);
        nextWeatherView = view.findViewById(R.id.next_day);
        dayContainer = view.findViewById(R.id.day_container);

//        gestureDetector = initGestureDetector();

        getLocation();

        return view;
    }

//    private GestureDetector initGestureDetector() {
//        dayContainer.setOnTouchListener(this);
//
//        return new GestureDetector(context, new OnSwipeListener() {
//                @Override
//                public boolean onSwipe(Direction direction) {
//                if (direction == Direction.right) {
//                    Log.d(TAG, "onSwipe: up");
//                    showNextDay();
//                }
//
//                if (direction == Direction.left) {
//                    // reload weather data
//                    showPreviousDay();
//                }
//
//                return true;
//                }
//        });
//    }

    // checks if GPS is enabled/disabled
    private boolean GPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getLocation() {
        // check permissions
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // no permission given, ask for permission
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_CODE);
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

    private void setBackground(String weather, int temp) {
        weather = weather.toLowerCase();
        Log.d(TAG, "setBackground: weather description: " + weather);
        switch (weather) {
            case "clouds":
                weeklyWeatherContainer.setBackgroundResource(R.drawable.clouded_blurred);
                break;
            case "drizzle":
            case "rain":
                weeklyWeatherContainer.setBackgroundResource(R.drawable.rain_blurred);
                break;
            case "thunderstorm":
                weeklyWeatherContainer.setBackgroundResource(R.drawable.thunder_blurred);
                break;
            case "snow":
                weeklyWeatherContainer.setBackgroundResource(R.drawable.snow_blurred);
                break;
            case "atmosphere":
                weeklyWeatherContainer.setBackgroundResource(R.drawable.fog_blurred);
                break;
            default:
                if (temp > 25) {
                    weeklyWeatherContainer.setBackgroundResource(R.drawable.hot_blurred);
                    break;
                }

                // case "sun" included here
                weeklyWeatherContainer.setBackgroundResource(R.drawable.sun_blurred);
                break;
        }
    }

    private void setImageView(String weather, int temp, ImageView imageView) {
        weather = weather.toLowerCase();
        switch (weather) {
            case "clouds":
                imageView.setImageResource(R.drawable.clouded);
                break;
            case "drizzle":
            case "rain":
                imageView.setImageResource(R.drawable.rain);
                break;
            case "thunderstorm":
                imageView.setImageResource(R.drawable.thunder);
                break;
            case "snow":
                imageView.setImageResource(R.drawable.snow);
                break;
            case "atmosphere":
                imageView.setImageResource(R.drawable.fog);
                break;
            default:
                if (temp > 25) {
                    imageView.setImageResource(R.drawable.hot);
                    break;
                }

                // case "sun" included here
                imageView.setImageResource(R.drawable.sun);
                break;
        }
    }

    public void showNextDay() {
        if (currentIndex < weeklyWeather.size() - 1) {
            currentIndex ++;
            updateUI();
        }
    }

    public void showPreviousDay() {
        if (currentIndex > 0) {
            currentIndex --;
            updateUI();
        }
    }

    public void updateUI() {
        Weather currentWeather = weeklyWeather.get(currentIndex);
        Weather previousWeather = null;
        Weather nextWeather = null;

        if (currentIndex > 0) {
            previousWeather = weeklyWeather.get(currentIndex - 1);
        }

        if (currentIndex < weeklyWeather.size() - 1) {
            nextWeather = weeklyWeather.get(currentIndex + 1);
        }

        // update day
        String dayPattern = "EEEE";
        SimpleDateFormat dayFormat = new SimpleDateFormat(dayPattern);
        String day = dayFormat.format(currentWeather.getDate());
        dayView.setText(day);

        // update date
        String datePattern = "MMMM d";
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        String date = dateFormat.format(currentWeather.getDate());
        String dateAndCity = date + ", " + city;
        dateView.setText(dateAndCity);

        // update temp
        tempView.setText(currentWeather.getTemp() + " °C");

        // update humidity
        humidityView.setText("humidity: " + currentWeather.getHumidity());

        // update background
        setBackground(currentWeather.getDescription(), currentWeather.getTemp());

        // update currentImage
        setImageView(currentWeather.getDescription(), currentWeather.getTemp(), currentWeatherView);

        // update previousImage
        if (previousWeather != null) {
            setImageView(previousWeather.getDescription(), previousWeather.getTemp(), previousWeatherView);
            previousWeatherView.setVisibility(View.VISIBLE);
        } else {
            previousWeatherView.setVisibility(View.INVISIBLE);
        }

        // update nextImage
        if (nextWeather != null) {
            setImageView(nextWeather.getDescription(), nextWeather.getTemp(), nextWeatherView);
            nextWeatherView.setVisibility(View.VISIBLE);
        } else {
            nextWeatherView.setVisibility(View.INVISIBLE);
        }

        spinnerWeather.setVisibility(View.GONE);
        weeklyWeatherContent.setVisibility(View.VISIBLE);
    }

    /**
     * Class for listening to location
     */
    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d(TAG, "onLocationChanged: found coordinates: lat: " + latitude + ", long: " + longitude);

            new WeeklyWeatherTask().execute();
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

    /**
     * Class for creating asynchronous weather task
     */
    @SuppressLint("StaticFieldLeak")
    class WeeklyWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // use HttpRequest class to send request to openweathermap API
            return HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/onecall?lat=" + latitude + "&lon=" + longitude + "&exclude=current,minutely,hourly,alerts&units=metric&appid=" + Keys.OPEN_WEATHER_API_KEY);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                Log.d(TAG, "onPostExecute: JSON: " + jsonObj.toString());
                JSONArray weekWeather = jsonObj.getJSONArray("daily");

                // create weather array
                for (int i = 0; i < weekWeather.length(); i++) {
                    JSONObject dayWeather = weekWeather.getJSONObject(i);
                    Date date = new Date(dayWeather.getLong("dt") * 1000);
                    int temp = (int) Math.round(dayWeather.getJSONObject("temp").getDouble("day"));
                    Log.d(TAG, "onPostExecute: temp: " + temp);
                    String description = dayWeather.getJSONArray("weather").getJSONObject(0).getString("main");
                    String humidity = dayWeather.getInt("humidity") + "%";
                    Weather weather = new Weather(date, temp, description, humidity);
                    weeklyWeather.add(weather);
                }

                previousWeatherView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPreviousDay();
                    }
                });

                nextWeatherView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showNextDay();
                    }
                });

                currentIndex = 0;
                updateUI();
//                JSONObject main = jsonObj.getJSONObject("main");
//                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
//
//                String temp = Math.round(main.getDouble("temp")) + " °C";
//                String tempMin = Math.round(main.getDouble("temp_min")) + " °C";
//                String tempMax = Math.round(main.getDouble("temp_max")) + " °C";
//                String humidity = "Humidity: " + main.getString("humidity") + "%";
//
//
//                String weatherDescription = weather.getString("description");
//                setBackground(weatherDescription, main.getDouble("temp"));

//                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                /* populate data into views */
//                cityTxt.setText(city);
//                tempTxt.setText(temp);
//                Log.d(TAG, "onPostExecute: temp: " + temp);
//                maxTempTxt.setText(tempMax);
//                minTempTxt.setText(tempMin);
//                humidityTxt.setText(humidity);

            } catch (JSONException e) {
                Log.d("[DEBUG]", "EXEPTION: onPostExecute: " + e.getMessage());
            }
        }
    }
}