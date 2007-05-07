package org.codeviation.javac;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class UsageItem  {
    private String method;
    private String clazz;
    
    public UsageItem(String method, String clazz) {
        if (method != null) {
            // extends or implements
            this.method = method.intern();
        }
        this.clazz = clazz.intern();
    }
    public String getMethod() {
        return method;
    }
    public String getClazz() {
        return clazz;
    }
    public String toString() {
        return clazz + "." + method; 
    }
    
    /** XXX computes package from class name
     */
    public String getPackage() {
        //default 
       String pack = "" ;  
       int index = -1;
       int prevIndex = -1;
       index = clazz.length() - 1;
       while ((index = clazz.lastIndexOf('.', index)) != -1) {
           if (index < clazz.length() && Character.isLowerCase(clazz.charAt(index + 1))) {
               break;
           }
           prevIndex = index;
           index--;
       }
       if (prevIndex != -1) {
            pack = clazz.substring(0,prevIndex);
       }
       return pack;
    }
    public boolean equals(Object object) {
        if (object instanceof UsageItem) {
            UsageItem u = (UsageItem)object;
            boolean bClass = getClazz().equals(u.getClazz());
            if (bClass) {
                String m1 = getMethod();
                String m2 = u.getMethod();
                if (m1 != null ) {
                    return  m1.equals(m2);
                } else {
                    return m2 == null;
                } 
            }
        }
        return false;
    }
    
    public  static UsageItem read(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        return new UsageItem((String)ois.readObject(),(String)ois.readObject());
    }
    
    public  void write(ObjectOutputStream oos) throws IOException {
        oos.writeObject(method);
        oos.writeObject(clazz);
    }
    public int hashCode() {
        int hash = getClazz().hashCode();
        if (method != null) {
            hash+= (getMethod().hashCode() >> 7); 
        }
        return hash ;
    }
       
}