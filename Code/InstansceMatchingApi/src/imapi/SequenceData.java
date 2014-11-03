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

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class SequenceData {
    private UserQueryConfiguration schemaInfo;
    private SequenceInfoHashtable data;
    
    SequenceData(UserQueryConfiguration querySequenceInfo){
        this.schemaInfo = querySequenceInfo.copy();
        data = new SequenceInfoHashtable();
    }
    
    
    UserQueryConfiguration getSchemaInfo(){
        return this.schemaInfo;
    }
    String getParameterNameOfSpecificSequenceAndStep(int stepPos){
        String[] pNames =this.schemaInfo.getSortedParameterNamesCopy();
        if(stepPos<pNames.length){
            return pNames[stepPos];
        }
        
        return "";
    }
    
    Vector<DataRecord> getValuesOfKey(String KeyStr){
        if(this.data.containsKey(KeyStr)){
            return this.data.get(KeyStr);
        }
        else{
            return new Vector<DataRecord>();
        }
    }
    void addTriplet(String parameterKey, String value){
        addTriplet(parameterKey,value,"");
    }
    
    void addTriplet(String parameterKey, String value, String lang){
        DataRecord newVal = new DataRecord(value, lang);
        if(data.containsKey(parameterKey)){
            if(this.data.get(parameterKey).contains(newVal)==false){
                this.data.get(parameterKey).add(newVal);
            }
        }
        else{
            Vector<DataRecord> newVec = new Vector<DataRecord>();
            newVec.add(newVal);
            this.data.put(parameterKey, newVec);
        }
    }
}
