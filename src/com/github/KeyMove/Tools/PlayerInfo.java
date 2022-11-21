/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import java.util.UUID;

/**
 *
 * @author Administrator
 */
public class PlayerInfo{
    
    public static PlayerData dt;
    
    public static void init(boolean mod){
        if(mod)
            dt=new PlayerData12();
        else
            dt=new PlayerDataVanilla();
    }
    
    public static void setSpawnPoint(UUID world,int x,int y,int z){
        dt.setSpawn(world, x, y, z);
    }
    
    public static void setSpawnPoint(int world,int x,int y,int z){
        dt.setSpawn(world, x, y, z);
    }
    
    public static boolean SpawnSet(){
        return dt.setspawn;
    }
    
    public static byte[] save(UUID p){
        return dt.save(p);
    }
    
    public static void load(byte [] arrays,UUID player){
        dt.load(arrays, player);
    }
    
    
    public static void saveData(UUID uuid,byte[] data){
        dt.saveUUID(uuid,data);
    }
    
}
