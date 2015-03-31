package com.example.zim.testapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends ActionBarActivity {
    TextView RUR;
    TextView EUR;
    TextView USD;
    TextView WEATHERTEXT;
    ImageView WEATHER;
    static String privatLink = "https://api.privatbank.ua/p24api/pubinfo?exchange&coursid=5";
    static String weatherLink = "http://api.openweathermap.org/data/2.5/weather?q=Kiev&mode=xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyApplication().onCreate();
        RUR = (TextView)findViewById(R.id.rubtext);
        EUR = (TextView)findViewById(R.id.eurotext);
        USD = (TextView)findViewById(R.id.dollartext);
        WEATHERTEXT = (TextView) findViewById(R.id.weatherText);
        WEATHER = (ImageView)findViewById(R.id.weatherImg);
        new CurrenParsing().execute(privatLink);
        new WeatherParsing().execute(weatherLink);
    }

    class CurrenParsing extends AsyncTask<String, Void, List<Valuta>>{

        @Override
        protected List<Valuta> doInBackground(String... params) {
            publishProgress(new Void[]{});
            ArrayList<Valuta> valList = new ArrayList<>();
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                URL url = new URL(params[0]);
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(200);
                conn.setReadTimeout(200);
                Document doc = db.parse(conn.getInputStream());
                Element element = doc.getDocumentElement();
                for (int i = 0; i < element.getElementsByTagName("row").getLength(); i++) {
                    NamedNodeMap att = element.getElementsByTagName("row").item(i).getChildNodes().item(0).getAttributes();
                    String val = att.getNamedItem("ccy").getNodeValue();
                    String buy = att.getNamedItem("buy").getNodeValue().substring(0, 5);
                    String sale = att.getNamedItem("sale").getNodeValue().substring(0, 5);
                    valList.add(new Valuta(val, buy, sale));
                    //Get current Date
                    String mDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                    //Save current value to the DB
                    MyCurrency temp = new MyCurrency(mDate, val, buy, sale);
                    temp.save();
                }
            } catch (Exception e){e.printStackTrace();}

            return valList;
        }

        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        protected void onPostExecute(List<Valuta> result) {
            super.onPostExecute(result);
                RUR.setText("B:" + result.get(0).buy + "\nS:" + result.get(0).sell);
                EUR.setText("B:" + result.get(1).buy + "\nS:" + result.get(1).sell);
                USD.setText("B:" + result.get(2).buy + "\nS:" + result.get(2).sell);
        }
    }

    class WeatherParsing extends AsyncTask<String, Void, String>{
        String rain = "";
        String precipitation = "";
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String city = "";
            String temperature = "";
            try {
                URL urlConn = new URL(url);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(new HttpGet(urlConn.toURI()));
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(urlConn.openStream());
                Element element = doc.getDocumentElement();

                city = element.getElementsByTagName("city").item(0).getAttributes().getNamedItem("name").getNodeValue();
                rain = element.getElementsByTagName("precipitation").item(0).getAttributes().getNamedItem("mode").getNodeValue();
                precipitation = element.getElementsByTagName("clouds").item(0).getAttributes().getNamedItem("value").getNodeValue();
                double temp = Double.parseDouble(element.getElementsByTagName("temperature").item(0).getAttributes().getNamedItem("value").getNodeValue()) - 272.15d;
                temperature = String.valueOf(temp).substring(0, 4);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return ("City: " + city + "\nTemperature: " + temperature + "\nPressure: ");
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            WEATHERTEXT.setText(result);
            if(rain.equals("rain")){
                WEATHER.setImageResource(R.drawable.rain);
            } else {
                if(Double.parseDouble(precipitation) > 50){
                    WEATHER.setImageResource(R.drawable.clouds);
                }else{
                    WEATHER.setImageResource(R.drawable.sun);
                }
            }

        }

    }

    @Table(name = "CurrencyDB")
    static class MyCurrency extends Model {

        @Column(name = "Day")
        public String date;
        @Column(name = "Currency")
        public String currency;
        @Column(name = "Buy")
        public String buy;
        @Column(name = "Sell")
        public String sell;

        public MyCurrency(){
            super();
        }

        public MyCurrency(String date, String currency, String buy, String sell){
            super();
            this.date = date;
            this.currency = currency;
            this.buy = buy;
            this.sell = sell;
        }

        public static List<MyCurrency> getAll() {
            return new Select()
                    .from(MyCurrency.class)
                    .execute();
        }
    }

    class Valuta {
        String name = "";
        String buy = "";
        String sell = "";

        public Valuta(String name, String buy, String sell) {
            this.name = name;
            this.buy = buy;
            this.sell = sell;
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.updateButton:
                new CurrenParsing().execute(privatLink);
                new WeatherParsing().execute(weatherLink);
                break;
            case R.id.showDbButton:
                startActivity(new Intent(MainActivity.this, DbActivity.class));
                break;
        }
    }
}
