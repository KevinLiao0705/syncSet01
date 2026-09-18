package base3;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Keypad extends javax.swing.JDialog 
{
    static int ret_f;
    static String ret_str;
    
    String title_str="title_str";
    int fullScr_f=0;
    int winW = 800;
    int winH = 480;
    int dechex_f=0;
    int float_f=0;
    int plus_minus_f=0;
    int vlen=16;
    String initv_str;
    
    Container cp;
    JLabel lb1,lb2;
    JTextField tf1;
    JButton[] bta1=new JButton[30] ; 
    int first_f=1;


    
    //static MyLayout ly=new MyLayout();

    public Keypad(java.awt.Frame parent, boolean modal) 
    {
        super(parent, modal);
        Keypad cla=this;
        cla.setBounds(-100,-100,0,0);
    }
    public void create()
    {        
        Keypad cla=this;
        
        if (GB.cursorOff_f == 1) {
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            cla.getContentPane().setCursor(blankCursor);
        }
        
        
        cla.addWindowListener(new KeypadWinLis(cla));
        cla.setTitle("Keypad");
        Font myFont= new Font("Serif", Font.BOLD, 24);
        KeypadMsLis mslis=new KeypadMsLis(this); 
        first_f=1;
        //===============================================
        cp=cla.getContentPane();
        cp.setLayout(null);
        cp.setBackground(Color.CYAN);
        //===============================================
        lb1=new JLabel();
        lb1.setFont(myFont);
        lb1.setHorizontalAlignment(JLabel.CENTER);
        cp.add(lb1);
        //===============================================
        lb2=new JLabel();
        lb2.setFont(myFont);
        lb2.setHorizontalAlignment(JLabel.CENTER);
        lb2.setBackground(Color.YELLOW);
        lb2.setOpaque(true);
        cp.add(lb2);
        //===============================================
        tf1=new JTextField();
        tf1.setName(Integer.toString(0*256+0));
        tf1.addMouseListener(mslis);
        tf1.setHorizontalAlignment(JTextField.CENTER);
        tf1.setOpaque(true);
        tf1.setForeground(Color.BLUE);
        //tf1.addKeyListener(keylis);
        tf1.setFont(myFont);
        cp.add(tf1);
        
        
        
        int i;
        for(i=0;i<bta1.length;i++)
        {    
            bta1[i]=new JButton();
            bta1[i].setFont(myFont);
            bta1[i].setName(Integer.toString(1*256+i));
            bta1[i].addMouseListener(mslis);
            bta1[i].setVisible(false);
            cp.add(bta1[i]);
        }
        //=======================
        bta1[0].setText("A");
        bta1[1].setText("B");
        bta1[2].setText("C");
        bta1[3].setText("D");
        bta1[4].setText("E");
        bta1[5].setText("F");
        //=======================
        bta1[6].setText("7");
        bta1[7].setText("8");
        bta1[8].setText("9");
        bta1[9].setText("");
        bta1[10].setText("DEC");
        bta1[11].setText("Back");
        //=======================
        bta1[12].setText("4");
        bta1[13].setText("5");
        bta1[14].setText("6");
        bta1[15].setText("");
        bta1[16].setText("HEX");
        bta1[17].setText("Clear");
        //=======================
        bta1[18].setText("1");
        bta1[19].setText("2");
        bta1[20].setText("3");
        bta1[21].setText("");
        bta1[22].setText("");
        bta1[23].setText("Enter");
        //=======================
        bta1[24].setText("0");
        bta1[25].setText(".");
        bta1[26].setText("+/-");
        bta1[27].setText("");
        bta1[28].setText("");
        bta1[29].setText("Exit");
        
        
        
        
        
        
        //=======================================================
    }
}
class KeypadWinLis extends WindowAdapter
{
    Keypad cla;
    KeypadWinLis(Keypad owner)
    {
        cla=owner;
    }
    
    
    @Override
    public void windowClosing(WindowEvent e) 
    {
        //System.exit(0);
    }
    
    @Override
    public void windowOpened(WindowEvent e) 
    {
        Rectangle r=new Rectangle();
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();        
        cla.lb1.setText(cla.title_str);
        Keypad.ret_f=0;
        cla.tf1.setText(cla.initv_str);
        cla.first_f=1;
        //======================================================
        if(cla.fullScr_f==1)
        {
            r.width=screenSize.width;
            r.height=screenSize.height-GB.winFrame_bm;
            r.x=0;
            r.y=0;
        }   
        else
        {
            r.width=cla.winW;
            r.height=cla.winH;
            r.x=(screenSize.width-r.width)/2;
            r.y=(screenSize.height-r.height-GB.winFrame_bm)/2;
        }    
        cla.setBounds(r);
        cla.cp.setBounds(0,0,r.width-GB.winFrame_wm,r.height-GB.winFrame_hm);
        if(cla.dechex_f==1)
            cla.lb2.setText("HEX");
        else
            cla.lb2.setText("DEC");
        //=================================
        MyLayout.ctrA[0]=cla.lb1;
        MyLayout.rateH=0.1;
        MyLayout.gridLy();
        //=================================
        int ibuf=MyLayout.yend;
        MyLayout.yst=MyLayout.yend;
        MyLayout.ctrA[0]=cla.lb2;
        MyLayout.rateH=0.1;
        MyLayout.rateW=0.2;
        MyLayout.gridLy();
                
        MyLayout.yst=ibuf;
        MyLayout.xst=MyLayout.xend;
        MyLayout.ctrA[0]=cla.tf1;
        MyLayout.rateH=0.1;
        MyLayout.rateW=1;
        MyLayout.gridLy();
        //=================================
        MyLayout.yst=MyLayout.yend;
        for(int i=0;i<30;i++)
        {    
            MyLayout.ctrA[i]=cla.bta1[i];
            cla.bta1[i].setVisible(true);
        }    
        MyLayout.eleAmt=30;
        MyLayout.xc=6;
        MyLayout.yc=5;
        MyLayout.gridLy();
        
    }
}


