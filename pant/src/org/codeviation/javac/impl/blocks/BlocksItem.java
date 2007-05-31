/*
 * BlocksItem.java
 *
 * Created on March 14, 2007, 8:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.javac.impl.blocks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.codeviation.statistics.RecordType;

/**
 *A block item. Don't remove or change order of items. Because it
 * breaks persistence.
 * @author pzajac
 */
public enum BlocksItem {
    BLOCK,
    METHOD,
    FOR_LOOP,
    WHILE_LOOP,
    DO_WHILE_LOOP,
    SWITCH,
    CLASS,
    TRY,
    CATCH,
    IF,
    ELSE,
    CASE;
    
    public static BlocksItem read(ObjectInputStream ois) throws IOException {
        byte index = ois.readByte();
        return BlocksItem.values()[index];
        
    }
    
    public int getIndex() {
        BlocksItem vals[] = BlocksItem.values();
        for (int i = 0 ; i <vals.length; i++) {
            if (vals[i] == this) {
                return i;
            }
        }
        return -1;
    }
    public void write(ObjectOutputStream oos) throws IOException {
        BlocksItem vals[] = BlocksItem.values();
        for (int i = 0 ; i < vals.length ;i++) {
            if (vals[i] == this) {
                oos.writeByte((byte)i);
                break;
            }
        }
    }

    public RecordType getRecordType() {
        return getRecordTypes()[getIndex()];
    }
    private static RecordType records[];
    
    public static RecordType[] getRecordTypes() {
       if (records == null) {
         records = new RecordType[BlocksItem.values().length];
         for (int i = 0 ; i < records.length ; i++) {
            records[i] = new RecordType(BlocksItem.values()[i].toString(),i,false);
         }
       }
       return records;
    }
            
}
