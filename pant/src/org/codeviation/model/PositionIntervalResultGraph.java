/*
 * PositionIntervalResultGraph.java
 * 
 * Created on Sep 28, 2007, 5:24:54 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codeviation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *  Create sorted hierchy for positions intervals
 * (PositionIntervalResult). Tested in BlockMetricsTest.
 * @author pzajac
 */
public final class PositionIntervalResultGraph {
    List<List<Item>> categories = new ArrayList<List<Item>>();
    
    
    private PositionIntervalResultGraph(List<List<Item>> categories) {
        this.categories = categories;
    }
    public static final class Item implements Comparable<Item>{
        Item parent;
        List<Item> children ;
        PositionIntervalResult<?> pir;
        int startAbsPos;
        int endAbsPos;
        
        Item (PositionIntervalResult<?> pir,Version v) {
            this.pir = pir;
            PositionInterval interval = pir.getInterval();
            startAbsPos = interval.getStartPosition().getAbsolutePosition(v);
            endAbsPos = interval.getEndPosition().getAbsolutePosition(v);
        }

        public int getStartAbsPos() {
            return startAbsPos;
        }

        public int getEndAbsPos() {
            return endAbsPos;
        }
        
        public Item getParent() {
            return parent;
        }
        void addChildren (Item item) {
            if (children == null) {
                children = new ArrayList<PositionIntervalResultGraph.Item>(); 
            }
            children.add(item);
        }
        public List<Item> getChildren() {
            if (children == null) {
                return Collections.emptyList();
            } else {
                return Collections.unmodifiableList(children);
            }
        }
        public PositionIntervalResult<?> getPir() {
            return pir;
        }

        /** compare for first position
         * 
         * @param o
         * @return
         */
        public int compareTo(Item o) {
            return  getStartAbsPos() - o.getStartAbsPos();
        }
    }
    
    public List<Item> getItems(int category) {
        return Collections.unmodifiableList(categories.get(category));
    }
    /**
     * Create hierchy of positions intervals for specig revision. 
     * @param pirs hiearchicall position version intervat result containers  (e.g. classes, methods,bloks, usages)
     * @param v creates hiearchy for revision v
     * @param levels skip level for assgning children and parent in Item,
     * (1: classes -> methods -> blocks) (2: classes->methods , if a block is not in method then classes->block)
     * @return
     */
    public static PositionIntervalResultGraph createGraph (List<PositionVersionIntervalResultContainer<?>> pirs,Version v,int levels ) {
        List<List<Item>> categories = new ArrayList<List<PositionIntervalResultGraph.Item>>(pirs.size());
        for (PositionVersionIntervalResultContainer pirc : pirs) {
            Set<PositionIntervalResult> pirs1 = pirc.getPirs(v);
            List<Item> category = new ArrayList<Item>(pirs1.size()); 
            categories.add(category);
            for (PositionIntervalResult pir : pirs1) {
                category.add(new Item(pir,v));
            }
            Collections.sort(category);
        }
        PositionIntervalResultGraph pirg = new PositionIntervalResultGraph(categories);
        pirg.compute(levels);
        return pirg;
    }

    /** make connections between parents and childrens
     * 
     * @param levels interstep category 
     */
    private void compute(int levels) {
        // lets  category1 >  category2  > category3
        // means a position  interval in  category2 is subset of a pos in category1
        // 
        if (levels < 1) {
            throw new IllegalArgumentException(" level " + levels);
        }
        for (int level  = 1 ; level <= levels ; level++) {
            for (int i = 0; i < categories.size() - 1; i++) {
                List<Item> cat1  = categories.get(i);
                List<Item> cat2  = categories.get(i+level);
                Item c2Item = null;
                int c2Iter = 0;
                for (int c1 = 0; c1 < cat1.size(); c1++) {
                    PositionIntervalResultGraph.Item c1Item = cat1.get(c1);
                    while(true) {
                        if (c2Item == null) {
                            if (c2Iter == cat2.size()) {
                                break;
                            }
                            c2Item = cat2.get(c2Iter++);
                            if (c2Item.getParent() != null) {
                                continue;
                            }
                        }
                        if (c1Item.getStartAbsPos() <= c2Item.getStartAbsPos()) {
                            if (c1Item.getEndAbsPos() >= c2Item.getEndAbsPos()) {
                                c1Item.addChildren(c2Item);
                                c2Item.parent = c1Item;
                                c2Item = null;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
}
