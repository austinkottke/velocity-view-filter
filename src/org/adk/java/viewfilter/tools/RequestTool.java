/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter.tools;

import javax.servlet.http.HttpServletRequest;
import org.adk.java.log.KLogger;
import org.apache.velocity.app.Velocity;

/**
 *
 * @author Austin Kottke
 */
public class RequestTool extends BaseTool {

    private HttpServletRequest currentRequest;
    private KLogger logger = KLogger.getLogger(this.toString());
    
    /**
     *
     */
    public void RequestTool(){
    }

    /**
     *
     * @param r
     */
    public void setRequest( HttpServletRequest r){
        currentRequest = r;
    }

    /**
     *
     * @return
     */
    public String getContextPath(){
        return (String) Velocity.getProperty("core.context.path");
    }
    
    /**
     *
     * @return
     */
    public HttpServletRequest getRequest(){
        logger.debug("getRequest() called.");
        return currentRequest;
    }

    public String getParameter(String parameterId ){
       return currentRequest.getParameter(parameterId);
    }

    public Object getRequestAttribute(String attribute){
        try
        {
            Object p = currentRequest.getAttribute(attribute);
            return p;
        }catch(Exception e ){

        }
        return "property.not.found";
    }
     /**
     * Return session attributes
     * @return
     */
    public Object getSessionAttribute(String sessionAttribute){
        try
        {
            Object p = currentRequest.getSession().getAttribute(sessionAttribute);
            return p;
        }catch(Exception e ){
            
        }
        return "property.not.found";
    }
}
