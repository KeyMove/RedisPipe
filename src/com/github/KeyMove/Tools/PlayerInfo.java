/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

/**
 *
 * @author Administrator
 */
public class PlayerInfo {
    
    public ItemStack 头;
    public ItemStack 衣服;
    public ItemStack 腿部;
    public ItemStack 鞋子;
    public ItemStack[] 物品栏;
    public PotionEffect[] 药水效果;
    public double[] 生命值;
    public int 饱食度;
    public int 等级;
    public float 经验值;
    
    public static byte[] PlayerArray(Player p){
        PlayerData data=new PlayerData();
        ByteArrayOutputStream by=new ByteArrayOutputStream();
        ObjectOutputStream localDataOutputStream;
        try {
            localDataOutputStream = new ObjectOutputStream(new GZIPOutputStream(by));
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }
        data.生命值=new double[]{p.getHealth(),p.getMaxHealth()};
        data.饱食度=p.getFoodLevel();
        
        int len=p.getActivePotionEffects().size()*3;
        if(len!=0){
            data.药水效果=new int[len];
            int count=0;
            for(PotionEffect eff: p.getActivePotionEffects()){
                data.药水效果[count++]=eff.getType().getId();
                data.药水效果[count++]=eff.getDuration();
                data.药水效果[count++]=eff.getAmplifier();
            }
        }
        
        data.等级=p.getLevel();
        data.经验值=p.getExp();
        
        PlayerInventory inv=p.getInventory();
        ItemStack itemStack;
        itemStack=inv.getHelmet();
        if(itemStack!=null)
            data.头=NBTCoder.ItemStackArray(itemStack);
        itemStack=inv.getChestplate();
        if(itemStack!=null)
            data.衣服=NBTCoder.ItemStackArray(itemStack);
        itemStack=inv.getLeggings();
        if(itemStack!=null)
            data.腿部=NBTCoder.ItemStackArray(itemStack);
        itemStack=inv.getBoots();
        if(itemStack!=null)
            data.鞋子=NBTCoder.ItemStackArray(itemStack);
        data.物品栏=new byte[36][];
        //System.out.println("SAVE");
        for(int i=0;i<36;i++){
            ItemStack item=inv.getItem(i);
            if(item!=null)
            {
                data.物品栏[i]=NBTCoder.ItemStackArray(item);
                //System.out.println(Arrays.toString(data.物品栏[i]));
            }
            else
            {
                data.物品栏[i]=null;
            }
        }
        byte[] arrays=null;
        try {
            localDataOutputStream.writeObject(data);
            localDataOutputStream.flush();
            localDataOutputStream.close();
            arrays=by.toByteArray();
            by.close();
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }
        
        return arrays;
    }
    
    public static PlayerInfo ArrayPlayer(byte [] arrays,PlayerInfo player){
        PlayerData data;
        ByteArrayInputStream ib=new ByteArrayInputStream(arrays);
        ObjectInputStream localDataInputStream;
        try {
            localDataInputStream = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(ib)));
            data=(PlayerData)localDataInputStream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(PlayerInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        //System.out.print(data);
        if(data.头!=null)
            player.头=NBTCoder.ArrayItemStack(data.头);
        if(data.衣服!=null)
            player.衣服=NBTCoder.ArrayItemStack(data.衣服);
        if(data.腿部!=null)
            player.腿部=NBTCoder.ArrayItemStack(data.腿部);
        if(data.鞋子!=null)
            player.鞋子=NBTCoder.ArrayItemStack(data.鞋子);
        
        player.等级=data.等级;
        player.生命值=data.生命值;
        player.经验值=data.经验值;
        player.饱食度=data.饱食度;
        
        player.物品栏=new ItemStack[36];
        //System.out.println("LOAD");
        for(int i=0;i<36;i++){
            if(data.物品栏[i]!=null)
            {
                player.物品栏[i]=NBTCoder.ArrayItemStack(data.物品栏[i]);
               // System.out.println(Arrays.toString(data.物品栏[i]));
               // System.out.println(player.物品栏[i]);
            }
        }
        
        if(data.药水效果!=null){
            int len=data.药水效果.length/3;
            if((len)!=0){
                player.药水效果=new PotionEffect[len];
                for(int i=0;i<len;i++){
                    player.药水效果[i]=new PotionEffect(PotionEffectType.getById(data.药水效果[i*3]), data.药水效果[i*3+1], data.药水效果[i*3+2]);
                }
            }
        }
        
        return player;
    }
    
    public static void InfoToPlayer(PlayerInfo info,Player player){
        PlayerInventory inv=player.getInventory();
        inv.setHelmet(info.头);
        inv.setChestplate(info.衣服);
        inv.setLeggings(info.腿部);
        inv.setBoots(info.鞋子);
        if(info.物品栏!=null)
            for(int i=0;i<info.物品栏.length;i++){
                inv.setItem(i, info.物品栏[i]);
            }
        
        if(info.生命值!=null){
            player.setHealth(info.生命值[0]);
            player.setMaxHealth(info.生命值[1]);
        }
        player.setFoodLevel(info.饱食度);
        player.setLevel(info.等级);
        player.setExp(info.经验值);
        
        if(info.药水效果!=null)
            for(PotionEffect pe:info.药水效果){
                pe.apply(player);
            }
    }
    
    public void toPlayer(Player player){
        PlayerInventory inv=player.getInventory();
        inv.setHelmet(头);
        inv.setChestplate(衣服);
        inv.setLeggings(腿部);
        inv.setBoots(鞋子);
        if(物品栏!=null)
            for(int i=0;i<物品栏.length;i++){
                inv.setItem(i, 物品栏[i]);
            }
        if(生命值!=null)
        {   
            player.setHealth(生命值[0]);
            player.setMaxHealth(生命值[1]);
        }
        player.setFoodLevel(饱食度);
        player.setLevel(等级);
        player.setExp(经验值);
        
        if(药水效果!=null)
            for(PotionEffect pe:药水效果){
                if(pe!=null)
                    pe.apply(player);
            }
    }
    
}
