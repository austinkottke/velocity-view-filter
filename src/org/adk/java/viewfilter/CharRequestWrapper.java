/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author Austin Kottke
 */
public class CharRequestWrapper extends HttpServletRequestWrapper {

    private String requestUri;
    private StringBuffer requestURL;

    /**
     *
     * @param req
     * @param requestUri
     * @param requestUrl
     */
    public CharRequestWrapper( HttpServletRequest req, String requestUri, StringBuffer requestUrl ){
        super(req);
        setRequestUri( requestUri );
        setRequestURL( requestUrl );
    }

    /**
     *
     * @param uri
     */
    public void setRequestUri( String uri ){
       requestUri = uri;
    }

    /**
     *
     * @return
     */
    @Override
    public String getRequestURI(){
        return requestUri;
    }

    /**
     *
     * @return
     */
    @Override
    public StringBuffer getRequestURL() {
        return requestURL;
    }

    /**
     *
     * @param requestURL
     */
    public void setRequestURL(StringBuffer requestURL) {
        this.requestURL = requestURL;
    }
}
