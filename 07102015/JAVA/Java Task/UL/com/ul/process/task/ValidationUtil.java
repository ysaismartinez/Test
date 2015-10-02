/**
 * 
 */
package com.ul.process.task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

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
 * @author Zubair
 *
 */
public class ValidationUtil {

	/**
	 * 
	 */
	public ValidationUtil() {
		super();
		
	}
	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	String classname="ValidationUtil.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	UserRepository userRepository=null;
	UserInfo usrInfo=null;
	public final String SMTP_HOST_NAME= "smptp host name";
	public final String SMTP_PORT= "smptp port";
	public final String FROM_EMAIL_ADDRESS= "from email";
	
 public void sendNotification(String fname,String lname, String uid,String recordCreatedBy,String bodyAttr){
		
	 LOGGER.info("ValidationUtil.sendNotification() Entering the method");
		HashMap hm = new HashMap();
		hm.put("Employee_ID",uid);
		hm.put("First Name",fname);
		hm.put("Last Name",lname);
		hm.put("Attributes",bodyAttr);
		String hrEmail=null;
		String [] toList = null;
		hrEmail=getEmailID(recordCreatedBy);
		
		if(hrEmail!=null){
			
			toList=new String[] {hrEmail,"ULSDAP@unisys.com"};
			LOGGER.info("ValidationUtil.sendNotification() Hr email lsit "+toList);
			String result=sendNotification("Master Validation Template",hm,toList,new String[]{"LST.NBK.IAMAdmin@ul.com"},new String[]{});
			//LOGGER.info("ValidationUtil.sendNotification() email sending was "+result);
		}
		
	}
	
	private String sendNotification(String oimEmailDefinitionName, HashMap emailReplaceAttributes, String[] toList, String[] ccList,String[] bccList){
		 LOGGER.info("ValidationUtil.sendNotification() Entering into ");
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
		emailIntf = Platform.getService(tcEmailOperationsIntf.class);
		lookup = Platform.getService(tcLookupOperationsIntf.class);
				
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
			LOGGER.info("sendNotification() email sent successfully");
			
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
		
		if (toList!=null){
		addressTo = new InternetAddress[toList.length];
		
		for (int i = 0; i < toList.length; i++) {
			try {
				addressTo[i] = new InternetAddress(toList[i]);
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		return addressTo;
	}
	
	public String getLookupValue(String lookupName, String lookupCodKey)
	{
		String methodName="getLookupValue() ";
		lookup= Platform.getService(tcLookupOperationsIntf.class);
		
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
	
	public String getEmailID(String userID){
		String email=null;
		//userRepository= Platform.getService(UserRepository.class);
		//usrInfo = Platform.getService(UserInfo.class);
		userRepository = new DBUserRepository();
		usrInfo = userRepository.getUserAndManagerInfo(userID);
		email=usrInfo.getUserEmailID();
		
		return email;
	}
	public String populateLookup(String codeKey, String decodeKey, String lookupName) {
		String methodName = ":: populateLookupOnRun ::";
		LOGGER.info(classname  + methodName + " Entering");
		String result = "Error";
		try {
			LOGGER.info(classname  + methodName + " Code Key to Add :: " + codeKey  + " Decode Key to Add :: " + decodeKey + " in "+lookupName);
			lookup= Platform.getService(tcLookupOperationsIntf.class);
			lookup.addLookupValue(lookupName, codeKey, decodeKey, "", "");
			result = "Success";
		} catch (tcAPIException e) {
			e.printStackTrace();
			//LOGGER.error(classname  + methodName +"tcAPIException Occurs "+e);
		} catch (tcInvalidLookupException e) {
			e.printStackTrace();
			//LOGGER.error(classname  + methodName +"tcInvalidLookupException Occurs "+e);
		} catch (tcInvalidValueException e) {
			e.printStackTrace();
			//LOGGER.error(classname  + methodName +"tcInvalidValueException Occurs "+e);
		} catch (Exception e){
			e.printStackTrace();
			//LOGGER.error(classname  + methodName +" General Exception Occurs "+e);
		}finally{
			if (lookup!=null)
				lookup.close();
		}
		LOGGER.info(classname  + methodName + " Exiting");
		return result;
	}
	
	public String getReminderDate(){
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Now use today date.
		c.add(Calendar.DATE, 2); // Adding 2 days
		String output = sdf.format(c.getTime());
		System.out.println(output);
		
		return output;
		
	}
	


}
