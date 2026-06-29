<![CDATA[# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| Development (main branch) | ✅ |

## Reporting a Vulnerability

If you discover a security vulnerability in Rushd-ul-Ilm, please report it responsibly.

### How to Report
1. **Do NOT** open a public GitHub issue for security vulnerabilities
2. **Email**: Contact the project maintainer directly via GitHub profile
3. **Include**: Description of the vulnerability, steps to reproduce, and potential impact

### Response Timeline
- **Acknowledgement**: Within 48 hours
- **Assessment**: Within 1 week
- **Fix**: Within 2 weeks for critical vulnerabilities

## Security Principles

This project is built with a **privacy-first** design:

- ✅ **Zero telemetry** — no analytics, crash reporters, or tracking
- ✅ **No cloud AI APIs** — all inference runs locally or on self-hosted infrastructure
- ✅ **No data exfiltration** — voice recordings and questions never leave the device/server
- ✅ **No third-party SDKs** that collect user data
- ✅ **Self-hosted infrastructure** — Docker Compose, Ollama, Qdrant are all local

### Sensitive Data Handling
- User voice recordings are processed and immediately discarded
- Question history is stored only in the local Room database on the user's phone
- Server-side processing does not persist user data beyond the request lifecycle
- API keys (NVIDIA_API_KEY, HF_TOKEN) must be stored in environment variables, never in source code
- `local.properties` and `.env` files are excluded from version control

### Network Security
- The app uses `network_security_config.xml` to control HTTP traffic permissions
- Production deployments should use HTTPS for all server communication
- The development configuration allows cleartext HTTP only for `10.0.2.2` (Android emulator host)
]]>
