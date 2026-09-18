package base3;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Input extends javax.swing.JDialog {

    static int ret_f;
    static String ret_str;

    Timer tm1 = null;
    int cmdin_f=0;     
    int cmdin_inx=0;
    String cmdin_str="";
    
    
    String title_str = "title_str";
    int winW = 800;
    int winH = 480;
    int dechex_f = 0;
    int float_f = 0;
    int plus_minus_f = 0;
    int vlen = 16;
    String initv_str;

    Container cp;
    JLabel lb1, lb2;
    JTextField tf1;
    JButton[] bta1 = new JButton[24];
    int first_f = 1;

    //static MyLayout ly=new MyLayout();
    public Input(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        Input cla = this;
        cla.setBounds(-100, -100, 0, 0);
    }

    public void create() {
        Input cla = this;
        
        if (GB.cursorOff_f == 1) {
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            cla.getContentPane().setCursor(blankCursor);
        }
        
        
        cla.addWindowListener(new InputWinLis(cla));
        cla.setTitle("Input");
        if (GB.frameOn_f == 0) {
            cla.setUndecorated(true);
        }
        
        Font myFont = new Font("Serif", Font.BOLD, 24);
        InputMsLis mslis = new InputMsLis(this);
        first_f = 1;
        //===============================================
        cp = cla.getContentPane();
        cp.setLayout(null);
        cp.setBackground(Color.CYAN);
        //===============================================
        lb1 = new JLabel();
        lb1.setFont(myFont);
        lb1.setHorizontalAlignment(JLabel.CENTER);
        cp.add(lb1);
        //===============================================
        tf1 = new JTextField();
        tf1.setName(Integer.toString(0 * 256 + 0));
        tf1.addMouseListener(mslis);
        tf1.setHorizontalAlignment(JTextField.CENTER);
        tf1.setOpaque(true);
        tf1.setForeground(Color.BLUE);
        //tf1.addKeyListener(keylis);
        tf1.setFont(myFont);
        cp.add(tf1);

        int i;
        for (i = 0; i < bta1.length; i++) {
            bta1[i] = new JButton();
            bta1[i].setFont(myFont);
            bta1[i].setName(Integer.toString(1 * 256 + i));
            bta1[i].addMouseListener(mslis);
            bta1[i].setVisible(false);
            cp.add(bta1[i]);
        }
        //=======================
        //=======================
        bta1[0].setText("7");
        bta1[1].setText("8");
        bta1[2].setText("9");
        bta1[3].setText("");
        bta1[4].setText("");
        bta1[5].setText("Back");
        //=======================
        bta1[6].setText("4");
        bta1[7].setText("5");
        bta1[8].setText("6");
        bta1[9].setText("");
        bta1[10].setText("");
        bta1[11].setText("Clear");
        //=======================
        bta1[12].setText("1");
        bta1[13].setText("2");
        bta1[14].setText("3");
        bta1[15].setText("");
        bta1[16].setText("");
        bta1[17].setText("Enter");
        //=======================
        bta1[18].setText(".");
        bta1[19].setText("0");
        bta1[20].setText("+/-");
        bta1[21].setText("");
        bta1[21].setText("");
        bta1[23].setText("Exit");

        //=======================================================
        if (cla.tm1 == null) {
            cla.tm1 = new Timer(20, new InputTm1(cla));
            cla.tm1.start();
        }

        
        
    }

    void cmd(String cmdstr) {
        if (cmdstr.equals("7")) {
            cmd(1 * 256 + 0);
            return;
        }
        if (cmdstr.equals("8")) {
            cmd(1 * 256 + 1);
            return;
        }
        if (cmdstr.equals("9")) {
            cmd(1 * 256 + 2);
            return;
        }
        if (cmdstr.equals("4")) {
            cmd(1 * 256 + 6);
            return;
        }
        if (cmdstr.equals("5")) {
            cmd(1 * 256 + 7);
            return;
        }
        if (cmdstr.equals("6")) {
            cmd(1 * 256 + 8);
            return;
        }
        if (cmdstr.equals("1")) {
            cmd(1 * 256 + 12);
            return;
        }
        if (cmdstr.equals("2")) {
            cmd(1 * 256 + 13);
            return;
        }
        if (cmdstr.equals("3")) {
            cmd(1 * 256 + 14);
            return;
        }
        
        if (cmdstr.equals("*")) {
            cmd(1 * 256 + 18);
            return;
        }
        if (cmdstr.equals("0")) {
            cmd(1 * 256 + 19);
            return;
        }
        if (cmdstr.equals("pm")) {
            cmd(1 * 256 + 20);
            return;
        }
        
        
        if (cmdstr.equals("f1")) {  //back
            cmd(1 * 256 + 5);
            return;
        }
        if (cmdstr.equals("f2")) {  //clear
            cmd(1 * 256 + 11);
            return;
        }
        if (cmdstr.equals("f3")) {  //enter
            cmd(1 * 256 + 17);
            return;
        }
        if (cmdstr.equals("f4")) {  //esc
            cmd(1 * 256 + 23);
            //return;
        }
        if (cmdstr.equals("ok")) {  //enter
            cmd(1 * 256 + 17);
            //return;
        }
        if (cmdstr.equals("left")) {  //back
            cmd(1 * 256 + 5);
            //return;
        }
        if (cmdstr.equals("up")) {  //back
            cmd(1 * 256 + 23);
            //return;
        }
        

    }

    void cmd(int index) {
        Input cla = this;
        switch (index) {
            case 0 * 256 + 0:       //tf1
                //cla.dispose();
                //Input.ret_f=0;
                break;
            //===================================
            case 1 * 256 + 0:       //bta1[]
                editpad('7');
                break;
            case 1 * 256 + 1:
                editpad('8');
                break;
            case 1 * 256 + 2:
                editpad('9');
                break;
            case 1 * 256 + 3:
                break;
            case 1 * 256 + 4:
                break;
            case 1 * 256 + 5:   //back
                if (cla.tf1.getText().length() == 0) {
                    break;
                }
                cla.first_f = 0;
                cla.tf1.setForeground(Color.BLACK);
                cla.tf1.setText(cla.tf1.getText().substring(0, cla.tf1.getText().length() - 1));
                break;
            //===================================
            case 1 * 256 + 6:
                editpad('4');
                break;
            case 1 * 256 + 7:
                editpad('5');
                break;
            case 1 * 256 + 8:
                editpad('6');
                break;
            case 1 * 256 + 9:
                break;
            case 1 * 256 + 10:
                break;
            case 1 * 256 + 11:      //clear
                cla.tf1.setText("");
                break;
            //===================================
            case 1 * 256 + 12:
                editpad('1');
                break;
            case 1 * 256 + 13:
                editpad('2');
                break;
            case 1 * 256 + 14:
                editpad('3');
                break;
            case 1 * 256 + 15:
                break;
            case 1 * 256 + 16:
                break;
            case 1 * 256 + 17:      //enter
                Input.ret_f = 1;
                Input.ret_str = cla.tf1.getText();
                cla.dispose();
                break;
            //===================================
            case 1 * 256 + 18:
                editpad('.');
                break;
            case 1 * 256 + 19:
                editpad('0');
                break;
            case 1 * 256 + 20:
                if (cla.plus_minus_f == 1) {
                    break;
                }
                if (cla.tf1.getText().charAt(0) == '-') {
                    cla.tf1.setText(cla.tf1.getText().substring(1));
                } else {
                    cla.tf1.setText('-' + cla.tf1.getText());
                }
                break;
            case 1 * 256 + 21:
                break;
            case 1 * 256 + 22:
                break;
            case 1 * 256 + 23:
                Input.ret_f = 0;
                cla.dispose();
                break;

        }

    }

    void editpad(char ch) {
        Input cla = this;
        if (cla.first_f == 1) {
            cla.first_f = 0;
            cla.tf1.setForeground(Color.BLACK);
            cla.tf1.setText("");
        }
        if (cla.tf1.getText().length() >= cla.vlen) {
            return;
        }
        if (cla.dechex_f == 1) {
            if (ch <= '9' && ch >= '0') {
                cla.tf1.setText(cla.tf1.getText() + ch);
            } else if (ch <= 'F' && ch >= 'A') {
                cla.tf1.setText(cla.tf1.getText() + ch);
            } else if (ch <= 'f' && ch >= 'a') {
                cla.tf1.setText(cla.tf1.getText() + ch);
            } else {
            }
        } else {
            if (ch <= '9' && ch >= '0') {
                String str;
                cla.tf1.setText(cla.tf1.getText() + ch);
                str = cla.tf1.getText();

            }
            if (ch == '.') {
                cla.tf1.setText(cla.tf1.getText() + ch);
            }
        }

    }

    void onShow() {
        Input cla = this;
        Rectangle r = new Rectangle();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        cla.lb1.setText(cla.title_str);
        Input.ret_f = 0;
        cla.tf1.setText(cla.initv_str);
        cla.tf1.setForeground(Color.BLUE);

        cla.first_f = 1;
        //======================================================
        if (GB.fullScr_f == 1) {
            r.width = screenSize.width;
            r.height = screenSize.height - GB.winFrame_bm;
            r.x = 0;
            r.y = 0;
        } else {
            r.width = cla.winW;
            r.height = cla.winH;
            r.x = (screenSize.width - r.width) / 2;
            r.y = (screenSize.height - r.height - GB.winFrame_bm) / 2;
        }
        cla.setBounds(r);
        if (GB.frameOn_f == 1) {
            cla.cp.setBounds(0, 0, r.width - GB.winFrame_wm, r.height - GB.winFrame_hm);
        } else {
            cla.cp.setBounds(0, 0, r.width, r.height);
        }
        
        
        //=================================
        MyLayout.ctrA[0] = cla.lb1;
        MyLayout.rateH = 0.1;
        MyLayout.gridLy();
        //=================================

        MyLayout.yst = MyLayout.yend;
        MyLayout.ctrA[0] = cla.tf1;
        MyLayout.rateH = 0.1;
        MyLayout.rateW = 1;
        MyLayout.gridLy();
        //=================================
        MyLayout.yst = MyLayout.yend;
        for (int i = 0; i < 24; i++) {
            MyLayout.ctrA[i] = cla.bta1[i];
            cla.bta1[i].setVisible(true);
        }
        MyLayout.eleAmt = 24;
        MyLayout.xc = 6;
        MyLayout.yc = 4;
        MyLayout.gridLy();
        

    }

}


