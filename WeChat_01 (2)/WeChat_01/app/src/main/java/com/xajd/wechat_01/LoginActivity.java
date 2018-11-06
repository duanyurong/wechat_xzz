package com.xajd.wechat_01;


import android.content.Intent;

import android.graphics.Color;
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
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity {
    private InetAddress addr;
    private Socket skt;
    private TextView remark;
    private Handler handler;
    private User myself;
    private String serverIP="47.100.178.32";
    private int serverPoint=23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        remark = findViewById(R.id.remark);
        myself = new User();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                remark.setTextColor(android.graphics.Color.RED);
                remark.setText(msg.getData().getString("info"));
            }
        };
    }

    //登录
    public void login(View view) throws Exception {
        TextView account = findViewById(R.id.account);
        TextView password = findViewById(R.id.password);
        String acc = account.getText().toString().trim();
        String pas = password.getText().toString().trim();
        if(acc.equals("")){
            remark.setTextColor(Color.RED);
            remark.setText("请输入账户...");
            return;
        }
        if(pas.equals("")){
            remark.setTextColor(Color.RED);
            remark.setText("请输入密码...");
            return;
        }
        remark.setTextColor(Color.GREEN);
        remark.setText("正在登录，请稍后...");
        myself.setAccount(acc);
        myself.setPassword(pas);
        final String s = "101" + acc + "_" + pas + "\n";
        Log.i("s",s);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.i("dxl", "已启动...");
                    addr = InetAddress.getByName(serverIP);
                    skt = new Socket(addr, serverPoint);
                    //登录将账户与密码发送过去
                    OutputStream out = skt.getOutputStream();
                    out.write(s.getBytes());
                    //接收服务器返回的登录信息
                    InputStream in = skt.getInputStream();
                    //接收数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String mes = null;
                    //获取服务器传来的数据
                    mes = reader.readLine();
                    //账户不存在
                    if (mes.equals("null")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "账户不存在！");
                        handler.sendMessage(msg);
                    }//密码错误
                    else if (mes.equals("password_error")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "密码错误！");
                        handler.sendMessage(msg);
                    }//登录成功
                    else {
                        ArrayList<User> friends = deserializeToString(mes);
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, FriendsActivity.class);
                        Bundle bundleObject = new Bundle();
                        bundleObject.putSerializable("friends", friends);
                        bundleObject.putSerializable("myself", myself);
                        SocketBinder sb = new SocketBinder();
                        sb.setSkt(skt);
                        bundleObject.putBinder("sb", sb);
                        intent.putExtras(bundleObject);
                        startActivity(intent);
                        //startActivityForResult();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    //注册用户
    public void register(View view) {
        remark.setTextColor(Color.GREEN);
        TextView username =findViewById(R.id.username);
        String u_name=username.getText().toString().trim();
        if(u_name.equals("")){
            remark.setTextColor(Color.RED);
            remark.setText("注册需填写网名...");
            return;
        }
        TextView account = findViewById(R.id.account);
        TextView password = findViewById(R.id.password);
        String acc = account.getText().toString().trim();
        String pas = password.getText().toString().trim();
        if(acc.equals("")){
            remark.setTextColor(Color.RED);
            remark.setText("账户不能为空...");
            return;
        }
        if(pas.equals("")){
            remark.setTextColor(Color.RED);
            remark.setText("密码不能为空...");
            return;
        }
        remark.setText("正在注册，请稍后...");
        myself.setUsername(u_name);
        myself.setAccount(acc);
        myself.setPassword(pas);
        final String s = "104" + u_name+"_"+acc + "_" + pas + "\n";
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.i("dxl", "已启动...");
                    addr = InetAddress.getByName(serverIP);
                    skt = new Socket(addr, serverPoint);
                    //登录将账户与密码发送过去
                    OutputStream out = skt.getOutputStream();
                    out.write(s.getBytes());
                    //接收服务器返回的登录信息
                    InputStream in = skt.getInputStream();
                    //接收数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String mes = null;
                    //获取服务器传来的数据
                    mes = reader.readLine();
                    //注册成功
                    if (mes.equals("OK")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "注册成功，请登录！");
                        handler.sendMessage(msg);
                    }//注册失败
                    else if (mes.equals("Error")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "注册失败，请重新注册！");
                        handler.sendMessage(msg);
                    }//账户已被使用
                    else if (mes.equals("Used")) {
                        Message msg = Message.obtain();
                        msg.getData().putString("info", "账号已被使用，请更换账号!");
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //接收好友列表字符串并反序列化为对象
    public ArrayList<User> deserializeToString(String mes) {
        ArrayList<User> friends = new ArrayList<>();
        String[] strs = mes.split("&&");
        try {
            for (String str : strs) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
                ObjectInputStream objIn = null;
                objIn = new ObjectInputStream(byteIn);
                User u = (User) objIn.readObject();
                friends.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (User u : friends) {
            System.out.println(u);
        }
        return friends;
    }
}
