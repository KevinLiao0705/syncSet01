/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 *
 * @author Administrator
 */
public class Base3 {

    static Base3 scla;//= new Base3();
    static final Logger log = Logger.getLogger(Base3.class);

    int debug_f = 0;
    int ret_f = 0;
    int ret_i;
    //===========================
    
    
    
    
    Action x;

    public Base3() {
        Base3 cla = this;
        GB.initGB();
        x = new Action(this);
        
    //==========================================================
        String fileName = GB.logSetPath+"/log4j.properties";
        String wstr="";
        wstr+="\nlog4j.rootLogger=DEBUG, consoleout, fileout";
        wstr+="\nlog4j.appender.consoleout=org.apache.log4j.ConsoleAppender";
        wstr+="\nlog4j.appender.consoleout.Target=System.out";
        wstr+="\nlog4j.appender.consoleout.layout=org.apache.log4j.PatternLayout";
        wstr+="\nlog4j.appender.consoleout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %p %c:%L - %m%n";
        wstr+="\nlog4j.appender.fileout=org.apache.log4j.RollingFileAppender";
        wstr+="\nlog4j.appender.fileout.File="+GB.logPath+"/"+GB.appName+".log";
        wstr+="\nlog4j.appender.fileout.MaxFileSize=5MB";
        wstr+="\nlog4j.appender.fileout.MaxBackupIndex=10";
        wstr+="\nlog4j.appender.fileout.layout=org.apache.log4j.PatternLayout";
        wstr+="\nlog4j.appender.fileout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n";
        
        BufferedWriter outf=null;
        try {
            outf = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "UTF-8"));
            outf.write(wstr);
            outf.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PropertyConfigurator.configure(fileName);
        //===============================================
        log.info(GB.appName+" process start ...");        
        log.warn("test warnning");
        log.error("test error");
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int i;
        //=======================================================
        GB.os_inx = Lib.getOs();
        Base3 cla = new Base3();
        scla = cla;
        cla.x.act(0);//read setdata.xml to para[] 
        cla.x.act(1);//read database to para[] 
        cla.netInf(0);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/dd/MM HH:mm:ss");  
        Date date = new Date();  
        GB.startTime=formatter.format(date);          
        
        
        if (cla.debug_f != 1) {
            //Lib.exe("./io.sh");
            if (GB.process_inx == 0) {
                ConsoleMain cm1 = new ConsoleMain();
                cm1.create();
            }

            if (GB.process_inx == 1) {
            }

            if (GB.process_inx == 2) {
            }

            if (GB.process_inx == 3) {
            }
            System.exit(0);

        }
    }

    void readDatabase() {
        Connection c = null;
        String dbPath = GB.setdata_db;
        try {
            File f = new File(dbPath);
            System.out.println("dbPath: " + dbPath);
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
            //==============================================
            java.sql.Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM paraTable;");
            String paraName = "", paraValue = "";
            while (rs.next()) {
                paraName = rs.getString("paraName");
                paraValue = rs.getString("paraValue");
                //System.out.println( ">" + paraName+"  "+paraValue);
                //ta1.append( ">" + paraName+"  "+paraValue+"\n");
                GB.editNewPara(paraName, paraValue);

            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /*
    void wdateDb(String paraName, String paraValue) {
        Connection c = null;
        String dbPath = GB.setdata_db;
        String sql;
        try {
            File f = new File(dbPath);
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
            java.sql.Statement stmt = c.createStatement();
            sql = "UPDATE paraTable set paraValue = \"";
            sql = sql + paraValue;
            sql = sql + "\" where paraName=\"";
            sql = sql + paraName;
            sql = sql + "\";";
            stmt.executeUpdate(sql);
            c.commit();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
    
     */
    public boolean checkDb(String paraName) {
        String dbPath = GB.setdata_db;
        Connection con = null;
        String pName;
        String pValue;
        String sbuf;
        boolean ret = false;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            //==============================================
            java.sql.Statement stmt = con.createStatement();
            sbuf = "SELECT * FROM paraTable;";
            ResultSet rs = stmt.executeQuery(sbuf);
            while (rs.next()) {
                pName = rs.getString("paraName");
                if (pName.equals(paraName)) {
                    ret = true;
                    break;
                }
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return ret;
    }

    public int editNewDb(String paraName, String paraValue) {
        int line;
        line = editDb(paraName, paraValue);
        if (line > 0) {
            return line;
        }
        line = insertDb(paraName, paraValue);
        return line;
    }

    public int editDb(String paraName, String paraValue) {
        String dbPath = GB.setdata_db;
        Connection con;
        String sql;
        int chgLine = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            java.sql.Statement stmt = con.createStatement();
            //UPDATE paraTable set 
            sql = "UPDATE paraTable set paraValue = \"";
            sql = sql + paraValue;
            sql = sql + "\" where paraName=\"";
            sql = sql + paraName;
            sql = sql + "\";";
            chgLine = stmt.executeUpdate(sql);
            con.commit();
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return chgLine;
    }

    //statement.executeUpdate("INSERT INTO Customers " + "VALUES (1001, 'Simpson', 'Mr.', 'Springfield', 2001)");
    public int insertDb(String paraName, String paraValue) {
        String dbPath = GB.setdata_db;
        Connection con = null;
        String sql;
        int chgLine = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            java.sql.Statement stmt = con.createStatement();
            sql = "INSERT INTO paraTable VALUES ('";
            sql += paraName;
            sql += "','";
            sql += paraValue;
            sql += "');";
            chgLine = stmt.executeUpdate(sql);
            con.commit();
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return chgLine;
    }

    public String encSyssec(byte[] mac) {
        String str = "";
        byte[] enckey = new byte[8];
        byte[] hop = new byte[8];
        hop[1] = (byte) (mac[0] & 255);
        hop[2] = (byte) (mac[1] & 255);
        hop[3] = (byte) (mac[2] & 255);
        hop[4] = (byte) (mac[3] & 255);
        enckey[0] = (byte) (mac[0] & 255);
        enckey[1] = (byte) (mac[1] & 255);
        enckey[2] = (byte) (mac[2] & 255);
        enckey[3] = (byte) (mac[3] & 255);
        enckey[4] = (byte) (mac[4] & 255);
        enckey[5] = (byte) (mac[5] & 255);
        enckey[6] = (byte) (GB.syssec_xor);
        enckey[7] = (byte) (GB.syssec_xor);
        Lib.dechop(hop, enckey);
        str += Integer.toString(hop[1] & 255);
        str += Integer.toString(hop[2] & 255);
        str += Integer.toString(hop[3] & 255);
        str += Integer.toString(hop[4] & 255);
        return str;

    }

    public String netInf(int ww) {
        InetAddress ip;
        int i;
        int sti;
        String str = null;
        String localIp = null;
        GB.set_ip_str = Lib.rdInterfaces("address");
        GB.set_ipmask_str = Lib.rdInterfaces("netmask");
        GB.set_gateway_str = Lib.rdInterfaces("gateway");

        System.out.println("IP address in set : " + GB.set_ip_str);
        System.out.println("IP mask in set: " + GB.set_ipmask_str);
        System.out.println("Gateway address in set: " + GB.set_gateway_str);

        try {

            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                int ipCnt=0;
                while (ee.hasMoreElements()) {
                    InetAddress ia = (InetAddress) ee.nextElement();
                    str = ia.getHostAddress();
                    //System.out.println(str);
                    sti = str.indexOf("192.168.");
                    if (sti >= 0) {
                        localIp = str;
                        ipCnt++;
                        break;
                    }
                }
                if(ipCnt>0)
                    break;
            }

            if (localIp == null) {
                return str;
            }
            //ip = InetAddress.getLocalHost();
            //ip = InetAddress.getByName("192.168.0.57");
            ip = InetAddress.getByName(localIp);
            System.out.println("Current IP address : " + ip.getHostAddress());
            GB.real_ip_str = ip.getHostAddress();
            String[] strA = GB.real_ip_str.split("\\.");
            for (i = 0; i < 4; i++) {
                GB.realIp[i] = (byte) (Lib.str2int(strA[i], 0) & 255);
            }

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            //if(network==null)
            //    return;
            byte[] mac = network.getHardwareAddress();

            System.out.print("Current MAC address : ");

            StringBuilder sb = new StringBuilder();
            for (i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());
            str = "";
            if (mac.length < 6) {
                GB.syssec_f = 1;
            } else {
                GB.macStr = "" + (mac[0] & 255);
                GB.macStr += "." + (mac[1] & 255);
                GB.macStr += "." + (mac[2] & 255);
                GB.macStr += "." + (mac[3] & 255);
                GB.macStr += "." + (mac[4] & 255);
                GB.macStr += "." + (mac[5] & 255);
                str = encSyssec(mac);
            }
            if (GB.syssec.equals(str)) {
                GB.syssec_f = 1;
            } else {
                if (ww != 0) {
                    editNewDb("syssec", str);
                    GB.syssec = str;

                }
            }

        } catch (UnknownHostException | SocketException e) {
        }
        return str;
    }

}

//act 0: read para from setdata.xml
//act 1: read para frm database
//act 2: trans para to global various 
class Action {

    Base3 cla;

    Action(Base3 owner) {
        cla = owner;
    }

    public void act(int index) {
        String str;
        switch (index) {
            case 0: //read setdata.xml to GB.paraName[],GB.paraValue[],  
                System.out.println("Action 0");
                try {
                    FileReader reader = new FileReader(GB.setdata_xml);
                    BufferedReader br = new BufferedReader(reader);
                    String line;
                    String paraN;
                    String paraV;
                    GB.clrPara();
                    while ((line = br.readLine()) != null) {
                        String sline = line.trim();
                        if (sline.length() == 0) {
                            continue;
                        }
                        if (sline.charAt(0) != '[') {
                            continue;
                        }
                        if (Lib.search(sline, "[", "]") == 1) {
                            paraN = Lib.retstr.trim();
                            if (Lib.search(sline, "<", ">") == 1) {
                                paraV = Lib.retstr;
                                byte[] bb;
                                bb = paraV.getBytes("UTF-8");
                                paraV = "";
                                for (int h = 0; h < bb.length; h++) {
                                    paraV += (char) bb[h];
                                }
                                GB.newPara(paraN, paraV);

                                /*
                                byte[] bytes = new byte[paraV.length()];
                                String str1 = paraV;
                                for (int m = 0; m < str1.length(); m++) {
                                    bytes[m] = (byte) str1.charAt(m);
                                }
                                String test1 = new String(bytes, Charset.forName("UTF-8"));

                                GB.newPara(paraN, paraV);
                                 */
                            }
                        }
                    }
                } catch (IOException e2) {
                    System.out.println(e2);
                }

                break;
            case 1:
                System.out.println("Action 1");
                cla.readDatabase();
                break;
            case 2:
                System.out.println("Action 2");
                GB.loadPara2Form();

                int ok_f = 0;

                break;

            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;
            case 15:
                break;
            case 16:
                break;
            case 17:
                break;

        }

    }
}
