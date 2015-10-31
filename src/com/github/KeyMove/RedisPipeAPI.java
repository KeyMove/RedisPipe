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
    
    static Map<String,PlayerInfo> PlayerCache=new HashMap<>();
    
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
                        do{
                            messageJedis=pool.get();
                            try {
                                messageJedis.subscribe(handle, channelName);
                            } catch (Exception e) {
                            }
                            pool.release(messageJedis);
                        }while(relink);
                    }
                });
                handleThread.start();
            }
        }
        
        public void Stop(){
            relink=false;
            if(handleThread!=null)
                try {messageJedis.close();handleThread.stop();handleThread=null;} catch (Exception e) {}
        }
        
    }
    
    
    
    public void Chat(Player p, String message){
        if(聊天同步){
            if(!Check())return;
            database.publish(聊天通道, ServerName+"&"+p.getName()+"&"+message.replace("&", "§"));
        }
    }
    
    public RedisPipeAPI(RedisPool p){
        pool=p;
        database=pool.get();
        if(ChatHandle==null)
            ChatHandle=new RedisHandle(聊天通道,new ChannelMessage() {
            @Override
            public void OnMessage(String message) {
                String[] v=message.split("&");
                if(v.length!=3)return;
                if(v[0].equalsIgnoreCase(ServerName))return;
                Bukkit.broadcastMessage(MessageFormat.replace("%server%", v[0]).replace("%player%", v[1]).replace("%message%", v[2]));
            }
        });
        if(BackackHandle==null)
            BackackHandle=new RedisHandle(背包通道, new ChannelMessage() {
            @Override
            public void OnMessage(String message) {
                String[] v=message.split(",");
                if(v.length!=2)return;
                if(!v[0].equalsIgnoreCase(ServerName))return;
                Check();
                byte[] pdata=database.get(("PlayerData-"+v[1]).getBytes());
                if(pdata==null)return;
                PlayerInfo info=PlayerInfo.ArrayPlayer(pdata, new PlayerInfo());
                Player p=Bukkit.getPlayer(v[1]);
                if(p==null)
                    PlayerCache.put(v[1], info);
                else{
                    info.toPlayer(p);
                }
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
            PlayerCache.get(p.getName()).toPlayer(p);
            PlayerCache.remove(p.getName());
        }
    }
    
    public void SavePlayer(Player p){
        if(!Check())return;
        database.set(("PlayerData-"+p.getName()).getBytes(), PlayerInfo.PlayerArray(p));
    }
    
    public void LoadPlayer(Player p){
        if(!Check())return;
        byte[] arrays=database.get(("PlayerData-"+p.getName()).getBytes());
        if(arrays!=null){
            PlayerInfo info=PlayerInfo.ArrayPlayer(arrays, new PlayerInfo());
            info.toPlayer(p);
        }
    }
    
    
    public void SaveItemStack(String name,ItemStack item){
        if(!Check())return;
        try {
            ByteArrayOutputStream b=new ByteArrayOutputStream();
            DataOutputStream d=new DataOutputStream(b);
            NBTCoder.ItemStackArray(item, d);
            d.flush();
            database.set(name.getBytes(), b.toByteArray());
            b.close();
            d.close();
        } catch (IOException ex) {
            Logger.getLogger(RedisPipeAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void SaveItemStack(String name,ItemStack item,Runnable action){
        if(!Check())return;
        try {
            ByteArrayOutputStream b=new ByteArrayOutputStream();
            DataOutputStream d=new DataOutputStream(b);
            NBTCoder.ItemStackArray(item, d);
            d.flush();
            database.set(name.getBytes(), b.toByteArray());
            b.close();
            d.close();
        } catch (IOException ex) {
            Logger.getLogger(RedisPipeAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ItemStack LoadItemStack(String name){
        if(!Check())return null;
        try {
            byte[] data=database.get(name.getBytes());
            if(data==null)return null;
            ByteArrayInputStream ib=new ByteArrayInputStream(data);
            DataInputStream id=new DataInputStream(ib);
            ItemStack item=NBTCoder.ArrayItemStack(id);
            ib.close();
            id.close();
            return item;
        } catch (IOException ex) {
            Logger.getLogger(RedisPipeAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public int sendPlayer(Player player,String ServerName){
        if(!Check())return -1;
        if(背包同步)
            database.publish(背包通道, ServerName+","+player.getName()).intValue();
        return database.publish("tpplayer", player.getName()+","+ServerName).intValue();
    }
    
    public int sendPlayer(Player player,String addr,int port){
        if(!Check())return -1;
        return database.publish("tpplayer", player.getName()+","+addr+","+port).intValue();
    }
    
    
    
}
