package com.sj0512_final;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

public interface Config {
	
	int datasize = 100;// 基本数据包的大小
	int Packsize_head = 9;//控制包封装信息的大小，ID+flag+niedspace（必为int型）
	int Packsize_data = 9;//基本数据包封装信息的大小，ID+flag+sid
	SocketAddress localAddr_send_msg = new InetSocketAddress("169.254.117.240", 20001);
	SocketAddress localAddr_send_feedback = new InetSocketAddress("169.254.117.240", 20002);
	SocketAddress localAddr_recv_msg = new InetSocketAddress("169.254.117.240", 20003);
	SocketAddress localAddr_recv_feedback = new InetSocketAddress("169.254.117.240", 20004);
	SocketAddress destAddr_msg = new InetSocketAddress("169.254.117.240", 20003);
	SocketAddress destAddr_feedback = new InetSocketAddress("169.254.117.240", 20004);
	ArrayList<MsgList> msg_arr_send = new ArrayList<MsgList>();// 发送端的信息队列
	ArrayList<MsgList> msg_arr_recv = new ArrayList<MsgList>();// 接受端的信息队列
	// static final加上保证将该属性绑定在config类上，避免在不同类实现中生成不同的对象
}
