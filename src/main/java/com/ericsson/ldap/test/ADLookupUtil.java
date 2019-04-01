package com.ericsson.ldap.test;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

public class ADLookupUtil {

    private final int LDAP_AUTH_SUCCESS = 0;
    private final int LDAP_AUTH_FAIL = -1;
    private final int INVALID_CREDENTIAL = 49;
    private LDAPConnection connection = null;

    private final int UF_ACCOUNTDISABLE = 2;//0x0002
    private final int UF_LOCKOUT = 16;//0x0010
    private final int UF_PASSWORD_EXPIRED = 8388608;//0x800000
    private final int UF_DONT_EXPIRE_PASSWD = 65536;//0x00010000
    private final int UF_NORMAL_ACCOUNT = 512;//0x0200


    String searchAttribs[] = {"userAccountControl", "lockoutTime", "pwdLastSet", "logonCount"};

    /**
     * Constructor
     * Making a LDAP Connection
     */
    public ADLookupUtil() {
        connection = new LDAPConnection();
    }

    /**
     * Function tries to connect to Active Directory
     *
     * @param host Active Directory server name
     * @param port the port
     */
    private boolean connect(String host, String port) {
        try {
            connection.connect(host, new Integer(port).intValue());
            return true;
        } catch (LDAPException ldapex) {

        }

        return false;
    }

    /**
     * Function to disconnect from Active Directory
     */
    private void disconnect() {
        try {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        } catch (LDAPException ldapex) {
        }
    }

    /**
     * Function to authenticate the user to Active directory with user credentials
     *
     * @param baseDN the baseDN
     * @param passwd the password
     * @return
     */
    private int authenticate(String baseDN, String passwd) {
        try {
            try {
                connection.bind(LDAPConnection.LDAP_V3, baseDN.toString(), passwd.getBytes("UTF8"));
            } catch (UnsupportedEncodingException e) {
                disconnect();
                return LDAP_AUTH_FAIL;
            }
            return LDAP_AUTH_SUCCESS;
        } catch (LDAPException ldapex) {
            disconnect();
        }
        return LDAP_AUTH_FAIL;
    }

