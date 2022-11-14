package com.tetris.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tetris.classes.Block;
import com.tetris.classes.TetrisBlock;
import com.tetris.controller.TetrisController;
import com.tetris.network.GameClient;
import com.tetris.shape.CenterUp;
import com.tetris.shape.LeftTwoUp;
import com.tetris.shape.LeftUp;
import com.tetris.shape.Line;
import com.tetris.shape.Nemo;
import com.tetris.shape.RightTwoUp;
import com.tetris.shape.RightUp;

public class TetrisBoard extends JPanel implements Runnable, KeyListener, MouseListener, ActionListener, Serializable {
	private static final long serialVersionUID = 1L;

	private Tetris tetris;
	private GameClient client;
	private boolean sendBit;

	// 기존의 좌표 기준값들
	public static final int BLOCK_SIZE = 20;
	public static final int BOARD_X = 120;
	public static final int BOARD_Y = 50;
	private int minX = 1, minY = 0, maxX = 10, maxY = 21, down = 50, up = 0;

	private final int MESSAGE_X = 2;
	private final int MESSAGE_WIDTH = BLOCK_SIZE * (7 + minX); // 20 * 8 = 160
	private final int MESSAGE_HEIGHT = BLOCK_SIZE * (6 + minY); // 20 * 6 = 120

	private final int PANEL_WIDTH = 80 * BLOCK_SIZE + MESSAGE_WIDTH + BOARD_X; // (10/maxX
																				// *
																				// 20)
																				// +
																				// (20
																				// *
																				// 8)
																				// +
																				// 120
																				// =
																				// 480
	// 변경했을 때의 사이즈는 80 * 20 + 160 = 1760

	private final int PANEL_HEIGHT = 40 * BLOCK_SIZE + MESSAGE_HEIGHT + BOARD_Y; // (21/maxY
																					// *
																					// 20)
																					// +
																					// (20
																					// *
																					// 6)
																					// +
																					// 50
																					// =
																					// 590
	// 변경했을 때의 사이즈는 40 * 20 + 120 + 50 = 970
	// end of 기존의 좌표 기준값들

	// 새로운(추가된) 좌표 기준값들
	// public static final int NEW_BOARD_X
	public static final int NEW_BOARD_X = BOARD_X + BLOCK_SIZE * 35;
	public static final int NEW_BOARD_Y = BOARD_Y * 4;
	public static final int ENEMY_BLOCK_SIZE = 15;

	// 시스템 메시지를 출력하는 영역을 생성한다.
	private SystemMessageArea systemMsg = new SystemMessageArea( // x, y, width,
																	// height
			// 20 * 1, 50 + (20 + 20 * 7), 20 * 5, 20 * 12
			// 20, 210, 100, 240
			NEW_BOARD_X+415, BOARD_Y*4, BLOCK_SIZE * 10+25, BLOCK_SIZE * 5);
	/*btnStart.setBounds(NEW_BOARD_X+450, NEW_BOARD_Y*3+100, BLOCK_SIZE * 8,
			messageArea.getHeight() / 2);*/
	// 메시지를 출력하는 영역을 생성한다. 넘겨준 값을 경계로 하는 영역이 생성된다.
	private MessageArea messageArea = new MessageArea( // TetrisBoard, x, y,
														// width, height
			// 2, 472, 338, 118
			this, NEW_BOARD_X+415, BOARD_Y*7-40, BLOCK_SIZE * 10+25, BLOCK_SIZE * 8);

	// 게임을 시작하고 종료할 수 있는 버튼 생성
	private ImageIcon imgiconStart = new ImageIcon("images/1.png");
	private Image imgStart = imgiconStart.getImage();
	private Image new_imgStart = imgStart.getScaledInstance(200,100,Image.SCALE_SMOOTH);
	private ImageIcon imgiconStart2 = new ImageIcon(new_imgStart);
	private JButton btnStart = new JButton(imgiconStart2);
	
	private ImageIcon imgiconExit = new ImageIcon("images/2.png");
	private Image imgExit = imgiconExit.getImage();
	private Image new_imgExit = imgExit.getScaledInstance(200,100,Image.SCALE_SMOOTH);
	private ImageIcon imgiconExit2 = new ImageIcon(new_imgExit);	
	private JButton btnExit = new JButton(imgiconExit2);
	
	// 팀을 선택할 수 있는 버튼 생성
	private ImageIcon imgiconTeam1 = new ImageIcon("images/A-1.png");
	private Image imgTeam1 = imgiconTeam1.getImage();
	private Image new_imgTeam1 = imgTeam1.getScaledInstance(110,110,Image.SCALE_SMOOTH);
	private ImageIcon imgiconTeam1_2 = new ImageIcon(new_imgTeam1);	
	private JButton btnTeamA = new JButton(imgiconTeam1_2);
	
	private ImageIcon imgiconTeam2 = new ImageIcon("images/B-1.png");
	private Image imgTeam2 = imgiconTeam2.getImage();
	private Image new_imgTeam2 = imgTeam2.getScaledInstance(110,110,Image.SCALE_SMOOTH);
	private ImageIcon imgiconTeam2_2 = new ImageIcon(new_imgTeam2);	
	private JButton btnTeamB = new JButton(imgiconTeam2_2);
	
	private ImageIcon imgiconTeam3 = new ImageIcon("images/C-1.png");
	private Image imgTeam3 = imgiconTeam3.getImage();
	private Image new_imgTeam3 = imgTeam3.getScaledInstance(110,110,Image.SCALE_SMOOTH);
	private ImageIcon imgiconTeam3_2 = new ImageIcon(new_imgTeam3);		
	private JButton btnTeamC = new JButton(imgiconTeam3_2);
	
	// 선택되었을 때의 변경 이미지 생성
	private ImageIcon imgiconTeam1_sel = new ImageIcon("images/A-2.png");
	private Image imgTeam1_sel = imgiconTeam1_sel.getImage();
	private Image new_imgTeam1_sel = imgTeam1_sel.getScaledInstance(110,110,Image.SCALE_SMOOTH);
	private ImageIcon imgiconTeam1_2_sel = new ImageIcon(new_imgTeam1_sel);	
	
	private ImageIcon imgiconTeam2_sel = new ImageIcon("images/B-2.png");
	private Image imgTeam2_sel = imgiconTeam2_sel.getImage();
	private Image new_imgTeam2_sel = imgTeam2_sel.getScaledInstance(110,110,Image.SCALE_SMOOTH);
	private ImageIcon imgiconTeam2_2_sel = new ImageIcon(new_imgTeam2_sel);	
	
