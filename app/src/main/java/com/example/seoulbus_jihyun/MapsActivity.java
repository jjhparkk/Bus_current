package com.example.seoulbus_jihyun;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private String busNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // 사용자가 입력한 버스 번호를 받아옴
        Intent intent = getIntent();
        busNumber = intent.getStringExtra("BUS_NUMBER");

        initializeMap(mapFragment);
    }

    private void initializeMap(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;

            // 노선 버스 ID 추출을 위한 공공 DB API 호출
            String serviceUrl = "http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList";
            String serviceKey = "키값";
            String strUrl = serviceUrl + "?ServiceKey=" + serviceKey + "&strSrch=" + busNumber;

            DownloadWebpageTask task = new DownloadWebpageTask();
            task.execute(strUrl);
        });
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null; // 또는 에러 메시지를 리턴하도록 수정 가능
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(new StringReader(result));
                    int eventType = xpp.getEventType();

                    String headerCd = "";
                    String busRouteId = "";
                    String busRouteNm = "";

                    boolean bSet_headerCd = false;
                    boolean bSet_busRouteId = false;
                    boolean bSet_busRouteNm = false;

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                            // Nothing to do here for now
                        } else if (eventType == XmlPullParser.START_TAG) {
                            String tag_name = xpp.getName();
                            if (tag_name.equals("headerCd")) bSet_headerCd = true;
                            if (tag_name.equals("busRouteId")) bSet_busRouteId = true;
                            if (tag_name.equals("busRouteNm")) bSet_busRouteNm = true;
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (bSet_headerCd) {
                                headerCd = xpp.getText();
                                bSet_headerCd = false;
                            }

                            if (headerCd.equals("0")) {
                                if (bSet_busRouteId) {
                                    busRouteId = xpp.getText();
                                    bSet_busRouteId = false;
                                }

                                if (bSet_busRouteNm) {
                                    busRouteNm = xpp.getText();
                                    // tv.append("busRouteNm; " + busRouteNm + "\n"); // Assuming tv is declared and initialized
                                    bSet_busRouteNm = false;
                                }
                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                            // Nothing to do here for now
                        }

                        eventType = xpp.next();
                    }

                    // Bus Position
                    if (!busRouteId.isEmpty()) {
                        String serviceUrl = "http://ws.bus.go.kr/api/rest/buspos/getBusPosByRtid";
                        String serviceKey = "키값";
                        String strUrl = serviceUrl + "?ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
                        DownloadWebpageTask2 task2 = new DownloadWebpageTask2();
                        task2.execute(strUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class DownloadWebpageTask2 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null; // 또는 에러 메시지를 리턴하도록 수정 가능
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(new StringReader(result));
                    int eventType = xpp.getEventType();

                    String gpsX = "";
                    String gpsY = "";
                    String plainNo = "";

                    boolean bSet_gpsX = false;
                    boolean bSet_gpsY = false;
                    boolean bSet_plainNo = false;

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String tag_name = xpp.getName();
                            if (tag_name.equals("gpsX")) bSet_gpsX = true;
                            if (tag_name.equals("gpsY")) bSet_gpsY = true;
                            if (tag_name.equals("plainNo")) bSet_plainNo = true;
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (bSet_gpsX) {
                                gpsX = xpp.getText();
                                bSet_gpsX = false;
                            }

                            if (bSet_gpsY) {
                                gpsY = xpp.getText();
                                bSet_gpsY = false;
                            }

                            if (bSet_plainNo) {
                                plainNo = xpp.getText();
                                bSet_plainNo = false;

                                displayBus(gpsX, gpsY, plainNo);
                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                            // Nothing to do here for now
                        }

                        eventType = xpp.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            BufferedInputStream buf = new BufferedInputStream(conn.getInputStream());
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "utf-8"));
            String line;
            StringBuilder page = new StringBuilder();
            while ((line = bufreader.readLine()) != null) {
                page.append(line);
            }

            return page.toString();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void displayBus(String gpsX, String gpsY, String plainNo) {
        double latitude = Double.parseDouble(gpsY);
        double longitude = Double.parseDouble(gpsX);
        LatLng LOC = new LatLng(latitude, longitude);

        Marker mk1 = mMap.addMarker(new MarkerOptions()
                .position(LOC)
                .title(plainNo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(LOC));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
    }
}
