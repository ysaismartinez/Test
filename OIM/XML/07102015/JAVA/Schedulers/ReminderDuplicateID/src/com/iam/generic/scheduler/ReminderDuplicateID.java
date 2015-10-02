package com.iam.generic.scheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.text.StrSubstitutor;



import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.Platform;
import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;


/**
 * @author Zubair Khan 
 * 
 */
public class ReminderDuplicateID extends oracle.iam.scheduler.vo.TaskSupport {
	
	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	String classname="ReminderDuplicateID.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	UserRepository userRepository=null;
	UserInfo usrInfo=null;
	public final String SMTP_HOST_NAME= "smptp host name";
	public final String SMTP_PORT= "smptp port";
	public final String FROM_EMAIL_ADDRESS= "from email";
	
	/*
	 * Scheduled task entry point
	 */
	public void execute(HashMap taskparams) {
		
		LOGGER.info("Starting ReminderDuplicateID Scheduled Task ");
		
		lookup= Platform.getService(tcLookupOperationsIntf.class);
		//userRepository=Platform.getService(UserRepository.class);
		//usrInfo = Platform.getService(UserInfo.class);
		emailIntf = Platform.getService(tcEmailOperationsIntf.class);
		String lookupName= (String) taskparams.get("Lookup Name");
		String emailTemp = (String) taskparams.get("Email Template");
		LOGGER.info("lookupName "+lookupName);
		LOGGER.info("emailTemp "+emailTemp);
		HashMap<String,String> reminderDetMap = readLookupValues(lookup,lookupName);
		LOGGER.info("Lookup Map "+reminderDetMap);
		
		if (reminderDetMap!=null && reminderDetMap.size()>0){
			for (Entry<String, String> entry : reminderDetMap.entrySet()){
				//LOGGER.info("Inside for Loop ");
				String uid= entry.getKey();
				String decodeValue= entry.getValue();
				if (decodeValue!=null ){
					//LOGGER.info("Inside if and for ");
					String [] datenHrUid = decodeValue.split(",");
					String reminderDate = datenHrUid[0];
					String hrUID = datenHrUid[1];
					String attr= datenHrUid[2];
					//LOGGER.info(" reminderDate"+reminderDate+" "+hrUID);
					if (noOfDaysSinceCreation(reminderDate)==0){
						LOGGER.info("Now the reminder email would be sent");
						sendNotification(uid,hrUID,emailTemp,attr);
					}
				}
			}
			removeLookupValues(lookup,lookupName);
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
	private void sendNotification(String uid,String recordCreatedBy,String emailTemplate,String bodyAttr){
		
		LOGGER.info("Staring sendNotification");
		HashMap hm = new HashMap();
		UserInfo userinfo = getUserInfo(uid);
		hm.put("Employee_ID",uid);
		hm.put("First Name",userinfo.getFirstName());
		hm.put("Last Name",userinfo.getLastName());
		hm.put("Attributes",bodyAttr);
		//Lookup.Email.ITAdmin
		//EmailUtil email = new EmailUtil();
		//String emailList=email.getLookupValue("Lookup.Email.ITAdmin","hr");
		String hrEmail=getUserInfo(recordCreatedBy).getUserEmailID();
		String [] toList= {hrEmail,"ULSDAP@unisys.com"};
		LOGGER.info("Hr email lsit "+toList);
		sendNotification(emailTemplate,hm,toList,new String[]{},new String[]{});
	}
	
	public String sendNotification(String oimEmailDefinitionName, HashMap emailReplaceAttributes, String[] toList, String[] ccList,String[] bccList){
		System.out.println("Staring sendNotification()");
		String result="failure";
		String body=null;
		String substitutedBody=null;
		String subject=null;
		String substitutedSubject=null;
		tcResultSet rs= null;
		boolean debug = false;
		HashMap<String,String> emailProps = new HashMap<String, String>();
		String fromEmailToUse = null;
		HashMap hm = new HashMap();
		hm.put("emd_name", oimEmailDefinitionName);
		//emailIntf = Platform.getService(tcEmailOperationsIntf.class);
		//lookup = Platform.getService(tcLookupOperationsIntf.class);
		// UserRepository userRepository=Platform.getService(UserRepository.class);
		// UserInfo usrInfo=Platform.getService(UserInfo.class);
		
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

	
}
