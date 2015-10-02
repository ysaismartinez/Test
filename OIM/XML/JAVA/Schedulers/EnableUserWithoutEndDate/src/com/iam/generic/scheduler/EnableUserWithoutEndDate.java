package com.iam.generic.scheduler;

import java.sql.Date;
import java.util.HashMap;

import oracle.core.ojdl.logging.ODLLogger;
import Thor.API.Operations.tcUserOperationsIntf;

import com.thortech.xl.dataaccess.tcDataSetException;
import com.thortech.xl.dataobj.tcDataSet;
import com.thortech.xl.scheduler.tasks.SchedulerBaseTask;
import com.thortech.xl.util.logging.LoggerMessages;


/**
 * @author Zubair Khan 
 * 
 */
public class EnableUserWithoutEndDate extends SchedulerBaseTask {
	private static final ODLLogger logger = ODLLogger.getODLLogger("UL.CUSTOM");
	Date isCurrentDate;
	
	public EnableUserWithoutEndDate(){
		super();
	}
	
	public void init(){
		logger.info(LoggerMessages.getMessage("EnteredMethodDebug", "EnableUserWithoutEndDate/init"));
		isCurrentDate = new Date(System.currentTimeMillis());
		logger.info(LoggerMessages.getMessage("LeftMethodDebug", "EnableUserWithoutEndDate/init"));
	}
	
	public void execute(){
		logger.info(LoggerMessages.getMessage("EnteredMethodDebug", "EnableUserWithoutEndDate/execute"));
		tcDataSet moUsrDataSet = new tcDataSet();
		tcDataSet moDummyDataSet = new tcDataSet();
		int mnRowCount;
		try
		{
			if (isStopped()){
				return;      	
			}	
			moDummyDataSet.setQuery(getDataBase(), "select USR_DEPROVISIONING_DATE from usr where 1=2");
			moDummyDataSet.executeQuery();
			if (isStopped()){
				return;      	
			}
			moDummyDataSet.setDate("USR_DEPROVISIONING_DATE", isCurrentDate);
			logger.info("***CurrentDate Set");
			
			moUsrDataSet.setQuery(getDataBase(),"select usr_key, usr_rowver from usr where usr_hire_date < "
					+ moDummyDataSet.getSqlText("USR_DEPROVISIONING_DATE") + " and USR_STATUS='Disabled' and USR_END_DATE is null");
			moUsrDataSet.executeQuery();
			if (isStopped()){
				return;      	
			}
			
			mnRowCount=moUsrDataSet.getRowCount();
			logger.info("***EnableUserWithoutEndDate:execute:Number of users retrieved is=" + mnRowCount);
			
			if (isStopped()){
				return;      	
			}
			
			tcDataSet usrDataset = new tcDataSet();
			usrDataset.setQuery(getDataBase(), "select usr_key, usr_rowver from usr where 1=2");
			usrDataset.executeQuery();
			HashMap attrib = new HashMap();
			attrib.put("USR_DISABLED","0");
			//attrib.put("USR_STATUS","Active");
			tcUserOperationsIntf userUtil =(tcUserOperationsIntf) getUtility("Thor.API.Operations.tcUserOperationsIntf");
			for(int i=0; i<mnRowCount; i++)
			{
				if (isStopped()){
					return;      	
				}
				moUsrDataSet.goToRow(i);
				
				usrDataset.setByteArray("usr_rowver", moUsrDataSet.getByteArray("usr_rowver"));
				usrDataset.setString("usr_key", moUsrDataSet.getString("usr_key"));
				if (isStopped()){
					return;      	
				}
				
				try{
					//userUtil.updateUser(new tcMetaDataSet(usrDataset, getDataBase()), attrib);
					String userKey=moUsrDataSet.getString("usr_key");
					userUtil.enableUser(Long.parseLong(userKey));
					logger.info("***EnableUserWithoutEndDate:execute:Set user to Active to user key " + userKey);
				}
				catch(Exception e){
					logger.info(LoggerMessages.getMessage("ErrorMethodDebug", "EnableUserWithoutEndDate/execute", e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		catch(tcDataSetException dse)
		{
			//logger.info(LoggerMessages.getMessage("ErrorMethodDebug", "EnableUserWithoutEndDate/execute");
			logger.info("***EnableUserWithoutEndDate:execute: Data save Failed");
			dse.printStackTrace();
		}
		catch(Exception e)
		{
			logger.info("ErrorMethodDebug , EnableUserWithoutEndDate/execute");
			e.printStackTrace();
		}
		logger.info(LoggerMessages.getMessage("LeftMethodDebug", "EnableUserWithoutEndDate/execute"));
	}
	public boolean stop(){
		logger.info("***EnableUserWithoutEndDate:stop:Task being stopped");
		return true;
	}
}
