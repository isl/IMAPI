/*
 * Copyright 2014 Your Name <Elias Tzortzakakis at tzortzak@ics.forth.gr>.
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
 */

package imapi;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author tzortzak
 */
class UserConfigurationClass {
    
    private final String XPathforResultsThreshold = "/Root/ResultsThreshold";
    private final String XPathTargetSourceChoice  = "/Root/TargetSourceChoice";
    private final String XPathforSourceInputFiles = "/Root/SourceInputFiles/File";
    private final String XPathforTargetInputFiles = "/Root/TargetInputFiles/File";
    
    private final String XPathforTargetQueryConfigs = "/Root/QuerySequencesConfiguration/querysequence";
    private final String AttributeNameForWeight = "weight";
    private final String AttributeNameForMnemonic = "mnemonic";
    private final String subXPathForParamters = "./parameter";
    private final String AttributeNameForParamName = "name";
    
    private final String AttributeOfInputFileForPredicateDirectionUsage = "PredicateDirection";
    //private final String XpathForDatabasePredicateDirectionUsage = "/Root/TargetOnlineDatabaseConfiguration/@PredicateDirection";
    
    private final String XPathForMinimumLiteralSimilarity = "/Root/literalMinimumSimilarityValue";
    private static final String XPathforBaseClass = "/Root/QuerySequencesConfiguration/@BaseClassID";
    
    private Double minimumLiterlSimilarity = 0d;
    
    Double getMinimumLiteralSimilarity(){
        return this.minimumLiterlSimilarity;
    }
    
    
    private Vector<UserQueryConfiguration> userQueryConfigs = new Vector<UserQueryConfiguration>();
    
    int getNumberOfSequences(){
        return this.userQueryConfigs.size();
    }
    
    
    Vector<UserQueryConfiguration> getUserQueriesCopy(){
        Vector<UserQueryConfiguration> returnVec  = new Vector<UserQueryConfiguration>();
        for(int i=0; i<this.userQueryConfigs.size(); i++){
            returnVec.add(this.userQueryConfigs.get(i).copy());
        }
        
        return returnVec;
    }
    
    double getWeightAtUserQueryIndex(int index){
        if(index>=0 && index< this.userQueryConfigs.size()){
            return this.userQueryConfigs.get(index).getWeight();
        }
        else{
            return 0d;
        }
    }
    
    String getMnemonicAtUserQueryIndex(int index){
        if(index>=0 && index< this.userQueryConfigs.size()){
            return this.userQueryConfigs.get(index).getMnemonic();
        }
        else{
            return "";
        }
    }
    
    
    
    //private fields
    private Double ResultsThreshold = 0d;
    
    private ApiConstants.TargetSourceChoice ComparisonMode; 
    //private boolean isTargetDatabaseUsingInversePredicates = false;
    
    private Vector<CidocCrmCompatibleFile> SourceInputFiles = new Vector<CidocCrmCompatibleFile>();    
    private Vector<CidocCrmCompatibleFile> TargetInputFiles = new Vector<CidocCrmCompatibleFile>();
    
    Double getResultsThreshold(){
        return this.ResultsThreshold;
    }
    
    
    ApiConstants.TargetSourceChoice getComparisonMode(){
        return this.ComparisonMode;
    }
    /*
    boolean getIsTargetDatabaseUsingInversePredicates(){
        return this.isTargetDatabaseUsingInversePredicates;
    }
    */
    Vector<CidocCrmCompatibleFile> getSourceInputFiles(){
        return new Vector<CidocCrmCompatibleFile>(this.SourceInputFiles);
    }
    
    Vector<CidocCrmCompatibleFile> getTargetInputFiles(){
        return new Vector<CidocCrmCompatibleFile>(this.TargetInputFiles);
    }
    
