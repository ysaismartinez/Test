package com.iam.generic.scheduler.common;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.platform.Platform;
import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;

public class MailUtil {

	String className = this.getClass().toString() + "::";
	private String bodyText;
	private String subject;
	CommonUtil bhsfUtil;
	private static final ODLLogger logger = ODLLogger.getODLLogger("UL.CUSTOM");

	public MailUtil() {
		bodyText = null;
		subject = null;
		bhsfUtil = new CommonUtil();
	}

	/**
	 * This method reads a specified mail template
	 * @param mailTemp
	 * @throws Exception
	 */
	private void getMailDetailsFromTemplate(String mailTemp, tcEmailOperationsIntf emailOpIntf) throws Exception {
		HashMap hashMap = new HashMap();
		hashMap.put("Email Definition.Name", mailTemp);
		try {
			emailOpIntf = Platform.getService(tcEmailOperationsIntf.class);
			tcResultSet mailSet = emailOpIntf.findEmailDefinition(hashMap);
			subject = mailSet.getStringValue("Email Definition.Subject");
			logger.info(className+"Mail Subject Original : " + subject);
			bodyText = mailSet.getStringValue("Email Definition.Body");
			logger.info(className+"Mail Body Original : " + bodyText);
		} catch (Exception ex) {
			logger.info("Error occured while reading mail templete "+ mailTemp);
		}
	}

