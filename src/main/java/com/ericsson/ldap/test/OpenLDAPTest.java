package com.ericsson.ldap.test;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.*;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Vector;

public class OpenLDAPTest extends Thread {
    DirContext dc = null;
    String account = "cn=Directory Manager";//操作LDAP的帐户。默认就是Manager。
    String password = "123456";//帐户Manager的密码。
    String root = "o=example,c=com"; //LDAP的根节点的DC

    public OpenLDAPTest() {
        init();
        add();//添加节点

        //delete("ou=hi,dc=example,dc=com");//删除"ou=hi,dc=example,dc=com"节点

        //modifyInformation("ou=hi,dc=example,dc=com");//修改"ou=hi,dc=example,dc=com"属性

        //重命名节点"ou=new,o=neworganization,dc=example,dc=com"
        //renameEntry("ou=new,o=neworganization,dc=example,dc=com",
        //"ou=neworganizationalUnit,o=neworganization,dc=example,dc=com");

        //searchInformation("o=tcl,c=cn", "", "(objectclass=*)");//遍历所有根节点

        //遍历指定节点的分节点
        //searchInformation("o=neworganization,dc=example,dc=com","","(objectclass=*)");
        //close();
    }

    public void init() {
        if (dc != null) {
            try {
                dc.close();
            } catch (NamingException e) {
                System.out.println("NamingException in close():" + e);
            }
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://127.0.0.1:389/");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=" + account + "," + root);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {

            dc = new InitialDirContext(env);//初始化上下文
            System.out.println("认证成功");//这里可以改成异常抛出。
        } catch (javax.naming.AuthenticationException e) {
            System.out.println("认证失败");
        } catch (Exception e) {
            System.out.println("认证出错：" + e);
        }
    }

    public void close() {
        if (dc != null) {
            try {
                dc.close();
            } catch (NamingException e) {
                System.out.println("NamingException in close():" + e);
            }
        }
    }

    public void add() {
        try {
            String newUserName = "hi";
            BasicAttributes attrs = new BasicAttributes();
            BasicAttribute objclassSet = new BasicAttribute("objectClass");
            objclassSet.add("top");
            objclassSet.add("organizationalUnit");
            attrs.put(objclassSet);
            attrs.put("ou", newUserName);
            dc.createSubcontext("ou=" + newUserName + "," + root, attrs);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in add():" + e);
        }
    }

    public void delete(String dn) {
        try {
            dc.destroySubcontext(dn);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in delete():" + e);
        }
    }

