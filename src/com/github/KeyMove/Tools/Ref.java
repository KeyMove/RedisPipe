/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.KeyMove.Tools;

import static java.lang.System.out;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Ref {
    static Map<String,Class> g_Classmap=new HashMap<>();
    static Map<Class,Map<String,Method>> g_Methodmap=new HashMap<>();
    static Map<Class,Map<String,Field>> g_Fieldmap=new HashMap<>();
    static Map<Class,Map<Integer,Constructor>> g_ConstructorMap=new HashMap<>();
    static ClassLoader g_loader=Ref.class.getClassLoader();
    
    public static Class Class(String name){
        Class t=null;
        if(g_Classmap.containsKey(name)){
            return g_Classmap.get(name);
        }
        try {
            //t = Class.forName(name);
            t=Class.forName(name, true, g_loader);
            g_Classmap.put(name, t);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }
    
    public static Object getNewClass(String name){
        try {
            return Class(name).newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object New(Class obj,Object[] list){
        Class[] build;
        if(list.length==0){
            build=null;
        }
        else{
            build=new Class[list.length];
            for(int i=0;i<list.length;i++){
                build[i]=list[i].getClass();
            }
        }
        try {
            if(build==null)
                return obj.newInstance();
            return obj.getConstructor(build).newInstance(list);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object New(String name,Object[] list){
        Class[] build;
        Class obj=Class(name);
        if(obj==null)return null;
        if(list.length==0){
            build=null;
        }
        else{
            build=new Class[list.length];
            for(int i=0;i<list.length;i++){
                build[i]=list[i].getClass();
            }
        }
        try {
            if(build==null)
                return obj.newInstance();
            return obj.getConstructor(build).newInstance(list);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object ClassArray(String name,int len){
        Class s=Class(name);
        if(s!=null)
        {
            return java.lang.reflect.Array.newInstance(s, len);
        }
        return null;
    }
    
    public static Object ClassArray(Class s,int len){
        if(s!=null)
        {
            return java.lang.reflect.Array.newInstance(s, len);
        }
        return null;
    }
    
    public static Object getMembers(String name,Class c,Object o){
        Object oc=null;
        if(!g_Classmap.containsKey(c.getName()))
            c=Class(c.getName());
        Map<String,Field> p;
        Field f=null;
        if(!g_Fieldmap.containsKey(c)){
            p=new HashMap<>();
            g_Fieldmap.put(c, p);
        }
        else{
            p=g_Fieldmap.get(c);
        }
        try {
            f=c.getDeclaredField(name);
            f.setAccessible(true);
            p.put(name, f);
            oc=f.get(o);
        } catch (NoSuchFieldException | SecurityException |IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return oc;
    }
    
    public static Object getMembers(String name,Object o){
        Object oc=null;
        Class c=o.getClass();
        if(!g_Classmap.containsKey(c.getName()))
            c=Class(c.getName());
        Map<String,Field> p;
        Field f=null;
        if(!g_Fieldmap.containsKey(c)){
            p=new HashMap<>();
            g_Fieldmap.put(c, p);
        }
        else{
            p=g_Fieldmap.get(c);
        }
        try {
            f=c.getDeclaredField(name);
            f.setAccessible(true);
            p.put(name, f);
            oc=f.get(o);
        } catch (NoSuchFieldException | SecurityException |IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return oc;
    }
    
    public static void setMembers(String name,Class c,Object o,Object in){
        if(!g_Classmap.containsKey(c.getName()))
            c=Class(c.getName());
        Map<String,Field> p;
        Field f=null;
        if(!g_Fieldmap.containsKey(c)){
            p=new HashMap<>();
            g_Fieldmap.put(c, p);
        }
        else{
            p=g_Fieldmap.get(c);
        }
        try {
            f=c.getDeclaredField(name);
            f.setAccessible(true);
            p.put(name, f);
            f.set(o,in);
        } catch (NoSuchFieldException | SecurityException |IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void setMembers(String name,Object o,Object in){
        Class c=o.getClass();
        if(!g_Classmap.containsKey(c.getName()))
            c=Class(c.getName());
        Map<String,Field> p;
        Field f=null;
        if(!g_Fieldmap.containsKey(c)){
            p=new HashMap<>();
            g_Fieldmap.put(c, p);
        }
        else{
            p=g_Fieldmap.get(c);
        }
        try {
            f=c.getDeclaredField(name);
            f.setAccessible(true);
            p.put(name, f);
            f.set(o,in);
        } catch (NoSuchFieldException | SecurityException |IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Constructor getConstructor(String name,int id,Object[] type){
        Class c=Class(name);
        Map<Integer,Constructor> p=null;
        Class[] list=null;
        if(type.length!=0)
        {
            list=new Class[type.length];
            for(int i=0;i<type.length;i++)
            {
                list[i]=type[i].getClass();
                if(list[i]==Integer.class)
                {
                    list[i]=int.class;
                    continue;
                }
                if(list[i]==Double.class)
                {
                    list[i]=double.class;
                    continue;
                }
                if(list[i]==Boolean.class)
                {
                    list[i]=boolean.class;
                }
                if(list[i]==Class.class)
                    list[i]=(Class) type[i];
            }
        }
        if(g_ConstructorMap.containsKey(c))
        {
            return g_ConstructorMap.get(c).get(id);
        }
        else{
            p=new HashMap<>();
            g_ConstructorMap.put(c, p);
        }
        Constructor ct=null;
        try {
            ct=c.getConstructor(list);
            p.put(id, ct);
            return ct;
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object NewClass(String name,int id,Object[] type){
        Class c=Class(name);
        Constructor ct=getConstructor(name,id, type);
        try {
            //for(Object o:type)
              //  out.print(o+"-"+o.getClass());
            if(ct!=null)
                return ct.newInstance(type);
        } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Object Invoke(String name,Object o,Object... arg){
        Class c=o.getClass();
        Class[] tp;
        Object oc=null;
        if(!g_Classmap.containsKey(c.getName()))
            c=Class(c.getName());
        Map<String,Method> p;
        Method f=null;
        if(!g_Methodmap.containsKey(c)){
            p=new HashMap<>();
            g_Methodmap.put(c, p);
        }
        else{
            p=g_Methodmap.get(c);
        }
        if(p.containsKey(name)){
            try {
                if(arg!=null)
                    oc=p.get(name).invoke(o, arg);
                else
                    oc=p.get(name).invoke(o, (Object[])null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                if(arg!=null)
                {
                    tp=new Class[arg.length];
                    for(int i=0;i<arg.length;i++){
                        tp[i]=arg[i].getClass();
                    }
                    f=c.getDeclaredMethod(name,tp);
                }
                else
                    f=c.getDeclaredMethod(name);
                f.setAccessible(true);
                p.put(name, f);
                if(arg!=null)
                    oc=p.get(name).invoke(o, arg);
                else
                    oc=p.get(name).invoke(o, (Object[])null);
            } catch (SecurityException |IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return oc;
    }
    public static Class<?> type[]={short.class,int.class,long.class,double.class,float.class};
    public Class getType(String Name){
        switch(Format.valueOf(Name)){
            case Object:return Object.class;
                case Double:return double.class;
                case Float:return float.class;
                case Short:return short.class;
                case Byte:return byte.class;
                case Char:return char.class;
                case Long:return long.class;
                case Int:return int.class;
                case Bool:return boolean.class;
        }
        return null;
    }
    public static void test(){
        out.print(int.class);
        out.print(double.class);
        out.print(float.class);
        out.print(long.class);
    }
    
    public static int[] toIntArray(Object[] v){
        if(v.length==0)
            return (int[])null;
        int[] iv=new int[v.length];
        for(int i=0;i<v.length;i++)
            iv[i]=(int)v[i];
        return iv;
    }
    
    public static String[] toStringArray(Object[] v){
        if(v.length==0)
            return (String[])null;
        String[] iv=new String[v.length];
        for(int i=0;i<v.length;i++)
            iv[i]=(String)v[i];
        return iv;
    }
    
    public enum Format{
        Object,
        Double,
        Float,
        Short,
        Byte,
        Char,
        Long,
        Int,
        Bool,
    }
    
    public static Object[] Objects(Object[] list){
        Object[] out=null;
        Format type=Format.Object;
        if(list.length!=0)
            out=new Object[list.length];
        int count=0;
        for(Object obj:list){
            if(obj instanceof Format){
                type=(Format)obj;
                continue;
            }
            switch(type){
                case Object:out[count++]=obj;break;
                case Double:out[count++]=(double)obj;break;
                case Float:out[count++]=(float)obj;break;
                case Short:out[count++]=(short)obj;break;
                case Byte:out[count++]=(byte)obj;break;
                case Char:out[count++]=(char)obj;break;
                case Long:out[count++]=(long)obj;break;
                case Int:out[count++]=(int)obj;break;
                case Bool:out[count++]=(boolean)obj;break;
            }
        }
        return out;
    }
    
    @Deprecated
    public interface Data{
        int Object=-1;
        int Double=0;
        int Short=1;
        int Byte=2;
        int Char=3;
        int Long=4;
        int Int=5;
        int Bool=6;
    }
    
    @Deprecated
    public static Object[] DataVal(Object[] arg){
        Object[] obj=null;
        int len=arg.length&~1;
        if(len<2)
            return obj;
        obj=new Object[len>>1];
        int count=0;
        for(int i=0;i<len;i++){
            switch((int)arg[i])
            {
                case 0:
                    Object c=null;
                    i++;
                    if(arg[i].getClass()==Double.class)
                    {
                        float fv=(float)((double)arg[i]);
                        obj[count]=fv;
                    }
                    else
                    {
                        obj[count]=Float.parseFloat(arg[i].toString());
                    }
                    count++;
                    break;
                case 1:
                    i++;
                    int si=(int)arg[i];
                    obj[count]=(short)si;
                    count++;
                    break;
                case 2:
                    i++;
                    int bv=(int)arg[i];
                    obj[count]=(byte)bv;
                    count++;
                    break;
                case 3:
                    i++;
                    obj[count]=(char)arg[i];
                    count++;
                    break;
                case 4:
                    i++;
                    obj[count]=(long)arg[i];
                    count++;
                    break;
                case 5:
                    i++;
                    obj[count]=(int)arg[i];
                    count++;
                    break;
                case 6:
                    i++;
                    obj[count]=(boolean)arg[i];
                    count++;
                    break;
                default:
                    i++;
                    obj[count]=arg[i];
                    count++;
                    break;
            }
        }
        //for(Object o:obj)
          //  out.print(o+"-"+o.getClass());
        return obj;
    }
    
    public static Object Invoke(String name,Object o,Class[] type,Object[] arg){
        Class c=o.getClass();
        Class[] tp;
        Object oc=null;
        if(!g_Classmap.containsKey(c.getName()))
            c=Class(c.getName());
        Map<String,Method> p;
        Method f=null;
        if(!g_Methodmap.containsKey(c)){
            p=new HashMap<>();
            g_Methodmap.put(c, p);
        }
        else{
            p=g_Methodmap.get(c);
        }
        if(p.containsKey(name)){
            try {
                if(arg!=null)
                    oc=p.get(name).invoke(o, arg);
                else
                    oc=p.get(name).invoke(o, (Object[])null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                if(arg!=null)
                {
                    f=c.getDeclaredMethod(name,type);
                }
                else
                    f=c.getDeclaredMethod(name);
                f.setAccessible(true);
                p.put(name, f);
                if(arg!=null)
                    oc=p.get(name).invoke(o, arg);
                else
                    oc=p.get(name).invoke(o, (Object[])null);
            } catch (SecurityException |IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                Logger.getLogger(Ref.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return oc;
    }
    
    public static Field[] Fields(Class s){
        Field[] list=s.getDeclaredFields();
        return list;
    }
    
    public static Field[] Fields(String classname){
        return Fields(Class(classname));
    }
    
    public static Method[] Methods(Class s){
        Method[] list=s.getDeclaredMethods();
        return list;
    }
    
    public static Method[] Methods(String classname){
       return Methods(Class(classname));
    }
    
    public static Constructor[] Constructors(Class s){
        Constructor[] list=s.getDeclaredConstructors();
        return list;
    }
    
    public static Constructor[] Constructors(String classname){
        return Constructors(Class(classname));
    }
    
    public static Object Invoke(Method m,Object obj,Object[] list){
        try {
            return m.invoke(obj, list);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            out.print(ex);
        }
        return null;
    }
    
    public static Object ClassLoader(Object obj){
        return obj.getClass().getClassLoader();
    }
    
    public static void SetClassLoader(Object obj){
        if(obj instanceof ClassLoader){
            g_loader=(java.lang.ClassLoader) obj;
        }
    }
    
    
    public static Class SuperClass(Class class1){
        return class1.getSuperclass();
    }
    
    public static Field getFieldWithString(Object obj,String name){
        for(Field fd: obj.getClass().getDeclaredFields()){
            if(fd.getType().getName().contains(name)){
                return fd;
            }
        }
        return null;
    }
    public static Field getFieldWithString(Class c,String name){
        for(Field fd: c.getDeclaredFields()){
            if(fd.getType().getName().contains(name)){
                return fd;
            }
        }
        return null;
    }
    
    public static Object[] getMethodsWithString(Class c,String ret,String[] a){
        List<Method> mlist=new ArrayList<>();
        for(Method m:c.getDeclaredMethods()){
            if(ret!=null)
            if(!m.getGenericReturnType().getTypeName().contains(ret)){
                continue;
            }
            Class[] types=m.getParameterTypes();
            if(a.length!=types.length)continue;
            boolean cont=false;
            for(int i=0;i<a.length;i++){
                if(!types[i].getTypeName().contains(a[i]))
                {
                    cont=true;
                    break;
                }
            }
            if(cont)continue;
            mlist.add(m);
        }
        return mlist.toArray();
    }
    
}
