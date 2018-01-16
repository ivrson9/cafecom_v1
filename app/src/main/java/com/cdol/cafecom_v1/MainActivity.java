package com.cdol.cafecom_v1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.mommoo.permission.MommooPermission;
import com.mommoo.permission.listener.OnPermissionDenied;
import com.mommoo.permission.listener.OnPermissionGranted;
import com.mommoo.permission.listener.OnUserDirectPermissionDeny;
import com.mommoo.permission.listener.OnUserDirectPermissionGrant;
import com.mommoo.permission.repository.DenyInfo;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CafeListFragment.OnItemListener, BookmarkFragment.OnItemListener, SearchFragment.OnItemListener {

    MyLocation myLocation;
    FragmentManager fragmentManager;
    CafeListFragment cafeListFragment;
    MapFragment mapFragment;
    BookmarkFragment bookmarkFragment;
    SearchFragment searchFragment;
    SearchView listSearchView;
    ProgressDialog mProgressDialog;
    SharedPreferences auto;
    User user;
    ConnectivityManager manager;
    NetworkInfo networkInfo;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MommooPermission.Builder(this)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .setOnUserDirectPermissionGrant(new OnUserDirectPermissionGrant() {
                    @Override
                    public void onUserDirectGrant(List<String> permissionList) {
                        for (String permission : permissionList) System.out.println("userGrant " + permission);
                    }
                })
                .setOnUserDirectPermissionDeny(new OnUserDirectPermissionDeny() {
                    @Override
                    public void onUserDirectDeny(List<DenyInfo> deniedPermissionList) {
                        for (DenyInfo denyInfo : deniedPermissionList){
                            System.out.println("userDeny " + denyInfo.getPermission() +" , userNeverSeeChecked " + denyInfo.isUserNeverAskAgainChecked());
                        }
                    }
                })
                .setOnPermissionDenied(new OnPermissionDenied() {
                    @Override
                    public void onDenied(List<DenyInfo> deniedPermissionList) {
                        for (DenyInfo denyInfo : deniedPermissionList){
                            System.out.println("isDenied " + denyInfo.getPermission() +" , userNeverSeeChecked " + denyInfo.isUserNeverAskAgainChecked());
                        }
                    }
                })
                .setOnPermissionGranted(new OnPermissionGranted() {
                    @Override
                    public void onGranted(List<String> permissionList) {
                        for (String permission : permissionList) System.out.println("granted " + permission);
                    }
                })
                .setPreNoticeDialogData("Pre Notice","Please accept all permission to using this app")
                .setPostNoticeDialogData("Post Notice","If you don't accept permission\nyou have to grant permission directly")
                .setOfferGrantPermissionData("Move To App Setup",
                        "1. Touch the 'SETUP'\n" +
                                "2. Touch the 'Permission' tab\n"+
                                "3. Grant all permissions by dragging toggle button")
                .build()
                .checkPermissions();

        loadActivity();
    }

    private void loadActivity(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        Menu menu = navigationView.getMenu();

        manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = manager.getActiveNetworkInfo();

        // AdMob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        // User Info
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        LinearLayout infoLayout = (LinearLayout) headerView.findViewById(R.id.userInfoLayout);
        LinearLayout loginLayout = (LinearLayout) headerView.findViewById(R.id.loginLayout);
        TextView userEmail = (TextView) headerView.findViewById(R.id.userEmail);
        TextView userName = (TextView) headerView.findViewById(R.id.userName);
        ImageView userPhoto = (ImageView) headerView.findViewById(R.id.userImage);
        Button loginBtn = (Button) headerView.findViewById(R.id.loginBtn);

        user = new User();
        user.setEmail(auto.getString("inputEmail", ""));
        user.setName(auto.getString("inputName", ""));
        user.setPhoto(auto.getString("inputPhoto", ""));
        MenuItem logout = menu.findItem(R.id.nav_logOut);

        if(user.getEmail() == ""){
            infoLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginActivity();
                }
            });
            logout.setVisible(false);
        } else {
            infoLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            logout.setVisible(true);
        }
        userEmail.setText(user.getEmail());
        userName.setText(user.getName());
        Glide.with(this).load(user.getPhoto())
                .bitmapTransform(new CropCircleTransformation(this))
                .into(userPhoto);

        myLocation = new MyLocation();

        mainFragment("");
    }

    public void loginActivity(){
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // commentWriteActivity 종료
        if(requestCode == 1){
            Fragment frg = fragmentManager.findFragmentByTag("CafeViewFragment");
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
        } else {
            for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                fragmentManager.popBackStack();
            }
            loadActivity();
        }
    }

    public void mainFragment(String search){
        fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(myLocation.isAvailable(this) && networkInfo != null && networkInfo.isConnected()){
            myLocation.searchAddress(this);
            cafeListFragment = new CafeListFragment(myLocation, search);
            ft.add(R.id.mainFragment, cafeListFragment, "MainFragment");
        } else {
            NoneFragment noneFragment = new NoneFragment();
            ft.add(R.id.mainFragment, noneFragment, "NoneFragment");
        }

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.popBackStackImmediate();
            } else {
                //Toast.makeText(this, String.valueOf(fragmentManager.getBackStackEntryCount()), Toast.LENGTH_LONG).show();
                String closeAppComment = getString(R.string.closeAppComment);
                String app_name = getString(R.string.app_name);
                new AlertDialog.Builder(this)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.closeApp)
                        .setMessage(String.format(closeAppComment, app_name))
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.finishAffinity(MainActivity.this);
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        listSearchView = (SearchView) menu.findItem(R.id.list_search).getActionView();
        listSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        listSearchView.setQueryHint(getString(R.string.search_zip));
        listSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // 키보드 내림 + 포커스 제거
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                listSearchView.clearFocus();

                if (fragmentManager.findFragmentByTag("SearchFragment") == null) {
                    Bundle bundle = new Bundle(1); // 파라미터는 전달할 데이터 개수
                    bundle.putString("searchZip", s); // key , value
                    doFragmentTransaction("SearchFragment", bundle);
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_google) {
            if(fragmentManager.findFragmentByTag("MapFragment") == null) {
                doFragmentTransaction("MapFragment", null);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_viewList) {
            // List fragment까지 pop
            while (fragmentManager.getBackStackEntryCount() > 1) {
                fragmentManager.popBackStackImmediate();
            }
        } else if (id == R.id.nav_viewMap) {
            if(fragmentManager.findFragmentByTag("MapFragment") == null){
                doFragmentTransaction("MapFragment", null);
            }
        } else if( id == R.id.nav_bookmark){
            if (fragmentManager.findFragmentByTag("BookmarkFragment") == null) {
                doFragmentTransaction("BookmarkFragment", null);
            }
        } else if (id == R.id.nav_addCafe) {
            Intent intent = new Intent(getBaseContext(), CafeAddActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logOut) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setMessage(R.string.logoutComment).setCancelable(false).setPositiveButton(R.string.confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = auto.edit();
                            editor.clear();
                            editor.commit();

                            for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                                fragmentManager.popBackStack();
                            }
                            loadActivity();
                        }
                    }).setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'No'
                            return;
                        }
                    });
            AlertDialog alert = alert_confirm.create();
            alert.show();

        } else if (id == R.id.nav_email_send) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);

            try {
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ivrson9@gmail.com"});

                emailIntent.setType("text/html");
                emailIntent.setPackage("com.google.android.gm");
                if(emailIntent.resolveActivity(getPackageManager())!=null)
                    startActivity(emailIntent);

                startActivity(emailIntent);
            } catch (Exception e) {
                e.printStackTrace();

                emailIntent.setType("text/html");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ivrson9@gmail.com"});

                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemSelected(Cafe c, int fn) {
        Log.v("AndroidFragmentActivity", Integer.toString(c.getNo()));
        FragmentTransaction ft = fragmentManager.beginTransaction();
        CafeViewFragment cafeViewFragment = new CafeViewFragment();
        cafeViewFragment.setCafeContent(c);

        if(fn == 0) {
            ft.hide(fragmentManager.findFragmentByTag("MainFragment"));
        } else if (fn == 1) {
            ft.hide(fragmentManager.findFragmentByTag("BookmarkFragment"));
        } else if (fn == 2) {
            ft.hide(fragmentManager.findFragmentByTag("SearchFragment"));
        }

        ft.add(R.id.mainFragment, cafeViewFragment, "CafeViewFragment");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void doFragmentTransaction(String fragment, Bundle bundle){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        cafeListFragment = (CafeListFragment)getFragmentManager().findFragmentByTag("MainFragment");
        mapFragment = (MapFragment)getFragmentManager().findFragmentByTag("MapFragment");
        bookmarkFragment = (BookmarkFragment)getFragmentManager().findFragmentByTag("BookmarkFragment");
        searchFragment = (SearchFragment)getFragmentManager().findFragmentByTag("SearchFragment");

        if(cafeListFragment != null && cafeListFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("MainFragment"));
        } else if(mapFragment != null && mapFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("MapFragment"));
        } else if(bookmarkFragment != null && bookmarkFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("BookmarkFragment"));
        } else if(searchFragment != null && searchFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("SearchFragment"));
        }

        if(fragment.equals("MapFragment")){
            MapFragment f = new MapFragment(myLocation);
            f.setArguments(bundle); // bundle set
            ft.add(R.id.mainFragment, f, "MapFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        } else if(fragment.equals("BookmarkFragment")){
            BookmarkFragment f = new BookmarkFragment(myLocation);
            f.setArguments(bundle); // bundle set
            ft.add(R.id.mainFragment, f, "BookmarkFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        } else if(fragment.equals("SearchFragment")){
            SearchFragment f = new SearchFragment(myLocation);
            f.setArguments(bundle); // bundle set
            ft.add(R.id.mainFragment, f, "SearchFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onDestroy(){
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
