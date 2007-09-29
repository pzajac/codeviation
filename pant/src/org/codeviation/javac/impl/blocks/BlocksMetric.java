package  org.codeviation.javac.impl.blocks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumSet;
import java.util.Set;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.PositionVersionIntervalResultContainer;
import org.codeviation.model.Version;
import org.codeviation.model.VersionInterval;
import org.codeviation.model.VersionedMetric;
import org.codeviation.model.VersionedVector;


/**
 * This metrics persists versions of blocks
 * @author pzajac 
 */
public class BlocksMetric extends VersionedMetric<BlocksItem> implements java.io.Serializable,VersionedVector {
   private static final long serialVersionUID = 1;
   private PositionVersionIntervalResultContainer<String> classes = new PositionVersionIntervalResultContainer<String>();   
   private PositionVersionIntervalResultContainer<String> methods = new PositionVersionIntervalResultContainer<String>();   


   public void addClass(PositionIntervalResult<String> pir,Version v) {
       classes.add(pir, v);
   }
   public void addMethod(PositionIntervalResult<String> pir,Version v) {
       methods.add(pir, v);
   }

   public PositionVersionIntervalResultContainer<String> getClasses() {
       return classes;
   }
   public PositionVersionIntervalResultContainer<String> getMethods() {
       return methods;
   }

   public PositionVersionIntervalResultContainer<BlocksItem> filter(EnumSet<BlocksItem> enums) {
       PositionVersionIntervalResultContainer<BlocksItem> filtered = new PositionVersionIntervalResultContainer<BlocksItem>();
       for (PositionIntervalResult<BlocksItem> pir : getStorage().getAllObjects()) {
           if (enums.contains(pir.getObject())) {
                filtered.add(pir, getStorage().get(pir));
           }
       }
       return filtered;
   }
    /** Creates a new instance of BlocksMetric */
    public BlocksMetric() {
    }
    
    public String getName() {
        return "BlocksMetric";
    }

    public String getDescription() {
        return "Measures positions of all blocks for java file";
    }

    public boolean isPersistent() {
        return true;
    }

    public boolean isValid() {
        return true;
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // read blocks
        int size = ois.readInt();
        for (int i = 0 ; i < size ; i++) {
            PositionInterval pi = PositionInterval.read(ois);
            BlocksItem bi = BlocksItem.read(ois);
            VersionInterval vi = VersionInterval.read(ois);
            PositionIntervalResult<BlocksItem> pir = new PositionIntervalResult<BlocksItem>(pi,bi);
            getStorage().add(pir,vi);
        }
        // read classes 
        size = ois.readInt();
        classes = new PositionVersionIntervalResultContainer<String>();
        for (int i = 0 ; i < size ; i++) {
            PositionInterval pi = PositionInterval.read(ois);
            String name = (String) ois.readObject();
            VersionInterval vi = VersionInterval.read(ois);
            PositionIntervalResult<String> pir = new PositionIntervalResult<String>(pi,name);
            classes.add(pir, vi);
        }
        // read methods
        size = ois.readInt();
        methods = new PositionVersionIntervalResultContainer<String>();
        for (int i = 0 ; i < size ; i++) {
            PositionInterval pi = PositionInterval.read(ois);
            String name = (String) ois.readObject();
            VersionInterval vi = VersionInterval.read(ois);
            PositionIntervalResult<String> pir = new PositionIntervalResult<String>(pi,name);
            methods.add(pir, vi);
        }
        
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // blocks
        Set<PositionIntervalResult<BlocksItem>> blocks = getStorage().getAllObjects();
        oos.writeInt(blocks.size());
        for (PositionIntervalResult<BlocksItem> block : blocks) {
            block.getInterval().write(oos);
            block.getObject().write(oos);
            VersionInterval vi = getStorage().get(block);
            vi.write(oos);
        }
        // classes
        Set<PositionIntervalResult<String>> cpirs = classes.getAllObjects();
        oos.writeInt(cpirs.size());
        for (PositionIntervalResult<String> pir : cpirs) {
            pir.getInterval().write(oos);
            
            oos.writeObject(pir.getObject());
            VersionInterval vi = classes.get(pir);
            vi.write(oos);
        }
        
        // methods
        Set<PositionIntervalResult<String>> mpirs = methods.getAllObjects();
        oos.writeInt(mpirs.size());
        for (PositionIntervalResult<String> pir : mpirs) {
            pir.getInterval().write(oos);
            
            oos.writeObject(pir.getObject());
            VersionInterval vi = methods.get(pir);
            vi.write(oos);
        }
    }

    public Vector getVector(Version vers) {
        Vector vec = new DenseVector (BlocksItem.values().length);
        
        Set<PositionIntervalResult<BlocksItem>> bis = getStorage().getAllObjects();
        for (PositionIntervalResult<BlocksItem> pir : bis ) {
            VersionInterval vi = getStorage().get(pir);
            if (vi.contains(vers)) {
                int i = pir.getObject().getIndex();
                vec.add(i, 1);
            }
        }
        return vec;
    }

    public Set<Version> getVersions() {
       return classes.getAllVersion();
    }
}
