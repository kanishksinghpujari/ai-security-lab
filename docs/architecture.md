# Architecture

## Current slice

This is a deliberately compact commerce-like test target with clear security boundaries.

- Spring Boot exposes a JSON API on port 8080 and serves a static dashboard.
- HTTP Basic authenticates two disposable in-memory demo users.
- The lab-admin user has ADMIN and USER roles.
- The lab-customer user has USER role only.
- Product creation requires ADMIN.
- Order reads require the order owner or ADMIN.
- The default profile does not load the deliberately vulnerable demonstration controller.
- Docker publishes the app only on host loopback and runs the JVM as a non-root user.
- ClamAV runs as a separate service on the internal Compose network; port 3310 is not published to the host.

## Seed data

The in-memory demo includes two products and two orders. Order 1001 belongs to lab-customer; order 1002 belongs to another-customer. The fixture makes the ownership boundary testable without using real personal data.

## Upload path

The authenticated upload endpoint accepts PNG, JPEG, PDF and plain-text files, up to 5 MiB. It checks file signatures, streams bytes to ClamAV using the INSTREAM protocol, and accepts a file only when the scanner reports CLEAN. An unavailable scanner fails closed with HTTP 503; detected malware is rejected with HTTP 422. Files are scanned in memory and are not persisted.

## Important limitations

This is an intentionally compact test target, not a production commerce platform:
- Product and order data is in-memory and resets on restart.
- Authentication is HTTP Basic with local demo users, not an OIDC identity provider.
- No payment processing or persistent storage is implemented.
- The dashboard is static HTML/CSS/JavaScript, not a full React SPA.
- No LLM assistant is implemented yet, so PyRIT is deferred.
- CSRF is disabled for this stateless API demo; do not reuse this configuration for browser session/cookie authentication without a CSRF design.
- Production profile must supply real authentication and secret management.

## Vulnerable exercise

The lab-vulnerable Spring profile adds an IDOR route. Keep it local and isolated. The same sample order data lets Nuclei and manual tests compare the vulnerable route with the protected API route.
