/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;

import com.github.KeyMove.Tools.IP138;
import com.github.KeyMove.Tools.NBTCoder;
import java.io.File;
import java.io.IOException;
import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.*;
/**
 *
 * @author Administrator
 */
public class RedisPipe extends JavaPlugin implements Listener{
    static RedisPool pool;
    static Jedis 数据库=new Jedis();
    static RedisPipeAPI 数据库API=null;
    
    static String Host;
    static int Port;
    static String localaddr=null;
    static int localport=25565;
    
    static boolean CoverFlag=false;
    YamlConfiguration 配置文件;
    int linkcount=0;
    
    
    
    public static RedisPipeAPI getInstance(){
        return 数据库API;
    }
    
    public static void Updatelocaladdr(){
        String v=数据库.clientList();
        String[] list=v.split("\n");
        for(String s:list){
            //log(s);
            if(s.contains("name="+RedisPipeAPI.ServerName)){
                int spos=s.indexOf("addr=")+5;
                int epos=s.indexOf(" fd=");
                String[] a=s.substring(spos,epos).split(":");
                if(!a[0].contains("127.0.0.1"))
                    localaddr=a[0];
                if(localaddr==null)
                    localaddr=a[0];
                //log("本机地址: ["+localaddr+"]");
                return;
            }
        }
    }
    
    public static void Close(){
        if(数据库.isConnected()){
            try{
            数据库.lrem("server", 0, RedisPipeAPI.ServerName);
            数据库.clientSetname("");
            数据库.close();
            //数据库.connect();
            //数据库.disconnect();
            }catch(Exception e){
                
            }
        }
    }
    
