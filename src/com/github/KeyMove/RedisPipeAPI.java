/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;
import com.github.KeyMove.Tools.PlayerInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    
    static Map<String,byte[]> PlayerCache=new HashMap<>();
    static List<String> ServerList=new ArrayList<>();
    public static String MessageFormat=null;
    public static String ServerName=null;
    
    Map<String,RedisHandle> channelMap=new HashMap<>();
    
    public static abstract class ChannelMessage{
        abstract public void OnMessage(String message);
    }
    
    public String getServerName(){
        return ServerName;
    }
    
    public RedisHandle RegisterChannel(String ch,ChannelMessage msg){
        if(channelMap.containsKey(ch))return channelMap.get(ch);
        RedisHandle handle=new RedisHandle(ch,msg);
        channelMap.put(ch, handle);
        return handle;
    }
    
    public class RedisHandle extends JedisPubSub{
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
    
    
    
    public void Chat(String p, String message){
        if(聊天同步){
            if(!Check())return;
            database.publish(聊天通道, ServerName+"&"+p+"&"+message.replace("&", "§"));
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

    
    public RedisPipeAPI(RedisPool p,ChannelMessage ch聊天通道,ChannelMessage ch背包通道){
        pool=p;
        database=pool.get();
        if(ChatHandle==null)
            ChatHandle=new RedisHandle(聊天通道,ch聊天通道);
        if(BackackHandle==null)
            BackackHandle=new RedisHandle(背包通道, ch背包通道);
        
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
    
    public void PlayerBackpack(UUID p){
        if(PlayerCache.containsKey(p)){
            System.out.println("缓存同步");
            byte[] inf=PlayerCache.get(p);
            //System.out.println(inf);
            PlayerInfo.load(inf, p);
            PlayerCache.remove(p);
        }
        else{
            System.out.println("数据库同步");
            LoadPlayer(p);
        }
    }
    
    public boolean SavePlayer(UUID p){
        Jedis js=pool.get();
        if(js==null)return false;
        byte[] keys=("PlayerData-"+p).getBytes();
        try {
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
    
    public boolean LoadPlayer(UUID p){
        Jedis js=pool.get();
        if(js==null)return false;
        try {
            byte[] arrays=js.get(("PlayerData-"+p).getBytes());
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
    
    public int publishMessage(String ch,String message){
        if(!Check())return -1;
        return database.publish(ch, message).intValue();
    }
    
    public String get(String key){
        Jedis js=pool.get();
        String value=js.get(key);
        pool.release(js);
        return value;
    }
    
    public String set(String key,String value){
        Jedis js=pool.get();
        String v=js.set(key,value);
        pool.release(js);
        return v;
    }
    
    public int sendPlayer(UUID player,String ServerName){
        if(!Check())return -1;
        if(背包同步)
        {
            //System.out.println("保存");
            SavePlayer(player);
            System.out.println("发送: "+player+","+ServerName);
            return database.publish(背包通道, player+","+ServerName).intValue();
        }
        else{
            System.out.println("直接传送玩家: "+player+","+ServerName);
            return database.publish("tpplayer", player+","+ServerName).intValue();
        }
    }
    
    public int sendPlayer(UUID player,String addr,int port){
        if(!Check())return -1;
        return database.publish("tpplayer", player+","+addr+","+port).intValue();
    }
    
    
    
}
