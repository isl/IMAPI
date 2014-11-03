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

import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class SequencesVector extends Vector<SequenceData>{
    
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
