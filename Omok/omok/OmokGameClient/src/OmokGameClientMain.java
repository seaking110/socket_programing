
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class OmokGameClientMain extends JFrame {

   private static final long serialVersionUID = 1L;
   private OmokLoginPanel lp;
   private Container cp = this.getContentPane();
   private OmokLoginPanel panel;

   public static void main(String[] args) {
      EventQueue.invokeLater(new Runnable() {
         public void run() {
            try {
               OmokGameClientMain frame = new OmokGameClientMain();
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
   public OmokGameClientMain() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 0, 0);
      cp.removeAll();
      
      panel = new OmokLoginPanel(this);
      cp.add((Component) panel);
      
      int[] size = panel.getFrameSize();
      
      this.setSize(size[0], size[1]);
      this.setVisible(true);
      
   }
}

