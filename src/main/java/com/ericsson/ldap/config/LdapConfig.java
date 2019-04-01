package com.ericsson.ldap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.config.DefaultRenamingStrategyParser;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;

//@Configuration
//@org.springframework.transaction.annotation.EnableTransactionManagement
public class LdapConfig {
//    @Autowired
    private LdapProperties properties;
    private Environment environment;

    @Bean
    public ContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUserDn(this.properties.getUsername());
        contextSource.setPassword(this.properties.getPassword());
        contextSource.setBase(this.properties.getBase());
        contextSource.setBase(this.properties.getBase());
        return contextSource;
    }

    @Bean(name = "adLdapTemplate")
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }


    @Bean
    public ContextSourceTransactionManager ldapTxManager(ContextSource contextSource){
        ContextSourceTransactionManager manager = new ContextSourceTransactionManager();
        manager.setContextSource(contextSource);
        new DefaultRenamingStrategyParser();
        //manager.setRenamingStrategy(new DifferentSubtreeTempEntryRenamingStrategy());
        return new ContextSourceTransactionManager();
    }


}
