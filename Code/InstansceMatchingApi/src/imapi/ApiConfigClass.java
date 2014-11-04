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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author tzortzak
 */
class ApiConfigClass {

    private final String XPathforSupportedOnlineDbs = "/Root/SupportedDatabases/OnlineDatabase";
    
    private final String OnlineDbAttributeNameForDBType = "dbType";
    private final String OnlineDbAttributeNameForId = "sourceid";
    private final String OnlineDbElementNameForName = "Name";
    private final String OnlineDbElementNameForIdPredicateUri = "IdPredicateUri";
    private final String OnlineDbElementNameForSparqlEndpoint = "SparqlEndpoint";
    private final String OnlineDbSubPathForNamespaces = "QueryNamespaces/Namespace";
    private final String OnlineDbNamespaceAttributeNameForPrefix = "prefix";
    private final String OnlineDbAttributeForPredicateDirection = "PredicateDirection";

    private String targetOnlineDBName = "";
    private String targetOnlineDBSparqlEndpointURL = "";
    private String targetOnlineDBOutputFormat = "";
    
    
    private Vector<OnlineDatabase> validDatabases = new Vector<OnlineDatabase>();
    
    
    OnlineDatabase getOnlineDb(ApiConstants.TargetSourceChoice sourceChoice){
        
        OnlineDatabase db = null;
        for(int k=0; k< this.validDatabases.size(); k++){
            if(validDatabases.get(k).getDBChoice() == sourceChoice){
                db = validDatabases.get(k);
                break;
            }                    
        }
        return db;
    }
    
    Vector<OnlineDatabase> getSupportedDatabases(){
        return this.validDatabases;
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
            
            ApiConstants.TargetSourceChoice[] validTargetSourceChoices = ApiConstants.TargetSourceChoice.values();
            ApiConstants.PredicateDirectionUsage[] validPredicateDirections = ApiConstants.PredicateDirectionUsage.values();
            if (this.XPathforSupportedOnlineDbs != null) {
                NodeList nodes = (NodeList) xpath.evaluate(this.XPathforSupportedOnlineDbs, document, XPathConstants.NODESET);
                if (nodes != null) {

                    int howmanyNodes = nodes.getLength();
                    
                    
                    for (int i = 0; i < howmanyNodes; i++) {

                        String dbName="", dbSparqlEndpoint="", dbid="", dbPredDirectionSTr="", dbTypeStr ="", idPredicateUri ="";
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
                            
                            Node dbTypeAttributeNode = xNode.getAttributes().getNamedItem(this.OnlineDbAttributeNameForDBType);
                            if(dbTypeAttributeNode!=null){
                                String tempStr = dbTypeAttributeNode.getTextContent();
                                if(tempStr!=null){
                                    dbTypeStr = tempStr.trim().toLowerCase();
                                }
                            }
                        }
                        
                        //getDbName
                        Node xNodeName = (Node) xpath.evaluate("./"+this.OnlineDbElementNameForName, xNode, XPathConstants.NODE);
                        if(xNodeName!=null){
                            dbName = xNodeName.getTextContent();
                        }
                        
                        //getIdPredicate
                        Node xIdPredUri = (Node) xpath.evaluate("./"+this.OnlineDbElementNameForIdPredicateUri, xNode, XPathConstants.NODE);
                        if(xIdPredUri!=null){
                            idPredicateUri = xIdPredUri.getTextContent().trim();
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
                        OnlineDatabase newDb = new OnlineDatabase(sourceChoice, dbName, dbTypeStr, idPredicateUri, dbSparqlEndpoint, namespaces,predicatesDirection);
                        this.validDatabases.add(newDb);
                    }
                }
            }
            
            if(this.validDatabases.size()==0){
                System.out.println("Warning: No online database declared. only file comparison wiil be supported.");
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
