
package org.codeviation.statistics;

import java.util.HashMap;
import java.util.Map;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

/**
 *
 * @author pzajac
 */
public final class Record {
 Vector values;
 Map<String,Object> params;        
 public int getDimensions() {
      return (values == null) ? 0 : values.size();
  }
  
 public Object getParam(String value) {
     return (params == null) ? null : params.get(value);
 }
 
 public void putParam(String name,Object value) {
     if (params == null) {
         params = new HashMap<String,Object>();
     }
     params.put(name, value);
 }
  public void add (Vector val) {
      if (values == null) {
          values = new DenseVector(val);
      } else {
          values.add(val);
      }
      
  }
  /** @return computed value for dimension index
   */ 
  public float getValue (int index)  {
      return (float) ((values == null) ? 0
                          : values.get(index)); 
  }
  public Vector getValues() {
      return values;
  }
  public void setValues(Vector vec) {
      values = new DenseVector(vec);
  }
  
}
