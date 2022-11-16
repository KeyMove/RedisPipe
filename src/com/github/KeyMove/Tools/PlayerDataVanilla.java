/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove.Tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import static java.lang.System.out;
import java.util.UUID;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagList;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.Location;
import org.bukkit.World;
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
    
    Location safeLocation;
    
    @Override
    public void setSpawn(UUID uuid,int x,int y,int z){
        safeLocation=new Location(getServer().getWorld(uuid),x,y,z);
    }
    
     @Override
        public byte[] save(UUID p) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        ((EntityPlayer)((CraftPlayer)getServer().getPlayer(p)).getHandle()).save(nbttagcompound);
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            NBTCompressedStreamTools.a(nbttagcompound, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception|Error e) {
            throw new Error("玩家通过NMS序列化发送错误！",e);
        }
    }
        
        @Override
    public void load(byte[] bytes,UUID p) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            ((CraftPlayer)getServer().getPlayer(p)).getHandle().f(NBTCompressedStreamTools.a(byteArrayInputStream));
        }catch (Exception|Error e) {
            throw new Error("玩家通过NMS返序列化发送错误！",e);
        }
    }
    protected NBTTagList a(double... adouble) {
    NBTTagList nbttaglist = new NBTTagList();
    double[] adouble1 = adouble;
    int i = adouble.length;
    for (int j = 0; j < i; j++) {
      double d0 = adouble1[j];
      nbttaglist.add(new NBTTagDouble(d0));
    } 
    return nbttaglist;
  }
    @Override
    public void saveUUID(UUID uuid,byte[] data){
        try{
            out.print("[RedisPipe] save UUID: "+uuid);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            NBTTagCompound nbt=NBTCompressedStreamTools.a(byteArrayInputStream);
            NBTTagCompound oldnbt=null;
            File f = new File("./", "world/playerdata/"+uuid + ".dat");
            //out.print("[RedisPipe] save: "+f);
            if (f.exists())
                oldnbt=NBTCompressedStreamTools.a(new FileInputStream(f)); 
            if(oldnbt!=null){
                nbt.set("Pos",oldnbt.getList("Pos", 6));
                nbt.setBoolean("Invulnerable", false);
                 if (nbt.hasKey("Dimension")&&oldnbt.hasKey("Dimension"))
                    nbt.setInt("Dimension", oldnbt.getInt("Dimension"));
                 if(oldnbt.hasKey("WorldUUIDMost") && oldnbt.hasKey("WorldUUIDLeast")){
                     nbt.setLong("WorldUUIDLeast",oldnbt.getLong("WorldUUIDLeast"));
                     nbt.setLong("WorldUUIDMost",oldnbt.getLong("WorldUUIDMost"));
                 }
                 if(oldnbt.hasKeyOfType("SpawnX", 99) && oldnbt.hasKeyOfType("SpawnY", 99) && oldnbt.hasKeyOfType("SpawnZ", 99)){
                     nbt.setInt("SpawnX",oldnbt.getInt("SpawnX"));
                     nbt.setInt("SpawnY",oldnbt.getInt("SpawnY"));
                     nbt.setInt("SpawnZ",oldnbt.getInt("SpawnZ"));
                     nbt.setBoolean("SpawnForced",oldnbt.getBoolean("SpawnForced"));
                 }
                 nbt.setString("SpawnWorld", oldnbt.getString("SpawnWorld"));
                 //newsh.playercache.put(uuid, nbt);
                 try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
                NBTCompressedStreamTools.a(nbt, byteArrayOutputStream);
                data=byteArrayOutputStream.toByteArray();
                } catch (Exception|Error e) {
                    throw new Error("玩家通过NMS序列化发送错误！",e);
                }
            }
            else{
                if(safeLocation==null){
                    World w0=((World)getServer().getWorlds().get(0));
                    safeLocation=w0.getSpawnLocation();
                    safeLocation.setY(w0.getHighestBlockYAt(w0.getSpawnLocation()));
                }
                UUIDWorld=safeLocation.getWorld().getUID();
                nbt.set("Pos",(NBTBase)a(new double[] { safeLocation.getX(), safeLocation.getY(), safeLocation.getZ() }));
                nbt.setLong("WorldUUIDLeast",UUIDWorld.getLeastSignificantBits());
                nbt.setLong("WorldUUIDMost",UUIDWorld.getMostSignificantBits());
                //newsh.playercache.put(uuid, nbt);
                if(saveFile){
                    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
                    NBTCompressedStreamTools.a(nbt, byteArrayOutputStream);
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
            //out.print("[RedisPipe] save : "+uuid + ".dat");
            File file1 = new File("./","world/playerdata/"+uuid+ ".dat.tmp");
            File file2 = new File("./", "world/playerdata/"+uuid + ".dat");
            FileOutputStream fs=new FileOutputStream(file1);
            fs.write(data);
            fs.flush();
            fs.close();
            if (file2.exists())
              file2.delete(); 
            file1.renameTo(file2);
            }
        }catch (Exception|Error e) {
            throw new Error("同步玩家通错误！",e);
        }
    }
    
}
