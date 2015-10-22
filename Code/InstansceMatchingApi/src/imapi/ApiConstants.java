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

/**
 *
 * @author tzortzak
 */
public class ApiConstants {
    

    
    public static final int yearsJitter =5;
    
    //only true is implemented for checkForSimilaritiesAsWeGetResults
    static final boolean checkForSimilaritiesAsWeGetResults = true;
    
    public static final long timeToWaitAfterAQueryFailure = 1000*60*12;
    public static final int IMAPISuccessCode = 0;
    public static final int IMAPIFailCode = -1;
    
    public enum PredicateDirectionUsage{DIRECT,INVERSE,BOTH};
    
    public enum TargetSourceChoice {

        FILE_COMPARISON, BRITISH_MUSEUM_COLLECTION, /*CULTURA_ITALIA,*/ CLAROS, BM_BIOGRAPHY_COLLECTION
    };

   
    public static final String Type_URI = "uri";
    public static final String Type_Literal = "literal";
    public static final String Type_Timespan = "timespan";
    public static final String Type_Date = "date";
    
    static final String startingParameterName = "startingInstance";
    static final String filter_VALUES_placeHolder = "###FILTER_VALUES_STATEMENT###";
    static final String filter_STARTING_URIS_placeHolders = "###FILTER_STARTING_URIS_STATEMENT###";
}
