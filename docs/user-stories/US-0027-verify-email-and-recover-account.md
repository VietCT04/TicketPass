# US-0027 — Verify Email And Recover Account Access Securely

## User Story

As a TicketPass user, I want to verify control of my email address and recover access when I forget my password so that my account and marketplace activity remain secure without depending on manual support intervention.

## Context

TicketPass currently supports email/password signup, login, revocable opaque sessions, and authenticated marketplace actions. It does not confirm mailbox control, deliver transactional account links durably, or provide a forgotten-password workflow.

A production account-access lifecycle must remain safe when delivery is delayed, duplicated, retried, or interrupted. It must not expose whether an account exists, persist raw action credentials, keep compromised sessions alive after password recovery, or allow unverified accounts to create new marketplace commitments.

## Goal

Deliver a complete provider-neutral email-verification and password-recovery lifecycle with durable delivery, single-use credentials, abuse controls, session revocation, verified-email marketplace eligibility, and clear browser experiences.

## Scope

### Verification state

- Add authoritative email-verification state to user accounts.
- Treat existing accounts according to an explicit compatibility migration policy.
- Keep login and account-recovery access available to unverified users.
- Expose safe verification state through authenticated current-user responses.

### Account-action credentials

- Generate cryptographically random opaque credentials for email verification and password recovery.
- Store only hashes in the credential table.
- Bind each credential to one user, purpose, expiry, and single-use lifecycle.
- Supersede earlier usable credentials when a replacement is issued.
- Serialize redemption and password mutation through database locks.

### Durable account email delivery

- Persist transactional verification and recovery delivery intent atomically with credential issuance.
- Encrypt temporary action-secret material at rest with an externally configured versioned key.
- Use a provider-neutral delivery interface with stable message idempotency.
- Claim bounded work with processing leases and retry recoverable failures.
- Run external provider calls outside user and marketplace database locks.
- Clear encrypted secret material after confirmed delivery or terminal expiry.
- Provide an explicitly local development adapter without representing it as production email capability.

### Email verification

- Issue verification delivery for new accounts.
- Support public token redemption and authenticated resend.
- Enforce credential expiry, resend cooldown, and bounded issuance frequency.
- Make repeated redemption safe after the account is already verified.
- Record one minimal verification audit event for the effective transition.

### Password recovery

- Accept public recovery requests using generic responses that do not reveal account existence.
- Apply persistent bounded throttling using protected normalized-email identifiers.
- Support public token redemption with a compliant new password.
- Never trim, normalize, return, log, or persist plaintext passwords.
- Atomically replace the password hash, verify mailbox control, invalidate outstanding account-action credentials, and revoke every active session.
- Require a fresh login after successful recovery.
- Record one minimal password-recovery audit event.

### Marketplace eligibility

Require verified email before creating new marketplace commitments:

- creating a listing;
- submitting a missing-event request;
- creating a reservation;
- starting checkout.

Do not block safe account access or completion of already-existing obligations:

- browse, login, logout, profile, verification, resend, and recovery;
- reservation release and eligible listing cancellation;
- existing order progress;
- seller transfer confirmation and buyer receipt confirmation for existing paid orders.

### User experience

- Add public verification and password-recovery routes.
- Add authenticated verification status, resend controls, and unverified-account guidance.
- Keep action credentials in transient route/form memory only.
- Remove credentials from visible browser history after submission.
- Use allowlisted navigation targets only.
- Handle pending delivery, cooldown, expired, superseded, consumed, already-completed, and retryable outcomes.
- Keep email, password, and credential values out of browser persistence, analytics, logs, and error reporting.

## Delivery Issues

- #174 — Define email verification and account recovery contract.
- #187 — Implement verified-email and account-action persistence.
- #188 — Implement durable account email delivery.
- #189 — Implement email verification and resend backend.
- #190 — Implement password recovery and session revocation backend.
- #191 — Enforce verified-email marketplace eligibility.
- #192 — Build email verification experience.
- #193 — Build password recovery experience.

## Out of Scope

- Email-address changes.
- Passwordless login, OAuth, SSO, MFA, passkeys, security questions, or recovery codes.
- Admin recovery, impersonation, account suspension, or account deletion.
- Marketing email, SMS, push notifications, arbitrary messaging, or attachments.
- Production email-provider selection, DNS configuration, or provider account provisioning.
- CAPTCHA, device fingerprinting, KYC, or identity-document verification.
- Automatic migration of email addresses or account merging.

## Acceptance Criteria

- [ ] New accounts have truthful server-authoritative email-verification state.
- [ ] Existing accounts follow an explicit compatibility migration policy.
- [ ] Verification and recovery credentials are purpose-bound, expiring, single-use, and concurrency-safe.
- [ ] Raw account-action credentials are never persisted unencrypted or logged.
- [ ] Transactional account email survives restarts and transient delivery failures.
- [ ] Recovery requests do not disclose whether an account exists.
- [ ] Successful password recovery revokes every active session and requires fresh login.
- [ ] Verified-email requirements are enforced consistently for new marketplace commitments.
- [ ] Existing obligations and safe release actions remain accessible.
- [ ] Browser flows keep passwords and credentials out of persistent storage and analytics.
- [ ] Relevant API, database, security, deployment, operations, flow, concern, and continuity documentation is updated.

## Concerns

- Encryption-key rotation and missing-key startup behavior must fail closed.
- Generic public responses reduce but do not eliminate timing-analysis risk.
- Email delivery may be delayed or duplicated, so credential redemption remains authoritative.
- Recovery and authenticated password changes must share the same user-row lock.
- Persistent throttling must avoid storing additional raw email copies.
- Verification gating must remain centralized as new marketplace actions are added.
- A production email provider and domain reputation remain deployment decisions outside this story.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, image builds, Compose startup, or other verification commands in this story. Complete application implementation first; verification will be handled later as a separate final phase.
