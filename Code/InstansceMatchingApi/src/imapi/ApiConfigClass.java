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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author tzortzak
 */
class ApiConfigClass {

    private final String XPathforSupportedOnlineDbs = "/Root/SupportedDatabases/OnlineDatabase";
    
    
    private final String OnlineDbAttributeNameForId = "sourceid";
    private final String OnlineDbElementNameForName = "Name";
    private final String OnlineDbElementNameForSparqlEndpoint = "SparqlEndpoint";
    private final String OnlineDbSubPathForNamespaces = "QueryNamespaces/Namespace";
    private final String OnlineDbNamespaceAttributeNameForPrefix = "prefix";
    private final String OnlineDbAttributeForPredicateDirection = "PredicateDirection";

    //private final String temp_XPathforPredicateBaseQueries = "/Root/XXXSearchModeXXX/PredicateQueries/Predicate";
    //private final String temp_XPathforGetAllInstancesBaseQuery = "/Root/XXXSearchModeXXX/GetAllInstancesBaseQuery";
 
    //private final String TargetNamespaceAttributeName = "QueryFormatNamespace";
    //private final String PredicateIdAttributeName = "id";
    //private final String PredicateSubjectClassElementName = "SubjectClass";
    //private final String PredicateObjectClassElementName = "ObjectClass";	
    //private final String PredicateDirectPredicateElementName = "DirectPredicate";
    //private final String PredicateInversePredicateElementName = "InversePredicate";
    //private final String PredicateSPARQLQueryElementName = "SPARQLQUERY"; 

    //private final String XPathforSourceInputFiles = "/Root/SourceInputFiles/File";
    //private final String XPathforTargetInputFiles = "/Root/Comparisontarget[@selectedTarget='TargetFiles']/TargetFiles/File";
    //private final String XPathforTargetOnLineDBName ="/Root/Comparisontarget[@selectedTarget='OnlineDatabase']/OnlineDatabase/Name";
    //private final String XPathforTargetOnLineDBSparqlEndpoint ="/Root/Comparisontarget[@selectedTarget='OnlineDatabase']/OnlineDatabase/SparqlEndpoint";
    //private final String XPathforTargetOnLineDBOutputFormat ="/Root/Comparisontarget[@selectedTarget='OnlineDatabase']/OnlineDatabase/OutputFormat";
    //private String SearchMode = "";
    private String targetOnlineDBName = "";
    private String targetOnlineDBSparqlEndpointURL = "";
    private String targetOnlineDBOutputFormat = "";
    //private Vector<String> sourceInputFiles = new Vector<String>();
    //private Vector<String> targetInputFiles = new Vector<String>();

    
    private Vector<OnlineDatabase> validDatabases = new Vector<OnlineDatabase>();
    //private Hashtable<String,InitialPredicateClass> predicates = new Hashtable<String,InitialPredicateClass>();
    //private Hashtable<String,String> getAllInstancesBaseQueriesPerNamespace = new Hashtable<String,String>();

    
    /*
    public InitialPredicateClass getPredicateClassById(String predicateId){
        if(this.predicates.containsKey(predicateId)){
            
            return this.predicates.get(predicateId);
        }
        return null;
    }
    
    String GetAllInstancesBaseQuery(String namespaceKey) {
        if(this.getAllInstancesBaseQueriesPerNamespace.containsKey(namespaceKey)){
            return this.getAllInstancesBaseQueriesPerNamespace.get(namespaceKey);
        }
        else{
            if(IMAPIClass.SystemOutWarningsEnabled){
                System.out.println("Could Not find GetAllInstancesBaseQuery for namespace: "+namespaceKey+". Returning GetAllInstancesBaseQuery for " +ApiConstants.defaultCidocCrmNamespace );
            }
            return this.getAllInstancesBaseQueriesPerNamespace.get(ApiConstants.defaultCidocCrmNamespace);            
        }
        
    }
    */
    
