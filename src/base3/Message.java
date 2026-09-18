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
import javax.swing.JPanel;
import javax.swing.Timer;

public class Message extends javax.swing.JDialog {

    static int ret_i;
    //===============================
    Timer tm1 = null;
    int keyType_i = 0;
    int mesType_i = 0;
    int autoClose_tim = 0;
    Color cl;
    String title_str = "title_str";
    String winName_str = "winName_str";
    int fullScr_f = 0;
    int winW = 640;
    int winH = 300;

    Timer tm2 = null;
    int cmdin_f = 0;
    int cmdin_inx = 0;
    String cmdin_str = "";

    Container cp;
    JPanel pn1;
    JLabel lb1;
    JButton[] bta1 = new JButton[3];

    //static MyLayout ly=new MyLayout();
    public Message(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        Message cla = this;
        cla.setBounds(-100, -100, 0, 0);
    }

    public void create() {
        Message cla = this;
        
        if (GB.cursorOff_f == 1) {
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            cla.getContentPane().setCursor(blankCursor);
        }
        
        
        cla.addWindowListener(new MessageWinLis(this));
        cla.setTitle("Message");
        Font myFont = new Font("Serif", Font.BOLD, 24);
        MessageMsLis mslis = new MessageMsLis(this);
        //===============================================
        cp = cla.getContentPane();
        cp.setLayout(null);
        cp.setBackground(Color.pink);
        //===============================================
        lb1 = new JLabel();
        lb1.setFont(myFont);
        lb1.setHorizontalAlignment(JLabel.CENTER);
        cp.add(lb1);
        //===============================================
        int i;
        for (i = 0; i < 3; i++) {
            bta1[i] = new JButton();
            bta1[i].setFont(myFont);
            bta1[i].setName(Integer.toString(0 * 256 + i));
            bta1[i].addMouseListener(mslis);
            bta1[i].setText(Integer.toString(i + 1));
            bta1[i].setVisible(false);
            cp.add(bta1[i]);
        }
        //=======================================================
        if (cla.tm1 == null) {
            cla.tm1 = new Timer(100, new MessageTm1(cla));
            cla.tm1.start();
        }

        if (cla.tm2 == null) {
            cla.tm2 = new Timer(20, new MessageTm2(cla));
            cla.tm2.start();
        }

    }

    void cmd(String cmdstr) {
        if (cmdstr.equals("0")) {
            cmd(0);
            return;
        }
        if (cmdstr.equals("1")) {
            cmd(1);
            return;
        }
        if (cmdstr.equals("2")) {
            cmd(2);
            return;
        }

    }

    void cmd(int index) {
        Message cla = this;

        switch (index) {
            case 0 * 256 + 0:
                Message.ret_i = 0;
                cla.dispose();
                break;
            case 0 * 256 + 1:
                Message.ret_i = 1;
                cla.dispose();
                break;
            case 0 * 256 + 2:
                Message.ret_i = 2;
                cla.dispose();
                break;

        }
    }

}

class MessageWinLis extends WindowAdapter {

    Message cla;

    MessageWinLis(Message owner) {
        cla = owner;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        //System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        Rectangle r = new Rectangle();
        cla.lb1.setText(cla.title_str);
        //======================================================
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (cla.fullScr_f == 1) {
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
        cla.cp.setBounds(0, 0, r.width - GB.winFrame_wm, r.height - GB.winFrame_hm);

        switch (cla.mesType_i) {
            case 0: {
                cla.setTitle("Message");
                cla.cp.setBackground(Color.CYAN);
                break;
            }
            case 1: {
                cla.setTitle("Warning");
                cla.cp.setBackground(Color.YELLOW);
                break;
            }
            case 2: {
                cla.setTitle("Error");
                cla.cp.setBackground(Color.RED);
                break;
            }
            default: {
                cla.setTitle(cla.winName_str);
                cla.cp.setBackground(cla.cl);
                break;
            }
        }

        switch (cla.keyType_i) {
            case -1: {
                cla.bta1[0].setText("0. ESC");
                //=================================
                MyLayout.ctrA[0] = cla.lb1;
                MyLayout.rateH = 0.5;
                MyLayout.gridLy();
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.bta1[0];
                MyLayout.xcen = 1;
                MyLayout.ycen = 1;
                MyLayout.iw = 100;
                MyLayout.ih = 50;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(false);
                cla.bta1[1].setVisible(false);
                cla.bta1[2].setVisible(false);
                break;
            }
            case 0: {
                cla.bta1[0].setText("0. ESC");
                //=================================
                MyLayout.ctrA[0] = cla.lb1;
                MyLayout.rateH = 0.5;
                MyLayout.gridLy();
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.bta1[0];
                MyLayout.xcen = 1;
                MyLayout.ycen = 1;
                MyLayout.iw = 200;
                MyLayout.ih = 50;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(true);
                cla.bta1[1].setVisible(false);
                cla.bta1[2].setVisible(false);
                break;
            }
            case 1: {
                cla.bta1[0].setText("0. ESC");
                cla.bta1[1].setText("1. OK");
                //=================================
                MyLayout.ctrA[0] = cla.lb1;
                MyLayout.rateH = 0.5;
                MyLayout.gridLy();
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.bta1[0];
                MyLayout.ctrA[1] = cla.bta1[1];
                MyLayout.eleAmt = 2;
                MyLayout.xc = 2;
                MyLayout.xcen = 1;
                MyLayout.ycen = 1;
                MyLayout.iw = 200;
                MyLayout.ih = 50;
                MyLayout.xm = 100;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(true);
                cla.bta1[1].setVisible(true);
                cla.bta1[2].setVisible(false);
                break;
            }
            case 2: {
                cla.bta1[0].setText("0. ESC");
                cla.bta1[1].setText("1. OK");
                cla.bta1[2].setText("2. NO");
                //=================================
                MyLayout.ctrA[0] = cla.lb1;
                MyLayout.rateH = 0.5;
                MyLayout.gridLy();
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.bta1[0];
                MyLayout.ctrA[1] = cla.bta1[1];
                MyLayout.ctrA[2] = cla.bta1[2];
                MyLayout.eleAmt = 3;
                MyLayout.xc = 3;
                MyLayout.xcen = 1;
                MyLayout.ycen = 1;
                MyLayout.iw = 200;
                MyLayout.ih = 50;
                MyLayout.xm = 100;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(true);
                cla.bta1[1].setVisible(true);
                cla.bta1[2].setVisible(true);
                break;
            }
        }
    }
}

class MessageTm1 implements ActionListener {

    String str;
    Message cla;

    MessageTm1(Message owner) {
        cla = owner;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (cla.autoClose_tim != 0) {
            cla.autoClose_tim--;
            if (cla.autoClose_tim == 0) {
                cla.dispose();
                return;
            }
        }
    }

}

class MessageTm2 implements ActionListener {

    String str;
    Message cla;

    MessageTm2(Message owner) {
        cla = owner;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        //keyin
        if (cla.cmdin_f != 0) {
            if (cla.cmdin_f == 1) {
                cla.cmd(cla.cmdin_inx);
            }
            if (cla.cmdin_f == 2) {
                cla.cmd(cla.cmdin_str);
            }
            cla.cmdin_f = 0;
        }

    }

}

class MessageMsLis extends MouseAdapter {

    int enkey_f;
    Message cla;

    MessageMsLis(Message owner) {
        cla = owner;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int index;
        if (enkey_f != 1) {
            return;
        }

        index = Integer.parseInt(e.getComponent().getName());
        if (cla.cmdin_f == 0) {
            cla.cmdin_inx = index;
            cla.cmdin_f = 1;
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
