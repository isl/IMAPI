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

public class SequenceSimilarityResult {
    //sequence specific
    private int sequenceId;
    private String sequenceMnemonic;
    private Double sequenceWeight =0d;
    
    //step specific
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
    
    public int getSequenceId(){
        return this.sequenceId;
    }
    
    public String getSequenceMnemonic(){
        return this.sequenceMnemonic;
    }
    
    public Double getSequenceWeight(){
        return this.sequenceWeight;
    }
    
    //step specific
    public String getParameterName(){
        return this.parameterName;
    }
    
    public  String getParameterType(){
        return this.parameterType;
    }
    
    public DataRecord getSourceVal(){
        return this.sourceVal;
    }
    
    public DataRecord getTargetVal(){
        return this.targetVal;
    }
    
    public Double getSimilarity(){
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
