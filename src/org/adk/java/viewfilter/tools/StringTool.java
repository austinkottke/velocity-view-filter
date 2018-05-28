
package org.adk.java.viewfilter.tools;

import org.adk.java.log.KLogger;

/**
 * Utilities for string manipulation.
 * 
 * @author Austin Kottke
 */
public class StringTool extends BaseTool {

    private KLogger logger = KLogger.getLogger(this.toString());

    /***
     * Takes an input string and chops it based on a maximum set of characters.
     * It then returns this string either chopped or at its full size with
     * a string character at the end.
     * <P>
     * E.g: The Foundation Series
     *<P>
     * using this tool: $stringTool.formatLength("The Foundation Series", 5, "...")
     * <P>
     * would look like:
     *<P>
     * The Fo...
     *
     * @param inputString The string to modify
     * @param maxLength The maximum length that the string will be cut at
     * @param endsWith Strings to add at the end of the inputString
     * @return
     */
    public String formatLength( String inputString, int maxLength, String endsWith ){
        if( inputString == null ){
            return "";
        }
        logger.debug("formatLength " + inputString );
        
        String tempString = inputString;
        int len = maxLength;
        
        if( len >= inputString.length() ) {
         tempString = inputString;
         return tempString;
        } else {
         tempString = inputString.substring(0, maxLength);
        }
        
        return tempString + endsWith;
    }

    /***
     * Takes a string like joseph and returns Joseph, useful for initializing
     * first names and last names before the name goes into a database.
     *
     * This method runs through the entire string and breaks it between spaces,
     * and does every word in the inputstring separated by a space and returns
     * this string with every initial letter capitalized.
     *
     * @param inputString The string to modify
     * @return String with the initial cap words
     */
    public String initialCap( String inputString ){
       if( inputString.length() <= 1  ){
            return "";
        }
        logger.debug("initialCap " + inputString );
        String tempString = inputString;
        String newString = "";
        String[] strings = tempString.split(" ");

        for( int i=0; i<strings.length; i++ ){

            String t = strings[i];
            String t1 = strings[i].substring(0, 1);
            String t2 = strings[i].substring(1, strings[i].length());
            String s = t1.toUpperCase() + t2;
            newString += s;
            if( i != strings.length -1 )
                newString += " ";
        }

        return newString ;
    }
}
