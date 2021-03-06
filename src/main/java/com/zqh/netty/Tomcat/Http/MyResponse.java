package com.zqh.netty.Tomcat.Http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * @Author：zhengqh
 * @date 2020/2/14 10:30
 **/
public class MyResponse {
    //SocketChannel的封装
    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public MyResponse(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public void write(String out) {
        try {
            if (out == null || out.length() == 0) {
                return;
            }
            // 设置 http协议及请求头信息
            FullHttpResponse response = new DefaultFullHttpResponse(
                    // 设置http版本为1.1
                    HttpVersion.HTTP_1_1,
                    //设置响应状态码
                    HttpResponseStatus.OK,
                    // 将输出值写出 编码为UTF-8
                    Unpooled.wrappedBuffer(out.getBytes("UTF-8"))
            );

            response.headers().set("Content-Type","text/html;");

            ctx.write(response);

            } catch (Exception e) {
             e.getStackTrace();
        }finally {
            ctx.flush();
            ctx.close();
        }
    }
}