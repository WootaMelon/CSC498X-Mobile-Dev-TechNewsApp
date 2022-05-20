package com.mehdi.technewsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String[] articleIDs;
    String title = "";
    String siteurl = "";
    ArrayAdapter<String> adapter;
    SQLiteDatabase db;

    public class DownloadhtmlTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null) {
                    result += line;

                }


                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }


        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null) {
                    result += line;

                }


                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }


        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject json = new JSONObject(s);
                title = json.getString("title");
                title = title.replace("'", "");
                siteurl = json.getString("url");
                Log.i("Title of Site: ", title);
                Log.i("siteurl: ", siteurl);
                db.execSQL("INSERT INTO news(news_name, news_url) VALUES('" + title + "','" + siteurl + "')");

                db.execSQL("INSERT INTO news(news_name, news_url) VALUES('" + title + "','" + siteurl + "')");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }



    public void fetchArticleIDs() {
        DownloadTask idtask = new DownloadTask();

        String result = null;
        try {

            result = idtask.execute("https://hacker-news.firebaseio.com/v0/topstories.json").get();
            articleIDs = result.split(",");
            String x = articleIDs[0].substring(1, articleIDs[0].length());
            articleIDs[0] = x;
            for (int i = 10; i < 30; i++) {
                String nb = articleIDs[i];
                db.execSQL("INSERT INTO articlenbs(ID) VALUES('" + nb + "')");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchJSONs() {
        DownloadTask jsontask = new DownloadTask();
        try {
            for (int i = 10; i < 30; i++) {
                String apiURL = "https://hacker-news.firebaseio.com/v0/item/" + articleIDs[i] + ".json?print=pretty";
                jsontask.execute(apiURL);
                jsontask = new DownloadTask();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = this.openOrCreateDatabase("TechNewsDB", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS news (news_name VARCHAR, news_url VARCHAR)");
        db.execSQL("CREATE TABLE IF NOT EXISTS articlenbs (ID VARCHAR)");

//        Attempting to put HTML code to Database
        db.execSQL("CREATE TABLE IF NOT EXISTS articlehtml (htmlsrc VARCHAR)");

        ListView listView = (ListView) findViewById(R.id.technewslist);
        ArrayList<String> articleNames = new ArrayList<>();
        ArrayList<String> articleurls = new ArrayList<>();

        String htmlsourcecode = null;
        ArrayList<String> htmlsources = new ArrayList<>();
        DownloadhtmlTask getSrc = new DownloadhtmlTask();

        fetchArticleIDs();
        fetchJSONs();

        try {
            Cursor c = db.rawQuery("Select * from news", null);
            int newsnameindex = c.getColumnIndex("news_name");
            int newsurlindex = c.getColumnIndex("news_url");
            c.moveToFirst();
            while (c != null) {
                articleNames.add(c.getString(newsnameindex));
                articleurls.add(c.getString(newsurlindex));
                c.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Attempt to put HTML code into Database
        try {

            htmlsourcecode = getSrc.execute(articleurls.get(1)).get();
            Log.i("Source code: ", htmlsourcecode);
            db.execSQL("INSERT INTO articlehtml(htmlsrc) VALUES('" + htmlsourcecode + "')");


        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Cursor c = db.rawQuery("Select * from articlehtml", null);
            int htmlindex = c.getColumnIndex("htmlsrc");
            c.moveToFirst();
            while (c != null) {
                htmlsources.add(c.getString(htmlindex));
                c.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, articleNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), webViewActivity.class);
                intent.putExtra("key", articleurls.get(i));
                startActivity(intent);
            }
        });

    }
    //        try {
//            Cursor c = db.rawQuery("Select * from articlenbs", null);
//            int numberindex = c.getColumnIndex("ID");
//            c.moveToFirst();
//            while (c != null) {
//                listurls.add(c.getString(numberindex));
//                c.moveToNext();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
}