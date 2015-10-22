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

import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import com.hp.hpl.jena.sparql.function.library.substr;
import com.hp.hpl.jena.sparql.util.RomanNumeral;
//import com.sun.xml.internal.fastinfoset.util.StringArray;

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

    private Double combineStringSimilarities(Vector<Double> vec) {

        Double tempResult = 0d;

        //average
        for (int m = 0; m < vec.size(); m++) {
            tempResult += vec.get(m);
        }

        tempResult = tempResult / vec.size();

        //best
	//for(int m=0;m<vec.size(); m++){
            //if(vec.get(m)> tempResult){
                //tempResult = vec.get(m);
            //}
         //}
         
        return tempResult;
    }
    
   // ComparisonResult compareValueAgainstVector(String valueType, DataRecord valueToCompare, Vector<DataRecord> restOfValues, DataRecord srcCompareRecord, String mnemonic, Hashtable<String,String> table){
    Double compareValueAgainstVector(String valueType, DataRecord valueToCompare, Vector<DataRecord> restOfValues, DataRecord srcCompareRecord, String mnemonic, Hashtable<String,String> nationalityTable, Hashtable <String,String> placesTable){
    	  Double comparisonResult = 0d;
    	
    	ComparisonResult comparisonResultObj=new ComparisonResult ();
    	comparisonResultObj.setBreakMethod(false);
    	comparisonResultObj.setComparisonRes(0.0);
    	
    	
        //uri comparison
        if(valueType.equals(ApiConstants.Type_URI)){
        	
        	if (mnemonic.contains("Nationality")){
        		

                String uriVal = valueToCompare.getValue();	
        		
                for(int i=0; i< restOfValues.size(); i++){
                	String otherVal=restOfValues.get(i).getValue();
                	
                	
                	if(nationalityTable.containsKey(otherVal)){
                		otherVal=nationalityTable.get(otherVal);
                	}
                	//else return comparisonResult;
                	else return 0d;
                	
                    if(otherVal.equals(uriVal)){
                    	srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                    	comparisonResultObj.setComparisonRes(1.0);
                       // return comparisonResult;
                    	return 1.0d;
                    }
                    //gia na kanw ton diaxwrismo poia nationalities exoyn sygkri8ei alla einai diaforetika apo ekeina pou tous leipei to stoixeio tou nationality opote pali vgainei 0.
                    else {
                    	comparisonResultObj.setBreakMethod(true);
                    	comparisonResultObj.setComparisonRes(0.0);
                    	//return comparisonResult;
                    	return 0d;
                    }
                
                }
        	}

        	
        	else{
            String uriVal = valueToCompare.getValue();
            for(int i=0; i< restOfValues.size(); i++){
                if(restOfValues.get(i).getValue().equals(uriVal)){
                    srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                    comparisonResultObj.setComparisonRes(1.0);
                    //return comparisonResult;
                    return 1.0d;
                }
            }
            
            
        	} 
            
        }

        
        
        
        //literal comparison 
        if(valueType.equals(ApiConstants.Type_Literal)){
        	boolean RomanFlag=false;
        	
        	String[] romanNumbers={" ii#"," iii#"," iv#"," v#"," vi#"," vii#"," viii#"," ix#"," x#"," xi#"," xii#"," xiii#"," xiv#"," xv#"," xvi#",
        			" xvii#"," xviii#"," xix#"," xx#"};
        	int otherValposition=500;
         	int valToSearchposition=500;
        	
         
            String otherVal = valueToCompare.getValue().toLowerCase();
            
            
            // the code below is for the appellation matching algorithm
            if (otherVal!=null && mnemonic.contains("literalSurname")){
            	
            	if (otherVal.contains("monogramist")||otherVal.contains("carpenter")||otherVal.contains("carpentier")){
            		comparisonResultObj.setBreakMethod(true);
            		 //return comparisonResult;
            		return 0.0d;
            	}
            		 
            	else{
            	
            	otherVal=doReplacements(otherVal);
      
            	if (otherVal.indexOf(",")>0){
            		otherVal=otherVal.substring(0, otherVal.indexOf(","));
            	
            	}
            	otherVal=otherVal.trim();
        		otherVal=otherVal+"#";
            
            	for (int n = 0; n < romanNumbers.length; n++) {
            		
		            if (otherVal.contains(" "+romanNumbers[n])){
		            	RomanFlag=true;
		            	otherValposition=n;
		            }
		        }
              }
            }
            
            // the code below is for the appellation matching algorithm
            else if (otherVal!=null && mnemonic.contains("literalForname")){
            	
            		otherVal=doReplacements(otherVal);
            	if (otherVal.indexOf(",")>0){
            		otherVal=otherVal.substring(otherVal.indexOf(",")+1);
            		otherVal=otherVal.trim();
            		otherVal=otherVal+"#";
            		
            		for (int n = 0; n < romanNumbers.length; n++) {
                		
    		            if (otherVal.contains(" "+romanNumbers[n])){
    		            	RomanFlag=true;
    		            	otherValposition=n;
    		            }
    		        }
            	}
            	else {
            		otherVal="";
            		return 0d;
            	}
            		
            	
            }
            
            if (!mnemonic.contains("placesLiteral")){
            
            for(int i=0; i< restOfValues.size(); i++){
            	
                String valToSearch = restOfValues.get(i).getValue().toLowerCase();
                
                // the code below is for the appellation matching algorithm
                if (valToSearch!=null && mnemonic.contains("literalSurname")){
                	
                	valToSearch=doReplacements(valToSearch);
                	if (valToSearch.contains("monogramist")||valToSearch.contains("carpenter")||valToSearch.contains("carpentier"))
                		 return 0d;
                	
                	//valToSearch=doReplacements(valToSearch);
          
                	if (valToSearch.indexOf(",")>0){
                		valToSearch=valToSearch.substring(0, valToSearch.indexOf(","));
                	
                	}
                	
                	valToSearch=valToSearch.trim();
            		valToSearch=valToSearch+"#";
                
                	for (int l = 0; l < romanNumbers.length; l++) {
                		
    		            if (valToSearch.contains(" "+romanNumbers[l])){
    		            	RomanFlag=true;
    		            	valToSearchposition=l;
    		            }
    		        }
                }
                
                else if (valToSearch!=null && mnemonic.contains("literalForname")){
                	
                	valToSearch=doReplacements(valToSearch);
                	
                	if (valToSearch.indexOf(",")>0){
                		valToSearch=valToSearch.substring(valToSearch.indexOf(",")+1);
                		valToSearch=valToSearch.trim();
                		valToSearch=valToSearch+"#";
                		
                		for (int l = 0; l < romanNumbers.length; l++) {
                    		
        		            if (valToSearch.contains(romanNumbers[l])){
        		            	RomanFlag=true;
        		            	valToSearchposition=l;
        		            }
        		        }
                	}
                	else { valToSearch="";}
                		
                	
                }
                
                 if (otherVal!=null && valToSearch!=null && (mnemonic.contains("literalForname")|| mnemonic.contains("literalSurname"))){
                	//System.out.println("RomanFlag "+RomanFlag+" mnemonic "+mnemonic+" otherVal "+otherVal+" valToSearch "+valToSearch+" otherValposition "+otherValposition+" valToSearchposition "+valToSearchposition);
                	 
                	 if (RomanFlag=true && otherValposition!=valToSearchposition)
                		
                		 return 0d;
                 }
                
                Vector<Double> tempVec = new Vector<Double>();
                
                //case STR_CHARFREQ
                        //tempVec.add(new Double(strSimilarity.computeCharFrequencySimilarity(valToSearch, otherVal)));
                        //case STR_DIGRAM
                        tempVec.add(new Double(strSimilarity.computeDigramSimilarity(valToSearch, otherVal)));
                        //case STR_TRIGRAM
                        //tempVec.add(new Double(strSimilarity.computeTrigramSimilarity(valToSearch, otherVal)));
                        //case STR_SOUNDEX
                        //tempVec.add(new Double(strSimilarity.computeSoundexSimilarity(valToSearch, otherVal)));
                        //case STR_EDITDISTANCE
                        tempVec.add(new Double(strSimilarity.computeEditDistanceSimilarity(valToSearch, otherVal)));
                        //case STR_SINGLEERROR
                        //tempVec.add(new Double(strSimilarity.computeSingleErrorSimilarity(valToSearch, otherVal)));

                        Double tempResult = combineStringSimilarities(tempVec);

                        if(tempResult>=this.imClass.userConfig.getMinimumLiteralSimilarity()){
                            if (comparisonResult < tempResult) {
                                comparisonResult = tempResult;
                                srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                            }
                        }
            	}
            }
        	else if(mnemonic.contains("placesLiteral")){
        		
        		Vector<Double> tempVec = new Vector<Double>();
        		
        		String valToSearch = valueToCompare.getValue();	
        		
                for(int i=0; i< restOfValues.size(); i++){
                	 otherVal=restOfValues.get(i).getValue();
                	
                    if(otherVal.equals(valToSearch)){
                    	srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                    	comparisonResultObj.setComparisonRes(1.0);
                       // return comparisonResult;
                    	return 1.0d;
                    }
                    else {
                    	
                    if (placesTable.containsKey(otherVal)){
                		otherVal=placesTable.get(otherVal);
                		
                		  if(otherVal.equals(valToSearch)){
                          	srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                          	comparisonResultObj.setComparisonRes(1.0);
                             // return comparisonResult;
                          	return 1.0d;
                          }     
                		  else {
                			   //case STR_CHARFREQ
                              //tempVec.add(new Double(strSimilarity.computeCharFrequencySimilarity(valToSearch, otherVal)));
                              //case STR_DIGRAM
                              tempVec.add(new Double(strSimilarity.computeDigramSimilarity(valToSearch, otherVal)));
                              //case STR_TRIGRAM
                              //tempVec.add(new Double(strSimilarity.computeTrigramSimilarity(valToSearch, otherVal)));
                              //case STR_SOUNDEX
                              //tempVec.add(new Double(strSimilarity.computeSoundexSimilarity(valToSearch, otherVal)));
                              //case STR_EDITDISTANCE
                              tempVec.add(new Double(strSimilarity.computeEditDistanceSimilarity(valToSearch, otherVal)));
                              //case STR_SINGLEERROR
                              //tempVec.add(new Double(strSimilarity.computeSingleErrorSimilarity(valToSearch, otherVal))); 
                              
                              Double tempResult = combineStringSimilarities(tempVec);
                              
                              if(tempResult>=this.imClass.userConfig.getMinimumLiteralSimilarity()){
                                  if (comparisonResult < tempResult) {
                                      comparisonResult = tempResult;
                                      srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                                  }
                              }
                		  }
                    
                    }
                	//else return comparisonResult;
                	//else return 0d;
                    else{
     			   //case STR_CHARFREQ
                    //tempVec.add(new Double(strSimilarity.computeCharFrequencySimilarity(valToSearch, otherVal)));
                    //case STR_DIGRAM
                    tempVec.add(new Double(strSimilarity.computeDigramSimilarity(valToSearch, otherVal)));
                    //case STR_TRIGRAM
                    //tempVec.add(new Double(strSimilarity.computeTrigramSimilarity(valToSearch, otherVal)));
                    //case STR_SOUNDEX
                    //tempVec.add(new Double(strSimilarity.computeSoundexSimilarity(valToSearch, otherVal)));
                    //case STR_EDITDISTANCE
                    tempVec.add(new Double(strSimilarity.computeEditDistanceSimilarity(valToSearch, otherVal)));
                    //case STR_SINGLEERROR
                    //tempVec.add(new Double(strSimilarity.computeSingleErrorSimilarity(valToSearch, otherVal))); 
                    
                    Double tempResult = combineStringSimilarities(tempVec);
                    
                    if(tempResult>=this.imClass.userConfig.getMinimumLiteralSimilarity()){
                        if (comparisonResult < tempResult) {
                            comparisonResult = tempResult;
                            srcCompareRecord.replaceValues(restOfValues.get(i).getValue(), restOfValues.get(i).getLang());
                        }
                    }
                    }
                	}
                    
                }
        	}
            

        }
        
        //date comparison
        if(valueType.equals(ApiConstants.Type_Date)){
            
        }
        
        //timespan comparison
        if(valueType.equals(ApiConstants.Type_Timespan)){
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
                
                
                
                if( baseTspan.startDate.isBeforeOrEqual(compareTspan.startDate) &&  baseTspan.endDate.isAfterOrEqual(compareTspan.endDate)){
                    //System.out.println("Ok tDate");
                }
                
                else if(compareTspan.startDate.isBeforeOrEqual(baseTspan.startDate) && compareTspan.endDate.isAfterOrEqual(baseTspan.endDate)){
                    //System.out.println("Ok Date");
                }
                
                else if( baseTspan.startDate.isBeforeOrEqual(compareTspan.startDate) && 
                		compareTspan.endDate.isAfterOrEqual(baseTspan.endDate) && 
                		baseTspan.endDate.isAfterOrEqual (compareTspan.startDate) ) {
                	 //System.out.println("Ok Date");
                }
                else if(compareTspan.startDate.isBeforeOrEqual(baseTspan.startDate) && 
                		baseTspan.endDate.isAfterOrEqual(compareTspan.endDate) &&
                		compareTspan.endDate.isAfterOrEqual (baseTspan.startDate) ) {
                	//System.out.println("Ok Date");
                }
                
                else{
                    return 0d;
                }
                
                srcCompareRecord.replaceValues(otherVal, "");
                return 1d;
                
            }
        }
        
        ///To be deleted after evas Test
//        if (comparisonResult<0.4){
//        	return 0d;
//        }
//        else
       	///To be deleted
        	return comparisonResult;
    }
    
    int calculateFinalSimilarity(SequenceSimilarityResultVector similarities, double denominator) {
        Double result = 0d;
        
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
    
    String doReplacements(String val){
    	val=val.replace(".", " ");
    	val=val.replace("(", "");
    	val=val.replace(")", "");
    	
    	return val;
    }
    
}
