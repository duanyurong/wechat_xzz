package com.wechat.test;

import com.wechat.model.ChatContent;
import com.wechat.model.Friend;
import com.wechat.model.User;
import com.wechat.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName UserTest
 * @Description TODO
 * @Author Luo Xiaodong
 * @Date 2018/11/220:49
 **/
public class UserTest {
    @Test
    public void fun1() {
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from User where id=1";   //select * 省略
        Query query = session.createQuery(sql);
        User u = (User) query.uniqueResult();
        System.out.println(u);
        transaction.commit();
        session.close();
    }

    @Test
    public void fun2() throws Exception {
        User u = new User();
        u.setAccount("rr");
        u.setId(5);
        u.setPassword("po");
        u.setUsername("rrrr");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(u);
        String string = byteArrayOutputStream.toString("ISO-8859-1");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        System.out.println(string);
    }

    @Test
    public void fun3() {
        int id = 1;
        List<User> friends = new ArrayList<User>();
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from Friend f where f.u_id=?";   //select * 省略
        Query query = session.createQuery(sql);
        query.setParameter(0, id);
        List<Friend> list = query.list();
        System.out.println("listcount"+list.size());
        for (Friend f : list) {
            String sql2 = "from User u where u.id=?";   //select * 省略
            Query query1 = session.createQuery(sql2);
            query1.setParameter(0, f.getF_id());
            User u = (User) query1.uniqueResult();
            System.out.println("uuu" + u.getAccount());
            friends.add(u);
        }
        //登录成功
        transaction.commit();
        session.close();

    }
    @Test
    public void fun4() throws Exception{
        List<User> friends =new ArrayList<>();
        User u = new User();
        u.setAccount("qqqqqqqq");
        u.setId(1);
        u.setPassword("oooooooooo");
        u.setUsername("rrrrrrr");
        User u2 = new User();
        u2.setAccount("ee");
        u2.setId(4);
        u2.setPassword("123");
        u2.setUsername("ttt");
        friends.add(u2);
        friends.add(u);


        String s="";
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        for(User uu:friends) {
            objOut.writeObject(uu);
            System.out.println("tt:"+uu);
            String str = byteOut.toString("ISO-8859-1");
            s+=str+"_";
        }
        String ss[] =s.split("_");
        for(String t:ss){
            System.out.println(t);
        }
    }
    @Test
    public void fun5(){
        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        User u = new User();       //没有id值,与session无关联(缓存中没有)，瞬时状态
        u.setAccount( "ee");
        u.setPassword( "pp");
        u.setUsername("wxxyh"); //默认用户名 微信新用户
        session.save(u);
        transaction.commit();

    }
    @Test
    public void fun6(){
       String f_acc="yhh";
       String  myself_acc ="dxl";

        Session session = HibernateUtils.openSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from User u where u.account=?";
        Query query = session.createQuery(sql);
        query.setParameter(0, f_acc);
        User f = (User) query.uniqueResult();
        //好友不存在
        if (f == null) {
            System.out.println("NO_Exist");
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
        if(f_u!=null){
            System.out.println("Friend");
        }
        System.out.println("正在添加");
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
    }
    @Test
    public void fun7(){
        Session session = HibernateUtils.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        String sql = "from ChatContent oom where oom.self=?";
        Query query = session.createQuery(sql);
        query.setParameter(0, "ddd");
        List<ChatContent> ooms = (List<ChatContent>) query.list();
        transaction.commit();
    }

}
