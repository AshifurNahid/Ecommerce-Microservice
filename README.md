
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
graph TD

%% Frontend
A[Angular Frontend] -->|HTTP| GW[API Gateway]

%% API Gateway routes
GW -->|/customers| CUSTOMER[Customer Service]
GW -->|/products| PRODUCT[Product Service]
GW -->|/orders| ORDER[Order Service]

%% Customer Service
CUSTOMER -->|MongoDB| MDB_Customer[(MongoDB)]
CUSTOMER --> EUREKA

%% Product Service
PRODUCT -->|Postgres| PSQL_Product[(Postgres)]
PRODUCT --> EUREKA

%% Order Service
ORDER -->|Postgres| PSQL_Order[(Postgres)]
ORDER --> EUREKA
ORDER -->|Sync REST| PAYMENT[Payment Service]
ORDER -->|Async| KAFKA[Kafka Broker]

%% Payment Service
PAYMENT -->|Postgres| PSQL_Payment[(Postgres)]
PAYMENT --> EUREKA
PAYMENT -->|Async| KAFKA

%% Kafka to Notification
KAFKA --> NOTIFICATION[Notification Service]
NOTIFICATION -->|MongoDB| MDB_Notification[(MongoDB)]
NOTIFICATION --> EUREKA

%% Discovery and Config
GW --> EUREKA[Eureka Server]
EUREKA --> CONFIG[Config Server]

%% Distributed Tracing
CUSTOMER -.-> ZIPKIN[(Zipkin)]
PRODUCT -.-> ZIPKIN
ORDER -.-> ZIPKIN
PAYMENT -.-> ZIPKIN
NOTIFICATION -.-> ZIPKIN


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

