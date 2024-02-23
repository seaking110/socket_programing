
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;
import java.awt.*;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("unused")
public class OmokGameClientView extends JFrame {

   private static final long serialVersionUID = 1L;
   private JPanel contentPane;
   private JTextField txtInput;
//   private String UserName;
   private JButton btnSend;
   private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
   public Socket socket; // 연결소켓

   public ObjectInputStream ois;
   public ObjectOutputStream oos;

   private JLabel lblUserName;
   // private JTextArea textArea;
   private JTextPane textArea;

   private Frame frame;
   private FileDialog fd;
   private JButton imgBtn;

   JPanel panel;
   private JLabel lblMouseEvent;
   private Graphics gc;
   private int pen_size = 2; // minimum 2
   // 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
   private Image panelImage = null;
   private Graphics gc2 = null;
   public OmokGameClientView view;
   private OmokImageButton m_createRoomButton, m_enterRoomButton;
   private OmokImageButton replayButton;
   private OmokImageButton exitOmokButton;
   private Vector<String> vc = new Vector<String>(1); // 방 벡터
   private JScrollPane scPaneRoomList;
   private JScrollPane scPaneRoomList2;
   private JList m_roomList = new JList();
   private Container cp = this.getContentPane();
   protected String username;
   private String ip_addr;
   private String port_no;
   private OmokImageButton readyButton;
   private OmokImageButton backButton;
   private OmokImageButton kickButton;
   private OmokImageButton surrenderButton;
   private OmokImageButton exitButton;
   private OmokImageButton happyButton;
   private OmokImageButton angryButton;
   private OmokImageButton upsetButton;
   private OmokImageButton wowButton;
   private Map map = new Map(this);
   private JPanel m_canvasPanel;
   private JLabel m_gamer1, m_gamer2;
   private boolean count = true; //준비여부
   private String state;
   private String roomName = "";
   private boolean roomMaster = false;
   private boolean watching = false;
   private ArrayList<String> ul = new ArrayList<>(); // userList
   private ArrayList<Point> pointHistory2 = new ArrayList<>(); // 대전 복기용 기록
   protected ArrayList<OmokGameClientReplay> pointHistory3 = new ArrayList<>(); // 대전 선택용
   private JPanel m_roomListPanel = new JPanel() {
      public void paint(Graphics g) {
         this.paintComponents(g);
      }
   };
   private JPanel bgPanel = new JPanel() {
      public void paint(Graphics g) {
         ImageIcon icon = new ImageIcon("image/lobbyBg.jpg");
         g.drawImage(icon.getImage(), 0, 0, 340, 440, null);
         this.paintComponents(g);
      }
   };
   private JPanel omokGamePanel = new JPanel() {
      public void paint(Graphics g) {
         ImageIcon icon = new ImageIcon("image/gameRoomBg.jpg");
         g.drawImage(icon.getImage(), 0, 0, 850, 628, null);
         this.paintComponents(g);
      }
   };
   private JPanel omokGamePanel2 = new JPanel() {
      public void paint(Graphics g) {
         ImageIcon icon = new ImageIcon("image/badook_board.jpg");
         g.drawImage(icon.getImage(), 0, 0, 590, 593, null);
         this.paintComponents(g);
      }
   };

