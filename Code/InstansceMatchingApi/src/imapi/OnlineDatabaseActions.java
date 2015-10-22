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


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.core.Var;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


class OnlineDatabaseActions {

    OnlineDatabase currentDB = null;
    private BaseComparisonClass comp = null;
    private IMAPIClass imapi = null;

    
    OnlineDatabaseActions(IMAPIClass whichImClass, OnlineDatabase whichDB) {
        this.currentDB = whichDB;
        this.imapi = whichImClass;
        this.comp = new BaseComparisonClass(whichImClass);        
    }
       
    int checkIfDBisAvailable() {
        ApiConstants.TargetSourceChoice targetChoice = this.currentDB.getDBChoice();
        System.out.println("\n=======================================\n");
        System.out.println("Checking if online database " + targetChoice.toString() + " is available.");
        String baseURL = "";
        switch (targetChoice) {
            case BRITISH_MUSEUM_COLLECTION:
            case CLAROS:
            case BM_BIOGRAPHY_COLLECTION: {

                try {

                    baseURL = this.currentDB.getDbSparqlEndpoint();
                    String USER_AGENT = "Mozilla/5.0";

                    HttpURLConnection con = (HttpURLConnection) (new URL(baseURL)).openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");
                    con.setRequestProperty("HTTP", "1.1");
                    con.setRequestProperty("User-Agent", USER_AGENT);

                    int responseCode = con.getResponseCode();

                    if (responseCode != 200) {
                        System.out.println("Seems that online database: " + targetChoice.toString() + " at url: " + baseURL + " is unavailable.");
                        System.out.println("Please try again later or change user configuration xml file to " + ApiConstants.TargetSourceChoice.FILE_COMPARISON.toString() + ".");
                        System.out.println("Response Code: " + responseCode);
                        System.out.println("ReasonPhrase returned." + con.getResponseMessage());
                        return ApiConstants.IMAPIFailCode;
                    }
                    if (IMAPIClass.DEBUG) {
                        System.out.println("responce code = 200!");
                    }

                    if (targetChoice == ApiConstants.TargetSourceChoice.CLAROS) {
                        System.out.println("\n\nWARNING: " + targetChoice.toString() + " database does not support inference.\n"
                                + "Classes and properties should be set to the exact properties used in the database.\n"
                                + "i.e. If asking for E39_Actor this database will not also qualify the E21_Person instances \n"
                                + "     as it should since E21 is subClassOf E39.\n"
                                + "     and  if asking for P1 predicate this database will not also qualify the P131 predicates \n"
                                + "     as it should since P131 is subPropertyOf P1.\n\n");
                    }

                    /*
                     HttpClient client = new DefaultHttpClient();
                     HttpPost post = new HttpPost(baseURL);
                     post.setHeader("User-Agent", USER_AGENT);
                     HttpResponse response = client.execute(post);

                     int responseCode =  response.getStatusLine().getStatusCode();//con.getResponseCode();

                     if (responseCode != 200) {
                     System.out.println("Response Code: " + responseCode);
                     System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());
                     return ApiConstants.IMAPIFailCode;
                     }*/
                } catch (IOException ex) {
                    System.out.println("Seems that online database: " + targetChoice.toString() + " at url: " + baseURL + " is unavailable.");
                    Utilities.handleException(ex);
                    //Logger.getLogger(IMAPIClass.class.getName()).log(Level.SEVERE, null, ex);
                    return ApiConstants.IMAPIFailCode;
                }

                break;
            }
            default: {
                return ApiConstants.IMAPIFailCode;
            }

        }

        System.out.println("\n=======================================\n");
        System.out.println("Online database " + targetChoice.toString() + " is available.");
        return ApiConstants.IMAPISuccessCode;
    }

    long getDatabaseInstancesCount(StringObject queryPrefixesObj, StringObject countAllInstancesQueryObj){
        long returnResult = 0;
        
        String query = queryPrefixesObj.getString()+"\n"+ countAllInstancesQueryObj.getString();
        InputStream answerStream = performDatabaseQuery(query);
        if(answerStream==null){
            return ApiConstants.IMAPIFailCode;
        }            
        
        Vector<String> tempVec = new Vector<String>();
        int ret = this.readJsonUriVals(answerStream, tempVec);

        if (ret != ApiConstants.IMAPISuccessCode) {
            return ret;
        }
        
        if(tempVec.size()==1 && tempVec.get(0).length()>0){
            returnResult = Long.parseLong(tempVec.get(0));
        }
        
        return returnResult;        
    }
    
    int getUris(String query, Vector<String> mergeVals){
        
        InputStream answerStream = performDatabaseQuery(query);
        if(answerStream==null){
            return ApiConstants.IMAPIFailCode;
        }            
        
        Vector<String> tempVec = new Vector<String>();
        int ret = this.readJsonUriVals(answerStream, tempVec);

        if (ret != ApiConstants.IMAPISuccessCode) {
            return ret;
        }
        
        for(int i=0; i<tempVec.size(); i++){
            String val = tempVec.get(i);
            if(mergeVals.contains(val)==false){
                mergeVals.add(val);
            }
        }
        
        return ApiConstants.IMAPISuccessCode;        
    }
    
    int getUriPairs(String query, String startingUriName, String valueName, String valueType, Vector<DataRecord[]> returnVals){
        
        InputStream answerStream = performDatabaseQuery(query);
        if(answerStream==null){
            return ApiConstants.IMAPIFailCode;
        } 
        boolean tryToRetrieveLang = false;
        if(valueType.equals(ApiConstants.Type_Literal)){
            tryToRetrieveLang = true;
        }
        int ret = readJsonStartingUriAndValuePairs(answerStream, startingUriName,valueName,tryToRetrieveLang,returnVals);
        if (ret != ApiConstants.IMAPISuccessCode) {
            returnVals.clear();
            return ret;
        }
        
        return ApiConstants.IMAPISuccessCode;
    }
    
    private InputStream performDatabaseQuery(String query){
        
        ApiConstants.TargetSourceChoice currentComparisonChoice = this.currentDB.getDBChoice();
        int retryQueryCounter = 0;
        int responseCode = -1;
        while(responseCode != 200 && ++retryQueryCounter<=10 ){
             try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Utilities.handleException(ex);
            }
            try{

                HttpClient client = null;
                HttpPost   post   = null;
                //String countAllInstancesQuery = queryPrefixes+"\n"+this.imapi.qWeightsConfig.getCountAllInstancesQuery();
                switch (currentComparisonChoice) {
                    case BRITISH_MUSEUM_COLLECTION: {

                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);
                        post.setHeader("User-Agent", USER_AGENT);


                        //System.out.println(query);

                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                        urlParameters.add(new BasicNameValuePair("implicit", "true"));
                        urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                        urlParameters.add(new BasicNameValuePair("_form", "/sparql"));

                        post.setEntity(new UrlEncodedFormEntity(urlParameters));
                        break;
                    }
                    case CLAROS: {

                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint();

                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);

                        post.setHeader("User-Agent", USER_AGENT);
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        urlParameters.add(new BasicNameValuePair("format", "srj"));
                        post.setEntity(new UrlEncodedFormEntity(urlParameters));
                        break;                    
                    }
                    
                    case BM_BIOGRAPHY_COLLECTION: {

                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint();

                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);

                        post.setHeader("User-Agent", USER_AGENT);
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        //urlParameters.add(new BasicNameValuePair("format", "srj"));
                        post.setEntity(new UrlEncodedFormEntity(urlParameters));
                        break;                    
                    }

