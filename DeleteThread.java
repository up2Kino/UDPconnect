package com.sj0512_final;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DeleteThread extends Thread implements Config {

	private DatagramSocket recvSocket;// ���ؽ��ն˶�Ӧ��Socket

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
			LogTools.INFO(this.getClass(), " ���ڲ��� ��");
			try {
				recvSocket.receive(packet);
				tranmsg(packet.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	// ���յ�����Ϣ���мӹ�����
	private void tranmsg(byte[] buffer) {
		byte[] ID = new byte[4];
		byte[] SID = new byte[4];
		System.arraycopy(buffer, 0, ID, 0, 4);
		System.arraycopy(buffer, 4, SID, 0, 4);
		int id = byteArrayToInt(ID);
		int sid = byteArrayToInt(SID);
		LogTools.INFO(this.getClass(), "ID��" + id + "sid:" + sid + " ������Ϣ���յ�");
		for (MsgList msglist : msg_arr_send) {// ��ͷ��β��������ļ���д��
			System.out.println(msglist.getid());
			if (msglist.getid() == id) {
				msglist.fb_received(sid+1);
				tryRemove(msglist);
				LogTools.INFO(this.getClass(), "ID��" + id + " ��Ϣ���յ�������");
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
	 * ���յ�������Ϣ����Ķ���msglist��recv_feedback�����Ƿ�ȫΪtrue��ȫ���ѱ��յ���
	 * ������ϣ��򽫸�msglist�Ӷ������Ƴ�
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
