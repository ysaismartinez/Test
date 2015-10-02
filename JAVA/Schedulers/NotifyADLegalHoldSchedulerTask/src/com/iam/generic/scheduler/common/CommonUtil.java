package com.iam.generic.scheduler.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.conf.api.SystemConfigurationService;
import oracle.iam.conf.exception.SystemConfigurationServiceException;
import oracle.iam.conf.vo.SystemProperty;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.orgmgmt.api.OrganizationManager;
import oracle.iam.identity.orgmgmt.vo.Organization;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.Platform;
import oracle.iam.platform.authz.exception.AccessDeniedException;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.platform.entitymgr.vo.SearchCriteria.Operator;
import oracle.iam.provisioning.api.EntitlementService;
import oracle.iam.provisioning.exception.GenericEntitlementServiceException;
import oracle.iam.provisioning.vo.ApplicationInstance;
import oracle.iam.provisioning.vo.Entitlement;
import oracle.iam.reconciliation.api.ChangeType;
import oracle.iam.reconciliation.api.EventAttributes;
import oracle.iam.reconciliation.api.ReconOperationsService;
import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Exceptions.tcInvalidValueException;
import Thor.API.Operations.tcFormInstanceOperationsIntf;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Operations.tcUserOperationsIntf;

public class CommonUtil {

	String className = this.getClass().toString() + "::";
	private static final ODLLogger logger = ODLLogger.getODLLogger("UL.CUSTOM");

