package com.tetris.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ArrayList;

import com.tetris.classes.Block;
import com.tetris.window.Tetris;

//---------------------[ 클라이언트 ]---------------------
public class GameClient implements Runnable {	// 쓰레드로 동작하는 클라이언트
	private Tetris tetris;
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	//서버 IP
	private String ip;
	private int port;
	private String name;
	private int index;
	private boolean isPlay;
	
	private int team = 0;
	
	public int getTeam(){
		return this.team;
	}
	
	public void setTeam(int team){
		this.team = team;
	}
	
	public int getIndex(){
		return this.index;
	}
	
	// 추가된 부분
	private ArrayList<Integer> clientIndexList;
	// end of 추가된 부분
	
	//생성자
	public GameClient(Tetris tetris,String ip, int port, String name){
		this.tetris = tetris;
		this.ip = ip;
		this.port = port;
		this.name = name;
		clientIndexList = new ArrayList<Integer>();

	}//GameClient()

	public boolean start(){
		return this.execute();	
	}

	//소켓 & IO 처리
	public boolean execute(){
		try{
			socket = new Socket(ip,port);							// 새로운 소켓을 생성
			ip = InetAddress.getLocalHost().getHostAddress();		// localHost의 ip주소 정보를 가져온다.
			oos = new ObjectOutputStream(socket.getOutputStream());	// 소켓에 객체를 쓸 수 있는 스트림
			ois = new ObjectInputStream(socket.getInputStream());	// 소켓으로부터 객체를 읽을 수 있는 스트림
			System.out.println("클라이언트가 실행 중입니다.");
		}catch(UnknownHostException e){	// 예외 처리 (ip주소를 얻어오는 과정에서 처리)
			e.printStackTrace();
			return false;
		}catch(IOException e){			// 예외 처리
			e.printStackTrace();
			return false;
		}

		tetris.getBoard().clearMessage();	// 메시지 영역과 시스템 메시지 영역을 깨끗히 한다.
		
		// 이름, ip 정보 보내기
		DataShip data = new DataShip();
		data.setIp(ip);
		data.setName(name);
		send(data);
		
		//리스트받아오기
		printSystemMessage(DataShip.PRINT_SYSTEM_OPEN_MESSAGE);
		//리스트에 추가하기
		printSystemMessage(DataShip.PRINT_SYSTEM_ADDMEMBER_MESSAGE);
		//인덱스받아오기
		setIndex();
		//스레드
		Thread clientThread = new Thread(this);
		clientThread.setName("client");
		clientThread.start();	// 새로운 스레드를 시작함과 동시에 run() 호출
		
		return true;
	}//execute()

	
	//Run : 서버의 명령을 기다림.
	public void run(){
		DataShip data = null;
		while(true){
			try{
				data = (DataShip)ois.readObject(); 
			}catch(IOException e){e.printStackTrace();break;
			}catch(ClassNotFoundException e){e.printStackTrace();}


			// 서버로부터 DataShip Object를 받아옴.
			if (data == null)
				continue;
			
			if (data.getCommand() == DataShip.CLOSE_NETWORK) {
				reCloseNetwork();
				break;
			} else if (data.getCommand() == DataShip.SERVER_EXIT) {
				closeNetwork(false);
			} else if (data.getCommand() == DataShip.GAME_START) {
				reGameStart(data.isPlay(), data.getMsg(), data.getSpeed(), data.getIndexList());
			} else if (data.getCommand() == DataShip.ADD_BLOCK) {
				if (isPlay)
					reAddBlock(data.getMsg(), data.getNumOfBlock(), data.getIndex());
			} 
			///////////////////////////////////////////
			else if(data.getCommand() == DataShip.ADD_ITEM_BLOCK){
				if(isPlay)
					reAddItemBlock(data.getMsg(), data.getNumOfBlock(), data.getTarget(), data.getIndex());
			}
			else if(data.getCommand() == DataShip.REMOVE_ITEM_LINE){
				if(isPlay)
					reRemoveItemLine(data.getMsg(), data.getNumOfBlock(), data.getTarget(), data.getIndex());
			}
			///////////////////////////////////////////
			else if (data.getCommand() == DataShip.SET_INDEX) {
				reSetIndex(data.getIndex());
			} else if (data.getCommand() == DataShip.GAME_OVER) {
				if (index == data.getIndex())
					isPlay = data.isPlay();
				reGameover(data.getMsg(), data.getTotalAdd());
			} else if (data.getCommand() == DataShip.PRINT_MESSAGE) {
				rePrintMessage(data.getMsg());
			} else if (data.getCommand() == DataShip.PRINT_SYSTEM_MESSAGE) {
				rePrintSystemMessage(data.getMsg());
			} else if (data.getCommand() == DataShip.GAME_WIN) {
				rePrintSystemMessage(data.getMsg() + "\nTOTAL ADD : " + data.getTotalAdd());
				if(index == data.getIndex()){
					int team_index = this.getTeam();
					if(team_index==0)tetris.getBoard().winCallBack();
					DataShip data2 = new DataShip(DataShip.TEAM_WIN);
					data2.setIndex(team_index);
					send(data2);
				}
				else
					tetris.getBoard().loseCallBack();
				tetris.getBoard().setPlay(false);
			} 
			// 추가된 부분
			else if (data.getCommand() == DataShip.RENEWAL_CLINETS_BLOCKLIST) {
				reSendBlockList(data);
			}
			else if (data.getCommand() == DataShip.ADD_RENEWAL) {
				reAddRenewal(data);
			}
			else if(data.getCommand() == DataShip.TEAM_WIN){
				if(getTeam()!=0 && getTeam() == data.getIndex()){
					tetris.getBoard().winCallBack();
				}
			}
			

		}//while(true)
		
	}//run()

