package com.sj0512_final;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 发送（第二）线程，检查发送消息队列，并逐一发送
 * @author Administrator
 */
public class send_Thread extends Thread implements Config{
	public JTextField Sendarea;
	public JTextArea Recvarea;
	private DatagramSocket dSender;
	
	public send_Thread(JTextField Sendarea, JTextArea Recvarea) {
//		this.msg_arr_send = msg_arr_send;
 		this.Sendarea = Sendarea;
		this.Recvarea = Recvarea;
		try {
			dSender = new DatagramSocket(localAddr_send_msg);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true) {
			for(int i = 0; i < msg_arr_send.size(); i++) {
				for(int j = 0;j< msg_arr_send.get(i).getSize();j++) {
					if(!msg_arr_send.get(i).getreceived(j)) {//若未收到反馈，则发送之
						Send(msg_arr_send.get(i).get(j));
						msg_arr_send.get(i).Count(j);
					}
					if(msg_arr_send.get(i).getCount(j) >= 3) {//重发次数超过三次则放弃该信息
						msg_arr_send.get(i).fb_received(j);;//仅在储存ID号的
					}
				}
				
				
				
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void Send(byte[] msg) {
//		int id = message.getid();
//		byte[] ID = intxTo4Byte(id);
//		byte[] content = (message.getmsg()).getBytes();
//		byte[] msg = new byte[content.length+ID.length];
//		System.arraycopy(ID, 0, msg, 0, ID.length);
//		System.arraycopy(content, 0, msg, ID.length, content.length);
//		LogTools.INFO(this.getClass(),"ID："+id+" 信息已发送！");
		
		
		//send()直接发送byte[];
 		DatagramPacket dp = new DatagramPacket(msg, msg.length, destAddr_msg);// 5.创建要发送的数据包,指定内容,指定目标地址
		try {
			dSender.send(dp);
		} catch (IOException e2) {
			LogTools.ERROR("send "+msg.toString(), e2);
		}
	}

//	public static byte[] intxTo4Byte(int x) {
//		byte[] result = new byte[4];
//		result[0] = (byte) ((x >> 24) & 0xFF);
//		result[1] = (byte) ((x >> 16) & 0xFF);
//		result[2] = (byte) ((x >> 8) & 0xFF);
//		result[3] = (byte) (x & 0xFF);
//		return result;
//	}

}
