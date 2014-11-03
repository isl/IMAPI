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
class QueryPrototypeSequenceStep {
    private int stepPositionInSequence; 
    private String paramName = "";
    private String paramType = "";
    private String query = "";
    
    QueryPrototypeSequenceStep(int position, String parameterName, String parameterType, String queryString){
        this.stepPositionInSequence = position;
        this.paramName = parameterName;
        this.paramType = parameterType;
        this.query     = queryString;        
    }
    
    int getStepPositionInSequence(){
        return this.stepPositionInSequence;
    }
    
    String getParameterName(){
        return this.paramName;
    }
    
    String getParameterType(){
        return this.paramType;        
    }
    
    String getQuery(){
        return this.query;
    }
    
    QueryPrototypeSequenceStep makeCopy(){
        return new QueryPrototypeSequenceStep(stepPositionInSequence, paramName, paramType, query);
    }
    
}
