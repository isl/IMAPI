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

import static imapi.UserConfigurationClass.printSampleUserConfigurationFile;
import java.io.File;
import java.io.IOException;
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
    
    
    private final String XPathforCRMExtentionsNsForReplacement = "/Root/NamespaceExtensions/@TargetNamespaseReplaceMent";
    private final String XPathforCRMExtentions = "/Root/NamespaceExtensions";
    private final String XPathforValidCRMNamespaces = "/Root/ValidCidocNamespaces/Namespace";
    
    private final String baseClassPlaceHolder = "###BaseClass###";
    
    
    private final String XPathforGetAllInstances = "/Root/GetAllInstances/query";
    private final String AttributeForQueryLang = "lang";
    
    
    
    private final String XPathForQuerySequences ="/Root/QuerySequencesConfiguration/QuerySequence";
    private final String AttributeForQuerySequenceMnemonic = "mnemonic";
    
    private final String subXpathForEachSequenceStep = "QueryStep";
    private final String AttributeNameEachSequenceStepParamName = "name";
    private final String AttributeNameEachSequenceStepParamDatatype = "datatype";
    private final String subXpathForEachSequenceStepQuery = "query";
    private final String AttributeForEachSequenceStepQueryLang = "lang";
    //<QueryStep name="P131appellationURI" datatype="uri">
    //      <query lang="sparql">
    
    
    //private String baseClassId ="";
    private String getAllInstancesQuery = "";
    
    
    private Vector<QueryPrototypeSequence> querySequencePrototypes = new Vector<QueryPrototypeSequence>();
    
    private Vector<String> validCrmNamespaces = new Vector<String>();
    private String extentionsCrmReplacement="";
    private String extentionsCrmRDF="";
    
    String getCrmExtentionsString(String targetNamespace){
        return this.extentionsCrmRDF.replaceAll(this.extentionsCrmReplacement, targetNamespace);
    }
    
    Vector<String> getValidCrmNamespaces() {
        return this.validCrmNamespaces;
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
    
    String getTheAllInstancesQuery(){
        
        return this.getAllInstancesQuery;
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
        getAllInstancesQuery = "";
        this.querySequencePrototypes = new Vector<QueryPrototypeSequence>();
        this.extentionsCrmReplacement="";
        this.extentionsCrmRDF="";
        this.validCrmNamespaces = new Vector<String>();
        
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            
            Document document = builder.parse(queriesConfigurationXmlFile);
            XPath xpath = XPathFactory.newInstance().newXPath();

            /*
            //getBaseClass id
            if(this.XPathforBaseClass!=null)
            {
                Node xNode = (Node) xpath.evaluate(this.XPathforBaseClass, document, XPathConstants.NODE);
                if (xNode != null) {
                    this.baseClassId = xNode.getTextContent();
                }
            }
            
            if(this.baseClassId.length()==0 || this.baseClassId.matches("(?i)###CID:[^#]*###") ==false){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "BaseClass Identifier not correctly set. e.g.<BaseClass>###CID:E39###</BaseClass>");                
                return;
            }
            */
            
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
                ApiConfigClass.printSampleConfigurationFile();
                return;
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
            
            //get The query that gets all instances
            if(this.XPathforGetAllInstances!=null)
            {
                Node xNode = (Node) xpath.evaluate(this.XPathforGetAllInstances, document, XPathConstants.NODE);
                if (xNode != null) {
                    this.getAllInstancesQuery = xNode.getTextContent().replaceAll(this.baseClassPlaceHolder, baseClassIdStr).trim();
                }
            }
            
            if(this.getAllInstancesQuery.length()==0 ){
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
                            
                            NodeList xQueries = (NodeList) xpath.evaluate(this.subXpathForEachSequenceStepQuery, xStep, XPathConstants.NODESET);
                            
                            int qCount = xQueries.getLength();
                            for(int p=0; p < qCount; p++){
                                
                                Node qNode = xQueries.item(p);
                                
                                String queryStr = qNode.getTextContent();
                                
                                if(queryStr.length()>0){
                                    query = queryStr.replaceAll(this.baseClassPlaceHolder, baseClassIdStr).trim();
                                    break;
                                }
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
            printSampleQueryWeightsConfigurationFile();
            String tempMsg = "ParserConfigurationException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            printSampleQueryWeightsConfigurationFile();
            String tempMsg = "XPathExpressionException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            e.printStackTrace();
        } catch (SAXException e) {
            printSampleQueryWeightsConfigurationFile();
            String tempMsg = "SAXException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            e.printStackTrace();
        } catch (IOException e) {
            printSampleQueryWeightsConfigurationFile();
            String tempMsg = "IOException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            e.printStackTrace();
        }  
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
    
    
    static void printSampleQueryWeightsConfigurationFile(){
        System.out.println("Error Occured at Query Weight Configuration XML file.\r\nSample XML file:");
        System.out.println("------------------------------------------------------------\n\n");
        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<Root>        \n" +
"    <GetAllInstances>\n" +
"         <query lang=\"sparql\">\n" +
"                    <![CDATA[                     \n" +
"SELECT DISTINCT ?resultInstaceUri \n" +
"{\n" +
"    ?resultInstaceUri a ###BaseClass###.    \n" +
"    ###FILTERURISTATEMENT###\n" +
"}\n" +
"ORDER BY ?resultInstaceUri \n" +
"                    ]]>\n" +
"            </query>\n" +
"    </GetAllInstances>\n" +
"    <QuerySequencesConfiguration>\n" +
"        <QuerySequence mnemonic=\"a -> literal\">\n" +
"            <QueryStep name=\"stepAliteral\" datatype=\"literal\">\n" +
"                <query lang=\"sparql\">\n" +
"                   <![CDATA[ \n" +
"                                       \n" +
"SELECT DISTINCT ?resultInstaceUri ?stepAliteral\n" +
"{\n" +
"    ?resultInstaceUri a ###BaseClass### .\n" +
"    ?stepAliteral ###UNSETPID:a### ?resultInstaceUri .                   \n" +
"    \n" +
"}\n" +
"ORDER BY ?resultInstaceUri ?stepAliteral\n" +
"\n" +
"                    ]]>\n" +
"                    </query>                \n" +
"            </QueryStep>\n" +
"        </QuerySequence>\n" +
"        <QuerySequence mnemonic=\"(a -> literal) | literal\">\n" +
"            <QueryStep name=\"stepAuri\" datatype=\"uri\">\n" +
"                <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"                    \n" +
"SELECT DISTINCT ?resultInstaceUri ?stepAuri\n" +
"{\n" +
"    ?resultInstaceUri a ###BaseClass### .\n" +
"    ###FILTERURISTATEMENT###\n" +
"    \n" +
"    ?stepAuri ###UNSETPID:a### ?resultInstaceUri .        \n" +
"    \n" +
"}\n" +
"ORDER BY ?resultInstaceUri ?stepAuri\n" +
"                    ]]>\n" +
"                </query>                \n" +
"            </QueryStep>\n" +
"            <QueryStep name=\"stepBliteral\" datatype=\"literal\">\n" +
"                    <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"                    \n" +
"SELECT DISTINCT ?resultInstaceUri ?stepBliteral\n" +
"{\n" +
"    ?resultInstaceUri a ###BaseClass### .\n" +
"    {		\n" +
"        ?resultInstaceUri rdfs:label ?stepBliteral .\n" +
"    }\n" +
"    UNION \n" +
"    {    \n" +
"        ?stepAuri ###UNSETPID:a### ?resultInstaceUri .           \n" +
"        ?stepAuri rdfs:label ?stepBliteral .\n" +
"    }  	    \n" +
"}\n" +
"ORDER BY ?resultInstaceUri ?stepBliteral\n" +
"\n" +
"                    ]]>\n" +
"                    </query>                \n" +
"            </QueryStep>\n" +
"        </QuerySequence>\n" +
"        <QuerySequence mnemonic=\"(a1 | a1 -> a2) -> b\">\n" +
"            \n" +
"            <QueryStep name=\"stepAuri\" datatype=\"uri\">\n" +
"                <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"\n" +
"SELECT DISTINCT ?resultInstaceUri ?stepAuri\n" +
"{\n" +
"    ?resultInstaceUri a ###BaseClass### .\n" +
"    ###FILTERURISTATEMENT###\n" +
"    {\n" +
"        ?stepAuri ###UNSETPID:a1### ?resultInstaceUri .\n" +
"    }\n" +
"    UNION\n" +
"    {\n" +
"            ?result1 ###UNSETPID:a1### ?resultInstaceUri .\n" +
"            ?result1 ###UNSETPID:a2### ?stepAuri .                       \n" +
"    }\n" +
"}\n" +
"ORDER BY ?resultInstaceUri ?stepAuri\n" +
"                        ]]>\n" +
"                </query>\n" +
"            </QueryStep>\n" +
"            \n" +
"            <QueryStep name=\"stepBuri\" datatype=\"uri\">\n" +
"                <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"\n" +
"SELECT DISTINCT ?resultInstaceUri ?stepBuri\n" +
"{\n" +
"    ?resultInstaceUri a ###BaseClass### .\n" +
"    ###FILTERURISTATEMENT###\n" +
"    {\n" +
"        {\n" +
"            ?stepAuri ###UNSETPID:a1### ?resultInstaceUri .\n" +
"        }\n" +
"        UNION\n" +
"        {\n" +
"                ?result1 ###UNSETPID:a1### ?resultInstaceUri .\n" +
"                ?result1 ###UNSETPID:a2### ?stepAuri .                \n" +
"        }\n" +
"    }\n" +
"    ?stepAuri ###UNSETPID:b### ?stepBuri .\n" +
"    \n" +
"}\n" +
"ORDER BY ?resultInstaceUri ?stepBuri\n" +
"                        ]]>\n" +
"                </query>\n" +
"            </QueryStep>    \n" +
"            \n" +
"               \n" +
"        </QuerySequence>\n" +
"        <QuerySequence mnemonic=\"a -> b -> timespan(c1 - c2)\">\n" +
"            <QueryStep name=\"stepAuri\" datatype=\"uri\">\n" +
"                <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"SELECT DISTINCT  ?actor ?stepAuri\n" +
"WHERE \n" +
"{\n" +
"    ?actor a ###BaseClass### .    \n" +
"    ###FILTERURISTATEMENT###\n" +
"    ?actor ###UNSETPID:a### ?stepAuri .    \n" +
"}\n" +
"ORDER BY  ?actor ?stepAuri     \n" +
"\n" +
"            ]]>\n" +
"                </query> \n" +
"            </QueryStep>\n" +
"            <QueryStep name=\"stepBuri\" datatype=\"uri\">\n" +
"                <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"SELECT DISTINCT  ?actor ?stepBuri\n" +
"WHERE \n" +
"{\n" +
"    ?actor a ###BaseClass### .\n" +
"    ###FILTERURISTATEMENT###\n" +
"    ?actor ###UNSETPID:a### ?stepAuri .\n" +
"    ?stepAuri ###UNSETPID:b### ?stepBuri .\n" +
"    \n" +
"}\n" +
"ORDER BY  ?actor ?stepBuri     \n" +
"\n" +
"            ]]>\n" +
"                </query> \n" +
"            </QueryStep>\n" +
"            <QueryStep name=\"stepCtimespan\" datatype=\"timespan\">\n" +
"                    <query lang=\"sparql\">\n" +
"                    <![CDATA[ \n" +
"SELECT DISTINCT  ?actor ( CONCAT(CONCAT(IF(bound(?beginParam), STR(?beginParam), \"\"), \" - \"), IF(bound(?endParam), STR(?endParam), \"\") ) AS ?stepCtimespan) \n" +
"WHERE \n" +
"{\n" +
"    ?actor a ###BaseClass### .\n" +
"    ?actor ###UNSETPID:a### ?stepAuri .\n" +
"    ?stepAuri ###UNSETPID:b### ?stepBuri .\n" +
"    OPTIONAL{ ?stepBuri ###UNSETPID:c1### ?beginParam . }\n" +
"    OPTIONAL{ ?stepBuri ###UNSETPID:c2### ?endParam . }\n" +
"    FILTER ( (!BOUND(?beginParam) && BOUND(?endParam) ) || (!BOUND(?endParam) && BOUND(?beginParam) ) ||  ?beginParam <= ?endParam) .                    \n" +
"  \n" +
"    \n" +
"}\n" +
"ORDER BY  ?actor ?stepCtimespan     \n" +
"\n" +
"            ]]>\n" +
"                    </query> \n" +
"            </QueryStep>\n" +
"        </QuerySequence>\n" +
"        \n" +
"    </QuerySequencesConfiguration>    \n" +
"    \n" +
"</Root>");
    }
    
    
    
}
