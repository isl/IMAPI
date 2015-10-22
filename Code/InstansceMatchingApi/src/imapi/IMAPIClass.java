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
import com.hp.hpl.jena.util.FileManager;
import java.io.FileNotFoundException;
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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.atlas.io.IndentedWriter;

public class IMAPIClass {

    static boolean DEBUG = false;    
    static boolean nationalityTrans = false;    
    static boolean placesTrans = false;    
    //static boolean ExtentedMessagesEnabled = false;
    
    public void disableExtentedMessages() {
        IMAPIClass.DEBUG = false;
    }
    public void enableExtentedMessages() {
        IMAPIClass.DEBUG = true;
    }
    
    private int errCode = ApiConstants.IMAPIFailCode;
    private String errorMessage = "";

    public int getErrorCode() {
        return this.errCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;        
    }
    
    
    int getNumberOfSequences(){
        if(this.userConfig!=null){
            return this.userConfig.getNumberOfSequences();
        }
        else{
            return ApiConstants.IMAPIFailCode;
        }
    }
    
    double getWeightAtUserQueryIndex(int stepPosition){
        if(this.userConfig!=null){
            return this.userConfig.getWeightAtUserQueryIndex(stepPosition);
        }
        else{
            return 0d;
        }
    }
    
    String getMnemonicAtUserQueryIndex(int stepPosition){
        if(this.userConfig!=null){
            return this.userConfig.getMnemonicAtUserQueryIndex(stepPosition);
        }
        else{
            return "";
        }
    }
    
    public void enableNationalityTranslation() {
        IMAPIClass.nationalityTrans = true;
    }
    
    public void enablePlacesTranslation() {
        IMAPIClass.placesTrans = true;
    }

    //tools
    ApiConfigClass conf = null;
    UserConfigurationClass userConfig = null;
    QueryPrototypeConfigurationClass qWeightsConfig = null;
    NationalitiesFile nationalitiesFile = null;
    PlacesFile placesFile=null;
    
    /**
     * Do not forget to check IMAPIClass's getErrorCode() after initialization
     *
     * @param xmlConfigurationAbsolutePath Absolute path for the configuration
     * xml file
     *
     */
    public IMAPIClass(String baseFilePath, String userConfigurationXmlFile,String queriesConfigurationXmlFile) {

        System.out.println("New IMAPIClass instance created at time: "+java.util.Calendar.getInstance().getTime());
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
        
        try {
			nationalitiesFile= new NationalitiesFile();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.print("Error in new NationalitiesFile();");
		}
        
        try {
    			placesFile= new PlacesFile();
    		} catch (ParserConfigurationException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			System.out.print("Error in new PlacesFile();");
    		}

    }
    public static Vector<String> invalidTimespansDetected = new Vector<String>();
    
