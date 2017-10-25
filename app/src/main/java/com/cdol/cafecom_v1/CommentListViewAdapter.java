package com.cdol.cafecom_v1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by cdol on 2017. 2. 27..
 */

public class CommentListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<CommentListViewItem> commentListViewItemList = new ArrayList<CommentListViewItem>() ;

    // ListViewAdapter의 생성자
    public CommentListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return commentListViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.comment_list_item, parent, false);
            //convertView.setMinimumHeight(500); // item당 높이 설정
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView noTextView = (TextView) convertView.findViewById(R.id.commentNo) ;
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.comment_imageView) ;
        TextView nameTextView = (TextView) convertView.findViewById(R.id.comment_name) ;
        TextView commentTextView = (TextView) convertView.findViewById(R.id.comment_text) ;
        TextView dateTextView = (TextView) convertView.findViewById(R.id.comment_date) ;
        RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.comment_rate) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CommentListViewItem listViewItem = commentListViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        noTextView.setText(String.valueOf(listViewItem.getNo()));
        nameTextView.setText(listViewItem.getNameStr());
        commentTextView.setText(listViewItem.getCommentStr());
        dateTextView.setText(listViewItem.getDateStr());
        ratingBar.setRating(listViewItem.getRating());
        Glide.with(convertView.getContext()).load(listViewItem.getIconDrawable())
                .override(100,100)
                .bitmapTransform(new CropCircleTransformation(convertView.getContext()))
                .into(iconImageView)
                ;

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return commentListViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String icon, int no, String name, String comment, String date, float rate) {
        CommentListViewItem item = new CommentListViewItem();

        item.setNo(no);
        item.setIconDrawable(icon);
        item.setNameStr(name);
        item.setCommentStr(comment);
        item.setDateStr(date);
        item.setRating(rate);

        commentListViewItemList.add(item);
    }
}
