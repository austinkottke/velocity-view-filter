/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter.tools;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.VelocityContext;

/**
 *
 * @author Austin Kottke
 */
public class BaseTool {

    // Represents the actual path to the servlet on the  file system.
    private String servletFileSystemPath;
    private VelocityContext context;

    // Represents the initial directory that the servlet has, such
    // as /core/ or something that the initial dir has.
    private ServletContext servletContext;

    private String servletContextPath;
    // The current request
    private HttpServletRequest request;



    /**
     *
     * @return
     */
    public VelocityContext getContext() {
        return context;
    }

    /**
     *
     * @param context
     */
    public void setContext(VelocityContext context) {
        this.context = context;
    }

    /**
     *
     * @return
     */
    public String getServletFilePath() {
        return servletFileSystemPath;
    }

    /**
     *
     * @param servletFileSystemPath
     */
    public void setServletFilePath(String servletContextPath) {
        this.servletFileSystemPath = servletContextPath;
    }


    /**
     *
     * @return
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     *
     * @param servletContext
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    public String getServletContextPath() {
        return servletContextPath;
    }

    public void setServletContextPath(String servletContextPath) {
        this.servletContextPath = servletContextPath;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
