package com.tetris.controller;

import com.tetris.classes.Block;
import com.tetris.classes.TetrisBlock;

public class TetrisController {

	private int rotation_index;
	private TetrisBlock tetrisBlock;
	private Block[][] map;
	
	private int maxX, maxY;
	
	/**
	 * 테트리스 블럭을 조정하는 컨트롤러이다.
	 * 
	 * @param block : 움직일 테트리스 블럭
	 * @param minX : 블럭이 움직일 최소 GridX좌표
	 * @param minY : 블럭이 움직일 최소 GridY좌표
	 * @param maxX : 블럭이 움직일 최대 GridX좌표
	 * @param maxY : 블럭이 움직일 최대 GridY좌표
	 */
	public TetrisController(TetrisBlock tetrisBlock, int maxX, int maxY, Block[][] map) {
		this.tetrisBlock = tetrisBlock;
		
		this.maxX = maxX;
		this.maxY = maxY;
		
		this.map = map;
		this.rotation_index = tetrisBlock.getRotationIndex();
		
	}
	
	
	/**
	 * 움직일 테트리스 블럭을 넘겨준다.
	 * @param block 움직일 테트리스 블럭
	 */
	public void setBlock(TetrisBlock tetrisBlock){
		// 블럭 정보와 회전 정보를 받아온다.
		this.tetrisBlock = tetrisBlock;
		this.rotation_index = tetrisBlock.getRotationIndex();
	}
	
	
	/**
	 * 블럭의 좌표를 출력한다.
	 */
	public void showIndex(){
		for(Block blocks : tetrisBlock.getBlock()){
			if (blocks != null)
				System.out.print("(" + blocks.getX() + "," + blocks.getY() + ")");
		}
		System.out.println();
	}
	
	
	/**
	 * 블럭의 좌표 범위안에 있는지 확인한다.
	 * 
	 * @param maxX : 블럭이 움직일 수 있는 GridX좌표 개수
	 * @param maxY : 블럭이 움직일 수 있는 GridY좌표 개수
	 * @return
	 */
	public boolean checkIndex(int maxX, int maxY) {

		for (Block blocks : tetrisBlock.getBlock()) {

			if (blocks == null || blocks.getY() < 0)
				continue;

			// 최소 허용 범위(>= 0)와 최대 허용 범위(maxX, maxY)를 위반했을 때 false 반환 
			if (blocks.getX() < 0 || blocks.getY() < 0 || blocks.getX() > maxX || blocks.getY() > maxY)
				return false;
			else {	// 허용 범위 안에는 있지만, 이미 그 자리에 블럭이 있을 때 또한  false 반환
				if (map[blocks.getY()][blocks.getX()] != null)
					return false;
			}
		}
		// 위의 검사절차를 다 통과했다면 true 반환
		return true;
	}
	
	/**
	 * 왼쪽으로 이동
	 * default 1칸
	 */
	public void moveLeft() {
		moveLeft(1);
	}

	public void moveLeft(int x) {
		// 이동
		tetrisBlock.moveLeft(x);

		// 체크, 범위를 벗어났다면 원상복귀
		if (!checkIndex(maxX, maxY)) {
			tetrisBlock.moveLeft(-x);
		}
	}
	
	/**
	 * 오른쪽으로 이동
	 * default 1칸
	 */
	public void moveRight(){moveRight(1);}
	public void moveRight(int x){
		// 이동
		tetrisBlock.moveRight(x);
		
				
		// 체크, 범위를 벗어났다면 원상복귀
		if (!checkIndex(maxX, maxY)) {
			tetrisBlock.moveRight(-x);
		}
	}
	
	
	/**
	 * 아래로 이동
	 * default 1칸
	 */
	public boolean moveDown() {
		return moveDown(1);
	}

	public boolean moveDown(int y){
		
		boolean moved = true;
		
		// 이동
		tetrisBlock.moveDown(y);
		
		// 체크, 범위를 벗어났다면 원상복귀
		if (!checkIndex(maxX, maxY)) {
			tetrisBlock.moveDown(-y);
			moved = false;
		}
		// 범위를 벗어났다면 moved에 false값이 담겨 반환, false가 반환
		return moved;
	}
	
	/**
	 * 
	 * @param startY 현재 블럭의 위치
	 * @param moved 재귀함수에 필요한 인자로, 무조건 true로 한다.
	 * @return	moveQuickDown를 다시 호출한다.
	 */
	public boolean moveQuickDown(int startY, boolean moved){
		
		// 이동
		tetrisBlock.moveDown(1);
		// 체크, 범위를 벗어났다면 원상복귀
		if (!checkIndex(maxX, maxY)) {
			tetrisBlock.moveDown(-1);
			if(moved) return false;
		}
		return moveQuickDown(startY+1, true);
	}
	
	/**
	 * 테트리스 블럭을 회전시킨다.
	 * @param rotation_direction : 회전방향
	 * TetrisBlock.ROTATION_LEFT(시계방향), TetrisBlock.ROTATION_RIGHT(반시계방향)
	 */
	public void nextRotation(int rotation_direction){
		if(rotation_direction == TetrisBlock.ROTATION_LEFT) 
			this.nextRotationLeft();
		else if(rotation_direction == TetrisBlock.ROTATION_RIGHT) 
			this.nextRotationRight();
	}
	
	
	/**
	 * 테트리스 블럭을 회전시킨다. (시계방향)
	 * 만약 회전시 범위를 벗어나면, 회전을 하지 않는다.
	 */
	public void nextRotationLeft(){
		//회전
		rotation_index++;
		if (rotation_index == TetrisBlock.ROTATION_270 + 1)
			rotation_index = TetrisBlock.ROTATION_0;
		tetrisBlock.rotation(rotation_index);
		
		//체크, 범위를 벗어났다면 원상복귀
		if(!checkIndex(maxX,maxY)) {
			rotation_index--;
			if (rotation_index == TetrisBlock.ROTATION_0 - 1)
				rotation_index = TetrisBlock.ROTATION_270;
			tetrisBlock.rotation(rotation_index);
		}
	}
	
	
	/**
	 * 테트리스 블럭을 회전시킨다. (반시계방향)
	 * 만약 회전시 범위를 벗어나면, 회전을 하지 않는다.
	 */
	public void nextRotationRight(){
		//회전
		rotation_index--;
		if(rotation_index == TetrisBlock.ROTATION_0-1) rotation_index = TetrisBlock.ROTATION_270;
		tetrisBlock.rotation(rotation_index);
		
		//체크, 범위를 벗어났다면 원상복귀
		if(!checkIndex(maxX,maxY)) {
			rotation_index++;
			if(rotation_index == TetrisBlock.ROTATION_270+1) rotation_index = TetrisBlock.ROTATION_0;
			tetrisBlock.rotation(rotation_index);
		}
	}
}