    /**
     * Function to look up the active directory when corpId is supplied
     *
     * @param corpId the unique corpId of the person calling
     * @return the account status object
     */
//    public ActiveDirectoryResponse lookupEmployee(String corpId, Employee employee) {
//        ActiveDirectoryResponse adr = new ActiveDirectoryResponse();
//        ApplicationContext context = ApplicationContext.getInstance();
//        HashMap searchResult = new HashMap();
//        int adReturnValue = -1;
//        String searchFilter = "sAMAccountName=".concat(corpId);
//        try {
//
//            adr = new ActiveDirectoryResponse();
//            searchResult = lookupAD(context.getProperty(HOST),
//                    context.getProperty(PORT),
//                    context.getProperty(BASE_DN),
//                    context.getProperty(PASSWORD),
//                    context.getProperty(SEARCH_BASE),
//                    searchFilter, LDAPConnection.SCOPE_SUB);
//            try {
//                if (searchResult.size() == 0) {
//                    searchResult = lookupAD(context.getProperty(ANOTHER_HOST),
//                            context.getProperty(ANOTHER_PORT),
//                            context.getProperty(ANOTHER_BASE_DN),
//                            context.getProperty(ANOTHER_PASSWORD),
//                            context.getProperty(ANOTHER_SEARCH_BASE),
//                            searchFilter, LDAPConnection.SCOPE_SUB);
//                }
//            } catch (LDAPException lde) {
//            }
//        } catch (LDAPException lde) {
//        }
//
//        if (searchResult.size() > 0) {
//            try {
//                //Checks whether user account is disabled
//                long userAccountControl = new Long(searchResult.get(searchAttribs[0]).toString()).longValue();
//                if ((userAccountControl & UF_ACCOUNTDISABLE) == UF_ACCOUNTDISABLE) {
//                    adr.setAcDisabled(true);
//                    try {
//                        searchResult = lookupAD(context.getProperty(ANOTHER__HOST),
//                                context.getProperty(ANOTHER_PORT),
//                                context.getProperty(ANOTHER_BASE_DN),
//                                context.getProperty(ANOTHER_PASSWORD),
//                                context.getProperty(ANOTHER_SEARCH_BASE),
//                                searchFilter, LDAPConnection.SCOPE_SUB);
//                    } catch (LDAPSearchException lde) {
//                        (searchResult.size() > 0) {
//                            userAccountControl = new Long(searchResult.get(searchAttribs[0]).toString()).longValue();
//                            if ((userAccountControl & UF_ACCOUNTDISABLE) == UF_ACCOUNTDISABLE) {
//                                adr.setAcDisabled(true);
//                                return adr;
//                            } else {
//                                adr.setAcDisabled(false);
//                            }
//                        }else{
//                            return adr;
//                        }
//                    }
//
//                    //Checks whether the user's accout is locked out
//                    if (searchResult.containsKey(searchAttribs[1].toString())) {
//                        long lockoutTime = new Long(searchResult.get(searchAttribs[1]).toString()).longValue();
//                        if (lockoutTime > 0) {
//                            adr.setAcLocked(true);
//                        }
//                    }
//
//                    //Checks whether the user is a new hire
//                    if (searchResult.containsKey(searchAttribs[2].toString())) {
//                        long logonCount = -1;
//                        if (searchResult.containsKey(searchAttribs[3].toString()))
//                            logonCount = new Long(searchResult.get(searchAttribs[3]).toString()).longValue();
//                        else
//                            logonCount = 0;
//                        //long pwdLastSet = new Long(searchResult.get(searchAttribs[2]).toString()).longValue();
//                        long pwdLastSet = Long.parseLong(searchResult.get(searchAttribs[2]).toString());
//                        if (pwdLastSet != 0) {
//                            pwdLastSet -= 0x19db1ded53e8000L;//the difference Win32 date(1/1/1601) and java date(1/1/1970)
//                            pwdLastSet /= 10000;
//                        }
//                        pwdLastSet = new Date(pwdLastSet).getTime();
//                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new DateFormatSymbols(Locale.US));
//                        Date hireDate = (Date) formatter.parse(employee.getHireDate());
//
//                        System.out.println(" Password Last set on :" + formatter.format(new Date(pwdLastSet)));
//
//                        userAccountControl = new Long(searchResult.get(searchAttribs[0]).toString()).longValue();
//                        if (((pwdLastSet == 0)
//                                || (pwdLastSet < hireDate.getTime()))
//                                && (logonCount == 0)
//                                && (userAccountControl & UF_DONT_EXPIRE_PASSWD) != UF_DONT_EXPIRE_PASSWD) {
//                            adr.setIsNewHire(true);
//                        }
//                    }
//                    // Checks whether the user's password is expired
//                    if ((userAccountControl & UF_PASSWORD_EXPIRED) == UF_PASSWORD_EXPIRED) {
//                        adr.setAcExpired(true);
//                    }
//
//                }catch(Exception ee){
//                }
//            }
//            return adr;
//        }
//
//
//        private HashMap lookupAD (String adHost, String adPort, String adBaseDN,
//                String adPassword, String searchBase, String searchFilter,
//        int searchScope) throws LDAPException {
//            CSCWebAppTrace.entering(this, "lookupAD");
//            Long attributeValue = null;
//            Iterator allAttributes = null;
//            Enumeration allValues = null;
//            LDAPAttribute attribute = null;
//            LDAPAttributeSet attributeSet = null;
//            String sAttrName, sAttrValue = null;
//            LDAPSearchResults searchResults = null;
//            HashMap searchResult = new HashMap();
//
//            try {
//                connect(adHost, adPort);
//            } catch (ADConnectionException eadc) {
//                CSCWebAppTrace.debug(this, eadc.getMessage());
//                return searchResult;
//            }
//            try {
//                authenticate(adBaseDN, adPassword);
//            } catch (ADBindException eadb) {
//                return searchResult;
//            }
//            //Search the directory
//            try {
//                LDAPSearchConstraints srchConst = connection.getSearchConstraints();
//                connection.setConstraints(srchConst);
//
//                try {
//
//                    searchResults = connection.search(searchBase, //search base
//                            searchScope,//search scope
//                            searchFilter,//"cn=",//search filter
//                            searchAttribs,// return all attributes
//                            false,
//                            srchConst); //search constraints
//                } catch (LDAPException lde) {
//                    disconnect();
//                    return searchResult;
//                }
//
//
//                LDAPControl[] controls = connection.getResponseControls();
//                while (searchResults.hasMore()) {
//                    //Print out all the attributes for each entry
//                    attributeSet = searchResults.next().getAttributeSet();
//                    allAttributes = attributeSet.iterator();
//                    while (allAttributes.hasNext()) {
//                        attribute = (LDAPAttribute) allAttributes.next();
//                        sAttrName = attribute.getName();
//
//                        allValues = attribute.getStringValues();
//                        attributeValue = null;
//                        while (allValues.hasMoreElements()) {
//                            Object obj = allValues.nextElement();
//                            if (obj != null) {
//                                attributeValue = new Long((String) obj);
//                                searchResult.put(sAttrName, attributeValue);
//                                System.out.println("sAttrName :" + sAttrName + " value :" + attributeValue);
//                            }
//                        }
//                    }
//                } // end while
//            } // end try
//
//
//            catch (Exception e) {
//
//            }
//            disconnect();
//            return searchResult;
//        }

//    }
}
