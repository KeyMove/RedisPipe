/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;
import com.github.KeyMove.Tools.NBTCoder;
import com.github.KeyMove.Tools.PlayerInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 *
 * @author Administrator
 */
public class RedisPipeAPI {
    public static Jedis database;
    static boolean 背包同步=false;
    static boolean 聊天同步=false;
    static RedisPool pool;
    static RedisHandle ChatHandle;
    static String 聊天通道="Chat";
    static RedisHandle BackackHandle;
    static String 背包通道="Backpack";
    static List<Player> SendPlayers=new ArrayList<>();
    
    static Map<String,byte[]> PlayerCache=new HashMap<>();
    static List<String> ServerList=new ArrayList<>();
    public static String MessageFormat=null;
    public static String ServerName=null;
    
    abstract class ChannelMessage{
        abstract public void OnMessage(String message);
    }
    
    class RedisHandle extends JedisPubSub{
        ChannelMessage message;
        Thread handleThread;
        Jedis messageJedis;
        boolean relink=true;
        String channelName;
        
        public RedisHandle(String channel,ChannelMessage m) {
            message=m;
            channelName=channel;
        }
        
        @Override
        public void onMessage(String channel, String message) {
            this.message.OnMessage(message);
        }
        public void Start(){
            if(handleThread==null){
                relink=true;
                RedisHandle handle=this;
                handleThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        messageJedis=pool.get();
                        do{
                            try {
                                messageJedis.subscribe(handle, channelName);
                            } catch (Exception e) {
                                
                            }
                        }while(relink);
                        pool.release(messageJedis);
                    }
                });
                handleThread.start();
            }
        }
        
        public void Stop(){
            relink=false;
            if(handleThread!=null)
            {
                handleThread.stop();
                messageJedis.close();
                handleThread=null;
            }
        }
        
    }
    
    
    
    public void Chat(Player p, String message){
        if(聊天同步){
            if(!Check())return;
            database.publish(聊天通道, ServerName+"&"+p.getName()+"&"+message.replace("&", "§"));
        }
    }
    
    public List<String> Servers(){
        ServerList=database.lrange("server", 0, -1);
        return ServerList;
    }
    
    public boolean Servers(String ServerName){
        if(ServerName==null)return false;
        if(!Check())return false;
        if(ServerList.contains(ServerName)){
            return true;
        }
        try {
            ServerList=database.lrange("server", 0, -1);
            return ServerList.contains(ServerName);
        } catch (Exception e) {
        }
        return false;
        
    }
            
    
    public RedisPipeAPI(RedisPool p){
        pool=p;
        database=pool.get();
        if(ChatHandle==null)
            ChatHandle=new RedisHandle(聊天通道,new ChannelMessage() {
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
        });
        if(BackackHandle==null)
            BackackHandle=new RedisHandle(背包通道, new ChannelMessage() {
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
                        if(op.getName().equalsIgnoreCase(v[0])){
                            System.out.print("[RedisPipe] find ["+op.getName()+"] UUID: "+op.getUniqueId().toString());
                            PlayerInfo.saveData( op.getUniqueId().toString(), pdata);
                            b=true;
                            break;
                        }
                    }
                    if(!b){
                        System.out.print("[RedisPipe] new Player ["+v[0]+"]");
                        String uuid=js.get("UUID-"+v[0]);
                        PlayerInfo.saveData( uuid, pdata);
                    }
                    //PlayerCache.put(v[0], pdata);
                    js.publish("tpplayer", message).intValue();
                    System.out.print("[RedisPipe] send tp");
                    
                }
                else{
                    PlayerInfo.load(pdata,p);
                }
                pool.release(js);
            }
        });
        
        if(聊天同步){
            ChatHandle.Stop();
        }
        if(背包同步){
            BackackHandle.Stop();
        }
        
        //StartChat();
    }
    
    public boolean Check(){
        if(database==null)return false;
        if(!database.isConnected()){
            try{
            pool.release(database);
            database=pool.get();
            database.connect();
            }catch(Exception e){
                return false;
            }
        }
        return true;
    }
    
    public void NewPool(RedisPool p){
        pool.release(database);
        if(聊天同步){
            ChatHandle.Stop();
        }
        if(背包同步){
            BackackHandle.Stop();
        }
        pool=p;
        database=pool.get();
    }
    
    public void StartChat(){
        ChatHandle.Start();
    }
    public void StopChat(){
        ChatHandle.Stop();
    }
    
    public void StartBackpack(){
        BackackHandle.Start();
    }
    public void StopBackpack(){
        BackackHandle.Stop();
    }
    
    public void PlayerBackpack(Player p){
        if(PlayerCache.containsKey(p.getName())){
            System.out.println("缓存同步");
            byte[] inf=PlayerCache.get(p.getName());
            //System.out.println(inf);
            PlayerInfo.load(inf, p);
            PlayerCache.remove(p.getName());
        }
        else{
            System.out.println("数据库同步");
            LoadPlayer(p);
        }
    }
    
    public boolean SavePlayer(Player p){
        Jedis js=pool.get();
        if(js==null)return false;
        byte[] keys=("PlayerData-"+p.getName()).getBytes();
        try {
            js.set("UUID-"+p.getName(),p.getUniqueId().toString());
            js.del(keys);
            while(js.exists(keys));
            js.set(keys, PlayerInfo.save(p));
            while(!js.exists(keys));
            
            js.disconnect();
            pool.release(js);
        } catch (Exception e) {
            pool.release(js);
            return false;
        }
        return true;
    }
    
    public boolean LoadPlayer(Player p){
        Jedis js=pool.get();
        if(js==null)return false;
        try {
            byte[] arrays=js.get(("PlayerData-"+p.getName()).getBytes());
            if(arrays!=null){
                PlayerInfo.load(arrays, p);
            }
            pool.release(js);
        } catch (Exception e) {
            pool.release(js);
            return false;
        }
        return true;
    }
    
    
    
    public int sendPlayer(Player player,String ServerName){
        if(!Check())return -1;
        if(背包同步)
        {
            //System.out.println("保存");
            SavePlayer(player);
            //System.out.println("发送");
            return database.publish(背包通道, player.getName()+","+ServerName).intValue();
        }
        else{
            return database.publish("tpplayer", player.getName()+","+ServerName).intValue();
        }
    }
    
    public int sendPlayer(Player player,String addr,int port){
        if(!Check())return -1;
        return database.publish("tpplayer", player.getName()+","+addr+","+port).intValue();
    }
    
    
    
}
