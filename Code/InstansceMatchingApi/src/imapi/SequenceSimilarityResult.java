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

/**
 *
 * @author tzortzak
 */
class SequenceSimilarityResult {
    //sequence specific
    private int sequenceId;
    private String sequenceMnemonic;
    private Double sequenceWeight =0d;
    
    //step speceific
    private String parameterName;
    private String parameterType;
    private DataRecord sourceVal;
    private DataRecord targetVal;
    private Double similarity = 0d;
    
    SequenceSimilarityResult(int seqPos,String seqMnemonic, Double seqWeight){
        this.sequenceId = seqPos;
        this.sequenceMnemonic = seqMnemonic;
        this.sequenceWeight = seqWeight;
    }
    
    /*
    SequenceSimilarityResult(int seqPos,String seqMnemonic, Double seqWeight, String pName, String pType, DataRecord srcVal, DataRecord trgtVal, Double simVal){
        this.sequenceId = seqPos;
        this.sequenceMnemonic = seqMnemonic;
        this.sequenceWeight = seqWeight;
        
        this.parameterName = pName;
        this.parameterType = pType;
        this.sourceVal = new DataRecord(srcVal.getValue(), srcVal.getLang());
        this.targetVal = new DataRecord(trgtVal.getValue(), trgtVal.getLang());
        this.similarity = simVal;
    }
    */
    
    int getSequenceId(){
        return this.sequenceId;
    }
    
    String getSequenceMnemonic(){
        return this.sequenceMnemonic;
    }
    
    Double getSequenceWeight(){
        return this.sequenceWeight;
    }
    
    //step speceific
    String getParameterName(){
        return this.parameterName;
    }
    
    String getParameterType(){
        return this.parameterType;
    }
    
    DataRecord getSourceVal(){
        return this.sourceVal;
    }
    
    DataRecord getTargetVal(){
        return this.targetVal;
    }
    
    Double getSimilarity(){
        return this.similarity;
    }
    
    void setNewSimilarityResult(String pName, String pType, DataRecord srcVal, DataRecord trgtVal, Double newSimilarity){
        this.parameterName = pName;
        this.parameterType = pType;
        this.sourceVal = new DataRecord(srcVal.getValue(), srcVal.getLang());
        this.targetVal = new DataRecord(trgtVal.getValue(), trgtVal.getLang());
        this.similarity = newSimilarity;
                
    }
    
}
