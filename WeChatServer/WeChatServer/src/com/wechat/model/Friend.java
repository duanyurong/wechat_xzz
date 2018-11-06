package com.wechat.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName friend
 * @Description TODO
 * @Author Luo Xiaodong
 * @Date 2018/11/217:45
 **/
public class Friend {
    private int id;
    private int u_id;
    private String remark;
    private int f_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getU_id() {
        return u_id;
    }

    public void setU_id(int u_id) {
        this.u_id = u_id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getF_id() {
        return f_id;
    }

    public void setF_id(int f_id) {
        this.f_id = f_id;
    }
}
