package com.example.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String [] permission_list = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    Button gpsButton,next;
    TextView result,loc,tempText,time;
    ImageView weatherimg;

    LocationManager locationManager;

    Double lat,lon;
    int temp;



    class Weather extends AsyncTask<String,Void,String> {//First String means URL is in String, Void mean nothing, Third String means Return type will be String

        @Override
        protected String doInBackground(String... address) {//위에서 첫번째 string이므로 string 반환값 :  세번째 string
            //String... means multiple address can be send. It acts as array
            try {
                URL url = new URL(address[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //접속
                connection.connect();

                //서버와 연결되어 있는 스트림을 추출한다
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                //Retrieve data and return it as String
                int data =isr.read();
                String content = "";
                char ch;
                while (data != -1){
                    ch=(char) data;
                    content= content + ch;
                    data = isr.read();
                }
                return content;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission_list, 0);
        }else{
            getMyLocation();
        }
    }

    @Override
    protected void onStart() {//액티비티간 데이터 주고받기
        super.onStart();
        next=(Button)findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);

                intent.putExtra("temp",temp);

                startActivity(intent);
            }
        });
    }

    public void search2(View view){//위치를 받아서 날씨 찾기
        result=findViewById(R.id.textView);
        gpsButton = findViewById(R.id.gpsButton);
        tempText = findViewById(R.id.temp);
        weatherimg = findViewById(R.id.icon);
        time = findViewById(R.id.time);
        getMyLocation();
        String content;
        Weather weather = new Weather();

        try {
            content = weather.execute("https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&appid=4a1b15c4aacf678efc30be1d8b411e1d").get();
            //First we will check data is retrieve successfully or not

            //JSon
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            String mainTemperature = jsonObject.getString("main"); //this main is not part of weather array, it's seperate variablelike weather
//            Log.i("weatherData",weatherData);
            //weather data is in Array
            JSONArray array = new JSONArray(weatherData);

            String main = "";
            String temperature = "";
            String humidity = "";

            for(int i=0;i<array.length();i++){
                JSONObject weatherPart = array.getJSONObject(i);
                main = weatherPart.getString("main");
            }
            switch (main){
                case "Thunderstom" :
                    main = "천둥";
                    weatherimg.setImageResource(R.drawable.thunder);
                    break;
                case "Drizzle" :
                    main = "이슬비";
                    weatherimg.setImageResource(R.drawable.drizzle);
                    break;
                case "Rain" :
                    main = "비";
                    weatherimg.setImageResource(R.drawable.rain);
                    break;
                case "Snow" :
                    main = "눈";
                    weatherimg.setImageResource(R.drawable.snow);
                    break;
                case "Clear" :
                    main = "맑음";
                    weatherimg.setImageResource(R.drawable.clear);
                    break;
                case "Cloud" :
                    main = "구름";
                    weatherimg.setImageResource(R.drawable.cloud);
                    break;
                case "Dust":
                    main = "먼지";
                    weatherimg.setImageResource(R.drawable.other);
                    break;
                case "Tornado" :
                    main = "태풍";
                    weatherimg.setImageResource(R.drawable.other);
                    break;
                default:
                    main = "안개";
                    weatherimg.setImageResource(R.drawable.other);
                    break;
            }

            JSONObject mainPart = new JSONObject(mainTemperature);
            temperature = mainPart.getString("temp");
            humidity = mainPart.getString("humidity");

            temp = (int) Double.parseDouble(temperature);
            temp = temp - 273; //절대온도를 섭씨온도로 바꿔주는 작업

            String resultText="날씨 : "+main+"\n습도 : "+humidity+"%";

            result.setText(resultText);
            tempText.setText(temp+"도");

            //How we will show this result on screen

        } catch (Exception e) {
            e.printStackTrace();
        }

        //현재 날짜 구하기
        Date currentTime = Calendar.getInstance().getTime();
        String data_text = new SimpleDateFormat("MM월 dd일 EE요일", Locale.getDefault()).format(currentTime);
        time.setText(data_text);
    }



    public void getMyLocation(){
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        //권한 확인 작업
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                return;
            }
        }
        //이전에 측정했던 값을 가져온다.
        Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location1 !=null) {
            setMyLocation(location1);
        }else{
            if(location2 != null){
                setMyLocation(location2);
            }
        }
        //새롭게 측정한다.
        GetMyLocationListener listener = new GetMyLocationListener();
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)==true){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10f,listener);
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)==true){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10f,listener);
        }
    }
    public void setMyLocation(Location location){ //위도경도로 주소찾기
        lat=location.getLatitude();
        lon=location.getLongitude();
        loc=findViewById(R.id.textView2);

        Geocoder g = new Geocoder(this);
        List<Address> address=null;

        try{
            address =g.getFromLocation(lat,lon,10);
        }catch (IOException e){
            e.printStackTrace();
            Log.d("test","입출력오류");
        }
        if(address!=null){
            if(address.size()==0){
                loc.setText("주소찾기 오류");
            }else{
                Log.d("찾은주소",address.get(0).toString());
                loc.setText(address.get(0).getAddressLine(0));
            }
        }
    }


    //현재 위치 측정이 성공하면 반응하는 리스너
    class GetMyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            setMyLocation(location);
            locationManager.removeUpdates(this);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }


}