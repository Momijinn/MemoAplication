package com.example.kaname.samplewriereadlistview;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{
    private TextView Date_Text, Time_Text;
    private Button Date_Btn, Time_Btn;
    private EditText Memo_Edit;
    private Button Input_Btn, Save_Btn;
    private ListView Date_List;

    //初期の日付や時刻を持ってくるための変数
    private final Calendar calendar = Calendar.getInstance();
    private final int now_year = calendar.get(Calendar.YEAR); // 年
    private final int now_monthOfYear = calendar.get(Calendar.MONTH); // 月
    private final int now_dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); // 日
    private final int now_hour = calendar.get(Calendar.HOUR_OF_DAY); //時
    private final int now_minute = calendar.get(Calendar.MINUTE); //分

    //ユーザーがセットした日付や時刻、メモの変数
    //初期値として今の日付を入れておく
    private String set_day = String.valueOf(now_year)+"年"+String.valueOf(now_monthOfYear + 1)+"月"+String.valueOf(now_dayOfMonth)+"日";
    private String set_time = String.valueOf(now_hour) + ":" + String.valueOf(now_minute);

    //ストレージに保存するためのPATH
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EXSTORAGE_PATH = String.format("%s/%s", Environment.getExternalStorageDirectory().toString(), "SampleWrieReadListview");//デレクトリ
    private static final String MEMO_DATA = "MEMO.txt"; //ここに保存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TODO 初期起動時にディレクトリ作成及び保存先ディレクトリがあるか確認
        Initialize_CreateDirectory();

        /* Listview の設定*/
        // ListView に設定するデータ (アダプタ) を生成する (テキスト 2 行表示リスト)
        final List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        final SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
                new String[] {"main", "sub"}, new int[] {android.R.id.text1, android.R.id.text2});

        //TODO ストレージの読み込み(メモを保存してあるファイルを読み込む)
        try {
            File file = new File(EXSTORAGE_PATH + "/" + MEMO_DATA);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            //TODO ここでデータを読み込んでいる
            while((str = br.readLine()) != null){
                String[] strs = str.split(",", 0); //保存文字列は"メモ","時間"
                Map<String, String> map = new HashMap<String, String>();
                map.put("main", strs[1]);
                map.put("sub", strs[0]);
                list.add(map);
            }
            br.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        Date_List =(ListView)findViewById(R.id.Data_Listview);
        Date_List.setAdapter(adapter);

        Date_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //AlertDialogによる本当に削除するかの確認
                //参考:http://qiita.com/suzukihr/items/8973527ebb8bb35f6bb8
                new AlertDialog.Builder(MainActivity.this).setTitle("確認").setMessage("この項目を削除しますか？").
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list.remove(position); //削除
                                adapter.notifyDataSetChanged(); //これ入れないとリストの削除ができない
                                Toast.makeText(MainActivity.this, "「メモの保存」をクリックしないと完全に削除しません", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });


        /*日付の設定 */
        Date_Btn = (Button)findViewById(R.id.Date_Button);
        Date_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        set_day = String.valueOf(year) + "年" + String.valueOf(monthOfYear + 1) + "月" + String.valueOf(dayOfMonth) + "日";
                        Date_Text.setText(set_day);
                    }
                }, now_year, now_monthOfYear, now_dayOfMonth).show();
            }
        });
        /* 日付を格納するTexitViewには初期値として現在の日付を入れる */
        Date_Text = (TextView)findViewById(R.id.Date_TextView);
        Date_Text.setText(set_day);

        /* 時刻の設定 */
        Time_Btn = (Button)findViewById(R.id.Time_Button);
        Time_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        set_time = String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
                        Time_Text.setText(set_time);
                    }
                }, now_hour, now_minute, true).show();
            }
        });
        /* 時刻を格納するTexitViewには初期値として現在の時刻を入れる */
        Time_Text = (TextView)findViewById(R.id.Time_TextView);
        Time_Text.setText(set_time);

        /*EditTextの設定 */
        Memo_Edit = (EditText)findViewById(R.id.Memo_EditText);

        // TODO 追加ボタンが押された時の処理
        Input_Btn = (Button)findViewById(R.id.Input_Button);
        Input_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String memo = Memo_Edit.getText().toString();
                if(memo.equals("")){
                    Toast.makeText(MainActivity.this, "メモがありません",Toast.LENGTH_SHORT).show();
                }else {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("main", set_day + set_time);
                    map.put("sub", memo);
                    list.add(map);
                    Date_List.setAdapter(adapter);
                    Memo_Edit.setText(""); //最後にEditTextの中身を無くす
                    Toast.makeText(MainActivity.this, "メモを追加しました", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "保存する場合は「メモを保存」をクリックしてください", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // TODO 保存ボタンが押された時の処理 ファイルへ書き込み
        //参考:http://www.javadrive.jp/start/stream/index7.html
        Save_Btn = (Button)findViewById(R.id.Save_Button);
        Save_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File file = new File(EXSTORAGE_PATH + "/" + MEMO_DATA);
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

                    for (int i=0;i<Date_List.getCount(); i++){
                        Object sec_list = adapter.getItem(i);
                        String sec = sec_list.toString();
                        sec = sec.replaceAll("\\{sub=", "");
                        sec = sec.replaceAll("main=", "");
                        sec = sec.replaceAll("\\}", "");
                        pw.write(sec + '\n'); //最後に改行をつけて一行にする
                    }
                    pw.close(); //開けたら閉める
                    Toast.makeText(MainActivity.this, "メモを保存しました", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * ディレクトリ作成及び確認
     */
    private void Initialize_CreateDirectory() {
        File f = new File(EXSTORAGE_PATH);
        if(!f.exists()){
            if(f.mkdirs()) Log.v(TAG, "ディレクトリの作成に成功");
            else Log.v(TAG , "ディレクトリの作成に失敗");
        }else{
            Log.v(TAG, "すでにディレクトリ作成済み");
        }
    }
}
