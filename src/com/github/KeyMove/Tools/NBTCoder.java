/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import com.github.KeyMove.RedisPipeAPI;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Administrator
 */
public class NBTCoder {
    static Field itemhandle;//CraftItemStack handle
    static Field tag;//
    static Class NBT;
    static Class ServerItemStack;
    static Class CraftItemStack;
    static Method asBukkitCopy;
    static Method saveitem;
    static Method createStack;
    static Method save;
    static Method load;
    static Constructor track;
    static boolean InitField=false;
    
    static void log(String m){
        System.out.print("[Data] "+m);
    }
    
    public static Field getFieldWithString(Object obj,String name){
        for(Field fd: obj.getClass().getDeclaredFields()){
            if(fd.getType().getName().contains(name)){
                return fd;
            }
        }
        return null;
    }
    public static Field getFieldWithString(Class c,String name){
        for(Field fd: c.getDeclaredFields()){
            if(fd.getType().getName().contains(name)){
                return fd;
            }
        }
        return null;
    }
    
    public static Object[] getMethodsWithString(Class c,String ret,String... a){
        List<Method> mlist=new ArrayList<>();
        for(Method m:c.getDeclaredMethods()){
            if(ret!=null)
            if(!m.getGenericReturnType().getTypeName().contains(ret)){
                continue;
            }
            Class[] types=m.getParameterTypes();
            if(a.length!=types.length)continue;
            boolean cont=false;
            for(int i=0;i<a.length;i++){
                if(!types[i].getTypeName().contains(a[i]))
                {
                    cont=true;
                    break;
                }
            }
            if(cont)continue;
            mlist.add(m);
        }
        return mlist.toArray();
    }
    
    public static int Init(){
        String ver=Bukkit.getServer().getClass().getPackage().getName();
        ver=ver.substring(ver.lastIndexOf('.')+1)+".";
        String ItemPath="org.bukkit.craftbukkit."+ver+"inventory.CraftItemStack";
        log(ItemPath);
        Class ItemClass = null;
        try {
            CraftItemStack=ItemClass = Class.forName(ItemPath);
        } catch (ClassNotFoundException ex) {
            return 0;
        }
        itemhandle=getFieldWithString(CraftItemStack, "ItemStack");
        if(itemhandle==null)return 1;
        log(itemhandle.toString());
        tag=getFieldWithString(itemhandle.getType(),"NBTTagCompound");
        if(tag==null)return 2;
        log(tag.toString());
        itemhandle.setAccessible(true);
        tag.setAccessible(true);
        ServerItemStack=itemhandle.getType();
        NBT=tag.getType();
        log(NBT.toString());
        Object[] m=getMethodsWithString(CraftItemStack, "org.bukkit.inventory.ItemStack", "ItemStack");
        if(m.length==0)return 3;
        asBukkitCopy=(Method)m[0];
        log(asBukkitCopy.toString());
        m=getMethodsWithString(ServerItemStack, "NBTTagCompound", "NBTTagCompound");
        if(m.length==0)return 4;
        saveitem=(Method)m[0];
        log(saveitem.toString());
        m=getMethodsWithString(ServerItemStack, "ItemStack", "NBTTagCompound");
        if(m.length==0)return 5;
        createStack=(Method)m[0];
        log(createStack.toString());
        m=getMethodsWithString(NBT, null, "DataOutput");
        if(m.length==0)return 6;
        save=(Method)m[0];
        save.setAccessible(true);
        log(save.toString());
        m=getMethodsWithString(NBT, null, "DataInput","int","NBT");
        if(m.length==0)return 7;
        load=(Method)m[0];
        load.setAccessible(true);
        track=load.getParameterTypes()[2].getDeclaredConstructors()[0];
        //track.getConstructors()[0].newInstance(32768);
        log(load.toString());
        //load.getGenericParameterTypes()[2].getClass()
        return -1;
    }
    
    public static byte[] ItemStackArray(ItemStack item){
        byte[] out=null;
        try {
            ByteArrayOutputStream b=new ByteArrayOutputStream();
            DataOutputStream d=new DataOutputStream(b);
            ItemStackArray(item, d);
            d.flush();
            out=b.toByteArray();
            b.close();
            d.close();
        } catch (IOException ex) {
            Logger.getLogger(RedisPipeAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
    public static void ItemStackArray(ItemStack item,DataOutputStream os){
        try {
            save.invoke(saveitem.invoke(itemhandle.get(item),NBT.newInstance()),os);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(NBTCoder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException | InvocationTargetException ex) {
            Logger.getLogger(NBTCoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ItemStack ArrayItemStack(byte[] arrays){
        try {
            if(arrays==null)return null;
            ByteArrayInputStream ib=new ByteArrayInputStream(arrays);
            DataInputStream id=new DataInputStream(ib);
            ItemStack item=ArrayItemStack(id);
            ib.close();
            id.close();
            return item;
        } catch (IOException ex) {
            Logger.getLogger(RedisPipeAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static ItemStack ArrayItemStack(DataInputStream is){
        try {
            Object nbt=NBT.newInstance();
            load.invoke(nbt, is,32,track.newInstance(32768));
            return (ItemStack) asBukkitCopy.invoke(CraftItemStack,createStack.invoke(ServerItemStack,nbt));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(NBTCoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
