package com.cdol.cafecom_v1;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by cdol on 2017. 2. 24..
 *
 * Map Fragment
 *
 * 초기에는 현재 위치 기준 1km 주변으로 검색
 * 이후에는 현재 위치가 아닌 맵 센터 가준으로 검색 할 것
 * 결과 데이터가 작을시 지도 축소 할 것
 */
public class MapFragment extends Fragment {
    MapView mMapView;
    private GoogleMap googleMap;
    private MyLocation myLocation;
    private ArrayList<Cafe> cafeList;
    private String uri;
    private ProgressDialog mProgressDialog;
    private LatLng searchLatLng;
    private static final String TAG_RESULTS="result";

    public MapFragment(MyLocation location) {
        myLocation = location;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MapFragment", "onCreate()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("MapFragment", "onActivityCreated().");
        Log.v("ListsavedInstanceState", savedInstanceState == null ? "true" : "false");

        cafeList = new ArrayList();

        uri = getActivity().getString(R.string.url);
        getData(uri, "cafeL", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("MapFragment", "onCreateView()");
        Log.v("MapContainer", container == null ? "true" : "false");
        Log.v("MapsavedInstanceState", savedInstanceState == null ? "true" : "false");
        if (container == null) {
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        setHasOptionsMenu(true);

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
                String latLngStr = "";
                LatLng getLatLng = null;

                try{
                    latLngStr = getArguments().getString("latLngStr");
                    String[] latLngTmp =  (latLngStr).split(",");
                    getLatLng = new LatLng(Double.parseDouble(latLngTmp[0]), Double.parseDouble(latLngTmp[1]));

                }catch (Exception e){
                    ;
                }



                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
                googleMap.setMyLocationEnabled(true);

                googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

                LatLng myLatlng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                if(!latLngStr.equals("")){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng, 16));
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng, 16));
                }

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16), 500, null);
                        marker.showInfoWindow();
                        return true;
                    }
                });

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        CafeViewFragment cafeViewFragment = new CafeViewFragment();
                        cafeViewFragment.setCafeContent((Cafe) marker.getTag());
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.hide(((MainActivity)getActivity()).fragmentManager.findFragmentByTag("MapFragment"));
                        ft.add(R.id.mainFragment, cafeViewFragment, "CafeViewFragment");
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });
            }
        });

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.map, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchView mapSearchView = (SearchView) menu.findItem(R.id.map_search).getActionView();
        mapSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mapSearchView.setQueryHint(getString(R.string.search_zip));
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getData(uri, "googleGeo", s);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 15), 500, null);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mProgressDialog.dismiss();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void mapMarking(ArrayList<Cafe> list){
        for (Cafe c : list){
            LatLng position = new LatLng(c.getLatitude(), c.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(c.getName() + "|" + String.valueOf(c.getRating()));
            markerOptions.snippet(c.getAddress());
            markerOptions.position(position);

//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), marker_root_view)));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_coffee_marker));

            googleMap.addMarker(markerOptions).setTag(c);
        }
    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void getData(String url, final String fn, final String searchTxt) {
        class GetDataJSON extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];

                // location insert (get)
                if(fn.equals("cafeL")) {
                    uri = uri + "getData?fn=cafeL";
                    uri = uri + "&lat=" + myLocation.getLatitude();
                    uri = uri + "&lng=" + myLocation.getLongitude();
                } else if (fn.equals("googleGeo")){
                    uri = uri + "getData?fn=googleGeo";
                    uri = uri + "&address=" + searchTxt;
                }
                Log.v("URL", uri);
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    con.setConnectTimeout(1000);

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                hideProgressDialog();
                if (result != null) {
                    if(fn.equals("cafeL")) {
                        if (isAdded()) {
                            try {
                                JSONArray cafeJson = new JSONObject(result).getJSONArray(TAG_RESULTS);

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

                                    if (c.getInt((String) getText(R.string.itemRating)) == 10) {
                                        cafe.setRating(0);
                                    } else {
                                        cafe.setRating(c.getInt((String) getText(R.string.itemRating)));
                                    }

                                    cafeList.add(cafe);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (fn.equals("googleGeo")){
                        try {
                            JSONArray geoJson = new JSONObject(result).getJSONArray(TAG_RESULTS);
                            JSONObject geo = geoJson.getJSONObject(0);
                            double lat = Double.parseDouble(geo.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                            double lng = Double.parseDouble(geo.getJSONObject("geometry").getJSONObject("location").getString("lng"));

                            searchLatLng = new LatLng(lat, lng);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                mapMarking(cafeList);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getActivity().getLayoutInflater().inflate(R.layout.map_custom_infowindow, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            String data = marker.getTitle();
            String name = data.substring(0, data.indexOf("|"));
            float rating = Float.parseFloat(data.substring(data.indexOf("|")+1));
            String address = marker.getSnippet();

            TextView infoTitle = ((TextView)myContentsView.findViewById(R.id.infoTitle));
            infoTitle.setText(name);
            TextView infoSnippet = ((TextView)myContentsView.findViewById(R.id.infoSnippet));
            infoSnippet.setText(address);
            RatingBar infoRating = (RatingBar)myContentsView.findViewById(R.id.infoRate);
            infoRating.setRating(rating);

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
