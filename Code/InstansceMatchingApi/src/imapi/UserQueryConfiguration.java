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
 * @author tzortzak
 */
class UserQueryConfiguration {
    
    private String mnemonic ="";
    private String parameterizedMnemonic ="";
    private Double weight =0d;
    private int positionID;
    private Hashtable<String,String> predicateChoices = new Hashtable<String,String>();
    private QueryPrototypeSequence prototypeSequence;   
    
    private Hashtable<String,String> allQueryStepParameters = new Hashtable<String,String>();
    private String[] sortedParameterNames;
    private String[] sortedParameterTypes;
    private String[] sortedQueries;
    
    public Double getWeight(){
        return this.weight;
    }
    
    public String getMnemonic(){
        if(this.parameterizedMnemonic.length()>0){
            return "\""+this.parameterizedMnemonic +"\"";
        }
        else{
            return this.mnemonic;
        }
    }
    
    private void setParameterizedMnemonic(){
        String str = this.mnemonic;
        Vector<Character> seperatorChars = new Vector<Character>();
        seperatorChars.add('(');
        seperatorChars.add(')');
        seperatorChars.add(' ');
        seperatorChars.add('\t');
        seperatorChars.add('-');
        seperatorChars.add('>');
        seperatorChars.add('|');
        seperatorChars.add('&');
        
        Enumeration<String> paramEnum = this.predicateChoices.keys();
        while(paramEnum.hasMoreElements()){
            String key = paramEnum.nextElement();
            String val = this.predicateChoices.get(key);
            
            int loopChars = 0;
            int nextIndex = str.indexOf(key, loopChars);
            
            
            while(nextIndex >=0){
                
                //int indexOfKey  = str.indexOf(key);
                int maxIndex = str.length()-1;
                boolean skipReplaceMent = false;
                
                if(nextIndex>0){
                    char charBefore = str.charAt(nextIndex-1);
                    if(seperatorChars.contains(charBefore)==false){
                        skipReplaceMent = true;
                    }
                }
                
                
                if(nextIndex<maxIndex){
                    
                    char charAfter = str.charAt(nextIndex+key.length());
                    if(seperatorChars.contains(charAfter)==false){
                        skipReplaceMent = true;
                    }
                }
                
                if(skipReplaceMent==false){
                    String strBefore = str.substring(0,nextIndex);
                    
                    String strAfter  = str.substring(nextIndex+key.length());
                    
                    str = strBefore + val+strAfter;                    
                    
                }
                
                maxIndex = str.length()-1;
                
                loopChars += nextIndex+key.length();
                
                if(loopChars>=maxIndex){
                    break;
                }
                nextIndex = str.indexOf(key, loopChars);
            }
            
        }
        
        this.parameterizedMnemonic = str;
        
    }
    
    
    public int getPositionID(){
        return this.positionID;
    }
    
    
    
    public Hashtable<String,String> getAllQueryStepParameters(){
        Hashtable<String,String> returnVal = new Hashtable<String,String>();
        Enumeration<String> paramEnum = this.allQueryStepParameters.keys();
        while(paramEnum.hasMoreElements()){
            String key = paramEnum.nextElement();
            String val = this.allQueryStepParameters.get(key);
            returnVal.put(key, val);
                    
        }
        
        return returnVal;
    }
    
    public String[] getSortedParameterNamesCopy(){
        String[] returnVal = new String[this.sortedParameterNames.length];
        
        for(int i=0; i< this.sortedParameterNames.length; i++){
            returnVal[i] = this.sortedParameterNames[i];
        }
        
        return returnVal;
    }
    
    String[] getSortedParameterTypesCopy(){
        String[] returnVal = new String[this.sortedParameterTypes.length];
        
        for(int i=0; i< this.sortedParameterTypes.length; i++){
            returnVal[i] = this.sortedParameterTypes[i];
        }
        
        return returnVal;
    }
    
    
    String[] getSortedQueriesCopy(){
        String[] returnVal = new String[this.sortedQueries.length];
        
        for(int i=0; i< this.sortedQueries.length; i++){
            returnVal[i] = this.sortedQueries[i];
        }
        
        return returnVal;
    }
    
    UserQueryConfiguration(int position, QueryPrototypeSequence prototypeSeq, String mnemonicStr, Double weightVal, Hashtable<String,String> predicateChoicesTable){        this.mnemonic = mnemonicStr;
        this.weight = weightVal;
        Enumeration<String> userChoicesParams = predicateChoicesTable.keys();
        while(userChoicesParams.hasMoreElements()){
            String key = userChoicesParams.nextElement();
            String val = predicateChoicesTable.get(key);
            
            if(this.predicateChoices.containsKey(key)==false){
                this.predicateChoices.put(key, val);
            }
        }
        this.prototypeSequence = prototypeSeq;
        this.positionID = position;
        
        
        allQueryStepParameters = this.prototypeSequence.getAllSequenceParameterNamesAndTypes(); 
        sortedParameterNames   = this.prototypeSequence.getAllSequenceParametersNamesSorted();
        sortedParameterTypes   = this.prototypeSequence.getAllSequenceParametersTypesSorted();
        sortedQueries          = this.prototypeSequence.getAllSequenceQueriesSorted(this.predicateChoices);
        
        setParameterizedMnemonic();
    }
       
    UserQueryConfiguration copy(){
                
        UserQueryConfiguration returnVal = new UserQueryConfiguration(this.positionID, this.prototypeSequence, this.mnemonic, this.weight, this.predicateChoices);        
        return returnVal;
    }
    
    
    void replaceQueryInQuerySteps(String initialQuery, String newQuery){
        
        for(int i=0; i < this.sortedQueries.length; i++)
        {
            String initQuery = this.sortedQueries[i].trim();
            if(initQuery.equals(initialQuery.trim())){
                this.sortedQueries[i]=newQuery.trim();
            }            
        }

    }
    
    
}
