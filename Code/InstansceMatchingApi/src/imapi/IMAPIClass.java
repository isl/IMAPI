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


//import Structs.OnlineDatabase;
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
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.atlas.io.IndentedWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author tzortzak
 */
public class IMAPIClass {

    static boolean DEBUG = false;
    
    static final boolean AcceptUriEquality = true;
    //static String BMDBcidocNamespace = "http://erlangen-crm.org/current/";

    static boolean ExtentedMessagesEnabled = false;
    //public static final String SearchMode_Actor = "Actor";

    public void disableExtentedMessages() {
        this.ExtentedMessagesEnabled = false;
    }

    public void enableExtentedMessages() {
        this.ExtentedMessagesEnabled = true;
    }
    
    private int errCode = ApiConstants.IMAPIFailCode;
    String errorMessage = "";

    public int getErrorCode() {
        return this.errCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;
        /*if (this.errCode == ApiConstants.IMAPIFailCode) {
            System.out.println("ERROR occurred: " + errorMsg);
        }*/
    }
    
    

    ApiConfigClass conf = null;
    UserConfigurationClass userConfig = null;
    QueryPrototypeConfigurationClass qWeightsConfig = null;

    
    /**
     * Do not forget to check IMAPIClass's getErrorCode() after initialization
     *
     * @param xmlConfigurationAbsolutePath Absolute path for the configuration
     * xml file
     *
     */
    public IMAPIClass(String baseFilePath, String userConfigurationXmlFile,String queriesConfigurationXmlFile) {

        System.out.println("New IMAPIClass instance created at time:"+java.util.Calendar.getInstance().getTime());
        this.setErrorMessage(ApiConstants.IMAPISuccessCode, "");
        
        String baseClassIdStr = UserConfigurationClass.getBaseConfigurationClass(userConfigurationXmlFile);
        if(baseClassIdStr.length()==0){
            this.setErrorMessage(ApiConstants.IMAPIFailCode, "Error occured while retrieving base class.");
            return;
        }
        
        qWeightsConfig = new QueryPrototypeConfigurationClass(baseClassIdStr, queriesConfigurationXmlFile);
        if (qWeightsConfig.getErrorCode() != ApiConstants.IMAPISuccessCode) {
            this.setErrorMessage(qWeightsConfig.getErrorCode(), qWeightsConfig.getErrorMessage());
            return;
        }

        userConfig = new UserConfigurationClass(baseFilePath, userConfigurationXmlFile, qWeightsConfig);
        if (userConfig.getErrorCode() != ApiConstants.IMAPISuccessCode) {
            this.setErrorMessage(userConfig.getErrorCode(), userConfig.getErrorMessage());
            return;
        }
        
        conf = new ApiConfigClass();
        if (conf.getErrorCode() != ApiConstants.IMAPISuccessCode) {
            this.setErrorMessage(conf.getErrorCode(), conf.getErrorMessage());
            return;
        }       

    }

    
    
