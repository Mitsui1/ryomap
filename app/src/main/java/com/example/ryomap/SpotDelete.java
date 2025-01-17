package com.example.ryomap;



import android.app.AlertDialog;

import android.content.DialogInterface;

import android.os.Bundle;

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



import android.util.Log;



import android.content.Intent;

import android.widget.ListView;

import android.widget.Toast;



public class SpotDelete extends AppCompatActivity {



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

                Intent intent = new Intent(SpotDelete.this, SpotSelect.class);

                startActivity(intent);

                overridePendingTransition(0, 0);

            }

        });



        // スポットのリストを非同期で取得

        new SpotDelete.SelectSpotTask().execute();



        // ListViewのアイテムクリック時の処理

        listView.setOnItemClickListener((parent, view, position, id) -> {

            String selectedSpotName = spotNames.get(position);

            String spotId = spotMap.get(selectedSpotName).toString(); // spot_idを取得



            // 削除確認ダイアログを表示

            new AlertDialog.Builder(SpotDelete.this)

                    .setTitle("削除確認")

                    .setMessage(selectedSpotName + "を削除してもよろしいですか？")

                    .setPositiveButton("はい", new DialogInterface.OnClickListener() {

                        @Override

                        public void onClick(DialogInterface dialog, int which) {

                            // データベースから削除処理を実行

                            new DeleteSpotTask().execute(spotId);

                        }

                    })

                    .setNegativeButton("いいえ", null)

                    .show();

        });

    }



    // SQL接続(SELECT)

    private class SelectSpotTask extends AsyncTask<Void, Void, String> {



        @Override

        protected String doInBackground(Void... voids) {

            String result = "";

            ArrayList<String> resultList = new ArrayList<>();

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

                }

            } catch (ClassNotFoundException e) {

                Log.e("SpotDelete", "JDBCドライバが見つかりません: " + e.getMessage());

            } catch (SQLException e) {

                Log.e("SpotDelete", "データベースエラー: " + e.getMessage());

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



        // SQL更新後の処理(SELECT)

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            adapter.notifyDataSetChanged();

        }

    }



    // // SQL接続(DELETE)

    private class DeleteSpotTask extends AsyncTask<String, Void, String> {



        @Override

        protected String doInBackground(String... params) {

            String result = "";

            Connection connection = null;

            PreparedStatement preparedStatement = null;



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

                    String sql = "DELETE FROM spot WHERE spot_id = ?";

                    preparedStatement = connection.prepareStatement(sql);



                    // パラメータを設定

                    preparedStatement.setInt(1, Integer.parseInt(params[0])); // kategori_id



                    int rowsAffected = preparedStatement.executeUpdate();

                    result = rowsAffected > 0 ? "データ挿入成功" : "データ挿入失敗";

                } else {

                    result = "データベース接続失敗";

                }

            } catch (ClassNotFoundException | SQLException e) {

                Log.e("SpotDelete", "データベースエラー: " + e.getMessage());

            } finally {

                try {

                    if (preparedStatement != null) preparedStatement.close();

                    if (connection != null) connection.close();

                } catch (SQLException e) {

                    // Ignore

                }

            }

            return result;

        }



        // SQL更新後の処理(DELETE)

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            // データベース編集成功時に完了画面に遷移

            if ("データ挿入成功".equals(result)) {

                Intent intent = new Intent(SpotDelete.this, SpotDeleteSuccess.class);

                startActivity(intent);

                overridePendingTransition(0, 0); // アニメーションなしで遷移

            } else {

                Toast.makeText(SpotDelete.this, result, Toast.LENGTH_SHORT).show();

            }

        }

    }

}