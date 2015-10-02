package com.ul.eventhandlers;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.iam.conf.api.SystemConfigurationService;
import oracle.iam.conf.vo.SystemProperty;
import oracle.iam.identity.exception.AccessDeniedException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.orgmgmt.api.OrganizationManager;
import oracle.iam.identity.orgmgmt.vo.Organization;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.impl.UserMgrUtil;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.identity.utils.Utils;
import oracle.iam.identity.vo.Identity;
import oracle.iam.notification.api.NotificationService;
import oracle.iam.notification.exception.EventException;
import oracle.iam.notification.exception.MultipleTemplateException;
import oracle.iam.notification.exception.NotificationException;
import oracle.iam.notification.exception.NotificationResolverNotFoundException;
import oracle.iam.notification.exception.TemplateNotFoundException;
import oracle.iam.notification.exception.UnresolvedNotificationDataException;
import oracle.iam.notification.exception.UserDetailsNotFoundException;
import oracle.iam.notification.vo.NotificationEvent;
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.impl.PasswordMgmtLogger;
import oracle.iam.passwordmgmt.vo.Constants;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.Platform;
import oracle.iam.platform.context.ContextAware;
import oracle.iam.platform.context.ContextAwareString;
import oracle.iam.platform.entitymgr.EntityManager;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.kernel.spi.PostProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.platform.utils.crypto.CryptoException;
import oracle.iam.platform.utils.crypto.CryptoUtil;

import java.util.*;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import Thor.API.tcResultSet;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import oracle.core.ojdl.logging.ODLLogger;


public class EnableUserOps implements PostProcessHandler{
	
	    private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
		//protected static Logger logger = Logger.getLogger("BHSF.CUSTOM");
		private static final String LOOKUP_COLUMN_DECODE = "Lookup Definition.Lookup Code Information.Decode";
		String classname="EnableUserOps.";
		public static final String IT_RESOURCE_KEY = "IT Resources.Key";
	    public static final String IT_RESOURCE_NAME = "IT Resources.Name";
	    public static final String IT_RESOURCE_PARAM_NAME = "IT Resources Type Parameter.Name";
	    public static final String IT_RESOURCE_PARAM_VALUE = "IT Resources Type Parameter Value.Value";
		

		public boolean cancel(long l, long l1, AbstractGenericOrchestration abstractGenericOrchestration)
		{
			return false;
		}
		public void compensate(long l, long l1, AbstractGenericOrchestration abstractGenericOrchestration) {
		}

