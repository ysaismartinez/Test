package com.ul.javatasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.io.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.thortech.xl.dataaccess.tcDataProvider;

import Thor.API.tcResultSet;
import Thor.API.tcUtilityFactory;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcFormInstanceOperationsIntf;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcUserOperationsIntf;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.conf.api.SystemConfigurationService;
import oracle.iam.conf.vo.SystemProperty;
import oracle.iam.identity.exception.AccessDeniedException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.identity.utils.Utils;
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
import oracle.iam.passwordmgmt.vo.Constants;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.Platform;
import oracle.iam.platform.context.ContextAwareString;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.utils.crypto.CryptoException;
import oracle.iam.platform.utils.crypto.CryptoUtil;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.AccountData;
import oracle.iam.provisioning.vo.FormInfo;


public class combinePrepop {
	
	public combinePrepop() {  
        super();  
	}
	
	String classname="combinedPrepop.";
	private static final String LOOKUP_COLUMN_DECODE = "Lookup Definition.Lookup Code Information.Decode";
	public static final String IT_RESOURCE_KEY = "IT Resources.Key";
    public static final String IT_RESOURCE_NAME = "IT Resources.Name";
    public static final String IT_RESOURCE_PARAM_NAME = "IT Resources Type Parameter.Name";
    public static final String IT_RESOURCE_PARAM_VALUE = "IT Resources Type Parameter Value.Value";


	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	
        // ADDED by Nitin Natekar to accomodate First Name logic
        // IF Preffered name is present for user then preference should be given  to preferred name 
		//Additional Comment for testing
	 public String generateFirstName(String preferredName, String firstName)
	   {
                   String methodName="generateFirstName";
                   
        	   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Preferred  Name : " + preferredName);
	           LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "First  Name : " + firstName);
               
