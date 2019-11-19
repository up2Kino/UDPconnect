package com.sj0512_final;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SenderListener extends KeyAdapter implements ActionListener,Config {
	public JTextField Sendarea;
	public JTextArea Recvarea;
	public SimpleDateFormat dFormat;
	private Thread send_Thread;

	public SenderListener(JTextField Sendarea, JTextArea Recvarea, SimpleDateFormat dFormat) {
 		this.Sendarea = Sendarea;
		this.Recvarea = Recvarea;
		this.dFormat = dFormat;
		send_Thread = new send_Thread(Sendarea,Recvarea);
		send_Thread.start();
	}

	public void actionPerformed(ActionEvent e) {
 		Send();
	}

	public void keyReleased(KeyEvent e) {//回车发送功能的实现
		switch (e.getKeyCode()) {
		case 10:
			Send();
			break;
		}
	}

	private void Send() {
		if (Sendarea.getText().equals("")) {// 条件判断，避免发送空白信息
			System.out.println("You cannot send a null message");
		} else {//点击发送时，生成message并存入发送信息队列
 			int id =createID();
 			//添加一条要发送的信息，信息将会在MsgList里面进行拆分
			msg_arr_send.add(new MsgList(id, Sendarea.getText()));
			Recvarea.append(dFormat.format(new Date())+"\r\n"+Sendarea.getText()+"\r\n");
		}
	}

	private int createID() {//获取系统当前时间（时分秒毫秒），转化成毫秒单位的一个int型整数，当作ID号
		Calendar cal = Calendar.getInstance();
		int HH = cal.get(Calendar.HOUR_OF_DAY);
		int mm = cal.get(Calendar.MINUTE);
		int ss = cal.get(Calendar.SECOND);
		int MI = cal.get(Calendar.MILLISECOND);

		int ID = ((HH * 60 + mm) * 60 + ss) * 1000 + MI;

		return ID;

	}

	public static byte[] intxTo4Byte(int x) {//将一个int型整数转化为4个byte的byte数组
		byte[] result = new byte[4];
		result[0] = (byte) ((x >> 24) & 0xFF);
		result[1] = (byte) ((x >> 16) & 0xFF);
		result[2] = (byte) ((x >> 8) & 0xFF);
		result[3] = (byte) (x & 0xFF);
		return result;
	}
}
