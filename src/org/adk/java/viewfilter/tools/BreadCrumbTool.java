/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.adk.java.log.KLogger;
import org.adk.java.web.sitemap.SiteDirectory;
import org.adk.java.web.sitemap.SiteMapBuilder;
import org.adk.java.web.sitemap.SitePage;
import org.jdom.Attribute;

/***===============================================================================
 * The BreadCrumbTool generates a tree structure based on a site map xml file
 * located in the WEB-INF directory.
 * <p>
 * The way we do this, is through first using the {@link SiteMapBuilder} class which
 * generates a tree structure full of SiteDirectory and SitePage classes including
 * sub directories.
 * <p>
 * Then using the {@link SiteMapBuilder} class, we can retrieve the entire structure
 * in a TreeMap.
 * <p>
 * We then use velocity to create the breadcrumb as follows:<br>
 * =========================================================
 * <p>
 *   #set( $requestUri =  "$requestTool.getRequest().getRequestURI()" )
 *   #set ( $pageTitle = "")
 *   #set ( $dirs = $breadcrumbTool.getDirectory( $requestUri  ) )
 *
 *    <span  style="font-size: 10px; font-family: verdana">
 *    #foreach ($dir in $dirs )
 *       #set ( $isDisplayed = "$!dir.isDisplayed()")
 *
 *       <a href="$dir.getFinalTreeUrl()">$locale.get( "$dir.getId()" )</a>
 *    #end
 *
 *<p>
 * What this does:<br>
 * ===============
 *<p>
 * It retrieves the entire directory object and puts it in $dirs.
 *<p>
 * We then loop through each directory and generate a link and we pull the
 * title of the directory using the $locale tool.
 *<br>
 * As a note - this class needs major refactoring. I recommend another
 * version that handles dynamic directory creation.
 *
 * @see SiteMapBuilder
 * 
 * @version 0.1
 * @author Austin Kottke
 * ===============================================================================*/

public class BreadCrumbTool extends BaseTool {

    private KLogger logger = KLogger.getLogger(this.toString());
    private SiteMapBuilder primarySiteMap  = new SiteMapBuilder();
    private String siteMapXmlPath;
   
    /**
     *
     */
    public void BreadCrumbTool(){
    }

    /**
     *
     * @param params
     */
    public void configure(Map params)
    {
        logger.debug("configure() called.");
        siteMapXmlPath = params.get("siteMapXmlPath").toString() ;
       
    }

    /**
     *
     * @param requestUri
     */
    public void get( String requestUri ){
        if( ! primarySiteMap.isInitialized() ) {
            primarySiteMap.buildSiteMap( this.getServletFilePath() + siteMapXmlPath );
        }

        generateBreadcrumb(requestUri);
    }

    /**===============================================================================
     * This is the primary entry point to retrieve the breadcrumb directory.
     *
     * It returns an arraylist based on where the page request is coming
     * from.
     *
     * @param requestUri
     * @return
     *===============================================================================*/

    public ArrayList getDirectory( String requestUri ){
        logger.debug("getDirectory() called.");

        if( ! primarySiteMap.isInitialized() )
            primarySiteMap.buildSiteMap( this.getServletFilePath() + siteMapXmlPath );

        logger.debug("Servlet context name: " + getServletContext().getServletContextName() );

        String uriContextPath = this.getServletContextPath();
        String requestPath = requestUri.replace(uriContextPath, "");
        ArrayList b = generateBreadcrumb(requestPath);

        // Build urls...
        String url = "";

        for( int i=0; i<b.size(); i++ )
        {

            try
            {
                SiteDirectory dir = (SiteDirectory) b.get(i);
                if( dir.getDynamicPageId() != null) continue;


                url += dir.getUrl();
                SitePage p = null;

                if( dir.getPages().size() >= 1 )
                {
                    String pageKey = dir.getDefaultPage();
                    p = dir.getPages().get(pageKey);
                    url += "/"+p.getUrl();
                }

                dir.setFinalTreeUrl(url.replace(".jsp", ".html"));

                if( dir.getPages().size() >= 1) {

                    url = url.replace("/"+p.getUrl(), "");
                   // logger.debug("Modified url: " + url );
                }


            } catch (Exception e ) { }

        }

        List newbreadcrumb = (List) getRequest().getAttribute("additionalbreadcrumb");
        if( newbreadcrumb != null )
        {
            for( int j=0; j<newbreadcrumb.size(); j++)
                b.add( newbreadcrumb.get(j));
        }

        return b;
    }

