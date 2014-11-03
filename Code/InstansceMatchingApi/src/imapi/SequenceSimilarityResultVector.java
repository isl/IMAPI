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
class SequenceSimilarityResultVector extends Vector<SequenceSimilarityResult> {
    
    void addSequenceSimilarityResult(SequenceSimilarityResult newVal){
        Double newValSimilarity = newVal.getSimilarity();
        
        if(newValSimilarity <=0d){
            return;
        }
        int newValSeqId = newVal.getSequenceId();
        
        for(int i=0; i< this.size(); i++){
            SequenceSimilarityResult existingResult = this.get(i);
            if(existingResult.getSequenceId() == newValSeqId){
             
                Double existingSimilarity = existingResult.getSimilarity();
                
                if(newValSimilarity>existingSimilarity){
                    this.get(i).setNewSimilarityResult(newVal.getParameterName(),
                            newVal.getParameterType(), 
                            newVal.getSourceVal(), 
                            newVal.getTargetVal(),
                            newValSimilarity);                    
                }
                
                return;
            }            
        }
        
        this.add(newVal);
    }
}
