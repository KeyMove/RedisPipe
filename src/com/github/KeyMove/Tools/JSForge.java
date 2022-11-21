/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove.Tools;

import com.github.KeyMove.EventForge.SendPlayerEvent;
import com.github.KeyMove.RedisPipeAPI;
import com.github.KeyMove.RedisPipeAPI.RedisHandle;
import com.github.KeyMove.RedisPipeForge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 * @author KeyMove
 */
public class JSForge {
    
    void Log(Object o){
        
    }
    ScriptEngine engine;
    static MinecraftServer ms;
    static Method EVrun;
    static Method regEV;
    public ModContainer Container;
    Map<String,Class> eventmap=new HashMap<>();
    Map<String,EventRunner> g_Events=new HashMap<>();
    List<ICommand> g_Commands=new ArrayList<>();
    Field cmdSet;
    Field cmdMap;
    
    public Map<String,List<JSRunner>> RedisMessageMap=new HashMap<>();
    public Map<String,RedisPipeAPI.RedisHandle> RedisHandle=new HashMap<>();
    public Map<ChannelHandler,ChannelPipeline> HandlerList=new HashMap<>();
    public class EventRunner{
        List<JSRunner> list=new ArrayList();
        public void add(JSRunner js){
            list.add(js);
        }
        
        public void remove(JSRunner js){
            list.remove(js);
        }
        
        public void clear(){
            list.clear();
        }
        
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void run(Object e){
            for(JSRunner r : list)
                r.run(e);
        }
    }
    
    public interface JSRunner{
        public void run(Object e);
    }
    
    public void SyncThread(Runnable run){
        ms.func_152344_a(run);
    }
    
    public abstract class IEV{
        public abstract void OnEvent(Object e);
    }
    Map<Integer,IEV> imap=new HashMap<>();
    public void testIEV(int id,IEV ev){
        imap.put(id, ev);
    }
    
    public void callIEV(int id){
        IEV ev=imap.get(id);
        if(ev!=null)
            ev.OnEvent(ev);
    }
    
    public void Event(String name,JSRunner e){
        Event(name,this.Container,e);
    }
    
    public void Event(String name,ModContainer Container,JSRunner e){
        Class c=eventmap.get(name);
        if(c==null){
            try {
                c=Loader.instance().getModClassLoader().loadClass(name);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JSForge.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(c==null)return;
        }
        EventRunner er=g_Events.get(name);
        if(er==null){
            er=new EventRunner();
            er.add(e);
            g_Events.put(name, er);
        }
        else{
            er.add(e);
            return;
        }
        try{
            regEV.invoke(FMLCommonHandler.instance().bus(),c,er,EVrun,Container);
        }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException ex){
            ex.printStackTrace();
        }
        //eventManger.RegisterEvent((String)args[0], (JSObject)args[1]);
    }
    
    public class JSCommand extends CommandBase{

        JSRunner js;
        String name;
        String usage;
        public int cmdlevel=0;
        public ICommandSender sender;
        public String[] args;
        public JSCommand(String name,String usage,int level,JSRunner js){
            this.js=js;
            this.name=name;
            this.usage=usage;
            this.cmdlevel=level;
        }
        
        public void send(String message){
            sender.func_145747_a(new TextComponentString(message));
        }
        
        @Override
        public int func_82362_a(){
            return cmdlevel;
        }
        
        @Override
        public boolean func_184882_a(MinecraftServer p_184882_1_, ICommandSender p_184882_2_) {
            return cmdlevel<1;
        }
        
        @Override
        public String func_71517_b() {
            return name;
        }

        @Override
        public String func_71518_a(ICommandSender p_71518_1_) {
            return usage;
        }

        @Override
        public void func_184881_a(MinecraftServer paramMinecraftServer, ICommandSender paramICommandSender, String[] paramArrayOfString) throws CommandException {
            sender=paramICommandSender;
            args=paramArrayOfString;
            js.run(this);
        }
        
        @Override
        public boolean func_82358_a(String[] p_82358_1_, int p_82358_2_) {
            return p_82358_2_ == 0;
         }
        
    }
    
    public Object Command(String cmd,String usage,JSRunner js){
        JSCommand c=new JSCommand(cmd,usage,0,js);
        ((CommandHandler)ms.func_71187_D()).func_71560_a(c);
        g_Commands.add(c);
        return c;
    }
    
    public void SendMessage(EntityPlayerMP p,String s){
        p.func_145747_a(new TextComponentString(s));
    }
    
    
    
    RedisPipeAPI api;
        
        public Object OnMessage(String name,JSRunner js){
            List<JSRunner> list;
            if(!RedisMessageMap.containsKey(name)){
                list=new ArrayList<>();
                list.add(js);
                RedisMessageMap.put(name, list);
                RedisHandle handle=api.RegisterChannel(name,new RedisPipeAPI.ChannelMessage() {
                    @Override
                    public void OnMessage(String message) {
                        for(JSRunner obj : list){
                            obj.run(message);
                        }
                    }
                });
                handle.Start();
                RedisHandle.put(name, handle);
            }
            else{
                RedisMessageMap.get(name).add(js);
            }
            return js;
        }

        public void CancelMessage(String name,JSRunner js){
            if(RedisMessageMap.containsKey(name)){
                RedisMessageMap.get(name).remove(js);
            }
        }

        public int PublishMessage(String name,String message){
            return api.publishMessage(name,message);
        }
        
        
        public String set(String key,String value){
            return api.set(key, value);
        }
        
