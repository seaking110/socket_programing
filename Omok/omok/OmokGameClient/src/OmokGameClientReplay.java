import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.util.ArrayList;
@SuppressWarnings("unused")

public class OmokGameClientReplay extends Canvas  {
   public static final int BLACK = 1, WHITE = -1; // 흑과 백을 나타내는 상수
   private int[][] map; // 오목판 배열
   private int size; // size는 격자의 가로 또는 세로 개수, 15로 정한다.
   private int cell; // 격자의 크기(pixel)
   private String info = "오른쪽 방향키 : 다음수 왼쪽 방향키 : 이전수 스페이스 : 종료"; // 게임의 진행 상황을 나타내는 문자열
   private int color = BLACK; // 사용자의 돌 색깔

   // true이면 사용자가 돌을 놓을 수 있는 상태를 의미하고,
   // false이면 사용자가 돌을 놓을 수 없는 상태를 의미한다.
   private boolean enable = false;
   private boolean running = false; // 게임이 진행 중인가를 나타내는 변수
   private PrintWriter writer; // 상대편에게 메시지를 전달하기 위한 스트림
   private Graphics gboard, gbuff; // 캔버스와 버퍼를 위한 그래픽스 객체
   private Image buff; // 더블 버퍼링을 위한 버퍼
   private ArrayList<Point> board = new ArrayList<>();
   private int count;
   private int x, y;
   private OmokGameClientView client;
   private int xx[] = null;
   private int yy[] = null;
   public OmokGameClientReplay(ArrayList<Point> pointHistory2,OmokGameClientView client) {
      this.board = pointHistory2;
      this.client= client;
      size = 19;
      cell = 29;
      count = 0;
      map = new int[size + 3][]; // 맵의 크기를 정한다.
      for (int i = 0; i < map.length; i++)
         map[i] = new int[size + 3];

      setBackground(new Color(209, 144, 63)); // 오목판의 배경색을 정한다.
      setSize(size * (cell + 1) + size, size * (cell + 1) + size);
      xx = new int [board.size()];
      yy = new int [board.size()];
      for(int i=0;i<board.size();i++) {
         xx[i]= board.get(i).x;
         yy[i]= board.get(i).y;
      }
      addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.VK_SPACE) { // 스페이스 키를 눌렀으면
               client.generatorRoomListPanel();
               client.setSize(340, 440);
               client.setVisible(true);
               reset();
               count=0;
            }
            if (keyCode == KeyEvent.VK_LEFT) { // 왼쪽 방향 키
               if (count > 0) {
                  count--;
                  info = "오른쪽 방향키 : 다음수 왼쪽 방향키 : 이전수 스페이스 : 종료";
               } else {

               }
               x=xx[count];
               y=yy[count];
               map[x][y] = 0;
            } else if (keyCode == KeyEvent.VK_RIGHT) { // 오른쪽 방향 키
               if(count>=xx.length) {
                  info = "마지막 돌입니다. 종료하시려면 스페이스를 누르세요";
               }
               else {
                  x=xx[count];
                  y=yy[count];
                  if (count <= xx.length - 1) {
                     if (count % 2 == 0) {
                        map[x][y] = BLACK;
                     } else {
                        map[x][y] = WHITE;
                     }
                     count++;
                  }
               }
               
            }
            repaint();

         }
      });
   }

   synchronized private void drawBoard(Graphics g) { // 오목판을 그린다.
      // 버퍼에 먼저 그리고 버퍼의 이미지를 오목판에 그린다.
      gbuff.clearRect(0, 0, getWidth(), getHeight());
      drawLine();
      drawStones();
      gbuff.setColor(Color.white);
      gbuff.setFont(new Font("굴림체", Font.PLAIN, 16));
      gbuff.drawString(info, 20, 580);
      g.drawImage(buff, 0, 0, this);
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
      gbuff.drawImage(img, x * cell - cell / 2 + 3, y * cell - cell / 2 + 3, this);

   }

   private void drawWhite(int x, int y) { // 백 돌을 (x, y)에 그린다.
      Graphics2D gbuff = (Graphics2D) this.gbuff;
      Image img = Toolkit.getDefaultToolkit().getImage("image/WhiteStone.gif");
      // gbuff.drawImage(img,(x-1)*30+17,(y-1)*30+17,this);
      gbuff.drawImage(img, x * cell - cell / 2 + 3, y * cell - cell / 2 + 3, this);

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
   public void reset() { // 오목판을 초기화시킨다.
      for (int i = 0; i < map.length; i++)
         for (int j = 0; j < map[i].length; j++)
            map[i][j] = 0;
      repaint();
   }
}