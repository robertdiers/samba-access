/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sambaaccess;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 *
 * @author robert.diers
 */
public class SambaAccess {
    
    private static String samba = "";
    private static String user = "";
    private static String password = "";
    private static String action = "";
    private static String folder = "";
    private static NtlmPasswordAuthentication auth = null;

    public static void main(String[] args) {
        try {       
            
            samba = args[0];
            user = args[1];
            password = args[2];	
            action = args[3];
            if (args.length > 4) folder = args[4];
            
            //connecting to drive and read in of files
            auth = new NtlmPasswordAuthentication("", user, password);
            
            if (action.equalsIgnoreCase("upload")) {                
                uploadFile("", new File(folder), 0);   
            } else if (action.equalsIgnoreCase("delete")) {
                deleteFile(new SmbFile(samba, auth));
            } else if (action.equalsIgnoreCase("check")) {
                checkFile();
            } else {
                System.out.println("unsupported action: "+action);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void uploadFile(String folder, File file, long counter) throws MalformedURLException, SmbException, UnknownHostException, IOException {        
        
        System.out.println(folder+file.getName()); 
                
        if (file.isDirectory()) {
            if (counter != 0) {
                folder = folder+file.getName()+"/";
                SmbFile smbFile = new SmbFile(samba+folder, auth);
                if (!smbFile.exists()) {                          
                    smbFile.mkdirs();
                }
            }
            counter++;
            for (File tmp : file.listFiles()) {
                uploadFile(folder, tmp, counter);
            }            
        } else {        
            //replace file
            SmbFile smbFile = new SmbFile(samba+folder+file.getName(), auth);
            if (!smbFile.exists()) {                          
                smbFile.createNewFile();
            }
            SmbFileOutputStream out = new SmbFileOutputStream(smbFile, false);
            out.write(Files.readAllBytes(file.toPath()));
            out.close();
        }        
        
    }
    
    private static void deleteFile(SmbFile smbFile) throws MalformedURLException, SmbException {
        
        if (smbFile.isDirectory()) {
            for (SmbFile tmp : smbFile.listFiles()) {
                deleteFile(tmp);
            }
        } 
        
        smbFile.delete();
        System.out.println("deleted: "+smbFile.getName());        
    }
    
    private static void checkFile() throws MalformedURLException, SmbException, IOException {
        
        SmbFile smbFile = new SmbFile(samba, auth);	
        
        //list content of folder
        SmbFile[] inFiles = smbFile.listFiles();
        if (inFiles.length > 0) {
            for (SmbFile file : inFiles) {
                System.out.println(file.getName() + " " + file.getPermission().getName());
            }
        }
    }

}
