package com.github.KeyMove.Tools;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
final public class 类解析器 {
    byte[] 魔数=new byte[]{(byte)0xca,(byte)0xfe,(byte)0xba,(byte)0xbe};
    byte[] 编译器版本=new byte[]{0,0,0,34};
    int 常量数量=0;
    List<Object> 常量池;
    int 访问权限;
    int 本类;//指向常数池
    int 超类;//指向常数池
    int 接口数量;
    List<Integer> 接口池;//指向常数
    int 字段数量;
    List<字段> 字段池;
    int 方法数量;
    List<方法> 方法池;
    int 属性数量;
    List<属性> 属性池;
    
    Map<String,Integer> UTF8常量表=new HashMap<>();
    
    public interface 接口访问类型{
        int ACC_PUBLIC=1;
        int ACC_FINAL=0x0010;
        int ACC_SUPER=0x0020;
        int ACC_INTERFACE=0x0200;
        int ACC_ABSTRACT=0x0400;
        int ACC_SYNTHETIC=0x1000;
        int ACC_ANNOTATION=0x2000;
        int ACC_ENUM=0x4000;
    }
    public interface 常量类型{
        int UTF8=1;
        int Integer=3;
        int Float=4;
        int Long=5;
        int Double=6;
        int Class=7;
        int String=8;
        int Fieldref=9;
        int Methodref=10;
        int InterfaceMethodref=11;
        int NameAndType=12;
    }
    public interface 静态属性列表{
        String Code="Code";
        String LineNumberTable="LineNumberTable";
        String LocalVariableTable="LocalVariableTable";
        String RuntimeVisibleAnnotations="RuntimeVisibleAnnotations";
        String StackMapTable="StackMapTable";
        String SourceFile="SourceFile";
        String Nop="Nop";
    }
    public boolean 解析文件(File f){
        InputStream fs = null;
        try {
            fs=new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return 解析(fs);
    }
    public boolean 解析(InputStream fs){
        
        魔数=readbyte(4, fs);
        编译器版本=readbyte(4, fs);
        读取常量表(fs);
        访问权限=readint(2, fs);
        本类=readint(2, fs);
        超类=readint(2, fs);
        接口数量=readint(2, fs);
        接口池=new ArrayList<>();
        for(int i=0;i<接口数量;i++){
            接口池.add(readint(2, fs));
        }
        字段数量=readint(2, fs);
        字段池=new ArrayList<>();
        for(int i=0;i<字段数量;i++){
            字段池.add(new 字段(fs));
        }
        方法数量=readint(2, fs);
        方法池=new ArrayList<>();
        for(int i=0;i<方法数量;i++){
            方法池.add(new 方法(fs));
        }
        属性数量=readint(2, fs);
        属性池=new ArrayList<>();
        for(int i=0;i<属性数量;i++){
            属性池.add(new 属性(fs));
        }
        return true;
    }
    public boolean 保存文件(File f){
        OutputStream fs;
        try {
            if(!f.exists())
            {
                String path=f.getPath();
                File dir=new File(path.substring(0, path.lastIndexOf('\\')));
                if(!dir.exists())
                    dir.mkdirs();
                f.createNewFile();
            }
            fs=new FileOutputStream(f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return 保存(fs);
    }
    public boolean 保存(OutputStream fs){
        writeByte(4, 魔数, fs);
        writeByte(4, 编译器版本, fs);
        保存常量表(fs);
        writeInt(2, 访问权限, fs);
        writeInt(2, 本类, fs);
        writeInt(2, 超类, fs);
        writeInt(2, 接口数量, fs);
        for(int i=0;i<接口数量;i++){
            writeInt(2, 接口池.get(i), fs);
        }
        writeInt(2, 字段数量, fs);
        for(int i=0;i<字段数量;i++){
            ((字段)字段池.get(i)).write(fs);
        }
        writeInt(2, 方法数量, fs);
        for(int i=0;i<方法数量;i++){
            ((方法)方法池.get(i)).write(fs);
        }
        writeInt(2, 属性数量, fs);
        for(int i=0;i<属性数量;i++){
            ((属性)属性池.get(i)).write(fs);
        }
        return true;
    }
    public String 获取类型名称(Type type){
        if(type==null)
            return "V";
        String s=type.getTypeName();
        switch(s){
            case "int":return "I";
            case "int[]":return "[I";
            case "byte":return"B";
            case "byte[]":return"[B";
            case "short":return "S";
            case "short[]":return "[S";
            case "double":return "D";
            case "double[]":return "[D";
            case "float":return "F";
            case "float[]":return "[F";        
                default:
                    if(s.indexOf('[')==-1)
                        return "L"+s+";";
                    else{
                        s=s.substring(0, s.indexOf('[')-1);
                        return "[L"+s+";";
                    }
        }
    }
    
    public List<String> 获取所有UTF8常量(){
        List<String> 常量列表=new ArrayList<>();
        for(String 常量值:UTF8常量表.keySet()){
            常量列表.add(常量值);
        }
        return  常量列表;
    }
    
    public int 选择或添加UTF8常量(String 常量名称){
        if(UTF8常量表.containsKey(常量名称)){
            return UTF8常量表.get(常量名称);
        }
        else
        {
            常量池.add(new UTF8常量(常量名称.length(), 常量名称));
            UTF8常量表.put(常量名称, 常量数量);
            常量数量++;
        }
        return 常量数量-1;
    }
    public boolean 替换指定UTF8常量(String 常量名称,String 新常量名称){
        if(UTF8常量表.containsKey(新常量名称))return false;
        if(UTF8常量表.containsKey(常量名称)){
            int id=UTF8常量表.get(常量名称);
            UTF8常量 UTF8值=(UTF8常量)常量池.get(id);
            UTF8值.字符串=新常量名称;
            UTF8值.字符串长度=新常量名称.length();
            常量池.set(id, UTF8值);
            UTF8常量表.put(新常量名称, UTF8常量表.get(常量名称));
            return true;
        }
        return false;
    }
    
    public String 获取UTF8常量(int 索引){
        if(常量池.size()<索引)return "";
        常量 c=(常量)常量池.get(索引);
        if(c.类型==常量类型.UTF8){
            return ((UTF8常量)c).字符串;
        }
        return null;
    }
    
    public int 添加常量(Object o){
        if(o instanceof 常量)
        {
            常量池.add(o);
            常量数量++;
            return 常量数量;
        }
        return -1;
    }
    public int 添加方法(方法 method){
        方法池.add(method);
        方法数量++;
        return 方法数量;
    }
    public 方法 获取方法(String 方法名称){
        if(!UTF8常量表.containsKey(方法名称))return null;
        int id=UTF8常量表.get(方法名称);
        for(方法 m:方法池){
            if(m.名称索引==id)
                return m;
        }
        return null;
    }
    
    public 方法[] 获取全部方法(){
        方法[] 方法列表=new 方法[方法数量];
        for(int i=0;i<方法池.size();i++){
            方法列表[i]=方法池.get(i);
        }
        return 方法列表;
    }
    
    
        public Object 从表中寻找属性(String 名称,List<Object> 属性值){
            for(Object o:属性值){
                if(o instanceof 属性类型){
                    if(((属性类型)o).type.equalsIgnoreCase(名称))
                        return o;
                }
            }
            return null;
        }
        public List<Object> 设置表中属性(String 名称,Object 属性,List<Object> 属性值){
            for(int i=0;i<属性值.size();i++){
                Object o=属性值.get(i);
                if(o instanceof 属性类型){
                    if(((属性类型)o).type.equalsIgnoreCase(名称))
                    {
                        属性值.set(i, 属性);
                        return 属性值;
                    }
                }
            }
            return null;
        }
        
    public class 常量{
        int 类型;
        public 常量(int type) {
            this.类型=type;
        }
        public void write(OutputStream fs){
            writeInt(1, 类型, fs);
        }
    }
    
    public class UTF8常量 extends 常量{
        int 字符串长度;
        String 字符串;
        public UTF8常量(InputStream fs) {
            super(常量类型.UTF8);
            this.字符串长度=readint(2, fs);
            this.字符串=readString(this.字符串长度, fs);
        }
        public UTF8常量(int len,String str) {
            super(常量类型.UTF8);
            this.字符串=str;
            this.字符串长度=len;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.字符串长度, fs);
            writeString(this.字符串长度, 字符串, fs);
        }
    }   
    
    public class 整型常量 extends 常量{
        int 值;
        public 整型常量(InputStream fs) {
            super(常量类型.Integer);
            this.值=readint(4, fs);
        }
        public 整型常量(int 值) {
            super(常量类型.Integer);
            this.值 = 值;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(4, this.值, fs);
        }
    }
    
    public class 浮点常量 extends 常量{
        float 值;
        public 浮点常量(InputStream fs) {
            super(常量类型.Float);
            this.值=readfloat(fs);
        }
        public 浮点常量(float 值) {
            super(常量类型.Float);
            this.值 = 值;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writefloat(4, this.值, fs);
        }
    }
    
    public class 长整型常量 extends 常量{
        long 值;
        public 长整型常量(InputStream fs) {
            super(常量类型.Long);
            this.值=readlong(8, fs);
        }
        public 长整型常量(long 值) {
            super(常量类型.Long);
            this.值 = 值;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeLong(8, this.值, fs);
        }
    }
    
    public class 双精度浮点常量 extends 常量{
        double 值;
        public 双精度浮点常量(InputStream fs) {
            super(常量类型.Double);
            this.值=readdouble(fs);
        }
        public 双精度浮点常量(double 值) {
            super(常量类型.Double);
            this.值 = 值;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeDouble(8, this.值, fs);
        }
    }
    
    public class 类常量 extends 常量{
        int 索引;
        public 类常量(InputStream fs) {
            super(常量类型.Class);
            this.索引=readint(2, fs);
        }
        public 类常量(int 索引) {
            super(常量类型.Class);
            this.索引 = 索引;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.索引, fs);
        }
    }
    
    public class 字符串常量 extends 常量{
        int 索引;
        public 字符串常量(InputStream fs) {
            super(常量类型.String);
            this.索引=readint(2, fs);
        }
        public 字符串常量(int 索引) {
            super(常量类型.String);
            this.索引 = 索引;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.索引, fs);
        }
    }
    
