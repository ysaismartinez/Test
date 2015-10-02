

package com.aim.eventhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import oracle.iam.conf.api.SystemConfigurationService;
import oracle.iam.conf.vo.SystemProperty;
import oracle.iam.identity.usermgmt.impl.UserMgrUtil;
import oracle.iam.identity.utils.Utils;
import oracle.iam.notification.api.NotificationService;
import oracle.iam.notification.exception.EventException;
import oracle.iam.notification.exception.MultipleTemplateException;
import oracle.iam.notification.exception.NotificationException;
import oracle.iam.notification.exception.NotificationResolverNotFoundException;
import oracle.iam.notification.exception.TemplateNotFoundException;
import oracle.iam.notification.exception.UnresolvedNotificationDataException;
import oracle.iam.notification.exception.UserDetailsNotFoundException;
import oracle.iam.notification.vo.NotificationEvent;
import oracle.iam.passwordmgmt.domain.repository.DBUserRepository;
import oracle.iam.passwordmgmt.domain.repository.UserRepository;
import oracle.iam.passwordmgmt.impl.PasswordMgmtLogger;
import oracle.iam.passwordmgmt.vo.Constants;
import oracle.iam.passwordmgmt.vo.UserInfo;
import oracle.iam.platform.Platform;
import oracle.iam.platform.context.ContextAwareString;
import oracle.iam.platform.kernel.spi.PostProcessHandler;
import oracle.iam.platform.kernel.vo.AbstractGenericOrchestration;
import oracle.iam.platform.kernel.vo.BulkEventResult;
import oracle.iam.platform.kernel.vo.BulkOrchestration;
import oracle.iam.platform.kernel.vo.EventResult;
import oracle.iam.platform.kernel.vo.Orchestration;
import oracle.iam.platform.utils.crypto.CryptoException;
import oracle.iam.platform.utils.crypto.CryptoUtil;