	/**
	 * @param fromAddress
	 * @param toAddressList
	 * @param ccAddressList
	 * @param bodyParams
	 * @param subjectParams
	 * @param mailTemp
	 * @param host
	 * @param port
	 * @param isAdapterNotification
	 * This method will send the notification to the specified email address
	 * @return
	 */
	public boolean sendMailEnhanced(String fromAddress, String toAddressList,
			String ccAddressList, String[] bodyParams, String[] subjectParams,
			String mailTemp, tcEmailOperationsIntf emailOperationsIntf, 
			tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf) {

		String methodName = "sendMailEnhanced():";
		logger.info(className + "In " + methodName);
		boolean returnStatus = false;
		Properties props = new Properties();
		logger.info(className
				+ methodName
				+ "************sendMailEnhanced Execution Started*****************");

		logger.info(className + methodName + "fromAddress=" + fromAddress);
		logger.info(className + methodName + "toAddressList=" + toAddressList);
		logger.info(className + methodName + "ccAddressList=" + ccAddressList);
		logger.info(className + methodName + "mailTemp=" + mailTemp);
		// logger.info(className+"");

		if (fromAddress == null || fromAddress.trim().length() == 0) {
			logger.info(className + methodName
					+ "  fromAddress is null or empty");
			return returnStatus;
		}

		if (toAddressList == null || toAddressList.trim().length() == 0) {
			logger.info(className + methodName
					+ "  toAddressList is null or empty");
			return returnStatus;
		}

		if (mailTemp == null || mailTemp.trim().length() == 0) {
			logger.info(className + methodName + "  mailTemp is null or empty");
			return returnStatus;
		}

		/*
		 * 
		 * 
		 * if(subjectParams==null || subjectParams.length==0) {
		 * logger.info(className
		 * +methodName+"  subjectParams is null or empty"); return returnStatus;
		 * }
		 */
		if (bodyParams == null || bodyParams.length == 0) {
			logger.info(className + methodName
					+ "  bodyParams is null or empty");
			return returnStatus;
		}

		// Call getMailDetailsFromTemplate(mailTemp) to populate subject and
		// bodyText
		try {
			logger.info(className + "IN TRY before getMailDetailsFromTemplate");
			getMailDetailsFromTemplate(mailTemp,emailOperationsIntf);
		} catch (Exception e) {
			logger.info(className
					+ methodName
					+ "  Returning false as Exception in getMailDetailsFromTemplate "
					+ e.getMessage());
			return returnStatus;
		}

		// Check whether subject and bodyText populated successfully
		if (subject == null || subject.length() == 0) {
			logger.info(className + methodName + "  subject is null or empty");
			return returnStatus;
		}

		if (bodyText == null || bodyText.length() == 0) {
			logger.info(className + methodName + "  bodyText is null or empty");
			return returnStatus;
		}

		for (int i = 0; i < subjectParams.length; i++) {
			String traceStr = "&" + i;
			logger.info(className + methodName
					+ "subjectParams Replace String: " + traceStr);
			subject = subject.replaceAll(traceStr, subjectParams[i]);
		}

		logger.info(className + methodName
				+ "  Mail Subject after replacement : " + subject);

		for (int i = 0; i < bodyParams.length; i++) {
			String traceStr = "&" + i;
			logger.info(className + methodName + "bodyParams Replace String: "	+ traceStr);
			bodyText = bodyText.replaceAll(traceStr, bodyParams[i]);
		}

		logger.info(className + methodName
				+ "  Mail Body after replacement : " + bodyText);

		String smtpHost = "mail.smtp.host";
		String smtpPort = "mail.smtp.port";

		String smtpHostName = getITResourceParamValue(Constants.MAIL_SERVER, "Server Name",itResourceInstanceOperationsIntf);

		if (smtpHostName == null || smtpHostName.trim().length() == 0) {
			logger.info(className + methodName + "  smtpHostName is null or empty");
			return returnStatus;
		}
		logger.info(className + methodName + "Email server used is  : " + smtpHostName);
		
		final String fromEmail = getITResourceParamValue(Constants.MAIL_SERVER, "User Login",itResourceInstanceOperationsIntf);
		if (fromEmail == null || fromEmail.trim().length() == 0) {
			logger.info(className + methodName + " Email Server User Login  is null or empty");
			return returnStatus;
		}
		logger.info(className + methodName + "Email Server User Login is  : " + fromEmail);
		
		final String password = getITResourceParamValue(Constants.MAIL_SERVER, "User Password",itResourceInstanceOperationsIntf);
		if (password == null || password.trim().length() == 0) {
			logger.info(className + methodName + " Email Server User Password  is null or empty");
			//return returnStatus;
		}
		logger.info(className + methodName + "Email Server User Password is  : " + password);
		
		String smtpPortNumber = Constants.SMTP_PORT;
		if (smtpPortNumber == null || smtpPortNumber.trim().length() == 0) {
			logger.info(className + methodName + "  smtpPortNumber is null or empty");
			return returnStatus;
		}
		logger.info(className + methodName + "SMTP Port Number used is  : " + smtpPortNumber);

		props.put(smtpHost, smtpHostName);
		props.put(smtpPort, smtpPortNumber);
		props.put("mail.smtp.auth", "false"); //enable authentication
		props.put("mail.smtp.socketFactory.port", smtpPortNumber);
		props.put("mail.smtp.socketFactory.fallback", "false");
		//props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
		//create Authenticator object to pass in Session.getInstance argument
		/*Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		};*/

        Session session = Session.getDefaultInstance(props);
		Message msg = new MimeMessage(session);

		try {
			msg.setFrom(new InternetAddress(fromAddress.trim()));

			String[] toList = toAddressList.trim().split(",");

			InternetAddress[] toAddresses = new InternetAddress[toList.length];
			for (int count = 0; count < toList.length; count++) {
				toAddresses[count] = new InternetAddress(toList[count]);
				logger.info(className + methodName + "To Address : " + toList[count]);
			}
			msg.addRecipients(Message.RecipientType.TO, toAddresses);

			if (ccAddressList != null && !ccAddressList.equals("")) {
				String[] ccList = ccAddressList.trim().split(",");
				InternetAddress[] ccAddresses = new InternetAddress[ccList.length];
				for (int count = 0; count < ccList.length; count++) {
					ccAddresses[count] = new InternetAddress(ccList[count]);
					logger.info(className + methodName + "CC Address : " + ccList[count]);
				}
				msg.addRecipients(Message.RecipientType.CC, ccAddresses);
			}

			msg.setSubject(subject.trim());

			Multipart mp = new MimeMultipart();
			BodyPart bp2 = new MimeBodyPart();
			bp2.setContent(bodyText, "text/HTML");
			mp.addBodyPart(bp2);

			 // adds inline image attachments as email header
			/*Map<String, String> mapInlineImages = new HashMap<String, String>();
	        mapInlineImages.put("image1", Constants.MAIL_HEADER_IMAGE);
	        
	        if (mapInlineImages != null && mapInlineImages.size() > 0) {
	            Set<String> setImageID = mapInlineImages.keySet();
	             
	            for (String contentId : setImageID) {
	                MimeBodyPart imagePart = new MimeBodyPart();
	                imagePart.setHeader("Content-ID", "<" + contentId + ">");
	                imagePart.setDisposition(MimeBodyPart.INLINE);
	                 
	                String imageFilePath = mapInlineImages.get(contentId);
	                try {
	                    imagePart.attachFile(imageFilePath);
	                } catch (IOException ex) {
	                	logger.info(className + methodName + "Error in Image setting..." + ex.toString());
	                }
	                mp.addBodyPart(imagePart);
	            }
	        }*/
			
	        msg.setText(bodyText);
			msg.setContent(mp);
			Transport.send(msg);
			returnStatus = true;
			logger.info(className + methodName
					+ "************sendMailEnhanced Execution Ended Successfully*****************");
		} catch (SendFailedException e) {
			logger.info(className
					+ methodName + "******Exception thrown SendFailedException**************"
					+ e.getMessage());
		} catch (MessagingException e) {
			logger.info(className + methodName
					+ "******Exception thrown**************" + e.getMessage());
		} catch (Exception e) {
			logger.info(className + methodName
					+ "******Exception thrown MessagingException**************"
					+ e.getMessage());
		}
		logger.info(className + methodName + "Returning = " + returnStatus);
		return returnStatus;
	}

