package edu.ncku.application.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;

/**
 * 顯示地利位置的頁面，預設以Google Map顯示，假如開啟失敗則以網頁形式顯示
 */
public class LibMapFragment extends Fragment implements OnMapReadyCallback, IOConstatnt {
    private static final String TAG = LibMapFragment.class.getName();
    private static View rootView;
    private double[] lat_lng=null;
    /**
     * 為避免發生例外，此Fragment將只有單一實體
     *
     * @return LibMapFragment實體
     */
    // TODO: Rename and change types and number of parameters
    public static LibMapFragment getInstance() {
        return new LibMapFragment();
    }

    public LibMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        lat_lng = getArguments().getDoubleArray("lat_lng");
        readyToGo();
        try
        {
            // 重新獲取 RootView
            rootView = inflater.inflate(R.layout.fragment_lib_map, container, false);
            // 處理 FragmentManger 的版本問題
            FragmentManager fm;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if(showLogMsg){
                    Log.d(TAG, "using getFragmentManager");
                }
                fm = getFragmentManager();
            } else {
                if(showLogMsg){
                    Log.d(TAG, "using getChildFragmentManager");
                }
                fm = getChildFragmentManager();
            }
            MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return rootView;
    }

    protected boolean readyToGo() {
        GoogleApiAvailability checker = GoogleApiAvailability.getInstance();

        Context context = getActivity().getApplicationContext();
        int status = checker.isGooglePlayServicesAvailable(context);

        if (status == ConnectionResult.SUCCESS) {
            if (getVersionFromPackageManager(context) >= 2) {
                return (true); // 成功開啟Google Map
            } else {
                Toast.makeText(context, R.string.no_maps, Toast.LENGTH_LONG).show();
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            }
        } else {
            /* 開啟失敗，打開地理位置網頁 */
            Toast.makeText(context, R.string.no_maps, Toast.LENGTH_LONG).show();
            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            Uri uri = Uri.parse("https://app.lib.ncku.edu.tw/map.html");
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);
        }

        return (false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            if(showLogMsg){
                Log.e(TAG, "Sorry! unable to create maps");
            }
            return;
        }
        setUpMap(googleMap);
    }

    /**
     * 移動地圖到參數指定的經緯度座標
     *
     * @param place 經緯度座標
     * @param googleMap
     */
    private void moveMap(LatLng place, GoogleMap googleMap) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * 設置地標
     * @param googleMap
     */
    private void setUpMap(GoogleMap googleMap) {
        if(showLogMsg){
            Log.d(TAG, "Set up maps");
        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // 建立位置的座標物件
//        LatLng place = new LatLng(22.999770, 120.219925);
        LatLng place = new LatLng(lat_lng[0], lat_lng[1]);
        // 移動地圖
        moveMap(place, googleMap);

        addMarker(place, googleMap, "國立成功大學總圖書館", "台灣台南市東區大學路1號");
    }

    /**
     * 在地圖加入指定位置與標題的標記Icon
     *  @param place   經緯度座標
     * @param googleMap
     * @param title   標題
     * @param snippet 副標題
     */
    private void addMarker(LatLng place, GoogleMap googleMap, String title, String snippet) {
        BitmapDescriptor icon =
                BitmapDescriptorFactory.fromResource(R.drawable.ic_notification_red);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title)
                .snippet(snippet)
                .icon(icon);

        googleMap.addMarker(markerOptions);
    }

    // following from
    // https://android.googlesource.com/platform/cts/+/master/tests/tests/graphics/src/android/opengl/cts/OpenGlEsVersionTest.java

    /*
     * Copyright (C) 2010 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the
     * "License"); you may not use this file except in
     * compliance with the License. You may obtain a copy of
     * the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in
     * writing, software distributed under the License is
     * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
     * CONDITIONS OF ANY KIND, either express or implied. See
     * the License for the specific language governing
     * permissions and limitations under the License.
     */
    private static int getVersionFromPackageManager(Context context) {
        PackageManager packageManager = context.getPackageManager();
        FeatureInfo[] featureInfos =
                packageManager.getSystemAvailableFeatures();
        if (featureInfos != null && featureInfos.length > 0) {
            for (FeatureInfo featureInfo : featureInfos) {
                // Null feature name means this feature is the open
                // gl es version feature.
                if (featureInfo.name == null) {
                    if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
                        return getMajorVersion(featureInfo.reqGlEsVersion);
                    } else {
                        return 1; // Lack of property means OpenGL ES
                        // version 1
                    }
                }
            }
        }
        return 1;
    }

    /**
     * @see FeatureInfo#getGlEsVersion()
     */
    private static int getMajorVersion(int glEsVersion) {
        return ((glEsVersion & 0xffff0000) >> 16);
    }


