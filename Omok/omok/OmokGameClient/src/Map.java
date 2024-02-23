import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

import java.awt.event.*;
import java.awt.geom.*;

class Map extends Canvas { // 오목판을 구현하는 클래스
   public static final int BLACK = 1, WHITE = -1; // 흑과 백을 나타내는 상수
   private int[][] map; // 오목판 배열
   private int size; // size는 격자의 가로 또는 세로 개수, 15로 정한다.
   private int cell; // 격자의 크기(pixel)
   private String info = "게임 중지"; // 게임의 진행 상황을 나타내는 문자열
   private int color = BLACK; // 사용자의 돌 색깔

   // true이면 사용자가 돌을 놓을 수 있는 상태를 의미하고,
   // false이면 사용자가 돌을 놓을 수 없는 상태를 의미한다.
   private boolean enable = false;
   private boolean running = false; // 게임이 진행 중인가를 나타내는 변수
   private PrintWriter writer; // 상대편에게 메시지를 전달하기 위한 스트림
   private Graphics gboard, gbuff; // 캔버스와 버퍼를 위한 그래픽스 객체
   private Image buff; // 더블 버퍼링을 위한 버퍼
   private OmokGameClientView client;
   public ArrayList<Point> pointHistory = new ArrayList<Point>();

   Map(OmokGameClientView client) { // 오목판의 생성자(s=15, c=30)
//    this.size=s; this.cell=c;
      this.client = client;
      size = 19;
      cell = 29;
      map = new int[size + 3][]; // 맵의 크기를 정한다.
      for (int i = 0; i < map.length; i++)
         map[i] = new int[size + 3];

      setBackground(new Color(209, 144, 63)); // 오목판의 배경색을 정한다.
      setSize(size * (cell + 1) + size, size * (cell + 1) + size); // 오목판의 크기를 계산한다.

      // 오목판의 마우스 이벤트 처리
      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent me) { // 마우스를 누르면
            if (!enable)
               return; // 사용자가 누를 수 없는 상태이면 빠져 나온다.
            // 마우스의 좌표를 map 좌표로 계산한다.
            int x = (int) Math.round(me.getX() / (double) cell);
            int y = (int) Math.round(me.getY() / (double) cell);
            // 돌이 놓일 수 있는 좌표가 아니면 빠져 나온다.
            if (x == 0 || y == 0 || x == size + 1 || y == size + 1)
               return;

            // 해당 좌표에 다른 돌이 놓여져 있으면 빠져 나온다.
            if (map[x][y] == BLACK || map[x][y] == WHITE)
               return;
            if (color == BLACK) {
               if (threethree(x, y)) {
                  JOptionPane.showMessageDialog(client.getContentPane(), "33 규칙에 위반 됩니다.", "위반 알림",
                        JOptionPane.DEFAULT_OPTION);
                  info = "33 규칙에 위반 됩니다.";
                  repaint(); // 오목판을 그린다.
                  return;
               }
            }
            if (color == BLACK) {
               if (fourfour(x, y)) {
                  JOptionPane.showMessageDialog(client.getContentPane(), "44 규칙에 위반 됩니다.", "위반 알림",
                        JOptionPane.DEFAULT_OPTION);
                  info = "44 규칙에 위반 됩니다.";
                  repaint(); // 오목판을 그린다.
                  return;
               }
            }
            // 상대편에게 놓은 돌의 좌표를 전송한다.
//        writer.println("[STONE]"+x+" "+y);
            ProtocolCode obcm = new ProtocolCode(client.username, "700", color, x, y);
            client.SendObject(obcm);
            pointHistory.add(new Point(x, y));

            map[x][y] = color;

            // 이겼는지 검사한다.
            if (check(new Point(x, y), color)) {
               info = "이겼습니다.";
//          writer.println("[WIN]");
               winGame();
            }

            else
               info = "상대가 두기를 기다립니다.";
            repaint(); // 오목판을 그린다.

            // 사용자가 둘 수 없는 상태로 만든다.
            // 상대편이 두면 enable이 true가 되어 사용자가 둘 수 있게 된다.

            enable = false;
         }
      });
   }

   public void winGame() { // 게임을 멈춘다.
      ProtocolCode obcm1;
      if (color == BLACK)
         obcm1 = new ProtocolCode(client.username, "702", "흑돌 Win!!");
      else
         obcm1 = new ProtocolCode(client.username, "702", "백돌 Win!!");
      client.SendObject(obcm1);
   }

   public boolean isRunning() { // 게임의 진행 상태를 반환한다.
      return running;
   }

   public void startGame(String col) { // 게임을 시작한다.
      running = true;
      if (col.equals("BLACK")) { // 흑이 선택되었을 때
         enable = true;
         color = BLACK;
         info = "게임 시작... 두세요.";
      } else { // 백이 선택되었을 때
         enable = false;
         color = WHITE;
         info = "게임 시작... 기다리세요.";
      }
      repaint();
   }

   public void stopGame() { // 게임을 멈춘다.
      reset(); // 오목판을 초기화한다.
      writer.println("[STOPGAME]"); // 상대편에게 메시지를 보낸다.
      enable = false;
      running = false;
   }

   public void putOpponent(int x, int y) { // 상대편의 돌을 놓는다.
      map[x][y] = color == BLACK ? WHITE : BLACK;
      info = "상대가 두었습니다. 두세요.";
      pointHistory.add(new Point(x, y));
      for (Point i : pointHistory) {
         System.out.println(i);
      }
      System.out.println();
      repaint();
   }

   public void putOpponent2(int x, int y, int color) { // 관전자 입장 : 돌을 놓는다.
      map[x][y] = color == WHITE ? WHITE : BLACK;
      pointHistory.add(new Point(x, y));
      repaint();
   }

   public void setEnable(boolean enable) {
      this.enable = enable;
   }

   public boolean getEnable() {
      return enable;
   }

   public void setWriter(PrintWriter writer) {
      this.writer = writer;
   }

   public void update(Graphics g) { // repaint를 호출하면 자동으로 호출된다.
      paint(g); // paint를 호출한다.
   }

   public void paint(Graphics g) { // 화면을 그린다.
      if (gbuff == null) { // 버퍼가 없으면 버퍼를 만든다.
         buff = createImage(getWidth(), getHeight());
         gbuff = buff.getGraphics();
      }
      drawBoard(g); // 오목판을 그린다.
   }

   protected void removeHistory() { // 기록 초기화
      pointHistory.clear();
   }

   public void reset() { // 오목판을 초기화시킨다.
      for (int i = 0; i < map.length; i++)
         for (int j = 0; j < map[i].length; j++)
            map[i][j] = 0;
      info = "게임 중지";
      repaint();
   }

   private void drawLine() { // 오목판에 선을 긋는다.
      gbuff.setColor(Color.black);
      for (int i = 1; i <= size; i++) {
         gbuff.drawLine(cell, i * cell, cell * size, i * cell);
         gbuff.drawLine(i * cell, cell, i * cell, cell * size);
      }
   }

   private void drawBlack(int x, int y) { // 흑 돌을 (x, y)에 그린다.
      Graphics2D gbuff = (Graphics2D) this.gbuff;
      Image img = Toolkit.getDefaultToolkit().getImage("image/BlackStone.gif");
      // gbuff.drawImage(img,(x-1)*30+17,(y-1)*30+17,this);
      gbuff.drawImage(img, x * cell - cell / 2 + 3, y * cell - cell / 2 + 3, this);

//      Graphics2D gbuff = (Graphics2D) this.gbuff;
//      gbuff.setColor(Color.black);
//      gbuff.fillOval(x * cell - cell / 2, y * cell - cell / 2, cell - 5, cell - 5);
//      gbuff.setColor(Color.white);
//
//      gbuff.drawOval(x * cell - cell / 2, y * cell - cell / 2, cell - 5, cell - 5);
   }

   private void drawWhite(int x, int y) { // 백 돌을 (x, y)에 그린다.
      Graphics2D gbuff = (Graphics2D) this.gbuff;
      Image img = Toolkit.getDefaultToolkit().getImage("image/WhiteStone.gif");
      // gbuff.drawImage(img,(x-1)*30+17,(y-1)*30+17,this);
      gbuff.drawImage(img, x * cell - cell / 2 + 3, y * cell - cell / 2 + 3, this);

   }

   private void drawStones() { // map 놓여진 돌들을 모두 그린다.
      for (int x = 1; x <= size; x++)
         for (int y = 1; y <= size; y++) {
            if (map[x][y] == BLACK)
               drawBlack(x, y);
            else if (map[x][y] == WHITE)
               drawWhite(x, y);
         }
   }

   synchronized private void drawBoard(Graphics g) { // 오목판을 그린다.
      // 버퍼에 먼저 그리고 버퍼의 이미지를 오목판에 그린다.
      gbuff.clearRect(0, 0, getWidth(), getHeight());
      drawLine();
      drawStones();
      gbuff.setColor(Color.white);
      gbuff.setFont(new Font("이텔릭체", Font.ITALIC, 16));
      gbuff.drawString(info, 20, 580);
      g.drawImage(buff, 0, 0, this);
   }

   private boolean check(Point p, int col) {
      if (count(p, 1, 0, col) + count(p, -1, 0, col) == 4)
         return true;
      if (count(p, 0, 1, col) + count(p, 0, -1, col) == 4)
         return true;
      if (count(p, -1, -1, col) + count(p, 1, 1, col) == 4)
         return true;
      if (count(p, 1, -1, col) + count(p, -1, 1, col) == 4)

         return true;
      return false;
   }

   private int count(Point p, int dx, int dy, int col) {
      int i = 0;
      for (; map[p.x + (i + 1) * dx][p.y + (i + 1) * dy] == col; i++)
         ;
      return i;
   }

   public void backmove() {
      Point lastLocation = pointHistory.remove(pointHistory.size() - 1);
      map[(int) lastLocation.getX()][(int) lastLocation.getY()] = 0;
      repaint();
      if (!enable) {
         setEnable(true);
      } else {
         setEnable(false);
      }
   }

   private boolean threethree(int x, int y) {
      int count = 0;
      count = count + find1(x, y);
      // System.out.println(count);
      count = count + find2(x, y);
      // System.out.println(count);
      count = count + find3(x, y);
      // System.out.println(count);
      count = count + find4(x, y);
      // System.out.println(count);
      if (count >= 2) {
         return true;
      }
      return false;
   }

   private boolean fourfour(int x, int y) {
      int count = 0;
      count = count + find5(x, y);
      // System.out.println(count);
      count = count + find6(x, y);
      // System.out.println(count);
      count = count + find7(x, y);
      // System.out.println(count);
      count = count + find8(x, y);
      // System.out.println(count);
      if (count >= 2) {
         return true;
      }
      return false;
   }

   private int find1(int x, int y) {
      int leftstone = 0;
      int rightstone = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int x1 = x - 1;

      while (true) {

         // 좌표끝도달
         if (x1 == 0) {
            break;
         }
         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x1][y] == 1) {
            check = false;
            leftstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y] == -1) {
            System.out.println("상대돌 만남");
            break;
         }

         if (map[x1][y] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         x1--;
      }

      // →
      x1 = x + 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (x1 == 20) {
            break;
         }
         if (map[x1][y] == BLACK) {
            check = false;
            rightstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y] == WHITE) {
            break;
         }
         if (map[x1][y] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         x1++;
      }
      allStone = leftstone + rightstone;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone != 2) {
         return 0;
      }
      // 돌갯수가 3이면 열린 3인지 파악.

      int left = (leftstone + blink1);
      int right = (rightstone + blink2);
      // 벽으로 막힌경우 - 열린3이 아님
      if (x - left <= 1 || x + right >= 19) {
         return 0;
      } else // 상대돌로 막힌경우 - 열린3이 아님
      if (map[x - left - 1][y] == -1 || map[x + right][y] == -1) {
         System.out.println("상대돌이있네용");
         return 0;
      } else {
         return 1; // 열린3 일때 1 리턴
      }
   }

   private int find2(int x, int y) {
      int upstone = 0;
      int downstone = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int y1 = y + 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (y1 == 20) {
            break;
         }
         if (map[x][y1] == 1) {
            check = false;
            downstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x][y1] == -1) {
            break;
         }

         if (map[x][y1] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         y1++;
      }

      y1 = y - 1;

      while (true) {

         // 좌표끝도달
         if (y1 == 0) {
            break;
         }

         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x][y1] == 1) {
            check = false;
            upstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x][y1] == -1) {
            break;
         }

         if (map[x][y1] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         y1--;
      }

      // →

      allStone = upstone + downstone;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone != 2) {
         return 0;
      }
      // 돌갯수가 3이면 열린 3인지 파악.

      int down = (downstone + blink1);
      int up = (upstone + blink2);

      // 벽으로 막힌경우 - 열린3이 아님
      if (y - up <= 1 || y + down >= 19) {
         return 0;
      } else // 상대돌로 막힌경우 - 열린3이 아님
      if (map[x][y - up] == -1 || map[x][y + down + 1] == -1) {
         System.out.println("상대돌이있네용");
         return 0;
      } else {
         return 1; // 열린3 일때 1 리턴
      }
   }

   private int find3(int x, int y) {
      int diagonalstone1 = 0;
      int diagonalstone2 = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int x1 = x - 1;
      int y1 = y - 1;

      while (true) {

         // 좌표끝도달
         if (x1 == 0 || y1 == 0) {
            break;
         }
         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone1++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }

         if (map[x1][y1] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         x1--;
         y1--;
      }

      // →
      x1 = x + 1;
      y1 = y + 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (x1 == 20 || y1 == 20) {
            break;
         }
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone2++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }
         if (map[x1][y1] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         x1++;
         y1++;
      }

      allStone = diagonalstone1 + diagonalstone2;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone != 2) {
         return 0;
      }
      // 돌갯수가 3이면 열린 3인지 파악.

      int diagonal1 = (diagonalstone1 + blink1);
      int diagonal2 = (diagonalstone2 + blink2);

      // 벽으로 막힌경우 - 열린3이 아님
      if (x - diagonal1 <= 1 || y - diagonal1 <= 1 || x + diagonal2 >= 19 || y + diagonal2 >= 19) {
         return 0;
      } else // 상대돌로 막힌경우 - 열린3이 아님
      if (map[x - diagonal1 - 1][y - diagonal1 - 1] == -1 || map[x + diagonal2][y + diagonal2] == -1) {
         System.out.println("상대돌이있네용");
         return 0;
      } else {
         return 1; // 열린3 일때 1 리턴
      }
   }

   private int find4(int x, int y) {
      int diagonalstone1 = 0;
      int diagonalstone2 = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int x1 = x - 1;
      int y1 = y + 1;

      while (true) {

         // 좌표끝도달
         if (x1 == 0 || y1 == 20) {
            break;
         }
         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone1++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }
         if (map[x1][y1] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         x1--;
         y1++;
      }

      // →
      x1 = x + 1;
      y1 = y - 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (x1 == 20 || y1 == 0) {
            break;
         }
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone2++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }

         if (map[x1][y1] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         x1++;
         y1--;
      }

      allStone = diagonalstone1 + diagonalstone2;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone != 2) {
         return 0;
      }
      // 돌갯수가 3이면 열린 3인지 파악.

      int diagonal1 = (diagonalstone1 + blink1);
      int diagonal2 = (diagonalstone2 + blink2);

      // 벽으로 막힌경우 - 열린3이 아님
      if (x - diagonal1 <= 1 || y + diagonal1 >= 19 || x + diagonal2 >= 19 || y - diagonal2 <= 1) {
         return 0;
      } else // 상대돌로 막힌경우 - 열린3이 아님
      if (map[x - diagonal1 - 1][y + diagonal1 + 1] == -1 || map[x + diagonal2][y - diagonal2] == -1) {
         return 0;
      } else {
         return 1; // 열린3 일때 1 리턴
      }
   }

   private int find5(int x, int y) {
      int leftstone = 0;
      int rightstone = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int x1 = x - 1;

      while (true) {

         // 좌표끝도달
         if (x1 == 0) {
            break;
         }
         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x1][y] == 1) {
            check = false;
            leftstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y] == -1) {
            System.out.println("상대돌 만남");
            break;
         }

         if (map[x1][y] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         x1--;
      }

      // →
      x1 = x + 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (x1 == 20) {
            break;
         }
         if (map[x1][y] == BLACK) {
            check = false;
            rightstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y] == WHITE) {
            break;
         }
         if (map[x1][y] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         x1++;
      }
      allStone = leftstone + rightstone;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone == 3) {
         return 1;
      } else {
         return 0;
      }
   }

   private int find6(int x, int y) {
      int upstone = 0;
      int downstone = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int y1 = y + 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (y1 == 20) {
            break;
         }
         if (map[x][y1] == 1) {
            check = false;
            downstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x][y1] == -1) {
            break;
         }

         if (map[x][y1] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         y1++;
      }

      y1 = y - 1;

      while (true) {

         // 좌표끝도달
         if (y1 == 0) {
            break;
         }

         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x][y1] == 1) {
            check = false;
            upstone++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x][y1] == -1) {
            break;
         }

         if (map[x][y1] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         y1--;
      }

      // →

      allStone = upstone + downstone;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone == 3) {
         return 1;
      } else {
         return 0;
      }
   }

   private int find7(int x, int y) {
      int diagonalstone1 = 0;
      int diagonalstone2 = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int x1 = x - 1;
      int y1 = y - 1;

      while (true) {

         // 좌표끝도달
         if (x1 == 0 || y1 == 0) {
            break;
         }
         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone1++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }

         if (map[x1][y1] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         x1--;
         y1--;
      }

      // →
      x1 = x + 1;
      y1 = y + 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (x1 == 20 || y1 == 20) {
            break;
         }
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone2++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }
         if (map[x1][y1] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         x1++;
         y1++;
      }

      allStone = diagonalstone1 + diagonalstone2;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone == 3) {
         return 1;
      } else {
         return 0;
      }
   }

   private int find8(int x, int y) {
      int diagonalstone1 = 0;
      int diagonalstone2 = 0;
      int allStone = 0;
      int blink1 = 1;
      boolean check = false;
      int x1 = x - 1;
      int y1 = y + 1;

      while (true) {

         // 좌표끝도달
         if (x1 == 0 || y1 == 20) {
            break;
         }
         // check를 false로 바꿈으로 두번연속으로 만나는지 확인할수있게.
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone1++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }
         if (map[x1][y1] == 0) {
            // 처음 빈공간을만나 check가 true가 됬는데
            // 연달아 빈공간을만나면 탐색중지
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink1++;
               break;
            }

            if (blink1 == 1) {
               blink1--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         // 계속탐색
         x1--;
         y1++;
      }

      // →
      x1 = x + 1;
      y1 = y - 1; // 달라지는 좌표
      int blink2 = blink1; // blink1남은거만큼 blink2,
      if (blink1 == 1) // 빈공간을 만나지않은경우 없었음을기록
         blink1 = 0;
      check = false;
      while (true) {
         // 좌표끝도달
         if (x1 == 20 || y1 == 0) {
            break;
         }
         if (map[x1][y1] == 1) {
            check = false;
            diagonalstone2++;
         }

         // 상대돌을 만나면 탐색중지
         if (map[x1][y1] == -1) {
            break;
         }

         if (map[x1][y1] == 0) {
            // 두번연속으로 빈공간만날시 blink카운트를 되돌림.
            if (check == false) {
               check = true;
            } else {
               blink2++;
               break;
            }

            if (blink2 == 1) {
               blink2--;
            } else {
               break; // 빈공간을만났으나 빈공간을 두번만나면 끝임
            }
         }
         x1++;
         y1--;
      }

      allStone = diagonalstone1 + diagonalstone2;
      // 삼삼이므로 돌갯수가 2 + 1(현재돌)이아니면 0리턴
      // 이부분이 43을 허용하게해줌. 33만 찾게됨
      if (allStone == 3) {
         return 1;
      } else {
         return 0;
      }
   }
}