	/**
	 * @param fromAddress
	 * @param toAddressList
	 * @param ccAddressList
	 * @param bodyParams
	 * @param subjectParams
	 * @param mailTemp
	 * @param host
	 * @param port
	 * @param isAdapterNotification
	 * This method will send the notification to the specified email address
	 * @return
	 */
	public boolean sendMailEnhancedWithAttachment(String fromAddress, String toAddressList,
			String ccAddressList, String[] bodyParams, String[] subjectParams,
			String mailTemp,tcEmailOperationsIntf emailOperationsIntf,tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf) {

		String methodName = "sendMailEnhanced():";
		logger.info(className + "In " + methodName);
		boolean returnStatus = false;
		Properties props = new Properties();
		logger.info(className
				+ methodName
				+ "************sendMailEnhanced Execution Started*****************");

		logger.info(className + methodName + "fromAddress=" + fromAddress);
		logger.info(className + methodName + "toAddressList=" + toAddressList);
		logger.info(className + methodName + "ccAddressList=" + ccAddressList);
		logger.info(className + methodName + "mailTemp=" + mailTemp);
		// logger.info(className+"");

		if (fromAddress == null || fromAddress.trim().length() == 0) {
			logger.info(className + methodName
					+ "  fromAddress is null or empty");
			return returnStatus;
		}

		if (toAddressList == null || toAddressList.trim().length() == 0) {
			logger.info(className + methodName
					+ "  toAddressList is null or empty");
			return returnStatus;
		}

		if (mailTemp == null || mailTemp.trim().length() == 0) {
			logger.info(className + methodName + "  mailTemp is null or empty");
			return returnStatus;
		}

		/*
		 * 
		 * 
		 * if(subjectParams==null || subjectParams.length==0) {
		 * logger.info(className
		 * +methodName+"  subjectParams is null or empty"); return returnStatus;
		 * }
		 */
		if (bodyParams == null || bodyParams.length == 0) {
			logger.info(className + methodName
					+ "  bodyParams is null or empty");
			return returnStatus;
		}

		// Call getMailDetailsFromTemplate(mailTemp) to populate subject and
		// bodyText
		try {
			logger.info(className + "IN TRY before getMailDetailsFromTemplate");
			getMailDetailsFromTemplate(mailTemp,emailOperationsIntf);
		} catch (Exception e) {
			logger.info(className
					+ methodName
					+ "  Returning false as Exception in getMailDetailsFromTemplate "
					+ e.getMessage());
			return returnStatus;
		}

		// Check whether subject and bodyText populated successfully
		if (subject == null || subject.length() == 0) {
			logger.info(className + methodName + "  subject is null or empty");
			return returnStatus;
		}

		if (bodyText == null || bodyText.length() == 0) {
			logger.info(className + methodName + "  bodyText is null or empty");
			return returnStatus;
		}

		for (int i = 0; i < subjectParams.length; i++) {
			String traceStr = "&" + i;
			logger.info(className + methodName
					+ "subjectParams Replace String: " + traceStr);
			subject = subject.replaceAll(traceStr, subjectParams[i]);
		}

		logger.info(className + methodName
				+ "  Mail Subject after replacement : " + subject);

		for (int i = 0; i < bodyParams.length; i++) {
			String traceStr = "&" + i;
			logger.info(className + methodName + "bodyParams Replace String: "	+ traceStr);
			bodyText = bodyText.replaceAll(traceStr, bodyParams[i]);
		}

		logger.info(className + methodName
				+ "  Mail Body after replacement : " + bodyText);

		String smtpHost = "mail.smtp.host";
		String smtpPort = "mail.smtp.port";

		String smtpHostName = getITResourceParamValue(
				Constants.MAIL_SERVER, "Server Name",itResourceInstanceOperationsIntf);

		if (smtpHostName == null || smtpHostName.trim().length() == 0) {
			logger.info(className + methodName
					+ "  smtpHostName is null or empty");
			return returnStatus;
		}

		logger.info(className + methodName + "Email server used is  : "
				+ smtpHostName);

		props.put(smtpHost, smtpHostName);
		props.put(smtpPort, "25");
		// Session session = Session.getDefaultInstance(props, null);
		Session session = Session.getInstance(props, null);
		Message msg = new MimeMessage(session);

		try {
			msg.setFrom(new InternetAddress(fromAddress.trim()));

			String[] toList = toAddressList.trim().split(",");

			InternetAddress[] toAddresses = new InternetAddress[toList.length];
			for (int count = 0; count < toList.length; count++) {
				toAddresses[count] = new InternetAddress(toList[count]);
			}
			msg.addRecipients(Message.RecipientType.TO, toAddresses);

			if (ccAddressList != null && !ccAddressList.equals("")) {
				String[] ccList = ccAddressList.trim().split(",");
				InternetAddress[] ccAddresses = new InternetAddress[ccList.length];
				for (int count = 0; count < ccList.length; count++) {
					ccAddresses[count] = new InternetAddress(ccList[count]);
				}
				msg.addRecipients(Message.RecipientType.CC, ccAddresses);
			}

			msg.setSubject(subject.trim());
			javax.activation.DataSource source = new FileDataSource("/u01/app/weblogic/product/Middleware/Oracle_IDM1/temp/Non_Emp_End_Date.csv");

			Multipart mp = new MimeMultipart();
			BodyPart bp2 = new MimeBodyPart();
			bp2.setDataHandler( new DataHandler(source));
			bp2.setFileName("Non_Emp_End_Date.csv");
			logger.info("*****************FILE NAME SET****************");
			//bp2.setContent(bodyText, "text/HTML");
			mp.addBodyPart(bp2);

			msg.setText(bodyText);
			msg.setContent(mp);
			Transport.send(msg);
			returnStatus = true;
			logger.info(className
					+ methodName
					+ "************sendMailEnhanced Execution Ended Successfully*****************");
		} catch (SendFailedException e) {
			logger.info(className
					+ methodName
					+ "******Exception thrown SendFailedException**************"
					+ e.getMessage());
		} catch (MessagingException e) {
			logger.info(className + methodName
					+ "******Exception thrown**************" + e.getMessage());
		} catch (Exception e) {
			logger.info(className + methodName
					+ "******Exception thrown MessagingException**************"
					+ e.getMessage());
		}
		logger.info(className + methodName + "Returning = " + returnStatus);
		return returnStatus;
	}
	
