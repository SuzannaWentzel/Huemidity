package com.example.huemidity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    /* constants */
    private final String TAG = "[DEBUG]";
    //TODO: make city editable
    public String city = "Enschede";

    /* screen variables */
    public View splash_screen;
    public View main_screen;

    /* view elements */
    public TextView tempTxt;
    public TextView cityTxt;
    public TextView maxTempTxt;
    public TextView minTempTxt;
    public TextView humidityTxt;

    private GestureDetector gestureDetector;
    private ProgressBar weatherSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // set splash screen
        splash_screen = findViewById(R.id.splash_screen);
        splash_screen.setVisibility(View.VISIBLE);

        findViews();

        // init gesture detection
        gestureDetector = initGestureDetector();

        // wait 2s before starting request, to avoid flashing splash screen
        waitAndRunWeather();
    }

    private void findViews() {
        // init screens
        main_screen = findViewById(R.id.main_container);

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
        main_screen.setOnTouchListener(this);

        return new GestureDetector(this, new OnSwipeListener(){
            @Override
            public boolean onSwipe(Direction direction) {
                if (direction == Direction.up){
                    //TODO: show weekly weather
                    Log.d(TAG, "onSwipe: up");

                }

                if (direction == Direction.down){
                    // reload weather data
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

    @SuppressLint("StaticFieldLeak")
    class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            weatherSpinner.setVisibility(View.VISIBLE);
            Log.d(TAG, "onPreExecute: showing spinner...");
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
                maxTempTxt.setText(tempMax);
                minTempTxt.setText(tempMin);
                humidityTxt.setText(humidity);

                /* Views populated, Hiding the splash, Showing the main design */
                //TODO: add fade-transition between screens
                weatherSpinner.setVisibility(View.GONE);
                splash_screen.setVisibility(View.GONE);
                main_screen.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                Log.d("[DEBUG]", "onPostExecute: " + e.getMessage());
            }
        }

        private void setBackground(String weather, Double temp) {
            switch (weather) {
                case "few clouds":
                case "scattered clouds":
                case "broken clouds":
                    main_screen.setBackgroundResource(R.drawable.clouded);
                    break;
                case "shower rain":
                case "rain":
                    main_screen.setBackgroundResource(R.drawable.rain);
                    break;
                case "thunderstorm":
                    main_screen.setBackgroundResource(R.drawable.thunder);
                    break;
                case "snow":
                    main_screen.setBackgroundResource(R.drawable.snow);
                    break;
                case "mist":
                    main_screen.setBackgroundResource(R.drawable.fog);
                    break;
                default:
                    if (temp > 25) {
                        main_screen.setBackgroundResource(R.drawable.hot);
                        break;
                    }

                    // case "sun" included here
                    main_screen.setBackgroundResource(R.drawable.sun);
                    break;
            }
        }
    }
}