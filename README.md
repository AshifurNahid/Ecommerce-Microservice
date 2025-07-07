
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

## 🗺️ Architecture Diagram (Mermaid)

```mermaid
flowchart LR
    subgraph Public_Network
        ANGULAR[Angular Frontend]
    end

    subgraph Private_Network
        GW[API Gateway]

        CUSTOMER[Customer Service]
        PRODUCT[Product Service]
        ORDER[Order Service]
        PAYMENT[Payment Service]
        NOTIFICATION[Notification Service]
        KAFKA[Kafka Message Broker]
        ZIPKIN[Zipkin]
        EUREKA[Eureka Server]
        CONFIG[Config Server]
        
        MONGO_CUST[(MongoDB)]
        MONGO_NOTI[(MongoDB)]
        POSTGRES_PROD[(Postgres)]
        POSTGRES_ORD[(Postgres)]
        POSTGRES_PAY[(Postgres)]
    end

    %% public network to gateway
    ANGULAR -- HTTP --> GW

    %% API GW routes
    GW -- /customers --> CUSTOMER
    GW -- /products --> PRODUCT
    GW -- /orders --> ORDER

    %% Customer
    CUSTOMER -- stores --> MONGO_CUST
    CUSTOMER -.-> EUREKA

    %% Product
    PRODUCT -- stores --> POSTGRES_PROD
    PRODUCT -.-> EUREKA

    %% Order
    ORDER -- stores --> POSTGRES_ORD
    ORDER -.-> EUREKA
    ORDER -- Sync REST --> PAYMENT
    ORDER -- Async Order Confirm --> KAFKA

    %% Payment
    PAYMENT -- stores --> POSTGRES_PAY
    PAYMENT -.-> EUREKA
    PAYMENT -- Async Payment Confirm --> KAFKA

    %% Kafka to Notification
    KAFKA -- events --> NOTIFICATION
    NOTIFICATION -- stores --> MONGO_NOTI
    NOTIFICATION -.-> EUREKA

    %% Config + Eureka
    GW -.-> EUREKA
    EUREKA -- config --> CONFIG

    %% Distributed tracing
    CUSTOMER -.-> ZIPKIN
    PRODUCT -.-> ZIPKIN
    ORDER -.-> ZIPKIN
    PAYMENT -.-> ZIPKIN
    NOTIFICATION -.-> ZIPKIN

    %% Style to match dashed/dotted lines in PNG
    classDef dashed stroke-dasharray: 5 5
    GW -.-> EUREKA:::dashed
    CUSTOMER -.-> EUREKA:::dashed
    PRODUCT -.-> EUREKA:::dashed
    ORDER -.-> EUREKA:::dashed
    PAYMENT -.-> EUREKA:::dashed
    NOTIFICATION -.-> EUREKA:::dashed
    EUREKA -- config --> CONFIG:::dashed



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

## License

This project is licensed under the MIT License.

## Author

* Ashifur Nahid
* [LinkedIn](https://www.linkedin.com/in/ashifurnahid/)

