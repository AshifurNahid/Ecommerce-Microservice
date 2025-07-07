
# Ecommerce Microservices Architecture

A distributed, event-driven ecommerce platform built with Spring Boot and Spring Cloud, following best practices in microservice architecture. This project demonstrates how to design, implement, and orchestrate scalable microservices with asynchronous and synchronous communication, centralized configuration, service discovery, distributed tracing, and security.

## Features

✅ Microservice-based architecture  
✅ Spring Boot and Spring Cloud stack  
✅ API Gateway for routing and cross-cutting concerns  
✅ Eureka Discovery Server for service registration and discovery  
✅ Spring Cloud Config Server for centralized configuration management  
✅ Asynchronous communication with Apache Kafka  
✅ Synchronous communication using OpenFeign and RestTemplate  
✅ Distributed tracing with Zipkin and Spring Actuator  
✅ Secured with Keycloak (OAuth2 / OpenID Connect)  
✅ Docker and Docker Compose for containerized deployment  
✅ Clean, modular code structure for easy extensibility

## Architecture

## 🗺️ Architecture Diagram 

```mermaid
flowchart TD
    %% External Client
    Client[Client] --> Gateway[API Gateway]
    
    %% Private Network Services
    Gateway -->|/customers| Customer[Customer Service]
    Gateway -->|/products| Product[Product Service] 
    Gateway -->|/orders| Order[Order Service]
    
    %% Databases for each service
    Customer --> CustomerDB[(MongoDB)]
    Product --> ProductDB[(Database)]
    Order --> OrderDB[(Database)]
    
    %% Payment flow
    Order -->|Create Payment| Payment[Payment Service]
    Payment --> PaymentDB[(Database)]
    
    %% Async messaging through Kafka
    Payment -->|Send Payment Confirmation| Kafka[Kafka Message Broker]
    Kafka -->|Payment Confirmed| Notification[Notification Service]
    Kafka -->|Send Order Confirmation| Order
    
    %% Notification database
    Notification --> NotificationDB[(MongoDB)]
    
    %% External tracing
    Notification --> Zipkin[Zipkin Distributed Tracing]
    
    %% Infrastructure services (bottom)
    Eureka[Eureka Server]
    Config[Config Server]
    
    %% Network boundaries
    subgraph "Public Network"
        Client
    end
    
    subgraph "Private Network"
        Gateway
        Customer
        Product
        Order
        Payment
        Notification
        CustomerDB
        ProductDB
        OrderDB
        PaymentDB
        NotificationDB
        Kafka
    end
    
    %% Styling
    classDef service fill:#90EE90,stroke:#006400,stroke-width:2px,color:#000
    classDef database fill:#4682B4,stroke:#191970,stroke-width:2px,color:#fff
    classDef infra fill:#FFD700,stroke:#FF8C00,stroke-width:2px,color:#000
    classDef gateway fill:#FF6347,stroke:#8B0000,stroke-width:2px,color:#fff
    classDef client fill:#FF69B4,stroke:#8B008B,stroke-width:2px,color:#fff
    
    class Customer,Product,Order,Payment,Notification service
    class CustomerDB,ProductDB,OrderDB,PaymentDB,NotificationDB database
    class Eureka,Config,Zipkin,Kafka infra
    class Gateway gateway
    class Client client

```

This will spin up:

* Eureka Discovery Server
* Spring Cloud Config Server
* Kafka broker
* Zipkin
* Keycloak
* All microservices behind the API Gateway

Each service will register automatically with Eureka.


### Build and Run Individually

If you wish to run a service manually:

```bash
cd order-service
mvn clean install
mvn spring-boot:run
```

## Security

Keycloak is configured as an identity provider.
The API Gateway validates tokens before forwarding requests to downstream services.

## Distributed Tracing

Zipkin collects trace data from all microservices for easier debugging of distributed flows.

## Contributing

Pull requests are welcome! Please open an issue first to discuss what you would like to change.


## Author

* Ashifur Nahid
* [LinkedIn](https://www.linkedin.com/in/ashifurnahid/)

