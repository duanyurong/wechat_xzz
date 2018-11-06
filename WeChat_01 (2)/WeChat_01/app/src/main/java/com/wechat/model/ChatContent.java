package com.wechat.model;

import java.io.Serializable;

/**
 * Created by john on 2018/10/27.
 */

public class ChatContent implements Serializable {
    private int id;
    private boolean me;
    private String content;
    private String from_acc;
    private String my_acc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFrom_acc() {
        return from_acc;
    }

    public void setFrom_acc(String from_acc) {
        this.from_acc = from_acc;
    }

    public String getMy_acc() {
        return my_acc;
    }

    public void setMy_acc(String my_acc) {
        this.my_acc = my_acc;
    }
}
