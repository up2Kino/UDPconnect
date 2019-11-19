package com.sj0512_final;

import java.util.ArrayList;

public class MsgList implements Config {
	public ArrayList<byte[]> msglist ;
	private int id;
	private String msg;// ������е���Ϣ
	private int remainder;// �ü���Ϣʱ���������������һ�����Ĵ�С��������arraycopy������
	private int[] count;// count�����ÿ�����ݰ��ķ���������м���������ȷ��ÿ�����ڶ����ʽϸ�ʱ���ٷ�������
	private int nidedspace;// ���ն�����Ԥ��MsgList�Ĵ�С
	private byte[] finalmsg;
	private boolean[] recv_feedback;// ������־���飬���ڱ�ʾÿ��С���Ƿ��յ���Ӧ�ķ�����Ϣ�����ѱ�����
	private boolean[] recv_msg;// ������־���飬���ڱ�ʾ���շ��Ƿ��յ�����Ϣ

	// �˹��췽�����ڷ��Ͷ�������Ϣ�����������Ϣ
	MsgList(int id, String msg) {
		this.id = id;
		msglist = new ArrayList<byte[]>();
		this.cutmsg(id, msg);
	}

	// �˹��췽�����ڽ��ն˽��ܵ����ư�ʱʹ��
	MsgList(int id, int nidedspace) {
		this.id = id;
		this.nidedspace = nidedspace;
		msglist = new ArrayList<byte[]>();
		recv_msg = new boolean[nidedspace];
	}

	/*
	 * ��������Ϣ���в�ֲ���ӵ����Ͷ��еķ�����
	 */
	public void cutmsg(int id, String msg) {

		byte[] ID = intxTo4Byte(id);
		byte[] content = msg.getBytes();

		nidedspace = getNiedspace(content.length);// ��ȡ��Ϣ�ü��������ݰ�������

		count = new int[nidedspace+1];// �ڴ˴���ʼ������־����Ĵ�С
		recv_feedback = new boolean[nidedspace+1];

		msglist.add(CreateHead(ID, nidedspace));// ���ɿ��ư� ������ΪID+niedspace+flag

		// �������ݰ�����װ�����MsgList
		for (int i = 0; i < nidedspace; i++) {
			// ��װ��Ϣ
			byte[] DataPack = new byte[datasize + Packsize_data];
			System.arraycopy(ID, 0, DataPack, 0, ID.length);
			byte[] sid = intxTo4Byte(i);
			System.arraycopy(sid, 0, DataPack, 4, sid.length);
			DataPack[Packsize_data - 1] = 1;// flag,���ݰ�Ϊ1

			// ����
			if (i + 1 == nidedspace) {// �ж��Ƿ�Ϊ���һ�����ݰ������СӦΪremainder
				// ��100byteΪ�̶���������ν�ȡcontent������DataPack�ϣ����һ�����⣩
				System.arraycopy(content, i * datasize, DataPack, Packsize_data, remainder);
				msglist.add(DataPack);
			} else {
				System.arraycopy(content, i * datasize, DataPack, Packsize_data, datasize);
				msglist.add(DataPack);
			}

		}

	}

	// ����ID��nidedspace���ɿ��ư�
	private byte[] CreateHead(byte[] ID, int nidedspace) {
		byte[] head = new byte[Packsize_head];
		System.arraycopy(ID, 0, head, 0, ID.length);
		byte[] ns = intxTo4Byte(nidedspace);
		System.arraycopy(ns, 0, head, ID.length, ns.length);
		head[Packsize_head - 1] = 0;// flag��������󣬿��ư�Ϊ0
		return head;
	}

	/**
	 * ʹ������ȡ��������轫��Ϣ�ָ�ķ����������/100������
	 * 
	 * @param length ��Ϣ�ĳ���
	 * @return ע�Ͳ���Ϊԭʼ����������Math����API����
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

	public void set(int index, byte[] msg) {// ��дset��������ֱ�Ӹ���msglist�е�ĳԪ��
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

		if (flag == 0) {// ���������������
			for (int i = 0; i < recv_feedback.length; i++) {
				if (!recv_feedback[i]) {
					return false;
				}
			}
		} else if (flag == 1) {// ���������������
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