   protected void generatorRoomListPanel() {
      state = "M";
      setResizable(false);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      cp.removeAll();
      m_createRoomButton = new OmokImageButton("image/makeRoomButton.png", "Create Game Room",
            "image/makeRoomButtonOver.png");
      m_enterRoomButton = new OmokImageButton("image/enterRoomButton.png", "Enter Game Room",
            "image/enterRoomButtonOver.png");

      m_roomList.setListData(vc);

      scPaneRoomList = new JScrollPane(m_roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      scPaneRoomList2 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      scPaneRoomList.setBounds(5, 5, 150, 100);
      scPaneRoomList2.setBounds(170, 5, 150, 100);

      m_roomListPanel.add(m_createRoomButton);
      m_createRoomButton.setBounds(10, 110, 65, 44);
      m_roomListPanel.add(m_enterRoomButton);
      m_enterRoomButton.setBounds(85, 110, 65, 44);

      exitOmokButton = new OmokImageButton("./image/exitButton.png", "Eixt Game", "./image/exitButtonOver.png");
      replayButton = new OmokImageButton("./image/reviewButton.png", "Review Game", "./image/reviewButtonOver.png");
      m_roomListPanel.add(exitOmokButton);
      exitOmokButton.setBounds(250, 350, 65, 44);
      m_roomListPanel.add(replayButton);
      replayButton.setBounds(180, 350, 65, 44);

      textArea = new JTextPane();
      textArea.setEditable(false);
      textArea.setFont(new Font("굴림체", Font.PLAIN, 10));
      scPaneRoomList2.setViewportView(textArea);

      m_roomListPanel.setLayout(null);
      m_roomListPanel.setBounds(5, 5, 335, 435);

      m_createRoomButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            String resultStr = null;
            resultStr = JOptionPane.showInputDialog(cp, "방 이름 설정.", username);
            if (resultStr.equals(null) || resultStr.equals("")) {
               JOptionPane.showMessageDialog(cp, "방 이름을 적어야합니다.");
            } else {
               int result = JOptionPane.showConfirmDialog(cp, "관전을 허용하시겠습니까??", "관전 허용 알림",
                     JOptionPane.YES_NO_OPTION);
               if (result == JOptionPane.YES_OPTION) {
                  vc.add(resultStr);
                  roomMaster = true;
                  state = resultStr;
                  ProtocolCode msg = new ProtocolCode(username, "500", resultStr);
                  SendObject(msg);
                  generatorGameRoomPanel();
               } else {
                  vc.add(resultStr);
                  roomMaster = true;
                  state = resultStr;
                  ProtocolCode msg = new ProtocolCode(username, "510", resultStr);
                  SendObject(msg);
                  generatorGameRoomPanel();
               }

            }

         }
      });
      m_enterRoomButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (roomName.matches("")) {
               JOptionPane.showMessageDialog(cp, "방을 선택하십시오.");
            } else {
               state = roomName;
               generatorGameRoomPanel();
               ProtocolCode msg = new ProtocolCode(username, "501", roomName);
               SendObject(msg);
            }
         }
      });
      replayButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            generatorReplayPanel();
         }
      });

      exitOmokButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ProtocolCode msg = new ProtocolCode(username, "101", "Bye");
            SendObject(msg);
            System.exit(0);
         }
      });

      m_roomList.addListSelectionListener(new ListSelectionListener() {
         @Override
         public void valueChanged(ListSelectionEvent e) {
            if (m_roomList.getSelectedValue() != null) { // getSelectedValue() : 선택된 항목(Object 타입) 반환
               // 하나라도 선택된 경우
               roomName = m_roomList.getSelectedValue().toString();

            } else {
               roomName = "";
            }
         }
      });

      m_roomListPanel.add(bgPanel);
      bgPanel.setBounds(0, 0, 340, 440);

      cp.add(scPaneRoomList);
      cp.add(scPaneRoomList2);
      cp.add(m_roomListPanel);

   }

   private void generatorHistory() {
      pointHistory2.clear();
      for (int i = 0; i < map.pointHistory.size(); i++) {
         pointHistory2.add(map.pointHistory.get(i));
      }
      pointHistory3.add(new OmokGameClientReplay(map.pointHistory, this));
      map.removeHistory();
   }

   private void generatorReplayPanel() {
      String[] index = new String[pointHistory3.size()];
      for (int i = 1; i <= pointHistory3.size(); i++) {
         index[i - 1] = i + "번째 대전";
      }
      Object result = JOptionPane.showInputDialog(cp, "어떤 대전을 복기하시겠습니까?", "복기 알림", JOptionPane.QUESTION_MESSAGE, null,
            index, index[0]);
      if (result != null) {
         int n = result.toString().charAt(0) - '0';
         setResizable(false);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         cp.removeAll();
         omokGamePanel.setBounds(0, 0, 615, 628);
         omokGamePanel2.setBounds(10, 10, 600, 603);
         pointHistory3.get(n - 1).setLocation(10, 13);
         add(pointHistory3.get(n - 1));

         cp.add(omokGamePanel2);
         cp.add(omokGamePanel);

         setLayout(null);
         this.setSize(618, 650);
         this.setVisible(true);
      }
   }

   private JPanel generatorGameRoomPanel() {
      setResizable(false);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      cp.removeAll();
      omokGamePanel.setBounds(0, 0, 850, 628);
      omokGamePanel2.setBounds(10, 10, 600, 603);

      scPaneRoomList = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scPaneRoomList.setBounds(612, 330, 220, 250);
      textArea = new JTextPane();
      textArea.setEditable(false);
      textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
      scPaneRoomList.setViewportView(textArea);

      txtInput = new JTextField();
      txtInput.setBounds(612, 580, 160, 20);
      cp.add(txtInput);
      txtInput.setColumns(10);

      btnSend = new JButton("Send");
      btnSend.setFont(new Font("굴림", Font.PLAIN, 10));
      btnSend.setBounds(772, 580, 60, 20);
      cp.add(btnSend);
      cp.add(scPaneRoomList);
      Font font = new Font("serif", Font.BOLD, 20);
      m_gamer1 = new JLabel();
      m_gamer1.setBounds(627, 120, 150, 25);
      m_gamer1.setFont(font);
      cp.add(m_gamer1);
      m_gamer2 = new JLabel();
      m_gamer2.setBounds(767, 120, 150, 25);
      m_gamer2.setFont(font);
      cp.add(m_gamer2);
      readyButton = new OmokImageButton("./image/readyButton.png", "READY", "./image/readyButtonOver.png");
      readyButton.setBounds(650, 180, 65, 44);
      cp.add(readyButton);
      backButton = new OmokImageButton("./image/moveBackButton.png", "BACK", "./image/moveBackButtonOver.png");
      backButton.setBounds(728, 180, 65, 44);
      cp.add(backButton);
      surrenderButton = new OmokImageButton("./image/surrenderButton.png", "SURRENDER",
            "./image/surrenderButtonOver.png");
      surrenderButton.setBounds(612, 228, 65, 44);
      cp.add(surrenderButton);
      surrenderButton.setEnabled(false);
      kickButton = new OmokImageButton("./image/forcedExitButton.png", "FORCEDEXIT",
            "./image/forcedExitButtonOver.png");
      kickButton.setBounds(690, 228, 65, 44);
      cp.add(kickButton);
      exitButton = new OmokImageButton("./image/exitButton.png", "EXIT", "./image/exitButtonOver.png");
      exitButton.setBounds(767, 228, 65, 44);
      cp.add(exitButton);
      happyButton = new OmokImageButton("./image/happy.png", "HAPPY");
      happyButton.setBounds(625, 280, 44, 44);
      cp.add(happyButton);
      angryButton = new OmokImageButton("./image/angry.png", "ANGRY");
      angryButton.setBounds(674, 280, 44, 44);
      cp.add(angryButton);
      wowButton = new OmokImageButton("./image/wow.png", "WOW");
      wowButton.setBounds(723, 280, 44, 44);
      cp.add(wowButton);
      upsetButton = new OmokImageButton("./image/upset.png", "UPSET");
      upsetButton.setBounds(772, 280, 44, 44);
      cp.add(upsetButton);

      JPanel panel = new JPanel() {
         Image background = new ImageIcon("./image/bg2.JPG").getImage();

         public void paint(Graphics g) {// 그리는 함수
            g.drawImage(background, 0, 0, null);// background를 그려줌
         }
      };
      panel.setBounds(607, 10, 230, 320);
      cp.add(panel);

//      ListenNetwork net = new ListenNetwork();
//      net.start();
      TextSendAction action = new TextSendAction();
      btnSend.addActionListener(action);
      txtInput.addActionListener(action);
      txtInput.requestFocus();

      readyButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (count) { // true
               ProtocolCode obcm = new ProtocolCode(username, "300", state);
               SendObject(obcm);
               count = false;
               readyButton.setImage("./image/readyCancelButton.png");
               exitButton.setEnabled(false);
            } else { // false
               AppendTextR("준비 취소했습니다.");
               ProtocolCode obcm = new ProtocolCode(username, "300", state);
               SendObject(obcm);
               count = true;
               readyButton.setImage("./image/readyButton.png");
               exitButton.setEnabled(true);
            }
         }
      });
      backButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (map.getEnable() == false && map.pointHistory.size() >= 1) {
               int result = JOptionPane.showConfirmDialog(cp, "무르기 요청을 보내시겠습니까?", "알림", JOptionPane.YES_NO_OPTION);
               if (result == JOptionPane.YES_OPTION) {
                  ProtocolCode obcm = new ProtocolCode(username, "600", "moveback");
                  SendObject(obcm);
               }
            } else {
               AppendText("무르기는 1턴이후 혹은 상대턴에만 가능합니다");
            }

         }
      });
      kickButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (roomMaster == true) {
               String[] selections = new String[ul.size()];
               for (int i = 0; i < ul.size(); i++) {
                  selections[i] = ul.get(i);
               }
               Object result = JOptionPane.showInputDialog(cp, "누구를 강퇴하겠습니까?", "강퇴 알림",
                     JOptionPane.QUESTION_MESSAGE, null, selections, selections[0]);
               if (result != null) {
                  ProtocolCode obcm = new ProtocolCode(username, "301", (String) result);
                  SendObject(obcm);
                  for (int i = 0; i < ul.size(); i++) {
                     if (ul.get(i).matches((String) result))
                        ul.remove(i);
                  }
               }
            } else {
               AppendText("강퇴는 방장만 가능합니다");
            }

         }
      });
      surrenderButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(cp, "정말 기권하시겠습니까?", "알림", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
               map.reset();
               map.setEnable(false);
               SendMessage(username + "님이 기권하였습니다.\n");
               String whoWin = "흑돌 Win!!";
               if (username.matches(m_gamer1.getText()))
                  whoWin = "백돌 Win!!";
               ProtocolCode obcm = new ProtocolCode(username, "302", whoWin);
               SendObject(obcm);
            }
         }
      });
      exitButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (roomMaster) {
               ProtocolCode obcm = new ProtocolCode(username, "401", state);
               SendObject(obcm);
               int index = vc.indexOf(state);
               vc.remove(index);
               map.reset();
               ul.clear();
               roomMaster = false;
            } else {
               ProtocolCode obcm = new ProtocolCode(username, "400", state);
               SendObject(obcm);
            }
            generatorRoomListPanel();
            setSize(340, 440);
            setVisible(true);
         }
      });
      happyButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ImageIcon imoticon = new ImageIcon("./image/happy.png");
            AppendIcon(imoticon);
            AppendTextR("");
            ProtocolCode obcm = new ProtocolCode(username, "201", "HAPPY");
            SendObject(obcm);
         }
      });
      angryButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ImageIcon imoticon = new ImageIcon("./image/angry.png");
            AppendIcon(imoticon);
            AppendTextR("");
            ProtocolCode obcm = new ProtocolCode(username, "201", "ANGRY");
            SendObject(obcm);
         }
      });
      wowButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ImageIcon imoticon = new ImageIcon("./image/wow.png");
            AppendIcon(imoticon);
            AppendTextR("");
            ProtocolCode obcm = new ProtocolCode(username, "201", "WOW");
            SendObject(obcm);
         }
      });
      upsetButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ImageIcon imoticon = new ImageIcon("./image/upset.png");
            AppendIcon(imoticon);
            AppendTextR("");
            ProtocolCode obcm = new ProtocolCode(username, "201", "UPSET");
            SendObject(obcm);
         }
      });
      map.setLocation(10, 13);
      add(map);
      cp.add(omokGamePanel2);
      cp.add(omokGamePanel);

      setLayout(null);

      this.setSize(850, 650);
      this.setVisible(true);
      return omokGamePanel;
   }

   public OmokGameClientView(String username, String ip_addr, String port_no) {
      this.username = username;
      this.ip_addr = ip_addr;
      this.port_no = port_no;
      setBounds(100, 100, 392, 634);

      generatorRoomListPanel();

      this.setSize(340, 440);
      this.setVisible(true);

      try {
         socket = new Socket(ip_addr, Integer.parseInt(port_no));
         oos = new ObjectOutputStream(socket.getOutputStream());
         oos.flush();
         ois = new ObjectInputStream(socket.getInputStream());

//         SendMessage("/login " + UserName);
         ProtocolCode obcm = new ProtocolCode(username, "100", "Hello");
         SendObject(obcm);
//
         ListenNetwork net = new ListenNetwork();
         net.start();

//         btnSend.addActionListener(action);
//         txtInput.addActionListener(action);
//         txtInput.requestFocus();
//         
//         imgBtn.addActionListener(action2);

      } catch (NumberFormatException | IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         System.out.println("connect error");
      }

   }

   // Server Message를 수신해서 화면에 표시
   class ListenNetwork extends Thread {
      public void run() {
         while (true) {
            try {

               Object obcm = null;
               String msg = null;
               ProtocolCode cm;
               try {
                  obcm = ois.readObject();
               } catch (ClassNotFoundException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  break;
               }
               if (obcm == null)
                  break;
               if (obcm instanceof ProtocolCode) {
                  cm = (ProtocolCode) obcm;
                  msg = String.format("[%s]\n%s", cm.UserName, cm.data);
               } else
                  continue;
               switch (cm.code) {
               case "200": // 메세지
                  if (cm.UserName.equals(username))
                     AppendTextR(msg); // 내 메세지는 우측에
                  else if (cm.UserName.equals("SERVER"))
                     AppendText(cm.data);
                  else
                     AppendText(msg);
                  break;
               case "201": // 이모티콘
                  if (cm.data.equals("HAPPY")) {
                     ImageIcon imoticon = new ImageIcon("./image/happy.png");
                     AppendIcon(imoticon);
                     AppendText("");
                  } else if (cm.data.equals("ANGRY")) {
                     ImageIcon imoticon = new ImageIcon("./image/angry.png");
                     AppendIcon(imoticon);
                     AppendText("");
                  } else if (cm.data.equals("WOW")) {
                     ImageIcon imoticon = new ImageIcon("./image/wow.png");
                     AppendIcon(imoticon);
                     AppendText("");
                  } else {
                     ImageIcon imoticon = new ImageIcon("./image/upset.png");
                     AppendIcon(imoticon);
                     AppendText("");
                  }
                  break;
               case "301": // 강퇴
                  generatorRoomListPanel();
                  setSize(340, 440);
                  setVisible(true);
                  JOptionPane.showMessageDialog(cp, "강퇴 당하셨습니다.", "강퇴 알림", JOptionPane.DEFAULT_OPTION);
                  break;
               case "302": // 기권
                  if (!watching) {
                     map.setEnable(false);
                     backButton.setEnabled(false);
                     readyButton.setEnabled(true);
                     surrenderButton.setEnabled(false);
                     exitButton.setEnabled(true);
                     kickButton.setEnabled(true);
                     count = true;
                     generatorHistory();
                  } 
                  else
                     pointHistory2.clear();
                  readyButton.setImage("./image/readyButton.png");
                  m_gamer1.setText("");
                  m_gamer2.setText("");
                  map.reset();
                  JOptionPane.showMessageDialog(cp, cm.data, "기권 알림", JOptionPane.DEFAULT_OPTION);
                  break;
               case "400": // 나가기
                  if (roomMaster) {
                     for (int i = 0; i < ul.size(); i++) {
                        if (ul.get(i).matches(cm.UserName))
                           ul.remove(i);
                     }
                  } else {
                     
                  }
                     break;
               case "401": // 방장 방나가기
                  int index = vc.indexOf(cm.data);
                  vc.remove(index);
                  if (cm.data.matches(state)) {
                     state="M";
                     count=true;
                     map.reset();
                     JOptionPane.showMessageDialog(cp, "방장이 방을 나갔습니다.");
                     generatorRoomListPanel();
                     setSize(340, 440);
                     setVisible(true);
                  } else if (state == "M") {
                     cp.remove(scPaneRoomList);
                     m_roomList.setListData(vc);

                     scPaneRoomList = new JScrollPane(m_roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                     scPaneRoomList.setBounds(5, 5, 150, 100);
                     cp.add(scPaneRoomList);
                  }
                  break;
               case "402": // 관전
                  if (roomMaster == true) { // 만약 방장이라면
                     ul.add(cm.UserName);
                  } else {
                     watching = true;
                     AppendText("당신은 관전자입니다.");
                     readyButton.setEnabled(false);
                     backButton.setEnabled(false);
                     surrenderButton.setEnabled(false);
                     map.setEnable(false);
                  }
                  break;
               case "403": // 관전불가
                  generatorRoomListPanel();
                  setSize(340, 440);
                  setVisible(true);
                  state = "M";
                  JOptionPane.showMessageDialog(cp, "관전이 허용되지 않은 방입니다.", "관전 거부 알림", JOptionPane.DEFAULT_OPTION);
                  break;
               case "404": // 게임 진행중 관전불가
                  generatorRoomListPanel();
                  setSize(340, 440);
                  setVisible(true);
                  state = "M";
                  JOptionPane.showMessageDialog(cp, "게임이 진행중인 방입니다.", "관전 거부 알림", JOptionPane.DEFAULT_OPTION);
                  break;
               case "405": // 관전자 게임 참여
                  watching = false;
                  readyButton.setEnabled(true);
                  AppendText("플레이어로 변경되었습니다!!");
                  break;
               case "500": // 방만들기
                  vc.add(cm.data);
                  if (state.matches("M")) {
                     cp.remove(scPaneRoomList);
                     m_roomList.setListData(vc);

                     scPaneRoomList = new JScrollPane(m_roomList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                     scPaneRoomList.setBounds(5, 5, 150, 100);
                     cp.add(scPaneRoomList);
                  }
                  break;
               case "501": // 방 접속
                  if (roomMaster == true) { // 만약 방장이라면
                     ul.add(cm.UserName);
                  }
                  backButton.setEnabled(false);
                  surrenderButton.setEnabled(false);
                  break;
               case "502": // 방리스트
                  String room[] = cm.data.split(" ");
                  for (int i = 0; i < room.length; i++) {
                     vc.add(room[i]);
                  }
                  if (state.matches("M")) {
                     generatorRoomListPanel();
                     setSize(340, 440);
                     setVisible(true);
                  }
                  break;
               case "600": // 무르기 요청
                  if (watching == false) {
                     int result = JOptionPane.showConfirmDialog(cp, "무르기 요청이 들어왔습니다. 수락하시겠습니까?", "알림",
                           JOptionPane.YES_NO_OPTION);
                     if (result == JOptionPane.YES_OPTION) {
                        cm = new ProtocolCode(cm.UserName, "601", "yes");
                        SendObject(cm);
                     } else {
                        cm = new ProtocolCode(cm.UserName, "602", "no");
                        SendObject(cm);
                     }
                  }
                  break;
               case "601": // 무르기 요청 수락
                  if (watching == false) {
                     JOptionPane.showMessageDialog(cp, "무르기 요청이 수락되었습니다.", "수락 알림", JOptionPane.DEFAULT_OPTION);
                     map.backmove();
                  } else {
                     JOptionPane.showMessageDialog(cp, "무르기가 실행되었습니다.", "무르기 알림", JOptionPane.DEFAULT_OPTION);
                     map.backmove();
                  }
                  break;
               case "602": // 무르기 요청 거절
                  if (watching == false) {
                     JOptionPane.showMessageDialog(cp, "무르기 요청이 거절되었습니다.", "거절 알림", JOptionPane.DEFAULT_OPTION);
                  }
                  break;
               case "700": // 착수
                  if (watching == false) {
                     map.setEnable(true); // 사용자가 돌을 놓을 수 있도록 한다.
                     map.putOpponent(cm.x, cm.y);
                  } else {
                     map.putOpponent2(cm.x, cm.y, cm.color);
                  }
                  break;
               case "701": // 준비완료
                  if (cm.data.matches("black")) {
                     map.startGame("BLACK");
                     m_gamer1.setText(cm.UserName);
                  } else {
                     map.startGame("WHITE");
                     m_gamer2.setText(cm.UserName);
                  }
                  kickButton.setEnabled(false);
                  surrenderButton.setEnabled(true);
                  backButton.setEnabled(true);
                  break;
               case "702": // 게임 승리
                  if(!watching) {
                     generatorHistory();
                     map.setEnable(false);
                     backButton.setEnabled(false);
                     surrenderButton.setEnabled(false);
                     readyButton.setEnabled(true);
                     exitButton.setEnabled(true);
                     kickButton.setEnabled(true);
                     count = true;
                  }
                  else 
                     pointHistory2.clear();
                  readyButton.setImage("./image/readyButton.png");
                  AppendText(cm.data);
                  m_gamer1.setText("");
                  m_gamer2.setText("");
                  map.reset();
                  JOptionPane.showMessageDialog(cp, cm.data, "승리 알림", JOptionPane.DEFAULT_OPTION);
                  break;
               case "703": // 흰색돌 유저 이름 각인
                  m_gamer2.setText(cm.UserName);
                  break;
               case "704":// 깜장돌 유저 이름 각인
                  m_gamer1.setText(cm.UserName);
                  readyButton.setEnabled(false);
                  break;
               case "705": // 흰돌 준비 취소
                  m_gamer2.setText("");
                  break;
               }
            } catch (IOException e) {
               AppendText("ois.readObject() error");
               try {
//                  dos.close();
//                  dis.close();
                  ois.close();
                  oos.close();
                  socket.close();

                  break;
               } catch (Exception ee) {
                  break;
               } // catch문 끝
            } // 바깥 catch문끝

         }
      }
   }

//
   // keyboard enter key 치면 서버로 전송
   class TextSendAction implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         // Send button을 누르거나 메시지 입력하고 Enter key 치면
         if (e.getSource() == btnSend || e.getSource() == txtInput) {
            String msg = null;
            // msg = String.format("[%s] %s\n", UserName, txtInput.getText());
            msg = txtInput.getText();
            SendMessage(msg);
            txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
            txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
            if (msg.contains("/exit")) // 종료 처리
               System.exit(0);
         }
      }
   }

   public void AppendIcon(ImageIcon icon) {
      int len = textArea.getDocument().getLength();
      // 끝으로 이동
      textArea.setCaretPosition(len);
      textArea.insertIcon(icon);
   }

