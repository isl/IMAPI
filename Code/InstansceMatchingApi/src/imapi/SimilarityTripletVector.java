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
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class SimilarityTripletVector extends Vector<SimilarityTriplet>{
    
    boolean containsSequence(int position){
        for(int i=0; i<this.size();i++){
            if(this.get(i).getSequencePosition() == position){
                return true;
            }
        }
        
        return false;
    }
    
    Vector<Integer> returnSequenceIdsContainedInVector(){
        Vector<Integer>  returnVec = new Vector<Integer>();
        for(int i=0; i< this.size(); i++){
            int newVal = this.get(i).getSequencePosition();
            if(returnVec.contains(newVal)==false){
                returnVec.add(newVal);
            }
        }        
        Collections.sort(returnVec);
        return returnVec;
    }
    
    Double returnMaxSimilarityStepOfSpecificSequence(int sequencePosition){
        Double returnVal = 0d;
        for(int i=0; i< this.size(); i++){
            int newVal = this.get(i).getSequencePosition();
            if(newVal!=sequencePosition){
                continue;
            }
            if(returnVal<= this.get(i).getSimilarity()){
                returnVal = this.get(i).getSimilarity();
            }            
        }    
        
        return returnVal;
    }
}