    public int performComparison(Hashtable<Float, Vector<ResultSourceTargetPair>> resultInstances) {

        invalidTimespansDetected = new Vector<String>();
        //Utilities
        DataRetrievalOperations retrieveData = new DataRetrievalOperations(this);
        BaseComparisonClass compClass = new BaseComparisonClass(this);
        
        //Internal structures used        
        SourceDataHolder inputSourceInfo = new SourceDataHolder();        
        Hashtable<SourceTargetPair, SequenceSimilarityResultVector> pairSimilaritiesInSequences = new Hashtable<SourceTargetPair, SequenceSimilarityResultVector>();
        
        //If Comaprison with online database is selected check if it is avaliable
        if(this.userConfig.getComparisonMode()!= ApiConstants.TargetSourceChoice.FILE_COMPARISON){
        
            OnlineDatabase db = this.conf.getOnlineDb(this.userConfig.getComparisonMode());
            
            if(db==null){
                this.setErrorMessage(ApiConstants.IMAPIFailCode, "Not supported Database");
                return ApiConstants.IMAPIFailCode;
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
            try{
                int ret = retrieveData.retrieveDataFrom_SourceFile(inputFile, allRetrievedModels, inputSourceInfo);            

                if(ret!=ApiConstants.IMAPISuccessCode){
                    return ret;
                }
            }
            catch(FileNotFoundException ex){
                Utilities.handleException(ex);
                return ApiConstants.IMAPIFailCode;
            }
            if(inputFileIndex < (inputFiles.size()-1));
            System.out.println("=======================================");
        }
        
        int totalNumberOfSourceInstanceValuesFound = 0;
        Enumeration<String> fileEnum = inputSourceInfo.keys();
        while(fileEnum.hasMoreElements()){
            String fpath = fileEnum.nextElement();
            totalNumberOfSourceInstanceValuesFound += inputSourceInfo.get(fpath).keySet().size();
        }
        
        //print what data was found in source files
        System.out.println("\n\n============================================================");
        System.out.println("============================================================");
        System.out.println("============================================================");
        System.out.println("Found " + totalNumberOfSourceInstanceValuesFound + " instances in all \"\"SOURCE\"\" input files.");
        System.out.println("\n\n======================= SKIPING PRINTING OF SOURCE DATA =======================\n\n");
        //printSourceInfo(inputSourceInfo);
        
        //remove from source structure all records that do not have a chance 
        //of qualifying the threshold
        //This case occurs if the sum of the weight of the sequences found
        //for one instance is not greater than threshold
        removeSourceDataThatWillNotQualifyThreshold(inputSourceInfo);
        
        
        /*
        if(IMAPIClass.DEBUG){
            
            if(inputSourceInfo!=null){
                System.out.println("Found: " + inputSourceInfo.size() +" instances.");
                return ApiConstants.IMAPISuccessCode;
            }
        }
        
        */
        //make an analysis if quick method can be followed
        Utilities u = new Utilities(this);
        Vector<Boolean>  canQuickFilteringMethodBeFollowedForeachSequence = new Vector<Boolean>();        
        u.canAllSequencesFollowFastApproach(this.userConfig.getUserQueriesCopy(), inputSourceInfo, canQuickFilteringMethodBeFollowedForeachSequence);
        
        //Read Info from Target Files or Online Database
        System.out.println("\n=======================================");
        System.out.println("=======================================");
        System.out.println("=======================================\n\n");
        
       
        
        ApiConstants.TargetSourceChoice comparisonChoice  = this.userConfig.getComparisonMode();             
        System.out.println("Starting queries on TARGET source: "+comparisonChoice.toString()+"\n\n");
        boolean targetDataCollectedCorrectly = false;
        switch(comparisonChoice){
            case FILE_COMPARISON:{
                Vector<CidocCrmCompatibleFile> targetFiles = this.userConfig.getTargetInputFiles();
                for(int targetFileIndex =0; targetFileIndex<targetFiles.size(); targetFileIndex++){

                    CidocCrmCompatibleFile targetFile = targetFiles.get(targetFileIndex);
                    try{
                        int ret = retrieveData.retrieveDataFrom_TargetFileAndCollectSimilarities(targetFile, allRetrievedModels, inputSourceInfo,canQuickFilteringMethodBeFollowedForeachSequence, pairSimilaritiesInSequences);
                        if(ret==ApiConstants.IMAPISuccessCode){
                            targetDataCollectedCorrectly = true;
                        }
                        else{
                            return ret;
                        }   
                    }
                    catch(FileNotFoundException ex){
                        Utilities.handleException(ex);
                        return ApiConstants.IMAPIFailCode;
                    }
                    
                }                
                break;
            }
            // a target online db is selected
            default:{
                OnlineDatabase db = this.conf.getOnlineDb(comparisonChoice);
                if(db==null){
                    this.setErrorMessage(ApiConstants.IMAPIFailCode, "TargetSourceChoice not set correctly check user configuration xml file again.");
                    return ApiConstants.IMAPIFailCode;
                }
                else{
                    
                    /*
                    if(db.getDbType().equals("owlim")){
                        int ret = retrieveData.retrieveDataFrom_OWLIM_DB(db,allRetrievedModels, inputSourceInfo, pairSimilaritiesInSequences);
                        if(ret==ApiConstants.IMAPISuccessCode){
                            targetDataCollectedCorrectly = true;
                        }
                    }
                    else{
                        */
                        int ret = retrieveData.retrieveDataFrom_OnlineDatabaseAndCollectSimilarities(db, allRetrievedModels, inputSourceInfo,canQuickFilteringMethodBeFollowedForeachSequence, pairSimilaritiesInSequences);
                        if(ret==ApiConstants.IMAPISuccessCode){
                            targetDataCollectedCorrectly = true;
                        }
                    //}
                    
                    
                }
                break;
                
            }
        }
        
        inputSourceInfo = null;
        
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
                
                
                float floatVal = (float)((float) calculatedSimilarity / 100f);
                SequenceSimilarityResultVector tripVec = pairSimilaritiesInSequences.get(pair);
                
                if (resultInstances.containsKey(floatVal)) {
                    ResultSourceTargetPair newVal = new ResultSourceTargetPair(pair, tripVec);                    
                    resultInstances.get(floatVal).add(newVal);
                } else {
                    Vector<ResultSourceTargetPair> newVal = new Vector<ResultSourceTargetPair>();                    
                    newVal.add(new ResultSourceTargetPair(pair, tripVec));
                    resultInstances.put(floatVal, newVal);                    
                }
                
            }
        }
        
        
        
        
        if(targetDataCollectedCorrectly==false){
            System.out.println("Note that while collected data from target Error Occurred");
            System.out.println("errorMessage: "+this.errorMessage);
        }
        if(invalidTimespansDetected.size()>0){
            System.out.println("The following invalid timespans were detected:\n-----------------------------------------------\n\n");
            Collections.sort(invalidTimespansDetected);
            for(String str: invalidTimespansDetected){
                System.out.println(str);
            }
        }
        
