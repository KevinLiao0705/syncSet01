package base3;

class KvComm {

    String name;
    String instComm;
    SysUart uartC;
    Ssocket serverSocket;

    KvComm(String _name, String _instComm) {
        this(_name,_instComm,65536);
    }

    KvComm(String _name, String _instComm,int _inBufferSize) {
        name = _name;
        instComm = _instComm;
        if (instComm.equals("uart")) {
            uartC = new SysUart(name);
        }
        if (instComm.equals("serverSocket")) {
            serverSocket = new Ssocket(_inBufferSize);
            serverSocket.name=_name;
        }
    }
    
    
    
    String open() {
        String errStr = "instComm inavailable !!!";
        if (instComm.equals("uart")) {
            errStr = uartC.setUart();
        }
        if (instComm.equals("serverSocket")) {
            serverSocket.open();
            errStr = null;
        }
        return errStr;
    }

    void close() {
        if (instComm.equals("uart")) {
            uartC.closeUart();
        }
    }

}
