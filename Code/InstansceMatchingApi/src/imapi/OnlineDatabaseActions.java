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
import static imapi.IMAPIClass.AcceptUriEquality;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import sun.misc.IOUtils;


class OnlineDatabaseActions {

    OnlineDatabase currentDB =null;
    private BaseComparisonClass comp=null;
    private IMAPIClass imapi=null;
    
    public OnlineDatabaseActions(IMAPIClass whichImClass, OnlineDatabase whichDB) {
        this.currentDB = whichDB;
        this.imapi = whichImClass;
        this.comp = new BaseComparisonClass(whichImClass);
    }

    int checkIfDBisAvailable(){
        ApiConstants.TargetSourceChoice targetChoice = this.currentDB.getDBChoice();
        System.out.println("\n=======================================\n"); 
        System.out.println("Checking if online database " +  targetChoice.toString() +" is available.");
        String baseURL = "";
        switch(targetChoice){
            case BRITISH_MUSEUM_COLLECTION:
            case CLAROS:{
                
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
                        System.out.println("Seems that online database: " +targetChoice.toString() +" at url: " +baseURL+" is unavailable.");
                        System.out.println("Please try again later or change user configuration xml file to "+ApiConstants.TargetSourceChoice.FILE_COMPARISON.toString() +".");
                        System.out.println("Response Code: " + responseCode);
                        System.out.println("ReasonPhrase returned." + con.getResponseMessage());
                        return ApiConstants.IMAPIFailCode;
                    }
                    if(IMAPIClass.ExtentedMessagesEnabled){
                        System.out.println("responce code = 200!");
                    }
                    
                    if(targetChoice==ApiConstants.TargetSourceChoice.CLAROS){
                        System.out.println("\n\nWARNING: "+targetChoice.toString() +" database does not support inference.\n"
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
                    System.out.println("Seems that online database: " +targetChoice.toString() +" at url: " +baseURL+" is unavailable.");
                    if(IMAPIClass.DEBUG){
                        System.out.println("IOException caught while trying to access database "+ApiConstants.TargetSourceChoice.BRITISH_MUSEUM_COLLECTION.toString());                        
                        System.out.println(ex.getMessage());
                        ex.printStackTrace(System.out);
                    }
                    //Logger.getLogger(IMAPIClass.class.getName()).log(Level.SEVERE, null, ex);
                    return ApiConstants.IMAPIFailCode;
                }

                
                break;                
            }
            default:{
                return ApiConstants.IMAPIFailCode;
            }
                
        }
        
        System.out.println("\n=======================================\n"); 
        System.out.println("Online database "+targetChoice.toString()+" is available.");
        return ApiConstants.IMAPISuccessCode;
    }
    private void showContentsOfInputStream(InputStream stream){
        
        char[] buffer = new char[1024];
        StringBuilder out = new StringBuilder();
        try {
            final Reader in = new InputStreamReader(stream, "UTF-8");
            try {
                for (;;) {
                    int rsz = in.read(buffer, 0, buffer.length);
                    if (rsz < 0)
                        break;
                    out.append(buffer, 0, rsz);
                }
            }
            finally {
                in.close();
            }
        }
        catch (UnsupportedEncodingException ex) {
            /* ... */
        }
        catch (IOException ex) {
            /* ... */
        }
        System.out.println(out.toString());

    }
    private int readJSONToGetAllSimilarInstances(InputStream inputStream, 
            
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo, 
            Hashtable<SourceInstancePair, SequencesVector> targetSourceInfo,
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
                    Enumeration<SourceInstancePair> sourceEnum = inputSourceInfo.keys();
                    while(sourceEnum.hasMoreElements()){
                        SourceInstancePair pair = sourceEnum.nextElement();
                        if(pair.getInstanceUri().equals(uri)){
                            
                            SourceTargetPair newSTPair = new SourceTargetPair(pair, currentVal);
                            SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();
                            if(pairSimilaritiesInSequences.containsKey(newSTPair)==false){
                                pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                            }   
                            
                            if(ApiConstants.KeepandPresentAllTargetDataFound){
                                if (targetSourceInfo.containsKey(currentVal) == false) {
                                    targetSourceInfo.put(currentVal, new SequencesVector());
                                }
                            }
                        }
                    }
                }

            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
            return ApiConstants.IMAPIFailCode;
        }

        resultsReturned.setValue(cnt);
        return ApiConstants.IMAPISuccessCode;
        
            
        }
    /**
     * @param inputStream
     * @return
     */
    private int readJSON(InputStream inputStream, 
            UserQueryConfiguration currentSequence,
            
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo, 
            Hashtable<SourceInstancePair, SequencesVector> targetSourceInfo,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences,
            IntegerObject resultsReturned) {

        int currentSequencePosition = currentSequence.getPositionID();
        Hashtable<String,String> parameters = currentSequence.getAllQueryStepParameters();
        
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
                            if(uri.length()>0){
                                searchVal  = new SourceInstancePair(this.currentDB.getDBChoice().toString(), uri);
                                
                            }                            
                        }else {
                            
                            String langStr = "";

                            if (jObj.has("xml:lang")) {
                                langStr = jObj.get("xml:lang").getAsString();
                            }
                            
                            if(searchVal==null){
                                continue;
                            }
                            
                            DataRecord newRecord = new DataRecord(valStr, langStr);
                            //System.out.println(uri + "\t"+newRecord.toString());
                            //here we should see if we have to store the value found --> make the comparison
                            
                            Enumeration<SourceInstancePair> sourceEnum = inputSourceInfo.keys();
                            while(sourceEnum.hasMoreElements()){
                                SourceInstancePair pair = sourceEnum.nextElement();
                                SequencesVector pairdata = inputSourceInfo.get(pair);
                                
                                SequenceData seqData = pairdata.getSequenceDataAtPosition(currentSequencePosition);
                                if(seqData==null){
                                    continue;
                                }
                                
                                Vector<DataRecord> sourceVals = seqData.getValuesOfKey(currentPropertyName);
                                if(sourceVals ==null || sourceVals.size()==0){
                                    continue;
                                }
                                
                                String paramType = parameters.get(currentPropertyName);
                                DataRecord srcCompareRecord = new DataRecord("", "");
                                
                                Double tempComparisonResult = this.comp.compareValueAgainstVector(paramType,newRecord, sourceVals, srcCompareRecord);
                                if(tempComparisonResult>0d){
                                                         
                                    if(ApiConstants.KeepandPresentAllTargetDataFound){
                                        if (targetSourceInfo.containsKey(searchVal) == false) {
                                            SequencesVector newSeq = new SequencesVector();
                                            newSeq.addValueToSequence(currentSequence, currentPropertyName, valStr, langStr);
                                            targetSourceInfo.put(searchVal, newSeq);
                                        }
                                        else{
                                            targetSourceInfo.get(searchVal).addValueToSequence(currentSequence, currentPropertyName, valStr, langStr);
                                        }
                                    }
                                    
                                    
                                    SourceTargetPair newSTPair = new SourceTargetPair(pair, searchVal);
                                    //int currentSequencePosition = currentSequence.getSequencePosition();
                                    SequenceSimilarityResult newSimResult = new SequenceSimilarityResult(currentSequencePosition,seqData.getSchemaInfo().getMnemonic(), seqData.getSchemaInfo().getWeight());
                                    newSimResult.setNewSimilarityResult(currentPropertyName, paramType, srcCompareRecord, newRecord, tempComparisonResult);
                                            
                                    //SimilarityTriplet newSimTriplet = new SimilarityTriplet(currentSequencePosition, currentStepPosition, tempComparisonResult);
                                    if(pairSimilaritiesInSequences.containsKey(newSTPair)){
                                        
                                        pairSimilaritiesInSequences.get(newSTPair).addSequenceSimilarityResult(newSimResult);
                                        
                                    }
                                    else{
                                        SequenceSimilarityResultVector newSeqSimilarity = new SequenceSimilarityResultVector();
                                        newSeqSimilarity.add(newSimResult);
                                        pairSimilaritiesInSequences.put(newSTPair, newSeqSimilarity);
                                    }                                    
                                }                                
                            }

                        }
                    }
                }
                
                
                // end of asking for all predicates of select clause head/vars names 
              

            }// edn of while llop that iters across all bindings

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
            return ApiConstants.IMAPIFailCode;
        }
        

        resultsReturned.setValue(cnt);
        return ApiConstants.IMAPISuccessCode;
        
    }
    
    

    public int retrieveDataOfSpecificSequence(String queryPrefixes, 
            UserQueryConfiguration currentQuerySequence,
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo, 
            Hashtable<SourceInstancePair, SequencesVector> targetSourceInfo, 
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) throws IOException {
        
        int currentSequencePosition = currentQuerySequence.getPositionID();
        System.out.println("\n\nQuering Sequence "+(currentSequencePosition+1)+" --> mnemonic: "+currentQuerySequence.getMnemonic());        
        
        
        
        String[] stepParameterNames   = currentQuerySequence.getSortedParameterNamesCopy();
        String[] stepParameterTypes   = currentQuerySequence.getSortedParameterTypesCopy();
        String[] stepParameterQueries = currentQuerySequence.getSortedQueriesCopy();
        
        
        //performing all steps in a llop
        for(int k=0; k<stepParameterQueries.length; k++){

            
            System.out.println("In step "+(k+1)+" out of "+stepParameterQueries.length);
            int LIMIT = ApiConstants.onLineDBQueryStepSize;
            String currentStepParameterName = stepParameterNames[k];
            String parameterType = stepParameterTypes[k].toLowerCase();
            
            Vector<String> allValuesFound = new Vector<String>();
            
            //filtering case
            if(parameterType.equals("uri")){
                allValuesFound = DataRetrievalOperations.collectAllValuesOfSpecificParameter(currentSequencePosition, currentStepParameterName, inputSourceInfo);
                if(allValuesFound.size()==0){
                    System.out.println("No source values found skiping this query step to the database.");
                    continue;
                }
            }
            else{
                if(DataRetrievalOperations.containsValuesOfSpecificParameter(currentSequencePosition, currentStepParameterName, inputSourceInfo) == false){
                    System.out.println("No source values found skiping this query step to the database.");
                    continue;
                }
            }
            
            int currentCounter =0;
            int maxFilterCounter = allValuesFound.size();

            if(parameterType.equals("uri")==false){
                maxFilterCounter =1;
            }

            while(currentCounter<maxFilterCounter){
                
                Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter,allValuesFound);
                
                //with this statement i make sure that if no filtering is applied 
                //then only once the queriesa will be executed
                if(parameterType.equals("uri")){
                    currentCounter+=ApiConstants.UriFilterStepCount; 
                }
                else{
                    currentCounter = maxFilterCounter;
                }
                
            
                
                int OFFSET = -LIMIT;
                IntegerObject resultsReturned = new IntegerObject(LIMIT);
            
                
                
                //query =  query;
                
                while(resultsReturned.getValue()==LIMIT){
                    String query = queryPrefixes + stepParameterQueries[k];
                    //int currentStepPosition = qStep.getStepPositionInSequence();

                    query = QueryBuilder.replaceFilteringPlaceHolders(query, currentStepParameterName, currentSubset);
                
                    OFFSET+=LIMIT;
                    query +="\n LIMIT "+LIMIT; 
                    if(OFFSET>0){
                        query +="\n OFFSET "+OFFSET;
                    }   
                
                    
                    HttpClient client =null;
                    HttpPost post = null;
                    if(OFFSET==0){ 
                        if(IMAPIClass.ExtentedMessagesEnabled){
                            System.out.println("\n\nPerforming the following query:\n=============================================\n"+query+"\n");
                        }
                    }
                    else{
                        System.out.println("Quering with offset: "+OFFSET);
                    }
                    switch (this.currentDB.getDBChoice()) {
                        case BRITISH_MUSEUM_COLLECTION: {

                            String USER_AGENT = "Mozilla/5.0";
                            String baseURL = this.currentDB.getDbSparqlEndpoint()+".json";
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
                    
                    if(client==null || post ==null){
                        return ApiConstants.IMAPIFailCode;
                    }

                    HttpResponse response = client.execute(post);

                    int responseCode =  response.getStatusLine().getStatusCode();//con.getResponseCode();

                    if (responseCode != 200) {
                        System.out.println("Response Code: " + responseCode);                                
                        System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());                                    
                        return ApiConstants.IMAPIFailCode;
                    }
                    if(IMAPIClass.ExtentedMessagesEnabled){
                        System.out.println("responce code = 200!");
                    }
                    int ret = this.readJSON(response.getEntity().getContent(), currentQuerySequence, inputSourceInfo, targetSourceInfo, pairSimilaritiesInSequences, resultsReturned);                    
                    if(ret!=ApiConstants.IMAPISuccessCode){
                        return ret;
                    }                
                }//end of limit - offset while loop
            }//end of filtering values while loop
        }//end of query steps
                
        
        return ApiConstants.IMAPISuccessCode;
    }
        
    // HTTP Send GET request
    public int retrieveAllInstancesData(String baseQuery,
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo, 
            Hashtable<SourceInstancePair, SequencesVector> targetSourceInfo, 
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) throws IOException {

        System.out.println("Retrieving Relevant Instances");
        
        int LIMIT = ApiConstants.onLineDBQueryStepSize;
        
        String paramName ="";
        Query tempQuery = QueryFactory.create(baseQuery);
        List<Var> vars = tempQuery.getProjectVars();
        if(vars.size()>0){
            paramName = vars.get(0).getName().replace("?", "");                
        }            
        tempQuery = null;
        if(paramName.length()==0){
            System.out.println("Could not retrieve parameter name for get all instances query.");
            return ApiConstants.IMAPIFailCode;
        }
        
        
        
        Vector<String> allValuesFound  = DataRetrievalOperations.collectAllURIValues(inputSourceInfo);
        if(allValuesFound.size()==0){
            System.out.println("No source uris found skiping queries to the database.");
            return ApiConstants.IMAPISuccessCode;
        }
        
        int currentCounter =0;
        int maxFilterCounter = allValuesFound.size();


        while(currentCounter<maxFilterCounter){
            
            Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter,allValuesFound);
            currentCounter+=ApiConstants.UriFilterStepCount; 
            
            int OFFSET = -LIMIT;
            IntegerObject resultsReturned = new IntegerObject(LIMIT);
            
            
            
            
            
            while(resultsReturned.getValue()==LIMIT){
                String query = baseQuery;
                query = QueryBuilder.replaceFilteringPlaceHoldersForInstances(query, paramName, currentSubset);
                
                OFFSET+=LIMIT;
                query +="\n LIMIT "+LIMIT; 
                if(OFFSET>0){
                    query +="\n OFFSET "+OFFSET;
                }
                
                //System.out.println("In Getting instances Query with offset: "+OFFSET);
                HttpClient client =null;
                HttpPost post = null;
                
                if(IMAPIClass.ExtentedMessagesEnabled){
                    System.out.println("\n\nGetting instances with query:\n=============================================\n"+query);
                }
                
            
                switch (this.currentDB.getDBChoice()) {
                    case BRITISH_MUSEUM_COLLECTION: {

                        String USER_AGENT = "Mozilla/5.0";
                        String baseURL = this.currentDB.getDbSparqlEndpoint()+".json";
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
            
                if(client==null || post ==null){
                    return ApiConstants.IMAPIFailCode;
                }

                HttpResponse response = client.execute(post);

                int responseCode =  response.getStatusLine().getStatusCode();//con.getResponseCode();

                if (responseCode != 200) {
                    System.out.println("Response Code: " + responseCode);                                
                    System.out.println("ReasonPhrase returned." + response.getStatusLine().getReasonPhrase());                                    
                    return ApiConstants.IMAPIFailCode;
                }
                if(IMAPIClass.ExtentedMessagesEnabled){
                        System.out.println("responce code = 200!");
                    }

                int ret = this.readJSONToGetAllSimilarInstances(response.getEntity().getContent(), inputSourceInfo, targetSourceInfo, pairSimilaritiesInSequences, resultsReturned);                    

                if(ret!=ApiConstants.IMAPISuccessCode){
                    return ret;
                }

            }//end of while that performs the limit / offset mechanism
        }
        if(ApiConstants.KeepandPresentAllTargetDataFound){
            System.out.println("Found a total of " +targetSourceInfo.size() + " relevants instances.");
        }

        return ApiConstants.IMAPISuccessCode;
    }

}
