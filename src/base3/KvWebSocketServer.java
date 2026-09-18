/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kevin
 */
public class KvWebSocketServer extends WebSocketServer {

    List<WebSocket> onLine = new ArrayList<>();
    JSONObject mtxJson = new JSONObject();
    JSONObject webSockOutJson = new JSONObject();
    JSONObject wsSysJson = new JSONObject();

    public KvWebSocketServer(String ip, Integer port) {
        super(new InetSocketAddress(ip, port));
        putJson(wsSysJson, "serialTime", 0);
    }

    public void putJson(JSONObject jobj, String key, Object value) {
        try {
            jobj.put(key, value);//添加元素
        } catch (JSONException ex) {
        }
    }

    Object getJson(JSONObject jobj, String key) {
        try {
            return jobj.get(key);//添加元素
        } catch (JSONException ex) {
        }
        return null;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        //System.out.println("Server: a new client connect in：" + webSocket.getRemoteSocketAddress().getHostName() + ":" + webSocket.getRemoteSocketAddress().getPort());
        //onLine.add(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

        //System.out.println("Server: disconncet from client：" + webSocket.getRemoteSocketAddress().getHostName() + ":" + webSocket.getRemoteSocketAddress().getPort());
        //onLine.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        Object obj;
        JSONObject mesJson;
        try {
            mesJson = new JSONObject(message);
        } catch (JSONException ex) {
            return;
        }
        obj = getJson(wsSysJson, "serialTime");
        int serialTime = (int) obj;
        serialTime++;
        serialTime = serialTime % 10000;
        putJson(wsSysJson, "serialTime", serialTime);

        String userName = "";
        try {
            obj = mesJson.get("userName").toString();
            if (obj != null) {
                userName = obj.toString();
                ConnectCla conObj = GB.connectMap.get(userName);
                if (conObj != null) {
                    conObj.time = 0;
                } else {
                    conObj = new ConnectCla(userName, 100);//unit 20ms
                    GB.connectMap.put(userName, conObj);
                }
            }
        } catch (Exception ex) {

        }

        obj = getJson(mesJson, "deviceId");
        String deviceId = (String) obj;
        obj = getJson(mesJson, "act");
        String actStr = (String) obj;
        JSONObject outJson = new JSONObject();
        putJson(outJson, "act", "ret~"+actStr);
        putJson(outJson, "wsSysJson", wsSysJson.toString());
        switch (deviceId) {
            case "syncSet":
                outJson = ConsoleMain.wsCallBack(userName, mesJson, actStr, outJson);
                break;
        }
        webSocket.send(outJson.toString());
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();

    }

    @Override
    public void onStart() {
        System.out.println("Server: websocket start ...");
        System.out.println(GB.webSocketAddr+":"+GB.webSocketPort);
    }

    public static void serverStart() {
        
        KvWebSocketServer server = new KvWebSocketServer(GB.webSocketAddr, GB.webSocketPort);
        server.start();

    }
}


