/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import imapi.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/**
 *
 * @author tzortzak
 */
public class TestInstansceMatchingApi {

    /**
     * @param args the command line arguments
     */
    
    private static void printArgsHelp(){
        System.out.println("Parameters Expected:");
        System.out.println("\tparam1: absolute path to the base path that will be used in order to retrieve the full path to rdf files used as source or target data.");
        System.out.println("\tparam2: absolute path to user configuration xml file (enclosed in \"..path..\" e.g. \"C:\\Users\\Default\\Desktop\\UserConfiguration.xml\")");
        System.out.println("\tparam3: absolute path to Query weights configuration xml file (enclosed in \"..path..\" e.g. \"C:\\Users\\Default\\Desktop\\QueryWeightsConfiguration.xml\")");
    }
    
    public static void main(String[] args) {
        
        if(args==null || args.length<2){
            
            printArgsHelp();
            return;
        }
        
        String basePath = args[0];
        
        if (basePath == null || basePath.trim().length() == 0) {
            System.out.println("Empty path given for base file path.");            
            printArgsHelp();
            return;
        }
        File dir = new File(basePath.trim());
        if (dir.exists() == false || dir.isDirectory()==false) {
            System.out.println("Base Path folder not found at path: " + basePath);
            printArgsHelp();
            return;
        }
        
        String userConfigurationXmlPath = args[1];
        if (userConfigurationXmlPath == null || userConfigurationXmlPath.trim().length() == 0) {
            System.out.println("Empty path given for the user configuration xml file path.");            
            printArgsHelp();
            return;
        }

        if (userConfigurationXmlPath.toLowerCase().endsWith(".xml") == false) {
            System.out.println("User Configuration file path should be an xml file.");            
            printArgsHelp();
            return;
        }
        

        File f = new File(userConfigurationXmlPath.trim());
        if (f.exists() == false || f.isDirectory()) {
            System.out.println("User Configuration File not found at path: " + userConfigurationXmlPath);
            printArgsHelp();
            return;
        }
        
        String queryConfigXmlFile = args[2];
        
        if (queryConfigXmlFile == null || queryConfigXmlFile.trim().length() == 0) {
            System.out.println("Empty path given for the Query Weight configuration xml file path.");            
            printArgsHelp();
            return;
        }

        if (queryConfigXmlFile.toLowerCase().endsWith(".xml") == false) {
            System.out.println("Query Weight Configuration file path should be an xml file.");            
            printArgsHelp();
            return;
        }
        

        File f2 = new File(queryConfigXmlFile.trim());
        if (f2.exists() == false || f2.isDirectory()) {
            System.out.println("Query Weight Configuration File not found at path: " + queryConfigXmlFile);
            printArgsHelp();
            return;
        }
        
        String enableExtendedMessagesStr = "";
        boolean enableExtendedMessages = false;
        if(args.length>=4) {
            enableExtendedMessagesStr = args[3].trim().toLowerCase();
            if(enableExtendedMessagesStr.equals("true")|| enableExtendedMessagesStr.equals("on")){
                enableExtendedMessages = true;
            }
        }
        //Check User Configuration
        
        long startTime = System.nanoTime();
        
        imapi.IMAPIClass im = new IMAPIClass(basePath, userConfigurationXmlPath,queryConfigXmlFile);
        if(im.getErrorMessage().length()>0){
            System.out.println("Error occurred During IMAPIClass initialization:\r\n"+im.getErrorMessage());
        }
        else{            
            if(enableExtendedMessages){
                im.enableExtentedMessages();
            }
            
            int ret = im.performComparison();
            if(ret!=ApiConstants.IMAPISuccessCode){
                System.out.println("Error occurred:\r\n"+im.getErrorMessage());
            }
            
            
            System.out.println();
            System.out.println();
            //im.performQueryToFile(targetQueryString);
            //Hashtable<Integer, Vector<SourceTargetPair>> finalResults = new Hashtable<Integer, Vector<SourceTargetPair>>();
            //im.performComparison(finalResults);
        }
        
        
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("\n\n\nThe Whole Procedure Lasted: "+ TimeUnit.SECONDS.convert(estimatedTime,TimeUnit.NANOSECONDS) + " seconds.");
	
    }
    
}
