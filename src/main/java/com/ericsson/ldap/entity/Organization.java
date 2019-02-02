package com.ericsson.ldap.entity;

//@Entry(objectClasses = {"top","organizationalUnit"}, base = "ou=Departments")
public class Organization {
    private String ou; //必填属性

    private String telephoneNumber; //可选属性
    private String postalAddress; //可选属性
    private String postalCode; //可选属性
    private String seeAlso; //可选属性
    private String description;  //可选属性

    private String dn;//唯一
    private String parentUnit;//虚拟属性

    public String getDN() {
        StringBuilder builder = new StringBuilder("ou=").append(this.ou);
        if(this.parentUnit != null && this.parentUnit.length()>0){
            builder.append(",ou=").append(parentUnit);
        }
        builder.append(",ou=Departments");
        return builder.toString();
    }
    public String getOu() {
        return ou;
    }
    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
    public String getParentUnit() {
        return parentUnit;
    }

    public void setParentUnit(String parentUnit) {
        this.parentUnit = parentUnit;
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
}
