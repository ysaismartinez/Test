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

//import org.apache.commons.lang.text.StrSubstitutor;

import org.apache.commons.lang3.text.StrSubstitutor;







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
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;  
import oracle.iam.identity.usermgmt.vo.User;  
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.OIMInternalClient;
import oracle.iam.platform.Platform;  
import oracle.iam.request.vo.Beneficiary;  
import oracle.iam.request.vo.RequestData;  
import oracle.iam.platform.Platform;
import oracle.iam.scheduler.vo.TaskSupport;

public class SchedulerNotifyLocalITGroups  extends TaskSupport {

	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	private OIMClient oimClient = null;
	String classname="ScheduleNotifyLocalITGroups.";
	tcEmailOperationsIntf emailIntf=null;
	tcLookupOperationsIntf lookup =null;
	
	
	//UserRepository userRepository=null;
//	UserInfo usrInfo=null;
	public final String SMTP_HOST_NAME= "smptp host name";
	public final String SMTP_PORT= "smptp port";
	public final String FROM_EMAIL_ADDRESS= "from email";
	
	public void execute(HashMap hm) throws Exception 
    {
		String methodName="execute";

		String plainTextPassword = null;
		String userLogin = null;
		String location = null;
	    tcDataProvider dbProvider = null;
	    //New Addition
		String toAddress = ((String)hm.get("IAM Admin Group Email Address"));
	    String numOfDays = (String) hm.get("Number Of Days");
	    //
		HashMap emailReplaceAttributes = new HashMap();
		 emailIntf = Platform.getService(tcEmailOperationsIntf.class);
		 lookup = Platform.getService(tcLookupOperationsIntf.class);

	    oracle.iam.platform.OIMInternalClient localClient = new OIMInternalClient();
		//OIMInternalClient localClient = new OIMInternalClient();
        LOGGER.info(classname + methodName+ "----->>>>        " + "Inside Schedule Task ");
		LOGGER.info(classname + methodName+ "----->>>>        " + "Initiating signature based login to OIM");

		//localClient.signatureLogin("UBSUser3");
		localClient.signatureLogin("xelsysadm");
		//localClient.signatureLogin("Tuser5");
		
		//localClient.login("xelsysadm", "Welcome1".toCharArray());         //Update password of Admin with your environment password
         
		oimClient = localClient;
		LOGGER.info(classname + methodName+ "----->>>>        " + "After Authentication");

	    //Needed for database client
	     XLClientSecurityAssociation.setClientHandle(oimClient);
	     //Connection to OIM Schema
	     dbProvider = new tcDataBaseClient(); 
	     //Stores the result set of an executed query
	     tcDataSet dataSet = new tcDataSet(); 

	 	LOGGER.info(classname + methodName+ "----->>>>        " + "db op");
	 	LOGGER.info(classname + methodName+ "----->>>>        " + "Before Executing Query");
		 //Query Users table to get a single user
	 	//NEW QUERY
	 	//String query ="select usr_login,usr_password,usr_udf_usr_location  from ((select usr_login,usr_password,usr_udf_usr_location from (SELECT upa_usr.usr_key,upa_usr.usr_login,changed_by_user,usr.usr_password,usr.usr_udf_usr_location,upa_usr.upa_usr_eff_from_date AS changed_time,to_char(upa_usr.upa_usr_eff_from_date,'DD-MON-YY') as Enable_Date,field_name,field_old_value,field_new_value FROM upa_usr, upa_fields,usr,(SELECT field_new_value AS changed_by_user FROM upa_fields WHERE upa_fields_key =(SELECT MAX(upa_fields_key) FROM upa_fields, upa_usr  WHERE upa_usr.upa_usr_key = upa_fields.upa_usr_key AND upa_fields.field_name ='Users.Updated By Login' ) ) WHERE upa_usr.upa_usr_key = upa_fields.upa_usr_key AND upa_usr.usr_key = usr.usr_key and FIELD_OLD_VALUE = 'Disabled'and FIELD_NEW_VALUE = 'Active') where Enable_Date like (select sysdate from dual)) union (select usr_login, usr_password,usr_udf_usr_location  from USR where USR_CREATE LIKE (select sysdate from dual) ))";
	 	
	 	String query = "SELECT usr.usr_login,usr.usr_password,usr.usr_udf_usr_location\r\n" + 
	 			"FROM usr usr,\r\n" + 
	 			"  act act,\r\n" + 
	 			"  oiu oiu,\r\n" + 
	 			"  obi obi,\r\n" + 
	 			"  obj obj,\r\n" + 
	 			"  ost ost\r\n" + 
	 			//"WHERE TRUNC(usr_create) =TRUNC (sysdate - "+numOfDays+")\r\n" + 
	 			"WHERE TRUNC(oiu_create) =TRUNC (sysdate - "+numOfDays+")\r\n" +  //changes made by Hina on 05122015
	 			"AND usr.act_key            = act.act_key\r\n" + 
	 			"AND act_name LIKE 'Internal User'\r\n" + 
	 			"AND oiu.usr_key = usr.usr_key\r\n" + 
	 			"AND obi.obi_key =oiu.obi_key\r\n" + 
	 			"AND obi.obj_key =obj.obj_key \r\n" + 
	 			"AND ost.ost_key=oiu.ost_key\r\n" + 
	 			"AND obj.obj_name LIKE 'AD User'\r\n" + 
	 			"AND ost.ost_status in ('Enabled','Provisioned')";
	 			
	 	//String query ="select usr_login,usr_password,usr_udf_usr_location  from ((select usr_login,usr_password,usr_udf_usr_location from (SELECT upa_usr.usr_key,upa_usr.usr_login,changed_by_user,usr.usr_password,usr.usr_udf_usr_location,upa_usr.upa_usr_eff_from_date AS changed_time,to_char(upa_usr.upa_usr_eff_from_date,'DD-MON-YY') as Enable_Date,field_name,field_old_value,field_new_value FROM upa_usr, upa_fields,usr,(SELECT field_new_value AS changed_by_user FROM upa_fields WHERE upa_fields_key =(SELECT MAX(upa_fields_key) FROM upa_fields, upa_usr  WHERE upa_usr.upa_usr_key = upa_fields.upa_usr_key AND upa_fields.field_name ='Users.Updated By Login' ) ) WHERE upa_usr.upa_usr_key = upa_fields.upa_usr_key AND upa_usr.usr_key = usr.usr_key and FIELD_OLD_VALUE = 'Disabled'and FIELD_NEW_VALUE = 'Active') where Enable_Date like (select sysdate from dual)-1) union (select usr_login, usr_password,usr_udf_usr_location  from USR where USR_CREATE LIKE (select sysdate from dual)-1 ))";
	 	//
	     //String query = "select usr_login, usr_password , usr_udf_usr_location from USR where USR_CREATE LIKE (select sysdate from dual)"; 
	     //Set query and database provider     
	     dataSet.setQuery(dbProvider, query); 
	     //execute query and store results into dataSet object
	     dataSet.executeQuery(); 

	     LOGGER.info(classname + methodName+ "----->>>>        " + "Done Execute Query ");
	 	
	     //Get total records from result set
	     int records = dataSet.getTotalRowCount(); 
	     Map<String, String> userMap = new HashMap<String, String>();
	     for(int i = 0; i < records; i++) 
	     {
	       String str= null;
	       String strNew  = null;
	       String strCombined = null;
		   dataSet.goToRow(i); //move pointer to next record
	       plainTextPassword = dataSet.getString("USR_PASSWORD");
           userLogin = dataSet.getString("USR_LOGIN");
           location = dataSet.getString("USR_UDF_USR_LOCATION");
           //New Code
           str =    "<tr>"  
        		   + "<td>"
        		   + userLogin
        		   + "</td>" 
        		   + "<td>" 
        		   + plainTextPassword
        		   + "</td>"
        		   + "</tr>";
           //
           /*str = userLogin + 
        		  "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" 
        		   + plainTextPassword
        		   + "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	       */
           LOGGER.info(classname + methodName+ "----->>>>        " +  "user login: pwd and location" + userLogin  + plainTextPassword + location );
	     	     //strNew = map.get(location);
	       		// if (strNew == null)
	       		if(!userMap.containsKey(location))
			     {
	       			str = "<table border=\"1\"><tr><th>ID</th><th>Password</th></tr>"+str;
			    	 userMap.put(location, str);
			    	LOGGER.info(classname + methodName+ "----->>>>        " +  "NEW string" + str );
			    	LOGGER.info(classname + methodName+ "----->>>>        " +  "HashuserMap" + userMap.get(location));
				   	
			     }
			     else
			     {
			    	 strNew = userMap.get(location);
			    	 strCombined = strNew + str;
			    	 userMap.put(location, strCombined);
			    	 LOGGER.info(classname + methodName+ "----->>>>        " +  "Combined string" + strCombined );
				 }
			     
	     }     
	    // notifyLocalITGroups("IT Group Email Template","Lookup.Email.ITAdmin",dbProvider,)
	
	     //Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator() ;
	     	Set keys = userMap.keySet();
	        Iterator itr = keys.iterator();
	        String key;
	        String value;
	        while(itr.hasNext())
	        {
	 //       while(iterator.hasNext()){
	            //Map.Entry<String, String> locationEntry = iterator.next();
	           // LOGGER.info(classname + methodName+ "----->>>>        " + " :: "+ locationEntry.getValue());
	        	 key = (String)itr.next();
	        	 LOGGER.info(classname + methodName+ "----->>>>        " + "key "+key );
		    	 value = (String)userMap.get(key);
		    	 LOGGER.info(classname + methodName+ "----->>>>        " + "key "+key +" Value " +value);
		    	 value = value + "</table>";
	    	    //emailReplaceAttributes.put("id",locationEntry.getValue());
	            emailReplaceAttributes.put("id",value);
	            Date startDate= new Date();
	    		String strDate=null;
	            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	        	strDate =df.format(startDate);
	        	emailReplaceAttributes.put("date",strDate);
				LOGGER.info(classname + methodName+ "----->>>>        " + "After Replacing Attributes");
				//String adminEmailList =getLookupValue("Lookup.Email.ITAdmin",locationEntry.getKey());
				//String adminEmailList =getLookupValue("Lookup.Email.ITAdmin",key);
				String adminEmailList =getLookupValue("lookup.emailList",key);
				//New Addition
				adminEmailList = adminEmailList.concat(";"+toAddress);

				if(adminEmailList != null)
				{
					//String []toList=adminEmailList.split(",");
					String []toList=adminEmailList.split(";");
					LOGGER.info(classname + methodName+ "----->>>>        " + "Sending out Notification");
					sendNotification("Notify IT Group Schedule Task Template",emailReplaceAttributes,toList,new String[]{},new String[]{});
				}
	            //You can remove elements while iterating.
	            //iterator.remove();
	        }
	        
	     LOGGER.info(classname + methodName+ "----->>>>        " + "Sent Mail");
	 	
	   	}
     
	
	public String sendNotification(String oimEmailDefinitionName, HashMap emailReplaceAttributes, String[] toList, String[] ccList,String[] bccList){
		
		String methodName="sendNotification";
		//System.out.println("Staring sendNotification()");
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
	
	public String getLookupValue(String lookupName, String lookupCodKey)
            {
		
     
		String methodName="getLookupValue";

     String lookupDecodeKey = null;
     
     try {
            

            tcResultSet tcresultSet = lookup.getLookupValues(lookupName);

            LOGGER.info(classname + methodName+ "----->>>>        " +  "Successfully got the Lookpup values.");

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
