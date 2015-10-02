/**
 * This class would deal with all type of Validations for EBS HRIS Recon process
 */
package com.ul.process.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcLookupOperationsIntf;

import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.connectors.common.ConnectorException;
import oracle.iam.connectors.common.validate.Validator;
import oracle.iam.platform.Platform;

/**
 * @author Zubair
 *
 */
public class MasterValidation implements Validator{

	public MasterValidation(){
		
	}
	
	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");
	tcLookupOperationsIntf lookup =null;

	public boolean validate(HashMap hmUserDetails, HashMap hmEntitlementDetails, String field) throws ConnectorException {
		LOGGER.info("MasterValidation.validate() Entering the method");
		boolean isPassed= true;
				
		String sUserID=(String) hmUserDetails.get(field);
		String sFirstName=(String) hmUserDetails.get("First Name");
		String sLastName=(String) hmUserDetails.get("Last Name");
		String sEmployeeNumber=(String) hmUserDetails.get("Employee Number");
		String sLocation=(String) hmUserDetails.get("Location");
		String sSupervisorEmpNumber=(String) hmUserDetails.get("Supervisor Emp Number");
		String sRecordCreatedby=(String) hmUserDetails.get("User Record Createdby");
                
		DataSource ds = Platform.getOperationalDS();
		ValidationUtil valUtil = new ValidationUtil();
		
		// Removing email filter as per TFS:30150
		boolean isDuplicate=false;
		//boolean isEmailEmpty=false;
		boolean isDateEmpty=false;
		boolean isFirstNameEmpty = false;
		boolean isEmployeeNumberEmpty = false;
		boolean isLocationEmpty = false;
		boolean isSupervisorEmpNumberEmpty = false;
                int counter = 0;
	        String[] attributeList = new String[7];
                boolean isStrArrayEmpty = true;
		
		
		StringBuilder emailBody = new StringBuilder();
		
		if (isNewHire(sUserID,sFirstName,sLastName,ds)){
			LOGGER.info("MasterValidation.validate() since New Hire, all the validation will be done");
                        isDuplicate=isUIDDuplicate(hmUserDetails,ds);
			 //isEmailEmpty=isEmailEmpty(hmUserDetails);
                        isDateEmpty=isProjEndDateEmpty(hmUserDetails);
                        
                        if (isDuplicate){
                            LOGGER.info("Employee Number is Duplicate adding in List");
                            attributeList[counter] = "Duplicate";
                            counter=counter+1;
                        }
                        if(isDateEmpty){
                            LOGGER.info("Projected Date is Empty adding in List");
                            attributeList[counter] = "DateEmpty";
                            counter=counter+1;
                        }
                        if(sFirstName ==null || sFirstName.isEmpty()){
                            LOGGER.info("Employee First Name is empty, adding in List");
                            isFirstNameEmpty = true;
                            attributeList[counter] = "FirstName";
                            counter=counter+1;
                            
                        }
                        if(sEmployeeNumber ==null || sEmployeeNumber.isEmpty()){
                            LOGGER.info("Employee number is empty, adding in List");
                            isEmployeeNumberEmpty = true;
                            attributeList[counter] = "EmployeeNumber";
                            counter=counter+1;
                        }
                        if(sLocation ==null || sLocation.isEmpty()){
                            LOGGER.info("Employee Location is empty, adding in List");
                            isLocationEmpty = true;
                            attributeList[counter] = "Location";
                            counter=counter+1;
                        }
                        if(sSupervisorEmpNumber ==null || sSupervisorEmpNumber.isEmpty()){
                            LOGGER.info("Employee Supervisor Employee Number is empty, adding in List");    
                            isSupervisorEmpNumberEmpty = true;
                            attributeList[counter] = "SupervisorEmpNumber";
                            counter=counter+1;
                        }
			
                        isStrArrayEmpty = isStringArrayEmpty(attributeList);
		        LOGGER.info("String Array attributeList is empty " + isStrArrayEmpty);
                
                        if(!isStrArrayEmpty){
                            LOGGER.info("Validation List is not empty");
                            String decodeValueString=valUtil.getReminderDate();
                            decodeValueString = decodeValueString +","+sRecordCreatedby;
                            for(int i=0;i<attributeList.length;i++){
                                if(attributeList[i]=="Duplicate"){
                                    //TO DO: send email to the hr
                                    emailBody.append("<b>");
                                    emailBody.append("-Employee ID already in use");
                                    emailBody.append("<b>");
                                    decodeValueString = decodeValueString + "," + "-Employee ID already in use";
                                }
                                if(attributeList[i]=="DateEmpty"){
                                    //TO DO: send email to the hr
                                    emailBody.append("<b>");
                                    emailBody.append("-Projected End Date is missing or has past date");
                                    emailBody.append("<b>");
                                    decodeValueString = decodeValueString + "," + "-Projected End Date is missing or has past date";
                                }
                                if(attributeList[i]=="FirstName"){
                                    //TO DO: send email to the hr
                                    emailBody.append("<b>");
                                    emailBody.append("-First Name is missing");
                                    emailBody.append("<b>");
                                    decodeValueString = decodeValueString + "," + "-First Name is missing";
                                }
                                if(attributeList[i]=="EmployeeNumber"){
                                    //TO DO: send email to the hr
                                    emailBody.append("<b>");
                                    emailBody.append("-Employee Number is missing");
                                    emailBody.append("<b>");
                                    decodeValueString = decodeValueString + "," + "-Employee Number is missing";
                                }
                                
                                if(attributeList[i]=="Location"){
                                    //TO DO: send email to the hr
                                    emailBody.append("<b>");
                                    emailBody.append("-Location is missing");
                                    emailBody.append("<b>");
                                    decodeValueString = decodeValueString + "," + "-Location is missing";
                                }
                                
                                if(attributeList[i]=="SupervisorEmpNumber"){
                                    //TO DO: send email to the hr
                                    emailBody.append("<b>");
                                    emailBody.append("-Supervisor Employee Number is missing");
                                    emailBody.append("<b>");
                                    decodeValueString = decodeValueString + "," + "-Supervisor Employee Number is missing";
                                }
                            }
                        
                            valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
                            valUtil.populateLookup(sUserID,decodeValueString,"Lookup.Validation.Reminder");
                            isPassed=false;
                            
                        }
			/*if (isDuplicate && isEmailEmpty && isDateEmpty){
				LOGGER.info("MasterValidation.validate() All 3 cases detected for the user "+sUserID);
				// TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Email Missing");
				emailBody.append("<b>");
				emailBody.append("<br>");
				emailBody.append("<b>");
				emailBody.append("-Employee ID already in use");
				emailBody.append("<b>");
				emailBody.append("<br>");
				emailBody.append("<b>");
				emailBody.append("-Projected End Date is missing or has past date");
				emailBody.append("<b>");
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				
				// Adding entry into Lookup.Validation.Reminder
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Email Missing"+","+"-Employee ID already in use"+","+"-Projected End Date is missing or has past date";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				return false;
				
			}else if(isDuplicate && isEmailEmpty){
				LOGGER.info("MasterValidation.validate() Duplicate ID and empty email cases detected for the user "+sUserID);
                //TO DO: send email to the hr
				//TO DO: populate lookup for reminder email
				emailBody.append("<b>");
				emailBody.append("-Email Missing");
				emailBody.append("<b>");
				emailBody.append("<br>");
				emailBody.append("<b>");
				emailBody.append("-Employee ID already in use");
				emailBody.append("<b>");
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				//Adding entry into Lookup.Validation.Reminder
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Email Missing"+","+"-Employee ID already in use";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				return false;
				
			}*/
			/* if (isDuplicate && isDateEmpty){
				LOGGER.info("MasterValidation.validate() Duplicate ID and empty project end date cases detected for the user "+sUserID);
				//TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Employee ID already in use");
				emailBody.append("<b>");
				emailBody.append("<br>");
				emailBody.append("<b>");
				emailBody.append("-Projected End Date is missing or has past date");
				emailBody.append("<b>");
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Projected End Date is missing or has past date"+","+"-Employee ID already in use";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				
				return false;
				
			}*/
			 /*else if (isEmailEmpty && isDateEmpty){
				LOGGER.info("MasterValidation.validate() empty email and empty project end date cases detected for the user "+sUserID);
				//TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Email Missing");
				emailBody.append("<b>");
				emailBody.append("<br>");
				emailBody.append("<b>");
				emailBody.append("-Projected End Date is missing or has past date");
				emailBody.append("<b>");
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Projected End Date is missing or has past date"+","+"-Email Missing";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				return false;
				
			}else if (isEmailEmpty){
				LOGGER.info("MasterValidation.validate() empty email case detected for the user "+sUserID);
				//TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Email Missing");
				emailBody.append("<b>");
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Email Missing";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				
				isPassed=false;
			}*/	
			/*else if (isDuplicate){
				LOGGER.info("MasterValidation.validate() duplicate id case detected for the user "+sUserID);
				//TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Employee ID already in use");
				emailBody.append("<b>");
				
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Employee ID already in use";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				
				isPassed=false;
				
			}else if (isDateEmpty){
				LOGGER.info("MasterValidation.validate() empty project end date case detected for the user "+sUserID);
				//TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Projected End Date is missing or has past date");
				emailBody.append("<b>");
				
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Projected End Date is missing or has past date";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				
				isPassed=false;
			} */
		}
		
		else{
			LOGGER.info("MasterValidation.validate() since user exists only duplcate id check will be done");
			isDuplicate=isUIDDuplicate(hmUserDetails,ds);
			if (isDuplicate){
				LOGGER.info("MasterValidation.validate() duplicate id case detected for the user "+sUserID);
				//TO DO: send email to the hr
				emailBody.append("<b>");
				emailBody.append("-Employee ID already in use");
				emailBody.append("</b>");
				
				valUtil.sendNotification(sFirstName,sLastName,sUserID,sRecordCreatedby,emailBody.toString());
				String decodeValue=valUtil.getReminderDate()+","+sRecordCreatedby+","+"-Employee ID already in use";
				valUtil.populateLookup(sUserID,decodeValue,"Lookup.Validation.Reminder");
				isPassed=false;
				
			}
		}
			
		LOGGER.info("Status of Validation is " + isPassed);	
		return isPassed;
	}
	
	
	private boolean isNewHire(String userID,String firstName,String lastName,DataSource ds){
		boolean isnewHire= false;
		PreparedStatement pstmt=null;
		Connection con= null;
		ResultSet rs = null;
		try {
			con = ds.getConnection();
			LOGGER.info("MasterValidation.isNewHire() Successfully connected to DB");
			
			//String query = "select 1 from usr where usr_login =? and usr_first_name=? and usr_last_name=?";
			String query = "select 1 from usr where usr_login =? ";
			
			pstmt = con.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			pstmt.setString(1,userID);
			//pstmt.setString(2,firstName);
			//pstmt.setString(3,lastName);
			rs= pstmt.executeQuery();
			
			if (getRowCount(rs)== 0){
				isnewHire=true;
				LOGGER.info("MasterValidation.isNewHire() Its a new hire case-->empid-->"+userID);
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeDBResources(con,pstmt,rs);
		}
		
		
		return isnewHire;
	}
	
	private boolean isUIDDuplicate(HashMap hmUserDetails,DataSource ds){
		LOGGER.info("MasterValidation.isUIDDuplicate() Entering the method");
		boolean isDuplicate=false;
		PreparedStatement pstmt=null;
		Connection con= null;
		ResultSet rs = null;
		
		String sFirstName=(String) hmUserDetails.get("First Name");
		String sLastName=(String) hmUserDetails.get("Last Name");
		String sUserID=(String) hmUserDetails.get("User ID");
		String sPersonType=(String) hmUserDetails.get("Person Type");
		String sBusinessGrpID=(String) hmUserDetails.get("Business Group ID");
		LOGGER.info("MasterValidation.isUIDDuplicate() MapFirstName MapLastName "+sFirstName+" "+sLastName);
		
		try {
			con = ds.getConnection();
			//String query = "select usr_login, usr_first_name, usr_last_name from usr where USR_UDF_PERSON_ID =?";
			String query = "select usr_login, usr_first_name, usr_last_name, USR_UDF_BGID,USR_UDF_USR_PERSONTYPE  from usr where usr_login =?";
			LOGGER.info("MasterValidation.isUIDDuplicate() query->"+query);
			
			pstmt = con.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			pstmt.setString(1,sUserID);
			rs= pstmt.executeQuery();
			
			if(rs!=null){
				
				rs.last();
				if (rs.getRow()!=0){
					rs.beforeFirst();
					LOGGER.info("MasterValidation.isUIDDuplicate() user prsent in OIM ");
					rs.next();
					//LOGGER.info("MasterValidation.isUIDDuplicate() First Name and Last Name found in OIM "+rs.getString(2)+"<->"+rs.getString(3));
					String firstName = rs.getString("usr_first_name");
					String lastName = rs.getString("usr_last_name");
					String bgID = rs.getString("USR_UDF_BGID");
					String personType = rs.getString("USR_UDF_USR_PERSONTYPE");
					String uid = rs.getString("usr_login");
					
					LOGGER.info("MasterValidation.isUIDDuplicate() comparing uids for the personid "+uid+"<->"+sUserID);
					if (!StringUtils.isBlank(sBusinessGrpID) && !StringUtils.isBlank(bgID)){
						if (!sBusinessGrpID.equalsIgnoreCase(bgID)){
							LOGGER.info("MasterValidation.isUIDDuplicate() comparing business grp id "+sBusinessGrpID+"<->"+bgID);
							if (!lastName.equalsIgnoreCase(sLastName) || !firstName.equalsIgnoreCase(sFirstName)){
								LOGGER.info("MasterValidation.isUIDDuplicate() Duplcate ID Case detected ");
								isDuplicate = true;
							}
						}
					}else{
						LOGGER.info("MasterValidation.isUIDDuplicate() checking for personType case");
						if (!sPersonType.equalsIgnoreCase(personType)){
							LOGGER.info("MasterValidation.isUIDDuplicate() Person Type Doesn't match");
							if (!lastName.equalsIgnoreCase(sLastName) || !firstName.equalsIgnoreCase(sFirstName)){
								LOGGER.info("MasterValidation.isUIDDuplicate() Duplcate ID Case detected ");
								isDuplicate = true;
							}
						}
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeDBResources(con,pstmt,rs);
		}
				
		LOGGER.info("MasterValidation.isUIDDuplicate() Exiting the method");
		return isDuplicate;
	}
	
	
	private boolean isEmailEmpty(HashMap hmUserDetails){
		
		LOGGER.info("MasterValidation.isEmailEmpty() Entering the method");
		boolean isEmpty=false;
		String email=null;
		email= (String) hmUserDetails.get("Email Address");
		String sUserType= (String) hmUserDetails.get("Person Type");
		String contactType= getLookupValue("Lookup.Email.ITAdmin","contact");
		String [] contractor = contactType.split(",");
		LOGGER.info("MasterValidation.isEmailEmpty() email is "+email);
		
		for (int i=0; i< contractor.length; i++){
			if (sUserType.equalsIgnoreCase(contractor[i])){
				
				if(email ==null || email.isEmpty()){
					
					LOGGER.info("MasterValidation.isEmailEmpty() email field is empty");
					isEmpty=true;
					break;
				}
			}
			
		}
		LOGGER.info("MasterValidation.isEmailEmpty() Exiting the method");
		return isEmpty;
	}
	/**
	 * put a check to have projected end date geater than today apart from validating 
	 * whether it is empty.
	 * @param hmUserDetails
	 * @return
	 */
	private boolean isProjEndDateEmpty(HashMap hmUserDetails){
		LOGGER.info("MasterValidation.isProjEndDateEmpty() Entering the method");
		
		boolean isDateEmpty=false;
		Object projectedEndDate=null;
		Date projEndDate=null;
		java.sql.Date penDate=null;
		projectedEndDate= (Object) hmUserDetails.get("Projected End Date");
		String sUserType= (String) hmUserDetails.get("Person Type");
		//java.sql.Date pendDt=(java.sql.Date) ((Date) hmUserDetails.get("Projected End Date")==null ? null:(Object) hmUserDetails.get("Projected End Date")) ;
		if (!sUserType.equalsIgnoreCase("Employee") && !sUserType.equalsIgnoreCase("Intern")){
			LOGGER.info("MasterValidation.isProjEndDateEmpty() User is non employee");
			
			if( projectedEndDate ==null || "".equals(projectedEndDate)){
				LOGGER.info("MasterValidation.isProjEndDateEmpty() Projected End Date is empty "+projectedEndDate);
				isDateEmpty=true;								
			}else{
				LOGGER.info("MasterValidation.isProjEndDateEmpty() checking if date is in future");
				
				if (projectedEndDate instanceof Date){
					projEndDate= (Date) projectedEndDate;
					Date today = new Date();
					if(today.after(projEndDate)){
						LOGGER.info("MasterValidation.isProjEndDateEmpty()-- date-- Projected End Date is in past");
						isDateEmpty=true;	
					}
				}
				if (projectedEndDate instanceof java.sql.Date){
					penDate = (java.sql.Date) projectedEndDate;
					Date today = new Date();
					if(today.after(penDate)){
						LOGGER.info("MasterValidation.isProjEndDateEmpty()--sql date-- Projected End Date is in past");
						isDateEmpty=true;	
					}
				} if (projectedEndDate instanceof String){
					String strDate= (String) projectedEndDate;
					SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
					try {
						Date dt= df.parse(strDate);
						Date today = new Date();
						if(today.after(dt)){
							LOGGER.info("MasterValidation.isProjEndDateEmpty()--string-- Projected End Date is in past");
							isDateEmpty=true;	
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			}
		}
		LOGGER.info("MasterValidation.isProjEndDateEmpty() Exiting the method");
		return isDateEmpty;
	}
	
	public int getRowCount(ResultSet rs){
		int i=0;
		
		try {
			if(rs!=null){
				if(rs.last()){
					i=rs.getRow();
					rs.beforeFirst();
				}
			}
			LOGGER.info(" MasterValidation.getRowCount() the row count is "+i);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			closeDBResources(null,null,rs);
		}
		return i;
	}
	
	private void closeDBResources(Connection con, PreparedStatement prepStmt,
			ResultSet resultSet) {
		
		try {
			if (con != null) {
				con.close();
				con = null;
			}
			if (prepStmt != null) {
				prepStmt.close();
				prepStmt = null;
			}
			if (resultSet != null) {
				resultSet.close();
				resultSet = null;
			}
			
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

        private boolean isStringArrayEmpty(String[] strArray){ 
            boolean empty = true;
            for (int i=0; i<strArray.length; i++) {
              if (strArray[i] != null) {
                empty = false;
                break;
              }
            }
            return empty;
        }
	public String getLookupValue(String lookupName, String lookupCodKey) {
		String methodName = "getLookupValue() ";
		lookup = Platform.getService(tcLookupOperationsIntf.class);
		
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
					LOGGER.info(methodName + "The value of Decodekey "
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

}