	/**
	 * @param lookupName
	 * @param lookupOperationsIntf
	 * @return
	 * This method will get the values of a lookup in a hashmap
	 * @throws Exception
	 */
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
					resultGetLookupValues
					.getStringValue(Constants.LOOKUP_DEFINITION_LOOKUP_CODE_INFORMATION_CODE_KEY),
					resultGetLookupValues
					.getStringValue(Constants.LOOKUP_DEFINITION_LOOKUP_CODE_INFORMATION_DECODE));
		}
		return hashMap;
	}

	/**
	 * @param lookupName
	 * @param lookupOperationsIntf
	 * @return
	 * This method will get the values of a lookup in a hashmap in upper case
	 * @throws Exception
	 */
	public HashMap getHashMapFromLookupForLawson(String lookupName,tcLookupOperationsIntf lookupOperationsIntf) throws Exception {
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
					resultGetLookupValues
					.getStringValue(Constants.LOOKUP_DEFINITION_LOOKUP_CODE_INFORMATION_CODE_KEY).toUpperCase(),
					resultGetLookupValues
					.getStringValue(Constants.LOOKUP_DEFINITION_LOOKUP_CODE_INFORMATION_DECODE).toUpperCase());
		}
		return hashMap;
	}

	/**
	 * @param s
	 * This method will check for a String to be null or empty
	 * @return
	 */
	public boolean isNullOrEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	/**
	 * @param resultSet
	 * This method will check for a result set to be null or empty
	 * @return
	 */
	public boolean isResultSetNullOrEmpty(tcResultSet resultSet) {

		String methodName = ":: isResultSetNullOrEmpty ::";
		boolean isResultSetNullOrEmpty = true;
		try {
			isResultSetNullOrEmpty = resultSet == null || resultSet.isEmpty()
					|| resultSet.getRowCount() == 0;
		} catch (Throwable t) {
			logger.info(className  + methodName + t);
		}finally {
		}
		return isResultSetNullOrEmpty;
	}

	/**
	 * @param userID
	 * @param attrKey
	 * This Method will return a user Attribute based on the User ID
	 * @return
	 */
	public String getUserAttributeByEmailID(String userEmail, String attrKey, UserManager userManager) {

		String methodName=":: getUserAttributeByID ::";
		String userAttribute = "NOT_FOUND";
		try {
			Set <String>retAttributes = new HashSet<String>();
			retAttributes.add(attrKey);
			SearchCriteria srchCriteria = new SearchCriteria(AttributeName.EMAIL.getId(), userEmail, SearchCriteria.Operator.EQUAL);

			List<User> users = userManager.search(srchCriteria, retAttributes, null);
			if (users.size() == 1) {
				User mgr = users.get(0);
				userAttribute = (String)mgr.getAttribute(attrKey);
			}else if(users.size()==0){
				logger.info(className  + methodName + " No user found with the specified email "+userEmail);
			}else{
				logger.info(className  + methodName + " Multiple users found with the specified email "+userEmail);
			}
		}catch (UserSearchException e) {
			logger.info(className  + methodName + " UserSearchException occured" + e);
		} catch (AccessDeniedException e) {
			logger.info(className  + methodName + " AccessDeniedException occured" + e);
		}catch (Exception e) {
			logger.info(className  + methodName + " General Exception occured" + e);
		}
		return userAttribute;
	}

	/**
	 * @param userLogin
	 * @param userManager
	 * @param returnedAttribute
	 * This Method will return a user object containing a return attribute based on the User ID
	 * @return
	 */
	public List<User> searchUserAtrribute(String attributeKey,String attributeVal, UserManager userManager, String returnedAttribute){
		String methodName = ":: searchUserAtrribute ::";
		List<User> users = null;

		try {
			logger.info(className+methodName+"Search User Attribute "+attributeKey+" Value ::"+attributeVal);
			Set <String>retAttributes = new HashSet<String>();
			retAttributes.add(returnedAttribute);
			SearchCriteria srchCriteria = new SearchCriteria(attributeKey, attributeVal, SearchCriteria.Operator.EQUAL);

			users = userManager.search(srchCriteria, retAttributes, null);
			if(users!=null){
				logger.info(className+methodName+"Returned User Attribute Value ::"+users);
				return users;
			}

		} catch (AccessDeniedException e) {
			logger.info(className  + methodName +" AccessDeniedException Occurs "+e);
		} catch (UserSearchException e) {
			logger.info(className  + methodName +" UserSearchException Occurs "+e);
		} catch (Exception e){
			logger.info(className  + methodName +" General Exception Occurs "+e);
		}
		return users;
	}



	/**
	 * @param userKey
	 * @param userManager
	 * @param returnedAttribute
	 * This Method will return a user object containing a return attribute based on the User ID
	 * @return
	 */
	public List<User> searchUserAtrributes(String attributeKey,String attributeVal, UserManager userManager, Set <String>retAttributes){
		String methodName = ":: searchUserAtrributes ::";
		List<User> users = null;

		try {
			SearchCriteria srchCriteria = new SearchCriteria(attributeKey,attributeVal, SearchCriteria.Operator.EQUAL);

			users = userManager.search(srchCriteria, retAttributes, null);
			if(users!=null){
				logger.info(className+"Returned User Attribute Value ::"+users);
				return users;
			}

		} catch (AccessDeniedException e) {
			logger.info(className  + methodName +" AccessDeniedException Occurs "+e);
		} catch (UserSearchException e) {
			logger.info(className  + methodName +" UserSearchException Occurs "+e);
		} catch (Exception e){
			logger.info(className  + methodName +" General Exception Occurs "+e);
		}
		return users;
	}

	/**
	 * @param codeKey
	 * @param decodeKey
	 * @param lookupOperationsIntf
	 * @param lookupName
	 * This method will populate the lookup with the failed records during bulk
	 * @return
	 */
	public String populateLookupOnRun(String codeKey, String decodeKey, tcLookupOperationsIntf lookupOperationsIntf, String lookupName) {
		String methodName = ":: populateLookupOnRun ::";
		logger.info(className  + methodName + " Entering");
		String result = "Error";
		try {
			logger.info(className  + methodName + " Code Key to Add :: " + codeKey  + " Decode Key to Add :: " + decodeKey + " in "+lookupName);
			lookupOperationsIntf.addLookupValue(lookupName.trim(), codeKey, decodeKey, "", "");
			result = "Success";
		} catch (tcAPIException e) {
			logger.info(className  + methodName +"tcAPIException Occurs "+e);
		} catch (tcInvalidLookupException e) {
			logger.info(className  + methodName +"tcInvalidLookupException Occurs "+e);
		} catch (tcInvalidValueException e) {
			logger.info(className  + methodName +"tcInvalidValueException Occurs "+e);
		} catch (Exception e){
			logger.info(className  + methodName +" General Exception Occurs "+e);
		}
		logger.info(className  + methodName + " Exiting");
		return result;
	}

	/**
	 * @param lookupOperationsIntf
	 * @param lookupName
	 * @param failedRecordsMap
	 * This method will remove all the lookup entries after all the entries are processed through failed records scheduler 
	 * @return
	 */
	public boolean removeLookupValues(tcLookupOperationsIntf lookupOperationsIntf, String lookupName) {
		String methodName = ":: removeLookupValues ::";
		logger.info(className  + methodName + " Entering");
		boolean result = false;
		try {
			HashMap recordsMap = getHashMapFromLookup(lookupName, lookupOperationsIntf);
			if(recordsMap.size()>0){
				logger.info(className  + methodName + " Remove Attribute Lookup name "+lookupName);
				Iterator iterator = recordsMap.keySet().iterator();  
				while (iterator.hasNext()) {  
					String key = iterator.next().toString();  
					logger.info(className+ methodName +" Failed Records HASHMAP Key Value ::"+key);
					lookupOperationsIntf.removeLookupValue(lookupName, key);
				} 
			}else{
				logger.info(className+ methodName +" Nothing to Remove..Lookup is already empty");
			}
			result = true;
		} catch (tcAPIException e) {
			logger.info(className  + methodName +"tcAPIException Occurs "+e);
		} catch (tcInvalidLookupException e) {
			logger.info(className  + methodName +"tcInvalidLookupException Occurs "+e);
		} catch (tcInvalidValueException e) {
			logger.info(className  + methodName +"tcInvalidValueException Occurs "+e);
		} catch (Exception e){
			logger.info(className  + methodName +" General Exception Occurs "+e);
		}
		logger.info(className  + methodName + " Exiting");
		return result;
	}

	/**
	 * @param orgName
	 * @param orgManager
	 * This method will get the organization key based on the organization name
	 * @return
	 */
	public long getOrganizationKey(String orgName, OrganizationManager orgManager){
		String methodName = "::  getOrganizationKey  ::"; 
		String orgKey = "";
		long orgKeyLong = 0;
		try{
			Set retAttrs = new HashSet();
			retAttrs.add(oracle.iam.identity.orgmgmt.api.OrganizationManagerConstants.AttributeName.ORG_PARENT_KEY.getId());
			SearchCriteria orgCriteria = new SearchCriteria(oracle.iam.identity.orgmgmt.api.OrganizationManagerConstants.AttributeName.ORG_NAME.getId(), orgName, Operator.EQUAL);
			List<Organization> orgList = orgManager.search(orgCriteria, retAttrs, null);
			orgKeyLong = (Long)orgList.get(0).getAttribute(oracle.iam.identity.orgmgmt.api.OrganizationManagerConstants.AttributeName.ID_FIELD.getId());
		}catch(Exception e){
			logger.info(className  + methodName + " Exception occured in fetching Organization Key "+e);
		}
		return orgKeyLong;
	}

	/**
	 * @param map
	 * This method is used for debugging purpose to print a hashmap
	 */
	public void printMap(Map<String, String> map){
		logger.info(className+"UserIDGenerationForNonGovtUsers####printMap:");
		Iterator it = map.keySet().iterator();
		while(it.hasNext()){
			try{
				String key = (String)it.next();
				String val = map.get(key);
				logger.info(className+" printMap "+" key=" + key + " val="+val);
			}catch(Exception e){
				logger.info(className+" printMap "+e);
			}
		}
	}

	public void performReconciliation(ReconOperationsService reconOperationsService, HashMap reconciliationDataMap, String application){
		String methodName =  ":: performReconciliation :: ";
		logger.info(className + methodName + "ENTERING");
		try {
			EventAttributes eventAttributes = new EventAttributes();
			eventAttributes.setChangeType(ChangeType.REGULAR);
			eventAttributes.setActionDate(new Date(0));
			eventAttributes.setDateFormat("yyyy/MM/dd HH:mm:ss z");
			eventAttributes.setEventFinished(true);
			long reconciliationKey = reconOperationsService.createReconciliationEvent(application, reconciliationDataMap, eventAttributes);
			logger.info(className +methodName +"Finishing Reconciliation with Reconciliation Key :: " + reconciliationKey);
		} catch (Exception e) {
			logger.info(className +methodName+ e);
		}
		logger.info(className + methodName + "EXITING");
	}

	public String getSystemPropertyValue(SystemConfigurationService sysConfigSvc, String property){
		String methodName = ":: getSystemPropertyValue ::";
		String value = "";
		SystemProperty sysProperty;
		try{
			sysProperty = sysConfigSvc.getSystemProperty(property);
			value = sysProperty.getPtyValue();
		} catch (SystemConfigurationServiceException e) {
			logger.info(className+methodName+" Exception Occured "+ e);
		}
		return value;
	}

	public boolean checkAttributeInAD(String srchValue, String srchFilterAttr){
		String methodName=":: checkAttributeInAD ::";
		boolean isExists = false;
		DirContext ldapContext = null;
		try{
			MailUtil mailUtil = new MailUtil();
			tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf = Platform.getService(tcITResourceInstanceOperationsIntf.class);
			Hashtable resourceVal = mailUtil.getITResParameterDetails(Constants.AD_IT_RESOURCE_NAME,itResourceInstanceOperationsIntf);

			String ADContainer = (String) resourceVal.get("Container");

			if(ldapContext == null){
				ldapContext = connectToAD(resourceVal);
			}

			String searchFilter = "(" + srchFilterAttr + "=" + srchValue + ")";
			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<javax.naming.directory.SearchResult> results;
			results = ldapContext.search(ADContainer, searchFilter, sc);

			isExists = results.hasMore();
		} catch (NamingException e) {
			logger.info(className+methodName+" Naming Exception Occured "+ e);
		}
		logger.info(className + methodName + " Returning the result as ::"+isExists);
		return isExists;
	}

	/**
	 * Method Name :  checkUserLoginInAD
	 * This method checked the generated User Login by OIM is present in AD or not.
	 * Input Parameter: generated User Login.
	 * return/output: true or false depending on user availability. true if user is available false if user not available
	 */
	public boolean checkUserLoginInAD(String userLogin){
		String methodName=":: checkUserLoginInAD ::";
		boolean isExists = false;
		DirContext ldapContext = null;
		try{
			MailUtil mailUtil = new MailUtil();
			tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf = Platform.getService(tcITResourceInstanceOperationsIntf.class);
			Hashtable resourceVal = mailUtil.getITResParameterDetails(Constants.AD_IT_RESOURCE_NAME,itResourceInstanceOperationsIntf);

			String ADContainer = (String) resourceVal.get("Container");

			if(ldapContext == null){
				ldapContext = connectToAD(resourceVal);
			}
			//checking user for duplicate sAMAccountName
			logger.info(className + methodName + " Checking user for duplicate sAMAccountName");
			String searchFilter = "(sAMAccountName=" + userLogin + ")";  
			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<javax.naming.directory.SearchResult> results;
			results = ldapContext.search(ADContainer, searchFilter, sc);
			isExists = results.hasMore();
			//checking user for duplicate email id
			if(isExists == false){
				logger.info(className + methodName + " Checking user for duplicate Email id");
				String usrEmail = userLogin + "@" + Constants.EMAIL_DOMAIN;
				String searchFilter1 = "(mail=" + usrEmail + ")";
				SearchControls sc1 = new SearchControls();
				sc1.setSearchScope(SearchControls.SUBTREE_SCOPE);
				NamingEnumeration<javax.naming.directory.SearchResult> results1;
				results1 = ldapContext.search(ADContainer, searchFilter1, sc1);
				isExists = results1.hasMore();
				//checking user for duplicate proxyAddresses
				if(isExists == false){
					logger.info(className + methodName + " Checking user for duplicate proxyAddresses");
					String proxyAddr = "SMTP:" + usrEmail;
					String searchFilter2 = "(proxyAddresses=" + proxyAddr + ")";
					SearchControls sc2 = new SearchControls();
					sc2.setSearchScope(SearchControls.SUBTREE_SCOPE);
					NamingEnumeration<javax.naming.directory.SearchResult> results2;
					results2 = ldapContext.search(ADContainer, searchFilter2, sc2);
					isExists = results2.hasMore();
					//checking user for duplicate SIP address
					if(isExists == false){
						logger.info(className + methodName + " Checking user for duplicate SIP Addresses");
						String sipAddr = "sip:" + usrEmail;
						String searchFilter3 = "(msRTCSIP-PrimaryUserAddress=" + sipAddr + ")";
						SearchControls sc3 = new SearchControls();
						sc3.setSearchScope(SearchControls.SUBTREE_SCOPE);
						NamingEnumeration<javax.naming.directory.SearchResult> results3;
						results3 = ldapContext.search(ADContainer, searchFilter3, sc3);
						isExists = results3.hasMore();
					}
				}
			}
		} catch (NamingException e) {
			logger.info(className+methodName+" Naming Exception Occured "+ e);
		}
		logger.info(className + methodName + " Returning the result as ::"+isExists);
		return isExists;
	}
	
	/**
	 * Method Name :  checkUserEmailInAD
	 * This method checks the generated User Email Address by OIM is present in AD or not.
	 * Input Parameter: generated User Email Address.
	 * return/output: true or false depending on user availability. true if user is available false if user not available
	 */
	public boolean checkUserEmailInAD(String userLogin){
		String methodName=":: checkUserEmailInAD ::";
		boolean isExists = false;
		DirContext ldapContext = null;
		try{
			MailUtil mailUtil = new MailUtil();
			tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf = Platform.getService(tcITResourceInstanceOperationsIntf.class);
			Hashtable resourceVal = mailUtil.getITResParameterDetails(Constants.AD_IT_RESOURCE_NAME,itResourceInstanceOperationsIntf);

			String ADContainer = (String) resourceVal.get("Container");

			if(ldapContext == null){
				ldapContext = connectToAD(resourceVal);
			}
			//checking user for duplicate sAMAccountName
			logger.info(className + methodName + " Checking user for duplicate sAMAccountName");
			String searchFilter = "(sAMAccountName=" + userLogin + ")";
			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<javax.naming.directory.SearchResult> results;
			results = ldapContext.search(ADContainer, searchFilter, sc);
			isExists = results.hasMore();
			//checking user for duplicate mailNickname
			if(isExists == false){
				logger.info(className + methodName + " Checking user for duplicate mailNickname");
				String searchFilter4 = "(mailNickname=" + userLogin + ")";
				SearchControls sc4 = new SearchControls();
				sc4.setSearchScope(SearchControls.SUBTREE_SCOPE);
				NamingEnumeration<javax.naming.directory.SearchResult> results4;
				results4 = ldapContext.search(ADContainer, searchFilter4, sc4);
				isExists = results4.hasMore();
				//checking user for duplicate email id
				if(isExists == false){
					logger.info(className + methodName + " Checking user for duplicate Email id");
					String usrEmail = userLogin + "@" + Constants.EMAIL_DOMAIN;
					String searchFilter1 = "(mail=" + usrEmail + ")";
					SearchControls sc1 = new SearchControls();
					sc1.setSearchScope(SearchControls.SUBTREE_SCOPE);
					NamingEnumeration<javax.naming.directory.SearchResult> results1;
					results1 = ldapContext.search(ADContainer, searchFilter1, sc1);
					isExists = results1.hasMore();
					//checking user for duplicate proxyAddresses
					if(isExists == false){
						logger.info(className + methodName + " Checking user for duplicate proxyAddresses");
						String proxyAddr = "SMTP:" + usrEmail;
						String searchFilter2 = "(proxyAddresses=" + proxyAddr + ")";
						SearchControls sc2 = new SearchControls();
						sc2.setSearchScope(SearchControls.SUBTREE_SCOPE);
						NamingEnumeration<javax.naming.directory.SearchResult> results2;
						results2 = ldapContext.search(ADContainer, searchFilter2, sc2);
						isExists = results2.hasMore();
						if(isExists == true){  ////changed code... taken from saMAccountNae
							isExists = false;
							logger.info(className + methodName + " Returning the result as :: " + isExists);
							return isExists;
						}
						//checking user for duplicate SIP address
						if(isExists == false){
							logger.info(className + methodName + " Checking user for duplicate SIP Addresses");
							String sipAddr = "sip:" + usrEmail;
							String searchFilter3 = "(msRTCSIP-PrimaryUserAddress=" + sipAddr + ")";
							SearchControls sc3 = new SearchControls();
							sc3.setSearchScope(SearchControls.SUBTREE_SCOPE);
							NamingEnumeration<javax.naming.directory.SearchResult> results3;
							results3 = ldapContext.search(ADContainer, searchFilter3, sc3);
							isExists = results3.hasMore();
						}
					}
				}
			}
		} catch (NamingException e) {
			logger.info(className+methodName+" Naming Exception Occured "+ e);
		}
		logger.info(className + methodName + " Returning the result as :: " + isExists);
		return isExists;
	}

	/*public String isInitialLoad(){
		String methodName = ":: isInitialLoad ::";
		String isInitialLoad = "FALSE";
		try {
			SystemConfigurationService sysConfigSvc = Platform.getService(SystemConfigurationService.class);
			isInitialLoad = getSystemPropertyValue(sysConfigSvc, Constants.INITIAL_LOAD_SYSTEM_PROPERTY);
			logger.info(className+methodName+" Is it an Initial Laod "+ isInitialLoad);
		} catch (Exception e) {
			logger.info(className+methodName+" Exception Occured "+ e);
		}
		return isInitialLoad;
	}*/

	public String getADAttribute(String srchValue, String srchFilterAttr, String retAttribute){
		String methodName=":: getADAttribute ::";
		logger.info(className + methodName + " ENTERING ");
		DirContext ldapContext = null;
		String result = "";
		try{
			MailUtil mailUtil = new MailUtil();
			tcITResourceInstanceOperationsIntf itResourceInstanceOperationsIntf = Platform.getService(tcITResourceInstanceOperationsIntf.class);
			Hashtable resourceVal = mailUtil.getITResParameterDetails(Constants.AD_IT_RESOURCE_NAME,itResourceInstanceOperationsIntf);
			String ADContainer = (String) resourceVal.get("Container");

			if(ldapContext == null){
				ldapContext = connectToAD(resourceVal);
			}

			String searchFilter = "(" + srchFilterAttr + "=" + srchValue + ")";
			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

			//Specify the attributes to return
			String returnedAtts[]={retAttribute};
			sc.setReturningAttributes(returnedAtts);

			NamingEnumeration<javax.naming.directory.SearchResult> results;
			results = ldapContext.search(ADContainer, searchFilter, sc);

			if(results.hasMoreElements()){
				SearchResult sr = (SearchResult)results.next();
				Attributes attrs = sr.getAttributes();
				result = (String) attrs.get(retAttribute).get();
				//result = attrs.get(retAttribute).toString();
				logger.info(className + methodName + " Attribute from LDAP "+ result);
			}
		} catch (NamingException e) {
			logger.info(className+methodName+" Naming Exception Occured "+ e);
		} finally{
			try {
				ldapContext.close();
			} catch (NamingException e) {
				logger.info(className + methodName + " NamingException occurs "+ e);
			}
		}
		return result;
	}

	public boolean checkEntitlementIfNotRegistered(ApplicationInstance appInst, String appName, String entitlement) {
		String methodName = ":: checkEntitlementIfNotRegistered ::";
		boolean entExists = false;
		try {
			logger.info(className+methodName+"Entitlement to check ::"+entitlement);
			Long svrKey = appInst.getItResourceKey();
			SearchCriteria srchCriteria = new SearchCriteria(Entitlement.ENTITLEMENT_ITRESOURCEKEY,svrKey, SearchCriteria.Operator.EQUAL);
			HashMap configParams = new HashMap();
			EntitlementService entSvc = Platform.getService(EntitlementService.class);
			List<Entitlement> entList = entSvc.findEntitlements(srchCriteria, configParams);
			logger.info(className+methodName+"No. of ent found ::"+entList.size());
			for(int j=0;j<entList.size();j++){
				logger.info(className+methodName+" Entitlement "+j);
				Entitlement ent = entList.get(j);
				String entValue = ent.getEntitlementCode();
				logger.info(className+methodName+"Entitlement Value ::"+entValue);
				if(entValue.equalsIgnoreCase(entitlement)){
					entExists = true;
					break;
				}
			}
		} catch (GenericEntitlementServiceException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.getMessage();
		}
		return entExists;
	}

	/**
	 * Method Name :  MoveToBackup
	 * This method moves feeds from input/out dir to backup dir.
	 * Input Parameter: file name , path of input/out and backupdir.
	 * return/output: moves file.
	 */
	public void moveToBackup(String inputOutLocation,String backupLocation) {
		Date date = new Date();
		logger.info(" :ENTERING: Method Name: FileHandling#MoveToBackup()");
		File file = new File(inputOutLocation);
		File dir = new File(backupLocation);

		Calendar cal = Calendar.getInstance();
		Date newdate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String todayDate = sdf.format(newdate);
		logger.info("Final Date to attch with the file name::"+todayDate);

		// Move file to a new directory
		boolean success = file.renameTo(new File(dir, file.getName() + "_" + todayDate));
		if (success) {
			logger.info("File was successfully moved.\n");
		} else {
			logger.info("File was not successfully moved.\n");
		}
		logger.info(" :END: Method Name:  FileHandling#MoveToBackup()");
	}

	/**
	 * Method Name :  connectToAD
	 * This method connects to Active Directory.
	 * Input Parameter: AD IT Resource values.
	 * return/output: connected AD's context
	 */
	public DirContext connectToAD(Hashtable resourceVal){
		String methodName=":: connectToAD ::";
		logger.info(className + methodName + "Entering..");
		DirContext ldapContext = null;
		try {
			String keystore = Constants.KEY_STORE_PATH;
			System.setProperty("javax.net.ssl.trustStore",keystore);

			String ADIP = (String) resourceVal.get("DomainName");

			String ADAdmin = (String) resourceVal.get("DirectoryAdminName");
			String ADPasswd = (String) resourceVal.get("DirectoryAdminPassword");
			String isSSL = (String) resourceVal.get("UseSSL");
			String port = "";
			if (isSSL.toLowerCase().equals("no")){
				port = "389";
			}else{
				port = "636";
			}

			//logger.info(className + methodName + "Connecting to Port ::"+port);
			Hashtable<String, String> ldapEnv = new Hashtable<String, String>();
			ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); 
			ldapEnv.put(Context.PROVIDER_URL,  "ldap://" + ADIP + ":" + port); 

			//logger.info(className + methodName + " LDAP URL  ::"+"ldap://" + ADIP + ":" + port);
			ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); 
			ldapEnv.put(Context.SECURITY_PRINCIPAL, ADAdmin); 
			ldapEnv.put(Context.SECURITY_CREDENTIALS, ADPasswd);
			//specify use of ssl
			ldapEnv.put(Context.SECURITY_PROTOCOL,"ssl");

			ldapContext = new InitialDirContext(ldapEnv);
		} catch (NamingException e) {
			logger.info(className + methodName + "Naming Exception occurs "+e);
		}
		return ldapContext;
	}	

	public String transformDate(String dateValue,String sourceDateFormat){
		String methodName = ":: transformDate ::";
		String targetDateValue = "";
		try
		{
			String targetDateFormat = "yyyy/MM/dd HH:mm:ss z";
			if ((dateValue != null) && (!dateValue.equals(""))) {
				SimpleDateFormat sdfsource = new SimpleDateFormat(sourceDateFormat);
				Date sourceDate = sdfsource.parse(dateValue);
				SimpleDateFormat sdftarget = new SimpleDateFormat(targetDateFormat);
				targetDateValue = sdftarget.format(sourceDate);
				logger.info(methodName+"targetDateValue-------------------->" + targetDateValue);
			}
			return targetDateValue;
		}catch(Exception e){
			logger.info(methodName+"Exception Occurs ::"+e);
		}
		return targetDateValue;
	}
	
	/**
	 * Method Name :  getRecipientType
	 * This method gives the Recipient Type of Exchange Account.
	 * Input Parameter: usrKey.
	 * return/output: Recipient Type of Exchange Account
	 */
	public boolean getRecipientType(Long usrKey){
		String methodName=":: getRecipientType ::";
		logger.info(className + methodName + "Entering..");
		boolean recipentType = false;
		try{
		tcUserOperationsIntf userIntf = Platform.getService(tcUserOperationsIntf.class);
		tcFormInstanceOperationsIntf formIntf = Platform.getService(tcFormInstanceOperationsIntf.class);
		tcResultSet obResultSet = userIntf.getObjects(usrKey);
		if (obResultSet.isEmpty()){
			logger.info(className + methodName +"User has no provisioned objects");
		}else{
			for(int j=0; j<obResultSet.getRowCount(); j++){
				obResultSet.goToRow(j);
				if ((obResultSet.getStringValue("Objects.Name").equals("Exchange User")) &&
						(!(obResultSet.getStringValue("Objects.Object Status.Status").equals("Revoked")) &&
								!(obResultSet.getStringValue("Objects.Object Status.Status").equals("Provisioning")))){
					long plProcessInstanceKey = obResultSet.getLongValue("Process Instance.Key");
					tcResultSet resultSet=formIntf.getProcessFormData(plProcessInstanceKey);
					if (resultSet == null || resultSet.isEmpty()) {
						logger.info(className + methodName +"ResultSet is Empty or null");
						return recipentType;
					}
					int rowCount = resultSet.getRowCount();
					for (int k = 0; k < rowCount; k++) {
						resultSet.goToRow(k);
						String[] columnNames = resultSet.getColumnNames();
						for (int i = 0; i < columnNames.length; i++) {
							if(columnNames[i].equalsIgnoreCase("UD_EXCHANGE_RECIPIENTTYPE")){
								logger.info(className + methodName + "Recipient type: " + resultSet.getStringValue(columnNames[i]));
								if(resultSet.getStringValue(columnNames[i]).equalsIgnoreCase("UserMailbox")){
									recipentType = true;
									return recipentType;
								}
							}
						}
					}
				}
			}
		}
		}catch (Exception e) {
			logger.info(className + methodName + e.getMessage());
		}
		return recipentType;
	}
	
}
