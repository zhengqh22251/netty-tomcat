package com.zqh.netty.Tomcat.Http;

/**
 * @Author：zhengqh
 * @date 2020/2/14 10:28
 **/
public abstract  class MyServlet {
      public void service(MyRequest request,MyResponse response) throws Exception{
         if("GET".equalsIgnoreCase(request.getMethod())){
            doGet(request,response);
         }else{
             doPost(request,response);
         }
      }

    protected abstract void doPost(MyRequest request, MyResponse response) throws Exception;

    protected abstract void doGet(MyRequest request, MyResponse response) throws Exception;

}
