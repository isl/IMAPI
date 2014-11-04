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
import java.util.Comparator;

public class SourceInstancePair implements Comparator<SourceInstancePair>, Comparable<SourceInstancePair> {

    private String sourceName = "";
    private String instanceURI = "";

    public SourceInstancePair(String sName, String uri) {

        this.sourceName = sName == null ? "" : sName.trim();
        this.instanceURI = uri == null ? "" : uri.trim();
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public String getInstanceUri() {
        return this.instanceURI;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || (obj instanceof SourceInstancePair) == false) {
            return false;
        }

        SourceInstancePair otherObj = (SourceInstancePair) obj;
        if (this.instanceURI.equals(otherObj.getInstanceUri()) && this.sourceName.equals(otherObj.getSourceName())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.instanceURI + this.sourceName).hashCode();
    }

    @Override
    public int compareTo(SourceInstancePair o) {

        return this.compare(this, o);
    }

    @Override
    public int compare(SourceInstancePair o1, SourceInstancePair o2) {

        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        }

        if (o2 == null) {
            if (o1 != null) {
                return 1;
            }
        }

        if (o1.getSourceName().equals(o2.getSourceName())) {
            return o1.getInstanceUri().compareTo(o2.getInstanceUri());
        } else {
            return o1.getSourceName().compareTo(o2.getSourceName());
        }


    }

}