                   if( preferredName.trim() == null || preferredName.trim() == " " || preferredName.isEmpty()){
		       LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Preffered Name is Null retruning firstName : " + firstName);
		       return firstName;
		   }else {
		       LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Preffered Name is not Null retruning preferredName : " + preferredName);
                       return preferredName;
		   }
	   }
        // ADDED by Nitin Natekar to accomodate EBS Effective Date To changes
        // EBS Responsibility Effective Date To is not populating in EBS system          
             public void effectiveDateToEBS(String userId){
                 
                 String methodName="effectiveDateToEBS"; 
                 String effectiveDateToEBS;
                 LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName);
                 Connection con = null;
                 Statement st = null;
                 ResultSet rs = null;
                 Date dt = new Date();
                 String eDate = null;
                 String query = null;
                 
                 try {
                          con = Platform.getOperationalDS().getConnection();
                          st = con.createStatement();
                          eDate =  formatDate(dt);
                          query = "UPDATE UD_EBS_USER SET UD_EBS_USER_EFFDATETO = " + "'" + eDate + "'" + "Where UD_EBS_USER_USRNAME = " + "'" + "UL"+ userId + "'";
                          LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName + ":" + query);
                          st.executeUpdate(query);                        
                          LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName + ":" + "Query executed successfully");
                     
                 }catch (Exception e) {
                        LOGGER.info("Excpetion" + e);
                 
                  } finally {
                      try {
                          if (rs != null) {
                              rs = null;
                          }
                          if (st != null) {
                              st = null;
                          }
                          if (con != null) {
                              con.close();
                          }
                  
                      } catch (Exception e) {
                          LOGGER.info("Excpetion" + e);
                      }   
                  } 
             }
             
        //ADDED by Nitin Natekar
             
            public String formatDate(Date date){
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = sdf.format(date);                                 
                return formattedDate;                                                                     
            }
    
	   public String generateCommonName(String preferredName, String firstName, String middleName, String lastName)
	   {
		   String methodName="generateCommonName";
		   String commonName = null;
		   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Common  Name : " +commonName);
		   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "First   Name : " +firstName);
		   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Middle  Name : " +middleName);
		   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Last  Name : " +lastName);
		   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Preferred  Name : " +preferredName);
		   LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ "Formatted Code: ");
		   
		   if(!isNullOrEmpty(preferredName))
		   {
			   
			   if(!isNullOrEmpty(middleName))
			   {
				   commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
				   + " "
				   + middleName.substring(0, 1).toUpperCase() 
				   + "."
				   + " "
				   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
				   ;
				   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: " +commonName);
///////////////////////////////////
				   if( !getUserCN(commonName))
				   {
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: Searche in AD returned NULL " +commonName);
					   return commonName; 
					   
				   }
				   else
				   {
					   for (int i=0;i<50;i++)
					   {
						   if (i==0){
							   commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
							   + " "
							   + middleName.substring(0, 1).toUpperCase() 
							   + "."
							   + "X"
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
							   
						   }else{
							   commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
							   + " "
							   + middleName.substring(0, 1).toUpperCase() 
							   + "."
							   + "X"
							   + i
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
						   }
						   /*commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
						    + " "
						    + middleName.substring(0, 1).toUpperCase() 
						    + "."
						    + "X"
						    + i
						    + "."
						    + " "
						    + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
						    ;
						    LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: After AD search New Name " +commonName);
						    if( !getUserCN(commonName))
						    {
						    LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: After AD search Final " +commonName);
						    return commonName;  
						    
						    } */
					   }
				   }
				   /////////////////////////
				   
			   }
			   else
			   {
				   commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
				   + " "
				   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
				   ;
				   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 2: " +commonName);
///////////////////////////////////
				   if( !getUserCN(commonName))
				   {
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: Searche in AD returned NULL " +commonName);
					   return commonName; 
					   
				   }
				   else
				   {
					   for (int i=0;i<50;i++)
					   {
						   if (i==0){
							   commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
							   + " "
							  // + middleName.substring(0, 1).toUpperCase() 
							  // + "."
							   + "X"
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
							   
						   }else{
							   commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
							   + " "
							   //+ middleName.substring(0, 1).toUpperCase() 
							   //+ "."
							   + "X"
							   + i
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
						   }
						   /*commonName =  preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
						    + " "
						    + middleName.substring(0, 1).toUpperCase() 
						    + "."
						    + "X"
						    + i
						    + "."
						    + " "
						    + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
						    ;
						    LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: After AD search New Name " +commonName);
						    if( !getUserCN(commonName))
						    {
						    LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: After AD search Final " +commonName);
						    return commonName;  
						    
						    } */
					   }
				   }
				   /////////////////////////
				   
				   
			   }
		   }
		   else
		   {
			   LOGGER.info(classname + methodName+ "----->>>>        " + "Preferred Name is Null  ");
			   
			   if(!isNullOrEmpty(middleName))
			   {
				   commonName =  firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
				   + " "
				   + middleName.substring(0, 1).toUpperCase() 
				   + "."
				   + " "
				   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
				   ;
				   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 3: " +commonName);
///////////////////////////////////
				   if( !getUserCN(commonName))
				   {
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: Searche in AD returned NULL " +commonName);
					   return commonName; 
					   
				   }
				   else
				   {
					   for (int i=0;i<50;i++)
					   {
						   if (i==0){
							   commonName =  firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
							   + " "
							   + middleName.substring(0, 1).toUpperCase() 
							   + "."
							   + "X"
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
							   
						   }else{
							   commonName =  firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
							   + " "
							   + middleName.substring(0, 1).toUpperCase() 
							   + "."
							   + "X"
							   + i
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
						   }
						   
					   }
				   }
				   /////////////////////////
				   
			   }
			   else
			   {
				   commonName =  firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
				   + " "
				   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
				   ;
				   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 4: " +commonName);
				   ///////////////////////////////////
				   if( !getUserCN(commonName))
				   {
					   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1: Searche in AD returned NULL " +commonName);
					   return commonName; 
					   
				   }
				   else
				   {
					   for (int i=0;i<50;i++)
					   {
						   if (i==0){
							   commonName =  firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
							   + " "
							   //+ middleName.substring(0, 1).toUpperCase() 
							   //+ "."
							   + "X"
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 if loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
							   
						   }else{
							   commonName =  firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
							   + " "
							  // + middleName.substring(0, 1).toUpperCase() 
							   //+ "."
							   + "X"
							   + i
							   + "."
							   + " "
							   + lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
							   ;
							   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search New Name " +commonName);
							   if( !getUserCN(commonName))
							   {
								   LOGGER.info(classname + methodName+ "----->>>>        " + "Common  Name Case 1 else loop: After AD search Final " +commonName);
								   return commonName;  
								   
							   }
						   }
						   
					   }
				   }
				   /////////////////////////
			   }
			   
		   }
		   
		   return commonName;
		   
	   }
	   
	   public String generatedisplayName(String preferredName, String firstName, String middleName, String lastName)
		 {
		   		String methodName="generatedisplayName";
			    String displayName=null;
			    LOGGER.info(classname + methodName+ "----->>>>        " + "Display Name : " +displayName);

			    if(!isNullOrEmpty(preferredName))
				{
					
			    	if(!isNullOrEmpty(middleName))
			    	{
				    	displayName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
				    				  + ","
				    				  + " "
				    				  + preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
				    				  + " "
				    				  + middleName.substring(0, 1).toUpperCase() 
				    				  + "."
				    				  ;
				    	LOGGER.info(classname + methodName+ "----->>>>        " + "Display Name Case 1: " +displayName);
							
			    	}
			    	else
			    	{
				    	displayName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
			    				  + ","
			    				  + " "
			    				  + preferredName.substring(0, 1).toUpperCase() + preferredName.substring(1).toLowerCase()
			    				  ;
				    	LOGGER.info(classname + methodName+ "----->>>>        " + "Display Name Case 2: " +displayName);
						
			    	}
				}
			    else
			    {
			    	if(!isNullOrEmpty(middleName))
			    	{
			    		displayName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
			    				  + ","
			    				  + " "
			    				  + firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
			    				  + " "
			    				  + middleName.substring(0, 1).toUpperCase() 
			    				  + "."
			    				  ;
			    		LOGGER.info(classname + methodName+ "----->>>>        " + "Display Name Case 3: " +displayName);
						
			    	}
			    	else
			    	{
			    		displayName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase()
			    				  + ","
			    				  + " "
			    				  + firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase()
			    				  ;

			    		LOGGER.info(classname + methodName+ "----->>>>        " + "Display Name Case 4: " +displayName);
						
			    	}
			    	
			    }
			    
			    
			 
			 
			   return displayName;
		 }
	   
       //public String getUserDN(String adminid,String pwd,String adserver,String samAccountName,String searchBase)
	   public String getUserDN(String samAccountName)
       {
		   String methodName="getUserDN";

    	   String distinguishedName=null;
           LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDn");
           
       	  LOGGER.info(classname + methodName+ "----->>>>        " + "SAMAccountName: " +samAccountName);
       	    Hashtable ADparams =  getITResParameterDetails("Active Directory");

           Hashtable env = new Hashtable();
	        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
	        //set security credentials, note using simple cleartext authentication
	        env.put(Context.SECURITY_AUTHENTICATION,"simple");
	        //env.put(Context.SECURITY_PRINCIPAL,adminid);
	        env.put(Context.SECURITY_PRINCIPAL,ADparams.get("DirectoryAdminName"));
	        //env.put(Context.SECURITY_PRINCIPAL,"SVC.UL.IAMPD@global.ul.com");
	        
	        //env.put(Context.SECURITY_CREDENTIALS,pwd);
	        env.put(Context.SECURITY_CREDENTIALS,ADparams.get("DirectoryAdminPassword"));
	        //env.put(Context.SECURITY_CREDENTIALS,"!dentity&AM");
	        
	        LOGGER.info(classname + methodName+ "----->>>>        " + "Newest Creds ");
	       	  
	        //connect to my domain controller
	        env.put(Context.PROVIDER_URL,"ldap://"+ADparams.get("LDAPHostName")+":389");
	        //env.put(Context.PROVIDER_URL,"ldap://uspocx882t.global.ul.org:389");
           LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetLdapEnv");

           try {
           		LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDn Try Block");

                  LdapContext ctx = new InitialLdapContext(env,null);
                  SearchControls searchCtls = new SearchControls();
                  searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                  String searchFilter = "(&(objectClass=user)(sAMAccountName="+samAccountName+"))";
                  LOGGER.info(classname + methodName+ "----->>>>        " + "After Search Filter");
                  int totalResults = 0;

                  String returnedAtts[]={"distinguishedName"};
                  searchCtls.setReturningAttributes(returnedAtts);

                  //NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                  NamingEnumeration answer = ctx.search(ADparams.get("Container").toString(), searchFilter, searchCtls);
                  
                  LOGGER.info(classname + methodName+ "----->>>>        " + "After Search"+answer.hasMoreElements());
                  //Loop through the search results
                  while (answer.hasMoreElements()) {
                        SearchResult sr = (SearchResult)answer.next();
                        
                        Attributes attrs = sr.getAttributes();
                        LOGGER.info(classname + methodName+ "----->>>>        " + "After Search"+attrs);
                        if (attrs != null) {
                               for (NamingEnumeration ae = attrs.getAll();ae.hasMore();) {
                                      Attribute attr = (Attribute)ae.next();
                                      LOGGER.info(classname + methodName+ "----->>>>        " + "The search Attribute: " + attr.getID());
                                      for (NamingEnumeration e = attr.getAll();e.hasMore();totalResults++) {
                                             distinguishedName=(String) e.next();
                                      }
                                     
                                     
                                      LOGGER.info(classname + methodName+ "----->>>>        " + " The value of distinguishedName " +  distinguishedName );
                                      ctx.close();
                               }
                        }


                  }
           } catch (Exception e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
           }

           LOGGER.info(classname + methodName+ "----->>>>        " + " Return DN " +  distinguishedName );
           return distinguishedName;

    }
	   
		public String searchADforUser(String userLogin)
		{
			String methodName="searchADforUser";
			Hashtable env = getLdapEnv();
            boolean isExists = false;
            String result="failure";
			LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute");
			Hashtable ADparams =  getITResParameterDetails("Active Directory");
		        
            try {
                   LdapContext ctx = new InitialLdapContext(env,null);
                   SearchControls searchCtls = new SearchControls();
                   searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                  // String searchFilter = "(proxyAddresses="+userLogin+")";
                   String searchFilter = "(&(objectClass=user)(sAMAccountName="+userLogin+"))";
                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute start"+userLogin);
                   String returnedAtts[]={"distinguishedName"};
                   searchCtls.setReturningAttributes(returnedAtts);
                   //NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                   NamingEnumeration answer = ctx.search(ADparams.get("Container").toString(), searchFilter, searchCtls);
                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute  check Email"+ADparams.get("Container").toString());
                   isExists = answer.hasMoreElements();
                   LOGGER.info(classname + methodName+ "----->>>>        " + "isExists"+isExists);
                   if(isExists == true)
                   {
                	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute  Inside check user Success");
                	   result="user_found";
                   }
                   else
                   {
                	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute  check user failure");
                	   result="user_not_found";
                   }
                   //Loop through the search results
                  
       			
            } catch (Exception e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                   result="failure";
            }

            LOGGER.info(classname + methodName+ "----->>>>        " + "Return isExists");
            
				return result;

     
		}

	   public String sendEmailtoManageronADSuccess(String userID,String encryptedPassword)
       {
			 String result="failure";
			 String methodName="sendEmailtoManageronADSuccess";
			 UserRepository userRepository = new DBUserRepository();
		     UserInfo usrInfo = userRepository.getUserAndManagerInfo(userID);
		     try
		     {
		     /*
		     UserManager um = null;
			 um = (UserManager)Platform.getService(UserManager.class);
		     HashMap userDetails = new HashMap(); 
             userDetails =um.getDetails(userID, null, true).getAttributes();           
             Object encryptedPasswordObj =  userDetails.get(UserManagerConstants.AttributeName.PASSWORD.getId());
		     //Object encryptedPasswordObj =  usrInfo.getAttribute(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId());
		     LOGGER.info(classname + methodName+ "----->>>>        " + "String Object ->" + encryptedPasswordObj);
		     String encryptedPassword = "";
		     
		     if(encryptedPasswordObj instanceof ContextAwareString)
		            encryptedPassword = (String)((ContextAwareString)encryptedPasswordObj).getObjectValue();
		        if(encryptedPasswordObj instanceof String)
		            encryptedPassword = (String)encryptedPasswordObj;
		     */
		    	 LOGGER.info(classname + methodName+ "----->>>>        " + "String User Login ->" + userID   );
			     LOGGER.info(classname + methodName+ "----->>>>        " + "String Password ->" + encryptedPassword   );
		        
		         
		      //String notificationFlag = (String)ContextManager.getValue("SEND_PASSWORD_NOTIFICATION_FLAG");
		               // if(Boolean.valueOf(notificationFlag).booleanValue())
		                //{
		                   // char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
		                    LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Reset Password details");
		                    if(encryptedPassword != null)
		                    {
		                        sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
		                    	sendNotificationToUsr(usrInfo,encryptedPassword , "New Hire Password");
		                    }
		                        
		                    
		      }
		            
		            catch(Exception e)
		            {
		            	e.printStackTrace();
		                LOGGER.info(classname + methodName+ "----->>>>        " + "Exception Occurred. ");
		            }
		        
			 result = "success";
			 return result;
		   
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
	                // managerID //Changed by Nitin Natekar as per Hina M. code changes as on 3rd Jun,2015
	                managerID,"OIMINTERNAL"  //Changed by Nitin Natekar as per Hina M. code changes as on 3rd Jun,2015           
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
	         	e.printStackTrace();
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
	        catch(Exception e)
	        {
	            LOGGER.info(classname + methodName+ "----->>>>        " + "Error fetching system property for notification. ");
	        }
	        event.setUserIds(new String[] {
	                // managerID // Changed by Nitin Natekar as per Hina M. code changes as on 3rd Jun,2015 
	                managerID,"OIMINTERNAL" //Changed by Nitin Natekar as per Hina M. code changes as on 3rd Jun,2015s
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
	         	e.printStackTrace();
	            LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Password Notification is not successful.Event Exception occured.");
	        }
	        
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
	    	    LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ paramName.trim() +paramValue.trim());
	    	   }
	    	  } 
	    	  catch ( Exception e ) 
	    	  {
	    		  LOGGER.info(classname + methodName+ "----->>>>        " + classname+ methodName + "tcAPIException occured while retriving data from IT-Resource "+ sITRes);
	    	  }
	    	  LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ hITResParam);
	    	  LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ " EXITING ");
	    	  return hITResParam;
	    	 }

	    		     

	     //public boolean getUserCN(String adminid,String pwd,String adserver,String email,String searchBase)
	     public boolean getUserCN(String email)
		{
	    	 String methodName = "getUserCN";
           Hashtable env = getLdapEnv();
           boolean isExists = false;
			 LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserCN");
			 Hashtable ADparams =  getITResParameterDetails("Active Directory");
			    

           try {
                  LdapContext ctx = new InitialLdapContext(env,null);
                  SearchControls searchCtls = new SearchControls();
                  searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                  String searchFilter = "(cn="+email+")";
                  LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserCN start");

                  String returnedAtts[]={"distinguishedName"};
                  searchCtls.setReturningAttributes(returnedAtts);
                  //NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                  NamingEnumeration answer = ctx.search(ADparams.get("Container").toString(), searchFilter, searchCtls);
                  
                  LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserCN check Email");
                  isExists = answer.hasMoreElements();
                  
                  if(!isExists)
                  {
               	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserCN Inside check Email");
 	                 
               	String searchFilter1 = "(mail=" + email + ")";
                   SearchControls sc1 = new SearchControls();
					sc1.setSearchScope(SearchControls.SUBTREE_SCOPE);
					NamingEnumeration<javax.naming.directory.SearchResult> results1;
					//results1 = ctx.search(searchBase, searchFilter1, sc1);
					results1 = ctx.search(ADparams.get("Container").toString(), searchFilter1, sc1);

					isExists = results1.hasMoreElements();
                  }
                  //Loop through the search results
                 
      			
           } catch (Exception e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
           }

           LOGGER.info(classname + methodName+ "----->>>>        " + "Return isExists");
           
				return isExists;

           
    }
       public Hashtable getLdapEnv()
       {
    	    String methodName = "getLdapEnv";
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

		public String getLookupValue(String lookupName, String lookupCodKey)
	            {
			String methodName = "getLookupValue";
			tcLookupOperationsIntf lookup =null;
			 lookup = Platform.getService(tcLookupOperationsIntf.class);
				
	     //String methodName="getLookupValue() ";
	     LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetLookupValue");
         String lookupDecodeKey = null;
	     
	     try {
	            

	            tcResultSet tcresultSet = lookup.getLookupValues(lookupName);
	            LOGGER.info(classname + methodName+ "----->>>>        " + "Successfully got the Lookpup values");
	              
	            for (int i = 0; i < tcresultSet.getRowCount(); i++) {
	                  /*
	                  * Looping the Lookup result (tcresultSet) to get the code and
	                  * decode value.
	                  */
	                  tcresultSet.goToRow(i);
	                  if (tcresultSet.getStringValue(
	                                "Lookup Definition.Lookup Code Information.Code Key")
	                                .equalsIgnoreCase(lookupCodKey)
	                                && tcresultSet.getRowCount() > 0) {

	                         lookupDecodeKey = tcresultSet
	                                       .getStringValue("Lookup Definition.Lookup Code Information.Decode");
	                         
	                         LOGGER.info(classname + methodName+ "----->>>>        " + "The value of Decodekey "+ lookupDecodeKey);
	                         
	                  }

	            }

	     }

	     catch (Exception e) {
	           e.getMessage();

	     }
	     
	     return lookupDecodeKey;
	}
//NEW 18th Jan
		public String getAdContainerFromLocation(String lookupName, String lookupCodKey,String emailtoList , String defaultOU , String loginID,String firstName, String lastName , String itResourceName, String templateName)

		{
	String methodName = "getAdContainerFromLocation";
	String orgUnit = null;
	tcLookupOperationsIntf lookup =null;
	 lookup = Platform.getService(tcLookupOperationsIntf.class);
		
 //String methodName="getLookupValue() ";
 LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetLookupValue");
 LOGGER.info(classname + methodName+ "----->>>>        " + "LoginID"+loginID);
 String lookupDecodeKey = null;
 
 try {
        

        tcResultSet tcresultSet = lookup.getLookupValues(lookupName);
        LOGGER.info(classname + methodName+ "----->>>>        " + "Successfully got the Lookpup values");
          
        for (int i = 0; i < tcresultSet.getRowCount(); i++) {
              /*
              * Looping the Lookup result (tcresultSet) to get the code and
              * decode value.
              */
              tcresultSet.goToRow(i);
              if (tcresultSet.getStringValue(
                            "Lookup Definition.Lookup Code Information.Code Key")
                            .equalsIgnoreCase(lookupCodKey)
                            && tcresultSet.getRowCount() > 0) {

                     lookupDecodeKey = tcresultSet
                                   .getStringValue("Lookup Definition.Lookup Code Information.Decode");
                     
                     LOGGER.info(classname + methodName+ "----->>>>        " + "The value of Decodekey "+ lookupDecodeKey);
                     
              }

        }

 }

 catch (Exception e) 
 {
       e.getMessage();
       return "failure";
 }
 
 String returnString = "failure";
 if(lookupDecodeKey != null && !"".equals(lookupDecodeKey))
 {
	 orgUnit =  lookupDecodeKey.substring(lookupDecodeKey.lastIndexOf("~") + 1);
	 returnString = searchADforOU(orgUnit);
 	LOGGER.info(classname + methodName+ "----->>>> String After Removing IT Resource Key       " +orgUnit );
 }
 
 
 if(lookupDecodeKey != null && returnString.equalsIgnoreCase("ou_in_AD"))
 {
		LOGGER.info(classname + methodName+ "----->>>> " +returnString  );
		LOGGER.info(classname + methodName+ "----->>>> Return Value      " +lookupDecodeKey);
		return lookupDecodeKey;
 }
 else
 {
	long  itResourceKey =  getlongITResParameterKey(itResourceName);
	//lookupDecodeKey = itResourceKey+"~"+defaultOU;
	String []toList=emailtoList.split(";");
	HashMap emailReplaceAttributes = new HashMap();
 	emailReplaceAttributes.put("loginID",loginID);
 	emailReplaceAttributes.put("firstName",firstName);
 	emailReplaceAttributes.put("lastName",lastName);
 	LOGGER.info(classname + methodName+ "----->>>>        " + "After Replacing Attributes");
	LOGGER.info(classname + methodName+ "----->>>>        " + "Sending out Notification");
	//Missing AD Container Template
	sendNotification(templateName,emailReplaceAttributes,toList,new String[]{},new String[]{});
	if(lookupDecodeKey != null)
	{
		lookupDecodeKey = itResourceKey+"~"+defaultOU;
		LOGGER.info(classname + methodName+ "----->>>> " +returnString  );
		LOGGER.info(classname + methodName+ "----->>>> Return Value      " +lookupDecodeKey);

		return lookupDecodeKey;
	}

		lookupDecodeKey = itResourceKey+"~"+defaultOU;
		returnString =  "ou_not_in_lookup";
		LOGGER.info(classname + methodName+ "----->>>> " +returnString );
		LOGGER.info(classname + methodName+ "----->>>> Return Value      " + lookupDecodeKey);
		return lookupDecodeKey;
	
 }


 }

		public String getprePopAdContainerFromLocation(String lookupName, String lookupCodKey, String defaultOU , String itResourceName)

		{
	String methodName = "getprePopAdContainerFromLocation";
	String orgUnit = null;
	tcLookupOperationsIntf lookup =null;
	 lookup = Platform.getService(tcLookupOperationsIntf.class);
		
 //String methodName="getLookupValue() ";
 LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetLookupValue");
 String lookupDecodeKey = null;
 
 try {
        

        tcResultSet tcresultSet = lookup.getLookupValues(lookupName);
        LOGGER.info(classname + methodName+ "----->>>>        " + "Successfully got the Lookpup values");
          
        for (int i = 0; i < tcresultSet.getRowCount(); i++) {
              /*
              * Looping the Lookup result (tcresultSet) to get the code and
              * decode value.
              */
              tcresultSet.goToRow(i);
              if (tcresultSet.getStringValue(
                            "Lookup Definition.Lookup Code Information.Code Key")
                            .equalsIgnoreCase(lookupCodKey)
                            && tcresultSet.getRowCount() > 0) {

                     lookupDecodeKey = tcresultSet
                                   .getStringValue("Lookup Definition.Lookup Code Information.Decode");
                     
                     LOGGER.info(classname + methodName+ "----->>>>        " + "The value of Decodekey "+ lookupDecodeKey);
                     
              }

        }

 }

 catch (Exception e) 
 {
       e.getMessage();
       return "failure";
 }
 
 String returnString = "failure";
 if(lookupDecodeKey != null && !"".equals(lookupDecodeKey))
 {
	 orgUnit =  lookupDecodeKey.substring(lookupDecodeKey.lastIndexOf("~") + 1);
	 returnString = searchADforOU(orgUnit);
 	LOGGER.info(classname + methodName+ "----->>>> String After Removing IT Resource Key       " +orgUnit );
 }
 
 
 if(lookupDecodeKey != null && returnString.equalsIgnoreCase("ou_in_AD"))
 {
		LOGGER.info(classname + methodName+ "----->>>> " +returnString  );
		LOGGER.info(classname + methodName+ "----->>>> Return Value      " +lookupDecodeKey);
		return lookupDecodeKey;
 }
 else
 {
	long  itResourceKey =  getlongITResParameterKey(itResourceName);
	//lookupDecodeKey = itResourceKey+"~"+defaultOU;
	//Missing AD Container Template
	//sendNotification(templateName,emailReplaceAttributes,toList,new String[]{},new String[]{});
	if(lookupDecodeKey != null)
	{
		lookupDecodeKey = itResourceKey+"~"+defaultOU;
		LOGGER.info(classname + methodName+ "----->>>> " +returnString  );
		LOGGER.info(classname + methodName+ "----->>>> Return Value      " +lookupDecodeKey);

		return lookupDecodeKey;
	}

		lookupDecodeKey = itResourceKey+"~"+defaultOU;
		returnString =  "ou_not_in_lookup";
		LOGGER.info(classname + methodName+ "----->>>> " +returnString );
		LOGGER.info(classname + methodName+ "----->>>> Return Value      " + lookupDecodeKey);
		return lookupDecodeKey;
	
 }


 }
		public String sendOUErrorNotification(String oimEmailDefinitionName,  String loginID,String firstName, String lastName , String emailtoList ,String defaultOU ,String userOU)
		{
			
			String methodName="sendOUErrorNotification";
			//System.out.println("Staring sendNotification()");
			String result="failure";
			String body=null;
			String substitutedBody=null;
	    	String subject=null;
	    	String substitutedSubject=null;
	    	tcEmailOperationsIntf emailIntf=null;
	    	tcLookupOperationsIntf lookup =null;
	    	final String SMTP_HOST_NAME= "smptp host name";
	    	final String SMTP_PORT= "smptp port";
	    	final String FROM_EMAIL_ADDRESS= "from email";
			lookup = Platform.getService(tcLookupOperationsIntf.class);
			emailIntf = Platform.getService(tcEmailOperationsIntf.class);
			String[] ccList = new String[]{};
			String[] bccList = new String[]{};
			
			String []toList=emailtoList.split(";");
			LOGGER.info(classname + methodName+ "----->>>>        " + "userOU"+userOU + "DefaultOU"+defaultOU);
			if(userOU.contains(defaultOU))
			{
				
			HashMap emailReplaceAttributes = new HashMap();
		 	emailReplaceAttributes.put("loginID",loginID);
		 	emailReplaceAttributes.put("firstName",firstName);
		 	emailReplaceAttributes.put("lastName",lastName);
		 	LOGGER.info(classname + methodName+ "----->>>>        " + "After Replacing Attributes");
			LOGGER.info(classname + methodName+ "----->>>>        " + "Sending out Notification");

	    	tcResultSet rs= null;
			boolean debug = false;
			HashMap<String,String> emailProps = new HashMap<String, String>();
			String fromEmailToUse = null;
			HashMap hm = new HashMap();
	        hm.put("emd_name", oimEmailDefinitionName);
			
			try {
				emailProps=readLookupValues(lookup, "Lookup.Email.Properties");
				rs = emailIntf.findEmailDefinition(hm);
				if (rs.getRowCount()!=0){
					rs.goToRow(0);
					body=rs.getStringValue("Email Definition.Body");
					subject=rs.getStringValue("Email Definition.Subject");
				 }
				
				if (emailReplaceAttributes.size() !=0){
					substitutedBody = embedInReplceableAttribute(body, emailReplaceAttributes);
					substitutedSubject = embedInReplceableAttribute(subject, emailReplaceAttributes);
				}else{
					substitutedBody = body;
					substitutedSubject = subject;
				}
				
				//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	    		String smtp_host =(String) emailProps.get(SMTP_HOST_NAME);
	    		String smtp_port = (String) emailProps.get(SMTP_PORT);
	    		
	    		Properties props = new Properties();
	    		props.put("mail.smtp.host", smtp_host);
	    		props.put("mail.smtp.auth", "false");
	    		props.put("mail.debug", "true");
	    		props.put("mail.smtp.port", smtp_port);
	    		props.put("mail.smtp.socketFactory.port", smtp_port);
	    		props.put("mail.smtp.socketFactory.fallback", "false");
	    		Session session = Session.getDefaultInstance(props);
	    		session.setDebug(debug);
	    		Message msg = new MimeMessage(session);
	    		
	    		InternetAddress addressFrom = null;
	    		
	    		//if(fromEmail==null){
	    		
	    			fromEmailToUse = (String) emailProps.get(FROM_EMAIL_ADDRESS);
	    		/*}
	    		else {
	    			fromEmailToUse = fromEmail;
	    		}*/
	    		
	    		addressFrom = new InternetAddress(fromEmailToUse);
	    		
	    		msg.setFrom(addressFrom);
	    		
	    		
	    		
	    		InternetAddress[] addressTo=getEmailToSendList(toList);
	    		if(addressTo==null || addressTo.length<=0)
	    			return "SENDER_LIST_EMPTY";
	    		
	    	
	    		msg.setRecipients(Message.RecipientType.TO, addressTo);
	    		if (ccList.length> 0){
	    			InternetAddress[] addressCc=getEmailToSendList(ccList);
	    			msg.setRecipients(Message.RecipientType.CC, addressCc);
	    		}
	    		
	    		if (bccList.length> 0){
	    			InternetAddress[] addressBcc=getEmailToSendList(bccList);
	    			msg.setRecipients(Message.RecipientType.BCC, addressBcc);
	    		}
	    		
	    		msg.setSubject(substitutedSubject);
	    		msg.setContent(substitutedBody, "text/html");
	    		Transport.send(msg);
	    		result="success";
	    		System.out.println("sendNotification() email sent successfully");
	    		
			} catch (tcAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (tcColumnNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			}
			
			return result;
		}


		
		public String searchADforOU(String lookupDecodeKey)
		{
			String methodName="searchADforOU";
			Hashtable env = getLdapEnv();
            boolean isExists = false;
            String result="failure";
			LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute");
			Hashtable ADparams =  getITResParameterDetails("Active Directory");
		        
            try {
                   LdapContext ctx = new InitialLdapContext(env,null);
                   SearchControls searchCtls = new SearchControls();
                   searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                  // String searchFilter = "(proxyAddresses="+userLogin+")";
                   String searchFilter = "(&(objectClass=organizationalUnit)(distinguishedName="+lookupDecodeKey+"))";
                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute start"+lookupDecodeKey);
                   String returnedAtts[]={"distinguishedName"};
                   searchCtls.setReturningAttributes(returnedAtts);
                   //NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                   NamingEnumeration answer = ctx.search(ADparams.get("Container").toString(), searchFilter, searchCtls);
                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute  check Email"+ADparams.get("Container").toString());
                   isExists = answer.hasMoreElements();
                   LOGGER.info(classname + methodName+ "----->>>>        " + "isExists"+isExists);
                   if(isExists == true)
                   {
                	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute  Inside check user Success");
                	   result="ou_in_AD";
                   }
                   else
                   {
                	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside searchADforAttribute  check user failure");
                	   result="ou_not_in_AD";
                   }
                   //Loop through the search results
                  
       			
            } catch (Exception e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                   result="failure";
            }

            LOGGER.info(classname + methodName+ "----->>>>        " + "Return isExists");
            
				return result;

     
		}	
	
		public String sendNotification(String oimEmailDefinitionName, HashMap emailReplaceAttributes, String[] toList, String[] ccList,String[] bccList){
			
			
			String methodName="sendNotification";
			//System.out.println("Staring sendNotification()");
			String result="failure";
			String body=null;
			String substitutedBody=null;
	    	String subject=null;
	    	String substitutedSubject=null;
	    	tcEmailOperationsIntf emailIntf=null;
	    	tcLookupOperationsIntf lookup =null;
	    	final String SMTP_HOST_NAME= "smptp host name";
	    	final String SMTP_PORT= "smptp port";
	    	final String FROM_EMAIL_ADDRESS= "from email";
			lookup = Platform.getService(tcLookupOperationsIntf.class);
			emailIntf = Platform.getService(tcEmailOperationsIntf.class);
			 
	    	tcResultSet rs= null;
			boolean debug = false;
			HashMap<String,String> emailProps = new HashMap<String, String>();
			String fromEmailToUse = null;
			HashMap hm = new HashMap();
	        hm.put("emd_name", oimEmailDefinitionName);
			
			try {
				emailProps=readLookupValues(lookup, "Lookup.Email.Properties");
				rs = emailIntf.findEmailDefinition(hm);
				if (rs.getRowCount()!=0){
					rs.goToRow(0);
					body=rs.getStringValue("Email Definition.Body");
					subject=rs.getStringValue("Email Definition.Subject");
				 }
				
				if (emailReplaceAttributes.size() !=0){
					substitutedBody = embedInReplceableAttribute(body, emailReplaceAttributes);
					substitutedSubject = embedInReplceableAttribute(subject, emailReplaceAttributes);
				}else{
					substitutedBody = body;
					substitutedSubject = subject;
				}
				
				//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	    		String smtp_host =(String) emailProps.get(SMTP_HOST_NAME);
	    		String smtp_port = (String) emailProps.get(SMTP_PORT);
	    		
	    		Properties props = new Properties();
	    		props.put("mail.smtp.host", smtp_host);
	    		props.put("mail.smtp.auth", "false");
	    		props.put("mail.debug", "true");
	    		props.put("mail.smtp.port", smtp_port);
	    		props.put("mail.smtp.socketFactory.port", smtp_port);
	    		props.put("mail.smtp.socketFactory.fallback", "false");
	    		Session session = Session.getDefaultInstance(props);
	    		session.setDebug(debug);
	    		Message msg = new MimeMessage(session);
	    		
	    		InternetAddress addressFrom = null;
	    		
	    		//if(fromEmail==null){
	    		
	    			fromEmailToUse = (String) emailProps.get(FROM_EMAIL_ADDRESS);
	    		/*}
	    		else {
	    			fromEmailToUse = fromEmail;
	    		}*/
	    		
	    		addressFrom = new InternetAddress(fromEmailToUse);
	    		
	    		msg.setFrom(addressFrom);
	    		
	    		
	    		
	    		InternetAddress[] addressTo=getEmailToSendList(toList);
	    		if(addressTo==null || addressTo.length<=0)
	    			return "SENDER_LIST_EMPTY";
	    		
	    	
	    		msg.setRecipients(Message.RecipientType.TO, addressTo);
	    		if (ccList.length> 0){
	    			InternetAddress[] addressCc=getEmailToSendList(ccList);
	    			msg.setRecipients(Message.RecipientType.CC, addressCc);
	    		}
	    		
	    		if (bccList.length> 0){
	    			InternetAddress[] addressBcc=getEmailToSendList(bccList);
	    			msg.setRecipients(Message.RecipientType.BCC, addressBcc);
	    		}
	    		
	    		msg.setSubject(substitutedSubject);
	    		msg.setContent(substitutedBody, "text/html");
	    		Transport.send(msg);
	    		result="success";
	    		System.out.println("sendNotification() email sent successfully");
	    		
			} catch (tcAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (tcColumnNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			
			
			return result;
		}
		private String embedInReplceableAttribute(String emailbody,HashMap emailReplaceAttributes){
			StrSubstitutor substitutor = new StrSubstitutor(emailReplaceAttributes);
			emailbody = substitutor.replace(emailbody);
			return emailbody;
		}
		
		public HashMap readLookupValues(tcLookupOperationsIntf lookupOper,String emailAttrMapLookupCodeName){
			
			String methodName="readLookupValues";
			HashMap<String,String> emailAtrrLookupCodeDecodeMap = new HashMap<String, String>();
			try {
				tcResultSet lookupRS = lookupOper.getLookupValues(emailAttrMapLookupCodeName);
				for (int i = 0; i < lookupRS.getRowCount(); i++) {
					lookupRS.goToRow(i);
					emailAtrrLookupCodeDecodeMap.put(lookupRS
							.getStringValue("Lookup Definition.Lookup Code Information.Code Key"), lookupRS
							.getStringValue("Lookup Definition.Lookup Code Information.Decode"));
					System.out.println("EmailUtil/readLookupValues : "
							+ lookupRS.getStringValue("Lookup Definition.Lookup Code Information.Code Key")
							+ " --> "
							+ lookupRS.getStringValue("Lookup Definition.Lookup Code Information.Decode"));
				}
			} catch (tcAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (tcInvalidLookupException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (tcColumnNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return emailAtrrLookupCodeDecodeMap;
		}
		
		private InternetAddress[] getEmailToSendList(String toList[])
		{
			
			InternetAddress[] addressTo=null;
			
			addressTo = new InternetAddress[toList.length];
			for (int i = 0; i < toList.length; i++) {
				try {
					addressTo[i] = new InternetAddress(toList[i]);
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return addressTo;
		}
		
		public long getlongITResParameterKey(String sITRes) 
	    {
	   	 
	   	  String methodName = "getLongITResParameterDetails";
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
	   	   
	   	  } catch ( Exception e ) {
	   		  LOGGER.info(classname + methodName+ "----->>>>        " + classname+ methodName + "tcAPIException occured while retriving data from IT-Resource "+ sITRes);
	   	  }
	   	  LOGGER.info(classname + methodName+ "----->>>>        " + classname + methodName+ " EXITING ");
	   	  return lSvrKey;
	   	 
	    }

		
//END		
		public String generatePwd(String loginid)
		 {
			 String methodName = "generatePwd"; 
			 String result="failure";
				String password=null;
				try {
					LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GeneratePassword ");
					UserManager um = null;
					um = (UserManager)Platform.getService(UserManager.class);
					password= generatePassword(9);
					um.changePassword(AttributeName.USER_LOGIN.getId(), loginid, password.toCharArray());
					LOGGER.info(classname + methodName+ "----->>>>        " + "Password is assigned successfully to user as : " + password);
				} catch (Exception e) {
					 result="failure";
					LOGGER.info(classname + methodName+ "----->>>>        " + e.getMessage());
				}
			  
				 result="success";
			  return result;
		 }
		 public String generatePassword(int Length)
		 {
			    String methodName = "generatePassword"; 
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
			    sb.append(digitsArray[r.nextInt(digitsArray.length)]);
				
			    // get at least one uppercase letter
			    sb.append(upcaseArray[r.nextInt(upcaseArray.length)]);
			 
			    // get at least one digit
			    sb.append(digitsArray[r.nextInt(digitsArray.length)]);
			    
			    // get at least one symbol
			    sb.append(symbolsArray[r.nextInt(symbolsArray.length)]);  

			    // fill in remaining with random letters
			    for (int i = 0; i < Length - 5; i++) {
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
			    
			    LOGGER.info(classname + methodName+ "----->>>>        " + "Pwg generated with New Logic"+sb.toString());
			    return sb.toString();
			}
/*
		 public String enableUserTasks(String userlogin,String password,String personType,String orgName,String lookupname,String firstName,String middleName,String lastName,String preferredName) 
		 {
				LOGGER.info(classname + methodName+ "----->>>>        " + "OrgName" +orgName );
				LOGGER.info(classname + methodName+ "----->>>>        " + "User Type" +personType);
				LOGGER.info(classname + methodName+ "----->>>>        " + "First Name  is: " +firstName);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Middle Name  is: " +middleName);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Last Name  is: " +lastName);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Title/Preferred Name  is: " +preferredName);
				
				try
				{
				String userTypes=null;
				tcLookupOperationsIntf lookupOps = Platform
              		     .getService(tcLookupOperationsIntf.class);
               	   tcResultSet values = lookupOps.getLookupValues(lookupname);
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
					
					if(personType.equalsIgnoreCase(tokens[j]))
					{
						LOGGER.info(classname + methodName+ "----->>>>        " + "USERTYPES Matches");
						iGenerateEmail=0;
					}
						
				}
				
				//
    			if(orgName.equalsIgnoreCase("Internal User") && iGenerateEmail == 1 )
				//if(orgName.equals("Internal User"))
				{
					
				//String loginid = getParamaterValue(parameters, AttributeName.USER_LOGIN.getId());
							
				String generatedEmail = generateEmail(preferredName,firstName,middleName,lastName);
				LOGGER.info(classname + methodName+ "----->>>>        " + "Email Is: " +generatedEmail);
				}
    			
    			//SEND MAILS
    			
    			 UserRepository userRepository = new DBUserRepository();
    		     UserInfo usrInfo = userRepository.getUserAndManagerInfo(userlogin);
                 LOGGER.info(classname + methodName+ "----->>>>        " + "Sending Reset Password details\n");
                 if(password != null){
                     sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
                 	sendNotificationToUsr(usrInfo, String.valueOf(password), "New Hire Password");
                 }
  
		 }
    			catch (Exception e){
    				LOGGER.info(classname + methodName+ "----->>>>        " + "Error occured in updating user: " );
				   // eventLogger.severe("[" + methodName +"] Error occured in updating user" + e.getMessage());
                    }
                
			 
			 return null ;
		 }
		 
		   private void sendNotificationToUsr(UserInfo usrInfo, String password, String templateName)
		    {
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

	

		 private String generateEmail(String preferredName, String firstName, String middleName, String lastName)
    			 {
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
    					   if( getUserLogin(email) != 0)
    				       {	
    						   LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 1");

    				    	   return email; 
    				       }
    				       else{
    				    	   	  if(!isNullOrEmpty(middleName))
    				    	   		{
    				    	   		LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GenerateMail Empty Middle name");
    								
    				    	   		email = modifiedPreferredName+"."+modifiedmiddleName+"."+modifiedlastName+"@ul.com";
    				    	   		if( getUserLogin(email) != 0)
    				    	   		{
    				    	   			return email;  
    				  			    }
    				    	   	   }	
    				    	   	  else
    				    	   	  {
    					    	   		LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 3");

    				    	   		email = modifiedPreferredName+"."+InitialsOfFirstName+"."+modifiedlastName+"@ul.com";
    				    	   		if( getUserLogin(email) != 0)
    				    	   		{
    				    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 3.5");

    				    	   			return email;  
    				  			    }
    				    	   		  
    				    	   	  }
    				       		}   
    				   }
    				 
    				 email = modifiedfirstName+"."+modifiedlastName+"@ul.com";
    				 LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is: " +email);
    				   if( getUserLogin(email) != 0)
    			       {	
    					   LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 4");

    			    	   return email;  
    			       }
    			       else{
    			    	   	  if(!isNullOrEmpty(middleName))
    			    	   		{

    			    	   		email = modifiedfirstName+"."+modifiedmiddleName+"."+modifiedlastName+"@ul.com";
    			    	   		if( getUserLogin(email) != 0)
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

    						   if( getUserLogin(email) != 0)
    					       {	
    							   LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 7");

    					    	   return email; 
    					       }
    					       else{
    					    	   		email = modifiedPreferredName+".X1."+modifiedlastName+"@ul.com";
    					    	   		if( getUserLogin(email) != 0)
    					    	   		{
    					    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 8");

    					    	   			return email;  
    					  			         			
    					    	   		}
    					    	   		else{
    					    	   			email = modifiedPreferredName+".X2."+modifiedlastName+"@ul.com";
    						    	   		if( getUserLogin(email) != 0)
    						    	   		{
    						    	   			LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Case 9");

    						    	   			return email;  
    						  			         			
    						    	   		}	
    					    	   		}
    				    	   		}   
    					   }
    					 
    					 email = modifiedfirstName+".X."+modifiedlastName+"@ul.com";
    					 LOGGER.info(classname + methodName+ "----->>>>        " + "Email  is: " +email);
    					   if( getUserLogin(email) != 0)
    				       {	
    				    	   return email;  
    				       }
    				       else{
    				    	 	 
    				    	   		email = modifiedfirstName+".X1."+modifiedlastName+"@ul.com";
    				    	   		if( getUserLogin(email) != 0)
    				    	   		{
    				    	   			return email;  
    				  			         			
    				    	   		}
    				    	   		else{
    				    	   			email = modifiedfirstName+".X2."+modifiedlastName+"@ul.com";
    					    	   		if( getUserLogin(email) != 0)
    					    	   		{
    					    	   			return email;  
    					  			         			
    					    	   		}
    					    	   		
    					    	   		}
    				    	   		 for (int i=3;i<50;i++)
    				    	   		 {
    				    	   			email = modifiedfirstName+".X"+i+"."+modifiedlastName+"@ul.com";
    					    	   		if( getUserLogin(email) != 0)
    					    	   		{
    					    	   			return email;  
    					  			         			
    					    	   		}
    				    	   		 }
    				       		  }   
    				   		    
    					   
    				
    				 LOGGER.info(classname + methodName+ "----->>>>        " + "Returning Email: " +email);
    				 LOGGER.info(classname + methodName+ "----->>>>        " + "GenerateEmail Email");

    			        return email ;

    			 }
    			 private int getUserLogin(String Email) {
    			        Vector mvUsers = new Vector();
    			        UserManager userService = (UserManager)Platform.getService(UserManager.class); 
    			        Set<String> retAttrs = new HashSet<String>();
    			        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin");
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
    					        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 2");

    		               		return 0;
    		               	}
    				        else
    				        {
    					        LOGGER.info(classname + methodName+ "----->>>>        " + "GetUserLogin 3");

    				        	isExists=getUserDN1("SVC.UL.IAMDEV","!dentity&AM","uspocx882t.global.ul.com",Email,"DC=global,DC=ul,DC=org");
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
    				        }
    			            
    			        } catch (AccessDeniedException ade) {
    			            // handle exception
    			        } catch (UserSearchException use) {
    			            // handle exception
    			        }
    			      return 0;  
    		} 
    			     
    			 public boolean getUserDN1(String adminid,String pwd,String adserver,String email,String searchBase)
    				{
    		            
    		            Hashtable env = getLdapEnv(adminid,pwd,adserver);
    		            boolean isExists = false;
    					 LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN");

    		            try {
    		                   LdapContext ctx = new InitialLdapContext(env,null);
    		                   SearchControls searchCtls = new SearchControls();
    		                   searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    		                   String searchFilter = "(proxyAddresses="+email+")";
    		                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN start");

    		                   String returnedAtts[]={"distinguishedName"};
    		                   searchCtls.setReturningAttributes(returnedAtts);
    		                   NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
    		                   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN check Email");
    		                   isExists = answer.hasMoreElements();
    		                   
    		                   if(isExists == false)
    		                   {
    		                	   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetUserDN Inside check Email");
    		  	                 
    		                	String searchFilter1 = "(mail=" + email + ")";
    		                    SearchControls sc1 = new SearchControls();
    							sc1.setSearchScope(SearchControls.SUBTREE_SCOPE);
    							NamingEnumeration<javax.naming.directory.SearchResult> results1;
    							results1 = ctx.search(searchBase, searchFilter1, sc1);
    							isExists = results1.hasMoreElements();
    		                   }
    		                   //Loop through the search results
    		                  
    		       			
    		            } catch (NamingException e) {
    		                   // TODO Auto-generated catch block
    		                   e.printStackTrace();
    		            }

    		            LOGGER.info(classname + methodName+ "----->>>>        " + "Return isExists");
    	                
    	   				return isExists;

    		            
    		     }
    				
    */			 
		 
		 public String deleteFromEBSChildForm(String processInstKey, String childColumn,String lookupName,String childTableName,String lookupCodKey,Date EffectiveEndDate,tcDataProvider tcdp)
		 {
			 String methodName="deleteFromEBSChildForm";
			 String result="failure";
			 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
			// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
			 
			 try 
			 {
				 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
				 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+parentFormDefKey);
				 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"active version "+activeVersion +EffectiveEndDate );
				 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
				 int i=rset.getRowCount();
				 if(i>0){
					 for(int j=0;j<i;j++){
						 rset.goToRow(j);
						 
						 String chtabName=rset.getStringValueFromColumn(2);
						 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+" Table Name"+chtabName);
						 

						 if(chtabName.equalsIgnoreCase(childTableName)){
							 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
			                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
			                 /*
			                 String decode= getLookupValue(lookupName,lookupCodKey);
							 String []resArray = decode.split(",");
							 */
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
							 for (int m = 0 ; m < childFormData.getRowCount();m++)  
		                     {  
		                       childFormData.goToRow(m);
		                         //long childFormKey = childFormData.getLongValue("UD_EBS_RESP_KEY");
		                       long childFormKey = childFormData.getLongValue(childTableName+"_KEY");
		                       		//   if (resArray !=null)
		                       		//  {
		      						//	 for(int k=0; k<resArray.length;k++)
		      						//	 {
		      							LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp"+childFormData.getStringValue(childColumn));
		      							//if (childFormData.getStringValue(childColumn).equalsIgnoreCase(resArray[k]))
		      							//			 {
		      		                    //LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp");
		      		                    //NEW
		      							
		      		            		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
		      		            		//Date date = ft.parse(EffectiveEndDate);
		      		            		LOGGER.info(classname + methodName+ "----->>>>        " + "responsibility Map "+EffectiveEndDate);
		      		            		 HashMap endDate= new HashMap();
		      		            		 //endDate.put(childColumn,EffectiveEndDate);
		      		            		endDate.put(childColumn,EffectiveEndDate);
		      		            		LOGGER.info(classname + methodName+ "----->>>>        " + "responsibility Map "+EffectiveEndDate);


		      		            		//tcFormInOpsIntf.updateProcessFormChildData(arg0, arg1, arg2);
		    							 tcFormInOpsIntf.updateProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey,endDate);
		    		                       //
		      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
		          							 //tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
		    							 LOGGER.info(classname + methodName+ "----->>>>        " + "Updated");
		    							 
		    							 //		 }
		      		                       
		    							 // }
		      						 
		      					// }
		                         
		                     }  
							
							// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
							 result="success";
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"the final result "+result);
							 break;
						 }
					 }
				 }
				 
			 } catch (Exception e) {
				 
				 e.printStackTrace();
			 } 	 
			 return result;
		}
		 
		 
		 public String UpdateEBSChildForm(String processInstKey,String childColumnRespName, String childColumn,String lookupName,String childTableName,String lookupCodKey,Date EffectiveEndDate,tcDataProvider tcdp)
		 {
			 String methodName="UpdateEBSChildForm";
			 String result="failure";
			 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
			 LOGGER.info(classname + methodName+ "----->>>>        Entered" + classname+methodName);
			// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
			 String decode= getLookupValue(lookupName,lookupCodKey);
		     String []resArray = decode.split(",");
			 LOGGER.info(classname + methodName+ "----->>>>        After Lookup Query" + classname+methodName);
		       
			 try 
			 {
				 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
				 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+parentFormDefKey);
				 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"active version "+activeVersion +EffectiveEndDate );
				 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
				 int i=rset.getRowCount();
				 if(i>0){
					 for(int j=0;j<i;j++){
						 rset.goToRow(j);
						 
						 String chtabName=rset.getStringValueFromColumn(2);
						 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+" Table Name"+chtabName);
						 

						 if(chtabName.equalsIgnoreCase(childTableName)){
							 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
			                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
			                 /*
			                 String decode= getLookupValue(lookupName,lookupCodKey);
							 String []resArray = decode.split(",");
							 */
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
							 for (int m = 0 ; m < childFormData.getRowCount();m++)  
		                     {  
		                       childFormData.goToRow(m);
		                         //long childFormKey = childFormData.getLongValue("UD_EBS_RESP_KEY");
		                       long childFormKey = childFormData.getLongValue(childTableName+"_KEY");
		                       		//   if (resArray !=null)
		                       		//  {
		      						//	 for(int k=0; k<resArray.length;k++)
		      						//	 {
		      							LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp"+childFormData.getStringValue(childColumn));
		      							//if (childFormData.getStringValue(childColumn).equalsIgnoreCase(resArray[k]))
		      							//			 {
		      		                    //LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp");
		      		                    //NEW
		      							
		      		            		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
		      		            		//Date date = ft.parse(EffectiveEndDate);
		      		            		 HashMap endDate= new HashMap();
		      		            		 //endDate.put(childColumn,EffectiveEndDate);
		      		            		endDate.put(childColumn,EffectiveEndDate);
		      		            		LOGGER.info(classname + methodName+ "----->>>>        " + "Effective End Date "+EffectiveEndDate);
	      			            		LOGGER.info(classname + methodName+ "----->>>>        CHILD COLUMN value" + childFormData.getStringValue(childColumnRespName));
		      		            		for(int k=0 ; k< resArray.length; k++)
		      		            		{

	          			            		LOGGER.info(classname + methodName+ "----->>>>        HashMap Value" + resArray[k]);
		      		            			if(childFormData.getStringValue(childColumnRespName).equalsIgnoreCase(resArray[k]))
		      		            			{
		          			            		LOGGER.info(classname + methodName+ "----->>>>     Match Found" );
		          			            		tcFormInOpsIntf.updateProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey,endDate);
		      		            			}
		      		            			 
		      		            		}
		      		            		//tcFormInOpsIntf.updateProcessFormChildData(arg0, arg1, arg2);
		    							   //
		      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
		          							 //tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
		    							 LOGGER.info(classname + methodName+ "----->>>>        " + "Updated");
		    							 
		    							 //		 }
		      		                       
		    							 // }
		      						 
		      					// }
		                         
		                     }  
							
							// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
							 result="success";
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"the final result "+result);
							 break;
						 }
					 }
				 }
				 
			 } catch (Exception e) {
				 
				 e.printStackTrace();
			 } 	 
			 return result;
		}


		 /*
		 public String searchAndProvisionADAppInstance(String userKey,String appInstName,String serverName,String resourceName,tcDataProvider tcdp)
		 {
			 String methodName="searchAndProvisionADAppInstance";
			 String result="failure";
			 int iProvisionFlag = 1;
			 ProvisioningService provisioningService = Platform.getService(ProvisioningService.class);
             ApplicationInstanceService applicationInstanceService = Platform.getService(ApplicationInstanceService.class);
             oracle.iam.provisioning.vo.ApplicationInstance applicationInstance =null;
             try
             {
             LOGGER.info(classname + methodName+ "----->>>>        " + "User Key and Long User Key " +userKey+Long.parseLong(userKey)); 
			 //Searching Logic
             tcResultSet rset; 
			 tcUserOperationsIntf userIntf = Platform.getService(tcUserOperationsIntf.class);
		     rset=userIntf.getObjects(Long.parseLong(userKey)); 
		     int count=rset.getRowCount(); 
		     // Here in this section of code 
		     for(int k=0;k < count ; k++)
		     { 
			     rset.goToRow(k); 
			     String targetName=rset.getStringValue("Objects.Name"); 
			     //String objInstKey = rset.getStringValue("Users-Object Instance For User.Key"); 
			     String status =rset.getStringValue("Objects.Object Status.Status"); 
			     LOGGER.info(classname + methodName+ "----->>>>        " + " Resource Name And Status " +targetName+status); 
			     if(targetName.equalsIgnoreCase(resourceName) )//&& status.equalsIgnoreCase("Disabled"))
			     {
			    	 iProvisionFlag = 0;
			     }
		     
		     } 

			     if(iProvisionFlag == 1)
			  {
					 //Provisioning Logic
					 applicationInstance = applicationInstanceService.findApplicationInstanceByName(appInstName);
		             FormInfo accountForm = applicationInstance.getAccountForm();
		             long formKey = accountForm.getFormKey();
		             Long itreskey = applicationInstance.getItResourceKey();
		             Map parentData = new HashMap();
		             parentData.put(serverName, itreskey);                                               
		             AccountData accountData = new AccountData(String.valueOf(formKey), null, parentData);
		             oracle.iam.provisioning.vo.Account account = new Account(applicationInstance, accountData);
		             account.setAccountType(Account.ACCOUNT_TYPE.Primary);
				     LOGGER.info(classname + methodName+ "----->>>>        " + " Before Account Provisioning "); 
		             provisioningService.provision(userKey,account);
		             LOGGER.info(classname + methodName+ "----->>>>        " + " After Account Provisioning "); 
		             
			     }
             	//LOGGER.info("ProvisionEBSTask.execute() User Provisioned EBS for userid= ");
		     	result="success";
             }
             catch (Exception e) 
             {
    		     result="failure";
            	 e.printStackTrace();
     		 }
			 //
			 return result;

		 }
		 */
		 public String searchResourceInstanceInUserProfile(String userKey,String resourceName)
		 {
			 String methodName="searchResourceInstanceInUserProfile";
			 String result="failure";
			 int iProvisionFlag = 1;
			 ProvisioningService provisioningService = Platform.getService(ProvisioningService.class);
             ApplicationInstanceService applicationInstanceService = Platform.getService(ApplicationInstanceService.class);
             oracle.iam.provisioning.vo.ApplicationInstance applicationInstance =null;
             try
             {
             LOGGER.info(classname + methodName+ "----->>>>        " + "User Key and Long User Key " +userKey+Long.parseLong(userKey)); 
			 //Searching Logic
             tcResultSet rset; 
			 tcUserOperationsIntf userIntf = Platform.getService(tcUserOperationsIntf.class);
		     rset=userIntf.getObjects(Long.parseLong(userKey)); 
		     int count=rset.getRowCount(); 
		     // Here in this section of code 
			     for(int k=0;k < count ; k++)
			     { 
				     rset.goToRow(k); 
				     String targetName=rset.getStringValue("Objects.Name"); 
				     //String objInstKey = rset.getStringValue("Users-Object Instance For User.Key"); 
				     String status =rset.getStringValue("Objects.Object Status.Status"); 
				     LOGGER.info(classname + methodName+ "----->>>>        " + " Resource Name And Status " +targetName+status); 
				     if(targetName.equalsIgnoreCase(resourceName) )//&& status.equalsIgnoreCase("Disabled"))
				     {
				    	 iProvisionFlag = 0;
				    	 return "resource_found";
				     }
			     
			     } 
			     	result="resource_not_found";
             }
             catch (Exception e) 
             {
    		     result="failure";
            	 e.printStackTrace();
     		 }
			 //
			 return result;

		 }
		 
		 public String ProvisionAppInstancetoUser(String userKey,String appInstName,String serverName)
		 {
			 String methodName="ProvisionAppInstancetoUser";
			 String result="failure";
			 int iProvisionFlag = 1;
			 ProvisioningService provisioningService = Platform.getService(ProvisioningService.class);
             ApplicationInstanceService applicationInstanceService = Platform.getService(ApplicationInstanceService.class);
             oracle.iam.provisioning.vo.ApplicationInstance applicationInstance =null;
             try
             {
             LOGGER.info(classname + methodName+ "----->>>>        " + "User Key and Long User Key " +userKey+Long.parseLong(userKey)); 
			//Provisioning Logic
			 applicationInstance = applicationInstanceService.findApplicationInstanceByName(appInstName);
             FormInfo accountForm = applicationInstance.getAccountForm();
             long formKey = accountForm.getFormKey();
             Long itreskey = applicationInstance.getItResourceKey();
             Map parentData = new HashMap();
             parentData.put(serverName, itreskey);                                               
             AccountData accountData = new AccountData(String.valueOf(formKey), null, parentData);
             oracle.iam.provisioning.vo.Account account = new Account(applicationInstance, accountData);
             account.setAccountType(Account.ACCOUNT_TYPE.Primary);
		     LOGGER.info(classname + methodName+ "----->>>>        " + " Before Account Provisioning "); 
             provisioningService.provision(userKey,account);
             LOGGER.info(classname + methodName+ "----->>>>        " + " After Account Provisioning "); 
             
			     
             	//LOGGER.info("ProvisionEBSTask.execute() User Provisioned EBS for userid= ");
		     	result="success";
             }
             catch (Exception e) 
             {
    		     result="failure";
            	 e.printStackTrace();
     		 }
			 //
			 return result;

		 }

		 

		 
		 public String resetEBSChildFormDate(String processInstKey, String childColumn,String childTableName,tcDataProvider tcdp)
		 {
			 String methodName="resetEBSChildFormDate";
			 String result="failure";
			 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
			// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
			 
			 try 
			 {
				 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
				 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+parentFormDefKey);
				 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"active version "+activeVersion  );
				 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
				 int i=rset.getRowCount();
				 if(i>0){
					 for(int j=0;j<i;j++){
						 rset.goToRow(j);
						 
						 String chtabName=rset.getStringValueFromColumn(2);
						 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+" Table Name"+chtabName);
						 

						 if(chtabName.equalsIgnoreCase(childTableName)){
							 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
			                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
			                 /*
			                 String decode= getLookupValue(lookupName,lookupCodKey);
							 String []resArray = decode.split(",");
							 */
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
							 for (int m = 0 ; m < childFormData.getRowCount();m++)  
		                     {  
		                       childFormData.goToRow(m);
		                         //long childFormKey = childFormData.getLongValue("UD_EBS_RESP_KEY");
		                       long childFormKey = childFormData.getLongValue(childTableName+"_KEY");
		                       		//   if (resArray !=null)
		                       		//  {
		      						//	 for(int k=0; k<resArray.length;k++)
		      						//	 {
		      							LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp"+childFormData.getStringValue(childColumn));
		      							//if (childFormData.getStringValue(childColumn).equalsIgnoreCase(resArray[k]))
		      							//			 {
		      		                    //LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp");
		      		                    //NEW
		      							
		      		            		//SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
		      		            		//Date date = ft.parse(EffectiveEndDate);
		      		            		LOGGER.info(classname + methodName+ "----->>>>        " + "responsibility Map ");
		      		            		 HashMap endDate= new HashMap();
		      		            		 //endDate.put(childColumn,EffectiveEndDate);
		      		            		endDate.put(childColumn," ");
		      		            		LOGGER.info(classname + methodName+ "----->>>>        " + "set Date to Empty ");


		      		            		//tcFormInOpsIntf.updateProcessFormChildData(arg0, arg1, arg2);
		    							 tcFormInOpsIntf.updateProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey,endDate);
		    		                       //
		      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
		          							 //tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
		    							 LOGGER.info(classname + methodName+ "----->>>>        " + "Updated");
		    							 
		    							 //		 }
		      		                       
		    							 // }
		      						 
		      					// }
		                         
		                     }  
							
							// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
							 result="success";
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"the final result "+result);
							 break;
						 }
					 }
				 }
				 
			 } catch (Exception e) {
				 
				 e.printStackTrace();
			 } 	 
			 return result;
		}

		 //BACKUP
		 
		 
		 public String BackupdeleteFromEBSChildForm(String processInstKey, String childColumn,String lookupName,String childTableName,String lookupCodKey,tcDataProvider tcdp)
		 {
			 String methodName="deleteFromEBSChildForm";
			 String result="failure";
			 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
			// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
			 
			 try {
				 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
				 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+parentFormDefKey);
				 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"active version "+activeVersion);
				 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
				 int i=rset.getRowCount();
				 if(i>0){
					 for(int j=0;j<i;j++){
						 rset.goToRow(j);
						 
						 String chtabName=rset.getStringValueFromColumn(2);
						 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+" Table Name"+chtabName);
						 

						 if(chtabName.equalsIgnoreCase(childTableName)){
							 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
			                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
			                 String decode= getLookupValue(lookupName,lookupCodKey);
							 String []resArray = decode.split(",");
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
							 for (int m = 0 ; m < childFormData.getRowCount();m++)  
		                     {  
		                       childFormData.goToRow(m);
		                       LOGGER.info(classname + methodName+ "----->>>>        " + "Inside Delete");
		     				
		                         //long childFormKey = childFormData.getLongValue("UD_EBS_RESP_KEY");
		                       long childFormKey = childFormData.getLongValue(childTableName+"_KEY");
		                         if (resArray !=null){
		      						 for(int k=0; k<resArray.length;k++){
		      							LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp"+childFormData.getStringValue(childColumn)+resArray[k]);
		      							 if (childFormData.getStringValue(childColumn).equalsIgnoreCase(resArray[k]))
		      							 {
		      		                       LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp");

		      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
		          							 tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
		      							 }
		      		                       
		      						 }
		      						 
		      					 }
		                         
		                     }  
							
							// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
							 result="success";
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"the final result "+result);
							 break;
						 }
					 }
				 }
				 
			 } catch (Exception e) {
				 
				 e.printStackTrace();
			 } 	 
			 return result;
		}

		 //
		 public String deleteFromADChildForm(String processInstKey,String childTableName,tcDataProvider tcdp)
		 {
			 String methodName="deleteFromAdChildForm";
			 String result="failure";
			 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
			// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
			 
			 try {
				 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
				 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+parentFormDefKey);
				 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
				 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"active version "+activeVersion);
				 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
				 int i=rset.getRowCount();
				 if(i>0){
					 for(int j=0;j<i;j++){
						 rset.goToRow(j);
						 
						 String chtabName=rset.getStringValueFromColumn(2);
						 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+" Table Name"+chtabName);
						 if(chtabName.equalsIgnoreCase(childTableName)){
							 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
			                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
			        		 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
							 for (int m = 0 ; m < childFormData.getRowCount();m++)  
		                     {  
		                       childFormData.goToRow(m);
		                       LOGGER.info(classname + methodName+ "----->>>>        " + "Inside Delete");
		                     //  	long childFormKey = childFormData.getLongValue("UD_ADUSRC_KEY");
		                     	long childFormKey = childFormData.getLongValue(childTableName+"_KEY");
		                         		LOGGER.info(classname + methodName+ "----->>>>        " + "Inside innermostloop delete resp");
		      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
		          							 tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
		                         
		                     }  
							
							// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
							 result="success";
							 LOGGER.info(classname + methodName+ "----->>>>        " + classname+methodName+"the final result "+result);
							 break;
						 }
					 }
				 }
				 
			 } catch (Exception e) {
				 
				 e.printStackTrace();
			 } 	 
			 return result;
		}

		 
    			 
	   public boolean isNullOrEmpty(String s) 
	   {
		   String methodName = "isNullOrEmpty";
		   LOGGER.info(classname + methodName+ "----->>>>        " + "Inside IsNullorEmpty ");
		   boolean result= false;
		   if(s == null || s.isEmpty())
		   {
			result = true;   
		   }
		   LOGGER.info(classname + methodName+ "----->>>>        " + "Output"+result);
		   return result;
       }	
}
