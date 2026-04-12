# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Integration tests for HTTP endpoints
- CI/CD pipeline with GitHub Actions
- Docker support with Docker Compose configuration
- Code quality workflows with clj-kondo and coverage reporting
- Release workflow for automated deployment
- Deployment guide with Kubernetes and Docker examples
- Contributing guidelines
- Pull request and issue templates

### Changed
- Updated project structure for production readiness

### Fixed
- HTTP handler route composition with repository and publisher dependencies

## [0.1.0] - 2024-01-01

### Added
- Initial project setup with Clojure 1.11.1
- Hexagonal architecture implementation
- Domain models: Notification and Event
- Business logic layer with notification processing
- HTTP adapter with REST API endpoints
- Datomic adapter for persistent storage
- Kafka adapter for event publishing
- Comprehensive unit tests for all layers
- Configuration management with Aero
- Logging with SLF4J and Logback
- Request/response validation with Prismatic Schema

### Features
- Create notifications via HTTP POST
- Retrieve notifications by ID via HTTP GET
- List all notifications with pagination via HTTP GET
- Health check endpoint
- Error handling with proper HTTP status codes
- Kafka event publishing
- Datomic transaction-based persistence
- Graceful shutdown

### Architecture
- Clean separation of concerns with Ports & Adapters pattern
- Domain-driven design principles
- Type-safe request/response handling
- Dependency injection for testability
- Mock-friendly adapter interfaces

---

## Version History

### 0.1.0-SNAPSHOT
- Development version with core functionality implemented
