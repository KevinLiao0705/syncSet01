package base3;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import org.json.JSONObject;


public class GB {
    static int emuMeterStatus_f = 0;
    static int emuPowerValue_f = 0;
    static int emuSspaValue_f = 0;
    static int debug_f=0;
    static String version = "1.2";
    static int emulate = 0;
    public static String webRootPath = "./";
    public static final int MAX_PARA_LEN = 8192;
    static int lang = 1;//0:english 1:chinese 
    public static String logSetPath = ".";
    public static String logPath = "./log";
    public static String appName = "MyAppName";
    public static String webSocketAddr = "";
    public static int webSocketPort = 8899;
    public static String winParaSetFullName = "d:/kevinJosnPcSync/syncSet/paraSet.json";
    public static String linuxParaSetFullName = "/home/admintx/syncSetExe/paraSet.json";
    public static String paraSetFullName ="";
    
    public static String laPath="C:/Program Files/Logic";
    public static String laAppName="Logic.exe";
    public static String chromePath="C:/Program Files/Google/Chrome/Application";
    public static String chromeAppName="chrome.exe";
    public static String chromeAddress="http://localhost/webBase/index.jsp";
    
    //=====================================================
    static int process_inx = 3;   //0:console sipph,1:desktop sipph,2:PhoneUi,3:Phone6in1 
    static int os_inx = 1;     //0:windows 1://linux    
    static String asteriskConfPath = "./asteridkConfPath";
    static String osName="win";

    //===============================
    static int cursorOff_f = 0;
    //================================================
    static int fullScr_f = 0;
    static int frameOn_f = 0;
    static int syssec_f = 1;
    static int syssec_xor = 0;
    static String macStr;
    //================================================
    static int winFrame_bm = 30;
    static int winFrame_wm = 8;
    static int winFrame_hm = 38;
    static String syssec = "123-125-222-456-111-123";
    static String web_password = "1234";

    static int appId = 0;
    //================================================
    //sipmd ui use
    //================================================

    static String netName = "eth0";
    static String maskStr = "255.255.0.0";
    static String gatewayStr = "192.168.0.1";
    static String startTime = "";

    //
    //sipmd_ui tx "sipphone information" to sipui_ui over socket
    static String sipui_ui_ip = "192.168.0.181";
    static int sipui_ui_port = 1336;
    //================================================
    //sipui ui use
    //================================================exit

    //sipui_ui tx "command data" to sipmd_ui over socket
    static String sipmd_ui_ip = "192.168.0.45";
    static int sipmd_ui_port = 1236;

    //==============================================================================
    //windows debug use===================================================================
    static String setdata_xml = "./setdata.xml";
    static String setdata_db = "./setdata.db";
    static String interfaces_path = "./interfaces";
    //==============================================================================
    static String real_ip_str = "";
    static String set_ip_str = "";
    static String set_ipmask_str = "";
    static String set_gateway_str = "";
    static byte[] realIp = new byte[]{0, 0, 0, 0};
    static String[] paraName = new String[MAX_PARA_LEN];
    static String[] paraValue = new String[MAX_PARA_LEN];
    //================================================
    static int paraLen = 0;
    static String ret_str;
    static int action_inx = 0;
    static int action_step = 0;
    static int action_tim = 0;
    static String preParaSetTime = "";

    //==================================================================
    public static Map<String, Object> paraSetMap = new HashMap();
    public static HashMap<String, ConnectCla> connectMap = new HashMap();
    public static HashMap<String, Object> paraSaveMap = new HashMap();

