package com.ericsson;

import com.alibaba.fastjson.JSON;
//import com.ericsson.domain.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.*;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.query.ConditionCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.NamingException;
import javax.naming.directory.*;

import java.util.*;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private LdapTemplate ldapTemplate;
	@Autowired
	private org.springframework.boot.autoconfigure.ldap.LdapProperties ldapProperties;
	/*@Autowired
	private UserRepo userRepo;
	@Autowired
	private GroupRepo groupRepo;
	@Autowired
	private OrganizationRepo orgRepo;*/

	@Test
	public void userRepo1() throws JsonProcessingException {
		com.ericsson.ldap.entity.User user = new com.ericsson.ldap.entity.User();
		user.setCn("Luka Eureka");
		user.setSn("Eureka");
		user.setDepartment("IT");
		user.setUnit("System");
		user.setMission("1000");
		user.setExtension("{\"abc\":\"1234v\",\"key\":\"2019.01.30\"}");

		//userRepo.create(user);

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
		Map map =new HashMap();
		map.put("name","zhangsan");
		map.put("age",19);
		map.put("company","apple");

		ObjectMapper mapper = new ObjectMapper();
		attr.put("jsona",JSON.parseObject(JSON.toJSONString(map)));//mapper.writeValueAsString(map));//map
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


		ldapTemplate.bind(LdapNameBuilder.newInstance("ou=Departments")
				.add("ou", user.getDepartment())
				.add("ou", user.getUnit())
				.add("cn", user.getCn())
				.build(), null, attr);

	}

	@Test
	public void find(){
		/*ldapTemplate.search(
				query().where("objectclass").is("daUser"),
				new AttributesMapper<String>() {
					public String mapFromAttributes(Attributes attrs)
							throws NamingException {
						System.out.println(attrs);
						return (String) attrs.get("pwdHistory").get();
					}
				}).stream().forEach(e -> System.out.println(e.toString()));*/

		String baseDN = "cn=Mietch Underwood,ou=Development,ou=IT,ou=Departments";
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "daUser"))
				.and(new LikeFilter("pwdHistory","20190301070853*"));
		String[] userAttrList = {"cn", "sn","pwdHistory","pwdChangedTime","userAccountControl"};
		SearchControls searchControls = new SearchControls(
				SearchControls.SUBTREE_SCOPE, 0, 0, null, false, false);
		searchControls.setReturningAttributes(userAttrList);
		List<Object> list = ldapTemplate.search("",
				andFilter.encode(),
				searchControls,new ContextMapper<Object>() {
			@Override
			public Object mapFromContext(Object ctx) throws NamingException {
				DirContextAdapter context = (DirContextAdapter)ctx;
				List<String> memberof = new ArrayList();
				Enumeration vals = context.getAttributes().get("pwdHistory").getAll();
				while (vals.hasMoreElements()) {
					String entry = (String)vals.nextElement();
					String[] history = entry.split("#");
					Long time = Long.valueOf(history[0].substring(0,14));
					System.out.println(time);
					memberof.add(entry);
				}
				return memberof;
			}
		});
		((List<String>)list.get(0)).stream().forEach(e -> System.out.println(e.toString()));
//		ldapTemplate.search(
//				query().where("objectclass").is("daUser"),
//				new ContextMapper<String>() {
//					@Override
//					public String mapFromContext(Object ctx) {
//						DirContextAdapter context = (DirContextAdapter)ctx;
//						return (String) ((DirContextAdapter) ctx).getStringAttribute("pwdHistory");
//					}
//				}).stream().forEach(e -> System.out.println(e.toString()));
	}

	@Test
	public void group(){
		String baseDN = "cn=Mietch Underwood,ou=Development,ou=IT,ou=Departments";
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "groupOfUniqueNames"))
				.and(new LikeFilter("uniqueMember","cn=System*"));
		String[] userAttrList = {"cn", "dn","uniqueMember","objectclass"};
		SearchControls searchControls = new SearchControls(
				SearchControls.SUBTREE_SCOPE, 0, 0, null, false, false);
		searchControls.setReturningAttributes(userAttrList);
		List<Object> list = ldapTemplate.search("",
				andFilter.encode(),
				searchControls,new ContextMapper<Object>() {
					@Override
					public Object mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter context = (DirContextAdapter)ctx;
						List<String> uniqueMembers = new ArrayList();
						Attribute attr = context.getAttributes().get("uniqueMember");

						for (int i=0;i<attr.size();i++){
							uniqueMembers.add((String) attr.get(i));
						}
						/*Enumeration vals = context.getAttributes().get("uniqueMember").getAll();
						while (vals.hasMoreElements()) {
							String entry = (String)vals.nextElement();
							String[] history = entry.split("#");
							Long time = Long.valueOf(history[0].substring(0,14));
							System.out.println(time);
							uniqueMembers.add(entry);
						}*/
						return uniqueMembers;
					}
				});
		System.out.println(list.size());
		((List<String>)list.get(0)).stream().forEach(e -> System.out.println(e.toString()));
	}

	@Test
	public void org(){
		Assert.assertEquals(ldapProperties.getBase(),"dc=example,dc=com");
		String orgDn = "ou=IT,ou=Departments,dc=example,dc=com";
		ldapTemplate.search(query()
						.base(orgDn.substring(0,orgDn.indexOf(ldapProperties.getBase())-1))
						.attributes("ou","createTimestamp")
						.searchScope(SearchScope.ONELEVEL)
						.where("objectClass").is("organizationalUnit"),
				new ContextMapper<Object>() {
					@Override
					public Object mapFromContext(Object ctx) throws NamingException {
						DirContextAdapter context = (DirContextAdapter) ctx;
						String dn = context.getStringAttribute("ou");
						System.out.println(dn);
						return null;
					}
				});
	}


}
