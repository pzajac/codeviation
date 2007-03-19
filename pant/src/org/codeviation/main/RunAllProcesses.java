
package org.codeviation.main;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.codeviation.tasks.RepositoryProcess;
import org.codeviation.tasks.RepositoryProcessEnv;
import org.codeviation.model.vcs.CVSMetric;
import org.openide.util.Lookup;

/**
 * Executes all proccesses, 
 * parameters:
 *   1 - workDir
 * @author pzajac
 */
public class RunAllProcesses {
    
    public static void main(String args[]) {
        Set<String> filters = new HashSet<String>();
        CVSMetric.setUpdateCVS(false);
        System.out.println(args.length);
        boolean atLeastOneProcess = false;
        if (args.length == 0) {
            help();
            return;
        }
        
        // init workdir 
        //
        File workDir = new File(args[0]);
        workDir.mkdirs();
        if (!workDir.isDirectory()) {
            System.out.println("Error: dir " + args[0] + " was not created.");
            return;
        }
        
        // init filters
        //
        for (int i = 1 ; i < args.length ; i++) {
            filters.add(args[i]);
        }
        
        // execute processes
        //
        Repository rep =  PersistenceManager.getDefault().getRepository("nbcvs");
        Set<String> tags = rep.getAllTags();
        
        System.out.println("Tags:\n");
        for (String tag : tags) {
            System.out.println(tag);
        }
        System.out.println("");
        if (rep == null) {
            System.out.println("No nbcvs repository found.");
            return;
        }
        for (RepositoryProcess process : Lookup.getDefault().lookupAll(RepositoryProcess.class)) {
            if (filters.size() == 0 || filters.contains(process.getName())) {
                atLeastOneProcess = true;
                System.out.println("Process execution: " + process.getName());
                File pwd = new File(workDir,process.getName());
                pwd.mkdirs();
                RepositoryProcessEnv env = new RepositoryProcessEnv();
                env.setWorkDir(pwd);
                env.setTags(tags);
                process.execute(rep, env);
            }
        }
        if (!atLeastOneProcess) {
            help();
        }
    }
    
    private static void help() {
        System.out.println("Invalid count of parameters.");
        System.out.println("Usage: RunAllProcesses <workdir> [<filter1> [<filterN]]" );
        System.out.println("\nPosible filters:");
        for (RepositoryProcess process :  Lookup.getDefault().lookupAll(RepositoryProcess.class)) {
            System.out.println('"' + process.getName() + '"' + " - " + process.getDescription() + "\n" );
        }

    }
}