    //=================================================================
    static void initGB() {
        String osName = System.getProperty("os.name");
        System.out.println("osName: "+osName);
        if (osName != null) {
            if (osName.toLowerCase().contains("win")) {
                GB.osName = "win";
            } else {
                GB.osName = "linux";
            }
        }    
        System.out.println("os: "+GB.osName);
        
        
        
        if (GB.osName.equals("win")) {
            GB.process_inx = 0; //0:consoleMain, 
            GB.os_inx = 0;     //0:windows 1://linux    
            GB.setdata_xml = "./setdata.xml";
            GB.setdata_db = "./setdata.db";
            GB.interfaces_path = "./interfaces";
            GB.paraSetFullName = GB.winParaSetFullName;
            GB.logSetPath = ".";
            GB.logPath ="e:/kevin/myCode/webServletBase/web/log";
            GB.laPath="C:/Program Files/Logic";
            GB.laAppName="Logic.exe";
            GB.chromePath="C:/Program Files/Google/Chrome/Application";
            GB.chromeAppName="chrome.exe";
            GB.chromeAddress="http://localhost:8080/webBase/index.jsp";
            //GB.logPath = "./log";
        }
        if (GB.osName.equals("linux")) {        
            GB.process_inx = 0; //0:consoleMain
            GB.os_inx = 1;     //0:windows 1://linux    
            GB.setdata_xml = "./setdata.xml";
            GB.setdata_db = "./setdata.db";
            GB.interfaces_path = "/etc/network/interfaces";
            GB.paraSetFullName = GB.linuxParaSetFullName;
            GB.logSetPath = "/home/admintx/syncSetExe";
            GB.logPath = "/home/admintx/syncSetExe/log";
            GB.laPath="/home/admintx/syncSetExe";
            GB.laAppName="la.sh";
            GB.chromePath="/usr/bin";
            GB.chromeAppName="google-chrome-stable --disable-session-crashed-bubble --no-sandbox";
            GB.chromeAddress="http://127.0.0.1";
            

        }
        //=============================================================
    }

