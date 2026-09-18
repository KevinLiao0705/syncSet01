package base3;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class SelfTest extends javax.swing.JDialog {

    static int ret_i;
    //===============================
    Timer tm1 = null;
    int keyType_i = 0;
    int mesType_i = 0;
    int autoClose_tim = 0;
    int selfTest_tim = 0;
    int selfTest_nextTime = 1;
    int selfTest_step = 0;
    int err1_f = 0;
    Color cl;
    String title_str = "title_str";
    String winName_str = "winName_str";
    int fullScr_f = 1;
    int winW = 800;
    int winH = 480;

    Timer tm2 = null;
    int cmdin_f = 0;
    int cmdin_inx = 0;
    String cmdin_str = "";

    Container cp;
    JPanel pn1;
    JLabel lb1;
    JTextArea ta1;
    JTextPane tp1;
    JButton[] bta1 = new JButton[3];
    JScrollPane scroll;

    //static MyLayout ly=new MyLayout();
    public SelfTest(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        SelfTest cla = this;
        cla.setBounds(-100, -100, 0, 0);
    }

    public void create() {
        SelfTest cla = this;
        if (GB.frameOn_f == 0) {
            cla.setUndecorated(true);
        }
        cla.addWindowListener(new SelfTestWinLis(this));
        cla.setTitle("SelfTest");
        Font myFont = new Font("Serif", Font.BOLD, 24);
        SelfTestMsLis mslis = new SelfTestMsLis(this);
        //===============================================
        cp = cla.getContentPane();
        cp.setLayout(null);
        cp.setBackground(Color.pink);
        //===============================================

        lb1 = new JLabel();
        lb1.setFont(myFont);
        lb1.setHorizontalAlignment(JLabel.CENTER);
        cp.add(lb1);

        /*
        ta1 = new JTextArea(); 
        ta1.setRows(25);
        ta1.setColumns(25);
        ta1.setWrapStyleWord(true);
        ta1.setFont(myFont);
        ta1.setBackground(Color.WHITE);
        ta1.setEditable(false); //<-- put it here
         */
        myFont = new Font("Serif", Font.PLAIN, 20);

        EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));
        tp1 = new JTextPane();
        tp1.setBorder(eb);
        tp1.setBackground(Color.BLACK);
        tp1.setFont(myFont);
        //tPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        tp1.setMargin(new Insets(5, 5, 5, 5));

        scroll = new JScrollPane(tp1);
        cp.add(scroll); //Object of Jpanel        

        //ta1 = new JTextArea();
        //ta1.setBackground(Color.WHITE);
        //cp.add(ta1);
        //===============================================
        int i;
        for (i = 0; i < 3; i++) {
            bta1[i] = new JButton();
            bta1[i].setFont(new Font("Serif", Font.BOLD, 20));
            bta1[i].setName(Integer.toString(0 * 256 + i));
            bta1[i].addMouseListener(mslis);
            bta1[i].setText(Integer.toString(i + 1));
            bta1[i].setVisible(false);
            cp.add(bta1[i]);
        }
        //=======================================================
        if (cla.tm1 == null) {
            cla.tm1 = new Timer(100, new SelfTestTm1(cla));
            cla.tm1.start();
        }

        if (cla.tm2 == null) {
            cla.tm2 = new Timer(20, new SelfTestTm2(cla));
            cla.tm2.start();
        }

    }

    void cmd(String cmdstr) {
        if (cmdstr.equals("ok")) {
            cmd(0);
            return;
        }
        if (cmdstr.equals("pageUp")) {
            cmd(1);
            return;
        }
        if (cmdstr.equals("pageDown")) {
            cmd(2);
            return;
        }

    }

    void cmd(int index) {
        SelfTest cla = this;

        switch (index) {
            case 0 * 256 + 0:
                SelfTest.ret_i = 1;
                cla.dispose();
                break;
            case 0 * 256 + 1:   //page up
                //JViewport jv = cla.scroll.getViewport();
                //jv.setViewPosition(new Point(0,0));                
                JScrollBar vertical1 = cla.scroll.getVerticalScrollBar();
                int pageDec = vertical1.getBlockIncrement(-1);        //up
                vertical1.setValue(vertical1.getValue() - pageDec);
                break;
            case 0 * 256 + 2:
                JScrollBar vertical2 = cla.scroll.getVerticalScrollBar();
                int pageInc = vertical2.getBlockIncrement(1);        //down
                vertical2.setValue(vertical2.getValue() + pageInc);
                break;

        }
    }

    void appendToPane(JTextPane tp, String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }
}

