# Architecture

EventDispatch follows the Hexagonal Architecture (Ports & Adapters) pattern, a design approach that places the business logic at the core and isolates it from external concerns through well-defined interfaces.

## Overview

The hexagonal architecture consists of three main layers:

1. Domain Core - Pure business logic
2. Boundary Layer - External communication interfaces
3. External World - Frameworks, databases, message brokers

## Layer Structure

### Domain Core

The domain layer contains the heart of the application: business logic, models, and orchestration. It has no dependencies on external frameworks or technologies.

#### Models (domain/models/)

Pure data structures representing domain concepts:

- `notification.clj` - Notification entity with state transitions
- `event.clj` - Event entity for tracking state changes

Models contain only data and pure functions. They are completely independent of how data is persisted or transmitted.

#### Logic (domain/logic/)

Business logic orchestration layer that combines models with external operations:

- `notification_logic.clj` - Coordinates notification creation, retrieval, and listing

The logic layer receives repository and publisher implementations as dependencies, allowing it to remain technology-agnostic.

#### Controllers (domain/controllers/)

Application entry points that coordinate requests and responses:

- `notification_controller.clj` - Handles incoming requests and delegates to logic layer

Controllers act as the bridge between the boundary layer and domain logic.

### Boundary Layer

The boundary layer handles all communication with the external world. It translates between external formats and domain concepts.

#### Wire Schemas (boundary/wire/schemas/)

Data schemas define the structure of messages at system boundaries:

- `notification_schema.clj` - Schemas for notification requests and responses
- `event_schema.clj` - Schemas for event messages
- `error_schema.clj` - Error response schemas

Schemas serve as contracts between the system and external consumers.

#### Adapters (boundary/adapters/)

Adapter implementations connect the domain to external systems.

##### Inbound Adapters (boundary/adapters/inbound/)

Handle incoming requests from the outside world:

- `http_handler.clj` - REST API endpoint handler

Inbound adapters translate HTTP requests into domain operations and return domain data as HTTP responses.

##### Outbound Adapters (boundary/adapters/outbound/)

Handle communication with external systems:

- `datomic_repository.clj` - Implements data persistence interface
- `kafka_publisher.clj` - Implements event publishing interface

Outbound adapters implement the behavior expected by the domain logic, translating domain operations into technology-specific calls.

### Configuration

- `config/loader.clj` - Loads and provides access to application configuration
- `resources/config.edn` - Environment-specific configuration

Configuration is centralized and loaded at startup, supporting environment variable overrides.

## Data Flow

### Incoming Request

1. HTTP Client sends REST request
2. HTTP Adapter receives and deserializes the request
3. Controller routes to appropriate logic
4. Logic layer creates domain object and coordinates operations
5. Repository adapter persists the notification
6. Publisher adapter sends event to Kafka
7. HTTP Adapter serializes response
8. Client receives HTTP response

### Outgoing Events

1. Domain logic creates event
2. Publisher adapter serializes event
3. Kafka receives message
4. Message persisted in message broker
5. External consumers read from Kafka

## Dependency Direction

All dependencies point inward toward the domain core:

- External systems depend on Adapters
- Adapters depend on domain Logic and Models
- Logic depends on Models
- Models depend on nothing

This ensures that business logic remains isolated and testable.

## Benefits

### Testability

Domain logic can be tested independently by providing mock implementations of repository and publisher functions.

### Technology Independence

The domain logic is independent of HTTP frameworks, databases, or message brokers. Any of these can be replaced without affecting business logic.

### Maintainability

Clear separation of concerns makes the codebase easier to understand and modify. Business rules are centralized in the domain layer.

### Scalability

Adapters can be scaled independently. For example, multiple Kafka consumer instances can be added without modifying domain logic.

## Adding New Features

To add a new feature following this architecture:

1. Define domain models in `domain/models/`
2. Implement business logic in `domain/logic/`
3. Create controllers in `domain/controllers/`
4. Define wire schemas in `boundary/wire/schemas/`
5. Implement inbound adapters for new endpoints
6. Implement outbound adapters for new external systems

This structure ensures consistency and maintainability across the codebase.
