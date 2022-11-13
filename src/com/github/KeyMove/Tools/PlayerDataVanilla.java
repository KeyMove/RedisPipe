/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove.Tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author KeyMove
 */
public class PlayerDataVanilla extends PlayerData {
    public byte[] 头;
    public byte[] 衣服;
    public byte[] 腿部;
    public byte[] 鞋子;
    public byte[][] 物品栏;
    public byte [][] 末影箱;
    public int[] 药水效果;
    public double[] 生命值;
    public int 饱食度;
    public int 等级;
    public float 经验值;
    
    
     @Override
        public byte[] save(Player p) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        ((CraftPlayer)p).getHandle().func_70014_b(nbttagcompound);
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            CompressedStreamTools.func_74799_a(nbttagcompound, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception|Error e) {
            throw new Error(p.getName()+"玩家通过NMS序列化发送错误！",e);
        }
    }
        
        @Override
    public void load(byte[] bytes,Player p) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            ((CraftPlayer)p).getHandle().func_70037_a(CompressedStreamTools.func_74796_a(byteArrayInputStream));
        }catch (Exception|Error e) {
            throw new Error(p.getName()+"玩家通过NMS返序列化发送错误！",e);
        }
    }
    
}
