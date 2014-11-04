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
import imapi.ApiConstants.PredicateDirectionUsage;
import java.util.Comparator;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class CidocCrmCompatibleFile implements Comparator<CidocCrmCompatibleFile>, Comparable<CidocCrmCompatibleFile> {

    
    private String filePath = "" ;//, predicateIdSeperator="", predicateInverseIdSuffix="";
    private PredicateDirectionUsage predicatesDirection = ApiConstants.PredicateDirectionUsage.BOTH;
    private Vector<String> crmCompatibleNamespacesDeclared = new Vector<String>();

    
    
    String getFilePath() {
        return this.filePath;
    }

    PredicateDirectionUsage getPredicateDirectionUsage() {
        return this.predicatesDirection;
    }
    
    /*
    public String getPredicateIdSeperator() {
        return this.predicateIdSeperator;
    }
    
    public String getPredicateInverseIdSuffix() {
        return this.predicateInverseIdSuffix;
    }
    */
    CidocCrmCompatibleFile(String absoluteFilePath, PredicateDirectionUsage predUsage /*, String predIdSeperator, String predInverseIdSuffix*/) {
        this.filePath = absoluteFilePath;
        this.predicatesDirection = predUsage;
        //this.predicateIdSeperator = predIdSeperator;
        //this.predicateInverseIdSuffix = predInverseIdSuffix;
    }

    @Override
    public int compare(CidocCrmCompatibleFile o1, CidocCrmCompatibleFile o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        }

        if (o2 == null) {
            if (o1 != null) {
                return 1;
            }
        }

        if (o1.getFilePath().equals(o2.getFilePath())) {            
            return 0;
                        
        } else {            
            return o1.getFilePath().compareTo(o2.getFilePath());
        }

        
    }

    @Override
    public int compareTo(CidocCrmCompatibleFile o) {
        return this.compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || (obj instanceof CidocCrmCompatibleFile) == false) {
            return false;
        }

        CidocCrmCompatibleFile otherObj = (CidocCrmCompatibleFile) obj;
        if (this.getFilePath().equals(otherObj.getFilePath())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {        
        return this.getFilePath().hashCode();
    }

}
