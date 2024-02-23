
import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

@SuppressWarnings("unused")
public class OmokGameServer extends JFrame {

   private static final long serialVersionUID = 1L;
   private JPanel contentPane;
   JTextArea textArea;
   private JTextField txtPortNumber;

   private ServerSocket socket; // 서버소켓
   private Socket client_socket; // accept() 에서 생성된 client 소켓
   private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
   private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
   private Vector<String> vc = new Vector<String>(); // 방을 저장할 벡터

   /**
    * Launch the application.
    */
   public static void main(String[] args) {
      EventQueue.invokeLater(new Runnable() {
         public void run() {
            try {
               OmokGameServer frame = new OmokGameServer();
               frame.setVisible(true);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });
   }

   /**
    * Create the frame.
    */
   public OmokGameServer() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 338, 440);
      contentPane = new JPanel();
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBounds(12, 10, 300, 298);
      contentPane.add(scrollPane);

      textArea = new JTextArea();
      textArea.setEditable(false);
      scrollPane.setViewportView(textArea);

      JLabel lblNewLabel = new JLabel("Port Number");
      lblNewLabel.setBounds(13, 318, 87, 26);
      contentPane.add(lblNewLabel);

      txtPortNumber = new JTextField();
      txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
      txtPortNumber.setText("30000");
      txtPortNumber.setBounds(112, 318, 199, 26);
      contentPane.add(txtPortNumber);
      txtPortNumber.setColumns(10);

      JButton btnServerStart = new JButton("Server Start");
      btnServerStart.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
            } catch (NumberFormatException | IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            AppendText("Chat Server Running..");
            btnServerStart.setText("Chat Server Running..");
            btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
            txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
            AcceptServer accept_server = new AcceptServer();
            accept_server.start();
         }
      });
      btnServerStart.setBounds(12, 356, 300, 35);
      contentPane.add(btnServerStart);
   }

   // 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
   class AcceptServer extends Thread {
      @SuppressWarnings("unchecked")
      public void run() {
         while (true) { // 사용자 접속을 계속해서 받기 위해 while문
            try {
               AppendText("Waiting new clients ...");
               client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
               AppendText("새로운 참가자 from " + client_socket);
               // User 당 하나씩 Thread 생성
               UserService new_user = new UserService(client_socket);
               UserVec.add(new_user); // 새로운 참가자 배열에 추가
               new_user.start(); // 만든 객체의 스레드 실행
               AppendText("현재 참가자 수 " + UserVec.size());
            } catch (IOException e) {
               AppendText("accept() error");
               // System.exit(0);
            }
         }
      }
   }

   public void AppendText(String str) {
      // textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
      textArea.append(str + "\n");
      textArea.setCaretPosition(textArea.getText().length());
   }

   public void AppendObject(ProtocolCode msg) {
      // textArea.append("사용자로부터 들어온 object : " + str+"\n");
      textArea.append("code = " + msg.code + "\n");
      textArea.append("id = " + msg.UserName + "\n");
      textArea.append("data = " + msg.data + "\n");
      textArea.setCaretPosition(textArea.getText().length());
   }

   class Room {
      private String roomName;
      private String userList;

      public Room(String roomName, String userList) {
         this.roomName = roomName;
         this.setUserList(userList);
      }

      public String getUserList() {
         return userList;
      }

      public void setUserList(String userList) {
         this.userList = userList;
      }

   }

   // User 당 생성되는 Thread
   // Read One 에서 대기 -> Write All
   class UserService extends Thread {
      private InputStream is;
      private OutputStream os;
      private DataInputStream dis;
      private DataOutputStream dos;

      private ObjectInputStream ois;
      private ObjectOutputStream oos;

      private Socket client_socket;
      private Vector user_vc;
      public String UserName = "";
      public String UserStatus;
      public String state = "M";
      private boolean readyMember = false; // 준비 여부
      private boolean watching = false; // 관전자 여부
      private boolean watchingAble = true; // 관전가능 여부
      private boolean roomMaster = false; // 방장여부
      private boolean gameState = false; // 게임 진행 여부

      public UserService(Socket client_socket) {
         // TODO Auto-generated constructor stub
         // 매개변수로 넘어온 자료 저장
         this.client_socket = client_socket;
         this.user_vc = UserVec;
         try {
            oos = new ObjectOutputStream(client_socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(client_socket.getInputStream());

         } catch (Exception e) {
            AppendText("userService error");
         }
      }

      public void Login() {
         AppendText("새로운 참가자 " + UserName + " 입장.");
         WriteOne("Welcome to Omok Game server\n");
         WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
         String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
         WriteOthers(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
      }

      public void Logout() {
         String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
         UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
         WriteRoomAll(msg, "M"); // 나를 제외한 다른 User들에게 전송
         AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
      }

      // 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
      public void WriteAll(String str) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.UserStatus == "O")
               user.WriteOne(str);
         }
      }

      // 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
      public void WriteAllObject(Object ob) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.UserStatus == "O")
               user.WriteOneObject(ob);
         }
      }

      // 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
      public void WriteOthers(String str) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.state=="M")
               user.WriteOne(str);
         }
      }

      public void WriteOthersObject(Object ob) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.UserStatus == "O")
               user.WriteOneObject(ob);
         }
      }
      
      //방장이 방을 나갔을 때 방에 있던 유저들 초기화 
      public void RoomUserResetObject(Object ob, String data) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.state.matches(data)) {
               user.state = "M";
               user.watching = false;
               user.readyMember = false;
               user.gameState = false;
            }
         }
      }

      // UserService Thread가 담당하는 Client 에게 1:1 전송
      public void WriteOne(String msg) {
         try {
            // dos.writeUTF(msg);
//            byte[] bb;
//            bb = MakePacket(msg);
//            dos.write(bb, 0, bb.length);
            ProtocolCode obcm = new ProtocolCode("SERVER", "200", msg);
            oos.writeObject(obcm);
         } catch (IOException e) {
            AppendText("dos.writeObject() error");
            try {
//               dos.close();
//               dis.close();
               ois.close();
               oos.close();
               client_socket.close();
               client_socket = null;
               ois = null;
               oos = null;
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            Logout(); // 에러가난 현재 객체를 벡터에서 지운다
         }
      }

      // 귓속말 전송
      public void WritePrivate(String msg) {
         try {
            ProtocolCode obcm = new ProtocolCode("귓속말", "200", msg);
            oos.writeObject(obcm);
         } catch (IOException e) {
            AppendText("dos.writeObject() error");
            try {
               oos.close();
               client_socket.close();
               client_socket = null;
               ois = null;
               oos = null;
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            Logout(); // 에러가난 현재 객체를 벡터에서 지운다
         }
      }

      // UserService Thread가 담당하는 Client 에게 1:1 전송 색 전송
      public void WriteColor(String msg) {
         try {
            ProtocolCode obcm = new ProtocolCode(UserName, "701", msg);
            oos.writeObject(obcm);
         } catch (IOException e) {
            AppendText("dos.writeObject() error");
            try {
//                     dos.close();
//                     dis.close();
               ois.close();
               oos.close();
               client_socket.close();
               client_socket = null;
               ois = null;
               oos = null;
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            Logout(); // 에러가난 현재 객체를 벡터에서 지운다
         }
      }

      public void WriteOneObject(Object ob) {
         try {
            oos.writeObject(ob);
         } catch (IOException e) {
            AppendText("oos.writeObject(ob) error");
            try {
               ois.close();
               oos.close();
               client_socket.close();
               client_socket = null;
               ois = null;
               oos = null;
            } catch (IOException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
            Logout();
         }
      }

      // 강퇴할 User에게 Object를 방송.
      public void WriteRoomOneObject(Object ob, String name) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.state.matches(state) && user.UserName.matches(name)) {
               user.state = "M";
               user.readyMember = false;
               user.gameState = false;
               user.watching = false;
               user.WriteOneObject(ob);
            }
         }
      }

      // 같은 방 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
      public void WriteRoomAllObject(Object ob, String data) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.state.matches(data))
               user.WriteOneObject(ob);
         }
      }

      // 나를 제외한 같은 방 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
      public void WriteRoomOthersObject(Object ob, String data) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.state.matches(data))
               user.WriteOneObject(ob);
         }
      }
      
      // 나를 제외한 같은 방 한 User들에게 Object를 방송. 플레이어 체인지
      public void WriteRoomOthersOneObject(Object ob, String data) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.state.matches(data) && user.watching) {
               user.watching = false;
               ProtocolCode cm = new ProtocolCode(user.UserName, "405", "CHANGE PLAYER");
               user.WriteOneObject(cm);
               break;
            }
         }
      }

      // 로비에 있는 유저에게
      public void WriteRoomListObject(Object ob) {
         String userList = "";
         for (int i = 0; i < vc.size(); i++) {
            if (i == 0) {
               userList = vc.get(i);
               continue;
            }
            userList = userList + " " + vc.get(i);
         }
         ProtocolCode obcmpc = new ProtocolCode(UserName, "502", userList);
         WriteOneObject(obcmpc);
      }

      // 방안에 있는 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
      public void WriteRoomAll(String str, String room) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user.state.matches(room))
               user.WriteOne(str);
         }
      }

      // 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
      public void WriteRoomOthers(String str, String room) {
         for (int i = 0; i < user_vc.size(); i++) {
            UserService user = (UserService) user_vc.elementAt(i);
            if (user != this && user.state.matches(room))
               user.WriteOne(str);
         }
      }

      public void run() {
         while (true) { // 사용자 접속을 계속해서 받기 위해 while문
            try {
               Object obcm = null;
               String msg = null;
               ProtocolCode cm = null;
               if (socket == null)
                  break;
               try {
                  obcm = ois.readObject();
               } catch (ClassNotFoundException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  return;
               }
               if (obcm == null)
                  break;
               if (obcm instanceof ProtocolCode) {
                  cm = (ProtocolCode) obcm;
                  AppendObject(cm);
               } else
                  continue;
               if (cm.code.matches("100")) {
                  UserName = cm.UserName;
                  UserStatus = "O"; // Online 상태
                  Login();
                  WriteRoomListObject(cm);
               } else if (cm.code.matches("101")) { // logout message 처리
                  Logout();
                  break;
               } else if (cm.code.matches("200")) {
                  msg = String.format("[%s] %s", cm.UserName, cm.data);
                  AppendText(msg); // server 화면에 출력
                  String[] args = msg.split(" "); // 단어들을 분리한다.
                  if (args.length == 1) { // Enter key 만 들어온 경우 Wakeup 처리만 한다.
                     UserStatus = "O";
                  } else { // 일반 채팅 메시지
                     UserStatus = "O";
                     // WriteAll(msg + "\n"); // Write All
                     WriteRoomAllObject(cm, state);
                  }
               } else if (cm.code.matches("201")) { // 이모티콘
                  WriteRoomOthersObject(cm, state);
               } else if (cm.code.matches("300")) { // 준비
                  readyMember = !readyMember ? true : false;
                  int count = 0;
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if (user.state.matches(cm.data)) {
                        if (user.readyMember) {
                           count++;
                        }
                     }
                  }
                  if (count != 2 && count != 0) {   // 처음으로 준비를 눌렀을 때
                     WriteOne("상대방이 준비할때까지 기다려주세요");
                     cm = new ProtocolCode(cm.UserName, "703", "white");
                     WriteRoomAllObject(cm, state);
                     WriteColor("white");
                  }
                  if (count == 2) {      // 두명 모두 준비눌렀을 시
                     WriteColor("black");
                     cm = new ProtocolCode(cm.UserName, "704", "black");
                     WriteRoomAllObject(cm, state);
                     WriteRoomAll("게임 시작!", state);
                     gameState = true;
                  }
                  if (count == 0) {      // 준비 취소
                     cm = new ProtocolCode(cm.UserName, "705", "readyfalse");
                     WriteRoomAllObject(cm, state);
                     gameState = false;
                  }
               } else if (cm.code.matches("301")) { // 강퇴
                  String exitMsg = "[" + cm.UserName + "]님이 강퇴당하였습니다.\n";
                  WriteRoomAll(exitMsg, state);
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if(user.state.matches(state) && user.UserName.matches(cm.data)) {
                        if(!user.watching)
                           WriteRoomOthersOneObject(cm, user.state);
                     }
                  }
                  WriteRoomOneObject(cm, cm.data);
                  
                  
               } else if (cm.code.matches("302")) { // 기권
                  readyMember = false;
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if (user.state.matches(state)) {
                        user.readyMember = false;
                        user.gameState = false;
                     }
                  }
                  WriteRoomAllObject(cm, state);
                  WriteRoomAll(cm.data, state);
               } else if (cm.code.matches("400")) { // 나가기
                  state = "M";
                  readyMember = false;               
                  String exitMsg = "[" + cm.UserName + "]님이 퇴장 하였습니다.\n";
                  WriteRoomAll(exitMsg, cm.data);
                  WriteRoomOthersObject(cm, cm.data);
                  if(!watching)
                     WriteRoomOthersOneObject(cm, cm.data);
                  watching = false;
                  
               } else if (cm.code.matches("401")) { // 방장 나가기
                  state = "M";
                  readyMember = false;
                  watching = false;
                  roomMaster = false;
                  watchingAble = true;
                  int index = vc.indexOf(cm.data);
                  vc.remove(index);
                  WriteOthersObject(cm);
                  RoomUserResetObject(cm, cm.data);
               } else if (cm.code.matches("500")) { // 방만들기 관전 o
                  roomMaster = true;
                  vc.add(cm.data);
                  state = cm.data;
                  WriteOthersObject(cm);
               } else if (cm.code.matches("510")) { // 방만들기 관전x
                  roomMaster = true;
                  watchingAble = false;
                  cm.code = "500";
                  vc.add(cm.data);
                  state = cm.data;
                  WriteOthersObject(cm);
               } else if (cm.code.matches("501")) { // 방 접속
                  int count = 0;
                  int n =0;
                  boolean can = true;
                  boolean gameStart = false;
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if (user.state.matches(cm.data)) {
                        count++;
                        if (user.gameState)
                           gameStart = true;
                        if (user.roomMaster && user.watchingAble == false)
                           can = false;
                        if(user.roomMaster) 
                           n = i;
                     }
                  }
                  state = cm.data;
                  if (count >= 2) {
                     if (!can) {
                        state = "M";
                        ProtocolCode cm2 = new ProtocolCode(cm.UserName, "403", "watching false");
                        WriteOneObject(cm2);
                     } else if (gameStart){
                        state = "M";
                        ProtocolCode cm2 = new ProtocolCode(cm.UserName, "404", "watching false");
                        WriteOneObject(cm2);
                     }
                     else {
                        String connectMsg = "[" + UserName + "]님이 입장 하였습니다.\n";
                        WriteRoomOthers(connectMsg, cm.data); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
                        watching = true;
                        ProtocolCode cm2 = new ProtocolCode(cm.UserName, "402", "watching");
                        WriteOneObject(cm2);
                        UserService user = (UserService) user_vc.elementAt(n);
                        user.WriteOneObject(cm2);
                     }
                  } else {
                     String connectMsg = "[" + UserName + "]님이 입장 하였습니다.\n";
                     WriteRoomOthers(connectMsg, cm.data); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
                     WriteRoomAllObject(cm, cm.data);
                  }
               } else if (cm.code.matches("600")) { // 무르기 요청
                  WriteRoomOthersObject(cm, state);
               } else if (cm.code.matches("601")) { // 무르기 수락
                  WriteRoomAllObject(cm, state);
               } else if (cm.code.matches("602")) { // 무르기 거절
                  WriteRoomOthersObject(cm, state);
               } else if (cm.code.matches("700")) { // 착수
                  WriteRoomOthersObject(cm, state);
               } else if (cm.code.matches("702")) { // 게임승리
                  readyMember = false;
                  for (int i = 0; i < user_vc.size(); i++) {
                     UserService user = (UserService) user_vc.elementAt(i);
                     if (user.state.matches(state)) {
                        user.readyMember = false;
                        user.gameState = false;
                     }
                  }
                  WriteRoomAllObject(cm, state);
               } else { // 300, 500, ... 기타 object는 모두 방송한다.
                  WriteAllObject(cm);
               }
            } catch (IOException e) {
               AppendText("ois.readObject() error");
               try {
//                  dos.close();
//                  dis.close();
                  ois.close();
                  oos.close();
                  client_socket.close();
                  Logout(); // 에러가난 현재 객체를 벡터에서 지운다
                  break;
               } catch (Exception ee) {
                  break;
               } // catch문 끝
            } // 바깥 catch문끝
         } // while
      } // run
   }

}