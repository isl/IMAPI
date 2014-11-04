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
public class SequenceSimilarityResultVector extends Vector<SequenceSimilarityResult> {
    
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
