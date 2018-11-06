package com.wechat.model;

/**
 * @ClassName OperateResult
 * @Description TODO  操作结果返回类型
 * @Author Luo Xiaodong
 * @Date 2018/11/223:30
 **/
public class OperateResult {

    public String mes;
    public Object obj;
    public String operateCode;
    public OperateResult(){
        //默认操作成功Code为0x0000  出错为0x0001
        this.operateCode="0x0000";
    }
}
