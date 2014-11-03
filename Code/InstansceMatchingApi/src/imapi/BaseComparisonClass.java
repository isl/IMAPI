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

import static imapi.IMAPIClass.AcceptUriEquality;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class BaseComparisonClass {

    private StringSimilarity strSimilarity;//  = new StringSimilarity();

    IMAPIClass imClass = null;

    BaseComparisonClass(IMAPIClass whichImClass) {
        this.imClass = whichImClass;
        strSimilarity = new StringSimilarity();
    }

    
    Double combineStringSimilarities(Vector<Double> vec) {

        Double tempResult = 0d;

        //average
        for (int m = 0; m < vec.size(); m++) {
            tempResult += vec.get(m);
        }

        tempResult = tempResult / vec.size();

        //best
		/*
         for(int m=0;m<vec.size(); m++){
         if(vec.get(m)> tempResult){
         tempResult = vec.get(m);
         }
         }
         */
        return tempResult;
    }
    
    Double compareValueAgainstVector(String valueType, DataRecord valueToCompare, Vector<DataRecord> restOfValues, DataRecord srcCompareRecord){
        Double comparisonResult = 0d;
        
        //uri comparison
        if(valueType.equals("uri")){
            String uriVal = valueToCompare.getValue();
            for(int i=0; i< restOfValues.size(); i++){
                if(restOfValues.get(i).getValue().equals(uriVal)){
                    srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                    return 1d;
                }
            }
        }
        
        //literal comparison 
        if(valueType.equals("literal")){
         
            String otherVal = valueToCompare.getValue().toLowerCase();
            for(int i=0; i< restOfValues.size(); i++){
                String valToSearch = restOfValues.get(i).getValue().toLowerCase();
                Vector<Double> tempVec = new Vector<Double>();
                
                //case STR_CHARFREQ
                        tempVec.add(new Double(strSimilarity.computeCharFrequencySimilarity(valToSearch, otherVal)));
                        //case STR_DIGRAM
                        tempVec.add(new Double(strSimilarity.computeDigramSimilarity(valToSearch, otherVal)));
                        //case STR_TRIGRAM
                        tempVec.add(new Double(strSimilarity.computeTrigramSimilarity(valToSearch, otherVal)));
                        //case STR_SOUNDEX
                        tempVec.add(new Double(strSimilarity.computeSoundexSimilarity(valToSearch, otherVal)));
                        //case STR_EDITDISTANCE
                        tempVec.add(new Double(strSimilarity.computeEditDistanceSimilarity(valToSearch, otherVal)));
                        //case STR_SINGLEERROR
                        tempVec.add(new Double(strSimilarity.computeSingleErrorSimilarity(valToSearch, otherVal)));

                        Double tempResult = combineStringSimilarities(tempVec);

                        if(tempResult>=this.imClass.userConfig.getMinimumLiteralSimilarity()){
                            if (comparisonResult < tempResult) {
                                comparisonResult = tempResult;
                                srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                            }
                        }
            }
            

        }
        
        //date comparison
        if(valueType.equals("date")){
            
        }
        
        //timespan comparison
        if(valueType.equals("timespan")){
            ValueOf_Timespan baseTspan = new ValueOf_Timespan(valueToCompare.getValue());
            
            if(baseTspan.startDate==null || baseTspan.endDate==null){
                return 0d;
            }
            
            for(int i=0; i< restOfValues.size(); i++){
                
                String otherVal = restOfValues.get(i).getValue();
                ValueOf_Timespan compareTspan = new ValueOf_Timespan(otherVal);
                
                if(compareTspan.startDate==null || compareTspan.endDate==null){
                    continue;
                }
                
                
                
                if( baseTspan.startDate.isBeforeOrEqual(compareTspan.startDate)){
                    //System.out.println("Ok StartDate");
                }
                else{
                    return 0d;
                }
                
                if( baseTspan.endDate.isAfterOrEqual(compareTspan.endDate)){
                    //System.out.println("Ok EndDate");
                }
                else{
                    return 0d;
                }
                
                srcCompareRecord.replaceValues(otherVal, "");
                return 1d;
                
            }
        }
        
        return comparisonResult;
    }
    
    
    public int calculateFinalSimilarity(SequenceSimilarityResultVector similarities, double denominator) {
        Double result = 0d;
        
        
        /*
        for (int i = 0; i < similarities.size(); i++) {
            SequenceSimilarityResult pair = similarities.get(i);

            
            if(AcceptUriEquality){
                //uri equality of instances
                if(pair.getSequenceId()==0){
                    //System.out.println("Found sequecne with id =0");
                    return 100;
                }
            }
            
        
        }
        */
        
        
        int maxSequences = this.imClass.userConfig.getNumberOfSequences();
        
        int seqCounter = 0;
        while(seqCounter<maxSequences){
            
            Double  seqWeight = this.imClass.userConfig.getWeightAtUserQueryIndex(seqCounter);
            Double seqMaxSimilarity =0d;
            
            for (int i = 0; i <similarities.size(); i++) {
                SequenceSimilarityResult pair = similarities.get(i);
                if(pair.getSequenceId() != seqCounter){
                    continue;
                }
                seqMaxSimilarity = pair.getSimilarity();
            }
                
            
            result += seqWeight * seqMaxSimilarity;
            seqCounter++;        
        }
        
        result = result / denominator;
        result*=100;
        
        int tempInt = Math.round(result.floatValue());

        return tempInt;
    }
    /*
    public int calculateFinalSimilarity(Vector<PredicateWeightPair> similarities, float denominator) {
        float result = 0;

        for (int i = 0; i < similarities.size(); i++) {
            PredicateWeightPair pair = similarities.get(i);

            if (pair.getPredicate() == null) {
                //CBIMClass.AcceptUriEquality is not needed here as this should alredy have filtered out the case where 
                //pred is null				
                return 100;
            } else {
                float weight = pair.getWeight().floatValue();
                result += weight * pair.getSimilarity();

            }
        }

        result = result / denominator;

        int tempInt = (int) (result * 100);

        return tempInt;
    }*/
}
