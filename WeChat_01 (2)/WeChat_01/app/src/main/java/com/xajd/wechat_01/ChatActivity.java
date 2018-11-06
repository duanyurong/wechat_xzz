package com.xajd.wechat_01;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.wechat.model.ChatContent;
import com.wechat.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private List<ChatContent> contents;
    private List<ChatContent> contents_db;
    private ArrayList<ChatContent> back_contents;
    private ChatContentAdapter adapter;
    private ListView chatLv;
    private Socket skt;
    private boolean isChat = true;
    private OutputStream out;
    private Handler handler;
    private MyDataBaseHelper dhHelper;
    private SQLiteDatabase db;
    private String f_acc, my_acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //自Andro3开始使用网络流就不允许在主线程里，可以使用子线程，也可以取消限制
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //聊天记录集合
        contents = new ArrayList<ChatContent>();
        contents_db = new ArrayList<ChatContent>();
        back_contents=new ArrayList<ChatContent>();
        //适配器
        chatLv = findViewById(R.id.chatListLv);
        adapter = new ChatContentAdapter(this, R.layout.chat_layout, contents);
        chatLv.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        SocketBinder sb = (SocketBinder) extras.getBinder("sb");
        skt = sb.getSkt();
        dhHelper = sb.getDhHelper();
        db = dhHelper.getReadableDatabase();
        f_acc = ((User) extras.getSerializable("friend")).getAccount();
        my_acc = ((User) extras.getSerializable("myself")).getAccount();
        //读数据
        readDB();
        back_contents =(ArrayList<ChatContent>) extras.getSerializable("in_contents");
        Log.i("dxl","此次载入缓存信息量:"+back_contents.size());
        for(ChatContent cc : back_contents){
            contents.add(cc);
            contents_db.add(cc);
        }
        back_contents.clear();
        adapter.notifyDataSetChanged();
        //handler处理界面显示
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                adapter.notifyDataSetChanged();
            }
        };
        //子线程接收信息
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream in = skt.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    //获取客户端传来的数据
                    while (isChat) {
                        String mes = reader.readLine();
                        Log.i("dxl","ChatActivity聊天框接收到信息:"+ mes);
                        if (mes.equals("backActivity")) {
                            //退出死循环，销毁线程
                            isChat = false;
                        } else {
                            //s[0] 信息来自好友的acc
                            String[] s = mes.split("_");
                            ChatContent cc = new ChatContent();
                            cc.setContent(s[1]);
                            cc.setMe(false);
                            if (s[0].equals(f_acc)) {
                                contents.add(cc);
                                contents_db.add(cc);
                                Message message = new Message();
                                handler.sendMessage(message);
                            }//如果有第三方客户端也发信息来，存入集合，返回好友列表后传回去
                            else {
                                //信息来自s[0]账号
                                cc.setFrom_acc(s[0]);
                                back_contents.add(cc);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        try {
            out = skt.getOutputStream();
            out.write("106\n".getBytes());
            //保存此次打开后的聊天记录
            writeDB();
        } catch (IOException e) {
            e.printStackTrace();
        }
        contents.clear();
        contents_db.clear();
        Intent intent = new Intent();
        //把返回数据存入Intent
        Bundle bundleObject = new Bundle();
        bundleObject.putSerializable("back_contents", back_contents);
        intent.putExtras(bundleObject);
        //设置返回数据
        ChatActivity.this.setResult(RESULT_OK, intent);
        //关闭Activity
        ChatActivity.this.finish();
        super.onBackPressed();
    }

    //从数据库读数据
    public void readDB() {
        //表如果存在就不在创建
        String sql = "create table if not exists ChatRecord(id integer primary key,f_acc text, isme Integer,record text);";
        dhHelper.OnCreatTable(db, sql);
        String data_sql = "select * from ChatRecord  where  f_acc=? ";
        Cursor cursor = db.rawQuery(data_sql, new String[]{f_acc});
        Log.i("dxl", "ChatActivity读取缓存信息数量:" + String.valueOf(cursor.getCount()));
        String text;
        int isMe;
        while (cursor.moveToNext()) {
            isMe = cursor.getInt(cursor.getColumnIndex("isme"));
            text = cursor.getString(cursor.getColumnIndex("record"));
            if (isMe == 1) {
                ChatContent cc = new ChatContent();
                cc.setContent(text);
                cc.setMe(true);
                contents.add(cc);
            } else {
                ChatContent cc = new ChatContent();
                cc.setContent(text);
                cc.setMe(false);
                contents.add(cc);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //保存当前聊天记录
    public void writeDB() {
        for (ChatContent cc : contents_db) {
            ContentValues values = new ContentValues();
            values.put("f_acc", f_acc);
            values.put("isme", cc.isMe() == true ? 1 : 0);
            values.put("record", cc.getContent());
            db.insert("ChatRecord", null, values);
        }
        Log.i("dxl", "ChatActivity保存此次聊天数据量：" + contents_db.size());
    }

    //发送信息
    public void send(View view) {
        EditText et = findViewById(R.id.sendMsg);
        Log.i("d---", et.getText().toString());
        if (et.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "不能发送空消息！", Toast.LENGTH_LONG).show();
            return;
        }
        String mes = et.getText().toString();
        ChatContent cc = new ChatContent();
        cc.setContent(mes);
        cc.setMe(true);
        et.setText("");
        contents.add(cc);
        contents_db.add(cc);
        adapter.notifyDataSetChanged();
        final String mes2 = "102" + f_acc + "_" + my_acc + "_" + mes + "\n";
        try {
            out = skt.getOutputStream();
            out.write(mes2.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

