package com.cdol.cafecom_v1;

import java.util.Date;

/**
 * Created by cdol on 2017. 2. 27..
 */

public class Comment {
    private int no = 0;
    private String name = "";
    private String comment = "";
    private Date comment_date = null;
    private String photo = "";
    private float rateing = 0;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getComment_date() {
        return comment_date;
    }

    public void setComment_date(Date comment_date) {
        this.comment_date = comment_date;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public float getRateing() {
        return rateing;
    }

    public void setRateing(float rateing) {
        this.rateing = rateing;
    }
}
