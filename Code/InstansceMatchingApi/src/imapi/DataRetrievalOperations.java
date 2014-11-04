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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.util.FileManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tzortzak
 */
class DataRetrievalOperations {

    IMAPIClass imClass = null;

    private BaseComparisonClass comp = null;
    private enum DataMode {

        SOURCE_DATA, TARGET_DATA
    };


    DataRetrievalOperations(IMAPIClass whichImClass) {
        this.imClass = whichImClass;
        this.comp = new BaseComparisonClass(this.imClass);
    }

    boolean checkNameSpaceUriInternetAvailability(String nsUri) {
        boolean returnVal = true;
        try {

            String USER_AGENT = "Mozilla/5.0";

            HttpURLConnection con = (HttpURLConnection) (new URL(nsUri)).openConnection();

            // optional default is GET
            con.setRequestMethod("GET");
            con.setRequestProperty("HTTP", "1.1");
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                returnVal = false;
            }

        } catch (MalformedURLException ex) {
            Utilities.handleException(ex);
            returnVal = false;
        } catch (IOException ex) {
            Utilities.handleException(ex);
            returnVal = false;
        }

        if (returnVal == false) {
            System.out.println("Could not find namespace: " + nsUri);
        }
        return returnVal;
    }

    Hashtable<String, Model> retrieveAllDeclaredNamespacesModels() {

        System.out.println("\n=======================================\n");
        Hashtable<String, Model> allDeclaredModels = new Hashtable<String, Model>();
        Vector<String> includeNamespaces = this.imClass.qWeightsConfig.getIncludeNamespaces();

        Vector<CidocCrmCompatibleFile> inputFiles = this.imClass.userConfig.getSourceInputFiles();
        Vector<String> finallySkippedNamespaces = new Vector<String>();

        Vector<String> sourceInputFilesNamespaces = new Vector<String>();
        sourceInputFilesNamespaces.add("http://erlangen-crm.org/current/");
        sourceInputFilesNamespaces.add("http://purl.org/NET/crm-owl#");
        sourceInputFilesNamespaces.add("http://www.w3.org/2004/02/skos/core#");
        sourceInputFilesNamespaces.add("http://www.w3.org/2000/01/rdf-schema#");
        sourceInputFilesNamespaces.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        for (int i = 0; i < inputFiles.size(); i++) {

            if (IMAPIClass.DEBUG) {
                System.out.println("DEBUG: Retrieving namespaces from file: " + inputFiles.get(i).getFilePath());
            }
            Model CidocCRMmodel = ModelFactory.createDefaultModel();
            InputStream cidocInputStream = FileManager.get().open(inputFiles.get(i).getFilePath());
            if (cidocInputStream == null) {
                continue;
            }

            CidocCRMmodel = CidocCRMmodel.read(cidocInputStream, null);

            Map<String, String> namespaces = CidocCRMmodel.getNsPrefixMap();
            Vector<String> nKeys = new Vector<String>(namespaces.keySet());
            for (int m = 0; m < nKeys.size(); m++) {

                String keyStr = nKeys.get(m);
                String fileNs = namespaces.get(keyStr);

                if (includeNamespaces.contains(fileNs) == false) {
                    if (finallySkippedNamespaces.contains(fileNs) == false) {
                        finallySkippedNamespaces.add(fileNs);
                    }
                    continue;
                }

                if (allDeclaredModels.containsKey(fileNs)) {
                    continue;
                }

                if (checkNameSpaceUriInternetAvailability(fileNs) == false) {
                    continue;
                }

                Model loopModel = ModelFactory.createDefaultModel();
                boolean exCaught = false;
                try {
                    System.out.println("Retrieving model of namespace " + fileNs);
                    loopModel = loopModel.read(fileNs);

                } catch (Exception ex) {
                    System.out.println("Exception caught while retrieving schema info for namespace: " + fileNs);
                    Utilities.handleException(ex);
                    exCaught = true;
                }
                if (exCaught) {
                    continue;
                }

                allDeclaredModels.put(fileNs, loopModel);

                /*
                 if i close them then an exception is caught
                 loopModel.removeAll();
                 loopModel.close();
                 loopModel = null;
                 */
            }

            CidocCRMmodel = CidocCRMmodel.removeAll();
            CidocCRMmodel.close();
            CidocCRMmodel = null;
            try {
                cidocInputStream.close();
            } catch (IOException ex) {
                Utilities.handleException(ex);
            }
            cidocInputStream = null;
        }

        ApiConstants.TargetSourceChoice targetChoice = this.imClass.userConfig.getComparisonMode();
        switch (targetChoice) {
            case FILE_COMPARISON: {
                Vector<CidocCrmCompatibleFile> targetFiles = this.imClass.userConfig.getTargetInputFiles();

                for (int i = 0; i < targetFiles.size(); i++) {
                    Model CidocCRMmodel = ModelFactory.createDefaultModel();
                    InputStream cidocInputStream = FileManager.get().open(targetFiles.get(i).getFilePath());
                    if (cidocInputStream == null) {
                        continue;
                    }

                    CidocCRMmodel.read(cidocInputStream, null);

                    Map<String, String> namespaces = CidocCRMmodel.getNsPrefixMap();
                    Vector<String> nKeys = new Vector<String>(namespaces.keySet());
                    for (int m = 0; m < nKeys.size(); m++) {
                        String keyStr = nKeys.get(m);
                        String fileNs = namespaces.get(keyStr);

                        if (includeNamespaces.contains(fileNs) == false) {
                            if (finallySkippedNamespaces.contains(fileNs) == false) {
                                finallySkippedNamespaces.add(fileNs);
                            }
                            continue;
                        }

                        if (allDeclaredModels.containsKey(fileNs)) {
                            continue;
                        }

                        if (checkNameSpaceUriInternetAvailability(fileNs) == false) {
                            continue;
                        }
                        Model loopModel = ModelFactory.createDefaultModel();
                        boolean exCaught = false;
                        try {
                            System.out.println("Retrieving model of namespace " + fileNs);
                            loopModel = loopModel.read(fileNs);
                        } catch (Exception ex) {
                            System.out.println("Exception caught while retrieving schema info for namespace: " + fileNs);
                            Utilities.handleException(ex);
                            exCaught = true;
                        }
                        if (exCaught) {
                            continue;
                        }

                        allDeclaredModels.put(fileNs, loopModel);

                    }

                    CidocCRMmodel.removeAll();
                    CidocCRMmodel.close();
                    CidocCRMmodel = null;
                    try {
                        cidocInputStream.close();
                    } catch (IOException ex) {
                        Utilities.handleException(ex);
                    }
                    cidocInputStream = null;
                }
                break;
            }
            /*
             case BRITISH_MUSEUM_COLLECTION:{
                
             }
             */
            default: {
                OnlineDatabase db = this.imClass.conf.getOnlineDb(targetChoice);
                if (db == null) {
                    break;
                }
                Hashtable<String, String> dbNamepaces = db.getDbNamesapcesCopy();

                Enumeration<String> nsEnum = dbNamepaces.keys();
                while (nsEnum.hasMoreElements()) {
                    String keyStr = nsEnum.nextElement();
                    String namespace = dbNamepaces.get(keyStr);

                    if (includeNamespaces.contains(namespace) == false) {
                        if (finallySkippedNamespaces.contains(namespace) == false) {
                            finallySkippedNamespaces.add(namespace);
                        }
                        continue;
                    }

                    if (allDeclaredModels.containsKey(namespace)) {
                        continue;
                    }

                    if (checkNameSpaceUriInternetAvailability(namespace) == false) {

                        continue;
                    }

                    Model loopModel = ModelFactory.createDefaultModel();
                    boolean exCaught = false;
                    try {
                        System.out.println("Retrieving model of namespace " + namespace);
                        loopModel = loopModel.read(namespace);
                    } catch (Exception ex) {
                        System.out.println("Exception caught while retrieving schema info for namespace: " + namespace);
                        System.out.println(ex.getMessage());
                        exCaught = true;
                    }
                    if (exCaught) {
                        continue;
                    }

                    allDeclaredModels.put(namespace, loopModel);

                }

                break;
            }
        }

        //Now add the extensions to valid cidoc namespaces
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();

        Enumeration<String> allNs = allDeclaredModels.keys();
        while (allNs.hasMoreElements()) {
            String next = allNs.nextElement();
            String initialNamespace = next;
            if (validCrmNs.contains(next) == false) {
                continue;
            }

            Model crmModel = allDeclaredModels.get(next);
            //CLAROS fix Claros database needs http://purl.org/NET/crm-owl# for 
            //it's queries but actually uses   http://erlangen-crm.org/091217/
            if (targetChoice == ApiConstants.TargetSourceChoice.CLAROS && next.equals("http://purl.org/NET/crm-owl#")) {
                next = "http://erlangen-crm.org/091217/";
            }
            String rdfStatements = this.imClass.qWeightsConfig.getCrmExtentionsString(next);
            Model loopModel = ModelFactory.createDefaultModel();

            loopModel.read(new java.io.StringReader(rdfStatements), next);

            crmModel = crmModel.union(loopModel);

            allDeclaredModels.put(initialNamespace, crmModel);
        }
        Collections.sort(finallySkippedNamespaces);
        System.out.println();
        for (int i = 0; i < finallySkippedNamespaces.size(); i++) {
            System.out.println((i + 1) + ".\t Skipped namespace: " + finallySkippedNamespaces.get(i));
        }
        System.out.println();

        return allDeclaredModels;
    }

    //Retrieving schema info and preparing queries
    private Model getSchemaInfoFromFile(CidocCrmCompatibleFile cidocLoadFile,
            Hashtable<String, Model> allRetrievedModels,
            StringObject queryPrefixes,
            StringObject getAllInstances,
            StringObject countAllInstances,
            Vector<UserQueryConfiguration> qSequences) {

        Model finalFileModel = ModelFactory.createDefaultModel();
        QueryBuilder qBuilder = new QueryBuilder(this.imClass);
        queryPrefixes.setString("");

        Hashtable<String, String> allNamespacesThatWillBeUsed = new Hashtable<String, String>();
        Vector<String> includeNamespaces = this.imClass.qWeightsConfig.getIncludeNamespaces();
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();
        Hashtable<String, String> validCrmNsUsed = new Hashtable<String, String>();

        String loadFilePath = cidocLoadFile.getFilePath();
        Model CidocCRMmodel = ModelFactory.createDefaultModel();
        InputStream cidocInputStream = FileManager.get().open(loadFilePath);
        if (cidocInputStream == null) {
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Error while trying to load file: " + loadFilePath);
            return null;
        }
        CidocCRMmodel = CidocCRMmodel.read(cidocInputStream, null);

        finalFileModel = finalFileModel.union(CidocCRMmodel);

        //<editor-fold defaultstate="collapsed" desc="Read models of included namespaces and collect prefixes">
        Map<String, String> namespaces = CidocCRMmodel.getNsPrefixMap();
        Vector<String> nKeys = new Vector<String>(namespaces.keySet());
        for (int m = 0; m < nKeys.size(); m++) {
            String keyStr = nKeys.get(m);
            String fileNs = namespaces.get(keyStr);

            queryPrefixes.setString(queryPrefixes.getString() + "PREFIX " + keyStr + ": <" + fileNs + ">\n");
            allNamespacesThatWillBeUsed.put(keyStr, fileNs);

            if (validCrmNs.contains(fileNs)) {

                validCrmNsUsed.put(keyStr, fileNs);

            }

            if (includeNamespaces.contains(fileNs)) {
                Model loopModel = allRetrievedModels.get(fileNs);
                if (loopModel != null) {
                    finalFileModel = finalFileModel.union(loopModel);
                }
            }
        }
        CidocCRMmodel.removeAll();
        CidocCRMmodel.close();
        CidocCRMmodel = null;

        try {
            cidocInputStream.close();
        } catch (Exception ex) {
            Utilities.handleException(ex);
        }
        cidocInputStream = null;

        if (validCrmNsUsed.size() > 1) {
            System.out.println("Warning: In file " + loadFilePath + " more that one Cidoc Namespaces were found:\n" + validCrmNsUsed.toString());
        }
        queryPrefixes.setString(queryPrefixes.getString() + "\n");

        //</editor-fold>
        //prepare a model that will be used in order to retrieve the cidoc classes and predicates 
        //it will only contain cidoc compatible namespaces
        Model prepareQueriesModel = ModelFactory.createDefaultModel();
        Enumeration<String> validCrmEnum = validCrmNsUsed.keys();
        while (validCrmEnum.hasMoreElements()) {
            Model loopModel = allRetrievedModels.get(validCrmNsUsed.get(validCrmEnum.nextElement()));
            prepareQueriesModel = prepareQueriesModel.union(loopModel);
        }

        int ret = qBuilder.prepareQueries(validCrmNsUsed, prepareQueriesModel, ApiConstants.TargetSourceChoice.FILE_COMPARISON, cidocLoadFile.getPredicateDirectionUsage(), getAllInstances, countAllInstances, qSequences);

        if (ret != ApiConstants.IMAPISuccessCode) {
            return null;
        }
        prepareQueriesModel = prepareQueriesModel.removeAll();
        prepareQueriesModel.close();
        prepareQueriesModel = null;

        return finalFileModel;
    }

    private int getInstancesCardinalityInFile(StringObject prefixes, StringObject baseQuery, OntModel fileModel) {

        int returnResult = -1;
        Query instancesQuery = QueryFactory.create(prefixes.getString() + baseQuery.getString());
        QueryExecution instancesQueryExecution = QueryExecutionFactory.create(instancesQuery, fileModel);
        com.hp.hpl.jena.query.ResultSet instancesResults = instancesQueryExecution.execSelect();

        while (instancesResults.hasNext()) {
            QuerySolution qs = instancesResults.next();

            Iterator<String> iter = qs.varNames();
            while (iter.hasNext()) {

                String str = iter.next();
                String val = qs.get(str).toString();
                if (val != null && val.length() > 0) {
                    returnResult = Integer.parseInt(val);
                }

            }
        }

        instancesQueryExecution.close();
        if (IMAPIClass.DEBUG) {
            System.out.println();
            System.out.println("File instances number: " + returnResult);
            System.out.println();
        }
        return returnResult;
    }

    private int retrieveDataFromFile(CidocCrmCompatibleFile cidocLoadFile,
            Hashtable<String, Model> allRetrievedModels,
            SourceDataHolder inputFilesInfo,
            DataMode dataMode,
            Vector<Boolean> canQuickFilteringMethodBeFollowedForeachSequence,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) throws FileNotFoundException {

        String loadFile = cidocLoadFile.getFilePath();
        System.out.println("\n=======================================\n");
        System.out.println("Processing source file: " + cidocLoadFile.getFilePath());

        int instanceUrisFilterStepCount = this.imClass.qWeightsConfig.getQueryFilteringInstancesCount();
        int valueUrisFilterStepCount = this.imClass.qWeightsConfig.getQueryFilteringValuesCount();
        
        StringObject queryPrefixes = new StringObject("");
        StringObject getAllInstances = new StringObject("");
        StringObject countAllInstances = new StringObject("");
        Vector<UserQueryConfiguration> qSequences = this.imClass.userConfig.getUserQueriesCopy();
        boolean allSequencesFast = false;
        if (dataMode == DataMode.TARGET_DATA) {
            if (canQuickFilteringMethodBeFollowedForeachSequence != null && canQuickFilteringMethodBeFollowedForeachSequence.contains(false) == false) {
                allSequencesFast = true;
            }
        }

        Model finalFileModel = this.getSchemaInfoFromFile(cidocLoadFile, allRetrievedModels, queryPrefixes, getAllInstances, countAllInstances, qSequences);
        if (finalFileModel == null) {
            return ApiConstants.IMAPIFailCode;
        }
        OntModel fileModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF, finalFileModel);

        InputStream fileInputStream = FileManager.get().open(loadFile);
        if (fileInputStream == null) {
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Error while trying to load file: " + loadFile);
            return ApiConstants.IMAPIFailCode;
        }
        fileModel = (OntModel) fileModel.read(fileInputStream, null);

        
        
      
        
        
        //First level loop --> Query in steps of some thousand instances this.imClass.qWeightsConfig.getQueryLimitSize() = 4000
        //Second level loop each sequence start from ApiConstants.instanceUrisFilterStepCount = 500 starting instances
        //Perhaps a third level loop is required in case this subset (ApiConstants.instanceUrisFilterStepCount = 500) returns too many results
        //first level loop - loop over instances
        int totalProcessedInstances = 0;
        int totalInstancesIntheFile = getInstancesCardinalityInFile(queryPrefixes, countAllInstances, fileModel);

        if(totalInstancesIntheFile==0){
            return ApiConstants.IMAPISuccessCode;
        }
        
        //first level   -- integer - sequence position
        //second level  -- String parameterName 
        //third level   -- Vector of uris on that parameter name
        Hashtable<Integer,Hashtable<String,Vector<String>>> indexedDataFromInputFiles = new Hashtable<Integer,Hashtable<String,Vector<String>>>();   
        //<editor-fold defaultstate="collapsed" desc="Fill indexedDataFromInputFiles with existing values of the file">
        if(dataMode == DataMode.TARGET_DATA && canQuickFilteringMethodBeFollowedForeachSequence.contains(true)){
            for (int i = 0; i < this.imClass.userConfig.getUserQueriesCopy().size(); i++) {
                boolean canFastMethodBeApplied = canQuickFilteringMethodBeFollowedForeachSequence.get(i);
                if(canFastMethodBeApplied == false){
                    continue;
                }
                
                
                int sequencePosition = this.imClass.userConfig.getUserQueriesCopy().get(i).getPositionID();
                String[] paramNames  = this.imClass.userConfig.getUserQueriesCopy().get(i).getSortedParameterNamesCopy();
                String[] paramTypes  = this.imClass.userConfig.getUserQueriesCopy().get(i).getSortedParameterTypesCopy();
                
                indexedDataFromInputFiles.put(sequencePosition, new Hashtable<String,Vector<String>>());
                
                for(int k=0; k <paramNames.length; k++){
                    String currentParamName = paramNames[k];
                    String currentParamType = paramTypes[k];
                    
                    if(currentParamType.equals(ApiConstants.Type_URI)==false) {
                        if(currentParamType.equals(ApiConstants.Type_Literal) && this.imClass.userConfig.getMinimumLiteralSimilarity() < 1d){
                            continue;
                        }
                    }
                    
                    Vector<String> allValues = inputFilesInfo.collectAllValuesOfSpecificParameter(sequencePosition, currentParamName);
                    
                    Vector<String> existingValues = new Vector<String>();
                    /*
                    int startIndex = 0;
                    int maxIndex = allValues.size();
                    while(startIndex<maxIndex){
                        System.out.println("startIndex: "+startIndex +" out of "+maxIndex + " for "+ currentParamName + " of sequence "  +sequencePosition +" time: "+Utilities.getCurrentTime());
                        Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(startIndex, 50, allValues);
                        startIndex+=currentSubset.size();
                        
                        String baseQuery = queryPrefixes.getString()+ "\n" +
                                "SELECT DISTINCT ?x \n" +
                                "{\n" +
                                "  \n" +
                                ApiConstants.filter_STARTING_URIS_placeHolders +" \n"+
                                "  ?x a ?class . \n" +
                                    
                                
                                "}\n"+
                                "ORDER BY ?x";
                        
                        String query = QueryBuilder.replaceFilteringPlaceHolders(baseQuery, ApiConstants.filter_STARTING_URIS_placeHolders, "x", currentSubset);
                        
                        Query instancesQuery = QueryFactory.create(query);
                        
                        QueryExecution instancesQueryExecution = QueryExecutionFactory.create(instancesQuery, fileModel);
                        com.hp.hpl.jena.query.ResultSet instancesResults = instancesQueryExecution.execSelect();
                        while (instancesResults.hasNext()) {
                            QuerySolution qs = instancesResults.next();
                            String val = qs.get("x").toString();
                            if(existingValues.contains(val)==false){
                                existingValues.add(val);
                            }
                        }
                        
                        instancesQueryExecution.close();
                        
                    }
                    
                    */
                    System.out.println("Checking existence of uris found in source data.");
                    java.io.File f = new java.io.File(loadFile);                    
                    Scanner sc = new Scanner(f);                
                    int lineCounter = 0;
                    int printcounter = 0 ;
                    String line ="";
                    while(sc.hasNextLine()){
                        line +=sc.nextLine() +"\n";
                        lineCounter++;
                        if(line.length() > 10000){
                            printcounter++;
                            if(printcounter%10==0){
                                System.out.print(".");
                            }
                            if(printcounter%1000==0){
                                System.out.println();
                            }
                            
                            for(int j=0; j<allValues.size(); j++){
                                String val = allValues.get(j);
                                if(existingValues.contains(val)==false && line.contains(val)){
                                 existingValues.add(val);
                                }
                            }
                            
                            allValues.removeAll(existingValues);
                            line = "";
                        }
                    }
                    for(int j=0; j<allValues.size(); j++){
                        String val = allValues.get(j);
                        if(existingValues.contains(val)==false && line.contains(val)){
                         existingValues.add(val);
                        }
                    }
                    
                    indexedDataFromInputFiles.get(sequencePosition).put(currentParamName, existingValues);
                }
            }
            System.out.println();    
        }
        
        //</editor-fold>
        
        int firstLevelLoopLIMIT = this.imClass.qWeightsConfig.getQueryLimitSize();
        int firstLevelLoopCounter = firstLevelLoopLIMIT;
        int firstLevelLoopOFFSET = -firstLevelLoopLIMIT;

        while (firstLevelLoopCounter == firstLevelLoopLIMIT) {

            Vector<String> instancesFound = new Vector<String>();
            Hashtable<String, SequencesVector> currentFileInfo = new Hashtable<String, SequencesVector>();

            if (getAllInstances.getString().length() > 0) {

                String query = getAllInstances.getString();
                firstLevelLoopOFFSET += firstLevelLoopLIMIT;
                query += "\n LIMIT " + firstLevelLoopLIMIT;
                if (firstLevelLoopOFFSET > 0) {
                    query += "\n OFFSET " + firstLevelLoopOFFSET;
                }
                firstLevelLoopCounter = 0;

                if (IMAPIClass.DEBUG) {
                    System.out.println("DEBUG: Just before instances retrieval with Offset " + firstLevelLoopOFFSET);
                }

                Query instancesQuery = QueryFactory.create(queryPrefixes.getString() + query);
                QueryExecution instancesQueryExecution = QueryExecutionFactory.create(instancesQuery, fileModel);
                com.hp.hpl.jena.query.ResultSet instancesResults = instancesQueryExecution.execSelect();
                if (IMAPIClass.DEBUG) {
                    System.out.println("DEBUG: Just after query excecution of instances retrieval");
                }

                while (instancesResults.hasNext()) {
                    QuerySolution qs = instancesResults.next();
                    firstLevelLoopCounter++;
                    Iterator<String> iter = qs.varNames();
                    while (iter.hasNext()) {

                        String str = iter.next();
                        String val = qs.get(str).toString();

                        if (currentFileInfo.containsKey(val) == false) {
                            currentFileInfo.put(val, new SequencesVector());
                            instancesFound.add(val);
                        }

                        //<editor-fold defaultstate="collapsed" desc="If target File and 'check as we read' policy is followed then check for similarity now">
                        if (dataMode == DataMode.TARGET_DATA && ApiConstants.checkForSimilaritiesAsWeGetResults) {

                            //CHECK FOR SIMILARITIES
                            Enumeration<String> sourceFilesEnum = inputFilesInfo.keys();
                            while (sourceFilesEnum.hasMoreElements()) {
                                String sourceFile = sourceFilesEnum.nextElement();
                                if (inputFilesInfo.get(sourceFile).containsKey(val)) {

                                    SourceInstancePair pair = new SourceInstancePair(sourceFile, val);
                                    SourceInstancePair newVal = new SourceInstancePair(loadFile, val);

                                    SourceTargetPair newSTPair = new SourceTargetPair(pair, newVal);

                                    SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();

                                    if (pairSimilaritiesInSequences.containsKey(newSTPair) == false) {
                                        pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                                    }
                                }

                            }
                        }
//</editor-fold>

                    }
                }

                instancesQueryExecution.close();
            }

            if (IMAPIClass.DEBUG) {
                System.out.println("DEBUG: instances retrieval ended. Found " + firstLevelLoopCounter + " new instanes.");
            }

            //second level loop through instaces of first loop
            int secondLevelLoopIndex = 0;
            int secondLevelMaxIndex = instancesFound.size();

            while (secondLevelLoopIndex < secondLevelMaxIndex) {

                //all this must get in a loop of instances filtered out
                Vector<String> secondLevelStartingUris = Utilities.collectSequenctiallyAsubsetOfValues(secondLevelLoopIndex, instanceUrisFilterStepCount, instancesFound);
                secondLevelLoopIndex += secondLevelStartingUris.size();
                    
                //third level loop - loop over sequences
                for (int thirdLevelLoopIndex = 0; thirdLevelLoopIndex < qSequences.size(); thirdLevelLoopIndex++) {

                    Hashtable<String, Vector<String>> uriValuesToStartingUris = new Hashtable<String, Vector<String>>();
                    UserQueryConfiguration currentSequence = qSequences.get(thirdLevelLoopIndex);
                    int sequencePosition = currentSequence.getPositionID();
                    boolean canFastMethodBeApplied = false;
                    if (dataMode == DataMode.TARGET_DATA && canQuickFilteringMethodBeFollowedForeachSequence != null) {
                        canFastMethodBeApplied = canQuickFilteringMethodBeFollowedForeachSequence.get(thirdLevelLoopIndex);
                    }

                    String[] parameterNames = currentSequence.getSortedParameterNamesCopy();
                    String[] parameterTypes = currentSequence.getSortedParameterTypesCopy();
                    String[] stepQueries = currentSequence.getSortedQueriesCopy();

                    Vector<String> startingUrisForStep = new Vector<String>();
                    //forth level loop - loop over the steps of each sequence
                    for (int forthLevelLoopStepIndex = 0; forthLevelLoopStepIndex < stepQueries.length; forthLevelLoopStepIndex++) {

                        String currentQuery = stepQueries[forthLevelLoopStepIndex];
                        String currentParamName = parameterNames[forthLevelLoopStepIndex];
                        String currentParamType = parameterTypes[forthLevelLoopStepIndex];
                        Vector<String> stepValues = new Vector<String>();
                        
                        if(forthLevelLoopStepIndex==0){
                            startingUrisForStep.addAll(secondLevelStartingUris);
                        }
                        Vector<String> nextStepStartingUris = new Vector<String>();
                        if(startingUrisForStep.size()==0){
                            break;
                        }

                        
                        int fifthLevelLoopIndex = 0;
                        int fifthLevelMaxIndex = startingUrisForStep.size();
                        while(fifthLevelLoopIndex<fifthLevelMaxIndex){
                            
                            Vector<String> currentStepSubsetOfStartingUris = Utilities.collectSequenctiallyAsubsetOfValues(fifthLevelLoopIndex, instanceUrisFilterStepCount, startingUrisForStep);
                            fifthLevelLoopIndex+=currentStepSubsetOfStartingUris.size();
                            String fifthLevelQuery  = QueryBuilder.replaceFilteringPlaceHolders(currentQuery, ApiConstants.filter_STARTING_URIS_placeHolders, ApiConstants.startingParameterName, currentStepSubsetOfStartingUris);
                            
                            //sixth level loop may be applied here
                            boolean valuesFilteringLoopExecuted = false;
                            boolean performValueFiltering = false;
                            int sixthLevelLoopStartIndex = -1;
                            int sixthLevelLoopMaxIndex = -1;
                            if (canFastMethodBeApplied) {
                                //perform a loop over values                                                      
                                performValueFiltering = true;
                                stepValues.addAll(indexedDataFromInputFiles.get(sequencePosition).get(currentParamName));
                                sixthLevelLoopStartIndex= 0;
                                sixthLevelLoopMaxIndex = stepValues.size();  
                            }
                            
                            while(valuesFilteringLoopExecuted==false || (sixthLevelLoopMaxIndex>0 && sixthLevelLoopStartIndex<sixthLevelLoopMaxIndex) ){
                        
                        
                                valuesFilteringLoopExecuted = true;

                                String sixthLevelQuery = fifthLevelQuery;
                                if(performValueFiltering){
                                    Vector<String> uriValues = Utilities.collectSequenctiallyAsubsetOfValues(sixthLevelLoopStartIndex, valueUrisFilterStepCount, stepValues);
                                    sixthLevelLoopStartIndex += uriValues.size();
                                    sixthLevelQuery = QueryBuilder.replaceFilteringPlaceHolders(fifthLevelQuery, ApiConstants.filter_VALUES_placeHolder, currentParamName, uriValues);                                
                                }

                                //seventh level loop limit and offset must be applied
                                int seventhLevelLoopLIMIT = this.imClass.qWeightsConfig.getQueryLimitSize();
                                int seventhLevelLoopCounter = seventhLevelLoopLIMIT;
                                int seventhLevelLoopOFFSET = -seventhLevelLoopLIMIT;

                                while (seventhLevelLoopCounter == seventhLevelLoopLIMIT) {
                        
                            
                                    String seventhLevelQuery = sixthLevelQuery;
                                    seventhLevelLoopOFFSET += seventhLevelLoopLIMIT;
                                    seventhLevelQuery += "\n LIMIT " + seventhLevelLoopLIMIT;
                                    if (seventhLevelLoopOFFSET > 0) {
                                        seventhLevelQuery += "\n OFFSET " + seventhLevelLoopOFFSET;
                                        if (IMAPIClass.DEBUG) {
                                            System.out.println("7th level with offset " + seventhLevelLoopOFFSET + " seq: " + sequencePosition + " step: " + (forthLevelLoopStepIndex + 1) + " time: " +Utilities.getCurrentTime());
                                            /*
                                            if(forthLevelLoopStepIndex==0 && seventhLevelLoopOFFSET>=80000){
                                                System.out.println(currentStepSubsetOfStartingUris.toString().replace("[", "").replace("]", "").replaceAll(", ", "\n "));
                                            }*/
                                        }
                                    }
                                    seventhLevelLoopCounter = 0;
                                    

                                Query predicateQuery = QueryFactory.create(queryPrefixes.getString() + seventhLevelQuery);
                                QueryExecution instancesQueryExecution = QueryExecutionFactory.create(predicateQuery, fileModel);
                                com.hp.hpl.jena.query.ResultSet predicateResults = instancesQueryExecution.execSelect();

                                while (predicateResults.hasNext()) {

                                    seventhLevelLoopCounter++;

                                    QuerySolution qs = predicateResults.next();

                                    String startingUri = "";
                                    String value = "";
                                    String lang = "";

                                    //<editor-fold defaultstate="collapsed" desc="Get Starting Instance">
                                    if (qs.contains(ApiConstants.startingParameterName)) {
                                        RDFNode startingUriNode = qs.get(ApiConstants.startingParameterName);
                                        if (startingUriNode != null) {
                                            startingUri = startingUriNode.toString();
                                        }
                                    } else {
                                        System.out.println("DEBUG: No starting instance returned. Check Query Prototypes for sequernce " + currentSequence.getMnemonic() + " step: " + parameterNames[forthLevelLoopStepIndex]);
                                    }
                                //</editor-fold>

                                    //<editor-fold defaultstate="collapsed" desc="Get step output value and lang">
                                    if (qs.contains(currentParamName)) {
                                        RDFNode valueNode = qs.get(currentParamName);
                                        if (valueNode != null) {
                                            if (valueNode.isLiteral()) {
                                                value = valueNode.asLiteral().getString();
                                                lang = valueNode.asLiteral().getLanguage();
                                            } else {
                                                value = valueNode.toString();
                                            }
                                        }
                                    } else {
                                        System.out.println("DEBUG: No value returned. Check Query Prototypes for sequernce " + currentSequence.getMnemonic() + " step: " + parameterNames[forthLevelLoopStepIndex]);
                                    }
                                //</editor-fold>

                                    //in case one wants to print this info
                                    String resultLine = "\t" + startingUri + "\t" + currentParamName + ": " + value + " " + lang;
                                    //System.out.println(resultLine);

                                    //PREPARE NEXT STARTING URIS
                                    if (currentParamType.equals(ApiConstants.Type_URI)) {
                                        if (nextStepStartingUris.contains(value) == false) {
                                            nextStepStartingUris.add(value);
                                        }
                                    }

                                    handlePairOf_StartingUri_and_Value(forthLevelLoopStepIndex, startingUri, value, lang, 
                                            currentSequence, currentFileInfo, currentParamName, currentParamType, uriValuesToStartingUris, 
                                            dataMode, loadFile, inputFilesInfo, pairSimilaritiesInSequences, canFastMethodBeApplied);
                                    /*
                                        handlePairOf_StartingUri_and_Value(forthLevelLoopStepIndex, startingUri, value, lang, 
                                                currentSequence, currentFileInfo, currentParamName, currentParamType, uriValuesToStartingUris, 
                                                dataMode, loadFile, inputFilesInfo, pairSimilaritiesInSequences, canFastMethodBeApplied);
                                        
                                    handlePairOf_StartingUri_and_Value(forthLevelLoopStepIndex, startingUri, value, lang, 
                                            currentSequence, currentFileInfo, currentParamName, currentParamType, uriValuesToStartingUris, 
                                            dataMode, loadFile, inputFilesInfo, pairSimilaritiesInSequences, canFastMethodBeApplied);
*/
                                    }
                                
                                    instancesQueryExecution.close();

                                }//end of seventh level loop over the set of values (Limit / offset)


                                
                                //RETEST CONDITION AND BREAK WHAT?
                                //if no instances found then do not continue
                                /*
                                if (currentParamType.equals(ApiConstants.Type_URI) &&  nextStepStartingUris.size() == 0) {
                                    break;
                                }                         
                                */

                            }//end of potentially sixth level loop that will filter over values if fast method can be applied to the whole sequence
                            
                            
                        }// end of fifth level loop over starting uris of each step
                        
                        startingUrisForStep = new Vector<String>(nextStepStartingUris);
                    }//end of forth level loop -- steps
                
                }//end of third level loop over sequences
                
                //if (IMAPIClass.DEBUG) {
                    double percentage =((double) (secondLevelLoopIndex + totalProcessedInstances) * 100d) / (double) totalInstancesIntheFile;
                    System.out.println("\tProcessed next " + secondLevelLoopIndex + " values. Percentage: " + Utilities.df.format(percentage) + "% at time: " + Utilities.getCurrentTime());
                //}

            }//end of second level loop over subpack of filtering instances
            
            //if (IMAPIClass.DEBUG) {
                totalProcessedInstances += firstLevelLoopCounter;
                double percentage = ((double) totalProcessedInstances * 100d) / (double) totalInstancesIntheFile;
                System.out.println("Processed " + totalProcessedInstances + " instances. Total number of instaces in file: " + totalInstancesIntheFile + ". File percentage: " + Utilities.df.format(percentage) + "% at time: " + Utilities.getCurrentTime());
            //}

            if (dataMode == DataMode.SOURCE_DATA) {
                if (inputFilesInfo.containsKey(loadFile) == false) {
                    inputFilesInfo.put(loadFile, currentFileInfo);
                } else {
                    //currentFileInfo will only contain new keys
                    inputFilesInfo.get(loadFile).putAll(currentFileInfo);
                }
            }

        }//end of first level loop over instances of file

        return ApiConstants.IMAPISuccessCode;
    }
    
    int retrieveDataFrom_SourceFile(CidocCrmCompatibleFile cidocLoadFile,
            Hashtable<String, Model> allRetrievedModels,
            SourceDataHolder inputFilesInfo) throws FileNotFoundException {
        return retrieveDataFromFile(cidocLoadFile, allRetrievedModels, inputFilesInfo, DataMode.SOURCE_DATA, null, null);
    }

    int retrieveDataFrom_TargetFileAndCollectSimilarities(CidocCrmCompatibleFile cidocLoadFile,
            Hashtable<String, Model> allRetrievedModels,
            SourceDataHolder inputFilesInfo,
            Vector<Boolean> canQuickFilteringMethodBeFollowedForeachSequence,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) throws FileNotFoundException {

        return retrieveDataFromFile(cidocLoadFile, allRetrievedModels, inputFilesInfo, DataMode.TARGET_DATA, canQuickFilteringMethodBeFollowedForeachSequence, pairSimilaritiesInSequences);        
    }

    int retrieveDataFrom_OnlineDatabaseAndCollectSimilarities(OnlineDatabase db,
            Hashtable<String, Model> allRetrievedModels,
            SourceDataHolder inputSourceInfo,
            Vector<Boolean> canQuickFilteringMethodBeFollowedForeachSequence,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) {

        QueryBuilder qBuilder = new QueryBuilder(this.imClass);
        
        StringObject queryPrefixes = new StringObject("");
        StringObject getAllInstances = new StringObject("");
        StringObject countAllInstances = new StringObject("");
        Vector<UserQueryConfiguration> qSequences = this.imClass.userConfig.getUserQueriesCopy();        
        boolean allSequencesFast = false;        
        if (canQuickFilteringMethodBeFollowedForeachSequence != null && canQuickFilteringMethodBeFollowedForeachSequence.contains(false) == false) {
            allSequencesFast = true;
        }
        int instanceUrisFilterStepCount = this.imClass.qWeightsConfig.getQueryFilteringInstancesCount();
        int valueUrisFilterStepCount = this.imClass.qWeightsConfig.getQueryFilteringValuesCount();
        
        
        //<editor-fold defaultstate="collapsed" desc="prepare queries">
        Hashtable<String, String> dbNs = db.getDbNamesapcesCopy();

        //get available list of cidoc namepsaces and get another structure
        //that will be filled with the subset of the above that is declared for this db
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();
        Hashtable<String, String> validCrmNsUsed = new Hashtable<String, String>();

        
        Vector<String> nKeys = new Vector<String>(dbNs.keySet());

        for (int m = 0; m < nKeys.size(); m++) {
            String keyStr = nKeys.get(m);
            String fileNs = dbNs.get(keyStr);

            queryPrefixes.setString(queryPrefixes.getString()+"PREFIX " + keyStr + ": <" + fileNs + ">\n");
            if (validCrmNs.contains(fileNs)) {
                validCrmNsUsed.put(keyStr, fileNs);
            }
        }

        queryPrefixes.setString(queryPrefixes.getString()+"\n");

        

        //prepare the queries --> retrieve from model the class and predicate names
        Model prepareQueriesModel = ModelFactory.createDefaultModel();
        Enumeration<String> validCrmEnum = validCrmNsUsed.keys();
        while (validCrmEnum.hasMoreElements()) {
            Model loopModel = allRetrievedModels.get(validCrmNsUsed.get(validCrmEnum.nextElement()));
            prepareQueriesModel = prepareQueriesModel.union(loopModel);
        }

        int ret = qBuilder.prepareQueries(validCrmNsUsed, prepareQueriesModel, this.imClass.userConfig.getComparisonMode(), db.getTargetDatabasePredicateDirectionUsage(), getAllInstances, countAllInstances, qSequences);
        if (ret != ApiConstants.IMAPISuccessCode) {
            return ret;
        }
        //</editor-fold>
        
        
        OnlineDatabaseActions qSource = new OnlineDatabaseActions(this.imClass, db);

        long totalProcessedInstances = 0;
        long totalInstancesIntheFile = qSource.getDatabaseInstancesCount(queryPrefixes, countAllInstances);
        

        System.out.println("Database instances number: " + totalInstancesIntheFile);
        if(totalInstancesIntheFile==0){
            return ApiConstants.IMAPISuccessCode;
        }
        
        //<editor-fold defaultstate="collapsed" desc="Fill indexedDataFromInputFiles with existing values of the file">
        //first level   -- integer - sequence position
        //second level  -- String parameterName 
        //third level   -- Vector of uris on that parameter name
        Hashtable<Integer,Hashtable<String,Vector<String>>> indexedDataFromInputFiles = new Hashtable<Integer,Hashtable<String,Vector<String>>>();           
        
        if(canQuickFilteringMethodBeFollowedForeachSequence.contains(true)){
            System.out.println("Checking existence of uris found in source data.");
        }
        for (int i = 0; i < this.imClass.userConfig.getUserQueriesCopy().size(); i++) {
                boolean canFastMethodBeApplied = canQuickFilteringMethodBeFollowedForeachSequence.get(i);
                if(canFastMethodBeApplied == false){
                    continue;
                }
                
                
                int sequencePosition = this.imClass.userConfig.getUserQueriesCopy().get(i).getPositionID();
                String[] paramNames  = this.imClass.userConfig.getUserQueriesCopy().get(i).getSortedParameterNamesCopy();
                String[] paramTypes  = this.imClass.userConfig.getUserQueriesCopy().get(i).getSortedParameterTypesCopy();
                
                indexedDataFromInputFiles.put(sequencePosition, new Hashtable<String,Vector<String>>());
                
                for(int k=0; k <paramNames.length; k++){
                    String currentParamName = paramNames[k];
                    String currentParamType = paramTypes[k];
                    
                    if(currentParamType.equals(ApiConstants.Type_URI)==false) {
                        if(currentParamType.equals(ApiConstants.Type_Literal) && this.imClass.userConfig.getMinimumLiteralSimilarity() < 1d){
                            continue;
                        }
                    }
                    
                    Vector<String> allValues = inputSourceInfo.collectAllValuesOfSpecificParameter(sequencePosition, currentParamName);                    
                    Vector<String> existingValues = new Vector<String>();
                    
                    int startIndex = 0;
                    int maxIndex = allValues.size();
                    int printcounter = 0 ;
                    while(startIndex<maxIndex){
                        printcounter++;
                        System.out.print(".");
                        if(printcounter%10==0){
                            System.out.println();
                        }                        
                        
                        //System.out.println("startIndex: "+startIndex +" out of "+maxIndex + " for "+ currentParamName + " of sequence "  +sequencePosition +" time: "+Utilities.getCurrentTime());
                        Vector<String> currentSubset = Utilities.collectSequenctiallyAsubsetOfValues(startIndex, instanceUrisFilterStepCount, allValues);
                        startIndex+=currentSubset.size();
                        
                        String baseQuery = queryPrefixes.getString()+ "\n" +
                                "SELECT DISTINCT ?x \n" +
                                "{\n" +
                                "  \n" +
                                ApiConstants.filter_STARTING_URIS_placeHolders +" \n"+
                                "  ?x ?pred ?class . \n" +
                                "}\n"+
                                "ORDER BY ?x ";
                        
                        String query = QueryBuilder.replaceFilteringPlaceHolders(baseQuery, ApiConstants.filter_STARTING_URIS_placeHolders, "x", currentSubset);
                        
                        ret = qSource.getUris(query, existingValues);
                        if(ret!=ApiConstants.IMAPISuccessCode){
                            return ret;
                        }
                        
                    }
                    System.out.println();
                    
                    indexedDataFromInputFiles.get(sequencePosition).put(currentParamName, existingValues);
                }
        }
        //</editor-fold>
        
        
        
        //First level loop --> Query in steps of some thousand instances this.imClass.qWeightsConfig.getQueryLimitSize() = 4000
        //Second level loop each sequence start from ApiConstants.instanceUrisFilterStepCount = 500 starting instances
        //Perhaps a third level loop is required in case this subset (ApiConstants.instanceUrisFilterStepCount = 500) returns too many results
        //first level loop - loop over instances
        int firstLevelLoopLIMIT = this.imClass.qWeightsConfig.getQueryLimitSize();
        int firstLevelLoopCounter = firstLevelLoopLIMIT;
        int firstLevelLoopOFFSET = -firstLevelLoopLIMIT;

        while (firstLevelLoopCounter == firstLevelLoopLIMIT) {

            Vector<String> instancesFound = new Vector<String>();
            Hashtable<String, SequencesVector> currentFileInfo = new Hashtable<String, SequencesVector>();
            //Hashtable<SourceTargetPair, SequenceSimilarityResultVector> firtsiLoopPairSimilaritiesInSequences = new Hashtable<SourceTargetPair,SequenceSimilarityResultVector>();

            if (getAllInstances.getString().length() > 0) {

                String query = getAllInstances.getString();
                firstLevelLoopOFFSET += firstLevelLoopLIMIT;
                query += "\n LIMIT " + firstLevelLoopLIMIT;
                if (firstLevelLoopOFFSET > 0) {
                    query += "\n OFFSET " + firstLevelLoopOFFSET;
                }
                firstLevelLoopCounter = 0;

                if (IMAPIClass.DEBUG) {
                    System.out.println("DEBUG: Just before instances retrieval with Offset " + firstLevelLoopOFFSET);
                }

                String instancesQuery = queryPrefixes.getString() + query;
                ret = qSource.getUris(instancesQuery, instancesFound);
                if(ret!=ApiConstants.IMAPISuccessCode){
                    return ret;
                }
                /*
                if(IMAPIClass.DEBUG){
                    instancesFound= new Vector<String>();
                    instancesFound.add("http://collection.britishmuseum.org/id/person-institution/62763");
                }
                */
                firstLevelLoopCounter+=instancesFound.size();
                for(int m=0; m <instancesFound.size();m++){
                    
                    String val = instancesFound.get(m);
                    
                    if (currentFileInfo.containsKey(val) == false) {
                        currentFileInfo.put(val, new SequencesVector());
                    }
                    
                    //<editor-fold defaultstate="collapsed" desc="If target File and 'check as we read' policy is followed then check for similarity now">
                    if (ApiConstants.checkForSimilaritiesAsWeGetResults) {

                        //CHECK FOR SIMILARITIES
                        Enumeration<String> sourceFilesEnum = inputSourceInfo.keys();
                        while (sourceFilesEnum.hasMoreElements()) {
                            String sourceFile = sourceFilesEnum.nextElement();
                            if (inputSourceInfo.get(sourceFile).containsKey(val)) {

                                SourceInstancePair pair = new SourceInstancePair(sourceFile, val);
                                SourceInstancePair newVal = new SourceInstancePair(db.getDBChoice().toString(), val);

                                SourceTargetPair newSTPair = new SourceTargetPair(pair, newVal);

                                SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();

                                if (pairSimilaritiesInSequences.containsKey(newSTPair) == false) {
                                    pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                                }
                            }

                        }
                    }
                    //</editor-fold>
                }
                
            }

            if (IMAPIClass.DEBUG) {
                System.out.println("DEBUG: instances retrieval ended. Found " + firstLevelLoopCounter + " new instanes.");
            }

            
            
            //second level loop through instaces of first loop
            int secondLevelLoopIndex = 0;
            int secondLevelMaxIndex = instancesFound.size();

            while (secondLevelLoopIndex < secondLevelMaxIndex) {

                //all this must get in a loop of instances filtered out                
                Vector<String> secondLevelStartingUris = Utilities.collectSequenctiallyAsubsetOfValues(secondLevelLoopIndex, instanceUrisFilterStepCount, instancesFound);
                secondLevelLoopIndex += secondLevelStartingUris.size();

                    
                //third level loop - loop over sequences
                for (int thirdLevelLoopIndex = 0; thirdLevelLoopIndex < qSequences.size(); thirdLevelLoopIndex++) {

                    Hashtable<String, Vector<String>> uriValuesToStartingUris = new Hashtable<String, Vector<String>>();
                    UserQueryConfiguration currentSequence = qSequences.get(thirdLevelLoopIndex);
                    int sequencePosition = currentSequence.getPositionID();
                    boolean canFastMethodBeApplied = canQuickFilteringMethodBeFollowedForeachSequence.get(thirdLevelLoopIndex);
                

                    String[] parameterNames = currentSequence.getSortedParameterNamesCopy();
                    String[] parameterTypes = currentSequence.getSortedParameterTypesCopy();
                    String[] stepQueries = currentSequence.getSortedQueriesCopy();

                    Vector<String> startingUrisForStep = new Vector<String>();
                    //forth level loop - loop over the steps of each sequence
                    for (int forthLevelLoopStepIndex = 0; forthLevelLoopStepIndex < stepQueries.length; forthLevelLoopStepIndex++) {

                        String currentQuery = stepQueries[forthLevelLoopStepIndex];
                        String currentParamName = parameterNames[forthLevelLoopStepIndex];
                        
                        String currentParamType = parameterTypes[forthLevelLoopStepIndex];
                        String nextParamType = "";
                        if((forthLevelLoopStepIndex+1)<(parameterTypes.length)){
                            nextParamType = parameterTypes[(forthLevelLoopStepIndex+1)];
                        }
                        Vector<String> stepValues = new Vector<String>();
                        
                        if(forthLevelLoopStepIndex==0){
                            startingUrisForStep.addAll(secondLevelStartingUris);
                        }
                        Vector<String> nextStepStartingUris = new Vector<String>();
                        if(IMAPIClass.DEBUG){
                            System.out.println("\t\t\tRetrieving "+currentParamName+". Starting uris for this step: " + startingUrisForStep.size());
                        }
                        if(startingUrisForStep.size()==0){
                            break;
                        }
                        
                        int fifthLevelLoopIndex = 0;
                        int fifthLevelMaxIndex = startingUrisForStep.size();
                        while(fifthLevelLoopIndex<fifthLevelMaxIndex){
                            
                            Vector<String> currentStepSubsetOfStartingUris = Utilities.collectSequenctiallyAsubsetOfValues(fifthLevelLoopIndex, instanceUrisFilterStepCount, startingUrisForStep);
                            fifthLevelLoopIndex+=currentStepSubsetOfStartingUris.size();
                            String fifthLevelQuery  = QueryBuilder.replaceFilteringPlaceHolders(currentQuery, ApiConstants.filter_STARTING_URIS_placeHolders, ApiConstants.startingParameterName, currentStepSubsetOfStartingUris);
                            
                            //sixth level loop may be applied here
                            boolean valuesFilteringLoopExecuted = false;
                            boolean performValueFiltering = false;
                            int sixthLevelLoopStartIndex = -1;
                            int sixthLevelLoopMaxIndex = -1;
                            if (canFastMethodBeApplied) {
                                //perform a loop over values                                                      
                                performValueFiltering = true;
                                stepValues.addAll(indexedDataFromInputFiles.get(sequencePosition).get(currentParamName));
                                sixthLevelLoopStartIndex= 0;
                                sixthLevelLoopMaxIndex = stepValues.size();  
                            }
                            
                            while(valuesFilteringLoopExecuted==false || (sixthLevelLoopMaxIndex>0 && sixthLevelLoopStartIndex<sixthLevelLoopMaxIndex) ){
                            
                                valuesFilteringLoopExecuted = true;

                                String sixthLevelQuery = fifthLevelQuery;
                                if(performValueFiltering){
                                    Vector<String> uriValues = Utilities.collectSequenctiallyAsubsetOfValues(sixthLevelLoopStartIndex, valueUrisFilterStepCount, stepValues);
                                    sixthLevelLoopStartIndex += uriValues.size();
                                    sixthLevelQuery = QueryBuilder.replaceFilteringPlaceHolders(fifthLevelQuery, ApiConstants.filter_VALUES_placeHolder, currentParamName, uriValues);                                
                                }

                                //seventh level loop limit and offset must be applied
                                int seventhLevelLoopLIMIT = this.imClass.qWeightsConfig.getQueryLimitSize();
                                int seventhLevelLoopCounter = seventhLevelLoopLIMIT;
                                int seventhLevelLoopOFFSET = -seventhLevelLoopLIMIT;

                                while (seventhLevelLoopCounter == seventhLevelLoopLIMIT) {

                                    String seventhLevelQuery = sixthLevelQuery;
                                    seventhLevelLoopOFFSET += seventhLevelLoopLIMIT;
                                    seventhLevelQuery += "\n LIMIT " + seventhLevelLoopLIMIT;
                                    if (seventhLevelLoopOFFSET > 0) {
                                        seventhLevelQuery += "\n OFFSET " + seventhLevelLoopOFFSET;
                                        if (IMAPIClass.DEBUG) {
                                            System.out.println("7th level with offset " + seventhLevelLoopOFFSET + " seq: " + sequencePosition + " step: " + (forthLevelLoopStepIndex + 1) + " time: " +Utilities.getCurrentTime());
                                            /*
                                            if(forthLevelLoopStepIndex==0 && seventhLevelLoopOFFSET>=80000){
                                                System.out.println(currentStepSubsetOfStartingUris.toString().replace("[", "").replace("]", "").replaceAll(", ", "\n "));
                                            }*/
                                        }
                                    }
                                    seventhLevelLoopCounter = 0;

                                    String seventhLevelQueryForDb = queryPrefixes.getString() + seventhLevelQuery;
                                    
                                    Vector<DataRecord[]> stepVals = new Vector<DataRecord[]>();
                                    if(IMAPIClass.DEBUG){
                                        //System.out.println("\t\t\t\tPerforming database query. fifthLevelLoopIndex: "+fifthLevelLoopIndex+" seventhLevelLoopOFFSET " +seventhLevelLoopOFFSET+" time: " +Utilities.getCurrentTime() );
                                    }
                                    ret = qSource.getUriPairs(seventhLevelQueryForDb, ApiConstants.startingParameterName, currentParamName,currentParamType, stepVals);
                                    if(ret!=ApiConstants.IMAPISuccessCode){
                                        return ret;
                                    }
                                    seventhLevelLoopCounter+=stepVals.size();
                                    for(int tempIndex =0; tempIndex< stepVals.size(); tempIndex++){

                                        String startingUri = "";
                                        String value = "";
                                        String lang = "";
                                        
                                        DataRecord[] currentRec = stepVals.get(tempIndex);
                                        if(currentRec==null || currentRec.length!=2){
                                            if(IMAPIClass.DEBUG){
                                                System.out.println("Propably an error exists in getUriPairs method. Returned a null value or an array with length !=2.");
                                            }
                                            return ApiConstants.IMAPIFailCode;
                                        }
                                        

                                        
                                        startingUri = currentRec[0].getValue();
                                        value = currentRec[1].getValue();
                                        lang = currentRec[1].getLang();

                                       
                                        //PREPARE NEXT STARTING URIS
                                        if (currentParamType.equals(ApiConstants.Type_URI)) {
                                            if (nextStepStartingUris.contains(value) == false) {
                                                nextStepStartingUris.add(value);
                                            }
                                        }

                                        handlePairOf_StartingUri_and_Value(forthLevelLoopStepIndex, startingUri, value, lang, 
                                                currentSequence, currentFileInfo, currentParamName, currentParamType, uriValuesToStartingUris, 
                                                DataMode.TARGET_DATA, db.getDBChoice().toString(), inputSourceInfo, pairSimilaritiesInSequences, canFastMethodBeApplied);

                                    }

                                }//end of seventh level loop over the set of values (Limit / offset)


                                
                                //RETEST CONDITION AND BREAK WHAT?
                                //if no instances found then do not continue
                                /*
                                if (currentParamType.equals(ApiConstants.Type_URI) &&  nextStepStartingUris.size() == 0) {
                                    break;
                                }                         
                                */

                            }//end of potentially sixth level loop that will filter over values if fast method can be applied to the whole sequence
                            
                            
                        }// end of fifth level loop over starting uris of each step
                        
                        startingUrisForStep = new Vector<String>(nextStepStartingUris);
                        
                        
                    }//end of forth level loop over steps

                }//end of third level loop over sequences
                
                //if (IMAPIClass.DEBUG) {
                    double percentage =((double) (secondLevelLoopIndex + totalProcessedInstances) * 100d) / (double) totalInstancesIntheFile;
                    System.out.println("\tProcessed next " + secondLevelLoopIndex + " values. Percentage: " + Utilities.df.format(percentage) + "% at time: " + Utilities.getCurrentTime());
                //}
            }//end of second level loop over subpack of filtering instances
            
            //if (IMAPIClass.DEBUG) {
                totalProcessedInstances += firstLevelLoopCounter;
                double percentage = ((double) totalProcessedInstances * 100d) / (double) totalInstancesIntheFile;
                System.out.println("Processed " + totalProcessedInstances + " instances. Total number of instaces in "+db.getDbName()+": " + totalInstancesIntheFile + ". Percentage: " + Utilities.df.format(percentage) + "% at time: " + Utilities.getCurrentTime());
            //}

            //pairSimilaritiesInSequences.putAll(firtsiLoopPairSimilaritiesInSequences);
        }//end of first level loop over instances of file
        
        

        return ApiConstants.IMAPISuccessCode;
    }
    
    //may be improved for speed
    private void handlePairOf_StartingUri_and_Value(int queryStepIndex, String startingUri, String value, String lang,
            UserQueryConfiguration currentSequence, 
            Hashtable<String, SequencesVector> currentFileInfo,
            String currentParamName, 
            String currentParamType,            
            Hashtable<String, Vector<String>> uriValuesToStartingUris,
            DataMode dataMode,
            String sourceName,
            SourceDataHolder inputFilesInfo,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences,
            boolean canFastMethodBeApplied) {

        if (dataMode == DataMode.SOURCE_DATA || (dataMode == DataMode.TARGET_DATA && ApiConstants.checkForSimilaritiesAsWeGetResults == false) ) {
            
            if (queryStepIndex == 0) {
                
                currentFileInfo.get(startingUri).addValueToSequence(currentSequence, currentParamName, value, lang);

                if (currentParamType.equals(ApiConstants.Type_URI)) {
                    if (uriValuesToStartingUris.containsKey(value)) {
                        if (uriValuesToStartingUris.get(value).contains(startingUri) == false) {
                            uriValuesToStartingUris.get(value).add(startingUri);
                        }
                    } else {
                        Vector<String> vals = new Vector<String>();
                        vals.add(startingUri);
                        uriValuesToStartingUris.put(value, vals);
                    }
                }

            } else {

                //get starting uris of
                Vector<String> initiaInstances = uriValuesToStartingUris.get(startingUri);

                if (currentParamType.equals(ApiConstants.Type_URI)) {

                    if (uriValuesToStartingUris.containsKey(value)) {

                        if (initiaInstances != null && initiaInstances.size() > 0) {
                            for (int mergeIndex = 0; mergeIndex < initiaInstances.size(); mergeIndex++) {
                                if (uriValuesToStartingUris.get(value).contains(initiaInstances.get(mergeIndex)) == false) {
                                    uriValuesToStartingUris.get(value).add(initiaInstances.get(mergeIndex));
                                }
                            }
                        }
                    } else {
                        if (initiaInstances != null && initiaInstances.size() > 0) {
                            uriValuesToStartingUris.put(value, initiaInstances);
                        }
                    }

                    initiaInstances = uriValuesToStartingUris.get(value);

                }

                if (initiaInstances != null && initiaInstances.size() > 0) {
                    for (int mergeIndex = 0; mergeIndex < initiaInstances.size(); mergeIndex++) {
                        //SourceInstancePair searchVal = new SourceInstancePair(sourceName, initiaInstances.get(mergeIndex));
                        currentFileInfo.get(initiaInstances.get(mergeIndex)).addValueToSequence(currentSequence, currentParamName, value, lang);
                    }
                }
            }
        } else {
            
            
            

            if (queryStepIndex == 0) {

                SourceInstancePair searchVal = new SourceInstancePair(sourceName, startingUri);
                currentFileInfo.get(startingUri).addValueToSequence(currentSequence, currentParamName, value, lang);

                //keep track of values with related instances
                if (currentParamType.equals(ApiConstants.Type_URI)) {
                    if (uriValuesToStartingUris.containsKey(value)) {
                        if (uriValuesToStartingUris.get(value).contains(startingUri) == false) {
                            uriValuesToStartingUris.get(value).add(startingUri);
                        }
                    } else {
                        Vector<String> vals = new Vector<String>();
                        vals.add(startingUri);
                        uriValuesToStartingUris.put(value, vals);
                    }
                }
                DataRecord checkRecord = new DataRecord(value, lang);

                Enumeration<String> filesEnum = inputFilesInfo.keys();
                while (filesEnum.hasMoreElements()) {
                    String file = filesEnum.nextElement();
                    Hashtable<String, SequencesVector> fileinfo = inputFilesInfo.get(file);

                    Enumeration<String> instanceEnum = fileinfo.keys();
                    while (instanceEnum.hasMoreElements()) {
                        String sourceuri = instanceEnum.nextElement();
                        SequencesVector pairdata = fileinfo.get(sourceuri);

                        SequenceData seqData = pairdata.getSequenceDataAtPosition(currentSequence.getPositionID());

                        if (seqData == null) {
                            continue;
                        }

                        Vector<DataRecord> sourceVals = seqData.getValuesOfKey(currentParamName);
                        if (sourceVals == null || sourceVals.size() == 0) {
                            continue;
                        }

                        if (canFastMethodBeApplied && sourceVals.contains(checkRecord) == false) {
                            continue;
                        }

                        DataRecord srcCompareRecord = new DataRecord("", "");

                        Double tempComparisonResult = this.comp.compareValueAgainstVector(currentParamType, checkRecord, sourceVals, srcCompareRecord);
                        if (tempComparisonResult > 0d) {

                            SourceInstancePair pair = new SourceInstancePair(file, sourceuri);
                            SourceTargetPair newSTPair = new SourceTargetPair(pair, searchVal);

                            SequenceSimilarityResult newSimResult = new SequenceSimilarityResult(currentSequence.getPositionID(), seqData.getSchemaInfo().getMnemonic(), seqData.getSchemaInfo().getWeight());
                            newSimResult.setNewSimilarityResult(currentParamName, currentParamType, srcCompareRecord, checkRecord, tempComparisonResult);

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

            } else {

                //start - next pairs --> next is connected with other uris?
                Vector<String> initiaInstances = uriValuesToStartingUris.get(startingUri);
                if (currentParamType.equals(ApiConstants.Type_URI)) {

                    if (uriValuesToStartingUris.containsKey(value)) {

                        if (initiaInstances != null && initiaInstances.size() > 0) {
                            for (int mergeIndex = 0; mergeIndex < initiaInstances.size(); mergeIndex++) {
                                if (uriValuesToStartingUris.get(value).contains(initiaInstances.get(mergeIndex)) == false) {
                                    uriValuesToStartingUris.get(value).add(initiaInstances.get(mergeIndex));
                                }
                            }
                        }
                    } else {
                        if (initiaInstances != null && initiaInstances.size() > 0) {
                            uriValuesToStartingUris.put(value, initiaInstances);
                        }
                    }

                    initiaInstances = uriValuesToStartingUris.get(value);

                }

                if (initiaInstances != null && initiaInstances.size() > 0) {
                    for (int mergeIndex = 0; mergeIndex < initiaInstances.size(); mergeIndex++) {
                        SourceInstancePair searchVal = new SourceInstancePair(sourceName, initiaInstances.get(mergeIndex));
                        currentFileInfo.get(initiaInstances.get(mergeIndex)).addValueToSequence(currentSequence, currentParamName, value, lang);

                        DataRecord checkRecord = new DataRecord(value, lang);

                        Enumeration<String> filesEnum = inputFilesInfo.keys();
                        while (filesEnum.hasMoreElements()) {
                            String file = filesEnum.nextElement();

                            Enumeration<String> uriEnum = inputFilesInfo.get(file).keys();
                            while (uriEnum.hasMoreElements()) {
                                String srcuri = uriEnum.nextElement();
                                SequencesVector pairdata = inputFilesInfo.get(file).get(srcuri);

                                SequenceData seqData = pairdata.getSequenceDataAtPosition(currentSequence.getPositionID());

                                if (seqData == null) {
                                    continue;
                                }

                                Vector<DataRecord> sourceVals = seqData.getValuesOfKey(currentParamName);
                                if (sourceVals == null || sourceVals.size() == 0) {
                                    continue;
                                }

                                if (canFastMethodBeApplied && sourceVals.contains(checkRecord) == false) {
                                    continue;
                                }

                                DataRecord srcCompareRecord = new DataRecord("", "");

                                Double tempComparisonResult = this.comp.compareValueAgainstVector(currentParamType, checkRecord, sourceVals, srcCompareRecord);
                                if (tempComparisonResult > 0d) {
                                    SourceInstancePair pair = new SourceInstancePair(file, srcuri);

                                    SourceTargetPair newSTPair = new SourceTargetPair(pair, searchVal);
                                    //int currentSequencePosition = currentSequence.getSequencePosition();
                                    SequenceSimilarityResult newSimResult = new SequenceSimilarityResult(currentSequence.getPositionID(), seqData.getSchemaInfo().getMnemonic(), seqData.getSchemaInfo().getWeight());
                                    newSimResult.setNewSimilarityResult(currentParamName, currentParamType, srcCompareRecord, checkRecord, tempComparisonResult);

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

        }

    }

    //<editor-fold defaultstate="collapsed" desc="Abandoned not necessarily working code">
    /*
    int DELETE_retrieveDataFrom_OWLIM_DB(
            OnlineDatabase db,
            Hashtable<String, Model> allRetrievedModels,
            SourceDataHolder sourceData,
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences) {

        //Utilities
        QueryBuilder qBuilder = new QueryBuilder(this.imClass);
        OnlineDatabaseActions qSource = new OnlineDatabaseActions(this.imClass, db);

        //find out not existing uris
        Vector<String> checkSourceDataUrisExistenceInDB = sourceData.collectAllUriValuesOfInstancesAndSequences();
        Vector<String> urisThatDoNotExistInTheDatabase = new Vector<String>();
        try {
            int ret = qSource.DELETE_findOutUrisNotExistingIntheDatabase(checkSourceDataUrisExistenceInDB, urisThatDoNotExistInTheDatabase);
            if (ret != ApiConstants.IMAPISuccessCode) {
                System.out.println("Error code returned while trying to filter out uri instances that do not exist.");
                return ret;
            }
        } catch (IOException ex) {
            System.out.println("Error occured while trying to filter out uri instances that do not exist.");
            Utilities.handleException(ex);
            return ApiConstants.IMAPIFailCode;
        }

        if (IMAPIClass.ExtentedMessagesEnabled) {
            System.out.println("The following uris were not found in the database:\n " + urisThatDoNotExistInTheDatabase.toString().replace("[", "").replace("]", "").replace(",", "\n"));
        }
        

        //prepare queries actions starting
        Hashtable<String, String> dbNs = db.getDbNamesapcesCopy();

        //get available list of cidoc namepsaces and get another structure
        //that will be filled with the subset of the above that is declared for this db
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();
        Hashtable<String, String> validCrmNsUsed = new Hashtable<String, String>();

        String queryPrefixes = "";

        Vector<String> nKeys = new Vector<String>(dbNs.keySet());

        for (int m = 0; m < nKeys.size(); m++) {
            String keyStr = nKeys.get(m);
            String fileNs = dbNs.get(keyStr);

            queryPrefixes += "PREFIX " + keyStr + ": <" + fileNs + ">\r\n";
            if (validCrmNs.contains(fileNs)) {
                validCrmNsUsed.put(keyStr, fileNs);
            }
        }

        queryPrefixes += "\r\n";

        StringObject getAllInstances = new StringObject("");
        StringObject countAllInstances = new StringObject("");
        Vector<UserQueryConfiguration> qSequences = this.imClass.userConfig.getUserQueriesCopy();

        //prepare the queries --> retrieve from model the class and predicate names
        Model prepareQueriesModel = ModelFactory.createDefaultModel();
        Enumeration<String> validCrmEnum = validCrmNsUsed.keys();
        while (validCrmEnum.hasMoreElements()) {
            Model loopModel = allRetrievedModels.get(validCrmNsUsed.get(validCrmEnum.nextElement()));
            prepareQueriesModel = prepareQueriesModel.union(loopModel);
        }

        int ret = qBuilder.prepareQueries(validCrmNsUsed, prepareQueriesModel, this.imClass.userConfig.getComparisonMode(), db.getTargetDatabasePredicateDirectionUsage(), getAllInstances, countAllInstances, qSequences);
        if (ret != ApiConstants.IMAPISuccessCode) {
            return ret;
        }
        

        //retrieve instances
        if (getAllInstances.getString().length() > 0) {

            ret = qSource.DELETE_retrieveSimilaritiesTo_Owlim_DB(queryPrefixes,
                    getAllInstances.getString(),
                    countAllInstances.getString(),
                    qSequences,
                    sourceData,
                    urisThatDoNotExistInTheDatabase,
                    pairSimilaritiesInSequences);

            if (ret != ApiConstants.IMAPISuccessCode) {
                return ret;
            }
        }
        

        return ApiConstants.IMAPISuccessCode;
    }
    */
    //</editor-fold>
}