	private ImageIcon imgiconTeam3_sel = new ImageIcon("images/C-2.png");
	private Image imgTeam3_sel = imgiconTeam3_sel.getImage();
	private Image new_imgTeam3_sel = imgTeam3_sel.getScaledInstance(110,110,Image.SCALE_SMOOTH);
	private ImageIcon imgiconTeam3_2_sel = new ImageIcon(new_imgTeam3_sel);	
	
	
	private String winMsg = "";
	private int teamWinFlag = 0;
	private int teamLoseFlag = 0;

	// 고스트 모드를 설정/해제 할 수 있는 체크 박스 생성하고, 디폴트로 미리 체크해둔다.
	private JCheckBox checkGhost = new JCheckBox("고스트모드", true);

	// 속도를 설정할 수 있는 콤보 박스를 생성한다. 콤보 박스란 다양한 옵션을 선택할 수 있는 박스다.
	private Integer[] lv = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
	private JComboBox<Integer> comboSpeed = new JComboBox<Integer>(lv);

	private String ip;
	private int port;
	private String nickName;

	private Thread th;
	private ArrayList<Block> blockList; // 바닥에 쌓여져 있는 블럭들의 정보를 담을 ArrayList
	private ArrayList<TetrisBlock> nextBlocks;

	// 추가된 부분
	private ArrayList<Block> opBlockList1; // 적들의 쌓여 있는 블럭 정보를 담을 ArrayList
	private ArrayList<Block> opBlockList2;
	private ArrayList<Block> opBlockList3;
	private ArrayList<Block> opBlockList4;
	
	private int opIndex1 = -1;
	private int opIndex2 = -1;
	private int opIndex3 = -1;
	private int opIndex4 = -1;
	// end of 추가된 부분

	/////////////////////////////////////
	public static final int Item_minus1 = 100;
	public static final int Item_minus2 = 200;
	public static final int Item_plus1 = 300;
	public static final int Item_plus2 = 400;

	private ArrayList<Integer> ItemList;

	/////////////////////////////////////

	private TetrisBlock shap; // 랜덤으로 생성되는 하나의 TetrisBlock
	private TetrisBlock ghost; // 위의 shap 정보를 받아 나타나는 ghost모드의 TetrisBlock
	private TetrisBlock hold;

	private Block[][] map;
	private TetrisController controller;
	private TetrisController controllerGhost;

	private boolean isPlay = false;
	private boolean isHold = false;

	// 고스트모드와 격자모드의 체크 박스를 생성할 때 디폴트로 미리 체크되어 있게 설정했기 때문에 둘 다 true값으로 설정한다.
	private boolean usingGhost = true;
	private boolean usingGrid = true;

	private int removeLineCount = 0;
	private int removeLineCombo = 0;

