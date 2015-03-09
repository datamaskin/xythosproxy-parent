<%-- 
    Document   : ProcessFileUpload
    Created on : Jun 15, 2008, 7:36:54 PM
    Author     : david
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="org.apache.commons.fileupload.DiskFileUpload"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.io.File"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
<title>Process File Upload</title>
</head>
<%
        System.out.println("Content Type ="+request.getContentType());

        DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(10000000);

        List fileItems = fu.parseRequest(request);
        Iterator itr = fileItems.iterator();

        while(itr.hasNext()) {
          FileItem fi = (FileItem)itr.next();
          String fileName = null;
          long fileSize = -1;

          //Check if not form field so as to only handle the file inputs
          //else condition handles the submit button input
          if(!fi.isFormField()) {
            fileName = fi.getName();
            fileSize = fi.getSize();
            System.out.println("\nNAME: "+fileName);
            System.out.println("SIZE: "+fileSize);
            System.out.println(fi.getOutputStream().toString());
            File fNew= new File(application.getRealPath("/"), fileName);

            System.out.println(fNew.getAbsolutePath());
            fi.write(fNew);
          }
          else {
            System.out.println("Field ="+fi.getFieldName());
          }
        }
%>
<body>
Upload Successful!!
</body>
</html>
