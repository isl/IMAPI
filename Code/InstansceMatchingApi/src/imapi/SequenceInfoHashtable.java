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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class SequenceInfoHashtable extends Hashtable<String, Vector<DataRecord>> {

    SequenceInfoHashtable() {
        
    }

    //<editor-fold defaultstate="collapsed" desc="Abandoned not necessarily working code">
    /*
    Vector<String> DELETE_getKeysStartingWithPrefix(String prefix) {

        Vector<String> returnVec = new Vector<String>();

        if (prefix != null && prefix.trim().length() > 0) {
            String prefixLowered = prefix.trim();

            Enumeration<String> keysIter = this.keys();
            if (keysIter != null) {
                while (keysIter.hasMoreElements()) {
                    String strKey = keysIter.nextElement();
                    if (strKey.startsWith(prefixLowered)) {
                        returnVec.add(strKey);
                    }
                }
            }
        }

        return returnVec;
    }

    void DELETE_mergeWithOhterSequenceInfoHashtable(SequenceInfoHashtable other) {
        Enumeration<String> otherTableIter = other.keys();

        while (otherTableIter.hasMoreElements()) {
            String key = otherTableIter.nextElement();

            Vector<DataRecord> mergedVec = new Vector<DataRecord>();
            if (this.containsKey(key)) {

                mergedVec = this.get(key);
            }

            Vector<DataRecord> Othervec = other.get(key);

            for (int i = 0; i < Othervec.size(); i++) {
                DataRecord bClass = Othervec.get(i);
                if (mergedVec.contains(bClass) == false) {
                    mergedVec.add(bClass);
                }
            }

            this.put(key, mergedVec);

        }

    }
    
    void DELETE_printContents(){
        Vector<String> vec = new Vector<String>(this.keySet());
        Collections.sort(vec);
        for(int i=0; i< vec.size(); i++){
            String key = vec.get(i);
            Vector<DataRecord> bClassVec = this.get(key);
            System.out.println("\tKey: " + key +"\n\t------------------------------------------------");
            for(int k=0; k< bClassVec.size(); k++){
                DataRecord bClass = bClassVec.get(k);
                System.out.println("\t\t" + bClass.toString());
            }
        }
    }
    */
    //</editor-fold>
}
