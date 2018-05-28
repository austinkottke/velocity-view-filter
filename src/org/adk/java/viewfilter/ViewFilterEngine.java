
package org.adk.java.viewfilter;

import org.adk.java.locale.LocaleTool;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.adk.java.store.StoreWebApplication;
import org.adk.java.store.WebSessionBean;
import org.adk.java.log.KLogger;
import org.adk.java.secure.SecureRedirectorActionBean;
import org.adk.java.stripes.ActionBeanUrlManager;
import org.adk.java.viewfilter.tools.BaseTool;
import org.adk.java.viewfilter.tools.BreadCrumbTool;
import org.adk.java.viewfilter.tools.PropertyTool;
import org.adk.java.viewfilter.tools.RequestTool;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.view.XMLToolboxManager;

/********************************************************************************************
 * <p>
 * Our basic filter, all it does is take our WebSessionBean which is a wrapper around
 * the current session and passes it in the request, so that all jsps and other objects
 * can access the data, such as the current users shopping cart, his user info, and
 * other pertinent details to the session for this user.
 * <p>
 * In addition to basic session addition, we also take the content and
 * add in the velocity template engine so the session can be accessed from
 * velocity, and all content, after stripes is done with it is modified
 * with velocity.
 * <p>
 * We perform the following steps:
 * -------------------------------
 * <ol>
 * <li>  Load in the velocity toolbox so these can be exposed and used throughout
 *    the web app.
 * 
 * <li> Add the session to the queue and attributes
 *
 * <li>  Perform the filter chain and take the modified content in the form
 *    of a PrintWriter
 *
 * <li>  Modify this content with the Velocity Engine.
 *
 * <li>  Using the localeTool, we can then render all translations loaded in
 *    the appropriate strings.xml file.
 *</ol>
 *
 * @author Austin Kottke
 ********************************************************************************************/
public class ViewFilterEngine implements Filter {

    private StoreWebApplication webApp = StoreWebApplication.getInstance();
    private KLogger logger = KLogger.getLogger(this.toString());
    private VelocityContext velocityContext = new VelocityContext();
    private static ViewFilterEngine instance ;
    private FilterConfig filterConfig;
    private ServletContext servletContext;

    // Security redirector
    private SecureRedirectorActionBean secureRedir = new SecureRedirectorActionBean();

    // The actual path of the primary servlet. From here we can load
    // in all other files based on this path.
    private String currentPath;
    private String toolboxPath;
    private XMLToolboxManager toolboxManager;
    private Map toolBox;

    // A reference to the locale tool.
    private LocaleTool localeTool;
    private RequestTool requestTool;
    private BreadCrumbTool breadCrumbTool;

    private boolean initialized = false;

    // Action url manager
    private ActionBeanUrlManager actionUrlManager ;
    
    private void ViewFilterEngine(){
        
    }

    /**
     *
     * @return
     */
    public static ViewFilterEngine getInstance(){
        if (instance == null ){
            return new ViewFilterEngine();
        }
        return instance;
    }

    

