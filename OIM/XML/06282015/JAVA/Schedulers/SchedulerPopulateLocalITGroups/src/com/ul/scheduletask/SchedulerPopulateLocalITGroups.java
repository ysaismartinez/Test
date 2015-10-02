package com.ul.scheduletask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;  
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;  
import java.util.Iterator;
import java.util.List;  
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.text.StrSubstitutor;














import com.thortech.xl.client.dataobj.tcDataBaseClient;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;

import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Security.XLClientSecurityAssociation;
import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.identity.usermgmt.api.UserManager;  
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;  
import oracle.iam.identity.usermgmt.vo.User;  
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.OIMInternalClient;
import oracle.iam.platform.Platform;  
import oracle.iam.request.vo.Beneficiary;  
import oracle.iam.request.vo.RequestData;  
import oracle.iam.platform.Platform;
import oracle.iam.scheduler.vo.TaskSupport;

public class SchedulerPopulateLocalITGroups  extends TaskSupport {

	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	private OIMClient oimClient = null;
	String classname="SchedulerPopulateLocalITGroups.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	
	
	//UserRepository userRepository=null;
//	UserInfo usrInfo=null;
	
	public void execute(HashMap hm) throws Exception 
    {
		/*
		String methodName="execute";

		try
        {
        	//Read from file
            LOGGER.info(classname + methodName+ "----->>>>        " + " Read the file1");
            BufferedReader in = new BufferedReader(new FileReader("/oracle/scheduletask/LocalITCode"));
            String str=null;
            ArrayList<String> lines = new ArrayList<String>();
            while((str = in.readLine()) != null){
                lines.add(str);
                LOGGER.info(classname + methodName+ "----->>>>        " + " Str Value"+str);
            }
            String[] linesArray = lines.toArray(new String[lines.size()]);
            LOGGER.info(classname + methodName+ "----->>>>        " + " Lines size"+lines.size());
           
            LOGGER.info(classname + methodName+ "----->>>>        " + " Read the file2");
            BufferedReader in1 = new BufferedReader(new FileReader("/oracle/scheduletask/LocalITDecode"));
            String str1=null;
            ArrayList<String> lines1 = new ArrayList<String>();
            while((str1 = in1.readLine()) != null){
                lines1.add(str1);
                LOGGER.info(classname + methodName+ "----->>>>        " + " Str Value"+str1);
                
            }
            String[] linesArray1 = lines1.toArray(new String[lines1.size()]);
            LOGGER.info(classname + methodName+ "----->>>>        " + " Lines1 size"+lines1.size());
            
            //lookup definition. 
            tcLookupOperationsIntf lookup= Platform.getService(tcLookupOperationsIntf.class);
            
        LOGGER.info(classname + methodName+ "----->>>>        " + "Inside =========");
        String lookupName = "TestLookup56";
        lookup.addLookupCode(lookupName);
        LOGGER.info(classname + methodName+ "----->>>>        " + "Lookup Table Created");
        LOGGER.info(classname + methodName+ "----->>>>        " + " Lines Length"+linesArray1.length);
        LOGGER.info(classname + methodName+ "----->>>>        " + " Lines Length"+linesArray.length);
        for( int i = 0; i < linesArray1.length - 1; i++)
        {
            lookup.addLookupValue(lookupName,linesArray[i],linesArray1[i],"","");
            LOGGER.info(classname + methodName+ "----->>>>        " + " Lines size"+linesArray[i]);
            LOGGER.info(classname + methodName+ "----->>>>        " + " Lines size"+linesArray1[i]);
            
        }
       // String lookupCode="basedn";
       // String lookupValue="dc=test,dc=com";
        //lookup.addLookupValue(lookupName,lookupCode,lookupValue,"","");
        
        LOGGER.info(classname + methodName+ "----->>>>        " + "Lookup Value Added");
        
        }
        catch (Exception e)
        {
                    System.out.print(" Exception"+ e);
        }
    */    
		String methodName="execute";

		try
        {
			/*
        	//Read from file
            LOGGER.info(classname + methodName+ "----->>>>        " + " Read the file1");
            BufferedReader in = new BufferedReader(new FileReader("/oracle/scheduletask/LocalITCode"));
            */
		    LOGGER.info(classname + methodName+ "----->>>>        " + "Inside =========");
	        // Connection String
	        URL url;
        	String lookupName = (String) hm.get("Lookup Name");
        	String server = (String) hm.get("Server");
        	String fileName = (String) hm.get("FileName");
	        url = new URL(server + "/" + fileName);
		    LOGGER.info(classname + methodName+ "----->>>>        LookupName" + lookupName +"Server" + server+" File Name" +fileName  );
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            LOGGER.info(classname + methodName+ "----->>>>        Reading file start.");
	        String str=null;
            //lookup definition. 
            tcLookupOperationsIntf lookup= Platform.getService(tcLookupOperationsIntf.class);
            //String lookupName = "TestLookup56";
            lookup.removeLookupCode(lookupName);
            lookup.addLookupCode(lookupName);
            while((str = in.readLine()) != null)
            {
            	  String[] temp=null;
         	      temp = str.split("\\|");
         	      LOGGER.info(classname + methodName+ "----->>>>        " + "STR"+str + "temp0"+ temp[0] + "temp1"+ temp[1]);
         	      if(!temp[1].equalsIgnoreCase("Email List"))
         	      {
         	    	  lookup.addLookupValue(lookupName,temp[0],temp[1],"","");
         	    	  LOGGER.info(classname + methodName+ "----->>>>        " + "Lookup Value Added");
         	      }
         	      else
         	      {
         	    	 LOGGER.info(classname + methodName+ "----->>>>        " + "First Line");
          	      }
                  
            }
        
        }
        catch (Exception e)
        {
                    System.out.print(" Exception"+ e);
        }

    }
     

	   @Override
	    public HashMap getAttributes() 
	    {
	        return null;
	    }
	 
	    @Override
	    public void setAttributes() 
	    {
	         
	    }
}
