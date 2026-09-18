package base3;

import org.json.JSONObject;

public class CmdTask {
    public String name;
    public int retryTim = 9999;//unit 20ms
    public int retryDly = 50;
    public int retryCnt = 0;
    public int retryAmt = 1;
    public int stepInx = 0;
    public int stepTim = 9999;
    public int stepDly = 1;
    public int stepCnt = 0;
    public int stepAmt = 1;
    public String[] paras=new String[]{"","","","","","","",""};
    public JSONObject jobj = new JSONObject();    
    public String holdKey="";
    public byte[] paraBytes=new byte[64];
    public CmdTask(String _key) {
        name = _key;
    }
}