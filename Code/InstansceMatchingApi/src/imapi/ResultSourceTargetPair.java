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

//should have all data needed for each integer similarity
import imapi.SequenceSimilarityResultVector;
import java.util.Comparator;

public class ResultSourceTargetPair implements Comparator<ResultSourceTargetPair>, Comparable<ResultSourceTargetPair> {

    private SourceTargetPair privatePair;
    private SequenceSimilarityResultVector privateTripVec;

    public ResultSourceTargetPair(SourceTargetPair pair, SequenceSimilarityResultVector tripVec) {
        privatePair = pair;
        privateTripVec = tripVec;
    }

    public SourceTargetPair getSourceTargetPair() {
        return this.privatePair;
    }

    public SequenceSimilarityResultVector getSimilarityResultsVector() {
        return this.privateTripVec;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || (obj instanceof ResultSourceTargetPair) == false) {
            return false;
        }

        ResultSourceTargetPair otherObj = (ResultSourceTargetPair) obj;

        return this.getSourceTargetPair().equals(otherObj.getSourceTargetPair());

    }

    @Override
    public int hashCode() {
        return this.getSourceTargetPair().hashCode();
    }

    @Override
    public int compareTo(ResultSourceTargetPair o) {
        return this.compare(this, o);
    }

    @Override
    public int compare(ResultSourceTargetPair o1, ResultSourceTargetPair o2) {

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

        return o1.getSourceTargetPair().compareTo(o2.getSourceTargetPair());

    }
}