    /**
     *
     * @param aConfig
     * @throws ServletException
     */
    public void init(FilterConfig aConfig) throws ServletException {
        this.filterConfig=aConfig;
        this.currentPath = filterConfig.getServletContext().getRealPath("");
        this.toolboxPath = currentPath + filterConfig.getInitParameter("ToolBoxPath");
        this.servletContext = filterConfig.getServletContext() ;
        
        logger.initFilters( currentPath );
        instance = this;
        try {

            toolboxManager = new XMLToolboxManager();
            Velocity.init(currentPath + "/WEB-INF/classes/velocity.properties");
            logger.debug("Loading velocity toolbox: " );
            toolboxManager.load( toolboxPath );
           
            // Initialize the velocity tools and add them to the
            // velocity context so they can be used in the app
            initVelocityTools();
        
        } catch (Exception ex) {
            Logger.getLogger(ViewFilterEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Propery loads and initializes all of the velocity tools so they can be used
     * throughout the application.
     */
    public void initVelocityTools()
    {
         // Retrieve the toolbox
         toolBox = toolboxManager.getToolbox(null);
         Set keys = toolBox.keySet();
         if( keys.size() <= 0 ) return;

         for( Object key : keys )
         {
            String k  = key.toString();
            Object obj = toolBox.get(k);
            logger.debug("Tool object ["+k+"] = " + obj);

            velocityContext.put(k, obj);

            try
            {
                if( obj.getClass().getSuperclass().toString().indexOf("BaseTool") >= 0  )
                {
                    logger.debug("Setting tool properties: " + obj);
                    BaseTool t = (BaseTool ) obj;
                    logger.debug("Property set!");
                    t.setServletFilePath(currentPath);
                    t.setContext(velocityContext);
                    t.setServletContext(servletContext);
                }
            } catch(Exception e){}
        }
         try
         {
            
             if( toolBox.get("breadcrumbTool") != null )
             breadCrumbTool = (BreadCrumbTool) toolBox.get("breadcrumbTool");

             if( toolBox.get("locale") != null)
             localeTool = (LocaleTool) toolBox.get("locale");

             if( toolBox.get("requestTool") != null)
             requestTool = (RequestTool) toolBox.get("requestTool");
            
         } catch( Exception e )
         {
             logger.error("Error - missing locale tool and / or request tool in the toolbox.xml");
         }
    }

    /**========================================================================
     * Here we add the session to the webApp singleton and add the user to the
     * session queue if he is not already there. We also modify the content
     * with VTL.
     * 
     * @param request
     * @param response
     * @param chain
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     *========================================================================*/

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req =  (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        PropertyTool p  = (PropertyTool) toolBox.get("propertyTool");
        /*********************************************************************
         * Initalize the tools that need the current request object.
         *********************************************************************/

        doRequestInitialization(p,req,resp);
        p.initProperties(req.getContextPath(),  req.getRequestURI() );
        if( ! initialized )
        {
                webApp.doInitialization();
                actionUrlManager = ActionBeanUrlManager.getActionUrlManager();
                initialized = true;
        }
       
        /*********************************************************************
         * Take the current session, and if it is not already added to
         * to the current request, add this to the current session.
         *********************************************************************/
        HttpSession s = addSessionToQueue( req, resp);

        /*********************************************************************
         * Check if the page should be redirected to Https.
         *********************************************************************/
       if( doHttpsCheck(p, req,resp)  )
           return;
        
        /*********************************************************************
         * Return the html stream.
         *********************************************************************/
        CharArrayWriter caw = new CharArrayWriter();

        logger.debug("Retrieving html stream... ["+req.getRequestURI()+"]");
        
        try
        {

         PrintWriter out = getHtmlStream(chain, req, resp, caw );

        /*********************************************************************
         * render the html stream with velocity and persist the session
         * so velocity can access this.
         *********************************************************************/
         
        velocityContext.put( "velocitySession", s );
        
        out.write(  renderVelocity( caw.toString() ).toString() );
        } catch(Exception e )
        {
            logger.error("Error: " + e.toString() );
        }
        
    }

    /***
     * Initializes the tools and the property tool for requests that need the
     * information. It also initializes all of the tools that are instances
     * of base tools, including setting the current request and setting the
     * current locale for the localeTool.
     * 
     * @param p
     * @param request
     * @param response
     */
    public void doRequestInitialization(PropertyTool p, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            breadCrumbTool.setServletContextPath( request.getContextPath() );
            String curLocale = request.getParameter("locale");
            if( curLocale != null ) {
                Locale l = null;
                String lang = curLocale.split("_")[0];
                if( curLocale.indexOf("_") >= 0)
                {
                    String country = curLocale.split("_")[1];
                    l = new Locale(lang, country);
                }
                else
                {
                    if( lang.indexOf("en") >=0 )
                    l = new Locale(lang, "US");
                    else
                    l = new Locale(lang,lang);
                    
                }
                localeTool.setCurrentLocale( l );
            }
            
            
            
            Collection c = toolBox.values();
            Iterator i = c.iterator();
            while( i.hasNext() )
            {
                try
                {
                     Object o = i.next();

                     if( o.getClass().getSuperclass().toString().indexOf("BaseTool") >= 0  )
                     {
                         //logger.debug("Base tool! " + o);
                         BaseTool t = (BaseTool) o;
                         t.setRequest(request);
                     }
                 
                }catch(Exception e ){}
            }

            

        } catch(Exception e ){
            logger.error("Did you not load in the propertyTool ? - Check your velocity toolbox.xml file.");
            logger.error("Error: you might be missing a /global.properties in the site root! " + e.toString());
        }
    }
    /***
     * Modifys the request if this page should be secured or not. If it is
     * supposed to be secured, based on the global properties 'securePage' we
     * then forward to Https. If it is not supposed to be secured, then
     * we redirect to a regular url.
     *
     * @param p The property tool for lookup of the securePage property.
     * @param request The current request
     * @param response The response request.
     * @return
     */

    public boolean doHttpsCheck(PropertyTool p, HttpServletRequest request, HttpServletResponse response)
    {

        logger.debug("Secure page: " + p.get("securePage") );

        try
        {
            if( p.get("securePage").equals("1") && request.isSecure() == false)
            {
                logger.debug("This page should be secured...");
                String securedUrl = secureRedir.returnSecureUrl(request, request.getRequestURI() );
                logger.debug("Secure url: "  + securedUrl );
                try {
                    response.sendRedirect(securedUrl);
                    return true;
                } catch (Exception ex) {
                    logger.error("Error redirecting to url...");
                    return false;
                }
            }

            // Do check for if this should not be a secure page, but is in https mode.


            if( p.get("securePage").equals("0") && request.isSecure() == true)
            {
                logger.debug("This page should be NOT be secured!");
                 String regularUrl = secureRedir.returnRegularUrl(request, request.getRequestURI() );
                 try {
                    logger.debug("Redirecting to: " + regularUrl);
                    response.sendRedirect(regularUrl);
                    return true;
                } catch (Exception ex) {
                    logger.error("Error redirecting to url... " +ex.toString());
                    return false;
                }
            }
        }
        catch(Exception e ){
            return false;
        }
        return false;
    }

    /*
     *
     * Adds the session bean to our web app session queue, so this can be
     * referred to throughout the application
     *
     * @param request
     * @param response
     * @return Returns the HttpSession instance
     **/

    public HttpSession addSessionToQueue(HttpServletRequest request, HttpServletResponse response )
    {
        HttpSession currentSession = (HttpSession) request.getSession();
        WebSessionBean cartSession = null;

        logger.debug("Total sessions: " + webApp.getActiveSessions());
        
        boolean isSessioninQueue = webApp.isSessionInQueue(currentSession.getId() );

        if( ! isSessioninQueue ) {
            cartSession = new WebSessionBean ( currentSession );
            webApp.addSession( cartSession );
        }

        if( isSessioninQueue ) cartSession = (WebSessionBean) webApp.getSession(currentSession.getId());

        // on the request for this particular user, make sure the session is included
        // with the shopping info, etc.
        request.setAttribute( "sessionBean", cartSession );

        return cartSession;

    }

    

    /**
     * Returns a PrintWriter with the current Html stream, after it has gone
     * through all redirects and filters.
     *
     * We do this by making a wrapper around the current html response and then
     * writing the current content after the  filters have been performed.
     * Once we have a writer we can then modify the response with whatever is
     * needed. Currently we modify the current response with the
     * velocity template engine, so we can also use VTL in a jsp or html.
     *
     * @param chain
     * @param request
     * @param response
     * @param writer
     * @return A PrintWriter with the modified html content.
     **/

    public PrintWriter getHtmlStream(FilterChain chain, HttpServletRequest request, HttpServletResponse response, CharArrayWriter writer ){
        /****************************************************************
         * Add in our custom response wrapper, so all filters and content
         * are written to, then we can add in our own content once we
         * have the outputted content in string form.
         ****************************************************************/
        PrintWriter out = null;

        try
        {
            out = response.getWriter();
            CharRequestWrapper reqWrapper = new CharRequestWrapper( request, request.getRequestURI(), request.getRequestURL() );
            CharResponseWrapper respWrapper = new CharResponseWrapper( response );

            /****************************************************************
             * If what was requested was an html page, then we need to
             * still retrieve the content based on the request, since we use
             * jsp as our presentation technology we still want to serve
             * up the content based on an html string.
             ****************************************************************/
             
            chain.doFilter(reqWrapper, respWrapper);
            
            /****************************************************************
             * If we have a 404, then we forward to the error pages...
             ****************************************************************/
            boolean sendErrorPage = false;
            if( respWrapper.getErrorCode() == HttpServletResponse.SC_NOT_FOUND ) sendErrorPage = true;
            if( respWrapper.getErrorCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) sendErrorPage = true;

            if( sendErrorPage ) respWrapper.sendRedirect(reqWrapper.getContextPath() + "/engine/error/error.jsp");
            
            
            writer.write( respWrapper.getContent());
            

        } catch (Throwable ex) {
            Logger.getLogger(ViewFilterEngine.class.getName()).log(Level.SEVERE, null, ex);
        } 

        return out;
    }

    /**
     *
     * @param htmlContent
     * @return
     */
    public CharArrayWriter renderVelocity(String htmlContent)
    {
       
        CharArrayWriter outputVelocity = new CharArrayWriter();
        try {

           
            Velocity.evaluate(velocityContext, outputVelocity, "log", htmlContent);
        } catch (Throwable ex) {
            Logger.getLogger(ViewFilterEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return outputVelocity;
    }

    /**
     *
     * @return
     */
    public XMLToolboxManager getToolboxManager() {
        return toolboxManager;
    }

    /**
     *
     * @param t
     */
    public void setToolboxManager(XMLToolboxManager t) {
         toolboxManager = t;
    }

    /**
     *
     */
    public void destroy() {
        webApp.clearSessions();
    }

    /**
     *
     * @return
     */
    public Map getToolBox() {
        return toolBox;
    }

    /**
     *
     * @param toolBox
     */
    public void setToolBox(Map toolBox) {
        this.toolBox = toolBox;
    }

    
}
