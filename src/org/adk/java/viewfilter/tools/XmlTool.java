/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adk.java.log.KLogger;

import org.dom4j.Node;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**----------------------------------------------------------------------------
 * The xml tool is a velocity tool that allows traversing of xml files
 * that are located in a subfolder, so that these can be used to generate
 * menus, navigation elements or any other type of content that is used
 * in an xml file.
 * 
 * @author Austin Kottke
 *----------------------------------------------------------------------------*/
public class XmlTool extends BaseTool {

    private KLogger logger = KLogger.getLogger(this.toString());

    // The map where all the xml files are stored.
    private HashMap xmlConfigMap = new HashMap();

    // Input sources for xml
    private HashMap<String,Document> xmlDocs = new HashMap<String,Document>();
    private HashMap<String,File> fileList = new HashMap<String,File>();
    private HashMap<String,Long> fileModifiedList = new HashMap<String,Long>();
    
    // Input sources for xml
    private boolean isCached = true;

    Node n;

    Element t;

   
    

    /**
     *
     */
    public XmlTool(){ }

    /*----------------------------------------------------------------------------
     * Configure method takes all parameters and expects each parameter
     * points to the xml file that we want to do a lookup with in the
     * xml tool.
     *
     * E.g. $xmlTool.appConfig.MainNavigation.Content
     * 
     * @param params
     *----------------------------------------------------------------------------*/
    /**
     *
     * @param params
     */
    public void configure(Map params)
    {
        logger.debug("configure() called.");
        
        for( int i=0; i<params.size(); i++  )
        {
          String k =  params.entrySet().toArray()[i].toString().split("=")[0];
          String v =  params.entrySet().toArray()[i].toString().split("=")[1];
          logger.debug("Add to xml map: " + k + " == " + v );
          xmlConfigMap.put( k, v);
        }
    }
    
    /***----------------------------------------------------------------------------
     * The primary get tool is used to splice the key value pairs and
     * retrieve content from the xml file. This method sets off
     * everything else.
     *
     *----------------------------------------------------------------------------
     * @param aString
     * @return
     */
    public Object get( String aString ){
        return doXpathLookup( aString );
    }

    /***----------------------------------------------------------------------------
     * Do xpath lookup does quite a bit, one of the major things it does
     * is create an expression about the string requested.
     *
     * E.g
     *
     * $xmlTool.appConfig_MainNavigation_Category
     *
     * This ends up being the xpath expression //MainNavigation/Category
     *
     * and then queried. All nodes from the xml file are then returned
     * using JDOM.
     *
     * Additionally, in the xml tool we also have caching, this way, the
     * xml file is loaded and stored for later use. If the file gets
     * modified since it was loaded, it gets reloaded and then put
     * into the same key so it can be re-used with the same data.
     *
     *
     * @param path
     * @return
     *----------------------------------------------------------------------------*/

    public Object doXpathLookup( String path )
    {

        String appKey = path.split("_")[0];
        String directory[] = path.split("_") ;
        String xpathexpression = "//";
       
        for( int i=0; i<directory.length; i++)
        {
            if( i != 0 ) xpathexpression += directory[i] ;

            if( i != 0 && i < directory.length-1) xpathexpression += "/";
        }

        logger.debug("Xpath expression: " + xpathexpression );
        
        try
        {
           /***********************************************************************
             * Load in the xml file if it has not been loaded already.
             ***********************************************************************/
            String xmlPath = getServletFilePath() + (String) xmlConfigMap.get(appKey);
            SAXBuilder builder = new SAXBuilder();
            Document doc = xmlDocs.get(appKey);
            File aFile = fileList.get( appKey );
            Long lastModified = fileModifiedList.get( appKey );
            boolean forceReload = false;

            /***********************************************************************
             * Check if the file that is requested is newer than the file we have
             * cached away. If so, then re-load the xml content.
             ***********************************************************************/
             if( aFile != null ) {
                if( lastModified  < aFile.lastModified() )
                {
                    forceReload = true;
                    logger.debug("File has been modified since, forcing reload.");
                }
             }
            
            /***********************************************************************
             * Check if the xml file has been loaded already, if it has then we dont
             * need to do anything.
             ***********************************************************************/
             if( doc == null || forceReload == true)
             {
                 File f = new File(xmlPath);
                 
                 doc = builder.build(f);
                 xmlDocs.put( appKey, doc);
                 fileList.put( appKey, f);
                 fileModifiedList.put( appKey, f.lastModified() );
             }

             
            
            /***********************************************************************
             * Splice the content after the key to lookup the xml path requested.
             * E.g.:
             *
             * appConfig-MainNavigation
             *
             * would execute a certain xpath query /ApplicationConfig/MainNavigation
             *
             ***********************************************************************/

            XPath contentPath = XPath.newInstance( xpathexpression );
            List nodes = contentPath.selectNodes(doc);
            
            return nodes;

        } catch ( Exception e ){
            logger.error("Error retrieving key or xml path failed: " + e.toString() );
        }

        return "Expression failed.";
    }

    /**
     *
     * @return
     */
    public boolean isCached() {
        return isCached;
    }

    /**
     *
     * @param isCached
     */
    public void setCaching(boolean isCached) {
        this.isCached = isCached;
    }

}