    public class 字段常量 extends 常量{
        int 类索引;
        int 名称和类型索引;
        public 字段常量(InputStream fs) {
            super(常量类型.Fieldref);
            this.类索引=readint(2, fs);
            this.名称和类型索引=readint(2, fs);
        }
        public 字段常量(int 类索引, int 名称和类型索引, int type) {
            super(type);
            this.类索引 = 类索引;
            this.名称和类型索引 = 名称和类型索引;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.类索引, fs);
            writeInt(2, this.名称和类型索引, fs);
        }
    }
    public class 方法常量 extends 常量{
        int 类索引;
        int 名称和类型索引;
        public 方法常量(InputStream fs) {
            super(常量类型.Methodref);
            this.类索引=readint(2, fs);
            this.名称和类型索引=readint(2, fs);
        }
        public 方法常量(int 类索引, int 名称和类型索引, int type) {
            super(常量类型.Methodref);
            this.类索引 = 类索引;
            this.名称和类型索引 = 名称和类型索引;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.类索引, fs);
            writeInt(2, this.名称和类型索引, fs);
        }
    }
    public class 接口方法常量 extends 常量{
        int 类索引;
        int 名称和类型索引;
        public 接口方法常量(InputStream fs) {
            super(常量类型.InterfaceMethodref);
            this.类索引=readint(2, fs);
            this.名称和类型索引=readint(2, fs);
        }
        public 接口方法常量(int 类索引, int 名称和类型索引, int type) {
            super(常量类型.InterfaceMethodref);
            this.类索引 = 类索引;
            this.名称和类型索引 = 名称和类型索引;
        }
        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.类索引, fs);
            writeInt(2, this.名称和类型索引, fs);
        }
    }
    public class 名称与类型常量 extends 常量{
        int 名称索引;
        int 类型索引;

        public 名称与类型常量(InputStream fs) {
            super(常量类型.NameAndType);
            this.名称索引=readint(2, fs);
            this.类型索引=readint(2, fs);
        }
        
        public 名称与类型常量(int 名称索引, int 类型索引) {
            super(常量类型.NameAndType);
            this.名称索引 = 名称索引;
            this.类型索引 = 类型索引;
        }

        @Override
        public void write(OutputStream fs) {
            super.write(fs); //To change body of generated methods, choose Tools | Templates.
            writeInt(2, this.名称索引, fs);
            writeInt(2, this.类型索引, fs);
        }
    }
    void 读取常量表(InputStream fs){
        常量数量=readint(2, fs);
        常量池=new ArrayList<>();
        常量池.add(常量数量);
        for(int i=0;i<常量数量-1;i++){
            int index=readint(1, fs);
            switch(index){
                case 常量类型.UTF8:常量池.add(new UTF8常量(fs));break;
                case 常量类型.Integer:常量池.add(new 整型常量(fs));break;
                case 常量类型.Float:常量池.add(new 浮点常量(fs));break;
                case 常量类型.Long:常量池.add(new 长整型常量(fs));break;
                case 常量类型.Double:常量池.add(new 双精度浮点常量(fs));break;
                case 常量类型.String:常量池.add(new 字符串常量(fs));break;
                case 常量类型.Class:常量池.add(new 类常量(fs));break;
                case 常量类型.Fieldref:常量池.add(new 字段常量(fs));break;
                case 常量类型.Methodref:常量池.add(new 方法常量(fs));break;
                case 常量类型.InterfaceMethodref:常量池.add(new 接口方法常量(fs));break;
                case 常量类型.NameAndType:常量池.add(new 名称与类型常量(fs));break;
            }
        }
        for(int i=0;i<常量数量-1;i++){
            Object o=常量池.get(i);
            if(o instanceof UTF8常量)
            {
                UTF8常量表.put(((UTF8常量)常量池.get(i)).字符串, i);
            }
        }
    }
    void 保存常量表(OutputStream fs){
        writeInt(2, 常量数量, fs);
        for(int i=1;i<常量数量;i++){
            常量 o=(常量) 常量池.get(i);
            o.write(fs);
        }
    }
    
    public class 字段{
        int 访问权限;
        int 名称索引;
        int 描述索引;
        int 属性数量;
        List<属性> 属性列表;
        public 字段(int 访问权限, int 名称索引, int 描述索引, int 属性数量, List<属性> s) {
            this.访问权限 = 访问权限;
            this.名称索引 = 名称索引;
            this.描述索引 = 描述索引;
            this.属性数量 = 属性数量;
            this.属性列表 = s;
        }
        public 字段(InputStream fs) {
            this.访问权限=readint(2, fs);
            this.名称索引=readint(2, fs);
            this.描述索引=readint(2, fs);
            this.属性数量=readint(2, fs);
            属性列表=new ArrayList<>();
            for(int i=0;i<this.属性数量;i++){
                属性列表.add(new 属性(fs));
            }
        }
        public void write(OutputStream fs){
            writeInt(2, this.访问权限, fs);
            writeInt(2, this.名称索引, fs);
            writeInt(2, this.描述索引, fs);
            writeInt(2, this.属性数量, fs);
            for(int i=0;i<this.属性数量;i++){
                属性列表.get(i).write(fs);
            }
        }
    }
    public class 方法{
        public int 访问权限;
        public int 名称索引;
        public int 描述索引;
        public int 属性数量;
        public List<属性> 属性列表;
        public 方法(int 访问权限, int 名称索引, int 描述索引, int 属性数量, List<属性> s) {
            this.访问权限 = 访问权限;
            this.名称索引 = 名称索引;
            this.描述索引 = 描述索引;
            this.属性数量 = 属性数量;
            this.属性列表 = s;
        }
        public 方法(InputStream fs) {
            this.访问权限=readint(2, fs);
            this.名称索引=readint(2, fs);
            this.描述索引=readint(2, fs);
            this.属性数量=readint(2, fs);
            属性列表=new ArrayList<>();
            for(int i=0;i<this.属性数量;i++){
                属性列表.add(new 属性(fs));
            }
        }
        public Object 寻找属性(String 属性名称){
            for(int i=0;i<属性数量;i++){
                List<Object> vx=属性列表.get(i).属性解析();
                if(vx!=null){
                    if(((属性类型)vx.get(0)).type.equalsIgnoreCase(属性名称))
                    return vx.get(0);
                }    
            }
            return null;
        }
        public boolean 设置属性(String 属性名称,Object 值){
            for(int i=0;i<属性数量;i++){
                属性 x=属性列表.get(i);
                if(值!=null)
                {
                    List<Object> v=new ArrayList<>();
                    v.add(值);
                    x.构建属性(v);
                    属性列表.set(i, x);
                    return true;
                }                        
            }
            return false;
        }
        public void write(OutputStream fs){
            writeInt(2, this.访问权限, fs);
            writeInt(2, this.名称索引, fs);
            writeInt(2, this.描述索引, fs);
            writeInt(2, this.属性数量, fs);
            for(int i=0;i<this.属性数量;i++){
                属性列表.get(i).write(fs);
            }
        }
        
        public void 设置方法名称(String s){
            this.名称索引=选择或添加UTF8常量(s);
        }
        
        public String 获取方法名称(){
            return 获取UTF8常量(this.名称索引);
        }
        
        public void 设置方法返回值与参数(Type type,Type... types){
            String output=")"+获取类型名称(type);
            String input="(";
            for(Type t:types)
                input+=获取类型名称(t);
            this.描述索引=选择或添加UTF8常量(input+output);
        }
        public void 设置方法返回值与参数(Type type,String... types){
            String output=")"+获取类型名称(type);
            String input="(";
            for(String t:types)
                input+="L"+t+";";
            this.描述索引=选择或添加UTF8常量(input+output);
        }
        
        public 方法 克隆(){
            List<属性> 新属性表=new ArrayList<>();
            for(属性 x:this.属性列表)
                新属性表.add(x.克隆());
            方法 v=new 方法(访问权限, 名称索引, 描述索引, 属性数量, 新属性表);
            return v;
        }
    }
    
    public class 属性{
        int 属性索引;
        int 属性长度;
        byte[] 属性;
        public 属性(InputStream fs) {
            this.属性索引=readint(2, fs);
            this.属性长度=readint(4, fs);
            this.属性=readbyte(this.属性长度, fs);
        }
        public 属性(BArray ar) {
            this.属性索引=ar.readint(2);
            this.属性长度=ar.readint(4);
            this.属性=ar.readbyte(this.属性长度);
        }
        public 属性(int 属性索引, int 属性长度, byte[] 属性) {
            this.属性索引 = 属性索引;
            this.属性长度 = 属性长度;
            this.属性 = 属性;
        }
        public List<Object> 属性解析(){
        List<Object> 属性值=new ArrayList<>();
        BArray arr=new BArray(属性);
        int count;
        switch(((UTF8常量)常量池.get(this.属性索引)).字符串){
            case 静态属性列表.Code:
                属性值.add(new 代码属性(arr));
                break;
            case 静态属性列表.LineNumberTable:
                count=arr.readint(2);
                while(count--!=0)
                属性值.add(new 行号属性(arr));
                break;
            case 静态属性列表.LocalVariableTable:
                count=arr.readint(2);
                while(count--!=0)
                属性值.add(new 局部变量属性(arr));
                break;
            case 静态属性列表.SourceFile:
                属性值.add(new 源文件(arr));
                break;
            case 静态属性列表.StackMapTable:
                属性值.add(new 栈堆表(属性));
                break;
            case 静态属性列表.RuntimeVisibleAnnotations:
                属性值.add(new 运行可见注释(属性));
                break;
            default:
                属性值.add(new 其他属性(属性));
                break;
        }
        return 属性值;
        }
        public boolean 构建属性(List<Object> 属性值){
            BArray ar=new BArray(10240);
            switch(((属性类型)属性值.get(0)).type){
                case 静态属性列表.LineNumberTable:
                    ar.writeint(2, 属性值.size());
                    break;
                case 静态属性列表.LocalVariableTable:
                    ar.writeint(2, 属性值.size());
                    break;
            }
            for(Object o:属性值){
                ((属性类型)o).write(ar);
            }
            this.属性=ar.getarray();
            return true;
        }
        public void write(OutputStream fs){
            writeInt(2, this.属性索引, fs);
            writeInt(4, this.属性.length, fs);
            writeByte(this.属性.length, this.属性, fs);
        }
        public void write(BArray ar){
            ar.writeint(2, this.属性索引);
            ar.writeint(4, this.属性.length);
            ar.writebyte(this.属性.length, this.属性);
        }
        public 属性 克隆(){
            byte[] 值=new byte[属性.length];
            System.arraycopy(this.属性, 0, 值, 0, this.属性.length);
            return new 属性(属性索引, 属性长度, 属性);
        }
    }
    public class 属性类型{
        String type;
        public 属性类型(java.lang.String type) {
            this.type = type;
        }
        public void write(BArray ar){
        }
    }
    
    public class 代码属性 extends 属性类型{
        public int 最大栈深度;
        public int 最大局部变量数量;
        public int 字节码数量;
        public byte[] 字节码;
        public int 异常表数量;
        public List<byte[]> 异常表;
        public int 属性数量;
        public List<属性> 属性;

        public 代码属性(int 最大栈深度, int 最大局部变量数量, int 字节码数量, byte[] 字节码, int 异常表数量, List<byte[]> 异常表, int 属性数量, List<属性> 属性) {
            super(静态属性列表.Code);
            this.最大栈深度 = 最大栈深度;
            this.最大局部变量数量 = 最大局部变量数量;
            this.字节码数量 = 字节码数量;
            this.字节码 = 字节码;
            this.异常表数量 = 异常表数量;
            this.异常表 = 异常表;
            this.属性数量 = 属性数量;
            this.属性 = 属性;
        }
        
        public 代码属性(BArray ar) {
            super(静态属性列表.Code);
            this.最大栈深度=ar.readint(2);
            this.最大局部变量数量=ar.readint(2);
            this.字节码数量=ar.readint(4);
            this.字节码=ar.readbyte(this.字节码数量);
            this.异常表数量=ar.readint(2);
            this.异常表=new ArrayList<>();
            for(int i=0;i<this.异常表数量;i++){
                this.异常表.add(ar.readbyte(8));
            }
            this.属性数量=ar.readint(2);
            this.属性=new ArrayList<>();
            for(int i=0;i<this.属性数量;i++){
                this.属性.add(new 属性(ar));
            }
        }
        @Override
        public void write(BArray ar){
            ar.writeint(2, this.最大栈深度);
            ar.writeint(2, this.最大局部变量数量);
            ar.writeint(4, this.字节码数量);
            ar.writebyte(this.字节码数量, this.字节码);
            ar.writeint(2, this.异常表数量);
            for(int i=0;i<this.异常表数量;i++){
                ar.writebyte(8, this.异常表.get(i));
            }
            ar.writeint(2, this.属性数量);
            for(int i=0;i<this.属性数量;i++){
                this.属性.get(i).write(ar);
            }
        }
        public List<Object> 寻找属性(String 属性名称){
            for(int i=0;i<属性数量;i++){
                List<Object> vx=属性.get(i).属性解析();
                if(vx!=null){
                    if(((属性类型)vx.get(0)).type.equalsIgnoreCase(属性名称))
                        return vx;
                }    
            }
            return null;
        }
        public boolean 设置属性(String 属性名称,List<Object> 值){
            for(int i=0;i<属性数量;i++){
                属性 x=属性.get(i);
                List<Object> vx=x.属性解析();
                if(((属性类型)vx.get(0)).type.equalsIgnoreCase(属性名称))
                if(值!=null)
                {
                    x.构建属性(值);
                    属性.set(i, x);
                    return true;
                }                        
            }
            return false;
        }
        public boolean 设置属性(String 属性名称,Object 值){
            List<Object> v=new ArrayList<>();
            v.add(值);
            return 设置属性(属性名称,v);
        }
        public 代码属性 克隆(){
            byte[] 新字节码=new byte[this.字节码.length];
            System.arraycopy(this.字节码, 0, 新字节码, 0, this.字节码.length);
            代码属性 v=new 代码属性(最大栈深度, 最大局部变量数量, 字节码数量, 新字节码, 异常表数量, 异常表, 属性数量, 属性);
            return v;
        }
    }
    
    public class 行号属性 extends 属性类型{
        public int start_pc;
        public int line_number;
        public 行号属性(BArray ar) {
            super(静态属性列表.LineNumberTable);
            start_pc=ar.readint(2);
            line_number=ar.readint(2);
        }

        @Override
        public void write(BArray ar) {
            ar.writeint(2, this.start_pc);
            ar.writeint(2, this.line_number);
        }
    }
    
    public class 局部变量属性 extends 属性类型{
        public int start_pc;
        public int 长度;
        public int 局部变量名称索引;
        public int 局部变量类型索引;
        public int 数组索引;
        public 局部变量属性(BArray ar) {
            super(静态属性列表.LocalVariableTable);
            this.start_pc=ar.readint(2);
            this.长度=ar.readint(2);
            this.局部变量名称索引=ar.readint(2);
            this.局部变量类型索引=ar.readint(2);
            this.数组索引=ar.readint(2);
        }
        public void 设置局部变量属性(String name,Type type){
            this.局部变量名称索引=选择或添加UTF8常量(name);
            this.局部变量类型索引=选择或添加UTF8常量(获取类型名称(type));
        }
        public void 设置局部变量属性(String name,String type){
            this.局部变量名称索引=选择或添加UTF8常量(name);
            this.局部变量类型索引=选择或添加UTF8常量("L"+type+";");
        }
        @Override
        public void write(BArray ar) {
            ar.writeint(2, this.start_pc);
            ar.writeint(2, this.长度);
            ar.writeint(2, this.局部变量名称索引);
            ar.writeint(2, this.局部变量类型索引);
            ar.writeint(2, this.数组索引);
        }
    }
    public class 源文件 extends 属性类型{
        public int 文件名索引;
        public 源文件(BArray ar) {
            super(静态属性列表.SourceFile);
            this.文件名索引=ar.readint(2);
        }
        @Override
        public void write(BArray ar) {
            ar.writeint(2, this.文件名索引);
        }
    }
    
    public class 栈堆表 extends 属性类型{
        public byte[] array;
        public 栈堆表(byte[] b) {
            super(静态属性列表.StackMapTable);
            array=b;
        }
        @Override
        public void write(BArray ar) {
            ar.writebyte(array.length, array);
        }
    }
    public class 运行可见注释 extends 属性类型{
        public int 描述索引;
        public int 数量;
        public byte[] array;
        public 运行可见注释(byte[] v) {
            super(静态属性列表.RuntimeVisibleAnnotations);
            array=v;
            BArray ar=new BArray(v);
            this.描述索引=ar.readint(2);
            this.数量=ar.readint(2);
            for(int i=0;i<this.数量;i++){
                
            }
        }

        @Override
        public void write(BArray ar) {
            ar.writebyte(array.length, array);
        }
        
    }
    public class 其他属性 extends 属性类型{
        public byte[] array;
        public 其他属性(byte[] b) {
            super(静态属性列表.Nop);
            array=b;
        }
        @Override
        public void write(BArray ar) {
            ar.writebyte(array.length, array);
        }
    }
    
    public class BArray{
        byte[] b;
        int pos;
        public BArray(byte[] b) {
            this.b=b;
            this.pos = 0;
        }
        public BArray(int len){
            this.b=new byte[len];
            this.pos=0;
        }
        public int readint(int len){
            int v=0;
            for(int i=0;i<len;i++){
                v<<=8;
                v|=b[pos];
                pos++;
            }
            return v;
        }
        public byte[] readbyte(int len){
            byte[] a=new byte[len];
            for(int i=0;i<len;i++){
                a[i]=b[pos];
                pos++;
            }
            return a;
        }
        public void writeint(int len,int val){
            int v=8*len;
            for(int i=0;i<len;i++){
                v-=8;
                b[pos]=(byte)(val>>v);
                pos++;
            }
        }
        public void writebyte(int len,byte[] a){
            for(int i=0;i<len;i++){
                b[pos]=a[i];
                pos++;
            }
        }
        public byte[] getarray(){
            byte[] a=new byte[pos];
            for(int i=0;i<pos;i++){
                a[i]=b[i];
            }
            return a;
        }
    }
    
    
    
    
    
    
    
    
    
    
        static byte[] readbyte(int len,InputStream fs){
            byte[] b=new byte[len];
            for(int i=0;i<len;i++)
                try {
                    b[i]=(byte) fs.read();
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            return b;
        }
        static int readint(int len,InputStream fs){
            int c=0;
            for(int i=0;i<len;i++)
                try {
                    c<<=8;
                    c|=((byte) fs.read())&0xff;
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            return c;
        }
        static long readlong(int len,InputStream fs){
            long c=0;
            for(int i=0;i<len;i++)
                try {
                    c<<=8;
                    c|=((byte) fs.read())&0xff;
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            return c;
        }
        static float readfloat(InputStream fs){
            byte[] c=new byte[4];
            for(int i=0;i<4;i++)
                try {
                    c[i]=(byte) fs.read();
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            return (float)((c[0]<<24)|(c[1]<<16)|(c[2]<<8)|(c[3]));
        }
        static double readdouble(InputStream fs){
            byte[] c=new byte[8];
            for(int i=0;i<8;i++)
                try {
                    c[i]=(byte) fs.read();
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            return (float)((c[0]<<56)|(c[1]<<48)|(c[2]<<40)|(c[3]<<32)|(c[4]<<24)|(c[5]<<16)|(c[6]<<8)|(c[7]));
        }
        static String readString(int len,InputStream fs){
            String c;
            byte[] b=new byte[len];
            for(int i=0;i<len;i++)
                try {
                    b[i]=(byte) fs.read();
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            c=new String(b);
            return c;
        }
        static void writeByte(int len,byte[] b,OutputStream fs){
                try {
                    fs.write(b, 0, len);
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        static void writeInt(int len,int val,OutputStream fs){
            byte[] b=new byte[len];
            int rl=len*8-8;
            for(int i=0;i<len;i++)
            {
                b[i]=(byte) (val>>rl);
                rl-=8;
            }
            try {
                    fs.write(b);
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        static void writeLong(int len,long val,OutputStream fs){
            byte[] b=new byte[len];
            int rl=len*8-8;
            for(int i=0;i<len;i++)
            {
                b[i]=(byte) (val>>rl);
                rl-=8;
            }
            try {
                    fs.write(b);
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        static void writeString(int len,String val,OutputStream fs){
            byte[] b=new byte[len];
            try {
                    fs.write(val.getBytes(),0,len);
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        static void writefloat(int len,float val,OutputStream fs){
            int v=Float.floatToIntBits(val);
            int rl=32;
            while(rl!=0){
            try {
                    rl-=8;
                    fs.write(((v>>rl)&0xff));
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        static void writeDouble(int len,double val,OutputStream fs){
            long v=Double.doubleToLongBits(val);
            int rl=64;
            while(rl!=0){
            try {
                    rl-=8;
                    fs.write((int) ((v>>rl)&0xff));
                } catch (IOException ex) {
                    Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        static public Class 动态加载类(File f,String Name){
        Class dc=null;
        try {
            URLClassLoader l=new URLClassLoader(new URL[]{f.toURI().toURL()});
            dc=l.loadClass(Name);
        } catch (IllegalArgumentException | ClassNotFoundException|SecurityException | MalformedURLException ex) {
            Logger.getLogger(类解析器.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return dc;
        }
        static public Class<?> 动态加载类(ClassLoader cl,byte[] bytecode){
        Class<?> classv=new ClassLoader(cl) {
            public Class<?> loadClass(byte [] code) {
                return super.defineClass(code, 0, code.length);
            }
        }.loadClass(bytecode);
        return classv;
        }
        class defLoader extends ClassLoader{
            public Class<?> Load(String name,byte[] bytecode){
                return defineClass(name, bytecode, 0, bytecode.length);
            }
        }
}
