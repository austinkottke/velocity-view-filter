/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.adk.java.viewfilter;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.adk.java.log.KLogger;

/******************************************************************************
 * Basic implementation of the char response wrapper, but
 * overwritten, so we can do custom parsing routines to the
 * content written to the filter.
 *
 * We do this so we can add in our own custom tags and tools, such
 * as locale utilities.
 *
 * We extend HttpServletResponse, as when using the normal HttpServletResponseWrapper
 * this was found to not work correctly when trying to add in utilities to modify
 * the content at the end. It did not work correctly with redirect urls to
 * https using redirects, so this was bypassed and we now implement the
 * whole HttpServletResponse interface. Whoo!
 * 
 * @author Austin Kottke
 ******************************************************************************/

public class CharResponseWrapper implements HttpServletResponse {
    
    private KLogger logger = KLogger.getLogger(this.toString());
    private CharArrayWriter output;
    private int errorCode;
    private HttpServletResponse r;

    /**
     *
     * @param response
     */
    public CharResponseWrapper(HttpServletResponse response){
        r = response;
        output = new CharArrayWriter();
    }

    /**
     *
     * @return
     */
    public String getContent(){
        return output.toString();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public PrintWriter getWriter() throws IOException {
        PrintWriter writer = new PrintWriter(output);
         return writer;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public ServletOutputStream getOutputStream() throws IOException {
            return r.getOutputStream();
    }


     /**
      *
      * @return
      */
     public String getCharacterEncoding() {
        return r.getCharacterEncoding();
    }

     /**
      *
      * @return
      */
     public String getContentType() {
        return r.getCharacterEncoding();
    }

     /**
      *
      * @param arg0
      */
     public void setCharacterEncoding(String arg0) {
      r.setCharacterEncoding(arg0);
    }

    /**
     *
     * @param arg0
     */
    public void setContentLength(int arg0) {
        r.setContentLength(arg0);
    }

    /**
     *
     * @param arg0
     */
    public void setContentType(String arg0) {
        r.setContentType(arg0);
    }

    /**
     *
     * @param arg0
     */
    public void setBufferSize(int arg0) {
       r.setBufferSize(arg0);
    }

    /**
     *
     * @return
     */
    public int getBufferSize() {
        return r.getBufferSize();
    }

    /**
     *
     * @throws IOException
     */
    public void flushBuffer() throws IOException {
        r.flushBuffer();
    }

    /**
     *
     */
    public void resetBuffer() {
      r.resetBuffer();
    }

    /**
     *
     * @return
     */
    public boolean isCommitted() {
       return r.isCommitted();
    }

    /**
     *
     */
    public void reset() {
        r.reset();
    }

    /**
     *
     * @param arg0
     */
    public void setLocale(Locale arg0) {
        r.setLocale(arg0);
    }

    /**
     *
     * @return
     */
    public Locale getLocale() {
       return r.getLocale();
    }

    /**
     *
     * @param cookie
     */
    public void addCookie(Cookie cookie) {
        r.addCookie(cookie);
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean containsHeader(String name) {
        return r.containsHeader(name);
    }

    /**
     *
     * @param url
     * @return
     */
    public String encodeURL(String url) {
         return r.encodeURL(url);
    }

    /**
     *
     * @param url
     * @return
     */
    public String encodeRedirectURL(String url) {
        return r.encodeRedirectURL(url);
    }

    /**
     *
     * @param url
     * @return
     */
    public String encodeUrl(String url) {
       return r.encodeUrl(url);
    }

    /**
     *
     * @param url
     * @return
     */
    public String encodeRedirectUrl(String url) {
       return r.encodeRedirectUrl(url);
    }

    /**
     *
     * @param sc
     * @param msg
     * @throws IOException
     */
    public void sendError(int sc, String msg) throws IOException {
        errorCode = sc;
        logger.error("Error code: " + sc );
        //r.sendError(sc,msg);

    }

    /**
     *
     * @return
     */
    public int getErrorCode(){
        logger.debug("returning error code. " + errorCode);
        return errorCode;
    }

    /**
     *
     * @param sc
     * @throws IOException
     */
    public void sendError(int sc) throws IOException {
         errorCode = sc;
        logger.error("Error code: " + sc );
       // r.sendError(sc);
    }

    /**
     *
     * @param location
     * @throws IOException
     */
    public void sendRedirect(String location) throws IOException {
        r.sendRedirect(location);
    }

    /**
     *
     * @param name
     * @param date
     */
    public void setDateHeader(String name, long date) {
      r.setDateHeader(name,date);
    }

    /**
     *
     * @param name
     * @param date
     */
    public void addDateHeader(String name, long date) {
        r.addDateHeader(name,date);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setHeader(String name, String value) {
        r.setHeader(name,value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
       r.addHeader(name,value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setIntHeader(String name, int value) {
        r.setIntHeader(name,value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addIntHeader(String name, int value) {
         r.addIntHeader(name,value);
    }

    /**
     *
     * @param sc
     */
    public void setStatus(int sc) {
        logger.debug("Status: " + sc) ;
        r.setStatus(sc);
    }

    /**
     *
     * @param sc
     * @param sm
     */
    public void setStatus(int sc, String sm) {
        logger.debug("Status: " + sc) ;
        r.setStatus(sc,sm);
    }



}