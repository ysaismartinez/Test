package com.iam.generic.scheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

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
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.identity.exception.AccessDeniedException;
import oracle.iam.identity.exception.NoSuchUserException;
import oracle.iam.identity.exception.SearchKeyNotUniqueException;
import oracle.iam.identity.exception.UserModifyException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.exception.ValidationFailedException;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.Platform;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;

//import org.apache.commons.lang.text.StrSubstitutor;

import org.apache.commons.lang3.text.StrSubstitutor;

import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcFormEntryNotFoundException;
import Thor.API.Exceptions.tcFormNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Exceptions.tcNotAtomicProcessException;
import Thor.API.Exceptions.tcProcessNotFoundException;
import Thor.API.Exceptions.tcRequiredDataMissingException;
import Thor.API.Exceptions.tcVersionNotDefinedException;
import Thor.API.Exceptions.tcVersionNotFoundException;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcFormInstanceOperationsIntf;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcUserOperationsIntf;


/**
 * @author Zubair Khan 
 * 
 */
public class IdentityTypeConversion extends oracle.iam.scheduler.vo.TaskSupport {
	
	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	String classname="IdentityTypeConversion.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	UserRepository userRepository=null;
	UserManager userService = null; 
	UserInfo usrInfo=null;
	tcITResourceInstanceOperationsIntf itResourceIntf=null;
	public final String SMTP_HOST_NAME= "smptp host name";
	public final String SMTP_PORT= "smptp port";
	public final String FROM_EMAIL_ADDRESS= "from email";
	
