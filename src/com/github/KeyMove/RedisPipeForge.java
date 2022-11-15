/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove;


import com.github.KeyMove.RedisPipeAPI.ChannelMessage;
import static com.github.KeyMove.RedisPipeAPI.MessageFormat;
import static com.github.KeyMove.RedisPipeAPI.ServerName;
import com.github.KeyMove.Tools.PlayerInfo;
import com.google.common.collect.Lists;
import java.io.File;
import static java.lang.System.out;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import redis.clients.jedis.Jedis;

/**
 *
 * @author KeyMove
 */
@Mod.EventBusSubscriber
@Mod(modid="redispipe", name="RedisPipe",serverSideOnly = true,acceptableRemoteVersions="*")
public class RedisPipeForge {
    public static RedisPool pool;
    public static Jedis 数据库=new Jedis();
    public static RedisPipeAPI 数据库API=null;
    
    public static String Host;
    public static int Port;
    public static String localaddr=null;
    public static int localport=25565;
    
    public static Logger log=Logger.getLogger("RedisPipe");
    
    public static MinecraftServer ms;
    public static PlayerList pl;
    static void log(String message){
        out.print("[RedisPipe] "+message);
    }
    Configuration config;
    void loadconfig(){
        config=new Configuration(ConfigFile);
        Host=config.get("data", "Host", "127.0.0.1","Redis服务器地址").getString();
        Port=config.get("data", "Port", 6379,"Redis服务器端口").getInt();
        RedisPipeAPI.ServerName=config.get("data", "ServerName", "lobby","服务器名称").getString();
        RedisPipeAPI.聊天同步=config.get("data", "ServerChat",true,"是否同步聊天内容").getBoolean();
        RedisPipeAPI.MessageFormat=config.get("data", "ServerChatFormat", "'[%server%] <%player%> %message%'","聊天格式").getString();
        RedisPipeAPI.背包同步=config.get("data", "ServerBackpack", true,"是否同步玩家背包").getBoolean();
        MessageFormat=config.get("data", "ServerChatFormat", "'[%server%] <%player%> %message%'","聊天格式").getString();
        Close();
        pool=new RedisPool(Host, Port);
        if(!RedisPool.TryConnect(Host,Port,500)){
            return;
        }
        数据库=pool.get();
        数据库.clientSetname(RedisPipeAPI.ServerName);
        if(localaddr==null)Updatelocaladdr();
        数据库.lpush("server", RedisPipeAPI.ServerName);
            if(!localaddr.contains("127.0.0.1"))
            {
                数据库.set("serverinfo-"+RedisPipeAPI.ServerName, localaddr+","+localport);
                log("本机地址: ["+localaddr+"]");
            }
        log("数据库初始化成功!");
        if(数据库API==null)
            数据库API=new RedisPipeAPI(pool,new ChannelMessage() {
            @Override
            public void OnMessage(String message) {
                //System.out.print("[RedisPipe] recvMessage "+message);
                String[] v=message.split("&");
                //System.out.print("[RedisPipe] recvMessage 1");
                if(v.length!=3)return;
                //System.out.print("[RedisPipe] recvMessage 2");
                if(v[0].equalsIgnoreCase(RedisPipeAPI.ServerName))return;
                String bc=MessageFormat.replace("%server%", v[0]).replace("%player%", v[1]).replace("%message%", v[2]);
                //System.out.print("[RedisPipe] recvMessage "+bc);
                //MinecraftServer.
                //Bukkit.getServer().broadcastMessage(bc);
                if(ms!=null)
                    ms.func_184103_al().func_148544_a(ITextComponent.Serializer.func_150699_a("{\"text\":\""+bc+"\"}"),true);
            }
        },
            new ChannelMessage() {
            @Override
            public void OnMessage(String message) {
                String[] v=message.split(",");
                if(v.length!=2)return;
                //System.out.print(v[0]);
                //System.out.print(v[1]);
                //log.log(Level.INFO,"[RedisPipe] message: "+v[0]);
                if(!v[1].equalsIgnoreCase(RedisPipeAPI.ServerName))return;
                //Check();
                Jedis js=pool.get();
                if(js==null)return;
                byte[] pdata=js.get(("PlayerData-"+v[0]).getBytes());
                log.log(Level.OFF,"[RedisPipe] PlayerData-"+v[0]);
                if(pdata==null){pool.release(js);return;}
                PlayerInfo.saveData( UUID.fromString(v[0]), pdata);
                js.publish("tpplayer", message).intValue();
                //log.log(Level.INFO,"[RedisPipe] send tp");
                pool.release(js);
            }
        });
        else
            数据库API.NewPool(pool);
        if(RedisPipeAPI.聊天同步){
            数据库API.StartChat();
            log("注册聊天通道!");
        }
        if(RedisPipeAPI.背包同步){
            数据库API.StartBackpack();
            log("注册背包同步通道!");
        }
    }
    
