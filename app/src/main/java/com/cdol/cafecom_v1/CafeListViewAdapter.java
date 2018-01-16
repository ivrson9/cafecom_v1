package com.cdol.cafecom_v1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class CafeListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<CafeListViewItem> cafeListViewItemList = new ArrayList<CafeListViewItem>() ;

    // ListViewAdapter의 생성자
    public CafeListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return cafeListViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        ViewHolder viewHolder;

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cafe_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CafeListViewItem listViewItem = cafeListViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        viewHolder.titleTextView.setText(listViewItem.getTitle());
        viewHolder.addressTextView.setText(listViewItem.getAddressStr());
        viewHolder.distanceTextView.setText(listViewItem.getDistance());
        viewHolder.ratingRatingBar.setRating(listViewItem.getRatingFloat());
        viewHolder.isBookmarkImage.setVisibility(listViewItem.getBookmarkVisibility());

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
        return cafeListViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String title, String address, float rating, String distance, boolean isBookmark) {
        CafeListViewItem item = new CafeListViewItem();

        item.setTitle(title);
        item.setAddressStr(address);
        item.setRatingFloat(rating);
        item.setDistance(distance);
        item.setBookmarkVisibility(isBookmark);

        cafeListViewItemList.add(item);
    }

    private static class ViewHolder {
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView titleTextView;
        TextView addressTextView;
        TextView distanceTextView;
        RatingBar ratingRatingBar;
        ImageView isBookmarkImage;

        public ViewHolder(View view) {
            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            titleTextView = (TextView) view.findViewById(R.id.id) ;
            addressTextView = (TextView) view.findViewById(R.id.name) ;
            distanceTextView = (TextView) view.findViewById(R.id.distance) ;
            ratingRatingBar = (RatingBar) view.findViewById(R.id.rate);
            isBookmarkImage = (ImageView) view.findViewById(R.id.is_bookmark) ;
        }
    }
}