	// 서버에게 요청함
	public void send(DataShip data){
		try{
			oos.writeObject(data);	// oos에 data 정보를 쓴다. oos는 소켓으로부터 얻는다. 즉 소켓에 쓰는 과정
			oos.flush();
			// resetTest
			oos.reset();
			// end of resetTest
		}catch(IOException e){
			e.printStackTrace();
		}
	} //sendData()
	
	//요청하기 : 연결끊기
	public void closeNetwork(boolean isServer){
		DataShip data = new DataShip(DataShip.CLOSE_NETWORK);
		if(isServer) data.setCommand(DataShip.SERVER_EXIT);
		send(data);
	}
	//실행하기 : 연결끊기
	public void reCloseNetwork(){

		tetris.closeNetwork();
		try {
			ois.close();
			oos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//요청하기 : 게임시작
	public void gameStart(int speed){
		DataShip data = new DataShip(DataShip.GAME_START);
		data.setSpeed(speed);
		send(data);
	}
	//실행하기 : 게임시작
	public void reGameStart(boolean isPlay, String msg, int speed, ArrayList<Integer> indexList){
		// 상대인덱스를 사용하기 위해 고객리스트에 다른 고객들 등록
		for (int i = 0; i < indexList.size(); i++) {
			int clientIndex = indexList.get(i);
			System.out.println(clientIndex + " ");
			if (clientIndex != this.index)	// 내가 아닐 경우에만 고객리스트에 순차적으로 추가
				clientIndexList.add(clientIndex);
		}
		
		this.isPlay = isPlay;
		tetris.gameStart(speed);
		rePrintSystemMessage(msg);
	}
	//요청하기 : 메시지
	public void printSystemMessage(int cmd){
		DataShip data = new DataShip(cmd);
		send(data);
	}
	//실행하기 : 메시지
	public void rePrintSystemMessage(String msg){
		tetris.printSystemMessage(msg);
	}
	//요청하기: 블럭 추가
	public void addBlock(int numOfBlock){
		DataShip data = new DataShip(DataShip.ADD_BLOCK);
		data.setNumOfBlock(numOfBlock);
		data.setIndex(index);
		send(data);
	}
	// 실행하기: 블럭 추가
	public void reAddBlock(String msg, int numOfBlock, int index){
		// 나를 제외한 다른 클라이언트에게 공격을 보내는 개념이므로 난 제외
		tetris.getBoard().addBlockLine(numOfBlock);

		 rePrintSystemMessage(msg);	// 주석처리 고민 중
	}
	
	////////////////////////////////////////////
	
	public void addItemBlock(int numOfBlock, int target){
		DataShip data = new DataShip(DataShip.ADD_ITEM_BLOCK);
		data.setNumOfBlock(numOfBlock);
		data.setTarget(target);
		data.setIndex(this.index);
		send(data);
	}
	
	public void reAddItemBlock(String msg, int numOfBlock, int target, int index){
		if(target == this.index)
			tetris.getBoard().addBlockLine(numOfBlock);
		rePrintSystemMessage(msg);
	}
	
	public void removeItemLine(int numOfBlock, int target){
		DataShip data = new DataShip(DataShip.REMOVE_ITEM_LINE);
		data.setNumOfBlock(numOfBlock);
		data.setTarget(target);
		data.setIndex(this.index);
		send(data);
	}
	
	public void reRemoveItemLine(String msg, int numOfBlock, int target, int index){
		if(target == this.index)
			tetris.getBoard().ItemRemove(numOfBlock);
		rePrintSystemMessage(msg);
	}
	
	public void addRenewal(ArrayList<Block> blockList) {

		DataShip data = new DataShip(DataShip.ADD_RENEWAL);

		data.setBlockList(blockList);
		data.setIndex(index);
		
		send(data);
	}

	public void reAddRenewal(DataShip data) {
		ArrayList<Block> blockList = data.getBlockList();

		int fromIndex = data.getIndex();

		int relativeIndex = -1; // 클라이언트의 상대적인 인덱스를 설정하기 위한 지역변수

		// 검사하여 클라이언트리스트에 추가하는 기능 필요
		for (int i = 0; i < clientIndexList.size(); i++) {
			if (clientIndexList.get(i) == fromIndex) {
				relativeIndex = i;
				break;
			}
		}

		if (relativeIndex != -1) { // 이미 존재하는 고객이었을 경우
			// 상대 인덱스에 맞게 설정
			// System.out.println("alreadyExistIndex : " + relativeIndex);
			// 모든 블럭의 상대 인덱스 값을 설정
			Iterator<Block> blockItor = blockList.iterator();
			while (blockItor.hasNext()) {
				blockItor.next().setIndex(relativeIndex);
			}
			// 모든 블럭의 상대 인덱스 값 설정 완료

			switch (relativeIndex) {
			case 1:
				tetris.getBoard().setOpBlockList1(blockList);
				tetris.getBoard().setOpIndex1(data.getIndex());
				break;
			case 2:
				tetris.getBoard().setOpBlockList2(blockList);
				tetris.getBoard().setOpIndex2(data.getIndex());
				break;
			case 3:
				tetris.getBoard().setOpBlockList3(blockList);
				tetris.getBoard().setOpIndex3(data.getIndex());

				break;
			case 4:
				tetris.getBoard().setOpBlockList4(blockList);
				tetris.getBoard().setOpIndex4(data.getIndex());

				break;
			}
		}
	}
	////////////////////////////////////////////
	
	
	// 추가된 부분
	// 서버에게 자신의 블럭리스트 정보를 전송한다.
	public void sendBlockList(ArrayList<Block> blockList) {
		DataShip data = new DataShip(DataShip.RENEWAL_CLINETS_BLOCKLIST);
		data.setBlockList(blockList);
		data.setIndex(index);
		send(data);
	}
	
	// 서버로부터 브로드캐스팅 된 data 정보를 받아온다.
	public void reSendBlockList(DataShip data) {
		ArrayList<Block> blockList = data.getBlockList();
		
		int fromIndex = data.getIndex();

		if(fromIndex != this.index)	// 내 것이 아닐 때만 Board에 별도로 설정
		{
			int relativeIndex = -1;	// 클라이언트의 상대적인 인덱스를 설정하기 위한 지역변수

			// 검사하여 클라이언트리스트에 추가하는 기능 필요
			for(int i = 0; i < clientIndexList.size(); i++) {
				if(clientIndexList.get(i) == fromIndex) {
					relativeIndex = i;
					break;
				}
			}
			
			if (relativeIndex != -1) {	// 이미 존재하는 고객이었을 경우
				// 상대 인덱스에 맞게 설정
//				System.out.println("alreadyExistIndex : " + relativeIndex);
				// 모든 블럭의 상대 인덱스 값을 설정
				Iterator<Block> blockItor = blockList.iterator();
				while(blockItor.hasNext()) {
					blockItor.next().setIndex(relativeIndex);
				}
				// 모든 블럭의 상대 인덱스 값 설정 완료
				
				switch(relativeIndex) {
				case 1:
					tetris.getBoard().setOpBlockList1(blockList);
					tetris.getBoard().setOpIndex1(data.getIndex());
					break;
				case 2:
					tetris.getBoard().setOpBlockList2(blockList);
					tetris.getBoard().setOpIndex2(data.getIndex());
					break;
				case 3:
					tetris.getBoard().setOpBlockList3(blockList);
					tetris.getBoard().setOpIndex3(data.getIndex());

					break;
				case 4:
					tetris.getBoard().setOpBlockList4(blockList);
					tetris.getBoard().setOpIndex4(data.getIndex());

					break;
				}
			}
		}
		/*	
			자신의 테트리스 보드에 해당 블럭리스트 정보를 보드에 추가한다.
		 *	최초의 블럭리스트인지 아닌지의 여부를 구분하여 보드에 정보를 보관한다. 
		 *	블럭에 그리는 정보가 있으므로 인덱스에 따라 다른 출력 방법이 필요
		 * */
	}
	// end of (추가된 부분)
	
	// 요청하기: 인덱스 세팅
	public void setIndex(){
		DataShip data = new DataShip(DataShip.SET_INDEX);
		send(data);
	}
	// 실행하기: 인덱스 세팅
	public void reSetIndex(int index){
		this.index = index;
		clientIndexList.add(index);	// 자신은 0번째 클라이언트로 등록
	}
	
	//요청하기 : 게임종료
	public void gameover(){
		DataShip data = new DataShip(DataShip.GAME_OVER);
		send(data);
	}
	//실행하기 : 게임종료
	public void reGameover(String msg, int totalAdd){
		tetris.printSystemMessage(msg);
		tetris.printSystemMessage("TOTAL ADD : "+totalAdd);
		
	}
	
	public void printMessage(String msg){
		DataShip data = new DataShip(DataShip.PRINT_MESSAGE);
		data.setMsg(msg);
		send(data);
	}
	public void rePrintMessage(String msg){
		tetris.printMessage(msg);
	}
	
	public void reChangSpeed(Integer speed) {
		tetris.changeSpeed(speed);
	}
}
