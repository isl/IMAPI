/*
 * Copyright 2014 Institute of Computer Science,
 *                Foundation for Research and Technology - Hellas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
 * Contact: 
 * =============================================================================
 * Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
 *     Tel: +30-2810-391632
 *     Fax: +30-2810-391638
 *  E-mail: isl@ics.forth.gr
 * WebSite: http://www.ics.forth.gr/isl/
 * 
 * =============================================================================
 * Authors: 
 * =============================================================================
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 */
import imapi.ResultSourceTargetPair;
import imapi.*;
import imapi.DataRecord;
import imapi.SourceInstancePair;
import imapi.SourceTargetPair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        System.out.println("\tparam4: Optional set to true or false in order to enable or disable extended debug messages.");
        System.out.println("\tparam5: Optional set to true or false in order redirect output to a seperate log file.");
        System.out.println("\tparam6: if param 5 is set to true then define the path where the output log will be created.");
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
        
        boolean redirectTolocalLog = false;
        if(args.length>=5) {
            String redirectOutputStr = args[4].trim().toLowerCase();
            if(redirectOutputStr.equals("true")|| redirectOutputStr.equals("on")){
                redirectTolocalLog = true;
            }
        }
        
        //Check User Configuration
        if(redirectTolocalLog){
            
            String baseOutputPath = "";
            if(args.length>=6) {
                String baseOutputStr = args[5].trim().toLowerCase();
                File f3 = new File(baseOutputStr);
                if(f3!=null && f3.exists() && f3.isDirectory()){
                    baseOutputPath = f3.getAbsolutePath();
                    
                    if(baseOutputPath.contains("\\") && baseOutputPath.endsWith("\\")==false){
                        baseOutputPath +="\\";
                    }
                    else if(baseOutputPath.contains("/") && baseOutputPath.endsWith("/")==false){
                        baseOutputPath +="/";
                    }
                    
                    if(baseOutputPath.endsWith("\\")==false && baseOutputPath.endsWith("/")==false){
                        baseOutputPath +="\\";
                    }
                    
                }
            }
            
            java.util.Date currentDate = java.util.Calendar.getInstance().getTime();
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String dateStr = DATE_FORMAT.format(currentDate);
            
            
            PrintStream printStream; 
            try {
                
                if(baseOutputPath.length()>0){
                    System.out.println("\nRedirecting output to file: " +baseOutputPath+ "output("+dateStr+").log");
                }
                else{
                    Path currentRelativePath = Paths.get("");
                    String s = currentRelativePath.toAbsolutePath().toString();
                    if(s.contains("\\") && s.endsWith("\\")==false){
                        s +="\\";
                    }
                    else if(s.contains("/") && s.endsWith("/")==false){
                        s +="/";
                    }
                    
                    if(s.endsWith("\\")==false && s.endsWith("/")==false){
                        s +="\\";
                    }
                    System.out.println("\nRedirecting output to file: " +s+ "output("+dateStr+").log");
                }
                
                
                try {
                    if(baseOutputPath.length()>0){
                        printStream = new PrintStream(baseOutputPath+"output("+dateStr+").log", "UTF-8");
                    }
                    else{
                        printStream = new PrintStream("output("+dateStr+").log", "UTF-8");
                    }
                    System.setOut(printStream);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(TestInstansceMatchingApi.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TestInstansceMatchingApi.class.getName()).log(Level.SEVERE, null, ex);
            }        
        }
        
        long startTime = System.nanoTime();
        
        
        Hashtable<Float, Vector<ResultSourceTargetPair>> finalResults = new Hashtable<Float, Vector<ResultSourceTargetPair>>();
                
        
        imapi.IMAPIClass im = new IMAPIClass(basePath, userConfigurationXmlPath,queryConfigXmlFile);
        if(im.getErrorMessage().length()>0){
            System.out.println("Error occurred During IMAPIClass initialization:\r\n"+im.getErrorMessage());
        }
        else{            
            if(enableExtendedMessages){
                im.enableExtentedMessages();
            }
            
            int ret = im.performComparison(finalResults);
            
            if(ret!=ApiConstants.IMAPISuccessCode){
                System.out.println("Error occurred:\r\n"+im.getErrorMessage());
            }
            
            System.out.println("\n\nResults with similarity above threshold:");
            System.out.println("---------------------------------------------------------------------------");
            im.printResultInstances(finalResults);
            
            System.out.println();
            System.out.println();
            
        }
        
        
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("\n\n\nThe Whole Procedure Lasted: "+ TimeUnit.SECONDS.convert(estimatedTime,TimeUnit.NANOSECONDS) + " seconds.");
	
    }
}
