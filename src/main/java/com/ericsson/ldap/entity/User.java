package com.ericsson.ldap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

//@Entry(objectClasses = {"daUser", "inetOrgPerson", "organizationalPerson", "person", "top" }, base = "ou=Departments")
public class User {
    private String sn; //必填属性
    private String cn; //必填属性

    private String userPassword; //可选属性
    private String telephoneNumber; //可选属性
    private String mail; //可选属性
    private String seeAlso; //可选属性
    private String description;  //可选属性

    //@JsonIgnore
    private String dn;//唯一
    private String unit;//2级 虚拟属性
    private String department;//1级 虚拟属性

    private String extension;//扩展属性
    private String mission;//扩展属性

    public String getDn() {
        return new StringBuilder("cn=").append(this.cn)
                .append(",ou=").append(unit)
                .append(",ou=").append(department)
                .append(",ou=Departments").toString();
    }
    /*
    public void setDn(String dn) {
        this.dn = dn;
    }
    */
    public String getSn() {
        return sn;
    }
    public void setSn(String sn) {
        this.sn = sn;
    }
    public String getCn() {
        return cn;
    }
    public void setCn(String cn) {
        this.cn = cn;
    }
    public String getUserPassword() {
        return userPassword;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public String getTelephoneNumber() {
        return telephoneNumber;
    }
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }
}
