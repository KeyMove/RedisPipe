/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author Administrator
 */
public class RedisPool{
    
    public static boolean TryConnect(String host,int port,int timeout){
        try {
            Socket socket=new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(RedisPool.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    List<Jedis> JedisList=new ArrayList<>();
    List<Jedis> UseJedList=new ArrayList<>();
    public RedisPool(String host,int port){
        //super(getConfig(), host, port);
        for(int i=0;i<50;i++){
            JedisList.add(new Jedis(host, port,10000,60000));
        }
    }

    public static JedisPoolConfig getConfig() {
        JedisPoolConfig config;
        config = new JedisPoolConfig();
        config.setMaxTotal(50);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000);
        config.setTestOnBorrow(true);
        return config;
    }
    
    
    
    public Jedis get(){
        return getResource();
    }
    
    public void release(Jedis js){
        if(js!=null){
            returnResource(js);
        }
    }

    private Jedis getResource() {
        if(!JedisList.isEmpty())
        {
            Jedis js=JedisList.get(0);
            JedisList.remove(js);
            UseJedList.add(js);
            return js;
        }
        return null;
    }

    private void returnResource(Jedis js) {
        if(UseJedList.contains(js)){
            UseJedList.remove(js);
            JedisList.add(js);
            js.close();
        }
    }
}
