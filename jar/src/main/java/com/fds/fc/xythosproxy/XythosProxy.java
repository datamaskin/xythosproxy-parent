package com.fds.fc.xythosproxy;

import java.io.File;
import java.io.ObjectInputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.apache.log4j.Logger;

/**
 * Class to act as proxy between the client and the XythosProxyServlet.
 * 7 overloaded methods to upload local disk files to the Xythos Virtual File System.
 */
public class XythosProxy {
    private static String proxyUrl                 = null;
    private static String virtualServer          = null;
    private static String user                        = null;
    private static String entry                       = null;
    private static String mimeType               = null;
    private static String description              = null;
    private static File file                               = null;
    private static boolean expect                   = false;
    private static XythosProxyResponse xpr  = null;
    
    static Logger log = Logger.getLogger(XythosProxy.class.getName());
    
    // Lists from mimetypes.org
    final static  String[] mimeTypes = {"application/SLA","application/STEP","application/STEP","application/acad","application/andrew-inset",
                "application/clariscad","application/drafting","application/dsptype","application/dxf","application/excel","application/i-deas",
                "application/java-archive","application/mac-binhex40","application/mac-compactpro","application/mspowerpoint",
                "application/mspowerpoint","application/mspowerpoint","application/mspowerpoint","application/msword","application/octet-stream",
                "application/octet-stream","application/octet-stream","application/octet-stream","application/octet-stream","application/octet-stream",
                "application/oda","application/ogg","application/ogg","application/pdf","application/pgp", "application/postscript",
                "application/postscript","application/postscript","application/pro_eng","application/rtf","application/set","application/smil",
                "application/smil","application/solids","application/vda","application/vnd.mif","application/vnd.ms-excel","application/vnd.ms-excel",
                "application/vnd.ms-excel","application/vnd.ms-excel","application/vnd.ms-excel","application/vnd.rim.cod","application/x-arj-compressed",
                "application/x-bcpio","application/x-cdlink","application/x-chess-pgn","application/x-cpio","application/x-csh",
                "application/x-debian-package","application/x-director","application/x-director","application/x-director","application/x-dvi",
                "application/x-freelance","application/x-futuresplash","application/x-gtar","application/x-gunzip","application/x-gzip",
                "application/x-hdf","application/x-ipix","application/x-ipscript","application/x-javascript","application/x-koan",
                "application/x-koan","application/x-koan","application/x-koan","application/x-latex","application/x-lisp","application/x-lotusscreencam",
                "application/x-mif","application/x-msdos-program","application/x-msdos-program","application/x-msdos-program",
                "application/x-netcdf","application/x-netcdf","application/x-perl","application/x-perl","application/x-rar-compressed",
                "application/x-sh","application/x-shar","application/x-shockwave-flash","application/x-stuffit","application/x-sv4cpio",
                "application/x-sv4crc","application/x-tar-gz","application/x-tar-gz","application/x-tar","application/x-tcl","application/x-tex",
                "application/x-texinfo","application/x-texinfo","application/x-troff-man","application/x-troff-me","application/x-troff-ms",
                "application/x-troff","application/x-troff","application/x-troff","application/x-ustar","application/x-wais-source","application/x-zip-compressed",
                "application/zip","audio/TSP-audio","audio/basic","audio/basic","audio/midi","audio/midi","audio/midi","audio/mpeg","audio/mpeg",
                "audio/mpeg","audio/ulaw","audio/x-aiff","audio/x-aiff","audio/x-aiff","audio/x-mpegurl","audio/x-ms-wax","audio/x-ms-wma","audio/x-pn-realaudio-plugin",
                "audio/x-pn-realaudio","audio/x-pn-realaudio","audio/x-realaudio","audio/x-wav","chemical/x-pdb","chemical/x-pdb","image/cmu-raster",
                "image/gif","image/ief","image/jpeg","image/jpeg","image/jpeg","image/png","image/tiff","image/tiff","image/tiff","image/x-cmu-raster",
                "image/x-portable-anymap","image/x-portable-bitmap","image/x-portable-graymap","image/x-portable-pixmap","image/x-rgb","image/x-xbitmap",
                "image/x-xpixmap","image/x-xwindowdump","model/iges","model/iges","model/mesh","model/mesh","model/mesh","model/vrml",
                "model/vrml","text/css","text/html","text/html","text/html","text/plain","text/plain","text/plain","text/plain","text/plain","text/plain","text/plain",
                "text/plain","text/plain","text/plain","text/richtext","text/rtf","text/sgml","text/sgml","text/tab-separated-values","text/vnd.sun.j2me.app-descriptor",
                "text/x-setext","text/xml","video/dl","video/fli","video/flv","video/gl","video/mpeg","video/mpeg","video/mpeg","video/mpeg","video/quicktime",
                "video/quicktime","video/vnd.vivo","video/vnd.vivo","video/x-fli","video/x-ms-asf","video/x-ms-asx","video/x-ms-wmv","video/x-ms-wmx","video/x-ms-wvx",
                "video/x-msvideo","video/x-sgi-movie","www/mime","x-conference/x-cooltalk","x-world/x-vrml","x-world/x-vrml"
            };
            
