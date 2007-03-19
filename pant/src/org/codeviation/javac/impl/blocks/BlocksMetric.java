package  org.codeviation.javac.impl.blocks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import org.codeviation.model.PositionInterval;
import org.codeviation.model.PositionIntervalResult;
import org.codeviation.model.VersionInterval;
import org.codeviation.model.VersionedMetric;
import org.codeviation.javac.UsageItem;


/**
 *
 * @author pzajac
 */
public class BlocksMetric extends VersionedMetric<BlocksItem> implements java.io.Serializable {
    private static final long serialVersionUID = 1;

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
        int size = ois.readInt();
        for (int i = 0 ; i < size ; i++) {
            PositionInterval pi = PositionInterval.read(ois);
            BlocksItem bi = BlocksItem.read(ois);
            VersionInterval vi = VersionInterval.read(ois);
            PositionIntervalResult<BlocksItem> pir = new PositionIntervalResult<BlocksItem>(pi,bi);
            getStorage().add(pir,vi);
        }
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {        
        Set<PositionIntervalResult<BlocksItem>> blocks = getStorage().getAllObjects();
        oos.writeInt(blocks.size());
        for (PositionIntervalResult<BlocksItem> block : blocks) {
            block.getInterval().write(oos);
            block.getObject().write(oos);
            VersionInterval vi = getStorage().get(block);
            vi.write(oos);
        }        
    }
}
