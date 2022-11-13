/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;


/**
 *
 * @author Administrator
 */
public class PlayerData{
    Location safeLocation;
    UUID UUIDWorld;
    public byte[] save(Player p){
        return null;
    }
    
    public void load(byte[] bytes,Player p){
    
    }
    
    public void saveUUID(String uuid,byte[] data){
        
    }
    
}
