/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Administrator
 */
public class IP138 {
    public static String getMyIP() throws IOException {  
        InputStream ins = null;  
        try {  
            URL url = new URL("http://1111.ip138.com/ic.asp");  
            URLConnection con = url.openConnection();  
           ins = con.getInputStream();  
            InputStreamReader isReader = new InputStreamReader(ins, "GB2312");  
            BufferedReader bReader = new BufferedReader(isReader);  
            StringBuffer webContent = new StringBuffer();  
            String str = null;  
           while ((str = bReader.readLine()) != null) {  
                webContent.append(str);  
            }  
            int start = webContent.indexOf("[") + 1;  
            int end = webContent.indexOf("]");  
            return webContent.substring(start, end);  
        } finally {  
            if (ins != null) {  
                ins.close();  
            }  
        }  
    }

}
