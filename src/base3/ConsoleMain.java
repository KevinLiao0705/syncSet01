package base3;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.annotation.Native;
import javax.swing.JFrame;

//asterisk sound location: usr/share/asterisk/sounds/en/
//admintx 123456789 192.168.0.219
public class ConsoleMain {

    static ConsoleMain scla;
    Timer tm1 = null;
    ConsoleMainCmdExe cexe;
    Map<String, CmdTask> taskMap;
    Map<String, ChkRxA> rxMap;
    Map<String, CmdStatus> cmdStaMap;
    //SlotSta[] slotStaA = new SlotSta[16];
    int ctrIoPort = 23501;
    int myDeviceId = 0x2403;
    int devicePcioId = 0x2301;
    int mySerialId = 0x0000;
    String preCmdStr = "";
    KvComm ioComm;
    Uart uart0 = new Uart();
    Uart uart1 = new Uart();
    SyncData syncData = new SyncData();
    LogicThread logicThread = null;
    ChromeThread chromeThread = null;
    int easyCommand = 0;
    int easyParas = 0;
    int easyCommandTime = 0;
    int testUartTime = 0;
    int connectFpgaCnt = 0;
    int[] drvDataClrBuf = new int[36];
    int addBufDebugCnt = 0;
    int sysFlag0 = 0;
    int sysFlag1 = 0;
    short emuPowerValueCnt = 0;
    short emuSspaValueCnt = 0;
    int powerOn_f = 0;
    int moduleOn_f = 0;
    int writeBackParaSet_f = 0;
    String writeBackName = "";

    //===========================
    //dataToFpga
    /*
    u16 deviceId=25011
    u16 serialId=appId
    u16 groupId=0xAB00;
    u16 cmdLen(10+payload len)  
    u16 cmd
    u16 para0
    u16 para1
    u16 para2
    u16 para3
    paload
     */
    //command
    //command 1000:
    //* [openSspaPower -1]: open all enable sspaPower;
    //      [openSspaPower %inx]: open enable sspaPower of serial inx ;
    //* [closeSspaPower -1]: close all sspaPower;
    //      [closeSspaPower %inx]: close sspaPower of serial inx ;
    //* [openSspaModule -1]: open all enable sspaModule;
    //      [openSspaPower %inx]: open enable sspaModule of serial inx ;
    //* [closeSspaModule -1]: close all sspaModule;
    //      [closeSspaPower %inx]: close sspaModule of serial inx ;
    static public JSONObject wsCallBack(String userName, JSONObject mesJson, String actStr, JSONObject outJson) {
        try {
            outJson.put("systemName", "dummyTarget");
            String act = (String) mesJson.get("act");
            if (act.equals("tick")) {
                scla.transSyncData(outJson);
                return outJson;
            }

            Object obj = null;
            try {
                obj = mesJson.get("paras");
            } catch (Exception ex) {

            }
            JSONArray paras = null;
            if (obj != null) {
                paras = (JSONArray) obj;
            }

            outJson.put("status", "ok");
            if (GB.appId == 0) {
                if (act.equals("mastPulseEnable")) {
                    scla.setEasyCommand(0x200b, paras);
                    outJson.put("status", "ok");
                    return outJson;
                }
                if (act.equals("mastPulseDisable")) {
                    scla.setEasyCommand(0x200c, paras);
                    outJson.put("status", "ok");
                    return outJson;
                }
                if (act.equals("mastSubRadarSet")) {
                    scla.setEasyCommand(0x2020, paras);
                    outJson.put("status", "ok");
                    return outJson;
                }
                if (act.equals("mastSubRadarCtr")) {
                    scla.setEasyCommand(0x2021, paras);
                    outJson.put("status", "ok");
                    return outJson;
                }

            }

            if (act.equals("closeUi")) {
                //scla.cmdFunc("simulateKeyExit");
                System.exit(0);
                //return outJson;

            }

            if (act.equals("exeLogic")) {
                scla.cmdFunc("exeLogic");
                return outJson;
            }

            if (act.equals("powerDown")) {
                scla.cmdFunc("powerDown");
                return outJson;
            }
            
            if (act.equals("fullScreenOff")) {
                scla.cmdFunc("fullScreenOff");
                return outJson;
            }
            

            if (act.equals("selfTestStartAll")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2008, null);
                return outJson;
            }
            if (act.equals("selfTestStopAll")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2009, null);
                return outJson;
            }
            if (act.equals("selfTestSlot")) {
                scla.setEasyCommand(0x200a, paras);
                outJson.put("status", "ok");
                return outJson;
            }

            if (act.contains("RadiationOn")) {
                scla.setEasyCommand(0x2004, paras);
                outJson.put("status", "ok");
                return outJson;
            }
            if (act.contains("RadiationOff")) {
                scla.setEasyCommand(0x2005, null);
                outJson.put("status", "ok");
                return outJson;
            }

            if (GB.appId == 0) {
                return outJson;
            }

            int emergency = scla.syncData.systemStatus0 & (1 << 25);
            int ready_f = scla.syncData.systemStatus0 & (3 >> (GB.appId * 2));

            if (act.contains("SspaPowerOn")) {
                scla.setEasyCommand(0x2000, paras);
                return outJson;
            }
            if (act.contains("SspaPowerOff")) {
                scla.setEasyCommand(0x2001, paras);
                outJson.put("status", "ok");
                return outJson;
            }
            if (act.contains("SspaModuleOn")) {
                scla.setEasyCommand(0x2002, paras);
                outJson.put("status", "ok");
                return outJson;
            }
            if (act.contains("SspaModuleOff")) {
                scla.setEasyCommand(0x2003, paras);
                outJson.put("status", "ok");
                return outJson;
            }
            if (act.contains("remoteRadOn")) {
                scla.setEasyCommand(0x2010, paras);
                outJson.put("status", "ok");
                return outJson;
            }
            if (act.contains("remoteRadOff")) {
                scla.setEasyCommand(0x2011, null);
                outJson.put("status", "ok");
                return outJson;
            }

