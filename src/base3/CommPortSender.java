/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

/**
 *
 * @author kevin
 */
import java.io.IOException;  
import java.io.OutputStream;  
  
public class CommPortSender {  
    OutputStream out;    
    int encMode=0;
    MyStm stm = new MyStm();
      
    public void setWriterStream(OutputStream os) {    
        out = os;    
    }    

    public void send() {    
        send(stm.tdata,0,stm.txlen);
        stm.txlen=0;
    }
    public void send(byte[] bytes) {    
        try {    
            out.write(bytes);    
            out.flush();    
        } catch (IOException e) {    
            e.printStackTrace();    
        }    
    }      
    public void send(byte[] bytes,int off,int len) {    
        if(len<=0)
            return; 
        try {    
            out.write(bytes,off,len);    
            out.flush();    
        } catch (IOException e) {    
            e.printStackTrace();    
        }    
    }      
    
    
}  