package com.ul.scheduletask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;  
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;  
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;  
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.text.StrSubstitutor;























import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.thortech.xl.client.dataobj.tcDataBaseClient;
import com.thortech.xl.dataaccess.tcDataProvider;
import com.thortech.xl.dataaccess.tcDataSet;

import Thor.API.tcResultSet;
import Thor.API.Exceptions.tcAPIException;
import Thor.API.Exceptions.tcColumnNotFoundException;
import Thor.API.Exceptions.tcInvalidLookupException;
import Thor.API.Operations.tcEmailOperationsIntf;
import Thor.API.Operations.tcITResourceInstanceOperationsIntf;
import Thor.API.Operations.tcLookupOperationsIntf;
import Thor.API.Security.XLClientSecurityAssociation;
import oracle.core.ojdl.logging.ODLLogger;
import oracle.iam.identity.usermgmt.api.UserManager;  
import oracle.iam.identity.usermgmt.api.UserManagerConstants;
import oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName;  
import oracle.iam.identity.usermgmt.vo.User;  
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.OIMInternalClient;
import oracle.iam.platform.Platform;  
import oracle.iam.request.vo.Beneficiary;  
import oracle.iam.request.vo.RequestData;  
import oracle.iam.platform.Platform;
import oracle.iam.scheduler.vo.TaskSupport;

public class SchedulerDataDiscrepancyReport  extends TaskSupport {

	private static final ODLLogger LOGGER = ODLLogger.getODLLogger("UL.CUSTOM");

	static String classname="SchedulerDataDiscrepancyReport.";
	//public final static String server = "ftp://tgftp.nws.noaa.gov/data/observations/metar/stations";
	public static final int EBS_COLUMN_FIRSTNAME = 2;
    public static final int EBS_COLUMN_LASTNAME = 3;
    public static final int EBS_COLUMN_EMAIL = 4;
    public static final int EBS_COLUMN_BGID = 10;
    public static final int EBS_COLUMN_DEPARTMENT = 16;
    public static final int EBS_COLUMN_EFFENDDATE = 10;

    public static final int OIM_COLUMN_FIRSTNAME = 2;
    public static final int OIM_COLUMN_LASTNAME = 1;
    public static final int OIM_COLUMN_EMAIL = 10 ;
    public static final int OIM_COLUMN_BGID = 16;
    public static final int OIM_COLUMN_DEPARTMENT = 15;
    public static final int OIM_COLUMN_STATUS = 6;

    public static final String OIM_PRIMARY_KEY = "USR_LOGIN";
    public static final String EBS_PRIMARY_KEY = "EMPLOYEE_NUMBER";

