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

import imapi.ApiConstants;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author tzortzak
 */
class OnlineDatabase {
    
    private ApiConstants.PredicateDirectionUsage predicatesDirection = ApiConstants.PredicateDirectionUsage.BOTH;
    
    private String db_Name = "", db_SparqlEndpoint="";
    private Hashtable<String,String> db_Namespaces;
    
    private ApiConstants.TargetSourceChoice db_Choice;
    
    
    ApiConstants.PredicateDirectionUsage getTargetDatabasePredicateDirectionUsage() {
        return this.predicatesDirection;
    }
    
    
    OnlineDatabase(ApiConstants.TargetSourceChoice dbChoice, String dbName, String dbSparqlEndpoint,Hashtable<String,String> dbNamespaces, ApiConstants.PredicateDirectionUsage predDirection){
        db_Namespaces = new Hashtable<String,String>();
        Enumeration<String> nsEnum = dbNamespaces.keys();
        while(nsEnum.hasMoreElements()){
            String key= nsEnum.nextElement();
            String val = dbNamespaces.get(key);
            this.db_Namespaces.put(key, val);
        }
        db_Name =dbName;
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
