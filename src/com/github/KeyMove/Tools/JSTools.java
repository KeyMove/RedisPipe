/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.KeyMove.Tools;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;


import javax.script.ScriptEngineManager;

/**
 *
 * @author KeyMove
 */
public class JSTools {
    static ScriptEngine engine;
    public static ScriptEngine getInstance(){
        //((Invocable)engine).invokeFunction(name, args)
        if(engine==null){
            engine = new ScriptEngineManager().getEngineByName("js");
            try{
                ScriptContext context = engine.getContext();
                for (String s : new String[] {  "load", "loadWithNewGlobal", "exit", "quit" })
                  context.removeAttribute(s, context.getAttributesScope(s)); 
            }
            catch (Exception e) {  
                System.out.print(e);
            }
        }
        return engine;
    }
}
