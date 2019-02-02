package com.ericsson.web;

import com.ericsson.ldap.dao.OrganizationRepo;
import com.ericsson.ldap.dao.OrganizationRepo;
import com.ericsson.ldap.entity.Organization;
import com.ericsson.ldap.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrganizationController {
    @Autowired
    private OrganizationRepo organizationRepo;

    @PostMapping("/organization")
    public Organization create(@RequestBody Organization organization){
        organizationRepo.create(organization);
        return organization;
    }

    /**
     * 全部属性更新
     * @param organization
     * @return
     */
    @PutMapping("/organization")
    public Organization updateAll(@RequestBody Organization organization){
        organizationRepo.update(organization);
        return organization;
    }

    /**
     * 全部属性更新
     * @param organization
     * @return
     */
    @PatchMapping("/organization")
    public Organization patchUpdate(@RequestBody Organization organization){
        organizationRepo.update(organization);
        return organization;
    }

    /**
     * 获取用户
     * @param dn
     * @return
     */
    @GetMapping("/organization")
    public Organization get(@RequestParam(required = true,name = "dn") String dn){
        return organizationRepo.getOrganizationDetail(dn);
    }

    /**
     * 查询
     * @param organization
     * @return
     */
    @GetMapping("/organizations")
    public List<Organization> query(@RequestBody Organization organization){
        return organizationRepo.list(organization);
    }
}
