
package org.codeviation.model;

import junit.framework.Assert;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author pzajac
 */
public class DefaultLookupUtil extends ProxyLookup{
    public static DefaultLookupUtil DEFAULT_LOOKUP = null;
    
    public DefaultLookupUtil() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
     static {
        DefaultLookupUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", DefaultLookupUtil.class.getName());
        Assert.assertEquals(DefaultLookupUtil.class, Lookup.getDefault().getClass());
    }

    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    public static void setLookup(Object[] instances) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances)
        });
    }

}
