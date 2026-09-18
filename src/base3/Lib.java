/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author kevin
 */
public class Lib {

    public static int ptLevel = 3;
    public static List<String> lsClassName;
    public static List<String> lsClassData;
    public static String errString;

    static int error_f = 0;
    static int valueInt = 0;
    static long valueLong = 0;
    static float valueFloat = 0;
    static double valueDouble = 0;
    static int valueType = 0;//0: none, 1: int, 2: long, 3:float, 4:double

    static String retstr;
    static char[] asciiTbl = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static int reti;
    static float retf;
    static String rets;
    static ArrayList<String> retsal = new ArrayList();
    static int[] retia;
    static float[] retfa;

    //return errflag
    public static int exe(String exestr) {
        try {
            Process process = Runtime.getRuntime().exec(exestr);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            return 1;
        }
    }
    public static int chkProcessExist(String processName) {
            String exeCmdStr="tasklist";
            if (GB.osName.equals("linux")) {
                exeCmdStr="ps -ef";
            }
            String retStr=Lib.exeCmdRet(exeCmdStr);
            if(retStr!=null){
                String[] strA=retStr.split("\n");
                for(int i=0;i<strA.length;i++){
                    if(strA[i].contains(processName))
                        return 1;
                }
            }
            return 0;    
        
        
    }
    public static String exeCmdRet(String cmd) {
        String retStr = null;
        String[] command=new String[]{"cmd.exe", "/c",""};
        if (GB.osName.equals("linux")) {
            command=new String[]{"sh", "-c",""};
        }
        command[2]=cmd;
        try {
            // Create a ProcessBuilder instance
            ProcessBuilder builder = new ProcessBuilder(command);
            // Optionally, redirect error stream to output stream
            builder.redirectErrorStream(true);
            // Start the process
            Process process = builder.start();
            // Read the output from the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return retStr;
    }

    static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    static int readParaSet(String keyName, int errOut) {
        int ibuf = errOut;
        try {
            Gson gson = new Gson();
            String content = Lib.readFile("paraSet.json");
            JsonObject jsPara = JsonParser.parseString(content).getAsJsonObject();
            ibuf = jsPara.get(keyName).getAsInt();
            return ibuf;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ibuf;
    }

    static void writeParaSet(String keyName, int ibuf) {
        try {
            Gson gson = new Gson();
            String content = Lib.readFile("paraSet.json");
            JsonObject jsPara = JsonParser.parseString(content).getAsJsonObject();
            ibuf = jsPara.get(keyName).getAsInt();
            ibuf ^= 1;
            jsPara.add(keyName, gson.toJsonTree(ibuf));
            content = jsPara.toString();
            BufferedWriter outf = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("paraSet.json"), "UTF-8"));
            try {
                outf.write(content);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                outf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static String[] splitSeg(String _istr) {
        String istr = _istr.trim();
        int len = istr.length();
        List<String> list = new ArrayList<String>();
        String nstr = "";
        int start_f = 0;
        for (int i = 0; i < len; i++) {
            char ch = istr.charAt(i);
            if (ch == ' ') {
                if (start_f == 0) {
                    continue;
                }
                if (istr.charAt(i + 1) == ' ') {
                    list.add(nstr);
                    start_f = 0;
                    continue;
                }
                nstr += ch;
                continue;
            } else {
                if (start_f == 0) {
                    start_f = 1;
                    nstr = "";
                }
                nstr += ch;
            }
        }
        if (start_f == 1) {
            list.add(nstr);
        }
        return list.toArray(new String[0]);

    }

    static int jsonPut(JSONObject jobj, String key, Object obj) {
        try {
            jobj.put(key, obj);
            return 0;
        } catch (Exception ex) {
            return -1;
        }

    }

    static public int ping(String ip, int wait_tim) {
        int i = 0;
        try {
            if (InetAddress.getByName(ip).isReachable(wait_tim)) {
                return 0;
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

//Ok return 0:
    //else return 1;
    public static final int ping(String hostname) {
        try {
            if (GB.os_inx == 0) //n=tx count w=wait time
            {
                //return Runtime.getRuntime().exec("ping -n 1 -w 1000 " + hostname).waitFor();  //windows
                return ping(hostname, 1000);
            }
            if (GB.os_inx == 1) {
                return Runtime.getRuntime().exec("ping -c 1 " + hostname).waitFor();  //linux
            }
            return 1;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static final int linuxPing(String hostname) {
        try {
            return Runtime.getRuntime().exec("ping -c 1 " + hostname).waitFor();  //linux
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    static String readFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, 0, bytes.length, "UTF-8");
    }

    static String actResponse(String act, String status, int cmdInx) {
        String str = "{";
        str += "\"act\":\"actResponse\"";
        str += ", \"actName\":\"" + act + "\"";
        str += ", \"status\":\"" + status + "\"";
        str += ", \"cmdInx\":" + cmdInx;
        str += ", \"reti\":1";
        str += "}";
        return str;
    }

    static String actResponseContent(String act, String status, int cmdInx, String content) {
        String str = "{";
        str += "\"act\":\"actResponse\"";
        str += ", \"actName\":\"" + act + "\"";
        str += ", \"status\":\"" + status + "\"";
        str += ", \"cmdInx\":" + cmdInx;
        str += ", \"reti\":1";
        str += ", \"content\":\"" + content + "\"";
        str += "}";
        return str;
    }

    public static String byteToHexString(byte bt) {
        String str = "";
        str += Lib.asciiTbl[(bt >> 4) & 15];
        str += Lib.asciiTbl[bt & 15];
        return str;
    }

    public static byte[] toBytes(String str, String type) {
        if (str.length() == 0) {
            return null;
        }
        if (type.equals("ASCII")) {
            return str.getBytes();
        }

        if (type.equals("HEX")) {
            int[] ints = new int[4096];
            byte[] bts = str.getBytes();
            int binx = 0;
            int valueInx = 0;
            for (int i = 0; i < bts.length; i++) {
                if (bts[i] == ' ' || bts[i] == ',') {
                    binx = 0;
                    continue;
                }
                binx++;
                int value;
                if (binx == 2) {
                    if (bts[i - 1] == '0' && bts[i] == 'x') {
                        binx = 0;
                        continue;
                    }
                    if (bts[i - 1] == '0' && bts[i] == 'X') {
                        binx = 0;
                        continue;
                    }
                    binx = 0;
                    value = 0;
                    for (int j = 0; j < 2; j++) {
                        value = value * 16;
                        byte bt = bts[i + j - 1];
                        if (bt >= '0' && bt <= '9') {
                            value += bt - '0';
                            continue;
                        }
                        if (bt >= 'a' && bt <= 'f') {
                            value += bt - 'a' + 10;
                            continue;
                        }
                        if (bt >= 'A' && bt <= 'F') {
                            value += bt - 'A' + 10;
                            continue;
                        }
                        return null;
                    }
                    ints[valueInx] = value;
                    valueInx++;
                }

            }
            byte[] retBytes = new byte[valueInx];
            for (int i = 0; i < retBytes.length; i++) {
                retBytes[i] = (byte) ints[i];
            }
            return retBytes;

        }
        return null;
    }

    public static byte[] toHexBytes(String str) {
        if (str.length() == 0) {
            return null;
        }
        String[] strA = str.split(",");
        byte[] retBytes = new byte[strA.length];

        for (int i = 0; i < strA.length; i++) {
            int ibuf = Integer.parseInt(strA[i], 16);
            retBytes[i] = (byte) ibuf;
        }
        return retBytes;
    }

    public static void log(String inf) {
        System.out.println(inf);
    }

    public static void lp1(String inf) { //print 0       hard
        if (ptLevel < 1) {
            System.out.println(inf);
        }
    }

    public static void lp2(String inf) {    //punint 0,1t   middle
        if (ptLevel < 2) {
            System.out.println(inf);
        }
    }

    public static void lp3(String inf) {     //print 0,1,2   easy
        if (ptLevel < 3) {
            System.out.println(inf);
        }
    }

    public static void putJos(JSONObject jo, String key, Object value) {
        try {
            //jo.accumulate(key, value);  //if exist trans to array
            jo.put(key, value);//添加元素
            //jo.append(key, value);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void unzipFile(String zipName, String _destDir) throws IOException {
        String fileZip = zipName;
        File destDir = new File(_destDir);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();

            // ...
        }

        zis.closeEntry();
        zis.close();
    }

    public static void zipDir(String dirName, String zipName) throws IOException {
        //dirName="dirName";
        //zipName="dirCompressed.zip";
        String sourceFile = dirName;
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                String zipFile = childFile.getName();
                String[] strA = zipFile.split("\\.");
                if (strA.length == 2) {
                    if (strA[1].equals("kvzip")) {
                        continue;
                    }
                    if (strA[1].equals("kvbin")) {
                        continue;
                    }
                }
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

// 取設定檔屬性Map
    public static HashMap<String, String> XMLMap(String fileName, String strType) throws Exception {
        HashMap<String, String> mapXML = new HashMap<String, String>();
        File file = new File(fileName);
        // 建立工廠
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 建立解析器
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        // 建立 Xpath 
        XPath xPath = XPathFactory.newInstance().newXPath();
        // 設定節點 Xpath
        String expression = "/properties/propertie[@name='" + strType + "']/item"; // 路徑
        // 取得結點資料
        NodeList nodelist = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        // 讀取properties.xml放入MAP
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            Element element = (Element) node;
            mapXML.put(element.getAttribute("key"), element.getAttribute("value"));
        }
        return mapXML;
    }

    public static void WriteSomthing() throws Exception {
        // 度取資料
        String strDir = System.getProperty("user.dir");
        File file = new File(strDir + "\\properties.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder(); // 建立解析器
        Document doc = builder.parse(file);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/properties/propertie[@name='Login']/item[@key='USER']"; // 路徑
        NodeList node = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        // 取出 element 
        Element element = (Element) node.item(0);

        // 寫入檔案
        // 修改參數
        element.setAttribute("value", "PedroTest");
        // 建立轉換器
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // source 不一定是整份檔案，只要 xml 格式正確可以是子階樹結構
        DOMSource source = new DOMSource(doc);
        // 寫入的檔案
        FileWriter writer = new FileWriter(new File(file.getPath()));
        // 可以用 java.io.Writer or java.io.OutputStream 來創建 StreamResult.
        StreamResult result = new StreamResult(writer);
        // 寫入原來的檔案
        transformer.transform(source, result);
        // // 寫入到其他路徑
        // StreamResult resultToFile = new StreamResult(new File("C:/temp/XMLFromPathValue.xml"));
        // transformer.transform(source, resultToFile);
    }

    public String josnToJs(String inStr) {

        String outStr = "";
        JSONObject jobj;
        String key;
        Object value;
        String str;
        int len = inStr.length();
        int fg = 0;
        for (int i = 0; i < len; i++) {
            if (inStr.charAt(i) == '{') {
                fg = 1;
                break;
            }
            if (inStr.charAt(i) == '[') {
                fg = 2;
                break;
            }
        }
        try {
            if (fg == 0) {
                Lib.errString = "Json Format First frror !!!";
                return null;
            }
            if (fg == 1) {
                jobj = new JSONObject(inStr);
                Iterator<String> it = jobj.keys();
                int first = 0;
                while (it.hasNext()) {
                    key = it.next();
                    if (first != 0) {
                        outStr += ',';
                    }
                    first = 1;
                    value = jobj.get(key);
                    str = value.toString();

                    for (int j = 0; j < str.length(); j++) {
                        if (value.getClass().getSimpleName().equals("String")) {
                            value = "\"" + value.toString() + "\"";
                        }
                    }
                    outStr += "\n" + key + "=" + value;
                }

            }
        } catch (Exception ex) {
            Lib.errString = ex.toString();
            return null;

        }

        return outStr;

    }

    //=======================================================================
    public static String fileToString(String fileName) {
        try {
            String contents;
            contents = new String(Files.readAllBytes(Paths.get(fileName)));
            //return new String(contents.getBytes("utf-8"),"utf-8");
            return contents;
        } catch (IOException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static String readStringFile(String fileName) {
        String content = "";
        int first = 0;
        try {
            File file = new File(fileName);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            //BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (first == 1) {
                    content += "\n";
                }
                first = 1;
                content += currentLine;
            }
            reader.close();
            return content;
        } catch (Exception ex) {
            Lib.errString = ex.toString();
            ex.printStackTrace();
        }
        return null;
    }

    //=======================================================================
    public static boolean saveFile(String _fileName, String inStr) {
        String fileName = _fileName;
        FileOutputStream outfile;
        try {
            outfile = new FileOutputStream(fileName);
            outfile.write(inStr.getBytes("utf-8"));
            outfile.close();
            return true;
        } catch (FileNotFoundException ex) {
            Lib.log("FileNotFound: " + fileName);
            return false;
        } catch (IOException ex) {
            Lib.log("SaveFileError: " + fileName);
            return false;
        }
    }
    //=======================================================================

    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

    public static int copyFile(String sourceName, String destName) {
        File source = new File(sourceName);
        File dest = new File(destName);

        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        int err = 0;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();
        } catch (IOException ex) {
            Lib.log("CopuFileError: " + source + " -> " + dest);
            err = 1;
        }
        return err;
    }

    public static void prt(String str) {
        System.out.println(str);
    }

    public static long str2long(String str, long errorCode) {
        long lg = errorCode;
        try {
            lg = Long.parseLong(str);
        } catch (NumberFormatException ex) {
            System.out.println("parseLong Error line");
        }
        return lg;
    }

    public static int str2int(String str, int default_i, int max, int min) {
        try {

            int ibuf = Integer.parseInt(str);
            if (ibuf > max) {
                return default_i;
            }
            if (ibuf < min) {
                return default_i;
            }
            return ibuf;
        } catch (NumberFormatException e) {
            return default_i;
        }
    }

    public static int str2int(String str, int errorCode) {
        int lg = errorCode;
        try {
            lg = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            System.out.println("parseLong Error line");
        }
        return lg;
    }

    public static float str2float(String str, float errorCode) {
        float lg = errorCode;
        try {
            lg = Float.parseFloat(str);
        } catch (NumberFormatException ex) {
            System.out.println("parseFlat Error line");
        }
        return lg;
    }

    static Object getJson(JSONObject jobj, String key) {
        try {
            return jobj.get(key);//添加元素
        } catch (JSONException ex) {
        }
        return null;
    }

    //".\\jsA.js"
    public static String fileFunc2str(String fileName, String funcName) {
        int i;
        char ch;
        char[] chs;
        String str = "";
        FileReader fr;
        int index = 0;
        chs = funcName.toCharArray();
        char[] cbuf = new char[chs.length];
        int cbuf_len = 0;
        int cbuf_inx1 = 0;
        int cbuf_inx2 = 0;
        int ibuf;

        try {
            fr = new FileReader(fileName);
            try {
                //===========================================
                for (;;) {
                    if (cbuf_len > 0) {
                        ch = cbuf[cbuf_inx2++];
                        cbuf_len--;
                    } else {
                        ibuf = fr.read();
                        if (ibuf == -1) {
                            break;
                        }
                        ch = (char) ibuf;
                        cbuf[cbuf_inx1++] = ch;
                    }
                    if (ch == chs[index++]) {
                        if (index == chs.length) {
//==============================================================================                            
                            int func_start_f = 0;
                            int mark_start_f = 0;
                            int quot_cnt = 0;
                            String func_str = "";

                            for (;;) {
                                ibuf = fr.read();
                                if (ibuf == -1) {
                                    return "";
                                }
                                ch = (char) ibuf;
                                func_str += ch;
                                if (mark_start_f == 1) {
                                    if (ch == '/') {
                                        mark_start_f = 2;
                                        continue;
                                    }
                                    if (ch == '*') {
                                        mark_start_f = 3;
                                        continue;
                                    }
                                    mark_start_f = 0;
                                }
                                if (mark_start_f == 2) {
                                    if (ch == '\n') {
                                        mark_start_f = 0;
                                    }
                                    continue;
                                }
                                if (mark_start_f == 3) {
                                    if (ch == '*') {
                                        mark_start_f = 4;
                                    }
                                    continue;
                                }
                                if (mark_start_f == 4) {
                                    if (ch == '/') {
                                        mark_start_f = 0;
                                        continue;
                                    }
                                    mark_start_f = 3;
                                    continue;
                                }
                                if (ch == '/') {
                                    mark_start_f = 1;
                                    continue;
                                }
                                if (func_start_f == 0) {
                                    if (ch == '{') {
                                        func_start_f = 1;
                                        quot_cnt = 1;
                                    }
                                    continue;
                                }
                                if (ch == '{') {
                                    quot_cnt++;
                                }
                                if (ch == '}') {
                                    quot_cnt--;
                                    if (quot_cnt == 0) {
                                        return funcName + func_str;

                                    }
                                }
                            }
//==============================================================================                            
                        }
                    } else {
                        if (cbuf_inx1 > 0) {
                            cbuf_len = cbuf_inx1 - 1;
                            cbuf_inx2 = 1;
                        }
                        index = 0;
                        cbuf_inx1 = 0;
                    }
                }
                return "";
            } catch (IOException ex) {
                Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        }

        return str;
    }

    public static String fileData2str(String fileName, String sepstr) {
        int i;
        int ibuf;
        char ch;
        char[] chs;
        String str = "";
        FileReader fr;
        chs = sepstr.toCharArray();
        char[] cbuf = new char[chs.length];
        int index = 0;
        int cbuf_len = 0;
        int cbuf_inx1 = 0;
        int cbuf_inx2 = 0;
        int same_cnt = 0;
        String middata_str = "";
        try {
            fr = new FileReader(fileName);
            try {
                //===========================================
                for (;;) {
                    if (cbuf_len > 0) {
                        ch = cbuf[cbuf_inx2++];
                        cbuf_len--;
                    } else {
                        ibuf = fr.read();
                        if (ibuf == -1) {
                            break;
                        }
                        ch = (char) ibuf;
                        cbuf[cbuf_inx1++] = ch;
                    }
                    if (same_cnt == 1) {
                        middata_str += ch;
                    }
                    if (ch == chs[index++]) {
//==============================================================================                            
                        if (index == chs.length) {
                            if (same_cnt == 0) {
                                index = 0;
                                cbuf_len = 0;
                                cbuf_inx1 = 0;
                                cbuf_inx2 = 0;
                                same_cnt++;
                            } else {
                                return middata_str.substring(0, middata_str.length() - chs.length);
                            }

                        }
//==============================================================================                            
                    } else {
                        if (cbuf_inx1 > 0) {
                            cbuf_len = cbuf_inx1 - 1;
                            cbuf_inx2 = 1;
                        }
                        index = 0;
                        cbuf_inx1 = 0;
                    }
                }
                return "";
            } catch (IOException ex) {
                Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        }

        return str;
    }

    public static List<String> getFileClassNames(String fileName, String[] sepstr) {
        List<String> lsClassName = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String[] strA, strB;
            String className = "";
            int findInx = 0;
            String line;
            while (true) {
                line = reader.readLine();

                if (line == null) {
                    break;
                }
                if (line.contains(sepstr[0])) {
                    strA = line.split(sepstr[0]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[1]);
                    if (strB.length != 2) {
                        continue;
                    }
                    className = strB[0];
                    findInx = 1;
                    continue;
                }
                if (findInx == 0) {
                    continue;
                }
                if (line.contains(sepstr[2])) {
                    findInx = 0;
                    strA = line.split(sepstr[2]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[3]);
                    if (strB.length != 2) {
                        continue;
                    }
                    if (!className.equals(strB[0])) {
                        continue;
                    }
                    lsClassName.add(className);
                    System.out.println(className);
                }
            }
            reader.close();
            return lsClassName;
        } catch (IOException e) {
            return lsClassName;
        }

    }

    public static void getModelClassLib(String fileName, String[] sepstr) {
        BufferedReader reader;
        Lib.lsClassName = new ArrayList<>();
        Lib.lsClassData = new ArrayList<>();

        try {
            File f = new File(fileName);
            InputStreamReader read = new InputStreamReader(new FileInputStream(f), "UTF-8");
            reader = new BufferedReader(read);
            //reader = new BufferedReader(new FileReader(fileName));
            String[] strA, strB;
            String className = "";
            String classData = "";

            int findInx = 0;
            String line;
            while (true) {
                line = reader.readLine();

                if (line == null) {
                    break;
                }
                classData += "\n" + line;
                if (line.contains(sepstr[0])) {
                    strA = line.split(sepstr[0]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[1]);
                    if (strB.length != 2) {
                        continue;
                    }
                    className = strB[0];
                    classData = line;
                    findInx = 1;
                    continue;
                }
                if (findInx == 0) {
                    continue;
                }
                if (line.contains(sepstr[2])) {
                    findInx = 0;
                    strA = line.split(sepstr[2]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[3]);
                    if (strB.length != 2) {
                        continue;
                    }
                    if (!className.equals(strB[0])) {
                        continue;
                    }
                    Lib.lsClassName.add(className);
                    Lib.lsClassData.add(classData);
                }
            }
            reader.close();
        } catch (IOException e) {
        }
    }

    public static void saveModelClassLib(String fileName, String className, String addString) {
        FileOutputStream outfile = null;
        String source = "";
        System.out.println(fileName);

        for (int i = 0; i < Lib.lsClassData.size(); i++) {
            System.out.println(Lib.lsClassName.get(i));
            if (!Lib.lsClassName.get(i).equals(className)) {
                source += "\n";
                source += Lib.lsClassData.get(i);
            }
        }
        source += "\n";
        source += addString;
        try {
            outfile = new FileOutputStream(fileName);
            outfile.write(source.getBytes("utf-8"));
            outfile.close();
        } catch (IOException e) {

        }
    }

    public static void removeModelClassLib(String fileName, String className) {
        FileOutputStream outfile = null;
        String source = "";
        for (int i = 0; i < Lib.lsClassName.size(); i++) {
            if (!Lib.lsClassName.get(i).equals(className)) {
                source += Lib.lsClassData.get(i);
            }
        }
        try {
            outfile = new FileOutputStream(fileName);
            outfile.write(source.getBytes("utf-8"));
            outfile.close();
        } catch (IOException e) {
        }
    }

    public static String json2Obj(String jsonStr) {
        JsData jd = new JsData(jsonStr);
        jd.transObj();
        return jd.outStr;
    }

    public static String[] toStringArray(JSONArray array) {
        if (array == null) {
            return null;
        }

        String[] arr = new String[array.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = array.optString(i);
        }
        return arr;
    }

    public static ArrayList<String> toStringList(JSONArray array) {
        if (array == null) {
            return null;
        }
        ArrayList<String> strList = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            strList.add(array.optString(i));
        }
        return strList;
    }

    public static void thSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {

        }
    }

    public static String stringListToString(ArrayList<String> alString) {
        String outStr = "[";
        for (int i = 0; i < alString.size(); i++) {
            if (i != 0) {
                outStr += ",";
            }
            outStr += "\"";
            outStr += alString.get(i);
            outStr += "\"";
        }
        outStr += "]";
        return outStr;

    }

    static int getOs() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            return 0;
        }
        if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            return 1;
        }
        if (OS.contains("mac")) {
            return 2;
        }
        if (OS.contains("sunos")) {
            return 3;
        }
        return -1;

    }

    static void dechop(byte[] hop, byte[] enckey) {
        int i, j, ibuf, ibuf1;
        for (i = 0; i < 11; i++) {
            for (j = 0; j < 48; j++) {
                ibuf = 1;
                if ((hop[3] & 0x08) != 0) {
                    ibuf = 0x10;
                }
                if ((hop[2] & 0x01) != 0) {
                    ibuf <<= 2;
                }

                if ((hop[1] & 0x01) != 0) {
                    ibuf <<= 1;
                }
                if ((hop[4] & 0x40) != 0) {
                    ibuf1 = 0x5c;
                    if ((hop[4] & 0x02) != 0) {
                        ibuf1 = 0x3a;
                    }
                } else {
                    ibuf1 = 0x2e;
                    if ((hop[4] & 0x02) != 0) {
                        ibuf1 = 0x74;
                    }
                }
                ibuf = ibuf & ibuf1;
                if (ibuf != 0) {
                    ibuf = 0x80;
                }
                ibuf ^= hop[2];
                ibuf ^= hop[4];
                ibuf ^= enckey[1];
                ibuf = ibuf << 1;
                hop[1] = (byte) (hop[1] << 1);
                hop[2] = (byte) (hop[2] << 1);
                hop[3] = (byte) (hop[3] << 1);
                hop[4] = (byte) (hop[4] << 1);
                if ((ibuf & 0x100) != 0) {
                    hop[1]++;
                }
                if ((hop[1] & 0x100) != 0) {
                    hop[2]++;
                }
                if ((hop[2] & 0x100) != 0) {
                    hop[3]++;
                }
                if ((hop[3] & 0x100) != 0) {
                    hop[4]++;
                }
                enckey[0] <<= 1;
                enckey[1] <<= 1;
                enckey[2] <<= 1;
                enckey[3] <<= 1;
                enckey[4] <<= 1;
                enckey[5] <<= 1;
                enckey[6] <<= 1;
                enckey[7] <<= 1;
                if ((enckey[7] & 0x100) != 0) {
                    enckey[0]++;
                }
                if ((enckey[0] & 0x100) != 0) {
                    enckey[1]++;
                }
                if ((enckey[1] & 0x100) != 0) {
                    enckey[2]++;
                }
                if ((enckey[2] & 0x100) != 0) {
                    enckey[3]++;
                }
                if ((enckey[3] & 0x100) != 0) {
                    enckey[4]++;
                }
                if ((enckey[4] & 0x100) != 0) {
                    enckey[5]++;
                }
                if ((enckey[5] & 0x100) != 0) {
                    enckey[6]++;
                }
                if ((enckey[6] & 0x100) != 0) {
                    enckey[7]++;
                }
            }
        }
    }

    static String rdInterfaces(String cmpstr) {
        String fnameInterfaces = GB.interfaces_path;
        File f = new File(fnameInterfaces);
        if (f.exists() && !f.isDirectory()) {
            FileReader fr;
            BufferedReader br;
            String[] fields;
            String tmp;
            String str;

            try {
                fr = new FileReader(fnameInterfaces);
                br = new BufferedReader(fr);
                while ((tmp = br.readLine()) != null) {
                    if (tmp.contains(cmpstr)) {
                        str = tmp.trim();
                        fields = str.split("[ ]+");
                        return fields[1];
                    }
                }
                fr.close();
                br.close();
                return null;
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }

        } else {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
            }
        }
        return null;

    }

    //return 1:ok  0:err 
    public static int wrInterfaces(String netName, String ipStr, String maskStr, String gatewayStr) {
        String fname;
        String bstr;
        fname = GB.interfaces_path;
        try {
            FileWriter fw = new FileWriter(fname);
            fw.write("\nauto lo");
            fw.write("\niface lo inet loopback");
            fw.write("\n");
            fw.write("\nauto " + netName);
            fw.write("\niface " + netName + " inet static");
            bstr = "\naddress " + ipStr;
            fw.write(bstr);
            bstr = "\nnetmask " + maskStr;
            fw.write(bstr);
            bstr = "\ngateway " + gatewayStr;
            fw.write(bstr);
            fw.flush();
            fw.close();
            return 1;
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return 0;
    }

    static int search(String str, String st, String end) {
        int sti, endi;
        sti = str.indexOf(st);
        if (sti < 0) {
            return -1;
        }
        endi = str.indexOf(end, sti + st.length());
        if (endi < 0) {
            return -1;
        }
        retstr = str.substring(sti + st.length(), endi);
        return 1;
    }

    static int searchEnd(String str, String st, String end) {
        int sti, endi;
        sti = str.indexOf(st);
        if (sti < 0) {
            return -1;
        }
        endi = str.indexOf(end, sti + st.length());
        if (endi < 0) {
            endi = str.length();
        }
        retstr = str.substring(sti + st.length(), endi);
        return 1;
    }

    public static ArrayList<String> readFileNames(String initDir, String[] compareNames) {
        String[] strA;
        String fileName;
        ArrayList<String> fileNameList = new ArrayList<String>();
        File folder = new File(initDir);
        if (folder.exists() && folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            loop1:
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    fileName = listOfFiles[i].getName();
                    for (int j = 0; j < compareNames.length; j++) {
                        if (compareString(fileName, compareNames[j].trim()) == 1) {
                            fileNameList.add(fileName);
                            //System.out.println("File " + fileName);
                            break;
                        }
                    }
                    /*
                    if(extNames.length==0){
                        fileNameList.add("./" + initDir + "/" + fileName);
                        continue;
                    }
                    strA = fileName.split("\\.");
                    if (strA.length < 2) {
                        continue;
                    }
                    int j;
                    for(j=0;j<extNames.length;j++){
                        if(strA[strA.length - 1].equals(extNames[i]))
                            break;
                        if(strA[strA.length - 1].equals(extNames[i]))
                            break;
                        if(strA[strA.length - 1].equals(extNames[i]))
                            break;
                    }
                    if(j==extNames.length)
                        continue;
                    fileNameList.add("./" + initDir + "/" + fileName);
                    //System.out.println("File " + fileName);
                     */
                } else if (listOfFiles[i].isDirectory()) {
                    //System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
        }
        return fileNameList;
    }

    //**find the string from between given string st and end
    /**
     * Find the string between given string stStr and endStr
     *
     * @param inStr The input string.
     * @param stStr The first match string. if is null from position 0.
     * @param endStr The end match string. if is null from position end.
     * @return if doesn't find the stStr or endStr return null, else return the
     * string between stStr and endStr.
     */
    static String getStrBetween(String inStr, String stStr, String endStr) {
        int sti, endi;
        if (stStr == null) {
            sti = 0;
        } else {
            sti = inStr.indexOf(stStr);
            if (sti < 0) {
                return null;
            }
        }
        if (endStr == null) {
            endi = inStr.length();
        } else {
            endi = inStr.indexOf(endStr, sti + stStr.length());
            if (endi < 0) {
                return null;
            }
        }
        retstr = inStr.substring(sti + stStr.length(), endi);
        return retstr;
    }

    static String insertStrBetween(String inStr, String insStr, int st, int end) {
        String firstStr = inStr.substring(0, st);
        String endStr = inStr.substring(end, inStr.length());
        return firstStr + insStr + endStr;
    }

    static boolean readSetdataFileToPara(String fileFullName, HashMap hmap) {
        FileReader reader;
        try {
            reader = new FileReader(fileFullName);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String paraN;
            String paraV;
            String str;
            while ((line = br.readLine()) != null) {
                str = line.trim();
                if (str.length() == 0) {
                    continue;
                }
                if (str.charAt(0) == '#') {
                    continue;
                }
                paraN = Lib.getStrBetween(str, "[", "]");
                if (paraN == null) {
                    continue;
                }
                paraN = paraN.trim();
                paraV = Lib.getStrBetween(str, "<", ">");
                if (paraV == null) {
                    continue;
                }
                hmap.put(paraN, paraV);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static int compareString(String orgStr, String cmpStr) {
        int olen = orgStr.length();
        int clen = cmpStr.length();
        int i, j, k, ibuf;
        String sbuf, tbuf;
        ibuf = cmpStr.indexOf('*');
        if (ibuf == -1) {
            if (orgStr.equals(cmpStr)) {
                return 1;
            }
            return 0;
        }
        if (ibuf == 0) {    //first *
            if (clen == 1) //cmpStr="*"
            {
                return 1;
            }
            if (cmpStr.charAt(clen - 1) == '*') {     //comStr="*xxx*"
                sbuf = cmpStr.substring(1, clen - 1);
                if (orgStr.indexOf(sbuf) == -1) {
                    return 0;
                }
                return 1;
            }
            sbuf = cmpStr.substring(1);

            int slen = sbuf.length();
            for (i = 0; i < slen; i++) {
                if (sbuf.charAt(slen - 1 - i) != orgStr.charAt(olen - 1 - i)) {
                    return 0;
                }
            }
            return 1;
        }
        if (ibuf == clen - 1) {   //end *
            sbuf = cmpStr.substring(0, clen - 1);
            ibuf = orgStr.indexOf(sbuf);
            if (ibuf != 0) {
                return 0;
            }
            return 1;
        }
        sbuf = cmpStr.substring(0, ibuf);
        tbuf = cmpStr.substring(ibuf + 1, clen);
        ibuf = orgStr.indexOf(sbuf);
        if (ibuf != 0) {
            return 0;
        }
        int tlen = tbuf.length();
        for (i = 0; i < tlen; i++) {
            if (tbuf.charAt(tlen - 1 - i) != orgStr.charAt(olen - 1 - i)) {
                return 0;
            }
        }
        if (sbuf.length() + tbuf.length() > olen) {
            return 0;
        }
        return 1;
    }

}

class JsData {

    String inStr = "";
    String outStr = "";
    int inx = 0;
    int err_i = 0;
    int len;

    JsData(String str) {
        inStr = str;
        len = inStr.length();
    }

    public void transObj() {
        char ch;
        int mm_i = 0;
        int array_i = 0;
        for (; inx < len; inx++) {
            ch = inStr.charAt(inx);
            outStr += ch;
            if (ch == '{') {
                inx++;
                tobj();
            }
        }
    }

    public void tobj() {
        char ch;
        int mm_i = 0;
        int array_i = 0;
        int pp_i = 0;
        for (; inx < len; inx++) {
            ch = inStr.charAt(inx);
            if (mm_i == 0) {
                if (ch == '\"') {
                    continue;
                }
                if (ch == ':') {
                    mm_i = 1;
                }
                outStr += ch;
                continue;
            } else {
                outStr += ch;
                if (ch == '[') {
                    array_i = 1;
                    continue;
                }
                if (ch == '\"') {
                    if (pp_i == 0) {
                        pp_i = 1;
                    } else {
                        pp_i = 0;
                    }
                    continue;
                }

                if (array_i == 0) {
                    if (ch == ',') {
                        if (pp_i == 0) {
                            outStr += '\n';
                            mm_i = 0;
                        }
                        continue;
                    }
                    if (ch == '{') {
                        inx++;
                        tobj();
                        continue;
                    }
                } else {
                    if (ch == ']') {
                        array_i = 0;
                        continue;
                    }
                    if (ch == '{') {
                        inx++;
                        tobj();
                        continue;
                    }
                    if (ch == '}') {
                        return;
                    }

                }
            }
        }
    }
}

abstract class MapCbk {

    public abstract String prg(String sendJid, Map<String, String> map);
}

abstract class AbsRxBytes {

    public abstract void rx(byte[] bytes, int len);
}

interface StrCallback {

    public String prg(String vstr);
}

interface BytesCallback {

    public String prg(byte[] bytes, int len);
}

interface ObjCallback {

    public String prg(Object vobj);
}

interface StrObjCallback {

    public String prg(String str, Object vobj);
}

class KvJson {

    String jstr = "";
    int keyCnt = 0;

    KvJson() {

    }

    void jStart() {
        keyCnt = 0;
        jstr = "{";
    }

    String jEnd() {
        keyCnt = 0;
        jstr += "}";
        return jstr;
    }

    void jadd(String key, int ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, long ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, short ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, byte ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, float ff) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ff;
        keyCnt++;
    }

    void jadd(String key, String ss) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ss;
        keyCnt++;
    }

    void jadd(String key, long[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        if (ia != null) {
            for (int i = 0; i < ia.length; i++) {
                if (i != 0) {
                    jstr += ",";
                }
                jstr += ia[i];
            }
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, int[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        if (ia != null) {
            for (int i = 0; i < ia.length; i++) {
                if (i != 0) {
                    jstr += ",";
                }
                jstr += ia[i];
            }
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, int[] ia, int len) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        int iaLen = len;
        if (ia != null) {
            for (int i = 0; i < iaLen; i++) {
                if (i != 0) {
                    jstr += ",";
                }
                jstr += ia[i];
            }
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, short[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < ia.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += ia[i];
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, byte[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < ia.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += ia[i] & 255;
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, float[] fa) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < fa.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += fa[i];
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, String[] sa) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < sa.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += sa[i];
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, int[][] iaa) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < iaa.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += "[";
            for (int j = 0; j < iaa[i].length; j++) {
                if (j != 0) {
                    jstr += ",";
                }
                jstr += iaa[i][j];
            }
            jstr += "]";
        }
        jstr += "]";
        keyCnt++;
    }

    static String objToJson(Object inst) {
        String ss;
        byte bb;
        int ii;
        long ll;
        float ff;
        double dd;
        String[] sa;
        byte[] ba;
        int[] ia;
        long[] la;
        float[] fa;
        double[] da;
        String jstr;
        try {
            Class aClassHandle = inst.getClass();
            Field[] fields = aClassHandle.getDeclaredFields();
            String jsonStr = "{";
            for (int i = 0; i < fields.length; i++) {
                Object value = fields[i].get(inst);
                if (value == null) {
                    continue;
                }
                String keyName = "";
                if (i != 0) {
                    keyName += ",";
                }
                keyName += "\"" + fields[i].getName() + "\": ";
                if (value instanceof String) {
                    ss = (String) value;
                    jsonStr += keyName + "\"" + ss.replace("\n", "\\n") + "\"";
                } else if (value instanceof Byte) {
                    bb = (Byte) value;
                    jsonStr += keyName + bb;
                } else if (value instanceof Integer) {
                    ii = (Integer) value;
                    jsonStr += keyName + ii;
                } else if (value instanceof Long) {
                    ll = (Long) value;
                    jsonStr += keyName + ll;
                } else if (value instanceof Float) {
                    ff = (Float) value;
                    jsonStr += keyName + ff;
                } else if (value instanceof Double) {
                    dd = (Double) value;
                    jsonStr += keyName + dd;
                } else if (value instanceof String[]) {
                    sa = (String[]) value;
                    jstr = "[";
                    for (int j = 0; j < sa.length; j++) {
                        if (j != 0) {
                            jstr += ",";
                        }
                        jstr += "\"" + sa[j].replace("\n", "\\n") + "\"";
                    }
                    jstr += "]";
                    jsonStr += keyName + jstr;
                } else if (value instanceof byte[]) {
                    ba = (byte[]) value;
                    jsonStr += keyName + Arrays.toString(ba);
                } else if (value instanceof int[]) {
                    ia = (int[]) value;
                    jsonStr += keyName + Arrays.toString(ia);
                } else if (value instanceof long[]) {
                    la = (long[]) value;
                    jsonStr += keyName + Arrays.toString(la);
                } else if (value instanceof float[]) {
                    fa = (float[]) value;
                    jsonStr += keyName + Arrays.toString(fa);
                } else if (value instanceof double[]) {
                    da = (double[]) value;
                    jsonStr += keyName + Arrays.toString(da);
                } else {
                    Class vClassHandle = value.getClass();
                    String classHandleName = vClassHandle.getName();
                    if (classHandleName.contains("HashMap")) {
                        HashMap<String, Object> map = (HashMap<String, Object>) value;
                        jsonStr += keyName + "{";
                        int kinx = 0;
                        for (String key : map.keySet()) {
                            if (kinx != 0) {
                                jsonStr += ",";
                            }
                            kinx++;
                            jsonStr += "\"" + key + "\": ";
                            String tstr = KvJson.objToJson(map.get(key));
                            jsonStr += tstr;
                        }
                        String testStr = value.toString();
                        jsonStr += "}";

                    }
                    if (classHandleName.contains("kevin")) {
                        if (classHandleName.contains("[L")) {
                            Object[] objA = (Object[]) value;
                            jsonStr += keyName + "[";
                            for (int j = 0; j < objA.length; j++) {
                                if (j != 0) {
                                    jsonStr += ",";
                                }
                                String tstr = KvJson.objToJson(objA[j]);
                                jsonStr += tstr;
                            }
                            jsonStr += "]";

                        } else {
                            String subJsonStr = KvJson.objToJson(value);
                            jsonStr += keyName + subJsonStr;
                        }
                    }
                }
            }
            jsonStr += "}";
            return jsonStr;
        } catch (Exception ex) {
            Logger.getLogger(KvJson.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

class ConnectCla {

    String name;
    int time = 0;
    int timeTh = 0;

    public ConnectCla(String _name, int _timeTh) {
        time = 0;
        name = _name;
        timeTh = _timeTh;
    }
}
