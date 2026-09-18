/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.io.*;
import java.util.Arrays;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 *
 * @author Administrator
 */
public class Ssh {

    private String ipAddress;
    private String username;
    private String password;
    public static final int DEFAULT_SSH_PORT = 22;
    private Vector<String> stdout;

    JSch mJsch;
    Session mSession;
    MyUserInfo mUserInfo;
    Channel mChannel;
    //BufferedReader mInput;
    InputStream inStrm;
    OutputStream outStrm;
    
    
    int connect_f = 0;

    public Ssh(final String ipAddress, final String username, final String password) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        stdout = new Vector<String>();
    }

    public int connect() {
        try {
            /*
            JSch mJsch = new JSch();
            mSession = mjsch.getSession(username, ipAddress,DEFAULT_SSH_PORT);  
            jsch.addIdentity("src/test/resources/id_rsa");
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);
    session.connect();
    ChannelShell channel = (ChannelShell) session.openChannel("shell");
    PipedInputStream pis = new PipedInputStream();
    PipedOutputStream pos = new PipedOutputStream();
    channel.setInputStream(new PipedInputStream(pos));
    channel.setOutputStream(new PipedOutputStream(pis));
    channel.connect();
    pos.write("test run bob\r".getBytes(StandardCharsets.UTF_8));
    pos.flush();
    verifyResponse(pis, "test run bob");
    pis.close();
    pos.close();
    channel.disconnect();
    session.disconnect();            
             */

            JSch mjsch = new JSch();
            mUserInfo = new MyUserInfo();
            mSession = mjsch.getSession(username, ipAddress, DEFAULT_SSH_PORT);
            mSession.setPassword(password);
            //设置第一次登陆的时候提示，可选值：(ask | yes | no)
            mSession.setConfig("StrictHostKeyChecking", "no");
            //mSession.setUserInfo(mUserInfo);
            //设置登陆超时时间   
            mSession.connect(0);
            //==================================================================
            mChannel = mSession.openChannel("shell");
            //mChannel.setInputStream(null);
            //mInput = new BufferedReader(new InputStreamReader(mChannel.getInputStream()));
            
            inStrm = mChannel.getInputStream();
            outStrm = mChannel.getOutputStream();
            
            mChannel.connect();
            connect_f = 1;
            return 0;

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //public void sshShell(String ip, String user, String psw,int port, String privateKey, String passphrase) throws Exception {}
    
    public void sshShellz() throws JSchException, Exception

    {

        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();

        /*
        //设置密钥和密码
        if (privateKey != null && !"".equals(privateKey)) {
            if (passphrase != null && "".equals(passphrase)) {
                //设置带口令的密钥
                jsch.addIdentity(privateKey, passphrase);
            } else {
                //设置不带口令的密钥
                jsch.addIdentity(privateKey);
            }
        }
        */
        
            //mSession = mjsch.getSession(username, ipAddress, DEFAULT_SSH_PORT);
        
            //session = jsch.getSession(user, ip, port);
            
            
            
        
        //如果服务器连接不上，则抛出异常
        if (session == null) {
            throw new Exception("session is null");
        }

        //设置登陆主机的密码
        session.setPassword(password);//设置密码   
        //设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        //设置登陆超时时间   
        session.connect(30000);

        try {
            //创建sftp通信通道
            channel = (Channel) session.openChannel("shell");
            channel.connect(1000);

            //获取输入流和输出流
            InputStream instream = channel.getInputStream();
            OutputStream outstream = channel.getOutputStream();

            //发送需要执行的SHELL命令，需要用\n结尾，表示回车
            String shellCommand = "ls \n";
            outstream.write(shellCommand.getBytes());
            outstream.flush();

            //获取命令执行的结果
            if (instream.available() > 0) {
                byte[] data = new byte[instream.available()];
                int nLen = instream.read(data);

                if (nLen < 0) {
                    throw new Exception("network error.");
                }

                //转换输出结果并打印出来
                String temp = new String(data, 0, nLen, "iso8859-1");
                System.out.println(temp);
            }
            outstream.close();
            instream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.disconnect();
            channel.disconnect();
        }
    }

    public int execute(final String command) {
        int returnCode = 0;
        JSch jsch = new JSch();
        MyUserInfo userInfo = new MyUserInfo();

        try {
            // Create and connect session.  
            Session session = jsch.getSession(username, ipAddress, DEFAULT_SSH_PORT);
            session.setPassword(password);
            session.setUserInfo(userInfo);
            session.connect();

            // Create and connect channel.  
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader(channel
                    .getInputStream()));

            channel.connect();
            System.out.println("The remote command is: " + command);

            // Get the output of remote command.  
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
                
            }
            input.close();

            // Get the return code only after the channel is closed.  
            if (channel.isClosed()) {
                returnCode = channel.getExitStatus();
            }

            // Disconnect the channel and session.  
            channel.disconnect();
            session.disconnect();
        } catch (JSchException e) {
            // TODO Auto-generated catch block  
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnCode;
    }

    public Vector<String> getStandardOutput() {
        return stdout;
    }

    public static void main(final String[] args) throws IOException {
        char ch;
        Ssh sshExecutor = new Ssh("192.168.0.152", "pi", "raspberry");
        for (;;) {
            sshExecutor.execute("ls");
            Vector<String> stdout = sshExecutor.getStandardOutput();
            for (String str : stdout) {
                System.out.println(str);
            }
            ch = (char) System.in.read();
        }
    }
}

/**
 * This class provide interface to feedback information to the user.
 */
class MyUserInfo implements UserInfo {

    private String password;

    private String passphrase;

    @Override
    public String getPassphrase() {
        System.out.println("MyUserInfo.getPassphrase()");
        return null;
    }

    @Override
    public String getPassword() {
        System.out.println("MyUserInfo.getPassword()");
        return null;
    }

    @Override
    public boolean promptPassphrase(final String arg0) {
        System.out.println("MyUserInfo.promptPassphrase()");
        System.out.println(arg0);
        return false;
    }

    @Override
    public boolean promptPassword(final String arg0) {
        System.out.println("MyUserInfo.promptPassword()");
        System.out.println(arg0);
        return false;
    }

    @Override
    public boolean promptYesNo(final String arg0) {
        System.out.println("MyUserInfo.promptYesNo()");
        System.out.println(arg0);
        if (arg0.contains("The authenticity of host")) {
            return true;
        }
        return false;
    }

    @Override
    public void showMessage(final String arg0) {
        System.out.println("MyUserInfo.showMessage()");
    }
}