//
   // 화면에 출력
   public void AppendText(String msg) {
      // textArea.append(msg + "\n");
      // AppendIcon(icon1);
      msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
      int len = textArea.getDocument().getLength();
      // 끝으로 이동
      // textArea.setCaretPosition(len);
      // textArea.replaceSelection(msg + "\n");

      StyledDocument doc = textArea.getStyledDocument();
      SimpleAttributeSet left = new SimpleAttributeSet();
      StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
      StyleConstants.setForeground(left, Color.BLACK);
      doc.setParagraphAttributes(doc.getLength(), 1, left, false);
      try {
         doc.insertString(doc.getLength(), msg + "\n", left);
      } catch (BadLocationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      len = textArea.getDocument().getLength();
      textArea.setCaretPosition(len);

   }

   // 화면 우측에 출력
   public void AppendTextR(String msg) {
      msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
      StyledDocument doc = textArea.getStyledDocument();
      SimpleAttributeSet right = new SimpleAttributeSet();
      StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
      StyleConstants.setForeground(right, Color.BLUE);
      doc.setParagraphAttributes(doc.getLength(), 1, right, false);
      try {
         doc.insertString(doc.getLength(), msg + "\n", right);
      } catch (BadLocationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      int len = textArea.getDocument().getLength();
      textArea.setCaretPosition(len);

   }

   public void AppendImage(ImageIcon ori_icon) {

      int len = textArea.getDocument().getLength();
      textArea.setCaretPosition(len); // place caret at the end (with no selection)
      Image ori_img = ori_icon.getImage();
      Image new_img;
      ImageIcon new_icon;
      int width, height;
      double ratio;
      width = ori_icon.getIconWidth();
      height = ori_icon.getIconHeight();
      // Image가 너무 크면 최대 가로 또는 세로 200 기준으로 축소시킨다.
      if (width > 200 || height > 200) {
         if (width > height) { // 가로 사진
            ratio = (double) height / width;
            width = 200;
            height = (int) (width * ratio);
         } else { // 세로 사진
            ratio = (double) width / height;
            height = 200;
            width = (int) (height * ratio);
         }
         new_img = ori_img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
         new_icon = new ImageIcon(new_img);
         textArea.insertIcon(new_icon);
      } else {
         textArea.insertIcon(ori_icon);
         new_img = ori_img;
      }
      len = textArea.getDocument().getLength();
      textArea.setCaretPosition(len);

      gc2.drawImage(ori_img, 0, 0, panel.getWidth(), panel.getHeight(), panel);
      gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);

   }

   // Server에게 network으로 전송
   public void SendMessage(String msg) {
      try {
         // dos.writeUTF(msg);
//         byte[] bb;
//         bb = MakePacket(msg);
//         dos.write(bb, 0, bb.length);
         ProtocolCode obcm = new ProtocolCode(username, "200", msg);
         oos.writeObject(obcm);
      } catch (IOException e) {
         // AppendText("dos.write() error");
         AppendText("oos.writeObject() error");
         try {
//            dos.close();
//            dis.close();
            ois.close();
            oos.close();
            socket.close();
         } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.exit(0);
         }
      }
   }

   public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
      try {
         oos.writeObject(ob);
      } catch (IOException e) {
         // textArea.append("메세지 송신 에러!!\n");
         AppendText("SendObject Error");
      }
   }
}