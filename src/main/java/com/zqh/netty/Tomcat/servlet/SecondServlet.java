package com.zqh.netty.Tomcat.servlet;


import com.zqh.netty.Tomcat.Http.MyRequest;
import com.zqh.netty.Tomcat.Http.MyResponse;
import com.zqh.netty.Tomcat.Http.MyServlet;

public class SecondServlet extends MyServlet {

	public void doGet(MyRequest request, MyResponse response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(MyRequest request, MyResponse response) throws Exception {
		response.write("This is Second Serlvet");
	}

}
