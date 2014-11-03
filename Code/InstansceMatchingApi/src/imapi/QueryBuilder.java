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

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sun.security.krb5.internal.APReq;

/**
 *
 * @author tzortzak
 */
class QueryBuilder {
    
    static final String filterUriPlaceHolder = "###FILTERURISTATEMENT###";
    
    private static final String endLine = "\r\n";
    private static final String tabSpace ="    ";
    IMAPIClass imClass = null;
    
    QueryBuilder(IMAPIClass whichImClass){
        this.imClass = whichImClass;          
    }
    
    /*
    String get_All_Instances_Query(String targetCidocNameSpace, String targetClass, boolean includeOutputLabel){
        String basicQuery ="";
        
            //namespaces
            basicQuery += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + endLine;
            basicQuery += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + endLine;
            basicQuery += "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" + endLine;
            basicQuery += "PREFIX crm: <" + targetCidocNameSpace + ">" + endLine + endLine;

            //select case
            basicQuery += "SELECT DISTINCT";
            basicQuery += " ?resultInstaceUri";
            if(includeOutputLabel){
                basicQuery+=" ?resultInstaceLabel";
            }

            basicQuery += endLine;
            //where case
            basicQuery += "WHERE" + endLine;
            basicQuery += "{" + endLine;
            basicQuery += "    ?resultInstaceUri a crm:"+targetClass+" ." + endLine;
            
            if(includeOutputLabel){
                basicQuery += tabSpace+"OPTIONAL { ?resultInstaceUri rdfs:label ?resultInstaceLabel . } " + endLine;
            }
            basicQuery += "}";
        
            System.out.println(basicQuery);
        
        return basicQuery;
    }
    */
    
    private void getClassIdentifiersOfRawQueries(Vector<String> queries, Hashtable<String, Vector<String>> classIds) {

        for (int qIndex = 0; qIndex < queries.size(); qIndex++) {

            String query = queries.get(qIndex);

            List<String> matchList = new ArrayList<String>();
            try {
                Pattern regex = Pattern.compile("###CID:[^#]+###", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                Matcher regexMatcher = regex.matcher(query);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group().replaceAll("###", ""));
                }
            } catch (PatternSyntaxException ex) {
                // Syntax error in the regular expression
            }
            try {
                Pattern regex = Pattern.compile("###PID:[^#]+###", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                Matcher regexMatcher = regex.matcher(query);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group().replaceAll("###", ""));
                }
            } catch (PatternSyntaxException ex) {
                // Syntax error in the regular expression
            }

