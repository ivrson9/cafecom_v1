package com.cdol.cafecom_v1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cdol on 2017. 3. 13..
 *
 * CommentWrite(Popup)
 */

public class CommentWriteActivity extends Activity {

    private String uri;
    private CafeViewFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
        layoutParams.flags= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount= 0.7f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.dialog);

        Intent intent = getIntent();
        final int cafeNo = intent.getExtras().getInt("cafeNo");
        // 0:add, 1:edit
        final int fn = intent.getExtras().getInt("fn");
        final TextView id = (TextView) findViewById(R.id.comment_id);
        final EditText com = (EditText) findViewById(R.id.comment_text);
        final RatingBar rate = (RatingBar) findViewById(R.id.cafeRating);
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        final String email = auto.getString("inputEmail", "");
        Button submit = (Button) findViewById(R.id.comment_submit_btn);
        Button cancel = (Button) findViewById(R.id.comment_cancel_btn);

        id.setText(auto.getString("inputName", ""));
        if(fn == 1){
            com.setText(intent.getExtras().getString("comment"));
            rate.setRating(intent.getExtras().getFloat("rating"));
        }
        uri = getString(R.string.url);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentId = email;
                String comment = com.getText().toString();
                float rating = rate.getRating();

                addComment(uri, cafeNo, fn, commentId, comment, rating);

                Intent returnIntent = new Intent();
                //returnIntent.putExtra("result",result); // 반환값
                setResult(Activity.RESULT_OK,returnIntent);
                finish();

//                Intent in = new Intent(CommentWriteActivity.this, MainActivity.class);
//                startActivity(in);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void addComment(String url, final int no, final int fn, final String id, final String comment, final float rating) {
        class AddData extends AsyncTask<String, String, String> {
            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];

                // location insert (get)
                if(fn == 0){
                    uri += "getData?fn=comA";
                } else if (fn == 1){
                    uri += "getData?fn=comM";
                }
                uri += "&no=" + String.valueOf(no);
                uri += "&id=" + id;
                uri += "&comment=" + comment;
                uri += "&rating=" + rating;
                Log.v("URL", uri);
                BufferedReader bufferedReader = null;
                try {
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

            }
        }
        AddData a = new AddData();
        a.execute(url);

    }
}
