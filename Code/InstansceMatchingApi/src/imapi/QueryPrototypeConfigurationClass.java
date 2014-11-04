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


import java.io.File;
import java.io.IOException;
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
class QueryPrototypeConfigurationClass {
    
    private final String XPathForCountAllInstances = "/Root/CountAllInstances/query";
    private final String XPathforIncludeNameSpaces = "/Root/IncludeNamespaces/Namespace";
    private final String XPathforCRMExtentionsNsForReplacement = "/Root/NamespaceExtensions/@TargetNamespaseReplaceMent";
    private final String XPathforCRMExtentions = "/Root/NamespaceExtensions";
    private final String XPathforValidCRMNamespaces = "/Root/ValidCidocNamespaces/Namespace";
    
    private final String baseClassPlaceHolder = "###BaseClass###";
    private final String idPredicatePlaceHolder = "###IDPREDICATE###";
    
    
    private final String XPathforGetAllInstances = "/Root/GetAllInstances/query";
    private final String AttributeForQueryLang = "lang";
    
    private final String XPathForQuerySequences ="/Root/QuerySequencesConfiguration/QuerySequence";
    private final String AttributeForQuerySequenceMnemonic = "mnemonic";
    
    private final String subXpathForEachSequenceStep = "QueryStep";
    private final String AttributeNameEachSequenceStepParamName = "name";
    private final String AttributeNameEachSequenceStepParamDatatype = "datatype";
    
    
        
    private final String XPathforQueryLimit = "/Root/ApiConfigs/QueryLimitSize";
    private final String XPathforInstanceUrisFilterStepCount = "/Root/ApiConfigs/QueryFilteringInstancesCount";
    private final String XPathforValueUrisFilterStepCount = "/Root/ApiConfigs/QueryFilteringValuesCount";
    
    //default values assigned
    private int queryLimitSize = 4000;    
    private int instanceUrisFilterStepCount = 500;
    private int valueUrisFilterStepCount = 10;
    
    int getQueryLimitSize(){
        return queryLimitSize;
    }
    
    int getQueryFilteringInstancesCount(){
        return instanceUrisFilterStepCount;
    }
    
    int getQueryFilteringValuesCount(){
        return valueUrisFilterStepCount;
    }
    
    private Hashtable<String,String> getAllInstancesQuery = new Hashtable<String,String>();
    private Vector<QueryPrototypeSequence> querySequencePrototypes = new Vector<QueryPrototypeSequence>();    
    private Vector<String> includeNamespaces = new Vector<String>();
    private Vector<String> validCrmNamespaces = new Vector<String>();
    private String extentionsCrmReplacement="";
    private String extentionsCrmRDF="";
    
    
    private int errCode = ApiConstants.IMAPISuccessCode;
    private String errorMessage = "";
    
