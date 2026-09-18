package base3;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Select extends javax.swing.JDialog {

    static int ret_i;
    static int ret_f = 0;

    String title_str = "title_str";
    int fullScr_f = 0;
    int frameOn_f = 1;
    int winW = 800;
    int winH = 600;
    String[] selstr = new String[32];
    int count = 6;
    int xc = 1;
    int xm = 20;
    int ym = 10;
    int lm = 20;
    int rm = 20;
    int ih = 0;

    Container cp;
    JLabel lb1;
    JButton[] bta1 = new JButton[32];
    JButton btesc = new JButton();
    JPanel pn1;


    //static MyLayout ly=new MyLayout();
    public Select(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        Select cla = this;
        cla.setBounds(-100, -100, 0, 0);
    }

    public void create() {
        Select cla = this;
        cla.setTitle("Select");
        Font myFont = new Font("Serif", Font.BOLD, 24);
        cla.addWindowListener(new SelectWinLis(cla));
        SelectMsLis mslis = new SelectMsLis(cla);
        //===============================================
        cp = cla.getContentPane();
        cp.setLayout(null);
        cp.setBackground(Color.CYAN);
        //===============================================
        lb1 = new JLabel();
        lb1.setFont(myFont);
        lb1.setHorizontalAlignment(JLabel.CENTER);
        cp.add(lb1);
        
        pn1 = new JPanel();
        pn1.setLayout(null);
        cp.add(pn1);
        pn1.setBackground(Color.CYAN);
        
        
        //===============================================
        int i;
        for (i = 0; i < cla.count; i++) {
            bta1[i] = new JButton();
            bta1[i].setFont(myFont);
            bta1[i].setName(Integer.toString(1 * 256 + i));
            bta1[i].addMouseListener(mslis);
            bta1[i].setVisible(false);
            pn1.add(bta1[i]);
        }

        btesc = new JButton();
        btesc.setFont(myFont);
        btesc.setName(Integer.toString(0 * 256 + 0));
        btesc.addMouseListener(mslis);
        btesc.setVisible(true);
        btesc.setText("ESC");
        cp.add(btesc);
        //=======================================================
    }

    void onShow() {
        Select cla = this;
        Rectangle r = new Rectangle();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        cla.lb1.setText(cla.title_str);
        Select.ret_f = 0;
        //======================================================
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
        if(frameOn_f==1)
            cla.cp.setBounds(0, 0, r.width - GB.winFrame_wm, r.height - GB.winFrame_hm);
        else    
            cla.cp.setBounds(0, 0, r.width , r.height );
        //=================================
        MyLayout.ctrA[0] = cla.lb1;
        MyLayout.rateH = 0.05;
        MyLayout.gridLy();
        MyLayout.yst = MyLayout.yend;
        MyLayout.ctrA[0] = cla.pn1;
        MyLayout.rateH = 0.9;
        MyLayout.gridLy();
        MyLayout.yst = MyLayout.yend;
        MyLayout.ctrA[0] = cla.btesc;
        MyLayout.xcen = 1;
        MyLayout.rateH = 1;
        MyLayout.iw = 200;
        MyLayout.gridLy();
        //=================================
        MyLayout.lm = cla.lm;
        MyLayout.rm = cla.rm;
        MyLayout.xm = cla.xm;
        MyLayout.ym = cla.ym;
        //MyLayout.ycen = 1;
        //MyLayout.ih = cla.ih;
        for (int i = 0; i < cla.count; i++) {
            MyLayout.ctrA[i] = cla.bta1[i];
            cla.bta1[i].setText(cla.selstr[i]);
            cla.bta1[i].setVisible(true);
        }
        MyLayout.eleAmt = cla.count;
        MyLayout.xc = cla.xc;
        MyLayout.yc = MyLayout.eleAmt / cla.xc;
        if (MyLayout.eleAmt % cla.xc != 0) {
            MyLayout.yc++;
        }
        MyLayout.gridLy();




        
        
        
    }

}

class SelectWinLis extends WindowAdapter {

    Select cla;

    SelectWinLis(Select owner) {
        cla = owner;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        //System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        cla.onShow();

    }
}

class SelectMsLis extends MouseAdapter {

    int enkey_f;
    Select cla;

    SelectMsLis(Select owner) {
        cla = owner;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int index;
        if (enkey_f != 1) {
            return;
        }
        index = Integer.parseInt(e.getComponent().getName());
        switch (index) {
            case 0 * 256 + 0:
                cla.dispose();
                Select.ret_f = 0;
                break;
            default:
                if ((index / 256) == 1) {
                    Select.ret_f = 1;
                    Select.ret_i = index % 256;
                    cla.dispose();
                }
                break;

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
