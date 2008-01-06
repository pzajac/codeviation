
package org.codeviation.model;

import java.io.File;

public class PersistenceManagerBridge {

     public static Repository getOrCreateRepository(PersistenceManager pm,File root,String name) {
         return pm.getOrCreateRepository(root, name);
     }
   
}
