package com.ericsson.web;

import com.ericsson.ldap.dao.GroupRepo;
import com.ericsson.ldap.dao.UserRepo;
import com.ericsson.ldap.entity.Group;
import com.ericsson.ldap.entity.User;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GroupController {
    @Autowired
    private GroupRepo groupRepo;

    @PostMapping("/group")
    public Group create(@RequestBody Group group){
        groupRepo.create(group);
        return group;
    }

    /**
     * 全部属性更新
     * @param group
     * @return
     */
    @PutMapping("/group")
    public Group updateAll(@RequestBody Group group){
        groupRepo.update(group);
        return group;
    }

    /**
     * 部分属性更新
     * @param group
     * @return
     */
    @PatchMapping("/group")
    public Group patchUpdate(@RequestBody Group group){
        groupRepo.update(group);
        return group;
    }

    /**
     * 获取用户组
     * @param dn
     * @return
     */
    @GetMapping("/group")
    public Group get(@RequestParam(required = true,name = "dn") String dn){
        return groupRepo.getGroupDetail(dn);
    }

    /**
     * 查询
     * @param group
     * @return
     */
    @GetMapping("/groups")
    public List<Group> query(@RequestBody Group group){
        return groupRepo.list(group);
    }

    /**
     * 把用户放进用户组
     * @param map {"groupName":"ROLE_ADMIN",
     *            "users":[{
     *                  "cn":"Jackson Micheal",
     *                  "unit":"Support",
     *                  "department":"IT"
     *            },{
     *                  "cn":"Tomkins Jeremy",
     *                  "unit":"Consult",
     *                  "department":"IT"
     *            }]}
     * @return
     */
    @PostMapping("/groups/addMembers")
    public Object addMembers(@RequestBody Map map){
        String groupName = map.get("groupName").toString();
        List<Map> users = (List)map.get("users");
        users.stream().forEach(
                e ->{
                    User user = new User();
                    BeanUtils.copyProperties(e,user);
                    groupRepo.addMemberToGroup(groupName,user);
                }
        );
        return true;
    }

    /**
     * 把用户移出用户组
     * @param map
     * @return
     */
    @PostMapping("/groups/removeMembers")
    public Object removeMembers(@RequestBody Map map){
        String groupName = map.get("groupName").toString();
        User user = new User();
        BeanUtils.copyProperties(map.get("user"),user);
        return groupRepo.addMemberToGroup(groupName,user);
    }
}
