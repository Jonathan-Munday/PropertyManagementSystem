package za.ac.mycput.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import za.ac.mycput.domain.service.BusinessCalendar;
import za.ac.mycput.domain.service.LeaseTermPolicy;
import za.ac.mycput.domain.service.MaintenanceCostPolicy;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;
import za.ac.mycput.domain.service.RentCollectionPolicy;
import za.ac.mycput.domain.service.RentalAmountPolicy;
import za.ac.mycput.domain.service.RolePermissionPolicy;

import java.time.Clock;

@Configuration
public class DomainPolicyConfig {

    @Bean
    RentalAmountPolicy rentalAmountPolicy() {
        return new RentalAmountPolicy();
    }

    @Bean
    RentCollectionPolicy rentCollectionPolicy() {
        return new RentCollectionPolicy();
    }

    @Bean
    MaintenanceCostPolicy maintenanceCostPolicy() {
        return new MaintenanceCostPolicy();
    }

    @Bean
    PropertyOccupancyPolicy propertyOccupancyPolicy() {
        return new PropertyOccupancyPolicy();
    }

    @Bean
    LeaseTermPolicy leaseTermPolicy() {
        return new LeaseTermPolicy();
    }

    @Bean
    Clock clock() {
        return Clock.system(BusinessCalendar.ZONE);
    }

    @Bean
    RolePermissionPolicy rolePermissionPolicy() {
        return new RolePermissionPolicy();
    }
}