    Vector<OnlineDatabase> getSupportedDatabases(){
        return this.validDatabases;
    }
    
    static void printSampleConfigurationFile() {
        System.out.println("Printing Sample Configuration XML file");
        System.out.println("------------------------------------------------------------\n\n");
        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<Root>\n" +
"    \n" +
"    <ValidCidocNamespaces>\n" +
"        <Namespace>http://www.cidoc-crm.org/cidoc-crm/</Namespace>\n" +
"        <Namespace>http://erlangen-crm.org/current/</Namespace>\n" +
"        <Namespace>http://erlangen-crm.org/140220/</Namespace>\n" +
"        <!--<Namespace>http://erlangen-crm.org/120111/</Namespace>-->\n" +
"        <Namespace>http://purl.org/NET/crm-owl#</Namespace>\n" +
"    </ValidCidocNamespaces>\n" +
"    <NamespaceExtensions TargetNamespaseReplaceMent=\"###CIDOCNAMESPACE###\">\n" +
"        <![CDATA[ \n" +
"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>        \n" +
"<rdf:RDF\n" +
"    \n" +
"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
"    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
"    xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"    \n" +
"    xml:base=\"###CIDOCNAMESPACE###\">\n" +
"        <rdf:Property rdf:about=\"P81a_end_of_the_begin\">\n" +
"	<rdfs:label xml:lang=\"en\">end of the begin</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"el\">τέλος της αρχής</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"de\">Ende des Anfangs</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"fr\">fin du début</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"ru\">конец начала</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"pt\">fim do início</rdfs:label>\n" +
"	<rdfs:comment>This is defined as the first boundary of the property P81</rdfs:comment>\n" +
"	<rdfs:domain rdf:resource=\"E52_Time-Span\"/>\n" +
"	<rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#dateTime\"/>\n" +
"	<rdfs:subPropertyOf rdf:resource=\"P81_ongoing_throughout\"/>	\n" +
"</rdf:Property>\n" +
"<rdf:Property rdf:about=\"P81b_begin_of_the_end\">\n" +
"	<rdfs:label xml:lang=\"en\">begin of the end</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"fr\">début de la fin</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"el\">αρχή του τέλους</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"de\">Anfang vom Ende</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"ru\">начать в конце</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"pt\">começar do fim</rdfs:label>\n" +
"	<rdfs:comment>This is defined as the second boundary of the property P81</rdfs:comment>\n" +
"	<rdfs:domain rdf:resource=\"E52_Time-Span\"/>\n" +
"	<rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#dateTime\"/>\n" +
"	<rdfs:subPropertyOf rdf:resource=\"P81_ongoing_throughout\"/>	\n" +
"</rdf:Property>\n" +
"<rdf:Property rdf:about=\"P82a_begin_of_the_begin\">\n" +
"	<rdfs:label xml:lang=\"en\">begin of the begin</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"fr\">début du début</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"el\">αρχή της αρχής</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"de\">Anfang des Anfangs</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"ru\">начать с начала</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"pt\">começar do início</rdfs:label>\n" +
"	<rdfs:comment>This is defined as the first boundary of the property P82</rdfs:comment>\n" +
"	<rdfs:domain rdf:resource=\"E52_Time-Span\"/>\n" +
"	<rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#dateTime\"/>\n" +
"	<rdfs:subPropertyOf rdf:resource=\"P82_at_some_time_within\"/>	\n" +
"</rdf:Property>\n" +
"<rdf:Property rdf:about=\"P82b_end_of_the_end\">\n" +
"	<rdfs:label xml:lang=\"en\">end of the end</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"fr\">fin de la fin</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"el\">τέλος του τέλους</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"de\">Ende vom Ende</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"ru\">конец конец</rdfs:label>\n" +
"	<rdfs:label xml:lang=\"pt\">fim do fim</rdfs:label>\n" +
"	<rdfs:comment>This is defined as the second boundary of the property P82</rdfs:comment>\n" +
"	<rdfs:domain rdf:resource=\"E52_Time-Span\"/>\n" +
"	<rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#dateTime\"/>\n" +
"	<rdfs:subPropertyOf rdf:resource=\"P82_at_some_time_within\"/>	\n" +
"</rdf:Property>\n" +
"\n" +
"\n" +
"        </rdf:RDF>\n" +
"        ]]>\n" +
"    </NamespaceExtensions>\n" +
"\n" +
"    <SupportedDatabases>\n" +
"        <OnlineDatabase sourceid=\"BRITISH_MUSEUM_COLLECTION\" PredicateDirection=\"direct\">\n" +
"            <Name>British Museum Semantic Web Collection Online</Name>\n" +
"            <SparqlEndpoint>http://collection.britishmuseum.org/sparql</SparqlEndpoint>            \n" +
"            <QueryNamespaces>\n" +
"                <Namespace prefix=\"ecrm\">http://erlangen-crm.org/current/</Namespace>\n" +
"                <Namespace prefix=\"rdfs\">http://www.w3.org/2000/01/rdf-schema#</Namespace>\n" +
"                <Namespace prefix=\"rdf\">http://www.w3.org/1999/02/22-rdf-syntax-ns#</Namespace>     \n" +
"                <Namespace prefix=\"skos\">http://www.w3.org/2004/02/skos/core#</Namespace>                \n" +
"                <Namespace prefix=\"bmo\">http://collection.britishmuseum.org/id/ontology/</Namespace>\n" +
"            </QueryNamespaces>\n" +
"        </OnlineDatabase>\n" +
"        \n" +
"        <OnlineDatabase sourceid=\"CLAROS\" PredicateDirection=\"both\">\n" +
"            <Name>CLAROS</Name>\n" +
"            <SparqlEndpoint>http://data.clarosnet.org/sparql/</SparqlEndpoint>            \n" +
"            <QueryNamespaces>\n" +
"                <Namespace prefix=\"crm\">http://purl.org/NET/crm-owl#</Namespace>\n" +
"                <Namespace prefix=\"rdfs\">http://www.w3.org/2000/01/rdf-schema#</Namespace>\n" +
"                <Namespace prefix=\"rdf\">http://www.w3.org/1999/02/22-rdf-syntax-ns#</Namespace>     \n" +
"                <Namespace prefix=\"skos\">http://www.w3.org/2004/02/skos/core#</Namespace>                                              \n" +
"            </QueryNamespaces>\n" +
"        </OnlineDatabase>\n" +
"    </SupportedDatabases>\n" +
"    \n" +
"</Root>");
    }

    /*
    private String getXPathforAllInstances() {
        return this.temp_XPathforGetAllInstancesBaseQuery.replace("XXXSearchModeXXX", this.SearchMode);
    }
    
    private String getXPathforPredicates() {
        return this.temp_XPathforPredicateBaseQueries.replace("XXXSearchModeXXX", this.SearchMode);
    }
    */
    private int errCode = ApiConstants.IMAPISuccessCode;

    int getErrorCode() {
        return this.errCode;
    }
    private String errorMessage = "";

    String getErrorMessage() {
        return this.errorMessage;
    }

    /*
    String getPredicateBaseQuery(String pid,String ns){
        if(this.predicates.containsKey(pid)){
            return this.predicates.get(pid).getSparqlQuery(ns);
        }
        return "";
    }
    */
    private void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;
        if (this.errCode == ApiConstants.IMAPIFailCode) {
            System.out.println("ERROR occurred: " + errorMsg);
        }
    }

    
    public ApiConfigClass() {

        this.setErrorMessage(ApiConstants.IMAPISuccessCode, "");
        
        
        this.validDatabases = new Vector<OnlineDatabase>();
        
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            
            Document document = builder.parse(ApiConfigClass.class.getResourceAsStream("/imapi/Configuration.xml"));
            XPath xpath = XPathFactory.newInstance().newXPath();

            /*//get comparison mode
             if (this.XPathforPerformingFileComparison != null) {
             //get database connection string
             Node node = (Node) xpath.evaluate(this.XPathforPerformingFileComparison, document, XPathConstants.NODE);
             if (node != null) {

             String performFilecomparisonStr = node.getTextContent();
             performFilecomparisonStr = performFilecomparisonStr == null ? "" : performFilecomparisonStr.trim().toLowerCase();
             if (performFilecomparisonStr.equals("yes") || performFilecomparisonStr.equals("true")) {
             this.fileComparison = true;
             } else {
             this.fileComparison = false;
             }
             }
             }

             //get search mode
             if (this.XPathforSearchMode != null) {
             //get database connection string
             Node node = (Node) xpath.evaluate(this.XPathforSearchMode, document, XPathConstants.NODE);
             if (node != null) {

             this.SearchMode = node.getTextContent();
             this.SearchMode = this.SearchMode == null ? "" : this.SearchMode.trim();
             }
             }
             */
            
            
            ApiConstants.TargetSourceChoice[] validTargetSourceChoices = ApiConstants.TargetSourceChoice.values();
            ApiConstants.PredicateDirectionUsage[] validPredicateDirections = ApiConstants.PredicateDirectionUsage.values();
            if (this.XPathforSupportedOnlineDbs != null) {
                NodeList nodes = (NodeList) xpath.evaluate(this.XPathforSupportedOnlineDbs, document, XPathConstants.NODESET);
                if (nodes != null) {

                    int howmanyNodes = nodes.getLength();
                    
                    
                    for (int i = 0; i < howmanyNodes; i++) {

                        String dbName="", dbSparqlEndpoint="", dbid="", dbPredDirectionSTr;
                        ApiConstants.TargetSourceChoice sourceChoice = ApiConstants.TargetSourceChoice.FILE_COMPARISON;
                        ApiConstants.PredicateDirectionUsage predicatesDirection = ApiConstants.PredicateDirectionUsage.BOTH;
                        Hashtable<String,String> namespaces = new Hashtable<String,String>();
                        
                        //boolean isValidCidocCrmDetected = false;
                        
                        Node xNode = nodes.item(i);
                        
                        boolean is_ComparisonMode_correctly_set = false;
                        if(xNode.hasAttributes()){
                            Node idAttrNode = xNode.getAttributes().getNamedItem(this.OnlineDbAttributeNameForId);
                            dbid = idAttrNode.getTextContent().toUpperCase();
                            
                            if(dbid!=null){

                                for(int k=0;k<validTargetSourceChoices.length; k++){
                                    String modeStr = validTargetSourceChoices[k].name().toUpperCase();
                                    if(dbid.equals(modeStr)){
                                        sourceChoice = validTargetSourceChoices[k];
                                        is_ComparisonMode_correctly_set = true;
                                        break;
                                    }
                                }                        
                            }     
                            
                            Node directionAttrNode = xNode.getAttributes().getNamedItem(this.OnlineDbAttributeForPredicateDirection);
                            dbPredDirectionSTr = directionAttrNode.getTextContent().toUpperCase();
                            
                            if(dbPredDirectionSTr!=null){

                                for(int k=0;k<validPredicateDirections.length; k++){
                                    String modeStr = validPredicateDirections[k].name().toUpperCase();
                                    if(dbPredDirectionSTr.equals(modeStr)){
                                        predicatesDirection = validPredicateDirections[k];
                                        
                                        break;
                                    }
                                }                        
                            } 
                        }
                        
                        //getDbName
                        Node xNodeName = (Node) xpath.evaluate("./"+this.OnlineDbElementNameForName, xNode, XPathConstants.NODE);
                        if(xNodeName!=null){
                            dbName = xNodeName.getTextContent();
                        }
                        
                        //getSPARQL endpoint
                        Node xNodeSPARQLEndpoint = (Node) xpath.evaluate("./"+this.OnlineDbElementNameForSparqlEndpoint, xNode, XPathConstants.NODE);
                        if(xNodeSPARQLEndpoint!=null){
                            dbSparqlEndpoint = xNodeSPARQLEndpoint.getTextContent();
                        }
                        
                        
                        //getNamespaces
                        NodeList xNodeNamepsaces = (NodeList) xpath.evaluate("./"+this.OnlineDbSubPathForNamespaces, xNode, XPathConstants.NODESET);
                        if(xNodeNamepsaces!=null){
                            
                            for(int m=0; m< xNodeNamepsaces.getLength(); m++){
                                Node nsNode = xNodeNamepsaces.item(m);
                                
                                String prefix ="";
                                String val ="";
                                
                                if(nsNode.hasAttributes()){
                                    Node prefixAttrNode = nsNode.getAttributes().getNamedItem(this.OnlineDbNamespaceAttributeNameForPrefix);
                                    prefix = prefixAttrNode.getTextContent();
                                }
                                
                                val = nsNode.getTextContent();
                                
                                if(prefix.length()>0 && val.length()>0 && namespaces.containsKey(prefix)==false){
                                    
                                    namespaces.put(prefix, val);
                                    /*if(this.validCrmNamespaces.contains(val)){
                                        isValidCidocCrmDetected=true;
                                    }*/
                                }
                                        
                            }
                            
                        }
                        
                        if(!is_ComparisonMode_correctly_set || sourceChoice == ApiConstants.TargetSourceChoice.FILE_COMPARISON){
                            System.out.println("Not supported database " + this.OnlineDbAttributeNameForId + " Skipping Database..");
                            continue;
                        }
                        
                        if(dbName.length()==0){
                            System.out.println("No database name defined for online database. Check configutation file again. Skipping Database..");
                            continue;
                        }
                        
                        if(dbSparqlEndpoint.length()==0){
                            System.out.println("No SPARQL endpoint  defined for online database. Check configutation file again. Skipping Database..");
                            continue;
                        }
                        
                        if(namespaces.size()==0){
                            System.out.println("No namespaces defined for online database: "+ dbName+" Skipping Database..");
                            continue;
                        }
                        
                        /*
                        if(!isValidCidocCrmDetected){
                            this.setErrorMessage(ApiConstants.IMAPIFailCode, "No valid cidoc crm namespace defined for online database: "+ dbName+
                                    ".Namespaces defined:\n" + namespaces.values().toString()+"\n Skipping Database..");
                             ApiConfigClass.printSampleConfigurationFile();
                             return;
                        }
                        */
                        OnlineDatabase newDb = new OnlineDatabase(sourceChoice, dbName, dbSparqlEndpoint, namespaces,predicatesDirection);
                        this.validDatabases.add(newDb);
                    }
                }
            }
            
            if(this.validDatabases.size()==0){
                System.out.println("Warning: No online database declared. only file comparison wiil be supported.");
            }
            
            
        } catch (ParserConfigurationException e) {
            ApiConfigClass.printSampleConfigurationFile();
            String tempMsg = "ParserConfigurationException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            ApiConfigClass.printSampleConfigurationFile();
            String tempMsg = "XPathExpressionException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);            
            e.printStackTrace();
        } catch (SAXException e) {
            ApiConfigClass.printSampleConfigurationFile();
            String tempMsg = "SAXException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            e.printStackTrace();
        } catch (IOException e) {
            ApiConfigClass.printSampleConfigurationFile();
            String tempMsg = "IOException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            e.printStackTrace();
        }

    }

    //Values that will be read from XML
    //private boolean fileComparison = false;
    //private String SearchMode = "";
    //private Vector<String> sourceInputFiles = new Vector<String>();
    //private Vector<WeightedPredicate> predicates = new Vector<WeightedPredicate>();
    //private String InstancesQuery = "";
}
