
// ChatMsg.java 채팅 메시지 ObjectStream 용.
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.ImageIcon;

class ProtocolCode implements Serializable {
   private static final long serialVersionUID = 1L;
   public String code; // 100:로그인, 400:로그아웃, 200:채팅메시지, 300:Image, 500: Mouse Event
   public String UserName;
   public String data;
   public ImageIcon img;
   public MouseEvent mouse_e;
   public int color;
   public int x;
   public int y;

   public ProtocolCode(String UserName, String code, String msg) {
      this.code = code;
      this.UserName = UserName;
      this.data = msg;
   }

   public ProtocolCode(String UserName, String code, int color, int x, int y) {
      this.UserName = UserName;
      this.code = code;
      this.color = color;
      this.x = x;
      this.y = y;
   }
}