/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;


import com.github.KeyMove.EventsManager.EventManger;
import com.github.KeyMove.RedisPipe;
import com.github.KeyMove.RedisPipeAPI;
import com.github.KeyMove.RedisPipeAPI.RedisHandle;
import static java.lang.System.out;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import jdk.nashorn.api.scripting.JSObject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;


/**
 *
 * @author Administrator
 */
public class JSBukkit{
    

    ScriptEngine engine;
    public JSBukkit(){
        engine=JSTools.getInstance();
    }
    
    static SimpleCommandMap g_CommandMap;
    static Map g_knownCommand;
    static List<String> g_CommandName=new ArrayList<>();
    
    static Plugin g_Plugin;
    
    static List<jsBukkitThread> g_ThreadList=new ArrayList<>();
    static List<Integer> g_RunnableList=new ArrayList<>();
    
    public static Map<String,List<JSObject>> EventMap=new HashMap<>();
    public static Map<String,List<JSObject>> RedisMessageMap=new HashMap<>();
    public static Map<String,RedisPipeAPI.RedisHandle> RedisHandle=new HashMap<>();
    
    //static Map<Player, PacketHookBase> g_PacketHook=new HashMap<>();
    
    class jsBukkitCommand extends Command {
        JSObject jsMethod;
        public jsBukkitCommand(String name, String description, String usageMessage, List<String> aliases, JSObject jsMethod){
            super(name,description,usageMessage,aliases);
            this.jsMethod=jsMethod;
        }
        public jsBukkitCommand(String name, JSObject jsMethod){
            super(name);
            this.jsMethod=jsMethod;
        }
        @Override
        public boolean execute(CommandSender cs, String string, String[] strings) {
            
            this.jsMethod.call(this,cs, (Object[]) strings);
            return true;
        }
    }
    
    class jsBukkitThread extends Thread{
        JSObject LuaMethod;
        public jsBukkitThread(JSObject LuaMethod) {
            this.LuaMethod = LuaMethod;
        }
        @Override
        public void run() {
            this.LuaMethod.call(this,this);
        }
    }
    
    class jsBukkitRunnable implements Runnable{
        JSObject LuaMethod;

        public jsBukkitRunnable(JSObject LuaMethod) {
            this.LuaMethod = LuaMethod;
        }
        public void Start(){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(g_Plugin, this, 1);
        }
        public int Start(int time){
            return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(g_Plugin, this, 1, time);
        }
        @Override
        public void run() {
            this.LuaMethod.call(null);
        }
        
    }
    
    static void unregisterCommand(String name){
        if(g_knownCommand.containsKey(name))
            g_knownCommand.remove(name);
    }
    
    public static void ReInit(){
        
        for(String k : RedisMessageMap.keySet()){
            RedisMessageMap.get(k).clear();
            RedisHandle.get(k).Stop();
        }
        
        EventMap.clear();
        for(String name:g_CommandName){
            unregisterCommand(name);
        }
        g_CommandName.clear();
        for(jsBukkitThread t:g_ThreadList){
            t.stop();
        }
        g_ThreadList.clear();
        for(int id:g_RunnableList){
            g_Plugin.getServer().getScheduler().cancelTask(id);
        }
        g_RunnableList.clear();
        /*
        for(PacketHookBase h:g_PacketHook.values()){
            h.unregister();
        }
        g_PacketHook.clear();
        */
    }
    
    public void Command(Object... obj){
        if(obj.length==2){
            g_CommandMap.register("jsBukkit", new jsBukkitCommand((String)obj[0], (JSObject)obj[1]));
            g_CommandName.add((String)obj[0]);
        }
        else{
            g_CommandMap.register("jsBukkit", new jsBukkitCommand((String)obj[0],(String)obj[1],(String)obj[2], new ArrayList(), (JSObject)obj[1]));
        }
    }
    
    public void AsyncThread(Object... args){
        jsBukkitThread thread=new jsBukkitThread((JSObject)args[0]);
        g_ThreadList.add(thread);
        thread.start();
    }
    
    public Object SyncThread(Object... args){
        jsBukkitRunnable thread=new jsBukkitRunnable((JSObject)args[0]);
                    if(args.length>1){
                        int id=thread.Start((int)args[1]);
                        g_RunnableList.add(id);
                        return id;
                    }else{
                        thread.Start();
                    }
                    return null;
    }
    
    public void CancelSync(Object... args){
        Bukkit.getServer().getScheduler().cancelTask((int)args[0]);
    }
    
    public void Event(Object... args){
        eventManger.RegisterEvent((String)args[0], (JSObject)args[1]);
    }
    
