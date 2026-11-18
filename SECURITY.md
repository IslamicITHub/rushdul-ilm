# Security Policy

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

The app currently has no analytics or third-party crash reporting identified in its dependencies. The backend has both local and cloud inference paths:

- When `NVIDIA_API_KEY` is configured, the backend sends the user's question and retrieved source context to NVIDIA NIM (`openai/gpt-oss-20b`).
- When the key is absent, the backend selects local Ollama (`qwen3:4b`).
- A failed NIM request does not automatically retry with Ollama.
- Audio sent to backend STT leaves the phone and is processed by the configured backend service.
- Saved answer history is stored in the Android Room database; backend request persistence was not independently runtime-verified during the documentation audit.

### Sensitive Data Handling
- User voice recordings are sent to the configured backend STT service when using online transcription; retention/deletion behavior should be verified before deployment
- Question history is stored only in the local Room database on the user's phone
- Do not claim server-side non-persistence without verifying the deployed service and its logs/storage
- API keys (NVIDIA_API_KEY, HF_TOKEN) must be stored in environment variables, never in source code
- `local.properties` and `.env` files are excluded from version control

### Network Security
- The app uses `network_security_config.xml` to control HTTP traffic permissions
- Production deployments should use HTTPS for all server communication
- The development configuration permits cleartext HTTP for configured development endpoints; production deployments should use HTTPS and review `network_security_config.xml`

