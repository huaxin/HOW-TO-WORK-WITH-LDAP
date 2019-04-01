package com.ericsson.ldap.dao;

import com.ericsson.ldap.entity.User;
import com.sun.jndi.ldap.ctl.PasswordExpiredResponseControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserRepo {
    @Autowired
    @Qualifier("adLdapTemplate")
    private LdapTemplate ldapTemplate;

    /**
     * 添加 一条记录
     * @param user
     */
    public void create(User user) {
        BasicAttribute ba = new BasicAttribute("objectclass");
        ba.add("daUser");
        Attributes attr = new BasicAttributes();
        attr.put(ba);
        //必填属性，不能为null也不能为空字符串
        attr.put("cn", user.getCn());
        attr.put("sn", user.getSn());

        //可选字段需要判断是否为空，如果为空则不能添加
        if (user.getDescription() != null
                && user.getDescription().length() > 0) {
            attr.put("description", user.getDescription());
        }

        if (user.getUserPassword() != null
                && user.getUserPassword().length() > 0) {
            attr.put("userPassword", user.getUserPassword());
        }
        if (user.getSeeAlso() != null
                && user.getSeeAlso().length() > 0) {
            attr.put("seeAlso", user.getSeeAlso());
        }
        if (user.getTelephoneNumber() != null
                && user.getTelephoneNumber().length() > 0) {
            attr.put("telephoneNumber", user.getTelephoneNumber());
        }
        if (user.getMail() != null
                && user.getMail().length() > 0) {
            attr.put("mail", user.getMail());
        }
        if (user.getExtension() != null
                && user.getExtension().length() > 0) {
            attr.put("extension", user.getExtension());
        }
        if (user.getMission() != null
                && user.getMission().length() > 0) {
            attr.put("mission", user.getMission());
        }

        //bind方法即是添加一条记录。
        try {
            ldapTemplate.bind(buildDN(user), null, attr);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 换部门
     * @param oldDN
     * @param newDN
     * @return
     */
    public boolean changeOU(String oldDN, String newDN) {
        String old = Arrays.stream(oldDN.split(","))
                .filter(e->e != null && !"dc=example".equals(e.toLowerCase()) && !"dc=com".equals(e.toLowerCase()))
                .collect(Collectors.joining(","));

        String target = Arrays.stream(newDN.split(","))
                .filter(e->e != null && !"dc=example".equals(e.toLowerCase()) && !"dc=com".equals(e.toLowerCase()))
                .collect(Collectors.joining(","));

        try {
            ldapTemplate.rename(old, newDN);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**

     /**
     * 根据dn查询详细信息
     * @param dn
     * @return
     */
    public User getUserDetail(String dn) {
        //ldapTeplate的lookup方法是根据dn进行查询，此查询的效率较高
        User user = null;
        try {
            //user = (User)ldapTemplate.lookup(buildDN(dn),new UserAttributesMapper());
            //dn = "cn=Mietch Underwood,ou=Development,ou=IT,ou=Departments";
            user = (User)ldapTemplate.lookup(buildDN(dn),new UserContextMapper());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * 根据自定义的属性值查询person列表
     * @param user
     * @return
     */
    public List<User> list(User user) {
        List<User> list = new ArrayList<User>();
        //查询过滤条件
        AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "daUser"));


        if (user.getCn() != null
                && user.getCn().length() > 0) {
            andFilter.and(new LikeFilter("cn", "*"+user.getCn()+"*"));
        }
        if (user.getSn() != null
                && user.getSn().length() > 0) {
            andFilter.and(new EqualsFilter("sn", user.getSn()));
        }

        if (user.getDescription() != null
                && user.getDescription().length() > 0) {
            andFilter.and(new EqualsFilter("description", user.getDescription()));
        }

        if (user.getUserPassword() != null
                && user.getUserPassword().length() > 0) {
            andFilter.and(new EqualsFilter("userPassword", user.getUserPassword()));
        }
        if (user.getSeeAlso() != null
                && user.getSeeAlso().length() > 0) {
            andFilter.and(new EqualsFilter("seeAlso", user.getSeeAlso()));
        }
        if (user.getTelephoneNumber() != null
                && user.getTelephoneNumber().length() > 0) {
            andFilter.and(new EqualsFilter("telephoneNumber", user.getTelephoneNumber()));
        }
        if (user.getMail() != null
                && user.getMail().length() > 0) {
            andFilter.and(new EqualsFilter("mail", user.getMail()));
        }
        if (user.getExtension() != null
                && user.getExtension().length() > 0) {
            andFilter.and(new EqualsFilter("extension", user.getExtension()));
        }
        if (user.getMission() != null
                && user.getMission().length() > 0) {
            andFilter.and(new EqualsFilter("mission", user.getMission()));
        }
        String base = "";
        if(!StringUtils.isEmpty(user.getDepartment())&&!StringUtils.isEmpty(user.getUnit())){
            base = new StringBuffer("ou=").append(user.getUnit())
                    .append(",ou=").append(user.getDepartment())
                    .append(",ou=Departments").toString();
        }
        //search是根据过滤条件进行查询，第一个参数是父节点的dn，可以为空，不为空时查询效率更高
        list = ldapTemplate.search(base,
                andFilter.encode(),
                new UserContextMapper());
        return list;
    }

    /**
     * 根据dn删除一条记录
     * @param dn
     */
    public void remove(String dn) {
        try {
            //dn = "cn=Mietch Underwood,ou=Development,ou=IT,ou=Departments";
            ldapTemplate.unbind(buildDN(dn));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改操作
     * @param user
     */
    public void update(User user) {
        if (user == null || user.getCn() == null || user.getCn().length() <= 0) {
            return;
        }
        List<ModificationItem> mList = new ArrayList<ModificationItem>();

        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("sn",user.getSn())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("description",user.getDescription())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("seeAlso",user.getSeeAlso())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("telephoneNumber",user.getTelephoneNumber())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("mission",user.getMission())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("mail",user.getMail())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("extension",user.getExtension())));
        mList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("userPassword",user.getUserPassword())));

        if (mList.size() > 0) {
            ModificationItem[] mArray = new ModificationItem[mList.size()];
            for (int i = 0; i < mList.size(); i++) {
                mArray[i] = mList.get(i);
            }
            //modifyAttributes 方法是修改对象的操作，与rebind（）方法需要区别开
            try {
                ldapTemplate.modifyAttributes(this.buildDN(user), mArray);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected Name buildDN(User user) throws NameNotFoundException {
        if(user == null){
            throw new NameNotFoundException();
        }
        //顶层DN:"ou=Departments,dc=example,dc=com"
        return LdapNameBuilder.newInstance("ou=Departments")
                .add("ou", user.getDepartment())
                .add("ou", user.getUnit())
                .add("cn", user.getCn())
                .build();
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
    private class UserAttributesMapper implements AttributesMapper<User> {

        @Override
        public User mapFromAttributes(Attributes attr) throws NamingException {
            User user = new User();
            user.setSn((String)attr.get("sn").get());
            user.setCn((String)attr.get("cn").get());

            if (attr.get("userPassword") != null) {
                //user.setUserPassword((String)attr.get("userPassword").get());
            }
            if (attr.get("telephoneNumber") != null) {
                user.setTelephoneNumber((String)attr.get("telephoneNumber").get());
            }
            if (attr.get("seeAlso") != null) {
                user.setSeeAlso((String)attr.get("seeAlso").get());
            }
            if (attr.get("description") != null) {
                user.setDescription((String)attr.get("description").get());
            }
            if (attr.get("extension") != null) {
                user.setExtension((String)attr.get("extension").get());
            }
            if (attr.get("mission") != null) {
                user.setMission((String)attr.get("mission").get());
            }
            return user;
        }
    }

    private static class UserContextMapper implements ContextMapper {
        @Override
        public Object mapFromContext(Object ctx) {
            DirContextAdapter context = (DirContextAdapter)ctx;
            User user = new User();
            user.setSn(context.getStringAttribute("sn"));
            user.setCn(context.getStringAttribute("cn"));

            Name dn = context.getDn();
            if(dn.size()>2){
                user.setDepartment(dn.get(1).trim().substring(3));
            }
            if(dn.size()>3 && !dn.get(2).toLowerCase().contains(user.getCn().toLowerCase())){
                user.setUnit(dn.get(2).trim().substring(3));
            }
            //if (attr.get("userPassword") != null) {
                //user.setUserPassword((String)attr.get("userPassword").get());
            //}
            if (context.getStringAttribute("telephoneNumber") != null) {
                user.setTelephoneNumber(context.getStringAttribute("telephoneNumber"));
            }
            if (context.getStringAttribute("seeAlso") != null) {
                user.setSeeAlso(context.getStringAttribute("seeAlso"));
            }
            if (context.getStringAttribute("description") != null) {
                user.setDescription(context.getStringAttribute("description"));
            }
            if (context.getStringAttribute("extension") != null) {
                user.setExtension(context.getStringAttribute("extension"));
            }
            if (context.getStringAttribute("mission") != null) {
                user.setMission(context.getStringAttribute("mission"));
            }
            return user;
        }
    }
}
