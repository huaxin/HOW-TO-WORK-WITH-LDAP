package com.ericsson.ldap.test;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="AsyncServlet",urlPatterns={"/testAsyn.do"},asyncSupported=true)
public class AsyncServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        ServletInputStream input = request.getInputStream();
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = input.read(b)) != -1) {

        }

    }
    public void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        //通过request获得AsyncContent对象
        AsyncContext actx = request.startAsync();
        //设置异步调用超时时长
        actx.setTimeout(30*3000);
        ServletInputStream servletInputStreams = request.getInputStream();
        //异步读取（实现了非阻塞式读取）
        servletInputStreams.setReadListener(new MyReadListener(servletInputStreams,actx));
        // 直接输出到页面的内容(不等异步完成就直接给页面)
        ServletOutputStream servletOutputStream = response.getOutputStream();
        //.....
        servletOutputStream.setWriteListener(new MyWriteListener(servletOutputStream,actx));

        servletOutputStream.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
        servletOutputStream.flush();
    }
}
