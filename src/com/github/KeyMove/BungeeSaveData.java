/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class BungeeSaveData implements Serializable{
        public String Host;
        public int Port;
        public BungeeSaveData(String host,int port){
            this.Host=host;
            this.Port=port;
        }
}
