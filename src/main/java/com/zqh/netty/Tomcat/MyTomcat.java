package com.zqh.netty.Tomcat;

import com.zqh.netty.Tomcat.Http.MyRequest;
import com.zqh.netty.Tomcat.Http.MyResponse;
import com.zqh.netty.Tomcat.Http.MyServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author：zhengqh
 * @date 2020/2/14 10:16
 **/

//Netty就是一个同时支持多协议的网络通信框架
public class MyTomcat {
    private int port =8900;
    private Properties webxml= new Properties();
    private Map<String,MyServlet> servletMapping =new HashMap<>();

    private void init(){
        try {
            //加载web.xml文件,同时初始化 ServletMapping对象
            String WEB_INF = this.getClass().getResource("/").getPath();
            FileInputStream fis =new FileInputStream(WEB_INF+"web.properties");

            webxml.load(fis);

            for (Object k: webxml.keySet()) {
                String key =k.toString();
               if(key.endsWith(".url")){
                   String servletName =key.replaceAll("\\.url$","");
                   String url= webxml.getProperty(key);
                   String className = webxml.getProperty(servletName+".className");
                   MyServlet servlet = (MyServlet) Class.forName(className).newInstance();
                   servletMapping.put(url,servlet);
               }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public  void start(){
      init();

        //Netty封装了NIO，Reactor模型，Boss，worker
        // Boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // Worker线程
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
        // Netty服务
        //ServerBootstrap   ServerSocketChannel
        ServerBootstrap server = new ServerBootstrap();
        // 链路式编程
        server.group(bossGroup,workGroup)
          // 主线程处理类,看到这样的写法，底层就是用反射
                .channel(NioServerSocketChannel.class)
        // 子线程处理类 , Handler
        .childHandler(new ChannelInitializer<SocketChannel>() {
            // 客户端初始化处理
            protected void initChannel(SocketChannel client) throws Exception {
                // 无锁化串行编程
                //Netty对HTTP协议的封装，顺序有要求
                // HttpResponseEncoder 编码器
               client.pipeline().addLast(new HttpResponseEncoder());
                // HttpRequestDecoder 解码器
                client.pipeline().addLast(new HttpRequestDecoder());
                // 业务逻辑处理
                client.pipeline().addLast(new MyTomcatHandler());

            }
        })
        // 针对主线程的配置 分配线程最大数量 128
        .option(ChannelOption.SO_BACKLOG,128)
        // 针对子线程的配置 保持长连接
        .childOption(ChannelOption.SO_KEEPALIVE,true);

       //启动服务器

            ChannelFuture f = server.bind(port).sync();
            System.out.println("MyTomcat 启动了，监听端口："+port);
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

     class MyTomcatHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg  instanceof HttpRequest){
                HttpRequest req = (HttpRequest) msg;
                // 转交给我们自己的request实现
                MyRequest request = new MyRequest(ctx,req);
                // 转交给我们自己的response实现
                MyResponse response = new MyResponse(ctx,req);

                // 实际业务处理
                String url =request.getUrl();

                if(servletMapping.containsKey(url)){
                  servletMapping.get(url).service(request,response);
                }else{
                    response.write("404  not found");
                }

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        }
    }



    public static void main(String[] args) {
        new MyTomcat().start();
    }
}

