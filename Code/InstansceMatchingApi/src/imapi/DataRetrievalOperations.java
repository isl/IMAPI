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


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.util.FileManager;
import static imapi.IMAPIClass.AcceptUriEquality;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author tzortzak
 */
class DataRetrievalOperations {

    IMAPIClass imClass = null;
    //private StringSimilarity strSimilarity;//  = new StringSimilarity();

    private BaseComparisonClass comp=null;
    DataRetrievalOperations(IMAPIClass whichImClass) {
        this.imClass = whichImClass;
        this.comp = new BaseComparisonClass(this.imClass);
        
    }

    
    Hashtable<String, Model> retrieveAllDeclaredNamespacesModels(){
        System.out.println("\n=======================================\n"); 
        System.out.println("Retrieving Schema info from all namespaces used.");
        Hashtable<String, Model> allDeclaredModels = new Hashtable<String, Model>();
        
        Vector<CidocCrmCompatibleFile> inputFiles = this.imClass.userConfig.getSourceInputFiles();
        
        for(int i=0; i< inputFiles.size(); i++){
            Model CidocCRMmodel = ModelFactory.createDefaultModel();
            InputStream cidocInputStream = FileManager.get().open(inputFiles.get(i).getFilePath());        
            if (cidocInputStream == null) {
                continue;
            }

            CidocCRMmodel.read(cidocInputStream, null);
            
            Map<String, String> namespaces = CidocCRMmodel.getNsPrefixMap();
            Vector<String> nKeys = new Vector<String>(namespaces.keySet());
            for (int m = 0; m < nKeys.size(); m++) {
                String keyStr = nKeys.get(m);
                String fileNs = namespaces.get(keyStr);

                if(allDeclaredModels.containsKey(fileNs)){
                    continue;
                }
                Model loopModel = ModelFactory.createDefaultModel();
                boolean exCaught = false;
                try {
                    System.out.println("Retrieving model of namespace "+fileNs);
                    loopModel = loopModel.read(fileNs);
                    
                } catch (Exception ex) {
                    System.out.println("Exception caught while retrieving schema info for namespace: " + fileNs);
                    System.out.println(ex.getMessage());
                    exCaught = true;
                }
                if (exCaught) {
                    continue;
                }
                
                allDeclaredModels.put(fileNs, loopModel);
                
            }
        }
        
        ApiConstants.TargetSourceChoice targetChoice = this.imClass.userConfig.getComparisonMode();
        switch(targetChoice){
            case FILE_COMPARISON:{
                Vector<CidocCrmCompatibleFile> targetFiles = this.imClass.userConfig.getTargetInputFiles();
        
                for(int i=0; i< targetFiles.size(); i++){
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

                        if(allDeclaredModels.containsKey(fileNs)){
                            continue;
                        }
                        Model loopModel = ModelFactory.createDefaultModel();
                        boolean exCaught = false;
                        try {
                            System.out.println("Retrieving model of namespace "+fileNs);
                            loopModel = loopModel.read(fileNs);
                        } catch (Exception ex) {
                            System.out.println("Exception caught while retrieving schema info for namespace: " + fileNs);
                            System.out.println(ex.getMessage());
                            exCaught = true;
                        }
                        if (exCaught) {
                            continue;
                        }

                        
                        allDeclaredModels.put(fileNs, loopModel);
                        
                    }
                }
                break;
            }
            /*
            case BRITISH_MUSEUM_COLLECTION:{
                
            }
            */
            default:{
                
                Vector<OnlineDatabase> allSupportedDbs = this.imClass.conf.getSupportedDatabases();
                for(int i=0; i<allSupportedDbs.size(); i++){
                    OnlineDatabase db = allSupportedDbs.get(i);
                    if(db.getDBChoice()!=targetChoice){
                        continue;
                    }
                    Hashtable<String,String> dbNamepaces = db.getDbNamesapcesCopy();
                    
                    Enumeration<String> nsEnum = dbNamepaces.keys();
                    while(nsEnum.hasMoreElements()){
                        String keyStr = nsEnum.nextElement();
                        String namespace = dbNamepaces.get(keyStr);
                        if(allDeclaredModels.containsKey(namespace)){
                            continue;
                        }
                        
                        Model loopModel = ModelFactory.createDefaultModel();
                        boolean exCaught = false;
                        try {
                            System.out.println("Retrieving model of namespace "+namespace);
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
                    
                    
                }
                
                break;
            }
        }
        
        
        //Now add the extensions to valid cidoc namespaces
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();
        
        Enumeration<String> allNs = allDeclaredModels.keys();
        while(allNs.hasMoreElements()){
            String next = allNs.nextElement();
            String initialNamespace = next;
            if(validCrmNs.contains(next)==false){
                continue;
            }
            
            Model crmModel = allDeclaredModels.get(next);
            if(targetChoice==ApiConstants.TargetSourceChoice.CLAROS && next.equals("http://purl.org/NET/crm-owl#")){
                next = "http://erlangen-crm.org/091217/";
            }
            String rdfStatements = this.imClass.qWeightsConfig.getCrmExtentionsString(next);
            Model loopModel = ModelFactory.createDefaultModel();
            
            loopModel.read(new java.io.StringReader(rdfStatements), next);
            
            crmModel = crmModel.union(loopModel);
            
            allDeclaredModels.put(initialNamespace, crmModel);
        }
        //System.out.println("\n=======================================\n"); 
        //System.out.println("Finished Retrieving Schema info from namespaces declared in all input files.");
        
        return allDeclaredModels;
    }
    
    

    
    
    int retrieveDataFromFileAndCollectSimilarities(CidocCrmCompatibleFile cidocLoadFile, 
            Hashtable<String,Model> allRetrievedModels,
            Hashtable<SourceInstancePair, SequencesVector> inputFilesInfo, 
            Hashtable<SourceInstancePair, SequencesVector> targetFilesInfo, 
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences){
        
        String loadFile = cidocLoadFile.getFilePath();
        System.out.println("\n=======================================\n"); 
        System.out.println("Processing target file: " + cidocLoadFile.getFilePath());
        QueryBuilder qBuilder = new QueryBuilder(this.imClass);
        

        Model CidocCRMmodel = ModelFactory.createDefaultModel();
        InputStream cidocInputStream = FileManager.get().open(loadFile);        
        if (cidocInputStream == null) {
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Error while trying to load file: " + loadFile);
            return ApiConstants.IMAPIFailCode;
        }

        CidocCRMmodel.read(cidocInputStream, null);

        //System.out.println("=======================================");            
        
        //System.out.println("Retrieving Schema Info from file: " + loadFile);

        String queryPrefixes = "";
        Hashtable<String, String> allNamespacesThatWillBeUsed = new Hashtable<String, String>();
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();

        Hashtable<String,String> validCrmNsUsed = new Hashtable<String,String>();
        
        Model finalFileModel = ModelFactory.createDefaultModel();
        finalFileModel = finalFileModel.union(CidocCRMmodel);

        
        
        Map<String, String> namespaces = CidocCRMmodel.getNsPrefixMap();
        Vector<String> nKeys = new Vector<String>(namespaces.keySet());
        for (int m = 0; m < nKeys.size(); m++) {
            String keyStr = nKeys.get(m);
            String fileNs = namespaces.get(keyStr);

            queryPrefixes +="PREFIX "+keyStr+": <" +fileNs+">\n";
            allNamespacesThatWillBeUsed.put(keyStr, fileNs);
            if(validCrmNs.contains(fileNs)){
                validCrmNsUsed.put(keyStr, fileNs);
            }
            

            Model loopModel = allRetrievedModels.get(fileNs);
            
            //queryNamespacesDeclaration += "PREFIX " + keyStr + ": <" + fileNs + ">\n";
            finalFileModel = finalFileModel.union(loopModel);
        }
        

        if(validCrmNsUsed.size()>1){
            System.out.println("Warning: In file " + loadFile + " more that one Cidoc Namespaces were found:\n" + validCrmNsUsed.toString());
        }
        queryPrefixes+="\n";


        StringObject getAllInstances = new StringObject("");
        Vector<UserQueryConfiguration> qSequences = this.imClass.userConfig.getUserQueriesCopy();
        
        
        

        //prepare the queries --> retrieve from model the class and predicate names
        Model prepareQueriesModel = ModelFactory.createDefaultModel();
        Enumeration<String> validCrmEnum = validCrmNsUsed.keys();
        while(validCrmEnum.hasMoreElements()){
            Model loopModel = allRetrievedModels.get(validCrmNsUsed.get(validCrmEnum.nextElement()));
            prepareQueriesModel = prepareQueriesModel.union(loopModel);
        }
        
        int ret = qBuilder.prepareQueries(validCrmNsUsed, prepareQueriesModel, cidocLoadFile.getPredicateDirectionUsage(), getAllInstances, qSequences);

        if(ret!=ApiConstants.IMAPISuccessCode){
            return ret;
        }
       

        //System.out.println("=======================================");            
        
        //System.out.println("Starting queries on file: " + loadFile);
        
        OntModel fileModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF, finalFileModel);
        
        
        InputStream fileInputStream = FileManager.get().open(loadFile);
        if (fileInputStream == null) {
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Error while trying to load file: " + loadFile);
            return ApiConstants.IMAPIFailCode;
        }

        fileModel.read(fileInputStream, null);
        
        System.out.println("Retrieving Relevant Instances");
        int instancesCounter =0;
        if(getAllInstances.getString().length()>0){
            
            
                Vector<String> allValuesFound  = DataRetrievalOperations.collectAllURIValues(inputFilesInfo);
                //int currentStepPosition = qStep.getStepPositionInSequence();
                
                int currentCounter =0;
                int maxFilterCounter = allValuesFound.size();


                while(currentCounter<maxFilterCounter){
                    int resultCounter = 0;
                    Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter,allValuesFound);
                    currentCounter+=ApiConstants.UriFilterStepCount; 
                    
                    String currentQuery = getAllInstances.getString();
                    
                    
                    
                    Query tempQuery = QueryFactory.create(queryPrefixes + currentQuery);
                    List<Var> vars = tempQuery.getProjectVars();
                    if(vars.size()>0){
                        String paramName = vars.get(0).getName().replace("?", "");
                        currentQuery = QueryBuilder.replaceFilteringPlaceHoldersForInstances(currentQuery, paramName, currentSubset);
                        
                        if(IMAPIClass.DEBUG){
                            System.out.println(queryPrefixes+currentQuery);
                        }
                    }
                    
                    Query instancesQuery = QueryFactory.create(queryPrefixes + currentQuery);
                    QueryExecution instancesQueryExecution = QueryExecutionFactory.create(instancesQuery, fileModel);
                    com.hp.hpl.jena.query.ResultSet instancesResults = instancesQueryExecution.execSelect();

                    while (instancesResults.hasNext()) {
                        resultCounter++;
                        instancesCounter++;
                        QuerySolution qs = instancesResults.next();
                        Iterator<String> iter = qs.varNames();
                        while (iter.hasNext()) {
                            String str = iter.next();
                            String val = qs.get(str).toString();

                            SourceInstancePair newVal = new SourceInstancePair(loadFile, val);

                            Enumeration<SourceInstancePair> sourceEnum = inputFilesInfo.keys();
                            while(sourceEnum.hasMoreElements()){
                                SourceInstancePair pair = sourceEnum.nextElement();
                                if(pair.getInstanceUri().equals(val)){
                                    SourceTargetPair newSTPair = new SourceTargetPair(pair, newVal);
                                    SequenceSimilarityResultVector newSeqVec = new SequenceSimilarityResultVector();
                                            
                                    if(pairSimilaritiesInSequences.containsKey(newSTPair)==false){
                                        pairSimilaritiesInSequences.put(newSTPair, newSeqVec);
                                    }
                                    if(ApiConstants.KeepandPresentAllTargetDataFound){
                                        if (targetFilesInfo.containsKey(newVal) == false) {
                                            
                                            targetFilesInfo.put(newVal, new SequencesVector());
                                        }
                                    }
                                }
                            }

                        }
                    }

                    instancesQueryExecution.close();
                    if(IMAPIClass.DEBUG){
                        System.out.println("Adding "+resultCounter+" results.");
                    }
                }
                    
            
        }
        
