package com.example.administrator.chinaweather10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import android.widget.LinearLayout.LayoutParams;

public class WeatherActivity extends AppCompatActivity implements Runnable{
    HttpURLConnection httpURLConnection = null;
    InputStream din = null;
    Vector<String> cityname = new Vector<String>();
    Vector<String> low = new Vector<String>();
    Vector<String> high = new Vector<String>();
    Vector<String> icon = new Vector<String>();
    Vector<Bitmap> bitmap = new Vector<Bitmap>();
    Vector<String> summary = new Vector<String>();
    int weatherIndex[] = new int[20];
    String city = "guangzhou";
    boolean bPress = false;
    boolean bHasData = false;
    LinearLayout body;
    Button search;
    EditText value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        setTitle("天气查询");
        body = (LinearLayout) findViewById(R.id.show);
        search = (Button) findViewById(R.id.search);
        value = (EditText) findViewById(R.id.city_name);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                body.removeAllViews();
                city = value.getText().toString();
                Toast.makeText(WeatherActivity.this,"正在查询天气信息...",Toast.LENGTH_LONG).show();
                Thread th = new Thread(WeatherActivity.this);
                th.start();
            }
        });
    }

    @Override
    public void run() {
        cityname.removeAllElements();
        low.removeAllElements();
        high.removeAllElements();
        icon.removeAllElements();
        bitmap.removeAllElements();
        summary.removeAllElements();
        parseData();
        downImage();
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }
    public void parseData(){
        int i = 0;
        String sValue;
        String weatherUrl = "http://flash.weather.com.cn/wmaps/xml/"+city+".xml";
        String weatherIcon = "http://m.weather.com.cn/img/c";
        try{
            URL url = new URL(weatherUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            din = httpURLConnection.getInputStream();
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(din,"UTF-8");
            int evtType = xmlPullParser.getEventType();
            while(evtType!=XmlPullParser.END_DOCUMENT){
                switch (evtType){
                    case XmlPullParser.START_TAG:
                        String tag = xmlPullParser.getName();
                        if(tag.equalsIgnoreCase("city")){
                            cityname.addElement(xmlPullParser.getAttributeValue(null,"cityname")+"天气：");
                            summary.addElement(xmlPullParser.getAttributeValue(null,"stateDetailed"));
                            low.addElement("最低："+xmlPullParser.getAttributeValue(null,"tem2"));
                            high.addElement("最高："+xmlPullParser.getAttributeValue(null,"tem1"));
                            icon.addElement(weatherIcon+xmlPullParser.getAttributeValue(null,"state1")+".gif");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        default:
                            break;
                }
                evtType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void downImage(){
        int i = 0;
        for(i=0;i<icon.size();i++){
            try{
                URL url = new URL(icon.elementAt(i));
                System.out.println(icon.elementAt(i));
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                din = httpURLConnection.getInputStream();
                bitmap.addElement(BitmapFactory.decodeStream(httpURLConnection.getInputStream()));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    din.close();
                    httpURLConnection.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    private final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    showData();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public void showData(){
        body.removeAllViews();
        body.setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.weight = 80;
        params.height = 50;
        for(int i = 0;i<cityname.size();i++){
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            //1
            TextView dayView = new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(cityname.elementAt(i));
            linearLayout.addView(dayView);
            //2
            TextView summaryView = new TextView(this);
            summaryView.setLayoutParams(params);
            summaryView.setText(summary.elementAt(i));
            linearLayout.addView(summaryView);
            //3
            ImageView icon = new ImageView(this);
            icon.setLayoutParams(params);
            icon.setImageBitmap(bitmap.elementAt(i));
            linearLayout.addView(icon);
            //4
            TextView lowView = new TextView(this);
            lowView.setLayoutParams(params);
            lowView.setText(low.elementAt(i));
            linearLayout.addView(lowView);
            //5
            TextView highView = new TextView(this);
            highView.setLayoutParams(params);
            highView.setText(high.elementAt(i));
            linearLayout.addView(highView);
            //6
            body.addView(linearLayout);
        }
    }
}
