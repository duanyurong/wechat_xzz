package com.wechat.dao;

import com.wechat.model.ChatContent;
import com.wechat.model.Friend;
import com.wechat.model.OperateResult;
import com.wechat.model.User;
import com.wechat.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName UserDao
 * @Description TODO
 * @Author Luo Xiaodong
 * @Date 2018/11/221:27
 **/
public class UserDao {
    public OperateResult login(String acc, String pass) {
        OperateResult op = new OperateResult();
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from User u where u.account=?";   //select * 省略
        Query query = session.createQuery(sql);
        query.setParameter(0, acc);
        User u = (User) query.uniqueResult();
        //如果为空，账户不存在
        if (u == null) {
            op.operateCode = "0x0001";
            op.mes = "null";
            return op;
        }
        //如果密码错误
        if (!u.getPassword().equals(pass)) {
            op.operateCode = "0x0002";
            op.mes = "password_error";
            return op;
        }
        //登录成功
        transaction.commit();
        session.close();
        op.obj = u;
        return op;
    }

    //获取好友列表
    public List<User> getFriends(int id) {
        List<User> friends = new ArrayList<User>();
        Session session = HibernateUtils.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from Friend f where f.u_id=?";   //select * 省略
        Query query = session.createQuery(sql);
        query.setParameter(0, id);
        List<Friend> list = query.list();
        for (Friend f : list) {
            String sql2 = "from User u where u.id=?";   //select * 省略
            Query query1 = session.createQuery(sql2);
            query1.setParameter(0, f.getF_id());
            User u = (User) query1.uniqueResult();
            friends.add(u);
        }
        //登录成功
        transaction.commit();
        //session.close();  ///HibernateUtils.getCurrentSession(); 获取的session内部自动关闭
        return friends;
    }

    //注册用户
    public String register(String u_name, String acc, String pass) {
        String s = "OK";
        try {
            Session session = HibernateUtils.openSession();
            Transaction transaction = session.beginTransaction();

            String sql = "from User u where u.account=?";   //select * 省略
            Query query = session.createQuery(sql);
            query.setParameter(0, acc);
            User u = (User) query.uniqueResult();
            if (u != null) {
                s = "Used";
                return s;
            }
            u = new User();       //没有id值,与session无关联(缓存中没有)，瞬时状态
            u.setAccount(acc);
            u.setPassword(pass);
            u.setUsername(u_name);
            session.save(u);
            transaction.commit();
        } catch (Exception e) {
            s = "Error";
        } finally {
            return s;
        }
    }

    //保存离线信息
    public void writeOffOnLineMes(String mes) {
        //s[0]:接收者，s[1]:发送者，s[2] 信息内容
        String s[] = mes.split("_");
        System.out.println("即将保存离线信息：" + mes);
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        ChatContent oom = new ChatContent();
        oom.setMy_acc(s[0]);
        oom.setFrom_acc(s[1]);
        oom.setContent(s[2]);
        oom.setMe(false);
        session.save(oom);
        transaction.commit();
        session.close();
    }

    //发送离线信息
    public String readOffOnLineMes(String my_acc) {
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from ChatContent oom where oom.my_acc=?";
        Query query = session.createQuery(sql);
        query.setParameter(0, my_acc);
        List<ChatContent> ooms = (List<ChatContent>) query.list();
        String ss = "";
        try {
            for (ChatContent o : ooms) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
                objOut.writeObject(o);
                String str = byteOut.toString("ISO-8859-1");
                ss += str + "&&";
            }
        } catch (Exception e) {
            System.out.println("bug"+e.getMessage());
        } finally {
            transaction.commit();
            session.close();
        }
        //把取出来的数据从数据库删除
        new Thread() {
            @Override
            public void run() {
                Session session2 = HibernateUtils.openSession();
                Transaction transaction2 = session2.beginTransaction();
                String sql2 = "delete from ChatContent oom where oom.my_acc=?";
                Query query2 = session2.createQuery(sql2);
                query2.setParameter(0, my_acc);
                query2.executeUpdate();
                transaction2.commit();
                session2.close();
            }
        }.start();
        return ss;
    }

    //添加好友
    public String addFriend(String myself_acc, String f_acc) {
        String s = "OK";
        try {
            System.out.println(myself_acc + "_" + f_acc);
            Session session = HibernateUtils.openSession();
            Transaction transaction = session.beginTransaction();
            String sql = "from User u where u.account=?";
            Query query = session.createQuery(sql);
            query.setParameter(0, f_acc);
            //查找好友
            User f = (User) query.uniqueResult();
            //好友不存在
            if (f == null) {
                s = "NO_Exist";
                System.out.println("好友不存在...");
                return s;
            }
            String sql2 = "from User u where u.account=?";
            Query query2 = session.createQuery(sql);
            query2.setParameter(0, myself_acc);
            User myself = (User) query2.uniqueResult();

            String sql3 = "from Friend f where f.f_id=? and f.u_id=?";
            Query query3 = session.createQuery(sql3);
            query3.setParameter(0, f.getId());
            query3.setParameter(1, myself.getId());
            Friend f_u = (Friend) query3.uniqueResult();
            //已经是好友
            if (f_u != null) {
                s = "Friend";
                System.out.println("已经是好友...");
                return s;
            }
            System.out.println("正在添加...");
            Friend f1 = new Friend();
            f1.setU_id(myself.getId());
            f1.setRemark("gf");
            f1.setF_id(f.getId());
            Friend f2 = new Friend();
            f2.setU_id(f.getId());
            f2.setRemark("gf");
            f2.setF_id(myself.getId());
            session.save(f1);
            session.save(f2);
            transaction.commit();
            //把好友对象序列化后传回客户端
            s = SerializetoStrObj(f);
        } catch (Exception e) {
            s = "Error";
        } finally {
            return s;
        }
    }

    //查找好友
    public User findByAcc(String acc) {
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from User u where u.account=?";
        Query query = session.createQuery(sql);
        query.setParameter(0, acc);
        //查找好友
        User f = (User) query.uniqueResult();
        return f;
    }

    //把好友集合序列化为字符串
    public String SerializetoStr(List<User> friends) throws Exception {
        String s = "";
        for (User u : friends) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(u);
            String str = byteOut.toString("ISO-8859-1");
            s += str + "&&";
        }
        String ss[] = s.split("_");
        for (String t : ss) {
            System.out.println(t);
        }
        return s;
    }

    //把好友序列化为字符串
    public String SerializetoStrObj(User friend) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(friend);
        System.out.println("tt:" + friend);
        String s = byteOut.toString("ISO-8859-1");
        return s;
    }
}