            final static String[] fileExt = {
                "stl","step","stp","dwg","ez","ccad","drw","tsp","dxf","xls","unv","jar","hqx","cpt","pot","pps","ppt","ppz","doc","bin","class","dms",
                "exe","lha","lzh","oda","ogg","ogm","pdf","pgp","ai","eps","ps","prt","rtf","set","smi","smil","sol","vda","mif","xlc","xll","xlm","xls","xlw",
                "cod","arj","bcpio","vcd","pgn","cpio","csh","deb","dcr","dir","dxr","dvi","pre","spl","gtar","gz","gz","hdf","ipx","ips","js","skd","skm",
                "skp","skt","latex","lsp","scm","mif","bat","com","exe","cdf","nc","pl","pm","rar","sh","shar","swf","sit","sv4cpio","sv4crc","tar.gz","tgz","tar",
                "tcl","tex","texi","texinfo","man","me","ms","roff","t","tr","ustar","src","zip","zip","tsi","au","snd","kar","mid","midi","mp2","mp3","mpga","au",
                "aif","aifc","aiff","m3u","wax","wma","rpm","ram","rm","ra","wav","pdb","xyz","ras","gif","ief","jpe","jpeg","jpg","png","tif","tif","tiff","ras","pnm",
                "pbm","pgm","ppm","rgb","xbm","xpm","xwd","iges","igs","mesh","msh","silo","vrml","wrl","css","htm","html","html","asc","asc","c","cc","f90",
                "f","h","hh","m","txt","rtx","rtf","sgm","sgml","tsv","jad","etx","xml","dl","fli","flv","gl","mp2","mpe","mpeg","mpg","mov","qt",
                "viv","vivo","fli","asf","asx","wmv","wmx","wvx","avi","movie","mime","ice","vrm","vrml"
            };

    private static String lookUpMimeType() throws IllegalArgumentException {
            String filenameExtension = XythosProxy.getFile().getName();
            if(filenameExtension == null || filenameExtension.length()==0) {
                if(log.isDebugEnabled())
                    log.error("lookUpMimeType.filenameExtension: null or empty");
                
                throw new IllegalArgumentException("Filename cannot be null");
            }

            String extension = null;
            int dotPos = filenameExtension.lastIndexOf(".");
            dotPos++; // bump past the dot
            boolean found = false;
            extension = filenameExtension.substring(dotPos);
            int pos = 0;

            for (int i = 0; i < fileExt.length && !found; i++) {
                if (extension.equalsIgnoreCase(fileExt[i])) {
                    found = true;
                    pos = i;
                    break;
                }
            }
            // If the user selects a file w/ an unknown extension...
            if (!found) {
                if(log.isDebugEnabled())
                    log.error("lookUpMimeType.fileExt error: file extension unknown.");
                
                throw new IllegalArgumentException("Wrong file extension for: " + filenameExtension);
            }
            return mimeTypes[pos];
        } // END LOOKUPMIMETYPE()
    /*
    public static void fileUpload(File file, String entry, String mimeType, String description) {
        if(file==null) {
            if(log.isDebugEnabled())
                log.error("fileUpload().file is null");
            
            throw new IllegalArgumentException("File object: file was null");
        }
        XythosProxy.setFile(file);
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.error("fileUpload().entry is null or empty");
            
            throw new IllegalArgumentException("File entry destination was null or empty");
        }
        if(mimeType==null || mimeType.length()==0) {
            if(log.isDebugEnabled())
                log.debug("fileUpload().mimeType is null or empty");
* @return 
           mimeType = XythosProxy.lookUpMimeType();
        }
        if(description==null || description.length()==0) {
            if(log.isDebugEnabled())
                log.error("fileUpload().description is null or empty");
            
            throw new IllegalArgumentException("Description was null or empty");
        }
        
        XythosProxy.setEntry(entry);
        XythosProxy.setMimeType(mimeType);
        XythosProxy.setDescription(description);
        
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        
         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            msgStr.append("Uploading " + XythosProxy.getFile().getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                msgStr.append("Upload complete, response=" + filePost.getResponseBodyAsString() + "\n");
            } else { // TODO: log.error()
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                log.error("fileUpload() exception: " + msgStr.toString());
            ex.printStackTrace();
        } finally {
            if(log.isDebugEnabled())
                log.debug("fileUpload().filePost connection released.");
            filePost.releaseConnection();
        }
    }
    */
    
