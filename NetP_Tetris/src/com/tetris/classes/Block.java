package com.tetris.classes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.Serializable;

import com.tetris.window.TetrisBoard;

public class Block implements Serializable {
	private static final long serialVersionUID = 1L;
	private int size = TetrisBoard.BLOCK_SIZE;	// 현재 설계에 의하면 크기는 20
	private int width = size, height = size;
	
	// 추가된 부분
	private int op_size = size * 3 / 4;	// 최초의 설계에 의하면 크기는 15
	private int op_width = op_size, op_height = op_size;
	// end of 추가된 부분
	
	private int gap = 3;
	private int fixGridX, fixGridY;	// 시작 좌표
	private int posGridX, posGridY;
	private Color color;
	private Color ghostColor;	
	private boolean ghost;
	
	// 추가된 부분
	private int index = 0;	// 디폴트는 자신의 인덱스
	// end of 추가된 부분
	public int item = 0;
	public int itemIndex;
	
	private int board1x = 660;
	private int board1y = 133;
	private int board2x = 20;
	private int board2y = 129;
	/**
	 * 
	 * @param fixGridX : 사각형 고정 X 그리드좌표
	 * @param fixGridY : 사각형 고정 Y 그리드좌표
	 * @param color : 사각형 색상
	 */
	public Block(int fixGridX, int fixGridY, Color color, Color ghostColor, int Item, int ItemIndex) {
		this.fixGridX = fixGridX;
		this.fixGridY = fixGridY;
		this.color=color;
		this.ghostColor = ghostColor;
		this.item = Item;
		this.itemIndex = ItemIndex;
	}
	

