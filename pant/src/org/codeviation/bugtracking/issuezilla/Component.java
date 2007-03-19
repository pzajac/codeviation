package org.codeviation.bugtracking.issuezilla;

import java.util.HashSet;
import java.util.Set;

 /** issuezilla component with subcomponent
     */
public final class Component {
     private String component;
     private String subComponent;
     Set<Integer> issues = new HashSet<Integer>();

        public Component(String component, String subComponent) {
            this.component = component;
            this.subComponent = subComponent;
        }

        public void addIssue(int id) {
            issues.add(id);
        }
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final Component test = (Component) o;

            if (this.component != test.component && this.component != null &&
                !this.component.equals(test.component))
                return false;
            if (this.subComponent != test.subComponent &&
                this.subComponent != null &&
                !this.subComponent.equals(test.subComponent))
                return false;
            return true;
        }

        public int getIssuesSize() {
            return issues.size();
        }
        
        public int hashCode() {
            int hash = 3;

            hash = 59 * hash +
                   (this.component != null ? this.component.hashCode()
                                           : 0);
            hash = 59 * hash +
                   (this.subComponent != null ? this.subComponent.hashCode()
                                              : 0);
            return hash;
        }
        
        public String getComponent() {
            return component;
        }
        
        public String getSubComponent() {
            return subComponent;
        }
   }