    public boolean modifyInformation(String dn) {
        try {
            ModificationItem[] mods = new ModificationItem[1];

            /*添加属性*/
//           Attribute attr0 = new BasicAttribute("description",
//                   "测试");
//           mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,attr0);

            /*修改属性*/
//           Attribute attr0 = new BasicAttribute("description", "尚");
//           mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
//                   attr0);

            /*删除属性*/
            Attribute attr0 = new BasicAttribute("description", "尚");
            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                    attr0);
            dc.modifyAttributes(dn, mods);
            return true;
        } catch (NamingException ne) {
            ne.printStackTrace();
            System.err.println("Error: " + ne.getMessage());
            return false;
        }

    }

    /**
     * @param base ：根节点(在这里是"o=tcl,c=cn")
     * @param scope ：搜索范围,分为"base"(本节点),"one"(单层),""(遍历)
     * @param filter ：指定子节点(格式为"(objectclass=*)",*是指全部，你也可以指定某一特定类型的树节点)
     */
    public void searchInformation(String base, String scope, String filter) {
        SearchControls sc = new SearchControls();
        if (scope.equals("base")) {
            sc.setSearchScope(SearchControls.OBJECT_SCOPE);
        } else if (scope.equals("one")) {
            sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        } else {
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }

        NamingEnumeration ne = null;
        try {
            ne = dc.search(base, filter, sc);
            // Use the NamingEnumeration object to cycle through
            // the result set.
            while (ne.hasMore()) {
                System.out.println();
                SearchResult sr = (SearchResult) ne.next();
                String name = sr.getName();

                if (base != null && !base.equals("")) {
                    System.out.println("entry: " + name + "," + base);
                } else {
                    System.out.println("entry: " + name);
                }

                Attributes at = sr.getAttributes();
                NamingEnumeration ane = at.getAll();

                while (ane.hasMore()) {
                    Attribute attr = (Attribute) ane.next();
                    String attrType = attr.getID();
                    System.out.println("********attrType = attr.getID():  " + attrType);
                    NamingEnumeration values = attr.getAll();
                    Vector vals = new Vector();
                    // Another NamingEnumeration object, this time
                    // to iterate through attribute values.
                    while (values.hasMore()) {
                        Object oneVal = values.nextElement();
                        if (oneVal instanceof String) {
                            System.out.println(attrType + ": " + (String) oneVal);
                        } else {
                            System.out.println(attrType + ": " + new String((byte[]) oneVal));
                        }
                    }
                    if(values != null){
                        values.close();
                    }
                }
                if(ane != null){
                    ane.close();
                }
            }
            if(ne != null){
                ne.close();
            }
        } catch (Exception nex) {
            System.err.println("Error: " + nex.getMessage());
            nex.printStackTrace();
        }
    }

    public void searchOneNode(String base){
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.OBJECT_SCOPE);
        String filter = "(objectclass=*)";

        NamingEnumeration ne = null;
        try {
            ne = dc.search(base, filter, sc);
            while(ne.hasMore()){
                System.out.println();
                SearchResult sr = (SearchResult) ne.next();
                String name = sr.getName();
                if(base != null && !base.equals("")){
                    System.out.println("entry: " + name + "," + base);
                } else {
                    System.out.println("entry: " + name);
                }

                Attributes at = sr.getAttributes();
                NamingEnumeration ane = at.getAll();

                while(ane.hasMore()){
                    Attribute attr = (Attribute) ane.next();
                    String attrType = attr.getID();
                    NamingEnumeration values = attr.getAll();
                    Vector vals = new Vector();
                    while(values.hasMore()){
                        Object oneVal = values.next();
                        if(oneVal instanceof String){
                            System.out.println(attrType + ":" + oneVal);
                        }else{
                            System.out.println(attrType + ":" + new String((byte[]) oneVal));
                        }
                    }
                    if(values != null){
                        values.close();
                    }
                }
                if(ane != null){
                    ane.close();
                }
            }
            if(ne != null){
                ne.close();
            }
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void searchAttrOfValue(String base, String attribute){
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.OBJECT_SCOPE);
        String filter = "(objectclass=*)";
        String outValue = null;
        NamingEnumeration ne = null;
        try {
            ne = dc.search(base, filter, sc);
            while(ne.hasMore()){
                System.out.println();
                SearchResult sr = (SearchResult) ne.next();
                String name = sr.getName();
                if(base != null && !base.equals("")){
                    System.out.println("entry: " + name + "," + base);
                } else {
                    System.out.println("entry: " + name);
                }

                Attributes at = sr.getAttributes();
                NamingEnumeration ane = at.getAll();

                while(ane.hasMore()){
                    Attribute attr = (Attribute) ane.next();
                    String attrType = attr.getID();

                    if(attrType.equals(attribute)){
                        NamingEnumeration values = attr.getAll();
                        Vector vals = new Vector();
                        int num = 1;
                        while(values.hasMore()){
                            Object oneVal = values.next();
                            if(oneVal instanceof String){
                                System.out.println(attrType + "-------:" + oneVal);
                                outValue = attrType + ": " + oneVal;
                            }else{
                                System.out.println(attrType + "+++++++:" + new String(Base64.getEncoder().encode((byte[]) oneVal)));
//                               Base64.encode((byte[]) oneVal);
//                               outValue = attrType + ": " + new String((byte[]) oneVal);
                                outValue = new String(Base64.getEncoder().encode((byte[]) oneVal));
                                fileStore("outFile" + num , outValue);
                                num ++;
                            }
                        }
                        if(values != null){
                            values.close();
                        }
                    }
                }
                if(ane != null){
                    ane.close();
                }
            }
            if(ne != null){
                ne.close();
            }
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //********************
//    fileStore("outFile", outValue);
    }


    public boolean renameEntry(String oldDN, String newDN) {
        try {
            dc.rename(oldDN, newDN);
            return true;
        } catch (NamingException ne) {
            System.err.println("Error: " + ne.getMessage());
            return false;
        }
    }

    public void fileStore(String outFile, String attrValue){
        String str = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new StringReader(attrValue));
            out = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(outFile + ".crl", false)));
            while((str=in.readLine()) != null){
//                out.println(new Date());
                out.println(str);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if(in != null){
                in.close();
                in = null;
            }
            if(out != null){
                out.close();
                out = null;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

   /*
   private class LdapThread extends Thread{
       public void run(){
               try {
                   while (true) {
                    searchAttrOfValue("uid=Unmi,o=tcl,c=cn", "mail");
                    this.sleep(60000);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
       }
   }*/

    public void run(){
        try {
            while (true) {
                searchAttrOfValue("uid=Unmi,o=tcl,c=cn", "mail");
                this.sleep(60000);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
  /*
   public static void main(String[] args) {
       OpenLDAPTest ldap = new OpenLDAPTest();
//       ldap.searchInformation("o=tcl,c=cn", "", "(objectclass=*)");
//       ldap.searchOneNode("uid=aaa,o=tcl,c=cn");
//       ldap.searchAttrOfValue("uid=Unmi,o=tcl,c=cn", "mail");
//       new LdapThread().start();
       ldap.start();
       ldap.close();
   }*/

}
