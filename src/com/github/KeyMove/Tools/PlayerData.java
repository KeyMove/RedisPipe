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
public class PlayerData{
    int x,y,z;
    UUID UUIDWorld;
    boolean saveFile=true;
    
    public void setSpawn(UUID uuid,int x,int y,int z){
        
    }
    
    public byte[] save(UUID p){
        return null;
    }
    
    public void load(byte[] bytes,UUID p){
    
    }
    
    public void saveUUID(UUID uuid,byte[] data){
        
    }
    
}
