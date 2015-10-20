package com.ul.eventhandlers;


import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import oracle.iam.identity.exception.AccessDeniedException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.orgmgmt.api.OrganizationManager;
import oracle.iam.identity.orgmgmt.vo.Organization;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.impl.UserMgrUtil;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.identity.vo.Identity;
import oracle.iam.platform.Platform;
import oracle.iam.platform.context.ContextAware;
import oracle.iam.platform.entitymgr.EntityManager;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.kernel.spi.PostProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;

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
import oracle.iam.identity.usermgmt.impl.UserMgrUtil;


public class EmployeeConversionTasks implements PostProcessHandler{
		
		String classname="EmployeeConversionTasks.";
	    private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
		//protected static LOGGER LOGGER = LOGGER.getLOGGER("BHSF.CUSTOM");
		private static final String LOOKUP_COLUMN_DECODE = "Lookup Definition.Lookup Code Information.Decode";
		public boolean cancel(long l, long l1, AbstractGenericOrchestration abstractGenericOrchestration)
		{
			return false;
		}
		public void compensate(long l, long l1, AbstractGenericOrchestration abstractGenericOrchestration) {
		}

		
		public EventResult execute(long processId, long eventId, Orchestration orchestration) 
	    {
			String methodName="execute";
	        LOGGER.info(classname + methodName+ "----->>>>        " + "Start EmployeeConversion execute method: ");
	        try
	        {
	            LOGGER.info(classname + methodName+ "----->>>>        " + "----->>>>        " + String.format("Start execute() with ProcessId: %s and EventId %s",processId, eventId));
	            HashMap<String, Serializable> newParameters = orchestration.getParameters(); //contains only the new values
	            HashMap<String, Serializable> interParameters = orchestration.getInterEventData(); //contains old and new values of user
	            LOGGER.info(classname + methodName+ "----->>>>        " + String.format("Inter Parameters: %s ", interParameters));
	            LOGGER.info(classname + methodName+ "----->>>>        " + String.format("New Parameters: %s ", newParameters));
	            LOGGER.info(classname + methodName+ "----->>>>        " + String.format("Empconversion "));
        		Identity currentUserState = (User) interParameters.get("CURRENT_USER"); //Get target user's current (old) info state
        	    Identity userNewState = (User)interParameters.get("NEW_USER_STATE");

	            //Check if the user's password is being modified
	            //If it is, it should be in the newParameters object
            	LOGGER.info(classname + methodName+ "----->>>>        " + "Old State" +currentUserState.getAttribute("usr_PersonType").toString());
	            if(newParameters.get("usr_PersonType") != null && orchestration.getOperation().equalsIgnoreCase("MODIFY") && !currentUserState.getAttribute("usr_PersonType").toString().equals("Employee"))
	            {
	        		LOGGER.info(classname + methodName+ "----->>>>        " + "Employee Conversion Tasks Before adding into Lookup N");
	        		String userLogin = userNewState.getAttribute("User Login").toString(); //Get target user's login
	               // String userLogin = getParamaterValue(interParameters, AttributeName.USER_LOGIN.getId());
	                //String userLogin = getParamaterValue(interParameters, "User Login");
	                //String userLogin = (String) interParameters.get("User Login");
	        		//String userLogin = "Test1";
	                LOGGER.info(classname + methodName+ "----->>>>        " + "before Hire Date User Login"+userLogin);
	        		String empHireDate = userNewState.getAttribute("Hire Date").toString(); //Get target user's login
	        		//String empHireDate = getParamaterValue(interParameters, AttributeName.USER_LOGIN.getId());
	        			//getParamaterValue(interParameters,AttributeName.HIRE_DATE.getId()) .toString() ; 
	                LOGGER.info(classname + methodName+ "----->>>>        " + "empHireDate"+empHireDate);
	                //String dateStr = "Thu Jan 19 2012 01:00 PM";
	                DateFormat readFormat = new SimpleDateFormat( "EEE MMM dd hh:mm:ss zzz yyyy");
	                DateFormat writeFormat = new SimpleDateFormat( "MM/dd/yyyy");
	                Date date = null;
	                date = readFormat.parse(empHireDate);
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Date "+date);
	                String formattedDate = "";
	                if( date != null ) {
	                formattedDate = writeFormat.format(date);
	                }
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Formatted Date "+formattedDate);
	                //String empHireDate = "Test";
	        		//CUSTOM CODE 
	                //lookup definition. 
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Test2");
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Formatted Date"+formattedDate);
	                tcLookupOperationsIntf lookup= Platform.getService(tcLookupOperationsIntf.class);
	                LOGGER.info(classname + methodName+ "----->>>>        " + "Test3");
	               
	                lookup.addLookupValue("Lookup.Employee.Conversion",userLogin,formattedDate,"","");
	                LOGGER.info(classname + methodName+ "----->>>>        " + "After adding into Lookup");
	        		          
	             }
	            LOGGER.info(classname + methodName+ "----->>>>        " + String.format("Test "));
		           
	        } 
	 
	       catch (Exception ex) 
	       {
	    	   ex.printStackTrace();
	    	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside Exception"+ex.getMessage());
			}
	 
	        return new EventResult();
	    }
	 
	
		@SuppressWarnings("deprecation")
		public BulkEventResult execute(long processId, long eventId, BulkOrchestration bulkOrchestration) {
			String methodName="Bulk Execute";

			try {
				UserManager um = null;
				LOGGER.info(classname + methodName+ "----->>>>        " + "BULKEXECUTE ");
				LOGGER.info(classname + methodName+ "----->>>>        " + "Bulk processId = " + processId + "; eventId = " + eventId + "; bulkOrchestration = " + bulkOrchestration);
				um = (UserManager)Platform.getService(UserManager.class);
				EntityManager mgr=Platform.getService(EntityManager.class);
				
				//[] parametersArray = bulkOrchestration.getBulkParameters();
				HashMap<String, Serializable>[]parametersArray = bulkOrchestration.getBulkParameters();
	            String[] entityIds =bulkOrchestration.getTarget().getAllEntityId();
	            //NEW CODE
	            Identity[] userNewState = (Identity[])getNewUserStates(bulkOrchestration);
	            Identity[] currentState = (Identity[])getOldUserStates(bulkOrchestration);
	            //
				for (int i = 0; i < parametersArray.length; i++) {
					//HashMap newParameters = parametersArray[i];
					HashMap <String, Serializable>newParameters = parametersArray[i];
					LOGGER.info(classname + methodName+ "----->>>>        " + "parameters = " + newParameters);
					//Check if the user's password is being modified
		            //If it is, it should be in the newParameters object
					HashMap<String, Serializable> interParameters = bulkOrchestration.getInterEventData(); //contains old and new values of user
					LOGGER.info(classname + methodName+ "----->>>>        " + "Inter parameters = " + interParameters);
					//NEW CODE
					
					//executeEvent(parametersArray[i], bulkOrchestration.getTarget().getType(), entityIds[i], bulkOrchestration.getOperation(), currentState[i], userNewState[i]);
					//if(newParameters.get("usr_PersonType") != null && bulkOrchestration.getOperation().equalsIgnoreCase("MODIFY"))
	            	LOGGER.info(classname + methodName+ "----->>>>        " + "Old State" +currentState[i].getAttribute("usr_PersonType").toString());
		            if(bulkOrchestration.getOperation().equalsIgnoreCase("MODIFY") &&   isAttributeModified(currentState[i], userNewState[i], "usr_PersonType") && !currentState[i].getAttribute("usr_PersonType").toString().equals("Employee"))
		            {
		            	LOGGER.info(classname + methodName+ "----->>>>        " + "Employee Conversion Tasks Before adding into Lookup N");
		        		//Identity currentUserState = (User) interParameters.get("CURRENT_USER"); //Get target user's current (old) info state
		        	    //Identity userNewState = (User)interParameters.get("NEW_USER_STATE");
		        		//String userLogin = userNewState.getAttribute("User Login").toString(); //Get target user's login
		                //String userLogin = getParamaterValue(interParameters, AttributeName.USER_LOGIN.getId());
		                //String userLogin = getParamaterValue(interParameters, "User Login");
		                //String userLogin = (String) interParameters.get("User Login");
		        		//String userLogin = "Test1";
		        		//String empHireDate = userNewState.getAttribute("Hire Date").toString(); //Get target user's login
		        		//String empHireDate = getParamaterValue(interParameters, AttributeName.USER_LOGIN.getId());
		        		//getParamaterValue(interParameters,AttributeName.HIRE_DATE.getId()) .toString() ;
		            	
		            	//NEW
		              
		            	 String empHireDate = null;
		            	 Set<String> bulkKeySet = newParameters.keySet();
		            	 LOGGER.info(classname + methodName+ "----->>>>        " + "bulkKeySet ->" + bulkKeySet);
		            	 for (String key : bulkKeySet) {
		            		 LOGGER.info(classname + methodName+ "----->>>>        " + "key ->" + key);
		            		 Serializable serializable = newParameters.get(key);
		            		 if (key.equalsIgnoreCase("Hire Date")) {
		            			 empHireDate  = serializable.toString();
		            			 LOGGER.info(classname + methodName+ "----->>>>        " + "Hire Date ->" + empHireDate );
		            		 }


		            	 }

		            	//
		            	
		            	
		            	String userLogin = getParamaterValue(newParameters, AttributeName.USER_LOGIN.getId());
						LOGGER.info(classname + methodName+ "----->>>>        " + "before Hire Date User Login"+userLogin);
		                //empHireDate = getParamaterValue(newParameters, "Hire Date").toString(); 
		       		    LOGGER.info(classname + methodName+ "----->>>>        " + "empHireDate"+empHireDate);
		       		    
		                //String dateStr = "Thu Jan 19 2012 01:00 PM";
		                DateFormat readFormat = new SimpleDateFormat( "EEE MMM dd hh:mm:ss zzz yyyy");
		                DateFormat writeFormat = new SimpleDateFormat( "MM/dd/yyyy");
		                Date date = null;
		                date = readFormat.parse(empHireDate);
		                LOGGER.info(classname + methodName+ "----->>>>        " + "Date "+date);
		                String formattedDate = "";
		                if( date != null ) {
		                formattedDate = writeFormat.format(date);
		                }
		                LOGGER.info(classname + methodName+ "----->>>>        " + "Formatted Date "+formattedDate);
		                //String empHireDate = "Test";
		        		//CUSTOM CODE 
		                //lookup definition. 
		                LOGGER.info(classname + methodName+ "----->>>>        " + "Formatted Date"+formattedDate);
		                tcLookupOperationsIntf lookup= Platform.getService(tcLookupOperationsIntf.class);
		                LOGGER.info(classname + methodName+ "----->>>>        " + "Test3");
		               
		                lookup.addLookupValue("Lookup.Employee.Conversion",userLogin,formattedDate,"","");
		                LOGGER.info(classname + methodName+ "----->>>>        " + "After adding into Lookup");
		        		          
		               
		            	
		            	/*
		            	LOGGER.info(classname + methodName+ "----->>>>        " + "Before Adding into Lookup");
			        	
		            	String userLogin = getParamaterValue(newParameters, AttributeName.USER_LOGIN.getId());
						String empHireDate = getParamaterValue(newParameters, "Hire Date"); 
		 
					    LOGGER.info(classname + methodName+ "----->>>>        " + "empHireDate"+empHireDate);
			        	//CUSTOM CODE 
		                //lookup definition. 
		                tcLookupOperationsIntf lookup= Platform.getService(tcLookupOperationsIntf.class);
		                lookup.addLookupValue("Lookup.Employee.Conversion",userLogin,empHireDate,"","");

		                LOGGER.info(classname + methodName+ "----->>>>        " + "After Adding into Lookup");
			        	  */         
		             }
		 
					
					//String orgName = getParamaterValue(parameters, AttributeName.USER_ORGANIZATION.getName().toString());
				
				}
					
			}
			catch (Exception e) {
				LOGGER.info(classname + methodName+ "----->>>>        " + e.getMessage());
			}
			return new BulkEventResult();
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
		
		private Object getNewUserStates(BulkOrchestration orchestration)
		  {
			
		    Object newUserStates = null;
		    HashMap interEventData = orchestration.getInterEventData();
		    if (interEventData != null)
		      newUserStates = interEventData.get("NEW_USER_STATE");
		    return newUserStates;
		  }

		  private Object getOldUserStates(BulkOrchestration orchestration)
		  {
		    Object oldUserStates = null;
		    HashMap interEventData = orchestration.getInterEventData();
		    if (interEventData != null)
		      oldUserStates = interEventData.get("CURRENT_USER");
		    return oldUserStates;
		  }
		  
		  		  private HashMap getChangedAttributes(Identity currentState, Identity userNewState, String fieldName)
		  {
		  			String methodName="getChangedAttributes";
		    LOGGER.info(classname + methodName+ "----->>>>        " + "inside Changed Attributes");
		    HashMap newOldValue = new HashMap();
		    try
		    {
		      newOldValue.put("new", userNewState.getAttribute(fieldName));
		      newOldValue.put("old", currentState.getAttribute(fieldName));
		    }
		    catch (Exception e)
		    {
		    	LOGGER.info(classname + methodName+ "----->>>>        " + "changedAttributes error-->" + e.getMessage());
		    }

		    return newOldValue;
		  }
		  private boolean isAttributeModified(Identity currentState, Identity userNewState, String fieldName)
		  {
		    boolean attributeModified = false;
		    String methodName="isAttributeModified";
		    LOGGER.info(classname + methodName+ "----->>>>        " + "[UL.EmployeeConversion]:[isAttributeModified]:Inside AttributeModified Method");
		    try
		    {
		      HashMap newOldValueMap = getChangedAttributes(currentState, userNewState, fieldName);

		      Object newObj = newOldValueMap.get("new");
		      Object oldObj = newOldValueMap.get("old");

		      LOGGER.info(classname + methodName+ "----->>>>        " + "[UL.EmployeeConversion]:[isAttributeModified]: " + fieldName + ": New Value-->" + newObj);
		      LOGGER.info(classname + methodName+ "----->>>>        " + "[UL.EmployeeConversion]:[isAttributeModified]: " + fieldName + ": Old Value-->" + oldObj);

		      if ((newObj == null) && (oldObj == null))
		      {
		    	  LOGGER.info(classname + methodName+ "----->>>>        " + "[UL.EmployeeConversion]:[isAttributeModified]: " + fieldName + ": Attributes Not Modified ");

		        return false;


		      }
		      if ((newObj == null) || (oldObj == null))
		      {
		    	  LOGGER.info(classname + methodName+ "----->>>>        " + "[UL.EmployeeConversion]:[isAttributeModified]: " + fieldName + ": Attributes Modified ");
		        return true;
		      }
		      if (!newObj.equals(oldObj))
		      {
		    	  LOGGER.info(classname + methodName+ "----->>>>        " + "[UL.EmployeeConversion]:[isAttributeModified]: " + fieldName + ": Attributes  Modified ");
		        attributeModified = true;
		      }

		    }
		    catch (Exception e)
		    {
		    	LOGGER.severe("[UL.EmployeeConversion]:[isAttributeModified]: " + fieldName + ":attributeModified error-->" + e.getMessage());
		      e.printStackTrace();
		      return attributeModified;
		    }

		    return attributeModified;
		  }
	
}