        public String get(String key){
            return api.get(key);
        }
    
    
    public List<Class> getClassByPackage() {

        ClassLoader cl=ServerChatEvent.class.getClassLoader();
        String path=ServerChatEvent.class.getResource("").getFile().replaceAll("%20", " ");
        String[] jarInfo = path.split("!");  
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));  
        String packagePath = jarInfo[1].substring(1);
        //System.out.println(jarFilePath);
        //System.out.println(packagePath);
        List<Class> l=new ArrayList<>();
        
        File f=new File(jarFilePath+"!/"+packagePath);
        try {  
            //System.out.println(f);
            JarFile jarFile = new JarFile(jarFilePath);  
            Enumeration<JarEntry> entrys = jarFile.entries();  
            while (entrys.hasMoreElements()) {  
                JarEntry jarEntry = entrys.nextElement();  
                String entryName = jarEntry.getName();  
                if (entryName.contains(packagePath)&&entryName.endsWith(".class")) { 
                    entryName = entryName.substring(0, entryName.lastIndexOf("."));
                    if(entryName.endsWith("Event"))
                    {
                        try{
                            //System.out.println(entryName);
                            l.add(cl.loadClass(entryName));
                        }
                        catch (ClassNotFoundException ex) {  
                            Logger.getLogger(JSForge.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }  
                }
            }
        } catch (IOException e) {  
            Log(e);
        } 
        return l;
    }
    
    public void ChannelLast(ChannelPipeline pipe,JSRunner js){
        pipe.addLast(new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext chc, Object i) throws Exception {
                js.run(i);//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
    }
    
    public void ChannelLast(ChannelPipeline pipe,String b1,JSRunner js){
        pipe.addLast(b1,new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext chc, Object i) throws Exception {
                js.run(i);//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
    }
    
    public ChannelHandler ChannelBefor(ChannelPipeline pipe,String b1,String b2,JSRunner js){
        ChannelHandler ch;
        pipe.addBefore(b1,b2,ch=new SimpleChannelInboundHandler() {

            @Override
            protected void channelRead0(ChannelHandlerContext chc, Object i) throws Exception {
                
                js.run(i);
                chc.fireChannelRead(i);
                //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
        HandlerList.put(ch,pipe);
        return ch;
    }
    
    public void RemoveChannel(ChannelHandler ch){
        if(HandlerList.containsKey(ch)){
            HandlerList.get(ch).remove(ch);
            HandlerList.remove(ch);
        }
        
    }
    
    class PF implements IPlayerFileData{
        JSRunner save;
        JSRunner load;
        IPlayerFileData old;
        public PF(IPlayerFileData old,JSRunner load,JSRunner save){
            this.old=old;
            this.load=load;
            this.save=save;
        }
        @Override
        public void func_75753_a(EntityPlayer e) {
            save.run(e);//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public NBTTagCompound func_75752_b(EntityPlayer p_75752_1_) {
            NBTTagCompound nbt=new NBTTagCompound();
            load.run(new Object[]{p_75752_1_,nbt});
            return nbt;
            //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String[] func_75754_f() {
            return old.func_75754_f();//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
        
    }
    
    public IPlayerFileData PlayerFileProxy(IPlayerFileData old,JSRunner read,JSRunner save){
        return new IPlayerFileData() {
            @Override
            public void func_75753_a(EntityPlayer e) {
                save.run(e);//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public NBTTagCompound func_75752_b(EntityPlayer e) {
                List<Object> array=new ArrayList<>();
                array.add(e);
                read.run(array);
                return (NBTTagCompound)array.get(0);
                //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public String[] func_75754_f() {
                return old.func_75754_f();//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        };
    }
    
    public void restart(){
        for(EventRunner er : g_Events.values())
            er.clear();
        CommandHandler ch=(CommandHandler)ms.func_71187_D();
        for(ICommand c : g_Commands){
            try {
                ((Set)cmdSet.get(ch)).remove(c);
                ((Map)cmdMap.get(ch)).remove(c.func_71517_b());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(JSForge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        g_Commands.clear();
        for(List l : RedisMessageMap.values())
            l.clear();
        for(ChannelHandler cx:HandlerList.keySet()){
            HandlerList.get(cx).remove(cx);
        }
        HandlerList.clear();
    }
    public JSForge(ModContainer con){
        Container=con;
        for(Field f : CommandHandler.class.getDeclaredFields()){
            String name=f.getType().getName();
            System.out.println("field:"+name);
            if(name.contains("Set")){
                cmdSet=f;
                f.setAccessible(true);
            }
            if(name.contains("Map")){
                cmdMap=f;
                f.setAccessible(true);
            }
        }
        api=RedisPipeForge.getInstance();
        //NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        engine=JSTools.getInstance();
        ms=FMLCommonHandler.instance().getMinecraftServerInstance();
        for(Method m : EventBus.class.getDeclaredMethods()){
            if(m.getName().equalsIgnoreCase("register")){
                if(m.getParameters()[0].getType()!=Object.class){
                    regEV=m;
                    m.setAccessible(true);
                    System.out.println(m);
                    break;
                }
            }
        }
        EVrun=EventRunner.class.getMethods()[0];
        for(Class c : getClassByPackage()){
            String n=c.getName();
            n=n.substring(n.lastIndexOf(".")+1);
            eventmap.put(n, c);
            System.out.println(n);
        }
        eventmap.put("SendPlayerEvent", SendPlayerEvent.class);
        //engine=JSTools.getInstance();
        System.out.println("js:"+engine);
        //engine.getContext().setAttribute("allowAllAccess", true, ScriptContext.ENGINE_SCOPE);
        if(engine!=null)
        {
            engine.put("Tools", this);
            engine.put("MinecraftServer", ms);
            engine.put("Ref", Ref.class);
            engine.put("Loader", Loader.instance());
            engine.put("RedisApi", api);
        }
    }
}