    public static void Updatelocaladdr(){
        String v=数据库.clientList();
        String[] list=v.split("\n");
        for(String s:list){
            //log(s);
            if(s.contains("name="+RedisPipeAPI.ServerName)){
                int spos=s.indexOf("addr=")+5;
                int epos=s.indexOf(" fd=");
                String[] a=s.substring(spos,epos).split(":");
                if(!a[0].contains("127.0.0.1"))
                    localaddr=a[0];
                if(localaddr==null)
                    localaddr=a[0];
                //log("本机地址: ["+localaddr+"]");
                return;
            }
        }
    }
    File ConfigFile;
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        //inscall=this;
        FMLCommonHandler.instance().bus().register(this);
        log.info("[RedisPipe] 加载成功!");
        PlayerInfo.init(true);
        File f=new File(event.getModConfigurationDirectory(),"redispipe.cfg");
        ConfigFile=f;
        if(!f.exists())
        {
            config=new Configuration(f=event.getSuggestedConfigurationFile());
            config.get("data", "Host", "127.0.0.1","Redis服务器地址").set("127.0.0.1");
            config.get("data", "Port", 6379,"Redis服务器端口").set(6379);
            config.get("data", "ServerName", "lobby","服务器名称").set("lobby");
            config.get("data", "ServerChat",true,"是否同步聊天内容").set(true);
            config.get("data", "ServerBackpack", true,"是否同步玩家背包").set(true);
            config.get("data", "ServerChatFormat", "[%server%] <%player%> %message%","聊天格式").set("[%server%] <%player%> %message%");
            config.get("data", "SpwanPoint", "","自定义玩家出生点位置").set("");
            config.save();
        }
        loadconfig();
        
        
        
    }
    
    public static void Close(){
        if(数据库.isConnected()){
            try{
            数据库.lrem("server", 0, RedisPipeAPI.ServerName);
            数据库.quit();
            //数据库.clientSetname("");
            //数据库.close();
            //数据库.connect();
            //数据库.disconnect();
            }catch(Exception e){
                
            }
        }
        if(数据库API!=null){
            if(RedisPipeAPI.聊天同步){
                数据库API.StopChat();
                log("关闭背包同步通道!");
            }
            if(RedisPipeAPI.背包同步){
                数据库API.StopBackpack();
                log("关闭背包同步通道!");
            }
        }
    }
    
    public static void UpdateServerInfo(String name){
        if(数据库.isConnected()){
            数据库.lrem("server", 0, RedisPipeAPI.ServerName);
            RedisPipeAPI.ServerName=name;
            数据库.lpush("server", RedisPipeAPI.ServerName);
            数据库.set("serverinfo-"+RedisPipeAPI.ServerName, localaddr+","+localport);
            数据库.clientSetname(RedisPipeAPI.ServerName);
        }
        else{
            RedisPipeAPI.ServerName=name;
        }
    }
    public class TPCMD extends CommandBase{

        List<String> cmdlist = Lists.newArrayList("rptp");
        
        @Override
        public List<String> func_71514_a(){
            return cmdlist;
        }
        
        @Override
        public String func_71517_b() {
            return "rptp";
            //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String func_71518_a(ICommandSender p_71518_1_) {
            return "rptp <Player>";//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
        
        void tpproc(EntityPlayerMP mp){
            mp.func_184224_h(true);
                        new Thread(new Runnable(){
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(RedisPipeForge.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                ms.func_152344_a(new Runnable() {
                                    @Override
                                    public void run() {
                                        mp.func_184224_h(false);
                                    }
                                });
                            }
                        }).start();
        }

        @Override
        public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if(args.length<1)
            {
                sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] /rptp <服务器> 传送到指定服务器\"}"));
                sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] /rptp <服务器> <玩家ID> 传送玩家到指定服务器\"}"));
                return;
            }
            if(args.length>0&&sender instanceof EntityPlayerMP){
                        String servername=args[0];
                        if(RedisPipeAPI.ServerName.equalsIgnoreCase(servername)){
                            sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] 当前服务器["+servername+"]\"}"));
                            return;
                        }
                        if(!数据库API.Servers(servername)){
                            sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] 未发现该服务器\"}"));
                            return;
                        }
                        tpproc((EntityPlayerMP)sender);
                        数据库API.sendPlayer(((EntityPlayerMP)sender).func_110124_au(), servername);
                    }
                    if(args.length>1){
                        String servername=args[0];
                        
                        if(!数据库API.Servers(servername)){
                            sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] 未发现该服务器\"}"));
                            return;
                        }
                        EntityPlayerMP mp=ms.func_184103_al().func_152612_a(args[1]);
                        if(mp==null){
                            sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] 未找到该玩家\"}"));
                            return;
                        }
                        tpproc((EntityPlayerMP)sender);
                        数据库API.sendPlayer(mp.func_110124_au(), servername);
                    }
            //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
        
    }
    public class CMD extends CommandBase{
        
        List<String> cmdlist = Lists.newArrayList("reload","server","tp");
        
        @Override
        public List<String> func_71514_a(){
            return cmdlist;
        }
        
        @Override
        public String func_71517_b() {
            return "rp";
            //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public String func_71518_a(ICommandSender p_71518_1_) {
            return "rp <id>";//throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        
        
        @Override
        public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if(args.length<1)
            {
                sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] /rp server <名称> 重新设定服务器名称\"}"));
                sender.func_145747_a(ITextComponent.Serializer.func_150699_a("{\"text\":\"[RedisPipe] /rp reload 重载配置文件\"}"));
                return;
            }
            switch(args[0]){
                case "reload":
                    loadconfig();
                    break;
                case "server":
                    if(args.length>1){
                        UpdateServerInfo(args[1]);
                    }
                    break;
            }
            //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
        
    }
    
    @EventHandler
    public void start(FMLServerStartingEvent e){
        e.registerServerCommand(new CMD());
        e.registerServerCommand(new TPCMD());
        ms=e.getServer();
        pl=ms.func_184103_al();
        
        WorldServer w=ms.field_71305_c[0];
        BlockPos pos=w.func_175694_M();
        PlayerInfo.setSpawnPoint(null  , pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p()); 
    }
    
    @SubscribeEvent
    public void ServerChatEvent(ServerChatEvent event){
        String s=event.getMessage();
        //if(ms==null)
        //    ms=event.getPlayer().field_71133_b;
        //event.getPlayer().field_71133_b.func_184103_al().func_148544_a(ITextComponent.Serializer.func_150699_a("{\"text\":\""+s+"\"}"),true);
        //System.out.println("[RedisPipe] Chat: "+s);
        if(RedisPipeAPI.聊天同步)
            if(数据库API!=null)
                数据库API.Chat(event.getPlayer().func_70005_c_(), s);  
    }
}
