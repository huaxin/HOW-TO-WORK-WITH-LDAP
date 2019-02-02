package com.ericsson;

import com.alibaba.fastjson.JSON;
import com.ericsson.domain.*;
import com.ericsson.ldap.dao.GroupRepo;
import com.ericsson.ldap.dao.OrganizationRepo;
import com.ericsson.ldap.dao.UserRepo;
import com.ericsson.ldap.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private LdapTemplate ldapTemplate;
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
		ldapTemplate.search(
				query().where("objectclass").is("person"),
				new AttributesMapper<String>() {
					public String mapFromAttributes(Attributes attrs)
							throws NamingException {
						return (String) attrs.get("cn").get();
					}
				}).stream().forEach(e -> System.out.println(e.toString()));
	}


}
