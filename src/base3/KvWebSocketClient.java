/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author Kevin
 */
public class KvWebSocketClient extends WebSocketClient {

    public KvWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Client: websocket connect successed.");
    }

    @Override
    public void onMessage(String s) {
        System.out.println("Client: receive message from server：" + s);

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Client: disconnect from server.");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Client: communicate error with server.");
    }
}
