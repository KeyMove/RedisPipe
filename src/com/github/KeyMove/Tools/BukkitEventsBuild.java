/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import com.github.KeyMove.EventsManager.EventManger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.out;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Administrator
 */
public class BukkitEventsBuild {
    
    static List<String> UseEventsList=new ArrayList<>();
    static List<String> PacketInList=new ArrayList<>();
    static Class PacketHookClass;
    static boolean isPacketLoad=false;
    static String PacketInListenerName;
    static boolean log=true;
    public static void Log(Object obj){
        if(log)
            out.print("[RedisPipe] "+obj);
    }
    public static Class LoadClassFile(String filepath,ClassLoader cloader){
        Class desClass=null;
        String classname=filepath.substring(filepath.lastIndexOf("\\")+1, filepath.lastIndexOf("."));
        String p=filepath.substring(0,filepath.lastIndexOf("\\"));
        
        URL[] path;
        try {
            File file=new File(p);
            path = new URL[]{file.toURI().toURL()};
            URLClassLoader loader=new URLClassLoader(path,cloader);
            desClass=loader.loadClass(classname);
        } catch (MalformedURLException | ClassNotFoundException ex) {
            Log(ex);
        }
        return desClass;
    }
    
    static List<String> getPacketClass(String pahtname,List<String> classname){
        String path=Bukkit.class.getResource(pahtname).getFile().replaceAll("%20", " ");
        String[] jarInfo = path.split("!");  
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));  
        String packagePath = jarInfo[1].substring(1);  
        List<String> l=new ArrayList<>();
        File f=new File(jarFilePath+"!/"+packagePath);
        try {  
            JarFile jarFile = new JarFile(jarFilePath);  
            Enumeration<JarEntry> entrys = jarFile.entries();  
            while (entrys.hasMoreElements()) {  
                JarEntry jarEntry = entrys.nextElement();  
                String entryName = jarEntry.getName();  
                if (entryName.contains(packagePath)&&entryName.endsWith(".class")) { 
                    entryName = entryName.substring(0, entryName.lastIndexOf("."));
                    if(entryName.endsWith("Event"))
                    {
                        l.add(entryName);
                    }  
                }
            }  
        } catch (IOException e) {  
            Log(e);
        }  
        return l;  
    }
    public static List<String> getPacketAllClass(String path){
        return getPacketClass(path, null);
    }
    
    public static List<String> getPacketInClass(){
        String ver=Bukkit.getServer().getClass().getPackage().getName();
        ver=ver.substring(ver.lastIndexOf('.')+1);
        String path=Bukkit.class.getResource("").getFile().replaceAll("%20", " ");
        path=path.substring(0, path.lastIndexOf("org"));
        path+="net/minecraft/server/"+ver;
        String[] jarInfo = path.split("!");  
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);  
        List<String> l=new ArrayList<>();
        File f=new File(jarFilePath+"!/"+packagePath);
        try {  
            JarFile jarFile = new JarFile(jarFilePath);  
            Enumeration<JarEntry> entrys = jarFile.entries();  
            while (entrys.hasMoreElements()) {  
                JarEntry jarEntry = entrys.nextElement();  
                String entryName = jarEntry.getName();  
                if (entryName.contains(packagePath)&&entryName.endsWith(".class")) { 
                    entryName = entryName.substring(0, entryName.lastIndexOf("."));
                    if(entryName.contains("Packet")&&entryName.contains("PlayIn")&&!entryName.contains("$"))
                    {
                        l.add(entryName);
                    }  
                }
            }  
        } catch (IOException e) {  
            Log(e);
        }  
        return l;  
    }
    
    static void AddAllPacketEvent(File f){
        类解析器 builder=new 类解析器();
        builder.解析文件(f);
        List<String> utf8list=builder.获取所有UTF8常量();
        String ver=Bukkit.getServer().getClass().getPackage().getName();
        ver=ver.substring(ver.lastIndexOf('.')+1);
        String path=null;
        for(String value:utf8list){
            String newpath;
            if(value.contains("net/minecraft/server/"))
            {
                if(value.contains("Listener")&&!value.contains(";"))
                {
                        builder.替换指定UTF8常量(value,PacketInListenerName);
                        Log(value);
                        Log(PacketInListenerName);
                        continue;
                }
                if(path!=null){
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                }
                else{
                    path=value.substring(value.indexOf("net/minecraft/server/")+21, value.lastIndexOf('/'));
                    Log("net:"+path);
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                    Log(value);
                    Log(newpath);
                }
            }else if(value.contains("org/bukkit/craftbukkit/")){
                if(path!=null){
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                }
                else{
                    path=value.substring(value.indexOf("org/bukkit/craftbukkit/")+23, value.lastIndexOf('/'));
                    Log("org:"+path);
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                    Log(value);
                    Log(newpath);
                }
            }
        }
        builder.保存文件(f);
    }
    
    static void AddAllPacketEvent(InputStream f,OutputStream o){
        类解析器 builder=new 类解析器();
        builder.解析(f);
        List<String> utf8list=builder.获取所有UTF8常量();
        String ver=Bukkit.getServer().getClass().getPackage().getName();
        ver=ver.substring(ver.lastIndexOf('.')+1);
        String path=null;
        for(String value:utf8list){
            String newpath;
            if(value.contains("net/minecraft/server/"))
            {
                if(value.contains("Listener")&&!value.contains(";"))
                {
                        builder.替换指定UTF8常量(value,PacketInListenerName);
                        Log(value);
                        Log(PacketInListenerName);
                        continue;
                }
                if(path!=null){
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                }
                else{
                    path=value.substring(value.indexOf("net/minecraft/server/")+21, value.lastIndexOf('/'));
                    Log("net:"+path);
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                    Log(value);
                    Log(newpath);
                }
            }else if(value.contains("org/bukkit/craftbukkit/")){
                if(path!=null){
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                }
                else{
                    path=value.substring(value.indexOf("org/bukkit/craftbukkit/")+23, value.lastIndexOf('/'));
                    Log("org:"+path);
                    newpath=value.replace(path, ver);
                    builder.替换指定UTF8常量(value, newpath);
                    Log(value);
                    Log(newpath);
                }
            }
        }
        builder.保存(o);
    }
    
    static void AddAllEvent(File f,List<String> str){
        Log("开始查找事件");
        List<String> AllEvents=getPacketAllClass("event");
        类解析器 builder=new 类解析器();
        builder.解析文件(f);
        类解析器.方法 method=builder.获取方法("Z");
        类解析器.代码属性 code;
        List<Object> attList;
        类解析器.局部变量属性 tempvar;
        int count=0;
        Log("开始加载事件");
        for(String name:AllEvents){
            String value=name.substring(name.lastIndexOf('/')+1);
            if(str.indexOf(value)!=-1){
                Log("加载"+value);
                code=(类解析器.代码属性) method.寻找属性(类解析器.静态属性列表.Code);
                attList=code.寻找属性(类解析器.静态属性列表.LocalVariableTable);
                tempvar=(类解析器.局部变量属性) attList.get(attList.size()-1);
                method.设置方法名称("A"+count);
                method.设置方法返回值与参数(null,name);
                code.字节码[2]=(byte)(count/256);
                code.字节码[3]=(byte)(count%256);
                tempvar.设置局部变量属性("e", name);
                attList.set(attList.size()-1, tempvar);
                code.设置属性(类解析器.静态属性列表.LocalVariableTable, attList);
                method.设置属性(类解析器.静态属性列表.Code, code);
                if(count!=0)
                    builder.添加方法(method);
                count++;
                method=method.克隆();
            }
        }
        builder.保存文件(f);
    }
    
    static void AddAllEvent(InputStream f,OutputStream fo,List<String> str){
        Log("开始查找事件");
        List<String> AllEvents=getPacketAllClass("event");
        类解析器 builder=new 类解析器();
        builder.解析(f);
        类解析器.方法 method=builder.获取方法("Z");
        类解析器.代码属性 code;
        List<Object> attList;
        类解析器.局部变量属性 tempvar;
        int count=0;
        Log("开始加载事件");
        for(String name:AllEvents){
            String value=name.substring(name.lastIndexOf('/')+1);
            if(str.indexOf(value)!=-1){
                Log("加载"+value);
                code=(类解析器.代码属性) method.寻找属性(类解析器.静态属性列表.Code);
                attList=code.寻找属性(类解析器.静态属性列表.LocalVariableTable);
                tempvar=(类解析器.局部变量属性) attList.get(attList.size()-1);
                method.设置方法名称("A"+count);
                method.设置方法返回值与参数(null,name);
                code.字节码[2]=(byte)(count/256);
                code.字节码[3]=(byte)(count%256);
                tempvar.设置局部变量属性("e", name);
                attList.set(attList.size()-1, tempvar);
                code.设置属性(类解析器.静态属性列表.LocalVariableTable, attList);
                method.设置属性(类解析器.静态属性列表.Code, code);
                if(count!=0)
                    builder.添加方法(method);
                count++;
                method=method.克隆();
            }
        }
        builder.保存(fo);
    }
    
    public static void SaveFile(Plugin plugin,String FileName,String SavePath){
         try {
                InputStream ip=plugin.getResource(FileName);
                FileOutputStream op=new FileOutputStream(new File(plugin.getDataFolder(),SavePath));
                byte[] buff=new byte[1024];
                int len;
                while((len=ip.read(buff))!=-1){
                    op.write(buff, 0, len);
                }
                ip.close();
                op.close();
        } catch (IOException ex) {
            Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void SaveFile(String FileName,File path){
         try {
             FileOutputStream op;
             try (InputStream ip = BukkitEventsBuild.class.getResourceAsStream(FileName)) {
                 op = new FileOutputStream(path);
                 byte[] buff=new byte[1024];
                 int len;
                 while((len=ip.read(buff))!=-1){
                     op.write(buff, 0, len);
                 }
             }
                op.close();
        } catch (IOException ex) {
            Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void BuildPacketInFile(Plugin plugin){
        File f=new File(plugin.getDataFolder(),"Ext\\");
        if(!f.exists()){
            f.mkdirs();
        }
        f=new File(f,"PacketHook.class");
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
            }
            SaveFile(plugin, "PacketData.dat", "Ext\\PacketHook.class");
        }
        AddAllPacketEvent(f);
    }
    
    public static void BuildEventsFile(Plugin plugin,File f){
        String path=f.getPath();
        File dir=new File(path.substring(0, path.lastIndexOf('\\')));
        if(!dir.exists())
            dir.mkdirs();
        if(!f.exists())
        {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
            }
            SaveFile(plugin,"ClassData.dat","Ext\\Events.class");
        }
        File configfile=new File(plugin.getDataFolder(),"Ext\\config.yml");
        if(!configfile.exists())
            SaveFile(plugin, "config.yml", "Ext\\config.yml");
        YamlConfiguration config=YamlConfiguration.loadConfiguration(configfile);
        UseEventsList=config.getStringList("UseEventsList");
        Log("事件列表长度"+UseEventsList.size());
        AddAllEvent(f, UseEventsList);
    }
    
    public static void LoadPacketEvents(Plugin plugin){
        PacketInList=getPacketInClass();
        for(String v:PacketInList)
            if(v.contains("Listener"))
            {
                isPacketLoad=true;
                PacketInListenerName=v;
                Log(v);
                break;
            }
        if(!isPacketLoad){
            Log("未能注册包");
            return;
        }
        InputStream ip=plugin.getResource("PacketData.dat");
        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        AddAllPacketEvent(ip, bo);
        try {
            bo.close();
            ip.close();
            PacketHookClass=类解析器.动态加载类(plugin.getClass().getClassLoader(), bo.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
        }
        Log(PacketHookClass);
    }
    
    
    static int[] ClassData=new int[]{
0xCA,0xFE,0xBA,0xBE,0x00,0x00,0x00,0x34,0x00,0x1A,0x0A,0x00,0x04,0x00,0x14,0x0A,
0x00,0x03,0x00,0x15,0x07,0x00,0x16,0x07,0x00,0x17,0x01,0x00,0x06,0x3C,0x69,0x6E,
0x69,0x74,0x3E,0x01,0x00,0x03,0x28,0x29,0x56,0x01,0x00,0x04,0x43,0x6F,0x64,0x65,
0x01,0x00,0x0F,0x4C,0x69,0x6E,0x65,0x4E,0x75,0x6D,0x62,0x65,0x72,0x54,0x61,0x62,
0x6C,0x65,0x01,0x00,0x12,0x4C,0x6F,0x63,0x61,0x6C,0x56,0x61,0x72,0x69,0x61,0x62,
0x6C,0x65,0x54,0x61,0x62,0x6C,0x65,0x01,0x00,0x04,0x74,0x68,0x69,0x73,0x01,0x00,
0x08,0x4C,0x45,0x76,0x65,0x6E,0x74,0x73,0x3B,0x01,0x00,0x01,0x5A,0x01,0x00,0x28,
0x28,0x4C,0x6F,0x72,0x67,0x2F,0x62,0x75,0x6B,0x6B,0x69,0x74,0x2F,0x65,0x76,0x65,
0x6E,0x74,0x2F,0x65,0x6E,0x74,0x69,0x74,0x79,0x2F,0x50,0x69,0x67,0x5A,0x61,0x70,
0x45,0x76,0x65,0x6E,0x74,0x3B,0x29,0x56,0x01,0x00,0x01,0x65,0x01,0x00,0x25,0x4C,
0x6F,0x72,0x67,0x2F,0x62,0x75,0x6B,0x6B,0x69,0x74,0x2F,0x65,0x76,0x65,0x6E,0x74,
0x2F,0x65,0x6E,0x74,0x69,0x74,0x79,0x2F,0x50,0x69,0x67,0x5A,0x61,0x70,0x45,0x76,
0x65,0x6E,0x74,0x3B,0x01,0x00,0x19,0x52,0x75,0x6E,0x74,0x69,0x6D,0x65,0x56,0x69,
0x73,0x69,0x62,0x6C,0x65,0x41,0x6E,0x6E,0x6F,0x74,0x61,0x74,0x69,0x6F,0x6E,0x73,
0x01,0x00,0x1F,0x4C,0x6F,0x72,0x67,0x2F,0x62,0x75,0x6B,0x6B,0x69,0x74,0x2F,0x65,
0x76,0x65,0x6E,0x74,0x2F,0x45,0x76,0x65,0x6E,0x74,0x48,0x61,0x6E,0x64,0x6C,0x65,
0x72,0x3B,0x01,0x00,0x0A,0x53,0x6F,0x75,0x72,0x63,0x65,0x46,0x69,0x6C,0x65,0x01,
0x00,0x0B,0x45,0x76,0x65,0x6E,0x74,0x73,0x2E,0x6A,0x61,0x76,0x61,0x0C,0x00,0x05,
0x00,0x06,0x0C,0x00,0x18,0x00,0x19,0x01,0x00,0x06,0x45,0x76,0x65,0x6E,0x74,0x73,
0x01,0x00,0x2C,0x63,0x6F,0x6D,0x2F,0x67,0x69,0x74,0x68,0x75,0x62,0x2F,0x4B,0x65,
0x79,0x4D,0x6F,0x76,0x65,0x2F,0x45,0x76,0x65,0x6E,0x74,0x73,0x4D,0x61,0x6E,0x61,
0x67,0x65,0x72,0x2F,0x45,0x76,0x65,0x6E,0x74,0x4D,0x61,0x6E,0x67,0x65,0x72,0x01,
0x00,0x09,0x43,0x61,0x6C,0x6C,0x45,0x76,0x65,0x6E,0x74,0x01,0x00,0x16,0x28,0x49,
0x4C,0x6A,0x61,0x76,0x61,0x2F,0x6C,0x61,0x6E,0x67,0x2F,0x4F,0x62,0x6A,0x65,0x63,
0x74,0x3B,0x29,0x56,0x00,0x21,0x00,0x03,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x02,
0x00,0x01,0x00,0x05,0x00,0x06,0x00,0x01,0x00,0x07,0x00,0x00,0x00,0x2F,0x00,0x01,
0x00,0x01,0x00,0x00,0x00,0x05,0x2A,0xB7,0x00,0x01,0xB1,0x00,0x00,0x00,0x02,0x00,
0x08,0x00,0x00,0x00,0x06,0x00,0x01,0x00,0x00,0x00,0x10,0x00,0x09,0x00,0x00,0x00,
0x0C,0x00,0x01,0x00,0x00,0x00,0x05,0x00,0x0A,0x00,0x0B,0x00,0x00,0x00,0x00,0x00,
0x0C,0x00,0x0D,0x00,0x02,0x00,0x07,0x00,0x00,0x00,0x41,0x00,0x03,0x00,0x02,0x00,
0x00,0x00,0x09,0x2A,0x11,0x04,0x00,0x2B,0xB6,0x00,0x02,0xB1,0x00,0x00,0x00,0x02,
0x00,0x08,0x00,0x00,0x00,0x0A,0x00,0x02,0x00,0x00,0x00,0x13,0x00,0x08,0x00,0x14,
0x00,0x09,0x00,0x00,0x00,0x16,0x00,0x02,0x00,0x00,0x00,0x09,0x00,0x0A,0x00,0x0B,
0x00,0x00,0x00,0x00,0x00,0x09,0x00,0x0E,0x00,0x0F,0x00,0x01,0x00,0x10,0x00,0x00,
0x00,0x06,0x00,0x01,0x00,0x11,0x00,0x00,0x00,0x01,0x00,0x12,0x00,0x00,0x00,0x02,
0x00,0x13,};
    
    static ByteArrayInputStream eventclass(){
        byte[] data=new byte[ClassData.length];
        for(int i=0;i<ClassData.length;i++)
            data[i]=(byte)ClassData[i];
        return new ByteArrayInputStream(data);
    }
    
    public static EventManger LoadEvents(Plugin plugin){
        File configfile=new File(plugin.getDataFolder(),"event.yml");
        if(!configfile.exists())
            SaveFile(plugin, "event.yml", "event.yml");
        YamlConfiguration config=YamlConfiguration.loadConfiguration(configfile);
        UseEventsList=config.getStringList("UseEventsList");
        log=config.getBoolean("Log",false);
        if(config.getBoolean("PacketEvent",false)){
            LoadPacketEvents(plugin);
        }
        InputStream ip=eventclass();
        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        Log("事件列表长度"+UseEventsList.size());
        AddAllEvent(ip,bo, UseEventsList);
        try {
            bo.close();
            ip.close();
            return (EventManger)类解析器.动态加载类(plugin.getClass().getClassLoader(), bo.toByteArray()).newInstance();
        } catch (InstantiationException | IllegalAccessException | IOException ex) {
            Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /*
    public static PacketHookBase newPacketHook(){
        if(PacketHookClass!=null){
            try {
                return (PacketHookBase) PacketHookClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(BukkitEventsBuild.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }*/
    
    public static void GetPacketIn(Plugin plugin){
        File f=new File(plugin.getDataFolder(),"Ext\\PacketHook.class");
        PacketInList=getPacketInClass();
        for(String v:PacketInList)
            if(v.contains("Listener"))
            {
                isPacketLoad=true;
                PacketInListenerName=v;
                Log(v);
                break;
            }
        if(!isPacketLoad){
            Log("未能注册包");
            return;
        }
        if(!f.exists()){
            BuildPacketInFile(plugin);
        }
        f=new File(plugin.getDataFolder(),"\\Ext\\PacketHook.class");
        PacketHookClass=LoadClassFile(f.getPath(),plugin.getClass().getClassLoader());
        Log(PacketHookClass);
    }
    
    /*
    public static EventManger GetEvents(Plugin plugin){
        EventManger EventsObject=null;
        File classfile=new File(plugin.getDataFolder(),"\\Ext\\Events.class");
        if(!classfile.exists()){
            BuildEventsFile(plugin, classfile);
            GetPacketIn(plugin);
            //plugin.getClass().getClassLoader()
        }
            Class EventClass=LoadClassFile(classfile.getPath(),plugin.getClass().getClassLoader());        
            if(EventClass!=null){
                try {
                    EventsObject=(EventManger)EventClass.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    Log(ex);
                }
            }
        return EventsObject;
    }
    public static void Log(Object obj){
        if(log)
            out.print("[InGameLuaEdit] "+obj);
    }
    */
    
}