            if (act.contains("EmergencyOn")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2006, null);
                return outJson;
            }
            if (act.contains("EmergencyOff")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2007, null);
                return outJson;
            }
            if (act.contains("PulseSource")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x200d, paras);
                return outJson;
            }

            if (act.contains("sub1TxLoad")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2012, paras);
                return outJson;
            }
            if (act.contains("sub1BatShort")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2013, paras);
                return outJson;
            }

            if (act.contains("TxLoad")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x200e, paras);
                return outJson;
            }
            if (act.contains("BatShort")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x200f, paras);
                return outJson;
            }
            if (act.contains("SetFlags")) {
                outJson.put("status", "ok");
                scla.setEasyCommand(0x2020, paras);
                return outJson;
            }
            
            

        } catch (Exception ex) {

        }
        return outJson;

    }

    public void setEasyCommand(int cmd, int para0) {
        easyParas = para0;
        easyCommand = cmd;
        easyCommandTime = 0;

    }

    public void setEasyCommand(int cmd, JSONArray paras) {

        if (paras == null) {
            easyParas = 0;
            easyCommand = cmd;
            easyCommandTime = 0;
        } else {
            try {
                easyParas = (int) paras.get(0);
                easyCommand = cmd;
                easyCommandTime = 0;
            } catch (Exception ex) {
                return;
            }
        }

    }

    public void transSyncData(JSONObject outJson) {
        try {
            String paraSetStr = null;
            if (writeBackParaSet_f == 1) {
                File file = new File(GB.paraSetFullName);
                paraSetStr = Lib.readStringFile(GB.paraSetFullName);
                writeBackParaSet_f = 0;
            }

            if (GB.appId == 0) {
                KvJson kj = new KvJson();
                kj.jStart();
                kj.jadd("slotDataAA", syncData.slotDataAA);
                kj.jadd("systemStatus0", syncData.systemStatus0);
                kj.jadd("systemStatus1", syncData.systemStatus1);
                kj.jadd("gngga0", syncData.gngga0);
                kj.jadd("gngga1", syncData.gngga1);
                kj.jadd("gngga2", syncData.gngga2);
                kj.jadd("viewDatas", syncData.viewDatas);
                kj.jadd("conRxA", syncData.conRxA);
                kj.jadd("commOkRateA", syncData.commOkRateA);
                kj.jadd("subStatusA", syncData.subStatusA);

                int[] intA = new int[256];
                int pinx = 0;
                for (;;) {
                    int chgInx = (syncData.pulseFormAddBufA0Inx0 ^ syncData.pulseFormAddBufA0Inx1) & 255;
                    if (chgInx == 0) {
                        break;
                    }
                    intA[pinx++] = syncData.pulseFormAddBufA0[syncData.pulseFormAddBufA0Inx1 & 255];
                    syncData.pulseFormAddBufA0Inx1++;
                    if (pinx >= 256);
                    break;
                }
                kj.jadd("pulseFormAddBufA0", intA, pinx);
                intA[0] = syncData.pulseFormLowPeriod;
                intA[1] = syncData.pulseFormHighPeriod;
                intA[2] = syncData.pulseFormFreq;
                kj.jadd("pulseFormInf", intA, 3);

                if (paraSetStr != null) {
                    kj.jadd("paraSetStr#" + writeBackName, paraSetStr);
                }
                kj.jEnd();
                JSONObject syncJson = new JSONObject(kj.jstr);
                outJson.put("syncData", syncJson);
            }

            if (GB.appId == 1) {
                KvJson kj = new KvJson();
                kj.jStart();
                kj.jadd("slotDataAA", syncData.slotDataAA);
                kj.jadd("systemStatus0", syncData.systemStatus0);
                kj.jadd("systemStatus1", syncData.systemStatus1);
                kj.jadd("webReturnCmd",syncData.webReturnCmd );
                kj.jadd("webReturnCmdPara",syncData.webReturnCmdPara );
                if(syncData.webReturnCmd!=0)
                    syncData.webReturnCmd=0;
                kj.jadd("conRxA", syncData.conRxA);
                kj.jadd("gngga0", syncData.gngga0);
                kj.jadd("gngga1", syncData.gngga1);
                kj.jadd("gngga2", syncData.gngga2);
                kj.jadd("viewDatas", syncData.viewDatas);
                if (paraSetStr != null) {
                    kj.jadd("paraSetStr#" + writeBackName, paraSetStr);
                }
                int ii;
                if (syncData.commOkRateA[1] != 0) {
                    ii = syncData.commOkRateA[1];
                    int kk = ii;
                    int uu = kk;
                }

                kj.jadd("commOkRateA", syncData.commOkRateA);
                //================================
                int[] intA = new int[256];
                int pinx = 0;
                for (;;) {
                    int chgInx = (syncData.pulseFormAddBufA0Inx0 ^ syncData.pulseFormAddBufA0Inx1) & 255;
                    if (chgInx == 0) {
                        break;
                    }
                    intA[pinx++] = syncData.pulseFormAddBufA0[syncData.pulseFormAddBufA0Inx1 & 255];
                    syncData.pulseFormAddBufA0Inx1++;
                    if (pinx >= 256);
                    break;
                }
                kj.jadd("pulseFormAddBufA0", intA, pinx);
                intA[0] = syncData.pulseFormLowPeriod;
                intA[1] = syncData.pulseFormHighPeriod;
                intA[2] = syncData.pulseFormFreq;
                kj.jadd("pulseFormInf", intA, 3);
                //==========================================

                if (paraSetStr != null) {
                    kj.jadd("paraSetStr", paraSetStr);
                }
                kj.jEnd();
                JSONObject syncJson = new JSONObject(kj.jstr);
                outJson.put("syncData", syncJson);
            }

            if (GB.appId == 2) {
                if (GB.emuMeterStatus_f != 0) {
                    for (int i = 0; i < syncData.meterStatusAA.length; i++) {
                        syncData.meterStatusAA[i]++;
                        if (syncData.meterStatusAA[i] >= 1000) {
                            syncData.meterStatusAA[i] = 0;
                        }
                    }
                }
                /*
                    //0 connectFlag, 1 faultLed, 2:v50enLed, 3:v32enLed 
                    byte[] sspaPowerStatusAA = new byte[36];
                    short[] sspaPowerV50vAA = new short[36];
                    short[] sspaPowerV50iAA = new short[36];
                    short[] sspaPowerV50tAA = new short[36];
                    short[] sspaPowerV32vAA = new short[36];
                    short[] sspaPowerV32iAA = new short[36];
                    short[] sspaPowerV32tAA = new short[36];
                 */
                //=============================================

                int sf = 0x10;
                if (GB.emuPowerValue_f != 0) {
                    for (int i = 0; i < 36; i++) {
                        if (emuPowerValueCnt > 50) {
                            sf |= 0x01;
                        }
                        if (emuPowerValueCnt > 100) {
                            sf |= 0x02;
                        }
                        if (emuPowerValueCnt > 150) {
                            sf |= 0x04;
                        }
                        if (emuPowerValueCnt > 200) {
                            sf |= 0x08;
                        }
                        syncData.sspaPowerStatusAA[i] = (byte) sf;
                        syncData.sspaPowerV50vAA[i] = emuPowerValueCnt;
                        syncData.sspaPowerV50iAA[i] = emuPowerValueCnt;
                        syncData.sspaPowerV50tAA[i] = emuPowerValueCnt;
                        syncData.sspaPowerV32vAA[i] = emuPowerValueCnt;
                        syncData.sspaPowerV32iAA[i] = emuPowerValueCnt;
                        syncData.sspaPowerV32tAA[i] = emuPowerValueCnt;
                    }
                    if (++emuPowerValueCnt >= 300) {
                        emuPowerValueCnt = 0;
                    }
                }
                /*
                0:connect, 1:致能, 2 保護觸發, 3:工作比過高, 4:脈寬過高, 5:溫度過高, 6:反射過高, 
                byte[] sspaModuleStatusAA = new byte[36];
                short[] sspaModuleRfOutAA = new short[36];
                short[] sspaModuleTemprAA = new short[36];
                 */
                sf = 0;
                if (GB.emuSspaValue_f != 0) {
                    for (int i = 0; i < 36; i++) {
                        if (emuSspaValueCnt > 30) {
                            sf |= 0x01;
                        }
                        if (emuSspaValueCnt > 60) {
                            sf |= 0x02;
                        }
                        if (emuSspaValueCnt > 90) {
                            sf |= 0x04;
                        }
                        if (emuSspaValueCnt > 120) {
                            sf |= 0x08;
                        }
                        if (emuSspaValueCnt > 150) {
                            sf |= 0x10;
                        }
                        if (emuSspaValueCnt > 180) {
                            sf |= 0x20;
                        }
                        if (emuSspaValueCnt > 210) {
                            sf |= 0x40;
                        }

                        syncData.sspaModuleStatusAA[i] = (byte) sf;
                        syncData.sspaModuleRfOutAA[i] = emuSspaValueCnt;
                        syncData.sspaModuleTemprAA[i] = emuSspaValueCnt;
                    }
                    if (++emuSspaValueCnt >= 300) {
                        emuSspaValueCnt = 0;
                    }
                    //syncData.sspaModuleStatusAA[0]=(byte)0x00;
                    //syncData.sspaModuleStatusAA[1]=(byte)0x00;
                    //syncData.sspaModuleStatusAA[34]=(byte)0x00;
                    //syncData.sspaModuleStatusAA[35]=(byte)0x00;
                }

                KvJson kj = new KvJson();
                kj.jStart();
                kj.jadd("slotDataAA", syncData.slotDataAA);
                kj.jadd("systemStatus0", syncData.systemStatus0);
                kj.jadd("systemStatus1", syncData.systemStatus1);
                kj.jadd("enviStatusA", syncData.enviStatusA);
                kj.jadd("meterStatusAA", syncData.meterStatusAA);
                kj.jadd("sspaPowerStatusAA", syncData.sspaPowerStatusAA);
                kj.jadd("sspaPowerV50vAA", syncData.sspaPowerV50vAA);
                kj.jadd("sspaPowerV50iAA", syncData.sspaPowerV50iAA);
                kj.jadd("sspaPowerV50tAA", syncData.sspaPowerV50tAA);
                kj.jadd("sspaPowerV32vAA", syncData.sspaPowerV32vAA);
                kj.jadd("sspaPowerV32iAA", syncData.sspaPowerV32iAA);
                kj.jadd("sspaPowerV32tAA", syncData.sspaPowerV32tAA);
                kj.jadd("sspaModuleStatusAA", syncData.sspaModuleStatusAA);
                kj.jadd("sspaModuleRfOutAA", syncData.sspaModuleRfOutAA);
                kj.jadd("sspaModuleTemprAA", syncData.sspaModuleTemprAA);
                kj.jadd("viewDatas", syncData.viewDatas);
                kj.jadd("ioInA", syncData.ioInA);
                kj.jadd("conRxA", syncData.conRxA);
                //=================================
                /*
                syncData.pulseFormAddBufA0Len=6;
                syncData.pulseFormAddBufA0[0]=100000*2+1;
                syncData.pulseFormAddBufA0[1]=900000*2+0;
                syncData.pulseFormAddBufA0[2]=123000*2+1;
                syncData.pulseFormAddBufA0[3]=456000*2+0;
                syncData.pulseFormAddBufA0[4]=323000*2+1;
                syncData.pulseFormAddBufA0[5]=956000*2+0;
                 */

                int[] intA = new int[256];
                int pinx = 0;
                for (;;) {
                    int chgInx = (syncData.pulseFormAddBufA0Inx0 ^ syncData.pulseFormAddBufA0Inx1) & 255;
                    if (chgInx == 0) {
                        break;
                    }
                    intA[pinx++] = syncData.pulseFormAddBufA0[syncData.pulseFormAddBufA0Inx1 & 255];
                    syncData.pulseFormAddBufA0Inx1++;
                    if (pinx >= 256);
                    break;
                }
                kj.jadd("pulseFormAddBufA0", intA, pinx);
                intA[0] = syncData.pulseFormLowPeriod;
                intA[1] = syncData.pulseFormHighPeriod;
                intA[2] = syncData.pulseFormFreq;
                kj.jadd("pulseFormInf", intA, 3);

                if (paraSetStr != null) {
                    kj.jadd("paraSetStr#" + writeBackName, paraSetStr);
                }
                kj.jEnd();
                JSONObject syncJson = new JSONObject(kj.jstr);
                syncData.pulseFormAddBufA0Len = 0;
                outJson.put("syncData", syncJson);
            }
        } catch (Exception ex) {

        }
    }

    /*
    int[] slotIdA = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    // 0:none, 1:ready, 2:error 3:warn up
    int[] slotStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    //0:none, 1:PreTest,2:testing;
    int[] slotTestStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    
    
    
    int[] mastSystemStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] ctr1SystemStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] ctr2SystemStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] ctr1EnvStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] ctr2EnvStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[][] ctr1SspaPowerStatusAA = new int[36][];
    int[][] ctr2SspaPowerStatusAA = new int[36][];
    int[][] ctr1SspaModuleStatusAA = new int[36][];
    int[][] ctr2SspaModuleStatusAA = new int[36][];
    int[] ctr1MeterStatusA = new int[]{0, 0, 0, 0, 0, 0};
    int[] ctr2MeterStatusA = new int[]{0, 0, 0, 0, 0, 0};
    int[] mastGpsDataA = new int[]{22, 59, 59, 99, 122, 59, 59, 99, 123, 270};
    String mastGpsStatus = "";
    int[] sub1GpsDataA = new int[]{22, 59, 59, 99, 122, 59, 59, 99, 123, 270};
    String sub1GpsStatusA = "";
    int[] sub2GpsDataA = new int[]{22, 59, 59, 99, 122, 59, 59, 99, 123, 270};
    String sub2GpsStatus = "";
    int[] mastRadarStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] ctr1RadarStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] ctr2RadarStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] sub1CommStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] sub2CommStatusA = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     */
    public ConsoleMain() {
        scla = this;
    }

    public void create() {
        String str;
        String errStr;
        //=======================================================
        try {
            String content = Lib.readFile("paraSet.json");
            GB.paraSetMap.clear();
            //String content = stringBuilder.toString();
            JSONObject jsPara = new JSONObject(content);
            Iterator<String> it = jsPara.keys();
            while (it.hasNext()) {
                String key = it.next();
                GB.paraSetMap.put(key, jsPara.get(key));
            }
        } catch (Exception ex) {

        }

        final ConsoleMain cla = this;

        GB.webSocketAddr = (String) GB.paraSetMap.get("webSocketAddr");
        if (GB.webSocketAddr.length() == 0) {
            GB.webSocketAddr = GB.real_ip_str;
        }

        KvWebSocketServer.serverStart();
        //=======================================================
        rxMap = new HashMap<String, ChkRxA>();
        taskMap = new HashMap<String, CmdTask>();
        cmdStaMap = new HashMap<String, CmdStatus>();
        cexe = new ConsoleMainCmdExe(cla, taskMap);
        //=======================================
        ioComm = new KvComm("ioComm", "serverSocket");
        ioComm.serverSocket.format = 1;
        ioComm.serverSocket.rxcon_ltim = 100;
        ioComm.serverSocket.port = ctrIoPort;
        ioComm.serverSocket.stm.setCallBack(new BytesCallback() {
            @Override
            public String prg(byte[] bytes, int len) {
                gnRxPrg("", bytes, len);
                socketServerReturn();
                return null;

            }
        });
        ioComm.open();
        //=======================================
        if (cla.tm1 == null) {
            cla.tm1 = new Timer();
            tm1.schedule(new ConsoleMainTm1(cla), 1000, 20);
        }

        errStr = cmdFunc("openComPort0");
        if (errStr != null) {
            System.out.println(errStr);
        } else {
            System.out.println("open com port 0 ok.");
        }

        errStr = cmdFunc("openComPort1");
        if (errStr != null) {
            System.out.println(errStr);
        } else {
            System.out.println("open com port 1 ok.");
        }

        //cmdFunc("exeChrome");
        //=====================================
        System.out.println("ConsoleMain Ready.");

        while (true) {
            Scanner input = new Scanner(System.in);
            str = input.nextLine().trim();
            if (str.length() == 0) {
                continue;
            }
            errStr = cmdFunc(str);
            if (errStr != null) {
                System.out.println(errStr);
            }
        }
    }

    public int utxParaSet() {
        int deviceId = 25010;
        int serialId = 0;
        int groupId = 0xab00;
        int cmd = 0x1000;
        int para0 = (int) GB.paraSetMap.get("appId");
        int para1 = 0;
        int para2 = 0;
        int para3 = 0;
        return 0;

    }

    public String openUart0() {
        if (uart0.uartSeted_f != 0) {
            return null;
        }
        uart0.portName = "COM" + (int) GB.paraSetMap.get("uart0Port");
        if (GB.osName.equals("linux")) {
            uart0.portName = "ttyUSB0";
        }
        System.out.println(uart0.portName);

        uart0.boudrate = (int) GB.paraSetMap.get("uart0Boudrate");
        uart0.parity = "None";
        uart0.stopBit = 1;
        uart0.dataBit = 8;
        uart0.txEncMode = 1;
        uart0.rxEncMode = 1;
        //&w
        uart0.setCallBack(new BytesCallback() {
            @Override
            public String prg(byte[] bytes, int len) {
                int inx = 0;
                ByteLook bk = new ByteLook(bytes);
                int deviceId = bk.lookShortInt();
                int serialId = bk.lookShortInt();
                int groupId = bk.lookShortInt();
                int groupLen = bk.lookShortInt();
                int cmd = bk.lookShortInt();
                int para0 = bk.lookShortInt();
                int para1 = bk.lookShortInt();
                int para2 = bk.lookShortInt();
                int para3 = bk.lookShortInt();
                if (deviceId != 25010 || serialId != 0x0000) {
                    return null;
                }
                int ibuf = 0;
                int ibuf0, ibuf1, ibuf2, ibuf3;
                syncData.conRxA[24]++;
                if (cmd == 0x1000) {
                    if (para0 == 2)//fpgaId
                    {
                        connectFpgaCnt = 0;
                        for (int i = 0; i < 12; i++) {
                            syncData.slotDataAA[i] = bk.lookShort();
                        }
                        /*
                        ibuf0 = bk.lookInt() ^ syncData.systemStatus0;
                        ibuf0 = ibuf0 & 0xfe7fffff;
                        syncData.systemStatus0 ^= ibuf0;
                        ibuf0 = bk.lookInt() ^ syncData.systemStatus1;
                        ibuf0 = ibuf0 & 0x03ffffff;
                        syncData.systemStatus1 ^= ibuf0;
                         */
                        syncData.systemStatus0 = bk.lookInt();
                        syncData.systemStatus1 = bk.lookInt();
                        int ipcCmd = bk.lookShortInt();
                        int ipcCmdPara = bk.lookShortInt();
                        if (ipcCmd != 0) {
                            if (ipcCmd == 0x2012) {
                                GB.paraSaveMap.put("ctr1TxLoad", ipcCmdPara);
                                GB.saveParaSet();
                                GB.checkParaSet();
                                writeBackParaSet_f = 1;
                                writeBackName = "ctr1TxLoad";
                            }
                            if (ipcCmd == 0x2013) {
                                GB.paraSaveMap.put("ctr1BatShort", ipcCmdPara);
                                GB.saveParaSet();
                                writeBackParaSet_f = 1;
                                writeBackName = "ctr1BatShort";
                            }
                            if (ipcCmd == 0x2020) {
                                int ib=(ipcCmdPara>>2)&1;
                                GB.paraSaveMap.put("ctr1TxLoad", ib);
                                ib=(ipcCmdPara>>3)&1;
                                GB.paraSaveMap.put("ctr1BatShort", ib);
                                GB.saveParaSet();
                                writeBackParaSet_f = 1;
                                writeBackName = "ctr1TxLoad~ctr1BatShort";
                            }
                            
                            
                            
                            
                        }

                        for (;;) {
                            ibuf = bk.lookByteInt();
                            if (ibuf == 0xcd) {
                                break;
                            }
                            if ((ibuf == 0xaa)) {
                                ibuf = bk.lookByteInt();
                                syncData.enviStatusA[0] = bk.lookInt();
                                for (int i = 0; i < 6; i++) {
                                    if (GB.emuMeterStatus_f != 0) {
                                        bk.lookShort();
                                    } else {
                                        syncData.meterStatusAA[i] = bk.lookShort();
                                    }
                                }
                                continue;
                            }

                            if (ibuf == 0xab) {
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 36) {
                                    return null;
                                }
                                if (GB.emuPowerValue_f != 0) {
                                    bk.lookByte();
                                    bk.lookShort();
                                    bk.lookShort();
                                    bk.lookShort();
                                    bk.lookShort();
                                    bk.lookShort();
                                    bk.lookShort();
                                } else {
                                    syncData.sspaPowerStatusAA[ibuf] = bk.lookByte();
                                    syncData.sspaPowerV50vAA[ibuf] = bk.lookShort();
                                    syncData.sspaPowerV50iAA[ibuf] = bk.lookShort();
                                    syncData.sspaPowerV50tAA[ibuf] = bk.lookShort();
                                    syncData.sspaPowerV32vAA[ibuf] = bk.lookShort();
                                    syncData.sspaPowerV32iAA[ibuf] = bk.lookShort();
                                    syncData.sspaPowerV32tAA[ibuf] = bk.lookShort();
                                }
                                if (GB.emuSspaValue_f != 0) {
                                    bk.lookByte();
                                    bk.lookShort();
                                    bk.lookShort();
                                } else {
                                    syncData.sspaModuleStatusAA[ibuf] = bk.lookByte();
                                    syncData.sspaModuleRfOutAA[ibuf] = bk.lookShort();
                                    syncData.sspaModuleTemprAA[ibuf] = bk.lookShort();
                                }
                                drvDataClrBuf[ibuf] = 0;
                                continue;
                            }
                            if (ibuf == 0xac) {
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 8) {
                                    return null;
                                }
                                for (int i = 0; i < 8; i++) {
                                    syncData.viewDatas[ibuf * 8 + i] = bk.lookInt();
                                }
                                continue;
                            }

                            if (ibuf == 0xad || ibuf == 0xae || ibuf == 0xaf) {
                                byte[] byteA = null;
                                if (ibuf == 0xad) {
                                    byteA = syncData.gngga0;
                                }
                                if (ibuf == 0xae) {
                                    byteA = syncData.gngga1;
                                }
                                if (ibuf == 0xaf) {
                                    byteA = syncData.gngga2;
                                }
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 64) {
                                    return null;
                                }
                                for (int i = 0; i < ibuf; i++) {
                                    byteA[i] = bk.lookByte();
                                }
                                continue;
                            }

                            if (ibuf == 0xb0) {
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 32) {
                                    break;
                                }
                                for (int i = 0; i < ibuf; i++) {
                                    syncData.pulseFormAddBufA0[syncData.pulseFormAddBufA0Inx0 & 255] = bk.lookInt();
                                    syncData.pulseFormAddBufA0Inx0++;
                                }
                                continue;
                            }

                            if (ibuf == 0xb1) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 10) {
                                    break;
                                }
                                syncData.pulseFormLowPeriod = bk.lookInt();
                                syncData.pulseFormHighPeriod = bk.lookInt();
                                syncData.pulseFormFreq = bk.lookByte();
                                syncData.conRxA[18] = bk.lookByte();
                                continue;
                            }
                            if (ibuf == 0xb2) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 16) {
                                    break;
                                }
                                syncData.conRxA[0] = bk.lookByte();
                                syncData.conRxA[1] = bk.lookByte();
                                syncData.conRxA[2] = bk.lookByte();
                                syncData.conRxA[3] = bk.lookByte();
                                syncData.conRxA[4] = bk.lookByte();
                                syncData.conRxA[5] = bk.lookByte();
                                syncData.conRxA[6] = bk.lookByte();
                                syncData.conRxA[7] = bk.lookByte();
                                syncData.conRxA[8] = bk.lookByte();
                                syncData.conRxA[9] = bk.lookByte();
                                syncData.conRxA[10] = bk.lookByte();
                                syncData.conRxA[11] = bk.lookByte();
                                syncData.conRxA[12] = bk.lookByte();
                                syncData.conRxA[13] = bk.lookByte();
                                syncData.conRxA[14] = bk.lookByte();
                                syncData.conRxA[15] = bk.lookByte();
                                continue;
                            }

                            if (ibuf == 0xb4) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 12) {
                                    break;
                                }
                                for (int i = 0; i < 6; i++) {
                                    syncData.ioInA[i] = bk.lookShort();
                                }
                                continue;
                            }
                            break;
                        }

                    }

                    if (para0 == 1 || para0 == 0)//fpgaId
                    {
                        connectFpgaCnt = 0;
                        for (int i = 0; i < 12; i++) {
                            syncData.slotDataAA[i] = bk.lookShort();
                        }
                        /*
                        ibuf0 = bk.lookInt() ^ syncData.systemStatus0;
                        ibuf0 = ibuf0 & 0xfe7fffff;
                        syncData.systemStatus0 ^= ibuf0;
                        ibuf0 = bk.lookInt() ^ syncData.systemStatus1;
                        ibuf0 = ibuf0 & 0x03ffffff;
                        syncData.systemStatus1 ^= ibuf0;
                         */
                        syncData.systemStatus0 = bk.lookInt();
                        syncData.systemStatus1 = bk.lookInt();

                        short ipcCmd = bk.lookShort();
                        short ipcCmdPara = bk.lookShort();
                        if(ipcCmd!=0){
                            syncData.webReturnCmd=ipcCmd;
                            syncData.webReturnCmdPara=ipcCmdPara;
                            
                        }

                        for (;;) {
                            ibuf = bk.lookByteInt();
                            if (ibuf == 0xcd) {
                                break;
                            }
                            if (ibuf == 0xac) {
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 32) {
                                    return null;
                                }
                                for (int i = 0; i < 8; i++) {
                                    syncData.viewDatas[ibuf * 8 + i] = bk.lookInt();
                                }
                                continue;
                            }

                            if (ibuf == 0xad || ibuf == 0xae || ibuf == 0xaf) {
                                byte[] byteA = null;
                                if (ibuf == 0xad) {
                                    byteA = syncData.gngga0;
                                }
                                if (ibuf == 0xae) {
                                    byteA = syncData.gngga1;
                                }
                                if (ibuf == 0xaf) {
                                    byteA = syncData.gngga2;
                                }
                                if (para0 == 1) {
                                    int subNumber = (int) GB.paraSetMap.get("subNumber");
                                    if (subNumber == 0) {
                                        byteA = syncData.gngga1;
                                    } else {
                                        byteA = syncData.gngga2;
                                    }
                                }
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 64) {
                                    return null;
                                }
                                byteA[0] = 0;
                                for (int i = 0; i < ibuf; i++) {
                                    byteA[i] = bk.lookByte();
                                }
                                continue;
                            }

                            if (ibuf == 0xb0) {
                                ibuf = bk.lookByteInt();
                                if (ibuf >= 32) {
                                    break;
                                }
                                for (int i = 0; i < ibuf; i++) {
                                    syncData.pulseFormAddBufA0[syncData.pulseFormAddBufA0Inx0 & 255] = bk.lookInt();
                                    syncData.pulseFormAddBufA0Inx0++;
                                }
                                continue;
                            }

                            if (ibuf == 0xb1) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 10) {
                                    break;
                                }
                                syncData.pulseFormLowPeriod = bk.lookInt();
                                syncData.pulseFormHighPeriod = bk.lookInt();
                                syncData.pulseFormFreq = bk.lookByte();
                                syncData.conRxA[18] = bk.lookByte();
                                continue;
                            }
                            if (ibuf == 0xb2) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 10) {
                                    break;
                                }
                                syncData.conRxA[0] = bk.lookByte();
                                syncData.conRxA[1] = bk.lookByte();
                                syncData.conRxA[2] = bk.lookByte();
                                syncData.conRxA[3] = bk.lookByte();
                                syncData.conRxA[4] = bk.lookByte();
                                syncData.conRxA[5] = bk.lookByte();
                                syncData.conRxA[6] = bk.lookByte();
                                syncData.conRxA[7] = bk.lookByte();
                                syncData.conRxA[16] = bk.lookByte();
                                syncData.conRxA[17] = bk.lookByte();
                                continue;
                            }

                            if (ibuf == 0xb3) {
                                ibuf = bk.lookByteInt();
                                syncData.commOkRateA[0] = bk.lookShort();
                                syncData.commOkRateA[1] = bk.lookShort();
                                if (syncData.commOkRateA[0] <= 997) {
                                    int bb = 1;
                                }

                                continue;
                            }
                            if (ibuf == 0xb4) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 12) {
                                    break;
                                }
                                for (int i = 0; i < 6; i++) {
                                    syncData.ioInA[i] = bk.lookShort();
                                }
                                continue;
                            }
                            if (ibuf == 0xb5) {
                                ibuf = bk.lookByteInt();
                                if (ibuf != 8) {
                                    break;
                                }
                                for (int i = 0; i < 2; i++) {
                                    syncData.subStatusA[i] = bk.lookInt();
                                }
                                continue;
                            }

                            break;

                        }

                    }

                }

                uart0.rxSerialCnt++;
                uart0.rxSerialCnt &= 0xffff;
                if (uart0.rxSerialCnt != para1) {
                    uart0.rxErrCnt++;
                }
                uart0.rxSerialCnt = para1;
                uart0.rxPackageCnt++;
                uart0.uartConnectTime = 0;
                if (uart0.uartConnected_f == 0) {
                    System.out.print("uart0 connected.\n");
                    uart0.uartConnected_f = 1;
                }

                if ((uart0.rxPackageCnt
                        % 100) == 0) {
                    /*
                    System.out.print(" uart0Rx-" + uart0.rxErrCnt);
                    if ((uart0.rxPackageCnt % 1000) == 0) {
                        System.out.print("\n");
                    }
                     */
                }
                //===============================================================
                String preText;
                String preText1;

                try {
                    if (cmd == 0x1000) {//tick
                        uart0.txDeviceId = deviceId;
                        uart0.txSerialId = serialId;
                        uart0.txGroupId = 0xac00;
                        uart0.txCmd = cmd;
                        uart0.txPara0 = para0;  //fpgaId
                        uart0.txPara1 = para1;  //serialCnt
                        uart0.txPara2 = easyCommand; //easy command 
                        uart0.txPara3 = easyParas; //eaay command paras
                        uart0.txBufferLen = 0;
                        //================================================
                        if (easyCommand != 0) {
                            int xibuf = easyCommand;
                            easyCommand = 0;
                        }

                        int systemFlag0 = 0;
                        int systemFlag1 = 0;
                        preText = "";
                        preText1 = "";
                        if (para0 != -1) {//fpgaId
                            preText = "ctr1";
                            preText1 = "sub1";
                            //======
                            ibuf = (int) GB.paraSetMap.get("emulate");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 0;
                            //======
                            ibuf = (int) GB.paraSetMap.get("ctr1Remote");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 2;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("ctr2Remote");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 3;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("mastPulseSource");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 4;
                            ibuf = (int) GB.paraSetMap.get("sub1PulseSource");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 5;
                            ibuf = (int) GB.paraSetMap.get("ctr1PulseSource");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 6;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("ctr1BatShort");//戰備短路 0:關閉 1:開啟
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 7;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("ctr1TxLoad");//輸出裝置 0:假負載 1:天線
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 8;

                            ibuf = (int) GB.paraSetMap.get("sp4tCnt");//
                            ibuf &= 7;
                            systemFlag0 |= ibuf << 9;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("mastToSub1CommType");
                            ibuf &= 3;
                            systemFlag0 |= ibuf << 13;
                            ibuf = (int) GB.paraSetMap.get("mastToSub2CommType");
                            ibuf &= 3;
                            systemFlag0 |= ibuf << 15;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("sub1ChCommSet");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 17;
                            ibuf = (int) GB.paraSetMap.get("sub2ChCommSet");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 18;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("sub1CommType");
                            ibuf &= 3;
                            systemFlag0 |= ibuf << 19;
                            ibuf = (int) GB.paraSetMap.get("sub2CommType");
                            ibuf &= 3;
                            systemFlag0 |= ibuf << 21;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("sub1ChSyncType");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 23;
                            ibuf = (int) GB.paraSetMap.get("sub2ChSyncType");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 24;
                            //=======
                            ibuf = (int) GB.paraSetMap.get("mastToSub1SpeechEnable");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 25;
                            ibuf = (int) GB.paraSetMap.get("mastToSub2SpeechEnable");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 26;

                            ibuf = (int) GB.paraSetMap.get("wgProtectFlag");
                            ibuf &= 1;
                            systemFlag0 |= ibuf << 27;

                            ibuf = (int) GB.paraSetMap.get("rfDriverOutChannel");
                            ibuf &= 3;
                            systemFlag0 |= ibuf << 28;

                            sysFlag0 = systemFlag0;
                            sysFlag1 = systemFlag1;

                            //=============================================
                            int sspaPowerV32OnDly = (int) GB.paraSetMap.get(preText + "SspaPowerV32OnDly");//32V 延遲啟動時間 unit 0.1s 8bit
                            int sspaPowerV50OffDly = (int) GB.paraSetMap.get(preText + "SspaPowerV50OffDly");//32V 延遲關閉時間 unit 0.1s 8bit
                            int attenuator = (int) GB.paraSetMap.get(preText + "Attenuator");//衰減器   
                            //=============================================
                            JSONArray jarr = (JSONArray) GB.paraSetMap.get(preText + "SspaPowerExistA");
                            byte[] sspaPowerExistA = new byte[]{0, 0, 0, 0, 0};
                            for (int i = 0; i < 36; i++) {
                                ibuf = (int) jarr.get(i);
                                if (ibuf == 0) {
                                    continue;
                                }
                                sspaPowerExistA[(int) (i / 8)] |= 1 << (i % 8);
                            }
                            //=============================================
                            jarr = (JSONArray) GB.paraSetMap.get(preText + "SspaModuleExistA");
                            byte[] sspaModuleExistA = new byte[]{0, 0, 0, 0, 0};
                            for (int i = 0; i < 36; i++) {
                                ibuf = (int) jarr.get(i);
                                if (ibuf == 0) {
                                    continue;
                                }
                                sspaModuleExistA[(int) (i / 8)] |= 1 << (i % 8);
                            }
                            //=====================================
                            int preTrigTime = (int) GB.paraSetMap.get("preTrigTime");
                            int preRfOutTime = (int) GB.paraSetMap.get("preRfOutTime");
                            int afterTrigTime = (int) GB.paraSetMap.get("preRfOutTime");
                            //=====================================
                            //int commTestPacks = (int) GB.paraSetMap.get("commTestPacks");
                            int moduleOkLimit = (int) GB.paraSetMap.get("moduleOkLimit");
                            int vgTimeDelay = (int) GB.paraSetMap.get("vgTimeDelay");
                            int chTimeFineTune = (int) GB.paraSetMap.get(preText1 + "ChTimeFineTune");
                            int chFiberDelay = (int) GB.paraSetMap.get(preText1 + "ChFiberDelay");
                            int chRfDelay = (int) GB.paraSetMap.get(preText1 + "ChRfDelay");
                            int sub1ChRfTxCh = (int) GB.paraSetMap.get("sub1" + "ChRfTxCh");
                            int sub2ChRfTxCh = (int) GB.paraSetMap.get("sub2" + "ChRfTxCh");
                            int sub1ChRfRxCh = (int) GB.paraSetMap.get("sub1" + "ChRfRxCh");
                            int sub2ChRfRxCh = (int) GB.paraSetMap.get("sub2" + "ChRfRxCh");
                            int laGroupCh = (int) GB.paraSetMap.get("laGroupCh");
                            int subChDelay = (int) GB.paraSetMap.get("subChDelay");
                            int ctrChDelay = (int) GB.paraSetMap.get("ctrChDelay");
                            int drvChDelay = (int) GB.paraSetMap.get("drvChDelay");
                            int meterChDelay = (int) GB.paraSetMap.get("meterChDelay");

                            //=====================================
                            int pulseGenCh = (int) GB.paraSetMap.get("localPulseGenCh");//pulseGenCh   
                            if (uart0.txAltPackCnt >= 32) {
                                uart0.txAltPackCnt = 0;
                            }
                            jarr = (JSONArray) GB.paraSetMap.get("localPulseGenParas");
                            String str;
                            try {
                                str = (String) jarr.get(uart0.txAltPackCnt);
                            } catch (Exception ex) {
                                str = "0 0 1.0 3.0 1";
                            }
                            String[] strA = str.split(" ");
                            int widthCh = Lib.str2int(strA[1], 0);
                            //=====================================
                            jarr = (JSONArray) GB.paraSetMap.get("localPulseWidthParas");
                            String strw = "10";
                            try {
                                strw = (String) jarr.get(widthCh);
                            } catch (Exception ex) {
                            }
                            int pulseWidth = Math.round((Lib.str2float(strw, 10) * 10));//16bit
                            //=====================================
                            int freq = Math.round((Lib.str2float(strA[3], 3) * 100)) - 290;//8bit
                            int trigTimes = Lib.str2int(strA[4], 1);//8 bit
                            int dutyReg = Math.round((Lib.str2float(strA[2], 1) * 10));//16bit
                            int pri = Math.round(pulseWidth * 1000 / dutyReg);
                            pri &= 0x00ffffff;
                            ibuf = Lib.str2int(strA[0], 0) & 1;//enable_f
                            pri |= ibuf << 24;
                            //===============================
                            /*
                            systemFlag0 = 0x12345678;
                            systemFlag1 = 0x12345678;
                            sspaPowerV32OnDly = 0x56;
                            sspaPowerV50OffDly = 0x78;
                            attenuator = 0x9a;

                            sspaPowerExistA[0] = (byte) 0x01;
                            sspaPowerExistA[1] = (byte) 0x23;
                            sspaPowerExistA[2] = (byte) 0x45;
                            sspaPowerExistA[3] = (byte) 0x67;
                            sspaPowerExistA[4] = (byte) 0x89;

                            sspaModuleExistA[0] = (byte) 0x01;
                            sspaModuleExistA[1] = (byte) 0x23;
                            sspaModuleExistA[2] = (byte) 0x45;
                            sspaModuleExistA[3] = (byte) 0x67;
                            sspaModuleExistA[4] = (byte) 0x89;

                            pulseGenCh = 255;
                            
                            dutyReg = uart0.txAltPackCnt;
                            pulseWidth = uart0.txAltPackCnt * 256 + 1;
                            freq = uart0.txAltPackCnt;
                            trigTimes = uart0.txAltPackCnt;
                             */
                            //============================================
                            ByteLoad lb = new ByteLoad(uart0.txBuffer);
                            lb.wIntInt(systemFlag0);
                            lb.wIntInt(systemFlag1);
                            lb.wByteInt(sspaPowerV32OnDly);
                            lb.wByteInt(sspaPowerV50OffDly);
                            lb.wByteInt(attenuator);
                            //=========================================
                            for (int i = 0; i < 5; i++) {
                                lb.wByteInt(sspaPowerExistA[i]);
                            }
                            for (int i = 0; i < 5; i++) {
                                lb.wByteInt(sspaModuleExistA[i]);
                            }
                            //=========================================
                            lb.wShortInt(preTrigTime);
                            lb.wByteInt(preRfOutTime);
                            lb.wByteInt(afterTrigTime);
                            //=========================================
                            //lb.wShortInt(commTestPacks);
                            lb.wByteInt(moduleOkLimit);
                            lb.wByteInt(0);//spare
                            lb.wShortInt(vgTimeDelay);
                            lb.wShortInt(chTimeFineTune);
                            lb.wShortInt(chFiberDelay);
                            lb.wShortInt(chRfDelay);
                            lb.wByteInt(sub1ChRfTxCh);
                            lb.wByteInt(sub2ChRfTxCh);
                            lb.wByteInt(sub1ChRfRxCh);
                            lb.wByteInt(sub2ChRfRxCh);
                            //==========================================
                            lb.wByteInt(laGroupCh);
                            lb.wByteInt(subChDelay);
                            lb.wByteInt(ctrChDelay);
                            lb.wByteInt(drvChDelay);
                            lb.wByteInt(meterChDelay);
                            //=========================================
                            lb.wByteInt(0xab);
                            if (uart0.txAltPackCnt >= 32) {
                                uart0.txAltPackCnt = 0;
                            }
                            lb.wByteInt(uart0.txAltPackCnt);
                            uart0.txAltPackCnt++;
                            lb.wIntInt(pri);
                            lb.wShortInt(pulseWidth);   //unit 0.1us
                            lb.wByteInt(freq);
                            lb.wByteInt(trigTimes);     //

                            lb.wByteInt(0xcd);
                            uart0.txBufferLen = lb.inx;
                        }

                        uart0.encSend();
                        return null;
                    }
                } catch (Exception ex) {

                }
                //===============================================================
                //uart0.encSend(new byte[]{0x23, 0x02, 0x00, 0x02, 0x00, 0x00, 0x01}, 7);

                return null;
            }
        });
        String errStr = uart0.open();
        return errStr;
    }

    public String openUart1() {
        if (uart1.uartSeted_f != 0) {
            return null;
        }
        uart1.portName = "COM" + (int) GB.paraSetMap.get("uart1Port");
        if (GB.osName.equals("linux")) {
            uart1.portName = "ttyACM2";
        }
        System.out.println(uart1.portName);

        uart1.boudrate = (int) 115200;
        uart1.parity = "None";
        uart1.stopBit = 1;
        uart1.dataBit = 8;
        uart1.txEncMode = 1;
        uart1.rxEncMode = 1;
        //&w
        uart1.setCallBack(new BytesCallback() {
            @Override
            public String prg(byte[] bytes, int len) {
                int inx = 0;
                ByteLook bk = new ByteLook(bytes);
                int deviceId = bk.lookShortInt();
                int serialId = bk.lookShortInt();
                int groupId = bk.lookShortInt();
                int groupLen = bk.lookShortInt();
                int cmd = bk.lookShortInt();
                int para0 = bk.lookShortInt();
                int para1 = bk.lookShortInt();
                int para2 = bk.lookShortInt();
                int para3 = bk.lookShortInt();
                if (deviceId != 0x1737 || serialId != 0x0000) {
                    return null;
                }
                if (groupId != 0xab00) {
                    return null;
                }

                syncData.conRxA[25]++;

                if (cmd != 0x1000) {
                    return null;
                }
                int ibuf = 0;
                int ibuf0, ibuf1, ibuf2, ibuf3;
                uart1.rxSerialCnt++;
                uart1.rxSerialCnt &= 0xffff;
                if (uart1.rxSerialCnt != para1) {
                    uart1.rxErrCnt++;
                }
                uart1.rxSerialCnt = para1;
                uart1.rxPackageCnt++;
                if ((uart1.rxPackageCnt
                        % 100) == 0) {
                    System.out.print(" uart1Rx-" + uart1.rxErrCnt);
                    if ((uart1.rxPackageCnt % 1000) == 0) {
                        System.out.print("\n");
                    }
                }
                //===============================================================
                /*
                    0x01:0x02:0x03:0x04:0x05:0x06 POWER ,LOCAL/REMOTE, ENABLE, RADIATION, ANT, EMERGENCY                    
                    bit 0~3 press key
                    bit 4~7 release key
                    bit 8~11 continue key
                
                
                 */
                if (para0 != 0) {
                    int keyInId = para0;//
                    if (keyInId == 1) {
                        if (scla.powerOn_f == 0) {
                            scla.setEasyCommand(0x2000, -1);
                        } else {
                            scla.setEasyCommand(0x2001, -1);
                        }
                    }
                    if (keyInId == 2) {
                        //ibuf=Lib.readParaSet("ctr1PulseSource", -1);
                        ibuf = (int) GB.paraSetMap.get("ctr1PulseSource");
                        if (ibuf >= 0) {
                            ibuf ^= 1;
                            ibuf = (int) GB.paraSetMap.put("ctr1PulseSource", ibuf);
                            Lib.writeParaSet("ctr1PulseSource", ibuf);
                        }

                    }
                    if (keyInId == 3) {
                        if (scla.moduleOn_f == 0) {
                            scla.setEasyCommand(0x2002, -1);
                        } else {
                            scla.setEasyCommand(0x2003, -1);
                        }
                    }
                    if (keyInId == 4) {
                        if ((syncData.systemStatus0 & (1 << 25)) == 0) {
                            ibuf = (int) GB.paraSetMap.get("ctr1PulseSource");
                            int ibx = (int) GB.paraSetMap.get("localPulseGenCh");
                            if (ibuf == 0) {
                                scla.setEasyCommand(0x2004, 254);
                            } else {
                                scla.setEasyCommand(0x2004, ibx);
                            }
                        } else {
                            scla.setEasyCommand(0x2005, 0);
                        }
                    }
                    if (keyInId == 5) {
                        //ibuf=Lib.readParaSet("ctr1PulseSource", -1);
                        ibuf = (int) GB.paraSetMap.get("ctr1TxLoad");
                        if (ibuf < 0) {
                            return null;
                        }
                        ibuf ^= 1;
                        ibuf = (int) GB.paraSetMap.put("ctr1TxLoad", ibuf);
                        Lib.writeParaSet("ctr1TxLoad", ibuf);
                    }
                    if (keyInId == 6) {
                        if ((syncData.systemStatus0 & (1 << 26)) == 0) {
                            scla.setEasyCommand(0x2006, 0);
                        } else {
                            scla.setEasyCommand(0x2007, 0);
                        }
                    }
                }
                String preText;
                String preText1;

                try {
                    if (cmd == 0x1000) {//tick
                        uart1.txDeviceId = myDeviceId;
                        uart1.txSerialId = mySerialId;
                        uart1.txGroupId = 0xac00;
                        uart1.txCmd = cmd;
                        uart1.txPara0 = 0;
                        if (powerOn_f != 0) {
                            uart1.txPara0 |= (1 << 0);
                        }
                        if ((sysFlag0 & (1 << 6)) == 0) {
                            uart1.txPara0 |= (1 << 1);
                        }
                        if (moduleOn_f != 0) {
                            uart1.txPara0 |= (1 << 2);
                        }
                        if ((syncData.systemStatus0 & (1 << 25)) != 0) {
                            uart1.txPara0 |= (1 << 3);
                        }
                        if ((sysFlag0 & (1 << 8)) != 0) {
                            uart1.txPara0 |= (1 << 4);
                        }
                        if ((syncData.systemStatus0 & (1 << 26)) != 0) {
                            uart1.txPara0 |= (1 << 5);
                        }
                        //========================
                        uart1.txPara0 |= (1 << 8);
                        if ((sysFlag0 & (1 << 6)) != 0) {
                            uart1.txPara0 |= (1 << 9);
                        }
                        if ((sysFlag0 & (1 << 6)) == 0) {
                            uart1.txPara0 |= (1 << 10);
                        }
                        if ((sysFlag0 & (1 << 8)) == 0) {
                            uart1.txPara0 |= (1 << 11);
                        }
                        if ((sysFlag0 & (1 << 8)) != 0) {
                            uart1.txPara0 |= (1 << 12);
                        }
                        if ((sysFlag0 & (1 << 7)) != 0) {
                            uart1.txPara0 |= (1 << 13);
                        }
                        if ((syncData.systemStatus0 & (1 << 25)) != 0) {
                            uart1.txPara0 |= (1 << 14);
                        }

                        uart1.txPara1 = 0x0000;
                        uart1.txPara2 = 0x0000;
                        uart1.txPara3 = 0x0000;
                        uart1.txBufferLen = 0;
                        uart1.encSend();
                        return null;
                    }
                } catch (Exception ex) {

                }
                //===============================================================
                //uart1.encSend(new byte[]{0x23, 0x02, 0x00, 0x02, 0x00, 0x00, 0x01}, 7);

                return null;
            }
        });
        String errStr = uart1.open();
        return errStr;
    }

    public void txUartTest() {
        uart0.txDeviceId = 0x1234;
        uart0.txSerialId = 0x5678;
        uart0.txGroupId = 0xac00;
        uart0.txCmd = 0xabcd;
        uart0.txPara0 = 0x0001;  //fpgaId
        uart0.txPara1 = 0x0002;  //serialCnt
        uart0.txPara2 = 0x0003; //easy command 
        uart0.txPara3 = 0x0004; //eaay command paras
        uart0.txBufferLen = 0;
        uart0.encSend();

    }

    public void socketServerReturn() {
        byte[] sockUartData_buf = new byte[22];
        int inx = 0;
        sockUartData_buf[inx++] = (byte) ((myDeviceId) & 255);
        sockUartData_buf[inx++] = (byte) ((myDeviceId >> 8) & 255);
        sockUartData_buf[inx++] = (byte) ((mySerialId) & 255);
        sockUartData_buf[inx++] = (byte) ((mySerialId >> 8) & 255);
        int groupFlag = 0xAB00;
        sockUartData_buf[inx++] = (byte) ((groupFlag) & 255);
        sockUartData_buf[inx++] = (byte) ((groupFlag >> 8) & 255);
        int payLoadLen = 14;
        sockUartData_buf[inx++] = (byte) ((payLoadLen) & 255);
        sockUartData_buf[inx++] = (byte) ((payLoadLen >> 8) & 255);
        int cmdInx = 0x1000;
        sockUartData_buf[inx++] = (byte) ((cmdInx) & 255);
        sockUartData_buf[inx++] = (byte) ((cmdInx >> 8) & 255);

        sockUartData_buf[inx++] = (byte) ((GB.realIp[0]) & 255);
        sockUartData_buf[inx++] = (byte) ((GB.realIp[1]) & 255);
        sockUartData_buf[inx++] = (byte) ((GB.realIp[2]) & 255);
        sockUartData_buf[inx++] = (byte) ((GB.realIp[3]) & 255);

        int sockUartData_len = inx;

        MyStm stm = ioComm.serverSocket.stm;
        int stx_index = 0;
        stm.tbuf[stx_index++] = (byte) ((devicePcioId) & 255);
        stm.tbuf[stx_index++] = (byte) ((devicePcioId >> 8) & 255);
        stm.tbuf[stx_index++] = (byte) (255);
        stm.tbuf[stx_index++] = (byte) (255);

        stm.tbuf[stx_index++] = (byte) (0x10);//uart0
        stm.tbuf[stx_index++] = (byte) (0x00);//flag
        stm.tbuf[stx_index++] = (byte) (sockUartData_len & 255);//len low byte
        stm.tbuf[stx_index++] = (byte) ((sockUartData_len >> 8) & 255);//len high byte
        for (int i = 0; i < sockUartData_len; i++) {
            stm.tbuf[stx_index++] = sockUartData_buf[i];
        }
        stm.tbuf_byte = stx_index;
        ioComm.serverSocket.txReturn();
    }

    public String gnRxPrg(String name, byte[] bts, int len) {
        ConsoleMain cla = this;
        int inx = 0;
        int inxLim = len;
        int packageId = (bts[inx + 1] & 255) * 256 + (bts[inx + 0] & 255);
        int packageSerialId = (bts[inx + 3] & 255) * 256 + (bts[inx + 2] & 255);
        int packageGroupId = (bts[inx + 4] & 255);
        int packageFlags = (bts[inx + 5] & 255);
        int packageLen = (bts[inx + 7] & 255) * 256 + (bts[inx + 6] & 255);
        inx += 8;
        if (packageId == 0x2303 && packageGroupId == 0x10) {//from Pcio uart11
            int deviceId = (bts[inx + 1] & 255) * 256 + (bts[inx + 0] & 255);
            int deviceSerialId = (bts[inx + 3] & 255) * 256 + (bts[inx + 2] & 255);
            inx += 4;
            if (deviceId == myDeviceId && deviceSerialId == 0x00) {//from slot device
                while (inx + 4 < inxLim) {
                    int groupFlag = (bts[inx + 1] & 255) * 256 + (bts[inx + 0] & 255);
                    int dataLen = (bts[inx + 3] & 255) * 256 + (bts[inx + 2] & 255);
                    int ix = inx + 4;
                    if (groupFlag == 0xAB00) {//dataBeginId
                        int cmdInx = (bts[ix + 1] & 255) * 256 + (bts[ix + 0] & 255);
                        ix += 2;
                        if (cmdInx == 0x1000) {
                        }
                        ix += 8;
                    }
                    inx += dataLen + 4;
                }
            }
        }
        return null;
    }

    public String cmdPrg(String cmdstr) {
        String errStr = null;
        String content = null;
        if (cmdstr.equals("exit")) {
            System.exit(0);
            return errStr;
        }
        if (cmdstr.equals("quit")) {
            System.exit(0);
            return errStr;
        }
        String[] strCmdA = cmdstr.split(" ");

        if (strCmdA[0].equals("changeIp")) {

            //String winCmds="netsh interface ip set address name=乙太網路 source=static addr="+ipStr;
            //String winCmds = "netsh interface ip set address name=區域連線 source=static addr=" + strCmdA[1];
            System.out.print("\nChange " + strCmdA[1] + " Ip to " + strCmdA[2]);
            if (GB.os_inx == 0) {
                String winCmds = "netsh interface ip set address name=" + strCmdA[1] + " source=static addr=" + strCmdA[2];
                Process pp;
                try {
                } catch (Exception ex) {
                }
            } else {
                String cmdStr = "sudo /usr/sbin/ifconfig " + strCmdA[1] + " " + strCmdA[2] + " netmask " + GB.maskStr;
                System.out.print("\n" + cmdStr);
                Lib.wrInterfaces(strCmdA[1], strCmdA[2], GB.maskStr, GB.gatewayStr);
                if (Lib.exe(cmdStr) == 0) {
                    System.out.print("\nChange " + strCmdA[1] + " Ip to " + strCmdA[2] + " OK.");
                } else {
                    System.out.print("\nChange " + strCmdA[1] + " Ip to " + strCmdA[2] + " Error !!! ");
                }
            }
            return errStr;
        }
        return errStr;
    }

    public void addTask(String[] strCmdA, int retryAmt, int retryDly) {
        CmdTask task1 = new CmdTask(strCmdA[0]);
        for (int i = 1; i < strCmdA.length; i++) {
            task1.paras[i - 1] = strCmdA[i];
        }
        task1.retryAmt = retryAmt;
        task1.retryDly = retryDly;
        cexe.addMap(task1);
    }

    public String cmdFunc(String cmdstr) {
        final ConsoleMain cla = this;
        String errStr = null;
        String cmd = "";
        if (cmdstr.equals(":")) {
            cmdstr = preCmdStr;
        }
        preCmdStr = cmdstr;
        if (cmdstr.equals("exit")) {
            System.exit(0);
            return errStr;
        }

        if (cmdstr.equals("simulateKeyExit")) {
            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_F4);
                robot.keyRelease(KeyEvent.VK_F4);
                robot.keyRelease(KeyEvent.VK_CONTROL);
            } catch (AWTException e) {
                e.printStackTrace();
            }

        }

        if (cmdstr.equals("listComPort")) {
            String[] list = Uart.listComPort();
            String str = "ComPort: ";
            for (int i = 0; i < list.length; i++) {
                if (i != 0) {
                    str += ", ";
                }
                str += list[i];
            }
            System.out.println(str);
            return errStr;
        }

        if (cmdstr.equals("openComPort0")) {
            errStr = openUart0();
            return errStr;
        }
        if (cmdstr.equals("openComPort1")) {
            errStr = openUart1();
            return errStr;
        }
        if (cmdstr.equals("closeComPort0")) {
            uart0.close();
            return errStr;
        }
        if (cmdstr.equals("sendComTest")) {
            uart0.send("uart0 tx test");
            return errStr;
        }

        if (cmdstr.equals("test1")) {
            try {
                Path file = Paths.get(GB.paraSetFullName);
                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class
                );
                System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
            } catch (Exception ex) {

            }
            return errStr;
        }

        if (cmdstr.equals("testExe")) {
            int exist = Lib.chkProcessExist("Logic.exe");
            return errStr;
        }

        if (cmdstr.equals("powerDown")) {
            String cmdStr = "sudo shutdown -h now";
            Lib.exe(cmdStr);
            return errStr;
        }
        
        if (cmdstr.equals("fullScreenOff")) {
            /*
            String cmdStr = "sudo pkill -f kiosk";
            Lib.exe(cmdStr);
            cmdStr = "/usr/bin/google-chrome-stable --disable-session-crashed-bubble http://127.0.0.1";
            Lib.exe(cmdStr);
            */
            return errStr;
        }
        if(cmdstr.equals("exeChromeXxx")){
            String exeStr = "";
            String exePath = GB.chromePath + "/";
            Process process = null;
            try {
                if (GB.os_inx == 0) { //window
                    exePath = GB.chromePath + "/";
                    exeStr = GB.chromeAppName;
                    process = Runtime.getRuntime().exec(exePath + exeStr, null, new File(exePath));
                } else { //linux
                    process = Runtime.getRuntime().exec("./chrome.sh");
                    //process = Runtime.getRuntime().exec(exeStr, null, new File(exePath));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return errStr;
            
            
            
        }

        if (cmdstr.equals("exeLogic")) {
            /*
            String exeStr = "";
            String exePath = GB.laPath + "/";
            Process process = null;
            try {
                if (GB.os_inx == 0) { //window
                    exePath = GB.laPath + "/";
                    exeStr = GB.laAppName;
                    process = Runtime.getRuntime().exec(exePath + exeStr, null, new File(exePath));
                } else { //linux
                    exePath = GB.laPath;
                    exeStr = GB.laAppName+" --now-sandbox" ;
                    exePath = "/home/kevin/myCode/syncSet/";
                    exeStr= "la.sh";
                    process = Runtime.getRuntime().exec("./la.sh");
                    //process = Runtime.getRuntime().exec(exeStr, null, new File(exePath));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
             */

            int exist = Lib.chkProcessExist("Logic.exe");
            if (exist == 1) {
                System.out.println("LA is still running.");
                return errStr;
            }
            if (logicThread == null) {
                logicThread = new LogicThread();
                logicThread.start(); // 啟動執行緒            
                System.out.println("LA Starting.... ");
            } else {
                if (logicThread.isAlive()) {
                    System.out.println("LA is still running.");
                } else {
                    System.out.println("LA has finished.");
                    logicThread = null;
                }
            }
            return errStr;

        }

        if (cmdstr.equals("exeChrome")) {
            if (chromeThread == null) {
                chromeThread = new ChromeThread();
                chromeThread.start(); // 啟動執行緒            
                System.out.println("Chrome Starting.... ");
            } else {
                if (chromeThread.isAlive()) {
                    System.out.println("Chrome is still running.");
                } else {
                    System.out.println("Chrome has finished.");
                    chromeThread = null;
                }
            }
            return errStr;
        }

        String[] strCmdA = cmdstr.split(" ");

        if (strCmdA[0].equals("txsskui")) {
            if (cmdstr.length() > 8) {
            }
            return errStr;

        }

        if (strCmdA[0].equals("txFileToSocket")) {
            return errStr;
        }

        //=================================================
        cmd = "testResponse";
        //para0 = soltCnt,all when =-1
        if (strCmdA[0].equals(cmd)) {
            if (cexe.getMap(cmd) == null) {
                addTask(strCmdA, 1, 10);
                return null;
            }
            return "command is in process !!!";
        }

        if (cmdstr.equals("bypassSystemSecurity")) {
            return errStr;
        }
        if (cmdstr.equals("clearSystemSecurity")) {
            return errStr;
        }

        return "Command Not Found !!!";

    }

}

