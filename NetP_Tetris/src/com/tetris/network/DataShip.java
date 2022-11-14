package com.tetris.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ArrayList;

import com.tetris.classes.Block;

public class DataShip implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final int CLOSE_NETWORK = 0;	//서버와 연결을 끊음.
	public static final int EXIT = 1;			//프로그램종료
	public static final int SERVER_EXIT = 2;	//서버가 종료될 경우
	public static final int PRINT_SYSTEM_OPEN_MESSAGE = 3;	//리스트 이름 받아오기.
	public static final int PRINT_SYSTEM_ADDMEMBER_MESSAGE = 4;	//그 이후 들어오는 사람.
	public static final int GAME_START = 5;
	public static final int GAME_OVER = 6;
	public static final int ADD_BLOCK = 7;
	public static final int SET_INDEX = 8;
	public static final int PRINT_MESSAGE = 9;
	public static final int PRINT_SYSTEM_MESSAGE=10;
	public static final int GAME_WIN=11;
	public static final int TEAM_WIN=16;
	
	// 추가된 부분
	public static final int RENEWAL_CLINETS_BLOCKLIST = 12;
	// end of 추가된 부분
	
	public static final int ADD_ITEM_BLOCK = 13;
	public static final int REMOVE_ITEM_LINE = 14;
	public static final int ADD_RENEWAL = 15;

	
	private int cmd = -1;
	private String name;
	private String ip;
	private String msg;
	private int numOfBlock;
	private int index;
	private int rank;
	private boolean isPlay;
	private int totalAdd;
	private int speed;
	
	private int target;
	
	// 추가된 부분
	private String dummy;
	private ArrayList<Block> blockList;
	private ArrayList<Integer> indexList;
	// end of 추가된 부분
	
	public DataShip(){}
	public DataShip(int cmd){this.cmd = cmd;}

	public void setCommand(int type){this.cmd = type;};
	public int getCommand(){return cmd;}
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public String getIp() {return ip;}
	public void setIp(String ip) {this.ip = ip;}
	public String getMsg() {return msg;}
	public void setMsg(String msg) {this.msg = msg;}
	public int getNumOfBlock() {return numOfBlock;}
	public void setNumOfBlock(int numOfBlock) {this.numOfBlock = numOfBlock;}
	public int getIndex() {return index;}
	public void setIndex(int index) {this.index = index;}
	public int getRank() {return rank;}
	public void setRank(int rank) {this.rank = rank;}
	public boolean isPlay() {return isPlay;}
	public void setPlay(boolean isPlay) {this.isPlay = isPlay;}
	public int getTotalAdd() {return totalAdd;}
	public void setTotalAdd(int totalAdd) {this.totalAdd = totalAdd;}
	public int getSpeed() {return speed;}
	public void setSpeed(int speed) {this.speed = speed;}
	
	public int getTarget(){return target;}
	public void setTarget(int target){this.target = target;}
	
	
	// 추가된 부분
	public ArrayList<Block> getBlockList() { return blockList; }
	public void setBlockList(ArrayList<Block> blockList) { this.blockList = blockList; }
	public ArrayList<Integer> getIndexList() { return indexList; }
	public void setIndexList(ArrayList<Integer> indexList) { this.indexList = indexList; }
	public String getDummy() { return dummy; }
	public void setDummy(String dummy) { this.dummy = dummy; }
	// end of 추가된 부분
}
