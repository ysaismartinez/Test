package com.ul.scheduletask;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;  
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

public class SchedulerRemoveEmail  extends TaskSupport {

	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	private OIMClient oimClient = null;
	String classname="SchedulerRemoveEmail.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	
	
	//UserRepository userRepository=null;
//	UserInfo usrInfo=null;
	
	public void execute(HashMap hm) throws Exception 
    {
		String methodName="execute";
		String plainTextPassword = null;
		String userLogin = null;
		String userKey = null;
		String location = null;
	    tcDataProvider dbProvider = null;
	    oracle.iam.platform.OIMInternalClient localClient = new OIMInternalClient();
	    LOGGER.info(classname + methodName+ "----->>>>        " + "Inside Schedule Task >>>>> ");
		LOGGER.info(classname + methodName+ "----->>>>        " + "Initiating signature based login to OIM");
		//localClient.signatureLogin("UBSUser3");
		localClient.signatureLogin("xelsysadm");
		oimClient = localClient;
		LOGGER.info(classname + methodName+ "----->>>>        " + "After Authentication");
	     XLClientSecurityAssociation.setClientHandle(oimClient);
	     //Connection to OIM Schema
	     dbProvider = new tcDataBaseClient(); 
	     //Stores the result set of an executed query
	     tcDataSet dataSet = new tcDataSet(); 
	     LOGGER.info(classname + methodName+ "----->>>>        " + "db op");
	     //NEW
	     String numOfDays = (String) hm.get("Number Of Days");
	     
	     //
	 	 //Query Users table to get a single user
	     // String query = "select USR_KEY,USR_LOGIN,USR_CREATE,USR_UPDATE,USR_END_DATE from usr where (USR_STATUS like 'Disabled' and USR_UPDATE < SYSDATE - 5 and  USR_UPDATE > SYSDATE - 28)";
	     String query = "select USR_KEY,USR_LOGIN,USR_CREATE,USR_UPDATE,USR_END_DATE from usr where (USR_STATUS like 'Disabled' and USR_UPDATE < SYSDATE - "+numOfDays+")";
	         
	     //Set query and database provider     
	     dataSet.setQuery(dbProvider, query); 
	     //execute query and store results into dataSet object
	     dataSet.executeQuery(); 
	     LOGGER.info(classname + methodName+ "----->>>>        " + "Done Execute Query ");
	     UserRepository userRepository = new DBUserRepository();
	   //Get total records from result set
	     int records = dataSet.getTotalRowCount(); 
	     UserManager userService = (UserManager)Platform.getService(UserManager.class); 
		 for(int i = 0; i < records; i++) 
	     {
			 
	 	    dataSet.goToRow(i); //move pointer to next record
	 	    userLogin = dataSet.getString("USR_LOGIN");
	 	    LOGGER.info(classname + methodName+ "----->>>>        " + "User Login  "+userLogin);
	 	    userKey = dataSet.getString("USR_KEY");
	 	    LOGGER.info(classname + methodName+ "----->>>>        " + "User Key  "+userKey);

	 	    /*
		    UserInfo usrInfo = userRepository.getUserAndManagerInfo(userLogin);
	        usrInfo.setUserEmailID("test@test.com");
	        LOGGER.info(classname + methodName+ "----->>>>        " + "After Setting Email to Empty"+usrInfo.getUserEmailID());
	        
	        */
	 	   HashMap hm1 = new HashMap();
	 	    LOGGER.info(classname + methodName+ "----->>>>        " + "Before Setting Email to Empty");
	 	   hm1.put(UserManagerConstants.AttributeName.EMAIL.getId(), "");
		   User user = new User(userKey,hm1);
	 	   userService.modify(AttributeName.USER_LOGIN.getId(),userLogin,user);
	 	   LOGGER.info(classname + methodName+ "----->>>>        " + "After "+user.getEmail());
		 	   //  User user =userService.getDetails(userLogin, null, true) ;
		    // user.setEmail("testxx@test.com");
			 // userService.modify(user) ;
			  LOGGER.info(classname + methodName+ "----->>>>        " + "Exiting"+user.getEmail());

	     }
		 
 	     LOGGER.info(classname + methodName+ "----->>>>        " + "Removed Email from Disabled Users.");
 		
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
