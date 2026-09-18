/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import com.fazecast.jSerialComm.SerialPort;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Administrator
 */
public class Uart {

    public SerialPort hdUart;
    public int uartSeted_f = 0;
    public int uartConnected_f = 0;
    public int uartConnectTime = 0;
    public CommPortSender uartTx;
    public CommPortReceiver uartRx;
    public BytesCallback cbk = null;
    public String portName;
    public int boudrate;
    public String parity = "None";//None | Odd | Even
    public int stopBit = 1;   //1 | 2
    public int dataBit = 8;   //7 | 8
    public int txEncMode = 0;   //
    public int rxEncMode = 0;   //
    public int rxSerialCnt = 0;
    public int rxErrCnt = 0;
    public int rxPackageCnt = 0;
    //========================
    public int txDeviceId;
    public int txSerialId;
    public int txGroupId;
    public int txLen;
    public int txCmd;
    public int txPara0;
    public int txPara1;
    public int txPara2;
    public int txPara3;
    public byte[] txBuffer = new byte[1024];
    public int txBufferLen = 0;
    public int txAltPackCnt = 0;

    public Uart() {

    }

    public void close() {
        if (hdUart != null) {
            uartRx.terminate();
            Lib.thSleep(10);
            hdUart.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            hdUart.removeDataListener();
            //logger.debug("Going to close the port...");
            boolean result = hdUart.closePort();
            //logger.debug("Port closed? {}", result);
            hdUart = null;
            uartSeted_f = 0;
        }
    }

    public void setUar(String _portName, int _boudrate) {
        portName = _portName;
        boudrate = _boudrate;

    }

    public String rxPrg(byte[] bytes, int len) {
        if (cbk != null) {
            cbk.prg(bytes, len);
        }
        return null;
    }

    public void encSend() {
        int inx = 0;
        uartTx.stm.tbuf[inx++] = (byte) (txDeviceId & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txDeviceId >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txSerialId & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txSerialId >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txGroupId & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txGroupId >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txBufferLen + 10) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (((txBufferLen + 10) >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txCmd & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txCmd >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txPara0 & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txPara0 >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txPara1 & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txPara1 >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txPara2 & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txPara2 >> 8) & 255);
        uartTx.stm.tbuf[inx++] = (byte) (txPara3 & 255);
        uartTx.stm.tbuf[inx++] = (byte) ((txPara3 >> 8) & 255);
        for (int i = 0; i < txBufferLen; i++) {
            uartTx.stm.tbuf[inx++] = txBuffer[i];
        }
        uartTx.stm.tbuf_byte = inx;
        uartTx.stm.enc_mystm();
        uartTx.send();
    }

    public void encSend(byte[] bytes, int len) {
        for (int i = 0; i < len; i++) {
            uartTx.stm.tbuf[i] = bytes[i];
        }
        uartTx.stm.tbuf_byte = len;
        uartTx.stm.enc_mystm();
        uartTx.send();
    }

    public void send(byte[] bytes, int len) {
        if (uartSeted_f == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            uartTx.stm.tdata[i] = bytes[i];
        }
        uartTx.stm.txlen = len;
        uartTx.send();
    }

    public void send(String str) {
        byte[] bts = str.getBytes(StandardCharsets.UTF_8);
        send(bts, bts.length);
    }

    public String open() {
        String comName;
        String errStr = null;
        try {

            SerialPort[] ports = SerialPort.getCommPorts();
            uartSeted_f = 0;
            hdUart = null;
            if (ports.length == 0) {
                errStr = "No serial ports available !!!";
                return errStr;
            }
            int portToUse = -1;
            for (int i = 0; i < ports.length; i++) {
                SerialPort sp = ports[i];
                comName = sp.getSystemPortName();//.toLowerCase();
                if (comName.equals(portName)) {
                    portToUse = i;
                    break;
                }
            }
            if (portToUse < 0) {
                errStr = "No this port on this system !!!";
                return errStr;
            }
            int intParity = SerialPort.NO_PARITY;
            if (parity.equals("Even")) {
                intParity = SerialPort.EVEN_PARITY;
            }
            if (parity.equals("Odd")) {
                intParity = SerialPort.ODD_PARITY;
            }
            int stop_bit = SerialPort.ONE_STOP_BIT;
            if (stopBit == 2) {
                stop_bit = SerialPort.TWO_STOP_BITS;
            }

            hdUart = ports[portToUse];
            hdUart.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
            hdUart.setComPortParameters(boudrate, dataBit, stop_bit, intParity);
            //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
            //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            //logger.debug("Going to open the port...");
            boolean result = hdUart.openPort();
            if (result) {
                uartTx = new CommPortSender();
                uartTx.encMode = txEncMode;
                uartTx.setWriterStream(hdUart.getOutputStream());
                // setup serial port reader
                uartRx = new CommPortReceiver(hdUart.getInputStream());
                uartRx.encMode = rxEncMode;
                uartRx.setCallBack((bytes, len) -> rxPrg(bytes, len));
                uartRx.start();
                uartSeted_f = 1;
            } else {
                uartSeted_f = 0;
                hdUart = null;
                errStr = "This port is in used !!!";
                return errStr;
            }
            return errStr;
        } catch (Exception ex) {
            return "Open Uart Error !!!";
        }
    }

    public static String[] listComPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] result = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            result[i] = ports[i].getSystemPortName();
        }
        return result;
    }

    void setCallBack(BytesCallback callBackPrg) {
        cbk = callBackPrg;
    }

}
