
package org.codeviation.javac;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.codeviation.statistics.RecordType;

/**
 * Counts of Elements for a version; 
 * @author Petr Zajac
 */
public final class CountsItem {
    private int classes;
    private int interfaces;
    private int enums;
    private int annotations;
    private int methods;
    private int constructors;
    private int fields;
    private int parameters;
    private int variables;
    private int staticInits;
    private int enumConstants;
    private int exceptionParameters;

    public static final RecordType[] RECORD_TYPES = new RecordType[] {
      new RecordType("Classes",0,false),
      new RecordType("Interfaces",1,false),
      new RecordType("Enums",2,false),
      new RecordType("Annotations",3,false),
      new RecordType("Methods",4,false),
      new RecordType("Constructors",5,false),
      new RecordType("Fields",6,false),
      new RecordType("Parameters",7,false),
      new RecordType("Variables",8,false),
      new RecordType("Static inits",9,false),
      new RecordType("Enum constants",10,false),
      new RecordType("Exception Parameters",11,false)
    };
    public static enum Type {
        CLASSES,
        INTERFACES,
        ENUMS,
        ANNOTATIONS,
        METHODS,
        CONSTRUCTORS,
        FIELDS,
        PARAMETERS,
        VARIABLES,
        STATIC_INITS,
        ENUM_CONSTANTS,
        EXCEPTION_PARAMETERS
    }
    public void incStaticInits() {
        staticInits++;
    }
    public void incEnumConstants() {
        enumConstants++;
    }
    public void incExceptionParameters() {
        exceptionParameters++;
    }
    
    public int getStaticInits() {
        return staticInits;
    }
    public int getEnumConstants() {
        return enumConstants;
    }
    public int getExceptionParameters() {
        return exceptionParameters;
    }
    public void incClasses() {
        this.classes++;
    }
    
    public void incInterfaces () {
        this.interfaces++;
    }

    public int getClasses() {
        return classes;
    }

    public int getInterfaces() {
        return interfaces;
    }

    public int getEnums() {
        return enums;
    }

    public void incEnums() {
        this.enums++;
    }

    public int getAnnotations() {
        return annotations;
    }

    public void incAnnotations() {
        this.annotations++;
    }

    public int getMethods() {
        return methods;
    }

    public void incMethods() {
        this.methods++;
    }

    public int getConstructors() {
        return constructors;
    }

    public void incConstructors() {
        this.constructors++;
    }

    public int getFields() {
        return fields;
    }

    public void incFields() {
        this.fields++;
    }

    public int getParameters() {
        return parameters;
    }

    public void incParameters() {
        this.parameters++ ;
    }

    public int getVariables() {
        return variables;
    }

    public void incVariables() {
        this.variables++;
    }

    public Vector getVector() {
        Vector vec = new DenseVector(new double[]{
            classes,
            interfaces,
            enums,
            annotations,
            methods,
            constructors,
            fields,
            parameters,
            variables,
            staticInits,
            enumConstants,
            exceptionParameters
        });  
        return vec;
    }
    public void addCountsItem(CountsItem ci) {
        classes += ci.getClasses();
        interfaces += ci.getInterfaces();
        enums += ci.getEnums();
        annotations += ci.getAnnotations();
        methods += ci.getMethods();
        constructors += ci.getConstructors();
        fields += ci.getFields();
        parameters += ci.getParameters();
        variables += ci.getVariables();
        staticInits += ci.getStaticInits();
        enumConstants += ci.getEnumConstants();
        exceptionParameters += ci.getExceptionParameters();
    }
    
    public int getValue(Type type) {
        switch (type) {
            case CLASSES:
                return classes;
            case INTERFACES:
                return interfaces;
            case ENUMS:
                return enums;
            case ANNOTATIONS:
                return annotations;
            case METHODS:
                return methods;
            case CONSTRUCTORS:
                return constructors;
            case FIELDS:
                return fields;
            case PARAMETERS:
                return parameters;
            case VARIABLES:
                return variables;
            case STATIC_INITS:
                return staticInits;
            case ENUM_CONSTANTS:
                return enumConstants;
            case EXCEPTION_PARAMETERS:
                return exceptionParameters;
            default:
                throw new IllegalArgumentException("Invalid type " + type);
        }
    }
    public static CountsItem readRef(ObjectInputStream ois) throws IOException {
        CountsItem c = new CountsItem();
        c.classes = ois.readInt();
        c.interfaces  = ois.readInt();
        c.enums = ois.readInt();
        c.annotations = ois.readInt();
        c.methods = ois.readInt();
        c.constructors = ois.readInt();
        c.fields = ois.readInt();
        c.parameters = ois.readInt();
        c.variables = ois.readInt();
        c.staticInits = ois.readInt();
        c.enumConstants = ois.readInt();
        c.exceptionParameters = ois.readInt();
        return c;
    }
    
    public void writeRef(ObjectOutputStream oos ) throws IOException {
        oos.writeInt(classes);
        oos.writeInt(interfaces);
        oos.writeInt(enums);
        oos.writeInt(annotations);
        oos.writeInt(methods);
        oos.writeInt(constructors);
        oos.writeInt(fields);
        oos.writeInt(parameters);
        oos.writeInt(variables);
        oos.writeInt(staticInits);
        oos.writeInt(enumConstants);
        oos.writeInt(exceptionParameters);
    }
}
