
package org.codeviation.tasks;

import org.codeviation.model.Repository;
import org.codeviation.main.ClassRankMatrixGenerator;

/**
 * Computes history of class rank
 * @author pzajac
 */
public class PageRankProcess implements RepositoryProcess {
        
    public boolean execute(Repository rep, RepositoryProcessEnv env) {
        ClassRankMatrixGenerator generator = new ClassRankMatrixGenerator(ClassRankMatrixGenerator.ElementType.CLASS);
        for (String tag : env.getTags()) {
            generator.computeAndStore(rep, tag,env.getSourceRootFilter());
        }
        return true;
        
    }

    public String getName() {
        return "PageRank";
    }

    public String getDescription() {
       return "Computes history of page rank";
    }
}
