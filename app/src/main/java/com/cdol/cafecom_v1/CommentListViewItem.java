package com.cdol.cafecom_v1;

public class CommentListViewItem {
    private String iconDrawable ;
    private int no;
    private String nameStr ;
    private String commentStr ;
    private String dateStr ;
    private float rating;

    public String getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(String iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getNameStr() {
        return nameStr;
    }

    public void setNameStr(String nameStr) {
        this.nameStr = nameStr;
    }

    public String getCommentStr() {
        return commentStr;
    }

    public void setCommentStr(String commentStr) {
        this.commentStr = commentStr;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