    RedisPipeAPI api;
        
        public Object OnMessage(Object... args){
            String name=(String)args[0];
            List<JSObject> list;
            if(!RedisMessageMap.containsKey(name)){
                list=new ArrayList<>();
                list.add((JSObject)args[1]);
                RedisMessageMap.put(name, list);
                RedisHandle handle=api.RegisterChannel(name,new RedisPipeAPI.ChannelMessage() {
                    @Override
                    public void OnMessage(String message) {
                        for(JSObject obj : list){
                            obj.call(this, message);
                        }
                    }
                });
                handle.Start();
                RedisHandle.put(name, handle);
            }
            else{
                RedisMessageMap.get(name).add((JSObject)args[1]);
            }
            return (JSObject)args[1];
        }

        public void CancelMessage(Object... args){
            String name=(String)args[0];
            if(RedisMessageMap.containsKey(name)){
                RedisMessageMap.get(name).remove((JSObject)args[1]);
            }
        }

        public int PublishMessage(Object... args){
            return api.publishMessage((String)args[0],(String)args[1]);
        }
        
        
        public String set(Object... args){
            return api.set((String)args[0], (String)args[1]);
        }
        
        public String get(Object... args){
            return api.get((String)args[0]);
        }
    
    
    
    
    public Object LoadAPI(Object... args){
         String pname=(String)args[0];
         Plugin p=Bukkit.getServer().getPluginManager().getPlugin(pname);
         if(p!=null&&p.isEnabled()){
            try {
                String cname=(String)args[1];
                Class fc=Class.forName(cname);
                out.print(fc);
                RegisteredServiceProvider rp=Bukkit.getServicesManager().getRegistration(fc);
                out.print(rp);
                if(rp!=null)
                    return rp.getProvider();
                else
                    return null;
            } catch (ClassNotFoundException ex) {
                out.print(ex);
            }
         }
         else
             return p;
         return null;
    }
    
    
    EventManger eventManger;
    public JSBukkit(Plugin plugin,EventManger eventManger) {
        engine=JSTools.getInstance();
        engine.getContext().setAttribute("allowAllAccess", true, ScriptContext.ENGINE_SCOPE);
        //engine.createBindings().
        //Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        //bindings.put("polyglot.js.allowAllAccess", true);  
        
        api=RedisPipe.getInstance();
        this.eventManger=eventManger;
        g_Plugin=plugin;
        String ver=Bukkit.getServer().getClass().getPackage().getName();
        ver=ver.substring(ver.lastIndexOf('.')+1)+".";
        String nms="net.minecraft.server."+ver;
        try {
            Field fd=plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            fd.setAccessible(true);
            g_CommandMap=(SimpleCommandMap)fd.get(plugin.getServer().getPluginManager());
            Field fd2=g_CommandMap.getClass().getDeclaredField("knownCommands");
            fd2.setAccessible(true);
            g_knownCommand=(Map) fd2.get(g_CommandMap);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(JSBukkit.class.getName()).log(Level.SEVERE, null, ex);
        }
        String obc="org.bukkit.craftbukkit."+ver;
        engine.put("OBCPATH",obc);
        engine.put("NMSPATH",nms);
        engine.put("Tools", this);
        engine.put("Ref", Ref.class);
            /*
            set("PacketEvent", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    if(args.isfunction(2)){
                        Player p;
                        if(args.isstring(1)){
                            p=g_Plugin.getServer().getPlayer(args.tojstring(1));
                        }
                        else{
                            p=(Player) CoerceLuaToJava.coerce(args.arg(1), Player.class);
                        }
                        if(p!=null)
                        {
                            if(g_PacketHook.containsKey(p))
                            {
                                g_PacketHook.get(p).AddFunction(args.checkfunction(2));
                            }
                            else{
                                PacketHookBase b=EventsBuild.newPacketHook();
                                g_PacketHook.put(p, b);
                                b.HookPlayer(p);
                                b.AddFunction(args.checkfunction(2));
                            }
                        }
                    }
                    else{
                        out.print("注册事件失败");
                    }
                    return NIL;
                }
            });
            set("unPacketEvent", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                        Player p;
                        if(args.isstring(1)){
                            p=g_Plugin.getServer().getPlayer(args.tojstring(1));
                        }
                        else{
                            p=(Player) CoerceLuaToJava.coerce(args.arg(1), Player.class);
                        }
                        if(p!=null)
                        {
                            if(g_PacketHook.containsKey(p))
                            {
                                g_PacketHook.get(p).unregister();
                            }
                        }
                   
                    return NIL;
                }
            });
            */
            
        }
}
