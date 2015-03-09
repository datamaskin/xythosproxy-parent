 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fds.fc.xythosproxy;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;

import javax.servlet.*;
import javax.servlet.http.*;

// all the Xythos stuff here
import com.xythos.common.api.CustomLog;
import com.xythos.common.api.MessageBundle;
import com.xythos.common.api.MessageBundleFactory;
import com.xythos.common.api.VirtualServer;
import com.xythos.common.api.XythosException;
import com.xythos.storageServer.admin.api.AdminUtil;
import com.xythos.security.api.Context;
import com.xythos.security.api.ContextFactory;
import com.xythos.security.api.PrincipalManager;
import com.xythos.security.api.UserBase;
import com.xythos.security.api.WFSSecurityException;
import com.xythos.storageServer.api.CreateTopLevelDirectoryData;
import com.xythos.storageServer.api.CreateDirectoryData;
import com.xythos.storageServer.api.CreateFileData;
import com.xythos.storageServer.api.FileSystemEntryACLChangedEvent;
import com.xythos.storageServer.api.FileSystemEntryACLChangedListener;
import com.xythos.storageServer.api.FileSystem;
import com.xythos.storageServer.api.FileSystemEntry;
import com.xythos.storageServer.api.FileSystemTopLevelDirectory;
import com.xythos.storageServer.api.FileSystemDirectory;
import com.xythos.storageServer.api.FileSystemFile;
import com.xythos.storageServer.api.FileSystemUtil;
import com.xythos.storageServer.api.Parameters;
import com.xythos.storageServer.api.StorageServerException;
import com.xythos.storageServer.properties.api.TimestampProperty;
import com.xythos.storageServer.properties.api.BooleanProperty;
import com.xythos.storageServer.properties.api.LongProperty;
import com.xythos.storageServer.properties.api.StringProperty;
import com.xythos.storageServer.properties.api.TimestampArrayProperty;
import com.xythos.storageServer.properties.api.BooleanArrayProperty;
import com.xythos.storageServer.properties.api.LongArrayProperty;
import com.xythos.storageServer.properties.api.StringArrayProperty;
import com.xythos.storageServer.properties.api.Property;
import com.xythos.storageServer.properties.api.PropertyDefinition;
import com.xythos.storageServer.properties.api.PropertyDefinitionManager;
import com.xythos.storageServer.properties.api.PropertyValueFormatException;
import com.xythos.storageServer.api.StorageServerEventBroker;
import com.xythos.storageServer.permissions.api.AccessControlEntry;
import com.xythos.storageServer.permissions.api.DirectoryAccessControlEntry;
import com.xythos.util.api.ServletUtil;
import java.util.Enumeration;

/**
 *
 * @author david brown
 *  This servlet accepts and processes Http requests for both GET and POST methods
 *  using the single method: processRequest().
 *  This server expects 5 request parameters to complete the file upload and final streaming
 *  to the Xythos filesystem.
 */
