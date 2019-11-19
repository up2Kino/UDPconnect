package com.sj0512_final;

import java.util.Date;

public class LogTools {// ��־����
	private static byte debugLevel = 2;

	//static��̬����ʵ��ֱ��ͨ������÷�����������Ҫ��������
	public static void INFO(Class<?> c, String msg) {
		if (debugLevel > 1)
			System.out.println(c.getName() + " " + msg + new Date().toString());
	}

	public static void ERROR(String msg, Exception e) {
		if (debugLevel > 0)
			System.out.println(msg + " erro: " + e.getMessage());
		if (debugLevel == 3) {
			saveFile(msg);
		}
		if (debugLevel == 4) {
			send2Mobile(msg);
		}
	}

	private static void send2Mobile(String msg) {

	}

	private static void saveFile(String s) {

	}

}
