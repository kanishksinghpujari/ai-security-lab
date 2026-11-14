# Architecture

## Current slice

This first milestone is deliberately small so that the security boundary is easy to understand.

- Spring Boot exposes a JSON API on port 8080.
- HTTP Basic authenticates two disposable in-memory demo users.
- The lab-admin user has ADMIN and USER roles.
- The lab-customer user has USER role only.
- Product creation requires ADMIN.
- Order reads require the order owner or ADMIN.
- The default profile does not load the deliberately vulnerable demonstration controller.
- Docker publishes the service only on host loopback and runs the JVM as a non-root user.

## Seed data

The in-memory demo includes two products and two orders. Order 1001 belongs to lab-customer; order 1002 belongs to another-customer. The fixture makes the ownership boundary testable without using real personal data.

## Important limitations

This is an intentionally compact test target, not a production commerce platform:
- Data is in-memory and resets on restart.
- Authentication is HTTP Basic with local demo users, not an OIDC identity provider.
- No payment processing is implemented.
- Upload scanning, persistent storage, frontend and LLM assistant are future milestones.
- CSRF is disabled for this stateless API demo; do not reuse this configuration for browser session/cookie authentication without a CSRF design.
- Production profile must supply real authentication and secret management.

## Vulnerable exercise

The lab-vulnerable Spring profile adds an IDOR route. Keep it local and isolated. The same sample order data lets a scanner or manual test compare the vulnerable route with the protected API route.
