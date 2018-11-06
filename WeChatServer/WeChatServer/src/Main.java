import com.wechat.dao.UserDao;
import com.wechat.model.OperateResult;
import com.wechat.model.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName Main
 * @Description TODO
 * @Author Luo Xiaodong
 * @Date 2018/11/219:26
 **/
public class Main {

    public static void main(String[] args) {
        //启动服务器
        //子线程：接收连接的客户端
        Thread th1 = new Thread(new AcceptThread());
        th1.start();
    }
}

//服务器端监听客户端连接  线程
class AcceptThread implements Runnable {
    public ServerSocket server;
    public Socket skt;
    public HashMap<String, Socket> skts = new HashMap<String, Socket>();

    public AcceptThread() {
        try {
            server = new ServerSocket(23);
            System.out.println("服务器已启动...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("等待连接...");
                skt = server.accept();
                System.out.println("d" + skt);
                //接收到一个客户端连接，就开启一个线程来用作接收信息
                System.out.println("客户端：" + skt.getInetAddress() + "连接成功...");
                new Thread(new ReceiveMes(skt, skts)).start();
                Thread.sleep(5000);
                System.out.println("服务器已连接" + skts.size() + "个客户端！");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ReceiveMes implements Runnable {
    public HashMap<String, Socket> skts;
    private byte[] data;
    public Socket skt;
    public Socket f_skt;

    public ReceiveMes(Socket socket, HashMap<String, Socket> skts) {
        this.skt = socket;
        this.skts = skts;
    }

    @Override
    public void run() {
        boolean b = true;
        try {
            //定义流
            InputStream in = skt.getInputStream();
            OutputStream out = skt.getOutputStream();
            UserDao ud = new UserDao();
            //接收数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String mes = null;
            //获取客户端传来的数据
            while (b) {
                mes = reader.readLine();
                if (mes != null) {
                    System.out.println("接收到信息:" + mes);
                    String re = mes.substring(0, 3);
                    //如果前三个字符是"101"，表示登录
                    if (re.equals("101")) {
                        String login_pass = mes.substring(3);
                        String s[] = login_pass.split("_");
                        String acc = s[0];
                        String pass = s[1];
                        ud = new UserDao();
                        OperateResult op = ud.login(acc, pass);
                        //如果登录成功
                        if (op.operateCode == "0x0000") {
                            //登录成功，下面返回好友列表
                            User u = (User) op.obj;
                            List<User> friends = ud.getFriends(u.getId());
                            //序列化为字符串
                            String friends_str = ud.SerializetoStr(friends);
                            op.mes = friends_str + "\n";
                            //保存客户端skt
                            skts.put(acc, skt);
                        }
                        //把操作信息返回给客户端
                        out.write((op.mes+"\n").getBytes());
                        //把离线信息传给客户端
                        String offonlinemsg =ud.readOffOnLineMes(acc);
                        System.out.println("offonlinemsg："+offonlinemsg);
                        out.write((offonlinemsg+"\n").getBytes());
                        System.out.println("离线信息："+offonlinemsg);
                    }
                    //如果前三个字节是  102 表示聊天  skt_mes="102"+"微信号"+"_"+"信息" ,进行转发
                    else if (re.equals("102")) {
                        String skt_mes = mes.substring(3);
                        //s[0]:需要转发到的好友account s[1]信息来自好友的账号，s[2]信息
                        String s[] = skt_mes.split("_");
                        f_skt = skts.get(s[0]);
                        if (f_skt != null) {
                            System.out.println("正在保持连接的客户端数量：" + skts.size());
                            OutputStream f_out = f_skt.getOutputStream();
                            f_out.write((s[1]+"_"+s[2] + "\n").getBytes());
                            System.out.println("信息已经转发...");
                        } else {
                            //保存到数据库
                            ud.writeOffOnLineMes(skt_mes);
                            System.out.println("对方客户端未上线，信息已保存...");
                        }
                    } else if (re.equals("103")) {
                        System.out.println("此信息为安全退出信息...");
                        String acc = mes.substring(3);
                        f_skt = skts.get(acc);
                        OutputStream f_out = f_skt.getOutputStream();
                        f_out.write(("OUT\n").getBytes());
                        skts.remove(acc);
                        f_skt.close();
                        System.out.println("该客户端已安全退出...");
                        //本客户端退出后，该线程销毁
                        b = false;
                    }//104 表示注册
                    else if (re.equals("104")) {
                        System.out.println("接收到注册信息，正在准备注册新用户...");
                        String login_pass = mes.substring(3);
                        String s[] = login_pass.split("_");
                        String u_name = s[0];
                        String acc = s[1];
                        String pass = s[2];
                        ud = new UserDao();
                        String reg_mes = ud.register(u_name, acc, pass);
                        out.write((reg_mes + "\n").getBytes());
                        System.out.println("完成注册请求，结果已返回...");
                    }
                    //104 表示添加好友
                    else if (re.equals("105")) {
                        System.out.println("接收到添加好友信息，正在准备添加好友...");
                        String login_pass = mes.substring(3);
                        String s[] = login_pass.split("_");
                        String myself_acc = s[0];
                        String friend_acc = s[1];
                        String reg_mes = ud.addFriend(myself_acc, friend_acc);
                        out.write((reg_mes + "\n").getBytes());

                        //客户端接收此消息没实现
//                        //再把“我”序列化后传给好友
//                        f_skt = skts.get(friend_acc);
//                        //好友在线才发送此消息
//                        if (f_skt != null) {
//                            OutputStream f_out = f_skt.getOutputStream();
//                            String f_s = ud.SerializetoStrObj(ud.findByAcc(myself_acc));
//                            f_out.write((f_s + "\n").getBytes());
//                        }
                        System.out.println("完成添加好友请求，结果已返回...");
                    } //从聊天的窗口back回列表页面，需要让聊天窗口activity里的死循环接收信息子线程销毁
                    else if (re.equals("106")) {
                        System.out.println("接收到backActivity请求，正在返回信息...");
                        out.write(("backActivity\n").getBytes());
                        System.out.println("完成backActivity请求，结果已返回...");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}