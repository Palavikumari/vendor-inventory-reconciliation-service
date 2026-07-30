Project Overview



1\. Project Title

Vendor Inventory Reconciliation Service (VIRS)





2\. Project Description



The Vendor Inventory Reconciliation Service (VIRS) is a backend microservice developed using Java 17 and Spring Boot. The primary objective of this service is to automate the reconciliation of vendor inventory data received through CSV files against the organization's internal inventory records stored in a PostgreSQL database.



Vendor inventory files are uploaded to an Amazon S3 bucket, where the application retrieves, validates, and parses the data. The service compares vendor inventory with internal inventory records to identify discrepancies such as missing products, quantity mismatches, or invalid records. The reconciliation results are stored in the database, and notifications are published through Amazon SQS or Amazon SNS for downstream systems.



The application exposes secure, versioned REST APIs to query reconciliation results and manually re-trigger reconciliation batches. API documentation is generated using Swagger/OpenAPI, and sensitive configuration such as database credentials and API keys are managed securely through AWS Secrets Manager.



The solution follows a layered microservices architecture, emphasizing scalability, maintainability, security, and clean separation of concerns.





3\. Project Objective



The objective of this project is to design and implement a scalable and reliable inventory reconciliation service that:



* Reads vendor inventory CSV files from Amazon S3.
* Validates and parses uploaded inventory data.
* Reconciles vendor inventory against internal inventory records.
* Stores reconciliation results in PostgreSQL.
* Publishes discrepancy notifications to Amazon SQS or SNS.
* Retrieves secrets securely using AWS Secrets Manager.
* Exposes versioned REST APIs with Swagger/OpenAPI documentation.
* Supports both JSON and CSV responses through Content Negotiation.
* Provides an idempotent API to manually re-trigger reconciliation batches.





4\. Business Problem



Manual reconciliation of inventory data is time-consuming, error-prone, and difficult to scale when multiple vendors provide inventory exports regularly. Differences in inventory quantities, missing products, duplicate records, and inconsistent file formats can lead to inaccurate stock information and operational delays.



The Vendor Inventory Reconciliation Service automates this process by validating vendor files, comparing them with internal inventory records, and generating discrepancy reports with notification support, thereby improving operational efficiency and data accuracy.



5\. Scope



In Scope



* Vendor inventory CSV file processing
* Amazon S3 integration
* CSV validation and parsing
* Inventory reconciliation
* PostgreSQL data persistence
* REST API development
* Swagger/OpenAPI documentation
* API Versioning
* Content Negotiation (JSON and CSV)
* AWS Secrets Manager integration
* Amazon SQS/SNS notification publishing
* Git-based source control
* Python script for historical data backfill



OUT OF SCOPE:



* Frontend User Interface
* Vendor Authentication Portal
* Inventory Forecasting
* Real-time Inventory Synchronization
* Data Analytics Dashboard





6\. Functional Requirements



The application shall:



* Retrieve inventory files from Amazon S3.
* Validate CSV file structure and contents.
* Parse inventory records.
* Compare vendor inventory with internal inventory.
* Store reconciliation results.
* Publish discrepancy notifications.
* Expose REST APIs for reconciliation operations.
* Support manual reprocessing of batches.
* Generate OpenAPI documentation.
* Maintain API versioning.
* Support JSON and CSV response formats.



7\. Non-Functional Requirements



* High Availability
* Scalability
* Maintainability
* Secure credential management
* Fault tolerance
* Performance optimization
* Proper exception handling
* Centralized logging
* Unit testing with minimum 80% code coverage
* REST API standards compliance



8\. Technology Stack





&#x20;    Component                 Technology



* Programming Language       Java 17
* Framework                  Spring Boot
* Build Tool                 Maven
* Database                   PostgreSQL
* ORM                        Spring Data JPA
* Database Migration         Flyway
* File Storage               Amazon S3
* Messaging                  Amazon SQS / SNS
* Secret Management          AWS Secrets Manager
* API Documentation          Swagger / OpenAPI
* Testing                    JUnit 5, Mockito
* Version Control            Git \& GitHub
* IDE                        IntelliJ IDEA Ultimate
* Scripting                  Python



9\. High-Level Workflow



* Vendor uploads inventory CSV files to Amazon S3.
* Spring Boot application reads the uploaded files.
* CSV files are validated and parsed.
* Vendor inventory is compared with internal inventory records.
* Reconciliation results are stored in PostgreSQL.
* Inventory discrepancies are published to Amazon SQS/SNS.
* Clients query reconciliation results using versioned REST APIs.
* Swagger/OpenAPI provides interactive API documentation.



10\. Assumptions



* Vendor files are provided in CSV format.
* Inventory records contain unique product SKUs.
* PostgreSQL stores the internal inventory data.
* AWS services are logically integrated (or mocked during local development if AWS access is unavailable).
* GitHub is used for version control.



11\. Expected Deliverables



* High Level Design (HLD)
* Low Level Design (LLD)
* Architecture Decision Record (ADR)
* High-Level Architecture Diagram
* ER Diagram
* Database Design Document
* OpenAPI/API Specification
* Spring Boot Microservice
* Python Backfill Script
* Unit Test Report
* Git Repository
* AI Usage Report

