package com.wechat.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @ClassName HibernateUtils
 * @Description TODO
 * @Author Luo Xiaodong
 * @Date 2018/10/215:35
 **/
public class HibernateUtils {
    //SessionFactory在web项目中只创建一个对象
    private static SessionFactory sf;

    static {
        Configuration cfg = new Configuration().configure();
        sf = cfg.buildSessionFactory();
    }
    //获取新的session
    public static Session openSession() {
        Session session = sf.openSession();
        return session;
    }
    //获取与线程绑定的session
    public static Session getCurrentSession() {
        return sf.getCurrentSession();
    }
}
