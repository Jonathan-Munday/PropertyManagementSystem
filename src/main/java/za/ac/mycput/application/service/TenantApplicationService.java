package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.TenantCommand;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.factory.TenantFactory;
import za.ac.mycput.domain.model.Tenant;
import za.ac.mycput.domain.repository.ITenantRepository;
import za.ac.mycput.domain.valueobject.EmailAddress;

import java.util.List;

@Service
public class TenantApplicationService {

    private final ITenantRepository tenants;

    public TenantApplicationService(ITenantRepository tenants) {
        this.tenants = tenants;
    }

    @Transactional
    public List<Tenant> list() {
        return tenants.getAll();
    }

    @Transactional
    public Tenant get(String tenantId) {
        return RepositoryOutcomes.existing(tenants.read(tenantId), "Tenant", tenantId);
    }

    @Transactional
    public Tenant create(TenantCommand command) {
        Tenant tenant = build(command);
        RepositoryOutcomes.absent(tenants.read(tenant.getTenantId()), "Tenant", tenant.getTenantId());
        return RepositoryOutcomes.created(tenants.create(tenant), "Tenant", tenant.getTenantId());
    }

    @Transactional
    public Tenant update(String tenantId, TenantCommand command) {
        if (!tenantId.equals(command.tenantId())) {
            throw new DomainRuleException("The tenant id in the path and body must match");
        }
        RepositoryOutcomes.existing(tenants.read(tenantId), "Tenant", tenantId);
        return RepositoryOutcomes.updated(tenants.update(build(command)), "Tenant", tenantId);
    }

    @Transactional
    public void delete(String tenantId) {
        RepositoryOutcomes.deleted(tenants.delete(tenantId), "Tenant", tenantId);
    }

    private Tenant build(TenantCommand command) {
        new EmailAddress(command.email());
        return RepositoryOutcomes.built(
                TenantFactory.createTenant(
                        command.tenantId(),
                        command.firstName(),
                        command.lastName(),
                        command.email(),
                        command.phoneNumber()),
                "A tenant needs an id and a first name");
    }
}