class ConsoleMainTm1 extends TimerTask {

    ConsoleMain cla;
    String preParaSetTime = "";

    ConsoleMainTm1(ConsoleMain owner) {
        cla = owner;
    }

    @Override
    public void run() {
        try {
            //===============================

            cla.uart0.uartConnectTime++;
            if (cla.uart0.uartConnectTime > 50) {
                if (cla.uart0.uartConnected_f == 1) {
                    System.out.print("uart0 stop !!!\n");
                }
                cla.uart0.uartConnected_f = 0;
            }

            if (GB.emulate == 2) {
                cla.syncData.systemStatus0 &= 0xfffffffc;
                cla.syncData.systemStatus0 |= 0x00000002;
            }

            cla.easyCommandTime++;
            if (cla.easyCommandTime == 50) {
                cla.easyCommand = 0;

            }

            for (int i = 0; i < 36; i++) {
                cla.drvDataClrBuf[i]++;
                if (cla.drvDataClrBuf[i] > 50) {
                    cla.syncData.sspaPowerStatusAA[i] &= 0xfe;
                }
            }

            cla.testUartTime++;
            if (cla.testUartTime >= 5) {
                cla.testUartTime = 0;
                //cla.uart0.encSend(new byte[]{0x23, 0x02, 0x00, 0x02, 0x00, 0x00, 0x01}, 7);
                //System.out.println("txTest");

            }
            cla.connectFpgaCnt++;
            if (cla.connectFpgaCnt > 50) {
                cla.connectFpgaCnt = 0;
                cla.syncData.systemStatus0 &= 0xf8fffffc;
                cla.syncData.pulseFormHighPeriod = 0;
                cla.syncData.pulseFormLowPeriod = 0;
                cla.syncData.pulseFormAddBufA0[cla.syncData.pulseFormAddBufA0Inx1 & 255] = 20 * 1000 * 160 * 2;
                cla.syncData.pulseFormAddBufA0Inx1++;

                for (int i = 0; i < 36; i++) {
                    cla.syncData.sspaPowerStatusAA[i] = 0;
                    cla.syncData.sspaModuleStatusAA[i] = 0;
                }
            }

            byte[] powerStatusA = cla.syncData.sspaPowerStatusAA;
            byte[] moduleStatusA = cla.syncData.sspaModuleStatusAA;
            /*
            int powerOn_f = 0;
            int moduleOn_f = 0;
            for (int i = 0; i < 36; i++) {
                if (((powerStatusA[i] >> 4) & 1) == 1) {
                    powerOn_f = 1;
                }
                if (((moduleStatusA[i] >> 1) & 1) == 1) {
                    moduleOn_f = 1;
                }
            }
            */
            cla.powerOn_f = (cla.syncData.systemStatus0 >> 23) & 1;;
            cla.moduleOn_f = (cla.syncData.systemStatus0 >> 24) & 1;

            /*
            if (powerOn_f != 0) {
                cla.syncData.systemStatus0 |= 1 << 23;
            } else {
                cla.syncData.systemStatus0 &= (1 << 23) ^ 0xffffffff;
            }
            if (moduleOn_f != 0) {
                cla.syncData.systemStatus0 |= 1 << 24;
            } else {
                cla.syncData.systemStatus0 &= (1 << 24) ^ 0xffffffff;
            }
             */
            int ibuf = (int) GB.paraSetMap.get("ctr1PulseSource");
            if (ibuf != 0) {
                cla.syncData.systemStatus1 |= 1 << 26;
            } else {
                cla.syncData.systemStatus1 &= (1 << 26) ^ 0xffffffff;
            }

            ibuf = (int) GB.paraSetMap.get("ctr1TxLoad");
            if (ibuf != 0) {
                cla.syncData.systemStatus1 |= 1 << 27;
            } else {
                cla.syncData.systemStatus1 &= (1 << 27) ^ 0xffffffff;
            }

            //===============================
            GB.checkParaSet();
            cla.cexe.exeTaskMap();
        } catch (Exception ex) {
            //System.out.println(ex.toString());
        }
    }

}

