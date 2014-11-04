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
class SimpleDateClass {
    
    private int year;
    private int month;
    private int day;
    private boolean isCorrectlySet = false;
    private boolean isYearSet = false;
    private boolean isMonthSet = false;
    
            
    int getYear(){
        return this.year;
    }
    int getMonth(){
        return this.month;
    }
    int getDay(){
        return this.day;
    }
    
    boolean isSet(){
        return this.isCorrectlySet;
    }
    
    SimpleDateClass get1StartOfYear(){
        return new SimpleDateClass(this.year+"-"+1+"-"+1);
    }
    
    @Override
    public String toString(){
        if(this.isCorrectlySet){
            return this.year+"-"+this.month+"-"+this.day;
        }
        else{
            return "";
        }
    }
    
    boolean isBeforeOrEqual(SimpleDateClass otherDate){
        
        int otherYear = otherDate.getYear();
        int otherMonth = otherDate.getMonth();
        int otherDay = otherDate.getDay();
        
        if(this.year < otherYear){
            return true;
        }
        else if(this.year == otherYear){
            if(this.month < otherMonth){
                return true;
            }
            else if(this.month == otherMonth){
                if(this.day<=otherDay){
                    return true;
                }
            }
        }
        return false;
    }
    
    boolean isAfterOrEqual(SimpleDateClass otherDate){
        
        return otherDate.isBeforeOrEqual(this);
    }
    
    SimpleDateClass get1EndOfYear(){
        return new SimpleDateClass(this.year+"-"+12+"-"+31);
    }
    SimpleDateClass getCopy(){
        return new SimpleDateClass(this.year+"-"+this.month+"-"+this.day);
    }
    SimpleDateClass(String str){
        this.isCorrectlySet = false;
        this.isYearSet = false;
        this.isMonthSet = false;
        
        if(str.matches("[-]?[0-9]+-[0-9]{1,2}-[0-9]{1,2}")==false){
            isCorrectlySet = false;
        }
        
        isCorrectlySet = true;
        int yearMultiplier = 1;
        if(str.startsWith("-")){
            yearMultiplier = -1;
        }
        
        String[] parts = str.split("-");
        
        for(int i=0; i<parts.length; i++){
            String tmp = parts[i];
            if(tmp==null || tmp.trim().length()==0){
                continue;
            }
            tmp=tmp.trim();
            int tempInt= Integer.parseInt(tmp);
            if(isYearSet == false){
                this.year = yearMultiplier*tempInt;
                this.isYearSet = true;
                continue;
            }
            
            if(isYearSet && isMonthSet==false){
                
                if(tempInt>=1 && tempInt<=12){
                    this.month =tempInt;
                    isMonthSet = true;
                    continue;
                }
                else{
                    this.month = 1;
                    this.day = 1;
                    continue;
                }
            }
            if(isYearSet&&isMonthSet){
                int dayUpperBound =31;
                
                switch(this.month){
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                    {
                        dayUpperBound =30;
                        break;
                    }
                    case 2:{
                        if(this.year % 4 ==0){
                            dayUpperBound = 29;
                        }
                        else{
                            dayUpperBound = 28;
                        }
                    }
                    default:{
                        dayUpperBound =31;
                        break;
                    }
                }
                
                if(tempInt>=1 && tempInt<=dayUpperBound){
                    this.day = tempInt;
                    continue;
                }
                else{
                    this.day = 1;
                    continue;
                }
            }
        }
    }
}