public class PasswordNotification
    implements PostProcessHandler
{

    public PasswordNotification()
    {
    	super();
    }

    public boolean cancel(long processId, long eventId, AbstractGenericOrchestration abstractgenericorchestration)
    {
        return false;
    }

    public void compensate(long l, long l1, AbstractGenericOrchestration abstractgenericorchestration)
    {
    }

    public EventResult execute(long processId, long eventId, Orchestration orchestration)
    {
    	System.out.println("Starting the  PasswordNotification while reset");
        String operation = orchestration.getOperation();
        HashMap parameters = orchestration.getParameters();
        Object uname = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.USER_LOGIN.getId());
        System.out.println("UserName "+uname);
        Object encryptedPasswordObj = parameters.get(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD.getId());
        String encryptedPassword = "";
        if(encryptedPasswordObj instanceof ContextAwareString)
            encryptedPassword = (String)((ContextAwareString)encryptedPasswordObj).getObjectValue();
        if(encryptedPasswordObj instanceof String)
            encryptedPassword = (String)encryptedPasswordObj;
        String userID = null;
        try
        {
            if(uname != null)
            {
                userID = uname.toString();
            } else
            {
                String userKey = orchestration.getTarget().getEntityId();
                userID = UserMgrUtil.getUserLoginFromId(userKey);
            }
        }
        catch(Exception e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Error fetching user details", e);
        }
        UserRepository userRepository = new DBUserRepository();
        UserInfo usrInfo = userRepository.getUserAndManagerInfo(userID);
       /* if(oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.CREATE.toString().equalsIgnoreCase(operation)){
            try
            {
                boolean isPwdAutoGen = false;
                String passwdGen = (String)usrInfo.getAttribute(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeName.PASSWORD_GENERATED.getId());
                if(passwdGen != null && passwdGen.equalsIgnoreCase(oracle.iam.identity.usermgmt.api.UserManagerConstants.AttributeValues.PWD_AUTO_GENERATED_TRUE.getId()))
                    isPwdAutoGen = true;
                if(isPwdAutoGen)
                {
                    char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
                    if(password != null)
                        sendNotificationToUsr(usrInfo, String.valueOf(password), "GeneratedPasswordNotification");
                }
            }
            catch(CryptoException e)
            {
                PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Password decryption failed. ", e);
            }
        }
        else*/
        if(oracle.iam.identity.usermgmt.api.UserManagerConstants.Operations.RESET_PASSWORD.toString().equalsIgnoreCase(operation))
        {
            try
            {
                //String notificationFlag = (String)ContextManager.getValue("SEND_PASSWORD_NOTIFICATION_FLAG");
               // if(Boolean.valueOf(notificationFlag).booleanValue())
                //{
                    char password[] = CryptoUtil.getDecryptedPassword(encryptedPassword, null);
                    System.out.println("Sending Reset Password details\n");
                    if(password != null){
                        sendNotificationToUsrWithoutPwd(usrInfo, "New Hire Account");
                    	sendNotificationToUsr(usrInfo, String.valueOf(password), "New Hire Password");
                    }
                        
                    
                //}
            }
            catch(CryptoException e)
            {
                PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Password decryption failed. ", e);
            }
        }
        EventResult result = new EventResult();
        return result;
    }

    private void sendNotificationToUsr(UserInfo usrInfo, String password, String templateName)
    {
        NotificationService notificationService = (NotificationService)Platform.getService(oracle.iam.notification.api.NotificationService.class);
        NotificationEvent event = new NotificationEvent();
        event.setTemplateName(templateName);
        event.setSender(null);
        String userEmailID = usrInfo.getUserEmailID();
        String userID = usrInfo.getLoginID();
        String managerEmailID = usrInfo.getManagerEmailID();
        String managerID = usrInfo.getManagerLoginID();
        String managerFirstName=null;
        boolean notifyToOther = false;
        try
        {
        	UserRepository userRepository = new DBUserRepository();
            UserInfo usrmanagerInfo = userRepository.getUserAndManagerInfo(managerID);
            managerFirstName= usrmanagerInfo.getFirstName();
            SystemConfigurationService sysConfig = (SystemConfigurationService)Platform.getService(oracle.iam.conf.api.SystemConfigurationService.class);
            SystemProperty property = sysConfig.getSystemProperty("XL.NotifyPasswordGenerationToOther");
            notifyToOther = property.getPtyValue() != null ? property.getPtyValue().equalsIgnoreCase("true") : false;
        }
        catch(Exception ex)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Error fetching system property for notification. ", ex);
        }
        /*if(notifyToOther && managerID != null && !managerID.equals(""))
            event.setUserIds(new String[] {
                userID, managerID
            });
        else
            event.setUserIds(new String[] {
                userID
            });*/
        event.setUserIds(new String[] {
                 managerID
            });
        Date date= new Date();
        HashMap notificationData = new HashMap();
        notificationData.put("firstName", managerFirstName);
        notificationData.put("lastName", usrInfo.getFirstName()+" "+usrInfo.getLastName());
        notificationData.put("userLoginId", usrInfo.getLoginID());
        notificationData.put("user", usrInfo.getUserKey());
        notificationData.put("userManagerLoginId", usrInfo.getManagerLoginID());
        notificationData.put("password", password);
        notificationData.put("userEmail", userEmailID);
        notificationData.put("userManagerEmail", managerEmailID);
        if(Utils.isMTFriendly())
        {
            notificationData.put("Non MT User Login", usrInfo.getNonMTUserLoginID());
            notificationData.put(Constants.TENANT_NAME, usrInfo.getTenantName());
        }
        event.setParams(notificationData);
        try
        {
            notificationService.notify(event);
        }
        catch(EventException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Event Exception occured.", e);
        }
        catch(UnresolvedNotificationDataException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Data not resolved.", e);
        }
        catch(TemplateNotFoundException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Template not found ", e);
        }
        catch(MultipleTemplateException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Multiple template exception occured. ", e);
        }
        catch(NotificationResolverNotFoundException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Resolver not found. ", e);
        }
        catch(UserDetailsNotFoundException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.User Details not found. ", e);
        }
        catch(NotificationException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Exception occured. ", e);
        }
    }

    private void sendNotificationToUsrWithoutPwd(UserInfo usrInfo, String templateName)
    {
        NotificationService notificationService = (NotificationService)Platform.getService(oracle.iam.notification.api.NotificationService.class);
        NotificationEvent event = new NotificationEvent();
        event.setTemplateName(templateName);
        event.setSender(null);
        String userEmailID = usrInfo.getUserEmailID();
        String userID = usrInfo.getLoginID();
        String managerEmailID = usrInfo.getManagerEmailID();
        String managerID = usrInfo.getManagerLoginID();
        Date startDate = (Date) usrInfo.getAttribute("Hire Date");
        System.out.println("startDate "+startDate);
        String strDate=null;
        String managerFirstName=null;
        boolean notifyToOther = false;
        try
        {
        	SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy");
        	strDate =df.format(startDate);
        	UserRepository userRepository = new DBUserRepository();
            UserInfo usrmanagerInfo = userRepository.getUserAndManagerInfo(managerID);
            managerFirstName= usrmanagerInfo.getFirstName();
            SystemConfigurationService sysConfig = (SystemConfigurationService)Platform.getService(oracle.iam.conf.api.SystemConfigurationService.class);
            SystemProperty property = sysConfig.getSystemProperty("XL.NotifyPasswordGenerationToOther");
            notifyToOther = property.getPtyValue() != null ? property.getPtyValue().equalsIgnoreCase("true") : false;
        }
        catch(Exception ex)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Error fetching system property for notification. ", ex);
        }
        /*if(notifyToOther && managerID != null && !managerID.equals(""))
            event.setUserIds(new String[] {
                userID, managerID
            });
        else
            event.setUserIds(new String[] {
                userID
            });*/
        event.setUserIds(new String[] {
                 managerID
            });
        
        HashMap notificationData = new HashMap();
        notificationData.put("firstName", managerFirstName);
        notificationData.put("lastName", strDate);
        notificationData.put("userLoginId", usrInfo.getLoginID());
        notificationData.put("user", usrInfo.getUserKey());
        notificationData.put("userManagerLoginId", usrInfo.getManagerLoginID());
        notificationData.put("password", usrInfo.getFirstName()+" "+usrInfo.getLastName());
        notificationData.put("userEmail", userEmailID);
        notificationData.put("userManagerEmail", managerEmailID);
        if(Utils.isMTFriendly())
        {
            notificationData.put("Non MT User Login", usrInfo.getNonMTUserLoginID());
            notificationData.put(Constants.TENANT_NAME, usrInfo.getTenantName());
        }
        event.setParams(notificationData);
        try
        {
            notificationService.notify(event);
        }
        catch(EventException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Event Exception occured.", e);
        }
        catch(UnresolvedNotificationDataException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Data not resolved.", e);
        }
        catch(TemplateNotFoundException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Template not found ", e);
        }
        catch(MultipleTemplateException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Multiple template exception occured. ", e);
        }
        catch(NotificationResolverNotFoundException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Resolver not found. ", e);
        }
        catch(UserDetailsNotFoundException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.User Details not found. ", e);
        }
        catch(NotificationException e)
        {
            PasswordMgmtLogger.LOGGER.log(Level.WARNING, "Sending Password Notification is not successful.Notification Exception occured. ", e);
        }
    }

    
    public BulkEventResult execute(long processId, long eventId, BulkOrchestration orchestration)
    {
        BulkEventResult result = new BulkEventResult();
        return result;
    }

    public void initialize(HashMap hashmap)
    {
    }

    private static final String GENERATEDPASSWORD_NOTIFICATION_TEMPLATE = "GeneratedPasswordNotification";
    private static final String RESETPASSWORD_NOTIFICATION_TEMPLATE = "ResetPasswordNotification";
}