class KeypadMsLis extends MouseAdapter
{
    int enkey_f;
    Keypad cla;
    KeypadMsLis(Keypad owner)
    {
        cla=owner;
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        int index;
        if(enkey_f!=1)
            return;
        index = Integer.parseInt(e.getComponent().getName());
        switch(index)
        {
            case 0*256+0:       
                cla.dispose();
                Keypad.ret_f=0;
                break;
            case 1*256+0    :editpad('A');break;
            case 1*256+1    :editpad('B');break;
            case 1*256+2    :editpad('C');break;
            case 1*256+3    :editpad('D');break;
            case 1*256+4    :editpad('E');break;
            case 1*256+5    :editpad('F');break;
            //===================================
            case 1*256+6    :editpad('7');break;
            case 1*256+7    :editpad('8');break;
            case 1*256+8    :editpad('9');break;
            case 1*256+9    :break;
            case 1*256+10   :cla.dechex_f=0;cla.lb2.setText("DEC");break;
            case 1*256+11   :
                if(cla.tf1.getText().length()==0)
                        break;
                cla.tf1.setText(cla.tf1.getText().substring(0,cla.tf1.getText().length()-1));
                break;
            //===================================
            case 1*256+12   :editpad('4');break;
            case 1*256+13   :editpad('5');break;
            case 1*256+14   :editpad('6');break;
            case 1*256+15   :break;
            case 1*256+16   :cla.dechex_f=1;cla.lb2.setText("HEX");break;
            case 1*256+17   :cla.tf1.setText("");break;
            //===================================
            case 1*256+18   :editpad('1');break;
            case 1*256+19   :editpad('2');break;
            case 1*256+20   :editpad('3');break;
            case 1*256+21   :break;
            case 1*256+22   :break;
            case 1*256+23   :
                Keypad.ret_f=1;
                Keypad.ret_str=cla.tf1.getText();
                cla.dispose();
                break;
            //===================================
            case 1*256+24   :editpad('0');break;
            case 1*256+25   :
                if(cla.float_f==0)
                    break;
                editpad('.');
                break;
                
            case 1*256+26   :
                if(cla.plus_minus_f==1)
                    break;
                if(cla.tf1.getText().charAt(0)=='-')
                    cla.tf1.setText(cla.tf1.getText().substring(1));
                else
                    cla.tf1.setText('-'+cla.tf1.getText());
                break;
            case 1*256+27   :break;
            case 1*256+28   :break;
            case 1*256+29   :Keypad.ret_f=0;cla.dispose();break;
                
        }    
    }
    @Override
    public void mouseExited(MouseEvent e)
    {
        enkey_f=0;
    }
    @Override
    public void mousePressed(MouseEvent e)
    {
        enkey_f=1;
    } 
    //public void mouseClicked(MouseEvent e){} //在源组件上点击鼠标按钮
    //public void mousePressed(MouseEvent e){} //在源组件上按下鼠标按钮
    //public void mouseReleased(MouseEvent e){} //释放源组件上的鼠标按钮
    //public void mouseEntered(MouseEvent e){} //在鼠标进入源组件之后被调用
    //public void mouseExited(MouseEvent e){} //在鼠标退出源组件之后被调用
    //public void mouseDragged(MouseEvent e){} //按下按钮移动鼠标按钮之后被调用
    //public void mouseMoved(MouseEvent e){} //不按住按钮移动鼠标之后被调用
    
    void editpad(char ch)
    {
        if(cla.first_f==1)
        {
            cla.first_f=0;
            cla.tf1.setForeground(Color.BLACK);
            cla.tf1.setText("");
        }
        if(cla.tf1.getText().length()>=cla.vlen)
            return;
        if(cla.dechex_f==1)
        {
            if(ch<='9' && ch>='0')
                cla.tf1.setText(cla.tf1.getText()+ch);
            else if(ch<='F' && ch>='A')
                cla.tf1.setText(cla.tf1.getText()+ch);
            else if(ch<='f' && ch>='a')
                cla.tf1.setText(cla.tf1.getText()+ch);
            else
            {
            }
        }
        else
        {
            if(ch<='9' && ch>='0')
                cla.tf1.setText(cla.tf1.getText()+ch);
            if(ch=='.')
                cla.tf1.setText(cla.tf1.getText()+ch);
        }

    }
        
}    
            
    
    

