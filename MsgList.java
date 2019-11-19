package com.sj0512_final;

import java.util.ArrayList;

public class MsgList implements Config {
	public ArrayList<byte[]> msglist ;
	private int id;
	private String msg;// 输入框中的信息
	private int remainder;// 裁剪信息时的余数，用作最后一个包的大小【受限与arraycopy方法】
	private int[] count;// count数组对每个数据包的发送情况进行计数，以期确认每个包在丢包率较高时至少发送三次
	private int nidedspace;// 接收端所需预留MsgList的大小
	private byte[] finalmsg;
	private boolean[] recv_feedback;// 布尔标志数组，用于表示每个小包是否收到对应的反馈信息，即已被接收
	private boolean[] recv_msg;// 布尔标志数组，用于表示接收方是否收到该信息

	// 此构造方法用于发送端向发送信息队列中添加信息
	MsgList(int id, String msg) {
		this.id = id;
		msglist = new ArrayList<byte[]>();
		this.cutmsg(id, msg);
	}

	// 此构造方法用于接收端接受到控制包时使用
	MsgList(int id, int nidedspace) {
		this.id = id;
		this.nidedspace = nidedspace;
		msglist = new ArrayList<byte[]>();
		recv_msg = new boolean[nidedspace];
	}

	/*
	 * 用来将信息进行拆分并添加到发送队列的方法。
	 */
	public void cutmsg(int id, String msg) {

		byte[] ID = intxTo4Byte(id);
		byte[] content = msg.getBytes();

		nidedspace = getNiedspace(content.length);// 获取信息裁剪所需数据包的数量

		count = new int[nidedspace+1];// 于此处初始化两标志数组的大小
		recv_feedback = new boolean[nidedspace+1];

		msglist.add(CreateHead(ID, nidedspace));// 生成控制包 ，内容为ID+niedspace+flag

		// 生成数据包，封装后存入MsgList
		for (int i = 0; i < nidedspace; i++) {
			// 封装信息
			byte[] DataPack = new byte[datasize + Packsize_data];
			System.arraycopy(ID, 0, DataPack, 0, ID.length);
			byte[] sid = intxTo4Byte(i);
			System.arraycopy(sid, 0, DataPack, 4, sid.length);
			DataPack[Packsize_data - 1] = 1;// flag,数据包为1

			// 正文
			if (i + 1 == nidedspace) {// 判断是否为最后一个数据包，其大小应为remainder
				// 以100byte为固定间隔，依次截取content内容于DataPack上（最后一包除外）
				System.arraycopy(content, i * datasize, DataPack, Packsize_data, remainder);
				msglist.add(DataPack);
			} else {
				System.arraycopy(content, i * datasize, DataPack, Packsize_data, datasize);
				msglist.add(DataPack);
			}

		}

	}

	// 根据ID和nidedspace生成控制包
	private byte[] CreateHead(byte[] ID, int nidedspace) {
		byte[] head = new byte[Packsize_head];
		System.arraycopy(ID, 0, head, 0, ID.length);
		byte[] ns = intxTo4Byte(nidedspace);
		System.arraycopy(ns, 0, head, ID.length, ns.length);
		head[Packsize_head - 1] = 0;// flag，放于最后，控制包为0
		return head;
	}

	/**
	 * 使用向上取整法获得需将信息分割的份数，并算出/100的余数
	 * 
	 * @param length 信息的长度
	 * @return 注释部分为原始方法，可用Math——API代替
	 */
	private int getNiedspace(int length) {
//		int nidedspace = length / 100;
//		if (length % 100 != 0) {
//			remainder = length % 100;
//			nidedspace++;
//		}
//		return nidedspace;
		remainder = length % 100;
		return (int) Math.ceil((double) length / 100);
	}

	public String MakePiezintoPlaz() {
		finalmsg = new byte[nidedspace * datasize];
		for (int i = 0; i < nidedspace; i++) {
			System.arraycopy(msglist.get(i), 0, finalmsg, i * datasize, msglist.get(i).length);
		}
		return new String(finalmsg);

	}

	public void Count(int i) {
		++count[i];
	}

	public int getCount(int i) {
		return count[i];
	}

	public int getNidedspace() {
		return nidedspace;
	}

	public int getid() {
		return id;
	}

	public String getmsg() {
		return msg;
	}

	public int getSize() {
		return msglist.size();
	}

	public void add(byte[] content) {
		msglist.add(content);
	}

	public void set(int index, byte[] msg) {// 重写set函数，可直接覆盖msglist中的某元素
		msglist.set(index, msg);
	}

	public byte[] get(int j) {
		return msglist.get(j);
	}

	public boolean getreceived(int i) {
		return recv_feedback[i];
	}

	public void fb_received(int i) {
		recv_feedback[i] = true;
	}

	public void msg_received(int i) {
		recv_msg[i] = true;
	}

	public boolean check(int flag) {

		if (flag == 0) {// 请求遍历反馈数组
			for (int i = 0; i < recv_feedback.length; i++) {
				if (!recv_feedback[i]) {
					return false;
				}
			}
		} else if (flag == 1) {// 请求遍历接收数组
			for (int i = 0; i < recv_msg.length; i++) {
				if (!recv_msg[i]) {
					return false;
				}
			}
		}
		return true;
	}

	public static byte[] intxTo4Byte(int x) {
		byte[] result = new byte[4];
		result[0] = (byte) ((x >> 24) & 0xFF);
		result[1] = (byte) ((x >> 16) & 0xFF);
		result[2] = (byte) ((x >> 8) & 0xFF);
		result[3] = (byte) (x & 0xFF);
		return result;
	}

}
