# API standards

Base path: `/api`. The process listens on port 8080. Dates are JSON strings (`yyyy-MM-dd`), not arrays. Unknown JSON fields are ignored.

## Authentication

`/api/**` requires `Authorization: Bearer <supabase access token>`. The Java API verifies the signature with the Supabase JWKS endpoint, and checks the issuer, audience, and expiry. The `sub` claim is the user id. Setup, signing-key rotation, and the role matrix are in [authentication.md](authentication.md).

The user must have an active organisation membership before calling a portfolio route. Send `X-Organisation-Id` when the user belongs to more than one organisation. That header only selects a membership the token subject already has.

The `local` Spring profile accepts calls that omit the bearer token and uses the configured local organisation. A token that is sent is still verified. See `docs/database.md`.

`/actuator/health` and `/actuator/info` do not require a token.

| Method | Path | Success |
| --- | --- | --- |
| GET, PUT | `/api/v1/me` | 200. The signed-in profile and memberships |
| GET, POST | `/api/v1/organisations` | 200 list, 201 created organisation. The caller becomes `OWNER` |
| DELETE | `/api/v1/organisations/{id}` | 204 when the caller is the owner and no portfolio row still references it |
| GET | `/api/v1/organisations/{id}/members` | 200 |
| POST | `/api/v1/organisations/{id}/invitations` | 201. The token is returned once |
| POST | `/api/v1/invitations/accept` | 200 membership. The body contains the invitation token only |

Portfolio ids are UUID strings. A non-UUID id is `400 DOMAIN_RULE`. A caller who is not a member of the requested organisation receives `403 ORGANISATION_ACCESS`.

## Resources

Collections are singular, matching the names already used by the coursework model.

| Method | Path | Success |
| --- | --- | --- |
| GET | `/api/property` | 200 array. `?available=true` returns only available properties |
| GET | `/api/property/{propertyId}` | 200 |
| POST | `/api/property` | 201 |
| PUT | `/api/property/{propertyId}` | 200 |
| DELETE | `/api/property/{propertyId}` | 204 |
| GET, POST, PUT, DELETE | `/api/tenant`, `/api/lease`, `/api/payment`, `/api/maintenance`, `/api/manager`, `/api/agent`, `/api/address`, `/api/vendor` | same status codes |
| GET, POST, DELETE | `/api/notification` | notifications are not updated through this API |
| GET | `/api/billing/plans` | 200 package catalogue. `VIEW_BILLING` |
| GET | `/api/billing/subscription` | 200 current software subscription. This is not a rent payment |
| POST | `/api/billing/subscription` | 200 demo package change for an owner. A missing `planCode` is 400. An admin is 403 |
| POST | `/api/billing/subscription/cancel` | 200 cancels the open subscription. An admin is 403 |

A property body uses `propertyId`, `address`, `description`, `rentalAmount`, and `available`. `isAvailable` is accepted when `available` is absent. The response uses `available`.

A lease body uses `leaseId`, `propertyId`, `tenantName`, and `monthlyRent`. `tenantId` is optional. When it is present, that tenant must already exist in the same organisation. Opening a lease requires the property to be available and then marks that property unavailable.

A payment body uses `paymentId`, `leaseId`, `amount`, `paymentDate`, and `paymentMethod`. The amount must be positive and the lease must exist. A `status` field is ignored because the rent entity does not store one.

A maintenance body uses `requestId`, `description`, `status`, `reportedDate`, and `cost`.

Ids are supplied by the client and must be unique. The path id and the body id must match on update.

## Errors

```json
{ "error": "DOMAIN_RULE", "message": "A leased property cannot be marked available" }
```

| Status | `error` | When |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Bean validation failed |
| 400 | `MALFORMED_JSON` | The body could not be read |
| 400 | `DOMAIN_RULE` | A domain policy or factory rule failed |
| 401 | Spring Security body | Missing, expired, or invalid bearer token |
| 403 | `EMAIL_NOT_VERIFIED` | The token says the email address is not confirmed |
| 403 | `ORGANISATION_ACCESS` | The user has no active organisation, or the requested organisation is not one of theirs |
| 403 | `FORBIDDEN` | The membership does not allow that action |
| 404 | `<ENTITY>_NOT_FOUND` | The id is not in this organisation |
| 409 | `DUPLICATE` | The id already exists |
| 409 | `CONFLICT` | The database rejected the change, usually because a related row still exists |

## Persistence rules that show up as API behaviour

Rent and maintenance costs are stored as `numeric(12,2)`. A negative rental amount, rent payment, or maintenance cost is rejected. Deleting a property that still has a lease, a lease that still has a payment, or a tenant that still has a notification returns 409.

`GET /api/billing/subscription` returns the Starter demo entitlement when the organisation has no subscription row. A new organisation is stored with that Starter demo entitlement. Choosing a package through simulated checkout stores provider `demo`, subscription status `DEMO`, and an invoice status of `DEMO`. It does not create a rent payment, a `PAID` invoice, or an `ACTIVE` subscription. A canceled checkout does not change the package. A non-demo open subscription is not rewritten. Pending invitations count toward the member cap. A canceled subscription blocks new properties and invitations until a package is chosen again. Existing rows over the cap can still be viewed.
