package com.ericsson.ldap.test;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class MyReadListener implements ReadListener {
    private ServletInputStream in; private AsyncContext ac;
    public MyReadListener(ServletInputStream input,AsyncContext context){
        this.in = input;this.ac = context;
    }
    //数据可用时触发执行
    @Override
    public void onDataAvailable(){
        // TODO: handle
    }
    //数据读完时触发调用
    @Override
    public void onAllDataRead(){
        // TODO: handle
    }
    //数据出错触发调用
    @Override
    public void onError(Throwable t){
        // TODO: handle
    }
}