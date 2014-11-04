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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Elias Tzortzakakis <tzortzak@ics.forth.gr>
 */
public class SourceDataHolder extends Hashtable<String,Hashtable<String,SequencesVector>>{

    Vector<String> collectAllUriValuesOfInstances(){
        Vector<String> returnVals = new Vector<String>();
        
        Enumeration<String> fileEnum = this.keys();
        while(fileEnum.hasMoreElements()){
            String file = fileEnum.nextElement();
            Vector<String> uris = new Vector<String>(this.get(file).keySet());
            for(int i=0; i< uris.size(); i++){
                if(returnVals.contains(uris.get(i))==false){
                    returnVals.add(uris.get(i));
                }            
            }
            
        }
        
        return returnVals;
    }
    
    Vector<String> collectAllValuesOfSpecificParameter(int sequencePosition,  String parameterName){
        Vector<String> returnVals = new Vector<String>();
        
        Enumeration<String> filesEnum = this.keys();
        while(filesEnum.hasMoreElements()){
            String file = filesEnum.nextElement();
            
            Enumeration<String> uriEnum = this.get(file).keys();
            while(uriEnum.hasMoreElements()){
                String uri = uriEnum.nextElement();
                SequencesVector seqVec = this.get(file).get(uri);
                if(seqVec==null || seqVec.size()==0){
                    continue;
                }

                SequenceData seqData = seqVec.getSequenceDataAtPosition(sequencePosition);
                if(seqData==null){
                    continue;
                }

                Vector<DataRecord> pairVals = seqData.getValuesOfKey(parameterName);
                for(int i=0; i<pairVals.size();i++){
                    if(returnVals.contains(pairVals.get(i).getValue())==false){
                        returnVals.add(pairVals.get(i).getValue());
                    }
                }
            }
        }
        
        return returnVals;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Abandoned not necessarily working code">
    /*
    Vector<String> DELETE_collectAllUriValuesOfInstancesAndSequences(){
        
        
        Vector<String> returnVals = new Vector<String>();
        
        Enumeration<String> filesEnum = this.keys();
        while(filesEnum.hasMoreElements()){
            String file = filesEnum.nextElement();
            
            Enumeration<String> uriEnum = this.get(file).keys();
            while(uriEnum.hasMoreElements()){
                String uri = uriEnum.nextElement();
                if(returnVals.contains(uri)==false){
                    returnVals.add(uri);
                }
                SequencesVector seqVec = this.get(file).get(uri);
            
                if(seqVec==null || seqVec.size()==0){
                    continue;
                }
                for(int i=0; i< seqVec.size(); i++){
                    SequenceData seqData = seqVec.get(i);
                    if(seqData==null){
                        continue;
                    }
                    Vector<String> parameterNamesOfUriType = new Vector<String>();

                    Hashtable<String,String> seqParameterNamesAndTypes = seqData.getSchemaInfo().getAllQueryStepParameters();

                    Enumeration<String> paramNamesEnum = seqParameterNamesAndTypes.keys();
                    while(paramNamesEnum.hasMoreElements()){
                        String paramName = paramNamesEnum.nextElement();
                        String paramType = seqParameterNamesAndTypes.get(paramName);

                        if(paramType.equals(ApiConstants.Type_URI)){
                            parameterNamesOfUriType.add(paramName);
                        }
                    }

                    if(parameterNamesOfUriType.size()>0){
                        for(int k=0; k<parameterNamesOfUriType.size();k++){

                            Vector<DataRecord> pairVals = seqData.getValuesOfKey(parameterNamesOfUriType.get(k));
                            for(int m=0; m<pairVals.size();m++){
                                if(returnVals.contains(pairVals.get(m).getValue())==false){
                                    returnVals.add(pairVals.get(m).getValue());
                                }
                            }

                        }
                    }

                }           

            }
        }
        
        return returnVals;
    }
    
    
    boolean DELETE_containsValuesOfSpecificParameter(int sequencePosition,  String parameterName){
        
        Enumeration<String> filesEnum = this.keys();
        while(filesEnum.hasMoreElements()){
            String file = filesEnum.nextElement();
            
            Enumeration<String> uriEnum = this.get(file).keys();
            while(uriEnum.hasMoreElements()){
                String uri = uriEnum.nextElement();
                SequencesVector seqVec = this.get(file).get(uri);

                if(seqVec==null || seqVec.size()==0){
                    continue;
                }

                SequenceData seqData = seqVec.getSequenceDataAtPosition(sequencePosition);
                if(seqData==null){
                    continue;
                }

                Vector<DataRecord> pairVals = seqData.getValuesOfKey(parameterName);

                for(int i=0; i<pairVals.size();i++){
                    if(pairVals.get(i).getValue().trim().length()>0){
                        return true;
                    }

                }
            }
        }
        return false;
    }
    */
    //</editor-fold>
}