        System.out.println("\nFound a total of "+instancesCounter+" relevant instances.\n\n");
        
        //Double threshold = this.imClass.userConfig.getResultsThreshold();
        for(int i=0; i< qSequences.size(); i++){
            
            UserQueryConfiguration currentSequence = qSequences.get(i);
            int currentSequencePosition = currentSequence.getPositionID();
            System.out.println("Retrieving Relevant Records of sequence "+(currentSequence.getPositionID()+1)+" with mnemonic " + currentSequence.getMnemonic());
            instancesCounter =0;
        
            
            
            Hashtable<String,String> parameters = currentSequence.getAllQueryStepParameters();
            
            //System.out.println("\n\nQuering sequence "+currentSequence.getSequencePosition() +" --> mnemonic: "+currentSequence.getMnemonic());
            //SequenceData currentData = new SequenceData(currentSequence);
            
            String[] parameterNames = currentSequence.getSortedParameterNamesCopy();
            String[] parameterTypes = currentSequence.getSortedParameterTypesCopy();
            String[] stepQueries = currentSequence.getSortedQueriesCopy();
            
            for(int k=0; k<parameterNames.length; k++){
                
                
                String currentQuery = stepQueries[k];
                String currentStepParameterName = parameterNames[k];
                
                System.out.println("Adding "+currentStepParameterName+" records.");
                
                
                Vector<String> allValuesFound  = DataRetrievalOperations.collectAllValuesOfSpecificParameter(currentSequencePosition, currentStepParameterName, inputFilesInfo);
                //int currentStepPosition = qStep.getStepPositionInSequence();
                
                int currentCounter =0;
                int maxFilterCounter = allValuesFound.size();


                if(maxFilterCounter==0){
                    continue;
                }

                int resultCounter = 0;
                while(currentCounter<maxFilterCounter){
                    
                    Vector<String> currentSubset = QueryBuilder.collectSequenctiallyAsubsetOfValues(currentCounter,allValuesFound);
                    currentCounter+=ApiConstants.UriFilterStepCount; 
                    
                    currentQuery = QueryBuilder.replaceFilteringPlaceHolders(currentQuery, currentStepParameterName, currentSubset);

                    if(IMAPIClass.DEBUG){
                        System.out.println("currentQuery:\n"+queryPrefixes+currentQuery);
                    }
                    Query predicateQuery = QueryFactory.create(queryPrefixes + currentQuery);
                    QueryExecution instancesQueryExecution = QueryExecutionFactory.create(predicateQuery, fileModel);
                    com.hp.hpl.jena.query.ResultSet predicateResults = instancesQueryExecution.execSelect();

                    while (predicateResults.hasNext()) {
                        instancesCounter++;
                        resultCounter++;
                        QuerySolution qs = predicateResults.next();
                        Iterator<String> iter = qs.varNames();
                        String resultLine = "";
                        boolean gotInstanceUri = false;
                        String instanceUri ="";
                        SourceInstancePair searchVal = null;
                        while (iter.hasNext()) {
                            String KeyStr = iter.next();
                            if(!gotInstanceUri){
                                instanceUri = qs.get(KeyStr).toString();
                                searchVal  = new SourceInstancePair(loadFile, instanceUri);
                                gotInstanceUri = true;
                                continue;
                            }

                            String val =""; 
                            String lang="";
                            if(qs.get(KeyStr).isLiteral()){
                                val=qs.get(KeyStr).asLiteral().getString();
                                lang=qs.get(KeyStr).asLiteral().getLanguage();
                            }
                            else{
                                val=qs.get(KeyStr).toString();
                            }

                            resultLine +="\t"+instanceUri+"\t"+KeyStr+": " +val + " " + lang;
                            if(IMAPIClass.DEBUG){
                                System.out.println(resultLine);
                            }


                            DataRecord newRecord = new DataRecord(val, lang);
                            //here we should see if we have to store the value found --> make the comparison

                            
                            Enumeration<SourceInstancePair> sourceEnum = inputFilesInfo.keys();
                            while(sourceEnum.hasMoreElements()){
                                SourceInstancePair pair = sourceEnum.nextElement();
                                SequencesVector pairdata = inputFilesInfo.get(pair);

                                SequenceData seqData = pairdata.getSequenceDataAtPosition(currentSequencePosition);
                                if(seqData==null){
                                    continue;
                                }

                                Vector<DataRecord> sourceVals = seqData.getValuesOfKey(KeyStr);
                                if(sourceVals ==null || sourceVals.size()==0){
                                    continue;
                                }

                                String paramType = parameters.get(KeyStr);
                                DataRecord srcCompareRecord = new DataRecord("", "");

                                Double tempComparisonResult = this.comp.compareValueAgainstVector(paramType,newRecord, sourceVals, srcCompareRecord);
                                if(tempComparisonResult>0d){
    
                                    if(ApiConstants.KeepandPresentAllTargetDataFound){
                                        if (targetFilesInfo.containsKey(searchVal) == false) {
                                            SequencesVector newSeq = new SequencesVector();
                                            newSeq.addValueToSequence(currentSequence, KeyStr, val, lang);
                                            targetFilesInfo.put(searchVal, newSeq);
                                        }
                                        else{
                                            targetFilesInfo.get(searchVal).addValueToSequence(currentSequence, KeyStr, val, lang);
                                        }                                        
                                    }


                                    SourceTargetPair newSTPair = new SourceTargetPair(pair, searchVal);
                                    //int currentSequencePosition = currentSequence.getSequencePosition();
                                    SequenceSimilarityResult newSimResult = new SequenceSimilarityResult(currentSequencePosition,seqData.getSchemaInfo().getMnemonic(), seqData.getSchemaInfo().getWeight());
                                    newSimResult.setNewSimilarityResult(KeyStr, paramType, srcCompareRecord, newRecord, tempComparisonResult);

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
                    instancesQueryExecution.close();                                
                    
                }
                System.out.println("Adding "+resultCounter+" results.");
                
            }                   
            
            System.out.println("\nFound a total of "+instancesCounter+" relevant Records in sequence with mnemonic "+currentSequence.getMnemonic()+".\n\n");
        }

        return ApiConstants.IMAPISuccessCode;
    }
    
    int retrieveDataFromFile(CidocCrmCompatibleFile cidocLoadFile, 
            Hashtable<String,Model> allRetrievedModels,
            Hashtable<SourceInstancePair, SequencesVector> inputFilesInfo){
        
        
        String loadFile = cidocLoadFile.getFilePath();
        System.out.println("\n=======================================\n"); 
        System.out.println("Processing source file: " + cidocLoadFile.getFilePath());
        QueryBuilder qBuilder = new QueryBuilder(this.imClass);
        

        Model CidocCRMmodel = ModelFactory.createDefaultModel();
        InputStream cidocInputStream = FileManager.get().open(loadFile);        
        if (cidocInputStream == null) {
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Error while trying to load file: " + loadFile);
            return ApiConstants.IMAPIFailCode;
        }

        CidocCRMmodel.read(cidocInputStream, null);

        //System.out.println("=======================================");            
        
        //System.out.println("Retrieving Schema Info from file: " + loadFile);

        String queryPrefixes = "";
        Hashtable<String, String> allNamespacesThatWillBeUsed = new Hashtable<String, String>();
        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();

        Hashtable<String,String> validCrmNsUsed = new Hashtable<String,String>();
        
        Model finalFileModel = ModelFactory.createDefaultModel();
        finalFileModel = finalFileModel.union(CidocCRMmodel);

        Map<String, String> namespaces = CidocCRMmodel.getNsPrefixMap();
        Vector<String> nKeys = new Vector<String>(namespaces.keySet());
        for (int m = 0; m < nKeys.size(); m++) {
            String keyStr = nKeys.get(m);
            String fileNs = namespaces.get(keyStr);

            queryPrefixes +="PREFIX "+keyStr+": <" +fileNs+">\n";
            allNamespacesThatWillBeUsed.put(keyStr, fileNs);
            
            if(validCrmNs.contains(fileNs)){
                validCrmNsUsed.put(keyStr, fileNs);
                
            }
            
            Model loopModel = allRetrievedModels.get(fileNs);
            //queryNamespacesDeclaration += "PREFIX " + keyStr + ": <" + fileNs + ">\n";
            if(loopModel!=null){
            finalFileModel = finalFileModel.union(loopModel);
            }
        }
        
        
        if(validCrmNsUsed.size()>1){
            System.out.println("Warning: In file " + loadFile + " more that one Cidoc Namespaces were found:\n" + validCrmNsUsed.toString());
        }
        queryPrefixes+="\n";


        StringObject getAllInstances = new StringObject("");
        Vector<UserQueryConfiguration> qSequences = this.imClass.userConfig.getUserQueriesCopy();
        //prepare the queries --> retrieve from model the class and predicate names
        Model prepareQueriesModel = ModelFactory.createDefaultModel();
        Enumeration<String> validCrmEnum = validCrmNsUsed.keys();
        while(validCrmEnum.hasMoreElements()){
            Model loopModel = allRetrievedModels.get(validCrmNsUsed.get(validCrmEnum.nextElement()));
            prepareQueriesModel = prepareQueriesModel.union(loopModel);
        }
        
        int ret = qBuilder.prepareQueries(validCrmNsUsed, prepareQueriesModel, cidocLoadFile.getPredicateDirectionUsage(), getAllInstances, qSequences);

        if(ret!=ApiConstants.IMAPISuccessCode){
            return ret;
        }
       

        //System.out.println("=======================================");            
        
        //System.out.println("Starting queries on file: " + loadFile);
        
        OntModel fileModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF, finalFileModel);
        
        
        
        InputStream fileInputStream = FileManager.get().open(loadFile);
        if (fileInputStream == null) {
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Error while trying to load file: " + loadFile);
            return ApiConstants.IMAPIFailCode;
        }

        
        fileModel.read(fileInputStream, null);

        /*
        for(int p=0; p< validCrmNsUsed.size(); p++){
            String targetNs = validCrmNsUsed.get(p);
            
            String rdfStatements = this.imClass.conf.getCrmExtentionsString(targetNs);
            //Model loopModel = ModelFactory.createDefaultModel();
            
            //loopModel.read(new java.io.StringReader(rdfStatements), targetNs);
            fileModel.read(new java.io.StringReader(rdfStatements), targetNs);
        }
        */
        
        
        
        if(getAllInstances.getString().length()>0){
            Query instancesQuery = QueryFactory.create(queryPrefixes + getAllInstances.getString());
            QueryExecution instancesQueryExecution = QueryExecutionFactory.create(instancesQuery, fileModel);
            com.hp.hpl.jena.query.ResultSet instancesResults = instancesQueryExecution.execSelect();

            while (instancesResults.hasNext()) {
                QuerySolution qs = instancesResults.next();
                Iterator<String> iter = qs.varNames();
                while (iter.hasNext()) {
                    String str = iter.next();
                    String val = qs.get(str).toString();

                    SourceInstancePair newVal = new SourceInstancePair(loadFile, val);
                    if (inputFilesInfo.containsKey(newVal) == false) {
                        inputFilesInfo.put(newVal, new SequencesVector());
                    }                    
                }
            }
            
            instancesQueryExecution.close();
        }
        
        //Double threshold = this.imClass.userConfig.getResultsThreshold();
        for(int i=0; i< qSequences.size(); i++){
            
            UserQueryConfiguration currentSequence = qSequences.get(i);
            //int currentSequencePosition = currentSequence.getSequencePosition();
            //Hashtable<String,String> parameters = currentSequence.getParameters();
            //System.out.println("\n\nQuering sequence "+currentSequence.getSequencePosition() +" --> mnemonic: "+currentSequence.getMnemonic());
            //SequenceData currentData = new SequenceData(currentSequence);
            
            String[] parameterNames = currentSequence.getSortedParameterNamesCopy();
            String[] parameterTypes = currentSequence.getSortedParameterTypesCopy();
            String[] stepQueries = currentSequence.getSortedQueriesCopy();
            
            for(int k=0; k<stepQueries.length; k++){
            
                String currentQuery = stepQueries[k];
                //System.out.println("currentQuery:\n"+queryPrefixes+currentQuery);
                Query predicateQuery = QueryFactory.create(queryPrefixes + currentQuery);
                QueryExecution instancesQueryExecution = QueryExecutionFactory.create(predicateQuery, fileModel);
                com.hp.hpl.jena.query.ResultSet predicateResults = instancesQueryExecution.execSelect();

                while (predicateResults.hasNext()) {
                    //System.out.println("Result Found");
                    QuerySolution qs = predicateResults.next();
                    Iterator<String> iter = qs.varNames();
                    String resultLine = "";
                    boolean gotInstanceUri = false;
                    String instanceUri ="";
                    SourceInstancePair searchVal = null;
                    while (iter.hasNext()) {
                        String KeyStr = iter.next();
                        if(!gotInstanceUri){
                            instanceUri = qs.get(KeyStr).toString();
                            searchVal  = new SourceInstancePair(loadFile, instanceUri);
                            gotInstanceUri = true;
                            continue;
                        }
                        
                        String val =""; 
                        String lang="";
                        if(qs.get(KeyStr).isLiteral()){
                            val=qs.get(KeyStr).asLiteral().getString();
                            lang=qs.get(KeyStr).asLiteral().getLanguage();
                        }
                        else{
                            val=qs.get(KeyStr).toString();
                        }
                        
                        resultLine +="\t"+instanceUri+"\t"+KeyStr+": " +val + " " + lang;
                        /*System.out.println(resultLine);
                        if(val.length()>0 && parameterTypes[k].equals("timespan")){
                            ValueOf_Timespan vspan = new ValueOf_Timespan(val);
                        }
                        */
                        inputFilesInfo.get(searchVal).addValueToSequence(currentSequence, KeyStr, val, lang);
                        
                    }
                    
                }
                instancesQueryExecution.close();                
            }                       
        }

        return ApiConstants.IMAPISuccessCode;
    }
    
    int retrieveDataFromOnlineDBAndCollectSimilarities(OnlineDatabase db,
            Hashtable<String,Model> allRetrievedModels,
            Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo, 
            Hashtable<SourceInstancePair, SequencesVector> targetSourceInfo, 
            Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences){
            
        QueryBuilder qBuilder = new QueryBuilder(this.imClass);
        

        //Hashtable<String, String> allNamespacesThatWillBeUsed = new Hashtable<String, String>();
        Hashtable<String,String> dbNs = db.getDbNamesapcesCopy();

        Vector<String> validCrmNs = this.imClass.qWeightsConfig.getValidCrmNamespaces();


        Hashtable<String,String> validCrmNsUsed = new Hashtable<String,String>();
        
        Model finalFileModel = ModelFactory.createDefaultModel();
        String queryPrefixes ="";


        Vector<String> nKeys = new Vector<String>(dbNs.keySet());
        //Collections.sort(nKeys, Collections.reverseOrder());

        for (int m = 0; m < nKeys.size(); m++) {
            String keyStr = nKeys.get(m);
            String fileNs = dbNs.get(keyStr);


            queryPrefixes +="PREFIX "+keyStr+": <" +fileNs+">\r\n";
            if(validCrmNs.contains(fileNs)){
                validCrmNsUsed.put(keyStr,fileNs);
            }
            /*
            if (validCrmNamespaces.contains(fileNs)) {
                allNamespacesThatWillBeUsed.put(keyStr, fileNs);
            }
            */

            Model loopModel = allRetrievedModels.get(fileNs);
            
            //queryNamespacesDeclaration += "PREFIX " + keyStr + ": <" + fileNs + ">\n";
            finalFileModel = finalFileModel.union(loopModel);
        }
        
        queryPrefixes+="\r\n";

        StringObject getAllInstances = new StringObject("");
        Vector<UserQueryConfiguration> qSequences = this.imClass.userConfig.getUserQueriesCopy();
        
        
        //prepare the queries --> retrieve from model the class and predicate names
        Model prepareQueriesModel = ModelFactory.createDefaultModel();
        Enumeration<String> validCrmEnum = validCrmNsUsed.keys();
        while(validCrmEnum.hasMoreElements()){
            Model loopModel = allRetrievedModels.get(validCrmNsUsed.get(validCrmEnum.nextElement()));
            prepareQueriesModel = prepareQueriesModel.union(loopModel);
        }
        
        int ret = qBuilder.prepareQueries(validCrmNsUsed, prepareQueriesModel, db.getTargetDatabasePredicateDirectionUsage(), getAllInstances, qSequences);
        if(ret!= ApiConstants.IMAPISuccessCode){
            return ret;
        }
        
        
        //System.out.println("=======================================");            
        
        //System.out.println("Starting queries on database: " + db.getDbName());
        
        
        OnlineDatabaseActions qSource = new OnlineDatabaseActions(this.imClass, db);
        
        //retrieve instances 
        if(getAllInstances.getString().length()>0){
            String baseQueryString = queryPrefixes+getAllInstances.getString();//qBuild.get_All_Instances_Query(ApiConstants.getDBTargetNamespace(this.imClass.userConfig.getComparisonMode()), classId).trim();
            

            try {
                ret = qSource.retrieveAllInstancesData(baseQueryString, inputSourceInfo, targetSourceInfo, pairSimilaritiesInSequences);
            } catch (UnsupportedEncodingException e) {

                System.out.println("UnsupportedEncodingException caught: " + e.getMessage());
                e.printStackTrace();
                return ApiConstants.IMAPIFailCode;
                        
            } catch (MalformedURLException e) {

                System.out.println("MalformedURLException caught: " + e.getMessage());
                e.printStackTrace();
                return ApiConstants.IMAPIFailCode;
            } catch (IOException e) {

                System.out.println("IOException caught: " + e.getMessage());
                e.printStackTrace();
                return ApiConstants.IMAPIFailCode;
            }
            
            if(ret!=ApiConstants.IMAPISuccessCode){
                return ret;
            }
        }
        
        
        System.out.println();
        System.out.println();
        //retrieve sequences data
        for(int i=0; i< qSequences.size(); i++){
            
            UserQueryConfiguration currentSequence = qSequences.get(i);
            
            try {
                ret = qSource.retrieveDataOfSpecificSequence(queryPrefixes, currentSequence, inputSourceInfo, targetSourceInfo, pairSimilaritiesInSequences);
            } catch (UnsupportedEncodingException e) {
                System.out.println("UnsupportedEncodingException caught: " + e.getMessage());
                e.printStackTrace();
                return ApiConstants.IMAPIFailCode;
            } catch (MalformedURLException e) {

                System.out.println("MalformedURLException caught: " + e.getMessage());
                e.printStackTrace();
                return ApiConstants.IMAPIFailCode;
            } catch (IOException e) {

                System.out.println("IOException caught: " + e.getMessage());
                e.printStackTrace();
                return ApiConstants.IMAPIFailCode;
            }
            
            if(ret!=ApiConstants.IMAPISuccessCode){
                return ret;
            }
                         
        }
       
        return ApiConstants.IMAPISuccessCode;
    }
    

    static boolean containsValuesOfSpecificParameter(int sequencePosition,  String parameterName, Hashtable<SourceInstancePair, SequencesVector> sourceInfo){
        
        Enumeration<SourceInstancePair> pairEnum = sourceInfo.keys();
        while(pairEnum.hasMoreElements()){
            SequencesVector seqVec = sourceInfo.get(pairEnum.nextElement());
            
            if(seqVec==null || seqVec.size()==0){
                continue;
            }
            
            SequenceData seqData = seqVec.getSequenceDataAtPosition(sequencePosition);
            if(seqData==null){
                continue;
            }
            
            Vector<DataRecord> pairVals = seqData.getValuesOfKey(parameterName);
            
            for(int i=0; i<pairVals.size();i++){
                if(pairVals.get(i).getValue().trim().length()>0){
                    return true;
                }
                       
            }
        }
        return false;
    }
    static Vector<String> collectAllValuesOfSpecificParameter(int sequencePosition,  String parameterName, Hashtable<SourceInstancePair, SequencesVector> sourceInfo){
        Vector<String> returnVals = new Vector<String>();
        
        Enumeration<SourceInstancePair> pairEnum = sourceInfo.keys();
        while(pairEnum.hasMoreElements()){
            SequencesVector seqVec = sourceInfo.get(pairEnum.nextElement());
            
            if(seqVec==null || seqVec.size()==0){
                continue;
            }
            
            SequenceData seqData = seqVec.getSequenceDataAtPosition(sequencePosition);
            if(seqData==null){
                continue;
            }
            
            Vector<DataRecord> pairVals = seqData.getValuesOfKey(parameterName);
            for(int i=0; i<pairVals.size();i++){
                if(returnVals.contains(pairVals.get(i).getValue())==false){
                    returnVals.add(pairVals.get(i).getValue());
                }
            }
        }
        
        return returnVals;
    }
    
    static Vector<String> collectAllURIValues(Hashtable<SourceInstancePair, SequencesVector> sourceInfo){
        Vector<String> returnVals = new Vector<String>();
        
        Enumeration<SourceInstancePair> pairEnum = sourceInfo.keys();
        while(pairEnum.hasMoreElements()){
            SourceInstancePair pair = pairEnum.nextElement();
            
            if(returnVals.contains(pair.getInstanceUri())==false){
                returnVals.add(pair.getInstanceUri());
            }            
        }
        
        return returnVals;
    }
    
    
}
