package com.sj0512_final;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DeleteThread extends Thread implements Config {

	private DatagramSocket recvSocket;// 本地接收端对应的Socket

	public DeleteThread() {
		try {
			recvSocket = new DatagramSocket(localAddr_recv_feedback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			byte[] buffer = new byte[datasize];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			LogTools.INFO(this.getClass(), " 正在查找 ！");
			try {
				recvSocket.receive(packet);
				tranmsg(packet.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	// 对收到的信息进行加工处理
	private void tranmsg(byte[] buffer) {
		byte[] ID = new byte[4];
		byte[] SID = new byte[4];
		System.arraycopy(buffer, 0, ID, 0, 4);
		System.arraycopy(buffer, 4, SID, 0, 4);
		int id = byteArrayToInt(ID);
		int sid = byteArrayToInt(SID);
		LogTools.INFO(this.getClass(), "ID：" + id + "sid:" + sid + " 反馈信息已收到");
		for (MsgList msglist : msg_arr_send) {// 从头到尾遍历链表的简略写法
			System.out.println(msglist.getid());
			if (msglist.getid() == id) {
				msglist.fb_received(sid+1);
				tryRemove(msglist);
				LogTools.INFO(this.getClass(), "ID：" + id + " 信息已收到反馈！");
				break;
			}
		}
		
	}

	public static int byteArrayToInt(byte[] src) {
		int value;
		value = (int) (((src[0] & 0xFF) << 24) | ((src[1] & 0xFF) << 16) | ((src[2] & 0xFF) << 8) | (src[3] & 0xFF));
		return value;
	}
	
	/**
	 * 在收到反馈信息后检查改动的msglist的recv_feedback数组是否全为true（全部已被收到）
	 * 如果符合，则将该msglist从队列中移除
	 * @param msglist
	 */
	public void tryRemove(MsgList msglist) {
		boolean flag = true;
		for(int i=0;i<msglist.getNidedspace();i++) {
			if(!(flag = msglist.getreceived(i))) {
				break;
			}
		}
		if(flag) {
			msg_arr_send.remove(msglist);
		}
		
	}
}