	/**
	 * @param sITRes
	 * @param reqParamName
	 * @param itResourceIntf
	 * This method will get the parameter values of a specified IT Resource
	 * @return
	 */
	public String getITResourceParamValue(String sITRes, String reqParamName, tcITResourceInstanceOperationsIntf itResourceIntf) {
		String reqParamValue = "FAILURE";
		String methodName = "::getITResourceParamValue(String sITRes, String reqParamName)::";
		logger.info(className + "IN " + methodName);

		// Returns null as IT Resource name passed as an argument is either NULL
		// or empty
		if (reqParamName.equals(null)) {
			return reqParamValue;
		}

		Hashtable itResParameterVal = getITResParameterDetails(sITRes, itResourceIntf);
		logger.info(className + methodName+ "It Resource Details Done");

		String paramValue = (String) itResParameterVal.get(reqParamName);
		return paramValue;
	}

	/**
	 * @param sITRes
	 * @param itResourceIntf
	 * This method will get the parameter values of a specified IT Resource parameter
	 * @return
	 */
	public Hashtable getITResParameterDetails(String sITRes, tcITResourceInstanceOperationsIntf itResourceIntf) {
		String methodName = "::getITResParameterDetails(String sITRes)::";
		logger.info(className + methodName+ " ENTERING ");
		long lSvrKey = -1;
		Hashtable hITResParam = new Hashtable();

		//logger.info(className + methodName	+ "Enters for extracting details on IT Resource Name: "	+ sITRes);
		try {
			HashMap hashMap = new HashMap();
			hashMap.put(Constants.IT_RESOURCE_NAME, sITRes);

			tcResultSet tcresultset = itResourceIntf.findITResourceInstances(hashMap);
			//logger.info(className + "Size " + tcresultset.getRowCount());
			/*if (tcresultset != null) {
				logger.info(className + methodName	+ "Total number of rows found for IT resource "	+ sITRes + " is: " + tcresultset.getRowCount());
			} */

			tcresultset.goToRow(0);
			lSvrKey = tcresultset.getLongValue(Constants.IT_RESOURCE_KEY);
			hITResParam.put(Constants.IT_RESOURCE_KEY, "" + lSvrKey);

			tcresultset = itResourceIntf.getITResourceInstanceParameters(lSvrKey);

			if (tcresultset != null) {
				//logger.info(className+ methodName+ "Total number of rows found for IT resource parameter against "	+ sITRes + " is: " + tcresultset.getRowCount());
			} else {
				logger.info(className + methodName	+ "No rows found for IT resource parameter against "+ sITRes);
			}
			for (int i = 0; i < tcresultset.getRowCount(); i++) {
				tcresultset.goToRow(i);
				String paramName = tcresultset.getStringValue(Constants.IT_RESOURCE_PARAM_NAME);
				String paramValue = tcresultset	.getStringValue(Constants.IT_RESOURCE_PARAM_VALUE);
				hITResParam.put(paramName.trim(), paramValue.trim());
			}
		} catch (tcAPIException exception) {
			logger.info(className+ methodName + "tcAPIException occured while retriving data from IT-Resource "+ sITRes);
		} catch (tcColumnNotFoundException exception) {
			logger.info(className+ methodName+ "tcColumnNotFoundException occured while retriving data from IT-Resource "	+ sITRes);
		} catch (Exception exception) {
			logger.info(className+ methodName+ "Other Exception occured while retriving data from IT-Resource "+ sITRes);
		}
		logger.info(className + methodName+ " EXITING ");
		return hITResParam;
	}

}
