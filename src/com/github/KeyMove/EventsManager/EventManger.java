/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.EventsManager;

import static java.lang.System.out;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.nashorn.api.scripting.JSObject;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Administrator
 */

public class EventManger implements Listener{
    static Map<String,Integer> g_EventsMap=new HashMap<>();
    static List<JSObject>[] EventList;
    static int Count=0;
    static Plugin g_Plugin;
    public static Map<String,Integer> GetMap(){
            return g_EventsMap;
    }
    
    public void ClearEvent(){
        for(List<JSObject> lf:EventList){
            lf.clear();
        }
    }
    
    public void CallEvent(int EventId,Object event){
        if(EventId>Count)return;
        List<JSObject> list=EventList[EventId];
        if(event instanceof Cancellable){
            Cancellable cancellable=(Cancellable) event;
            for(JSObject function:list){
                try{
                    function.call(this,event);
                }
                catch(Throwable ex){
                    out.print(ex);
                }
                if(cancellable.isCancelled())return;
            }
        }
        else{
            for(JSObject function:list){
                try{
                    function.call(this,event);
                }
                catch(Throwable ex){
                    out.print(ex);
                }
            }
        }
    }
    
    public void CallEvent(Object e){
        if(e instanceof Event){
            if(g_Plugin!=null)
                out.print(g_Plugin.getName()+" Event!");
            String EventName=((Event)e).getEventName();
            if(g_EventsMap.containsKey(EventName))
            {
                out.print("New Event:"+EventName+" ID:"+Count++);
            }
            else{
                out.print("Event:"+EventName+"ID:"+g_EventsMap.get(EventName));
            }
        }
    }
    
    public void RegisterEvent(String EventName,JSObject value){
        int id;
        if(g_EventsMap.containsKey(EventName)){
            id=g_EventsMap.get(EventName);
            EventList[id].add(value);
        }
    }
    
    public void Setup(Plugin p){
        g_Plugin=p;
        for(Method m:this.getClass().getDeclaredMethods())
        {
            String name=m.getName();
            String eventname;
            if(name.indexOf('A')==0)
            {
                int id=Integer.parseInt(name.substring(1));
                eventname=m.getGenericParameterTypes()[0].getTypeName();
                eventname=eventname.substring(eventname.lastIndexOf('.')+1);
                if(!g_EventsMap.containsKey(eventname))
                {
                    g_EventsMap.put(eventname, id);
                    if(Count<id)Count=id;
                }
            }
        }
        EventList=new ArrayList[Count+1];
        for(int i=0;i<Count+1;i++){
            EventList[i]=new ArrayList<>();
        }
        out.print("加载了"+(Count+1)+"个事件!");
    }
}
