/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.out;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import redis.clients.jedis.*;

/**
 *
 * @author Administrator
 */
public class BungeeServer extends Plugin{

    static Jedis 数据库=new Jedis();
    static boolean isLink=false;
    static listerdata handle;
    Runnable 监听传送玩家=() -> {
        do{
            try{
                数据库.subscribe(handle, "tpplayer");
            }catch (Exception e){
            }
        }while(数据库.isConnected());
    };
    ScheduledTask 监控线程;
    Plugin 插件;
    String Host="localhost";
    int Port=6379;
    Map<String,ServerInfo> ServerList=new HashMap<>();
    public static boolean Link(String addr,int port){
        Jedis js=new Jedis(addr,port,500);
        try{
            List<String> servers=js.lrange("server",0,-1);
        }catch(Exception e){
            return false;
        }
        js.close();
        if(数据库!=null)数据库.close();
        数据库=new Jedis(addr,port);
        out.print("Init DataBase Done!");
        isLink=true;
        return true;
    }
    
    
    
    class listerdata extends JedisPubSub{
        @Override
        public void onMessage(String channel, String message) {
            //传送玩家 tp=new 传送玩家(message);
            //tp.task=ProxyServer.getInstance().getScheduler().schedule(插件, tp, 1, TimeUnit.DAYS);
            String[] data=message.split(",");
            String playername=data[0];
            String servername=data[1];
            ProxiedPlayer player=ProxyServer.getInstance().getPlayer(playername);
            if(player==null){
                out.print("Not Find Player ["+playername+"]!");
                return;
            }
            if(data.length==3){
                int port=Integer.parseInt(data[2]);
                player.connect(getProxy().constructServerInfo("temp link", new InetSocketAddress(servername, port), "临时服务器", true));
                return;
            }
            //Map servers = ProxyServer.getInstance().getServers();
            //if("233".equals(servername)){
            //    player.connect(getProxy().constructServerInfo("test", new InetSocketAddress("localhost", 58002), "null", true));
            //    return;
            //}
            //ServerInfo info=(ServerInfo) servers.get(servername);
            ServerInfo info=null;
            if(ServerList.containsKey(servername)){
                info=ServerList.get(servername);
            }
            else{
                Map servers = ProxyServer.getInstance().getServers();
                info=(ServerInfo) servers.get(servername);
                if(info!=null){
                    ServerList.put(servername, info);
                }
                else{
                    out.print("ReadServer!");
                    Jedis js=new Jedis(Host, Port);
                    List<String> list=js.lrange("server", 0, -1);
                    int index=list.indexOf(servername);
                    if(index!=-1){
                        if(js.clientList().contains("name="+servername)){
                            String v=js.get("serverinfo-"+servername);
                            if(v!=null){
                                String[] vr=v.split(",");
                                try{
                                    int port=Integer.parseInt(vr[1]);
                                    out.print("Find Server"+vr[0]+":"+port);
                                    info=getProxy().constructServerInfo(servername, new InetSocketAddress(vr[0], port), servername+"服务器", true);
                                    ServerList.put(servername, info);
                                }catch(Exception e){
                                }
                            }
                        }
                    }
                    js.close();
                    out.print("Deon!");
                }
            }
            if(info==null){
                out.print("Not Find Server ["+servername+"]!");
                player.sendMessage("§c无法传送到目标服务器!");
                return;
            }
            out.print("Player ["+player.getName()+"] Server ["+servername+"]!");
            player.connect(info);
        }
    }
    
    class 数据库重新连接 extends net.md_5.bungee.api.plugin.Command{

        public 数据库重新连接() {
            super("rp");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if(!cs.hasPermission("admin"))return;
            if(strings.length==0){
                cs.sendMessage("[RedisPipe] Host:"+Host+":"+Port+"State:"+数据库.isConnected());
                cs.sendMessage("[RedisPipe] /rp [ip] <port=6379> link database server!");
                cs.sendMessage("[RedisPipe] /rp save Save ip&port Data!");
                return;
            }
            if(strings.length!=0){
                if("save".equals(strings[0])){
                    saveconfig();
                    cs.sendMessage("Done!");
                    return;
                }
            }
            int port=6379;
            if(strings.length>=2)
            {
                try{
                Integer.parseInt(strings[1]);
                }catch(Exception e){
                    port=6379;
                }
            }
            if(Link(strings[0], port)){
                Host=strings[0];
                Port=port;
                cs.sendMessage("Link DataBase Done!");
                if(监控线程!=null)
                    监控线程.cancel();
                监控线程=ProxyServer.getInstance().getScheduler().runAsync(插件, 监听传送玩家);
            }
        }
        
    }
    
    void config(){
        File f=new File(getDataFolder(),"config.dat");
        if(!f.exists()){
            saveconfig();
            return;
        }
        Object temp=null;
        FileInputStream in;
        try {
            in = new FileInputStream(f);
            ObjectInputStream objIn=new ObjectInputStream(in);
            temp=objIn.readObject();
            objIn.close();
            Host=((BungeeSaveData)temp).Host;
            Port=((BungeeSaveData)temp).Port;
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }
    
    void saveconfig(){
        File f=new File(getDataFolder(),"config.dat");
        if(!f.exists())
            {
                String path=f.getPath();
                File dir=new File(path.substring(0, path.lastIndexOf("config.dat")-1));
                out.print(dir);
                if(!dir.exists())
                    dir.mkdirs();
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BungeeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            try {
                FileOutputStream s=new FileOutputStream(f);
                ObjectOutputStream objOut=new ObjectOutputStream(s);
                objOut.writeObject(new BungeeSaveData(Host, Port));
                objOut.flush();
                objOut.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BungeeServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BungeeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    @Override
    public void onEnable() {
        插件=this;
        handle=new listerdata();
        config();
        out.print("Link Host:"+Host+":"+Port);
        if(Link(Host,Port)){
            out.print("Link DataBase Done!");
            if(监控线程!=null)
                监控线程.cancel();
            监控线程=ProxyServer.getInstance().getScheduler().runAsync(插件, 监听传送玩家);
        }
        getProxy().getPluginManager().registerCommand(this, new 数据库重新连接());
    }
    
    
}