    public int performComparison() {

        
        DataRetrievalOperations retrieveData = new DataRetrievalOperations(this);
        BaseComparisonClass compClass = new BaseComparisonClass(this);
        
        //internal structures used
        Hashtable<Integer, Vector<SourceTargetPair>> finalResults = new Hashtable<Integer, Vector<SourceTargetPair>>();
        Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences = new Hashtable<SourceTargetPair, SequenceSimilarityResultVector>();
        Hashtable<SourceInstancePair, SequencesVector> inputSourceInfo = new Hashtable<SourceInstancePair, SequencesVector>();
        Hashtable<SourceInstancePair, SequencesVector> targetSourceInfo = new Hashtable<SourceInstancePair, SequencesVector>();
        
        
        //Make check if online db is on
        if(this.userConfig.getComparisonMode()!= ApiConstants.TargetSourceChoice.FILE_COMPARISON){
            
            Vector<OnlineDatabase> dbs = this.conf.getSupportedDatabases();
            OnlineDatabase db = null;
            for(int k=0; k< dbs.size(); k++){
                if(dbs.get(k).getDBChoice() == this.userConfig.getComparisonMode()){
                    db = dbs.get(k);
                    break;
                }

            }
            
            OnlineDatabaseActions qSource = new OnlineDatabaseActions(this, db);
            int ret = qSource.checkIfDBisAvailable();
            if(ret!=ApiConstants.IMAPISuccessCode){
                return ret;
            }
        }
                
        //retrieve all needed namespaces from internet
        Hashtable<String,Model> allRetrievedModels = retrieveData.retrieveAllDeclaredNamespacesModels();
        

        //read Source Files info
        Vector<CidocCrmCompatibleFile> inputFiles = this.userConfig.getSourceInputFiles();
        for(int inputFileIndex =0; inputFileIndex<inputFiles.size(); inputFileIndex++){
            
            CidocCrmCompatibleFile inputFile = inputFiles.get(inputFileIndex);
            int ret = retrieveData.retrieveDataFromFile(inputFile, allRetrievedModels, inputSourceInfo);            
            if(ret!=ApiConstants.IMAPISuccessCode){
                return ret;
            }
            //System.out.println("=======================================");
        }
        
        System.out.println("\n\n============================================================");
        System.out.println("============================================================");
        System.out.println("============================================================");
        //print what data was found
        System.out.println("Found " + inputSourceInfo.keySet().size() + " instances in all \"\"SOURCE\"\" input files.");
        printSourceInfo(inputSourceInfo);
        
                
        //Read Info from Target Files or Online Database
        System.out.println("\n=======================================");
        System.out.println("=======================================");
        System.out.println("=======================================\n\n");
        
        ApiConstants.TargetSourceChoice comparisonChoice  = this.userConfig.getComparisonMode();        
        System.out.println("Starting queries on TARGET source: "+comparisonChoice.toString()+"\n\n");
        
        switch(comparisonChoice){
            case FILE_COMPARISON:{
                Vector<CidocCrmCompatibleFile> targetFiles = this.userConfig.getTargetInputFiles();
                for(int targetFileIndex =0; targetFileIndex<targetFiles.size(); targetFileIndex++){

                    CidocCrmCompatibleFile targetFile = targetFiles.get(targetFileIndex);
                    int ret = retrieveData.retrieveDataFromFileAndCollectSimilarities(targetFile,allRetrievedModels, inputSourceInfo, targetSourceInfo, pairSimilaritiesInSequences);            
                    if(ret!=ApiConstants.IMAPISuccessCode){
                        return ret;
                    }
                    
                }
                
                break;
            }
            case CLAROS:            
            case BRITISH_MUSEUM_COLLECTION:{
                Vector<OnlineDatabase> dbs = this.conf.getSupportedDatabases();
                OnlineDatabase db = null;
                for(int k=0; k< dbs.size(); k++){
                    if(dbs.get(k).getDBChoice() == comparisonChoice){
                        db = dbs.get(k);
                        break;
                    }
                    
                }
                
                int ret = retrieveData.retrieveDataFromOnlineDBAndCollectSimilarities(db,allRetrievedModels, inputSourceInfo, targetSourceInfo, pairSimilaritiesInSequences);
                if(ret!=ApiConstants.IMAPISuccessCode){
                    return ret;
                }
                break;
            }
            default:{
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "TargetSourceChoice not set correctly check user configuration xml file again.");
                return ApiConstants.IMAPIFailCode;
            }
        }
        
        if(ApiConstants.KeepandPresentAllTargetDataFound){
            //print what data was found
            System.out.println("\n\nFound " + targetSourceInfo.keySet().size() + " RELEVANT instances in all \"\"TARGET\"\" input files.");
            printSourceInfo(targetSourceInfo);
        }
        inputSourceInfo = null;
        targetSourceInfo = null;
        //start comparing
        
        
        //find out the denominator in order to normalize results
        double similarityDenominator = 0;
        
        
        