                    default: {
                        System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                        return null;
                    }
                }//end of switch;

                if (client == null || post == null) {
                    continue;
                }

                HttpResponse response = client.execute(post);

                responseCode = response.getStatusLine().getStatusCode();

                if (responseCode != 200) {
                    System.out.println("Response Code: " + responseCode);
                    System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());
                }   
                else{
                    //response code was 200
                    return response.getEntity().getContent();
                }
            }
            catch(IOException ex){
                System.out.println("Exception occured while performing query:");            
                Utilities.handleException(ex);            

            }
        
            System.out.println("Trial no: "+retryQueryCounter+". Retrying query after " + ((ApiConstants.timeToWaitAfterAQueryFailure/1000)/60) +" minutes. Current Time: " + Utilities.getCurrentTime());

            try {
                Thread.sleep(ApiConstants.timeToWaitAfterAQueryFailure);
            } catch (InterruptedException ex) {
                Utilities.handleException(ex);
            }
        }
        if (responseCode != 200) {
            if(query!=null){
                System.out.println(query);
            }
        }
        
        return null;
    }
    
    private int readJsonUriVals(InputStream inputStream, Vector<String> uriVals){

        try {

            JsonReader rdr = new JsonReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            JsonElement jElement = parser.parse(rdr);

            if (jElement.isJsonObject() == false) {
                showContentsOfInputStream(inputStream);
                return ApiConstants.IMAPIFailCode;
            }

            // read head/vars from json in order to get the names of the
            // select clause in the order that they were declared.
            // Store in headVarNames vector
            Vector<String> headVarNames = new Vector<String>();

            JsonObject jRootObject = jElement.getAsJsonObject();
            JsonArray jHeadVarsArray = jRootObject.get("head")
                    .getAsJsonObject().get("vars").getAsJsonArray();

            Iterator<JsonElement> jVarsIter = jHeadVarsArray.iterator();
            while (jVarsIter.hasNext()) {
                JsonElement jVarElement = jVarsIter.next();
                if (jVarElement.isJsonPrimitive()) {
                    headVarNames.add(jVarElement.getAsString());
                }
            }

            if (jRootObject.has("results") == false
                    || jRootObject.get("results").getAsJsonObject()
                    .has("bindings") == false) {
                return ApiConstants.IMAPIFailCode;
            }

            // loop over all json bindings
            JsonArray jBindingsArray = jRootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
            Iterator<JsonElement> jBindingsIter = jBindingsArray.iterator();
            while (jBindingsIter.hasNext()) {
                
                // of the binding
                String uri = "";
                

                JsonElement jBindingElement = jBindingsIter.next();
                if (jBindingElement.isJsonObject() == false) {
                    continue;
                }
                JsonObject jBindingObject = jBindingElement.getAsJsonObject();

                for (int k = 0; k < headVarNames.size(); k++) {

                    String currentPropertyName = headVarNames.get(k);
                    if (jBindingObject.has(currentPropertyName)) {

                        JsonObject jObj = jBindingObject.get(currentPropertyName).getAsJsonObject();

                        String valStr = "";
                        if (jObj.has("value")) {
                            valStr = jObj.get("value").getAsString();
                        }

                        if (k == 0) {
                            uri = valStr;                            
                        } 
                    }
                }
                
                if(uri.length()>0 && uriVals.contains(uri)==false){
                    uriVals.add(uri);
                }

                // end of asking for all predicates of select clause head/vars names 
            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        
        
        return ApiConstants.IMAPISuccessCode;        
    }
    
    private int readJsonStartingUriAndValuePairs(InputStream inputStream, String startingUriName, String valueName, boolean checkForLang, Vector<DataRecord[]> returnVals){
        
        try {

            JsonReader rdr = new JsonReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            JsonElement jElement = parser.parse(rdr);

            if (jElement.isJsonObject() == false) {
                showContentsOfInputStream(inputStream);
                return ApiConstants.IMAPIFailCode;
            }

            // read head/vars from json in order to get the names of the
            // select clause in the order that they were declared.
            // Store in headVarNames vector
            Vector<String> headVarNames = new Vector<String>();

            JsonObject jRootObject = jElement.getAsJsonObject();
            JsonArray jHeadVarsArray = jRootObject.get("head").getAsJsonObject().get("vars").getAsJsonArray();

            Iterator<JsonElement> jVarsIter = jHeadVarsArray.iterator();
            while (jVarsIter.hasNext()) {
                JsonElement jVarElement = jVarsIter.next();
                if (jVarElement.isJsonPrimitive()) {
                    headVarNames.add(jVarElement.getAsString());
                }
            }

            if (jRootObject.has("results") == false
                    || jRootObject.get("results").getAsJsonObject()
                    .has("bindings") == false) {
                return ApiConstants.IMAPIFailCode;
            }

            // loop over all json bindings
            JsonArray jBindingsArray = jRootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
            Iterator<JsonElement> jBindingsIter = jBindingsArray.iterator();
            while (jBindingsIter.hasNext()) {
                
                // of the binding
                DataRecord start = null;
                DataRecord end = null;
                
                
                JsonElement jBindingElement = jBindingsIter.next();
                if (jBindingElement.isJsonObject() == false) {
                    continue;
                }
                JsonObject jBindingObject = jBindingElement.getAsJsonObject();

                for (int k = 0; k < headVarNames.size(); k++) {

                    String currentPropertyName = headVarNames.get(k);
                    if (jBindingObject.has(currentPropertyName)) {

                        JsonObject jObj = jBindingObject.get(currentPropertyName).getAsJsonObject();

                        String valStr = "";
                        
                        String langStr = "";
                        if (jObj.has("value")) {
                            valStr = jObj.get("value").getAsString();                            
                        }
                        
                        if(checkForLang){
                            //read lang if type is literal
                            if (jObj.has("xml:lang")) {
                                langStr = jObj.get("xml:lang").getAsString();
                            }
                        }

                        if(valStr==null || valStr.trim().length()==0){
                            continue;
                        }
                        
                        if(currentPropertyName.equals(startingUriName)){                            
                            start = new DataRecord(valStr,"");
                        }
                        
                        if(currentPropertyName.equals(valueName)){
                            end = new DataRecord(valStr,langStr);
                        }       
                    }
                }
                if(start!=null && end !=null){
                    DataRecord[] newVal = {start,end};
                    returnVals.add(newVal);
                }

                // end of asking for all predicates of select clause head/vars names 
            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        
        return ApiConstants.IMAPISuccessCode;
    }
    
    private void showContentsOfInputStream(InputStream stream) {

        char[] buffer = new char[1024];
        StringBuilder out = new StringBuilder();
        try {
            final Reader in = new InputStreamReader(stream, "UTF-8");
            try {
                for (;;) {
                    int rsz = in.read(buffer, 0, buffer.length);
                    if (rsz < 0) {
                        break;
                    }
                    out.append(buffer, 0, rsz);
                }
            } finally {
                in.close();
            }
        } catch (UnsupportedEncodingException ex) {
            Utilities.handleException(ex);
        } catch (IOException ex) {
            Utilities.handleException(ex);
        }
        System.out.println(out.toString());

    }

    
    //<editor-fold defaultstate="collapsed" desc="Abandoned not necessarily working code">
    /*
    int DELETE_findOutUrisNotExistingIntheDatabase(Vector<String> urisToCheck, Vector<String> notExistingUris) throws IOException{
        notExistingUris.clear();
        
        Vector<String> existingVals = new Vector<String>();
        int ret = DELETE_findOutUrisExistingIntheDatabase(urisToCheck,existingVals);
        if(ret!=ApiConstants.IMAPISuccessCode){
            return ret;
        }
        
        notExistingUris.addAll(urisToCheck);
        notExistingUris.removeAll(existingVals);                        
        
        return ApiConstants.IMAPISuccessCode;
    }
    
    
    private int DELETE_findOutUrisExistingIntheDatabase(Vector<String> urisToCheck, Vector<String> existingVals) throws UnsupportedEncodingException, IOException{
        existingVals.clear();
        
        String baseQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                           "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+ "\n" +
                           "SELECT DISTINCT ?x \n" +
                           "{\n" +
                           "  \n" +
                           "  ?x a ?class .\n"+
                           ApiConstants.filter_STARTING_URIS_placeHolders+" \n" +
                           "}\n"+
                           "ORDER BY ?x";
        

        int LIMIT = this.imapi.qWeightsConfig.getQueryLimitSize();
        int currentCounter = 0;
        int maxFilterCounter = urisToCheck.size();

        while (currentCounter < maxFilterCounter) {

            Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter, ApiConstants.instanceUrisFilterStepCount, urisToCheck);
            currentCounter += ApiConstants.instanceUrisFilterStepCount;

            if (IMAPIClass.ExtentedMessagesEnabled) {
                System.out.println("Checking values: " + currentSubset.toString());
            }
          
            int OFFSET = -LIMIT;
            IntegerObject resultsReturned = new IntegerObject(LIMIT);

            while (resultsReturned.getValue() == LIMIT) {
                
                
                //String filteringExpression = "  FILTER ( ";        
                //for(int m=0; m<currentSubset.size();m++){

                  //  if(m>0){
                    //    filteringExpression+=" || ";                
                    //}
                    //filteringExpression+="?x = <" +currentSubset.get(m)+">";
                //}
                //filteringExpression+=") \n";
                //baseQuery.replace(ApiConstants.filterInstancesUriPlaceHolder, filteringExpression);
        
                String query = QueryBuilder.replaceFilteringPlaceHolders(baseQuery, ApiConstants.filter_STARTING_URIS_placeHolders, "x", currentSubset);
                
                

                OFFSET += LIMIT;
                query += "\n LIMIT " + LIMIT;
                if (OFFSET > 0) {
                    query += "\n OFFSET " + OFFSET;
                }

                HttpClient client = null;
                HttpPost post = null;

                switch (this.currentDB.getDBChoice()) {
                    case BRITISH_MUSEUM_COLLECTION: {

                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);
                        post.setHeader("User-Agent", USER_AGENT);

                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                        urlParameters.add(new BasicNameValuePair("implicit", "true"));
                        urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                        urlParameters.add(new BasicNameValuePair("_form", "/sparql"));

                        post.setEntity(new UrlEncodedFormEntity(urlParameters));

                        break;
                    }

                    case CLAROS: {

                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint();

                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);

                        post.setHeader("User-Agent", USER_AGENT);
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        urlParameters.add(new BasicNameValuePair("format", "srj"));
                        post.setEntity(new UrlEncodedFormEntity(urlParameters));

                        break;
                    }
                    default: {
                        System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                        return ApiConstants.IMAPIFailCode;
                    }
                }//end of switch;

                if (client == null || post == null) {
                    return ApiConstants.IMAPIFailCode;
                }

                HttpResponse response = client.execute(post);

                int responseCode = response.getStatusLine().getStatusCode();//con.getResponseCode();

                if (responseCode != 200) {
                    System.out.println("Response Code: " + responseCode);
                    System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());
                    return ApiConstants.IMAPIFailCode;
                }
                
                int ret = this.readJsonUriVals(response.getEntity().getContent(), existingVals);

                if (ret != ApiConstants.IMAPISuccessCode) {
                    return ret;
                }

                resultsReturned.setValue(existingVals.size());
            }//end of while that performs the limit / offset mechanism
            
        }//end of filter uri step counter
        

        return ApiConstants.IMAPISuccessCode;
        
    }
    
    
    private int DELETE_readJSONtoGetAllInstancesFrom_OWLIM_DB(InputStream inputStream,
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo,
            Hashtable<Long,String> collectAllIdsAndUris,            
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences){
        
        //int cnt = 0;
        //resultsReturned.setValue(cnt);

        // Results Found 207771. Finished after: 74 seconds.
        try {
            //showContentsOfInputStream(inputStream);
            JsonReader rdr = new JsonReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            JsonElement jElement = parser.parse(rdr);

            if (jElement.isJsonObject() == false) {
                //showContentsOfInputStream(inputStream);
                return ApiConstants.IMAPIFailCode;
            }

            // read head/vars from json in order to get the names of the
            // select clause in the order that they were declared.
            // Store in headVarNames vector
            Vector<String> headVarNames = new Vector<String>();

            JsonObject jRootObject = jElement.getAsJsonObject();
            JsonArray jHeadVarsArray = jRootObject.get("head")
                    .getAsJsonObject().get("vars").getAsJsonArray();

            Iterator<JsonElement> jVarsIter = jHeadVarsArray.iterator();
            while (jVarsIter.hasNext()) {
                JsonElement jVarElement = jVarsIter.next();
                if (jVarElement.isJsonPrimitive()) {
                    headVarNames.add(jVarElement.getAsString());
                }
            }

            if (jRootObject.has("results") == false
                    || jRootObject.get("results").getAsJsonObject()
                    .has("bindings") == false) {
                return ApiConstants.IMAPIFailCode;
            }

            // loop over all json bindings
            JsonArray jBindingsArray = jRootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
            Iterator<JsonElement> jBindingsIter = jBindingsArray.iterator();
            while (jBindingsIter.hasNext()) {
                //cnt++;
                //resultsReturned.setValue(cnt);

                long id = -1;
                String uri = "";

                JsonElement jBindingElement = jBindingsIter.next();
                if (jBindingElement.isJsonObject() == false) {
                    continue;
                }
                JsonObject jBindingObject = jBindingElement.getAsJsonObject();

                // ask one by one the valid attributes of the select clause
                // the first will always contain our key
                for (int k = 0; k < headVarNames.size(); k++) {

                    String currentPropertyName = headVarNames.get(k);
                    if (jBindingObject.has(currentPropertyName)) {

                        JsonObject jObj = jBindingObject.get(currentPropertyName).getAsJsonObject();
                        String valStr = "";
                        if (jObj.has("value")) {
                            valStr = jObj.get("value").getAsString();
                        }

                        if (k == 0) {
                            String idStr = valStr;
                            id = Long.parseLong(idStr);
                            
                        }
                        else{
                            uri = valStr;
                        }
                    }
                }

                // end of asking for all predicates of select clause head/vars names 
                if (id > 0 && uri.length() > 0) {

                    SourceInstancePair currentVal = new SourceInstancePair(this.currentDB.getDBChoice().toString(), uri);

                    //just put it in target vector for comparison
                    if (collectAllIdsAndUris.containsKey(id) == false) {
                        collectAllIdsAndUris.put(id,uri);
                    }

                    Enumeration<SourceInstancePair> sourceEnum = inputSourceInfo.keys();
                    while (sourceEnum.hasMoreElements()) {
                        SourceInstancePair pair = sourceEnum.nextElement();
                        if (pair.getInstanceUri().equals(uri)) {

                            SourceTargetPair newSTPair = new SourceTargetPair(pair, currentVal);
                            SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();
                            if (pairSimilaritiesInSequences.containsKey(newSTPair) == false) {
                                pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                            }
                        }
                    }
                }

            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        //resultsReturned.setValue(cnt);
        
        return ApiConstants.IMAPISuccessCode;
    }
    
    private int DELETE_readJSONtoGetAllInstances(InputStream inputStream,
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo,
            Vector<String> collectAllUris,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences,
            IntegerObject resultsReturned) {
        int cnt = 0;
        resultsReturned.setValue(cnt);

        // Results Found 207771. Finished after: 74 seconds.
        try {
            //showContentsOfInputStream(inputStream);
            JsonReader rdr = new JsonReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            JsonElement jElement = parser.parse(rdr);

            if (jElement.isJsonObject() == false) {
                //showContentsOfInputStream(inputStream);
                return ApiConstants.IMAPIFailCode;
            }

            // read head/vars from json in order to get the names of the
            // select clause in the order that they were declared.
            // Store in headVarNames vector
            Vector<String> headVarNames = new Vector<String>();

            JsonObject jRootObject = jElement.getAsJsonObject();
            JsonArray jHeadVarsArray = jRootObject.get("head")
                    .getAsJsonObject().get("vars").getAsJsonArray();

            Iterator<JsonElement> jVarsIter = jHeadVarsArray.iterator();
            while (jVarsIter.hasNext()) {
                JsonElement jVarElement = jVarsIter.next();
                if (jVarElement.isJsonPrimitive()) {
                    headVarNames.add(jVarElement.getAsString());
                }
            }

            if (jRootObject.has("results") == false
                    || jRootObject.get("results").getAsJsonObject()
                    .has("bindings") == false) {
                return ApiConstants.IMAPIFailCode;
            }

            // loop over all json bindings
            JsonArray jBindingsArray = jRootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
            Iterator<JsonElement> jBindingsIter = jBindingsArray.iterator();
            while (jBindingsIter.hasNext()) {
                cnt++;
                resultsReturned.setValue(cnt);

                String uri = "";

                JsonElement jBindingElement = jBindingsIter.next();
                if (jBindingElement.isJsonObject() == false) {
                    continue;
                }
                JsonObject jBindingObject = jBindingElement.getAsJsonObject();

                // ask one by one the valid attributes of the select clause
                // the first will always contain our key
                for (int k = 0; k < headVarNames.size(); k++) {

                    String currentPropertyName = headVarNames.get(k);
                    if (jBindingObject.has(currentPropertyName)) {

                        JsonObject jObj = jBindingObject.get(currentPropertyName).getAsJsonObject();
                        String valStr = "";
                        if (jObj.has("value")) {
                            valStr = jObj.get("value").getAsString();
                        }

                        if (k == 0) {
                            uri = valStr;
                        }
                    }
                }

                // end of asking for all predicates of select clause head/vars names 
                if (uri.length() > 0) {

                    SourceInstancePair currentVal = new SourceInstancePair(this.currentDB.getDBChoice().toString(), uri);

                    //just put it in target vector for comparison
                    if (collectAllUris.contains(uri) == false) {
                        collectAllUris.add(uri);
                    }

                    Enumeration<SourceInstancePair> sourceEnum = inputSourceInfo.keys();
                    while (sourceEnum.hasMoreElements()) {
                        SourceInstancePair pair = sourceEnum.nextElement();
                        if (pair.getInstanceUri().equals(uri)) {

                            SourceTargetPair newSTPair = new SourceTargetPair(pair, currentVal);
                            SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();
                            if (pairSimilaritiesInSequences.containsKey(newSTPair) == false) {
                                pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                            }
                        }
                    }
                }

            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        resultsReturned.setValue(cnt);
        return ApiConstants.IMAPISuccessCode;

    }

    private int DELETE_readJSONToGetAllSimilarInstances(InputStream inputStream,
            SourceDataHolder inputSourceInfo,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences,
            IntegerObject resultsReturned) {

        int cnt = 0;
        resultsReturned.setValue(cnt);

        // Results Found 207771. Finished after: 74 seconds.
        try {
            //showContentsOfInputStream(inputStream);
            JsonReader rdr = new JsonReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            JsonElement jElement = parser.parse(rdr);

            if (jElement.isJsonObject() == false) {
                //showContentsOfInputStream(inputStream);
                return ApiConstants.IMAPIFailCode;
            }

            // read head/vars from json in order to get the names of the
            // select clause in the order that they were declared.
            // Store in headVarNames vector
            Vector<String> headVarNames = new Vector<String>();

            JsonObject jRootObject = jElement.getAsJsonObject();
            JsonArray jHeadVarsArray = jRootObject.get("head")
                    .getAsJsonObject().get("vars").getAsJsonArray();

            Iterator<JsonElement> jVarsIter = jHeadVarsArray.iterator();
            while (jVarsIter.hasNext()) {
                JsonElement jVarElement = jVarsIter.next();
                if (jVarElement.isJsonPrimitive()) {
                    headVarNames.add(jVarElement.getAsString());
                }
            }

            if (jRootObject.has("results") == false
                    || jRootObject.get("results").getAsJsonObject()
                    .has("bindings") == false) {
                return ApiConstants.IMAPIFailCode;
            }

            // loop over all json bindings
            JsonArray jBindingsArray = jRootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
            Iterator<JsonElement> jBindingsIter = jBindingsArray.iterator();
            while (jBindingsIter.hasNext()) {
                cnt++;
                resultsReturned.setValue(cnt);

                String uri = "";

                JsonElement jBindingElement = jBindingsIter.next();
                if (jBindingElement.isJsonObject() == false) {
                    continue;
                }
                JsonObject jBindingObject = jBindingElement.getAsJsonObject();

                // ask one by one the valid attributes of the select clause
                // the first will always contain our key
                for (int k = 0; k < headVarNames.size(); k++) {

                    String currentPropertyName = headVarNames.get(k);
                    if (jBindingObject.has(currentPropertyName)) {

                        JsonObject jObj = jBindingObject.get(currentPropertyName).getAsJsonObject();
                        String valStr = "";
                        if (jObj.has("value")) {
                            valStr = jObj.get("value").getAsString();
                        }

                        if (k == 0) {
                            uri = valStr;
                        }
                    }
                }

                // end of asking for all predicates of select clause head/vars names 
                if (uri.length() > 0) {

                    SourceInstancePair currentVal = new SourceInstancePair(this.currentDB.getDBChoice().toString(), uri);
                    
                    Enumeration<String> filesEnum = inputSourceInfo.keys();
                    while(filesEnum.hasMoreElements()){
                        String file = filesEnum.nextElement();
                        
                        if(inputSourceInfo.get(file).containsKey(uri)){
                            SourceInstancePair pair = new SourceInstancePair(file, uri);
                            SourceTargetPair newSTPair = new SourceTargetPair(pair, currentVal);
                            
                            SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();
                            if (pairSimilaritiesInSequences.containsKey(newSTPair) == false) {
                                pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                            }

                        
                        }
                    }                    
                    
                }

            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        resultsReturned.setValue(cnt);
        return ApiConstants.IMAPISuccessCode;

    }

    
    private int DELETE_readJSON(InputStream inputStream,
            UserQueryConfiguration currentSequence,
            SourceDataHolder inputSourceInfo,            
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences,
            IntegerObject resultsReturned) {

        int currentSequencePosition = currentSequence.getPositionID();
        Hashtable<String, String> parameters = currentSequence.getAllQueryStepParameters();

        int cnt = 0;
        resultsReturned.setValue(cnt);

        try {

            JsonReader rdr = new JsonReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            JsonElement jElement = parser.parse(rdr);

            if (jElement.isJsonObject() == false) {
                //showContentsOfInputStream(inputStream);
                return ApiConstants.IMAPIFailCode;
            }

            // read head/vars from json in order to get the names of the
            // select clause in the order that they were declared.
            // Store in headVarNames vector
            Vector<String> headVarNames = new Vector<String>();

            JsonObject jRootObject = jElement.getAsJsonObject();
            JsonArray jHeadVarsArray = jRootObject.get("head")
                    .getAsJsonObject().get("vars").getAsJsonArray();

            Iterator<JsonElement> jVarsIter = jHeadVarsArray.iterator();
            while (jVarsIter.hasNext()) {
                JsonElement jVarElement = jVarsIter.next();
                if (jVarElement.isJsonPrimitive()) {
                    headVarNames.add(jVarElement.getAsString());
                }
            }

            if (jRootObject.has("results") == false
                    || jRootObject.get("results").getAsJsonObject()
                    .has("bindings") == false) {
                return ApiConstants.IMAPIFailCode;
            }

            // loop over all json bindings
            JsonArray jBindingsArray = jRootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
            Iterator<JsonElement> jBindingsIter = jBindingsArray.iterator();
            while (jBindingsIter.hasNext()) {
                cnt++;
                resultsReturned.setValue(cnt);
                //System.out.println(cnt);
                // a json binding will give as a uri - the first from the select
                // clause
                // and a SequenceInfoHashtable that will contain the rest of the
                // properties
                // of the binding
                String uri = "";
                //SequenceInfoHashtable bTable = new SequenceInfoHashtable();
                //boolean isFilteredOut = false;

                JsonElement jBindingElement = jBindingsIter.next();
                if (jBindingElement.isJsonObject() == false) {
                    continue;
                }
                JsonObject jBindingObject = jBindingElement.getAsJsonObject();

                // ask one by one the valid attributes of the select clause
                // the first will always contain our key
                SourceInstancePair searchVal = null;
                for (int k = 0; k < headVarNames.size(); k++) {

                    String currentPropertyName = headVarNames.get(k);
                    //System.out.println(currentPropertyName);
                    if (jBindingObject.has(currentPropertyName)) {

                        JsonObject jObj = jBindingObject.get(currentPropertyName).getAsJsonObject();

                        String valStr = "";
                        if (jObj.has("value")) {
                            valStr = jObj.get("value").getAsString();
                        }

                        if (k == 0) {
                            uri = valStr;
                            if (uri.length() > 0) {
                                searchVal = new SourceInstancePair(this.currentDB.getDBChoice().toString(), uri);

                            }
                        } else {

                            String langStr = "";

                            if (jObj.has("xml:lang")) {
                                langStr = jObj.get("xml:lang").getAsString();
                            }

                            if (searchVal == null) {
                                continue;
                            }

                            DataRecord newRecord = new DataRecord(valStr, langStr);
                            //System.out.println(uri + "\t"+newRecord.toString());
                            //here we should see if we have to store the value found --> make the comparison

                            Enumeration<String> filesEnum = inputSourceInfo.keys();
                            while(filesEnum.hasMoreElements()){
                                String file = filesEnum.nextElement();

                                Enumeration<String> uriEnum = inputSourceInfo.get(file).keys();
                                while(uriEnum.hasMoreElements()){
                                    String srcuri = uriEnum.nextElement();
                                    SequencesVector pairdata = inputSourceInfo.get(file).get(srcuri);
                                    

                                    SequenceData seqData = pairdata.getSequenceDataAtPosition(currentSequencePosition);
                                    if (seqData == null) {
                                        continue;
                                    }

                                    Vector<DataRecord> sourceVals = seqData.getValuesOfKey(currentPropertyName);
                                    if (sourceVals == null || sourceVals.size() == 0) {
                                        continue;
                                    }

                                    String paramType = parameters.get(currentPropertyName);
                                    DataRecord srcCompareRecord = new DataRecord("", "");

                                    Double tempComparisonResult = this.comp.compareValueAgainstVector(paramType, newRecord, sourceVals, srcCompareRecord);
                                    if (tempComparisonResult > 0d) {

                                        SourceInstancePair pair = new SourceInstancePair(file,srcuri);
                                        SourceTargetPair newSTPair = new SourceTargetPair(pair, searchVal);
                                        //int currentSequencePosition = currentSequence.getSequencePosition();
                                        SequenceSimilarityResult newSimResult = new SequenceSimilarityResult(currentSequencePosition, seqData.getSchemaInfo().getMnemonic(), seqData.getSchemaInfo().getWeight());
                                        newSimResult.setNewSimilarityResult(currentPropertyName, paramType, srcCompareRecord, newRecord, tempComparisonResult);

                                        //SimilarityTriplet newSimTriplet = new SimilarityTriplet(currentSequencePosition, currentStepPosition, tempComparisonResult);
                                        if (pairSimilaritiesInSequences.containsKey(newSTPair)) {

                                            pairSimilaritiesInSequences.get(newSTPair).addSequenceSimilarityResult(newSimResult);

                                        } else {
                                            SequenceSimilarityResultVector newSeqSimilarity = new SequenceSimilarityResultVector();
                                            newSeqSimilarity.add(newSimResult);
                                            pairSimilaritiesInSequences.put(newSTPair, newSeqSimilarity);
                                        }
                                    }
                                }
                                }

                        }
                    }
                }

                // end of asking for all predicates of select clause head/vars names 
            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        resultsReturned.setValue(cnt);
        return ApiConstants.IMAPISuccessCode;

    }
    
    
     public int DELETE_retrieveDataOfSpecificSequence(String queryPrefixes,
            UserQueryConfiguration currentQuerySequence,
            SourceDataHolder inputSourceInfo,            
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) throws IOException {

        int currentSequencePosition = currentQuerySequence.getPositionID();
        System.out.println("\n\nQuering Sequence " + (currentSequencePosition + 1) + " --> mnemonic: " + currentQuerySequence.getMnemonic()+" at time "+java.util.Calendar.getInstance().getTime());

        String[] stepParameterNames = currentQuerySequence.getSortedParameterNamesCopy();
        String[] stepParameterTypes = currentQuerySequence.getSortedParameterTypesCopy();
        String[] stepParameterQueries = currentQuerySequence.getSortedQueriesCopy();

        //performing all steps in a llop
        for (int k = 0; k < stepParameterQueries.length; k++) {

            System.out.println("In step " + (k + 1) + " out of " + stepParameterQueries.length+" at time "+java.util.Calendar.getInstance().getTime());
            int LIMIT = this.imapi.qWeightsConfig.getQueryLimitSize();
            String currentStepParameterName = stepParameterNames[k];
            String parameterType = stepParameterTypes[k].toLowerCase();

            Vector<String> allValuesFound = new Vector<String>();

            //filtering case
            if (parameterType.equals(ApiConstants.Type_URI)) {
                allValuesFound = inputSourceInfo.collectAllValuesOfSpecificParameter(currentSequencePosition, currentStepParameterName);
                if (allValuesFound.size() == 0) {
                    System.out.println("No source values found skiping this query step to the database.");
                    continue;
                }
            } else {
                if (inputSourceInfo.containsValuesOfSpecificParameter(currentSequencePosition, currentStepParameterName) == false) {
                    System.out.println("No source values found skiping this query step to the database.");
                    continue;
                }
            }

            int currentCounter = 0;
            int maxFilterCounter = allValuesFound.size();

            if (parameterType.equals(ApiConstants.Type_URI) == false) {
                maxFilterCounter = 1;
            }

            while (currentCounter < maxFilterCounter) {

                Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter, ApiConstants.valueUrisFilterStepCount, allValuesFound);

                //with this statement i make sure that if no filtering is applied 
                //then only once the queriesa will be executed
                if (parameterType.equals(ApiConstants.Type_URI)) {
                    currentCounter += ApiConstants.valueUrisFilterStepCount;
                } else {
                    currentCounter = maxFilterCounter;
                }

                int OFFSET = -LIMIT;
                IntegerObject resultsReturned = new IntegerObject(LIMIT);

                //query =  query;
                while (resultsReturned.getValue() == LIMIT) {
                    String query = queryPrefixes + stepParameterQueries[k];
                    //int currentStepPosition = qStep.getStepPositionInSequence();

                    query = QueryBuilder.replaceFilteringPlaceHolders(query, ApiConstants.filter_VALUES_placeHolder, currentStepParameterName, currentSubset);

                    OFFSET += LIMIT;
                    query += "\n LIMIT " + LIMIT;
                    if (OFFSET > 0) {
                        query += "\n OFFSET " + OFFSET;
                    }

                    HttpClient client = null;
                    HttpPost post = null;
                    if (OFFSET == 0) {
                        if (IMAPIClass.ExtentedMessagesEnabled) {
                            System.out.println("\n\nPerforming the following query:\n=============================================\n" + query + "\n");
                        }
                    } else {
                        System.out.println("Quering with offset: " + OFFSET+" at time "+java.util.Calendar.getInstance().getTime());
                    }
                    switch (this.currentDB.getDBChoice()) {
                        case BRITISH_MUSEUM_COLLECTION: {

                            String USER_AGENT = "Mozilla/5.0";
                            String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                            client = new DefaultHttpClient();
                            post = new HttpPost(baseURL);
                            post.setHeader("User-Agent", USER_AGENT);

                            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                            urlParameters.add(new BasicNameValuePair("query", query));
                            urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                            urlParameters.add(new BasicNameValuePair("implicit", "true"));
                            urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                            urlParameters.add(new BasicNameValuePair("_form", "/sparql"));

                            post.setEntity(new UrlEncodedFormEntity(urlParameters));

                            break;
                        }

                        case CLAROS: {

                            String USER_AGENT = "Mozilla/5.0";
                            String baseURL = this.currentDB.getDbSparqlEndpoint();

                            client = new DefaultHttpClient();
                            post = new HttpPost(baseURL);

                            post.setHeader("User-Agent", USER_AGENT);
                            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                            urlParameters.add(new BasicNameValuePair("query", query));
                            //urlParameters.add(new BasicNameValuePair("default-graph-uri", ""));
                            //urlParameters.add(new BasicNameValuePair("exampleQueries=", ""));
                            //urlParameters.add(new BasicNameValuePair("common_prefixes", "on"));
                            urlParameters.add(new BasicNameValuePair("format", "srj"));
                            //urlParameters.add(new BasicNameValuePair("timeout", "0"));
                            //urlParameters.add(new BasicNameValuePair("debug", "on"));                    
                            post.setEntity(new UrlEncodedFormEntity(urlParameters));

                            break;
                        }
                        default: {
                            System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                            return ApiConstants.IMAPIFailCode;
                        }
                    }//end of switch;

                    if (client == null || post == null) {
                        return ApiConstants.IMAPIFailCode;
                    }

                    HttpResponse response = client.execute(post);

                    int responseCode = response.getStatusLine().getStatusCode();//con.getResponseCode();

                    if (responseCode != 200) {
                        System.out.println("Response Code: " + responseCode);
                        System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());
                        return ApiConstants.IMAPIFailCode;
                    }
                    if (IMAPIClass.ExtentedMessagesEnabled) {
                        System.out.println("responce code = 200!");
                    }
                    int ret = this.DELETE_readJSON(response.getEntity().getContent(), currentQuerySequence, inputSourceInfo, pairSimilaritiesInSequences, resultsReturned);
                    if (ret != ApiConstants.IMAPISuccessCode) {
                        return ret;
                    }

                }//end of limit - offset while loop
            }//end of filtering values while loop
        }//end of query steps

        return ApiConstants.IMAPISuccessCode;
    }
     
    int DELETE_retrieveSimilaritiesTo_Owlim_DB(String queryPrefixes,
            String instancesQuery, String countAllInstancesQuery,
            Vector<UserQueryConfiguration> qSequences,
            SourceDataHolder inputSourceInfo,
            Vector<String> urisThatDoNotExistInTheDatabase,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences){
        
        ApiConstants.TargetSourceChoice currentComparisonChoice = this.currentDB.getDBChoice();
        
        Utilities u = new Utilities(this.imapi);
        
        
        Vector<Boolean>  canQuickFilteringMethodBeFollowedForeachSequence = new Vector<Boolean>();
        boolean allSequencesFast = u.canAllSequencesFollowFastApproach(qSequences, inputSourceInfo, canQuickFilteringMethodBeFollowedForeachSequence);
        
        
        if(allSequencesFast){
            
            
            //return ApiConstants.IMAPISuccessCode;
        }
        
        long totalInstancesCount =0;
        
        
        try{
            
            HttpClient countInstancesClient = null;
            HttpPost   countInstancesPost   = null;
            //String countAllInstancesQuery = queryPrefixes+"\n"+this.imapi.qWeightsConfig.getCountAllInstancesQuery();
            switch (currentComparisonChoice) {
                case BRITISH_MUSEUM_COLLECTION: {
                    
                    String USER_AGENT = "Mozilla/5.0";
                    String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                    countInstancesClient = new DefaultHttpClient();
                    countInstancesPost = new HttpPost(baseURL);
                    countInstancesPost.setHeader("User-Agent", USER_AGENT);
                    
                    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                    urlParameters.add(new BasicNameValuePair("query", queryPrefixes+"\n"+ countAllInstancesQuery));
                    urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                    urlParameters.add(new BasicNameValuePair("implicit", "true"));
                    urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                    urlParameters.add(new BasicNameValuePair("_form", "/sparql"));
                    
                    countInstancesPost.setEntity(new UrlEncodedFormEntity(urlParameters));
                    break;
                }
                
                default: {
                    System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                    return ApiConstants.IMAPIFailCode;
                }
            }//end of switch;
            
            if (countInstancesClient == null || countInstancesPost == null) {
                return ApiConstants.IMAPIFailCode;
            }
            
            HttpResponse countInstancesResponse = countInstancesClient.execute(countInstancesPost);
            
            int countInstancesResponseCode = countInstancesResponse.getStatusLine().getStatusCode();
            
            if (countInstancesResponseCode != 200) {
                System.out.println("Response Code: " + countInstancesResponseCode);
                System.out.println("ReasonPhrase returned." + countInstancesResponse.getStatusLine().getReasonPhrase());
                
                System.out.println("Retrying query after " + ((ApiConstants.timeToWaitAfterAQueryFailure/1000)/60) +" minutes. Current Time: " + Utilities.getCurrentTime());
                
                try {
                    Thread.sleep(ApiConstants.timeToWaitAfterAQueryFailure);
                } catch (InterruptedException ex) {
                    Utilities.handleException(ex);
                }
                
                HttpClient client2 = null;
                HttpPost post2 = null;
                switch (currentComparisonChoice) {
                    case BRITISH_MUSEUM_COLLECTION: {
                        
                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                        client2 = new DefaultHttpClient();
                        post2 = new HttpPost(baseURL);
                        post2.setHeader("User-Agent", USER_AGENT);
                        
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", queryPrefixes+"\n"+ countAllInstancesQuery));
                        urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                        urlParameters.add(new BasicNameValuePair("implicit", "true"));
                        urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                        urlParameters.add(new BasicNameValuePair("_form", "/sparql"));
                        
                        post2.setEntity(new UrlEncodedFormEntity(urlParameters));
                        break;
                    }
                    
                    default: {
                        System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                        return ApiConstants.IMAPIFailCode;
                    }
                }//end of switch;
                
                
                
                HttpResponse response2 = client2.execute(post2);
                
                int responseCode2 = response2.getStatusLine().getStatusCode();//con.getResponseCode();
                if(responseCode2!=200){
                    System.out.println("Response Code: " + responseCode2);
                    System.out.println("ReasonPhrase returned." + response2.getStatusLine().getReasonPhrase());
                    return ApiConstants.IMAPIFailCode;
                }
                
            }//case of failure
            
            
            Vector<String> tempVec = new Vector<String>();
            int ret = this.readJsonUriVals(countInstancesResponse.getEntity().getContent(), tempVec);
            
            if (ret != ApiConstants.IMAPISuccessCode) {
                return ret;
            }
            if(tempVec.size()==1 && tempVec.get(0).length()>0){
                totalInstancesCount=Long.parseLong(tempVec.get(0));
            }
            
        }
        catch(IOException ex){
            
            Utilities.handleException(ex);
            
            return ApiConstants.IMAPIFailCode;
        }

        
        System.out.println("\nAll instances of the database must be checked. Total Number of instances: " + totalInstancesCount+"\n");
        
        //in this case get all instances regardless of filtering matches
        //get them in a limit offset loop, and consume each set by asking
        //all sequences for each set of instances. For each sequence ask if the
        //fast method can be followed and
        //then for each sequence
        System.out.println("\nStarting queries in loops of "+ this.imapi.qWeightsConfig.getQueryLimitSize() +" instances per loop. Time: "+Utilities.getCurrentTime()+"\n");
        
        int overallInstancesProgressCounter = 0;
        int OVERALL_INSTANCES_LIMIT = this.imapi.qWeightsConfig.getQueryLimitSize();
        int OVERALL_INSTANCES_OFFSET = -OVERALL_INSTANCES_LIMIT;
        IntegerObject instanceResultsReturned = new IntegerObject(OVERALL_INSTANCES_LIMIT);
        
        try{
            while (instanceResultsReturned.getValue() == OVERALL_INSTANCES_LIMIT) {
                String overall_instances_query = queryPrefixes+"\n"+ instancesQuery;
                
                OVERALL_INSTANCES_OFFSET += OVERALL_INSTANCES_LIMIT;
                Vector<String> loopData = new Vector<String>();
                
                overall_instances_query += "\n LIMIT " + OVERALL_INSTANCES_LIMIT;
                if (OVERALL_INSTANCES_OFFSET > 0) {
                    overall_instances_query += "\n OFFSET " + OVERALL_INSTANCES_OFFSET;
                }
                
                
                HttpClient instancesClient = null;
                HttpPost instancesPost = null;
                
                
                switch (currentComparisonChoice) {
                    case BRITISH_MUSEUM_COLLECTION: {
                        
                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                        instancesClient = new DefaultHttpClient();
                        instancesPost = new HttpPost(baseURL);
                        instancesPost.setHeader("User-Agent", USER_AGENT);
                        
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", overall_instances_query));
                        urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                        urlParameters.add(new BasicNameValuePair("implicit", "true"));
                        urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                        urlParameters.add(new BasicNameValuePair("_form", "/sparql"));
                        
                        instancesPost.setEntity(new UrlEncodedFormEntity(urlParameters));
                        break;
                    }
                    
                    default: {
                        System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                        return ApiConstants.IMAPIFailCode;
                    }
                }//end of switch;
                
                if (instancesClient == null || instancesPost == null) {
                    return ApiConstants.IMAPIFailCode;
                }
                
                HttpResponse instancesResponse = instancesClient.execute(instancesPost);
                
                int instancesResponseCode = instancesResponse.getStatusLine().getStatusCode();
                
                if (instancesResponseCode != 200) {
                    System.out.println("Response Code: " + instancesResponseCode);
                    System.out.println("ReasonPhrase returned." + instancesResponse.getStatusLine().getReasonPhrase());
                    
                    System.out.println("Retrying query after " + ((ApiConstants.timeToWaitAfterAQueryFailure/1000)/60) +" minutes. Current Time: " + Utilities.getCurrentTime());
                    
                    
                    try {
                        Thread.sleep(ApiConstants.timeToWaitAfterAQueryFailure);
                    } catch (InterruptedException ex) {
                        Utilities.handleException(ex);
                    }
                    
                    HttpClient client2 = null;
                    HttpPost post2 = null;
                    switch (currentComparisonChoice) {
                        case BRITISH_MUSEUM_COLLECTION: {
                            
                            String USER_AGENT = "Mozilla/5.0";
                            String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                            client2 = new DefaultHttpClient();
                            post2 = new HttpPost(baseURL);
                            post2.setHeader("User-Agent", USER_AGENT);
                            
                            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                            urlParameters.add(new BasicNameValuePair("query", overall_instances_query));
                            urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                            urlParameters.add(new BasicNameValuePair("implicit", "true"));
                            urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                            urlParameters.add(new BasicNameValuePair("_form", "/sparql"));
                            
                            post2.setEntity(new UrlEncodedFormEntity(urlParameters));
                            break;
                        }
                        
                        default: {
                            System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                            return ApiConstants.IMAPIFailCode;
                        }
                    }//end of switch;
                    
                    
                    
                    HttpResponse response2 = client2.execute(post2);
                    
                    int responseCode2 = response2.getStatusLine().getStatusCode();//con.getResponseCode();
                    if(responseCode2!=200){
                        System.out.println("Response Code: " + responseCode2);
                        System.out.println("ReasonPhrase returned." + response2.getStatusLine().getReasonPhrase());
                        return ApiConstants.IMAPIFailCode;
                    }

                }
                
                int ret = this.readJsonUriVals(instancesResponse.getEntity().getContent(), loopData);
                if (ret != ApiConstants.IMAPISuccessCode) {
                    return ret;
                }
                instanceResultsReturned.setValue(loopData.size());
                
                System.out.println("Starting processing of next " + instanceResultsReturned.getValue()+ " values. Time: "+Utilities.getCurrentTime());
                //process instances returned it this loop
                
                
                
                for(int i=0; i< loopData.size() ; i++){
                    String targetUri = loopData.get(i);
                    //loopInstancesData.put(targetUri, new SequencesVector());
                    
                    Enumeration<String> filesEnum = inputSourceInfo.keys();
                    while(filesEnum.hasMoreElements()){
                        String file = filesEnum.nextElement();
                        Hashtable<String, SequencesVector> fileInfo = inputSourceInfo.get(file);
                        
                        
                        if(fileInfo.containsKey(targetUri)){
                            
                            SourceTargetPair newPair = new SourceTargetPair( new SourceInstancePair(file, targetUri),
                                    new SourceInstancePair(currentComparisonChoice.toString(), targetUri));
                            if(pairSimilaritiesInSequences.containsKey(newPair)==false){
                                SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();
                                pairSimilaritiesInSequences.put(newPair, newSeqVec);
                            }
                        }
                    }
                }
                
                
                
                
                
                //for each sequence
                
                //if(canQuickFilteringMethodBeFollowed) then filtering on both instances and values
                //else if canQuickFilteringMethodBeFollowed == false then filtering on instances only
                
                
                
                //Vector<String> startingUris = new Vector<String>(loopData);
                //for each sequence check if the fast filtering method can be followed
                
                
                
                int processedInstancesInLoop = 0;
                int maxInstancesInLoop = loopData.size();
                while(processedInstancesInLoop<maxInstancesInLoop){
                    
                    Vector<String> currentInstancesSet = QueryBuilder.collectSequenctiallyAsubsetOfValues(processedInstancesInLoop, ApiConstants.instanceUrisFilterStepCount, loopData);
                    if(IMAPIClass.DEBUG){
                        System.out.println("\tProcessing next "+currentInstancesSet.size() +" vals.");
                    }
                    
                    
                    //THESE URIS DO EXIST IN THE DATABASE FOR SURE SO DO NOT TRY TO FILTER THEM
                    //currentInstancesSet.removeAll(urisThatDoNotExistInTheDatabase);
                    
                    
                    if(currentInstancesSet.size()==0){
                        continue;
                    }
                    
                    
                    for(int i=0; i< qSequences.size(); i++){
                        
                        
                        UserQueryConfiguration currentSeq = qSequences.get(i);
                        boolean canQuickFilteringMethodBeFollowed = canQuickFilteringMethodBeFollowedForeachSequence.get(i);
                        if(IMAPIClass.DEBUG){
                            System.out.println("\t\tDEBUG: sequence with mnemonic: " + currentSeq.getMnemonic() +" where "+(canQuickFilteringMethodBeFollowed?"fast":"slow")+" method must be followed");
                        }
                        
                        String[] parameterNames = currentSeq.getSortedParameterNamesCopy();
                        String[] parameterTypes = currentSeq.getSortedParameterTypesCopy();
                        String[] stepQueries    = currentSeq.getSortedQueriesCopy();
                        
                        int howmanySteps = parameterNames.length;
                        
                        //uri + step values
                        //Hashtable<String,SequencesVector> sequenceOutputData = new Hashtable<String, SequencesVector>();
                        //we also need the inverse hashtable SequenceData --> contained in Vector to Uri Strings
                        
                        
                        Vector<String> startingUris = new Vector<String>();
                        startingUris.addAll(currentInstancesSet);
                        
                        for(int j=0; j< howmanySteps; j++){
                            
                            Hashtable<String,Vector<DataRecord>> stepOutputData = new Hashtable<String, Vector<DataRecord>>();
                            
                            DELETE_getNextStepResultsStartingFromSpecificUris(startingUris,
                                    parameterNames[j],
                                    parameterTypes[j],
                                    queryPrefixes+stepQueries[j],
                                    stepOutputData);
                            
                            if(parameterTypes[j].equals(ApiConstants.Type_URI)){
                                
                                startingUris.clear();
                                Enumeration<String> valsEnum = stepOutputData.keys();
                                while(valsEnum.hasMoreElements())
                                {
                                    //also here find matchings and add to hash of sequence
                                    //adding to has h of sequence should be different if j==0 and if j>0
                                    
                                    String key = valsEnum.nextElement();
                                    Vector<DataRecord> seqVec = stepOutputData.get(key);
                                    for(int p=0; p< seqVec.size(); p++){
                                        DataRecord dat = seqVec.get(p);
                                        if(startingUris.contains(dat.getValue())==false){
                                            startingUris.add(dat.getValue());
                                        }
                                        
                                        Vector<String> relevantUris = new Vector<String>();
                                        
                                        if(j==0){
                                            relevantUris.add(key);
                                        }
                                        else{
                                            //add all instance keys from the hash that have as value the current key
                                        }
                                        
                                        
                                        
                                        
                                        //for(int instanceIndex =0; instanceIndex< relevantUris.size(); instanceIndex++){
                                        //    String uri = relevantUris.get(instanceIndex);
                                        //
                                        //    Enumeration<SourceInstancePair> sourceEnum = inputSourceInfo.keys();
                                        //    while (sourceEnum.hasMoreElements()) {
                                        //        SourceInstancePair pair = sourceEnum.nextElement();
                                        //        SequencesVector pairdata = inputSourceInfo.get(pair);
                                        
                                        //        SequenceData seqData = pairdata.getSequenceDataAtPosition(currentSequencePosition);
                                        //        if (seqData == null) {
                                        //            continue;
                                        //        }
                                        
                                        //        Vector<DataRecord> sourceVals = seqData.getValuesOfKey(currentPropertyName);
                                        //        if (sourceVals == null || sourceVals.size() == 0) {
                                        //            continue;
                                        //        }
                                        
                                        //        String paramType = parameters.get(currentPropertyName);
                                        //        DataRecord srcCompareRecord = new DataRecord("", "");
                                        
                                        //        Double tempComparisonResult = this.comp.compareValueAgainstVector(paramType, newRecord, sourceVals, srcCompareRecord);
                                        //        if (tempComparisonResult > 0d) {
                                        
                                        //            if (ApiConstants.KeepandPresentAllTargetDataFound) {
                                        //                if (targetSourceInfo.containsKey(searchVal) == false) {
                                        //                    SequencesVector newSeq = new SequencesVector();
                                        //                    newSeq.addValueToSequence(currentSequence, currentPropertyName, valStr, langStr);
                                        //                    targetSourceInfo.put(searchVal, newSeq);
                                        //                } else {
                                        //                    targetSourceInfo.get(searchVal).addValueToSequence(currentSequence, currentPropertyName, valStr, langStr);
                                        //                }
                                        //            }
                                        
                                        //            SourceTargetPair newSTPair = new SourceTargetPair(pair, searchVal);
                                        //int currentSequencePosition = currentSequence.getSequencePosition();
                                        //            SequenceSimilarityResult newSimResult = new SequenceSimilarityResult(currentSequencePosition, seqData.getSchemaInfo().getMnemonic(), seqData.getSchemaInfo().getWeight());
                                        //            newSimResult.setNewSimilarityResult(currentPropertyName, paramType, srcCompareRecord, newRecord, tempComparisonResult);
                                        
                                        //SimilarityTriplet newSimTriplet = new SimilarityTriplet(currentSequencePosition, currentStepPosition, tempComparisonResult);
                                        //            if (pairSimilaritiesInSequences.containsKey(newSTPair)) {
                                        
                                        //                pairSimilaritiesInSequences.get(newSTPair).addSequenceSimilarityResult(newSimResult);
                                        
                                        //            } else {
                                        //                SequenceSimilarityResultVector newSeqSimilarity = new SequenceSimilarityResultVector();
                                        //                newSeqSimilarity.add(newSimResult);
                                        //                pairSimilaritiesInSequences.put(newSTPair, newSeqSimilarity);
                                        //            }
                                        //        }
                                        //    }
                                        
                                        
                                        //}
                                        
                                    }
                                    
                                    
                                    
                                }
                                
                            }
                            else{
                                
                                //find matchings and break;
                                break;
                            }
                            
                            
                            
                            //if(canQuickFilteringMethodBeFollowed){
                            //}
                            //else{
                            //loopdata must be refilled in next step
                            //input uris --> outputUris
                            
                            //    if(parameterTypes[j].equals(ApiConstants.Type_URI)){
                            //startingUris.clear();
                            //stepOutputData.
                            //startingUris.addAll(stepOutputData.values());
                            //    }
                            //performExhaustiveStepQuery(loopData, currentSeq, inputSourceInfo, pairSimilaritiesInSequences);
                            //}
                        }
                        
                    }
                    
                    
                    processedInstancesInLoop += ApiConstants.instanceUrisFilterStepCount;
                    if(processedInstancesInLoop>maxInstancesInLoop){
                        processedInstancesInLoop = maxInstancesInLoop;
                    }
                    
                    if(IMAPIClass.DEBUG){
                        System.out.println("\tProcessed "+processedInstancesInLoop +" vals.");
                    }
                }
                
                
                double percentage = ((double)(overallInstancesProgressCounter+instanceResultsReturned.getValue())*100d)/(double)totalInstancesCount;
                
                System.out.println("Total values processed: " + (overallInstancesProgressCounter+instanceResultsReturned.getValue())+". Percentage: " + Utilities.df.format(percentage)+"% at time: "+Utilities.getCurrentTime());
                System.out.println();
                overallInstancesProgressCounter +=instanceResultsReturned.getValue();
                
            }//end of limit offset while loop over instances of interest
            System.out.println("\n\nFinished Instance Processing at: "+Utilities.getCurrentTime() +". Total Values Processed: "+overallInstancesProgressCounter);
        }
        catch(IOException ex){
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }
        
        return ApiConstants.IMAPISuccessCode;
    }
    
    
    private void DELETE_getNextStepResultsStartingFromSpecificUris(Vector<String> startingUris, String parameterName, String parameterType,
            String stepQuery, Hashtable<String,Vector<DataRecord>> nextStepResults){
        
    }
    
    
    
    // HTTP Send GET request
    int DELETE_retrieveAllInstancesData(String baseQuery,
            SourceDataHolder inputSourceInfo,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) throws IOException {
        
        System.out.println("Retrieving Relevant Instances");
        
        int LIMIT = this.imapi.qWeightsConfig.getQueryLimitSize();
        
        String paramName = "";
        Query tempQuery = QueryFactory.create(baseQuery);
        List<Var> vars = tempQuery.getProjectVars();
        if (vars.size() > 0) {
            paramName = vars.get(0).getName().replace("?", "");
        }
        tempQuery = null;
        if (paramName.length() == 0) {
            System.out.println("Could not retrieve parameter name for get all instances query.");
            return ApiConstants.IMAPIFailCode;
        }
        
        Vector<String> allValuesFound = inputSourceInfo.collectAllUriValuesOfInstances();
        if (allValuesFound.size() == 0) {
            System.out.println("No source uris found skiping queries to the database.");
            return ApiConstants.IMAPISuccessCode;
        }
        
        int currentCounter = 0;
        int maxFilterCounter = allValuesFound.size();
        
        while (currentCounter < maxFilterCounter) {
            
            Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter, ApiConstants.instanceUrisFilterStepCount, allValuesFound);
            currentCounter += ApiConstants.instanceUrisFilterStepCount;
            
            int OFFSET = -LIMIT;
            IntegerObject resultsReturned = new IntegerObject(LIMIT);
            
            while (resultsReturned.getValue() == LIMIT) {
                String query = baseQuery;
                query = QueryBuilder.replaceFilteringPlaceHolders(query,ApiConstants.filter_STARTING_URIS_placeHolders, paramName, currentSubset);
                
                OFFSET += LIMIT;
                query += "\n LIMIT " + LIMIT;
                if (OFFSET > 0) {
                    query += "\n OFFSET " + OFFSET;
                }
                
                //System.out.println("In Getting instances Query with offset: "+OFFSET);
                HttpClient client = null;
                HttpPost post = null;
                
                if (IMAPIClass.ExtentedMessagesEnabled) {
                    System.out.println("\n\nGetting instances with query:\n=============================================\n" + query);
                }
                
                switch (this.currentDB.getDBChoice()) {
                    case BRITISH_MUSEUM_COLLECTION: {
                        
                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint() + ".json";
                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);
                        post.setHeader("User-Agent", USER_AGENT);
                        
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        urlParameters.add(new BasicNameValuePair("_implicit", "false"));
                        urlParameters.add(new BasicNameValuePair("implicit", "true"));
                        urlParameters.add(new BasicNameValuePair("_equivalent", "false"));
                        urlParameters.add(new BasicNameValuePair("_form", "/sparql"));
                        
                        post.setEntity(new UrlEncodedFormEntity(urlParameters));
                        
                        break;
                    }
                    
                    case CLAROS: {
                        
                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint();
                        
                        client = new DefaultHttpClient();
                        post = new HttpPost(baseURL);
                        
                        post.setHeader("User-Agent", USER_AGENT);
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("query", query));
                        //urlParameters.add(new BasicNameValuePair("default-graph-uri", ""));
                        //urlParameters.add(new BasicNameValuePair("exampleQueries=", ""));
                        //urlParameters.add(new BasicNameValuePair("common_prefixes", "on"));
                        urlParameters.add(new BasicNameValuePair("format", "srj"));
                        //urlParameters.add(new BasicNameValuePair("timeout", "0"));
                        //urlParameters.add(new BasicNameValuePair("debug", "on"));
                        post.setEntity(new UrlEncodedFormEntity(urlParameters));
                        
                        break;
                    }
                    default: {
                        System.out.println("Not Implemented Choice: " + this.imapi.userConfig.getComparisonMode().toString());
                        return ApiConstants.IMAPIFailCode;
                    }
                }//end of switch;
                
                if (client == null || post == null) {
                    return ApiConstants.IMAPIFailCode;
                }
                
                HttpResponse response = client.execute(post);
                
                int responseCode = response.getStatusLine().getStatusCode();//con.getResponseCode();
                
                if (responseCode != 200) {
                    System.out.println("Response Code: " + responseCode);
                    System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());
                    return ApiConstants.IMAPIFailCode;
                }
                if (IMAPIClass.ExtentedMessagesEnabled) {
                    System.out.println("responce code = 200!");
                }
                
                int ret = this.DELETE_readJSONToGetAllSimilarInstances(response.getEntity().getContent(), inputSourceInfo, pairSimilaritiesInSequences, resultsReturned);
                
                if (ret != ApiConstants.IMAPISuccessCode) {
                    return ret;
                }
                
            }//end of while that performs the limit / offset mechanism
        }
        
        
        return ApiConstants.IMAPISuccessCode;
    }
    */

    
//</editor-fold>
}
