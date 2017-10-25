package com.cdol.cafecom_v1;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class MyLocation {
    Context context = null;
    double latitude = 0.0d;
    double longitude = 0.0d;
    String zipcode = "";
    String address = "";
    GpsInfo gps = null;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public boolean isAvailable(Context context){
        gps = new GpsInfo(context);
        return gps.isGetLocation();
    }

    // 현재 위치값 가져와서 셋팅

    public void setGpsCurrent(Context context) {
        // GPS 사용유무 가져오기
        if (isAvailable(context)) {
            setLatitude(gps.getLatitude());
            setLongitude(gps.getLongitude());
//            setLatitude(52.522773);
//            setLongitude(13.409722);
        } else {
            gps.showSettingsAlert();
        }
    }


    public void searchAddress(Context context){
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList = null;

        setGpsCurrent(context);

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
                setZipcode(addressList.get(0).getPostalCode());

            }
        }
    }
}
