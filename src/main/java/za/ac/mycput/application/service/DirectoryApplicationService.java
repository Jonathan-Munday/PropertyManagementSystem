package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.AddressCommand;
import za.ac.mycput.application.dto.NotificationCommand;
import za.ac.mycput.application.dto.VendorCommand;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.factory.AddressFactory;
import za.ac.mycput.domain.factory.MaintenanceFactory;
import za.ac.mycput.domain.factory.TenantFactory;
import za.ac.mycput.domain.model.Address;
import za.ac.mycput.domain.model.Notification;
import za.ac.mycput.domain.model.Vendor;
import za.ac.mycput.domain.repository.IAddressRepository;
import za.ac.mycput.domain.repository.INotificationRepository;
import za.ac.mycput.domain.repository.ITenantRepository;
import za.ac.mycput.domain.repository.IVendorRepository;

import java.util.List;

@Service
public class DirectoryApplicationService {

    private final IAddressRepository addresses;
    private final IVendorRepository vendors;
    private final INotificationRepository notifications;
    private final ITenantRepository tenants;

    public DirectoryApplicationService(IAddressRepository addresses,
                                       IVendorRepository vendors,
                                       INotificationRepository notifications,
                                       ITenantRepository tenants) {
        this.addresses = addresses;
        this.vendors = vendors;
        this.notifications = notifications;
        this.tenants = tenants;
    }

    @Transactional
    public List<Address> listAddresses() {
        return addresses.getAll();
    }

    @Transactional
    public Address getAddress(String addressId) {
        return RepositoryOutcomes.existing(addresses.read(addressId), "Address", addressId);
    }

    @Transactional
    public Address createAddress(AddressCommand command) {
        Address address = address(command);
        RepositoryOutcomes.absent(addresses.read(address.getAddressId()), "Address", address.getAddressId());
        return RepositoryOutcomes.created(addresses.create(address), "Address", address.getAddressId());
    }

    @Transactional
    public Address updateAddress(String addressId, AddressCommand command) {
        if (!addressId.equals(command.addressId())) {
            throw new DomainRuleException("The address id in the path and body must match");
        }
        RepositoryOutcomes.existing(addresses.read(addressId), "Address", addressId);
        return RepositoryOutcomes.updated(addresses.update(address(command)), "Address", addressId);
    }

    @Transactional
    public void deleteAddress(String addressId) {
        RepositoryOutcomes.deleted(addresses.delete(addressId), "Address", addressId);
    }

    @Transactional
    public List<Vendor> listVendors() {
        return vendors.getAll();
    }

    @Transactional
    public Vendor getVendor(String vendorId) {
        return RepositoryOutcomes.existing(vendors.read(vendorId), "Vendor", vendorId);
    }

    @Transactional
    public Vendor createVendor(VendorCommand command) {
        Vendor vendor = vendor(command);
        RepositoryOutcomes.absent(vendors.read(vendor.getVendorId()), "Vendor", vendor.getVendorId());
        return RepositoryOutcomes.created(vendors.create(vendor), "Vendor", vendor.getVendorId());
    }

    @Transactional
    public Vendor updateVendor(String vendorId, VendorCommand command) {
        if (!vendorId.equals(command.vendorId())) {
            throw new DomainRuleException("The vendor id in the path and body must match");
        }
        RepositoryOutcomes.existing(vendors.read(vendorId), "Vendor", vendorId);
        return RepositoryOutcomes.updated(vendors.update(vendor(command)), "Vendor", vendorId);
    }

    @Transactional
    public void deleteVendor(String vendorId) {
        RepositoryOutcomes.deleted(vendors.delete(vendorId), "Vendor", vendorId);
    }

    @Transactional
    public List<Notification> listNotifications() {
        return notifications.getAll();
    }

    @Transactional
    public Notification getNotification(String notificationId) {
        return RepositoryOutcomes.existing(notifications.read(notificationId), "Notification", notificationId);
    }

    @Transactional
    public Notification createNotification(NotificationCommand command) {
        RepositoryOutcomes.existing(tenants.read(command.tenantId()), "Tenant", command.tenantId());
        Notification notification = notification(command);
        RepositoryOutcomes.absent(
                notifications.read(notification.getNotificationId()), "Notification", notification.getNotificationId());
        return RepositoryOutcomes.created(
                notifications.create(notification), "Notification", notification.getNotificationId());
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        RepositoryOutcomes.deleted(notifications.delete(notificationId), "Notification", notificationId);
    }

    private Address address(AddressCommand command) {
        return RepositoryOutcomes.built(
                AddressFactory.createAddress(
                        command.addressId(),
                        command.streetAddress(),
                        command.city(),
                        command.province(),
                        command.postalCode()),
                "An address needs an id and a street address");
    }

    private Vendor vendor(VendorCommand command) {
        return RepositoryOutcomes.built(
                MaintenanceFactory.createVendor(
                        command.vendorId(), command.companyName(), command.contactNumber(), command.specialty()),
                "A vendor needs an id, a company name, a contact number, and a specialty");
    }

    private Notification notification(NotificationCommand command) {
        return RepositoryOutcomes.built(
                TenantFactory.createNotification(
                        command.notificationId(),
                        command.message(),
                        command.type(),
                        command.dateSent(),
                        command.tenantId()),
                "A notification needs an id and a message");
    }
}
