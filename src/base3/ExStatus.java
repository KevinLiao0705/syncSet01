package base3;

import org.json.JSONObject;

public class ExStatus {
    public String name;
    public int status=0;//0:unregister, 1:register, 2:login, 3:used ,4:ring 5:ringing
    String callWith="";
    String callFromName="";
    String callToNo="";
    public ExStatus(String _key) {
        name = _key;
    }
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("status", status);
            json.put("callWith", callWith);
            json.put("callFromName", callFromName);
            json.put("callToNo", callToNo);
        } catch (Exception ex) {
            return null;
        }
        return json;
    }
}