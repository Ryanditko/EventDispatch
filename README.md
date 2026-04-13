# EventDispatch

<p align="center">
  <img src="https://media.beehiiv.com/cdn-cgi/image/fit=scale-down,format=auto,onerror=redirect,quality=80/uploads/publication/logo/3324bc5c-9c57-4860-8d0a-5f63cb57c7b3/ED_New.png" alt="EventDispatch Wallpaper" width="800">
</p>

EventDispatch is an event-driven notification orchestration service built with Clojure, Kafka, and Datomic. It exposes a REST API to create and manage notifications, publishes events to Kafka for asynchronous processing, and persists an immutable delivery history in Datomic for auditing and analytics.

## Overview

EventDispatch handles high-volume event streams by:

- Receiving events from multiple producers
- Distributing them through load balancers
- Processing them asynchronously via Kafka
- Storing an immutable history in Datomic

## Architecture

EventDispatch follows the Hexagonal Architecture (Ports & Adapters) pattern, which promotes decoupling and testability:

```mermaid
graph LR
    subgraph external [External World]
        HTTPClient([HTTP Clients])
        KafkaBrokerExt([Kafka Broker])
        DatomicDB[(Datomic DB)]
    end

    subgraph boundary [Boundary Layer]
        HTTPDiplomat["HTTP Diplomat<br/>(Wire Format)"]
        KafkaDiplomat["Kafka Diplomat<br/>(Wire Format)"]
        HTTPAdapter["HTTP Adapter<br/>(Inbound)"]
        KafkaAdapter["Kafka Adapter<br/>(Outbound)"]
        DatomicAdapter["Datomic Adapter<br/>(Outbound)"]
    end

    subgraph domain [Domain Core]
        Controllers["Controllers<br/>(notification_controller)"]
        Logic["Logic Layer<br/>(notification_logic)"]
        Models["Models<br/>(notification, event)"]
    end

    HTTPClient -->|REST| HTTPDiplomat
    HTTPDiplomat -->|Serialize/Deserialize| HTTPAdapter
    HTTPAdapter -->|Request| Controllers
    Controllers -->|Business Rules| Logic
    Logic -->|Data| Models
    
    Logic -->|Event| KafkaAdapter
    KafkaAdapter -->|Kafka Diplomat| KafkaDiplomat
    KafkaDiplomat -->|Message| KafkaBrokerExt
    
    Logic -->|Persist| DatomicAdapter
    DatomicAdapter -->|Query| DatomicDB
```

### Design Principles

- **Models**: Pure domain entities representing business concepts
- **Logic**: Business rules and orchestration logic
- **Controllers**: Application entry points that coordinate logic
- **Diplomats**: Handle serialization/deserialization of wire formats
- **Adapters**: Implement ports to communicate with external systems
- **Ports**: Protocols that define contracts between domain and adapters

## Getting Started

### Prerequisites

- Java 11 or higher
- Clojure 1.10+
- Kafka 2.8+
- Datomic

### Installation

```bash
git clone https://github.com/Ryanditko/EventDispatch.git
cd EventDispatch
lein deps
```

## Development

```bash
lein run
```

## License

This project is licensed under the MIT License.
