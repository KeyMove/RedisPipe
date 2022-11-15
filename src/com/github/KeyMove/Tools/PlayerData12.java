package com.github.KeyMove.Tools;

import com.github.KeyMove.RedisPipeForge;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import static java.lang.System.out;
import java.util.UUID;
import java.util.logging.Level;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author KeyMove
 */
public class PlayerData12 extends PlayerData{
        
        
        public PlayerData12(){
            
            /*
            SaveHandler old=(SaveHandler)(((CraftServer)getServer()).getHandle().field_72412_k);
            out.print("[RedisPipe] Name: "+old.toString());
            Field[] fs=SaveHandler.class.getDeclaredFields();
            for(Field fd: SaveHandler.class.getDeclaredFields()){
                out.print("[RedisPipe] Field: "+fd.getType().getName());
            }
            for(int i=0;i<fs.length;i++){
                fs[i].setAccessible(true);
            }
            try {
                newsh=new SaveHandlerProxy(new File("/"),"World",fs[6].get(old)!=null,(DataFixer)fs[7].get(old));
                newsh.old=old;
                
                for(int i=0;i<fs.length;i++){
                    fs[i].setAccessible(true);
                    fs[i].set(newsh, fs[i].get(old));
                }
                
                //out.print("[RedisPipe] FileName: "+Bukkit.getWorldContainer());
                //out.print("[RedisPipe] FileName: "+getServer().getUpdateFolderFile().toString());
                //out.print("[RedisPipe] FileName: "+fs[5].get(old).toString());
                
                
                //newsh=new SaveHandlerProxy((SaveHandler)(((CraftServer)getServer()).getHandle().field_72412_k));
                ((CraftServer)getServer()).getHandle().field_72412_k=(IPlayerFileData) newsh;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(PlayerData12.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        }
     
        
        @Override
        public byte[] save(UUID p) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        RedisPipeForge.pl.func_177451_a(p).func_189511_e(nbttagcompound);
        //out.print("[RedisPipe] Save\n"+nbttagcompound.toString());
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            CompressedStreamTools.func_74799_a(nbttagcompound, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception|Error e) {
            throw new Error("玩家通过NMS序列化发送错误！",e);
        }
    }
       
        @Override
    public void load(byte[] bytes,UUID p) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            NBTTagCompound nbt=CompressedStreamTools.func_74796_a(byteArrayInputStream);
            //out.print("[RedisPipe] load\n"+nbt.toString());
            RedisPipeForge.pl.func_177451_a(p).func_70020_e(nbt);
        }catch (Exception|Error e) {
            throw new Error("玩家通过NMS返序列化发送错误！",e);
        }
    }
    
        
  protected NBTTagList func_70087_a(double... numbers) {
    NBTTagList nbttaglist = new NBTTagList();
    for (double d0 : numbers)
      nbttaglist.func_74742_a((NBTBase)new NBTTagDouble(d0)); 
    return nbttaglist;
  }
    static File PlayerData;
    @Override
    public void saveUUID(UUID uuid,byte[] data){
        try{
            RedisPipeForge.log.log(Level.INFO,"[RedisPipe] save UUID: "+uuid);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            NBTTagCompound nbt=CompressedStreamTools.func_74796_a(byteArrayInputStream);
            NBTTagCompound oldnbt=null;
            if(PlayerData==null)
                PlayerData=new File("./","world/playerdata/");
            File f = new File(PlayerData, uuid + ".dat");
            RedisPipeForge.log.log(Level.INFO, f.toString());
            if (f.exists())
                oldnbt=CompressedStreamTools.func_74796_a(new FileInputStream(f)); 
            if(oldnbt!=null){
                nbt.func_74782_a("Pos",oldnbt.func_150295_c("Pos", 6));
                nbt.func_74757_a("Invulnerable", false);
                 if (nbt.func_74764_b("Dimension")&&oldnbt.func_74764_b("Dimension"))
                    nbt.func_74768_a("Dimension", oldnbt.func_74762_e("Dimension"));
                 if(oldnbt.func_74764_b("WorldUUIDMost") && oldnbt.func_74764_b("WorldUUIDLeast")){
                     nbt.func_74772_a("WorldUUIDLeast",oldnbt.func_74763_f("WorldUUIDLeast"));
                     nbt.func_74772_a("WorldUUIDMost",oldnbt.func_74763_f("WorldUUIDMost"));
                 }
                 if(oldnbt.func_150297_b("SpawnX", 99) && oldnbt.func_150297_b("SpawnY", 99) && oldnbt.func_150297_b("SpawnZ", 99)){
                     nbt.func_74768_a("SpawnX",oldnbt.func_74762_e("SpawnX"));
                     nbt.func_74768_a("SpawnY",oldnbt.func_74762_e("SpawnY"));
                     nbt.func_74768_a("SpawnZ",oldnbt.func_74762_e("SpawnZ"));
                     nbt.func_74757_a("SpawnForced",oldnbt.func_74767_n("SpawnForced"));
                 }
                 
                 nbt.func_74778_a("SpawnWorld", oldnbt.func_74779_i("SpawnWorld"));
                 //newsh.playercache.put(uuid, nbt);
                 try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
                CompressedStreamTools.func_74799_a(nbt, byteArrayOutputStream);
                data=byteArrayOutputStream.toByteArray();
                } catch (Exception|Error e) {
                    throw new Error("玩家通过NMS序列化发送错误！",e);
                }
            }
            else{
                //if(safeLocation==null){
                //    World w0=((World)getServer().getWorlds().get(0));
                //    safeLocation=w0.getSpawnLocation();
                //    safeLocation.setY(w0.getHighestBlockYAt(w0.getSpawnLocation()));
                //}
                //UUIDWorld=safeLocation.getWorld().getUID();
                nbt.func_74782_a("Pos",(NBTBase)func_70087_a(new double[] { x, y, z }));
                //nbt.func_74772_a("WorldUUIDLeast",UUIDWorld.getLeastSignificantBits());
                //nbt.func_74772_a("WorldUUIDMost",UUIDWorld.getMostSignificantBits());
                //newsh.playercache.put(uuid, nbt);
                if(saveFile){
                    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
                    CompressedStreamTools.func_74799_a(nbt, byteArrayOutputStream);
                    data=byteArrayOutputStream.toByteArray();
                    } catch (Exception|Error e) {
                        throw new Error("玩家通过NMS序列化发送错误！",e);
                    }
                }
            }
            //NBTTagCompound nbt=CompressedStreamTools.func_74796_a(byteArrayInputStream);
            //out.print("[RedisPipe] load\n"+nbt.toString());
            //CraftPlayer cp=(CraftPlayer)getServer().getOfflinePlayer(uuid).getPlayer();
            //EntityPlayer player=(cp).getHandle();
            //NBTTagCompound oldnbt=((SaveHandler)(((CraftServer)cp.getServer()).getHandle()).field_72412_k).getPlayerData(uuid);
            if(saveFile){
            out.print("[RedisPipe] save : "+uuid + ".dat");
            File file1 = new File(PlayerData, uuid + ".dat.tmp");
            File file2 = new File(PlayerData, uuid + ".dat");
            FileOutputStream fs=new FileOutputStream(file1);
            fs.write(data);
            fs.flush();
            fs.close();
            if (file2.exists())
              file2.delete(); 
            file1.renameTo(file2);
            }
        }catch (Exception|Error e) {
            RedisPipeForge.log.log(Level.WARNING,e.toString());
        }
    }

}