	/**
	 * 고스트 모드 혹은 일반 모드의 사각형을 그려준다.
	 * @param g
	 */
	public void drawColorBlock(Graphics g) {
		if (ghost)
			g.setColor(ghostColor);
		else
			g.setColor(color);
		switch (index) {
		case 0:
			// 블럭을 감싸는 겉 테두리에 색을 입힌다. 고스트 모드의 경우는 ghostColor가 적용되고 일반 블럭일 경우
			// color가 적용된다.
			g.fillRect((fixGridX + posGridX) * size + board1x,
					(fixGridY + posGridY) * size + board1y, width, height);
			break;
		case 1:
			g.fillRect((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y, op_width, op_height);
			break;
		case 2:
			g.fillRect((fixGridX + posGridX) * op_size + board2x+190,
					(fixGridY + posGridY) * op_size + board2y+360, op_width, op_height);
			break;
		case 3:
			g.fillRect((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y+360, op_width, op_height);
			break;
		}
		// 블럭의 2차 테두리 컬러를 설정해준다. 제일 외곽을 뜻한다.
		g.setColor(Color.BLACK);

		switch (index) {
		case 0:
			// 가장 외곽 테두리를 출력한다.
			g.drawRect((fixGridX + posGridX) * size + board1x,
					(fixGridY + posGridY) * size + board1y, width, height);
			break;
		case 1:
			g.drawRect((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y, op_width, op_height);
			break;
		case 2:
			g.drawRect((fixGridX + posGridX) * op_size + board2x+190,
					(fixGridY + posGridY) * op_size + board2y+360, op_width, op_height);
			break;
		case 3:
			g.drawRect((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y+360, op_width, op_height);
			break;
		}

		switch (index) {
		case 0:
			// 외곽 테두리의 좌측 대각선 출력을 한다.
			g.drawLine((fixGridX + posGridX) * size + board1x,
					(fixGridY + posGridY) * size + board1y,
					(fixGridX + posGridX) * size + width + board1x,
					(fixGridY + posGridY) * size + height + board1y);
			break;
		case 1:
			g.drawLine((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y,
					(fixGridX + posGridX) * op_size + op_width + board2x,
					(fixGridY + posGridY) * op_size + op_height + board2y);
			break;
		case 2:
			g.drawLine((fixGridX + posGridX) * op_size + board2x+190,
					(fixGridY + posGridY) * op_size + board2y+360,
					(fixGridX + posGridX) * op_size + op_width + board2x+190,
					(fixGridY + posGridY) * op_size + op_height + board2y+360);
			break;
		case 3:
			g.drawLine((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y+360,
					(fixGridX + posGridX) * op_size + op_width + board2x,
					(fixGridY + posGridY) * op_size + op_height + board2y+360);
			break;
		}
		switch (index) {
		case 0:
			// 외곽 테두리의 우측 대각선 출력을 한다.
			g.drawLine((fixGridX + posGridX) * size + board1x,
					(fixGridY + posGridY) * size + height + board1y,
					(fixGridX + posGridX) * size + width + board1x,
					(fixGridY + posGridY) * size + board1y);
			break;
		case 1:
			g.drawLine((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + op_height + board2y,
					(fixGridX + posGridX) * op_size + op_width + board2x,
					(fixGridY + posGridY) * op_size + board2y);
			break;
		case 2:
			g.drawLine((fixGridX + posGridX) * op_size + board2x+190,
					(fixGridY + posGridY) * op_size + board2y+360,
					(fixGridX + posGridX) * op_size + op_width + board2x+190,
					(fixGridY + posGridY) * op_size + op_height + board2y+360);
			break;
		case 3:
			g.drawLine((fixGridX + posGridX) * op_size + board2x,
					(fixGridY + posGridY) * op_size + board2y+360,
					(fixGridX + posGridX) * op_size + op_width + board2x,
					(fixGridY + posGridY) * op_size + op_height + board2y+360);
			break;

		}
		if (ghost)
			g.setColor(ghostColor);
		else if(this.color == Color.GRAY)
			g.setColor(Color.GRAY);
		else
			g.setColor(Color.white);			

		switch (index) {
		// 대각선이 교차되는 부분 등을 가려주기 위해 아래와 같이 출력
		case 0:
			g.fillRect((fixGridX + posGridX) * size + gap + board1x,
					(fixGridY + posGridY) * size + gap + board1y, width - gap * 2, height - gap * 2);
			break;
		case 1:			
			g.fillRect((fixGridX + posGridX) * op_size + gap + board2x,
					(fixGridY + posGridY) * op_size + gap + board2y, op_width - gap * 2, op_height - gap * 2);
			break;
		case 2:
			g.fillRect((fixGridX + posGridX) * op_size + gap + board2x+190,
					(fixGridY + posGridY) * op_size + gap + board2y+360, op_width - gap * 2, op_height - gap * 2);
			break;
		case 3:
			g.fillRect((fixGridX + posGridX) * op_size + gap + board2x,
					(fixGridY + posGridY) * op_size + gap + board2y+360, op_width - gap * 2, op_height - gap * 2);
			break;

		}

		// 대각선이 교차되는 부분 등을 가리는 위의 Rect의 테두리를 그려주는 작업
		g.setColor(Color.BLACK);
		switch (index) {
		case 0:
			g.drawRect((fixGridX + posGridX) * size + gap + board1x,
					(fixGridY + posGridY) * size + gap + board1y, width - gap * 2, height - gap * 2);
			break;
		case 1:
			g.drawRect((fixGridX + posGridX) * op_size + gap + board2x,
					(fixGridY + posGridY) * op_size + gap + board2y, op_width - gap * 2, op_height - gap * 2);
			break;
		case 2:
			g.drawRect((fixGridX + posGridX) * op_size + gap + board2x+190, 
					(fixGridY + posGridY) * op_size + gap + board2y+360, op_width - gap * 2, op_height - gap * 2);
			break;
		case 3:
			g.drawRect((fixGridX + posGridX) * op_size + gap + board2x, 
					(fixGridY + posGridY) * op_size + gap + board2y+360, op_width - gap * 2, op_height - gap * 2);
			break;

		}

		g.setColor(new Color(168,199,255));
		if(item==1 && !ghost)
		switch (index) {
		case 0:
			g.fillRect((fixGridX + posGridX) * size + gap + board1x,
					(fixGridY + posGridY) * size + gap + board1y, width - gap * 2, height - gap * 2);
			break;
		case 1:
			
			g.fillRect((fixGridX + posGridX) * op_size + gap + board2x,
					(fixGridY + posGridY) * op_size + gap + board2y, op_width - gap * 2, op_height - gap * 2);
			break;
		case 2:
			g.fillRect((fixGridX + posGridX) * op_size + gap + board2x+190,
					(fixGridY + posGridY) * op_size + gap + board2y+360, op_width - gap * 2, op_height - gap * 2);
			break;
		case 3:
			g.fillRect((fixGridX + posGridX) * op_size + gap + board2x,
					(fixGridY + posGridY) * op_size + gap + board2y+360, op_width - gap * 2, op_height - gap * 2);
			break;
		}
	}
	
	/**
	 * 현재 블럭의 절대좌표를 보여준다.
	 * @return 현재블럭의 X절대좌표
	 */
	public int getX() {
		return posGridX + fixGridX;
	}	
	
	/**
	 * 현재 블럭의 절대좌표를 보여준다.
	 * @return 현재블럭의 Y절대좌표
	 */
	public int getY() {
		return posGridY + fixGridY;
	}
	
	/**
	 * Getter Setter
	 */
	public int getPosGridX(){return this.posGridX;}
	public int getPosGridY(){return this.posGridY;}
	public int getFixGridX(){return this.fixGridX;}
	public int getFixGridY(){return this.fixGridY;}

	public void setPosGridX(int posGridX) {this.posGridX = posGridX;}
	public void setPosGridY(int posGridY) {this.posGridY = posGridY;}
	public void setPosGridXY(int posGridX, int posGridY){this.posGridX = posGridX;this.posGridY = posGridY;}
	public void setFixGridX(int fixGridX) {this.fixGridX = fixGridX;}
	public void setFixGridY(int fixGridY) {this.fixGridY = fixGridY;}
	public void setFixGridXY(int fixGridX, int fixGridY){this.fixGridX = fixGridX;this.fixGridY = fixGridY;}
	public void setGhostView(boolean b){this.ghost = b;}
	
	// 추가된 부분
	public int getIndex() { return index; }
	public void setIndex(int index) { this.index = index; }
	// end of 추가된 부분
	
}