		@SuppressWarnings("deprecation")
		public EventResult execute(long processId, long eventID, Orchestration orchestration) {
			String methodName="execute";

			try {
				
				if (orchestration.getOperation().equalsIgnoreCase("ENABLE"))
				{
				LOGGER.info(classname + methodName+ "----->>>>        " + "ENABLE USER OPS");
				//LOGGER.info(classname + methodName+ "----->>>>        " + "execute: processId = " + processId + "; eventId = " + eventID + "; orchestration = " + orchestration);
				LOGGER.info(classname + methodName+ "----->>>>        " + "execute: processId = " + processId + "; eventId = " + eventID + "; orchestration = " + orchestration);
				HashMap parameters = orchestration.getParameters();
	            HashMap<String, Serializable> interParameters1 = orchestration.getInterEventData(); //contains old and new values of user
				UserManager um = null;
				//LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);
				um = (UserManager)Platform.getService(UserManager.class);
				EntityManager mgr=Platform.getService(EntityManager.class);
				
				LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Inter Parameters = " + interParameters1);
				  
				//String userLogin = (String)parameters.get("User Login");
				String loginid = null; 
			    String loginid1 = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
				   if(loginid1  != null)
		            {
					   loginid = loginid1.toString();
		            }
				   else
		            {
		                String userKey = orchestration.getTarget().getEntityId();
		                loginid = UserMgrUtil.getUserLoginFromId(userKey);
		            }
				   
				   if(loginid == null)
				   {
				   	    Identity userNewState = (User)interParameters1.get("NEW_USER_STATE");
		        		loginid = userNewState.getAttribute("User Login").toString(); //Get target user's login
		        		LOGGER.info(classname + methodName+ "----->>>>        " + "User Login New User State.  "+loginid );
		           }
				   
		        LOGGER.info(classname + methodName+ "----->>>>        " + "User Manager service retrieved sucessfully.  "+loginid );
                HashMap userDetails = new HashMap(); 
                userDetails =um.getDetails(loginid, null, true).getAttributes();           
                //logger.debug("userDetails ::  " + userDetails);
                //LOGGER.info(classname + methodName+ "----->>>>        " + "userDetails " + userDetails);
                LOGGER.info(classname + methodName+ "----->>>>        " + "Before Retrieving Org Key.  " );
                String orgName = userDetails.get(UserManagerConstants.AttributeName.USER_ORGANIZATION.getId()).toString();
    			LOGGER.info(classname + methodName+ "----->>>>        " + "OrgName" +orgName );
    			OrganizationManager orgManager=Platform.getService(OrganizationManager.class);
				Organization org=orgManager.getDetails(orgName, new HashSet(), false);
				String orgName1=org.getAttribute("Organization Name").toString();
				LOGGER.info(classname + methodName+ "----->>>>        " + "OrgName1" +orgName1 );
				//
		        UserRepository userRepository = new DBUserRepository();
                UserInfo usrInfo = userRepository.getUserAndManagerInfo(loginid);
				//String userType= getParamaterValue(parameters, "Role");
				String userType= usrInfo.getAttribute("Role").toString();
			   
				LOGGER.info(classname + methodName+ "----->>>>        " + "User Type" +userType);
				String userTypes=null;
				tcLookupOperationsIntf lookupOps = Platform
              		     .getService(tcLookupOperationsIntf.class);
               	   tcResultSet values = lookupOps.getLookupValues("Lookup.Excluded.Email.UserTypes");
               	   for (int i = 0; i < values.getRowCount(); i++) 
               	   {
	               	    values.goToRow(i);
	               	    System.out.print(values.getStringValue("Lookup Definition.Lookup Code Information.Decode"));
	               	    LOGGER.info(classname + methodName+ "----->>>>        " + ","+ values.getStringValue("Lookup Definition.Lookup Code Information.Code Key"));
	               	    userTypes=values.getStringValue("Lookup Definition.Lookup Code Information.Decode");
	               	}
               	LOGGER.info(classname + methodName+ "----->>>>        " + "USERTYPES" +userTypes);
				int iGenerateEmail=1;
               	String[] tokens = userTypes.split(",");
				for (int j = 0; j < tokens.length; j++)
				{
					LOGGER.info(classname + methodName+ "----->>>>        " + "Token"+tokens[j]);
					
					if(userType.equalsIgnoreCase(tokens[j]))
					{
						LOGGER.info(classname + methodName+ "----->>>>        " + "USERTYPES Matches");
						iGenerateEmail=0;
					}
						
				}
				
				//
    			if(orgName1.equalsIgnoreCase("Internal User") && iGenerateEmail == 1 )
				//if(orgName.equals("Internal User"))
				{

				//String loginid = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
				String firstname = usrInfo.getFirstName().toString();
				LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstname);
				//String firstname = getParamaterValue(parameters, AttributeName.FIRSTNAME.getName().toString());
				String lastname = usrInfo.getLastName().toString();
				LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name  is: " +lastname);
				//String lastname = getParamaterValue(parameters, AttributeName.LASTNAME.getName().toString());
				//String preferredname= getParamaterValue(parameters, "usr_PreferredName");
				String preferredname= usrInfo.getAttribute("usr_PreferredName").toString();

				//String preferredname= usrInfo.getAttribute("usr_PreferredName").toString();
				LOGGER.info(classname + methodName+ "----->>>>        " + "Title/Preferred Name  is: " +preferredname);
				//String country= usrInfo.getAttribute("Country").toString();
				String country= getParamaterValue(parameters, "Country");
				LOGGER.info(classname + methodName+ "----->>>>        " + "County is: " +country);
				//String middlename = usrInfo.getAttribute(AttributeName.MIDDLENAME.getName().toString()).toString();
				//String middlename = getParamaterValue(parameters, AttributeName.MIDDLENAME.getName().toString());
				String middlename = usrInfo.getAttribute(AttributeName.MIDDLENAME.getName().toString()).toString(); 
				LOGGER.info(classname + methodName+ "----->>>>        " + "Middle Name  is: " +middlename);

							
				String generatedEmail = generateEmail(preferredname,firstname,middlename,lastname,country,loginid);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Email Is: " +generatedEmail);
				
				HashMap modifyMap=new HashMap();
                modifyMap.put(UserManagerConstants.AttributeName.EMAIL.getId(),generatedEmail);
                mgr.modifyEntity(orchestration.getTarget().getType(), orchestration.getTarget().getEntityId(), modifyMap);
				}
                ///SEND MAILS 
            	LOGGER.info(classname + methodName+ "----->>>>        " + "Starting the  PasswordNotification while reset");
                String operation = orchestration.getOperation();
                Object uname = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_LOGIN.getId());
                //LOGGER.info(classname + methodName+ "----->>>>        " + "UserName "+uname);
	            HashMap<String, Serializable> interParameters = orchestration.getInterEventData(); //contains old and new values of user
	            LOGGER.info(classname + methodName+ "----->>>>        " + String.format("Inter Parameters: %s ", interParameters));
	            Identity userNewState = (User)interParameters.get("NEW_USER_STATE");
        		String userLogin = (String) userNewState.getAttribute("User Login"); //Get target user's login
                //LOGGER.info(classname + methodName+ "----->>>>        " + "UserName "+uname);
                Object  encryptedPasswordObj = userNewState.getAttribute(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId()); //Get target user's login
                LOGGER.info(classname + methodName+ "----->>>>        " + "Object Pwd Encrypted"+encryptedPasswordObj);
                String userPwd = (String) encryptedPasswordObj; 
                LOGGER.info(classname + methodName+ "----->>>>        " + "UserPwd, String  "+userPwd);
                //Object encryptedPasswordObj = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId());
                String encryptedPassword = "";
                if(encryptedPasswordObj instanceof ContextAwareString)
                    encryptedPassword = (String)((ContextAwareString)encryptedPasswordObj).getObjectValue();
                if(encryptedPasswordObj instanceof String)
                    encryptedPassword = (String)encryptedPasswordObj;
                String userID = null;
                try
                {
                    if(uname != null)
                    {
                        userID = uname.toString();
                    } else
                    {
                        String userKey = orchestration.getTarget().getEntityId();
                        userID = UserMgrUtil.getUserLoginFromId(userKey);
                    }
                }
                catch(Exception e)
                {
                    LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching user details");
                }
               /* if(oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.CREATE.toString().equalsIgnoreCase(operation)){
                    try
                    {
                        boolean isPwdAutoGen = false;
                        String passwdGen = (String)usrInfo.getAttribute(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD_GENERATED.getId());
                        if(passwdGen != null && passwdGen.equalsIgnoreCase(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeValues.PWD_AUTO_GENERATED_TRUE.getId()))
                            isPwdAutoGen = true;
                        if(isPwdAutoGen)
                        {
                            char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
                            if(password != null)
                                sendNotificationToUsr(usrInfo, String.valueOf(password), "GeneratedPasswordNotification");
                        }
                    }
                    catch(CryptoException e)
                    {
                        PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Password decryption failed. ", e);
                    }
                }
                else*/
                //NEWEST CODE
                /*
                if(	oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.ENABLE.toString().equalsIgnoreCase(operation))
                {
                    try
                    {
                        //String notificationFlag = (String)ContextManager.getValue("SEND_PASSWORD_NOTIFICATION_FLAG");
                       // if(Boolean.valueOf(notificationFlag).booleanValue())
                        //{
                            char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
                            LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Reset Password details"+String.valueOf(password));
                            if(password != null){
                                sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
                            	sendNotificationToUsr(usrInfo, String.valueOf(password), "New Hire Password");
                            }
                                
                            
                        //}
                    }
                    catch(CryptoException e)
                    {
                    	LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Password decryption failed. ");
                    }
                }
                */
                LOGGER.info(classname + methodName+ "----->>>>        " + "Enable Op succeeded");
            	    
				}	 
			} catch (Exception e) {
				LOGGER.info(classname + methodName+ "----->>>>        " + e.getMessage());
				e.printStackTrace();
			}
			return new EventResult();
		}

		@SuppressWarnings("deprecation")
		public BulkEventResult execute(long processId, long eventId, BulkOrchestration bulkOrchestration) {
			String methodName="BulkExecute";

			try {
				UserManager um = null;
				LOGGER.info(classname + methodName+ "----->>>>        " + "BULKEXECUTE ");
				LOGGER.info(classname + methodName+ "----->>>>        " + "Bulk processId = " + processId + "; eventId = " + eventId + "; bulkOrchestration = " + bulkOrchestration);
				um = (UserManager)Platform.getService(UserManager.class);
				EntityManager mgr=Platform.getService(EntityManager.class);

				HashMap[] parametersArray = bulkOrchestration.getBulkParameters();
	            String[] entityIds =bulkOrchestration.getTarget().getAllEntityId();

				for (int i = 0; i < parametersArray.length; i++) {
					HashMap parameters = parametersArray[i];
					LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);
					//String userLogin = (String)parameters.get("User Login");
					String loginid = null; 
				    String loginid1 = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
					   if(loginid1  != null)
			            {
						   loginid = loginid1.toString();
			            } else
			            {
			                String userKey = entityIds[i];
			                loginid = UserMgrUtil.getUserLoginFromId(userKey);
			            }
					 
					   UserRepository userRepository = new DBUserRepository();
		                UserInfo usrInfo = userRepository.getUserAndManagerInfo(loginid);
						//String userType= getParamaterValue(parameters, "Role");
						String userType= usrInfo.getAttribute("Role").toString();
						 
					//String loginid = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
					//String firstname = getParamaterValue(parameters, AttributeName.FIRSTNAME.getName().toString());
					   //String middlename = getParamaterValue(parameters, AttributeName.MIDDLENAME.getName().toString());
					   //String lastname = getParamaterValue(parameters, AttributeName.LASTNAME.getName().toString());
					   //String preferredname= getParamaterValue(parameters, "usr_PreferredName");
					
						//String orgName = getParamaterValue(parameters, AttributeName.USER_ORGANIZATION.getName().toString());
					//String orgName = getParamaterValue(parameters, "OrganizationName");
					//String orgID = getParamaterValue(parameters, "act_key");
					   String firstname = usrInfo.getFirstName().toString();
						String middlename = usrInfo.getAttribute(AttributeName.MIDDLENAME.getName().toString()).toString(); 
						String lastname = usrInfo.getLastName().toString();
						String preferredname= usrInfo.getAttribute("usr_PreferredName").toString();
						String country= usrInfo.getAttribute("Country").toString();
						   
					/*
					SearchCriteria criteria = new SearchCriteria("User Login", loginid, SearchCriteria.Operator.EQUAL);
					Set retAttrs = new HashSet();
					retAttrs.add(UserManagerConstants.AttributeName.USER_ORGANIZATION.getId());
					List users = um.search(criteria, retAttrs, null);
					User user = (User) users.get(0);
					LOGGER.info(classname + methodName+ "----->>>>        " + "Organization Key -- " + user.getAttribute("act_key") );
					String orgID = user.getAttribute("act_key").toString();
					OrganizationManager orgManager=Platform.getService(OrganizationManager.class);
					Organization org=orgManager.getDetails(orgID, new HashSet(), false);
					String orgName=org.getAttribute("Organization Name").toString();
					LOGGER.info(classname + methodName+ "----->>>>        " + "Organization = " +orgName);
					*/
					/*
					LOGGER.info(classname + methodName+ "----->>>>        " + "Before Org Name  " );
	                HashMap userDetails = new HashMap(); 
	                userDetails =um.getDetails(loginid, null, true).getAttributes();           
	                //logger.debug("userDetails ::  " + userDetails);
	                LOGGER.info(classname + methodName+ "----->>>>        " + "userDetails " + userDetails);
	    			String orgName = userDetails.get("Organization Name").toString();
	    			LOGGER.info(classname + methodName+ "----->>>>        " + "OrgName" +orgName );
	    			*/
					
	    			LOGGER.info(classname + methodName+ "----->>>>        " + "User Manager service retrieved sucessfully.  " );
	                HashMap userDetails = new HashMap(); 
	                userDetails =um.getDetails(loginid, null, true).getAttributes();           
	                //logger.debug("userDetails ::  " + userDetails);
	                //LOGGER.info(classname + methodName+ "----->>>>        " + "userDetails " + userDetails);
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Before Retrieving Org Key.  " );
	                String orgName = userDetails.get(UserManagerConstants.AttributeName.USER_ORGANIZATION.getId()).toString();
	    			LOGGER.info(classname + methodName+ "----->>>>        " + "OrgName" +orgName );
	    			OrganizationManager orgManager=Platform.getService(OrganizationManager.class);
					Organization org=orgManager.getDetails(orgName, new HashSet(), false);
					String orgName1=org.getAttribute("Organization Name").toString();
					LOGGER.info(classname + methodName+ "----->>>>        " + "OrgName1" +orgName1 );
					
					//
					//
					LOGGER.info(classname + methodName+ "----->>>>        " + "User Type" +userType);
					String userTypes=null;
					tcLookupOperationsIntf lookupOps = Platform
	              		     .getService(tcLookupOperationsIntf.class);
	               	   tcResultSet values = lookupOps.getLookupValues("Lookup.Excluded.Email.UserTypes");
	               	   for (int j = 0; j < values.getRowCount(); j++) 
	               	   {
		               	    values.goToRow(j);
		               	    System.out.print(values.getStringValue("Lookup Definition.Lookup Code Information.Decode"));
		               	    LOGGER.info(classname + methodName+ "----->>>>        " + ","+ values.getStringValue("Lookup Definition.Lookup Code Information.Code Key"));
		               	    userTypes=values.getStringValue("Lookup Definition.Lookup Code Information.Decode");
		               	}
	               	LOGGER.info(classname + methodName+ "----->>>>        " + "USERTYPES" +userTypes);
					int iGenerateEmail=1;
	               	String[] tokens = userTypes.split(",");
					for (int k = 0; k < tokens.length; k++)
					{

						LOGGER.info(classname + methodName+ "----->>>>        " + "Token"+tokens[k]);
						if(userType.equalsIgnoreCase(tokens[k]))
						{
							LOGGER.info(classname + methodName+ "----->>>>        " + "TOKEN Type Matches");
							iGenerateEmail=0;
						}
							
					}
					
		
					//
	    			
					if(orgName1.equalsIgnoreCase("Internal User") && iGenerateEmail==1)
					{
					LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstname);
					LOGGER.info(classname + methodName+ "----->>>>        " + "Middle Name  is: " +middlename);
					LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name  is: " +lastname);
					LOGGER.info(classname + methodName+ "----->>>>        " + "Title/Preferred Name  is: " +preferredname);
					LOGGER.info(classname + methodName+ "----->>>>        " + "County is: " +country);
								
					String generatedEmail = generateEmail(preferredname,firstname,middlename,lastname,country,loginid);
					LOGGER.info(classname + methodName+ "----->>>>        " + "Email Is: " +generatedEmail);
			
						//New code
					   //um.getDetails(loginid, null, true).setEmail(generatedEmail);
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Before Email Modification");
					  
					   HashMap<String, Object> userAttributeValueMap = new HashMap<String, Object>();
			          // userAttributeValueMap.put("act_key", new Long(1));
			           userAttributeValueMap.put("Email",generatedEmail);
			           /*
			           Set<String> resAttrs = new HashSet<String>();
			           LOGGER.info(classname + methodName+ "----->>>>        " + "Get User Details");
					   User retrievedUser = um.getDetails(loginid, null, true);
					   */
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Before Modify User ");
					   User user = new User(entityIds[i],userAttributeValueMap); 
			           LOGGER.info(classname + methodName+ "----->>>>        " + "After Modify User ");
					   um.modify(user);
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Modified User ");
					  	
					
				   /*
					HashMap modifyMap=new HashMap();
	                modifyMap.put(UserManagerConstants.AttributeName.EMAIL.getId(),generatedEmail);
	                
	                try {
	   
	                	//mgr.modifyEntity(bulkOrchestration.getTarget().getType(), bulkOrchestration.getTarget().getEntityId(), modifyMap);
	                	mgr.modifyEntity(bulkOrchestration.getTarget().getType(),entityIds[i] , modifyMap);
	                	
	                }catch (Exception e){
	    				LOGGER.info(classname + methodName+ "----->>>>        " + "Error occured in updating user: " +loginid );
					   // eventLogger.severe("[" + methodName +"] Error occured in updating user" + e.getMessage());
	                    }
	               	*/
					LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is assigned successfully to user as : ");
					}
					///SEND MAILS 
	            	LOGGER.info(classname + methodName+ "----->>>>        " + "Starting the  PasswordNotification while reset");
	                String operation = bulkOrchestration.getOperation();
	                Object uname = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_LOGIN.getId());
	                LOGGER.info(classname + methodName+ "----->>>>        " + "UserName "+uname);
	                Object encryptedPasswordObj = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId());
	                String encryptedPassword = "";
	                if(encryptedPasswordObj instanceof ContextAwareString)
	                    encryptedPassword = (String)((ContextAwareString)encryptedPasswordObj).getObjectValue();
	                if(encryptedPasswordObj instanceof String)
	                    encryptedPassword = (String)encryptedPasswordObj;
	                String userID = null;
	                try
	                {
	                    if(uname != null)
	                    {
	                        userID = uname.toString();
	                    } else
	                    {
	                        String userKey = entityIds[i];
	                        userID = UserMgrUtil.getUserLoginFromId(userKey);
	                    }
	                }
	                catch(Exception e)
	                {
	                    LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching user details");
	                }
	               /* if(oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.CREATE.toString().equalsIgnoreCase(operation)){
	                    try
	                    {
	                        boolean isPwdAutoGen = false;
	                        String passwdGen = (String)usrInfo.getAttribute(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD_GENERATED.getId());
	                        if(passwdGen != null && passwdGen.equalsIgnoreCase(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeValues.PWD_AUTO_GENERATED_TRUE.getId()))
	                            isPwdAutoGen = true;
	                        if(isPwdAutoGen)
	                        {
	                            char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
	                            if(password != null)
	                                sendNotificationToUsr(usrInfo, String.valueOf(password), "GeneratedPasswordNotification");
	                        }
	                    }
	                    catch(CryptoException e)
	                    {
	                        e"Sending Password Notification is not successful.Password decryption failed. ", e);
	                    }
	                }
	                else*/
	                //NEWEST CODE
	                /*
	                if(	oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.ENABLE.toString().equalsIgnoreCase(operation))
	                {
	                    try
	                    {
	                        //String notificationFlag = (String)ContextManager.getValue("SEND_PASSWORD_NOTIFICATION_FLAG");
	                       // if(Boolean.valueOf(notificationFlag).booleanValue())
	                        //{
	                            char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
	                            LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Reset Password details\n"+String.valueOf(password));
	                            if(password != null){
	                                sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
	                            	sendNotificationToUsr(usrInfo, String.valueOf(password), "New Hire Password");
	                            }
	                                
	                            
	                        //}
	                    }
	                    catch(CryptoException e)
	                    {
	                        LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Password decryption failed. ");
	                    }
	                }
					*/
	                
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Enable Op succeeded");
					  
				}	
			}
			catch (Exception e) {
				LOGGER.info(classname + methodName+ "----->>>>        " + e.getMessage());
			}
			return new BulkEventResult();
		}
		
		 private void sendNotificationToUsrWithoutPwd(UserInfo usrInfo, String templateName)
		    {
			 String methodName="sendNotificationToUsrWithoutPwd";
		        NotificationService notificationService = (NotificationService)Platform.getService(oracle.iam.notification.api.NotificationService.class);
		        NotificationEvent event = new NotificationEvent();
		        event.setTemplateName(templateName);
		        event.setSender(null);
		        String userEmailID = usrInfo.getUserEmailID();
		        String userID = usrInfo.getLoginID();
		        String managerEmailID = usrInfo.getManagerEmailID();
		        String managerID = usrInfo.getManagerLoginID();
		        //Date startDate = (Date) usrInfo.getAttribute("Hire Date");
		        String empHireDate = usrInfo.getAttribute("Hire Date").toString();
		        LOGGER.info(classname + methodName+ "----->>>>        " + "empHireDate "+empHireDate);
		        String managerFirstName=null;
		        String formattedDate = "";
		        boolean notifyToOther = false;
		        try
		        {
		        	DateFormat readFormat = new SimpleDateFormat( "EEE MMM dd hh:mm:ss zzz yyyy");
		            DateFormat writeFormat = new SimpleDateFormat( "MM/dd/yyyy");
		            Date date = null;
		            date = readFormat.parse(empHireDate);
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Date "+date);
		            if( date != null ) {
		            formattedDate = writeFormat.format(date);
		            }
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Formatted Date "+formattedDate);
		            //SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy");
		        	//strDate =df.format(empHireDate);
		        	UserRepository userRepository = new DBUserRepository();
		            UserInfo usrmanagerInfo = userRepository.getUserAndManagerInfo(managerID);
		            managerFirstName= usrmanagerInfo.getFirstName();
		            SystemConfigurationService sysConfig = (SystemConfigurationService)Platform.getService(oracle.iam.conf.api.SystemConfigurationService.class);
		            SystemProperty property = sysConfig.getSystemProperty("XL.NotifyPasswordGenerationToOther");
		            notifyToOther = property.getPtyValue() != null ? property.getPtyValue().equalsIgnoreCase("true") : false;
		        }
		        catch(Exception ex)
		        {
		            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Error fetching system property for notification. ", ex);
		        }
		        /*if(notifyToOther && managerID != null && !managerID.equals(""))
		            event.setUserIds(new String[] {
		                userID, managerID
		            });
		        else
		            event.setUserIds(new String[] {
		                userID
		            });*/
		        event.setUserIds(new String[] {
		                 managerID
		            });
		        
		        HashMap notificationData = new HashMap();
		        notificationData.put("firstName", managerFirstName);
		        notificationData.put("lastName",formattedDate);
		        notificationData.put("userLoginId", usrInfo.getLoginID());
		        notificationData.put("user", usrInfo.getUserKey());
		        notificationData.put("userManagerLoginId", usrInfo.getManagerLoginID());
		        notificationData.put("password", usrInfo.getFirstName()+" "+usrInfo.getLastName());
		        notificationData.put("userEmail", userEmailID);
		        notificationData.put("userManagerEmail", managerEmailID);
		        if(Utils.isMTFriendly())
		        {
		            notificationData.put("Non MT User Login", usrInfo.getNonMTUserLoginID());
		            notificationData.put(Constants.TENANT_NAME, usrInfo.getTenantName());
		        }
		        event.setParams(notificationData);
		        try
		        {
		            notificationService.notify(event);
		        }
		        catch(Exception e)
		        {
		        	e.getMessage();
		           LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Event Exception occured.");
		        }

		    }

		
		 private void sendNotificationToUsr(UserInfo usrInfo, String password, String templateName)
		    {
			 	String methodName="sendNotificationToUsr";
		        NotificationService notificationService = (NotificationService)Platform.getService(oracle.iam.notification.api.NotificationService.class);
		        NotificationEvent event = new NotificationEvent();
		        event.setTemplateName(templateName);
		        event.setSender(null);
		        String userEmailID = usrInfo.getUserEmailID();
		        String userID = usrInfo.getLoginID();
		        String managerEmailID = usrInfo.getManagerEmailID();
		        String managerID = usrInfo.getManagerLoginID();
		        String managerFirstName=null;
		        boolean notifyToOther = false;
	            LOGGER.info(classname + methodName+ "----->>>>        " + "Send Notification Password"+password);

		        try
		        {
		        	UserRepository userRepository = new DBUserRepository();
		            UserInfo usrmanagerInfo = userRepository.getUserAndManagerInfo(managerID);
		            managerFirstName= usrmanagerInfo.getFirstName();
		            SystemConfigurationService sysConfig = (SystemConfigurationService)Platform.getService(oracle.iam.conf.api.SystemConfigurationService.class);
		            SystemProperty property = sysConfig.getSystemProperty("XL.NotifyPasswordGenerationToOther");
		            notifyToOther = property.getPtyValue() != null ? property.getPtyValue().equalsIgnoreCase("true") : false;
		        }
		        catch(Exception ex)
		        {
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching system property for notification. ");
		        }
		        event.setUserIds(new String[] {
		                 managerID
		            });
		        Date date= new Date();
		        HashMap notificationData = new HashMap();
		        notificationData.put("firstName", managerFirstName);
		        notificationData.put("lastName", usrInfo.getFirstName()+" "+usrInfo.getLastName());
		        notificationData.put("userLoginId", usrInfo.getLoginID());
		        notificationData.put("user", usrInfo.getUserKey());
		        notificationData.put("userManagerLoginId", usrInfo.getManagerLoginID());
		        notificationData.put("password", password);
		        notificationData.put("userEmail", userEmailID);
		        notificationData.put("userManagerEmail", managerEmailID);
		        if(Utils.isMTFriendly())
		        {
		            notificationData.put("Non MT User Login", usrInfo.getNonMTUserLoginID());
		            notificationData.put(Constants.TENANT_NAME, usrInfo.getTenantName());
		        }
		        event.setParams(notificationData);
		        try
		        {
		            notificationService.notify(event);
		        }
		        catch(Exception e)
		        {
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Event Exception occured.");
		        }
		    		  
		    }
		 
		 /*
		    private void sendNotificationToUsrWithoutPwd(UserInfo usrInfo, String templateName)
		    {
		        NotificationService notificationService = (NotificationService)Platform.getService(oracle.iam.notification.api.NotificationService.class);
		        NotificationEvent event = new NotificationEvent();
		        event.setTemplateName(templateName);
		        event.setSender(null);
		        String userEmailID = usrInfo.getUserEmailID();
		        String userID = usrInfo.getLoginID();
		        String managerEmailID = usrInfo.getManagerEmailID();
		        String managerID = usrInfo.getManagerLoginID();
		        Date startDate = (Date) usrInfo.getAttribute("Start Date");
		        LOGGER.info(classname + methodName+ "----->>>>        " + "startDate "+startDate);
		        String strDate=null;
		        String managerFirstName=null;
		        boolean notifyToOther = false;
		        try
		        {
		        	SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy");
		        	strDate =df.format(startDate);
		        	UserRepository userRepository = new DBUserRepository();
		            UserInfo usrmanagerInfo = userRepository.getUserAndManagerInfo(managerID);
		            managerFirstName= usrmanagerInfo.getFirstName();
		            SystemConfigurationService sysConfig = (SystemConfigurationService)Platform.getService(oracle.iam.conf.api.SystemConfigurationService.class);
		            SystemProperty property = sysConfig.getSystemProperty("XL.NotifyPasswordGenerationToOther");
		            notifyToOther = property.getPtyValue() != null ? property.getPtyValue().equalsIgnoreCase("true") : false;
		        }
		        catch(Exception ex)
		        {
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching system property for notification. ");
		        }
		        event.setUserIds(new String[] {
		                 managerID
		            });
		        
		        HashMap notificationData = new HashMap();
		        notificationData.put("firstName", managerFirstName);
		        notificationData.put("lastName", strDate);
		        notificationData.put("userLoginId", usrInfo.getLoginID());
		        notificationData.put("user", usrInfo.getUserKey());
		        notificationData.put("userManagerLoginId", usrInfo.getManagerLoginID());
		        notificationData.put("password", usrInfo.getFirstName()+" "+usrInfo.getLastName());
		        notificationData.put("userEmail", userEmailID);
		        notificationData.put("userManagerEmail", managerEmailID);
		        if(Utils.isMTFriendly())
		        {
		            notificationData.put("Non MT User Login", usrInfo.getNonMTUserLoginID());
		            notificationData.put(Constants.TENANT_NAME, usrInfo.getTenantName());
		        }
		        event.setParams(notificationData);
		        try
		        {
		            notificationService.notify(event);
		        }
		        catch(Exception e)
		        {
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Event Exception occured.");
		        }
		        
		    }

	*/
		
			 private String generateEmail(String preferredName, String firstName, String middleName, String lastName,String countryName,String loginid)
		 {
				 String methodName="generateEmail"; 
			    String email=null;
				LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GenerateMail ");
				
				String modifiedPreferredName =null;
				String modifiedfirstName = null;
				String modifiedmiddleName = null;
				String modifiedlastName = null;
				if(!isNullOrEmpty(preferredName))
				{
					modifiedPreferredName = preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase();
				}
				if(!isNullOrEmpty(firstName ))
				{
					modifiedfirstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
					
				}
				if(!isNullOrEmpty(middleName))
					
				{
					modifiedmiddleName = middleName.substring(0, 1).toUpperCase() + middleName.substring(1).toLowerCase();
				}
				if(!isNullOrEmpty(lastName))
					
				{
					modifiedlastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
				}
				
				String delims = "[ ]+";
				String InitialsOfFirstName=null;
				String[] tokens = firstName.split(delims);
				for (int i = 0; i < tokens.length; i++)
				{
					LOGGER.info(classname + methodName+ "----->>>>        " + tokens[i]);
					if(i == 0)
					{
						InitialsOfFirstName=tokens[i].substring(0,1).toUpperCase();
					}
					else
					{
						InitialsOfFirstName=InitialsOfFirstName + tokens[i].substring(0,1).toUpperCase();
					}
						
				}
				LOGGER.info(classname + methodName+ "----->>>>        " + "InitialsOfFirstName"+InitialsOfFirstName);
				
			 if(!isNullOrEmpty(preferredName))
			  {
					
					
				 email = modifiedPreferredName+"."+modifiedlastName+"@ul.com";
				 LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is: " +email);
				   if( getUserLogin(email,loginid) != 0)
			       {	
					   LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 1");

			    	   return email; 
			       }
			       else{
			    	   	  if(!isNullOrEmpty(middleName))
			    	   		{
			    	   		LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GenerateMail Empty Middle name");
							
			    	   		email = modifiedPreferredName+"."+modifiedmiddleName+"."+modifiedlastName+"@ul.com";
			    	   		if( getUserLogin(email,loginid) != 0)
			    	   		{
			    	   			return email;  
			  			    }
			    	   	   }	
			    	   	  else
			    	   	  {
				    	   		LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 3");

			    	   		email = modifiedPreferredName+"."+InitialsOfFirstName+"."+modifiedlastName+"@ul.com";
			    	   		if( getUserLogin(email,loginid) != 0)
			    	   		{
			    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 3.5");

			    	   			return email;  
			  			    }
			    	   		  
			    	   	  }
			       		}   
			   }
			 
			 email = modifiedfirstName+"."+modifiedlastName+"@ul.com";
			 LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is: " +email);
			   if( getUserLogin(email,loginid) != 0)
		       {	
				   LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 4");

		    	   return email;  
		       }
		       else{
		    	   	  if(!isNullOrEmpty(middleName))
		    	   		{

		    	   		email = modifiedfirstName+"."+modifiedmiddleName+"."+modifiedlastName+"@ul.com";
		    	   		if( getUserLogin(email,loginid) != 0)
		    	   		{
		    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 5");

		    	   			return email;  
		  			         			
		    	   		}
		       		}   
		          }
			   if(!isNullOrEmpty(preferredName))
				  {
						
						
					 email = modifiedPreferredName+".X."+modifiedlastName+"@ul.com";
					 LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is: " +email);
					 LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 6");

					   if( getUserLogin(email,loginid) != 0)
				       {	
						   LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 7");

				    	   return email; 
				       }
				       else{
				    	   		email = modifiedPreferredName+".X1."+modifiedlastName+"@ul.com";
				    	   		if( getUserLogin(email,loginid) != 0)
				    	   		{
				    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 8");

				    	   			return email;  
				  			         			
				    	   		}
				    	   		else{
				    	   			email = modifiedPreferredName+".X2."+modifiedlastName+"@ul.com";
					    	   		if( getUserLogin(email,loginid) != 0)
					    	   		{
					    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 9");

					    	   			return email;  
					  			         			
					    	   		}	
				    	   		}
			    	   		}   
				   }
				 
				 email = modifiedfirstName+".X."+modifiedlastName+"@ul.com";
				 LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is: " +email);
				   if( getUserLogin(email,loginid) != 0)
			       {	
			    	   return email;  
			       }
			       else{
			    	 	 
			    	   		email = modifiedfirstName+".X1."+modifiedlastName+"@ul.com";
			    	   		if( getUserLogin(email,loginid) != 0)
			    	   		{
			    	   			return email;  
			  			         			
			    	   		}
			    	   		else{
			    	   			email = modifiedfirstName+".X2."+modifiedlastName+"@ul.com";
				    	   		if( getUserLogin(email,loginid) != 0)
				    	   		{
				    	   			return email;  
				  			         			
				    	   		}
				    	   		
				    	   		}
			    	   		 for (int i=3;i<50;i++)
			    	   		 {
			    	   			email = modifiedfirstName+".X"+i+"."+modifiedlastName+"@ul.com";
				    	   		if( getUserLogin(email,loginid) != 0)
				    	   		{
				    	   			return email;  
				  			         			
				    	   		}
			    	   		 }
			       		  }   
			   		    
				   
			
			 LOGGER.info(classname + methodName+ "----->>>>        " + "Returning Email: " +email);
			 LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Email");

		        return email ;

		 }
		 
		 private boolean isCountryAsia(String countryName) {
			 String methodName="isCountryAsia";
		 boolean isUserRetired = false;
		  try {
		   HashSet retiredContainers = readLookup("Lookup.UL.Countries");
		   Iterator itr = retiredContainers.iterator();
		   while(itr.hasNext()) {
		    String containername = itr.next().toString();
		    if(containername.contains(countryName)) {
		     isUserRetired = true;
		    }
		   } 
		  } catch (Exception e) {
		   LOGGER.info(classname + methodName+ "----->>>>        " + 
		     "Error checking User Container "
		     + e.getMessage());
		  }
		  return isUserRetired;
 
		}
		 
		 
			public boolean getUserDN(String email,String loginid )
			{
				String methodName="getUserDN";
	            Hashtable env = getLdapEnv();
	            String samAccountName=null;
	            boolean isExists = false;
	            int totalResults = 0;

				 LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN");
				 Hashtable ADparams =  getITResParameterDetails("Active Directory");
			        
	            try {
	                   LdapContext ctx = new InitialLdapContext(env,null);
	                   SearchControls searchCtls = new SearchControls();
	                   searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	                   String searchFilter = "(proxyAddresses="+email+")";
	                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN start");
	                   String returnedAtts[]={"sAMAccountName"};
	                   searchCtls.setReturningAttributes(returnedAtts);
	                   //NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
	                   NamingEnumeration answer = ctx.search(ADparams.get("Container").toString(), searchFilter, searchCtls);
	                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN check Email"+ADparams.get("Container").toString());
	                   //NEW CODE
	                 //NEW
	                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN Inside check Proxy");
						//Loop through the search results
		                  while (answer.hasMoreElements()) 
		                  {
		                        SearchResult sr = (SearchResult)answer.next();
		                        Attributes attrs = sr.getAttributes();
		                        LOGGER.info(classname + methodName+ "----->>>>        " + "After Search Proxy"+attrs);
		                        if (attrs != null) 
		                        {
		                               for (NamingEnumeration ae = attrs.getAll();
		                            		   ae.hasMore();) 
		                               		{
		                                      Attribute attr = (Attribute)ae.next();
		                                      LOGGER.info(classname + methodName+ "----->>>>        " + "The search Attribute: " + attr.getID());
		                                      for (NamingEnumeration e = attr.getAll();e.hasMore();totalResults++) 
		                                      {
		                                             samAccountName=(String) e.next();
		                                      }
		                                      
		                                     
		                                      LOGGER.info(classname + methodName+ "----->>>>        " + " The value of samAccountName " +  samAccountName );
		                                      ctx.close();
		                               }
		                        }
		                        LOGGER.info(classname + methodName+ "----->>>>        " +samAccountName + "LoginID      " +  loginid);
			  	                 
		        				if(!loginid.equalsIgnoreCase(samAccountName))
		                		{
		        			        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin Duplicate Email Found 1.5");
		        			    	return true;
		                		}
		                  }
		                 //New Code
	                   //
	                   //isExists = answer.hasMoreElements();
	                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN Inside check Email");
	  	               String searchFilter1 = "(mail=" + email + ")";
	                    SearchControls sc1 = new SearchControls();
	                    String returnedAtts1[]={"sAMAccountName"};
	                    sc1.setReturningAttributes(returnedAtts1);
		                sc1.setSearchScope(SearchControls.SUBTREE_SCOPE);
						NamingEnumeration<javax.naming.directory.SearchResult> results1;
						results1 = ctx.search(ADparams.get("Container").toString(), searchFilter1, sc1);
						//NEW
						//Loop through the search results
		                  while (results1.hasMoreElements()) 
		                  {
		                        SearchResult sr = (SearchResult)results1.next();
		                        Attributes attrs = sr.getAttributes();
		                        LOGGER.info(classname + methodName+ "----->>>>        " + "After Search"+attrs);
		                        if (attrs != null) 
		                        {
		                               for (NamingEnumeration ae = attrs.getAll();
		                            		   ae.hasMore();) 
		                               		{
		                                      Attribute attr = (Attribute)ae.next();
		                                      LOGGER.info(classname + methodName+ "----->>>>        " + "The search Attribute: " + attr.getID());
		                                      for (NamingEnumeration e = attr.getAll();e.hasMore();totalResults++) 
		                                      {
		                                             samAccountName=(String) e.next();
		                                      }
		                                      
		                                     
		                                      LOGGER.info(classname + methodName+ "----->>>>        " + " The value of samAccountName " +  samAccountName );
		                                      ctx.close();
		                               }
		                        }
		                        LOGGER.info(classname + methodName+ "----->>>>        " +samAccountName + "LoginID      " +  loginid);
			  	                 
		        				if(!loginid.equalsIgnoreCase(samAccountName))
		                		{
		        			        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin Duplicate Email Found 1.5");
		        			    	return true;
		                		}



		                  }
		                 //New Code
						//isExists = results1.hasMoreElements();
	                   
	                   //Loop through the search results
	                  
	       			
	            } catch (NamingException e) {
	                   // TODO Auto-generated catch block
	                   e.printStackTrace();
	            }

	            LOGGER.info(classname + methodName+ "----->>>>        " + "Return isExists");
                
   				return isExists;

	            
	     }

			/*
			private Hashtable getLdapEnv(String adminid,String pwd,String adserver){
			       Hashtable env = new Hashtable();
			        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
			        //set security credentials, note using simple cleartext authentication
			        env.put(Context.SECURITY_AUTHENTICATION,"simple");
			        env.put(Context.SECURITY_PRINCIPAL,adminid);
			        env.put(Context.SECURITY_CREDENTIALS,pwd);
			                 
			        //connect to my domain controller
			        env.put(Context.PROVIDER_URL,"ldap://uspocx882t.global.ul.org:389");
			        return env;
			    }
			    */
			//private Hashtable getLdapEnv(String adminid,String pwd,String adserver){
			private Hashtable getLdapEnv()
			{
				String methodName="getLdapEnv";
				    Hashtable env = new Hashtable();
			        Hashtable ADparams =  getITResParameterDetails("Active Directory");
			        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
			        //set security credentials, note using simple cleartext authentication
			        env.put(Context.SECURITY_AUTHENTICATION,"simple");
			        
			        //env.put(Context.SECURITY_PRINCIPAL,adminid);
			        env.put(Context.SECURITY_PRINCIPAL,ADparams.get("DirectoryAdminName"));
			        LOGGER.info(classname + methodName+ "----->>>>        " + "Admin Name"+ADparams.get("DirectoryAdminName")+"Pwd"+ADparams.get("DirectoryAdminPassword")+"LDAP Host="+"ldap://"+ADparams.get("LDAPHostName")+":389");
			        
			        //env.put(Context.SECURITY_CREDENTIALS,pwd);
			        env.put(Context.SECURITY_CREDENTIALS,ADparams.get("DirectoryAdminPassword"));
			                 
			        //connect to my domain controller
			        //env.put(Context.PROVIDER_URL,"ldap://uspocx882t.global.ul.org:389");
			        env.put(Context.PROVIDER_URL,"ldap://"+ADparams.get("LDAPHostName")+":389");
			        return env;
			    }
			

		     public Hashtable getITResParameterDetails(String sITRes) 
		     {
		    	 
		    	  String methodName = "getITResParameterDetails";
		    	  LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ " ENTERING ");
		    	  tcITResourceInstanceOperationsIntf itResourceIntf = Platform.getService(tcITResourceInstanceOperationsIntf.class);
		 		  long lSvrKey = -1;
		    	  Hashtable hITResParam = new Hashtable();

		    	  //LOGGER.info(classname + methodName+ "----->>>>        " + className + methodName + "Enters for extracting details on IT Resource Name: " + sITRes);
		    	  try {
		    	   HashMap hashMap = new HashMap();
		    	   hashMap.put(IT_RESOURCE_NAME, sITRes);

		    	   tcResultSet tcresultset = itResourceIntf.findITResourceInstances(hashMap);
		    	   //LOGGER.info(classname + methodName+ "----->>>>        " + className + "Size " + tcresultset.getRowCount());
		    	   /*if (tcresultset != null) {
		    	    LOGGER.info(classname + methodName+ "----->>>>        " + className + methodName + "Total number of rows found for IT resource " + sITRes + " is: " + tcresultset.getRowCount());
		    	   } */

		    	   tcresultset.goToRow(0);
		    	   lSvrKey = tcresultset.getLongValue(IT_RESOURCE_KEY);
		    	   hITResParam.put(IT_RESOURCE_KEY, "" + lSvrKey);

		    	   tcresultset = itResourceIntf.getITResourceInstanceParameters(lSvrKey);

		    	   if (tcresultset != null) {
		    	    //LOGGER.info(classname + methodName+ "----->>>>        " + className+ methodName+ "Total number of rows found for IT resource parameter against " + sITRes + " is: " + tcresultset.getRowCount());
		    	   } else {
		    	    LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName + "No rows found for IT resource parameter against "+ sITRes);
		    	   }
		    	   for (int i = 0; i < tcresultset.getRowCount(); i++) {
		    	    tcresultset.goToRow(i);
		    	    String paramName = tcresultset.getStringValue(IT_RESOURCE_PARAM_NAME);
		    	    String paramValue = tcresultset .getStringValue(IT_RESOURCE_PARAM_VALUE);
		    	    hITResParam.put(paramName.trim(), paramValue.trim());
		    	   }
		    	  } catch ( Exception e ) {
		    		  LOGGER.info(classname + methodName+ "----->>>>        " + classname+ methodName + "tcAPIException occured while retriving data from IT-Resource "+ sITRes);
		    	  }
		    	  LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ " EXITING ");
		    	  return hITResParam;
		    	 }

		    		     

	 
		// Method to read Lookup containing default OIM Roles
		 private HashSet readLookup(String lookup) 
		 {
			 String methodName="readLookup";
		  //logger.debug(CLASS_NAME + METHOD_NAME + "Inside ");
		  HashSet records = new HashSet();
		  try {
		   String lookupDecode = lookup;
		  
		   // Read Lookup to Find FilteredRoles
		   tcLookupOperationsIntf lookupOps = Platform
		     .getService(tcLookupOperationsIntf.class);
		   tcResultSet lookupResultSet = lookupOps
		     .getLookupValues(lookupDecode);
		   for (int i = 0; i < lookupResultSet.getRowCount(); i++) {
		    lookupResultSet.goToRow(i);
		    String decode = lookupResultSet.getStringValue(
		      LOOKUP_COLUMN_DECODE).trim();
		    records.add(decode);
		   }
		  } catch (Exception e) {
		  // logger.error(CLASS_NAME + METHOD_NAME + "Error Reading Lookup"  + e.getMessage());
		   e.printStackTrace();
		  }
		  return records;
		 }
		 /**
		     * Retrieves User login based on the first name using OIM 11g 
		     * UserManager service API. 
		     */
		     private int getUserLogin(String Email,String loginid) 
		     {
		    	 String methodName="getUserLogin"; 
		        Vector mvUsers = new Vector();
		        UserManager userService = (UserManager)Platform.getService(UserManager.class); 
		        Set<String> retAttrs = new HashSet<String>();
		        LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "GetUserLogin");
		        boolean isExists=false;
		        // Attributes that should be returned as part of the search. 
		        // Retrieve "User Login" attribute of the User.
		        // Note: Additional attributes can be specified in a 
		        // similar fashion.
		        retAttrs.add(AttributeName.USER_LOGIN.getId());
		 
		        // Construct a search criteria. This search criteria states 
		        // "Find User(s) whose 'First Name' equals 'Email'".  
		        SearchCriteria criteria;
		        criteria = new SearchCriteria(AttributeName.EMAIL.getId() , Email, SearchCriteria.Operator.EQUAL);
		        try {
		            // Use 'search' method of UserManager API to retrieve 
		            // records that match the search criteria. The return 
		            // object is of type User. 
		        	LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 1");

		            List<User> users = userService.search(criteria, retAttrs, null);
		            if(users.size()!=0)
	                {
		            	for(User user : users)
		            	{
	    			        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin Login ID " + loginid + "UserLogin" + user.getLogin());
		            		//System.out.println(user.getLogin());
		            		if(!loginid.equalsIgnoreCase(user.getLogin()))
		            		{
		    			        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin Duplicate Email Found 1.5");
		    			    	return 0;
		            		}
		            	}
		            	
				        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 2");

	               		//return 0;
	               	}
			        
				        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 3");

			        //	isExists=getUserDN("SVC.UL.IAMDEV","!dentity&AM","uspocx882t.global.ul.com",Email,"DC=global,DC=ul,DC=org");
				        isExists=getUserDN(Email,loginid);

			        	if (isExists == false)
			        	{
					        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 4");

			        		return 1;
			        	}
			        	else
			        	{
					        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 5");

			        		return 0;
			        	}
			        
		            
		        } catch (AccessDeniedException ade) {
		            // handle exception
		        } catch (UserSearchException use) {
		            // handle exception
		        }
		      return 0;  
	} 
		     
		
			private String getParamaterValue(HashMap<String, Serializable> parameters, String key)
		{
			String value = (parameters.get(key) instanceof ContextAware) ? (String)((ContextAware)parameters.get(key)).getObjectValue() : (String)parameters.get(key);
			return value;
		}

		private Identity[] getEventDataValue(HashMap<String, Serializable> parameters, String key)
		{
			Identity[] value = (parameters.get(key) instanceof ContextAware) ? (Identity[])((ContextAware)parameters.get(key)).getObjectValue() : (Identity[])parameters.get(key);
			return value;
		}
		public void initialize(HashMap<String, String> ht) {
			//LOGGER.info(classname + methodName+ "----->>>>        " + "Password Generation Event Handler initialized");
		}

		public boolean isNullOrEmpty(String s) {
			return s == null || s.trim().length() == 0;
		}
	
	
}
