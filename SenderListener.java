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

	public void keyReleased(KeyEvent e) {//�س����͹��ܵ�ʵ��
		switch (e.getKeyCode()) {
		case 10:
			Send();
			break;
		}
	}

	private void Send() {
		if (Sendarea.getText().equals("")) {// �����жϣ����ⷢ�Ϳհ���Ϣ
			System.out.println("You cannot send a null message");
		} else {//�������ʱ������message�����뷢����Ϣ����
 			int id =createID();
 			//���һ��Ҫ���͵���Ϣ����Ϣ������MsgList������в��
			msg_arr_send.add(new MsgList(id, Sendarea.getText()));
			Recvarea.append(dFormat.format(new Date())+"\r\n"+Sendarea.getText()+"\r\n");
		}
	}

	private int createID() {//��ȡϵͳ��ǰʱ�䣨ʱ������룩��ת���ɺ��뵥λ��һ��int������������ID��
		Calendar cal = Calendar.getInstance();
		int HH = cal.get(Calendar.HOUR_OF_DAY);
		int mm = cal.get(Calendar.MINUTE);
		int ss = cal.get(Calendar.SECOND);
		int MI = cal.get(Calendar.MILLISECOND);

		int ID = ((HH * 60 + mm) * 60 + ss) * 1000 + MI;

		return ID;

	}

	public static byte[] intxTo4Byte(int x) {//��һ��int������ת��Ϊ4��byte��byte����
		byte[] result = new byte[4];
		result[0] = (byte) ((x >> 24) & 0xFF);
		result[1] = (byte) ((x >> 16) & 0xFF);
		result[2] = (byte) ((x >> 8) & 0xFF);
		result[3] = (byte) (x & 0xFF);
		return result;
	}
}
