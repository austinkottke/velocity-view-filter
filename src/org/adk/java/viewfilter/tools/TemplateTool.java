/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter.tools;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileReader;
import java.util.Map;
import org.adk.java.log.KLogger;
import org.apache.velocity.app.Velocity;

/**
 *
 * @author Austin Kottke
 */
public class TemplateTool extends BaseTool {

    private KLogger logger = KLogger.getLogger(this.toString());
    
    private String templateDir;
    
    /*===========================================================================
     * Initializes the locale tool by passing in parameters from the
     * toolbox.xml file.
     *
     * Currently we only need the string property path in order to load
     * the locale properties into our localizedMap so these can be
     * available to the site.
     *
     * @param params
     *===========================================================================*/
    /**
     *
     * @param params
     */
    public void configure(Map params)
    {
        logger.debug("configure() called.");
        templateDir = params.get("templateDir").toString() ; 
    }
    
    /**
     *
     * @param templateName
     * @return
     */
    public String getTemplate( String templateName ){

        logger.debug("getTemplate() called. ");
        return getResourceStream(templateName).toString();
        
    }

    /**
     *
     * @param fileName
     * @return
     */
    public String getResourceStream(String fileName)
    {
        String htmlContent = "$template.noTemplateFound";
        
        try
        {
            String baseServletPath = getServletFilePath() + templateDir;
            logger.debug("Looking for: " + baseServletPath + fileName );

            BufferedReader inputStream = new BufferedReader(new FileReader(baseServletPath + fileName));
            
            String l;
            String out = "";
            while ((l = inputStream.readLine()) != null) 
                out += l;
            
            htmlContent=out;

            CharArrayWriter outputVelocity = new CharArrayWriter();
            Velocity.evaluate(getContext(), outputVelocity, "out", htmlContent);
            htmlContent = outputVelocity.toString();
            
        } catch (Exception e ){
            logger.error("Error loading template: ." + e.toString());
        }

        return htmlContent;
    }
    
}
