package com.iam.generic.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.platform.Platform;
import oracle.iam.scheduler.vo.TaskSupport;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;

import com.iam.generic.scheduler.common.CommonUtil;
import com.iam.generic.scheduler.common.Constants;
import com.iam.generic.scheduler.common.MailUtil;

public class NotifyEventFailuresScheduler extends TaskSupport{
	private static final ODLLogger logger = ODLLogger.getODLLogger("UL.CUSTOM");
	String className = "[NotifyEventFailuresScheduler]==>";
	public MailUtil mailUtil = new MailUtil();
	public CommonUtil commonUtil = new CommonUtil();
	// This method will send a notification for the failure events, records. and any provisioning failure for a particular day
	// All the failed records are maintained in the different lookups configured for this failure scenarios
	// This notification will include AD, Exchange and ProWatch provisioning failures
	
	public void execute(HashMap hashMap) throws Exception {
		String methodName = "::execute::";
		try{
			logger.info(className + methodName + " ENTERING ");
			String failedLookup = ((String)hashMap.get("Failed Task Lookups"));
			String toAddress = ((String)hashMap.get("Recipient Email Address"));
			String emailTemplateName = "Notify Event Failures";
			logger.info(className + methodName +"Input Parameters in scheduler are ==> Failed Task Lookups: " + failedLookup + ", Recipient Email: " + toAddress);
			
			tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf = Platform.getService(tcITResourceInstanceOperationsIntf.class);
			tcEmailOperationsIntf emailOperationsIntf = Platform.getService(tcEmailOperationsIntf.class);
			tcLookupOperationsIntf lookupOperationsIntf = Platform.getService(tcLookupOperationsIntf.class);
			HashMap globalMap = commonUtil.getHashMapFromLookup(Constants.NOTIFICATION_EMAILID_LOOKUP, lookupOperationsIntf);
			String fromAddress = (String)globalMap.get("from email");
			StringBuffer messageBody = new StringBuffer();
			String[] lookupArr = failedLookup.split("\\|");
			for(int i=0;i<lookupArr.length;i++){
				logger.info(className + methodName +"Failed Lookup Name: " + lookupArr[i]);
				HashMap failedHashMap = commonUtil.getHashMapFromLookup(lookupArr[i].trim(), lookupOperationsIntf);
				if(failedHashMap.size()>0){
					String subStrAppName = lookupArr[i].substring(lookupArr[i].indexOf('.')+1);
					
					StringBuffer failedRecordsSB = new StringBuffer();
					failedRecordsSB.append("<head><style>table {border-collapse: collapse;}table, td, th {border: 1px solid black;}</style></head>");
					failedRecordsSB.append("<table><tr><th>APPLICATION</th><th> USER ID</th><th>NOTES</th></tr>");
					
					Set lookupKeySet = failedHashMap.keySet();
					Iterator<String> itr = lookupKeySet.iterator();
					ArrayList<String> attrList = new ArrayList<String>();
					while(itr.hasNext()){
						attrList.add((String)itr.next());
					}

					for(int j=0;j<attrList.size();j++){
						logger.info(className+"Adding Attributes to Map "+attrList.get(j));
						failedRecordsSB.append("<tr><td>"+ subStrAppName.substring(0,subStrAppName.indexOf('.')) + " </td><td>"+attrList.get(j) + "</td><td>"+failedHashMap.get(attrList.get(j))+"</td></tr>");
					}
					failedRecordsSB.append("</table><br><br>");
					messageBody.append(failedRecordsSB);
				}
			}
			
			String subjectParams[] = new String[1];  
			String bodyParams[] = new String[2];
			//bodyParams[0] = "<img src=\"cid:image1\" /><br>";
			bodyParams[0] = messageBody.toString();
			
			boolean returnStatusEmail = mailUtil.sendMailEnhanced(fromAddress, toAddress, "", bodyParams, subjectParams, emailTemplateName, emailOperationsIntf, itResourceInstanceOperationsIntf);
			if(returnStatusEmail){
				logger.info(className + methodName + "Email sent successfully...");
				for(int i=0;i<lookupArr.length;i++){
					boolean removeLookupResult = commonUtil.removeLookupValues(lookupOperationsIntf, lookupArr[i].trim());
				}
			}
			logger.info(className + methodName + "Schedule job finished...");
		}catch (Exception e){
			logger.info("Exception Occurs " + e.toString());
		}
	}

	public HashMap getAttributes(){
		return null;
	}

	public void setAttributes(){

	}
}
