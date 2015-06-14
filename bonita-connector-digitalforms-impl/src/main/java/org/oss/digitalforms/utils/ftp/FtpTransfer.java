/*******************************************************************************
 * Copyright (c) 2015 Open Software Solutions GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 *
 * Contributors:
 *     Open Software Solutions GmbH
 ******************************************************************************/
package org.oss.digitalforms.utils.ftp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * @author Magnus Karlsson, Donat Müller
 *
 */
public class FtpTransfer {
	   private Logger LOGGER = Logger.getLogger(this.getClass().getName());
	   private final FTPClient ftp;

	   public FtpTransfer(String host, String user, String pwd) throws IOException {
		   this(host,user,pwd,false);
	   }
	    public FtpTransfer(String host, String user, String pwd, boolean debug) throws IOException{
	        ftp = new FTPClient();
	        if (debug) {
	        	ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
	        }
	        int reply;
	        ftp.connect(host);
	        reply = ftp.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(reply)) {
	            ftp.disconnect();
	            throw new IOException("Connection refused: " + ftp.getReplyString());
	        }
	        if (!ftp.login(user, pwd)) {
	            throw new IOException("Login failed");

	        }
	        ftp.setFileType(FTP.BINARY_FILE_TYPE);
	        ftp.enterLocalPassiveMode();
	        LOGGER.info("FTP Connected to " + host);
	    }


	    public void uploadFile(byte[] fileContent, String fileName, String hostDir) throws IOException {
	    	LOGGER.finest("Start uploading " + fileName + " Size: " + fileContent.length + " bytes");
	    	InputStream input = new ByteArrayInputStream(fileContent);
	    	ftp.storeFile(hostDir + File.separator + fileName, input);
	    	LOGGER.info("Done uploading " + fileName);
	    }

	    public void uploadFile(File file, String fileName, String hostDir) throws IOException {
	    	LOGGER.info("Start uploading " + fileName );
	    	InputStream input = new FileInputStream(file);
	    	ftp.storeFile(hostDir + File.separator + fileName, input);
	    	LOGGER.info("Done uploading " + fileName);
	    }

	    public void uploadFile(InputStream input, String fileName, String hostDir) throws IOException {
	    	LOGGER.info("Start uploading " + fileName );
	    	ftp.storeFile(hostDir + File.separator + fileName, input);
	    	LOGGER.info("Done uploading " + fileName);
	    }


	    public FTPFile[] listDirectory(String hostDir) throws IOException{
	    	ftp.changeWorkingDirectory(hostDir);
	        if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
	        	return ftp.listFiles();
	        }
	        throw new IOException("Directory does not exist: " + ftp.getReplyString());
	    }

	    public void createFolder(String folderName) throws IOException {
	    	ftp.makeDirectory(folderName);
	    }

	    public boolean fileExists(FTPFile[] ftpFiles, String fileName){
	    	if(ftpFiles != null && ftpFiles.length > 0) {
	    		for (FTPFile file : ftpFiles) {
	    			if (file.isFile()) {
	    				if(fileName.equals(file.getName())){
			    			return true;
		    			}
	    			}
	    		}
	    	}
	    	return false;
	    }

	    public boolean folderExists(FTPFile[] ftpFiles, String FolderName){
	    	if(ftpFiles != null && ftpFiles.length > 0) {
	    		for (FTPFile file : ftpFiles) {
	    			if (!file.isFile()) {
	    				if(FolderName.equals(file.getName())){
			    			return true;
		    			}
	    			}
	    		}
	    	}
	    	return false;
	    }

	    public File downloadToTempFile(String hostDir, String fileName) throws IOException{
	    	ftp.changeWorkingDirectory(hostDir);
	        if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
	        	boolean removeFile = true;
	        	File temp = File.createTempFile("ftp", ".data");
	        	FileOutputStream output = new FileOutputStream(temp);
	        	try {
	        		ftp.retrieveFile(fileName, output);
	    	        if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
	    	        	removeFile = false;
	    	        	return temp;
	    	        } else {
	    	        	throw new IOException("Unable to download file: " + ftp.getReplyString());
	    	        }
	        	} finally {
	        		output.close();
	        		if (removeFile) {
	        			temp.delete();
	        		}
	        	}
	        } else {
	        	throw new IOException("Directory does not exist: " + ftp.getReplyString());
	        }
	    }

	    public void deleteFile(String hostDir, String fileName) throws IOException{
	    	ftp.changeWorkingDirectory(hostDir);
	        if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
	        	ftp.deleteFile(fileName);
    	        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
    	        	throw new IOException("Unable to delete file: " + ftp.getReplyString());
    	        }
    	        return;
	        }
        	throw new IOException("Directory does not exist: " + ftp.getReplyString());
	    }

	    public void disconnect(){
	        if (this.ftp.isConnected()) {
	            try {
	                this.ftp.logout();
	                this.ftp.disconnect();
			        LOGGER.finest("FTP Disconnect");
	            } catch (IOException f) {
	                // do nothing as file is already saved to server
	            }
	        }
	    }
}
