package com.cdol.cafecom_v1;

import android.content.Context;

/**
 * Created by cdol on 2017. 4. 11..
 */

public class OpenHour {
    boolean isClose = false;
    String dayString = "";
    String close_time = "";
    String open_time = "";
    String close_time_str = "";
    String open_time_str = "";

    public void setDay(int day, Context context) {
        switch (day){
            case 0: this.dayString = context.getString(R.string.Sunday); break;
            case 1: this.dayString = context.getString(R.string.Monday); break;
            case 2: this.dayString = context.getString(R.string.Tuesday); break;
            case 3: this.dayString = context.getString(R.string.Wednesday); break;
            case 4: this.dayString = context.getString(R.string.Thursday); break;
            case 5: this.dayString = context.getString(R.string.Friday); break;
            case 6: this.dayString = context.getString(R.string.Saturday); break;
        }
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public String getDayString() {
        return dayString;
    }

    public void setDayString(String dayString) {
        this.dayString = dayString;
    }

    public void setClose_time(String close_time) {
        this.close_time = close_time;
        setClose_time_str(close_time);
    }

    public String getClose_time() {
        return close_time;
    }

    public void setOpen_time(String open_time) {
        this.open_time = open_time;
        setOpen_time_str(open_time);
    }

    public String getOpen_time() {
        return open_time;
    }

    public String getClose_time_str() {
        return close_time_str;
    }

    public void setClose_time_str(String close_time_str) {
        StringBuffer sb = new StringBuffer(close_time_str);
        sb.insert(2, ":");
        this.close_time_str = sb.toString();
    }

    public String getOpen_time_str() {
        return open_time_str;
    }

    public void setOpen_time_str(String open_time_str) {
        StringBuffer sb = new StringBuffer(open_time_str);
        sb.insert(2, ":");
        this.open_time_str = sb.toString();
    }

    public String getFullStr(){
        if(this.isClose){
            return this.getDayString() + ": " + R.string.closed;
        } else {
            return this.getDayString() + ": " + this.getOpen_time_str() + " ~ " + this.getClose_time_str();
        }
    }
}