	// 생성자 (Tetris, GameClient)
	public TetrisBoard(Tetris tetris, GameClient client) {
		this.tetris = tetris;
		this.client = client;
		this.sendBit = false;

		this.setPreferredSize(new Dimension(PANEL_WIDTH-350, PANEL_HEIGHT-120)); // 이
																			// 컴포넌트(TetrisBoard)의
																			// 기본크기를
																			// 설정한다.

		// 키입력과 마우스입력을 받을 수 있는 Listener를 추가해준다.
		this.addKeyListener(this);
		this.addMouseListener(this);

		this.setLayout(null);
		this.setFocusable(true); // 이벤트를 받을 수 있게끔 설정

		// 시작버튼의 경계선 설정
		// x, y, width, height
		// 340, 472, 140, 59
		btnStart.setBounds(NEW_BOARD_X+450, NEW_BOARD_Y*3+50, BLOCK_SIZE * 8,	60);
		btnStart.setBorderPainted(false);
		btnStart.setContentAreaFilled(false);
		btnStart.setFocusPainted(false);
		// 시작하기 버튼을 비활성화
		btnStart.setFocusable(false);
		btnStart.setEnabled(false);

		btnStart.addActionListener(this); // 시작하기 버튼이 이벤트 처리를 할 수 있게끔 설정

		// 나가기 버튼의 경계선 설정
		// x, y, width, height
		// 340, 531, 140, 59
		btnExit.setBounds(NEW_BOARD_X+450, NEW_BOARD_Y*3+120, BLOCK_SIZE * 8,	60);
		btnExit.setBorderPainted(false);
		btnExit.setContentAreaFilled(false);
		btnExit.setFocusPainted(false);
		
		// 나가기 버튼을 비활성화
		btnExit.setFocusable(false);

		// 나가기 버튼이 이벤트 처리를 할 수 있게끔 설정
		btnExit.addActionListener(this);
		
		
		btnTeamA.setBounds(NEW_BOARD_X+415, NEW_BOARD_Y*2+140, 70, 60);
		btnTeamA.setBorderPainted(false);
		btnTeamA.setContentAreaFilled(false);
		btnTeamA.setFocusPainted(false);
		btnTeamA.addActionListener(this);
		
		btnTeamB.setBounds(NEW_BOARD_X+492, NEW_BOARD_Y*2+140, BLOCK_SIZE*3+10, 60);
		btnTeamB.setBorderPainted(false);
		btnTeamB.setContentAreaFilled(false);
		btnTeamB.setFocusPainted(false);
		btnTeamB.addActionListener(this);
		
		btnTeamC.setBounds(NEW_BOARD_X+569, NEW_BOARD_Y*2+140, BLOCK_SIZE*3+10, 60);
		btnTeamC.setBorderPainted(false);
		btnTeamC.setContentAreaFilled(false);
		btnTeamC.setFocusPainted(false);
		btnTeamC.addActionListener(this);

		// 고스트 모드 버튼의 경계와 컬러, 폰트를 설정
		checkGhost.setBounds(PANEL_WIDTH - BLOCK_SIZE * 30, 10, 95, 20);
		checkGhost.setBackground(new Color(9,20,50));
		checkGhost.setForeground(Color.WHITE);
		checkGhost.setFont(new Font("굴림", Font.BOLD, 13));

		// 고스트 모드 버튼을 눌렀을 때 이벤트 처리를 할 수 있게끔 설정하는 메소드
		checkGhost.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				usingGhost = checkGhost.isSelected(); // 선택되어 있다면 true가
														// usingGhost에 저장됨
				TetrisBoard.this.setRequestFocusEnabled(true); // 이벤트를 처리할 수 있도록
																// 포커스 설정을 해준다.
				TetrisBoard.this.repaint(); // 지금 적용된 상황을 바로 보여주고자 repaint() 사용
			}
		});
		// 속도를 조절할 수 있는 콤보 박스의 경계를 설정한다.
		comboSpeed.setBounds(PANEL_WIDTH - BLOCK_SIZE * 34, 10, 45, 20);

		// 설정이 완료된 각 버튼을 JPanel TetrisBoard에 추가해준다.
		this.add(comboSpeed);
		this.add(systemMsg);
		this.add(messageArea);
		this.add(btnStart);
		this.add(btnExit);
		this.add(checkGhost);
		//this.add(checkGrid);
		
				
		this.add(btnTeamA);
		this.add(btnTeamB);
		this.add(btnTeamC);
	}

	// 매개변수로 넘어온 값들로 멤버 변수 값을 설정
	public void startNetworking(String ip, int port, String nickName) {
		this.ip = ip;
		this.port = port;
		this.nickName = nickName;
		this.repaint(); // 변경된 항목을 바로 JPanel 위에 출력한다.
	}

	/**
	 * TODO : 게임시작 게임을 시작한다.
	 */

	public void gameStart(int speed) {
		comboSpeed.setSelectedItem(new Integer(speed)); // 넘어온 speed 값으로 콤보 박스
														// 항목이 선택되게끔 설정

		// 돌고 있을 스레드를 정지시킨다.
		// 시작하기 버튼을 누를 때마다 기존의 게임이 없어지고 새로운 게임이 시작된다.
		if (th != null) {
			try {
				isPlay = false;
				th.join(); // 작업 중 다른 쓰레드의 작업이 먼저 수행되어야할 필요가 있을 때 사용, 즉 th의 작업이
							// 끝나기를 기다린다.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// 맵셋팅
		map = new Block[maxY][maxX]; // 10 * 21
		blockList = new ArrayList<Block>();
		// 추가된 부분
		opBlockList1 = new ArrayList<Block>();
		opBlockList2 = new ArrayList<Block>();
		opBlockList3 = new ArrayList<Block>();
		opBlockList4 = new ArrayList<Block>();

		ItemList = new ArrayList<Integer>();
		ItemList.add(Item_minus1);
		ItemList.add(Item_minus2);
		ItemList.add(Item_plus1);
		ItemList.add(Item_plus2);
		// end of 추가된 부분

		nextBlocks = new ArrayList<TetrisBlock>();

		// 도형셋팅
		shap = getRandomTetrisBlock(); // TetrisBlock shap에 랜덤한 블럭을 저장
		ghost = getBlockClone(shap, true); // TetrisBlock ghost에

		hold = null; // hold 도형은 일단 없다. 따라서 처음에는 홀드할 수 없다.
		isHold = false; // hold 도형이 없으므로 isHold 또한 false다.

		controller = new TetrisController(shap, maxX - 1, maxY - 1, map);
		controllerGhost = new TetrisController(ghost, maxX - 1, maxY - 1, map);

		this.showGhost();

		for (int i = 0; i < 5; i++) { // 다음에 나올 블럭을 5개 보여준다.
			nextBlocks.add(getRandomTetrisBlock());
		}

		// 스레드 셋팅 및 실행
		isPlay = true;
		th = new Thread(this);
		th.start();
	}

	// TODO : paint
	@Override
	protected void paintComponent(Graphics g) {
		g.clearRect(0, 0, this.getWidth(), this.getHeight() + 1);
		Image background=new ImageIcon("images/tetris_bg.png").getImage();
		g.drawImage(background, 0, 0, null);//background를 그려줌
		

		// 상단의 ip 정보 와 nickName 정보를 출력한다.
		g.setColor(Color.WHITE);
		g.drawString("ip : " + ip + "     port : " + port, BLOCK_SIZE * 40, 25);
		g.drawString("닉네임 : " + nickName, 220, 25);

		// 속도
		Font font = g.getFont(); // 원래의 폰트 정보를 저장해 놓는다.
		g.setFont(new Font("굴림", Font.BOLD, 13));
		g.drawString("속도", PANEL_WIDTH - BLOCK_SIZE * 37, 25);
		g.setFont(font); // 위에서 저장해 둔 폰트 값으로 다시 돌아온다.

		/*
		 * 수정된 파트는 위 쪽에 주석처리가 되어 있으며 새로 바뀐 파트가 바로 아래 작성되어 있다.
		 *
		 */
		g.setColor(new Color(53,150,240));
		g.fillRect(670, 123, maxX * BLOCK_SIZE+20, maxY * BLOCK_SIZE+45);
		g.setColor(Color.BLACK);
		// 테트리스 블럭이 직접적으로 움직일 게임 보드를 칠한다.
		// g.fillRect(BOARD_X + BLOCK_SIZE * minX, BOARD_Y, maxX * BLOCK_SIZE +
		// 1, maxY * BLOCK_SIZE + 1);
		// (140, 50, 201, 421)
		g.fillRect(680, 133, maxX * BLOCK_SIZE, maxY * BLOCK_SIZE);
		// 원래는 시작 x좌표가 NEW_BOARD_X 뿐이었지만, 출력 문제로 + 20으로 조정

		/*
		 * 적들의 블럭을 출력할 좌표판을 만든다. 좌표판은 총 4개로 본인을 포함하여 5인플레이 기준으로 한다. 원래 적군의 x좌표에
		 * -5를 해줘서 싱크를 맞춰줬다. 출력의 오류가 이로써 수정됐다.
		 */
		
		
		// 적군 1의 보드 (원래의 소스코드 플레이어1이 사용하는 공간이었다.)
		g.setColor(new Color(53,150,240));
		g.fillRect(30,123, (maxX * BLOCK_SIZE) * 3 / 4 + 13,
				(maxY * BLOCK_SIZE) * 3 / 4 + 13);
		g.setColor(Color.BLACK);
		g.fillRect(36, 129, (maxX * BLOCK_SIZE) * 3 / 4 + 1,
				(maxY * BLOCK_SIZE) * 3 / 4 + 1);
		
		// 적군 2의 보드
		g.setColor(new Color(53,150,240));
		g.fillRect(220,123, (maxX * BLOCK_SIZE) * 3 / 4 + 13,
				(maxY * BLOCK_SIZE) * 3 / 4 + 13);
		g.setColor(Color.BLACK);
		g.fillRect(226, 129, (maxX * BLOCK_SIZE) * 3 / 4 + 1,
				(maxY * BLOCK_SIZE) * 3 / 4 + 1);
		

		
		// 적군 3의 보드
		g.setColor(new Color(53,150,240));
		g.fillRect(30, 483, (maxX * BLOCK_SIZE) * 3 / 4 + 13,
				(maxY * BLOCK_SIZE) * 3 / 4 + 13);
		g.setColor(Color.BLACK);
		g.fillRect(36, 489, (maxX * BLOCK_SIZE) * 3 / 4 + 1,
				(maxY * BLOCK_SIZE) * 3 / 4 + 1);
	
		// 적군 4의 보드
		g.setColor(new Color(53,150,240));
		g.fillRect(220, 483, (maxX * BLOCK_SIZE) * 3 / 4 + 13,
				(maxY * BLOCK_SIZE) * 3 / 4 + 13);
		g.setColor(Color.BLACK);
		g.fillRect(226, 489, (maxX * BLOCK_SIZE) * 3 / 4 + 1,
				(maxY * BLOCK_SIZE) * 3 / 4 + 1);
		
		// 적군 5의 보드
		g.setColor(new Color(53,150,240));
		g.fillRect(400, 483, (maxX * BLOCK_SIZE) * 3 / 4 + 13,
				(maxY * BLOCK_SIZE) * 3 / 4 + 13);
		g.setColor(Color.BLACK);
		g.fillRect(406, 489, (maxX * BLOCK_SIZE) * 3 / 4 + 1,
				(maxY * BLOCK_SIZE) * 3 / 4 + 1);

		//////////////////////////////////////////////
		//	ItemList를 그릴 공간
		
		g.setColor(new Color(24,77,183));
		g.fillRect(680, 560, maxX * BLOCK_SIZE, BLOCK_SIZE);
		if(ItemList!=null)
		for (int i = 0; i < ItemList.size(); i++) {
			switch (ItemList.get(i)) {
			case Item_minus1:
				g.setColor(Color.cyan);
				g.fillRect(680+(BLOCK_SIZE*i), 560, BLOCK_SIZE, BLOCK_SIZE);
				g.setColor(Color.black);
				g.setFont(new Font(font.getFontName(), font.BOLD, 15));
				g.drawString("-1", 682+(BLOCK_SIZE*i), 572);
				break;
			case Item_minus2:
				g.setColor(Color.blue);
				g.fillRect(680+(BLOCK_SIZE*i), 560, BLOCK_SIZE, BLOCK_SIZE);
				g.setColor(Color.black);
				g.setFont(new Font(font.getFontName(), font.BOLD, 15));
				g.drawString("-2", 682+(BLOCK_SIZE*i), 572);
				break;
			case Item_plus1:
				g.setColor(Color.orange);
				g.fillRect(680+(BLOCK_SIZE*i), 560, BLOCK_SIZE, BLOCK_SIZE);
				g.setColor(Color.black);
				g.setFont(new Font(font.getFontName(), font.BOLD, 15));
				g.drawString("+1", 682+(BLOCK_SIZE*i), 572);
				break;
			case Item_plus2:
				g.setColor(Color.red);
				g.fillRect(680+(BLOCK_SIZE*i), 560, BLOCK_SIZE, BLOCK_SIZE);
				g.setColor(Color.black);
				g.setFont(new Font(font.getFontName(), font.BOLD, 15));
				g.drawString("+2", 682+(BLOCK_SIZE*i), 572);
				break;
			}
		}
		
		//////////////////////////////////////////////
		// 자기 자신의 정보(Index 등)를 표시할 공간
		g.setColor(new Color(24,77,183));
		g.fillRect(670, 600, maxX * BLOCK_SIZE+20, 3*BLOCK_SIZE);
		g.setColor(Color.black);
		g.fillRect(680, 610, maxX * BLOCK_SIZE, 3*BLOCK_SIZE-20);
		// 자기 자신의 Index 표시(GameClient 기준)
		g.setColor(Color.white);
		g.setFont(new Font(font.getFontName(), font.BOLD, 20));
		if(client!=null)
			g.drawString(Integer.toString(client.getIndex()), 690, 635);
		
		// 다른 player들의 정보(Index 등)을 표시할 공간
		
		// player1
		g.setColor(Color.BLACK);
		g.fillRect(30, 73, (maxX * BLOCK_SIZE) * 3 / 4 + 13, 3*BLOCK_SIZE-20);
		g.setColor(Color.white);
		if(opIndex1!=-1)
			g.drawString(Integer.toString(opIndex1), 40, 95);
		
		// player2
		g.setColor(Color.BLACK);
		g.fillRect(220, 453, (maxX * BLOCK_SIZE) * 3 / 4 + 13, 3*BLOCK_SIZE-33);
		g.setColor(Color.WHITE);
		if(opIndex2!=-1)
			g.drawString(Integer.toString(opIndex2), 230, 475);
		
		// player3
		g.setColor(Color.BLACK);
		g.fillRect(30, 453, (maxX * BLOCK_SIZE) * 3 / 4 + 13, 3*BLOCK_SIZE-33);
		g.setColor(Color.white);
		//BOARD_X + BLOCK_SIZE * minX, BOARD_Y * 8
		if(opIndex3!=-1)
			g.drawString(Integer.toString(opIndex3), 40, 475);
		
		// player4
		g.setColor(Color.BLACK);
		g.fillRect(400, 453, (maxX * BLOCK_SIZE) * 3 / 4 + 13, 3*BLOCK_SIZE-33);
		g.setColor(Color.WHITE);
		if(opIndex4!=-1)
			g.drawString(Integer.toString(opIndex4), 410, 475);
		
		// player5
		g.setColor(Color.BLACK);
		g.fillRect(220, 73, (maxX * BLOCK_SIZE) * 3 / 4 + 13, 3*BLOCK_SIZE-20);
		g.setColor(Color.WHITE);
		//if(opIndex4!=-1)
			//g.drawString(Integer.toString(opIndex4), 230, 95);
		//////////////////////////////////////////////
		

		g.setFont(new Font("굴림", Font.ITALIC, 30));
		g.drawString("[ Team Select ]", NEW_BOARD_X+415, NEW_BOARD_Y*2+120);
		
		int x = 0, y = 0, newY = 0;

		if (hold != null) { // 좌측 상단 HOLD 파트에 출력을 한다.
			x = 0;
			y = 0;
			newY = 6;

			x = hold.getPosX();
			y = hold.getPosY();

			hold.setPosX(-9 + minX);
			hold.setPosY(newY + minY);

			hold.drawBlock(g);

			hold.setPosX(x);
			hold.setPosY(y);
		}

		if (nextBlocks != null) {
			x = 0;
			y = 3;
			newY = 6;

			for (int i = 0; i < nextBlocks.size(); i++) {
				// 다음 블럭의 객체 정보를 가져와서 x좌표, y좌표의 값을 따로 저장해둔다.
				TetrisBlock block = nextBlocks.get(i);
				x = block.getPosX();
				y = block.getPosY();

				// 다음에 나올 블럭들이 우측 하단에 잘 위치해 있을 수 있게 설정
				block.setPosX(17 + minX);
				block.setPosY(newY + minY);

				// 우측 상단의 NEXT 파트를 별도로 처리해주기 위해 인위적으로 설정
				// NEXT 파트를 제외한 아래 부분은 일정한 간격 (+3의 간격)으로 띄어져 출력된다.
				if (newY == 6)
					newY = 9;
				// 위에서 설정한 좌표를 토대로 블럭을 출력한다.
				block.drawBlock(g);

				// 위에서 따로 저장해 둔 값으로 좌표를 초기화 해준다. 이는 원래 블럭의 x와 y값으로 돌아가는 작업이다.
				// 위에서 조작한 x, y좌표는 NEXT부분 출력을 위해서 잠깐 조작한 것이다.
				block.setPosX(x);
				block.setPosY(y);

				// +3의 간격으로 다음 블럭들이 출력된다.
				newY += 3;
			}
		}

		// 현재 바닥에 쌓여져 있는 블럭들을 출력하는 작업
		if (blockList != null) {
			x = 0;
			y = 0;
			for (int i = 0; i < blockList.size(); i++) {
				// 블럭 객체의 x, y좌표 값을 가져온다.
				Block block = blockList.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();

				block.setPosGridX(x + minX); // 이 부분이 생략되면 블럭들이 실제 있는 좌표에서 한 칸씩
												// 좌측으로 밀리는 현상 발생
				block.setPosGridY(y + minY);

				block.drawColorBlock(g); // 블럭을 출력한다.

				block.setPosGridX(x);
				block.setPosGridY(y);
			}
		}
		// 추가된 부분
		// 적들의 블럭리스트를 출력한다.
		if (opBlockList1 != null) {
			for (int i = 0; i < opBlockList1.size(); i++) {
				Block block = opBlockList1.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();

				block.setPosGridX(x + minX); 	// 이 부분이 생략되면 블럭들이 실제 있는 좌표에서 한 칸씩
												// 좌측으로 밀리는 현상 발생
				block.setPosGridY(y + minY);

				block.drawColorBlock(g); // 블럭을 출력한다.

				block.setPosGridX(x);
				block.setPosGridY(y);
			}
		}

		if (opBlockList2 != null) {
			for (int i = 0; i < opBlockList2.size(); i++) {
				Block block = opBlockList2.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();

				block.setPosGridX(x + minX); // 이 부분이 생략되면 블럭들이 실제 있는 좌표에서 한 칸씩
												// 좌측으로 밀리는 현상 발생
				block.setPosGridY(y + minY);

				block.drawColorBlock(g); // 블럭을 출력한다.

				block.setPosGridX(x);
				block.setPosGridY(y);

			}
		}
		if (opBlockList3 != null) {
			for (int i = 0; i < opBlockList3.size(); i++) {
				Block block = opBlockList3.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();

				block.setPosGridX(x + minX); // 이 부분이 생략되면 블럭들이 실제 있는 좌표에서 한 칸씩
												// 좌측으로 밀리는 현상 발생
				block.setPosGridY(y + minY);

				block.drawColorBlock(g); // 블럭을 출력한다.

				block.setPosGridX(x);
				block.setPosGridY(y);

			}
		}
		if (opBlockList4 != null) {
			for (int i = 0; i < opBlockList4.size(); i++) {
				Block block = opBlockList4.get(i);
				x = block.getPosGridX();
				y = block.getPosGridY();

				block.setPosGridX(x + minX); // 이 부분이 생략되면 블럭들이 실제 있는 좌표에서 한 칸씩
												// 좌측으로 밀리는 현상 발생
				block.setPosGridY(y + minY);

				block.drawColorBlock(g); // 블럭을 출력한다.

				block.setPosGridX(x);
				block.setPosGridY(y);

			}
		}
		// end of 추가된 부분

		if (ghost != null) {
			// 고스트 모드를 사용 중이라면
			if (usingGhost) {
				// 고스트 모드의 x, y좌표 값을 가져온다.
				x = 0;
				y = 0;
				x = ghost.getPosX();
				y = ghost.getPosY();

				ghost.setPosX(x + minX); // 이 부분이 생략되면 떨어질 위치를 표시하는 좌표가 한 칸씩
											// 좌측으로 밀리는 현상 발생
				ghost.setPosY(y + minY);

				ghost.drawBlock(g); // 고스트 블럭을 출력한다.

				ghost.setPosX(x);
				ghost.setPosY(y);
			}
		}

		// 위에서 떨어지고 있는 블럭을 출력한다.
		if (shap != null) {
			x = 0;
			y = 0;
			x = shap.getPosX();
			y = shap.getPosY();
			
			shap.setPosX(x + minX);
			shap.setPosY(y + minY);

			shap.drawBlock(g);

			shap.setPosX(x);
			shap.setPosY(y);
		}
		g.setColor(Color.yellow);
		g.setFont(new Font("굴림", Font.ITALIC, 30));
		if(teamWinFlag==1 && winMsg!=null)			
			g.drawString(winMsg, 700, 233);
		if(teamLoseFlag==1)
			g.drawString("Lose!", 700, 233);
	} // end of paintComponent

	@Override
	public void run() { // TetrisBoard 스레드 실행
		int countMove = (21 - (int) comboSpeed.getSelectedItem()) * 5;
		int countDown = 0;
		int countUp = up; // up의 초기값은 0으로 설정되어 있다.

		while (isPlay) { // 플레이 중이라면
			try {
				Thread.sleep(10); // 10 밀리 세컨드 = 0.01초
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (countDown != 0) { // 첫 실행에서는 이 부분이 실행되지 않는다.
				countDown--;

				if (countDown == 0) {
					// controller가 존재하고, moveDown 메소드에서 false가 반환됐을 때 블럭을 하단에 고정
					// moveDown 메소드에서 false가 반환된 건 더 이상 내려갈 수 없기 때문, 즉 바닥에 닿았다는
					// 걸 뜻한다.
					if (controller != null && !controller.moveDown())
						this.fixingTetrisBlock();
				}

				this.repaint(); // 컴포넌트의 상태가 변경됐으므로 화면에 다시 그린다.
				continue; // while문의 조건문으로 다시 간다.
			}

			countMove--; // 레벨에 맞게 설정된 countMove값 1 감소

			if (countMove == 0) { // 0이 되었다면 현재 레벨에 맞게 다시금 countMove가 조정된다.
				countMove = (21 - (int) comboSpeed.getSelectedItem()) * 5;

				// 컨트롤러가 존재하고 블럭이 바닥에 닿았다면
				if (controller != null && !controller.moveDown())
					countDown = down; // down의 값은 생성자에서 50으로 설정된다.
				else
					this.showGhost(); // 컨트롤러가 없거나 블럭이 아직 바닥에 닿지 않았다면 고스트를 출력
			}

			if (countUp != 0) { // countUp의 초기값은 0이므로 처음에 실행되지 않는다.
				countUp--;
				if (countUp == 0) {
					countUp = up;
//					addBlockLine(1);
				}
			}

			// 컴포넌트의 변경된 내용을 다시 출력한다.
			this.repaint();
		} // while()
	} // run()

	/**
	 * 맵(보이기, 논리)을 상하로 이동한다.
	 * 
	 * @param lineNumber
	 * @param num
	 *            -1 or 1
	 */
	public void dropBoard(int lineNumber, int num) {

		// 맵을 떨어트린다.
		this.dropMap(lineNumber, num);

		// 좌표바꿔주기(1만큼증가)
		this.changeTetrisBlockLine(lineNumber, num);

		// 다시 체크하기
		this.checkMap();
		
		if(sendBit == false)
			client.sendBlockList(blockList);

		// 고스트 다시 뿌리기
		this.showGhost();
	}

	/**
	 * lineNumber의 위쪽 라인들을 모두 num칸씩 내린다.
	 * 
	 * @param lineNumber
	 * @param num
	 *            칸수 -1,1
	 */
	private void dropMap(int lineNumber, int num) {
		if (num == 1) {
			// 한줄씩 내리기
			for (int i = lineNumber; i > 0; i--) {
				for (int j = 0; j < map[i].length; j++) {
					map[i][j] = map[i - 1][j];
				}
			}

			// 맨 윗줄은 null로 만들기
			for (int j = 0; j < map[0].length; j++) {
				map[0][j] = null;
			}
		} else if (num == -1) {
			// 한줄씩 올리기
			for (int i = 1; i <= lineNumber; i++) {
				for (int j = 0; j < map[i].length; j++) {
					map[i - 1][j] = map[i][j];
				}
			}

			// removeLine은 null로 만들기
			for (int j = 0; j < map[0].length; j++) {
				map[lineNumber][j] = null;
			}
		}
	}

	/**
	 * lineNumber의 위쪽 라인들을 모두 num만큼 이동시킨다.
	 * 
	 * @param lineNumber
	 * @param num
	 *            이동할 라인
	 */
	private void changeTetrisBlockLine(int lineNumber, int num) {
		int y = 0, posY = 0;
		for (int i = 0; i < blockList.size(); i++) {
			y = blockList.get(i).getY();
			posY = blockList.get(i).getPosGridY();
			if (y <= lineNumber)
				blockList.get(i).setPosGridY(posY + num);
		}

	}

	/**
	 * 테트리스 블럭을 고정시킨다. 테트리스가 바닥에 닿았을 때 이 메서드는 실행된다.
	 */
	private void fixingTetrisBlock() {
//		synchronized (this) {
//			if (stop) {
//				try {
//					this.wait(); // stop 비트가 켜져 있었다면 대기
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}

		boolean isCombo = false;
		removeLineCount = 0;

		// drawList 추가
		for (Block block : shap.getBlock()) { // shap을 구성하고 있는 미니 블럭들을
												// blockList에 추가
			blockList.add(block);
		}

		isCombo = checkMap(); // removeLineCount++가 이 메소드 안에서 실행된다.
		
		if (sendBit == false)
			client.sendBlockList(blockList);
		
		if (isCombo)
			removeLineCombo++;
		else
			removeLineCombo = 0;

		// 콜백메소드
		this.getFixBlockCallBack(blockList, removeLineCombo, removeLineCount);

		// 다음 테트리스 블럭을 가져온다.
		this.nextTetrisBlock();

		// 홀드가능상태로 만들어준다.
		isHold = false;


	}// fixingTetrisBlock()

	/**
	 * 
	 * @return true-지우기성공, false-지우기실패
	 */
	private boolean checkMap() {
		boolean isCombo = false;
		int count = 0;
		int item_count0= 0;
		int item_count1= 0;
		int item_count2= 0;
		int item_count3= 0;
		Block mainBlock;

		for (int i = 0; i < blockList.size(); i++) {
			mainBlock = blockList.get(i);

			// 여기서 getX와 getY는 각각 posGridXY + fixGridXY의 합으로 블럭의 절대 좌표를 의밓한다.
			// map에 추가
			if (mainBlock.getY() < 0 || mainBlock.getY() >= maxY)
				continue;

			// mainBlock의 행(getY)과 열(getX)의 범위가 한도를 초과하지 않을 때 실행
			// 맵 논리에서 수정해야할 부분
			// 맵 범위의 한도에 맞을 때 맵에 추가
			if (mainBlock.getY() < maxY && mainBlock.getX() < maxX)
				map[mainBlock.getY()][mainBlock.getX()] = mainBlock;

			// 줄이 꽉 찼을 경우. 게임을 종료한다.
			if (mainBlock.getY() == 1 && mainBlock.getX() > 2 && mainBlock.getX() < 7) {
				this.gameEndCallBack();
				break;
			}

			// 1줄개수 체크
			count = 0;
			item_count0 = 0;
			item_count1 = 0;
			item_count2 = 0;
			item_count3 = 0;
			for (int j = 0; j < maxX; j++) {
				if (map[mainBlock.getY()][j] != null){
					count++;
					//System.out.println(map[mainBlock.getY()][j].item);
					if (map[mainBlock.getY()][j].item==1)
						switch(map[mainBlock.getY()][j].itemIndex){
						case 0:item_count0++;break;
						case 1:item_count1++;break;
						case 2:item_count2++;break;
						case 3:item_count3++;break;
						default:break;
						}
				}
				
			}

			// block의 해당 line을 지운다.
			if (count == maxX) {
				if (ItemList.size() < 10) {
					if (item_count0 != 0)
						ItemList.add(Item_minus1);
					if (item_count1 != 0)
						ItemList.add(Item_minus2);
					if (item_count2 != 0)
						ItemList.add(Item_plus1);
					if (item_count3 != 0)
						ItemList.add(Item_plus2);
				}
				if(ItemList.size()>10)
					ItemList.remove(10);
				removeLineCount++;
				this.removeBlockLine(mainBlock.getY());
				isCombo = true;
			}
		}
		return isCombo;
	}

	/**
	 * 테트리스 블럭 리스트에서 테트리스 블럭을 받아온다.
	 */
	public void nextTetrisBlock() {
		shap = nextBlocks.get(0);
		this.initController();
		nextBlocks.remove(0);
		nextBlocks.add(getRandomTetrisBlock());
	}

	private void initController() {
		controller.setBlock(shap);
		ghost = getBlockClone(shap, true);
		controllerGhost.setBlock(ghost);
	}

	/**
	 * lineNumber 라인을 삭제하고, drawlist에서 제거하고, map을 아래로 내린다.
	 * 
	 * @param lineNumber
	 *            삭제라인
	 */
	private void removeBlockLine(int lineNumber) {
		// 1줄을 지워줌
		for (int j = 0; j < maxX; j++) {
			for (int s = 0; s < blockList.size(); s++) {
				Block b = blockList.get(s);
				if (b == map[lineNumber][j])
					blockList.remove(s);
			}
			map[lineNumber][j] = null;
		} // for(j)


		this.dropBoard(lineNumber, 1);
	}
	
	public void ItemRemove(int numOfBlock){
		switch(numOfBlock){
		case 2:
			this.removeBlockLine(19);
		case 1:
			this.removeBlockLine(20);
			break;
		default:
			break;
		}
	}

	/**
	 * TODO : 게임종료콜벡 게임이 종료되면 실행되는 메소드
	 */
	public void gameEndCallBack() {
		client.gameover();
		blockList.removeAll(blockList);
		repaint();
		loseCallBack();
		//System.out.println(client.getTeam() + " Dead!");
		this.isPlay = false;
	}

	/**
	 * TODO : 게임에 승리했을 때 승리(팀) 표시 메소드
	 */
	public void winCallBack(){
		if(client.getTeam()==1)
			winMsg = "Team A Win!";
		else if(client.getTeam()==2)
			winMsg = "Team B Win!";
		else if(client.getTeam()==3)
			winMsg = "Team C Win!";
		else
			winMsg = "Win!!";
		teamLoseFlag = 0;
		teamWinFlag = 1;
		System.out.println(client.getTeam() + " Win!");
		repaint();
	}
	/**
	 * TODO : 게임에 패배했을 때 승리(팀) 표시 메소드
	 */
	public void loseCallBack(){
		teamLoseFlag = 1;
		//System.out.println(client.getTeam() + " Win!");
	}
	/**
	 * 고스트블럭을 보여준다.
	 */
	private void showGhost() {
		ghost = getBlockClone(shap, true);
		controllerGhost.setBlock(ghost);
		controllerGhost.moveQuickDown(shap.getPosY(), true);
	}

	/**
	 * 랜덤으로 테트리스 블럭을 생성하고 반환한다.
	 * 
	 * @return 테트리스 블럭
	 */
	// 이 파트는 테트리스 블럭이 생성될 때 시작좌표를 지정해주는 파트다.
	public TetrisBlock getRandomTetrisBlock() {
		switch ((int) (Math.random() * 7)) { // 블럭의 종류에 따라 블럭을 생성 후 반환
		case TetrisBlock.TYPE_CENTERUP:
			return new CenterUp(4, 1);
		case TetrisBlock.TYPE_LEFTTWOUP:
			return new LeftTwoUp(4, 1);
		case TetrisBlock.TYPE_LEFTUP:
			return new LeftUp(4, 1);
		case TetrisBlock.TYPE_RIGHTTWOUP:
			return new RightTwoUp(4, 1);
		case TetrisBlock.TYPE_RIGHTUP:
			return new RightUp(4, 1);
		case TetrisBlock.TYPE_LINE:
			return new Line(4, 1);
		case TetrisBlock.TYPE_NEMO:
			return new Nemo(4, 1);
		}
		return null;
	}

	/**
	 * tetrisBlock과 같은 모양으로 고스트의 블럭모양을 반환한다.
	 * 
	 * @param tetrisBlock
	 *            고스트의 블럭모양을 결정할 블럭
	 * @return 고스트의 블럭모양을 반환
	 */
	public TetrisBlock getBlockClone(TetrisBlock tetrisBlock, boolean isGhost) {
		TetrisBlock ghostBlock = null;
		switch (tetrisBlock.getType()) {
		case TetrisBlock.TYPE_CENTERUP:
			ghostBlock = new CenterUp(4, 1);
			break;
		case TetrisBlock.TYPE_LEFTTWOUP:
			ghostBlock = new LeftTwoUp(4, 1);
			break;
		case TetrisBlock.TYPE_LEFTUP:
			ghostBlock = new LeftUp(4, 1);
			break;
		case TetrisBlock.TYPE_RIGHTTWOUP:
			ghostBlock = new RightTwoUp(4, 1);
			break;
		case TetrisBlock.TYPE_RIGHTUP:
			ghostBlock = new RightUp(4, 1);
			break;
		case TetrisBlock.TYPE_LINE:
			ghostBlock = new Line(4, 1);
			break;
		case TetrisBlock.TYPE_NEMO:
			ghostBlock = new Nemo(4, 1);
			break;
		}
		if (ghostBlock != null && isGhost) { // 매개변수로 넘어온 블럭이 존재하고, 고스트모드로 설정되어
												// 있었다면
			ghostBlock.setGhostView(isGhost);
			ghostBlock.setPosX(tetrisBlock.getPosX());
			ghostBlock.setPosY(tetrisBlock.getPosY());
			ghostBlock.rotation(tetrisBlock.getRotationIndex());
		}
		return ghostBlock;
	}

	/**
	 * TODO : 콜백메소드 테트리스 블럭이 고정될 때 자동 호출 된다.
	 * 
	 * @param removeCombo
	 *            현재 콤보 수
	 * @param removeMaxLine
	 *            한번에 지운 줄수
	 */
/*	public void attackClients(int numOfLine){
		client.addBlock(numOfLine);
		if()
	}*/
	
	public void getFixBlockCallBack(ArrayList<Block> blockList, int removeCombo, int removeMaxLine) {
		if (removeCombo < 3) {
			// 콤보가 3 미만이고 한번에 지운 라인의 수가 3일 때
			if (removeMaxLine == 3)
				client.addBlock(1); // 클라이언트에 1줄을 추가하는 공격 발동

			else if (removeMaxLine == 4)
				client.addBlock(3); // 한번에 지운 라인의 수가 4였다면, 클라이언트에 3줄을 추가하는 공격 발동
		} 
	}

	/**
	 * 블럭을 홀드시킨다.
	 */
	public void playBlockHold() {
		if (isHold)
			return; // 이미 홀드가 되어 있다면 그냥 리턴

		if (hold == null) {
			hold = getBlockClone(shap, false);
			this.nextTetrisBlock();
		} else {
			TetrisBlock tmp = getBlockClone(shap, false);
			shap = getBlockClone(hold, false);
			hold = getBlockClone(tmp, false);
			this.initController();
		}

		isHold = true;
	}

	/**
	 * 가장 밑에 줄에 블럭을 생성한다.
	 * 
	 * @param numOfLine
	 */
	boolean stop = false;

	public void addBlockLine(int numOfLine) {
		stop = true; // stop 비트가 켜져 있으므로 다른 스레드에서 접근 불가
		sendBit = true;	// 불필요한 네트워크 전송을 방지하기 위한 플래그
		// 내리기가 있을 때까지 대기한다.
		// 내리기를 모두 실행한 후 다시 시작한다.
		Block block;
		int rand = (int) (Math.random() * maxX);
		for (int i = 0; i < numOfLine; i++) {
			this.dropBoard(maxY - 1, -1);

			// 맨 하단에 rand 부분을 제외한 밑줄을 추가시키는 작업
			for (int col = 0; col < maxX; col++) {
				if (col != rand) { // rand를 제외한 나머지에서 회색 Block을 생성
					block = new Block(0, 0, Color.GRAY, Color.GRAY, 0, 0);
					block.setPosGridXY(col, maxY - 1); // 이 블럭의 절대좌표는 col, maxY
														// - 1
					blockList.add(block);
					map[maxY - 1][col] = block; // 블럭의 절대좌표에 해당하는 map에 생성된
												// block을 위치
				}
			}
			// 만약 내려오는 블럭과 겹치면 블럭을 위로 올린다.
			boolean up = false;
			for (int j = 0; j < shap.getBlock().length; j++) {
				Block sBlock = shap.getBlock(j);
				if (map[sBlock.getY()][sBlock.getX()] != null) {
					up = true;
					break;
				}
			}
			if (up) {
				controller.moveDown(-1);
			}
		}

		client.addRenewal(blockList);

		this.showGhost();
		this.repaint();

		sendBit = false;

	}
	
	public void ItemActivate(int index){
		if (ItemList.size() != 0) {
			switch (ItemList.get(0)) {
			case Item_plus1:
				client.addItemBlock(1, index);
				break;
			case Item_plus2:
				client.addItemBlock(2, index);
				break;
			case Item_minus1:
				client.removeItemLine(1, index);
				break;
			case Item_minus2:
				client.removeItemLine(2, index);
				break;
			default:
				break;
			}
			ItemList.remove(0);
		}
	}

	public void keyReleased(KeyEvent e) {
	} // 키를 뗐을 때 실행할 함수

	public void keyTyped(KeyEvent e) {
	} // 키가 입력된 순간 실행할 함수

	public void keyPressed(KeyEvent e) { // 키가 눌렸을 때 실행할 함수
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			messageArea.requestFocus(); // 메시지 영역이 입력 포커스를 받을 수 있게끔 설정
		}
		if (!isPlay)
			return; // 플레이 중이 아니었다면 함수를 빠져 나온다.

		/* 각 키의 입력에 따라 블럭의 이동 처리 */
		if (e.getKeyCode() == KeyEvent.VK_LEFT) { // 매개변수가 없는 경우 한 칸 이동을 의미
			controller.moveLeft();
			controllerGhost.moveLeft();
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			controller.moveRight();
			controllerGhost.moveRight();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			controller.moveDown();
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			controller.nextRotationLeft();
			controllerGhost.nextRotationLeft();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			controller.moveQuickDown(shap.getPosY(), true);
			this.fixingTetrisBlock();
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			playBlockHold();
		} else if (e.getKeyCode() == KeyEvent.VK_F) {
			this.removeBlockLine(20);
			// System.out.println);
			// System.out.println("key pressed!");
		} else if (e.getKeyCode() == KeyEvent.VK_1) {
			ItemActivate(1);
		} else if (e.getKeyCode() == KeyEvent.VK_2) {
			ItemActivate(2);
		} else if (e.getKeyCode() == KeyEvent.VK_3) {
			ItemActivate(3);
		} else if (e.getKeyCode() == KeyEvent.VK_4) {
			ItemActivate(4);
		} else if (e.getKeyCode() == KeyEvent.VK_5) {
			ItemActivate(5);
		}

		// 이동 데이터 처리 후 고스트 모드와 새로운 출력을 진행한다.
		this.showGhost();
		this.repaint();
	}

	/* 마우스에 대한 이벤트 처리는 별도로 하지 않는다. 단, 마우스가 눌렸을 때만 포커싱을 진행해준다. */
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		this.requestFocus();
	}

	public void mouseReleased(MouseEvent e) {
	}

	// 이벤트가 발생했을 때 처리해주는 메소드
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnStart) { // 시작하기 버튼이었을 경우
			if (client != null) {
				// client의 gameStart 메소드를 실행
				client.gameStart((int) comboSpeed.getSelectedItem());
			} else { // clinet가 존재하지 않을 때는 {TetrisBoard의 gameStart 메소드를 실행
				this.gameStart((int) comboSpeed.getSelectedItem());
			}
		} else if (e.getSource() == btnExit) { // 나가기 버튼이었을 경우
			if (client != null) {
				if (tetris.isNetwork()) { // 네트워크가 작동 중이면 네트워크를 닫아준다.
					client.closeNetwork(tetris.isServer());
				}
			} else {
				System.exit(0);
			}

		} else if (e.getSource() == btnTeamA){
			if(client!=null){
				client.setTeam(1);
				btnTeamA.setIcon(imgiconTeam1_2_sel);
				btnTeamB.setIcon(imgiconTeam2_2);
				btnTeamC.setIcon(imgiconTeam3_2);
			}
		} else if (e.getSource() == btnTeamB){
			if(client!=null){
				client.setTeam(2);
				btnTeamB.setIcon(imgiconTeam2_2_sel);
				btnTeamA.setIcon(imgiconTeam1_2);
				btnTeamC.setIcon(imgiconTeam3_2);
			}
		} else if (e.getSource() == btnTeamC){
			if(client!=null){
				client.setTeam(3);
				btnTeamC.setIcon(imgiconTeam3_2_sel);
				btnTeamB.setIcon(imgiconTeam2_2);
				btnTeamA.setIcon(imgiconTeam1_2);
			}
		}
	}

	public boolean isPlay() {
		return isPlay;
	}

	public void setPlay(boolean isPlay) {
		this.isPlay = isPlay;
	}

	public JButton getBtnStart() {
		return btnStart;
	}

	public JButton getBtnExit() {
		return btnExit;
	}

	public void setClient(GameClient client) {
		this.client = client;
	}

	public void printSystemMessage(String msg) {
		systemMsg.printMessage(msg);
	}

	public void printMessage(String msg) {
		messageArea.printMessage(msg);
	}

	public GameClient getClient() {
		return client;
	}

	public void changeSpeed(Integer speed) {
		comboSpeed.setSelectedItem(speed);
	}

	public void clearMessage() {
		messageArea.clearMessage();
		systemMsg.clearMessage();
	}

	// 추가된 부분
	public void setOpBlockList1(ArrayList<Block> opBlockList) {
		this.opBlockList1 = opBlockList;
	}

	public void setOpBlockList2(ArrayList<Block> opBlockList) {
		this.opBlockList2 = opBlockList;
	}

	public void setOpBlockList3(ArrayList<Block> opBlockList) {
		this.opBlockList3 = opBlockList;
	}

	public void setOpBlockList4(ArrayList<Block> opBlockList) {
		this.opBlockList4 = opBlockList;
	}

	public void setOpIndex1(int index){
		this.opIndex1 = index;
	}	
	public void setOpIndex2(int index){
		this.opIndex2 = index;
	}
	public void setOpIndex3(int index){
		this.opIndex3 = index;
	}
	public void setOpIndex4(int index){
		this.opIndex4 = index;
	}
	// end of 추가된 부분

}
