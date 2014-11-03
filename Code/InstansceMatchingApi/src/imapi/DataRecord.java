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
class DataRecord {

    //private String type = "";
    private String value = "";
    //private String datatype = "";
    private String lang = "";
    

    public DataRecord(String valueStr, String langStr) {

        //this.type = typeStr == null ? "" : typeStr;
        this.value = valueStr == null ? "" : valueStr;
        //this.datatype = dataTypeStr == null ? "" : dataTypeStr;
        this.lang = langStr == null ? "" : langStr;
    }

    /*public String getType() {
        return this.type;
    }
    */

    public String getLang() {
        return this.lang;
    }

    public String getValue() {
        return this.value;
    }
    
    public void replaceValues(String newVal, String newLang) {
        this.value = newVal;
        this.lang = newLang;                
    }

    
    
    

    /*
    public String getDataType() {
        return this.datatype;
    }
*/
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DataRecord)) {
            return false;
        }
        DataRecord otherBobj = (DataRecord) o;
        if (this.value.equals(otherBobj.getValue()) == false) {
            return false;
        }
        /*if (this.type.equals(otherBobj.getType()) == false) {
            return false;
        }
        if (this.datatype.equals(otherBobj.getDataType()) == false) {
            return false;
        }*/
        if (this.lang.equals(otherBobj.getLang()) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        //return (this.type + " " + this.value + " " + this.lang + " " + this.datatype).trim();
        return (this.value + " " + (this.lang.length()>0? "(lang: "+this.lang+")":"") ).trim();
    }

}
