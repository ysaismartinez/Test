package com.iam.generic.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authopss.exception.AccessDeniedException;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.exception.ApplicationInstanceNotFoundException;
import oracle.iam.provisioning.exception.GenericAppInstanceServiceException;
import oracle.iam.provisioning.exception.GenericProvisioningException;
import oracle.iam.provisioning.exception.UserNotFoundException;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.AccountData;
import oracle.iam.provisioning.vo.FormInfo;


/**
 * @author Zubair Khan 
 * 
 */
public class ProvisionEBSTask extends oracle.iam.scheduler.vo.TaskSupport {
	
	
	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	/*
	 * Scheduled task entry point
	 */
	public void execute(HashMap taskparams) {
		
		LOGGER.info("Starting ProvisionEBSTask.execute() Scheduled Task ");
		//EBSUMAppInstance
		//UD_EBS_USER_EBS_ITRES
		DataSource ds = Platform.getOperationalDS();
		ProvisioningService provisioningService = Platform.getService(ProvisioningService.class);
		ApplicationInstanceService applicationInstanceService = Platform.getService(ApplicationInstanceService.class);
		oracle.iam.provisioning.vo.ApplicationInstance applicationInstance =null;
		String noOfDay = (String) taskparams.get("No Of Day");
		String appInstName = (String) taskparams.get("Application Instance Name");
		String serverName = (String) taskparams.get("serverName");
		Connection con = null ;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		
		
		
		String query="select usr.usr_key, usr.usr_login from usr usr, act act where trunc(usr_hire_date) <=trunc (sysdate+"+noOfDay+") and " +
		" usr.act_key= act.act_key and act_name like 'Internal User' and usr.usr_key not in (select usr.usr_key from usr usr, act act, oiu oiu,obi obi, obj obj where " + "trunc(usr_hire_date) <=trunc (sysdate+"+noOfDay+") and "+
		" usr.act_key= act.act_key and act_name like 'Internal User'  and oiu.usr_key = usr.usr_key and obi.obi_key=oiu.obi_key and obi.obj_key=obj.obj_key and obj.obj_name like 'eBusiness Suite User')";
		
		try {
			
			con = ds.getConnection();
			ps = con.prepareStatement(query);              
			rs = ps.executeQuery();
			LOGGER.info("ProvisionEBSTask.execute() Query Executed "+query);
			if (rs!=null){
				int noOfUsersProvisioned=0;
				while (rs.next()){
					applicationInstance = applicationInstanceService.findApplicationInstanceByName(appInstName);
					FormInfo accountForm = applicationInstance.getAccountForm();
					long formKey = accountForm.getFormKey();
					Long itreskey = applicationInstance.getItResourceKey();
					Map parentData = new HashMap();
					parentData.put(serverName, itreskey);        			
					AccountData accountData = new AccountData(String.valueOf(formKey), null, parentData);
					oracle.iam.provisioning.vo.Account account = new Account(applicationInstance, accountData);
					
					String usr_key = rs.getString("usr_key");
					account.setAccountType(Account.ACCOUNT_TYPE.Primary);
					provisioningService.provision(usr_key,account);
					LOGGER.info("ProvisionEBSTask.execute() User Provisioned EBS for userid= "+rs.getString("usr_login"));
					noOfUsersProvisioned++;
				}
				LOGGER.info("ProvisionEBSTask.execute() total no of users provisioned = "+noOfUsersProvisioned);
			}
			LOGGER.info("ProvisionEBSTask.execute() Exiting");
			
		} catch (ApplicationInstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericAppInstanceServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GenericProvisioningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
	
	
	
	
}
