package com.ericsson.ldap.test;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

public class MyWriteListener implements WriteListener {
    private ServletOutputStream out;
    private AsyncContext ac;
    public MyWriteListener(ServletOutputStream output,AsyncContext context){
        this.out = output;
        this.ac = context;
    }

    @Override
    public void onWritePossible() throws IOException {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}