public class FileUploadServlet extends HttpServlet {
    //private static final String BASE_DIRECTORY = "/home/dwbrown/Public";
    private boolean found = false;
    private static Logger log = Logger.getLogger(FileUploadServlet.class);
    private 
    String absolutePath = null;
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, Exception {
        DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(10000000);

        List fileItems = fu.parseRequest(request);
        Iterator itr = fileItems.iterator();
        Enumeration names = request.getParameterNames();
        String vsname           = null;
        String userName     = null;
        String entryName    = null;
        String mimeType     = null;
        String description    = null;
        int fieldPos = 0;
        int vsPos = 0;
        String fileName = null;

        while(itr.hasNext()) {
          FileItem fi = (FileItem)itr.next();
          long fileSize = -1;

          //Check if not form field so as to only handle the file inputs
          //else condition handles the submit button input
          if(!fi.isFormField()) { // process the submit button
            fileName = fi.getName();
            fileSize = fi.getSize();
            log.debug("\nNAME: "+fileName);
            log.debug("SIZE: "+fileSize);
            log.debug(fi.getOutputStream().toString());
            File fNew = new File(request.getRealPath("/"), fileName);
            //File fNew = new File(BASE_DIRECTORY, fileName);
            absolutePath = fNew.getAbsolutePath();
            log.debug("FileUploadServlet absolute path: " +absolutePath);
            fi.write(fNew);
          }
          else { // process the request params.
                String fieldName = fi.getFieldName();
                String fieldVal = fi.getString();
                log.debug("Field name: "+fieldName + "\tField value: " + fieldVal);

               if(fieldName.equalsIgnoreCase("vsname"))
                   vsname = fieldVal; // Virtual Server name
               else
                   if(fieldName.equalsIgnoreCase("username"))
                    userName = fieldVal; // User name
                   else
                    if(fieldName.equalsIgnoreCase("entryname")) 
                        entryName = fieldVal; // uploaded file Xythos virtual file system directory destination.
                    else
                        if(fieldName.equalsIgnoreCase("mimetype"))
                            mimeType = fieldVal; // uploaded file mimetype (content-type).
                        else
                            if(fieldName.equalsIgnoreCase("description"))
                                description = fieldVal; // uploaded file description.

          }  // END ELSE
        } // END WHILE()
        
         // Process the file to Xythos
        FileInputStream fis = new FileInputStream(absolutePath);
        String uploadedFile = uploadFileToXythos(vsname, entryName, userName,  fis, mimeType, fileName, description);
        log.debug("Upload File: " + uploadedFile.toString());
        response.setContentType("text/html");
        
        // build and output the servlet response
        PrintWriter out = response.getWriter();
        out.println(uploadedFile);
        out.close();
        //ObjectOutputStream oos = new ObjectOutputStream (
                //new BufferedOutputStream (response.getOutputStream ()));
        //oos.writeObject (uploadedFile);
        //oos.flush ();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
    /**
     * 
     * @param p_username - Xythos user name
     * @param p_virtualserver - Xythos virtual server
     * @return - Xythos security context for this user.
     * @throws com.xythos.common.api.XythosException
     */
    private static Context getUserContext(String p_username, String p_virtualserver) throws XythosException {
        UserBase l_user = null;
        Context l_context = null;
        if (p_username == null) {
            log.debug("getUserContext().p_username field empty!!");
            throw new WFSSecurityException("The user field was empty!");
        }
        if (p_username.equalsIgnoreCase("ADMIN")) {
            return (AdminUtil.getContextForAdmin("1.1.1.1"));
        }

        l_user = PrincipalManager.findUser(p_username, p_virtualserver);
        if (l_user == null) {
            log.debug("getUserContext().l_user: NOT FOUND!");
            throw new WFSSecurityException("No user found!");
        }
        Properties l_prop = new Properties();
        l_prop.put("Xythos.Logger.IPAddress", "10.3.66.105");

        l_context = ContextFactory.create(l_user, l_prop);

        return l_context;
    } // getUserContext()
    /**
     *  Received from P. Dumas.
     * @param virtualServerName - Xythos virtual server
     * @param directory - uploaded file Xythos virtual filesystem destination repostitory.
     * @param user - Xythos user
     * @param fileInputStream - Java FileInputStream
     * @param contentType - uploaded file content or mimetype
     * @param xythosFileName - uploaded file's Xythos file system filename
     * @param description - uploaded file description Xythos metadata
     * @return - String array Xythos file system response.
     * @throws java.lang.Exception
     */
    public static String uploadFileToXythos(String virtualServerName, String directory, String user, FileInputStream fileInputStream, String contentType, String xythosFileName, String description) throws Exception {
        String[] entryInfo = new String[17];
        Context context = null;
        try {

            if (isValidVirtualServer(virtualServerName) == false) {
                throw new IllegalArgumentException("Invalid virtual server: " + virtualServerName);
            }

            context = getUserContext(user, virtualServerName);

            VirtualServer vs = VirtualServer.find(virtualServerName);
            
            if(vs==null) {
                throw new IllegalArgumentException("Virtual Server name does not exist in Xythos.");
            }
            
            UserBase myXythosUser = PrincipalManager.findUser(user, vs.getName());

            if(myXythosUser.getHomeDirectoryVirtualServer() == null) {
                throw new IllegalArgumentException("User's home directory property net set.");
            }

            if (myXythosUser != null) {
                if (log.isDebugEnabled())
                    log.debug("========== Begin Upload  to xythos ================== ");


                createDirectoryStructure(myXythosUser.getHomeDirectoryVirtualServer(), directory, myXythosUser, context, myXythosUser);

                CreateFileData uploadFile   = new CreateFileData(myXythosUser.getHomeDirectoryVirtualServer(),
                        directory,
                        xythosFileName,
                        contentType,
                        myXythosUser.getPrincipalID(),
                        fileInputStream);

                uploadFile.setOverwriteFlag(true);
                FileSystemFile myXythosFile = FileSystem.createFile(uploadFile, context);
                myXythosFile.createComment(myXythosUser.getPrincipalID(), description);
                context.commitContext();
                if (log.isDebugEnabled()) {
                    log.debug("========== File copied to xythos ================== ");
                    log.debug("========== Name ID = " + myXythosFile.getNameID() + " ================== ");
                }

                entryInfo[0] = myXythosFile.getCreatedByPrincipalID(); // File created by principal ID
                entryInfo[1] = myXythosFile.getLastUpdatedByPrincipalID(); // File last update by principal ID
                entryInfo[2] = myXythosFile.getCreationTimestamp().toString(); // File create timestamp
                entryInfo[3] = myXythosFile.getEntryOwnerPrincipalID();  //File entry owner principal ID
                entryInfo[4] = myXythosFile.getFileContentType(); // File content type:
                entryInfo[5] = myXythosFile.getFileMimeType();  //File mime type
                entryInfo[6] = myXythosFile.getFileRevisionID(); // File revision ID
                entryInfo[7] = myXythosFile.getFileVersionComment(); // File version comment
                entryInfo[8] = myXythosFile.getFileVersionCreatedBy(); // File version created by
                entryInfo[9] = "" + myXythosFile.getEntrySize();  // File entry size
                entryInfo[10] = myXythosFile.getFileVersionDigest(); // File version digest
                entryInfo[11] = myXythosFile.getFileVersionStorageFileName(); // File version storage file name
                entryInfo[12] = myXythosFile.getFileVersionTemporaryStorageFileName(); // File version temp storage file name
                entryInfo[13] = myXythosFile.getName(); // File name
                entryInfo[14] = myXythosFile.getNameID(); // File name I/d
                entryInfo[15] = "" + myXythosFile.getLatestFileVersion(); // File lastest file version
                entryInfo[16] = myXythosFile.getFileContentLanguage(); // File content language
                XythosProxyResponse pr = new XythosProxyResponse(XythosProxyResponse.COMPLETE, entryInfo);
                log.debug("uploadFileToXythos().pr: " + pr);
                return pr.getTextList()[XythosProxyResponse.FILE_NAME_ID];
            }
        }
        catch (XythosException e) {
            log.error("caught xythos exception", e);

            try {
                context.rollbackContext();
            }
            catch (XythosException ex) {
                log.warn("couldn't rollback xythos context");
            }
            throw e;
        }
        catch (Exception ex) {
            log.error("Exception in uploadFileToXythos()", ex);
            throw ex;
        }
        finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
            catch (Exception ex) {
                // do nothing
            }
        }
        return null;
    } // END UPLOADFILETOXYTHOS()