    public static void loadPara2Form() {

        Class type;
        Object obj;
        int i;
        String str;
        String[] strA;
        String[][] strAA;

        //java.lang.reflect.Field[] f3 = cla.getClass().getDeclaredFields();
        java.lang.reflect.Field[] f3 = GB.class.getDeclaredFields();
        for (i = 0; i < f3.length; i++) {
            f3[i].setAccessible(true);
            try {
                obj = f3[i].get(GB.class);

                if (obj instanceof String[][]) {
                    str = f3[i].getName();
                    strAA = (String[][]) obj;
                    for (int j = 0; j < paraLen; j++) {
                        String[] sbufA;
                        sbufA = paraName[j].split("~");
                        if (sbufA.length == 3) {
                            if (str.equals(sbufA[0])) {
                                byte[] bytes = new byte[paraValue[j].length()];
                                String str1 = paraValue[j];
                                for (int m = 0; m < str1.length(); m++) {
                                    bytes[m] = (byte) str1.charAt(m);
                                }
                                strAA[Integer.parseInt(sbufA[1])][Integer.parseInt(sbufA[2])] = new String(bytes, Charset.forName("UTF-8"));
                            }
                        }
                    }
                } else if (obj instanceof String[]) {
                    str = f3[i].getName();
                    strA = (String[]) obj;
                    for (int j = 0; j < paraLen; j++) {
                        String[] sbufA;
                        sbufA = paraName[j].split("~");
                        if (sbufA.length == 2) {
                            if (str.equals(sbufA[0])) {
                                byte[] bytes = new byte[paraValue[j].length()];
                                String str1 = paraValue[j];
                                for (int m = 0; m < str1.length(); m++) {
                                    bytes[m] = (byte) str1.charAt(m);
                                }
                                strA[Integer.parseInt(sbufA[1])] = new String(bytes, Charset.forName("UTF-8"));
                            }
                        }
                    }
                } else if (obj instanceof int[]) {
                    str = f3[i].getName();
                    int[] intA = (int[]) obj;
                    for (int j = 0; j < paraLen; j++) {
                        String[] sbufA;
                        sbufA = paraName[j].split("~");
                        if (sbufA.length == 2) {
                            if (str.equals(sbufA[0])) {
                                intA[Integer.parseInt(sbufA[1])] = Integer.parseInt(paraValue[j]);
                            }
                        }
                    }
                } else if (obj instanceof String) {
                    str = f3[i].getName();

                    for (int j = 0; j < paraLen; j++) {
                        if (str.equals(paraName[j])) {
                            byte[] bytes = new byte[paraValue[j].length()];
                            String str1 = paraValue[j];
                            for (int m = 0; m < str1.length(); m++) {
                                bytes[m] = (byte) str1.charAt(m);
                            }
                            f3[i].set(GB.class, new String(bytes, Charset.forName("UTF-8")));
                        }
                    }
                } else if (obj instanceof Integer) {
                    str = f3[i].getName();
                    for (int j = 0; j < paraLen; j++) {
                        if (str.equals(paraName[j])) {
                            f3[i].set(GB.class, Integer.parseInt(paraValue[j]));
                        }
                    }
                } else {

                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(GB.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    static void clrPara() {
        for (int i = 0; i < GB.paraLen; i++) {
            GB.paraName[i] = null;
            GB.paraValue[i] = null;
            GB.paraLen = 0;
        }
    }

    static int newPara(String name, String value) {
        if (paraLen >= MAX_PARA_LEN) {
            return 0;
        }
        paraName[paraLen] = name;
        paraValue[paraLen] = value;
        paraLen++;
        return 1;
    }

    static int editPara(String name, String value) {
        int i;
        for (i = 0; i < paraLen; i++) {
            if (paraName[i].equals(name)) {
                paraValue[i] = value;
                return 1;
            }
        }
        return 0;
    }

    static int editNewPara(String name, String value) {
        if (editPara(name, value) == 0) {
            return newPara(name, value);
        }
        return 1;
    }

    static String getPara(String name) {
        int i;
        for (i = 0; i < paraLen; i++) {
            if (paraName[i].equals(name)) {
                return paraValue[i];
            }
        }
        return null;
    }

    static int deletePara(String name) {
        int i;
        for (i = 0; i < paraLen; i++) {
            if (paraName[i].equals(name)) {
                i++;
                for (; i < paraLen; i++) {
                    paraName[i - 1] = paraName[i];
                    paraValue[i - 1] = paraValue[i];
                }
                paraLen--;
                return 1;
            }
        }
        return 0;
    }
    
    
    

    public static void saveParaSet() {
        if(GB.paraSaveMap.size()==0)
            return;
        try {
            Gson gson = new Gson();
            String content = Lib.readFile(GB.paraSetFullName);
            JsonObject jsPara = JsonParser.parseString(content).getAsJsonObject();
            //=======================================================================
            String[] strA;
            String keyName="";
            String dataType="str";
            for (String key : GB.paraSaveMap.keySet()) {
                strA = key.split("~");
                keyName=strA[0];
                if(strA.length==2){
                    dataType=strA[1];
                }
                Object value=GB.paraSaveMap.get(key);
                jsPara.add(keyName, gson.toJsonTree(value));
                System.out.println("Save paraSet: "+keyName+ "  "+value);
            }
            GB.paraSaveMap.clear();
            //=======================================================================
            content = jsPara.toString();
            BufferedWriter outf = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(GB.paraSetFullName), "UTF-8"));
            try {
                outf.write(content);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                outf.close();
                //Path file = Paths.get(GB.paraSetPath);
                //BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                //GB.preParaSetTime = attr.lastModifiedTime().toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void checkParaSet() {
        try{
            Path file = Paths.get(GB.paraSetFullName);
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            String nowParaSetTime = attr.lastModifiedTime().toString();
            if (!GB.preParaSetTime.equals(nowParaSetTime)) {
                GB.preParaSetTime = nowParaSetTime;
                System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
                String content = Lib.readFile(GB.paraSetFullName);
                //GB.paraSetMap.clear();
                JSONObject jsPara = new JSONObject(content);
                Iterator<String> it = jsPara.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    GB.paraSetMap.put(key, jsPara.get(key));
                }
                GB.appId = (int) GB.paraSetMap.get("appId");
                GB.emulate = (int) GB.paraSetMap.get("emulate");

            }
        }
        catch(Exception ex){
            
        }
    }     
}
