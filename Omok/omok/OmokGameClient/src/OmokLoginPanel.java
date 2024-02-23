
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;

@SuppressWarnings("serial")
public class OmokLoginPanel extends JPanel {
   
   private OmokGameClientMain client;

   private JTextField inputId;
   private JPasswordField inputPass;
   private OmokImageButton loginButton;
   private String username;
   private   String passwd;
   private   String ip_addr = "127.0.0.1";
   private String port_no = "30000";
   private Image icon = new ImageIcon("./image/loginForm.jpg").getImage();
   
   public OmokLoginPanel(final OmokGameClientMain omokGameClientMain) {
      this.client = omokGameClientMain;
      
      inputId = new JTextField(7);
      inputPass = new JPasswordField(7);
      loginButton = new OmokImageButton("./image/loginButton.jpg", "LOGIN","./image/loginButtonOver.jpg");
      
      add(inputId);
      add(inputPass);
      add(loginButton);
      
      inputId.setBounds(130,175,100,25);
      inputPass.setBounds(130,205,100,25);
      loginButton.setLocation(255, 165);
      
      inputId.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(!inputId.getText().equals("")) {
               username = inputId.getText();
            }
         }
      });
      
      inputPass.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(!inputId.getText().equals("")) {
            }
         }
      });
      
      loginButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(!inputId.getText().equals("")) {
               client.setVisible(false);
               OmokGameClientView view = new OmokGameClientView(inputId.getText(), ip_addr, port_no);
            }
         }
      });
      
      setLayout(null);
   }
   
   public void paint(Graphics g) {
      g.drawImage(icon,0,0,null,null);
      this.paintComponents(g);
   }
   
   public int[] getFrameSize() {
      int size[] = new int[2];
      size[0] = icon.getWidth(null)+15;
      size[1] = icon.getHeight(null)+35;
      return size;
   }
   
}