    public static boolean Link(String addr,int port,boolean flow){        
        if(!RedisPool.TryConnect(addr,port,500)){
            return false;
        }
        if(!CoverFlag){
            Jedis js=new Jedis(addr,port);
            try{
                if(js.lrange("server", 0, -1).contains(RedisPipeAPI.ServerName)){
                    if(!flow)
                    {
                        log("出现相同名字的服务器");
                        log("使用/rp server 修改");
                        log("或使用/rp link [server] [port] true 覆盖");
                        js.close();
                        return false;
                    }
                    else{
                        //js.clientKill(RedisPipeAPI.ServerName);
                    }
                }
            }catch(Exception e){
                log("连接超时!");
                js.close();
                return false;
            }
            js.close();
        }
        Close();
        pool=new RedisPool(addr, port);
        数据库=pool.get();
        数据库.clientSetname(RedisPipeAPI.ServerName);
        if(localaddr==null)Updatelocaladdr();
        数据库.lpush("server", RedisPipeAPI.ServerName);
        if(!localaddr.contains("127.0.0.1"))
        {
            数据库.set("serverinfo-"+RedisPipeAPI.ServerName, localaddr+","+localport);
            log("本机地址: ["+localaddr+"]");
        }
        else{
            new Thread(()->{try {
                                localaddr=IP138.getMyIP();
                                数据库.set("serverinfo-"+RedisPipeAPI.ServerName, localaddr+","+localport);
                                log("本机地址: ["+localaddr+"]");
                                } catch (IOException ex) {
                                    Logger.getLogger(RedisPipe.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }).start();
        }
        log("数据库初始化成功!");
        if(数据库API==null)
            数据库API=new RedisPipeAPI(pool);
        else
            数据库API.NewPool(pool);
        if(RedisPipeAPI.聊天同步){
            数据库API.StartChat();
            log("注册聊天通道!");
        }
        if(RedisPipeAPI.背包同步){
            数据库API.StartBackpack();
            log("注册背包同步通道!");
        }
        return true;
    }
    
    public boolean Init(){
        File 文件=new File(getDataFolder(),"config.yml");
        if(!文件.exists()){
            saveDefaultConfig();
        }
        配置文件=YamlConfiguration.loadConfiguration(文件);
        Host=配置文件.getString("Host");
        Port=配置文件.getInt("Port");
        RedisPipeAPI.ServerName=配置文件.getString("ServerName");
        RedisPipeAPI.聊天同步=配置文件.getBoolean("ServerChat");
        RedisPipeAPI.MessageFormat=配置文件.getString("ServerChatFormat");
        RedisPipeAPI.背包同步=配置文件.getBoolean("ServerBackpack");
        CoverFlag=配置文件.getBoolean("CoverServer");
        return Link(Host,Port,false);
    }
    
    void UpdateServerInfo(String name){
        if(数据库.isConnected()){
            数据库.lrem("server", 0, RedisPipeAPI.ServerName);
            RedisPipeAPI.ServerName=name;
            数据库.lpush("server", RedisPipeAPI.ServerName);
            数据库.set("serverinfo-"+RedisPipeAPI.ServerName, localaddr+","+localport);
            数据库.clientSetname(RedisPipeAPI.ServerName);
        }
        else{
            RedisPipeAPI.ServerName=name;
        }
    }
    
    void flowServer(String addr,int port){
        Jedis js=new Jedis(addr,port,500);
        try{
            if(js.clientList().contains("name="+RedisPipeAPI.ServerName)){
                js.lrem("server", 0, RedisPipeAPI.ServerName);
                js.clientKill(RedisPipeAPI.ServerName);
            }
        }catch(Exception e){
        }
        js.close();
    }
    
    @EventHandler void ChatEvent(AsyncPlayerChatEvent e){
        if(RedisPipeAPI.聊天同步)
            if(数据库API!=null)
                数据库API.Chat(e.getPlayer(), e.getMessage());    
    }
    @EventHandler void LoginEvent(PlayerLoginEvent e){
        if(RedisPipeAPI.背包同步)
            if(数据库API!=null)
                数据库API.PlayerBackpack(e.getPlayer());
    }
    @EventHandler void QuitEvent(PlayerQuitEvent e){
        if(RedisPipeAPI.背包同步)
            if(数据库API!=null)
                数据库API.SavePlayer(e.getPlayer());
    }
    
    @Override
    public void onEnable() {
        Init();
        getServer().getPluginManager().registerEvents(this, this);
        localport=getServer().getPort();
        if(NBTCoder.Init()==-1){
            log("初始化物品序列化成功");
        }
    }

    @Override
    public void onDisable() {
        Close();
        log("关闭连接");
    }
    
    static void log(String message){
        out.print("[RedisPipe] "+message);
    }
    
    void help(CommandSender sender){
        if(!数据库.isConnected())
        {
            sender.sendMessage("[RedisPipe] 目前状态未连接");
        }
        sender.sendMessage("[RedisPipe] 目标服务器"+"Host:"+Host+":"+Port+"服务器名称:"+RedisPipeAPI.ServerName);
        sender.sendMessage("[RedisPipe] 输入/rp reload重新加载");
        sender.sendMessage("[RedisPipe] 输入/rp link <ip> <port>连接数据库");
        sender.sendMessage("[RedisPipe] 输入/rp server [Name] 设置服务器名称");
        sender.sendMessage("[RedisPipe] 输入/rp save [Name] 保存配置");
    }
    
    void tphelp(CommandSender sender){
        sender.sendMessage("[RedisPipe] 输入/rptp [玩家] [服务器]");
        sender.sendMessage("[RedisPipe] 或者输入输入/rptp [玩家] [服务器IP] [服务器端口]");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("op"))
        {
            sender.sendMessage("没有足够的权限操作");
            return false;
        }
        //log(label);
        if("rptp".equals(label)){
            if(!数据库.isConnected()){
                sender.sendMessage("[RedisPipe] 数据库未连接!");
                return false;
            }
            if(args.length<2){
                tphelp(sender);
                return true;
            }
            Player p=getServer().getPlayer(args[0]);
            String servername=args[1];
            if(p==null){
                sender.sendMessage("[RedisPipe] 未找到该玩家");
                return true;
            }
            if(args.length>=3){
                try {
                    int port=Integer.parseInt(args[2]);
                    数据库API.sendPlayer(p, servername, port);
                } catch (NumberFormatException e) {
                    sender.sendMessage("[RedisPipe] 端口输入错误");
                }
                return true;
            }
            else
            {
                数据库API.sendPlayer(p, servername);
                return true;
            }
        }
        if(args.length<1)
        {
            help(sender);
            return true;
        }
            switch(args[0]){
                case "reload": Init(); break;
                case "ip":
                try {
                    String addr=IP138.getMyIP();
                    log(addr);
                    localaddr=addr;
                } catch (IOException ex) {
                    Logger.getLogger(RedisPipe.class.getName()).log(Level.SEVERE, null, ex);
                }
                    break;
                case "server":
                    if(args.length<2)
                    {
                        help(sender);
                        return false;
                    }
                    UpdateServerInfo(args[1]);
                    sender.sendMessage("服务器名称修改为 ["+args[1]+"] !");
                    break;
                case "link": 
                    int port=6379;
                    boolean flow=false;
                    if(args.length>=4)
                    {
                        flow=true;
                        CoverFlag=flow;
                    }
                    if(args.length>=3)
                    {
                        try{
                            port=Integer.parseInt(args[2]);
                        }catch(NumberFormatException e){
                            port=6379;
                        }
                    }
                    if(args.length>=2)
                    {
                        if(Link(args[1],port,flow)){
                            Updatelocaladdr();
                            sender.sendMessage("连接成功!");
                            Host=args[1];
                            Port=port;
                            linkcount=0;
                        }
                        else{
                            linkcount++;
                            sender.sendMessage("连接失败!");
                            if(linkcount>=3){
                                Link(Host,Port,true);
                            }
                        }
                    }
                    else
                    {
                        if(Link(Host,Port,false)){
                            linkcount=0;
                            Updatelocaladdr();
                            sender.sendMessage("连接成功!");
                        }
                        else{
                            linkcount++;
                            sender.sendMessage("连接失败!");
                            if(linkcount>=3){
                                Link(Host,Port,true);
                            }
                        }
                    }
                break;
                case "save":
                    配置文件.set("Host", Host);
                    配置文件.set("Port", Port);
                    配置文件.set("ServerName", RedisPipeAPI.ServerName);
        try {
            配置文件.save(new File(getDataFolder(),"config.yml"));
            sender.sendMessage("保存完成");
        } catch (IOException ex) {
            sender.sendMessage("保存失败");
        }
                    break;
                default:
                    help(sender);
                break;
            }
        return true;
    }
    
    public static void main(String[] args) {
        
    }
    
}
