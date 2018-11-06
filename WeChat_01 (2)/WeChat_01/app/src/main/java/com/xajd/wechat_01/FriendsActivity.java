package com.xajd.wechat_01;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.wechat.model.ChatContent;
import com.wechat.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {
    private ListView fri_lv;
    private FriendAdapter friendAdapter;
    private ArrayList<User> friends;
    private ArrayList<ChatContent> back_contents, in_contents;
    private Socket skt;
    private User myself;
    //数据库对象
    private MyDataBaseHelper dhHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        //聊天记录缓存
        dhHelper = new MyDataBaseHelper(this, "ChatRecord.db", null, 2);
        //好友列表
        friends = new ArrayList<>();
        back_contents = new ArrayList<>();
        in_contents = new ArrayList<>();
        Log.i("dxl", "进入FC,bc大小：" + back_contents.size());

        fri_lv = findViewById(R.id.fri_lv);
        fri_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(FriendsActivity.this, ChatActivity.class);
                Bundle bundleObject = new Bundle();
                SocketBinder sb = new SocketBinder();
                sb.setSkt(skt);
                sb.setDhHelper(dhHelper);
                bundleObject.putBinder("sb", sb);
                User friend = friends.get(position);
                String f_acc = friend.getAccount();
                in_contents.clear();
                Log.i("dxl", "信息缓冲池总量：" + back_contents.size());
                for (ChatContent cc : back_contents) {
                    if (cc.getFrom_acc().equals(f_acc)) {
                        in_contents.add(cc);
                    }
                }
                for (ChatContent cc : in_contents) {
                    back_contents.remove(cc);
                }
                Log.i("dxl", "该好友发来缓冲的信息量：" + in_contents.size());
                Log.i("dxl", "信息缓冲池剩余量：" + back_contents.size());
                bundleObject.putSerializable("in_contents", in_contents);
                bundleObject.putSerializable("myself", myself);
                bundleObject.putSerializable("friend", friend);
                intent.putExtras(bundleObject);
                startActivityForResult(intent, 1);
            }
        });
        //好友适配器准备
        friendAdapter = new FriendAdapter(this, R.layout.friend, friends);
        fri_lv.setAdapter(friendAdapter);

        Bundle extras = getIntent().getExtras();
        ArrayList<User> users = (ArrayList<User>) extras.getSerializable("friends");
        myself = (User) extras.getSerializable("myself");
        for (User u : users) {
            friends.add(u);
        }
        SocketBinder sb = (SocketBinder) extras.getBinder("sb");
        skt = sb.getSkt();
        friendAdapter.notifyDataSetChanged();
        //接收服务器信息
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream in = skt.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String mes = null;
                    while (true) {
                        //进行反序列化
                        //获取服务器传来离线数据信息
                        mes = reader.readLine();
                        String[] strs = mes.split("&&");
                        for (String str : strs) {
                            ByteArrayInputStream byteIn = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
                            ObjectInputStream objIn = null;
                            objIn = new ObjectInputStream(byteIn);
                            ChatContent oom = (ChatContent) objIn.readObject();
                            back_contents.add(oom);
                        }
                        Log.i("dxl", "信息缓冲池信息量：" + back_contents.size());
                    }
                } catch (Exception e) {
                }
            }
        }.start();
    }

    //返回键 安全退出
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("LogOut")
                .setMessage("确定退出微信吗？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    OutputStream os = skt.getOutputStream();
                                    InputStream in = skt.getInputStream();
                                    //接收数据
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                    os.write(("103" + myself.getAccount() + "\n").getBytes());
                                    skt.close();
                                    System.exit(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    //安全退出
    public void exit(View view) {
        new AlertDialog.Builder(this)
                .setTitle("LogOut")
                .setMessage("确定退出微信吗？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    OutputStream os = skt.getOutputStream();
                                    InputStream in = skt.getInputStream();
                                    //接收数据
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                    os.write(("103" + myself.getAccount() + "\n").getBytes());
                                    skt.close();
                                    System.exit(0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    //添加好友
    public void addfriend(View view) {
        Intent intent = new Intent();
        intent.setClass(FriendsActivity.this, AddFriendActivity.class);
        Bundle bundleObject = new Bundle();
        SocketBinder sb = new SocketBinder();
        sb.setSkt(skt);
        bundleObject.putBinder("sb", sb);
        bundleObject.putSerializable("myself", myself);
        intent.putExtras(bundleObject);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //进入添加好友Activity 后返回
        if (requestCode == 0) {
            //添加好友后，把好友加到列表中
            User u = (User) data.getExtras().getSerializable("friend");
            Log.i("dxl", "user:" + u);
            if (u != null) {
                friends.add(u);
                friendAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "添加好友成功...", Toast.LENGTH_LONG).show();
            }
        }//聊天后返回  接收上次与好友聊天过程中 来自 其他好友的信息,
        else if (requestCode == 1) {
            ArrayList<ChatContent> bc = (ArrayList<ChatContent>) data.getExtras().getSerializable("back_contents");
            back_contents.addAll(bc);
            Log.i("dxl", "从聊天窗口返回后，信息缓冲池总量：" + back_contents.size());
        }
    }
}
