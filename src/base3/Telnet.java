/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

/**
 *
 * @author Administrator
 */
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Protocol :    Client ----------- Server
 *
 *                       <---------  <path> (single line)
 * (single line)  <cmd>  ---------->
 *                       <---------  <output> (several lines)
 *                         <---------     ==end== (single line)
 *
 *                another session started again
 *
 *                       <---------  <path> (single line)
 *                       .....
 */
public class Telnet {

    static int port = 23;
    static final String endMsg = "==end==";

    public Telnet(String sc, String ip) {
        try {
            if (sc.equals("server")) {
                runServer();
            }
            if (sc.equals("client")) {
                runClient(ip, port);
            }
        } catch (Exception ex) {
            Logger.getLogger(Telnet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("server")) {
            runServer();
        } else if (args[0].equals("client")) {
            runClient(args[1], port);
        } else {
            System.out.println("Client : java Telnet client ip\r\n"
                    + "Server : java Telnet server");
        }
    }

    public static void runClient(String host, int port) throws Exception {
        System.out.println("Host " + host + "; port " + port);
        Socket socket = new Socket(host, port);
        new CommandClient(socket).start();
        System.out.println("Connected OK");
    }

    public static void runServer() throws Exception {
        ServerSocket server = new ServerSocket(port);  // 建立ServerSocket物件
        while (true) {  // 建立客戶端Socket物件
            Socket socket = server.accept();
            System.out.println("Accept Connection From : " + socket.getInetAddress());
            new CommandServer(socket).start();
        }
    }
}

class Command {

    ProcessBuilder pb;

    Command() {
        pb = new ProcessBuilder();
        pb.directory(new File("."));
    }

    public String exec(String command) throws Exception {
        String outMsg = "", errMsg = "";
        if (command.startsWith("cd ")) {
            String path = command.substring(3);
            if (path.equals("\\")) {
                String tPath = getPath();
                String rootPath = tPath.substring(0, 3);
                File root = new File(rootPath);
                pb.directory(root);
            } else {
                try {
                    File dir = new File(pb.directory(), path);
                    pb.directory(dir);
                } catch (Exception e) {
                    errMsg = "Error: " + e.toString();
                }
            }
        } else {
            String[] cmd = {"cmd.exe", "/C", command};
            pb.command(cmd);
            Process process = pb.start();
            // 取得命令執行的結果串流結果傳回給 Client.
            //   請注意 process.getInputStream() 取得的是 command 的輸出串流，
            //  被重導到輸入串流以利讀取的結果 (這在概念上很怪，但確實如此)
            outMsg = readAll(process.getInputStream());

            // 取得命令執行的錯誤串流結果傳回給 Client.
            errMsg = readAll(process.getErrorStream());
            process.waitFor();                     // 等待命令執行完畢
        }
        return outMsg + errMsg;
    }

    public String getPath() throws Exception {
        return pb.directory().getCanonicalPath();
    }

    public String readAll(InputStream is) throws Exception {
        StringBuffer rzText = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            rzText.append(line + "\r\n");
        }
        return rzText.toString();
    }
}

class CommandServer extends Thread {

    Socket socket;
    BufferedReader in;
    PrintStream out;
    Command command;

    public CommandServer(Socket s) throws Exception {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());
        command = new Command();
    }

    public void run() {
        try {
            while (true) {
                out.println(command.getPath());     // set path to client
                out.flush();
                String cmd = in.readLine();            // read command from client
                System.out.println(socket.getInetAddress() + " $ " + cmd);
                String rzMsg = command.exec(cmd);    // execute command
                out.print(rzMsg);
                out.println(Telnet.endMsg);            // 輸出結束訊息
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class CommandClient extends Thread {

    Socket socket;
    BufferedReader in, stdin;
    PrintStream out;

    public CommandClient(Socket s) throws Exception {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run() {
        byte[] bbuf = new byte[1024];
        char[] cbuf = new char[1024];
        int length;
        while(true){
            try {
                socket.setSoTimeout(1000);
                while ((length = in.read(cbuf)) > 0){                
                    for(int i=0;i<length;i++)
                        System.out.print((char)cbuf[i]);
               }
               System.out.print("AAAAAAAAA");
                
            } catch (IOException ex) {
                Logger.getLogger(CommandClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*
        try {
            System.out.println("Accept from : " + socket.getInetAddress());
            while (true) {
                String path = in.readLine();        // read path from server
                System.out.print(socket.getInetAddress() + " $ " + path + ">");
                String cmd = stdin.readLine();        // read command from console
                out.println(cmd);                    // send command to server
                while (true) {                        // read output of command from server
                    String line = in.readLine();
                    if (line.equals(Telnet.endMsg)) {
                        break;
                    }
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
