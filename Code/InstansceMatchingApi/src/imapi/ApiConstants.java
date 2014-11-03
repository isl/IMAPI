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
public class ApiConstants {
    

    static final int onLineDBQueryStepSize = 50000;
    static final boolean KeepandPresentAllTargetDataFound = false;
    
    static int yearsJitter =5;
    //static int averageLifespan = 100;
    
    //final decision to set this value to 1 because if set to a greater value 
    //BM collection database seemed not to respond well especially when the first 
    //value of filter did not exist
    static final int UriFilterStepCount = 1;
    
    
    //static final double literalMinimumSimilarityValue = 0.4d;
    public static final int IMAPISuccessCode = 0;
    public static final int IMAPIFailCode = -1;
    //static final String defaultCidocCrmNamespace  = "http://www.cidoc-crm.org/cidoc-crm/";
    //static final String BMcollectionNamespace = "http://erlangen-crm.org/current/";
    
    //static final boolean breakPredicateInfoRetrievalWhenFirstFound = true;
    
    public enum PredicateDirectionUsage{DIRECT,INVERSE,BOTH};
    
    public enum TargetSourceChoice {

        FILE_COMPARISON, BRITISH_MUSEUM_COLLECTION, /*CULTURA_ITALIA,*/ CLAROS
    };

    /*
    public enum SearchModeChoice {
        ACTOR
    };
    */

    static final boolean Compare_CidocCrmCompatibleFile_All_fields = false;
}
