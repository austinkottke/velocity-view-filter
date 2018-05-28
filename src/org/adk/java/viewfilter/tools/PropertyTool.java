
package org.adk.java.viewfilter.tools;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.adk.java.log.KLogger;

/**============================================================================
 * This is a tool that loads in a property file on a site wide basis
 * so that properties can be set and global properties can be set that
 * can be accessed in velocity and stripes/jsp.
 *
 * The sequence of properties is as follows:
 * 
 * 1. Load in /global.properties.
 * 
 * 2. Check if there is a page.properties in the directory, if so then
 *    we load this file.
 * 
 * 3. If there is a requestFileName.jsp.properties then we load in this
 *    file as well. This also applies to html, any other type of display
 *    page.
 *
 * We can then using this system enable on a sitewide basis, properties
 * that can be used to modify templates, change view specific information
 * on a per request basis.
 *
 * @example
 * 
 * Velocity syntax:           $propertyTool.propertyName
 *
 * Stripes/Velocity syntax:   <webtools:getProperty var="myvar" propertyName="template" />
 *
 * @author Austin Kottke
 *============================================================================*/
public class PropertyTool extends BaseTool {

    private KLogger logger = KLogger.getLogger(this.toString());
    private String propertyDir = "";
    private String defaultProperties = "page.properties";
    private boolean initialized = false;



    /**
     *
     */
    public PropertyTool(){
        logger.debug("created.");
    }


    /*===========================================================================
     * Initializes the property tool, specifying where the base path is to
     * load in property files. Specifying the root path will make it so you
     * can load in any property file starting from the web folder, using
     * the current folder request.
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
        try{
            propertyDir = params.get("propertyDir").toString() ;
            defaultProperties = params.get("defaultProperties").toString() ;
        }catch( Exception e ){
            
        }

    }

    /*===========================================================================
     * Attempt to initialize the properties based on the current request.
     *
     * 1. Load global properties.
     * 2. Load directory properties.
     * 3. Load page specific properties.
     *
     * @param requestPath The path to the current directory.
     *===========================================================================*/
    /**
     *
     * @param contextPath
     * @param requestPath
     */
    public void initProperties(String contextPath, String requestPath)
    {
        if( requestPath.indexOf("css") >= 0 ) return;

        logger.debug("initProperties() called for: " + getServletFilePath() + requestPath);
        try {

            // Remove the context path from the request and create a path
            // just based on where the current directory is.

            String currentDir = requestPath.replace(contextPath, "");
            String dir[] = currentDir.split("/");
            String path ="";
            
            for( int i=0; i<dir.length; i++ ){
                
                if( dir[i].indexOf(".") == -1 )
                        path += dir[i] + "/";
             }

            if( path.equals("") ){
                path = "/";
            }
            
            // Attempt to load in a property file at the current path,
            // defaulting to the file name properties.xml

            // Attempt to load global.properties
            String servletPath = getServletFilePath() + "/";
            loadProperties( servletPath + "/WEB-INF/global.properties", true);
            logger.debug("global properties loaded.");

            loadDynamicDirectoryProperties(path);

            String fullFilePath = servletPath + path + defaultProperties;

            logger.debug("attempting to load:  " + fullFilePath );
            if( new File( fullFilePath ).canRead() ){
                loadProperties(fullFilePath, false);
            }

            fullFilePath = servletPath + currentDir + ".properties";
            logger.debug("attempting to load:  " + fullFilePath );
            
            // The concept here is if we have a page that exactly matches the
            // properties file then we load this as well.
            if( new File( fullFilePath ).canRead() ){
                loadPageSpecificProperties(fullFilePath);
            }

            if( currentDir.indexOf(".html") >= 0 )
            {
                fullFilePath = servletPath + currentDir.replace(".html", ".jsp") + ".properties";
                logger.debug("attempting to load:  " + fullFilePath );
               
                // Load in the jsp version, if this is an html page.
                if( new File( fullFilePath ).canRead() ){
                    loadPageSpecificProperties(fullFilePath);
                }
            }

            
            setInitialized(true);

        } catch( Exception e ){
            logger.error("There was an error loading properties: " + e.toString());
        }
    }

    private void loadDynamicDirectoryProperties(String currentPath){
        String [] dynamicDirs = this.getProperties().get("dynamicDirs").split(",");
        logger.debug("Dynamic directories: " + dynamicDirs);
        for( int i=0; i<dynamicDirs.length; i++ ){

            if( currentPath.indexOf( dynamicDirs[i].toString() ) >= 0 )
            {
                String url = this.getServletFilePath() + dynamicDirs[i] + "/" + defaultProperties;
                loadPageSpecificProperties( url );
            }
        }
    }

    /*===========================================================================
     * Attempts to load in properties for a specific page, e.g. if you have a
     * file in the tree named login.jsp.properties
     *
     * Then this would load in the page properties specifically for that
     * login page and make the properties available like
     * $propertyTool.myPropertyName 
     *
     * @param path The path including the context with an exact file name.
     * 
     *===========================================================================*/

     private void loadPageSpecificProperties(String path ){
         loadProperties( path, false );
     }

    /*===========================================================================
     * Loads a set of properties with the specified request.
     *
     * @param path The path including the context with an exact file name.
     *===========================================================================*/
    

    private void loadProperties(String path, boolean resetProperties){
         
            logger.debug("loadProperties: " + path );
            File propertyFile = new File( path );

            if( resetProperties ) {
                logger.debug("Resetting properties.");
                HashMap<String,String> p = new HashMap<String,String>();
                logger.debug("Request: " + this.getRequest() );
                getRequest().setAttribute("requestProperties", p);
                logger.debug("Setting attributes...");
            }

            Properties prop = new Properties();

            try
            {
                prop.load(new FileReader(propertyFile));

                Set p = prop.entrySet();
                for( int i=0; i<p.size(); i++ ){
                    String propertyName = p.toArray()[i].toString().split("=")[0];
                    String propertyValue = p.toArray()[i].toString().split("=")[1];
                    getProperties().put(propertyName, propertyValue);
                    logger.debug("Adding property: " + propertyName + " = " + propertyValue);
                }
            } catch (Exception e )
            {
                
            }
    }

    /**
     *
     * @param propertyKey
     * @return
     */
    public Object get(String propertyKey){
        String key = propertyKey;

        try
        {
            return this.getProperties().get(key);
        } catch( Exception e ){
            
        }
        return "$propertyTool." + propertyKey;
    }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     *
     * @param initialized
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public HashMap<String,String> getProperties(){
        
        Object o = this.getRequest().getAttribute("requestProperties");
        if( o == null ){
            logger.debug("Creating new properties...");
            HashMap<String,String> p = new HashMap<String,String>();
            this.getRequest().setAttribute("requestProperties", p);
        }

        return (HashMap<String, String>) this.getRequest().getAttribute("requestProperties");
    }

     

}
