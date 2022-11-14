package com.tetris.window;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.tetris.network.GameClient;
import com.tetris.network.GameServer;

public class Tetris extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private GameServer server;
	private GameClient client;
	
	// 생성된 클라이언트가 없지만 제대로 된 보드 출력을 위해 보드를 우선 생성한다. 추후에  board.setClient()를 통해 제대로 생성된 클라이언트를 할당해준다. 
	private TetrisBoard board = new TetrisBoard(this, client);
	private JMenuItem itemServerStart = new JMenuItem("서버로 접속하기");
	private JMenuItem itemClientStart = new JMenuItem("클라이언트로 접속하기");
	
	private boolean isNetwork;
	private boolean isServer;

	
	// UI 구성 및 상단 메뉴(서버로 접속, 클라이언트로 접속) 이벤트를 처리한다.
	public Tetris() {
		JMenuBar mnBar = new JMenuBar();
		JMenu mnGame = new JMenu("게임하기");	// UI의 좌측 상단에 게임하기 메뉴 버튼이 생성된다.
		
		// 게임 메뉴 버튼에 JMenuItem인 itemServerStart를 mnGame의 항목으로 추가한다.
		mnGame.add(itemServerStart);
		// 게임 메뉴 버튼에 JMenuItem인 itemClientStart를 mnGame의 항목으로 추가한다.
		mnGame.add(itemClientStart);
		// JMenuBar인 mnBar에 하나의 JMenu로서 mnGame을 추가한다.
		mnBar.add(mnGame);
		
		// Tetris 클래스의 JMenuBar를 mnBar로 설정해준다. mnBar에는 mnGame이 포함되어 있다.
		this.setJMenuBar(mnBar);
		
		// 아래의 windowClosing 메소드를 수행할 수 있게끔 DO_NOTHING_ON_CLOSE로 설정해준다.
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.getContentPane().add(board);
		
		this.setResizable(false);	// user에 의해 창의 크기가 변경되지 않도록 고정
		this.pack();				// JFrame을 적절한 크기로 조절해준다.
		
		// 디스플레이의 사이즈를 가져온 후, 시작 위치를 설정해준다.
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		// this.getWidth(), getHeight()를 참가한 후 2로 나누지 않으면, 정가운데를 시작지점으로 하여 우측하단으로 팽창하기 때문에
		// 이 작업으로 JFrame은 화면의 정중앙에 위치할 수 있게 된다.
		this.setLocation((size.width - this.getWidth()) / 2, (size.height - this.getHeight()) / 2);
		this.setVisible(true); // false로 했을 시 보이지 않음
		
		// 클라이언트로 접속하기 버튼과 서버로 접속하기 버튼을 클릭했을 때 반응할 수 있게 해준다.
		// 각 접속하기 버튼을 눌렀을 경우 아래 정의 되어 있는 actionPerformed()가 실행된다.
		itemServerStart.addActionListener(this);
		itemClientStart.addActionListener(this);
		
		// 모든 JFrame을 담고 있는 Window에 Listener를 추가
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {	// Window 닫기 버튼을 눌렀을 때
				if (client != null) {
					
					// clinet가 존재하며 네트워크가 연결되어 있을 때 네트워크를 끊어주는 작업
					if(isNetwork){
						client.closeNetwork(isServer);	// 최하단부에 정의되어 있다.
					}
				} else {
					System.exit(0);
				}
				
			}
			
		});
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// 취소 버튼을 눌렀을 때의 처리가 따로 없으므로 입력을 그대로 진행한다.
		String ip = null;
		int port = 0;
		String nickName = null;
		if(e.getSource() == itemServerStart){	// 서버로 접속하기를 눌렀을 경우
			
			// port번호 입력란이 등장하고, 입력한 값을 sp에 저장한다. 디폴트 값은 "9500"으로 설정되어 있다.
			String sp = JOptionPane.showInputDialog("port번호를 입력해주세요", "9500");
			
			// sp에 저장된 값이 있고, 공백이 아니였을 경우 sp를 Integer변수로 변환하여 port에 저장한다.
			if (sp != null && !sp.equals(""))
				port = Integer.parseInt(sp);

			// 사용자가 입력한 닉네임을 nickName String변수에 저장한다. 디폴트 값은 "이름없음"으로 설정되어 있다.
			nickName = JOptionPane.showInputDialog("닉네임을 입력해주세요","이름없음");
			
			// 저장된 port값이 0이 아니었을 경우, 즉 디폴트 값이 아닌 다른 값이 저장되어 있을 경우 
			if(port != 0) {
				// 서버가 없었다면 해당 port 값에 맞는 GameServer를 생성한다.
				if(server == null) 
					server = new GameServer(port);
				
				// 서버를 시작한다. 서버는 결국 새로 들어온 클라이언트의 소켓을 새로운 핸들러와 연동하고 핸들러 리스트에 추가하는 역할을 한다.
				server.startServer();
				// 서버는 시작하면서 ss.accept()를 통해 클라이언트의 소켓을 할당 하고
				// 해당 소켓 정보와 핸들러 리스트, 인덱스 리스트를 토대로 새로운 핸들러를 생성한 후 리스트에 추가한다.
				// 핸들러를 생성한 후에는 핸들러를 시작한다.
				// 사실상 브로드캐스팅 등은 핸들러가 진행하게 된다. 당연히 클라이언트에게는 핸들러 리스트가 존재하지 않는다.
				
				// String타입의 ip주소를 가져와 ip에 저장한다.
				try { ip = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) { e1.printStackTrace(); }
				
				// ip주소 정보를 제대로 가져왔을 때
				if(ip != null){
					// 위에서 입력 받았던 정보를 토대로 client를 생성한다.
					client = new GameClient(this, ip, port, nickName);
					// client를 실행한다. 각 클라이언트의 소켓 정보는 게임 핸들러가 알고 있다.
					// client의 run()에서는 단지 소켓 ois를 통해 데이터가 읽혔는지 확인하고 메시지를 해석하여
					// 해당 상황에 맞는 data를 다시 oos에 쓰는 것뿐이다.
					/* 	클라이언트가 쓴 정보를 게임 핸들러는 ois로 다시 받아 해석하여 첫 실행 때는 ip와 name 정보를
					 	가져오고 그 이후에는 계속 클라이언트의 소켓에 정보가 쓰였는지를 확인한 후에 데이터가 있다면
					 	그 데이터를 브로드캐스팅 해주는 방식을 취하고 있다. */
					
					/*	start()는 execute()를 실행하며 execute()는 위에 생성자로 넘겨진 ip, port 정보를 토대로 새로운 소켓을 생성, 
					 * 	그 후 자신의 ip와 name 정보를 send()한다. send()는 단지 Socket.oos에 현재 자신의 데이터를 쓰는 행위를 수행한다.
					 *	후에 핸들러가 이 정보를 ois로 받아 해석할 것이다.
					 *	마지막으로 자신을 스레드로 지정하고 스레드를 시작한다. 그 후 클라이언트는 단지 ois에 데이터가 담겨 있는지를 확인하고 해당 작ㄱ업을
					 *	수행하는 것을 반복적으로 진행할 뿐이다. 수행한다는 것 역시 단지 oos에 자신이 해야할 일을 쓰는 것뿐이다.
					 * */
					if(client.start()){	// 실행이 성공적으로 되었다면 다른 버튼에 조작을 가할 필요가 없으므로
						// JMenu mnGame 안에 있는 항목들을 누를 수 없게 설정한다.
						itemServerStart.setEnabled(false);
						itemClientStart.setEnabled(false);
						board.setClient(client);
						board.getBtnStart().setEnabled(true);
						board.startNetworking(ip, port, nickName);
						isNetwork = true;
						isServer = true;
					}
				}
			}
		} else if (e.getSource() == itemClientStart) {	// 클라이언트로 접속하기를 눌렀을 경우
			try {
				// localHost의 ip주소를 String ip에 저장한다.
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			//"211.212.62.252"
			// server의 경우는 별도의 ip입력이 필요 없지만 client의 경우 별도의 ip를 입력해준다.
			// 디폴트로는 위에서 얻은 localHost의 ip주소로 되어 있다.
			ip = JOptionPane.showInputDialog("IP를 입력해주세요.", ip);
			String sp = JOptionPane.showInputDialog("port번호를 입력해주세요", "9500");
			if (sp != null && !sp.equals("")) port = Integer.parseInt(sp);		// 입력한 값이 있을 때만 int으로 변환 후 port에 저장
			nickName = JOptionPane.showInputDialog("닉네임을 입력해주세요", "이름없음");

			if (ip != null) {	// 입력한 ip주소값이 존재할 때만 아래의 작업을 수행
				client = new GameClient(this, ip, port, nickName);	// 입력한 값들을 토대로 client 생성
				if (client.start()) {	// client를 실행함과 동시에 게임 중 돌발상황이 발생할 수 없게끔 몇 버튼을 비활성화 시킨다.
					itemServerStart.setEnabled(false);
					itemClientStart.setEnabled(false);
					board.setClient(client);
					board.startNetworking(ip, port, nickName);
					isNetwork = true;
				}
			}
		}
	}

	public void closeNetwork(){
		isNetwork = false;
		client = null;
		itemServerStart.setEnabled(true);
		itemClientStart.setEnabled(true);
		board.setPlay(false);
		board.setClient(null);
	}

	public JMenuItem getItemServerStart() {return itemServerStart;}
	public JMenuItem getItemClientStart() {return itemClientStart;}
	public TetrisBoard getBoard(){return board;}
	public void gameStart(int speed){board.gameStart(speed);}
	public boolean isNetwork() {return isNetwork;}
	public void setNetwork(boolean isNetwork) {this.isNetwork = isNetwork;}
	public void printSystemMessage(String msg){board.printSystemMessage(msg);}
	public void printMessage(String msg){board.printMessage(msg);}
	public boolean isServer() {return isServer;}
	public void setServer(boolean isServer) {this.isServer = isServer;}

	public void changeSpeed(Integer speed) {board.changeSpeed(speed);}
}
