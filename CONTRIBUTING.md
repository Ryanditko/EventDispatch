# Contributing to EventDispatch

Thank you for your interest in contributing to EventDispatch! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help others learn and grow

## Getting Started

### Prerequisites

- Java 21 or later
- Clojure 1.11.1
- Leiningen (for building and testing)
- Git

### Setup Development Environment

```bash
# Clone the repository
git clone https://github.com/Ryanditko/EventDispatch.git
cd EventDispatch

# Install dependencies
lein deps

# Run tests to verify setup
lein test
```

## Development Workflow

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name main

# Or for bug fixes
git checkout -b fix/your-bug-fix main
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions
- `ci/` - CI/CD updates

### 2. Make Your Changes

Follow these guidelines:

- Keep commits focused and atomic
- Write clear commit messages
- Add tests for new functionality
- Update documentation as needed
- Follow existing code style

### 3. Testing

```bash
# Run all tests
lein test

# Run specific test namespace
lein test event_dispatch.domain.models.notification_test

# Run with coverage
lein cloverage
```

Requirements:
- All new code must have tests
- Existing tests must pass
- Aim for >80% code coverage

### 4. Code Style

- Follow Clojure conventions from the [Clojure Style Guide](https://guide.clojure.style/)
- Use meaningful variable and function names
- Keep functions focused and small
- Add docstrings to public functions
- Use 2-space indentation

Example:

```clojure
(defn create-notification
  "Create a new notification with the given recipient and message.
   
   Args:
     recipient - Email address of the recipient
     message - Notification message content
     type - Notification type (email, sms, push)
   
   Returns:
     A map with :id, :status, :created_at, and :recipient"
  [recipient message type]
  {:id (java.util.UUID/randomUUID)
   :recipient recipient
   :message message
   :type type
   :status "pending"
   :created_at (java.time.Instant/now)})
```

### 5. Documentation

- Update README.md for significant changes
- Add docstrings to functions
- Include examples for complex features
- Update CHANGELOG.md

### 6. Commit Messages

Use clear, descriptive commit messages:

```
feature: Add notification retry mechanism

- Implement exponential backoff strategy
- Add max retry configuration
- Add tests for retry logic

Fixes #42
```

Format:
- Type: `feature`, `fix`, `docs`, `refactor`, `test`, `ci`
- Subject: Clear, concise (50 chars max)
- Body: Explain what and why (wrap at 72 chars)
- Footer: Reference issues with `Fixes #123`

### 7. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a pull request on GitHub with:
- Clear title describing the change
- Description of what changed and why
- Reference to related issues
- Screenshots (if UI-related)

## Pull Request Review

Your PR will be reviewed for:

- **Functionality**: Does it work as intended?
- **Testing**: Are tests adequate and passing?
- **Code Quality**: Does it follow style guidelines?
- **Documentation**: Is it well-documented?
- **Performance**: Any performance implications?

Reviewers may request changes. Please address them promptly.

## Architecture Guidelines

EventDispatch follows Hexagonal Architecture (Ports & Adapters):

```
event_dispatch/
├── domain/          # Business logic (framework-independent)
│   ├── models/      # Data models
│   ├── logic/       # Business logic
│   └── schemas/     # Data validation schemas
├── boundary/        # Interfaces between layers
│   ├── input/       # Request handlers
│   └── output/      # Response formatters
└── adapters/        # External integrations (framework-dependent)
    ├── inbound/     # HTTP, gRPC, CLI
    └── outbound/    # Database, Message Queue, External APIs
```

When adding features:
- Business logic goes in `domain/logic/`
- Data models go in `domain/models/`
- External integrations go in `adapters/`
- Schemas for validation go in `domain/schemas/`

## Testing Guidelines

### Unit Tests

Test a single function in isolation:

```clojure
(deftest test-create-notification
  (testing "creates notification with correct fields"
    (let [notif (create-notification "user@example.com" "Hello" "email")]
      (is (uuid? (:id notif)))
      (is (= "pending" (:status notif))))))
```

### Integration Tests

Test interactions between components:

```clojure
(deftest test-notification-flow
  (testing "complete notification creation and retrieval flow"
    (let [repo (mock-repository)
          publisher (mock-publisher)
          routes (create-routes repo publisher)
          resp (routes (mock/request :post "/notifications" ...))]
      (is (= 201 (:status resp))))))
```

### Test Coverage

```bash
# Generate coverage report
lein cloverage --output coverage

# View HTML report
open coverage/index.html
```

## Reporting Issues

When reporting bugs:

1. **Check existing issues** - Avoid duplicates
2. **Provide details**:
   - Expected behavior
   - Actual behavior
   - Steps to reproduce
   - Environment (OS, Java version, etc.)
3. **Include examples**:
   - Minimal reproducible case
   - Error logs or stack traces
4. **Be specific** about:
   - Which component is affected?
   - When did the issue start?
   - Does it affect all uses cases?

## Performance Considerations

- Minimize heap allocations in hot paths
- Use lazy sequences for large datasets
- Cache expensive computations
- Profile before optimizing (`visualvm`, `flamegraph`)

## Security Considerations

- Validate all user input
- Sanitize data before storing
- Use HTTPS in production
- Keep dependencies updated
- Report security issues privately to maintainers

## Getting Help

- Check existing documentation in README.md
- Look through GitHub issues
- Ask questions in pull request discussions
- Contact maintainers directly for sensitive matters

## Recognition

Contributors will be recognized in:
- Git history (commits)
- GitHub contributors page
- Project README.md (for significant contributions)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to EventDispatch! 🚀
