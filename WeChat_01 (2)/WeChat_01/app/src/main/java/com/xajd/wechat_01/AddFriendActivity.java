package com.xajd.wechat_01;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wechat.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AddFriendActivity extends AppCompatActivity {
    private Socket skt;
    private TextView f_remark;
    private User myself;
    private Handler handler;
    private User add_friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        f_remark = findViewById(R.id.f_remark);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                f_remark.setTextColor(android.graphics.Color.RED);
                f_remark.setText(msg.getData().getString("info"));
            }
        };
        Bundle extras = getIntent().getExtras();
        SocketBinder sb = (SocketBinder) extras.getBinder("sb");
        skt = sb.getSkt();
        myself = (User) extras.getSerializable("myself");
    }

    //添加好友
    public void addF(View view) {
        TextView f_account = findViewById(R.id.f_account);
        if (f_account.getText().toString().equals("")) {
            f_remark.setTextColor(android.graphics.Color.RED);
            f_remark.setText("请输入好友微信号...");
        }
        final String s = "105" + myself.getAccount() + "_" + f_account.getText().toString().trim() + "\n";
        Log.i("f", s);
        new Thread() {
            @Override
            public void run() {
                try {
                    //将要添加好友的账号和本账号发送到服务器
                    OutputStream out = skt.getOutputStream();
                    out.write(s.getBytes());
                    //接收服务器返回的登录信息
                    InputStream in = skt.getInputStream();
                    //接收数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String mes = null;
                    //获取服务器传来的数据
                    mes = reader.readLine();
                    //好友不存在
                    if (mes.equals("NO_Exist")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "好友不存在！");
                        handler.sendMessage(msg);
                    }//添加错误
                    else if (mes.equals("Error")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "服务出错！");
                        handler.sendMessage(msg);
                    }
                    //已经是好友
                    else if (mes.equals("Friend")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "已经是好友！");
                        handler.sendMessage(msg);
                    }
                    //添加成功
                    else {
                        add_friend= deserializeToString(mes);
                        //添加成功后自动返回好友列表界面
                        //数据是使用Intent返回
                        Intent intent = new Intent();
                        //把返回数据存入Intent
                        intent.putExtra("friend", add_friend);
                        //设置返回数据
                        AddFriendActivity.this.setResult(RESULT_OK, intent);
                        //关闭Activity
                        AddFriendActivity.this.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        Log.i("dxl","back0");
        Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("friend", add_friend);
        //设置返回数据
        AddFriendActivity.this.setResult(RESULT_OK, intent);
        //关闭Activity
        AddFriendActivity.this.finish();
        super.onBackPressed();
        Log.i("dxl","back1");
    }

    //接收好友列表字符串并反序列化为对象
    public User deserializeToString(String mes) {
        User u = new User();
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(mes.getBytes("ISO-8859-1"));
            ObjectInputStream objIn = null;
            objIn = new ObjectInputStream(byteIn);
            u = (User) objIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }
}
