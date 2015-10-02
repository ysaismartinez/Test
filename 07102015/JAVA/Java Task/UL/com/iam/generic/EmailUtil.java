package com.iam.generic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.Platform;

import org.apache.commons.lang.text.StrSubstitutor;

import Thor.API.tcResultSet;
import Thor.API.tcUtilityFactory;
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
import Thor.API.Operations.tcLookupOperationsIntf;

import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataaccess.tcDataSetException;



public class EmailUtil {

	public EmailUtil() {
		 emailIntf = Platform.getService(tcEmailOperationsIntf.class);
		 lookup = Platform.getService(tcLookupOperationsIntf.class);
		 //userRepository=Platform.getService(UserRepository.class);
		 //usrInfo=Platform.getService(UserInfo.class);
	}
	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	String classname="EmailUtil.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	UserRepository userRepository=null;
	UserInfo usrInfo=null;
	public final String SMTP_HOST_NAME= "smptp host name";
	public final String SMTP_PORT= "smptp port";
	public final String FROM_EMAIL_ADDRESS= "from email";
	
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
		} finally{
			if(emailIntf !=null)
				emailIntf.close();
		}

		
		
		return result;
	}
	
	public String notifyLocalITGroups(String emailTemplate, String emailListLookup,tcDataProvider tcdp,String userID,String location){
		String result="failure";
		tcDataSet tcdataset = new tcDataSet();
		String password="";
		HashMap emailReplaceAttributes = new HashMap();
		String strDate=null;
		Date startDate= new Date();
		
		tcdataset.setQuery(tcdp,"select usr_password from usr where usr_login='"+userID.toUpperCase()+"'");
		try {
			tcdataset.executeQuery();
			if (tcdataset.getRowCount()>0){
				tcdataset.goToRow(0);
				password=tcdataset.getString("usr_password");
				System.out.println("usr_pwd "+password);
				SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	        	strDate =df.format(startDate);
			}
			emailReplaceAttributes.put("password",password);
			emailReplaceAttributes.put("id",userID);
			emailReplaceAttributes.put("date",strDate);
			
			String adminEmailList =getLookupValue(emailListLookup,location);
			String []toList=adminEmailList.split(",");
			System.out.println("Sending out Notification");
			result =sendNotification(emailTemplate,emailReplaceAttributes,toList,new String[]{"LST.NBK.IAMAdmin@ul.com"},new String[]{});
			
		} catch (tcDataSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		
	}
	/**
	 * 
	 * @param emailTemplate
	 * @param toEmail
	 * @param Employee_type
	 * @param Employee_location
	 * @param Employee_supervisor
	 * @param Employee_Hire_date
	 * @param Employee_ID
	 * @param Employee_Name
	 * @param email
	 * @param jobTitle
	 * @return
	 */
	public String notifyServiceNow(String emailTemplate, String toEmail,String Employee_type,String Employee_location,String Employee_supervisor,String Employee_Hire_date,
			String Employee_ID, String Employee_Name,String email,String jobTitle ){
		String result="failure";
		
		HashMap emailReplaceAttributes = new HashMap(); 
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		try {
			Date   date= df.parse(Employee_Hire_date);
			Employee_Hire_date=df.format(date);
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
		
		
		System.out.println("Sending out Notification in notifyServiceNow");
		result =sendNotification(emailTemplate,emailReplaceAttributes,new String[]{toEmail},new String[]{"LST.NBK.IAMAdmin@ul.com"},new String[]{});
		
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
	
	/**
	 * 
	 * @param lookupName
	 * @param lookupCodKey
	 * @return
	 */
	public String getLookupValue(String lookupName, String lookupCodKey) {

		String methodName = "getLookupValue() ";

		String lookupDecodeKey = null;

		try {

			tcResultSet tcresultSet = lookup.getLookupValues(lookupName);

			System.out.println("Successfully got the Lookpup values.");

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
					System.out.println(methodName + "The value of Decodekey "
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

 public HashMap getResponsibilityMap(String lookupName, String lookupCodKey,String childColumn){
	 HashMap responsibility= new HashMap();
	 String decode= getLookupValue(lookupName,lookupCodKey);
	 String []resArray = decode.split(",");
	 
	 if (resArray !=null){
		 for(int i=0; i<resArray.length;i++){
			 responsibility.put(childColumn,resArray[i]);
		 }
		 System.out.println("responsibility Map "+responsibility);
	 }
	 
	 return responsibility;
 }

 public String insertToChildForm(String processInstKey, String childColumn,String lookupName,String childTableName,String lookupCodKey,tcDataProvider tcdp){
	 String methodName="insertToChildForm() ";
	 String result="failure";
	 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
	// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
	 
	 try {
		 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
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
		 if (tcFormInOpsIntf !=null)
			 tcFormInOpsIntf.close();
	 }
	 
	 return result;
}
 
 public String getEmailID(String userID){
	 String email=null;
	  userRepository = new DBUserRepository();
      usrInfo = userRepository.getUserAndManagerInfo(userID);
     email=usrInfo.getUserEmailID();
	 
	 return email;
 }
 /**
	 * 
	 * @param emailTemplate
	 * @param toEmail
	 * @param Employee_type
	 * @param Employee_location
	 * @param Employee_supervisor
	 * @param Employee_Hire_date
	 * @param Employee_ID
	 * @param Employee_Name
	 * @param email
	 * @param jobTitle
	 * @return
	 */
	public String sendEmailtoServiceNow(String emailTemplate, String toEmail,String Employee_type,String Employee_location,String Employee_supervisor,String Employee_Hire_date,
			String Employee_ID, String Employee_Name,String email,String jobTitle ){
		String result="failure";
		LOGGER.info( "Inside Service Now.");
		LOGGER.info( "Inside Service Now."+emailTemplate+toEmail+jobTitle+email+Employee_Name+Employee_ID+Employee_Hire_date+Employee_supervisor+Employee_location+Employee_type);

		HashMap emailReplaceAttributes = new HashMap(); 
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
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
	public String deleteFromChildForm(String processInstKey, String childColumn,String lookupName,String childTableName,String lookupCodKey,tcDataProvider tcdp){
		 String methodName="insertToChildForm() ";
		 String result="failure";
		 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
		// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
		 
		 try {
			 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
			 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
			 LOGGER.info(classname+methodName+parentFormDefKey);
			 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
			 LOGGER.info(classname+methodName+"active version "+activeVersion);
			 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
			 int i=rset.getRowCount();
			 if(i>0){
				 for(int j=0;j<i;j++){
					 rset.goToRow(j);
					 
					 String chtabName=rset.getStringValueFromColumn(2);
					 LOGGER.info(classname+methodName+" Table Name"+chtabName);
					 

					 if(chtabName.equalsIgnoreCase(childTableName)){
						 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
		                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
		                 String decode= getLookupValue(lookupName,lookupCodKey);
						 String []resArray = decode.split(",");
						 LOGGER.info(classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
						 for (int m = 0 ; m < childFormData.getRowCount();m++)  
	                     {  
	                       childFormData.goToRow(m);
	                       LOGGER.info("Inside Delete");
	     				
	                         long childFormKey = childFormData.getLongValue("UD_EBS_RESP_KEY"); 
	                         if (resArray !=null){
	      						 for(int k=0; k<resArray.length;k++){
	      							LOGGER.info("Inside innermostloop delete resp"+childFormData.getStringValue(childColumn)+resArray[k]);
	      							 if (childFormData.getStringValue(childColumn).equalsIgnoreCase(resArray[k]))
	      							 {
	      		                       LOGGER.info("Inside innermostloop delete resp");

	      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
	          							 tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
	      							 }
	      		                       
	      						 }
	      						 
	      					 }
	                         
	                     }  
						
						// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
						 result="success";
						 LOGGER.info(classname+methodName+"the final result "+result);
						 break;
					 }
				 }
			 }
			 
		 } catch (Exception e) {
			 
			 e.printStackTrace();
		 } 	 
		 return result;
	}

	 public String deleteFromADChildForm(String processInstKey,String childTableName,tcDataProvider tcdp){
		 String methodName="insertToChildForm() ";
		 String result="failure";
		 tcFormInstanceOperationsIntf tcFormInOpsIntf = null;
		// HashMap<String,String> processDataMap= getResponsibilityMap(lookupName,lookupCodKey,childColumn);
		 
		 try {
			 tcFormInOpsIntf = (tcFormInstanceOperationsIntf)tcUtilityFactory.getUtility(tcdp, "Thor.API.Operations.tcFormInstanceOperationsIntf");
			 long parentFormDefKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstKey));
			 LOGGER.info(classname+methodName+parentFormDefKey);
			 int activeVersion = tcFormInOpsIntf.getActiveVersion(parentFormDefKey);
			 LOGGER.info(classname+methodName+"active version "+activeVersion);
			 tcResultSet rset = tcFormInOpsIntf.getChildFormDefinition(parentFormDefKey,activeVersion);
			 int i=rset.getRowCount();
			 if(i>0){
				 for(int j=0;j<i;j++){
					 rset.goToRow(j);
					 
					 String chtabName=rset.getStringValueFromColumn(2);
					 LOGGER.info(classname+methodName+" Table Name"+chtabName);
					 if(chtabName.equalsIgnoreCase(childTableName)){
						 String childFormDefKey = rset.getStringValue("Structure Utility.Child Tables.Child Key");
		                 tcResultSet childFormData = tcFormInOpsIntf.getProcessFormChildData(Long.parseLong(childFormDefKey) ,Long.parseLong(processInstKey));  
		        		 LOGGER.info(classname+methodName+ "childFormDefKey and LOng key ="+childFormDefKey);
						 for (int m = 0 ; m < childFormData.getRowCount();m++)  
	                     {  
	                       childFormData.goToRow(m);
	                       LOGGER.info("Inside Delete");
	                       	long childFormKey = childFormData.getLongValue("UD_ADUSRC_KEY"); 
	                         		LOGGER.info("Inside innermostloop delete resp");
	      								//String plChildTableName = rset.getStringValue(“Structure Utility.Table Name”);  
	          							 tcFormInOpsIntf.removeProcessFormChildData(Long.parseLong(childFormDefKey),childFormKey);
	                         
	                     }  
						
						// tcFormInOpsIntf.addProcessFormChildData(Long.parseLong(childFormDefKey), Long.parseLong(processInstKey),processDataMap);
						 result="success";
						 LOGGER.info(classname+methodName+"the final result "+result);
						 break;
					 }
				 }
			 }
			 
		 } catch (Exception e) {
			 
			 e.printStackTrace();
		 } 	 
		 return result;
	}
	 
	 public String isUserEmployee(String empType){
		 String response="not_employee";
		 if (empType.equalsIgnoreCase("Employee")){
			 response="employee";
		 }
		 return response;
	 }
	 
	 public String removeDataChildProcessForm(String processInstanceKey,String childFormPrimaryKey)  {
		 String response="failure";
		 
			long processFormDefinitionKey=-1;
			tcFormInstanceOperationsIntf tcFormInOpsIntf=null;
			try {
				tcFormInOpsIntf=Platform.getService(tcFormInstanceOperationsIntf.class);
				processFormDefinitionKey = tcFormInOpsIntf.getProcessFormDefinitionKey(Long.parseLong(processInstanceKey));
				int processParentFormVersion =  tcFormInOpsIntf.getProcessFormVersion(Long.parseLong(processInstanceKey));
				tcResultSet childFormDef = tcFormInOpsIntf.getChildFormDefinition(processFormDefinitionKey,processParentFormVersion);
				long childKey = childFormDef.getLongValue("Structure Utility.Child Tables.Child Key");
				tcResultSet childData = tcFormInOpsIntf.getProcessFormChildData(childKey, Long.parseLong(processInstanceKey));
				
				if (childData.getRowCount()>0){
					LOGGER.info(classname+"removing data from child form for primary Key  --> " +childFormPrimaryKey);
				for (int i = 0; i < childData.getRowCount(); i++) {
					childData.goToRow(i);
					long rowKey = childData.getLongValue(childFormPrimaryKey);
					LOGGER.info(classname+"row key --> " +rowKey);
					tcFormInOpsIntf.removeProcessFormChildData(childKey, rowKey);
					response="success";
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
			}finally{
				if (tcFormInOpsIntf!=null)
					tcFormInOpsIntf.close();
			}
			return response;
		}
   
	 	 

   
}
