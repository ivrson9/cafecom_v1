package com.cdol.cafecom_v1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CafeAddActivity extends AppCompatActivity {

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.addCafeToolbar);

        final EditText name = (EditText) findViewById(R.id.addCafeName);
        final EditText street = (EditText) findViewById(R.id.addCafeStreet);
        final EditText streetNum = (EditText) findViewById(R.id.addCafeStreetNum);
        final EditText zipcode = (EditText) findViewById(R.id.addCafeZipcode);
        final Spinner city = (Spinner) findViewById(R.id.addCity);
        final Spinner wifi = (Spinner) findViewById(R.id.addCafeWifi);
        final Spinner power = (Spinner) findViewById(R.id.addCafePower);
        Button submit = (Button) findViewById(R.id.addCafeSubmit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().equals("")){
                    Toast.makeText(CafeAddActivity.this, R.string.addCafeName, Toast.LENGTH_LONG).show();
                } else if (street.getText().toString().equals("")){
                    Toast.makeText(CafeAddActivity.this, R.string.addCafeStreet, Toast.LENGTH_LONG).show();
                } else if (streetNum.getText().toString().equals("")){
                    Toast.makeText(CafeAddActivity.this, R.string.addCafeNumber, Toast.LENGTH_LONG).show();
                } else if (zipcode.getText().toString().equals("")){
                    Toast.makeText(CafeAddActivity.this, R.string.addCafeZipcode, Toast.LENGTH_LONG).show();
                } else {
                    AddCafe cafe = new AddCafe();
                    cafe.setName(name.getText().toString());
                    cafe.setStreet(street.getText().toString());
                    cafe.setStreetNum(Integer.parseInt(streetNum.getText().toString()));
                    cafe.setZipcode(Integer.parseInt(zipcode.getText().toString()));
                    cafe.setCity(city.getSelectedItem().toString());
                    cafe.setWifi(wifi.getSelectedItemPosition());
                    cafe.setPower(power.getSelectedItemPosition());

                    cafe.setAddress(cafe.getStreet() + " " + cafe.getStreetNum() + ", " + cafe.getZipcode() + " " + cafe.getCity());

                    addCafeList(getString(R.string.url), cafe.getName(), cafe.getAddress(), cafe.getWifi(), cafe.getPower());
                }
            }
        });
    }

    public void addCafeList(String url, final String name, final String address, final int wifi, final int power) {
        class AddData extends AsyncTask<String, String, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];

                try {
                    // location insert (get)
                    uri += "getData?fn=cafeA";
                    uri += "&name=" + URLEncoder.encode(name, "UTF-8");
                    uri += "&address=" + URLEncoder.encode(address, "UTF-8");
                    uri += "&wifi=" + wifi;
                    uri += "&power=" + power;
                    Log.v("URL", uri);

                    BufferedReader bufferedReader = null;

                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    con.setConnectTimeout(1000);

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

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
                Toast.makeText(CafeAddActivity.this, R.string.addCompleted, Toast.LENGTH_LONG).show();
            }
        }
        AddData a = new AddData();
        a.execute(url);

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
}