	public void execute(HashMap hm) throws Exception 
	{
		
		String methodName="execute";

		try
        {
				//New Code
				String path=null;
				String fileEBS=null;
			    String fileOIM=null;
			    String fileOutput=null;
			    
			    
		       // Scanner input = new Scanner(System.in);
		        LOGGER.info(classname + methodName+ "Enter Path");
				path = (String) hm.get("File Location Path");
				LOGGER.info(classname + methodName+ "Enter EBS FIle");
				fileEBS = (String) hm.get("EBS Data File");
				LOGGER.info(classname + methodName+ "Enter OIM File");
				fileOIM = (String) hm.get("OIM Data File");
				LOGGER.info(classname + methodName+ "Enter Output File");
				fileOutput = (String) hm.get("Output File");
				
				
				/*
				path="C:\\Users\\Vrushank\\datadump\\";
			    fileEBS="EBSSITEXPORTUpdated.csv";
			    fileOIM="OIMSITEXPORT.csv";
				//String file1="EBSSITEXPORT.csv";
			    //String file2="OIMSITEXPORT.csv";
			    fileOutput="Result.csv";
			    */
		        // creates a FileWriter Object
				
				//lookup.datareport.columns
				
				//NEW
				
		        FileWriter writer = new FileWriter(path+fileOutput); 
		       	
			    Multimap<String, String> myMultimapEBS = ArrayListMultimap.create();

	        	//Read from file
	            //LOGGER.info(classname + methodName+ classname + methodName+ "----->>>>        " + " Read the file1");
	            BufferedReader in = new BufferedReader(new FileReader(path+fileEBS));
	            String strinputebs=null;
	            boolean ebsAttributeRow=false;
	        	HashMap<String, Integer> ebsAttributeMap = new HashMap<String, Integer>();

	            while((strinputebs = in.readLine()) != null)
	            {
	            	//LOGGER.info(strinputebs+"\r\n");
	            	//LOGGER.info(classname + methodName+ "Inside Reading File");
	               	  String str = strinputebs.replace("\"", "");
	  	        	//LOGGER.info(str+"\r\n");
	            	if(ebsAttributeRow == false)
	            	{
		            	 String ebsAttributesNames[] = str.split("\\|");
		   			    //System.out.println(Attributes1.length);
		   			    for(int n=0 ; n < ebsAttributesNames.length ; n++ )
		   			    {
		   			    	ebsAttributeMap.put(ebsAttributesNames[n],n);
		   			    	//LOGGER.info(classname + methodName+ "Inside EBS Reading File");

		   			    }
		   			    ebsAttributeRow= true;
	            	}
	            	else
	            	{	
	            		//LOGGER.info(classname + methodName+ "Inside Reading File 1");
  			    	String strEBS[] = str.split("\\|");
  			    	//LOGGER.info("EBS primary Key" + ebsAttributeMap.get(EBS_PRIMARY_KEY));
  			    	myMultimapEBS.put(strEBS[ebsAttributeMap.get(EBS_PRIMARY_KEY)],str);
  			    	//myMultimapEBS.put(strEBS[9],str);
   			    
	            	}

	             //   LOGGER.info(classname + methodName+ classname + methodName+ "----->>>>        " + " Str Value"+str +"\n");
	    			
	            }
			  // Adding some key/value
			   
			  // Getting the size
			  int size = myMultimapEBS.size();
			  
			  Multimap<String, String> myMultimapOIM = ArrayListMultimap.create();
				
			 
	            BufferedReader in1 = new BufferedReader(new FileReader(path+fileOIM));
	            String strinputoim=null;
	            boolean oimAttributeRow=false;
	        	HashMap<String, Integer> oimAttributeMap = new HashMap<String, Integer>();
	        	
	            while((strinputoim = in1.readLine()) != null)
	            {
	            	//LOGGER.info(strinputoim+"\r\n");
	            	String str1 = strinputoim.replace("\"", "");
	            	//LOGGER.info(strinputoim+"\r\n");
	            	
	               	  
	            	if(oimAttributeRow == false)
	            	{
		            	 String oimAttributesNames[] = str1.split("\\|");
		   			    //System.out.println(Attributes1.length);
		   			    for(int n=0 ; n < oimAttributesNames.length ; n++ )
		   			    {
		   			    	oimAttributeMap.put(oimAttributesNames[n],n);
		   			 	//LOGGER.info(classname + methodName+ "Inside OIM Reading File");

		   			    }
		   			    oimAttributeRow= true;
	            	}
	            	
	            	else
	            	{
	            		//LOGGER.info(classname + methodName+ "Vrushank");  // 4
	            		String strOIM[] = str1.split("\\|");
	            		//LOGGER.info(classname + methodName+ strOIM);  // 4
	            		//LOGGER.info("OIM primary Key" + oimAttributeMap.get(OIM_PRIMARY_KEY));
	            		myMultimapOIM.put(strOIM[oimAttributeMap.get(OIM_PRIMARY_KEY)],str1);
	            		//myMultimapOIM.put(strOIM[0],str1);
	            	}
	            }

	        	LOGGER.info(classname + methodName+ "After Reading Files");

				Set keySetOIM = myMultimapOIM.keySet();
	            Iterator keyIteratorOIM = keySetOIM.iterator();
//	            LOGGER.info(classname + methodName+ size);  // 4
 		        //writer.write("\"Error_Type\",\"USR_LOGIN\",\"USR_LAST_NAME\",\"USR_FIRST_NAME\",\"MANAGER_EMPLOYEE_NUM\",\"USR_TYPE\",\"USR_DISABLED\",\"USR_STATUS\",\"USR_EMP_TYPE\",\"USR_START_DATE\",\"USR_END_DATE\",\"USR_EMAIL\",\"USR_UDF_SUPERVISOR_ID\",\"USR_UDF_SUPERVISOR_NAME\",\"USR_UDF_JOB\",\"USR_UDF_GRADE\",\"USR_UDF_DEPARTMENT\",\"USR_UDF_BGID\",\"USR_UDF_PERSON_ID\"\r\n") ;
	            writer.write("\"Error_Type\"\\|\"USR_LOGIN\"\\|\"USR_LAST_NAME\"\\|\"USR_FIRST_NAME\"\\|\"MANAGER_EMPLOYEE_NUM\"\\|\"USR_TYPE\"\\|\"USR_DISABLED\"\\|\"USR_STATUS\"\\|\"USR_EMP_TYPE\"\\|\"USR_START_DATE\"\\|\"USR_END_DATE\"\\|\"USR_EMAIL\"\\|\"USR_UDF_SUPERVISOR_ID\"\\|\"USR_UDF_SUPERVISOR_NAME\"\\|\"USR_UDF_JOB\"\\|\"USR_UDF_GRADE\"\\|\"USR_UDF_DEPARTMENT\"\\|\"USR_UDF_BGID\"\\|\"USR_UDF_PERSON_ID\"\r\n") ;
	      		writer.flush();
	 	  		//Modified 
 		        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
 		        String date= dateFormatter.format(new Date());
 		        //Date todayDate = dateFormatter.parse(dateFormatter.format(new Date()));
 		        Date todayDate = new Date();
		        	
 		        while (keyIteratorOIM.hasNext() ) 
	            {	
 		        	//LOGGER.info(classname + methodName+ "Inside Loop");
	                String key = (String) keyIteratorOIM.next();
	                Collection<String> valuesEBS = myMultimapEBS.get(key);
	                Collection<String> valuesOIM = myMultimapOIM.get(key);
	                  String sEBS[] = valuesEBS.toArray(new String[valuesEBS.size()]);
	                  String sOIM[] = valuesOIM.toArray(new String[valuesOIM.size()]);
	                  String AttributesOIM[] = null;
  		        	  String AttributesEBS[] = null;
  		          	   
	                  //LOGGER.info(classname + methodName+ valuesEBS.size());
	                  //NEW CODE 
	                  tcLookupOperationsIntf lookup =null;
	     			 lookup = Platform.getService(tcLookupOperationsIntf.class);
	     			 //String methodName="getLookupValue() ";
	     			 //LOGGER.info(classname + methodName+ "----->>>>        " + "Inside GetLookupValue");
	     	         tcResultSet tcresultSet = lookup.getLookupValues("lookup.datareport.columns");
	     	        if(valuesEBS.size() >= 1 && valuesOIM.size() != 0)
	                {
	     	        	AttributesOIM = sOIM[0].split("\\|");;
	  		        	  AttributesEBS = sEBS[valuesEBS.size()-1].split("\\|");
	  		      
	     	         //LOGGER.info(classname + methodName+ "----->>>>        " + "Successfully got the Lookpup values");
	     	         for (int i = 0; i < tcresultSet.getRowCount(); i++) 
	     	         	{
	     	                  tcresultSet.goToRow(i);
	     	        		  //LOGGER.info(classname + methodName+ "----->>>>  in FOR LOOP      " + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode")+tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"));
	     	        		  //LOGGER.info(classname + methodName+ "----->>>>  in FOR LOOP      " + oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))+ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key")));
	     	        		  //LOGGER.info(classname + methodName+ "----->>>>  in FOR LOOP      " + AttributesOIM[oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))]+AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))]);
	     	                  if(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode").equals("USR_STATUS"))
	     	                  {
	     	                	  String userStatus=null;
	     	                	  /*
	     	                	  //LOGGER.info(classname + methodName+ "EffEndDate"+AttributesEBS[EBS_COLUMN_EFFENDDATE]);
	     	                	  String temp = AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))].replace("\"", "");
	     	                	  //LOGGER.info(classname + methodName+ "TrimmedEffEndDate"+temp);
	     	                	  Date effEndDate = dateFormatter.parse(temp);
	     	                	  //LOGGER.info(classname + methodName+ "todayDate"+todayDate+"effEndDate"+effEndDate);
	     	 	  		        	    	if(todayDate.compareTo(effEndDate)>0)
	     			 	  		        	{
	     			 	  		        		userStatus= "Disabled";
	     			 	  		        	}
	     			 	  		        	else
	     			 	  		        	{
	     			 	  		        		userStatus= "Active";
	     			 	  		        	}
	     			 	  		        	
	     			 	  		   */
	     	                	  //New Code
	     	                	 userStatus= AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))];
	     	                	 if(userStatus.equalsIgnoreCase("Terminated"))
	     	                	 {
	     	                		userStatus = "Disabled";
	     	                	 }
	     	                		 
	     	 	  		        if(!AttributesOIM[oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))].equals(userStatus))
	     	 	  		        	{
	     		  	 		        	LOGGER.info(classname + methodName+ "oim status"+AttributesOIM[OIM_COLUMN_STATUS]+"ebs status"+userStatus);
	     		  	 		        	LOGGER.info(classname + methodName+ "EBS Last Row"+sEBS[valuesEBS.size()-1]+"\r\n");
	     		  	 		        	LOGGER.info(classname + methodName+ "OIM Row"+sOIM[0]+"\r\n");
	     	   		     	 			//writer.write("Status MisMatch"+" \r\n"+sOIM[0]+" \r\n\r\n");
	     		  	 		        writer.write("Status MisMatch"
	     		  	 		        		  +" \r\n"
	     	                				  + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode")
	     	                				  +"---"
	     	                				  + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key")
	     	                				  + " \r\n"
	     	                				  +AttributesOIM[oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))]
			     	                		  +"---"
			     	                		  +AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))]
			     	                		  + " \r\n"
			     	                		  +sOIM[0]
			     	                		  + " \r\n"
			     	                		  + sEBS[valuesEBS.size()-1]
	     	                				  +" \r\n\r\n"); 
	     		  	 		        		writer.flush();
	     	 	  		        		break;
	     	 	  		        	}
 
	     	                  }
	     	                  else
	     	                  {
	     	                	  if(!AttributesOIM[oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))].equals(AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))]))
	     	                	  {
	     	                		  LOGGER.info(classname + methodName+ "MisMatch\r\n");
	     	                		  LOGGER.info(classname + methodName+ "EBS Last Row"+sEBS[valuesEBS.size()-1]+"\r\n");
	     	                		  LOGGER.info(classname + methodName+ "OIM Row"+sOIM[0]+"\r\n");
		     	   		     	 		
	     	   		     	 		  if(valuesEBS.size() > 1)
	     	                		  {
		     	                		  writer.write("Multiple Rows Column MisMatch"
	     	                		              +" \r\n"
		     	                				  + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode")
		     	                				  +"---"
		     	                				  + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key")
		     	                				  + " \r\n"
		     	                				  +AttributesOIM[oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))]
				     	                		  +"---"
				     	                		  +AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))]
				     	                		  + " \r\n"
				     	                		  +sOIM[0]
				     	                		  + " \r\n"
				     	                		  + sEBS[valuesEBS.size()-1]
		     	                				  +" \r\n\r\n"); 
		     	                		  writer.flush();
		     	                		  break;
	     	                		  }
	     	                		  else
	     	                			  writer.write("Single Row Column MisMatch"
	     	                					  +" \r\n"
		     	                				  + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode")
		     	                				  +"---"
		     	                				  + tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key")
		     	                				  + " \r\n"
		     	                				  +AttributesOIM[oimAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Decode"))]
				     	                		  +"---"
				     	                		  +AttributesEBS[ebsAttributeMap.get(tcresultSet.getStringValue("Lookup Definition.Lookup Code Information.Code Key"))]
				     	                		  + " \r\n"
				     	                		  +sOIM[0]
				     	                		  + " \r\n"
				     	                		  + sEBS[valuesEBS.size()-1]
		     	                				  +" \r\n\r\n"); 	     	                		  {
		     	                		  writer.flush();
		     	                		  break;
	     	                		  }
	     	                	  }	  
	     	                  }
		     	                

	     	         	}
	                }
	     	        
	     	        if(valuesOIM.size() == 0)
	     	        {
	     	        		  LOGGER.info(classname + methodName+ "No Value in OIM \r\n");
	                		  writer.write("No Value in OIM"+sEBS[0]+" \r\n\r\n"); 
	                		  writer.flush();
	            
	     	        }
	     	        
	     	         //new code end
	     	        /*    
	                if(valuesEBS.size() > 1)
	                {
	                	  AttributesOIM = sOIM[0].split(",");;
	  		        	  AttributesEBS = sEBS[valuesEBS.size()-1].split(",");

	                      
	  		        	  if(!(AttributesOIM[OIM_COLUMN_FIRSTNAME].equals(AttributesEBS[EBS_COLUMN_FIRSTNAME]) 
	                	   && AttributesOIM[OIM_COLUMN_LASTNAME].equals(AttributesEBS[EBS_COLUMN_LASTNAME])		
	                	   ))
	                	{
	  		        		 LOGGER.info(classname + methodName+ "MisMatch\r\n");
	 	 		             writer.write("Duplicate"+sOIM[0]+" \r\n"); 
	   	 		             writer.flush();
	                	}
	  		        	 
	  		        	String userStatus=null;
            			//LOGGER.info(classname + methodName+ "EffEndDate"+AttributesEBS[EBS_COLUMN_EFFENDDATE]);
 		        		String temp = AttributesEBS[EBS_COLUMN_EFFENDDATE].replace("\"", "");
 		        		//LOGGER.info(classname + methodName+ "TrimmedEffEndDate"+temp);
	  		        	Date effEndDate = dateFormatter.parse(temp);
 		        		//LOGGER.info(classname + methodName+ "todayDate"+todayDate+"effEndDate"+effEndDate);

 	  		        	    	if(todayDate.compareTo(effEndDate)>0)
		 	  		        	{
		 	  		        		userStatus= "\"Disabled\"";
		 	  		        	}
		 	  		        	else
		 	  		        	{
		 	  		        		userStatus= "\"Active\"";
		 	  		        	}
 	  		        	if(!AttributesOIM[OIM_COLUMN_STATUS].equals(userStatus))
 	  		        	{
	  	 		        	LOGGER.info(classname + methodName+ "oim status"+AttributesOIM[OIM_COLUMN_STATUS]+"ebs status"+userStatus);
	  	 		        	LOGGER.info(classname + methodName+ "EBS Last Row"+sEBS[valuesEBS.size()-1]+"\r\n");
 	  		        		writer.write("Status MisMatch"+sOIM[0]+" \r\n"); 
 	  		        		writer.flush();
 	  		        	}
	                     //LOGGER.info(classname + methodName+ sOIM[0]+"Vrushank\r\n");
	 		             //writer.write(sOIM[0]+" \r\n"); 
  	 		             //writer.flush();

	                }
                    //LOGGER.info(classname + methodName+ key+"Vrushank\r\n");
                	*/
	             }
	             
	            //Write into a file. 
		        // creates a FileWriter Object
		        
	        LOGGER.info(classname + methodName+ classname + methodName+ "----->>>>        " + "Lookup Value Added");
        	
        }
        catch (Exception e)
        {
                    LOGGER.info(classname + methodName+ " Exception"+ e);
        }
        
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