        return ApiConstants.IMAPISuccessCode;
    }
    
    public void printResultInstances(Hashtable<Float, Vector<ResultSourceTargetPair>> resultInstances){
        
        int resultsCounter = 0;
        if (resultInstances.size() == 0) {
            System.out.println("0 results found.");
        } else {
            
            Vector<Float> sortBySimilarityVec = new Vector<Float>(resultInstances.keySet());
            Collections.sort(sortBySimilarityVec);
            Collections.reverse(sortBySimilarityVec);
            
            for (int i = 0; i < sortBySimilarityVec.size(); i++) {
                float sim = sortBySimilarityVec.get(i);
                Vector<ResultSourceTargetPair> stPairs = resultInstances.get(sim);
                Collections.sort(stPairs);
                
                for (int k = 0; k < stPairs.size(); k++) {
                    ResultSourceTargetPair resultInfo = stPairs.get(k);
                    SourceTargetPair pair = resultInfo.getSourceTargetPair();
                    SequenceSimilarityResultVector tripVec = resultInfo.getSimilarityResultsVector();
                    
                    System.out.println((++resultsCounter) + ".\t" +  Utilities.df.format(sim) + "\t" + pair.getSourceInstance().getSourceName() + "    " + pair.getSourceInstance().getInstanceUri() + "    " + pair.getTargetInstance().getSourceName() + "    " + pair.getTargetInstance().getInstanceUri());
                    
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

    }
    
    void removeSourceDataThatWillNotQualifyThreshold(SourceDataHolder sourceInfo){
        
        double similarityDenominator = 0;
        
        
        
        int maxNumberOfQueriesIndex = this.userConfig.getNumberOfSequences();
        for (int i = 0; i < maxNumberOfQueriesIndex; i++) {            
            similarityDenominator += this.userConfig.getWeightAtUserQueryIndex(i);
        }
        
        Enumeration<String> fileEnum = sourceInfo.keys();
        while(fileEnum.hasMoreElements()){
            String fpath = fileEnum.nextElement();

            
            Enumeration<String> fileUris = sourceInfo.get(fpath).keys();
            int howmanyCleared = 0;
            while(fileUris.hasMoreElements()){
                String uri = fileUris.nextElement();
                SequencesVector seqVec = sourceInfo.get(fpath).get(uri);
                
                Double maximumSimilarityValue =0d;
                
                for(SequenceData seqData : seqVec){
                    maximumSimilarityValue+=seqData.getSchemaInfo().getWeight(); 
                }
                
                if((maximumSimilarityValue/similarityDenominator)<this.userConfig.getResultsThreshold()){
                    //do not remove uri as we might also find such a similarity
                    sourceInfo.get(fpath).get(uri).clear();
                    howmanyCleared++;
                }
            }
            
            System.out.println("Removed "+howmanyCleared+" values from input file "+ fpath+" as they will never pass the threshold");
            
            
        }
    }
    
    void printSourceInfo(SourceDataHolder sourceInfo){
        
        int counter = 0;
        Vector<String> fileInstances = new Vector<String>(sourceInfo.keySet());
        Collections.sort(fileInstances);
        
        //System.out.println("Found " + fileInstances.size() + " instances in all \"\"SOURCE\"\" input files.");
        for (int i = 0; i < fileInstances.size(); i++) {
            String filename = fileInstances.get(i);
            Vector<String> uris = new Vector<String>(sourceInfo.get(filename).keySet());
            Collections.sort(uris);
            
            for(int j=0 ; j< uris.size(); j++){
                counter ++;
                String uri = uris.get(j);
                SequencesVector allSeqData = sourceInfo.get(filename).get(uri);
                System.out.println("\r\n" + counter + ". " + uri + "\t\tin source: " + filename);
            
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
    }
    
    void printSimilaritiesData(SequenceSimilarityResultVector tripleVec){
     
        int maxSeqCounter = this.userConfig.getNumberOfSequences();
        
        for(int stepCounter=0; stepCounter< maxSeqCounter; stepCounter++){

            boolean seqNotFound = true;
            for(int i=0; i< tripleVec.size(); i++){
                SequenceSimilarityResult currentSeqResult = tripleVec.get(i);
                if(currentSeqResult.getSequenceId()!= stepCounter){
                    continue;
                }
                seqNotFound = false;
                System.out.println("\t\t\tsequence: " + (currentSeqResult.getSequenceId()+1)+" weight: "+currentSeqResult.getSequenceWeight()+" similarity: " + Utilities.df.format(currentSeqResult.getSimilarity().floatValue())+ " mnemonic: "+currentSeqResult.getSequenceMnemonic()  );
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
