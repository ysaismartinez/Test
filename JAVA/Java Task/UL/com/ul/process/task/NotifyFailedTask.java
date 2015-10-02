package com.ul.process.task;

import java.util.HashMap;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.platform.Platform;
import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Operations.tcLookupOperationsIntf;

import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;
import com.thortech.xl.dataobj.util.XLDatabase;

public class NotifyFailedTask {
	private static final ODLLogger logger = ODLLogger.getODLLogger("UL.CUSTOM");
	String className = this.getClass().toString() + "::";
	public NotifyFailedTask(){
		
	}
	
	public String populateLookupOnError(String userLogin, String roName, String lookupName){
		String methodName = ":populateLookupOnError():";
		logger.info(className + "In " + methodName);
		String response = "Failed";
		String errMessage = "";
		try{
			String taskDesc = null;
			String taskName = null;
			final tcDataProvider dataProvider = XLDatabase.getInstance().getDataBase();
		    String taskQuery = "SELECT USR.USR_LOGIN,MIL.MIL_NAME, " +
				" (select RSC_DESC from RSC where RSC_KEY = OSI.RSC_KEY) as TASK_DESCRIPTION, " +
				" cast(SCH.SCH_UPDATE as Timestamp) as TASK_TIME " +
				" FROM OSI,SCH,STA,MIL,TOS,PKG,OIU,OBJ,OST,USR " +
				" WHERE OSI.MIL_KEY=MIL.MIL_KEY " +
				" AND SCH.SCH_KEY=OSI.SCH_KEY AND STA.STA_STATUS=SCH.SCH_STATUS " +
				" AND TOS.PKG_KEY=PKG.PKG_KEY AND MIL.TOS_KEY=TOS.TOS_KEY " +
				" AND OIU.OST_KEY=OST.OST_KEY AND OST.OBJ_KEY=OBJ.OBJ_KEY " +
				" AND OSI.ORC_KEY=OIU.ORC_KEY AND OIU.USR_KEY=USR.USR_KEY " +
				" AND STA.STA_BUCKET = 'Rejected' AND USR.USR_LOGIN = '" + userLogin + "' " +
				" AND OBJ.OBJ_NAME='" + roName + "' " +
				" AND cast(SCH.SCH_UPDATE as Timestamp) in ( SELECT MAX(cast(SCH.SCH_UPDATE as Timestamp)) " +
				" FROM OSI,SCH,STA,MIL,TOS,PKG,OIU,OBJ,OST,USR " +
				" WHERE OSI.MIL_KEY=MIL.MIL_KEY " +
				" AND SCH.SCH_KEY=OSI.SCH_KEY AND STA.STA_STATUS=SCH.SCH_STATUS " +
				" AND TOS.PKG_KEY=PKG.PKG_KEY AND MIL.TOS_KEY=TOS.TOS_KEY " +
				" AND OIU.OST_KEY=OST.OST_KEY AND OST.OBJ_KEY=OBJ.OBJ_KEY " +
				" AND OSI.ORC_KEY=OIU.ORC_KEY AND OIU.USR_KEY=USR.USR_KEY " +
				" AND STA.STA_BUCKET = 'Rejected' AND USR.USR_LOGIN = '" + userLogin + "' " + 
				" AND OBJ.OBJ_NAME='" + roName + "')";
		    //logger.info("DB query is::" + taskQuery);
		    tcDataSet dataSet = new tcDataSet();
		    dataSet.setQuery(dataProvider, taskQuery);
		    try{
				dataSet.executeQuery();
				for (int j=0; j<dataSet.getRowCount(); j++){
					dataSet.goToRow(j);
					taskName = dataSet.getString("MIL_NAME");
					taskDesc = dataSet.getString("TASK_DESCRIPTION");
					errMessage = userLogin + " - \"" + taskName + "\" Task Failed";
				}
		    }catch(Exception e){
		    	logger.info("In catch due to error::" + e.getMessage());
		    }

		    tcLookupOperationsIntf lookupOperationsIntf = Platform.getService(tcLookupOperationsIntf.class);

		    HashMap<String, String> deptMap = getHashMapFromLookup(lookupName, lookupOperationsIntf);
		    if(!deptMap.containsKey(errMessage.trim().toUpperCase())){
		    	boolean populateLookup = populateLookupOnRun(taskDesc, errMessage, lookupOperationsIntf, lookupName);
		    	if(populateLookup){
		    		response = "Success";
		    	}
		    }else{
		    	response = "Success";
		    }
		}catch(Exception e){
			logger.info("In catch due to error::" + e.getMessage());
		}
		return response;
	}
	
	public static boolean populateLookupOnRun(String decodeKey, String codeKey, tcLookupOperationsIntf lookupOperationsIntf, String failedRecordsLookup) {
		boolean result = false;
		try {
			logger.info(" Code Key to Add :: " + codeKey  + " Decode Key to Add :: " + decodeKey + " in "+failedRecordsLookup);
			lookupOperationsIntf.addLookupValue(failedRecordsLookup.trim(), codeKey, decodeKey, "", "");
			result = true;
		} catch (tcAPIException e) {
			logger.info("tcAPIException Occurs "+e);
		} catch (Exception e){
			logger.info("Exception Occurs "+e.getMessage());
		}
		return result;
	}
	
	public HashMap getHashMapFromLookup(String lookupName,tcLookupOperationsIntf lookupOperationsIntf) throws Exception {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (isNullOrEmpty(lookupName))
			return hashMap;
		tcResultSet resultGetLookupValues = lookupOperationsIntf
				.getLookupValues(lookupName);
		if (isResultSetNullOrEmpty(resultGetLookupValues))
			return hashMap;
		int countResultOracleReconLookup = resultGetLookupValues.getRowCount();
		for (int i = 0; i < countResultOracleReconLookup; i++) {
			resultGetLookupValues.goToRow(i);
			hashMap.put(
					resultGetLookupValues.getStringValue("Lookup Definition.Lookup Code Information.Code Key").toUpperCase(),
					resultGetLookupValues.getStringValue("Lookup Definition.Lookup Code Information.Decode").toUpperCase());
		}
		return hashMap;
	}

	private static boolean isNullOrEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public static boolean isResultSetNullOrEmpty(tcResultSet resultSet) {
		boolean isResultSetNullOrEmpty = true;
		try {
			isResultSetNullOrEmpty = resultSet == null || resultSet.isEmpty()
					|| resultSet.getRowCount() == 0;
		} catch (Throwable t) {
			logger.info("Exception Occurs "+ t);
		}
		return isResultSetNullOrEmpty;
	}

}

