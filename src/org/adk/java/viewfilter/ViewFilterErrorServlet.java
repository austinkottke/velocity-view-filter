/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.adk.java.log.KLogger;

/**=====================================================================
 * Handles content that is requested at an html reference. Since we want all
 * content to not show what server technology we are using we use this smartly.
 *
 * If a user requests index.html, we then forward them to an index.jsp page,
 * but the user doesn't know this. He just sees index.html. This way he
 * can bookmark the page with a simple url and doesn't know the server technology
 * behind it.
 *
 * The sequence is as follows:
 *
 * 1. Request comes in for an html page. This servlet listens for html
 *    pages and receives the request.
 *
 * 2. If the resource cannot be found, then we change the extension of
 *    the html page, and attempt to forward to a jsp page matching the
 *    filename of the html.
 *
 * 3. The request, then gets forwarded, so the user sees an html page
 *    request, but we serve a jsp page that appears to be an html page.
 *
 * The nice thing about this, is that it works great with SSL, as opposed
 * to an include, and all of our security can be maintained.
 * 
 * @author Austin Kottke
 *=====================================================================*/
public class ViewFilterErrorServlet extends HttpServlet {

    private KLogger logger = KLogger.getLogger(this.toString());

    private boolean initialized = false;
    private String path;
    
    /**
     *
     */
    public void ViewFilterErrorServlet(){}

    /**
     *
     */
    @Override
    public void init(){
        path = this.getServletContext().getRealPath("");
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
          doPost(request, response);
    }

 /**=====================================================================
 *  If we find a path that generates a 404, particularly an html page,
  * then we forward to a jsp with that same name. We can then
  * hide the technology.
  
  * @param request
  * @param response
  * @throws ServletException
  * @throws IOException
  *=====================================================================*/

    @Override
    public void doPost (HttpServletRequest request,  HttpServletResponse response)
        throws ServletException, IOException {

            CharResponseWrapper responseWrapper = new CharResponseWrapper(response);

            if( ! initialized ) {
                init();
                initialized = true;
            }
            
            /**=====================================================================
             *  Attempt to find the requested resource.
             *=====================================================================*/
           try
           {
               logger.debug("Requested path: " + path + request.getServletPath());
               new FileReader( path + request.getServletPath() ).ready();
           }
           catch( FileNotFoundException e )
           {
             /**=====================================================================
             *  Get the file path and replace the extension with a jsp file.
             *=====================================================================*/
              String filePath = request.getServletPath();
              String newJspFilePath = "";
              
              String fileNameTemp[] = filePath.split("\\.");
              String fileNameWithoutExtension = fileNameTemp[0];
              newJspFilePath = fileNameWithoutExtension + ".jsp";

            /**=====================================================================
             * Forward to the new jsp.
             *=====================================================================*/
              logger.debug("Forwarding to: " + newJspFilePath);
              RequestDispatcher view = request.getRequestDispatcher(newJspFilePath);
              view.forward(request, response);
             
               
           }

           
    }



}
