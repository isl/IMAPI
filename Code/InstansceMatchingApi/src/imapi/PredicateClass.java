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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class PredicateClass {
    private String ID = "";
    
    private String SubjectClass = "";
    private String PredicateName = "";
    private String InversePredicateName = "";
    private String ObjectClass = "";
    
    //Get Functions
    String getID() {
        return ID;
    }
    
    String getSubjectClass() {
        return SubjectClass;
    }

    String getPredicateName() {
        return PredicateName;
    }

    String getInversePredicateName() {
        return InversePredicateName;
    }

    String getObjectClass() {
        return ObjectClass;
    }
    
    

    PredicateClass(String idStr) {

        this.ID = idStr == null ? "" : idStr.trim();
    //    this.SubjectClass = sbjClass == null ? "" : sbjClass.trim();
    //    this.ObjectClass = objClass == null ? "" : objClass.trim();
    //    this.PredicateName = predStr == null ? "" : predStr.trim();
    //    this.InversePredicateName = inversePredStr == null ? "" : inversePredStr.trim();        
    }
    
    
    void setDirectPredicateName(String predStr){
        this.PredicateName = predStr == null ? "" : predStr.trim();
    }
    void setInverseDirectPredicateName(String inversePredStr){
        this.InversePredicateName = inversePredStr == null ? "" : inversePredStr.trim();
    }
    
    
    void setSubjectClass(String sbjClass){
        this.SubjectClass = sbjClass == null ? "" : sbjClass.trim();
    }
    void setObjectClass(String objClass){
        this.ObjectClass = objClass == null ? "" : objClass.trim();
    }
    
    public boolean equals(Object otherObj) {

        if (otherObj == null || (otherObj instanceof PredicateClass) == false) {
            return false;
        }
        PredicateClass otherPred = (PredicateClass) otherObj;
        return this.ID.equals(otherPred.getID());
    }
    
    String getSparqlPart(String subject,String object){
        String returnStr ="";
        String tabPrefix = "    ";
        returnStr += tabPrefix+"{\n";
        returnStr += tabPrefix+tabPrefix+"{ ?"+subject+" crm:"+ this.PredicateName+ " ?"+object+ ". }\n";
        returnStr += tabPrefix+tabPrefix+"UNION\n";
        returnStr += tabPrefix+tabPrefix+"{ ?"+object+" crm:"+ this.PredicateName+ " ?"+subject+ ". }\n";
        //if(this.InversePredicateName.length()>0){
//            returnStr += tabPrefix+tabPrefix+"UNION { ?"+subject+" crm:"+ this.InversePredicateName+ " ?"+object+ ". }\n";
//            returnStr += tabPrefix+tabPrefix+"UNION { ?"+object+" crm:"+ this.InversePredicateName+ " ?"+subject+ ". }\n";
//        }
        returnStr += tabPrefix+"}\n";
        
        return returnStr;
    }
}