    private static boolean isValidVirtualServer(String virtualServerName) {
        return VirtualServer.find(virtualServerName) != null;
    }

    private static boolean createDirectoryStructure(VirtualServer xythosVirtualServer, String path,
            UserBase xythosOwnerUser, Context xythosContext, UserBase externalXythosUser)
      throws XythosException
    {
        if (log.isDebugEnabled()) {
                log.debug("createDirectoryStructure()");
                log.debug("xythosVirtualServer               = " + xythosVirtualServer);
                log.debug("xythosOwnerUser                   = " + xythosOwnerUser);
                log.debug("xythosContext                          = " + xythosContext);
                log.debug("externalXythosUser               = " + externalXythosUser);
        }

        //first see if the project base exists, if so we are good, keep going
        FileSystemDirectory closingBaseDirectory = (FileSystemDirectory) FileSystem.findEntry(xythosVirtualServer,
                                                       path, false, xythosContext);

        if (closingBaseDirectory == null) {
            log.debug("directory does not exist, create it");

            //add code to create directory base here.
            String[] dirs    = ServletUtil.parseName(path);
            String   baseDir = "";

            for (int i = 0; i < dirs.length; i++) {
                String tmpDir = dirs[i];

                log.debug("create dir named = " + tmpDir);

                FileSystemDirectory xythosTmpDir = (FileSystemDirectory) FileSystem.findEntry(xythosVirtualServer,
                                                       baseDir + "/" + tmpDir, false, xythosContext);

                if (xythosTmpDir == null) {
                    log.debug("directory does not exist, create.");

                    CreateDirectoryData tmpDirectory = new CreateDirectoryData(xythosVirtualServer, baseDir,
                                                           tmpDir, xythosOwnerUser.getPrincipalID());

                    xythosTmpDir = FileSystem.createDirectory(tmpDirectory, xythosContext);

                    //set permissions so "xythos" user can see the directory
                    DirectoryAccessControlEntry tmpDirectoryAce =
                        (DirectoryAccessControlEntry) xythosTmpDir
                            .getAccessControlEntry(externalXythosUser.getPrincipalID());

                    tmpDirectoryAce.setAccessControlEntry(false /*recurse*/, new Boolean(true) /*read*/,
                                                          new Boolean(true) /*write*/,
                                                          new Boolean(false) /*delete*/,
                                                          new Boolean(false) /*permissionable*/,
                                                          new Boolean(false) /*inherit read*/,
                                                          new Boolean(false) /*inherit write*/,
                                                          new Boolean(false) /*interit delete*/,
                                                          new Boolean(false) /*inherit permissionable*/);
                }
                else {
                    log.debug("directory already exists.  skip to next level in path.");
                }

                baseDir = baseDir + "/" + tmpDir;
            }
        }
        else {
            log.debug("project directory exists, proceed if we can write to it.");

            if (!xythosContext.isEntryWriteable(closingBaseDirectory)) {
                log.warn("user " + xythosOwnerUser.getID() + "cannot write to the following directory: " + path
                         + " base directory.  short circuit.");

                return false;
            }
        }

        return true;
    }
    
    /**
     *  This method compares the input Virtual Server name to a list of VirtualServer objects obtained
     *  from the system. If the VirtualServer object is found then the VirtualServer object is returned
     * @param virtualServerName
     * @return VirtualServer object
     */
    private static VirtualServer getXythosVirtualServer(String virtualServerName)  {
        return VirtualServer.find(virtualServerName);
        /*
        VirtualServer vs[] = VirtualServer.findVirtualServers();
            int vpos = 0;
            boolean found = false;
            for(int i=0; i<vs.length && !found; i++) {
                String vsName = vs[i].getName();  // just get the first one for now
                log.debug("FileUploadServlet.vsName: " + vsName);
                if(vsName.equalsIgnoreCase(virtualServerName)) {
                    found = true;
                    vpos = i;
                    break;
                }
            } // END FOR()
        return vs[vpos];
         */
    }
}