	/*
	 * Scheduled task entry point
	 */
	public void execute(HashMap taskparams) {
		
		LOGGER.info("Starting IdentityTypeConversion Scheduled Task ");
		
		lookup= Platform.getService(tcLookupOperationsIntf.class);
		//userRepository=Platform.getService(UserRepository.class);
		//usrInfo = Platform.getService(UserInfo.class);
		emailIntf = Platform.getService(tcEmailOperationsIntf.class);
		itResourceIntf= Platform.getService(tcITResourceInstanceOperationsIntf.class);
		userService = (UserManager)Platform.getService(UserManager.class); 
		tcUserOperationsIntf tcuser= Platform.getService(tcUserOperationsIntf.class);
		tcFormInstanceOperationsIntf tcform= Platform.getService(tcFormInstanceOperationsIntf.class);
		
		String lookupName= (String) taskparams.get("Lookup Name");
		String emailTemp = (String) taskparams.get("Email Template");
		String toEmail = (String) taskparams.get("Service Now toEmail");
                String adChildColumn= (String) taskparams.get("AD Child Column");
		String roAD = (String) taskparams.get("AD RO Name");   
		String roEBS = (String) taskparams.get("EBS RO Name"); 
		String chtEBS = (String) taskparams.get("EBS Child Table");
		String chtAD = (String) taskparams.get("AD Child Table");
		String ebsChildColumn= (String) taskparams.get("EBS Child Column");
		String adChildColumnKey= (String) taskparams.get("AD Child Column Key");
		String ebsChildColumnKey= (String) taskparams.get("EBS Child Column Key");
		String contractorLookupName = (String) taskparams.get("Contractor Lookup Name");
		String contractorCodeKey = (String) taskparams.get("Contractor Lookup Code Key");
		String adITRes = (String) taskparams.get("AD IT Resource");

		
		LOGGER.info("lookupName "+lookupName);
		LOGGER.info("emailTemp "+emailTemp);
		HashMap<String,String> reminderDetMap = readLookupValues(lookup,lookupName);
		LOGGER.info("Lookup Map "+reminderDetMap);
		
		if (reminderDetMap!=null && reminderDetMap.size()>0){
			for (Entry<String, String> entry : reminderDetMap.entrySet()){
				//LOGGER.info("Inside for Loop ");
				String uid= entry.getKey();
                                LOGGER.info("uid" +uid);
				String effectiveStartDate= entry.getValue();
				if (effectiveStartDate!=null ){
					
					if (noOfDaysSinceCreation(effectiveStartDate)==0){
						
						LOGGER.info("checking if user has ul email");
						String email=getEmailID(uid);
                                                LOGGER.info("User Email is" + email);  //changes made by Hina 
                                                    //	if (!email.contains("@ul.com")){   //changes made by Hina 
                                                        if (email == null || !email.contains("@ul.com")){  //changes made by Hina 
							LOGGER.info("user does not have ul email.....generating it....");
							UserInfo userinfo =getUserInfo(uid);
							String preferredName=(String) userinfo.getAttribute("usr_PreferredName");
							LOGGER.info("preferredName "+preferredName);
							String middleName=(String) userinfo.getAttribute("Middle Name");
							String genEmail=generateEmail(preferredName,userinfo.getFirstName(),middleName,userinfo.getLastName(),adChildColumn);
							LOGGER.info("generated Email "+genEmail);
							HashMap hm = new HashMap();
							LOGGER.info("Modifying the userform with new email");
							hm.put(UserManagerConstants.AttributeName.EMAIL.getId(), genEmail);
							User user = new User(userinfo.getUserKey(),hm);
							try {
								userService.modify(AttributeName.USER_LOGIN.getId(),uid,user);
							} catch (ValidationFailedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UserModifyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NoSuchUserException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SearchKeyNotUniqueException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (oracle.iam.platform.authz.exception.AccessDeniedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							LOGGER.info("After modification "+user.getEmail());
						}
						LOGGER.info("Now the  email would be sent to service now");
						sendNotification(uid,toEmail,emailTemp,effectiveStartDate);
                                                LOGGER.info("Main sent to service now successfully");
                                                removeLookupValues(lookup,lookupName,uid);  //Changes made by Hina to call remove look up on Hire Date
                                                LOGGER.info("Values remove from lookup successfully");
					         // Start of changes Made by Hina to stop enddating responsibilities and group membership in EBS and AD

						//clearing all the child entries for AD and EBS
						//String pInstKeyforAD= getProcessInstanceKey(uid,roAD,tcuser);
						//String pInstKeyforEBS= getProcessInstanceKey(uid,roEBS,tcuser);
						//LOGGER.info("ProcessInst Key for AD and EBS: "+pInstKeyforAD+" "+pInstKeyforEBS);
						
						//removeDataChildProcessForm(Long.parseLong(pInstKeyforAD),adChildColumnKey,tcform);
						//LOGGER.info("Child data cleared from AD");
						//removeDataChildProcessForm(Long.parseLong(
                                                //),ebsChildColumnKey,tcform);
						//LOGGER.info("Child data cleared from EBS");
						
						//Assigning new responsibilities to the employee turned contractor
						//String result= insertToChildForm(pInstKeyforEBS,ebsChildColumn,contractorLookupName,chtEBS,contractorCodeKey,tcform);
						//LOGGER.info("Responsibilities added to ebs result "+result);
												 
						//updateEBSDate(pInstKeyforEBS,effectiveStartDate,tcform);
						//LOGGER.info(" Date updated in ebs ");
                                                
						// End of changes Made by Hina to stop enddating responsibilities and group membership in EBS and AD

					}
				}
			}
			
		}
		
		
	}
	
	public HashMap getAttributes() {
		System.out.println("entering get attributes");
		return null;
	}
	
	public void setAttributes() {
		System.out.println("entering set attributes");
	}
	
	
	
	public int daysBetween(Date d1, Date d2){
		return (int)( (d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24));
	}
	
	/**
	 * 
	 * @param lookupOper
	 * @param emailAttrMapLookupCodeName
	 * @return
	 */
	public HashMap<String,String> readLookupValues(tcLookupOperationsIntf lookupOper,String emailAttrMapLookupCodeName){
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
	
	public int noOfDaysSinceCreation(String strReminderDate) {
		 int i=-1;
		 
		 	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date reminderDate=null;
			//Date _creationDate;
			try {
				reminderDate=sdf.parse(strReminderDate);
				Calendar remindercal = Calendar.getInstance();
				remindercal.setTime(reminderDate);
				//LOGGER.info("reminderDate "+reminderDate);
				String datestr= sdf.format(new Date());
				//LOGGER.info("datestr "+datestr);
				Date today=sdf.parse(datestr);
				//LOGGER.info("today "+today);
				Calendar todaycal = Calendar.getInstance();
				todaycal.setTime(today);
				i=daysBetween(todaycal.getTime(), remindercal.getTime());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			LOGGER.info("no of days since entry is created "+i);
		 return i;
	 }
	private void sendNotification(String uid,String toMail,String emailTemplate,String hireDate){
		
		LOGGER.info("Staring sendNotification");
		UserInfo userInfo = getUserInfo(uid);
		Map hm = userInfo.getAttributes();
		LOGGER.info(classname+".sendNotification() Attribute Map "+hm);
		
		String Employee_type = (String) userInfo.getAttribute("usr_PersonType");
		String Employee_location = (String) userInfo.getAttribute("usr_Location");
		String Employee_supervisor = (String) userInfo.getAttribute("MANAGER_LOGIN");
		//String Employee_Hire_date = (String) userInfo.getAttribute("Hire Date");
		String Employee_Name = userInfo.getFirstName()+" "+userInfo.getLastName();
		String jobTitle = (String) userInfo.getAttribute("usr_udf_Job");
		String email = (String) userInfo.getAttribute("Email");
				
		LOGGER.info("to email lsit "+toMail);
		sendEmailtoServiceNow(emailTemplate,toMail,Employee_type,Employee_location,Employee_supervisor,hireDate,uid,Employee_Name,email,jobTitle);
	}
	
	public String sendNotification(String oimEmailDefinitionName, HashMap emailReplaceAttributes, String[] toList, String[] ccList,String[] bccList){
		System.out.println("Staring sendNotification()");
		String result="failure";
		String body=null;
		String substitutedBody=null;
		String subject=null;
		String substitutedSubject=null;
		tcResultSet rs= null;
		boolean info = false;
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
			props.put("mail.info", "true");
			props.put("mail.smtp.port", smtp_port);
			props.put("mail.smtp.socketFactory.port", smtp_port);
			props.put("mail.smtp.socketFactory.fallback", "false");
			Session session = Session.getDefaultInstance(props);
			session.setDebug(info);
			Message msg = new MimeMessage(session);
			
			InternetAddress addressFrom = null;
			
			//if(fromEmail==null){
			
			fromEmailToUse = (String) emailProps.get(FROM_EMAIL_ADDRESS);
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
		} finally{
			if (emailIntf!=null)
				emailIntf.close();
		}
		
		
		
		return result;
	}
	
	
	
	private String embedInReplceableAttribute(String emailbody,HashMap emailReplaceAttributes){
		StrSubstitutor substitutor = new StrSubstitutor(emailReplaceAttributes);
		emailbody = substitutor.replace(emailbody);
		return emailbody;
	}
	public UserInfo getUserInfo(String userID){
		//String email=null;
		//userRepository= Platform.getService(UserRepository.class);
		//usrInfo = Platform.getService(UserInfo.class);
		userRepository = new DBUserRepository();
		return usrInfo = userRepository.getUserAndManagerInfo(userID);
		
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
	public String sendEmailtoServiceNow(String emailTemplate, String toEmail,String Employee_type,String Employee_location,String Employee_supervisor,String Employee_Hire_date,
			String Employee_ID, String Employee_Name,String email,String jobTitle ){
		String result="failure";
		LOGGER.info( "Inside Service Now.");
		LOGGER.info( "Inside Service Now."+emailTemplate+toEmail+jobTitle+email+Employee_Name+Employee_ID+Employee_Hire_date+Employee_supervisor+Employee_location+Employee_type);

		HashMap emailReplaceAttributes = new HashMap(); 
		
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat df1 = new SimpleDateFormat("dd-MMM-yy");
		
		try {
			Date   date= df.parse(Employee_Hire_date);
			Employee_Hire_date=df1.format(date);
		} catch (ParseException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		emailReplaceAttributes.put("Employee_type",Employee_type);
		emailReplaceAttributes.put("Employee_location",Employee_location);
		emailReplaceAttributes.put("Employee_supervisor",Employee_supervisor);
		emailReplaceAttributes.put("Employee_Hire_date",Employee_Hire_date);
		emailReplaceAttributes.put("Employee_ID",Employee_ID);
		emailReplaceAttributes.put("Employee_Name",Employee_Name);
		emailReplaceAttributes.put("Email",email);
		emailReplaceAttributes.put("Job Title",jobTitle);
		emailReplaceAttributes.put("Supervisor Email",getEmailID(Employee_supervisor));
		
		
		LOGGER.info("Sending out Notification in notifyServiceNow");
		result =sendNotification(emailTemplate,emailReplaceAttributes,new String[]{toEmail},new String[]{"LST.NBK.IAMAdmin@ul.com"},new String[]{});
		
		return result;
		
	}

	public String getEmailID(String userID){
		 String email=null;
		  userRepository = new DBUserRepository();
	      usrInfo = userRepository.getUserAndManagerInfo(userID);
	     email=usrInfo.getUserEmailID();
		 
		 return email;
	 }
	public String getUserKey(String userID){
		 String email=null;
		  userRepository = new DBUserRepository();
	      usrInfo = userRepository.getUserAndManagerInfo(userID);
	     email=usrInfo.getUserKey();
		 
		 return email;
	 }
	
	private  String getProcessInstanceKey(String userId, String resourceName,tcUserOperationsIntf usrIntf) {
		String methodName="getProcessInstanceKey";
		LOGGER.info(classname+methodName+"getProcessInstanceKey() Enter into method");
		
		tcResultSet rset;
		String oikey="";
		
		String objectName=null;
		
		try {
			rset = usrIntf.getObjects(Long.parseLong(getUserKey(userId)));
			LOGGER.info(classname+methodName+"Row count is  --> " +rset.getRowCount());
			if(rset.getRowCount() > 0){
				// Loop through for each resource
				for (int i = 0; i < rset.getRowCount(); i++) {
					rset.goToRow(i);
					LOGGER.info("UpdateChildTable/getProcessInstanceKey() iterating ");
					objectName=rset.getStringValue("Objects.Name");
					LOGGER.info(classname+methodName+"Object name is  --> " +objectName);
					LOGGER.info(classname+methodName+"User login is  --> " +userId);
					if(resourceName.equalsIgnoreCase(objectName)) {
						oikey = rset.getStringValue("Process Instance.Key");
						LOGGER.info(classname+methodName+"Process Instance key is  --> " +oikey);
						break;
					}
				}
			}
		} catch (Exception tcfnf) {
			LOGGER.info("getProcessInstanceKey() error occured" + tcfnf.getMessage());
			tcfnf.printStackTrace();
			return "error";
		}finally{
			if(usrIntf!=null)
				usrIntf.close();
		}
		return oikey;
	}
    // Start of changes Made by Hina to stop enddating responsibilities and group membership in EBS and AD
/*	public void removeDataChildProcessForm(long processInstanceKey,String childFormPrimaryKey,tcFormInstanceOperationsIntf tcFormInOpsIntf)  {
		long processFormDefinitionKey=-1;
		try {
			processFormDefinitionKey = tcFormInOpsIntf.getProcessFormDefinitionKey(processInstanceKey);
			int processParentFormVersion =  tcFormInOpsIntf.getProcessFormVersion(processInstanceKey);
			tcResultSet childFormDef = tcFormInOpsIntf.getChildFormDefinition(processFormDefinitionKey,processParentFormVersion);
			long childKey = childFormDef.getLongValue("Structure Utility.Child Tables.Child Key");
			tcResultSet childData = tcFormInOpsIntf.getProcessFormChildData(childKey, processInstanceKey);
			
			if (childData.getRowCount()>0){
				LOGGER.info(classname+"removing data from child form for primary Key  --> " +childFormPrimaryKey);
			for (int i = 0; i < childData.getRowCount(); i++) {
				childData.goToRow(i);
				long rowKey = childData.getLongValue(childFormPrimaryKey);
				LOGGER.info(classname+"row key --> " +rowKey);
				tcFormInOpsIntf.removeProcessFormChildData(childKey, rowKey);
				
			}
			}
		} catch (tcAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcProcessNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcFormNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcNotAtomicProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcVersionNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcColumnNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcFormEntryNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (tcVersionNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(tcFormInOpsIntf!=null)
				tcFormInOpsIntf.close();
		}
		
	}*/
        
	/*public String insertToChildForm(String processInstKey, String childColumn,String lookupName,String childTableName,String lookupCodKey,tcFormInstanceOperationsIntf tcFormInOpsIntf){
		 String methodName="insertToChildForm() ";
		 String result="failure";
		 		 
		 try {
			 //tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
			 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
			 System.out.println(classname+methodName+parentFormDefKey);
			 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
			 System.out.println(classname+methodName+"active version "+activeVersion);
			 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
			 int i=rset.getRowCount();
			 if(i>0){
				 for(int j=0;j<i;j++){
					 rset.goToRow(j);
					 
					 String chtabName=rset.getStringValueFromColumn(2);
					 System.out.println(classname+methodName+" Table Name"+chtabName);
					 
					 if(chtabName.equalsIgnoreCase(childTableName)){
						 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
						 System.out.println(classname+methodName+ "childFormDefKey="+childFormDefKey);
						 
						 String decode= getLookupValue(lookupName,lookupCodKey);
						 String []resArray = decode.split(",");
						 
						 if (resArray !=null){
							 for(int k=0; k<resArray.length;k++){
								 HashMap responsibility= new HashMap();
								 responsibility.put(childColumn,resArray[k]);
								 System.out.println("responsibility Map "+responsibility);
								 tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),responsibility);
							 }
							 
						 }
						// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
						 result="success";
						 System.out.println(classname+methodName+"the final result "+result);
						 break;
					 }
				 }
			 }
			 
		 } catch (tcAPIException e) {
			 
			 e.printStackTrace();
		 } catch (NumberFormatException e) {
			
		 } catch (tcProcessNotFoundException e) {
			 
			 e.printStackTrace();
		 } catch (tcFormNotFoundException e) {
			
		 } catch (tcVersionNotDefinedException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 } catch (tcColumnNotFoundException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 } catch (tcRequiredDataMissingException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 } catch (tcInvalidValueException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 } catch (tcNotAtomicProcessException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }finally{
			 if(tcFormInOpsIntf!=null)
				 tcFormInOpsIntf.close();
		 }
		 
		 return result;
	}
	*/
	// End of changes Made by Hina to stop enddating responsibilities and group membership in EBS and AD
	public String getLookupValue(String lookupName, String lookupCodKey)
	{
		String methodName="getLookupValue() ";
		
		String lookupDecodeKey = null;
		
		try {
			
			
			tcResultSet tcresultSet = lookup.getLookupValues(lookupName);
			
			System.out.println( "Successfully got the Lookpup values.");
			
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
					System.out.println( methodName+"The value of Decodekey "
							+ lookupDecodeKey);
					
				}
				
			}
			
		}
		
		catch (tcAPIException e) {
			e.getMessage();
			
		}
		
		catch (tcInvalidLookupException e) {
			e.getMessage();
		} catch (tcColumnNotFoundException e) {
			e.getMessage();
		} finally {
			if (lookup != null) {
				
				lookup.close();
			}
		}
		
		return lookupDecodeKey;
	}

	 private String generateEmail(String preferredName, String firstName, String middleName, String lastName,String ADServer)
	 {
		 String email=null;
		 LOGGER.info("Inside GenerateMail ");
		 
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
			 LOGGER.info(tokens[i]);
			 if(i == 0)
			 {
				 InitialsOfFirstName=tokens[i].substring(0,1).toUpperCase();
			 }
			 else
			 {
				 InitialsOfFirstName=InitialsOfFirstName + tokens[i].substring(0,1).toUpperCase();
			 }
			 
		 }
		 LOGGER.info("InitialsOfFirstName"+InitialsOfFirstName);
		 
		 if(!isNullOrEmpty(preferredName))
		 {
			 
			 
			 email = modifiedPreferredName+"."+modifiedlastName+"@ul.com";
			 LOGGER.info("Email  is: " +email);
			 if( getUserLogin(email,ADServer) != 0)
			 {	
				 LOGGER.info("GenerateEmail Case 1");
				 
				 return email; 
			 }
			 else{
				 if(!isNullOrEmpty(middleName))
				 {
					 LOGGER.info("Inside GenerateMail Empty Middle name");
					 
					 email = modifiedPreferredName+"."+modifiedmiddleName+"."+modifiedlastName+"@ul.com";
					 if( getUserLogin(email,ADServer) != 0)
					 {
						 return email;  
					 }
				 }	
				 else
				 {
					 LOGGER.info("GenerateEmail Case 3");
					 
					 email = modifiedPreferredName+"."+InitialsOfFirstName+"."+modifiedlastName+"@ul.com";
					 if( getUserLogin(email,ADServer) != 0)
					 {
						 LOGGER.info("GenerateEmail Case 3.5");
						 
						 return email;  
					 }
					 
				 }
			 }   
		 }
		 
		 email = modifiedfirstName+"."+modifiedlastName+"@ul.com";
		 LOGGER.info("Email  is: " +email);
		 if( getUserLogin(email,ADServer) != 0)
		 {	
			 LOGGER.info("GenerateEmail Case 4");
			 
			 return email;  
		 }
		 else{
			 if(!isNullOrEmpty(middleName))
			 {
				 
				 email = modifiedfirstName+"."+modifiedmiddleName+"."+modifiedlastName+"@ul.com";
				 if( getUserLogin(email,ADServer) != 0)
				 {
					 LOGGER.info("GenerateEmail Case 5");
					 
					 return email;  
					 
				 }
			 }   
		 }
		 if(!isNullOrEmpty(preferredName))
		 {
			 
			 
			 email = modifiedPreferredName+".X."+modifiedlastName+"@ul.com";
			 LOGGER.info("Email  is: " +email);
			 LOGGER.info("GenerateEmail Case 6");
			 
			 if( getUserLogin(email,ADServer) != 0)
			 {	
				 LOGGER.info("GenerateEmail Case 7");
				 
				 return email; 
			 }
			 else{
				 email = modifiedPreferredName+".X1."+modifiedlastName+"@ul.com";
				 if( getUserLogin(email,ADServer) != 0)
				 {
					 LOGGER.info("GenerateEmail Case 8");
					 
					 return email;  
					 
				 }
				 else{
					 email = modifiedPreferredName+".X2."+modifiedlastName+"@ul.com";
					 if( getUserLogin(email,ADServer) != 0)
					 {
						 LOGGER.info("GenerateEmail Case 9");
						 
						 return email;  
						 
					 }	
				 }
			 }   
		 }
		 
		 email = modifiedfirstName+".X."+modifiedlastName+"@ul.com";
		 LOGGER.info("Email  is: " +email);
		 if( getUserLogin(email,ADServer) != 0)
		 {	
			 return email;  
		 }
		 else{
			 
			 email = modifiedfirstName+".X1."+modifiedlastName+"@ul.com";
			 if( getUserLogin(email,ADServer) != 0)
			 {
				 return email;  
				 
			 }
			 else{
				 email = modifiedfirstName+".X2."+modifiedlastName+"@ul.com";
				 if( getUserLogin(email,ADServer) != 0)
				 {
					 return email;  
					 
				 }
				 
			 }
			 for (int i=3;i<50;i++)
			 {
				 email = modifiedfirstName+".X"+i+"."+modifiedlastName+"@ul.com";
				 if( getUserLogin(email,ADServer) != 0)
				 {
					 return email;  
					 
				 }
			 }
		 }   
		 
		 
		 
		 LOGGER.info("Returning Email: " +email);
		 LOGGER.info("GenerateEmail Email");
		 
		 return email ;
		 
	 }
	 
	 public boolean getUserDN(String email,String ADServer)
	 {
		 
		 Hashtable env = getLdapEnv(ADServer);
		 boolean isExists = false;
		 LOGGER.info("Inside GetUserDN");
		 Hashtable ADparams =  getITResParameterDetails("Active Directory");
		 
		 try {
			 LdapContext ctx = new InitialLdapContext(env,null);
			 SearchControls searchCtls = new SearchControls();
			 searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			 String searchFilter = "(proxyAddresses="+email+")";
			 LOGGER.info("Inside GetUserDN start");
			 
			 String returnedAtts[]={"distinguishedName"};
			 searchCtls.setReturningAttributes(returnedAtts);
			 //NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
			 NamingEnumeration answer = ctx.search(ADparams.get("Container").toString(), searchFilter, searchCtls);
			 LOGGER.info("Inside GetUserDN check Email"+ADparams.get("Container").toString());
			 isExists = answer.hasMoreElements();
			 
			 if(isExists == false)
			 {
				 LOGGER.info("Inside GetUserDN Inside check Email");
				 
				 String searchFilter1 = "(mail=" + email + ")";
				 SearchControls sc1 = new SearchControls();
				 sc1.setSearchScope(SearchControls.SUBTREE_SCOPE);
				 NamingEnumeration<javax.naming.directory.SearchResult> results1;
				 results1 = ctx.search(ADparams.get("Container").toString(), searchFilter1, sc1);
				 isExists = results1.hasMoreElements();
			 }
			 //Loop through the search results
			 
			 
		 } catch (NamingException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 
		 LOGGER.info("Return isExists");
		 
		 return isExists;
		 
		 
	 }
		//private Hashtable getLdapEnv(String adminid,String pwd,String adserver){
		private Hashtable getLdapEnv(String ADServer){
			    Hashtable env = new Hashtable();
		        Hashtable ADparams =  getITResParameterDetails(ADServer);
		        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		        //set security credentials, note using simple cleartext authentication
		        env.put(Context.SECURITY_AUTHENTICATION,"simple");
		        
		        //env.put(Context.SECURITY_PRINCIPAL,adminid);
		        env.put(Context.SECURITY_PRINCIPAL,ADparams.get("DirectoryAdminName"));
		        LOGGER.info("Admin Name"+ADparams.get("DirectoryAdminName")+"Pwd"+ADparams.get("DirectoryAdminPassword")+"LDAP Host="+"ldap://"+ADparams.get("LDAPHostName")+":389");
		        
		        //env.put(Context.SECURITY_CREDENTIALS,pwd);
		        env.put(Context.SECURITY_CREDENTIALS,ADparams.get("DirectoryAdminPassword"));
		                 
		        //connect to my domain controller
		        //env.put(Context.PROVIDER_URL,"ldap://uspocx882t.global.ul.org:389");
		        env.put(Context.PROVIDER_URL,"ldap://"+ADparams.get("LDAPHostName")+":389");
		        return env;
		    }
	     private int getUserLogin(String Email,String ADServer) {
	        Vector mvUsers = new Vector();
	        
	        Set<String> retAttrs = new HashSet<String>();
	        LOGGER.info("GetUserLogin");
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
	        	LOGGER.info("GetUserLogin 1");

	            List<User> users = userService.search(criteria, retAttrs, null);
	            if(users.size()!=0)
                {
			        LOGGER.info("GetUserLogin 2");

               		return 0;
               	}
		        else
		        {
			        LOGGER.info("GetUserLogin 3");

		        	//isExists=getUserDN("SVC.UL.IAMDEV","!dentity&AM","uspocx882t.global.ul.com",Email,"DC=global,DC=ul,DC=org");
			        isExists=getUserDN(Email,ADServer);
		        	if (isExists == false)
		        	{
				        LOGGER.info("GetUserLogin 4");

		        		return 1;
		        	}
		        	else
		        	{
				        LOGGER.info("GetUserLogin 5");

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
	     
	     public Hashtable getITResParameterDetails(String sITRes) 
	     {
	    	 
	    	 String methodName = "::getITResParameterDetails(String sITRes)::";
	    	 LOGGER.info(classname + methodName+ " ENTERING ");
	    	 
	    	 long lSvrKey = -1;
	    	 Hashtable hITResParam = new Hashtable();
	    	 
	    	 //logger.info(classname + methodName + "Enters for extracting details on IT Resource Name: " + sITRes);
	    	 try {
	    		 HashMap hashMap = new HashMap();
	    		 hashMap.put("IT Resources.Name", sITRes);
	    		 
	    		 tcResultSet tcresultset = itResourceIntf.findITResourceInstances(hashMap);
	    		 //logger.info(classname + "Size " + tcresultset.getRowCount());
	    		 /*if (tcresultset != null) {
	    		  logger.info(classname + methodName + "Total number of rows found for IT resource " + sITRes + " is: " + tcresultset.getRowCount());
	    		  } */
	    		 
	    		 tcresultset.goToRow(0);
	    		 lSvrKey = tcresultset.getLongValue("IT Resources.Key");
	    		 hITResParam.put("IT Resources.Key", "" + lSvrKey);
	    		 
	    		 tcresultset = itResourceIntf.getITResourceInstanceParameters(lSvrKey);
	    		 
	    		 if (tcresultset != null) {
	    			 //logger.info(classname+ methodName+ "Total number of rows found for IT resource parameter against " + sITRes + " is: " + tcresultset.getRowCount());
	    		 } else {
	    			 LOGGER.info(classname + methodName + "No rows found for IT resource parameter against "+ sITRes);
	    		 }
	    		 for (int i = 0; i < tcresultset.getRowCount(); i++) {
	    			 tcresultset.goToRow(i);
	    			 String paramName = tcresultset.getStringValue("IT Resources Type Parameter.Name");
	    			 String paramValue = tcresultset .getStringValue("IT Resources Type Parameter Value.Value");
	    			 hITResParam.put(paramName.trim(), paramValue.trim());
	    		 }
	    	 } catch ( Exception e ) {
	    		 LOGGER.info(classname+ methodName + "tcAPIException occured while retriving data from IT-Resource "+ sITRes);
	    	 }
	    	 LOGGER.info(classname + methodName+ " EXITING ");
	    	 return hITResParam;
	     }
	     
	     public boolean isNullOrEmpty(String s) {
	    	 return s == null || s.trim().length() == 0;
	     }
	     
	     private void updateEBSDate(String processInstKey,String startDate,tcFormInstanceOperationsIntf tcFormInOpsIntf){
	    	 LOGGER.info(classname + "updateEBSDate() Starting ");
	    	 SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	    	 try {
	    		 Date dt = df.parse(startDate);
	    		 Map hm = new HashMap();
	    		 hm.put("UD_EBS_USER_EFFDATEFROM",dt);
	    		 tcFormInOpsIntf.setProcessFormData(Long.parseLong(processInstKey),hm);
	    	 } catch (ParseException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (NumberFormatException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (tcAPIException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (tcInvalidValueException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (tcNotAtomicProcessException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (tcFormNotFoundException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (tcRequiredDataMissingException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 } catch (tcProcessNotFoundException e) {
	    		 // TODO Auto-generated catch block
	    		 e.printStackTrace();
	    	 }
	     }
	     
	     /**
	 	 * @param lookupOperationsIntf
	 	 * @param lookupName
	 	 * This method will remove all the lookup entries after all the entries are processed 
	 	 * @return
	 	 */
	 	public boolean removeLookupValues(tcLookupOperationsIntf lookupOperationsIntf, String lookupName) {
	 		String methodName = ":: removeLookupValues ::";
	 		LOGGER.info(classname  + methodName + " Entering");
	 		boolean result = false;
	 		try {
	 			HashMap recordsMap = readLookupValues(lookupOperationsIntf,lookupName);
	 			if(recordsMap.size()>0){
	 				LOGGER.info(classname  + methodName + " Remove Attribute Lookup name "+lookupName);
	 				Iterator iterator = recordsMap.keySet().iterator();  
	 				while (iterator.hasNext()) {  
	 					String key = iterator.next().toString();  
	 					LOGGER.info(classname+ methodName +" Failed Records HASHMAP Key Value ::"+key);
	 					lookupOperationsIntf.removeLookupValue(lookupName, key);
	 				} 
	 			}else{
	 				LOGGER.info(classname+ methodName +" Nothing to Remove..Lookup is already empty");
	 			}
	 			result = true;
	 		} catch (tcAPIException e) {
	 			LOGGER.info(classname  + methodName +"tcAPIException Occurs "+e);
	 		} catch (tcInvalidLookupException e) {
	 			LOGGER.info(classname  + methodName +"tcInvalidLookupException Occurs "+e);
	 		} catch (tcInvalidValueException e) {
	 			LOGGER.info(classname  + methodName +"tcInvalidValueException Occurs "+e);
	 		} catch (Exception e){
	 			LOGGER.info(classname  + methodName +" General Exception Occurs "+e);
	 		}
	 		LOGGER.info(classname  + methodName + " Exiting");
	 		return result;
	 	}
             
             //Changes Made by hina to include check for user id
    public boolean removeLookupValues(tcLookupOperationsIntf lookupOperationsIntf, String lookupName, String uid) {
            String methodName = ":: removeLookupValues ::";
            LOGGER.info(classname  + methodName + " Entering");
            boolean result = false;
            try {
                    HashMap recordsMap = readLookupValues(lookupOperationsIntf,lookupName);
                    if(recordsMap.size()>0){
                            LOGGER.info(classname  + methodName + " Remove Attribute Lookup name "+lookupName);
                            Iterator iterator = recordsMap.keySet().iterator();  
                            while (iterator.hasNext()) {  
                                    String key = iterator.next().toString();  
                                    LOGGER.info(classname+ methodName +" Failed Records HASHMAP Key Value ::"+key);
                                if(uid.equalsIgnoreCase(key.toString())) {
                                    lookupOperationsIntf.removeLookupValue(lookupName, key);
                                }
                            } 
                    }else{
                            LOGGER.info(classname+ methodName +" Nothing to Remove..Lookup is already empty");
                    }
                    result = true;
            } catch (tcAPIException e) {
                    LOGGER.info(classname  + methodName +"tcAPIException Occurs "+e);
            } catch (tcInvalidLookupException e) {
                    LOGGER.info(classname  + methodName +"tcInvalidLookupException Occurs "+e);
            } catch (tcInvalidValueException e) {
                    LOGGER.info(classname  + methodName +"tcInvalidValueException Occurs "+e);
            } catch (Exception e){
                    LOGGER.info(classname  + methodName +" General Exception Occurs "+e);
            }
            LOGGER.info(classname  + methodName + " Exiting");
            return result;
    }

}