//    // 這是一個用來顯示路徑的物件
//    private class GMapV2Direction {
//        public final static String MODE_DRIVING = "driving";
//        public final static String MODE_WALKING = "walking";
//
//        public GMapV2Direction() {
//        }
//
//        public Document getDocument(LatLng start, LatLng end, String mode) {
//            String url = "http://maps.googleapis.com/maps/api/directions/xml?"
//                    + "origin=" + start.latitude + "," + start.longitude
//                    + "&destination=" + end.latitude + "," + end.longitude
//                    + "&sensor=false&units=metric&mode=driving";
//            Log.d("url", url);
//            try {
//                String str = HttpClient.sendPost(url, "");
////                JSONObject json = new JSONObject(str);
////                HttpContext localContext = new BasicHttpContext();
////                HttpPost httpPost = new HttpPost(url);
////                HttpResponse response = httpClient.execute(httpPost, localContext);
////                InputStream in = response.getEntity().getContent();
//                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
//                        .newDocumentBuilder();
//                Document doc = builder.parse(str);
//                return doc;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        public String getDurationText(Document doc) {
//            try {
//
//                NodeList nl1 = doc.getElementsByTagName("duration");
//                Node node1 = nl1.item(0);
//                NodeList nl2 = node1.getChildNodes();
//                Node node2 = nl2.item(getNodeIndex(nl2, "text"));
//                Log.i("DurationText", node2.getTextContent());
//                return node2.getTextContent();
//            } catch (Exception e) {
//                return "0";
//            }
//        }
//
//        public int getDurationValue(Document doc) {
//            try {
//                NodeList nl1 = doc.getElementsByTagName("duration");
//                Node node1 = nl1.item(0);
//                NodeList nl2 = node1.getChildNodes();
//                Node node2 = nl2.item(getNodeIndex(nl2, "value"));
//                Log.i("DurationValue", node2.getTextContent());
//                return Integer.parseInt(node2.getTextContent());
//            } catch (Exception e) {
//                return -1;
//            }
//        }
//
//        public String getDistanceText(Document doc) {
//        /*
//         * while (en.hasMoreElements()) { type type = (type) en.nextElement();
//         *
//         * }
//         */
//
//            try {
//                NodeList nl1;
//                nl1 = doc.getElementsByTagName("distance");
//
//                Node node1 = nl1.item(nl1.getLength() - 1);
//                NodeList nl2 = null;
//                nl2 = node1.getChildNodes();
//                Node node2 = nl2.item(getNodeIndex(nl2, "value"));
//                Log.d("DistanceText", node2.getTextContent());
//                return node2.getTextContent();
//            } catch (Exception e) {
//                return "-1";
//            }
//
//        /*
//         * NodeList nl1; if(doc.getElementsByTagName("distance")!=null){ nl1=
//         * doc.getElementsByTagName("distance");
//         *
//         * Node node1 = nl1.item(nl1.getLength() - 1); NodeList nl2 = null; if
//         * (node1.getChildNodes() != null) { nl2 = node1.getChildNodes(); Node
//         * node2 = nl2.item(getNodeIndex(nl2, "value")); Log.d("DistanceText",
//         * node2.getTextContent()); return node2.getTextContent(); } else return
//         * "-1";} else return "-1";
//         */
//        }
//
//        public int getDistanceValue(Document doc) {
//            try {
//                NodeList nl1 = doc.getElementsByTagName("distance");
//                Node node1 = null;
//                node1 = nl1.item(nl1.getLength() - 1);
//                NodeList nl2 = node1.getChildNodes();
//                Node node2 = nl2.item(getNodeIndex(nl2, "value"));
//                Log.i("DistanceValue", node2.getTextContent());
//                return Integer.parseInt(node2.getTextContent());
//            } catch (Exception e) {
//                return -1;
//            }
//        /*
//         * NodeList nl1 = doc.getElementsByTagName("distance"); Node node1 =
//         * null; if (nl1.getLength() > 0) node1 = nl1.item(nl1.getLength() - 1);
//         * if (node1 != null) { NodeList nl2 = node1.getChildNodes(); Node node2
//         * = nl2.item(getNodeIndex(nl2, "value")); Log.i("DistanceValue",
//         * node2.getTextContent()); return
//         * Integer.parseInt(node2.getTextContent()); } else return 0;
//         */
//        }
//
//        public String getStartAddress(Document doc) {
//            try {
//                NodeList nl1 = doc.getElementsByTagName("start_address");
//                Node node1 = nl1.item(0);
//                Log.i("StartAddress", node1.getTextContent());
//                return node1.getTextContent();
//            } catch (Exception e) {
//                return "-1";
//            }
//
//        }
//
//        public String getEndAddress(Document doc) {
//            try {
//                NodeList nl1 = doc.getElementsByTagName("end_address");
//                Node node1 = nl1.item(0);
//                Log.i("StartAddress", node1.getTextContent());
//                return node1.getTextContent();
//            } catch (Exception e) {
//                return "-1";
//            }
//        }
//        public String getCopyRights(Document doc) {
//            try {
//                NodeList nl1 = doc.getElementsByTagName("copyrights");
//                Node node1 = nl1.item(0);
//                Log.i("CopyRights", node1.getTextContent());
//                return node1.getTextContent();
//            } catch (Exception e) {
//                return "-1";
//            }
//
//        }
//
//        public ArrayList<LatLng> getDirection(Document doc) {
//            NodeList nl1, nl2, nl3;
//            ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
//            nl1 = doc.getElementsByTagName("step");
//            if (nl1.getLength() > 0) {
//                for (int i = 0; i < nl1.getLength(); i++) {
//                    Node node1 = nl1.item(i);
//                    nl2 = node1.getChildNodes();
//
//                    Node locationNode = nl2
//                            .item(getNodeIndex(nl2, "start_location"));
//                    nl3 = locationNode.getChildNodes();
//                    Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
//                    double lat = Double.parseDouble(latNode.getTextContent());
//                    Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
//                    double lng = Double.parseDouble(lngNode.getTextContent());
//                    listGeopoints.add(new LatLng(lat, lng));
//
//                    locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
//                    nl3 = locationNode.getChildNodes();
//                    latNode = nl3.item(getNodeIndex(nl3, "points"));
//                    ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
//                    for (int j = 0; j < arr.size(); j++) {
//                        listGeopoints.add(new LatLng(arr.get(j).latitude, arr
//                                .get(j).longitude));
//                    }
//
//                    locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
//                    nl3 = locationNode.getChildNodes();
//                    latNode = nl3.item(getNodeIndex(nl3, "lat"));
//                    lat = Double.parseDouble(latNode.getTextContent());
//                    lngNode = nl3.item(getNodeIndex(nl3, "lng"));
//                    lng = Double.parseDouble(lngNode.getTextContent());
//                    listGeopoints.add(new LatLng(lat, lng));
//                }
//            }
//
//            return listGeopoints;
//        }
//
//        private int getNodeIndex(NodeList nl, String nodename) {
//            for (int i = 0; i < nl.getLength(); i++) {
//                if (nl.item(i).getNodeName().equals(nodename))
//                    return i;
//            }
//            return -1;
//        }
//
//        private ArrayList<LatLng> decodePoly(String encoded) {
//            ArrayList<LatLng> poly = new ArrayList<LatLng>();
//            int index = 0, len = encoded.length();
//            int lat = 0, lng = 0;
//            while (index < len) {
//                int b, shift = 0, result = 0;
//                do {
//                    b = encoded.charAt(index++) - 63;
//                    result |= (b & 0x1f) << shift;
//                    shift += 5;
//                } while (b >= 0x20);
//                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//                lat += dlat;
//                shift = 0;
//                result = 0;
//                do {
//                    b = encoded.charAt(index++) - 63;
//                    result |= (b & 0x1f) << shift;
//                    shift += 5;
//                } while (b >= 0x20);
//                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//                lng += dlng;
//
//                LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
//                poly.add(position);
//            }
//            return poly;
//        }
//    }
}