    /**===============================================================================
     * Let us say that you are adding in dynamic directories and do not want to
     * use the site map, you can add urls at request time so that the bread crumb
     * shows up properly.
     *
     * This is recommended to use in case of very dynamic cases, but otherwise you
     * should configure the SiteMap.xml file.
     *
     * @param localeId The current page locale id to be displayed to the end user
     * @param urlPath the Path to the service or url being generated.
     *===============================================================================*/

    public void addDirectory(String localeId, String urlPath){
        logger.debug("addUrl() called.");
        
        List breadcrumb = getAdditionalBreadcrumb();
        
        SiteDirectory dir = new SiteDirectory();
        dir.setId(localeId);
        dir.setFinalTreeUrl( urlPath );
        dir.setDynamicPageId( "dynamic" );
        breadcrumb.add(dir);
        
        getRequest().setAttribute("additionalbreadcrumb", breadcrumb);
    }

    /***
     * Adds a page using the localeId as the name, this is primarily
     * used for static pages that do not change.
     * 
     * @param localeId
     */

    public void addPage(String localeId ){
        List breadcrumb = getAdditionalBreadcrumb();

        SitePage p = new SitePage();
        p.setId( localeId );

        breadcrumb.add(p);
    }

    /***
     * Used for dynamic pages that change the title based on a specific
     * attribute passed.
     * 
     * @param localeId
     * @param dynamicPageTitleAttribute
     */

     public void addPage(String localeId, String dynamicPageTitleAttribute ){
        List breadcrumb = getAdditionalBreadcrumb();

        SitePage p = new SitePage();
        p.setId( localeId );
        p.setDynamicPageTitle( dynamicPageTitleAttribute );

        breadcrumb.add(p);
    }
    /***
     * Stores a bread crumb in the request attributes. Used primarily for
     * creating directories that are marked as dynamic. We then can
     * add on additional directories and subdirectories at will. Particularly
     * useful for pages that have very dynamic page structures.
     * 
     * @return List A breadcrumb list that will be added on to the end of the breadcrumb
     */
    public List getAdditionalBreadcrumb(){
        List breadcrumb = null ;
        try {
            if( getRequest().getAttribute("additionalbreadcrumb") != null )
                breadcrumb = (List)getRequest().getAttribute("additionalbreadcrumb");
            else
                breadcrumb = new ArrayList();

        } catch(Exception e ){}
        return breadcrumb;
    }
    

    /**
     *
     * @return
     */
    public SitePage getPage(){
         
        return null;
    }

    /****************************************************************************
     * Creates the bread crumb.
     * 
     * @param requestUri
     * @return
     ****************************************************************************/
    public ArrayList generateBreadcrumb(String requestUri){

        logger.debug("generateBreadcrumb for: " + requestUri);
        TreeMap<String,SiteDirectory>  map = this.primarySiteMap.getSiteMap();
        Set directorySet = map.entrySet();
        String reqUri = requestUri;

        // Check if the action is in the request uri, which needs to be taken
        // out as this is not a top level directory
        if( requestUri.indexOf("action") >= 0 )
            reqUri = requestUri.split("/action")[1];
                    
        String [] directories = reqUri.split("/");

        SiteDirectory parentDir = null;
        ArrayList breadcrumb = new ArrayList();

        breadcrumb.add(map.get("dir_home"));

        logger.debug("Directory size: " + directorySet.size());

        for( int i=0; i<directorySet.size(); i++ )  {
            String key = directorySet.toArray()[i].toString().split("=")[0];
            SiteDirectory d = map.get(key);
            String topLeveldirectoryName = d.getUrl().replace("/", "");
            
            for( int k=0; k<directories.length; k++ )
            {
                String directoryToFind = directories[1].toString().trim();
                
                if( directoryToFind.length() > 1 ) {

                    if( topLeveldirectoryName.equals(directoryToFind))
                    {
                        logger.debug("Matching top level directory: " + topLeveldirectoryName );
                        getFinalDirectoryStructure(d, directories, breadcrumb, 0);
                        return breadcrumb;
                    }
                }
            }
            
        }

        
        
        return breadcrumb;
    }

    /***===============================================================================
     * Generates the final directory used for the breadcrumb, this can include
     * any number of directories and a single page to send the url to.
     * 
     * @param dir 
     * @param requestPath
     * @param intDirIndex
     * @param breadcrumb
     * ===============================================================================*/

