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
 * Evangelia Daskalaki <eva@ics.forth.gr>
 * Elias Tzortzakakis <tzortzak@ics.forth.gr>
 * 
 */
package imapi;

import java.util.ArrayList;

class StringSimilarity {

    private static int MATCH = 0,
            MISMATCH = 1,
            GAP = 1; // treating gap = mismatch

    float computePrefixSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0;
        }
        if (str1.startsWith(str2) || str2.startsWith(str1)) {
            return 1;
        }
        /*
         {
         int len1 = str1.length();
         int len2 = str2.length();
         return (len1<len2)?(float)len1/len2:(float)len2/len1;
         }
         */
        return 0;
    }

    float computeSuffixSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0;
        }
        if (str1.endsWith(str2) || str2.endsWith(str1)) {
            return 1;
        }
        /*
         {
         int len1 = str1.length();
         int len2 = str2.length();
         return (len1<len2)?(float)len1/len2:(float)len2/len1;
         }
         */
        return 0;
    }

    /*POSSIBLE ERROR IN THIS METHOD*/
    float computeAffixSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0;
        }

        if (str1.length() > 1 && str2.length() > 1) {
            return computeSuffixSimilarity(str1, str2);
        } else {
            return computePrefixSimilarity(str1, str2);
        }
    }

    float computeCharFrequencySimilarity(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return 0;
        }

        ArrayList charList1 = new ArrayList(), occList1 = new ArrayList();
        ArrayList charList2 = new ArrayList(), occList2 = new ArrayList();

        /* deriving occurences of characters from the first string */
        for (int i = 0; i < str1.length(); i++) {
            Character c = new Character(str1.charAt(i));
            int j = charList1.indexOf(c);
            if (j != -1) {
                int occ = ((Integer) occList1.get(j)).intValue() + 1;
                occList1.set(j, new Integer(occ));
            } else {
                charList1.add(c);
                occList1.add(new Integer(1));
            }
        }

        /* deriving occurences of characters from the second string */
        for (int i = 0; i < str2.length(); i++) {
            Character c = new Character(str2.charAt(i));
            int j = charList2.indexOf(c);
            if (j != -1) {
                int occ = ((Integer) occList2.get(j)).intValue() + 1;
                occList2.set(j, new Integer(occ));
            } else {
                charList2.add(c);
                occList2.add(new Integer(1));
            }
        }

        /* compute the similary based on character occurence lists */
        int matched = 0;
        for (int i = 0; i < charList1.size(); i++) {
            Character c = (Character) charList1.get(i);
            int j = charList2.indexOf(c);
            if (j != -1) {
                int a = ((Integer) occList1.get(i)).intValue();
                int b = ((Integer) occList2.get(j)).intValue();
                if (a <= b) {
                    matched += 2 * a;
                } else {
                    matched += 2 * b;
                }
            }
        }

        return (float) matched / (str1.length() + str2.length());
    }

    String[] generateNGrams(String str, int gramlength) {
        if (str == null || str.length() == 0) {
            return null;
        }

        ArrayList grams = new ArrayList();
        int length = str.length();
        String gram;

        if (length < gramlength) {
            for (int i = 1; i <= length; i++) {
                gram = str.substring(0, i);
                if (grams.indexOf(gram) == -1) {
                    grams.add(gram);
                }
            }

            gram = str.substring(length - 1, length);
            if (grams.indexOf(gram) == -1) {
                grams.add(gram);
            }
        } else {
            for (int i = 1; i <= gramlength - 1; i++) {
                gram = str.substring(0, i);
                if (grams.indexOf(gram) == -1) {
                    grams.add(gram);
                }
            }

            for (int i = 0; i < length - gramlength + 1; i++) {
                gram = str.substring(i, i + gramlength);
                if (grams.indexOf(gram) == -1) {
                    grams.add(gram);
                }
            }

            for (int i = length - gramlength + 1; i < length; i++) {
                gram = str.substring(i, length);
                if (grams.indexOf(gram) == -1) {
                    grams.add(gram);
                }
            }
        }
        return (String[]) grams.toArray(new String[0]);
    }

    float computeNGramSimilarity(String str1, String str2, int gramlength) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return 0;
        }
        String[] grams1 = generateNGrams(str1, gramlength);
        //for (int i=0; i<grams1.length; i++) System.out.print(grams1[i] + ";"); System.out.println();
        String[] grams2 = generateNGrams(str2, gramlength);
        //for (int i=0; i<grams2.length; i++) System.out.print(grams2[i] + ";"); System.out.println();
        int count = 0;
        for (int i = 0; i < grams1.length; i++) {
            for (int j = 0; j < grams2.length; j++) {
                if (grams1[i].equals(grams2[j])) {
                    //System.out.println("Common gram: " + grams1[i]);
                    count++;
                    break;
                }
            }
        }
	//System.out.println("Common grams: " + count);
        //float sim = (float) 1 / (1+grams1.length +grams2.length - 2 * count);
        //float sim = 1 - 0.5 * ((float)(grams1.length-count)/grams1.length +
        //		       (float)(grams2.length-count )/grams2.length);
        float sim = (float) 2 * count / (grams1.length + grams2.length); // Dice-Coefficient
        return sim;
    }

    float computeDigramSimilarity(String str1, String str2) {
        return computeNGramSimilarity(str1, str2, 2);
    }

    float computeTrigramSimilarity(String str1, String str2) {
        return computeNGramSimilarity(str1, str2, 3);
    }

    float compute4GramSimilarity(String str1, String str2) {
        return computeNGramSimilarity(str1, str2, 4);
    }

    float compute5GramSimilarity(String str1, String str2) {
        return computeNGramSimilarity(str1, str2, 5);
    }

    String soundex(String str, boolean fullFlag) {
        // compute the soundex equivalent to str
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return "";
        }

        int iIn, iOut;
        char c, prevDig;
        String out = "";
        str = str.toLowerCase();
        if (fullFlag) {
            iIn = 0;
            iOut = 0;
            prevDig = '*';
        } else { // skip the first char
            iIn = 1;
            iOut = 1;
            out += str.charAt(0);
            prevDig = str.charAt(0);
        }
        while (iIn < str.length() && iOut < 4) {
            switch (str.charAt(iIn)) {
                case 'b':
                case 'p':
                case 'f':
                case 'v':
                    c = '1';
                    break;
                case 'c':
                case 's':
                case 'k':
                case 'g':
                case 'j':
                case 'q':
                case 'x':
                case 'z':
                    c = '2';
                    break;
                case 'd':
                case 't':
                    c = '3';
                    break;
                case 'l':
                    c = '4';
                    break;
                case 'm':
                case 'n':
                    c = '5';
                    break;
                case 'r':
                    c = '6';
                    break;
                default:
                    c = '*';
            }

            if (c != prevDig && c != '*') {
                out += c;
                prevDig = c;
                iOut++;
            }

            iIn++;
        }

        if (iOut < 4) {
            for (iIn = iOut; iIn < 4; iIn++) {
                out += '0';
            }
        }

        return out;
    }

    float computeSoundexSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return 0;
        }
        String se1 = soundex(str1, false);
        String se2 = soundex(str2, false);
        if (se1.equals(se2)) {
            return 1;
        }
        return 0;
    }

    int min(int a, int b) {
        return (a < b) ? a : b;
    }

    int max(int a, int b) {
        return (a < b) ? b : a;
    }

    int distance(Character a, Character b) {
        if (a == null || b == null) {
            return GAP;
        }
        if (!a.equals(b)) {
            return MISMATCH;
        }
        return MATCH;
    }

    int levenshteinDistance(String str1, String str2) {
        //Add a dummy character to the beginning of both strings
        str1 = " " + str1;
        str2 = " " + str2;
        int n = str1.length(), m = str2.length();
        int D[][] = new int[n][m];
        D[0][0] = 0;
        int i, j;
        for (i = 1; i < n; i++) {
            D[i][0] = D[i - 1][0] + distance(null, null);
        }
        for (j = 1; j < m; j++) {
            D[0][j] = D[0][j - 1] + distance(null, null);
        }
        for (i = 1; i < n; i++) {
            for (j = 1; j < m; j++) {
                int m1 = D[i - 1][j] + distance(new Character(str1.charAt(i)), null);
                int m2 = D[i - 1][j - 1] + distance(new Character(str1.charAt(i)), new Character(str2.charAt(j)));
                int m3 = D[i][j - 1] + distance(null, new Character(str2.charAt(j)));
                D[i][j] = min(min(m1, m2), m3);
                //System.out.print(D[i][j] + " ");
            }
            //System.out.println("");
        }
        return D[n - 1][m - 1];
    }

    float computeEditDistanceSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return 0;
        }
        int n = str1.length(), m = str2.length();
        return 1 - (float) levenshteinDistance(str1, str2) / max(n, m);
        //return (float) 1/ (1 + levenshteinDistance(str1, str2));
    }

    float computeSingleErrorSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return 0;
        }
        int levenDist = levenshteinDistance(str1, str2);
        if (levenDist <= 1) {
            return 1;
        }
        return 0;
    }


    /*
     public static void main(String args[])
     {
     StringSimilarity ssm = new StringSimilarity();
     System.out.println("Prefix      :" + ssm.computePrefixSimilarity(args[0], args[1]));
     System.out.println("Suffix      :" + ssm.computeSuffixSimilarity(args[0], args[1]));
     System.out.println("Char Freq   :" + ssm.computeCharFrequencySimilarity(args[0], args[1]));
     System.out.println("Digramm     :" + ssm.computeDigramSimilarity(args[0], args[1]));
     System.out.println("Trigramm    :" + ssm.computeTrigramSimilarity(args[0], args[1]));
     System.out.println("4-gramm     :" + ssm.compute4GramSimilarity(args[0], args[1]));
     System.out.println("5-gramm     :" + ssm.compute5GramSimilarity(args[0], args[1]));
     System.out.println("Soundex     :" + ssm.computeSoundexSimilarity(args[0], args[1]));
     System.out.println("EditDistance:" + ssm.computeEditDistanceSimilarity(args[0], args[1]));
     System.out.println("SingleError :" + ssm.computeSingleErrorSimilarity(args[0], args[1]));
     }
     */
}
