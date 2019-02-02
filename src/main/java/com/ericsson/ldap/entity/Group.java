package com.ericsson.ldap.entity;

import java.util.ArrayList;
import java.util.List;

//@Entry(objectClasses = {"groupOfNames", "top"}, base = "ou=Groups")
public class Group {

    private String cn; //必填属性

    private String seeAlso; //可选属性
    private String description;  //可选属性

    List<String> members = new ArrayList<String>();

    public String getCn() {
        return cn;
    }
    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSeeAlso() {
        return seeAlso;
    }
    public void setSeeAlso(String seeAlso) {
        this.seeAlso = seeAlso;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