    public void getFinalDirectoryStructure(SiteDirectory dir, String[] requestPath, ArrayList breadcrumb, int intDirIndex)
    {
        breadcrumb.add( dir );

        for( int k=0; k<requestPath.length; k++ )
        {
            String pathToFind = requestPath[k].toString().trim();

            int dirLength = dir.getSubcategories().size();
            int pageLength = dir.getPages().size();

            Set pages = dir.getPages().entrySet();
            Set directories = dir.getSubcategories().entrySet();
            
            //logger.debug("Current dir index: " + dir.getId() );
            // Check directories and then check pages.
            for( int i=0; i<dirLength; i++ )
            {
                String dirKey = directories.toArray()[i].toString().split("=")[0];
                SiteDirectory d = dir.getSubcategories().get(dirKey);

                if( pathToFind.equals( d.getUrl().replace("/", "") ) )  {
                    logger.debug("Directory: " + d.getDynamicPageId() );
                    getFinalDirectoryStructure(d, requestPath, breadcrumb, i);
                    return;
                }
                
            }

            // Now go through the last few pages...
            for( int m=0; m<pageLength; m++ )
            {
                String pageKey = pages.toArray()[m].toString().split("=")[0];
                SitePage p =  dir.getPages().get(pageKey);

                String url = p.getUrl();

                if( url.equals( requestPath[k]) || url.equals( requestPath[k].replace(".html", ".jsp") ) ){
                    logger.debug("Found a page match: ["+p.getUrl()+"] in directory: ["+dir.getId()+"]");
                    
                    breadcrumb.add(p);
                    return;
                }
            }
        }
        
    }

    /****************************************************************************
     * Builds a dynamic directory based on the requestUri. A store has
     * many categories and sub categories which are generated dynamically
     * and are not in the site map.
     * <p>
     * As a result of this, it is impossible if not tedious to create an entire
     * directory structure based on a possible directory structure that has
     * many infinite possibilities of shopping options.
     *<p>
     * So to generate the dynamic directory we simply build the structure based
     * on the uri and let the action handler correctly go to the correct directory
     * structure.
     *<p>
     * Example of a Directory:
     * ------------------------
     * <p>
     *
     * Home > Store > Books > Fiction > New and Used > Foundation Series
     * <p>
     * As you can see the directory structure is very long and the categories
     * are built within Books to Fiction to New and Used.
     *
     * @deprecated Not used, use the addDirectory and addPage methods as these
     *             are less messy.
     ****************************************************************************/

    public void addDynamicDirectoryToBreadCrumb( ArrayList breadcrumb, SiteDirectory d, Object[] requestDirectories)
    {
        logger.debug("addDynamicDirectoryToBreadCrumb() called: " + d );

        // ==============================================================================
        // We need to first find what the directory name is and in the current path array
        // contains the requestUri. We find the index into this path array so we can then
        // parse the remaining part of the array and build a dynamic directory with all
        // path info taken into account.
        // ==============================================================================
        String currentDirPathName = d.getUrl().split("/")[1];
        int currentDirectoryIndex = 0;
        for( int k=0; k<requestDirectories.length; k++ ) {
            if( requestDirectories[k].toString().equals( currentDirPathName ) ) currentDirectoryIndex=k;
        }
        // ==============================================================================
        // Now we have the current directory index, we now need to build the correct
        // dynamic directory.
        //
        // The remaining directory names correspond to localeIds and refer to
        // an action service.
        // ==============================================================================
        Attribute e = d.getAttributeById("dynamicUrl");
        String previousUrl = d.getAttributeById("previousUrl").getValue();

        SiteDirectory dynamicDir = new SiteDirectory();
        if( previousUrl != null )
        dynamicDir.setFinalTreeUrl( getServletContextPath() + previousUrl + requestDirectories[currentDirectoryIndex+1] );
        dynamicDir.setId( requestDirectories[currentDirectoryIndex+1].toString() );
        dynamicDir.setDynamicPageId("dynamic");
        breadcrumb.add( dynamicDir );

        for( int i=currentDirectoryIndex+2; i<requestDirectories.length; i++)
        {
            SiteDirectory newDynamicDir = new SiteDirectory();

            if( requestDirectories[i].toString().indexOf(".html") <= -1 )
            {
                newDynamicDir.setId( requestDirectories[i].toString() );
                newDynamicDir.setFinalTreeUrl( getServletContextPath() + e.getValue() + "/" + requestDirectories[i] );
                newDynamicDir.setDynamicPageId("dynamic");
                logger.debug("Directories: " +requestDirectories[i] );
                breadcrumb.add( newDynamicDir );
            }

            // This is a page, add a site page to the breadcrumb.
            if( requestDirectories[i].toString().indexOf(".html") >= 0 )
            {
                SitePage p = new SitePage();
                p.setId(requestDirectories[i].toString().split(".html")[0]);
                breadcrumb.add(p);
            }

        }

        logger.debug("Index: " + currentDirectoryIndex );
    }
}
