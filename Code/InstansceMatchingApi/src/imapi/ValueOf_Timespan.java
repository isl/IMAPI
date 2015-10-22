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
    int tmpInt;
    
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
            // if does not match something like 2010-12-12 or -1200 then continue
            if(tmp.matches("[-]?[0-9]+-[0-9]{1,2}-[0-9]{1,2}")==false){
            	
                //if (tmp.matches("[-]?\\d{4}$")==true){
                if (tmp.matches("[-]?[0-9]{1,4}$")==true){
                	
                	if (tmp.startsWith("-")){
                		tmp=tmp.substring(1);
                	}
                	
                	//To give a slack -1 year for the start date
                	if (startDate==null){

                         try {
							 tmpInt=Integer.parseInt(tmp);
							tmpInt=tmpInt-1;
							tmp=Integer.toString(tmpInt);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();}
                         
                         tmp=tmp+"-01-01";
                	}
                	//To give a slack +1 year to the end date
    
                	else {
                         try {

							int tmpInt=Integer.parseInt(tmp);
							tmpInt=tmpInt+1;
							tmp=Integer.toString(tmpInt);

						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();}
                         
                         
                         tmp=tmp+"-12-31";
                	}
                	
                	
                
                     
                }
                
                
                else if(tmp.matches("[-]?[0-9]+-[0-9]{1,2}$")==true){
                	
                	if (tmp.startsWith("-")){
                		tmp=tmp.substring(1);
                	}
                	//To give a slack -1 year for the start date
                	if (startDate==null){
         
                		String tmpYear=tmp.split("-")[0];
                		String tmpMonth=tmp.split("-")[1];
                		
                		 try {
							 tmpInt=Integer.parseInt(tmpYear);
							tmpInt=tmpInt-1;
							tmp=Integer.toString(tmpInt)+"-"+tmpMonth;
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();}
                		 
                		 
                			tmp=tmp+"-01";
                	}
                		
                	//To give a slack +1 year for the end date
                	else {
                		
                		String tmpYear=tmp.split("-")[0];
                		String tmpMonth=tmp.split("-")[1];
                		
                		 try {
							 tmpInt=Integer.parseInt(tmpYear);
							tmpInt=tmpInt+1;
							tmp=Integer.toString(tmpInt)+"-"+tmpMonth;
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();}
                		 
                		 
                        if(tmp.endsWith("01")||tmp.endsWith("03")||tmp.endsWith("05")||tmp.endsWith("07")||tmp.endsWith("08")||tmp.endsWith("10")||tmp.endsWith("12")){
                            tmp=tmp+"-31";
                        }
                        else if(tmp.endsWith("04")||tmp.endsWith("06")||tmp.endsWith("09")||tmp.endsWith("11")){
                            tmp=tmp+"-30";
                        }
                        else if(tmp.endsWith("02")){
                            tmp=tmp+"-28";
                        }
                    }
                }
                
                else {
                    continue;
                }
       
            }
            else {
            	
            	if (tmp.startsWith("-")){
            		tmp=tmp.substring(1);
            	}
            	
            	//To give a slack -1 year for the start date
            	if (startDate==null){
     
            		String tmpYear=tmp.split("-")[0];
            		String tmpMonth=tmp.split("-")[1];
            		String tmpDay=tmp.split("-")[2];
            		
            		 try {
						 tmpInt=Integer.parseInt(tmpYear);
						tmpInt=tmpInt-1;
						tmp=Integer.toString(tmpInt)+"-"+tmpMonth+"-"+tmpDay;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();}
            	}
            		
            	//To give a slack +1 year for the end date
            	else {
            		
            		String tmpYear=tmp.split("-")[0];
            		String tmpMonth=tmp.split("-")[1];
            		String tmpDay=tmp.split("-")[2];
            		
            		 try {
						 tmpInt=Integer.parseInt(tmpYear);
						tmpInt=tmpInt+1;
						tmp=Integer.toString(tmpInt)+"-"+tmpMonth+"-"+tmpDay;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();}
            	}
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
        
        if(startDate==null && endDate==null){
            
            if(IMAPIClass.DEBUG){
                System.out.println("Invalid timespan - startDate: null endDate:null " + timeSpanStr);        
            }
            if(IMAPIClass.invalidTimespansDetected.contains("Invalid timespan - startDate: null endDate:null " + timeSpanStr)==false){
                IMAPIClass.invalidTimespansDetected.add("Invalid timespan - startDate: null endDate:null " + timeSpanStr);
            }
        }
        else{
        
	        if(startDate==null && endDate!=null){
	            startDate = endDate.getCopy();//.getStartOfYear();            
	        }
	        
	        if(startDate!=null && endDate==null){
	            
	            endDate = startDate.getCopy();//.getEndOfYear();  
	            
	           
	            
	        }
	        
	        if(startDate.isBeforeOrEqual(endDate)){
	            //System.out.println("OK");
	        }
	      
	        else{
                    if(IMAPIClass.DEBUG){
                        System.out.println("Invalid timespan: startDate: " + startDate.toString() + "  endDate: " + endDate.toString());
                    }
                    if(IMAPIClass.invalidTimespansDetected.contains("Invalid timespan: startDate: " + startDate.toString() + "  endDate: " + endDate.toString())==false){
                        IMAPIClass.invalidTimespansDetected.add("Invalid timespan: startDate: " + startDate.toString() + "  endDate: " + endDate.toString());
                    }
	            
	        }
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
