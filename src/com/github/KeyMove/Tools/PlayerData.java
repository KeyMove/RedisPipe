/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import java.io.Serializable;

/**
 *
 * @author Administrator
 */
public class PlayerData implements Serializable{
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
}
