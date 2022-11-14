package com.tetris.window;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class MessageArea extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private TetrisBoard board;
	private JTextArea area = new JTextArea();
	private JTextField txtField = new JTextField();	// 유저가 메시지를 입력할 수 있는 창, 최하단에 위치
	
	public MessageArea(TetrisBoard board, int x, int y, int width, int height) {
		this.board = board;
		this.setLayout(new BorderLayout(2,2));
		this.setBounds(x, y, width, height);	// 생성자의 인자로 주어진 값들로 경계를 설정한다.
		area.setEditable(false);				// 편집할 수 없게끔 설정
		area.setLineWrap(true);					// 할당된 영역을 이탈할 정도로 크기가 주어졌다면, wrap될 수 있게끔 설정
		
		JScrollPane scroll = new JScrollPane(area);	// 수직 스크롤바가 항상 보일 수 있게끔 설정
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		txtField.addActionListener(this);	// JTextField txtField가 이벤트 처리를 할 수 있게끔 설정
		
		// Center값이 아닌 West 등을 입력하게 되면, 메시지 영역이 축소되거나 올바른 크기로 보이지 않는다.
		this.add("Center", scroll);		// 입력한 메시지가 출력되는 MessageArea에 수직 스크롤바를 표시한다.
		
		this.add("South", txtField);	// 최하단에 txtField 추가, 유저는 이 곳에 메시지를 입력할 수 있다.
	}
	
	// 메시지가 오면, 개행문자와 함께 출력하는 메소드
	public void printMessage(String msg){
		area.append(msg+"\n");
		// 맨 아래로 항상 스크롤이 가 있게끔 caretPosition을 메세지 영역의 길이만큼 설정해준다.
		// 설정해놓지 않으면 메시지가 많아져 스크롤을 넘어갈 때 따라가지 않고 그대로 위에 남아 있게 되므로 새로운 메시지를 즉각즉각 확인할 수 없다.
		area.setCaretPosition(area.getText().length());
	}
	
	// 메시지 영역을 공백문자로 클리어 해주는 메소드
	public void clearMessage(){
		area.setText("");
	}
	
	
	@Override	// 컴포넌트에서 이벤트를 받을 수 있게 설정하는 메소드
	public void requestFocus() {
		txtField.requestFocus();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(board.isPlay())	board.requestFocus();	// 플레이 중일 때만 이벤트를 처리할 수 있다.
		
		// client가 있을 때만  실행되는 출력메소드
		if(board.getClient() != null)
			board.getClient().printMessage(txtField.getText().trim());	// txtField에 있는 메시지를 공백 제거 후 출력
		
		// 필드를 공백으로 초기화
		txtField.setText("");
	}
}
