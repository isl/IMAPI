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

import java.util.Vector;

/**
 *
 * @author tzortzak
 */
public class SequencesVector extends Vector<SequenceData>{
    
    SequenceData getSequenceDataAtPosition(int position){
        for(int i=0; i< this.size(); i++){
            if(this.get(i).getSchemaInfo().getPositionID()==position){
                return this.get(i);
            }
        }
        return null;
    }
    
    void addValueToSequence(UserQueryConfiguration currentSequence, String parameterKey, String value, String lang){
        int seqPosition = currentSequence.getPositionID();
        boolean sequenceNotFound = true;
        if(this.size()>0){
            
            for(int i=0; i< this.size(); i++){
                if(this.get(i).getSchemaInfo().getPositionID()== seqPosition){
                    
                    this.get(i).addTriplet(parameterKey, value, lang);
                    sequenceNotFound = false;
                    break;
                }
            }
        }
        
        if(sequenceNotFound){
            SequenceData newSeqData = new SequenceData(currentSequence);
            newSeqData.addTriplet(parameterKey, value, lang);
            this.add(newSeqData);
        }
        
        
    }
    
}