class InputTm1 implements ActionListener {

    String str;
    Input cla;

    InputTm1(Input owner) {
        cla = owner;
    }
    @Override
    public void actionPerformed(ActionEvent evt) {
        //keyin
        
        cla.toFront();
        if(cla.cmdin_f!=0)
        {   
            if(cla.cmdin_f==1)
                cla.cmd(cla.cmdin_inx);
            if(cla.cmdin_f==2)
                cla.cmd(cla.cmdin_str);
            cla.cmdin_f=0;    
        }    

    }


}


class InputWinLis extends WindowAdapter {

    Input cla;

    InputWinLis(Input owner) {
        cla = owner;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        //System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        //cla.onShow();
        //cla.toFront();
    }

}

class InputMsLis extends MouseAdapter {

    int enkey_f;
    Input cla;

    InputMsLis(Input owner) {
        cla = owner;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int index;
        if (enkey_f != 1) {
            return;
        }
        if(cla.cmdin_f==0)
        {    
          index = Integer.parseInt(e.getComponent().getName());
          cla.cmdin_inx=index;
          cla.cmdin_f=1;
          //cla.cmd(index);
        }  
    }

    @Override
    public void mouseExited(MouseEvent e) {
        enkey_f = 0;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        enkey_f = 1;
    }
    //public void mouseClicked(MouseEvent e){} //在源组件上点击鼠标按钮
    //public void mousePressed(MouseEvent e){} //在源组件上按下鼠标按钮
    //public void mouseReleased(MouseEvent e){} //释放源组件上的鼠标按钮
    //public void mouseEntered(MouseEvent e){} //在鼠标进入源组件之后被调用
    //public void mouseExited(MouseEvent e){} //在鼠标退出源组件之后被调用
    //public void mouseDragged(MouseEvent e){} //按下按钮移动鼠标按钮之后被调用
    //public void mouseMoved(MouseEvent e){} //不按住按钮移动鼠标之后被调用

}
