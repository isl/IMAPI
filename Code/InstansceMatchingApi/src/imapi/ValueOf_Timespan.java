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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 *
 * @author tzortzak
 */
class ValueOf_Timespan {
    
    SimpleDateClass startDate = null;
    SimpleDateClass endDate = null;
    
    ValueOf_Timespan(String timeSpanStr){
        if(timeSpanStr==null || timeSpanStr.trim().length()==0){
            return;
        }
        String localTimeSpanStr = timeSpanStr.trim();
        String startDateStr ="";
        String endDateStr ="";
        
        String[] parts = localTimeSpanStr.split(" ");
        
        for(int i=0; i<parts.length; i++){
            String tmp = parts[i];
            if(tmp==null || tmp.trim().length()==0){
                continue;
            }
            
            tmp=tmp.trim();
            if(tmp.matches("[-]?[0-9]+-[0-9]{1,2}-[0-9]{1,2}")==false){
                continue;
            }
            SimpleDateClass tempDate = new SimpleDateClass(tmp);
            
            if(tempDate.isSet() == false){
                continue;
            }
            
            if(i==0){
                startDate = tempDate;
            }
            else{
                endDate = tempDate;
            }
        }
        
        if(startDate==null & endDate!=null){
            startDate = endDate.getCopy();//.getStartOfYear();            
        }
        
        if(startDate!=null & endDate==null){
            
            endDate = startDate.getCopy();//.getEndOfYear();            
        }
        
        if(startDate.isBeforeOrEqual(endDate)){
            //System.out.println("OK");
        }
        else{
            System.out.println("Invalid timespan: startDate: " + startDate.toString() + "  endDate: " + endDate.toString());
        }
    }
            
    /*
    ValueOf_Timespan(String startDateStr, String endDateStr){
        startDate = parseDate(startDateStr);
        
        endDate = parseDate(endDateStr);
        
        if(startDate==null & endDate!=null){
            
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.YEAR, -1* ApiConstants.yearsJitter);
            startDate = cal.getTime();            
        }
        
        if(startDate!=null & endDate==null){
            
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.YEAR, ApiConstants.yearsJitter);
            endDate = cal.getTime();            
        }
    }
    */
    /*
    Date parseDate(String s) {

        java.util.Date returnDate = null;
        if (s == null || s.trim().length() == 0) {
            return null;
        }

        String stringToParse = s;

        if (stringToParse.matches("[-]?[0-9]{1,4}-[0-9]{1,2}-[0-9]{1,2}") == false) {
            System.out.println("Unexpected Date Format: " + stringToParse);

            return null;
        }

        try {
            java.text.SimpleDateFormat parseFormat = new java.text.SimpleDateFormat("yyyy-MM-dd G");

            if (stringToParse.startsWith("-")) {
                stringToParse = stringToParse.substring(1);
                stringToParse += " BC";
            } else {
                stringToParse += " AD";
            }

            returnDate = parseFormat.parse(stringToParse);

        } catch (ParseException e) {
            Utilities.handleException(e);
        }

        return returnDate;
    }
    */
    
}
