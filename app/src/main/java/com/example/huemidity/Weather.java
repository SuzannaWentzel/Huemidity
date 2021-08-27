package com.example.huemidity;

import java.util.Date;

public class Weather {
    private Date date;
    private int temp;
    private String description;
    private String humidity;

    public Weather(Date date, int temp, String description, String humidity) {
        this.date = date;
        this.temp = temp;
        this.description = description;
        this.humidity = humidity;
    }

    public Date getDate() {
        return date;
    }

    public int getTemp() {
        return temp;
    }

    public String getDescription() {
        return description;
    }

    public String getHumidity() {
        return humidity;
    }
}
