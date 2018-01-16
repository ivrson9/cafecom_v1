package com.cdol.cafecom_v1;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class CafeViewFragment extends Fragment {

    Cafe cafe = null;
    MapView mMapView;
    ListView listView;
    CommentListViewAdapter commentListAdapter;
    private GoogleMap googleMap;
    private ArrayList<Comment> commentList;
    private String uri;
    private ImageView bookmark_add;
    private int dayofweek;
    private OpenHour today;
    private SharedPreferences session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("DetailFragment", "onCreate()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("DetailFragment", "onActivityCreated()");

        commentList = new ArrayList();
        listView = (ListView) getView().findViewById(R.id.commentList);
        int cafeNo = cafe.getNo();

        session = ((MainActivity)getActivity()).auto;

        uri = getActivity().getString(R.string.url);
        getData(uri, cafeNo, "comment");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("DetailFragment", "onCreateView()");
        final View view = inflater.inflate(R.layout.fragment_cafe_view, container, false);
        setHasOptionsMenu(true);

        if(cafe != null) {
            TextView name = (TextView) view.findViewById(R.id.cafeName);
            TextView address = (TextView) view.findViewById(R.id.cafeAddress);
            TextView lanlog = (TextView) view.findViewById(R.id.cafeLatlng);
            TextView opening_hour = (TextView) view.findViewById(R.id.opening_hour);
            TextView open_now = (TextView) view.findViewById(R.id.open_now);
            RatingBar rating = (RatingBar) view.findViewById(R.id.cafeRating);
            TextView wifi = (TextView) view.findViewById(R.id.cafeWifi);
            TextView power = (TextView) view.findViewById(R.id.cafePower);
            TextView comment_write = (TextView) view.findViewById(R.id.cafe_comment_layout);
            ImageView cafe_share = (ImageView) view.findViewById(R.id.cafeShare);
            bookmark_add = (ImageView) view.findViewById(R.id.cafeBookmark);

            name.setText(cafe.getName());
            address.setText(cafe.getAddress());
            lanlog.setText(cafe.getLatitude() + ", " + cafe.getLongitude());

            // Opening_Hour Setting
            try {
                JSONObject open_json = new JSONObject(cafe.getOpening_hours());
                final JSONArray weekday_text = open_json.getJSONArray("periods");
                Calendar oCalendar = Calendar.getInstance();  // 현재 날짜/시간 등의 각종 정보 얻기
                dayofweek = oCalendar.get(Calendar.DAY_OF_WEEK); // Today

                // 월요일 시작으로 변경
//                if(dayofweek == 1) dayofweek = 6;
//                else dayofweek -= 2;

                // Set OpenHour
                final ArrayList<OpenHour> open_list = new ArrayList();
                int j = 0;
                for(int i=0 ; i < 7 ; i++){
                    JSONObject day = (JSONObject)weekday_text.get(j);
                    JSONObject day_close = day.getJSONObject("close");
                    JSONObject day_open = day.getJSONObject("open");

                    OpenHour openHour = new OpenHour();
                    openHour.setDay(i, view.getContext());
                    if(Integer.parseInt(day_open.getString("day")) != i){
                        openHour.setClose(true);
                    } else {
                        openHour.setClose_time(day_close.getString("time"));
                        openHour.setOpen_time(day_open.getString("time"));
                        j++;
                    }

                    open_list.add(openHour);
                }

                today = open_list.get(dayofweek);

                opening_hour.setText(today.getFullStr());
                opening_hour.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View layout = inflater.inflate(R.layout.activity_open_dialog,(ViewGroup) view.findViewById(R.id.dialog));
                        try {
                            ((TextView)layout.findViewById(R.id.openhour_1)).setText(open_list.get(0).getFullStr());
                            ((TextView)layout.findViewById(R.id.openhour_2)).setText(open_list.get(1).getFullStr());
                            ((TextView)layout.findViewById(R.id.openhour_3)).setText(open_list.get(2).getFullStr());
                            ((TextView)layout.findViewById(R.id.openhour_4)).setText(open_list.get(3).getFullStr());
                            ((TextView)layout.findViewById(R.id.openhour_5)).setText(open_list.get(4).getFullStr());
                            ((TextView)layout.findViewById(R.id.openhour_6)).setText(open_list.get(5).getFullStr());
                            ((TextView)layout.findViewById(R.id.openhour_7)).setText(open_list.get(6).getFullStr());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());

                        alertDialogBuilder.setTitle(R.string.openingHours);
                        alertDialogBuilder.setView(layout);

                        // AlertDialog 셋팅
                        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });

                        // 다이얼로그 생성
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // 다이얼로그 보여주기
                        alertDialog.show();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Open Now
            SimpleDateFormat transFormat = new SimpleDateFormat("HHmm");
            try {
                Date currentTime = new Date(System.currentTimeMillis());
                int current = Integer.parseInt(transFormat.format(currentTime));
                int openTime = Integer.parseInt(today.getOpen_time());
                int closeTime = Integer.parseInt(today.getClose_time());

                // 새벽 0 ~ 6시 사이에 종료 할 경우
                if(closeTime >= 0 && closeTime <= 600){
                    closeTime += 2400;
                }

                if(current >= openTime && current <= closeTime){
                    open_now.setText(R.string.open);
                    open_now.setTextColor(ContextCompat.getColor(getActivity(), R.color.openNow));
                } else {
                    open_now.setText(R.string.close);
                    open_now.setTextColor(ContextCompat.getColor(getActivity(), R.color.closed));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Rating custom
            rating.setRating(cafe.getRating());

            // Features
            switch (cafe.getWifi()){
                case 3: wifi.setText(R.string.featuresFree); break;
                case 2: wifi.setText(R.string.featuresRestricted); break;
                case 1: wifi.setText(R.string.featuresPay); break;
                case 0: wifi.setText(R.string.featuresNone); break;
            }

            switch (cafe.getPower()){
                case 2: power.setText(R.string.featuresMany); break;
                case 1: power.setText(R.string.featuresFew); break;
                case 0: power.setText(R.string.featuresNone); break;
            }

            cafe_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, cafe.getName());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, cafe.getAddress());
                    shareIntent.putExtra(Intent.EXTRA_TITLE, R.string.app_name);
                    shareIntent.setType("text/plain");

                    String sharing = getString(R.string.sharing);
                    startActivity(Intent.createChooser(shareIntent, sharing));
                }
            });

            // Bookmark
            changeBookmarkImg(cafe.isBookmark());
            bookmark_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(session.getString("inputEmail", "").equals("")){
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();     //닫기
                            }
                        });
                        alert.setMessage(R.string.needLogin);
                        alert.show();
                    } else {
                        getData(uri, cafe.getNo(), "bookmark");
                    }
                }
            });

            // Write Comment
            comment_write.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });

                    int i = 0;
                    for (Comment c : commentList){
                        if(session.getString("inputEmail", "").equals("")){
                            alert.setMessage(R.string.needLogin);
                            alert.show();
                            break;
                        }
                        if(c.getEmail().equals(session.getString("inputEmail",""))){
                            Intent intent = new Intent(getActivity(), CommentWriteActivity.class);
                            intent.putExtra("cafeNo", cafe.getNo());
                            intent.putExtra("fn", 1);
                            intent.putExtra("comment", c.getComment());
                            intent.putExtra("rating", c.getRating());
                            getActivity().startActivityForResult(intent,1);
                            break;
                        }
                        i++;
                    }
                    if(i == commentList.size()){
                        Intent intent = new Intent(getActivity(), CommentWriteActivity.class);
                        intent.putExtra("cafeNo", cafe.getNo());
                        intent.putExtra("fn", 0);
                        getActivity().startActivityForResult(intent,1);
                    }
                }
            });
        }

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setScrollGesturesEnabled(false); // 맵 이동 불가
                uiSettings.setZoomGesturesEnabled(true);   // 맵 확대 불가

                LatLng cafeLatlng = new LatLng(cafe.getLatitude(), cafe.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(cafeLatlng));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cafeLatlng, 16));

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        String latLngStr =  (String.valueOf(cafe.getLatitude())+","+String.valueOf(cafe.getLongitude()));
                        Bundle bundle = new Bundle(1); // 파라미터는 전달할 데이터 개수
                        bundle.putString("latLngStr", latLngStr); // key , value
                        ((MainActivity)getActivity()).doFragmentTransaction("MapFragment", bundle);
                    }
                });
                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        ClipboardManager clipboardManager = (ClipboardManager)getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("label", cafe.getAddress());
                        clipboardManager.setPrimaryClip(clipData);

                        Toast.makeText(getActivity(), R.string.copyAddress, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setCafeContent(Cafe c) {
        cafe = c;
    }

    private void displayListView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        commentListAdapter = new CommentListViewAdapter(this);
        for (Comment c : commentList) {
            commentListAdapter.addItem(c.getPhoto(), c.getNo(), c.getEmail(), c.getName(), c.getComment(), dateFormat.format(c.getComment_date()), c.getRating());
        }

        listView.setAdapter(commentListAdapter);
        listView.setVerticalScrollBarEnabled(false);
        setListViewHeightBasedOnChildren();

        //enables filtering for the contents of the given ListView
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View listItem = commentListAdapter.getView(position, null, listView);
                listItem.setMinimumHeight(500+100);

                setListViewHeightBasedOnChildren();
            }
        });

    }

    protected void displayNoneList(){
        //listView.setEmptyView(getView().findViewById(android.R.id.empty));
        NoneListViewAdapter noneListViewAdapter = new NoneListViewAdapter();
        noneListViewAdapter.addItem("None Comment");

        listView.setAdapter(noneListViewAdapter);
        listView.setVerticalScrollBarEnabled(false);

        //enables filtering for the contents of the given ListView
        listView.setTextFilterEnabled(true);
    }

    public void getData(String url, final int cafeNo, final String fn) {
        class GetDataJSON extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];

                if(fn == "comment") {
                    // location insert (get)
                    uri = uri + "getData?fn=comL";
                    uri = uri + "&no=" + String.valueOf(cafeNo);
                } else if(fn == "bookmark"){
                    uri = uri + "getData?fn=userBA";
                    uri = uri + "&no=" + String.valueOf(cafeNo);
                    uri = uri + "&email=" + session.getString("inputEmail", "");
                } else if(fn == "commentD"){
                    uri += "getData?fn=comD";
                    uri += "&commentNo=" + String.valueOf(cafeNo);
                }
                Log.v("URL", uri);
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    //con.setConnectTimeout(1000);

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    Log.d("!!!", sb.toString());
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    if(fn == "comment") {
                        try {
                            JSONArray commentJson = new JSONObject(result).getJSONArray("result");
                            if(commentJson.length() == 0){
                                displayNoneList();
                            } else {
                                for (int i = 0; i < commentJson.length(); i++) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    JSONObject c = commentJson.getJSONObject(i);

                                    Comment comment = new Comment();

                                    comment.setNo(c.getInt((String) getText(R.string.commentNo)));
                                    comment.setEmail(c.getString((String)getText(R.string.commentEmail)));
                                    comment.setName(c.getString((String) getText(R.string.commentName)));
                                    comment.setComment(c.getString((String) getText(R.string.comment)));
                                    comment.setRating((float) c.getDouble((String) getText(R.string.userRating)));
                                    comment.setComment_date(dateFormat.parse(c.getString((String) getText(R.string.commentDate))));
                                    comment.setPhoto(c.getString((String) getText(R.string.userPhoto)));

                                    commentList.add(comment);
                                }
                                displayListView();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (fn == "bookmark") {
                        try {
                            JSONObject c = new JSONObject(result);
                            String list_string = session.getString("bookmark", "");
                            ArrayList<String> list;
                            if(list_string.equals("")){
                                list = new ArrayList();
                            } else {
                                list = new ArrayList(Arrays.asList(list_string.split(",")));
                            }

                            if(c.getString("result").equals("Already")){
                                changeBookmarkImg(false);
                                list.remove(String.valueOf(cafeNo));
                                Toast.makeText(getActivity(), "Bookmark cancel", Toast.LENGTH_LONG).show();
                            } else {
                                changeBookmarkImg(true);
                                list.add(String.valueOf(cafeNo));
                                Toast.makeText(getActivity(), "Bookmark Add", Toast.LENGTH_LONG).show();
                            }

                            SharedPreferences.Editor edit = session.edit();
                            edit.putString("bookmark",
                                    list.size()==1 ? list.get(0) : android.text.TextUtils.join(",", list)); // ArrayList to String
                            edit.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (fn == "commentD"){
                        commentListAdapter.notifyDataSetChanged();
                        listView.setAdapter(commentListAdapter);
                    }
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }

    // 아이템 개수에 따라 높이 조절
    public void setListViewHeightBasedOnChildren() {

        int totalHeight = 0;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < commentList.size(); i++) {
            View listItem = commentListAdapter.getView(i, null, listView);
            //listItem.measure(0, 0);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += listItem.getMeasuredHeight();
            //totalHeight += 300;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    public void changeBookmarkImg(boolean set){
        if(set){
            bookmark_add.setImageResource(R.mipmap.ic_bookmark_red_30);
        } else {
            bookmark_add.setImageResource(R.mipmap.ic_bookmark_30);
        }
    }

}
