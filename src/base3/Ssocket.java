/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ssocket {

    String name = "";
    int connect_f = 0;
    int ready_f = 0;
    String status_str;
    String conip_address = null;
    byte[] inbuf;
    int inBufferSize = 65536;
    int inbuf_len;

    int port = 9999;
    int format = 0;   //0:encode formate 
    int rxcon_ltim = 200;//unit 10, ms;

    int txon_f = 0;
    SsktxTd txTd;
    int txTd_stop_f = 0;
    SskrxTd rxTd;
    int rxTd_stop_f = 0;

    String tx_startMode = "";
    String tx_str;
    String tx_ip;
    int tx_port;
    byte[] tx_bytes;
    String rxip;
    int rxport;

    OutputStream outstr;
    InputStream instr;

    ServerSocket serverSocket;
    MyStm stm;
    Ssocket cla;
    HashMap<String, String> thTxMap = new HashMap<String, String>();
    HashMap<String, String> thTxErrMap = new HashMap<String, String>();
    public BytesCallback cbk;

    public TrxData txData;
    public TrxData rxData;
    ArrayList<MyRxData> myRxDataList;

    public Ssocket() {
        inbuf = new byte[inBufferSize];
        cla = this;
        stm = new MyStm();
    }

    public Ssocket(int _inBufferSize) {
        inBufferSize = _inBufferSize;
        inbuf = new byte[inBufferSize];
        cla = this;
        stm = new MyStm();
    }

    public void open() {
        open(port);
    }

    void setCallBack(BytesCallback callBackPrg) {
        cbk = callBackPrg;
    }

    public void open(int pt) {
        Ssocket cla = this;
        try {
            port = pt;
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (cla.rxTd_stop_f == 0 || cla.txTd_stop_f == 0) {
                cla.rxTd_stop_f = 1;
                cla.txTd_stop_f = 1;
                Lib.thSleep(100);
            }
            serverSocket = new ServerSocket(port);

            cla.rxTd = new SskrxTd(cla);
            cla.rxTd_stop_f = 0;
            cla.rxTd.start();

            cla.txTd = new SsktxTd(cla);
            cla.txTd_stop_f = 0;
            cla.txTd.start();

            ready_f = 1;

        } catch (Exception ex) {
            status_str = "\n Socket啟動有問題 ! ";
            status_str += "\n IOException : " + ex.toString();
            System.out.println(ex.toString());

        }
    }

    public void close() {
        Ssocket cla = this;
        try {
            cla.txTd_stop_f = 1;
            cla.rxTd_stop_f = 1;
            serverSocket.close();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        serverSocket = null;
        cla.ready_f = 0;
    }

    public void txReturn() {

        int stx_index = 0;
        int i;
        /*
        stm.tbuf[stx_index++] = (byte) 0xA2;
        stm.tbuf[stx_index++] = (byte) 0x12;
        stm.tbuf[stx_index++] = (byte) 0x34;
        stm.tbuf[stx_index++] = (byte) 0x56;
        stm.tbuf[stx_index++] = (byte) 0x78;
        stm.tbuf_byte = stx_index;
         */
        stm.enc_mystm();
        for (i = 0; i < stm.txlen; i++) {
            try {
                outstr.write(stm.tdata[i]);
            } catch (IOException ex) {
                Logger.getLogger(Ssocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String thTx(String ipaddr, int txport, TrxData txd) {
        String errStr = null;
        Set txSet = thTxMap.keySet();
        if (txSet.size() >= 20) {
            errStr = "Thread Size Over 20 !!!";
            return errStr;
        }
        String ss = thTxMap.get(ipaddr);
        if (ss != null) {
            if (ss.equals("connecting")) {
                return "Tx Is In Process !!!";
            }
        }
        thTxMap.put(ipaddr, "connecting");
        new Thread(() -> {
            Socket client = new Socket();
            InetSocketAddress isa = new InetSocketAddress(ipaddr, txport);
            try {
                client.connect(isa, txd.connectTime);
                BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
                // 送出字串

                int winx = 0;
                byte[] bts = new byte[2];
                bts[0] = (byte) (cla.port & 255);
                bts[1] = (byte) ((cla.port >> 8) & 255);
                out.write(bts);
                winx += 2;

                for (int i = 0; i < txd.packageLen; i++) {
                    txd.heads[i][0] = (byte) (0xab);
                    txd.heads[i][1] = (byte) (txd.formats[i] & 255);
                    txd.heads[i][2] = (byte) ((txd.packageIds[i]) & 255);
                    txd.heads[i][3] = (byte) ((txd.packageIds[i] >> 8) & 255);
                    int len = txd.datas[i].length;
                    txd.heads[i][4] = (byte) (len & 255);
                    txd.heads[i][5] = (byte) ((len >> 8) & 255);
                    txd.heads[i][6] = (byte) ((len >> 16) & 255);
                    txd.heads[i][7] = (byte) ((len >> 24) & 255);
                    out.write(txd.heads[i]);
                    winx += 8;
                    winx = wStream(winx, out, txd.datas[i]);
                }
                if (winx > 0) {
                    out.flush();
                }

                out.close();
                client.close();
                thTxMap.remove(ipaddr);

            } catch (java.io.IOException e) {
                thTxMap.remove(ipaddr);
                String str = "Socket Connect Error !!! ip:" + ipaddr + ", port:" + txport;
                //System.out.println(str);
                thTxErrMap.put(ipaddr, str);
            }
        }).start();
        return errStr;
    }

    public String thTxStr(String ipaddr, int txport, String txStr) {
        TrxData txData = new TrxData();
        txData.formats[0] = 1;
        txData.packageIds[0] = 0;
        txData.datas[0] = txStr.getBytes();
        return thTx(ipaddr, txport, txData);
    }

    public String thTxJsonStr(String ipaddr, int txport, String txJsonStr) {
        TrxData txData = new TrxData();
        txData.formats[0] = 0;
        txData.packageIds[0] = 0;
        txData.datas[0] = txJsonStr.getBytes();
        return thTx(ipaddr, txport, txData);
    }

    public void txPackRetJsonStr(String txJsonStr) {
        TrxData txData = new TrxData();
        txData.formats[0] = 0;
        txData.packageIds[0] = 0;
        txData.datas[0] = txJsonStr.getBytes();
        txPackRet(txData);
    }

    public void txPackRet(TrxData txd) {
        String addr;
        txon_f = 1;
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(cla.rxip, cla.rxport);
        try {
            client.connect(isa, 100);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 送出字串
            int winx = 0;
            byte[] bts = new byte[2];
            bts[0] = (byte) (cla.port & 255);
            bts[1] = (byte) ((cla.port >> 8) & 255);
            out.write(bts);
            winx += 2;
            for (int i = 0; i < txd.packageLen; i++) {
                txd.heads[i][0] = (byte) (0xab);
                txd.heads[i][1] = (byte) (txd.formats[i] & 255);
                txd.heads[i][2] = (byte) ((txd.packageIds[i]) & 255);
                txd.heads[i][3] = (byte) ((txd.packageIds[i] >> 8) & 255);
                int len = txd.datas[i].length;
                txd.heads[i][4] = (byte) (len & 255);
                txd.heads[i][5] = (byte) ((len >> 8) & 255);
                txd.heads[i][6] = (byte) ((len >> 16) & 255);
                txd.heads[i][7] = (byte) ((len >> 24) & 255);
                out.write(txd.heads[i]);
                winx += 8;
                winx = wStream(winx, out, txd.datas[i]);
            }
            if (winx > 0) {
                out.flush();
            }
            out.close();
            client.close();
        } catch (java.io.IOException ex) {
            String str = "Socket連線有問題 !" + rxip + " port: " + cla.rxport;
            System.out.println(str);
        }
        txon_f = 0;

    }

    public int wStream(int inx, BufferedOutputStream out, byte[] bts) {
        int txLen;
        int txOff = 0;
        int btsSize = bts.length;
        int flushSize = 4096;
        try {
            while (txOff < btsSize) {
                txLen = flushSize - inx;
                if ((txLen + txOff) >= btsSize) {
                    txLen = btsSize - txOff;
                }
                out.write(bts, txOff, txLen);
                out.flush();
                txOff += txLen;
                inx = 0;
            }
            if (inx > 0) {
                out.flush();
                inx = 0;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());

        }

        return inx;
    }

    public void txret(String txstr, int txport) {
        String addr;
        if (conip_address == null) {
            return;
        }
        txon_f = 1;
        addr = conip_address.substring(1);
        rxip = addr;
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(addr, txport);
        try {
            client.connect(isa, 100);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 送出字串
            out.write(txstr.getBytes());
            out.flush();
            out.close();
            client.close();
            txon_f = 0;

        } catch (java.io.IOException ex) {
            String str;
            str = "Socket連線有問題 !" + rxip + " port: " + txport;
            System.out.println(str);
            System.out.println("IOException :" + ex.toString());
        }
        txon_f = 0;
    }

    public void txret(byte[] bytes, int txport) {
        String addr;
        if (conip_address == null) {
            return;
        }
        txon_f = 1;
        addr = conip_address.substring(1);
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(addr, txport);
        try {
            client.connect(isa, 100);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 送出字串
            out.write(bytes);
            out.flush();
            out.close();
            client.close();

        } catch (java.io.IOException e) {
            String str;
            str = "Socket連線有問題 !" + addr + " port: " + txport;
            System.out.println(str);
            System.out.println("IOException :" + e.toString());
        }
        txon_f = 0;
    }

    public void txip(String ipaddr, String txstr, int txport) {
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(ipaddr, txport);
        try {
            client.connect(isa, 1000);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 送出字串
            out.write(txstr.getBytes());
            out.flush();
            out.close();
            client.close();

        } catch (java.io.IOException ex) {
            System.out.println("IOException :" + ex.toString());
            //Logger.getLogger(Ssocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void txip(String ipaddr, byte[] bytes, int txport) {
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(ipaddr, txport);
        try {
            client.connect(isa, 1000);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 送出字串
            out.write(bytes);
            out.flush();
            out.close();
            client.close();

        } catch (java.io.IOException ex) {
            System.out.println("IOException :" + ex.toString());
            //Logger.getLogger(Ssocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void txip(String ipaddr, int txport) {
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(ipaddr, txport);
        try {
            client.connect(isa, 1000);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            if (cla.format == 1) {
                cla.stm.enc_mystm();
                out.write(cla.stm.tdata, 0, cla.stm.txlen);
            } else {
                out.write(cla.stm.tbuf, 0, cla.stm.tbuf_byte);
            }
            out.flush();
            out.close();
            client.close();
        } catch (java.io.IOException ex) {
            System.out.println("IOException :" + ex.toString());

            //Logger.getLogger(Ssocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class SskrxTd extends Thread {

    Ssocket cla;

    SskrxTd(Ssocket owner) {
        cla = owner;
    }

    @Override
    public void run() { // override Thread's run()
        int i, j;
        Socket socket;
        int length;
        byte[] bbuf = new byte[1024];
        int rxdata_buf;
        int rxcon_tim;
        cla.status_str = "\n 伺服器已啟動 !";
        while (cla.rxTd_stop_f != 1) {
            try {
                if (cla.serverSocket == null) {
                    Lib.thSleep(20);
                    continue;
                }
                //synchronized (cla.serverSocket) {
                socket = cla.serverSocket.accept();//block untile received
                //}
                cla.conip_address = socket.getInetAddress().toString();
                cla.rxip = cla.conip_address.substring(1);

                cla.instr = socket.getInputStream();
                cla.outstr = socket.getOutputStream();
                int inbuf_inx = 0;
                //==============================================================
                if (cla.format == 0 || cla.format == 2) {
                    socket.setSoTimeout(cla.rxcon_ltim * 10);//time out occur error exeption: 

                    int overSize_f = 0;
                    while ((length = cla.instr.read(bbuf)) > 0)// <=0的話就是結束了
                    {
                        if ((inbuf_inx + length) > cla.inBufferSize) {
                            overSize_f = 1;
                            break;
                        }
                        for (i = 0; i < length; i++) {
                            cla.inbuf[(inbuf_inx + i)] = bbuf[i];
                        }
                        inbuf_inx += length;
                        cla.inbuf_len = inbuf_inx;
                    }
                    cla.instr.close();
                    socket.close();
                    if (overSize_f == 0) {
                        if (cla.cbk != null) {
                            if (cla.format == 2) {
                                cla.myRxDataList = new ArrayList<MyRxData>();
                                int inx = 0;
                                cla.rxport=(cla.inbuf[0]&255)+(cla.inbuf[1]&255)*256;
                                inx+=2;
                                while (true) {
                                    if (cla.inbuf[inx] != (byte) 0xab) {
                                        break;
                                    }
                                    MyRxData mrd = new MyRxData();
                                    mrd.format = (cla.inbuf[inx + 1] & 255);
                                    mrd.id = (cla.inbuf[inx + 2] & 255) + (cla.inbuf[inx + 3] & 255) * 256;
                                    mrd.len = (cla.inbuf[inx + 4] & 255) + (cla.inbuf[inx + 5] & 255) * 256;
                                    mrd.len += ((cla.inbuf[inx + 6] & 255) + (cla.inbuf[inx + 7] & 255) * 256) * 65536;
                                    mrd.offset = inx + 8;
                                    inx += mrd.len + 8;
                                    if (inx > cla.inbuf_len) {
                                        break;
                                    }
                                    cla.myRxDataList.add(mrd);
                                }
                            }
                            cla.cbk.prg(cla.inbuf, cla.inbuf_len);
                        }
                    }
                    //cla.rxproc(0);
                }
                //==============================================================
                if (cla.format == 1) {
                    socket.setSoTimeout(cla.rxcon_ltim * 20);
                    rxcon_tim = 0;
                    while (true) {
                        rxdata_buf = cla.instr.read();
                        if (rxdata_buf == -1) {
                            if (++rxcon_tim >= cla.rxcon_ltim) {
                                cla.instr.close();
                                socket.close();
                                cla.status_str = "\n 連線中斷 : InetAddress = " + cla.conip_address;
                                break;
                            }
                            continue;
                        }
                        rxdata_buf &= 0xff;
                        rxcon_tim = 0;

                        cla.stm.dec_mystm((byte) rxdata_buf);

                        if (cla.stm.rxin_f == 1) {
                            cla.stm.rxin_f = 0;
                            //input stm.rdata,str.rxlen
                            //cla.rxproc(1);
                        }

                        if (cla.rxTd_stop_f == 1) {
                            break;
                        }

                    }

                }
                //==============================================================

            } catch (Exception ex) {
                //Logger.getLogger(Ssocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class SsktxTd extends Thread {

    Ssocket cla;

    SsktxTd(Ssocket owner) {
        cla = owner;
    }

    @Override
    public void run() { // override Thread's run()
        while (cla.txTd_stop_f == 0) {
            switch (cla.tx_startMode) {
                case "":
                    break;
                case "txRetStr":
                    cla.txret(cla.tx_str, cla.tx_port);
                    break;
                case "txIpStr":
                    cla.txip(cla.tx_ip, cla.tx_str, cla.tx_port);
                    break;
                case "txRetBytes":
                    cla.txret(cla.tx_bytes, cla.tx_port);
                    break;
                case "txIpBytes":
                    cla.txip(cla.tx_ip, cla.tx_bytes, cla.tx_port);
                    break;
                case "txIpStm":
                    cla.txip(cla.tx_ip, cla.tx_port);
                    break;

            }
            cla.tx_startMode = "";
            Lib.thSleep(10);
        }
    }
}

class TrxData {

    int packageLen = 1;
    int connectTime = 1000;//in milisec
    int[] formats;
    int[] packageIds;
    byte[][] heads;
    byte[][] datas;

    public TrxData() {
        this(1000, 1);
        this.packageIds[0] = 0;
    }

    public TrxData(int _packageLen) {
        this(1000, _packageLen);
    }

    public TrxData(int _connectTime, int _packageLen) {
        connectTime = _connectTime;
        packageLen = _packageLen;
        heads = new byte[packageLen][];
        datas = new byte[packageLen][];
        packageIds = new int[packageLen];
        formats = new int[packageLen];
        for (int i = 0; i < packageLen; i++) {
            heads[i] = new byte[8];
        }
    }

}

class MyRxData {

    int format = 2;
    int id = 0;
    int len = 0;
    int offset = 0;

    public MyRxData() {
    }
}
