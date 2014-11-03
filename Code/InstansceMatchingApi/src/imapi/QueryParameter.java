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

enum ParameterTypes{ URI, LITERAL, DATE, TIMESPAN };

class QueryParameter {
    
    private String paramName;
    private ParameterTypes paramType;
    
    String getParameterName(){
        return this.paramName;
    }
    
    ParameterTypes getParameterType(){
        return this.paramType;
    }
    
    QueryParameter(String parameterName, ParameterTypes parameterValueType){
        this.paramName = parameterName;
        this.paramType = parameterValueType;
    }
    
    
}
