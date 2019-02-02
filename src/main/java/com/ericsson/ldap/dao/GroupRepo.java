package com.ericsson.ldap.dao;

import com.ericsson.ldap.entity.Group;
import com.ericsson.ldap.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;
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
public class GroupRepo {
    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * 添加 一条记录
     * @param group
     */
    public void create(Group group) {
        BasicAttribute ba = new BasicAttribute("objectclass");
        ba.add("groupOfNames");
        Attributes attr = new BasicAttributes();
        attr.put(ba);
        //必填属性，不能为null也不能为空字符串
        attr.put("cn", group.getCn());

        //可选字段需要判断是否为空，如果为空则不能添加
        if (group.getDescription() != null
                && group.getDescription().length() > 0) {
            attr.put("description", group.getDescription());
        }

        if (group.getSeeAlso() != null
                && group.getSeeAlso().length() > 0) {
            attr.put("seeAlso", group.getSeeAlso());
        }

        //bind方法即是添加一条记录。
        try {
            ldapTemplate.bind(buildDN(group), null, attr);
            if(group.getMembers() != null && group.getMembers().size()>0){
                group.getMembers().stream().forEach(
                        e -> {
                            String groupDn = "CN=" + group.getCn() + ",ou=Groups";
                            DirContextOperations ctxGroup = ldapTemplate.lookupContext(groupDn);
                            DirContextOperations ctxUser = ldapTemplate.lookupContext(e);
                            ctxGroup.addAttributeValue("member", ctxUser.getStringAttribute("distinguishedname"));
                            ldapTemplate.modifyAttributes(ctxGroup);
                        }
                );
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean addMemberToGroup(String groupName, User user) {

        String groupDn = "CN=" + groupName + ",ou=Groups";

        DirContextOperations ctxGroup = ldapTemplate.lookupContext(groupDn);
        DirContextOperations ctxUser = ldapTemplate.lookupContext(LdapNameBuilder.newInstance("ou=Departments")
                .add("ou", user.getDepartment())
                .add("ou", user.getUnit())
                .add("cn", user.getCn())
                .build());
        try {
            ctxGroup.addAttributeValue("member", ctxUser.getStringAttribute("distinguishedname"));
            ldapTemplate.modifyAttributes(ctxGroup);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeMemberToGroup(String groupName, User user) {
        String groupDn = "CN=" + groupName + ",ou=Groups";
        DirContextOperations ctxGroup = ldapTemplate.lookupContext(groupDn);
        DirContextOperations ctxUser = ldapTemplate.lookupContext(LdapNameBuilder.newInstance("ou=Departments")
                .add("ou", user.getDepartment())
                .add("ou", user.getUnit())
                .add("cn", user.getCn())
                .build());

        try {
            ctxGroup.removeAttributeValue("member", ctxUser.getStringAttribute("distinguishedname"));
            ldapTemplate.modifyAttributes(ctxGroup);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**

     /**
     * 根据dn查询详细信息
     * @param cn
     * @return
     */
    public Group getGroupDetail(String cn) {
        //ldapTeplate的lookup方法是根据dn进行查询，此查询的效率较高
        Group group = null;
        try {
            group = (Group)ldapTemplate.lookup(buildDN(cn),new UserAttributesMapper());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return group;
    }

    /**
     * 根据自定义的属性值查询person列表
     * @param group
     * @return
     */
    public List<Group> list(Group group) {
        List<Group> list = new ArrayList<Group>();
        //查询过滤条件
        AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "groupOfNames"));


        if (group.getCn() != null
                && group.getCn().length() > 0) {
            andFilter.and(new EqualsFilter("cn", group.getCn()));
        }
        if (group.getDescription() != null
                && group.getDescription().length() > 0) {
            andFilter.and(new EqualsFilter("description", group.getDescription()));
        }
        if (group.getSeeAlso() != null
                && group.getSeeAlso().length() > 0) {
            andFilter.and(new EqualsFilter("seeAlso", group.getSeeAlso()));
        }

        //search是根据过滤条件进行查询，第一个参数是父节点的dn，可以为空，不为空时查询效率更高
        list = ldapTemplate.search("ou=Groups",
                andFilter.encode(),
                new UserAttributesMapper());
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
     * @param group
     */
    public void update(Group group) {
        if (group == null || group.getCn() == null || group.getCn().length() <= 0) {
            return;
        }
        List<ModificationItem> mList = new ArrayList<ModificationItem>();

        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("description",group.getDescription())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("seeAlso",group.getSeeAlso())));

        if (mList.size() > 0) {
            ModificationItem[] mArray = new ModificationItem[mList.size()];
            for (int i = 0; i < mList.size(); i++) {
                mArray[i] = mList.get(i);
            }
            try {
                ldapTemplate.modifyAttributes(this.buildDN(group.getCn()), mArray);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected Name buildDN(Group group) throws NameNotFoundException {
        if(group == null){
            throw new NameNotFoundException();
        }
        //顶层DN:"ou=Groups,dc=example,dc=com"
        return LdapNameBuilder.newInstance("ou=Groups")
                .add("cn", group.getCn())
                .build();
    }

    protected Name buildDN(String dn) throws NameNotFoundException {
        if(StringUtils.isEmpty(dn)){
            throw new NameNotFoundException();
        }
        String dnStr = Arrays.stream(dn.split(","))
                .filter(e->e != null && !"dc=example".equals(e.toLowerCase()) && !"dc=com".equals(e.toLowerCase()))
                .collect(Collectors.joining(","));

        return LdapNameBuilder.newInstance(dnStr).build();
    }

    /**
     * 这个类的作用是将ldap中的属性转化为实体类的属性值
     */
    private class UserAttributesMapper implements AttributesMapper<Group> {

        @Override
        public Group mapFromAttributes(Attributes attr) throws NamingException {
            Group group = new Group();
            group.setCn((String)attr.get("cn").get());

            if (attr.get("seeAlso") != null) {
                group.setSeeAlso((String)attr.get("seeAlso").get());
            }
            if (attr.get("description") != null) {
                group.setDescription((String)attr.get("description").get());
            }
            return group;
        }
    }
}
