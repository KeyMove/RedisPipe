/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;

import com.github.KeyMove.RedisPipeAPI.ChannelMessage;
import static com.github.KeyMove.RedisPipeAPI.MessageFormat;
import static com.github.KeyMove.RedisPipeAPI.ServerName;
import static com.github.KeyMove.Tools.BukkitEventsBuild.LoadEvents;
import com.github.KeyMove.EventsManager.EventManger;
import com.github.KeyMove.Tools.IP138;
import com.github.KeyMove.Tools.JSBukkit;
import com.github.KeyMove.Tools.JSTools;
import com.github.KeyMove.Tools.NBTCoder;
import com.github.KeyMove.Tools.PlayerInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.*;
/**
 *
 * @author Administrator
 */
public class RedisPipe extends JavaPlugin implements Listener{
    public static RedisPool pool;
    public static Jedis 数据库=new Jedis();
    public static RedisPipeAPI 数据库API=null;
    
    public static String Host;
    public static int Port;
    public static String localaddr=null;
    public static int localport=25565;
    public static boolean EnableJs=true;
    
    static boolean CoverFlag=false;
    static boolean RealIP=false;
    YamlConfiguration 配置文件;
    int linkcount=0;
    
    List<String> noSave=new ArrayList();
    
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
            数据库.quit();
            //数据库.clientSetname("");
            //数据库.close();
            //数据库.connect();
            //数据库.disconnect();
            }catch(Exception e){
                
            }
        }
        if(数据库API!=null){
            if(RedisPipeAPI.聊天同步){
                数据库API.StopChat();
                log("关闭背包同步通道!");
            }
            if(RedisPipeAPI.背包同步){
                数据库API.StopBackpack();
                log("关闭背包同步通道!");
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
        try {
            Close();
        pool=new RedisPool(addr, port);
        数据库=pool.get();
        数据库.clientSetname(RedisPipeAPI.ServerName);
        if(localaddr==null)Updatelocaladdr();
            if(!数据库.lrange("server", 0, -1).contains(RedisPipeAPI.ServerName))
                数据库.lpush("server", RedisPipeAPI.ServerName);
            if(!localaddr.contains("127.0.0.1")||!RealIP)
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
            数据库API=new RedisPipeAPI(pool,new ChannelMessage() {
            @Override
            public void OnMessage(String message) {
                //System.out.print("[RedisPipe] recvMessage "+message);
                String[] v=message.split("&");
                //System.out.print("[RedisPipe] recvMessage 1");
                if(v.length!=3)return;
                //System.out.print("[RedisPipe] recvMessage 2");
                if(v[0].equalsIgnoreCase(ServerName))return;
                String bc=MessageFormat.replace("%server%", v[0]).replace("%player%", v[1]).replace("%message%", v[2]);
                //System.out.print("[RedisPipe] recvMessage "+bc);
                Bukkit.getServer().broadcastMessage(bc);
            }
        },
            new ChannelMessage() {
            @Override
            public void OnMessage(String message) {
                String[] v=message.split(",");
                if(v.length!=2)return;
                //System.out.print(v[0]);
                //System.out.print(v[1]);
                if(!v[1].equalsIgnoreCase(ServerName))return;
                //Check();
                Jedis js=pool.get();
                if(js==null)return;
                byte[] pdata=js.get(("PlayerData-"+v[0]).getBytes());
                System.out.print("[RedisPipe] PlayerData-"+v[0]);
                if(pdata==null){pool.release(js);return;}
                Player p=Bukkit.getPlayer(v[0]);
                if(p==null)
                {   
                    boolean b=false;
                    for(OfflinePlayer op:Bukkit.getOfflinePlayers()){
                        if(op.getUniqueId().toString().equalsIgnoreCase(v[0])){
                            System.out.print("[RedisPipe] find ["+op.getName()+"] UUID: "+op.getUniqueId().toString());
                            PlayerInfo.saveData( op.getUniqueId(), pdata);
                            b=true;
                            break;
                        }
                    }
                    if(!b){
                        System.out.print("[RedisPipe] new Player ["+v[0]+"]");
                        PlayerInfo.saveData( UUID.fromString(v[0]), pdata);
                    }
                    //PlayerCache.put(v[0], pdata);
                    js.publish("tpplayer", message).intValue();
                    System.out.print("[RedisPipe] send tp");
                    
                }
                else{
                    PlayerInfo.load(pdata,p.getUniqueId());
                }
                pool.release(js);
            }
        });
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
        } catch (Exception e) {
        }
        
        return false;
    }
    private Location safeLocation;
    
    void loadPosData(String posdata){
        if(posdata==null){
            newLoc();
            return;
        }
        String[] dt=posdata.split(",");
        if(dt.length!=4){
            newLoc();
            return;
        }
        try{
            World w0=getServer().getWorld(UUID.fromString(dt[0]));
            safeLocation=new Location(w0,Integer.parseInt(dt[1]), Integer.parseInt(dt[2]), Integer.parseInt(dt[3]));
        }
        catch(Exception e){
            newLoc();
        }
    }
    
    
    
    public boolean Init(){
        PlayerInfo.init(false);
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
        RealIP=配置文件.getBoolean("RealIP");
        CoverFlag=配置文件.getBoolean("CoverServer");
        loadPosData(配置文件.getString("SpwanPoint"));
        EnableJs=配置文件.getBoolean("EnableJS", true);
        return Link(Host,Port,false);
    }
    
    public static void UpdateServerInfo(String name){
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
                数据库API.Chat(e.getPlayer().getName(), e.getMessage());    
    }
    /*
    @EventHandler void LoginEvent(PlayerLoginEvent e){
        
        if(RedisPipeAPI.背包同步)
            if(数据库API!=null)
            {
                //数据库API.PlayerBackpack(e.getPlayer());
                getServer().getScheduler().runTaskLater(this, new Runnable() {
                    @Override
                    public void run() {
                        数据库API.PlayerBackpack(e.getPlayer());
                        log("同步背包完成!");
                    }
                }, 50);
                //e.getPlayer().getInventory().setItem(1, new ItemStack(Material.IRON_AXE));
                log("开始同步背包");
            }
        
    }
    @EventHandler void QuitEvent(PlayerQuitEvent e){
        if(RedisPipeAPI.背包同步)
            if(数据库API!=null)
            {
                if(noSave.contains(e.getPlayer().getName())){
                    noSave.remove(e.getPlayer().getName());
                    return;
                }
                getServer().getScheduler().runTask(this, new Runnable() {

                    @Override
                    public void run() {
                        数据库API.SavePlayer(e.getPlayer());
                        log("背包保存完成");
                    }
                });
                log("开始保存背包");
            }
    }
    */
    
    void LoadJSFile(){
        LoadJS(new File(getDataFolder(),"js"));
    }
    
    void LoadJS(File f){
        if(f==null)return;
        if(!f.exists())return;
        for(File mf:f.listFiles()){
            if(mf.isDirectory()){
                LoadJS(mf);
            }
            else{
                String p=mf.getAbsolutePath();
                if(p.endsWith(".js")){
                    try{
                        InputStreamReader reader = new InputStreamReader(new FileInputStream(p),"UTF-8");
                        System.out.println(reader.getEncoding());
                        JSTools.getInstance().eval(reader);
                        reader.close();
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
                }
            }
        }
    }
        
    EventManger eventManger;
    JSBukkit Tools;
    void LoadJS(){
        if(!EnableJs)return;
        eventManger=LoadEvents(this);
        out.print("Bytecode Load!");
        out.print(eventManger);
        eventManger.Setup(this);
        Tools=new JSBukkit(this, eventManger);
        //out.print(eventManger);
        getServer().getPluginManager().registerEvents(eventManger, this);
        out.print(this.getClass().getTypeName());
        LoadJSFile();
    }
    @Override
    public void onEnable() {
        Init();
        getServer().getPluginManager().registerEvents(this, this);
        localport=getServer().getPort();
        if(NBTCoder.Init()==-1){
            log("初始化物品序列化成功");
        }
        LoadJS();
    }

    @Override
    public void onDisable() {
        Close();
        HandlerList.unregisterAll(eventManger);
        eventManger.ClearEvent();
        Tools.ReInit();
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
        sender.sendMessage("[RedisPipe] 输入/rp setspawn <PlayerName> 以指定玩家位置设置出生点");
        
    }
    
    void tphelp(CommandSender sender){
        sender.sendMessage("[RedisPipe] 输入/rptp [服务器] 传送到目标服务器");
        sender.sendMessage("[RedisPipe] 输入/rptp [玩家] [服务器] 传送玩家到目标服务器");
        sender.sendMessage("[RedisPipe] 或者输入输入/rptp [玩家] [服务器IP] [服务器端口]");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p;
        String servername;
        if("rptp".equals(label)){
            if(!数据库.isConnected()){
                sender.sendMessage("[RedisPipe] 数据库未连接!");
                return false;
            }
            //sender.sendMessage("[RedisPipe] t1");
            if(args.length==1){
                if(sender instanceof Player){
                    p=(Player)sender;
                    servername=args[0];
                    if(p.hasPermission("rptp.notp")&&!p.isOp()){
                        sender.sendMessage("[RedisPipe] 没有足够的权限操作");
                        return false;
                    }
                    //sender.sendMessage("[RedisPipe] t2");
                    if(RedisPipeAPI.ServerName.equalsIgnoreCase(servername)){
                        sender.sendMessage("[RedisPipe] 当前服务器["+servername+"]");
                        return false;
                    }
                    if(!数据库API.Servers(servername)){
                        sender.sendMessage("[RedisPipe] 未发现该服务器");
                        return false;
                    }
                    //sender.sendMessage("[RedisPipe] t3");
                    if(RedisPipeAPI.背包同步){
                        //sender.sendMessage("[RedisPipe] t4");
                        SendPlayer(p, servername);
                        //sender.sendMessage("[RedisPipe] t5");
                    }else{
                        数据库API.sendPlayer(p.getUniqueId(), servername);
                    }
                }
                return true;
            }
        }
        if(!sender.hasPermission("op"))
        {
            sender.sendMessage("没有足够的权限操作");
            return false;
        }
        if("rptp".equals(label)){
            if(!数据库.isConnected()){
                sender.sendMessage("[RedisPipe] 数据库未连接!");
                return false;
            }
            if(args.length<2){
                tphelp(sender);
                return true;
            }
            p=getServer().getPlayer(args[0]);
            servername=args[1];
            if(p==null){
                sender.sendMessage("[RedisPipe] 未找到该玩家");
                return true;
            }
            if(args.length>=3){
                try {
                    int port=Integer.parseInt(args[2]);
                    数据库API.sendPlayer(p.getUniqueId(), servername, port);
                } catch (NumberFormatException e) {
                    sender.sendMessage("[RedisPipe] 端口输入错误");
                }
                return true;
            }
            else
            {
                if(RedisPipeAPI.ServerName.equalsIgnoreCase(servername)){
                    sender.sendMessage("[RedisPipe] 当前服务器["+servername+"]");
                    return false;
                }
                if(!数据库API.Servers(servername)){
                    sender.sendMessage("[RedisPipe] 未发现该服务器");
                    return false;
                }
                if(RedisPipeAPI.背包同步){
                    SendPlayer(p, servername);
                }else{
                    数据库API.sendPlayer(p.getUniqueId(), servername);
                }
                return true;
            }
        }
        //log(label);
        if(args.length<1)
        {
            help(sender);
            return true;
        }
            switch(args[0]){
                case "setspawn":
                    
                    if(args.length>1){
                        safeLocation=getServer().getPlayer(args[1]).getLocation();
                        saveSpwanPoint();
                        sender.sendMessage("设置成功!");
                    }
                    else if(sender instanceof Player){
                        safeLocation=((Player)sender).getLocation();
                        saveSpwanPoint();
                        sender.sendMessage("设置成功!");
                    }
                    else{
                        sender.sendMessage("[RedisPipe] 输入/rp setspawn <PlayerName> 以指定玩家位置设置出生点");
                    }
                    break;
                case "load":
                    if(args.length==2)
                    {
                        p=getServer().getPlayer(args[1]);
                        数据库API.LoadPlayer(p.getUniqueId());
                        sender.sendMessage("重新加载玩家背包 ["+args[1]+"] !");
                        return false;
                    }
                    break;
                
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
                    saveSpwanPoint();
                    sender.sendMessage("保存完成");
                    break;
                case "reloadjs":
                    eventManger.ClearEvent();
                    Tools.ReInit();
                    LoadJSFile();
                    break;
                default:
                    help(sender);
                break;
            }
        return true;
    }

    private void SendPlayer(Player p, String servername) throws IllegalArgumentException {
        noSave.add(p.getName());
        p.setNoDamageTicks(60);
        getServer().getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
                log("同步数据");
                数据库API.sendPlayer(p.getUniqueId(), servername);
                log("同步数据完成");
            }
        });
    }

    void saveSpwanPoint(){
        配置文件.set("SpwanPoint", safeLocation.getWorld().getUID().toString()+","+safeLocation.getBlockX()+","+safeLocation.getBlockY()+","+safeLocation.getBlockZ());
        PlayerInfo.setSpawnPoint(safeLocation.getWorld().getUID(),safeLocation.getBlockX(),safeLocation.getBlockY(),safeLocation.getBlockZ());
        try {
            配置文件.save(new File(getDataFolder(),"config.yml"));
        } catch (IOException ex) {
            Logger.getLogger(RedisPipe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void newLoc() {
        World w0=((World)getServer().getWorlds().get(0));
        safeLocation=w0.getSpawnLocation();
        safeLocation.setY(w0.getHighestBlockYAt(w0.getSpawnLocation()));
        saveSpwanPoint();
    }
    
    public static void main(String[] args) {
        
    }
    
}
