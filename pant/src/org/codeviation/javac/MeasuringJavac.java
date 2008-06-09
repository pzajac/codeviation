/*
 * MeasuringJava.java
 *
 * Created on April 28, 2006, 4:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codeviation.javac;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.JavacFileManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.StreamHandler;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.Javac13;
import org.apache.tools.ant.types.Commandline;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.SourceRoot;

/**
 *
 * @author phrebejk,pzajac
 */
public final class MeasuringJavac extends Javac13 {
    public static final String CVS_TAG_PROP_NAME = "pant.cvs.tag";
    private StreamHandler handler;
   
    /** log file for exceptions from MetricBuilder
     */ 
    public static final String LOG_FILE_SYSTEM_PROPERTY_NAME = "pant.log.file";
    public MeasuringJavac() {
        super();
        MetricsRunner.initialize();
    }
    /**
     * Run the compilation.
     *
     * @exception BuildException if the compilation has problems.
     */
    @Override
    public boolean execute() throws BuildException{
        MetricsRunner.setLastSourceRoot(null);
        attributes.log("Using patched compiler compiler", Project.MSG_VERBOSE);
        String cvsProp = getProject().getProperty(CVS_TAG_PROP_NAME);
        if (cvsProp != null) {
            System.setProperty(CVS_TAG_PROP_NAME, cvsProp);
        }
        MetricsRunner.initCvsTag();
        attributes.log("tag :" + cvsProp);
        attributes.log(PersistenceManager.PANT_CACHE_FOLDER + " :" + PersistenceManager.getDefault().getFolder(), Project.MSG_VERBOSE);
        // XXX rather customize target via property
        // We use java 1.6, rather use jdk1.5 target
        this.target = "1.5";
        try {
            setupLogFile();
            Commandline cmd = new Commandline();
            
            setupModernJavacCommandlineSwitches( cmd );
            List<String> options = new ArrayList<String>(Arrays.asList( cmd.getArguments() ));
            // XXX -JxmXXX fails
            attributes.log("Options:");
            for (int i = 0 ; i < options.size() ; i++) {
                String o = options.get(i);
                if (o.startsWith("-J-Xmx")) {
                    options.remove(i--);
                } else {
                    attributes.log(o + ",");
                }
            }
            // XXX this is not the best way how to setup the compiler
            JavacTool jt = JavacTool.create();

            JavacFileManager dfm = jt.getStandardFileManager(null,null,null);
            MetricsRunner.setFileManager(dfm);
            JavacTaskImpl task = (JavacTaskImpl) jt.getTask(null, null, null, options, null,
                                       dfm.getJavaFileObjectsFromFiles(Arrays.asList(compileList)));
            MetricsRunner.setTask(task);        
            task.setTaskListener( new MetricsRunner(  attributes ) );

            try {
                task.analyze();
            } catch(IOException ioe) {
                throw new BuildException(ioe);
            }
            // XXX should check compilation status
            //
             boolean status = super.execute();
             SourceRoot srcRoot = MetricsRunner.getLastSourceRoot();
             if (srcRoot != null) {
                 srcRoot.getRepository().addSourceRootCompilationStatus(srcRoot,status,System.getProperty(CVS_TAG_PROP_NAME));
             }
             return true;
             
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        } finally {
            if (handler != null) {
                handler.close();
            }
        }
    }
    private void setupLogFile() throws IOException {
//        String logFileName = System.getProperty(LOG_FILE_SYSTEM_PROPERTY_NAME);
//        if (logFileName != null) {
//             attributes.log(LOG_FILE_SYSTEM_PROPERTY_NAME + ":" + logFileName);
//             File logFile = new File (logFileName);
//             OutputStream oos = new FileOutputStream(logFile,true);
//             handler = new StreamHandler(oos,new SimpleFormatter());
//        } else {
//            handler = new ConsoleHandler();
//        }
//        MetricsRunner.setHandler(handler);
//         handler.setLevel(Level.INFO);
//         Logger.getLogger("org.codeviation").addHandler(handler);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (handler != null) {
            handler.close();
        }
    }
    
}
