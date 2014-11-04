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

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class Utilities {
    
    static DecimalFormat df = new DecimalFormat("#.##");
    
    static String getCurrentTime(){
        return java.util.Calendar.getInstance().getTime().toString();
    }
    
    static void handleException(Exception ex){
        if(IMAPIClass.DEBUG){
            if(ex.getClass()!=null && ex.getClass().getName()!=null){
                System.out.println(ex.getClass().getName());
            }
            if(ex.getMessage()!=null){
                System.out.println(ex.getMessage());
            }
            ex.printStackTrace(System.out);
            System.out.println();
        }
    }
    
    static Vector<String> collectSequenctiallyAsubsetOfValues(int startindex,int step, Vector<String> targetVals){
        Vector<String> returnVals = new Vector<String>();
        
        int maxIndex =targetVals.size(); 
        if(startindex<targetVals.size()){
            for(int i = 0; i< step; i++){
                
                if((startindex+i)>=maxIndex){
                    break;
                }
                else{
                    returnVals.add(targetVals.get(i+startindex));
                }
                
                
            }
        }
        
        return returnVals;
    }
    
    private IMAPIClass imClass =null;
    
    Utilities(IMAPIClass imApiClass){
        this.imClass = imApiClass;
                
    }
    
    boolean canAllSequencesFollowFastApproach(Vector<UserQueryConfiguration> qSequences, 
            SourceDataHolder inputSourceInfo, 
            Vector<Boolean>  canQuickFilteringMethodBeFollowedForeachSequence){
        
        boolean allSequencesFast = true;
        canQuickFilteringMethodBeFollowedForeachSequence.clear();
        
                
        //<editor-fold defaultstate="collapsed" desc="Analyze User sequences and source data in order to see if all database uris must be searched or filtering may be applied">
        
        //For each sequence determine if Quick filtering method 
        //may be followed. that is that for eahc sequence 
        //1) source data retrieved contain only uris
        //2) source data retrieved contain only uris and literals and literal similarity threshold is set to 1
        
        
        //for every sequence get the parameter types and 
        //for each parameter type that is not uri check 
        //the source data found. if values are found there 
        //and parameter is of timespan type or parameter is
        //of type literal and literal threshold is less that 1 then 
        //canQuickFilteringMethodBeFollowed = false; break;
        
        
        for(int i=0; i< qSequences.size(); i++){
            
            boolean canQuickFilteringMethodBeFollowed = true;
            
            int sequencePosition = qSequences.get(i).getPositionID();
            String[] parameterNames = qSequences.get(i).getSortedParameterNamesCopy();
            String[] parameterTypes = qSequences.get(i).getSortedParameterTypesCopy();
            
            for(int k=0; k< parameterTypes.length; k++){
                String type = parameterTypes[k];
                if(type.equals(ApiConstants.Type_URI)){
                    continue;
                }
                if(type.equals(ApiConstants.Type_Literal) && this.imClass.userConfig.getMinimumLiteralSimilarity()==1d){
                    continue;
                }
                
                
                Enumeration<String> filesEnum = inputSourceInfo.keys();
                while(filesEnum.hasMoreElements()){
                    if(canQuickFilteringMethodBeFollowed==false){
                        break;
                    }
                    String file = filesEnum.nextElement();
                    Hashtable<String, SequencesVector> fileinfo = inputSourceInfo.get(file);

                    Enumeration<String> instanceEnum = fileinfo.keys();
                    while(instanceEnum.hasMoreElements()){
                        
                        SequencesVector seqVec = fileinfo.get(instanceEnum.nextElement());

                        SequenceData seqData = seqVec.getSequenceDataAtPosition(sequencePosition);
                        if(seqData!=null){
                            Vector<DataRecord> vals = seqData.getValuesOfKey(parameterNames[k]);
                            if(vals !=null && vals.size()>0){
                                canQuickFilteringMethodBeFollowed = false;
                                break;
                            }
                        }
                    }
                        
                    
                }
                
                if(canQuickFilteringMethodBeFollowed==false){
                    break;
                }
            }
            canQuickFilteringMethodBeFollowedForeachSequence.add(canQuickFilteringMethodBeFollowed);
        }
        
        
        
        
        for(int m=0;m< canQuickFilteringMethodBeFollowedForeachSequence.size(); m++){
            if(canQuickFilteringMethodBeFollowedForeachSequence.get(m)==false){
                allSequencesFast = false;
                break;
            }
        }
        //</editor-fold>
        
        
        return allSequencesFast;   
    }
    
    
}
