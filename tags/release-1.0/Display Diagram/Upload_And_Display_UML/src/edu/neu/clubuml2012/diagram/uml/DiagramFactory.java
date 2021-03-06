/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package neu.kai.uml;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 *
 */
public class DiagramFactory {
    
    private static String absolutePath;
    private static String libPath;
    private String ecoreFileName;
    private String javaFileName;
    private String dotFileName;
    private String pngFileName;
    private ArrayList<EObject> pkgs;  
    private BufferedWriter out;
    String command;
    File javaFile;
    private String FontColor[] = {"red", "black", "blue", "red", "yellow" , "black"};
    private String FillColor[] = {"pink", "orange", "LightYellow", "PaleGreen", "White", "LightGray"};
    private int pkgIndex = 0;
    private final int MAX_NUM_FONT_COLORS = 6;
    private final String DEFAULT_ATTRIBUTE_TYPE = "int";
    
    public static void main(String[] args) {
        //This method is used to test out the creation of diagrams.
        //FYI to put arguments in Eclipse:  Run->RunConfigurations->Arguments where you put 1 arg per line
        // I used  "", library.ecore, C:\\java\\Project\\UMLGraph-5.6\\lib\\
        //This meant my .ecore file, and the .png were at the same dir level as the src dir in this project
        
        //verify correct number of args
        if (args.length !=3)
        {
            System.out.println("usage:  CreatePng PathNameToEcorefile ecorefilename pathToUMLGraphLib");
            System.out.println("        where PathNameToEcorefile is the absolute path of the file");
            System.out.println("        and ecorefilename is the  file name");
            System.out.println("        and pathToLib is the path to UMLGRAPH library");
            return;
        }
        
        DiagramFactory obj = new DiagramFactory(args[0],args[1], args[2]);
        obj.process();
    }
    
    public DiagramFactory(String absolutePath, String newName, String libPath){
        DiagramFactory.absolutePath = absolutePath;
        DiagramFactory.libPath = libPath;
        ecoreFileName = newName;
        javaFileName = newName  + ".java";
        dotFileName = newName + ".dot";
        pngFileName = newName + ".png"; 
    }

    public String process() {
        try {
            String ecoreFilePath = absolutePath + ecoreFileName;
            EcoreParser parserObj = new EcoreParser(ecoreFilePath);
            pkgs = parserObj.getList();
        } catch (IOException e) {
            System.out.println("Error in getting Eobject");
            e.printStackTrace();
        }

        createJavaFile();
        createPngFile();
        
        return command;
    }

    //In order to use UMLGraph, you need to create a "java" file of the classes
    public void createJavaFile() {


        try {        //Create Stream Writer
            //open java file and create stream writer
            javaFile = new File(absolutePath + javaFileName);
            FileWriter fstream = new FileWriter(javaFile);
            out = new BufferedWriter(fstream);
            
            // write a header for the whole file
            //In future, you might need to add more files depending on what your ecore files use
            out.write("import java.util.Date;\n");
            out.write("import java.util.*; \n");
            out.write("import java.io.*; \n");            
            
            pkgIndex = 0;
            
            //for each package in the file
            for (int k = 0; k < pkgs.size(); k++) {
                readPkgForClasses(pkgs.get(k));  //recursive function                        
            }
            out.close();
        } catch (Exception e) {
            try {
                out.close();
            } catch (Exception e2) {
                System.out.println("Error closing the file");
            }
            System.out.println("Got an error creating the file");
        }
    }

