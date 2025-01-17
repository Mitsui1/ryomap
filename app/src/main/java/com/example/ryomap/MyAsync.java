package com.example.ryomap;
import com.example.ryomap.RouteSearch;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
public class MyAsync extends AsyncTask<String, Void, String> {
    private Activity mActivity;
    private GoogleMap mMap;
    RouteSearch routeSearch = new RouteSearch();
    private Polyline current_polyline;
    public MyAsync(Activity activity, GoogleMap googleMap, Polyline polyline) {
        mActivity = activity;
        mMap = googleMap;
        current_polyline = polyline;
    }
    public interface ResponseListener {
        void onResponseDataReceived(String responseData);
    }

    //引数paramsにURLを渡す
    @Override
    protected String doInBackground(String... params) {
        //HTTP通信処理
        //サーバから取得したデータをString型変数に格納
        //onPostExecuteの引数に渡す
        String data = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        // URLからデータ取得(MainThreadからはできないので注意)
        try {
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            data = stringBuffer.toString();
//            Log.d("myLog", "Download URL:" + data.toString());
            bufferedReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onPostExecute(String string) {
        //Text表示処理
        drawRoute(string);
    }


    private void drawRoute(String data) {
        if (data == null) {
            Log.w("SampleMap", "Can not draw route because of no data!!");
            return;
        }

        JSONArray jsonArray = new JSONArray();
        JSONArray legsArray = new JSONArray();
        JSONArray stepArray = new JSONArray();
        ArrayList<ArrayList<LatLng>> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            jsonArray = jsonObject.getJSONArray("routes");
            for (int i = 0; i < jsonArray.length(); i++) {
                legsArray = ((JSONObject) jsonArray.get(i)).getJSONArray("legs");
            }
            for (int i = 0; i < legsArray.length(); i++) {
                stepArray = ((JSONObject) legsArray.get(i)).getJSONArray("steps");
                for (int stepIndex = 0; stepIndex < stepArray.length(); stepIndex++) {
                    JSONObject stepObject = stepArray.getJSONObject(stepIndex);
                    // ルート案内で必要となるpolylineのpointsを取得し、デコード後にリストに格納
                    list.add(decodePolyline(stepObject.getJSONObject("polyline").get("points").toString()));
                }
            }
        } catch (
                JSONException e) {
            e.printStackTrace();

        }

        routeSearch.mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                // 既存のポリラインがあれば削除
                if (current_polyline != null) {
                    Log.d("ルート削除します", "ポリラインを削除");
                    current_polyline.remove();
                    current_polyline = null;  // ポリラインをnullに設定して、再利用しないようにする
                }

                // 新しいポリラインオプションの作成
                PolylineOptions polylineOptions = new PolylineOptions();

                // 経路のポイントをポリラインオプションに追加
                for (int i = 0; i < list.size(); i++) {
                    polylineOptions.addAll(list.get(i));
                }

                // ラインオプションを設定
                polylineOptions.width(10); // 線の太さ
                polylineOptions.color(Color.RED); // 線の色

                // 新しいポリラインをマップに描画
                current_polyline = mMap.addPolyline(polylineOptions);
//                if (current_polyline != null) {
//                    current_polyline.remove();
//                }
            }
        });

    }

    /**

     * Decodes polyline binary data given vy Google directions API. See below.

     * https://developers.google.com/maps/documentation/utilities/polylinealgorithm

     */

    private ArrayList<LatLng> decodePolyline (String encoded) {
        ArrayList<LatLng> point = new ArrayList<>();
        int index = 0;

        int len = encoded.length();

        int lat = 0;

        int lng = 0;



        while (index < len){

            int b;

            int shift = 0;

            int result = 0;

            do {

                b = encoded.charAt(index++) - 63;

                result |= (b & 0x1f) << shift;

                shift += 5;

            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            lat += dlat;



            shift = 0;

            result = 0;

            do {

                b = encoded.charAt(index++) - 63;

                result |= (b & 0x1f) << shift;

                shift += 5;

            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            lng += dlng;



            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));

            point.add(p);

        }



        return point;

    }

}