class SelfTestWinLis extends WindowAdapter {

    SelfTest cla;

    SelfTestWinLis(SelfTest owner) {
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

        /*
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
         */
        switch (cla.mesType_i) {
            case 0: {
                cla.setTitle("Self Test");
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
            case 0: {
                cla.bta1[0].setText("0. OK");
                //=================================
                MyLayout.ctrA[0] = cla.lb1;
                MyLayout.rateH = 0.05;
                MyLayout.gridLy();
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.scroll;
                MyLayout.rateH = 0.9;
                MyLayout.gridLy();
                //cla.ta1.setText("fdfdfdfds");
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.bta1[0];
                MyLayout.xcen = 1;
                MyLayout.ycen = 1;
                MyLayout.iw = 100;
                MyLayout.ih = 50;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(true);
                cla.bta1[1].setVisible(false);
                cla.bta1[2].setVisible(false);
                break;
            }
            case 1: {
                cla.bta1[0].setText("0. OK");
                cla.bta1[1].setText("1. UP");
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
                MyLayout.iw = 100;
                MyLayout.ih = 50;
                MyLayout.xm = 100;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(true);
                cla.bta1[1].setVisible(true);
                cla.bta1[2].setVisible(false);
                break;
            }
            case 2: {
                cla.bta1[0].setText("0. OK");
                cla.bta1[1].setText("1. UP");
                cla.bta1[2].setText("2. DOWN");
                //=================================
                MyLayout.ctrA[0] = cla.lb1;
                MyLayout.rateH = 0.1;
                MyLayout.gridLy();
                //=================================
                MyLayout.yst = MyLayout.yend;
                MyLayout.ctrA[0] = cla.scroll;
                MyLayout.rateH = 0.82;
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
                MyLayout.iw = 150;
                //MyLayout.ih = 50;
                MyLayout.xm = 20;
                MyLayout.bm = 5;
                MyLayout.gridLy();
                cla.bta1[0].setVisible(true);
                cla.bta1[1].setVisible(true);
                cla.bta1[2].setVisible(true);
                break;
            }
        }
    }
}

class SelfTestTm1 implements ActionListener {

    String str;
    SelfTest cla;

    SelfTestTm1(SelfTest owner) {
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
        cla.selfTest_tim++;
        if (cla.selfTest_tim == cla.selfTest_nextTime) {
            switch (cla.selfTest_step) {
                case 0:
                    cla.appendToPane(cla.tp1, "自測開始....\n", Color.WHITE);
                    cla.appendToPane(cla.tp1, "=========================================\n", Color.WHITE);
                    cla.selfTest_nextTime += 10;
                    cla.selfTest_step++;
                    break;
                case 1:
                    cla.appendToPane(cla.tp1, "IO通道測試 ==> ", Color.YELLOW);
                    cla.selfTest_nextTime += 10;
                    cla.selfTest_step++;
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    if (cla.err1_f == 0) {
                        cla.appendToPane(cla.tp1, "成功!\n", Color.GREEN);
                    } else {
                        cla.appendToPane(cla.tp1, "失敗!!!\n", Color.RED);
                    }
                    cla.appendToPane(cla.tp1, "=========================================\n", Color.WHITE);
                    cla.appendToPane(cla.tp1, "測試完成     \n", Color.WHITE);
                    cla.selfTest_nextTime += 10;
                    cla.selfTest_step++;

                    break;

                default:

                    break;

            }

        }

    }

}

class SelfTestTm2 implements ActionListener {

    String str;
    SelfTest cla;

    SelfTestTm2(SelfTest owner) {
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

class SelfTestMsLis extends MouseAdapter {

    int enkey_f;
    SelfTest cla;

    SelfTestMsLis(SelfTest owner) {
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
