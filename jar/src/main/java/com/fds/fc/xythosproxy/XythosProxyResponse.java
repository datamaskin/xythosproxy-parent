/*
 * XythosProxyResponse.java     $Id: XythosProxyResponse.java,v 1.1 2004/05/25 14:02:19 todd Exp $
 *
 * Copyright 2000 First American Flood Data Service, All rights reserved.
 *
 */

package com.fds.fc.xythosproxy;

import java.io.Serializable;
import java.util.*;

/**
 * Class XythosProxyResponse - Class returned by all public and some private methods
 * of the FileUploadServlet.java
 *
 * status = UNKNOWN, unusable, property never set.
 * status = COMPLETE, method 
 * status = COMPLETE_EMPTY, method 
 * status = INVALID_REQUEST, methods getReason, getErrorField
 * status = VALID_REQUEST, methods 
 *
 * @author Todd Whitehead
 * @version $Id: XythosProxyResponse.java,v 1.1 2004/05/25 14:02:19 todd Exp $
 */

public class XythosProxyResponse implements Serializable
{
    public static final long serialVersionUID = 3L;
    
    // Creation
    public final static int UNKNOWN                   = 0;
    public final static int COMPLETE                   = 1;
    public final static int COMPLETE_EMPTY     = 2;
    public final static int INVALID_REQUEST     = 3;
    public final static int VALID_REQUEST          = 4;
    public final static int ERROR                            = 10;
    
    public final static int RESPONSE_MAX                     = 17;
    public final static int PRINCIPAL_ID_CREATED      = 0;
    public final static int PRINCIPAL_ID_UPDATE        = 1;
    public final static int TIMESTAMP_CREATED         = 2;
    public final static int PRINCIPAL_ID_OWNER         = 3;
    public final static int CONTENT_TYPE                     = 4;
    public final static int MIME_TYPE                             = 5;
    public final static int REVISION_ID                           = 6;
    public final static int VERSION_COMMENT             = 7;
    public final static int VERSION_CREATED               = 8;
    public final static int ENTRY_SIZE                            = 9;
    public final static int VERSION_DIGEST                   = 10;
    public final static int VERSION_FILENAME              = 11;
    public final static int VERSION_TEMP_FILENAME  = 12;
    public final static int FILE_NAME                               = 13;
    public final static int FILE_NAME_ID                         = 14;
    public final static int VERSION_LATEST                    = 15;
    public final static int CONTENT_LANGUAGE            = 16;
    

    //  Text 
    public static final String[] STATUS = {
        " UNKNOWN ", " COMPLETE ", " COMPLETE_EMPTY ", " INVALID_REQUEST ", " VALID_REQUEST ", " ERROR " };

    private int                  status;
    private String             text;
    private String[]          textList;
    private String             reason;
    private String             errorField;
    private ArrayList        searchResultsList;
    //private String             selectStatement;
    private ArrayList        checkEntryResponseList;

    /**
     * Constructor XythosProxyResponse - used primarily for complete_empty, invalid_request, canceled, duplicate, error
     *
     *
     *
     * @param status
     * @param reason
     * @param errorField
     */
    public XythosProxyResponse(int status, String reason, String errorField)
    {
        this.status         = status;
        this.reason        = reason;
        this.errorField   = errorField;
    }

    /**
     * Constructor XythosProxyResponse - used primarily for complete_empty, invalid_request, canceled, duplicate, error
     *
     *
     *
     * @param status
     */
    public XythosProxyResponse(int status)
    {
        this.status = status;
    }

    /**
     * Constructor XythosProxyResponse - used primarily for complete_empty, invalid_request, canceled, duplicate, error
     * @param status
     * @param text
     */
    public XythosProxyResponse(int status, String text)
    {
        this.status = status;
        this.text   = text;
    }

    /**
     * Constructor XythosProxyResponse - used primarily for... ???
     *
     *
     */
    public XythosProxyResponse()
    {
    }

    public XythosProxyResponse(int status, String[] textList) {
        this.status = status;
        this.textList = textList;
    }
    
    public boolean isValidRequest() {
        return ( status == XythosProxyResponse.VALID_REQUEST );
    }
    
    
    // get methods //////////////////////////////////////////

    /**
     * Method getStatus - returns the status, the type of order response
     *
     *
     * @return int
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Method getText - returns the text
     *
     *
     * @return String
     */
    public String getText()
    {
        return text;
    }
    
    /**
     * 
     * @return String[] textList of Xythos file upload items metadata
     */
    public String[] getTextList() {
        return textList;
    }

    /**
     * Method getReason - returns the reason for a NON-COMPLETE
     *
     *
     * @return String
     */
    public String getReason()
    {
        return reason;
    }

    /**
     * Method getErrorField - returns the field that caused an error when the
     * status is INVALID_REQUEST
     *
     *
     * @return String
     */
    public String getErrorField()
    {
        return errorField;
    }

    /**
     * Method getSearchResultsList - returns the searchResultsList, the type of order response
     *
     *
     * @return ArrayList
     */
    public ArrayList getSearchResultsList()
    {
        return searchResultsList;
    }

    /**
     * Method getXythosProxyResponseList - returns the XythosProxyResponseLIst, the type of order response
     *
     *
     * @return ArrayList
     */
    public ArrayList getXythosProxyResponseList()
    {
        return checkEntryResponseList;
    }

    // set methods /////////////////////////////////////////

    /**
     * Method setStatus
     *
     *
     * @param status
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * Method setText
     *
     *
     * @param text
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    /**
     * 
     * @param textList
     */
    public void setTextList(String[] textList)
    {
        this.textList = textList;
    }

    /**
     * Method setReason
     *
     *
     * @param reason
     */
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    /**
     * Method setErrorField
     *
     *
     * @param errorField
     */
    public void setErrorField(String errorField)
    {
        this.errorField = errorField;
    }

    /**
     * Method setSearchResultsList
     *
     *
     * @param searchResultsList
     */
    public void setSearchResultsList(ArrayList searchResultsList)
    {
        this.searchResultsList = searchResultsList;
    }

    /**
     * Method setXythosProxyResponseList
     *
     *
     *
     * @param XythosProxyResponseList
     */
    public void setXythosProxyResponseList(ArrayList checkEntryResponseList)
    {
        this.checkEntryResponseList = checkEntryResponseList;
    }

    /**
     * Method toString
     *
     * @return String
     */
    public String toString()
    {
        return ("XythosProxyResponse:-- " 
                + "    status = '" + status + "' " + XythosProxyResponse.STATUS[status]
                + "'   text = '" + text 
                + "'   reason = '" + reason
                + "'   errorField = '" + errorField 
                + "' --End.");
    }
    
    public int hashCode() {
        return status+text.hashCode()+textList.hashCode()+
                    reason.hashCode()+errorField.hashCode()+
                    searchResultsList.hashCode()+checkEntryResponseList.hashCode();
                                                                                                    
    }
}