package base3;

import java.awt.BorderLayout;
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
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Menu extends javax.swing.JDialog {
//public class Menu extends javax.swing.JFrame {

    static String retstr;

    Timer tm1 = null;
    int cmdin_f = 0;
    int cmdin_inx = 0;
    String cmdin_str = "";

    int input_on_f = 0;
    int winW = 800;
    int winH = 480;
    //String[] selstr = new String[32];
    //int count = 6;
    int xc = 1;
    int xm = 20;
    int ym = 10;
    int lm = 20;
    int rm = 20;
    int ih = 0;
    int menustr_amt;

    Container cp;
    JLabel lb1;
    JButton[] bta1 = new JButton[32];
    JButton btesc;
    JButton btsave;
    JPanel pn1;
    Object[] obj = new Object[100];
    int objLen = 0;

    MenuList menuRoot;
    MenuList nowMenuList;
    Input inp1;
    int save_f=0;

    //static MyLayout ly=new MyLayout();
    public Menu(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        Menu cla = this;
        cla.setBounds(-100, -100, 0, 0);
        //menuRoot=new MenuList();
        nowMenuList = menuRoot;
    }

    public void create() {
        Menu cla = this;
        cla.setTitle("Menu");
        
        if (GB.cursorOff_f == 1) {
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            cla.getContentPane().setCursor(blankCursor);
        }
        
        
        
        if (GB.frameOn_f == 0) {
            cla.setUndecorated(true);
        }
        Font myFont = new Font("Serif", Font.BOLD, 24);
        cla.addWindowListener(new MenuWinLis(cla));
        MenuMsLis mslis = new MenuMsLis(cla);
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
        for (int i = 0; i < 32; i++) {
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
        btesc.setText("0. 離開");
        cp.add(btesc);
        
        btsave = new JButton();
        btsave.setFont(myFont);
        btsave.setName(Integer.toString(0 * 256 + 1));
        btsave.addMouseListener(mslis);
        btsave.setVisible(true);
        btsave.setText("9. 儲存");
        cp.add(btsave);
        
        //=======================================================
        if (cla.tm1 == null) {
            cla.tm1 = new Timer(20, new MenuTm1(cla));
            cla.tm1.start();
        }
        inp1 = new Input(null, true);
        inp1.create();
    }

    void clear() {

    }

    void onShow() {
        Menu cla = this;
        menustr_amt = nowMenuList.mdataList.size();
        cla.xc = nowMenuList.xc;
        Rectangle r = new Rectangle();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        cla.lb1.setText(nowMenuList.name);
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
        int ibuf = cla.cp.getHeight();
        int vbuf;
        vbuf = ibuf - 30 - 50 - 3 * MyLayout.tm - 1 * MyLayout.bm;
        //=================================
        MyLayout.ctrA[0] = cla.lb1;
        //MyLayout.rateH = 0.05;
        MyLayout.ih = 30;
        MyLayout.gridLy();
        MyLayout.yst = MyLayout.yend;
        MyLayout.ctrA[0] = cla.pn1;
        MyLayout.ih = vbuf;
        MyLayout.gridLy();
        MyLayout.yst = MyLayout.yend;
        
        MyLayout.ctrA[0] = cla.btesc;
        MyLayout.ctrA[1] = cla.btsave;
        MyLayout.xcen = 1;
        //MyLayout.rateH = 1;
        MyLayout.eleAmt=2;
        MyLayout.xc = 2;
        MyLayout.iw = 200;
        MyLayout.ih = 50;
        MyLayout.gridLy();
        //=================================
        //MyLayout.ycen = 1;
        //MyLayout.ih = cla.ih;
        int i;
        for (i = 0; i < menustr_amt; i++) {
            MyLayout.ctrA[i] = cla.bta1[i];
            cla.bta1[i].setText(nowMenuList.mdataList.get(i).name);
            cla.bta1[i].setVisible(true);
        }
        for (; i < 32; i++) {
            cla.bta1[i].setVisible(false);
        }

        MyLayout.eleAmt = menustr_amt;
        MyLayout.lm = cla.lm;
        MyLayout.rm = cla.rm;
        MyLayout.xm = cla.xm;
        MyLayout.ym = cla.ym;
        MyLayout.xc = cla.xc;
        MyLayout.yc = MyLayout.eleAmt / cla.xc;
        if (MyLayout.eleAmt % cla.xc != 0) {
            MyLayout.yc++;
        }
        MyLayout.gridLy();
        if (cla.bta1[0].getHeight() > 60) {
            MyLayout.eleAmt = menustr_amt;
            MyLayout.lm = cla.lm;
            MyLayout.rm = cla.rm;
            MyLayout.xm = cla.xm;
            MyLayout.ym = cla.ym;
            MyLayout.xc = cla.xc;
            MyLayout.yc = MyLayout.eleAmt / cla.xc;
            if (MyLayout.eleAmt % cla.xc != 0) {
                MyLayout.yc++;
            }
            //MyLayout.ycen = 1;
            MyLayout.ih = 60;
            MyLayout.gridLy();

        }

    }

    void cmd(String cmdstr) {
        if (cmdstr.equals("esc")) {
            cmd(-1);
            return;
        }
        if (cmdstr.equals("return")) {
            cmd(-2);
            return;
        }
        if (cmdstr.equals("1")) {
            cmd(256 + 0);
            return;
        }
        if (cmdstr.equals("2")) {
            cmd(256 + 1);
            return;
        }
        if (cmdstr.equals("3")) {
            cmd(256 + 2);
            return;
        }
        if (cmdstr.equals("4")) {
            cmd(256 + 3);
            return;
        }
        if (cmdstr.equals("5")) {
            cmd(256 + 4);
            return;
        }
        if (cmdstr.equals("6")) {
            cmd(256 + 5);
            return;
        }
        if (cmdstr.equals("7")) {
            cmd(256 + 6);
            return;
        }
        if (cmdstr.equals("8")) {
            cmd(256 + 7);
            return;
        }
        if (cmdstr.equals("9")) {
            cmd(1);
            return;
        }
        if (cmdstr.equals("0")) {
            cmd(0);
            return;
        }

    }

    void cmd(int index) {
        String str;
        Menu cla = this;
        switch (index) {
            case -1:
            case 0 * 256 + 0:
                cla.dispose();
                save_f=0;
                break;
            case 0 * 256 + 1:
                cla.dispose();
                save_f=1;
                break;
            case -2:
                if (cla.nowMenuList.preMenuList != null) {
                    MenuList menuTmp;
                    menuTmp = cla.nowMenuList.preMenuList;
                    cla.nowMenuList = menuTmp;
                    cla.onShow();
                } else {
                    cla.dispose();
                }
                break;

            default:
                if ((index / 256) == 1) {
                    int inx;
                    inx = index % 256;
                    if (inx >= cla.nowMenuList.mdataList.size()) {
                        break;
                    }
                    if (cla.nowMenuList.mdataList.get(inx).func == 0) {  //command
                        if (inx == 4) {
                            Menu.retstr = "selfTest";
                            cla.dispose();
                            break;
                        }
                        if (inx == 5) {
                            Menu.retstr = "reboot";
                            cla.dispose();
                            break;
                        }

                    }
                    if (cla.nowMenuList.mdataList.get(inx).func == 1) {  //enter menu next
                        if (cla.nowMenuList.mdataList.get(inx).mlist != null) {
                            MenuList menuTmp;
                            menuTmp = cla.nowMenuList.mdataList.get(inx).mlist;
                            cla.nowMenuList = menuTmp;
                            cla.onShow();

                        }
                        break;
                    }
                    if (cla.nowMenuList.mdataList.get(inx).func == 2) {  //return
                        if (cla.nowMenuList.preMenuList != null) {
                            MenuList menuTmp;
                            menuTmp = cla.nowMenuList.preMenuList;
                            cla.nowMenuList = menuTmp;
                            cla.onShow();

                        } else {
                            cla.dispose();
                        }
                        break;

                    }

                    if (cla.nowMenuList.mdataList.get(inx).func == 3) {  //edit

                        inp1 = new Input(null, true);
                        inp1.create();

                        inp1.title_str = cla.nowMenuList.mdataList.get(inx).name;
                        str = "";

                        if (cla.nowMenuList.mdataList.get(inx).dataTyp == 0)//String
                        {
                            str = (String) cla.nowMenuList.mdataList.get(inx).obj;
                        }
                        if (cla.nowMenuList.mdataList.get(inx).dataTyp == 8)//ipaddr string
                        {
                            str = (String) cla.nowMenuList.mdataList.get(inx).obj;

                        }
                        if (cla.nowMenuList.mdataList.get(inx).dataTyp == 9)//ipaddr int array[4]
                        {
                            int[] ia;
                            ia = (int[]) cla.nowMenuList.mdataList.get(inx).obj;
                            str = "" + ia[0] + "." + ia[1] + "." + ia[2] + "." + ia[3];
                        }

                        inp1.initv_str = str;

                        //cla.dispose();
                        inp1.onShow();
                        input_on_f = 1;
                        inp1.setVisible(true);
                        input_on_f = 0;

                        if (Input.ret_f == 1) {
                            if (cla.nowMenuList.mdataList.get(inx).dataTyp == 0)//String
                            {
                                cla.nowMenuList.mdataList.get(inx).obj = Input.ret_str;
                                break;

                            }
                            if (cla.nowMenuList.mdataList.get(inx).dataTyp == 8 || cla.nowMenuList.mdataList.get(inx).dataTyp == 9)//ip_address
                            {
                                str = Input.ret_str;
                                String[] slst;
                                slst = str.split("\\.");
                                int err_f = 1;
                                int[] ia = new int[4];
                                int ibuf;
                                while (true) {
                                    if (slst.length != 4) {
                                        break;
                                    }
                                    ibuf = Lib.str2int(slst[0], -1, 255, 0);
                                    if (ibuf == -1) {
                                        break;
                                    }
                                    ia[0] = ibuf;
                                    ibuf = Lib.str2int(slst[1], -1, 255, 0);
                                    if (ibuf == -1) {
                                        break;
                                    }
                                    ia[1] = ibuf;
                                    ibuf = Lib.str2int(slst[2], -1, 255, 0);
                                    if (ibuf == -1) {
                                        break;
                                    }
                                    ia[2] = ibuf;
                                    ibuf = Lib.str2int(slst[3], -1, 255, 0);
                                    if (ibuf == -1) {
                                        break;
                                    }
                                    ia[3] = ibuf;
                                    err_f = 0;
                                    break;
                                }
                                if (err_f == 1) {
                                    Message mes1 = new Message(null, true);
                                    mes1.keyType_i = 0;
                                    mes1.mesType_i = 1;
                                    mes1.title_str = "Input Error";
                                    mes1.autoClose_tim = 10;
                                    mes1.create();
                                    mes1.setVisible(true);
                                    break;
                                }
                                if (cla.nowMenuList.mdataList.get(inx).dataTyp == 8) {
                                    cla.nowMenuList.mdataList.get(inx).obj = Input.ret_str;
                                    break;
                                }
                                if (cla.nowMenuList.mdataList.get(inx).dataTyp == 9) {
                                    int[] foo;
                                    foo = (int[]) cla.nowMenuList.mdataList.get(inx).obj;
                                    foo[0] = ia[0];
                                    foo[1] = ia[1];
                                    foo[2] = ia[2];
                                    foo[3] = ia[3];
                                    break;
                                }

                            }
                        }

                        break;

                    }

                }
                break;

        }

    }

}

class MenuTm1 implements ActionListener {

    String str;
    Menu cla;

    MenuTm1(Menu owner) {
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

class MenuWinLis extends WindowAdapter {

    Menu cla;

    MenuWinLis(Menu owner) {
        cla = owner;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        //System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }
}

class MenuMsLis extends MouseAdapter {

    int enkey_f;
    Menu cla;

    MenuMsLis(Menu owner) {
        cla = owner;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int index;
        String str;
        if (enkey_f != 1) {
            return;
        }

        index = Integer.parseInt(e.getComponent().getName());
        if (cla.cmdin_f == 0) {
            cla.cmdin_inx = index;
            cla.cmdin_f = 1;
        }
        //cla.cmd(index);

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

class MenuList {

    ArrayList<MData> mdataList;
    String name;
    MenuList preMenuList;
    int xc;

    MenuList(String nm, int w) {
        mdataList = new ArrayList<>();
        name = nm;
        preMenuList = null;
        xc = w;
    }

    //vtype=9 ip address
    //fun=0: none,1: enter next menu,2: return,3:edit
    void add(String name, int fun) {
        mdataList.add(new MData(name, fun));
    }

    void add(String name, int fun, int vtyp, Object obj) {
        mdataList.add(new MData(name, fun, vtyp, obj));
    }

    void add(String name, int fun, int vtyp, Object obj, Object vmin, Object vmax) {
        mdataList.add(new MData(name, fun, vtyp, obj, vmin, vmax));
    }

}

class MData {

    MenuList mlist;
    String name;
    int func;
    int dataTyp;
    Object obj;
    Object dataMin;
    Object dataMax;

    MData(String nm, int fun) {
        mlist = null;
        name = nm;
        func = fun;
        dataTyp = 0;
        obj = null;
        dataMin = null;
        dataMax = null;
    }

    MData(String nm, int fun, int vtype, Object va) {
        mlist = null;
        name = nm;
        func = fun;
        dataTyp = vtype;
        obj = va;
        dataMin = null;
        dataMax = null;
    }

    MData(String nm, int fun, int vtype, Object va, Object vmin, Object vmax) {
        mlist = null;
        name = nm;
        func = fun;
        dataTyp = vtype;
        obj = va;
        dataMin = vmin;
        dataMax = vmax;
    }

}
