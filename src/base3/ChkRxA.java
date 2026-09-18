package base3;

import org.json.JSONObject;

public class ChkRxA {
    public String ip;
    public String act;
    public int cmdInx;
    public int retCmdInx;
    public String statusStr;
    public int reti = 0;//0:none return 1:ok 2:error
    public JSONObject jobj=null;
    public int rxTrig_f=0;
    public int slotCnt=0;
    public ChkRxA(String _ip, String _act, int _cmdInx) {
        ip = _ip;
        act = _act;
        cmdInx = _cmdInx;
    }
}
