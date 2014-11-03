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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class SequenceInfoHashtable extends Hashtable<String, Vector<DataRecord>> {

    public SequenceInfoHashtable() {
        // TODO Auto-generated constructor stub
    }

    public Vector<String> getKeysStartingWithPrefix(String prefix) {

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

    public void mergeWithOhterSequenceInfoHashtable(SequenceInfoHashtable other) {
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
    
    protected void printContents(){
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
}
