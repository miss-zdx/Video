package com.yf.video;

public class DownFileBean {

    private int code;
    private String msg;
    public DownFileBean(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "DownFileBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
