package com.sj0512_final;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

public interface Config {
	
	int datasize = 100;// �������ݰ��Ĵ�С
	int Packsize_head = 9;//���ư���װ��Ϣ�Ĵ�С��ID+flag+niedspace����Ϊint�ͣ�
	int Packsize_data = 9;//�������ݰ���װ��Ϣ�Ĵ�С��ID+flag+sid
	SocketAddress localAddr_send_msg = new InetSocketAddress("169.254.117.240", 20001);
	SocketAddress localAddr_send_feedback = new InetSocketAddress("169.254.117.240", 20002);
	SocketAddress localAddr_recv_msg = new InetSocketAddress("169.254.117.240", 20003);
	SocketAddress localAddr_recv_feedback = new InetSocketAddress("169.254.117.240", 20004);
	SocketAddress destAddr_msg = new InetSocketAddress("169.254.117.240", 20003);
	SocketAddress destAddr_feedback = new InetSocketAddress("169.254.117.240", 20004);
	ArrayList<MsgList> msg_arr_send = new ArrayList<MsgList>();// ���Ͷ˵���Ϣ����
	ArrayList<MsgList> msg_arr_recv = new ArrayList<MsgList>();// ���ܶ˵���Ϣ����
	// static final���ϱ�֤�������԰���config���ϣ������ڲ�ͬ��ʵ�������ɲ�ͬ�Ķ���
}