    public  static void setFile(File file) {
        XythosProxy.file = file;
    }

    private static File getFile() {
        return XythosProxy.file;
    }

    public static void setProxyUrl(String proxyUrl) {
        XythosProxy.proxyUrl = proxyUrl;
    }

    private static String getProxyUrl() {
        return XythosProxy.proxyUrl;
    }
    
    public static void setUser(String user) {
        XythosProxy.user = user;
    }

    private static String getUser() {
        return XythosProxy.user;
    }

    public static void setVirtualServer(String virtualServer) {
        XythosProxy.virtualServer = virtualServer;
    }

    private static String getVirtualServer() {
        return XythosProxy.virtualServer;
    }
    
    public static void setEntry(String entry) {
        XythosProxy.entry = entry;
    }
    
    private static String getEntry() {
        return XythosProxy.entry;
    }
    
    public static void setDescription(String description) {
        XythosProxy.description = description;
    }
    
    private static String getDescription() {
        return XythosProxy.description;
    }

    public static void setUseExpectHeader(boolean expect) {
        XythosProxy.expect = expect;
    }
    
    private static boolean getUserExpectHeader() {
        return XythosProxy.expect;
    }
    
    public static void setMimeType(String mimeType) {
        XythosProxy.mimeType = mimeType;
    }
    
    private static String getMimeType() {
        return XythosProxy.mimeType;
    }
    
    public static void setXythosResponse(XythosProxyResponse xpr) {
        XythosProxy.xpr = xpr;
    }
    
    private static XythosProxyResponse getXythosResponse() {
        return XythosProxy.xpr;
    }
    
