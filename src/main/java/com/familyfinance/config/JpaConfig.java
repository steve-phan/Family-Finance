package com.familyfinance.config;

import com.familyfinance.tenant.FamilyTenantResolver;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @Bean
    HibernatePropertiesCustomizer tenantHibernateProperties(FamilyTenantResolver tenantResolver) {
        return properties -> properties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                (CurrentTenantIdentifierResolver<Long>) tenantResolver
        );
    }
}
