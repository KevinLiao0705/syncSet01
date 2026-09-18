/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedOutputStream;
 
public class Csocket {
    private String address = "127.0.0.1";// 連線的ip
    private int port = 1235;// 連線的port
 
    public Csocket(String add,int po) 
    {
        address=add;
        port=po; 
    }
    public void tx(String txstr) 
    {
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(this.address, this.port);
        try {
            client.connect(isa, 10000);
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            // 送出字串
            out.write(txstr.getBytes());
            out.flush();
            out.close();
            client.close();
 
        } catch (java.io.IOException e) {
            System.out.println("Socket連線有問題 !");
            System.out.println("IOException :" + e.toString());
        }
    }   
    
    
}