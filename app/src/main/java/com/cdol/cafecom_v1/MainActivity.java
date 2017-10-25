package com.cdol.cafecom_v1;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CafeListFragment.OnItemListener, BookmarkFragment.OnItemListener {

    MyLocation myLocation;
    FragmentManager fragmentManager;
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

        mainFragment();
    }

    public void loginActivity(){
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
        loadActivity();
    }

    public void mainFragment(){
        fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(myLocation.isAvailable(this) && networkInfo != null && networkInfo.isConnected()){
            myLocation.searchAddress(this);
            CafeListFragment listFragment = new CafeListFragment(myLocation);
            ft.add(R.id.mainFragment, listFragment, "MainFragment");
        } else {
            NoneFragment noneFragment = new NoneFragment();
            ft.add(R.id.mainFragment, noneFragment, "NoneFragment");
        }

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
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
        }

        ft.add(R.id.mainFragment, cafeViewFragment, "CafeViewFragment");
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
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Closing Activity")
                        .setMessage("Are you sure you want to close this activity?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.finishAffinity(MainActivity.this);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_zip));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
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
                doFragmentTransaction("MapFragment");
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
                doFragmentTransaction("MapFragment");
            }
        } else if( id == R.id.nav_bookmark){
            if (fragmentManager.findFragmentByTag("BookmarkFragment") == null) {
                doFragmentTransaction("BookmarkFragment");
            }
        } else if (id == R.id.nav_addCafe) {
            Intent intent = new Intent(getBaseContext(), CafeAddActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logOut) {
            auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = auto.edit();
            editor.clear();
            editor.commit();

            for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                fragmentManager.popBackStack();
            }
            loadActivity();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void doFragmentTransaction(String fragment){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        CafeListFragment cafeListFragment = (CafeListFragment)getFragmentManager().findFragmentByTag("MainFragment");
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentByTag("MapFragment");
        BookmarkFragment bookmarkFragment = (BookmarkFragment)getFragmentManager().findFragmentByTag("BookmarkFragment");

        if(cafeListFragment != null && cafeListFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("MainFragment"));
        } else if(mapFragment != null && mapFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("MapFragment"));
        } else if(bookmarkFragment != null && bookmarkFragment.isVisible()){
            ft.hide(fragmentManager.findFragmentByTag("BookmarkFragment"));
        }

        if(fragment.equals("MapFragment")){
            MapFragment f = new MapFragment(myLocation);
            ft.add(R.id.mainFragment, f, "MapFragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        } else if(fragment.equals("BookmarkFragment")){
            BookmarkFragment f = new BookmarkFragment(myLocation);
            ft.add(R.id.mainFragment, f, "BookmarkFragment");
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