            for (int i = 0; i < matchList.size(); i++) {
                if (classIds.containsKey(matchList.get(i)) == false) {
                    //System.out.println("Adding " + matchList.get(i));
                    classIds.put(matchList.get(i), new Vector<String>());
                }
            }
        }

    }

    private String retrieveDirectOrInversePredicate(String predIdentifier,boolean preferInverse, 
            Vector<String> predicateNames, Hashtable<String, String> validCidocNamespacesDeclared) {
        if (predicateNames == null || predicateNames.size() == 0) {
            return "";
        }

        ApiConstants.TargetSourceChoice currenctChoice = this.imClass.userConfig.getComparisonMode();
        String inverseStr = "";
        String returnStr = "";
        for (int i = 0; i < predicateNames.size(); i++) {

            
            String str = predicateNames.get(i);
            
            Enumeration<String> validCidocNamespacesDeclaredEnum = validCidocNamespacesDeclared.keys();
            while (validCidocNamespacesDeclaredEnum.hasMoreElements()) {
                String tempStr = str;
                String checkNsPrefix = validCidocNamespacesDeclaredEnum.nextElement();
                String checkNs = validCidocNamespacesDeclared.get(checkNsPrefix);
                
                tempStr = tempStr.replace(checkNs, "");
                if(tempStr.startsWith(predIdentifier) ==false 
                        && currenctChoice== ApiConstants.TargetSourceChoice.CLAROS 
                        && checkNs.equals("http://purl.org/NET/crm-owl#")){
                    tempStr = tempStr.replace("http://erlangen-crm.org/091217/", "");
                }
                if (tempStr.startsWith(predIdentifier)) {
                    char separatorOrInverseIndicator = tempStr.toLowerCase().charAt(tempStr.indexOf(predIdentifier) + predIdentifier.length());

                    if (Character.isDigit(separatorOrInverseIndicator)) {
                        continue;
                    }

                    if (separatorOrInverseIndicator == 'i' || separatorOrInverseIndicator == 'b') {
                        inverseStr = checkNsPrefix + ":" + tempStr;
                        break;
                    } else {
                        returnStr = checkNsPrefix + ":" + tempStr;
                        break;
                    }
                }

            }

        }
        
        if(returnStr.length()==0 && inverseStr.length()==0 && predicateNames.size()>0){
            String tempStr = predicateNames.get(0);
            if(tempStr.length()>0){
                return "<"+tempStr+">";
            }
        }

        if(preferInverse){
            if(inverseStr.length()>0){
                return inverseStr;
            }
        }
        
        if (returnStr.length() > 0) {
            return returnStr;
        } else {
            return inverseStr;
        }
    }

    
    int prepareQueries(Hashtable<String, String> validCidocNamespacesDeclared, Model targetModel, ApiConstants.PredicateDirectionUsage predicateDirection, StringObject allInstancesQuery, Vector<UserQueryConfiguration> sequences) {

        if(IMAPIClass.ExtentedMessagesEnabled){
            System.out.println("Retrieving Class and Predicate Names that will be used in the queries");
        }
        ApiConstants.TargetSourceChoice currentChoice = this.imClass.userConfig.getComparisonMode();
        allInstancesQuery.setString(this.imClass.qWeightsConfig.getTheAllInstancesQuery());
        
        //sequences.clear();
        //sequences.addAll(this.imClass.qWeightsConfig.getUserSequences());
        
        Vector<String> allStrsForTranslation = new Vector<String>();
        allStrsForTranslation.add(allInstancesQuery.getString());
        
        for (int seqIndex = 0; seqIndex < sequences.size(); seqIndex++) {
            String[] stepQueries = sequences.get(seqIndex).getSortedQueriesCopy();
            for (int stepIndex = 0; stepIndex < stepQueries.length; stepIndex++) {
                allStrsForTranslation.add(stepQueries[stepIndex]);
            }            
        }
        
        
        //ask model for classes declared with this id-->e.g. CID:39
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF, targetModel);
        

        
        
        Hashtable<String, Vector<String>> queryClassAndPredicateReplacementsFound = new Hashtable<String, Vector<String>>();
        this.getClassIdentifiersOfRawQueries(allStrsForTranslation, queryClassAndPredicateReplacementsFound);

        Vector<String> classesThatShouldBeFound = new Vector<String>();
        Vector<String> predicatesThatShouldBeFound = new Vector<String>();
        {
            Enumeration<String> classEnum = queryClassAndPredicateReplacementsFound.keys();
            while(classEnum.hasMoreElements()){
                String newKey = classEnum.nextElement();
                if(newKey.startsWith("CID:")){
                    if(classesThatShouldBeFound.contains(newKey)==false){
                        classesThatShouldBeFound.add(newKey);
                    }
                }
                else if(newKey.startsWith("PID:")){
                    if(predicatesThatShouldBeFound.contains(newKey)==false){
                        predicatesThatShouldBeFound.add(newKey);
                    }
                }
            }
        }
        
        //Run through all classes of cidoc-models 
        //System.out.println("Run through all classes of cidoc-models");
        ExtendedIterator<OntClass> clIter = ontModel.listClasses();
        while (clIter.hasNext()) {

            if(classesThatShouldBeFound.size()==0){
                break;
            }
            
            
            OntClass clClass = clIter.next();
            String clName = clClass.getLocalName();

            if (clName == null) {
                continue;
            }
            
            if(currentChoice == ApiConstants.TargetSourceChoice.CLAROS){
                clName = clName.replace(".", "_");
            }
            
            Enumeration<String> queryIdentifiersIter = queryClassAndPredicateReplacementsFound.keys();
            while (queryIdentifiersIter.hasMoreElements()) {
                String baseIdentifier = queryIdentifiersIter.nextElement();
                String regexUsed = "^";
                if (baseIdentifier.startsWith("CID:")) {
                    regexUsed += baseIdentifier.replace("CID:", "");
                } else {
                    continue;
                }
                regexUsed += "[^0-9]+.*";

                if (clName.matches(regexUsed)) {
                    //System.out.println("Found class: " + clClass.getNameSpace() + clName);
                    queryClassAndPredicateReplacementsFound.get(baseIdentifier).add(clClass.getNameSpace() + clName);
                    classesThatShouldBeFound.remove(baseIdentifier);
                    
                }
            }

        }
        
        //determine if i need to extract both direct and inverse predicates
        boolean useOnlyOneDirection = (predicateDirection != ApiConstants.PredicateDirectionUsage.BOTH) ;
        boolean inverseInsteadOfDirectPredicates = (predicateDirection == ApiConstants.PredicateDirectionUsage.INVERSE);
        
        
        Vector<Property> allProperties = new Vector<Property>();
        ExtendedIterator<ObjectProperty> ObjPropIter = ontModel.listObjectProperties();
        while (ObjPropIter.hasNext()) {
            ObjectProperty ontProp = ObjPropIter.next();
            if(ontProp.getLocalName()==null){
                continue;
            }
            /*
            if(ontProp.getLocalName().startsWith("P82a")){
                    System.out.println("DEBUGH");
                }*/

            Property prop = (Property) ontProp;
            if(allProperties.contains(prop)==false){
                allProperties.add(prop);
                if(IMAPIClass.DEBUG){
                    //System.out.println("Adding "+prop.getLocalName());
                }
            }            
        }
            
        
            
        
        for(int i=0;i<allProperties.size(); i++){
            if(predicatesThatShouldBeFound.size()==0){
                break;
            }
            String str = allProperties.get(i).getLocalName();
            
            if(currentChoice == ApiConstants.TargetSourceChoice.CLAROS){
                
                str = str.replace("F.", "_");
                str = str.replace("B.", "i_");
                str = str.replace("I.", "i_");
                str = str.replace(".", "_");
            }
            int indexFound = -1;
            for(int k=0; k<predicatesThatShouldBeFound.size();k++){
                
                String predSearch = predicatesThatShouldBeFound.get(k);
                String regexUsed = "^";
                if (predSearch.startsWith("PID:")) {
                    regexUsed += predSearch.replace("PID:", "");
                } else {
                    continue;
                }
                regexUsed += "[^0-9]+.*";
                
                if (str.matches(regexUsed)){
                    indexFound = k;
                    break;
                }
                else if(str.equals(predSearch.replace("PID:", ""))){
                    indexFound = k;
                    break;
                }
            }
            
            if(indexFound>=0){
                predicatesThatShouldBeFound.remove(indexFound);
            }
            
        }
        
        if(predicatesThatShouldBeFound.size()>0){
            
            ExtendedIterator<OntProperty> OntPropIter = ontModel.listOntProperties();
            while (OntPropIter.hasNext()) {
                OntProperty ontProp = OntPropIter.next();
                if(ontProp.getLocalName()==null){
                    continue;
                }
                /*
                if(ontProp.getLocalName().startsWith("P82a")){
                    System.out.println("DEBUGH");
                }*/
                Property prop = (Property) ontProp;
                if(allProperties.contains(prop)==false){
                    allProperties.add(prop);
                    if(IMAPIClass.DEBUG){
                        //System.out.println("Adding "+prop.getLocalName());
                    }
                }            

            }
        }
        
        for(int i=0;i<allProperties.size(); i++){
            if(predicatesThatShouldBeFound.size()==0){
                break;
            }
            String str = allProperties.get(i).getLocalName();
            
            if(currentChoice == ApiConstants.TargetSourceChoice.CLAROS){
                
                str = str.replace("F.", "_");
                str = str.replace("B.", "i_");
                str = str.replace("I.", "i_");
                str = str.replace(".", "_");
            }
            int indexFound = -1;
            for(int k=0; k<predicatesThatShouldBeFound.size();k++){
                
                String predSearch = predicatesThatShouldBeFound.get(k);
                String regexUsed = "^";
                if (predSearch.startsWith("PID:")) {
                    regexUsed += predSearch.replace("PID:", "");
                } else {
                    continue;
                }
                regexUsed += "[^0-9]+.*";
                
                if (str.matches(regexUsed)){
                    indexFound = k;
                    break;
                }
                else if(str.equals(predSearch.replace("PID:", ""))){
                    indexFound = k;
                    break;
                }
            }
            
            if(indexFound>=0){
                predicatesThatShouldBeFound.remove(indexFound);
            }
            
        }
        
        if(predicatesThatShouldBeFound.size()>0){
            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode, "Could not find predicate identifier for predicates with ids:\n "+predicatesThatShouldBeFound.toString().replace("[", "").replace("]", "").replace(",", "\n"));
            return ApiConstants.IMAPIFailCode;
        }
        
        //Run through all properties of cidoc-models
        //System.out.println("Run through all properties of cidoc-models");
        //ExtendedIterator<ObjectProperty> propIter = ontModel.listObjectProperties();
        /*if(currentChoice!= ApiConstants.TargetSourceChoice.CLAROS){
            propIter = ontModel.listOntProperties();
        }
        else{
            
        }*/
        
        for(int i=0; i<allProperties.size(); i++) {
                        
            Property ontProp = allProperties.get(i);
            String prName = ontProp.getLocalName();
            //System.out.println(prName);
            if (prName == null) {
                continue;
            }
            
            if(currentChoice == ApiConstants.TargetSourceChoice.CLAROS){
                
                prName = prName.replace("F.", "_");
                prName = prName.replace("B.", "i_");
                prName = prName.replace("I.", "i_");
                prName = prName.replace(".", "_");
            }

            Enumeration<String> queryIdentifiersIter = queryClassAndPredicateReplacementsFound.keys();
            while (queryIdentifiersIter.hasMoreElements()) {
                String baseIdentifier = queryIdentifiersIter.nextElement();
                String regexUsed = "^";
                if (baseIdentifier.startsWith("PID:")) {
                    regexUsed += baseIdentifier.replace("PID:", "");
                } else {
                    continue;
                }
                regexUsed += "[^0-9]+.*";

                if (prName.matches(regexUsed)) {

                    /*
                    if(prName.startsWith("P82a")){
                        System.out.println("DEBUGH");
                    }*/
                    //System.out.println("Found property: " + ontProp.getNameSpace() + prName);
                    queryClassAndPredicateReplacementsFound.get(baseIdentifier).add(ontProp.getNameSpace() + prName);                        
                    /*
                    String predIdentifier = baseIdentifier.replace("PID:", "");
                    {                        
                        
                        int charIndex = prName.indexOf(predIdentifier) + predIdentifier.length();
                        if(prName.length()<=charIndex){
                            this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode,
                                "Unexpected Error when searching for predId: " + predIdentifier + ".");
                            return ApiConstants.IMAPIFailCode;
                        }

                        char separatorOrInverseIndicator = prName.toLowerCase().charAt(charIndex);


                        if (separatorOrInverseIndicator == 'i' || separatorOrInverseIndicator == 'b') {
                            isInverse = true;
                        }
                    }*/
                    
                }
                else if (prName.equals(baseIdentifier.replace("PID:", ""))){
                    queryClassAndPredicateReplacementsFound.get(baseIdentifier).add(ontProp.getNameSpace() + prName);     
                }
            }
        }
        
        
        
        
        //System.out.println("Run through all properties of cidoc-models Ended");
        

        Vector<String>printReplacements = new Vector<String>();
        
        for (int qIndex = 0; qIndex < allStrsForTranslation.size(); qIndex++) {
            String returnStr = allStrsForTranslation.get(qIndex);
            String initialStr = new String(returnStr);
            //Replace Classes in the query
            Enumeration<String> queryIdentifiersIter = queryClassAndPredicateReplacementsFound.keys();
            while (queryIdentifiersIter.hasMoreElements()) {
                String newCid = queryIdentifiersIter.nextElement();
                if (newCid.toLowerCase().startsWith("cid:") == false) {
                    continue;
                }
                Vector<String> translationsFound = queryClassAndPredicateReplacementsFound.get(newCid);
                String trUsed = "";
                if (translationsFound == null || translationsFound.size() == 0) {
                    this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode,
                            "Did not find any CIDOC-CRM compatible interpretation for query paramter: " + newCid + ".");
                    return ApiConstants.IMAPIFailCode;
                } else if (translationsFound.size() > 1) {
                    this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode,
                            "Multiple CIDOC-CRM compatible interpretations for query paramter: " + newCid + ".");
                    return ApiConstants.IMAPIFailCode;
                }

                trUsed = translationsFound.get(0);
                String replaceMent = "";

                //for each class find the namespace prefix
                Enumeration<String> validCidocNamespacesDeclaredEnum = validCidocNamespacesDeclared.keys();
                while (validCidocNamespacesDeclaredEnum.hasMoreElements()) {
                    String checkNsPrefix = validCidocNamespacesDeclaredEnum.nextElement();
                    String checkNs = validCidocNamespacesDeclared.get(checkNsPrefix);
                    
                    if(trUsed.startsWith(checkNs) ==false && 
                            currentChoice == ApiConstants.TargetSourceChoice.CLAROS && 
                            checkNs.equals("http://purl.org/NET/crm-owl#") ){
                        checkNs = "http://erlangen-crm.org/091217/";
                    }
                    
                    if (trUsed.startsWith(checkNs)) {
                        replaceMent = checkNsPrefix + ":" + trUsed.replace(checkNs, "");
                        break;
                    }
                    
                }

                if(replaceMent.length()>0 && returnStr.contains("###" + newCid + "###")){
                    String action = "Replacing "+newCid +"\t with "+replaceMent;
                    if(printReplacements.contains(action)==false){
                        printReplacements.add(action);
                    }
                    while (returnStr.contains("###" + newCid + "###")) {
                        returnStr = returnStr.replace("###" + newCid + "###", replaceMent);
                    }
                }

            }

            //clear whitespace 
            while (returnStr.contains("\t")) {
                returnStr = returnStr.replace("\t", " ");
            }

            //replace predicates in the query
            queryIdentifiersIter = queryClassAndPredicateReplacementsFound.keys();
            while (queryIdentifiersIter.hasMoreElements()) {
                String newPid = queryIdentifiersIter.nextElement();
                if (newPid.toLowerCase().startsWith("pid:") == false) {
                    continue;
                }

                Pattern generalregex = Pattern.compile("[^ ]* +###"+newPid+"### +[^ ]* *\\.?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                while (generalregex.matcher(returnStr).find()) {

                    String ResultString = null;
                    try {
                        //Pattern regex = Pattern.compile("[^ ]* +###PID:[^#]*### +[^ ]*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        Pattern regex = Pattern.compile("[^ ]* +###"+newPid+"### +[^ ]* *\\.?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        Matcher regexMatcher = regex.matcher(returnStr);
                        if (regexMatcher.find()) {
                            ResultString = regexMatcher.group();

                        }
                    } catch (PatternSyntaxException ex) {
                        // Syntax error in the regular expression
                    }

                    if (ResultString == null) {
                        continue;
                    }
                    String[] parts = ResultString.split(" ");
                    if (parts == null || (parts.length != 3 && parts.length != 4)) {
                        this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode,
                                "Error While trying to retrieve the statement parts of " + newPid + ".");
                        return ApiConstants.IMAPIFailCode;
                    }
                    String sbj = parts[0];
                    String pred = parts[1]; //.replaceAll("###","");            
                    String obj = parts[2];

                    if (pred.contains("###" + newPid + "###") == false) {
                        //System.out.println("Unexpected Error Occured at interpretation of "+newPid);
                        continue;
                    }

                    Vector<String> translationsFound = queryClassAndPredicateReplacementsFound.get(newPid);
                    String trUsed = "";
                    if (translationsFound == null || translationsFound.size() == 0) {
                        this.imClass.setErrorMessage(ApiConstants.IMAPIFailCode,
                                "Did not find any CIDOC-CRM compatible interpretation for predicate query paramter: " + newPid + ".");
                        return ApiConstants.IMAPIFailCode;
                    }

                    String printReplacementsStr = "Replacing "+newPid;
                    
                    trUsed = this.retrieveDirectOrInversePredicate(newPid.replace("PID:", ""), inverseInsteadOfDirectPredicates, translationsFound, validCidocNamespacesDeclared);
                
                    printReplacementsStr += "\t with " +trUsed;
                    String replacementStr = "\n    {" + sbj + " " + trUsed + " " + obj + " . }\n";
                    replacementStr += "    UNION\n";
                    replacementStr += "    {" + obj + " " + trUsed + " " + sbj + " . }\n";
                    if(predicateDirection==ApiConstants.PredicateDirectionUsage.BOTH){
                        String otherDirection = this.retrieveDirectOrInversePredicate(newPid.replace("PID:", ""), !inverseInsteadOfDirectPredicates, translationsFound, validCidocNamespacesDeclared);
                        if(otherDirection.equals(trUsed)==false){
                            printReplacementsStr += " and " +otherDirection;
                            replacementStr += "    UNION\n";
                            replacementStr += "    {" + sbj + " " + otherDirection + " " + obj + " . }\n";
                            replacementStr += "    UNION\n";
                            replacementStr += "    {" + obj + " " + otherDirection + " " + sbj + " . }\n";
                        }
                    }
                    
                    

                    if(returnStr.contains(ResultString)){
                        
                        if(printReplacements.contains(printReplacementsStr)==false){
                            printReplacements.add(printReplacementsStr);
                        }
                        
                        while (returnStr.contains(ResultString)) {
                            returnStr = returnStr.replace(ResultString, replacementStr);
                        }
                    }

                }

            }

            if(allInstancesQuery.getString().trim().equals(initialStr.trim())){
                allInstancesQuery.setString(returnStr);
            }
            
            
            for (int seqIndex = 0; seqIndex < sequences.size(); seqIndex++) {                
                
                sequences.get(seqIndex).replaceQueryInQuerySteps(initialStr, returnStr);
                
            }
        
        }

        /*
        for (int qIndex = 0; qIndex < baseQueries.size(); qIndex++) {
            //String base = baseQueries.get(qIndex);
            String returnStr = returnStrs.get(qIndex);
            //System.out.println("\n\n############################################\nInput Query:");
            //System.out.println(base);
            System.out.println("\n==============================================\nOutput Query:");
            System.out.println(returnStr);

            System.out.println();
        }
*/
        //Vector<String> getVec = new Vector<String>(ontModel.listImportedOntologyURIs());
        //for(int p = 0; p<getVec.size(); p++)
        //{
        //    String targetClass = getVec.get(p);
        //    System.out.println(p+".\t"+ targetClass);
        //}
        Collections.sort(printReplacements);
        
        //System.out.println("Class and Predicate Names retrieval ended.");
        if(IMAPIClass.ExtentedMessagesEnabled){
            System.out.println(" " + printReplacements.toString().replace("[", "").replace("]","").replaceAll(",", "\n").replaceAll(" +", " ").trim()+"\n");
        }
        return ApiConstants.IMAPISuccessCode;
    }
    
    static String replaceFilteringPlaceHoldersForInstances(String initialQuery,String currentStepParameterName,Vector<String> replacementValues){
        String returnStr = initialQuery;
        if(returnStr.contains(QueryBuilder.filterUriPlaceHolder) && replacementValues!=null && replacementValues.size()>0){
            
            String replaceMent = " FILTER ( ";
            for(int p =0; p< replacementValues.size(); p++){
                if(p>0){
                    replaceMent += " || \n ";
                }
                
                replaceMent +="?"+currentStepParameterName + " = <"+replacementValues.get(p)+">";
            }
            replaceMent +=" ) .\n";
            
            
            /*
            String replaceMent = "";
            for(int p =0; p< replacementValues.size(); p++){
                if(p>0){
                    replaceMent += " UNION \n";
                }
                replaceMent+=" { SELECT ?"+currentStepParameterName+" "+
                    " { "+
                    " ?"+currentStepParameterName + " ?anypred ?anyclass . ";//FILTER ( \n";
            
            
                
                replaceMent += " FILTER ( ?"+currentStepParameterName + " = <"+replacementValues.get(p)+"> ).  ";
                replaceMent +=" } }\n";
            }
            */
            returnStr = returnStr.replace(QueryBuilder.filterUriPlaceHolder, replaceMent);
        }
        else{
            returnStr = returnStr.replace(QueryBuilder.filterUriPlaceHolder, "");
        }
        return returnStr;
    }
    
    static String replaceFilteringPlaceHolders(String initialQuery,String currentStepParameterName, Vector<String> replacementValues){
        String returnStr = initialQuery;
        if(returnStr.contains(QueryBuilder.filterUriPlaceHolder) && replacementValues!=null && replacementValues.size()>0){
            
            String replaceMent = " FILTER ( ";
            for(int p =0; p< replacementValues.size(); p++){
                if(p>0){
                    replaceMent += " || \n ";
                }
                
                replaceMent +="?"+currentStepParameterName + " = <"+replacementValues.get(p)+">";
            }
            replaceMent +=" ) .\n";
            
            /*
            String replaceMent = "  {  SELECT ?"+currentStepParameterName+" \n"+
                    "     { \n"+
                    "         ?"+currentStepParameterName + " a ?anyclass . \n";//FILTER ( \n";
            
            for(int p =0; p< replacementValues.size(); p++){
                if(p>0){
                    replaceMent += "         UNION \n";
                }
                replaceMent += "         { FILTER ( ?"+currentStepParameterName + " = <"+replacementValues.get(p)+"> ). } \n";
            }
            
            replaceMent +="     } \n  }\n";
            */
            /*
            String replaceMent = "";
            for(int p =0; p< replacementValues.size(); p++){
                if(p>0){
                    replaceMent += " UNION \n";
                }
                replaceMent+=" { SELECT ?"+currentStepParameterName+" "+
                    " { "+
                    " ?"+currentStepParameterName + " a ?anyclass . ";//FILTER ( \n";
            
            
                
                replaceMent += " FILTER ( ?"+currentStepParameterName + " = <"+replacementValues.get(p)+"> ).  ";
                replaceMent +=" } }\n";
            }
            */
            
            returnStr = returnStr.replace(QueryBuilder.filterUriPlaceHolder, replaceMent);
        }
        else{
            returnStr = returnStr.replace(QueryBuilder.filterUriPlaceHolder, "");
        }
        
        return returnStr;
    }
    
    static Vector<String> collectSequenctiallyAsubsetOfValues(int startindex, Vector<String> targetVals){
        Vector<String> returnVals = new Vector<String>();
        
        int maxIndex =targetVals.size(); 
        if(startindex<targetVals.size()){
            for(int i = 0; i< ApiConstants.UriFilterStepCount; i++){
                
                if((startindex+i)>=maxIndex){
                    break;
                }
                else{
                    returnVals.add(targetVals.get(i+startindex));
                }
                
                
            }
        }
        
        return returnVals;
    }
}
