package com.tetris.window;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class SystemMessageArea extends JScrollPane {
	private static final long serialVersionUID = 1L;
	private static JTextArea area = new JTextArea();	// text를 출력할 수 있는 JTextArea area
	
	// 생성자
	public SystemMessageArea(int x, int y, int width, int height) {
		super(area);	// JScrollPane(JTextArea area)
		this.setBounds(x, y, width, height);	// 인자로 넘어온 값들을 토대로 경계 생성
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);	// 수직 스크롤이 항상 보이게끔 설정
		area.setEditable(false);	// 편집할 수 없게 설정
		area.setLineWrap(true);		// 수직 스크롤바가 너무 길 경우 감싸줄 수 있게 설정
	}
	
	public void printMessage(String msg){
		if(msg!=null && !msg.equals("")){
			area.append(msg+"\n");	// 메세지가 있다면, 개행문자와 함께 JTextArea area에 그 메시지를 출력한다.
			area.setCaretPosition(area.getText().length());
		}
	}
	
	// JTextArea area 영역을 공백으로 초기화
	public void clearMessage(){
		area.setText("");
	}
}
