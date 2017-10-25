package com.cdol.cafecom_v1;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class Cafe {
    private int no = 0;
    private String name = "";
    private String address = "";
    private String zipcode = "";
    private double latitude = 0;
    private double longitude = 0;
    private float rating = 0;
    private int wifi = 0;
    private int power = 0;
    private String opening_hours = "";
    private boolean isBookmark = false;

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getNo() { return no; }

    public void setNo(int no) { this.no = no; }

    public float getRating() { return rating; }

    public void setRating(float rating) { this.rating = rating; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpening_hours() {
        return opening_hours;
    }

    public void setOpening_hours(String opening_hours) {
        this.opening_hours = opening_hours;
    }

    public int getWifi() { return wifi; }

    public void setWifi(int wifi) { this.wifi = wifi; }

    public int getPower() { return power; }

    public void setPower(int power) { this.power = power; }

    public boolean isBookmark() { return isBookmark; }

    public void setIsBookmark(boolean isBookmark) { this.isBookmark = isBookmark; }

    // 현재 위치값 가져와서 셋팅
    public void setGpsCurrent(Context context) {
        GpsInfo gps = new GpsInfo(context);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            setLatitude(gps.getLatitude());
            setLongitude(gps.getLongitude());
        } else {
            gps.showSettingsAlert();
        }
    }


    public void searchAddress(Context context){
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList = null;

        if(this.getLatitude() == 0 && this.getLongitude() == 0){
            setGpsCurrent(context);
        } else {
            try {
                addressList = geocoder.getFromLocation(
                        latitude, // 위도
                        longitude, // 경도
                        10); // 얻어올 값의 개수
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
            }
            if (addressList != null) {
                if (addressList.size() == 0) {
                    Toast.makeText(context, "해당되는 주소 정보는 없습니다", Toast.LENGTH_LONG).show();
                } else {
                    setZipcode(addressList.get(0).getPostalCode().toString());
                }
            }
        }
    }
}

