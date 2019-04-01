package com.ericsson.ldap.dao;

import com.ericsson.ldap.entity.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganizationRepo {
    @Autowired
//    @Qualifier("adLdapTemplate")
    private LdapTemplate ldapTemplate;

    /**
     * 添加 一条记录
     * @param org
     */
    public void create(Organization org) {
        BasicAttribute ba = new BasicAttribute("objectclass");
        ba.add("organizationalUnit");
        Attributes attr = new BasicAttributes();
        attr.put(ba);
        //必填属性，不能为null也不能为空字符串
        attr.put("ou", org.getOu());

        //可选字段需要判断是否为空，如果为空则不能添加
        if (org.getDescription() != null
                && org.getDescription().length() > 0) {
            attr.put("description", org.getDescription());
        }

        if (org.getPostalAddress() != null
                && org.getPostalAddress().length() > 0) {
            attr.put("postalAddress", org.getPostalAddress());
        }
        if (org.getSeeAlso() != null
                && org.getSeeAlso().length() > 0) {
            attr.put("seeAlso", org.getSeeAlso());
        }
        if (org.getTelephoneNumber() != null
                && org.getTelephoneNumber().length() > 0) {
            attr.put("telephoneNumber", org.getTelephoneNumber());
        }
        if (org.getPostalCode() != null
                && org.getPostalCode().length() > 0) {
            attr.put("postalCode", org.getPostalCode());
        }

        //bind方法即是添加一条记录。
        try {
            ldapTemplate.bind(buildDN(org), null, attr);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

     /**
     * 根据dn查询详细信息
     * @param cn
     * @return
     */
    public Organization getOrganizationDetail(String cn) {
        //ldapTeplate的lookup方法是根据dn进行查询，此查询的效率较高
        Organization org = null;
        try {
            org = (Organization)ldapTemplate.lookup(buildDN(cn),new OrganizationAttributesMapper());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return org;
    }

    /**
     * 根据自定义的属性值查询person列表
     * @param org
     * @return
     */
    public List<Organization> list(Organization org) {
        List<Organization> list = new ArrayList<Organization>();
        //查询过滤条件
        AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "organizationalUnit"));


        if (org.getOu() != null
                && org.getOu().length() > 0) {
            andFilter.and(new EqualsFilter("ou", org.getOu()));
        }
        if (org.getDescription() != null
                && org.getDescription().length() > 0) {
            andFilter.and(new EqualsFilter("description", org.getDescription()));
        }

        if (org.getPostalAddress() != null
                && org.getPostalAddress().length() > 0) {
            andFilter.and(new EqualsFilter("postalAddress", org.getPostalAddress()));
        }
        if (org.getSeeAlso() != null
                && org.getSeeAlso().length() > 0) {
            andFilter.and(new EqualsFilter("seeAlso", org.getSeeAlso()));
        }
        if (org.getTelephoneNumber() != null
                && org.getTelephoneNumber().length() > 0) {
            andFilter.and(new EqualsFilter("telephoneNumber", org.getTelephoneNumber()));
        }
        if (org.getPostalCode() != null
                && org.getPostalCode().length() > 0) {
            andFilter.and(new EqualsFilter("postalCode", org.getPostalCode()));
        }
        LdapNameBuilder builder = LdapNameBuilder.newInstance("ou=Departments");
        // ou=Development,ou=IT,ou=Departments,dc=example,dc=com
        if(!StringUtils.isEmpty(org.getParentUnit())){
            builder.add("ou",org.getParentUnit());
        }
        //search是根据过滤条件进行查询，第一个参数是父节点的dn，可以为空，不为空时查询效率更高
        list = ldapTemplate.search(builder.build(),
                andFilter.encode(),
                new OrganizationAttributesMapper());
        return list;
    }

    /**
     * 根据dn删除一条记录
     * @param cn
     */
    public void remove(String cn) {
        try {
            ldapTemplate.unbind(buildDN(cn));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改操作
     * @param org
     */
    public void update(Organization org) {
        if (org == null || org.getOu() == null || org.getOu().length() <= 0) {
            return;
        }
        List<ModificationItem> mList = new ArrayList<ModificationItem>();

        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("description",org.getDescription())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("seeAlso",org.getSeeAlso())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("telephoneNumber",org.getTelephoneNumber())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("postalCode",org.getPostalCode())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("postalAddress",org.getPostalAddress())));

        if (mList.size() > 0) {
            ModificationItem[] mArray = new ModificationItem[mList.size()];
            for (int i = 0; i < mList.size(); i++) {
                mArray[i] = mList.get(i);
            }
            //modifyAttributes 方法是修改对象的操作，与rebind（）方法需要区别开
            try {
                ldapTemplate.modifyAttributes(this.buildDN(org.getOu()), mArray);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取第一层级的组织机构
     * @return
     */
    public List<Organization> getAllDepartments() {
        return ldapTemplate.search(
                LdapUtils.newLdapName("ou=Departments"),
                null,
                new OrganizationAttributesMapper());
    }

    /**
     * 获取第二层级的部门
     * @param department
     * @return
     */
    public List<Organization> getAllUnitsForDepartment(String department) {
        return ldapTemplate.search(
                LdapNameBuilder.newInstance("ou=Departments").add("ou", department).build(),
                null,
                new OrganizationAttributesMapper());
    }

    protected Name buildDN(Organization org) throws NameNotFoundException {
        if(org == null){
            throw new NameNotFoundException();
        }
        // ou=IT,ou=Departments,dc=example,dc=com
        // ou=Development,ou=IT,ou=Departments,dc=example,dc=com
        LdapNameBuilder builder = LdapNameBuilder.newInstance("ou=Departments");

        if(!StringUtils.isEmpty(org.getParentUnit())){
            builder.add("ou",org.getParentUnit());
        }
        return builder.build();
    }

    protected Name buildDN(String dn) throws NameNotFoundException {
        if(StringUtils.isEmpty(dn)){
            throw new NameNotFoundException();
        }
        //顶层DN:"dc=example,dc=com"
        String dnStr = Arrays.stream(dn.split(","))
                .filter(e->e != null && !"dc=example".equals(e.toLowerCase()) && !"dc=com".equals(e.toLowerCase()))
                .collect(Collectors.joining(","));

        return LdapNameBuilder.newInstance(dnStr).build();
    }

    /**
     * 这个类的作用是将ldap中的属性转化为实体类的属性值
     */
    private static class OrganizationAttributesMapper implements AttributesMapper<Organization> {

        @Override
        public Organization mapFromAttributes(Attributes attr) throws NamingException {
            Organization org = new Organization();
            org.setOu((String)attr.get("ou").get());

            if (attr.get("postalAddress") != null) {
                org.setPostalAddress((String)attr.get("postalAddress").get());
            }
            if (attr.get("telephoneNumber") != null) {
                org.setTelephoneNumber((String)attr.get("telephoneNumber").get());
            }
            if (attr.get("seeAlso") != null) {
                org.setSeeAlso((String)attr.get("seeAlso").get());
            }
            if (attr.get("description") != null) {
                org.setDescription((String)attr.get("description").get());
            }
            if (attr.get("postalCode") != null) {
                org.setPostalCode((String)attr.get("postalCode").get());
            }
            return org;
        }
    }
}
