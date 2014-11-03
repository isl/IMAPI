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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class QueryPrototypeSequence {
    
    private String mnemonic ="";    
    private Hashtable<String, String> parameters = new Hashtable<String, String>();
    private Vector<QueryPrototypeSequenceStep> querySteps = new Vector<QueryPrototypeSequenceStep>();
    private int minQueryStepPosition=1000;
    private int maxQueryStepPosition=0;
    private int queryStepsSize =0;
    
    String getMnemonic(){
        return this.mnemonic;
    }
    
    Hashtable<String, String> getParameterNames(){
        return this.parameters;
    }
    
    void retrieveParameterNames(String mnemonicStr){
        parameters.clear();
        
        String tempStr = mnemonicStr;
        tempStr=tempStr.replaceAll("[()->|]", " ");
        
        
        String[] parts = tempStr.split(" ");
        for(int i=0; i< parts.length; i++){
            String tmp = parts[i];
            if(tmp==null || tmp.trim().length()==0){
                continue;
            }
            tmp = tmp.trim();
            if(this.parameters.containsKey(tmp)==false){
                this.parameters.put(tmp, tmp);
            }
        }
    }
    
    QueryPrototypeSequence(String mnemonicString, Vector<QueryPrototypeSequenceStep> qSteps){
        this.mnemonic = mnemonicString;
        parameters = new Hashtable<String, String>();
        this.retrieveParameterNames(mnemonicString);
        this.querySteps = new Vector<QueryPrototypeSequenceStep>();
        for(int i=0; i<qSteps.size(); i++){
            QueryPrototypeSequenceStep newStep = qSteps.get(i).makeCopy();
            this.querySteps.add(newStep);
            int newStepPosition = newStep.getStepPositionInSequence();
            if(minQueryStepPosition>newStepPosition){
                minQueryStepPosition = newStepPosition;
            }
            if(maxQueryStepPosition<newStepPosition){
                maxQueryStepPosition = newStepPosition;
            }
        }
        queryStepsSize = this.querySteps.size();
    }
    
    int getMinQueryStepPosition(){
        return this.minQueryStepPosition;
    }
    
    int getMaxQueryStepPosition(){
        return this.maxQueryStepPosition;
    }
    
    QueryPrototypeSequenceStep getQueryStepAtPosition(int position){
        if(this.querySteps==null){
            return null;
        }
        
        for(int i=0; i< this.querySteps.size(); i++){
            if(this.querySteps.get(i).getStepPositionInSequence()==position){
                return this.querySteps.get(i);
            }
        }        
        return null;
    }
    
    Hashtable<String,String> getAllSequenceParameterNamesAndTypes(){
        Hashtable<String,String> returnVal = new Hashtable<String,String>();
        
        for(int i=0; i< querySteps.size(); i++){
            String pName = querySteps.get(i).getParameterName();
            String pType = querySteps.get(i).getParameterType();
            returnVal.put(pName, pType);            
        }
        
        return returnVal;
    }
    
    
    String[] getAllSequenceParametersNamesSorted(){
        Vector<String> returnVec = new Vector<String>();
        
        int howmanySteps = querySteps.size();
        
        int counter =0;
        while(counter<howmanySteps){
            for(int i=0; i< howmanySteps; i++){
                if(querySteps.get(i).getStepPositionInSequence()==counter){
                    counter ++;
                    returnVec.add(querySteps.get(i).getParameterName());
                    break;
                }
            }
        }
        String[] returnVal = new String[returnVec.size()];
        for(int i=0; i< returnVec.size(); i++){
            returnVal[i] = returnVec.get(i);
        }
        
        return returnVal;
    }
    
    String[] getAllSequenceParametersTypesSorted(){
        Vector<String> returnVec = new Vector<String>();
        
        int howmanySteps = querySteps.size();
        
        int counter =0;
        while(counter<howmanySteps){
            for(int i=0; i< howmanySteps; i++){
                if(querySteps.get(i).getStepPositionInSequence()==counter){
                    counter ++;
                    returnVec.add(querySteps.get(i).getParameterType());
                    break;
                }
            }
        }
        
        String[] returnVal = new String[returnVec.size()];
        for(int i=0; i< returnVec.size(); i++){
            returnVal[i] = returnVec.get(i);
        }
        
        return returnVal;
    }
    
    
    String[] getAllSequenceQueriesSorted(Hashtable<String, String> predicateChoices){
        Vector<String> returnVec = new Vector<String>();
        
        int howmanySteps = querySteps.size();
        
        int counter =0;
        while(counter<howmanySteps){
            for(int i=0; i< howmanySteps; i++){
                if(querySteps.get(i).getStepPositionInSequence()==counter){
                    counter ++;
                    String query = this.replacePredicateChoicesInQuery(querySteps.get(i).getQuery(),predicateChoices); 
                    if(query.length()>0){
                        returnVec.add(query);
                    }                    
                    break;
                }
            }
        }
        
        
        
        
        String[] returnVal = new String[returnVec.size()];
        for(int i=0; i< returnVec.size(); i++){
            returnVal[i] = returnVec.get(i);
        }
        
        return returnVal;
    }
    
    private String replacePredicateChoicesInQuery(String initial, Hashtable<String, String> predicateChoices){
        
        
        String query = initial;
            
        Enumeration<String> paramVals = predicateChoices.keys();
        while(paramVals.hasMoreElements()){
            String key = paramVals.nextElement();
            String val = "###PID:"+predicateChoices.get(key)+"###";
            String searchForVal  = "###UNSETPID:" + key +"###";
            while(query.contains(searchForVal)){
                query = query.replace(searchForVal,val);
            }
        }
        
        if(query.contains("###UNSETPID:")){
            System.out.println("Could not replace all parameter  values of:\n"+initial + "\n\nBest effort trial returned query:\n"+query);
            return "";
        }
        
        return query;
        
    }
    
    
}
