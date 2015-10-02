package com.ul.eventhandlers;

import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.conf.api.SystemConfigurationService;
import oracle.iam.conf.vo.SystemProperty;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.impl.UserMgrUtil;
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
import oracle.iam.platform.kernel.spi.PostProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.platform.utils.crypto.CryptoException;
import oracle.iam.platform.utils.crypto.CryptoUtil;

public class passwordgenerator implements PostProcessHandler{
	
		String classname="passwordgenerator.";
        private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	
		//protected static Logger logger = Logger.getLogger("BHSF.CUSTOM");
		
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
				UserManager um = null;
				LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "EXECUTE ");
				/*
				final Logger logger = Logger.getLogger("TESTLOGGER");
			    final FileHandler fh = new FileHandler("/oracle/logs/test.log");
			    logger.addHandler(fh);
			    */
				LOGGER.info(classname + methodName+ "----->>>>        " + "execute: processId = " + processId + "; eventId = " + eventID + "; orchestration = " + orchestration);
				HashMap parameters = orchestration.getParameters();
				//LOGGER.info(classname + methodName+ "----->>>>        " + "VRUSHANK");
		        LOGGER.severe("error message");
		        LOGGER.fine("fine message"); //won't show because to high level of logging
				um = (UserManager)Platform.getService(UserManager.class);
				LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);
				//String userLogin = (String)parameters.get("User Login");
				String loginid = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
				String firstname = getParamaterValue(parameters, AttributeName.FIRSTNAME.getName().toString());
				String lastname = getParamaterValue(parameters, AttributeName.LASTNAME.getName().toString());
				String emptype = getParamaterValue(parameters, AttributeName.DEPARTMENT_NUMBER.getId());
				String password=null;
				/*if(emptype.equals("ACQUISITION"))
				{
				LOGGER.info(classname + methodName+ "----->>>>        " + "Login ID  is: " +loginid );
				LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstname );
				LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name is: " +lastname );
				firstname = firstname.substring(0,2);
				lastname = lastname.substring(0,2);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Login ID  is: " +loginid );
				LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstname );
				LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name is: " +lastname );
				String modifiedfirstname= firstname.substring(0, 1).toUpperCase() + firstname.substring(1).toLowerCase();
				String modifiedlastname= lastname.substring(0, 1).toLowerCase() + lastname.substring(1).toLowerCase();
				//String trimmedloginid=loginid.substring(1,4);
				String trimmedloginid = loginid.substring(Math.max(0, loginid.length() - 4)).toLowerCase();
				LOGGER.info(classname + methodName+ "----->>>>        " + "Modified First Name: " +modifiedfirstname );
				LOGGER.info(classname + methodName+ "----->>>>        " + "Trimmed Login ID: " +trimmedloginid );
				//LOGGER.info(classname + methodName+ "----->>>>        " + "Date of Birth is:: " + dob);
				//String password = generatePasswd(dob);
				password = modifiedfirstname+modifiedlastname+trimmedloginid;
				} 
				*/
				//else
				//{
					password= generatePassword(9);
				//}
				um.changePassword(AttributeName.USER_LOGIN.getId(), loginid, password.toCharArray());
				//String password = generatePasswd(dob);
				//um.changePassword(AttributeName.EMPLOYEE_NUMBER.getId(), empNumber, password.toCharArray());
				LOGGER.info(classname + methodName+ "----->>>>        " + "Password is assigned successfully to user as : " + password);
				/*
				//NEW CODE
		    	LOGGER.info(classname + methodName+ "----->>>>        " + "Starting the  PasswordNotification while reset");
		        String operation = orchestration.getOperation();
		        Object uname = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_LOGIN.getId());
		        LOGGER.info(classname + methodName+ "----->>>>        " + "UserName "+uname);
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
		        	e.getMessage();
		            LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching user details");
		        }
		        UserRepository userRepository = new DBUserRepository();
		        UserInfo usrInfo = userRepository.getUserAndManagerInfo(userID);
		        if(oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.RESET_PASSWORD.toString().equalsIgnoreCase(operation)
		        		||
		        		oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.ENABLE.toString().equalsIgnoreCase(operation)
		        		||
		        		oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.CREATE.toString().equalsIgnoreCase(operation))
		        {
		            try
		            {
		                //String notificationFlag = (String)ContextManager.getValue("SEND_PASSWORD_NOTIFICATION_FLAG");
		               // if(Boolean.valueOf(notificationFlag).booleanValue())
		                //{
		                    LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Reset Password details\n");
		                    if(password != null){
		                        sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
		                    	sendNotificationToUsr(usrInfo, password, "New Hire Password");
		                    }
		                        
		                    
		                //}
		            }
		            catch(Exception e)
		            {
		                LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Password decryption failed. ");
		            }
		        }
				
				*/
				//
			} catch (Exception e) {
				LOGGER.info(classname + methodName+ "----->>>>        " + e.getMessage());
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
				String operation = bulkOrchestration.getOperation();
				HashMap[] parametersArray = bulkOrchestration.getBulkParameters();
				for (int i = 0; i < parametersArray.length; i++) {
					HashMap parameters = parametersArray[i];
					LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);				//LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);
					um = (UserManager)Platform.getService(UserManager.class);
					LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + parameters);
					//String userLogin = (String)parameters.get("User Login");
					String loginid = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
					String firstname = getParamaterValue(parameters, AttributeName.FIRSTNAME.getName().toString());
					String lastname = getParamaterValue(parameters, AttributeName.LASTNAME.getName().toString());
					String emptype = getParamaterValue(parameters, AttributeName.DEPARTMENT_NUMBER.getId());
					String password=null;
					/*
					if(emptype.equals("ACQUISITION"))
					{
					LOGGER.info(classname + methodName+ "----->>>>        " + "Login ID  is: " +loginid );
					LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstname );
					LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name is: " +lastname );
					firstname = firstname.substring(0,2);
					lastname = lastname.substring(0,2);
					LOGGER.info(classname + methodName+ "----->>>>        " + "Login ID  is: " +loginid );
					LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstname );
					LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name is: " +lastname );
					String modifiedfirstname= firstname.substring(0, 1).toUpperCase() + firstname.substring(1).toLowerCase();
					String modifiedlastname= lastname.substring(0, 1).toLowerCase() + lastname.substring(1).toLowerCase();
					//String trimmedloginid=loginid.substring(1,4);
					String trimmedloginid = loginid.substring(Math.max(0, loginid.length() - 4)).toLowerCase();
					LOGGER.info(classname + methodName+ "----->>>>        " + "Modified First Name: " +modifiedfirstname );
					LOGGER.info(classname + methodName+ "----->>>>        " + "Trimmed Login ID: " +trimmedloginid );
					//LOGGER.info(classname + methodName+ "----->>>>        " + "Date of Birth is:: " + dob);
					//String password = generatePasswd(dob);
					password = modifiedfirstname+modifiedlastname+trimmedloginid;
					} 
					*/
					//else
					//{
						password= generatePassword(9);
					//}
				
					um.changePassword(AttributeName.USER_LOGIN.getId(), loginid, password.toCharArray());
					//String password = generatePasswd(dob);
					//um.changePassword(AttributeName.EMPLOYEE_NUMBER.getId(), empNumber, password.toCharArray());
					LOGGER.info(classname + methodName+ "----->>>>        " + "Password is assigned successfully to user as : " + password);
					/*
					//NEW CODE
					Object uname = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_LOGIN.getId());
			        LOGGER.info(classname + methodName+ "----->>>>        " + "UserName "+uname);
			        //NEW
			      			        //
			         String userID = null;
			        try
			        {
			            if(uname != null)
			            {
			                userID = uname.toString();
			            } else
			            {
			                String userKey = bulkOrchestration.getTarget().getEntityId();
			                userID = UserMgrUtil.getUserLoginFromId(userKey);
			            }
			        }
			        catch(Exception e)
			        {
			            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Error fetching user details", e);
			        }
			       UserRepository userRepository = new DBUserRepository();
			       UserInfo usrInfo = userRepository.getUserAndManagerInfo(userID);
			        //Object encryptedPasswordObj = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId());
			        //Object encryptedPasswordObj =  usrInfo.getAttribute(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId());
			   	    um = (UserManager)Platform.getService(UserManager.class);
				    HashMap userDetails = new HashMap(); 
				    try{
				    	userDetails =um.getDetails(userID, null, true).getAttributes();           
		                
				    }catch ( Exception e ) {
			    		  LOGGER.info(classname + methodName+ "----->>>>        " + "tcAPIException occured while retriving data from IT-Resource ");
			    	  }
				    
	                //logger.debug("userDetails ::  " + userDetails);
	                //LOGGER.info(classname + methodName+ "----->>>>        " + "userDetails " + userDetails);
					 LOGGER.info(classname + methodName+ "----->>>>        " + "String Password ->" + password);
		        
			        if(oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.RESET_PASSWORD.toString().equalsIgnoreCase(operation)
			        		||
			        		oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.ENABLE.toString().equalsIgnoreCase(operation)
			        		||
			        		oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.CREATE.toString().equalsIgnoreCase(operation))
			        {
			            try
			            {
			                //String notificationFlag = (String)ContextManager.getValue("SEND_PASSWORD_NOTIFICATION_FLAG");
			               // if(Boolean.valueOf(notificationFlag).booleanValue())
			                //{
			                    LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Reset Password details\n");
			                    if(password != null){
			                        sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
			                    	sendNotificationToUsr(usrInfo, String.valueOf(password), "New Hire Password");
			                    }
			                        
			                    
			                //}
			            }
			            catch(Exception e)
			            {
			                LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Password decryption failed.");
			            }
			        }    

				*/	
					//
				}
				
			}
			catch (Exception e) {
				LOGGER.info(classname + methodName+ "----->>>>        " + e.getMessage());
			}
			return new BulkEventResult();
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
	        try
	        {
	        	UserRepository userRepository = new DBUserRepository();
	            UserInfo usrmanagerInfo = userRepository.getUserAndManagerInfo(managerID);
	            managerFirstName= usrmanagerInfo.getFirstName();
	            SystemConfigurationService sysConfig = (SystemConfigurationService)Platform.getService(oracle.iam.conf.api.SystemConfigurationService.class);
	            SystemProperty property = sysConfig.getSystemProperty("XL.NotifyPasswordGenerationToOther");
	            notifyToOther = property.getPtyValue() != null ? property.getPtyValue().equalsIgnoreCase("true") : false;
	        }
	        catch(Exception e)
	        {
	            LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching system property for notification. ");
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
	        catch(Exception e)
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
		
		private  String generatePassword(int Length) 
		{
			String methodName="generatePassword";

		    final java.util.Random r = new java.util.Random();
		    final String DIGITS = "23456789";
		    final String LOCASE_CHARACTERS = "abcdefghjkmnpqrstuvwxyz";
		    final String UPCASE_CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ";
		    final String SYMBOLS = "@#$%=?";
		    final char[] upcaseArray = UPCASE_CHARACTERS.toCharArray();
		    final char[] locaseArray = LOCASE_CHARACTERS.toCharArray();
		    final char[] digitsArray = DIGITS.toCharArray();
		    final char[] symbolsArray = SYMBOLS.toCharArray();
		    final String ALL = DIGITS + LOCASE_CHARACTERS + UPCASE_CHARACTERS+SYMBOLS;
		    final char[] allArray = ALL.toCharArray();
		    
		    StringBuilder sb = new StringBuilder();
		 
			LOGGER.info(classname + methodName+ "----->>>>        " + "Inside Password Function" );
		    // get at least one lowercase letter
		    sb.append(locaseArray[r.nextInt(locaseArray.length)]);
		 
		    // get at least one digit
		    //sb.append(digitsArray[r.nextInt(digitsArray.length)]);
			
		    // get at least one uppercase letter
		    sb.append(upcaseArray[r.nextInt(upcaseArray.length)]);
		 
		    //get at least one digit
		    sb.append(digitsArray[r.nextInt(digitsArray.length)]);
		    
		    // get at least one symbol
		    sb.append(symbolsArray[r.nextInt(symbolsArray.length)]);  

		    for (int i = 0; i < 1; i++) 
		    {
		    	//New Code
		    	//System.out.println("INCOMPLETE PWD ARRAY "+sb.toString());
		    	char c = digitsArray[r.nextInt(digitsArray.length)];
		    	//System.out.println("New character"+c);
		    	//System.out.println("Character "+Character.toString(c));
			    if (sb.toString().contains(Character.toString(c)))
		    	{
			    	//System.out.println("String Contains character");
			    	i--;	
		    	}
		    	else
		    	{
			    	//System.out.println("String DOES NOT Contain character");
			        sb.append(c);
			 	   
		    	}
		    	//
		        //sb.append(allArray[r.nextInt(allArray.length)]);
		    	
		    }
		    
		    // fill in remaining with random letters
		    for (int i = 0; i < Length - 5; i++) 
		    {
		    	//New Code
		    	//System.out.println("INCOMPLETE PWD ARRAY "+sb.toString());
		    	char c = allArray[r.nextInt(allArray.length)];
		    	//System.out.println("New character"+c);
		    	//System.out.println("Character "+Character.toString(c));
			    if (sb.toString().contains(Character.toString(c)))
		    	{
			    	//System.out.println("String Contains character");
			    	Length++;	
		    	}
		    	else
		    	{
			    	//System.out.println("String DOES NOT Contain character");
			        sb.append(c);
			 	   
		    	}
		    	//
		        //sb.append(allArray[r.nextInt(allArray.length)]);
		    }
		    LOGGER.info(classname + methodName+ "----->>>>        " + "Pwd generated with ModifiedLogic"+sb.toString());
		    
		    return sb.toString();
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
