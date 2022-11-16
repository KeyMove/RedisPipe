/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove.Tools;
import javax.script.Invocable;
import javax.script.ScriptContext;

import javax.script.ScriptEngine;

import javax.script.ScriptEngineFactory;

import javax.script.ScriptEngineManager;

import javax.script.ScriptException;
/**
 *
 * @author KeyMove
 */
public class JSTools {
    static ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
    public static ScriptEngine getInstance(){
        //((Invocable)engine).invokeFunction(name, args)
        return engine;
    }
}
