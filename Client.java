package com.sj0512_final;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client implements Config {

	public SimpleDateFormat dFormat;// 显示时间用到的格式
	private DatagramSocket recvSocket;// 本地接收端对应的Socket
	private DatagramSocket dSender_feedback;

	public static void main(String[] args) {
		Client client = new Client();
		client.initUI();
	}

	public Client() {
		initAddr();
	}
	
	private void initAddr() {
		dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {// 2.负责接收信息的UDP端口
			recvSocket = new DatagramSocket(localAddr_recv_msg);
			dSender_feedback = new DatagramSocket(localAddr_send_feedback);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void initUI() {
		JFrame frame = new JFrame("通讯器");
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(3);
		frame.setLayout(new BorderLayout());
		JTextArea recvArea = new JTextArea(10, 10);// 接收信息
		recvArea.setEditable(false);

		JTextField sendArea = new JTextField();// 发送信息
		sendArea.setBackground(new Color(240, 240, 240));

		JPanel Recvpanel = new JPanel();
		recvArea.setPreferredSize(new Dimension(500, 300));
		recvArea.setBackground(new Color(245, 245, 245));
		recvArea.setAlignmentX(1);// 设置对齐方式为右对齐
		recvArea.setLineWrap(true);// 激活自动换行功能
		recvArea.setWrapStyleWord(true);// 激活断行不断字功能
		Recvpanel.setLayout(new BorderLayout());
		Recvpanel.add(recvArea, BorderLayout.CENTER);

		JPanel Dividepanel = new JPanel();// 分隔线
		Dividepanel.setPreferredSize(new Dimension(500, 20));
		Dividepanel.setBackground(new Color(80, 128, 180));

		JButton jSButton = new JButton("Send");// 发送信息的按钮
		jSButton.setPreferredSize(new Dimension(100, 50));
		jSButton.setBackground(new Color(40, 156, 255));

		SenderListener sl = new SenderListener(sendArea, recvArea, dFormat);
		jSButton.addActionListener(sl);
		sendArea.addKeyListener(sl);

		JPanel Sendpanel = new JPanel();
		Sendpanel.setPreferredSize(new Dimension(500, 100));
		Sendpanel.setLayout(new BorderLayout());
		Sendpanel.add(sendArea, BorderLayout.CENTER);
		Sendpanel.add(jSButton, BorderLayout.EAST);

		frame.add(Recvpanel, BorderLayout.NORTH);
		frame.add(Dividepanel, BorderLayout.CENTER);
		frame.add(Sendpanel, BorderLayout.SOUTH);
		frame.setVisible(true);

		DeleteThread dThread = new DeleteThread();
		dThread.start();
		recvRun(recvArea);// 负责接收信息的线程
	}


	private void recvRun(JTextArea recvArea) {
		while (true) {

			byte[] buffer = new byte[Packsize_data + datasize];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			LogTools.INFO(this.getClass(), " 等待接收数据 ");			
			try {
				recvSocket.receive(packet);
				LogTools.INFO(this.getClass(), " 已接受信息 ");
				tranmsg(packet.getData(), recvArea);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * 此方法负责对接受到的信息进行加工 此处接收到的数据包分为控制包或数据包，其大小均不会超过100byte
	 * @param buffer   处理对象是byte[]
	 * @param recvArea 用于处理完信息后显示于对话框上，现待进行改动
	 */
	private void tranmsg(byte[] buffer, JTextArea recvArea) {

		if (buffer[Packsize_head - 1] == 0) {// 根据标志位判断，若为控制包
			byte[] ID = new byte[4];
//			byte[] content = new byte[datasize];
			byte[] nidedspace_byte = new byte[4];

			System.arraycopy(buffer, 0, ID, 0, 4);
			System.arraycopy(buffer, 4, nidedspace_byte, 0, 4);
//			System.arraycopy(buffer, Packsize_head, content, 0, 4);

			int id = byteArrayToInt(ID);
			int nidedspace = byteArrayToInt(nidedspace_byte);
			// 检查确认在接收队列中是否有以此id创建的msgList，如果没有才创建
			// 确保在反馈包丢失而发送方重新发送控制包时不重复创建msgList
			int flag = 1;
			for (int i = 0; i < msg_arr_recv.size(); i++) {
				if (msg_arr_recv.get(i).getid() == id) {
					flag = 0;
				}
			}
			if (flag == 1) {
				msg_arr_recv.add(new MsgList(id, nidedspace));
				int pos = msg_arr_recv.size() - 1;// 记下此msglist的位置以便于后续操作
				byte[] blank = new byte[datasize];
				for (int i = 0; i < nidedspace; i++) {// 于msglist中提前创建好所需数量的数据包进行占位，便于在接收数据包时直接替代
					msg_arr_recv.get(pos).add(blank);
				}
			}
			feedback(ID,-1);// 无论是否成功创建msglist，都发送反馈信息

		} else if (buffer[Packsize_head - 1] == 1) {// 若为数据包

			byte[] ID = new byte[4];
			byte[] sid_byte = new byte[4];
			byte[] content = new byte[datasize];

			System.arraycopy(buffer, 4, sid_byte, 0, 4);
			System.arraycopy(buffer, 0, ID, 0, 4);
			int id = byteArrayToInt(ID);
			int sid = byteArrayToInt(sid_byte);
			
			for (int i = 0; i < msg_arr_recv.size(); i++) {

				if (msg_arr_recv.get(i).getid() == id) {
					System.arraycopy(buffer, Packsize_data, content, 0, buffer.length - Packsize_data);
					msg_arr_recv.get(i).set(sid, content);
					msg_arr_recv.get(i).msg_received(sid);
					if(msg_arr_recv.get(i).check(1)) {
						String msg = msg_arr_recv.get(i).MakePiezintoPlaz();
						recvArea.append(dFormat.format(new Date()) + "\r\n" + msg + "\r\n");
					}
					break;
				}
			}
			feedback(ID, sid);// 无论是否成功插入，均进行有效反馈收到
		}
	}

	public byte[] intxTo4Byte(int x) {// 将一个int型整数转化为4个byte的byte数组
		byte[] result = new byte[4];
		result[0] = (byte) ((x >> 24) & 0xFF);// 不影响x自身
		result[1] = (byte) ((x >> 16) & 0xFF);
		result[2] = (byte) ((x >> 8) & 0xFF);
		result[3] = (byte) (x & 0xFF);
		return result;
	}

	/**
	 * 一实不需要与运算，二应先转化为int再进行位运算，直接对byte进行移位只能将数据清零
	 */
	public static int byteArrayToInt(byte[] src) {
		int value;
		value = (int) (((src[0] & 0xFF) << 24) | ((src[1] & 0xFF) << 16) | ((src[2] & 0xFF) << 8) | (src[3] & 0xFF));
		return value;
	}

	/**
	 * 此方法需区分反馈的是控制包或是数据包 如果是数据包，应为ID+sid，以便发送队列进行删除
	 * 暂定方案：采取与发送信息时近似的封装方式，即ID(4)+flag(1)+sid(4)【控制包不需要sid，故内容仅需5byte，方便起见将flag放与第5位】
	 * 讨论确定方案：将两种数据包的反馈方法合为一种：大小均为八个字节，内容为ID+sid/flag
	 * sid/flag=-1时，为控制包
	 * sid/flag>=0，为数据包，且其值为sid
	 * 在接收方加上识别处理方法即可
	 */
	
	public void feedback(byte[] ID, int sid) {//反馈信息包发送
		byte[] fb = new byte[8];
		byte[] SID = intxTo4Byte(sid);
		System.arraycopy(ID, 0, fb, 0, 4);
		System.arraycopy(SID, 0, fb, 4, 4);
		int id = byteArrayToInt(ID);
		DatagramPacket packet = new DatagramPacket(fb, fb.length, destAddr_feedback);
		try {
			dSender_feedback.send(packet);
			LogTools.INFO(this.getClass(), "控制包-ID：" + id + " 已收到，其反馈信息已发送！");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
