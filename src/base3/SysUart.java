
package base3;

import com.fazecast.jSerialComm.SerialPort;

class SysUart {

    String name;
    SerialPort uartPort;
    int seted_f = 0;
    CommPortSender uartTx;
    CommPortReceiver uartRx;
    String portStr = "1";
    String boudrateStr = "115200";
    String parityStr = "None";//Noen | Even | Odd
    public BytesCallback cbk;

    SysUart(String _name) {
        name = _name;
    }

    void setCallBack(BytesCallback callBackPrg) {
        cbk = callBackPrg;
    }

    public static String listUart() {
        String comName;
        SerialPort[] ports = SerialPort.getCommPorts();
        String str = "";
        for (int i = 0; i < ports.length; i++) {
            SerialPort sp = ports[i];
            comName = sp.getSystemPortName();
            if (i != 0) {
                str += ",";
            }
            str += comName;
        }
        return str;
    }

    public String setUart() {
        String errStr = null;
        try {
            closeUart();
            int sys232Port = Lib.str2int(portStr, 1);
            int sys232DataBit = 8;
            int sys232Boudrate = Lib.str2int(boudrateStr, 115200);
            String comErr = openUart("COM" + sys232Port, parityStr, sys232Boudrate);
            return comErr;
        } catch (Exception ex) {
            String comErr = "userSet.json Formate Error !!!";
            return comErr;
        }
    }

    public void closeUart() {
        if (uartPort != null) {
            uartRx.terminate();
            Lib.thSleep(10);
            uartPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            uartPort.removeDataListener();
            boolean result = uartPort.closePort();
            uartPort = null;
            seted_f = 0;
        }
    }

    public String openUart(String portName, String Parity, int boudrate) {
        String comName;
        SerialPort[] ports = SerialPort.getCommPorts();
        seted_f = 0;
        if (ports.length == 0) {
            return "Uart1: No serial ports available!";
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
            return "Uart1: No this port on this system!";
        }
        int parity = SerialPort.NO_PARITY;
        if (Parity.equals("Even")) {
            parity = SerialPort.EVEN_PARITY;
        }
        if (Parity.equals("Odd")) {
            parity = SerialPort.ODD_PARITY;
        }
        uartPort = ports[portToUse];
        uartPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        uartPort.setComPortParameters(boudrate, 8, SerialPort.ONE_STOP_BIT, parity);
        //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        //logger.debug("Going to open the port...");
        boolean result = uartPort.openPort();
        if (result) {
            uartTx = new CommPortSender();
            uartTx.setWriterStream(uartPort.getOutputStream());
            // setup serial port reader
            uartRx = new CommPortReceiver(uartPort.getInputStream());
            uartRx.setCallBack((bytes, len) -> rxPrg(bytes, len));
            uartRx.start();
            seted_f = 1;
        } else {
            seted_f = 0;
            uartPort = null;
            return "Uart1: This port is in used !!!";
        }
        return null;
    }

    String rxPrg(byte[] bts, int len) {
        if (cbk != null) {
            cbk.prg(bts, len);
        }
        return null;
    }

}
