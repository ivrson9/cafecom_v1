package com.cdol.cafecom_v1;

import android.graphics.drawable.Drawable;
import android.view.View;

public class CafeListViewItem {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String addressStr ;
    private float ratingDouble ;
    private String distanceStr ;
    private int bookmarkVisibility;

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
    public void setBookmarkVisibility(boolean bookmarkVisibility) {
        if(bookmarkVisibility) {
            this.bookmarkVisibility = View.VISIBLE;
        } else {
            this.bookmarkVisibility = View.GONE;
        }
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
    public int getBookmarkVisibility() {
        return bookmarkVisibility;
    }
}