class ConsoleMainCmdExe {

    ConsoleMain cla;
    Map<String, CmdTask> taskMap;

    ConsoleMainCmdExe(ConsoleMain owner, Map<String, CmdTask> _taskMap) {
        cla = owner;
        taskMap = _taskMap;
    }

    public void exeTaskMap() {
        try {
            Set<String> iss = taskMap.keySet();
            Object[] objA = iss.toArray();
            for (int i = 0; i < objA.length; i++) {
                if (objA[i] == null) {
                    continue;
                }
                String key = (String) objA[i];
                CmdTask task1 = taskMap.get(key);
                if (task1 == null) {
                    continue;
                }
                exeTask(task1);

            }

            /*
            for (String key : iss) {
                CmdTask task1 = taskMap.get(key);
                if (task1 == null) {
                    continue;
                }
                exeTask(task1);
            }
             */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public CmdTask getMap(String name) {
        CmdTask task = taskMap.get(name);
        return task;
    }

    public void addMap(CmdTask task) {
        taskMap.put(task.name, task);
    }

    public int taskEnd(CmdTask task) {
        task.stepInx = 0;
        task.stepTim = 0;
        task.retryTim = 0;
        task.retryCnt += 1;
        if (task.retryAmt > 0) {
            if (task.retryCnt >= task.retryAmt) {
                taskMap.remove(task.name);
                return 1;
            }
        }
        return 0;
    }

    //if slotCnt < 0 commsnd slot all
    public int exeTask(CmdTask task) {
        if (task.retryTim < task.retryDly) {
            task.retryTim++;
            return 0;
        }
        switch (task.name) {
            case "reNewExtensions":
            case "reNewParaSet":
            case "upLoadFile":
            case "readFile":
            case "getExRecordNames":
            case "getRecordFile":
            case "testResponse":
            case "getSlotInf":
            case "startAsterisk":
            case "stopAsterisk":
            default:
                return 0;
        }
    }
}

class SyncData {

    /*
	 array 0:mast, 1:sub1, 2:sub2, 3:ctr1, 4:ctr2, 5:drv1a, 6:drv1b, 7:drv2a, 8:drv2b
	 *** slotId[3:0] ==>
	 	 "none 			id=0;
	 	 "ＩＰＣ控制模組",     	id=1;
	 	 "ＦＰＧＡ控制模組",    id=2;
	 	 "ＩＯ控制模組",       id=3;
	 	 "邏輯分析模組",       id=4;
	 	 "光纖傳輸模組",     	id=5;
	 	 "ＲＦ傳輸模組	",     	id=6;
	 	 "語音通信模組",   	id=7;
	 	 "SSPA驅動模組",   	id=8;
	  *** slotSerNo		7:4
	  *** slotStatus	9:8 ==> 0:none, 1:ready, 2:error 3:warn up
          *** slotTestStatus 11:10 ==> 0:none, 1:PreTest, 2:testing;
     */
    short[] slotDataAA = new short[12];

    /*=================================================
     mast mainStatus[1:0] 		==> 0:none, 1:warn up, 2:ready, 3:error
     sub1 mainStatus[3:2] 		==> 0:none, 1:warn up, 2:ready, 3:error
     sub2 mainStatus[5:4] 		==> 0:none, 1:warn up, 2:ready, 3:error
     ctr1 mainStatus[7:6] 		==> 0:none, 1:warn up, 2:ready, 3:error
     ctr2 mainStatus[9:8] 		==> 0:none, 1:warn up, 2:ready, 3:error
     drv1a mainStatus[11:10] 	==> 0:none, 1:warn up, 2:ready, 3:error
     drv1b mainStatus[13:12] 	==> 0:none, 1:warn up, 2:ready, 3:error
     drv2a mainStatus[15:14] 	==> 0:none, 1:warn up, 2:ready, 3:error
     drv2b mainStatus[17:16] 	==> 0:none, 1:warn up, 2:ready, 3:error
     ctr1Meter mainStatus[19:18] 	==> 0:none, 1:warn up, 2:ready, 3:error
     ctr2Meter mainStatus[21:20] 	==> 0:none, 1:warn up, 2:ready, 3:error
     //===
     ctr1 rfPulse detect flag[22]       ==> 0:none  1:OK
     ctr1 電源啟動[23] 			==> 0:停止 1:啟動
     ctr1 SSPA致能[24] 			==> 0:停止 1:啟動
     ctr1 本地脈波啟動[25] 		==> 0:停止 1:啟動
     ctr1 緊急停止[26] 			==> 0:備便 1:停止
     //===================================================
     ctr2 rfPulse detect flag[27] ==> 0:none  1:OK          //mast use 
     ctr2 電源啟動[28] 			==> 0:停止 1:啟動
     ctr2 SSPA致能[29] 			==> 0:停止 1:啟動
     ctr2 本地脈波啟動[30] 		==> 0:停止 1:啟動
     ctr2 緊急停止[31] 			==> 0:備便 1:停止
     */
    int systemStatus0;
    /*=================================================
    sub1 光纖連線狀態[0]	==> 0:未連線, 1:未連線
    sub1 RF連線狀態[1]          ==> 0:未連線, 1:未連線
    sub2 光纖連線狀態[2] 	==> 0:未連線, 1:未連線      mast use
    sub2 RF連線狀態[3]          ==> 0:未連線, 1:未連線      mast use
    ctr1 遠端遙控[4]      ==> 0:關閉, 1:開啟                    
    ctr2 遠端遙控[5]      ==> 0:關閉, 1:開啟                mast use
    mast spPulseExist[6]	==  0:none 1:exist          mast use
     */
    int systemStatus1;

    /* enviStatus every item is 1 bit
     value 0:none, 1:error
     airFlow left
     airFlow middle
     airFlow right
     waterFlow 1
     waterFlow 2
     waterFlow 3
     waterFlow 4
     waterFlow 5
     waterFlow 6
     waterFlow temperature
     */
    int[] enviStatusA = new int[]{0, 0};
    /*
     0:input rf power
     1:
     2:pre amp output rf power
     3:driver amp output rf power
     4:cw output rf power
     5:ccw output rf power
     */
    short[] meterStatusAA = new short[6];
    //=============================================
    //0 connectFlag, 1 faultLed, 2:v50enLed, 3:v32enLed  4:powerOn_f
    byte[] sspaPowerStatusAA = new byte[36];
    short[] sspaPowerV50vAA = new short[36];
    short[] sspaPowerV50iAA = new short[36];
    short[] sspaPowerV50tAA = new short[36];
    short[] sspaPowerV32vAA = new short[36];
    short[] sspaPowerV32iAA = new short[36];
    short[] sspaPowerV32tAA = new short[36];
    //=============================================
    /*
     0:connect, 1:致能, 2 保護觸發, 3:工作比過高, 4:脈寬過高, 5:溫度過高, 6:反射過高, 
     */
    byte[] sspaModuleStatusAA = new byte[36];
    short[] sspaModuleRfOutAA = new short[36];
    short[] sspaModuleTemprAA = new short[36];
    //=============================================

    byte[][] gpsDataAA = new byte[3][16];
    byte[] gngga0 = new byte[64];
    byte[] gngga1 = new byte[64];
    byte[] gngga2 = new byte[64];

    short[] adjTimeOf1588A = new short[2];
    short[] commPackageCntA = new short[2];
    short[] commOkRateA = new short[2];
    short[] rfRxPowerA = new short[4];//mast rx1,mast rx1,sub1 rx sub2 rx
    short[] ioInA = new short[6];
    int[] subStatusA = new int[2];

    int[] pulseWaveA = new int[256];
    int pulseWaveInx = 0;

    int[] viewDatas = new int[64];
    //ArrayList<int> list = new ArrayList<int>();
    int pulseFormAddBufA0Len = 0;
    int pulseFormAddBufA0Inx0 = 0;
    int pulseFormAddBufA0Inx1 = 0;
    int[] pulseFormAddBufA0 = new int[256];
    int pulseFormLowPeriod = 0;
    int pulseFormHighPeriod = 0;
    byte pulseFormFreq = 0;
    short webReturnCmd=0;
    short webReturnCmdPara=0;
            

    /*
	 subA->ctrA,subB->ctrB,ctrA->subA,ctrB->subB
	 ctrA->drvaA,ctrB->drvaB,devaA->ctrA,devaB->ctrB
	 ctrA->drvbA,ctrB->drvbB,devbA->ctrA,devbB->ctrB
	 ctrA->meterA,ctrB->meterB,meterA->ctrA,meterB->ctrB
	 s1FiberRx,s1RfRx,s1RxPackCnt,xxx
	 hostS1FiberRx,,hostS1RfRx,hostS2FiberRx,,hostS2RfRx,
	 uart0,uart1,xxx,xxx
     */
    byte[] conRxA = new byte[28];

    SyncData() {
    }

}

class ByteLook {

    byte[] bta;
    int inx = 0;

    ByteLook(byte[] arr) {
        bta = arr;
    }

    int getByteInt(int ii) {
        return bta[ii] & 255;
    }

    byte lookByte() {
        return bta[inx++];
    }

    int lookByteInt() {
        return bta[inx++] & 255;
    }

    short lookShort() {
        int ibuf = (bta[inx++]) & 255;
        ibuf += (bta[inx++] & 255) * 256;
        return (short) ibuf;
    }

    int lookShortInt() {
        int ibuf = (bta[inx++]) & 255;
        ibuf += (bta[inx++] & 255) * 256;
        return ibuf;
    }

    int lookInt() {
        int ibuf = (bta[inx++]) & 255;
        ibuf += (bta[inx++] & 255) * 256;
        ibuf += (bta[inx++] & 255) * 65536;
        ibuf += (bta[inx++] & 255) * 16777216;
        return ibuf;
    }

}

class ByteLoad {

    byte[] bta;
    int inx = 0;

    ByteLoad(byte[] arr) {
        bta = arr;
    }

    void wByteByte(byte data) {
        bta[inx++] = data;
    }

    void wByteInt(int data) {
        bta[inx++] = (byte) (data & 255);
    }

    void wShortInt(int data) {
        bta[inx++] = (byte) (data & 255);
        bta[inx++] = (byte) ((data >> 8) & 255);
    }

    void wIntInt(int data) {
        bta[inx++] = (byte) (data & 255);
        bta[inx++] = (byte) ((data >> 8) & 255);
        bta[inx++] = (byte) ((data >> 16) & 255);
        bta[inx++] = (byte) ((data >> 24) & 255);
    }

}

class LogicThread extends Thread {

    ConsoleMain cla = ConsoleMain.scla;

    public void run() {
        try {
            // Replace "path/to/your/program.exe" with the actual path
            Process process = Runtime.getRuntime().exec(GB.laPath + "/" + GB.laAppName);
            // Optionally, wait for the process to complete and get its exit code
            int exitCode = process.waitFor();
            System.out.println("LA program exited with code: " + exitCode);
            cla.logicThread = null;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            cla.logicThread = null;
        }

    }
}

class ChromeThread extends Thread {

    ConsoleMain cla = ConsoleMain.scla;

    public void run() {
        try {
            // Replace "path/to/your/program.exe" with the actual path
            Process process=null;
            if(GB.os_inx == 0)
                process = Runtime.getRuntime().exec(GB.chromePath + "/"+GB.chromeAppName+" " + GB.chromeAddress);
            else
                process = Runtime.getRuntime().exec("./chrome.sh");
            // Optionally, wait for the process to complete and get its exit code
            
            
            int exitCode = process.waitFor();
            System.out.println("Chrome program exited with code: " + exitCode);
            cla.chromeThread = null;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            cla.chromeThread = null;
        }

    }
}
