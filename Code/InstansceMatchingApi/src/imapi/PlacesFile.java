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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author edask
 */
class PlacesFile {

    private String XpahthPlacesDutch ="Root/places/translation/dutch/text()";
    private String XpahthPlacesEnglish ="Root/places/translation/english/text()";
    private String XpahthPlaces ="Root/places/translation";
    private DocumentBuilder builder;
    private Document document;
    
    
    Hashtable<String,String> placesTrans = new Hashtable<String,String>();
    
    private final String OnlineElementDutchPlaces = "places";
    private final String OnlineElementEnglishPlaces = "translation";
    
    private int errCode = ApiConstants.IMAPISuccessCode;

    int getErrorCode() {
        return this.errCode;
    }
    private String errorMessage = "";

    String getErrorMessage() {
        return this.errorMessage;
    }

    private void setErrorMessage(int errorCode, String errorMsg) {
        this.errCode = errorCode;
        this.errorMessage = errorMsg;
        if (this.errCode == ApiConstants.IMAPIFailCode) {
            System.out.println("ERROR occurred: " + errorMsg);
        }
    }


    
    public PlacesFile() throws ParserConfigurationException {

       // this.setErrorMessage(ApiConstants.IMAPISuccessCode, "");

        try {
            this.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            
            this.document = builder.parse(ApiConfigClass.class.getResourceAsStream("/PlacesTranslation(TypeB).xml"));
            XPath xpath = XPathFactory.newInstance().newXPath();

            if (this.XpahthPlaces != null) {
                NodeList nodesDuch = (NodeList) xpath.evaluate(this.XpahthPlacesDutch, document, XPathConstants.NODESET);
                NodeList nodesEnglish = (NodeList) xpath.evaluate(this.XpahthPlacesEnglish, document, XPathConstants.NODESET);
                if (nodesDuch != null && nodesEnglish!=null) {

                    int howmanyNodes = nodesDuch.getLength();
                    
                    
                    for (int i = 0; i < howmanyNodes; i++) {
                    

                        String Dutch="", English="";
                        
                        Node xNodeDuch = nodesDuch.item(i);
                        Dutch=xNodeDuch.getNodeValue();
                        
                        Node xNodeEnglish = nodesEnglish.item(i);
                        try {
							English=xNodeEnglish.getNodeValue();
						} catch (DOMException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                        this.placesTrans.put(Dutch, English);
                       
                    }
                }
            }
            
        }  catch (SAXException e) {
            String tempMsg = "SAXException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            Utilities.handleException(e);
        } catch (IOException e) {
            String tempMsg = "IOException occured:\r\n" + e.getMessage();
            this.setErrorMessage(ApiConstants.IMAPIFailCode, tempMsg);
            Utilities.handleException(e);
        } catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    public String FindTranslation(String xpathS) throws XPathExpressionException{
    	
    	XPath xpath = XPathFactory.newInstance().newXPath();
    	String translation = "";
    	NodeList nodes = (NodeList) xpath.evaluate(this.XpahthPlaces+"[dutch='"+xpathS+"']/english/text()", this.document, XPathConstants.NODESET);
      if (nodes != null) {
          int howmanyNodes = nodes.getLength();
          
          
          for (int i = 0; i < howmanyNodes; i++) {
          
              Node xNode = nodes.item(i);
              translation=xNode.getNodeValue();

          }
      }
      else {
    	  nodes = (NodeList) xpath.evaluate(this.XpahthPlaces+"[english='"+xpathS+"']/dutch/text()", this.document, XPathConstants.NODESET);
          if (nodes != null) {
              int howmanyNodes = nodes.getLength();
              
              
              for (int i = 0; i < howmanyNodes; i++) {
              
                  Node xNode = nodes.item(i);
                  translation=xNode.toString();

              }
          }
      }

	return translation;
    }
}
