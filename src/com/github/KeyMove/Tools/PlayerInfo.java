/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Administrator
 */
public class PlayerInfo{
    
    public static PlayerData dt;
    
    public static void init(){
        dt=new PlayerData12();
    }
    
    public static void setSpawnPoint(Location spawnPoint){
        dt.safeLocation=spawnPoint;
    }
    
    public static byte[] save(Player p){
        return dt.save(p);
    }
    
    public static void load(byte [] arrays,Player player){
        dt.load(arrays, player);
    }
    
    
    public static void saveData(String uuid,byte[] data){
        dt.saveUUID(uuid,data);
    }
    
}
