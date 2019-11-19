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

	public SimpleDateFormat dFormat;// ��ʾʱ���õ��ĸ�ʽ
	private DatagramSocket recvSocket;// ���ؽ��ն˶�Ӧ��Socket
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
		try {// 2.���������Ϣ��UDP�˿�
			recvSocket = new DatagramSocket(localAddr_recv_msg);
			dSender_feedback = new DatagramSocket(localAddr_send_feedback);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void initUI() {
		JFrame frame = new JFrame("ͨѶ��");
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(3);
		frame.setLayout(new BorderLayout());
		JTextArea recvArea = new JTextArea(10, 10);// ������Ϣ
		recvArea.setEditable(false);

		JTextField sendArea = new JTextField();// ������Ϣ
		sendArea.setBackground(new Color(240, 240, 240));

		JPanel Recvpanel = new JPanel();
		recvArea.setPreferredSize(new Dimension(500, 300));
		recvArea.setBackground(new Color(245, 245, 245));
		recvArea.setAlignmentX(1);// ���ö��뷽ʽΪ�Ҷ���
		recvArea.setLineWrap(true);// �����Զ����й���
		recvArea.setWrapStyleWord(true);// ������в����ֹ���
		Recvpanel.setLayout(new BorderLayout());
		Recvpanel.add(recvArea, BorderLayout.CENTER);

		JPanel Dividepanel = new JPanel();// �ָ���
		Dividepanel.setPreferredSize(new Dimension(500, 20));
		Dividepanel.setBackground(new Color(80, 128, 180));

		JButton jSButton = new JButton("Send");// ������Ϣ�İ�ť
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
		recvRun(recvArea);// ���������Ϣ���߳�
	}


	private void recvRun(JTextArea recvArea) {
		while (true) {

			byte[] buffer = new byte[Packsize_data + datasize];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			LogTools.INFO(this.getClass(), " �ȴ��������� ");			
			try {
				recvSocket.receive(packet);
				LogTools.INFO(this.getClass(), " �ѽ�����Ϣ ");
				tranmsg(packet.getData(), recvArea);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * �˷�������Խ��ܵ�����Ϣ���мӹ� �˴����յ������ݰ���Ϊ���ư������ݰ������С�����ᳬ��100byte
	 * @param buffer   ���������byte[]
	 * @param recvArea ���ڴ�������Ϣ����ʾ�ڶԻ����ϣ��ִ����иĶ�
	 */
	private void tranmsg(byte[] buffer, JTextArea recvArea) {

		if (buffer[Packsize_head - 1] == 0) {// ���ݱ�־λ�жϣ���Ϊ���ư�
			byte[] ID = new byte[4];
//			byte[] content = new byte[datasize];
			byte[] nidedspace_byte = new byte[4];

			System.arraycopy(buffer, 0, ID, 0, 4);
			System.arraycopy(buffer, 4, nidedspace_byte, 0, 4);
//			System.arraycopy(buffer, Packsize_head, content, 0, 4);

			int id = byteArrayToInt(ID);
			int nidedspace = byteArrayToInt(nidedspace_byte);
			// ���ȷ���ڽ��ն������Ƿ����Դ�id������msgList�����û�вŴ���
			// ȷ���ڷ�������ʧ�����ͷ����·��Ϳ��ư�ʱ���ظ�����msgList
			int flag = 1;
			for (int i = 0; i < msg_arr_recv.size(); i++) {
				if (msg_arr_recv.get(i).getid() == id) {
					flag = 0;
				}
			}
			if (flag == 1) {
				msg_arr_recv.add(new MsgList(id, nidedspace));
				int pos = msg_arr_recv.size() - 1;// ���´�msglist��λ���Ա��ں�������
				byte[] blank = new byte[datasize];
				for (int i = 0; i < nidedspace; i++) {// ��msglist����ǰ�������������������ݰ�����ռλ�������ڽ������ݰ�ʱֱ�����
					msg_arr_recv.get(pos).add(blank);
				}
			}
			feedback(ID,-1);// �����Ƿ�ɹ�����msglist�������ͷ�����Ϣ

		} else if (buffer[Packsize_head - 1] == 1) {// ��Ϊ���ݰ�

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
			feedback(ID, sid);// �����Ƿ�ɹ����룬��������Ч�����յ�
		}
	}

	public byte[] intxTo4Byte(int x) {// ��һ��int������ת��Ϊ4��byte��byte����
		byte[] result = new byte[4];
		result[0] = (byte) ((x >> 24) & 0xFF);// ��Ӱ��x����
		result[1] = (byte) ((x >> 16) & 0xFF);
		result[2] = (byte) ((x >> 8) & 0xFF);
		result[3] = (byte) (x & 0xFF);
		return result;
	}

	/**
	 * һʵ����Ҫ�����㣬��Ӧ��ת��Ϊint�ٽ���λ���㣬ֱ�Ӷ�byte������λֻ�ܽ���������
	 */
	public static int byteArrayToInt(byte[] src) {
		int value;
		value = (int) (((src[0] & 0xFF) << 24) | ((src[1] & 0xFF) << 16) | ((src[2] & 0xFF) << 8) | (src[3] & 0xFF));
		return value;
	}

	/**
	 * �˷��������ַ������ǿ��ư��������ݰ� ��������ݰ���ӦΪID+sid���Ա㷢�Ͷ��н���ɾ��
	 * �ݶ���������ȡ�뷢����Ϣʱ���Ƶķ�װ��ʽ����ID(4)+flag(1)+sid(4)�����ư�����Ҫsid�������ݽ���5byte�����������flag�����5λ��
	 * ����ȷ�����������������ݰ��ķ���������Ϊһ�֣���С��Ϊ�˸��ֽڣ�����ΪID+sid/flag
	 * sid/flag=-1ʱ��Ϊ���ư�
	 * sid/flag>=0��Ϊ���ݰ�������ֵΪsid
	 * �ڽ��շ�����ʶ����������
	 */
	
	public void feedback(byte[] ID, int sid) {//������Ϣ������
		byte[] fb = new byte[8];
		byte[] SID = intxTo4Byte(sid);
		System.arraycopy(ID, 0, fb, 0, 4);
		System.arraycopy(SID, 0, fb, 4, 4);
		int id = byteArrayToInt(ID);
		DatagramPacket packet = new DatagramPacket(fb, fb.length, destAddr_feedback);
		try {
			dSender_feedback.send(packet);
			LogTools.INFO(this.getClass(), "���ư�-ID��" + id + " ���յ����䷴����Ϣ�ѷ��ͣ�");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
