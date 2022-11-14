package com.tetris.network;
// 서버 클래스와 핸들러 클래스를 가지고 있는 GameServer.java

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import com.tetris.classes.Block;

// 핸들러의 역할은 각 클라이언트의 소켓 정보를 전부 가지고 있으면서 각 클라이언트가 자신의 소켓에 쓰는 정보들을 모두 읽어들여
// 자신이 가지고 있는 모든 클라이언트의 소켓에 해당 정보를 브로드캐스팅 하여 동기화가 안정적으로 이루어지는 역할을 수행한다.
//TODO:--------------------------[ 핸들러 ]--------------------------
class GameHandler extends Thread {	// 쓰레드 클래스
	private static boolean isStartGame;
	private static int maxRank;
	private int rank;
	
	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String ip;
	private String name;
	private int index;
	private int totalAdd = 0;
	
	private ArrayList<GameHandler> handlerList;
	private ArrayList<Integer> indexList;
	
	public GameHandler(Socket socket, ArrayList<GameHandler> handlerList, int index,
			ArrayList<Integer> indexList) {
		super.setName("handler" + index);	// 쓰레드 이름 설정, 만약 다중 스레드라면 이름 설정에 관해 좀 더 고민해봐야할 것
		
		this.index = index;	// 서버였을 경우 이 index 값은 1이다.
		this.indexList = indexList;
		this.socket = socket;
		this.handlerList = handlerList;
		
		try{
			ois = new ObjectInputStream(socket.getInputStream());	// 소켓으로부터 읽어올 수 있는 ois 스트림
			oos = new ObjectOutputStream(socket.getOutputStream());	// 소켓에 출력을 할 수 있는 oos 스트림
			oos.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		try{
			DataShip data = (DataShip)ois.readObject();	// 소켓에 저장되어 있는 DataShip을 ois 스트림을 사용하여 읽어들인다.
			ip = data.getIp();							// 데이터를 받은 후에 ip 정보와 게임이름에 대한 정보를 받는다.
			name = data.getName();
			
			data = (DataShip)ois.readObject();
			
			// 처음 실행될 때는 크게 의미가 없는 아래의 두 실행문
			// 단, 다른 핸들러가 추가적으로 제작되어 유입됐을 때 필요하므로 기술
			printSystemOpenMessage();
			printMessage(ip + ":" + name + "님이 입장하였습니다.");
		} catch(IOException e) { e.printStackTrace();
		} catch(ClassNotFoundException e) { e.printStackTrace();}
		
		
	}//GameHandler


//TODO:--------------------------[ 요청 대기 ]-------------------------
	public void run(){	// 쓰레드 클래스라면 필수로 오버라이드 해야하는 메소드
		DataShip data = null;
		
		// 게임 클라이언트의 소켓에 쓰여 있는 정보를 읽어들여 처리
		while(true){
			try{
				data = (DataShip)ois.readObject();
			} catch (IOException e) { e.printStackTrace(); break;
			} catch (ClassNotFoundException e) { e.printStackTrace(); }

			if (data == null)	// 데이터가 오지 않으면 계속 반복한다.
				continue;
			
			if (data.getCommand() == DataShip.CLOSE_NETWORK) {
				printSystemMessage("<" + index + "P> EXIT");
				printMessage(ip + ":" + name + "님이 퇴장하였습니다");
				closeNetwork();
				break;		// 오로지 퇴장 했을 때(closeNetwork)만 현재 while문을 벗어날 수 있다.
			} else if (data.getCommand() == DataShip.SERVER_EXIT) {
				exitServer();
			} else if (data.getCommand() == DataShip.PRINT_SYSTEM_OPEN_MESSAGE) {
				printSystemOpenMessage();
			} else if (data.getCommand() == DataShip.PRINT_SYSTEM_ADDMEMBER_MESSAGE) {
				printSystemAddMemberMessage();
			} else if (data.getCommand() == DataShip.ADD_BLOCK) {
				addBlock(data);
			} 
			///////////////////////////////////////////
			else if(data.getCommand() == DataShip.ADD_ITEM_BLOCK){
				addItemBlock(data.getNumOfBlock(), data.getTarget(), data.getIndex());
			}			
			else if(data.getCommand() == DataShip.REMOVE_ITEM_LINE){
				removeItemLine(data.getNumOfBlock(), data.getTarget(), data.getIndex());
			}
			else if (data.getCommand() == DataShip.ADD_RENEWAL) {
				addRenewal(data);
			}
			///////////////////////////////////////////
			
			else if (data.getCommand() == DataShip.GAME_START) {
				gameStart(data.getSpeed());
			} else if (data.getCommand() == DataShip.SET_INDEX) {
				setIndex();
			} else if (data.getCommand() == DataShip.GAME_OVER) {
				rank = maxRank--;
				gameover(rank);
			} else if (data.getCommand() == DataShip.PRINT_MESSAGE) {
				printMessage(data.getMsg());
			} else if (data.getCommand() == DataShip.PRINT_SYSTEM_MESSAGE) {
				printSystemMessage(data.getMsg());
			}
			// 추가된 부분
			else if (data.getCommand() == DataShip.RENEWAL_CLINETS_BLOCKLIST) {
				broadcastBlockList(data);
			}
			else if(data.getCommand() == DataShip.TEAM_WIN){
				teamWin(data.getIndex());
			}
			
			
		}//while(true)
		
		// 위에서 DataShip의 내용을 확인하여 퇴장이었을 경우에 한 해 내려온 상태에서 try가 진행된다.
		try {
			handlerList.remove(this);	// 퇴장시 현재의 GameHandler list에서 현재의 객체를 지워준다.
			ois.close();
			oos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	} //run
	
	public void printMessage(String msg) {
		// DataShip(int cmd) 생성자를 호출하여 cmd 값을 PRINT_MESSAGE로 초기화하며 생성
		DataShip data = new DataShip(DataShip.PRINT_MESSAGE);
		data.setMsg(name + "(" + index + "P)>" + msg);
		broadcast(data);
	}


	//응답하기 : 네트워크종료
	public void closeNetwork() {
		DataShip data = new DataShip(DataShip.CLOSE_NETWORK);
		indexList.add(index);
		
		int tmp;
		
		// 미완성 버블 정렬, 단 한 번의 탐색만 진행하면서 오름차순으로 정렬을 시도한다.
		if (indexList.size() > 1) {		// 인덱스 리스트에 항목이 2개 이상일 경우 아래의 for문을 반복한다.
			for (int i = 0; i < indexList.size() - 1; i++) {
				if (indexList.get(i) > indexList.get(i + 1)) {	// 리스트에 순차적으로 숫자가 들어가 있지 않을 때, 즉 전의 항목이 다음 항목보다 인덱스 값이 더 클 때
					tmp = indexList.get(i + 1);					// 다음항목의 값을 tmp에 저장
					indexList.remove(i + 1);					// 다음항목을 지워준 후에
				
					indexList.add(i, new Integer(tmp));			// 전 항목 위치에 저장했던 값을 넣어준다. 이로써 기존에 들어 있던 값들은 한 칸씩 뒤로 옮겨진다.
				}
			}
		}
		send(data);
	}
	
	//응답하기 : 서버종료
	public void exitServer(){
		DataShip data = new DataShip(DataShip.SERVER_EXIT);
		broadcast(data);
	}
	
	//응답하기 : 게임시작
	public void gameStart(int speed) {
		isStartGame = true;
		totalAdd = 0;
		maxRank = handlerList.size();
		DataShip data = new DataShip(DataShip.GAME_START);
		data.setPlay(true);
		data.setSpeed(speed);
		data.setMsg("<Game Start>");
		data.setIndexList(indexList);

		broadcast(data);
		for (int i = 0; i < handlerList.size(); i++) {
			GameHandler handler = handlerList.get(i);
			handler.setRank(0);
		}
	}

	// ip와 port설정을 한 후에 접속을 했을 때: <1P> 113.195.81.185:이름없음 의 형식으로 브로드캐스팅
	public void printSystemOpenMessage() {
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < handlerList.size(); i++) {
			sb.append("<" + handlerList.get(i).index + "P> " + handlerList.get(i).ip + ":" + handlerList.get(i).name);
			if (i < handlerList.size() - 1)
				sb.append("\n");
		}
		
		data.setMsg(sb.toString());
		send(data);
	}

	// 서버로 방을 파고, 해당 방에 다른 클라이언트가 들어 오면, <1P> 113.195.81.184:이름없음 의 형식으로 브로드캐스팅
	public void printSystemAddMemberMessage() {
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		data.setMsg("<" + index + "P> " + ip + ":" + name);
		broadcast(data);
	}

	// 승리 했을 때 메시지를 1P> WIN 형식으로 브로드캐스팅, 매개변수로 몇 번째 플레이어인지가 넘어 옴
	public void printSystemWinMessage(int index) {
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		data.setMsg(index + "P> WIN");
		broadcast(data);
	}

	// 매개변수로 넘어 온 msg를 브로드캐스팅
	public void printSystemMessage(String msg) {
		DataShip data = new DataShip(DataShip.PRINT_SYSTEM_MESSAGE);
		data.setMsg(msg);
		broadcast(data);
	}

	// 응답하기 : 블럭추가, 매개변수로 넘어온 블럭의 갯수만큼 브로드캐스팅
	public void addBlock(DataShip oldData) {
		DataShip data = new DataShip(DataShip.ADD_BLOCK);

		int numOfBlock = oldData.getNumOfBlock();
		int fromIndex = oldData.getIndex();

		data.setNumOfBlock(numOfBlock);
		data.setMsg(index + "P -> ADD:" + numOfBlock);

		totalAdd += numOfBlock;

		// n명 에게 브로드캐스팅

		for (int i = 0; i < handlerList.size(); i++) { // 모든 클라이언트에게
			GameHandler handler = handlerList.get(i); // GameHandler list에서 정보를
			if (handler != null && i != fromIndex - 1) {	// 나를 제외한 핸들러들에게 전송
				try {
					handler.getOOS().writeObject(data);
					handler.getOOS().flush(); // 모든 핸들러의 OOS의 버퍼 내용을 즉시 목적지에 쓴다.
					// handler.getOOS().reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	///////////////////////////////////////////
	public void addItemBlock(int numOfBlock, int target, int index){
		DataShip data = new DataShip(DataShip.ADD_ITEM_BLOCK);
		data.setNumOfBlock(numOfBlock);
		data.setTarget(target);
		data.setIndex(index);
		data.setMsg(index + " attacked " + target + " [" + numOfBlock + "]");
		totalAdd += numOfBlock;
		broadcast(data);
	}
	public void removeItemLine(int numOfBlock, int target, int index){
		DataShip data = new DataShip(DataShip.REMOVE_ITEM_LINE);
		data.setNumOfBlock(numOfBlock);
		data.setTarget(target);
		data.setIndex(index);
		data.setMsg(index + " helped " + target + " [" + numOfBlock + "]");
		totalAdd += numOfBlock;
		broadcast(data);
	}
	
	public void teamWin(int index){
		DataShip data = new DataShip(DataShip.TEAM_WIN);
		data.setIndex(index);
		broadcast(data);
	}
	///////////////////////////////////////////
	
	
	public void sendBlockList(DataShip oldData) {
		DataShip data = new DataShip(DataShip.RENEWAL_CLINETS_BLOCKLIST);		
		
		ArrayList<Block> blockList = oldData.getBlockList();
		int index = oldData.getIndex();
		
		data.setBlockList(blockList);
		data.setIndex(index);

		broadcast(data);
	}
	
	// end of 추가된 부분
	
	
	//응답하기 : 인덱스주기
	public void setIndex(){
		DataShip data = new DataShip(DataShip.SET_INDEX);
		data.setIndex(index);
		send(data);
	}
	
	//응답하기 : 게임오버
	public void gameover(int rank){
		DataShip data = new DataShip(DataShip.GAME_OVER);
		data.setMsg(index+"P -> OVER:"+rank);
		data.setIndex(index);
		data.setPlay(false);
		data.setRank(rank);
		data.setTotalAdd(totalAdd);
		broadcast(data);
		
		if(rank == 2){
			isStartGame = false;
			for(int i=0 ; i<handlerList.size() ;i++){
				GameHandler handler = handlerList.get(i);
				if(handler.getRank() == 0){	// 승리를 알 수 있는 지표: 핸들러의 랭크 값이 0
					handler.win();
				}		
			}
		}
	}
	
	public void win(){
		DataShip data = new DataShip(DataShip.GAME_WIN);
		data.setIndex(index);
		data.setMsg(index+"P -> WIN");
		data.setTotalAdd(totalAdd);
		broadcast(data);
	}
	
	
	
//TODO:--------------------------[ 명령 전송 ]--------------------------[완료]
	// 1명
	private void send(DataShip dataShip){	// 매개변수로 넘어온 dataShip을 oos에 쓴다.
		try{
			oos.writeObject(dataShip);
			oos.flush();
			// resetTest
			// end of resetTest
		}catch(IOException e){e.printStackTrace();}
	}
	
	// n명 에게 브로드캐스팅
	private void broadcast(DataShip dataShip) {
		for(int i=0 ; i<handlerList.size() ; i++){		// 모든 클라이언트에게
			GameHandler handler = handlerList.get(i);	// GameHandler list에서 정보를 가져온다.
			if(handler!=null){
				try{
					handler.getOOS().writeObject(dataShip);	// 모든 핸들러의 OOS에 매개변수로 넘어온 data를 기록한다.
					handler.getOOS().flush();				// 모든 핸들러의 OOS의 버퍼 내용을 즉시 목적지에 쓴다.
//					handler.getOOS().reset();
				}catch(IOException e) { e.printStackTrace(); }
			}
		}

	} // broadcast
	
	
	private void broadcastBlockList(DataShip dataShip) {
		for (int i = 0; i < handlerList.size(); i++) {
			if (i != this.index - 1) {	// 나를 제외한 모든 클라이언트에게 나의 블럭리스트 전송
				GameHandler handler = handlerList.get(i);	// GameHandler list에서 정보를 가져온다.
				if (handler != null) {
					try{
						handler.getOOS().writeObject(dataShip);	// 모든 핸들러의 OOS에 매개변수로 넘어온 data를 기록한다.
						handler.getOOS().flush();				// 모든 핸들러의 OOS의 버퍼 내용을 즉시 목적지에 쓴다.
//						handler.getOOS().reset();
					}catch(IOException e) { e.printStackTrace(); }
				}
			}
//			else {
//				GameHandler handler = handlerList.get(i);
//				try {
//					handler.getOOS().reset();
//				} catch(IOException e) { e.printStackTrace(); }
//			}
		}
	}
	
	private void addRenewal(DataShip dataShip) {
		for (int i = 0; i < handlerList.size(); i++) {
			if (i != dataShip.getIndex() - 1) {	// 애드리뉴얼 호출자를 제외한 모든 클라이언트에게 나의 블럭리스트 전송
				GameHandler handler = handlerList.get(i);	// GameHandler list에서 정보를 가져온다.
				if (handler != null) {
					try{
						handler.getOOS().writeObject(dataShip);	// 모든 핸들러의 OOS에 매개변수로 넘어온 data를 기록한다.
						handler.getOOS().flush();				// 모든 핸들러의 OOS의 버퍼 내용을 즉시 목적지에 쓴다.
//						handler.getOOS().reset();
					}catch(IOException e) { e.printStackTrace(); }
				}
			}
//			else {
//				GameHandler handler = handlerList.get(i);
//				try {
//					handler.getOOS().reset();
//				} catch(IOException e) { e.printStackTrace(); }
//			}
		}
	}
	
	
	public ObjectOutputStream getOOS() { return oos; }
	public int getRank() { return rank; }
	public void setRank(int rank) { this.rank = rank; }
	public boolean isPlay() { return isStartGame; }
} //GameHandler



//TODO:--------------------------[ 서버 ]--------------------------[완료]
public class GameServer implements Runnable {	// 쓰레드로 처리되는 서버 클래스
	private ServerSocket ss; // 서버소켓
	private ArrayList<GameHandler> handlerList = new ArrayList<GameHandler>();
	private ArrayList<Integer> indexList = new ArrayList<Integer>();
	private int index = 1;	// 서버의 인덱스는 1번
	
	public GameServer(int port) {	// 매개변수로 넘어온 포트 넘버로 서버소켓을 연다.
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	} //GameServer()
	
	public void startServer(){
		System.out.println("서버가 작동하고 있습니다.");
		index = 1;	// 서버를 의미
		Thread serverThread = new Thread(this);
		serverThread.setName("server");
		serverThread.start();	// 호출스택 공간이 생기면서 run() 호출	}
	}

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (GameServer.class) {	// 동기화 진행


					// 클라이언트가 ServerSocket의 ip와 port로 접속하면 실행된다.
					Socket socket = ss.accept();	// 서버소켓으로부터 소켓을 하나 더 승인받는다.
					System.out.println("accept is called");
					indexList.add(index);
					GameHandler handler = new GameHandler(socket, handlerList, index, indexList);
					// 소켓이 생기면서 핸들러도 생성, 유저가 한 명 늘어난 것과 동일한 개념으로 해석
					index++;
					
					handlerList.add(handler);	// 핸들러 리스트에 새로 만들어진 핸들러 추가
					handler.start();	// 핸들러 스레드를 실행


				}
			} // while(true)

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}//GameServer