    /**
     * Overloaded method: uploads a file to the Xythos Virtual filesystem
     * @return result String of Xythos metadata file ID.
     * @throws java.lang.IllegalAccessException
     */
    public static String uploadFile() throws IllegalAccessException {
        String result = null; // the returned Xythos metadata file ID.
        // if the File object is not set by method then throw the Exception
        if(XythosProxy.getFile() == null) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Java File object: file was null");
            throw new IllegalArgumentException("File object was null.");
        }
       // if the Xythos file entry parameter is not set by method then throw the Exception
        if(XythosProxy.getEntry()==null || XythosProxy.getEntry().length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Xythos file entry param was null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
       // if the virutalServer parameter is not set by method then throw the Exception
        if(XythosProxy.getVirtualServer()==null || XythosProxy.getVirtualServer().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): Xythos virtualServe// the returned Xythos metadata file ID.r object name parameter was null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
       // if the user name is not set by method then thow the Exception
        if(XythosProxy.getUser()==null || XythosProxy.getUser().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): userName parameter was null or empty");
            throw new IllegalArgumentException("User name is null or empty");
        }
       // if the mimetype is not set by method then look-it-up
        if(XythosProxy.getMimeType()==null || XythosProxy.getMimeType().length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
       // if the description is not set by method the set to a blank string.
        if(XythosProxy.getDescription()==null || XythosProxy.getDescription().length()==0) {
           XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE()
    
    /**
     * Overloaded method: uploads a file to the Xythos Virtual filesystem
     * @param file: Java File object created from file to be uploaded
     * @return result String of Xythos metadata file ID.
     * @throws java.lang.IllegalAccessException
     */
    public static String uploadFile(File file) throws IllegalAccessException {
        
         String result = null; // the returned Xythos metadata file ID.
        // if the File object is null then throw the Exception
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Java File object: file was null");
            throw new IllegalArgumentException("File object was null.");
        }
       // if the Xythos file entry parameter is not set by method then throw the Exception
        if(XythosProxy.getEntry()==null || XythosProxy.getEntry().length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Xythos file entry param was null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
       // if the virutalServer parameter is not set by method then throw the Exception
        if(XythosProxy.getVirtualServer()==null || XythosProxy.getVirtualServer().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): Xythos virtualServe// the returned Xythos metadata file ID.r object name parameter was null or empty");
            throw new IllegalArgumentException("Virtual Server is null or eXythosProxy.setDescription(description)mpty");
        }
       // if the user name is not set by method then thow the Exception
        if(XythosProxy.getUser()==null || XythosProxy.getUser().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): userName parameter was null or empty");
            throw new IllegalArgumentException("User name is null or empty");
        }
       // if the mimetype is not set by method then look-it-up
        if(XythosProxy.getMimeType()==null || XythosProxy.getMimeType().length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
       // if the description is not set by method the set to a blank string.
        if(XythosProxy.getDescription()==null || XythosProxy.getDescription().length()==0) {
           XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());
        
        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE)
    
    /**
     * Overloaded method: uploads a file to the Xythos Virtual filesystem
     * @param file: Java File object created from file to be uploaded
     * @param entry: full Xythos TLD and sub-directory of file destination
     * @return result String of Xythos metadata file ID.
     * @throws java.lang.IllegalAccessException
     */
    public static String uploadFile(File file, String entry) throws IllegalAccessException {

        String result = null; // the returned Xythos metadata file ID.
       // if the File object is null then throw the Exception
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Java File object: file was null");
            throw new IllegalArgumentException("File object was null.");
        }
       // if the Xythos file entry parameter is null or empty throw the Exception
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Xythos file entry param was null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
       // if the virutalServer parameter is not set by method then throw the Exception
        if(XythosProxy.getVirtualServer()==null || XythosProxy.getVirtualServer().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): Xythos virtualServe// the returned Xythos metadata file ID.r object name parameter was null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
       // if the user name is not set by method then thow the Exception
        if(XythosProxy.getUser()==null || XythosProxy.getUser().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): userName parameter was null or empty");
            throw new IllegalArgumentException("User name is null or empty");
        }
       // if the mimetype is not set by method then look-it-up
        if(XythosProxy.getMimeType()==null || XythosProxy.getMimeType().length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
       // if the description is not set by method the set to a blank string.
        if(XythosProxy.getDescription()==null || XythosProxy.getDescription().length()==0) {
           XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        
         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE, entry)
    
    /**
     * Overloaded method: uploads a file to the Xythos Virtual filesystem
     * @param file: Java File object created from file to be uploaded
     * @param entry: full Xythos TLD and sub-directory of file destination
     * @param virtualServer: xythos virtual server object name
     * @return result String of Xythos metadata file ID.
     */
     public static String uploadFile(File file, String entry, String virtualServer) throws IllegalAccessException {

       String result = null; // the returned Xythos metadata file ID.
       // if the File object is null then throw the Exception
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Java File object: file was null");
            throw new IllegalArgumentException("File object was null.");
        }
       // if the Xythos file entry parameter is null or empty throw the Exception
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Xythos file entry param was null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
       // if the virutalServer parameter is null or empty throw the Exception
        if(virtualServer==null || virtualServer.length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): Xythos virtualServe// the returned Xythos metadata file ID.r object name parameter was null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
       // if the user name is not set by method then thow the Exception
        if(XythosProxy.getUser()==null || XythosProxy.getUser().length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): userName parameter was null or empty");
            throw new IllegalArgumentException("User name is null or empty");
        }
       // if the mimetype is not set by method then look-it-up
        if(XythosProxy.getMimeType()==null || XythosProxy.getMimeType().length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
       // if the description is not set by method the set to a blank string.
        if(XythosProxy.getDescription()==null || XythosProxy.getDescription().length()==0) {
           XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        XythosProxy.setVirtualServer(virtualServer);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        
         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE, entry, virtualserver )
    
    /**
     * Overloaded method: uploads a file to the Xythos Virtual filesystem
     * @param file: Java File object created from file to be uploaded
     * @param entry: full Xythos TLD and sub-directory of file destination
     * @param virtualServer: xythos virtual server object name
     * @param userName: xythos user
     * @return result String of Xythos metadata file ID.
     */
    public static String uploadFile(File file, String entry, String virtualServer, String userName) throws IllegalAccessException {

        String result = null; // the returned Xythos metadata file ID.
        // if the File object is null then throw the Exception
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Java File object: file was null");
            throw new IllegalArgumentException("File object was null.");
        }
        // if the Xythos file entry paramter is null or empty then throw the Exception
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Xythos file entry param was null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
        // if the Xythos virtualServer paramter is null or empty then throw the Exception
        if(virtualServer==null || virtualServer.length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): Xythos virtualServer object name parameter was null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
        // if the userName paramter is null or empty then throw the Exception
        if(userName==null || userName.length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): userName parameter was null or empty");
            throw new IllegalArgumentException("User name is null or empty");
        }
        // if the mimeType paramter is not set by method then look-it-up
        if(XythosProxy.getMimeType()==null || XythosProxy.getMimeType().length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
        // if the description parameter is null or empty then set the field to a blank string.
        if(XythosProxy.getDescription()==null || XythosProxy.getDescription().length()==0) {
           XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        XythosProxy.setVirtualServer(virtualServer);
        XythosProxy.setUser(userName);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        
         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE, entry, virtualserver, username )
 
    /**
     * Overloaded method: uploads a file to the Xythos Virtual filesystem
     * @param file: Java File object created from file to be uploaded
     * @param entry: full Xythos TLD and sub-directory of file destination
     * @param virtualServer: xythos virtual server object name
     * @param userName: xythos user
     * @param mimeType: file content type
     * @return result String of Xythos metadata file ID.
     */
     public static String uploadFile(File file, String entry, String virtualServer, String userName, String mimeType) throws IllegalAccessException {

        String result = null; // the returned Xythos metadata file ID.
        // if the File object is null then throw the Exception
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Java File object: file was null");
            throw new IllegalArgumentException("File object was null.");
        }
        // if the Xythos file entry paramter is null or em[ty then throw the Exception
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile(): Xythos file entry param was null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
        // if the Xythos virtualServer parmater is null or empty then throw the Exception
        if(virtualServer==null || virtualServer.length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): Xythos virtualServer object name parameter was null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
        // if the userName parameter is null or empty then throw the Exception
        if(userName==null || userName.length()==0) {
             if(log.isDebugEnabled())
                 log.debug("uploadFile(): userName parameter was null or empty");
            throw new IllegalArgumentException("User name is null or empty");
        }
        // if the mimeType paramter is null or empty then look-it-up
        if(mimeType==null || mimeType.length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType looked up as: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
        // if the description paramter is null or empty then set to a blank String.
        if(XythosProxy.getDescription()==null || XythosProxy.getDescription().length()==0) {
           if(log.isDebugEnabled())
               log.debug("uploadFile().description: set to a blank string");
           XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        XythosProxy.setVirtualServer(virtualServer);
        XythosProxy.setUser(userName);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        
         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE, entry, virtualserver, username, mimetype )
    
     /**
      * Overloaded method:  uploads a file to the Xythos Virtual filesystem
      * @param file: Java File object created from file to be uploaded
      * @param entry: full Xythos TLD and sub-directory of file destination
      * @param virtualServer: xythos virtual server object name
      * @param userName: xythos user
      * @param mimeType: file content type
      * @param description: misc. text description
      * @return result String of Xythos metadata file ID.
      * @throws java.lang.IllegalAccessException
      */
     public static String uploadFile(File file, String entry, String virtualServer, String userName, String mimeType, String description) throws IllegalAccessException {
        String result = null;
        // if File object is null then throw the Exception.
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadfile().file: is null.");
            throw new IllegalArgumentException("File object was null.");
        }
        // if entry is null or empty then throw the Exception.
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile().entry: is null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
        // if virtualServer is null or empty then throw the Exception.
        if(virtualServer==null || virtualServer.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile().virtualServer: is null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
        if(userName==null || userName.length()==0) {
            throw new IllegalArgumentException("User name is null or empty");
        }
        // if mimeType is null or empty then look-it-up
        if(mimeType==null || mimeType.length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType lookedup as: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
        // if the description parameter is null or empty then set it to a blank String.
        if(description==null || description.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile().description: set to a blank string.");
            XythosProxy.setDescription("");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        XythosProxy.setVirtualServer(virtualServer);
        XythosProxy.setUser(userName);
        XythosProxy.setDescription(description);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                 msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE, entry, virtualserver, username, mimetype, description )
     
     public static String uploadFile(File file, String entry, String virtualServer, String userName, String mimeType, String description, String proxyURL) throws IllegalAccessException {
        String result = null;
        // if File object is null then throw the Exception.
        if(file == null) {
            if(log.isDebugEnabled())
                log.debug("uploadfile().file: is null.");
            throw new IllegalArgumentException("File object was null.");
        }
        // if entry is null or empty then throw the Exception.
        if(entry==null || entry.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile().entry: is null or empty");
            throw new IllegalArgumentException("File destination entry was null or empty");
        }
        // if virtualServer is null or empty then throw the Exception.
        if(virtualServer==null || virtualServer.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile().virtualServer: is null or empty");
            throw new IllegalArgumentException("Virtual Server is null or empty");
        }
        if(userName==null || userName.length()==0) {
            throw new IllegalArgumentException("User name is null or empty");
        }
        // if mimeType is null or empty then look-it-up
        if(mimeType==null || mimeType.length()==0) {
           mimeType = lookUpMimeType();
           if(log.isDebugEnabled())
                log.debug("uploadFile().mimeType lookedup as: " + mimeType);
           XythosProxy.setMimeType(mimeType);
        }
        // if the description parameter is null or empty then set it to a blank String.
        if(description==null || description.length()==0) {
            if(log.isDebugEnabled())
                log.debug("uploadFile().description: set to a blank string.");
            XythosProxy.setDescription("");
        }
        // if the proxyURL parameter is null or empty then throw an Exception
        if(proxyURL==null || proxyURL.length()==0) {
            throw new IllegalArgumentException("Proxy URL is null or empty");
        }

        XythosProxy.setFile(file);
        XythosProxy.setEntry(entry);
        XythosProxy.setVirtualServer(virtualServer);
        XythosProxy.setUser(userName);
        XythosProxy.setDescription(description);
        XythosProxy.setProxyUrl(proxyURL);
        StringBuilder msgStr = new StringBuilder();

        PostMethod filePost = new PostMethod(XythosProxy.getProxyUrl());

        // Custom retry handler is necessary
        filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

         if(XythosProxy.getUserExpectHeader())
            filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, 
                                                                                               XythosProxy.getUserExpectHeader());

        try {
            if(log.isDebugEnabled())
                log.debug("uploadFile().getProxyUrl(): " + XythosProxy.getProxyUrl());
            msgStr.append("Uploading " + file.getName() + " to " + XythosProxy.getProxyUrl() + "\n");
            Part[] parts = {
                new StringPart("entryname",     XythosProxy.getEntry()),
                new StringPart("filename",        XythosProxy.getFile().getName()),
                new StringPart("vsname",          XythosProxy.getVirtualServer()),
                new StringPart("username",      XythosProxy.getUser()),
                new StringPart("mimetype",      XythosProxy.getMimeType()),
                new StringPart("description",   XythosProxy.getDescription()),
                new FilePart(XythosProxy.getFile().getName(), XythosProxy.getFile())
            };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                if(log.isDebugEnabled())
                    log.debug("uploadFile().status: " + status);
                result = filePost.getResponseBodyAsString();
                msgStr.append("Upload complete, response=" + result + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
               
            } else {
                msgStr.append("Upload failed, response=" + HttpStatus.getStatusText(status) + "\n");
                if(log.isDebugEnabled())
                    log.debug(msgStr);
            }
        } catch (Exception ex) {
            msgStr.append("ERROR: " + ex.getClass().getName() + " " + ex.getMessage() + "\n");
            if(log.isDebugEnabled())
                    log.debug(msgStr);
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
            if(log.isDebugEnabled())
                log.debug("uploadFile.filePost released and result returned: " + result);
            return result;
        }
    } // END UPLOADFILE(FILE, entry, virtualserver, username, mimetype, description, proxyURL )
}
