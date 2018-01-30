package com.cdol.cafecom_v1;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.baoyz.widget.PullRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by cdol on 2017. 2. 24..
 *
 * CafeList Fragment
 *
 * 현재위치 주변 1km 기준으로 검색
 * Limit 설정 x
 * Distance 안드로이드, 데이터베이스 둘다 계산 중
 */
public class CafeListFragment extends Fragment {

    OnItemListener mListener;
    PullRefreshLayout swipeRefreshLayout;
    ListView listView;
    CafeListViewAdapter cafeListAdapter;
    ArrayAdapter noneAdapter;
    private MyLocation myLocation;
    private String searchText;
    private ArrayList<Cafe> cafeList;
    private String uri;
    private static final String TAG_RESULTS="result";

    public CafeListFragment(MyLocation location, String search) {
        myLocation = location;
        searchText = search;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("CafeListFragment", "onCreate()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("CafeListFragment", "onActivityCreated().");
        Log.v("ListsavedInstanceState", savedInstanceState == null ? "true" : "false");

        swipeRefreshLayout = (PullRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        cafeList = new ArrayList();
        listView = (ListView) getView().findViewById(android.R.id.list);
        // empty List
        //listView.setEmptyView(getView().findViewById(android.R.id.empty));

        uri = getActivity().getString(R.string.url);
        getData(uri);

        // 당겨서 리스트 새로고침
        swipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cafeList = new ArrayList();
                getData(uri);
            }
        });

//        Button listRefresh = (Button) getView().findViewById(R.id.list_refresh);
//        listRefresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onResume();
//            }
//        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("CafeListFragment", "onCreateView()");
        Log.v("ListContainer", container == null ? "true" : "false");
        Log.v("ListsavedInstanceState", savedInstanceState == null ? "true" : "false");
        if (container == null) {
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_cafe_list, container, false);

        return view;
    }


    // Container Activity must implement this interface
    public interface OnItemListener {
        void onItemSelected(Cafe c, int fn);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnURLSelectedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void displayListView() {
        cafeListAdapter = new CafeListViewAdapter();
        for (Cafe c : cafeList) {
            String dis = getDistance(c);
            cafeListAdapter.addItem(c.getName(), c.getAddress(), c.getRating(), dis, c.isBookmark());
        }

        listView.setAdapter(cafeListAdapter);
        //enables filtering for the contents of the given ListView
        //listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Send the URL to the host activity
                //mListener.onItemSelected(((TextView) view).getText().toString());
                mListener.onItemSelected(cafeList.get(position), 0);

            }
        });

    }

    // 거리재기
    public String getDistance(Cafe c){
        String dis = "";
        double distance = 0;
        Location my = new Location("my");
        Location cafe = new Location("cafe");

        my.setLatitude(myLocation.getLatitude());
        my.setLongitude(myLocation.getLongitude());
        cafe.setLatitude(c.getLatitude());
        cafe.setLongitude(c.getLongitude());

        distance = my.distanceTo(cafe);
        // 방위 차후 업데이트
        //Log.d("bearing", String.valueOf(my.bearingTo(cafe)));

        if(distance > 1000) {
            distance /= 1000;
            dis = String.valueOf(String.format("%.1f",distance)) + " km";
        } else {
            dis = String.valueOf((long)distance) + " m";
        }

        return dis;
    }


    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ((MainActivity)getActivity()).showProgressDialog();
            }
            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                // location insert (get)
                uri = uri + "getData?fn=cafeL";
                uri = uri + "&lat=" + myLocation.getLatitude();
                uri = uri + "&lng=" + myLocation.getLongitude();
                if(!searchText.equals("")){
                    uri = uri + "&zip=" + searchText;
                }
                Log.d("URL", uri);

                try {
                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    if(conn != null){
                        conn.setConnectTimeout(10000);
                        conn.setUseCaches(false);
                        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
                            String json;
                            while ((json = br.readLine()) != null) {
                                sb.append(json+"\n");
                            }

                            br.close();
                        }
                        conn.disconnect();
                    }

                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                //((MainActivity)getActivity()).hideProgressDialog();
                if(isAdded()) {
                    try {
                        JSONArray cafeJson = new JSONObject(result).getJSONArray(TAG_RESULTS);
                        String list_string = ((MainActivity)getActivity()).auto.getString("bookmark", "");

                        for (int i = 0; i < cafeJson.length(); i++) {
                            JSONObject c = cafeJson.getJSONObject(i);
                            Cafe cafe = new Cafe();

                            cafe.setNo(c.getInt((String) getText(R.string.itemNo)));
                            cafe.setName(c.getString((String) getText(R.string.itemName)));
                            cafe.setAddress(c.getString((String) getText(R.string.itemAddress)));
                            cafe.setLatitude(Double.parseDouble(c.getString((String) getText(R.string.itemLatitude))));
                            cafe.setLongitude(Double.parseDouble(c.getString((String) getText(R.string.itemLongitude))));
                            cafe.setOpening_hours(c.getString((String) getText(R.string.itemOpeningHours)));
                            cafe.setWifi(c.getInt((String) getText(R.string.itemWifi)));
                            cafe.setPower(c.getInt((String) getText(R.string.itemPower)));

                            // Set isBookmark
                            if(!list_string.equals("")) {
                                ArrayList<String> list = new ArrayList(Arrays.asList(list_string.split(",")));
                                for (int j = 0; j < list.size(); j++) {
                                    if (cafe.getNo() == Integer.parseInt(list.get(j))) {
                                        cafe.setIsBookmark(true);
                                    }
                                }
                            }

                            // Set Rating
                            if(c.getInt((String) getText(R.string.itemRating)) == 10){
                                cafe.setRating(0);
                            } else {
                                cafe.setRating((float)c.getDouble((String) getText(R.string.itemRating)));
                            }


                            cafeList.add(cafe);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    displayListView();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);

    }

    @Override
    public void onDestroy(){
        ((MainActivity)getActivity()).mProgressDialog.dismiss();
        super.onDestroy();
    }
}
