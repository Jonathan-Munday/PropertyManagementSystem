# Domain model

The portfolio model is the set of entities already built for the coursework. They stay immutable behind builders. Factories remain the supported way to construct them. New rules that do not belong on one entity live in domain services and are expressed with value objects.

## Entities

| Entity | Identity | What it represents |
| --- | --- | --- |
| `Property` | `propertyId` | A lettable unit: address line, description, rental amount, availability |
| `Address` | `addressId` | A structured postal address. It is not embedded in `Property` |
| `Tenant` | `tenantId` | A person who may sign a lease |
| `Lease` | `leaseId` | An agreement for one property, stored with `propertyId`, `tenantName`, and `monthlyRent` |
| `Payment` | `paymentId` | Rent collected for a lease. It is not a software subscription charge |
| `Maintenance` | `requestId` | A maintenance request: description, status, reported date, and cost |
| `Manager` | `managerId` | A staff manager |
| `Agent` | `agentId` | A staff agent with an email and a job label |
| `Vendor` | `vendorId` | A contractor company and specialty |
| `Notification` | `notificationId` | A message addressed to a tenant |

`Property.address` remains a single string. Replacing it with the `Address` entity would break `PropertyFactory` and the property tests. Structured addresses are stored through `Address` on their own.

`Lease` can store an optional `tenantId`. Start date and end date are still absent. `Payment` does not store a paid/pending/overdue status. The API ignores those extra JSON fields. Maintenance status is `OPEN`, `IN_PROGRESS`, or `RESOLVED`.

## Factories

The original factory methods still exist and still return `null` when their original checks fail. Additional overloads accept a caller-supplied id for tenants, managers, vendors, and notifications. The API uses those overloads so a client id is not replaced by a random UUID. The original methods still generate an id and delegate to the new overload.

`PaymentFactory.createAgent` still builds agents. `MaintenanceFactory.createVendor` still builds vendors. `TenantFactory.createNotification` still builds notifications. Those methods were not split into new factory types.

## Value objects

`Money` is an amount and a currency. `Money.zar` rejects non-finite numbers and scales to cents. Policies use it to decide sign. It does not replace the `double` fields on the entities.

`EmailAddress` rejects a blank or malformed address. Tenant and agent creation in the application layer require one. The original tenant factory does not, so its tests still accept the values they already used.

## Invariants enforced by domain services

`RentalAmountPolicy` rejects a negative rental amount. Zero is allowed.

`PropertyOccupancyPolicy` allows a new lease only when the property exists, the lease refers to that property, the property is available, and the monthly rent is positive. It also rejects marking a property available while a lease still points at it.

`RentCollectionPolicy` requires a lease id, a payment method, and a positive amount. Recording rent also requires the lease to exist; that lookup is the use case's job because it needs a repository.

`MaintenanceCostPolicy` requires a description, a status, and a cost that is not negative. Zero is allowed.

Entities still do not throw for every invalid combination, because the existing factories return `null` and the existing tests depend on that. The application layer treats a `null` factory result as `DomainRuleException`.

## Identity and equality

Equality is by id for `Property`, `Tenant`, `Lease`, `Payment`, `Agent`, `Address`, `Notification`, `Maintenance`, `Manager`, and `Vendor`. Tenant and notification equality no longer throw when the id is null. `Money` and `EmailAddress` are equal by value.

## Repository ports

`IRepository` is still create, read, update, delete, and getAll. `IPropertyRepository` adds `findAvailable`. `IPaymentRepository` adds `findByPaymentMethod`. `IMaintenanceRepository` keeps `save`, `findById`, `findAll`, `update`, and `delete` so the existing maintenance tests keep compiling. Lease, agent, address, vendor, and notification now have their own ports extending `IRepository`.

The in-memory property update removes only the property whose id matches. It does not clear the set.
