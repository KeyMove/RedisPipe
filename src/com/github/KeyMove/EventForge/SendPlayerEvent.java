/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove.EventForge;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 *
 * @author KeyMove
 */
@Cancelable
public class SendPlayerEvent extends Event{
        EntityPlayerMP mp;
        String ServerName;
        public UUID uuid;
        boolean isSend;
        
        public SendPlayerEvent(UUID mp,String ServerName,boolean isSend){
            this.uuid=mp;
            this.ServerName=ServerName;
            this.isSend=isSend;
        }
        
        public SendPlayerEvent(EntityPlayerMP mp,String ServerName,boolean isSend){
            this.mp=mp;
            this.uuid=mp.func_110124_au();
            this.ServerName=ServerName;
            this.isSend=isSend;
        }
        
        public EntityPlayerMP getPlayer(){
            return mp;
        }
        
        public String getServerName(){
            return ServerName;
        }
        
        public boolean isSend(){
            return isSend;
        }
}
