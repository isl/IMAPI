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
package imapi;

import imapi.ApiConstants;


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
    
    private final String XPathForMinimumLiteralSimilarity = "/Root/literalMinimumSimilarityValue";
    private static final String XPathforBaseClass = "/Root/QuerySequencesConfiguration/@BaseClassID";
    
    private Double minimumLiterlSimilarity = 0d;
    private Double ResultsThreshold = 0d;    
    private Vector<UserQueryConfiguration> userQueryConfigs = new Vector<UserQueryConfiguration>();
    private ApiConstants.TargetSourceChoice ComparisonMode; 
    private Vector<CidocCrmCompatibleFile> SourceInputFiles = new Vector<CidocCrmCompatibleFile>();    
    private Vector<CidocCrmCompatibleFile> TargetInputFiles = new Vector<CidocCrmCompatibleFile>();
    
    
    private int errCode = ApiConstants.IMAPISuccessCode;
    private String errorMessage = "";

    public int getErrorCode() {
        return this.errCode;
    }
    public String getErrorMessage() {
        return this.errorMessage;
    }
    private void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;     
    }
    
    
    public Double getMinimumLiteralSimilarity(){
        return this.minimumLiterlSimilarity;
    }
    
    public int getNumberOfSequences(){
        return this.userQueryConfigs.size();
    }
    
    public Vector<UserQueryConfiguration> getUserQueriesCopy(){
        Vector<UserQueryConfiguration> returnVec  = new Vector<UserQueryConfiguration>();
        for(int i=0; i<this.userQueryConfigs.size(); i++){
            returnVec.add(this.userQueryConfigs.get(i).copy());
        }
        
        return returnVec;
    }
    
    public double getWeightAtUserQueryIndex(int index){
        if(index>=0 && index< this.userQueryConfigs.size()){
            return this.userQueryConfigs.get(index).getWeight();
        }
        else{
            return 0d;
        }
    }
    
    public String getMnemonicAtUserQueryIndex(int index){
        if(index>=0 && index< this.userQueryConfigs.size()){
            return this.userQueryConfigs.get(index).getMnemonic();
        }
        else{
            return "";
        }
    }
    
    public Double getResultsThreshold(){
        return this.ResultsThreshold;
    }
    
    public ApiConstants.TargetSourceChoice getComparisonMode(){
        return this.ComparisonMode;
    }
    
    public Vector<CidocCrmCompatibleFile> getSourceInputFiles(){
        return new Vector<CidocCrmCompatibleFile>(this.SourceInputFiles);
    }
    
    public Vector<CidocCrmCompatibleFile> getTargetInputFiles(){
        return new Vector<CidocCrmCompatibleFile>(this.TargetInputFiles);
    }
    
    public static String getBaseConfigurationClass(String userConfigurationXmlFile){
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
            Utilities.handleException(e);
        } catch (XPathExpressionException e) {
            Utilities.handleException(e);
        } catch (SAXException e) {
            Utilities.handleException(e);
        } catch (IOException e) {
            Utilities.handleException(e);
        }  
        
        
        return "";
    }

    private String combineBaseAndRelativePath(String basePath, String relativePath){
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
    
    public UserConfigurationClass(String baseFilePath, String userConfigurationXmlFile, QueryPrototypeConfigurationClass qClass){
        
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
            
            String tempMsg = "ParserConfigurationException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            Utilities.handleException(e);
        } catch (XPathExpressionException e) {
            
            String tempMsg = "XPathExpressionException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            Utilities.handleException(e);
        } catch (SAXException e) {
            
            String tempMsg = "SAXException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            Utilities.handleException(e);
        } catch (IOException e) {
            
            String tempMsg = "IOException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            Utilities.handleException(e);
        }
        System.out.println();
    }
    
    
    
}
