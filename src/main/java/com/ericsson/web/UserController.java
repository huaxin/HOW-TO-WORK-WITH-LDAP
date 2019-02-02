package com.ericsson.web;

import com.ericsson.ldap.dao.UserRepo;
import com.ericsson.ldap.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/")
    public String index(){
        return "Hello world";
    }

    @PostMapping("/user")
    public User create(@RequestBody User user){
        userRepo.create(user);
        return user;
    }

    /**
     * 全部属性更新
     * @param user
     * @return
     */
    @PutMapping("/user")
    public User updateAll(@RequestBody User user){
        userRepo.update(user);
        return user;
    }

    /**
     * 全部属性更新
     * @param user
     * @return
     */
    @PatchMapping("/user")
    public User patchUpdate(@RequestBody User user){
        userRepo.update(user);
        return user;
    }

    /**
     * 获取用户
     * @param dn
     * @return
     */
    //@GetMapping("/user")
    public User get(@RequestParam(required = true,name = "dn") String dn){
        return userRepo.getUserDetail(dn);
    }

    /**
     * 获取用户
     * @param map
     * @return
     */
    @GetMapping("/user")
    public Object get(@RequestBody Map map){
        String dn = map.get("dn").toString();
        if(StringUtils.isEmpty(dn)){
            return "{\"success\":false,\"message\":\"missing the parameter dn\"}";
        }
        return userRepo.getUserDetail(dn);
    }

    /**
     * 查询
     * @param user
     * @return
     */
    @GetMapping("/users")
    public List<User> query(@RequestBody User user){
        return userRepo.list(user);
    }

    /**
     * 换部门
     * @param map {"dn":"ou=zhang san,ou=Support,ou=IT,ou=Departments",
     *            "newDN":"ou=zhang san,ou=Development,ou=IT,ou=Departments"}
     * @return
     */
    @PostMapping("/user/changeOu")
    public Object create(@RequestBody Map map){

        return userRepo.changeOU(map.get("dn").toString(),map.get("newDN").toString());
    }
}
