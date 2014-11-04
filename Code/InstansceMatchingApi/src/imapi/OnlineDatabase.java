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
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An integer holding object in order to by used for pass as reference through function calls
 * 
 * @author tzortzak
 */
class OnlineDatabase {
    
    private ApiConstants.PredicateDirectionUsage predicatesDirection = ApiConstants.PredicateDirectionUsage.BOTH;
    
    private String db_Name = "", db_SparqlEndpoint="", dbType ="", idPredicate="";
    
    private Hashtable<String,String> db_Namespaces;
    
    private ApiConstants.TargetSourceChoice db_Choice;
    
    
    ApiConstants.PredicateDirectionUsage getTargetDatabasePredicateDirectionUsage() {
        return this.predicatesDirection;
    }
    
    
    OnlineDatabase(ApiConstants.TargetSourceChoice dbChoice, String dbName, String dbTypeStr, String idPredicateStr, String dbSparqlEndpoint,Hashtable<String,String> dbNamespaces, ApiConstants.PredicateDirectionUsage predDirection){
        db_Namespaces = new Hashtable<String,String>();
        Enumeration<String> nsEnum = dbNamespaces.keys();
        while(nsEnum.hasMoreElements()){
            String key= nsEnum.nextElement();
            String val = dbNamespaces.get(key);
            this.db_Namespaces.put(key, val);
        }
        db_Name =dbName;
        this.dbType = dbTypeStr;
        this.idPredicate = idPredicateStr;
        db_SparqlEndpoint =dbSparqlEndpoint;
        this.db_Choice = dbChoice;     
        this.predicatesDirection = predDirection;
        
    }
    
    ApiConstants.TargetSourceChoice getDBChoice(){
        return this.db_Choice;
    }
    String getDbName(){
        return this.db_Name;        
    }
    String getDbType(){
        return this.dbType;
    }
    String getIdPredicate(){
        return "<"+this.idPredicate+">";
    }
    
    String getDbSparqlEndpoint(){
        return this.db_SparqlEndpoint;        
    }
    
    Hashtable<String,String> getDbNamesapcesCopy(){
        Hashtable<String,String> returnVal = new Hashtable<String,String>();
        Enumeration<String> nsEnum = this.db_Namespaces.keys();
        while(nsEnum.hasMoreElements()){
            String key= nsEnum.nextElement();
            String val = this.db_Namespaces.get(key);
            returnVal.put(key, val);
        }
        return returnVal;
    }
    
    String getQueriesPrefixDeclarationPart(){
        String returnStr = "";
        Enumeration<String> nsEnum = this.db_Namespaces.keys();
        while(nsEnum.hasMoreElements()){
            String key= nsEnum.nextElement();
            String val = this.db_Namespaces.get(key);
            
            returnStr += "PREFIX "+key+": <" +val+">\n";
        }
        returnStr+="\n";
        return returnStr;
    }
}