    //This method is recursive so should catch pkgs within pkgs
    public void readPkgForClasses(EObject obj) {
        try {
            //determine if this is a pkg
            if (obj instanceof EPackage) {
                // get contents of pkg
                //increment pkgIndex for colors then loop back to beginning
                pkgIndex = (pkgIndex + 1) % MAX_NUM_FONT_COLORS;
                out.write("//Name of Ecore Package = " + ((ENamedElement) obj).getName()  + "\n\n");
                EList<EObject> objList = obj.eContents();
                for (int k = 0; k < objList.size(); k++) {
                    readPkgForClasses(objList.get(k));
                }
            } else if (obj instanceof EClass) {
                 //if not a pkg must be a class since underneath a package  
                 ReadClass(obj);
            } else
                System.out.println("Expecting an EClass, got: " + obj.toString()); 
       // } 
        } catch (Exception e) {
            System.out.println("Got an writing to file" + e); 
        }
    }
    
    
    public void createPngFile() {
        try {
            String umlGraphPath = libPath + "UmlGraph.jar";
            String command1 = "java" + " -jar " + umlGraphPath + " -all -private "+ absolutePath + javaFileName +
                    " -output " +  absolutePath + dotFileName;
            Runtime.getRuntime().exec(command1);
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            //Graphviz_Path: C:\Program Files (x86)\Graphviz 2.28\bin
            String command2[] = {"dot", "-Tpng", "-o", absolutePath + pngFileName,
                    absolutePath + dotFileName};
            Process proc2 = Runtime.getRuntime().exec(command2);
            command = command2.toString();
            while(proc2.waitFor() >= 0) {
                Thread.sleep(2000L);
            }
            

        } catch (Exception e) {
            System.out.println("Error in creating the png file");
            e.printStackTrace();
        }
    }
    
    
    public void ReadClass(EObject obj) {
        try { 
            EClass cls = (EClass) obj;
            //Start to write the Javadocs that UMLGraph reads
            out.write("\n");
            out.write("/**\n");
            //define colors for the class
            out.write(" * @opt nodefontcolor " + "\"" + FontColor[pkgIndex] + "\""  + "\n");
            out.write(" * @opt nodefillcolor " + "\"" + FillColor[pkgIndex] + "\""  + "\n");  
            //The EReference data goes in Javadoc comments
            EList<EReference>  refList = cls.getEReferences();
            for (int j = 0; j < refList.size(); j++) {
                EReference ref = refList.get(j);
                ReadReference(ref);
            } 
            out.write(" */ \n");  //end of Javadoc comments
            if (cls.isInterface()) {
                out.write("interface " + cls.getName()); 
            } else {
                out.write("class " + cls.getName());
            }

            //check if this class extends any other classes
            EList<EClass> superClsList = cls.getESuperTypes();
            if (superClsList.size() >= 1) {
                //Need to determine if class inheritance or interface inheritance
                //can only allow one extends for class inheritance
                for (int i = 0; i <superClsList.size(); i++ ){
                    EClass otherCls = superClsList.get(i);
                    if (!otherCls.isInterface()) {
                        out.write(" extends " + otherCls.getName());
                        break;
                    }
                }
                //allow multiple implements
                boolean firstImplements = true;
                for (int i = 0; i <superClsList.size(); i++ ){
                    EClass otherCls = superClsList.get(i);
                    if (otherCls.isInterface()) {
                        if (firstImplements) { //only do this first time
                            out.write(" implements " + otherCls.getName());
                            firstImplements = false;
                        } else {
                            out.write(" , " + otherCls.getName());                        
                        }
                    }
                }
            }
            out.write(" {\n");
            //for each variable in the class
            EList<EAttribute> attr = cls.getEAttributes();
            if (attr != null) {
                String tempType = "";
                for (int j = 0; j < attr.size(); j++) {
                    try {//in case there is no type name for this variable
                        tempType = attr.get(j).getEAttributeType().getInstanceClassName();
                    } catch (Exception e) {                   
                        tempType = DEFAULT_ATTRIBUTE_TYPE;
                    } finally {
                        //this check verifies there is an attribute
                        //(ecore sometimes gives an attribute named "" if user deleted an attribute
                        if (!attr.get(j).getName().equals("")) {
                            out.write("    " + tempType
                                + " " + attr.get(j).getName() + ";\n");
                        }
                    }
                }
            }

            //get operation objects into a list
            EList<EOperation> ope = cls.getEOperations();
            for (int j = 0; j < ope.size(); j++) {
                String tempType = "";
                try {
                    tempType = ope.get(j).getEType().getInstanceTypeName();  
                    
                } catch (Exception e) {
                    tempType = "void";
                } finally {
                    //if operation was deleted in emf, sometimes the ecore shows an operation named ""
                    if (!ope.get(j).getName().equals("")) {
                        out.write("    " + tempType + " " + ope.get(j).getName() + "();\n");
                    }
                }
            }
            out.write("}\n");  
        
        } catch (Exception e) {
            System.out.println("Error in writing to .java file");
            e.printStackTrace();
        }        
    }
    
    public void ReadReference(EReference ref) {
        
        try {    
            if (ref.isContainment()) {
                out.write (" * @composed 1 - "); 

                //This is the wrong upper bound, I need the bound at the other end
                //This part needs work for multiplicities
                int lowerBound = ref.getLowerBound() ;
                int upperBound = ref.getUpperBound() ;

                out.write(Integer.toString(lowerBound));

                 //if there is a range of values              
                if (lowerBound != upperBound) {
                    out.write("..");
                    if (upperBound== -1) {
                        out.write ("*");
                    } else if (upperBound != 0) {
                        out.write(Integer.toString(upperBound));
                    }
                } 
                out.write( " " + ref.getEReferenceType().getName() + "\n");
       
            } else {  //not a containment, but another relationship
                //can't put multiplicity at one end in emf
                out.write (" * @assoc -"); //this has no num at this end            
               
                if (ref.getName() == null) {
                    out.write(" - ");  
                } else {
                    out.write(" "); //space
                    out.write("\"" + ref.getName() + "\""); //relationship in quotes
                    out.write(" "); //space
                }

                out.write(Integer.toString(ref.getLowerBound()));
                              
                //if there is a range of values
                if (ref.getLowerBound() != ref.getUpperBound()) {//
                    out.write("..");
                    if (ref.getUpperBound() == -1) {
                        out.write ("*");
                    } else {
                    out.write(Integer.toString(ref.getUpperBound()));
                    }
                }
                out.write( " " + ref.getEReferenceType().getName() + "\n");               
            }
        } catch (Exception e) {
            System.out.println("Error in writing to .java file");
            e.printStackTrace();
        }
    }  //end ReadClass
    
}//end class