        int maxNumberOfQueriesIndex = this.userConfig.getNumberOfSequences();
        for (int i = 0; i < maxNumberOfQueriesIndex; i++) {            
            similarityDenominator += this.userConfig.getWeightAtUserQueryIndex(i);
        }
        
        
        //PRINT OUT
        //System.out.println("Total Number of results with similarity > 0:\r\n#################  " + allSimilaritiesHash.keySet().size() + "  ##################");
        System.out.println("\n\n============================================================");
        System.out.println("============================================================");
        System.out.println("============================================================");
        
        //for each sequence save the parameter name - type compared values and the similarity
        Enumeration<SourceTargetPair> similarityIterator = pairSimilaritiesInSequences.keys();
        while (similarityIterator.hasMoreElements()) {

            SourceTargetPair pair = similarityIterator.nextElement();
            
            
            SequenceSimilarityResultVector similarities = pairSimilaritiesInSequences.get(pair);

            //Vector<SequenceSimilarityResult> similarityResults = new Vector<SequenceSimilarityResult>();
            
            int calculatedSimilarity = 0;
            if(pair.getSourceInstance().getInstanceUri().equals(pair.getTargetInstance().getInstanceUri())){
                calculatedSimilarity = 100;
            }
            else{
                calculatedSimilarity = compClass.calculateFinalSimilarity(similarities, similarityDenominator);
            }
            if (calculatedSimilarity >= (userConfig.getResultsThreshold() * 100)) {
                if (finalResults.containsKey(calculatedSimilarity)) {
                    finalResults.get(calculatedSimilarity).add(pair);
                } else {
                    Vector<SourceTargetPair> newVec = new Vector<SourceTargetPair>();
                    newVec.add(pair);
                    finalResults.put(calculatedSimilarity, newVec);
                }
                //System.out.println((++resultsCounter) + ".\t"+df.format(calculatedSimilarity)+"\t"+ pair.getSourceInstance().getSourceName()+"    " + pair.getSourceInstance().getInstanceUri()+"    " + pair.getTargetInstance().getSourceName()+"    " + pair.getTargetInstance().getInstanceUri());
            }
        }
        
        
        System.out.println("\n\nResults with similarity above threshold: " + userConfig.getResultsThreshold());
        System.out.println("---------------------------------------------------------------------------");
        int resultsCounter = 0;
        if (finalResults.size() == 0) {
            System.out.println("0 results found.");
        } else {
            
            Vector<Integer> sortBySimilarityVec = new Vector<Integer>(finalResults.keySet());
            Collections.sort(sortBySimilarityVec);
            Collections.reverse(sortBySimilarityVec);
            
            for (int i = 0; i < sortBySimilarityVec.size(); i++) {
                int sim = sortBySimilarityVec.get(i);
                Vector<SourceTargetPair> stPairs = finalResults.get(sim);
                Collections.sort(stPairs);
                
                for (int k = 0; k < stPairs.size(); k++) {
                    SourceTargetPair pair = stPairs.get(k);
                    System.out.println((++resultsCounter) + ".\t" + ((float) (sim / 100f)) + "\t" + pair.getSourceInstance().getSourceName() + "    " + pair.getSourceInstance().getInstanceUri() + "    " + pair.getTargetInstance().getSourceName() + "    " + pair.getTargetInstance().getInstanceUri());
                    
                    SequenceSimilarityResultVector tripVec = pairSimilaritiesInSequences.get(pair);
                    
                    //check if uri similarity is encoutered
                    
                    if(pair.getSourceInstance().getInstanceUri().equals(pair.getTargetInstance().getInstanceUri())){
                        System.out.println("\t\t\turi similarity 1\n");
                        continue;
                    }
                    else{
                        this.printSimilaritiesData(tripVec);
                    }
                        
                    
                    
                    System.out.println();
                    
                }
            }
        }
        
        
        return ApiConstants.IMAPISuccessCode;
    }
    
    void printSourceInfo(Hashtable<SourceInstancePair, SequencesVector> sourceInfo){
        
        Vector<SourceInstancePair> fileInstances = new Vector<SourceInstancePair>(sourceInfo.keySet());
        Collections.sort(fileInstances);
        
        //System.out.println("Found " + fileInstances.size() + " instances in all \"\"SOURCE\"\" input files.");
        for (int i = 0; i < fileInstances.size(); i++) {
            SourceInstancePair pair = fileInstances.get(i);
            System.out.println("\r\n" + (i + 1) + ". " + pair.getInstanceUri() + "\t\tin source: " + pair.getSourceName());
            SequencesVector allSeqData = sourceInfo.get(pair);
            for(int k=0; k< allSeqData.size(); k++){
                SequenceData currentSeq = allSeqData.get(k);
                System.out.println("\tData found on sequence: "+(currentSeq.getSchemaInfo().getPositionID()+1)+" mnemonic: " + currentSeq.getSchemaInfo().getMnemonic() + " with weight: " +currentSeq.getSchemaInfo().getWeight());
                
                Hashtable<String,String> parameterTypes = currentSeq.getSchemaInfo().getAllQueryStepParameters();
                
                String[] parameterNames = currentSeq.getSchemaInfo().getSortedParameterNamesCopy();
                for(int paramStep=0; paramStep<parameterNames.length; paramStep++ ){
                    String paramName = parameterNames[paramStep];
                    String paramType = parameterTypes.get(paramName);
                    //System.out.println(paramName);
                    //String type 
                    //String stepName = currentSeq.getSchemaInfo().getQuerySteps().get(step).getParameterName();
                    //String type = currentSeq.getSchemaInfo().getQuerySteps().get(step).getParameterType();
                    
                    Vector<DataRecord> vals = currentSeq.getValuesOfKey(paramName);
                    
                    for(int valIndex  =0; valIndex< vals.size(); valIndex++){
                        DataRecord rec = vals.get(valIndex);
                        String printStr = "\t\t"+paramName+": "+paramType+" -->\t"+rec.toString();
                        System.out.println(printStr);
                    }
                }
                
            }
            
        }
    }
    
    void printSimilaritiesData(SequenceSimilarityResultVector tripleVec){
     
        DecimalFormat df = new DecimalFormat("#.##");
        //System.out.println("tripleVec.size() " + tripleVec.size());
        int maxSeqCounter = this.userConfig.getNumberOfSequences();
        Vector<UserQueryConfiguration> udfQueries = this.userConfig.getUserQueriesCopy();
        for(int stepCounter=0; stepCounter< maxSeqCounter; stepCounter++){

            boolean seqNotFound = true;
            for(int i=0; i< tripleVec.size(); i++){
                SequenceSimilarityResult currentSeqResult = tripleVec.get(i);
                if(currentSeqResult.getSequenceId()!= stepCounter){
                    continue;
                }
                seqNotFound = false;
                System.out.println("\t\t\tsequence: " + (currentSeqResult.getSequenceId()+1)+" weight: "+currentSeqResult.getSequenceWeight()+" similarity: " + df.format(currentSeqResult.getSimilarity())+ " mnemonic: "+currentSeqResult.getSequenceMnemonic()  );
                System.out.println("\t\t\t\tSource Comparison Value: "+currentSeqResult.getParameterName()+": " + currentSeqResult.getSourceVal().toString());
                System.out.println("\t\t\t\t=====================================");
                System.out.println("\t\t\t\tTarget Comparison Value: "+currentSeqResult.getParameterName()+": " + currentSeqResult.getTargetVal().toString());
                System.out.println();

            }
            
            if(seqNotFound){
                System.out.println("\t\t\tsequence: " + (stepCounter+1)+   " weight: "+this.userConfig.getWeightAtUserQueryIndex(stepCounter) + " similarity: 0.0"+" mnemonic: "+this.userConfig.getMnemonicAtUserQueryIndex(stepCounter));
            }
        }
        
    }
    
}