    int getErrorCode() {
        return this.errCode;
    }
    String getErrorMessage() {
        return this.errorMessage;
    }
    private void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;        
    }
    
    
    String getCrmExtentionsString(String targetNamespace){
        return this.extentionsCrmRDF.replaceAll(this.extentionsCrmReplacement, targetNamespace);
    }
    
    Vector<String> getValidCrmNamespaces() {
        return this.validCrmNamespaces;
    }

    Vector<String> getIncludeNamespaces(){
        Vector<String> returnVal = new Vector<String>(this.includeNamespaces);
        
        returnVal.addAll(this.validCrmNamespaces);
        return returnVal;
    }
    
    private String countAllInstancesQuery = "";
    String getCountAllInstancesQuery(){
        return this.countAllInstancesQuery;
    }
    
    QueryPrototypeSequence getSequenceFromMnemonic(String mnemonic){
        
        for(int i=0; i< this.querySequencePrototypes.size();i++)
        {
            if(this.querySequencePrototypes.get(i).getMnemonic().trim().toLowerCase().equals(mnemonic.trim().toLowerCase()))
            {
                return this.querySequencePrototypes.get(i);
            }
        }
        return null;
    }
    
    String returnValidPrototypeMnemonics(){
        String str = "";
        if(this.querySequencePrototypes!=null && this.querySequencePrototypes.size()>0){
            for(int i=0; i< this.querySequencePrototypes.size(); i++){
                str+="\""+this.querySequencePrototypes.get(i).getMnemonic().trim()+"\"\n";
            }
        }
        return str;
    }
    
    String getTheAllInstancesQuery(String key, String idPredicateStr){
        
        String returnStr = "";
        if(this.getAllInstancesQuery.containsKey("default")){
            returnStr =  this.getAllInstancesQuery.get("default");
        }
        /*
        if(this.getAllInstancesQuery.containsKey(key)){
            returnStr =  this.getAllInstancesQuery.get(key);
        }
        else{
            if(this.getAllInstancesQuery.containsKey("default")){
                returnStr =  this.getAllInstancesQuery.get("default");
            }
            
        }   
        */
        returnStr = returnStr.replace(idPredicatePlaceHolder, idPredicateStr);
        
        return returnStr;
    }
    
    
   
    
    QueryPrototypeConfigurationClass(String baseClassIdStr, String queriesConfigurationXmlFile){
        
                
        //make some initial checks to the xml file given as input
        if (queriesConfigurationXmlFile == null || queriesConfigurationXmlFile.trim().length() == 0) {
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "Empty path given for the Query Weights Configuration xml file path.");
            return;
        }

        if (queriesConfigurationXmlFile.toLowerCase().endsWith(".xml") == false) {
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "Query Weights Configuration file path should be an xml file.");
            return;
        }
        
        File f = new File(queriesConfigurationXmlFile.trim());
        if (f.exists() == false || f.isDirectory()) {
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "Query Weights Configuration File not found at path: " + queriesConfigurationXmlFile);
            return;
        }
        
        //baseClassId ="";
        getAllInstancesQuery = new Hashtable<String,String>();

        this.querySequencePrototypes = new Vector<QueryPrototypeSequence>();
        this.extentionsCrmReplacement="";
        this.extentionsCrmRDF="";
        this.validCrmNamespaces = new Vector<String>();
        this.includeNamespaces = new Vector<String>();
        this.countAllInstancesQuery = "";
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            
            Document document = builder.parse(queriesConfigurationXmlFile);
            XPath xpath = XPathFactory.newInstance().newXPath();

            //get apiconfigs for queries
            if(this.XPathforQueryLimit !=null){
                Node node = (Node) xpath.evaluate(this.XPathforQueryLimit, document, XPathConstants.NODE);
                if (node != null) {
                    String queryLimitStr = node.getTextContent();                    
                    this.queryLimitSize = Integer.parseInt(queryLimitStr);  
                    if(IMAPIClass.DEBUG){
                        System.out.println("Setting QueryLimitSize to " + this.queryLimitSize);
                    }
                }
            }
            
            //get apiconfigs for queries
            if(this.XPathforInstanceUrisFilterStepCount !=null){
                Node node = (Node) xpath.evaluate(this.XPathforInstanceUrisFilterStepCount, document, XPathConstants.NODE);
                if (node != null) {
                    String instFilterCountStr = node.getTextContent();                    
                    this.instanceUrisFilterStepCount = Integer.parseInt(instFilterCountStr);            
                    if(IMAPIClass.DEBUG){
                        System.out.println("Setting QueryFilteringInstancesCount to " + this.instanceUrisFilterStepCount);
                    }
                }
            }
            
            //get apiconfigs for queries
            if(this.XPathforValueUrisFilterStepCount !=null){
                Node node = (Node) xpath.evaluate(this.XPathforValueUrisFilterStepCount, document, XPathConstants.NODE);
                if (node != null) {
                    String valuesFilterCountStr = node.getTextContent();                    
                    this.valueUrisFilterStepCount = Integer.parseInt(valuesFilterCountStr);                    
                    if(IMAPIClass.DEBUG){
                        System.out.println("Setting QueryFilteringValuesCount to " + this.valueUrisFilterStepCount);
                    }
                }
            }
            
            //get Valid CRM Namespaces
            if (this.XPathforValidCRMNamespaces != null) {
                NodeList nodes = (NodeList) xpath.evaluate(this.XPathforValidCRMNamespaces, document, XPathConstants.NODESET);
                if (nodes != null) {

                    int howmanyNodes = nodes.getLength();

                    for (int i = 0; i < howmanyNodes; i++) {

                        String nspace = nodes.item(i).getTextContent();

                        if (validCrmNamespaces.contains(nspace) == false) {
                            validCrmNamespaces.add(nspace);
                        }
                    }
                }
            }
            

            if (validCrmNamespaces.size() == 0) {
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "No valid Cidoc Crm namespace provided in configuration file");                
                return;
            }
            
            //get Problematic Namespaces to skip
            if (this.XPathforIncludeNameSpaces != null) {
                NodeList nodes = (NodeList) xpath.evaluate(this.XPathforIncludeNameSpaces, document, XPathConstants.NODESET);
                if (nodes != null) {

                    int howmanyNodes = nodes.getLength();

                    for (int i = 0; i < howmanyNodes; i++) {

                        String nspace = nodes.item(i).getTextContent();

                        if (includeNamespaces.contains(nspace) == false) {
                            includeNamespaces.add(nspace);
                        }
                    }
                }
            }
            
            if(this.XPathforCRMExtentionsNsForReplacement!=null){
                
                Node node = (Node) xpath.evaluate(this.XPathforCRMExtentionsNsForReplacement, document, XPathConstants.NODE);
                if (node != null) {

                    this.extentionsCrmReplacement = node.getTextContent();
                    //System.out.println(this.extentionsCrmReplacement);
                }                
            }
            
            
            if(this.XPathforCRMExtentions!=null){
                Node node = (Node) xpath.evaluate(this.XPathforCRMExtentions, document, XPathConstants.NODE);
                if (node != null) {

                    
                    this.extentionsCrmRDF = node.getTextContent().trim();
                    //System.out.println(this.extentionsCrmRDF);
                }                
            }
            
            if(this.XPathForCountAllInstances!=null){
                Node xNode = (Node) xpath.evaluate(this.XPathForCountAllInstances,document, XPathConstants.NODE);
                if(xNode!=null){
                    this.countAllInstancesQuery = xNode.getTextContent().replaceAll(this.baseClassPlaceHolder, baseClassIdStr).trim();
                }
            }
            
            //get The query that gets all instances
            if(this.XPathforGetAllInstances!=null)
            {
                NodeList xNodes = (NodeList) xpath.evaluate(this.XPathforGetAllInstances, document, XPathConstants.NODESET);
                if (xNodes != null) {
                    int howmany = xNodes.getLength();
                    
                    for(int i=0; i< howmany; i++){
                        Node xNode = xNodes.item(i);
                        String dbType ="default";
                        
                        if(xNode.hasAttributes()){
                            Node xDBTypeNode = xNode.getAttributes().getNamedItem("dbType");
                            if(xDBTypeNode !=null){
                                String tempStr = xDBTypeNode.getTextContent();
                                if(tempStr!=null && tempStr.trim().length()>0){
                                    dbType = tempStr.trim().toLowerCase();
                                }
                            }
                        }
                        String query =xNode.getTextContent().replaceAll(this.baseClassPlaceHolder, baseClassIdStr).trim();
                        
                        
                        if(query==null || query.length()==0){
                            continue;
                        }
                        if(this.getAllInstancesQuery.containsKey(dbType)==false){
                            this.getAllInstancesQuery.put(dbType, query);
                        }
                    }
                    
                }
            }
            
            if(this.getAllInstancesQuery.size()==0 ){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "Get All Instances query not correctly set.");                
                return;
            }
            
            
            
            //get Query Sequences
            if(this.XPathForQuerySequences!=null){
                NodeList xList = (NodeList) xpath.evaluate(this.XPathForQuerySequences, document, XPathConstants.NODESET);
                int howmany = xList.getLength();
                //int qSeqIndex =1;
                for(int k=0; k < howmany; k++){
                    
                    String mnemonic ="";
                    Vector<QueryPrototypeSequenceStep> qSteps = new Vector<QueryPrototypeSequenceStep>();
                    
                    Node xNode = xList.item(k);                    
                    if(xNode.hasAttributes()){
                        
                        Node xMnemonicNode = xNode.getAttributes().getNamedItem(this.AttributeForQuerySequenceMnemonic);
                        if(xMnemonicNode!=null){
                            mnemonic = xMnemonicNode.getTextContent();
                        }
                    }
                    
                    //NodeList 
                    NodeList xQStepsList = (NodeList) xpath.evaluate(this.subXpathForEachSequenceStep, xNode, XPathConstants.NODESET);
                    if(xQStepsList!=null){
                        int stepsCount = xQStepsList.getLength();
                        int stepIndex = 0;
                        String parameterName = "";
                        String parameterType = "";
                        String query ="";
                        
                        for(int m=0; m< stepsCount; m++){
                            
                            Node xStep = xQStepsList.item(m);
                            
                            if(xStep.hasAttributes()){
                                
                                Node xAttributeParamName = xStep.getAttributes().getNamedItem(this.AttributeNameEachSequenceStepParamName);
                                if(xAttributeParamName!=null){
                                    parameterName = xAttributeParamName.getTextContent();
                                }
                                
                                Node xAttributeParamType  = xStep.getAttributes().getNamedItem(this.AttributeNameEachSequenceStepParamDatatype);
                                if(xAttributeParamType!=null){
                                    parameterType = xAttributeParamType.getTextContent();
                                }
                            }
                            String queryStr = xStep.getTextContent();
                                
                            if(queryStr.length()>0){
                                query = queryStr.replaceAll(this.baseClassPlaceHolder, baseClassIdStr).trim();                                
                            }
                            
                            if(parameterName.length()==0){
                                System.out.println("Encountered query step with no parameter Name. Skipping Query Step..");
                                continue;
                            }
                            if(parameterType.length()==0){
                                System.out.println("Encountered query step with no parameter type. Skipping Query Step..");
                                continue;
                            }
                            if(query.length()==0){
                                System.out.println("Encountered query step with no query defined. Skipping Query Step..");
                                continue;
                            }
                            
                            QueryPrototypeSequenceStep qStep = new QueryPrototypeSequenceStep(stepIndex++, parameterName,parameterType,query);
                            
                            qSteps.add(qStep);
                        }
                    }
                    
                    if(mnemonic.length()==0){
                        System.out.println("Encountered sequence prototype with no mnemonic. Skipping Sequence..");
                        continue;
                    }
                    
                    if(qSteps.size()==0){
                        System.out.println("Encountered sequence prototype with no query steps. Skipping Sequence..");
                        continue;
                    }
                    
                    QueryPrototypeSequence seq = new QueryPrototypeSequence(mnemonic,qSteps);
                    
                    this.querySequencePrototypes.add(seq);                    
                }
            }

            //Make the necessary checks for Query Sequences
            if(this.querySequencePrototypes.size()==0){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "No user Sequences defined.");                
                return;
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
    }
    
}
