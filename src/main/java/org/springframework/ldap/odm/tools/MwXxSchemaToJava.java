package org.springframework.ldap.odm.tools;

import java.util.ArrayList;
import java.util.List;

public class MwXxSchemaToJava {
    /** LDAP连接地址URL */
    private static final String url = "ldap://127.0.0.1:389";
    /** LDAP管理用户 */
    private static final String username = "cn=Directory Manager";
    /** LDAP管理用户的密码 */
    private static final String password = "123456";
    /** 生成类的基础包路径 */
    private static final String basePackage = "com.ericsson.sample.";

    public static void main(String[] args) {
        List<String[]> list = new ArrayList<String[]>();
        // 主帐号
        list.add(new String[] { basePackage , "User", "person,organizationalPerson,inetOrgPerson,top" });
        // 组织机构
        list.add(new String[] { basePackage , "Organization", "organizationalUnit,top" });
        // 用户组
        list.add(new String[] { basePackage , "Group", "groupOfNames,top" });

        String tempDir = "C:\\Users\\elnghxn\\Documents\\work\\IAM\\ldap";

        for (String[] item : list) {
            String[] flags = new String[] {
                    // 连接LDAP的URL
                    "--url", url,
                    // LDAP管理用户
                    "--username", username,
                    // LDAP管理用户的密码
                    "--password", password,
                    // 需要反向生成实体类的ObjectClass对象类型，多个可以用英文 ","号分割
                    "--objectclasses", item[2],
                    // 生成的实体类名
                    "--class", item[1],
                    // 生成的实体类包路径
                    "--package", item[0],
                    // 反向生成的输出目录
                    "--outputdir", tempDir };
            SchemaToJava.main(flags);
        }
    }
}
