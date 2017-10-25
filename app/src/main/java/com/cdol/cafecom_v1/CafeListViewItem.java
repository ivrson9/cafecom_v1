package com.cdol.cafecom_v1;

import android.graphics.drawable.Drawable;

public class CafeListViewItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String addressStr ;
    private float ratingDouble ;
    private String distanceStr ;
    private boolean isBookmark = false ;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setAddressStr(String address) {
        addressStr = address ;
    }
    public void setRatingFloat(float rating) {
        ratingDouble = rating ;
    }
    public void setDistance(String distance) {
        distanceStr = distance ;
    }
    public void setBookmark(boolean bookmark) {
        isBookmark = bookmark;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getAddressStr() {
        return this.addressStr ;
    }
    public float getRatingFloat() {
        return this.ratingDouble ;
    }
    public String getDistance() {
        return this.distanceStr ;
    }
    public boolean isBookmark() {
        return isBookmark;
    }
}
