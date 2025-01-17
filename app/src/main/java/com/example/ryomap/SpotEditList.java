package com.example.ryomap;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.widget.ListView;



public class SpotEditList extends AppCompatActivity {



    private ListView listView;

    private ArrayAdapter<String> adapter;

    private ArrayList<String> spotNames; // spot_name用

    private HashMap<String, Integer> spotMap; // spot_name -> spot_idマップ



    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.spotdelete);



        // ListView の初期化

        listView = findViewById(R.id.listView);

        spotNames = new ArrayList<>();

        spotMap = new HashMap<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spotNames);

        listView.setAdapter(adapter);



        // メニュー画面へ

        Button button = findViewById(R.id.menu_back_button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Intent intent = new Intent(SpotEditList.this, MenuActivity.class);

                startActivity(intent);

                overridePendingTransition(0, 0);

            }

        });



        // SQL接続

        new SpotEditList.SelectSpotTask().execute();



        // リストのクリック

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedSpotName = spotNames.get(position);

                int spotId = spotMap.get(selectedSpotName); // spot_idを取得



                // SpotEditにデータを渡して画面遷移

                Intent intent = new Intent(SpotEditList.this, SpotEdit.class);

                intent.putExtra("spot_id", spotId);

                startActivity(intent);

            }

        });

    }



    private class SelectSpotTask extends AsyncTask<Void, Void, String> {



        @Override

        protected String doInBackground(Void... voids) {

            String result = "";

            Connection connection = null;

            PreparedStatement preparedStatement = null;

            ResultSet resultSet = null;



            try {

                // JDBCドライバのロード

                Class.forName("org.mariadb.jdbc.Driver");



                // データベースに接続

                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";

                String user = "root";

                String password = "h11y24n20";

                connection = DriverManager.getConnection(url, user, password);



                if (connection != null) {

                    // SQLクエリを準備

                    String sql = "SELECT spot_id, spot_name FROM spot";

                    preparedStatement = connection.prepareStatement(sql);



                    // クエリを実行して結果を取得

                    resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {

                        int spotId = resultSet.getInt("spot_id");

                        String spotName = resultSet.getString("spot_name");



                        spotNames.add(spotName); // spot_nameをリストに追加

                        spotMap.put(spotName, spotId); // spot_name -> spot_idマップ

                    }

                } else {

                    result = "接続に失敗しました";

                }



            } catch (ClassNotFoundException e) {

                result = "JDBCドライバが見つかりません: " + e.getMessage();

            } catch (SQLException e) {

                result = "データベース接続エラー: " +e.getMessage();

            } finally {

                try {

                    if (resultSet != null) resultSet.close();

                    if (preparedStatement != null) preparedStatement.close();

                    if (connection != null) connection.close();

                } catch (SQLException e) {

                    // Ignore

                }

            }

            return result;

        }



        @Override

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            adapter.notifyDataSetChanged();
        }

    }

}