    static String getBaseConfigurationClass(String userConfigurationXmlFile){
        String returnVal = "";
        
        
        //make some initial checks to the xml file given as input
        if (userConfigurationXmlFile == null || userConfigurationXmlFile.trim().length() == 0) {
            System.out.println("Empty path given for the User Configuration xml file path.");
            return "";
        }

        if (userConfigurationXmlFile.toLowerCase().endsWith(".xml") == false) {
            System.out.println("User Configuration file path should be an xml file.");
            return "";
        }
        File f1 = new File(userConfigurationXmlFile.trim());
        if (f1.exists() == false || f1.isDirectory()) {
            System.out.println("User Configuration File not found at path: " + userConfigurationXmlFile);
            return "";
        }
        
        
        
        //this.userPredicateSequencesDefinition = new Vector<UserConfigurationPredicateSequence>();
        
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            
            Document document = builder.parse(userConfigurationXmlFile);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            //getBaseClass id
            if(XPathforBaseClass!=null)
            {
                Node xNode = (Node) xpath.evaluate(XPathforBaseClass, document, XPathConstants.NODE);
                if (xNode != null) {
                    returnVal = "###CID:"+xNode.getTextContent().trim()+"###";
                }
            }
            
            if(returnVal.length()==0 || returnVal.matches("(?i)###CID:[^#]*###") ==false){
                System.out.println("BaseClass Identifier not correctly set.");                
                return "";
            }
            else{
                return returnVal;
            }
        
          
        } catch (ParserConfigurationException e) {
            
            String tempMsg = "ParserConfigurationException occured:\r\n" + e.getMessage();
            System.out.println(tempMsg);         
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            
            String tempMsg = "XPathExpressionException occured:\r\n" + e.getMessage();
            System.out.println(tempMsg);         
            e.printStackTrace();
        } catch (SAXException e) {
            
            String tempMsg = "SAXException occured:\r\n" + e.getMessage();
            System.out.println(tempMsg);         
            e.printStackTrace();
        } catch (IOException e) {
            
            String tempMsg = "IOException occured:\r\n" + e.getMessage();
            System.out.println(tempMsg);         
            e.printStackTrace();
        }  
        
        
        return "";
    }

    String combineBaseAndRelativePath(String basePath, String relativePath){
        String finalPath ="";
        String relativePathLocalCopy = relativePath;
        if (basePath == null || basePath.trim().length() == 0) {
            //System.out.println("Empty path given for base file path.");                        
            return "";
        }
        if (relativePathLocalCopy == null || relativePathLocalCopy.trim().length() == 0) {
            //System.out.println("Empty path given for base file path.");                        
            return "";
        }
        
        
        File dir = new File(basePath.trim());
        if (dir.exists() == false || dir.isDirectory()==false) {
            System.out.println("Base Path folder not found at path: " + basePath);
            return "";
        }
        
        finalPath = dir.getAbsolutePath();
        
        while(finalPath.contains("\\")){
            finalPath = finalPath.replace("\\", "/");
        }
        
        while(relativePathLocalCopy.contains("\\")){
            relativePathLocalCopy = relativePathLocalCopy.replace("\\", "/");
        }
        
        while(relativePathLocalCopy.contains("/./")){
            relativePathLocalCopy = relativePathLocalCopy.replace("/./", "/");
        }
        
        if(relativePathLocalCopy.startsWith("/")){
            finalPath +=relativePathLocalCopy;
        }
        else{            
            if(relativePathLocalCopy.startsWith("./")){
                finalPath+=relativePathLocalCopy.substring(1);
            }
            else{
                
                
                //starts with ../ 
                //or starts with just relative path name
                while(relativePathLocalCopy.startsWith("../")){
                    
                    relativePathLocalCopy = relativePathLocalCopy.replaceFirst("\\.\\./", "");
                    File parentDir = dir.getParentFile();
                    if(parentDir!=null && parentDir.exists()){
                        dir = parentDir;
                    }
                }
                
                finalPath = dir.getAbsolutePath();
                while(finalPath.contains("\\")){
                    finalPath = finalPath.replace("\\", "/");
                }
                finalPath +="/"+relativePathLocalCopy;
            }
        }
        
        
        File fileInstance = new File(finalPath);
        if (fileInstance.exists() == false || fileInstance.isDirectory()) {
            finalPath = "";
        }
                        
        return finalPath;
    }
    
    UserConfigurationClass(String baseFilePath, String userConfigurationXmlFile, QueryPrototypeConfigurationClass qClass){
        
        this.setErrorMessage(ApiConstants.IMAPISuccessCode, "");
        
        //make some initial checks to the xml file given as input
        if (userConfigurationXmlFile == null || userConfigurationXmlFile.trim().length() == 0) {
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "Empty path given for the User Configuration xml file path.");
            return;
        }

        if (userConfigurationXmlFile.toLowerCase().endsWith(".xml") == false) {
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "User Configuration file path should be an xml file.");
            return;
        }
        File f1 = new File(userConfigurationXmlFile.trim());
        if (f1.exists() == false || f1.isDirectory()) {
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "User Configuration File not found at path: " + userConfigurationXmlFile);
            return;
        }
        
        
        //initialize private fields
        this.ResultsThreshold = 0d;
        this.userQueryConfigs = new Vector<UserQueryConfiguration>();
        this.SourceInputFiles = new Vector<CidocCrmCompatibleFile>();
        this.TargetInputFiles = new Vector<CidocCrmCompatibleFile>(); 
        
        
        //this.userPredicateSequencesDefinition = new Vector<UserConfigurationPredicateSequence>();
        
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            
            Document document = builder.parse(userConfigurationXmlFile);
            XPath xpath = XPathFactory.newInstance().newXPath();

            boolean is_Thershold_correctly_set = false;
            if(this.XPathforResultsThreshold!=null){
                Node node = (Node) xpath.evaluate(this.XPathforResultsThreshold, document, XPathConstants.NODE);
                if (node != null) {
                    String thersholdStr = node.getTextContent();
                    if(thersholdStr!=null){
                        Double tempDouble = Double.parseDouble(thersholdStr);
                        if(tempDouble!=null && tempDouble>0 && tempDouble<=1d){
                            this.ResultsThreshold = tempDouble;
                            is_Thershold_correctly_set = true;
                        }
                    }
                }
            }
            
            if(!is_Thershold_correctly_set){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "ResultsThreshold should be set to a double value > 0 and <= 1. e.g.:\r\n\t<ResultsThreshold>0.6</ResultsThreshold>");                
                return;
            }
            DecimalFormat df = new DecimalFormat("#.##");
            
            System.out.println("\nResults Threshold set to: " + df.format(ResultsThreshold));
            
            //Get Minimum literal similarity
            if(this.XPathForMinimumLiteralSimilarity!=null)
            {
                Node xNode = (Node) xpath.evaluate(this.XPathForMinimumLiteralSimilarity, document, XPathConstants.NODE);
                if (xNode != null) {
                    String minimumDoubleStyr = xNode.getTextContent();
                    Double tempDouble = Double.parseDouble(minimumDoubleStyr);
                    if(tempDouble!=null && tempDouble>0 && tempDouble<=1d){
                        this.minimumLiterlSimilarity = tempDouble;
                    }
                }
            }
            
            
            if(this.minimumLiterlSimilarity<0 || this.minimumLiterlSimilarity>1){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "Minimum literal similarity value is not correctly set.\nAcceptable values are >=0 and <=1.\nValue Found was: " + this.minimumLiterlSimilarity);                
                return;
            }
            System.out.println("Minimum Literal Similarity set to: " + df.format(minimumLiterlSimilarity));
            //get comparison mode
            ApiConstants.TargetSourceChoice[] validTargetSourceChoices = ApiConstants.TargetSourceChoice.values();
            boolean is_ComparisonMode_correctly_set = false;
            if(this.XPathTargetSourceChoice!=null){
                Node node = (Node) xpath.evaluate(this.XPathTargetSourceChoice, document, XPathConstants.NODE);
                if (node != null) {
                    String comparisonModeStr = node.getTextContent().trim().toLowerCase();
                    
                    if(comparisonModeStr!=null){
                        while(comparisonModeStr.contains(":")){
                            comparisonModeStr = comparisonModeStr.replace(":","").trim();
                        }
                        for(int i=0;i<validTargetSourceChoices.length; i++){
                            String modeStr = validTargetSourceChoices[i].name().toLowerCase();
                            if(comparisonModeStr.equals(modeStr)){
                                this.ComparisonMode = validTargetSourceChoices[i];
                                is_ComparisonMode_correctly_set = true;
                                break;
                            }
                        }                        
                    }
                }
            }            
            
            
            if(!is_ComparisonMode_correctly_set){
                String validValsStr = "";
                for(int i=0; i<validTargetSourceChoices.length ; i ++){
                    if(validValsStr.length()>0){
                        validValsStr+=", ";
                    }
                    validValsStr+=validTargetSourceChoices[i].name();
                }
                this.setErrorMessage(ApiConstants.IMAPIFailCode, 
                        "TargetSourceChoice should be set to one of the following values: " +
                                validValsStr +" e.g.:\r\n\t<TargetSourceChoice>"+validTargetSourceChoices[0].name()+"</TargetSourceChoice>");                
                return;
            }
            
            System.out.println("Comparison Mode set to: " + ComparisonMode.toString());
            
            ApiConstants.PredicateDirectionUsage[] validPredicateDirectionUsage = ApiConstants.PredicateDirectionUsage.values();
            
            //get isTargetDatabaseUsingInversePredicates
            /*
            if(this.XpathForDatabasePredicateDirectionUsage!=null && this.ComparisonMode!= ApiConstants.TargetSourceChoice.FILE_COMPARISON){
                Node node = (Node) xpath.evaluate(this.XpathForDatabasePredicateDirectionUsage, document, XPathConstants.NODE);
                if(node!=null){
                    String nodeVal = node.getTextContent().trim().toLowerCase();
                    
                    
                    for(int q=0; q< validPredicateDirectionUsage.length; q++){
                        String checkStr = validPredicateDirectionUsage[q].toString().toLowerCase();
                        if(checkStr.equals(nodeVal)){
                            this.predicatesDirection = validPredicateDirectionUsage[q];
                        }
                    }
                    
                }
            }
            */
            
            //get source input files 
            String tempErrorMsg = "";
            if (this.XPathforSourceInputFiles != null) {
                NodeList nodes = (NodeList) xpath.evaluate(this.XPathforSourceInputFiles, document, XPathConstants.NODESET);
                if (nodes != null) {
                    
                    int howmanyNodes = nodes.getLength();                    
                    for (int i = 0; i < howmanyNodes; i++) {
                        Node node = nodes.item(i);
                        String xmlFilePath = node.getTextContent();
                        
                        ApiConstants.PredicateDirectionUsage predicateUsage = ApiConstants.PredicateDirectionUsage.BOTH;
                        
                        if(node.hasAttributes()){
                            
                            Node attrNode = node.getAttributes().getNamedItem(this.AttributeOfInputFileForPredicateDirectionUsage);
                            if(attrNode!=null){
                                String val = attrNode.getTextContent().trim().toLowerCase();
                                
                                for(int q=0; q< validPredicateDirectionUsage.length; q++){
                                    String checkStr = validPredicateDirectionUsage[q].toString().toLowerCase();
                                    if(checkStr.equals(val)){
                                        predicateUsage = validPredicateDirectionUsage[q];
                                    }
                                }
                                
                            }
                           
                        }
                        
                        if(xmlFilePath==null || xmlFilePath.trim().length()==0){
                            continue;
                        }
                        String file = combineBaseAndRelativePath(baseFilePath, xmlFilePath);
                        
                        
                        if(file==null || file.trim().length()==0){
                            tempErrorMsg +="Not found the source input file: " + xmlFilePath;
                            continue;                            
                        }
                        
                        CidocCrmCompatibleFile newFile = new CidocCrmCompatibleFile(file, predicateUsage/*,predIdSeparator,predInverseSeperator*/);
                        
                        if (this.SourceInputFiles.contains(newFile) == false) {
                            SourceInputFiles.add(newFile);
                        }
                    }
                }
            }
            
            if(tempErrorMsg.length()>0){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "\r\n"+tempErrorMsg);
                return;
            }
            
            if(this.SourceInputFiles.size()==0){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "No input files declared.e.g:\r\n\t<SourceInputFiles>\n" +
                                            "\t\t<File QueryUsingInversePredicates=\"false\">C:/Users/tzortzak/Desktop/CultureBrokers/testdata/cidoc4.rdf</File>\n" +
                                            "\t</SourceInputFiles>    " );
                return;
            }
            
            System.out.println("\nSource Input Files set to:\n==================================================");
            for(int i=0;i<this.SourceInputFiles.size(); i++){
                String separator =" ";
                switch(this.SourceInputFiles.get(i).getPredicateDirectionUsage()){
                    case DIRECT:{
                        separator+=" ";
                        break;
                    }
                    case INVERSE:{                        
                        break;
                    }
                    case BOTH:{
                        separator+="   ";
                        break;
                    }
                    default:
                        break;
                }
                System.out.println((i+1)+".  Predicate Direction: " +this.SourceInputFiles.get(i).getPredicateDirectionUsage()+separator + this.SourceInputFiles.get(i).getFilePath());
            }
            
            //get target input files
            if(this.ComparisonMode == ApiConstants.TargetSourceChoice.FILE_COMPARISON){
                tempErrorMsg = "";
                if (this.XPathforTargetInputFiles != null) {
                    NodeList nodes = (NodeList) xpath.evaluate(this.XPathforTargetInputFiles, document, XPathConstants.NODESET);
                    if (nodes != null) {

                        int howmanyNodes = nodes.getLength();

                        for (int i = 0; i < howmanyNodes; i++) {

                            Node node = nodes.item(i);
                            String xmlFilePath = node.getTextContent();
                            ApiConstants.PredicateDirectionUsage predicateUsage = ApiConstants.PredicateDirectionUsage.BOTH;
                            
                            if(node.hasAttributes()){
                                Node attrNode = node.getAttributes().getNamedItem(this.AttributeOfInputFileForPredicateDirectionUsage);
                                if(attrNode!=null){
                                    String val = attrNode.getTextContent().trim().toLowerCase();
                                    for(int q=0; q< validPredicateDirectionUsage.length; q++){
                                        String checkStr = validPredicateDirectionUsage[q].toString().toLowerCase();
                                        if(checkStr.equals(val)){
                                            predicateUsage = validPredicateDirectionUsage[q];
                                        }
                                    }
                                }                            
                            }

                            if(xmlFilePath==null || xmlFilePath.trim().length()==0){
                                continue;
                            }
                            
                            String file = combineBaseAndRelativePath(baseFilePath, xmlFilePath);
                            
                            if(file==null || file.trim().length()==0){
                                tempErrorMsg +="Not found the target input file: " + xmlFilePath;
                                continue;                            
                            }

                            CidocCrmCompatibleFile newFile = new CidocCrmCompatibleFile(file, predicateUsage/*, predIdSeparator, predInverseSeperator*/);
                            if (this.TargetInputFiles.contains(newFile) == false) {
                                TargetInputFiles.add(newFile);
                            }                            
                        }
                    }
                }

                if(tempErrorMsg.length()>0){
                    this.setErrorMessage(ApiConstants.IMAPIFailCode, "\r\n"+tempErrorMsg);
                    return;
                }

                if(this.TargetInputFiles.size()==0){
                    this.setErrorMessage(ApiConstants.IMAPIFailCode, "No target files declared.e.g:\r\n\t<TargetInputFiles>\n" +
                                                "\t\t<File QueryUsingInversePredicates=\"false\">C:/Users/tzortzak/Desktop/CultureBrokers/testdata/cidoc4.rdf</File>\n" +
                                                "\t</TargetInputFiles>    " );
                    return;
                }
                
                System.out.println("\nTarget Input Files set to:\n==================================================");
                for(int i=0;i<this.TargetInputFiles.size(); i++){
                    String separator =" ";
                    switch(this.TargetInputFiles.get(i).getPredicateDirectionUsage()){
                        case DIRECT:{
                            separator+=" ";
                            break;
                        }
                        case INVERSE:{                        
                            break;
                        }
                        case BOTH:{
                            separator+="   ";
                            break;
                        }
                        default:
                            break;
                    }
                    System.out.println((i+1)+".  Predicate Direction: " +this.TargetInputFiles.get(i).getPredicateDirectionUsage()+separator+ this.TargetInputFiles.get(i).getFilePath());
                }
            }
            
            System.out.println();
            System.out.println("\nBaseClassID set to: " + UserConfigurationClass.getBaseConfigurationClass(userConfigurationXmlFile).replace("###CID:", "").replace("###", ""));
            
            Vector<String> foundSequences = new Vector<String>();
            Vector<String> skippedSequences = new Vector<String>();
            int userDefinedQueriesCount =0;
            if(this.XPathforTargetQueryConfigs!=null){
                NodeList nodes = (NodeList) xpath.evaluate(this.XPathforTargetQueryConfigs, document, XPathConstants.NODESET);
                if (nodes != null) {

                    int howmanyNodes = nodes.getLength();

                    for (int i = 0; i < howmanyNodes; i++) {
                        
                        String mnemonic = "";
                        Double weight =0d;
                        
                        Hashtable<String, String> params = new Hashtable<String,String>();
                        
                        Node xConfigNode = nodes.item(i);
                        
                        
                        if(xConfigNode.hasAttributes()){
                            Node xMnemonicNode = xConfigNode.getAttributes().getNamedItem(this.AttributeNameForMnemonic); 
                            if(xMnemonicNode!=null){
                                mnemonic = xMnemonicNode.getTextContent();
                                if(mnemonic!=null && mnemonic.length()>0){
                                    mnemonic = mnemonic.trim();
                                }
                            }
                            
                            Node xWeightNode = xConfigNode.getAttributes().getNamedItem(this.AttributeNameForWeight); 
                            if(xWeightNode!=null){
                                String weightStr = xWeightNode.getTextContent();
                                if(weightStr!=null && weightStr.length()>0){
                                    weight = Double.parseDouble(weightStr);
                                }
                            }                            
                        }
                        
                        NodeList parameterNodes = (NodeList) xpath.evaluate(this.subXPathForParamters, xConfigNode, XPathConstants.NODESET);
                        if(parameterNodes!=null){
                            for (int k = 0; k < parameterNodes.getLength(); k++) {
                                String paramName ="";
                                String paramValue ="";
                                Node paramNode = parameterNodes.item(k);
                                if(paramNode.hasAttributes()){
                                    Node nameNode = paramNode.getAttributes().getNamedItem(this.AttributeNameForParamName);
                                    if(nameNode!=null){
                                        paramName = nameNode.getTextContent().trim();
                                    }
                                }
                                paramValue = paramNode.getTextContent().trim();
                                
                                if(paramName.length()>0 && paramValue.length()>0 && params.containsKey(paramName)==false){
                                    params.put(paramName, paramValue);
                                }
                            }
                        }
                        
                        QueryPrototypeSequence checking = qClass.getSequenceFromMnemonic(mnemonic);
                        if(weight>0d && weight<=1d && checking==null){
                            this.setErrorMessage(ApiConstants.IMAPIFailCode, "Could Not find Query Prototype with mnemonic: \""+mnemonic+"\".\nValid mnemonics are:\n"+qClass.returnValidPrototypeMnemonics() );
                            return;
                        }
                        if(weight>0d && weight<=1d){
                            
                            UserQueryConfiguration newQClass = new UserQueryConfiguration(userDefinedQueriesCount++, checking, mnemonic, weight, params);
                            this.userQueryConfigs.add(newQClass);
                            foundSequences.add("User configuration sequence no: "+(newQClass.getPositionID()+1)+" with weight: " + weight+" and prototype mnemonic: \"" + mnemonic + "\" parameterized as: " + newQClass.getMnemonic());
                            
                        }
                        else{
                            if(weight<=0d){
                                
                                skippedSequences.add("Skipping zero weight Query Sequence with mnemonic " + mnemonic);
                                
                            }
                            else{
                                this.setErrorMessage(ApiConstants.IMAPIFailCode, "Error in Queries configuration "+mnemonic+"\nCheck Mnemonic parameters and weight>0 and <=1." );
                                return;
                            }
                        }
                    }
                }                    
            }  
            
            if(foundSequences.size()>0 || skippedSequences.size()>0){
                System.out.println("\nQuerieSequencesConfiguration:\n==================================================");
            }
            
            for(int i=0; i< foundSequences.size(); i++){
                System.out.println(foundSequences.get(i));
            }
            if(foundSequences.size()>0 && skippedSequences.size()>0){
                System.out.println();
            }
            for(int i=0; i< skippedSequences.size(); i++){
                System.out.println(skippedSequences.get(i));
            }
            
        } catch (ParserConfigurationException e) {
            printSampleUserConfigurationFile();
            String tempMsg = "ParserConfigurationException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            printSampleUserConfigurationFile();
            String tempMsg = "XPathExpressionException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            e.printStackTrace();
        } catch (SAXException e) {
            printSampleUserConfigurationFile();
            String tempMsg = "SAXException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            e.printStackTrace();
        } catch (IOException e) {
            printSampleUserConfigurationFile();
            String tempMsg = "IOException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private int errCode = ApiConstants.IMAPISuccessCode;

    int getErrorCode() {
        return this.errCode;
    }
    private String errorMessage = "";

    String getErrorMessage() {
        return this.errorMessage;
    }

    private void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;
        /*if (this.errCode == ApiConstants.IMAPIFailCode) {
            System.out.println("ERROR occurred: " + errorMsg);
        }*/
    }
    
    
    static void printSampleUserConfigurationFile(){
        
        System.out.println("Error Occured at User Configuration XML file.\r\nSample XML file:");
        System.out.println("------------------------------------------------------------\n\n");
        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<Root>\n" +
"    \n" +
"    <!-- ResultsThreshold: A decimal Value greater than 0.0 and less than or \n" +
"         equal to 1.0 determining the threshold that will be used in order \n" +
"         to qualify matches to the result set \n" +
"         Suggested value: 0.4\n" +
"    -->             \n" +
"    <ResultsThreshold>0.4</ResultsThreshold>    \n" +
"    \n" +
"    <!-- literalMinimumSimilarityValue: A decimal Value greater or equal to 0.0 \n" +
"         and less than or equal to 1.0 determining the threshold that will be \n" +
"         used in order to qualify matches between literals. Every literal with \n" +
"         a similarity value less that literalMinimumSimilarityValue will not \n" +
"         be kept in the result set. It's purpose is to avoid keeping pairs\n" +
"         with very small similarity especially when comparing with an online database.\n" +
"         Suggested value: 0.4\n" +
"    -->    \n" +
"    <literalMinimumSimilarityValue>0.4</literalMinimumSimilarityValue>\n" +
"    \n" +
"    \n" +
"    <!-- TargetSourceChoice: \n" +
"         - FILE_COMPARISON: In case comparison is intended to be performed \n" +
"           between, all SourceInputFiles and all TargetInputFiles.\n" +
"         - BRITISH_MUSEUM_COLLECTION: In case Comparison is intended to be \n" +
"           performed between, all SourceInputFiles and the online British Museum\n" +
"           collection SPARQL endpoint:\n" +
"           http://collection.britishmuseum.org/sparql\n" +
"         - CLAROS: In case Comparison is intended to be performed between,\n" +
"           all SourceInputFiles and the CLAROS SPARQL endpoint:\n" +
"           http://data.clarosnet.org/sparql/\n" +
"    \n" +
"        WARNING!!! CLAROS database does not support inference i.e. \n" +
"                   subClasses and subProperties are not included in the result \n" +
"                   set thus predicates used should be exactly these that are \n" +
"                   used in the database.\n" +
"                   \n" +
"                   i.e. If asking for E39_Actor this database will not also \n" +
"                        qualify the E21_Person instances as it should since \n" +
"                        E21 is subClassOf E39.\n" +
"                        Also, if asking for P1 predicate this database will \n" +
"                        not qualify the P131 predicates as it should since \n" +
"                        P131 is subPropertyOf P1.\n" +
"    -->\n" +
"    <TargetSourceChoice>BRITISH_MUSEUM_COLLECTION</TargetSourceChoice>\n" +
"    \n" +
"    <!-- SourceInputFiles / TargetInputFiles:\n" +
"        - These two elements contain the full path to the files that will be used\n" +
"          as the source and the target respectively. Files declared inside TargetInputFiles\n" +
"          elements will only be used if TargetSourceChoice is set to FILE_COMPARISON.\n" +
"          \n" +
"          It is recommended that all files contain the minimum set of namespaces\n" +
"          needed because the program will try to retrieve their schema and find the \n" +
"          exact name of the predicates that will be used in the queries.\n" +
"          \n" +
"                              \n" +
"          The attibute \"PredicateDirection\" may have the following values:\n" +
"          - \"direct\"\n" +
"          - \"inverse\"\n" +
"          - \"both\" (default value if none declared)\n" +
"          \n" +
"          and determines the direction of the predicates that will be used. \n" +
"          \n" +
"          i.e. if a query contains predicate with id P1 then:\n" +
"          - in case of \"direct\":  \"P1_is_identified_by\" will be used\n" +
"          - in case of \"inverse\": \"P1i_identifies\" will be used\n" +
"          - in case of \"both\":    \"P1_is_identified_by\" and \"P1i_identifies\" will be used\n" +
"          \n" +
"          \n" +
"          If inference is included in the cidoc compatible namespace of the file\n" +
"          then \"direct\" choice may be used safely otherwise \"both\" option should be chosen. \n" +
"         \n" +
"          e.g. The http://erlangen-crm.org/current/ cidoc compatible namespace\n" +
"               leads to an owl file that contains statements about inverse \n" +
"               predicates thus enabling inference in queries. So files using \n" +
"               this namespace may just use the \"direct\" PredicateDirection option.\n" +
"               \n" +
"               The http://www.cidoc-crm.org/cidoc-crm/ does not contain the \n" +
"               inverse property statemets so the \"both\" PredicateDirection option\n" +
"               must be used unless user is certain that only one of the two directions\n" +
"               is used throughout the file. If \"inverse\" option is selected then \n" +
"               inverse predicates will be used wherever possible meaning that for P3\n" +
"               predicate which does not have an inverse Property the P3_has_note will \n" +
"               be used. \n" +
"    -->\n" +
"    \n" +
"    <SourceInputFiles>\n" +
"        <File PredicateDirection=\"direct\">C:/Users/tzortzak/Desktop/CultureBrokers/testdata/cidoc1.rdf</File>        \n" +
"        <File PredicateDirection=\"both\">C:/Users/tzortzak/Desktop/CultureBrokers/testdata/cidoc4.rdf</File>\n" +
"    </SourceInputFiles>    \n" +
"    \n" +
"    <TargetInputFiles>        \n" +
"        <File PredicateDirection=\"both\">C:/Users/tzortzak/Desktop/CultureBrokers/testdata/cidoc6 - Copy.rdf</File> \n" +
"    </TargetInputFiles>\n" +
"    \n" +
"    <!-- \n" +
"    QuerieSequencesConfiguration:\n" +
"        \n" +
"        This XML element includes all the data used in order to extract the \n" +
"        information of interest. It includes the BaseClassID attribute and an \n" +
"        arbitary number of \"querysequence\" elements that will be used in order to \n" +
"        construct the queries that will be performed in each source (file or online db)\n" +
"        \n" +
"        \"BaseClassID\" Attribute: It defines the cidoc identifier of the instances \n" +
"                                 we are interested in and that will serve as the \n" +
"                                 base for every query performed.\n" +
"                                 Thus E39 stands for E39_Actor whereas E21 stands \n" +
"                                 for E21_Person.\n" +
"        \n" +
"        Suggested value: E21\n" +
"        \n" +
"        \n" +
"        \n" +
"        Attributes of \"querysequence\" Element:\n" +
"        \n" +
"        - weight:  a decimal value is provided here in order to define the \n" +
"                   influence of each similar pair to the overall similarity result.\n" +
"                   it's value is restricted to be greater than or equal to 0.0\n" +
"                   and less than or equal to 1.0\n" +
"                   \n" +
"                   sim_i: the similarity of two instances with different uris\n" +
"                          based on query sequence i\n" +
"                   w_i:   the weight of query sequence i\n" +
"                   \n" +
"                   The final similarity is calculated using the following algorithm:\n" +
"                   ( (w_1*sim_1)+(w_2*sim_2)+...+(w_n*sim_n) ) / (w_1+w_2+...+w_n)\n" +
"                   \n" +
"                   \n" +
"        - mnemonic: a character sequence used as id in order to define which query \n" +
"                    prototype sequence defined in QueryPrototypesConfiguration.xml \n" +
"                    will be used. \n" +
"                    \n" +
"                    Each query sequence defines a set of query steps denoted \n" +
"                    by letters a,b,c while multiple predicates used in each step \n" +
"                    are denoted with a numerical suffix to the step variable a1, a2 etc. \n" +
"                    Each query step is actually a query that results in a set of \n" +
"                    pairs with the uri of the instance of interst as the one part \n" +
"                    and the step result as the other part of the pair.\n" +
"                    \n" +
"                    For each query sequence similarity is calculated for every step.\n" +
"                    \n" +
"                    \n" +
"                    Currently there are supported 4 query sequence prototypes \n" +
"                    that are defined in QueryPrototypesConfiguration.xml:\n" +
"                    \n" +
"                    \n" +
"                    1. \"a -> literal\": 1 step defined step A\n" +
"                       step A:\n" +
"                       find all literals that are connected to the instances of\n" +
"                       interest (denoted by BaseClassID attribute above) \n" +
"                       via the predicate with id \"a\". \n" +
"                       \n" +
"                       E.g. a = P3 translates to:\n" +
"                            a = P3_has_note \n" +
"                        \n" +
"                    2. \"(a -> literal) | literal\": 1 step defined step A\n" +
"                       step A:\n" +
"                       find all literals that are connected to the instances of\n" +
"                       interest via the predicate with id \"a\" or the literals that\n" +
"                       are directly connected to each instance\n" +
"\n" +
"                       E.g. a = P1 translates to: \n" +
"                            a = P1_is_identified_by and /or P1_identifies\n" +
"                        \n" +
"                                                                      \n" +
"                    3. \"(a1 | a1 -> a2) -> b \": 2 steps defined step A and step B\n" +
"                       step A:\n" +
"                       find all uris that will be denoted as \"stepAuri\" that are \n" +
"                       connected to the instances of interest via the predicate \n" +
"                       with id \"a1\" or via the predicate sequence a1 and then a2.\n" +
"\n" +
"                       E.g. a1 = P14 and a2 =P9 translates to: \n" +
"                            a1 = P14_carried_out_by and /or P14i_performed\n" +
"                            a2 = P9_consists_of\n" +
"                            \n" +
"                       step B:\n" +
"                       find all uris that will be denoted as \"stepBuri\" that are\n" +
"                       connected to the instances of interest via the previous \n" +
"                       \"stepAuri\" results and are followed by the predicate with \n" +
"                       id \"b\" \n" +
"\n" +
"                       E.g. b = P108 translates to:\n" +
"                            b = P108_has_produced and /or P108i_was_produced_by\n" +
"                            \n" +
"                      Thus this query sequence with parameters \n" +
"                      a1=P14, a2=P9, b=P108 and BaseClassID=E21 \n" +
"                      will retrieve the uri pairs of persons and objects where \n" +
"                      the person has participated in the production event of the object.\n" +
"                                            \n" +
"                    \n" +
"                    4. \"a -> b -> timespan(c1 - c2)\" 3 steps defined step A step B and step C\n" +
"                    step A:\n" +
"                       find all uris that will be denoted as \"stepAuri\" that are \n" +
"                       connected to the instances of interest via the predicate \n" +
"                       with id \"a\"\n" +
"\n" +
"                       E.g. a = P98 translates to: \n" +
"                            a = P98_brought_into_life and /or P98i_was_born\n" +
"                            \n" +
"                            \n" +
"                       step B:\n" +
"                       find all uris that will be denoted as \"stepBuri\" that are\n" +
"                       connected to the instances of interest via the previous \n" +
"                       \"stepAuri\" results and are followed by the predicate with \n" +
"                       id \"b\" \n" +
"\n" +
"                       E.g. b = P4 translates to:\n" +
"                            b = P4_has_time-span and /or P4i_is_time-span_of\n" +
"                       \n" +
"                       step C:\n" +
"                       find all timespans defined by c1 - c2 that will be denoted \n" +
"                       as \"stepCtimespan\" which are connected to the instances of \n" +
"                       interest via the previous \"stepAuri\" results followed by \n" +
"                       the \"stepBuri\" results and followed by the predicates \n" +
"                       with id \"c1\" and \"c2\" \n" +
"\n" +
"                       E.g. c1 = P82a and c2=P82b translates to:\n" +
"                            c1 = P82a_begin_of_the_begin \n" +
"                            c2 = P82b_end_of_the_end\n" +
"                            \n" +
"                      Thus this query sequence with parameters \n" +
"                      a=P98, b=P4, c1=P82a, c2=P82b and BaseClassID=E21 \n" +
"                      will retrieve the uri pairs of persons and timespans that \n" +
"                      their birth date is estimated to be.                   \n" +
"        \n" +
"    -->\n" +
"    <QuerieSequencesConfiguration BaseClassID=\"E39\">\n" +
"        <querysequence weight=\"0.3\" mnemonic=\"a -> b -> timespan(c1 - c2)\">\n" +
"            <parameter name=\"a\">P98</parameter>            \n" +
"            <parameter name=\"b\">P4</parameter>            \n" +
"            <parameter name=\"c1\">P82a</parameter>            \n" +
"            <parameter name=\"c2\">P82b</parameter>            \n" +
"        </querysequence>        \n" +
"        \n" +
"        <querysequence weight=\"0.2\" mnemonic=\"(a -> label) | label\">\n" +
"            <parameter name=\"a\">P131</parameter>            \n" +
"        </querysequence>\n" +
"        \n" +
"        <querysequence weight=\"0.1\" mnemonic=\"a -> label\">\n" +
"            <parameter name=\"a\">P3</parameter>            \n" +
"        </querysequence>        \n" +
"\n" +
"        <querysequence weight=\"0.4\" mnemonic=\"(a1 | a1 -> a2) -> b\">\n" +
"            <parameter name=\"a1\">P14</parameter>\n" +
"            <parameter name=\"a2\">P9</parameter>\n" +
"            <parameter name=\"b\">P108</parameter>\n" +
"        </querysequence>\n" +
"        \n" +
"    </QuerieSequencesConfiguration>\n" +
"</Root>");                
    }
    
}
