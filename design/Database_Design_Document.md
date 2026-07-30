&#x20;                                                             **Database Design Document**

&#x20;                                                   **Vendor Inventory Reconciliation Service (VIRS)



1. Document Information

| Attribute         | Value                                          |**

**| ----------------- | ---------------------------------------------- |**

**| \*\*Project Name\*\*  | Vendor Inventory Reconciliation Service (VIRS) |**

**| \*\*Document Name\*\* | Database Design Document                       |**

**| \*\*Version\*\*       | 1.0                                            |**

**| \*\*Author\*\*        | palavi                                         |**

**| \*\*Date\*\*          | 28-Jul-2026                                    |


2. Purpose**



**The purpose of this document is to define the logical database design for the Vendor Inventory Reconciliation Service (VIRS) using PostgreSQL.**

**The database supports the following business and technical objectives:**



* **Storage of vendor inventory records received through CSV files.**
* **Tracking of inventory reconciliation batch executions.**
* **Comparison of vendor inventory against internal inventory records.**
* **Persistence of reconciliation outcomes and audit information.**
* **Generation and tracking of discrepancy notifications.**
* **Support for REST API queries and reporting requirements.**
* **Maintenance of data integrity, consistency, and traceability.**
* **Scalability for processing large inventory files and multiple reconciliation batches.**



**The database design follows normalized relational modeling principles to support maintainability, performance, and future extensibility.

3. Database Overview

| Attribute                 | Value                            |**

**| ------------------------- | -------------------------------- |**

**| \*\*Database Name\*\*         | virs\\\_db                         |**

**| \*\*Database Type\*\*         | PostgreSQL                       |**

**| \*\*Schema\*\*                | public                           |**

**| \*\*Design Pattern\*\*        | Third Normal Form (3NF)          |**

**| \*\*Application Type\*\*      | Java 17 Spring Boot Microservice |**

**| \*\*Persistence Framework\*\* | Spring Data JPA                  |**

**| \*\*Cloud Platform\*\*        | AWS                              |


Design Principles**



* **Third Normal Form (3NF) compliance**
* **Referential integrity enforcement**
* **UUID-based entity identification**
* **Optimized read and write performance**
* **Scalable reconciliation processing**
* **Auditability and data traceability**




**4. Database Design
| Table Name                    | Purpose                                                             |**

**| ----------------------------- | ------------------------------------------------------------------- |**

**| \*\*batch\\\_execution\*\*          | Stores reconciliation batch execution details and processing status |**

**| \*\*vendor\\\_inventory\*\*         | Stores vendor inventory records loaded from CSV files               |**

**| \*\*internal\\\_inventory\*\*       | Stores enterprise inventory records used for comparison             |**

**| \*\*reconciliation\\\_result\*\*    | Stores reconciliation outcomes and mismatch information             |**

**| \*\*discrepancy\\\_notification\*\* | Stores discrepancy notifications generated for downstream systems   |

5. Entity Relationship Overview**



**The VIRS database model is centered around batch-driven inventory reconciliation processing.**

**Relationships**



* **One Batch Execution processes multiple Vendor Inventory records.**
* **One Batch Execution creates multiple Reconciliation Results.**
* **One Internal Inventory record can participate in multiple reconciliation processes.**
* **One Reconciliation Result can generate multiple Discrepancy Notifications.**
* **Reconciliation results maintain traceability between vendor inventory and internal inventory records.**



**ER Relationship Summary


batch\_execution**

&#x20;     **|**

&#x20;     **| 1:N**

&#x20;     **|**

**vendor\_inventory**

&#x20;     **|**

&#x20;     **| N:1**

&#x20;     **|**

**internal\_inventory**

&#x20;     **|**

&#x20;     **| 1:N**

&#x20;     **|**

**reconciliation\_result**

&#x20;     **|**

&#x20;     **| 1:N**

&#x20;     **|**

**discrepancy\_notification


6. Table Design

6.1 Table: batch\_execution**



**Purpose**



**Stores reconciliation batch execution metadata and lifecycle status.**



**Primary Key**



**batch\_id


| Column Name          | Data Type    | Constraint   | Description                    |**

**| -------------------- | ------------ | ------------ | ------------------------------ |**

**| batch\_id             | UUID         | PK, NOT NULL | Unique batch identifier        |**

**| file\_name            | VARCHAR(255) | NOT NULL     | Uploaded CSV filename          |**

**| exceution\_type       | VARCHAR(50)  | NOT NULL     | Scheduled or manual            |**

**| status               | VARCHAR(30)  | NOT NULL     | Processing status              |**

**|start\_time            | TImestamp    | NOT NULL     | processing start time          |**

**| end\_time             | Timestamp    | Null         | processing completion time    |**

**| processed\_records    | INTEGER      | Default      | Successfully processed records |**

**| failed\_records       |Interger      | Default      | number of failed records       |


Primary Key

batch\_id

6.2 Table: vendor\_inventory
Purpose**

**Stores inventory records received from vendor CSV files.**

**Primary Key**

**vendor\_inventory\_id**

**Foreign Keys**



**batch\_id → batch\_execution(batch\_id)

| Column Name          | Data Type     | Constraint       | Description                        |**

**| ---------------------| ------------- | ---------------- | ---------------------------------- |**

**| vendor\_inventory\_id  | UUID          | PK, NOT NULL     | Unique vendor inventory identifier |**

**| batch\_id             | UUID          | FK, NOT NULL     | Associated batch                   |**

**| vendor\_id            | VARCHAR(50)   | NOT NULL         | Vendor identifier                  |**

**| sku                  | VARCHAR(100)  | NOT NULL, UNIQUE | Product SKU                        |**

**| product\_name         | VARCHAR(255)  | NOT NULL         | Product name                       |**

**| quantity             | INTEGER       | NOT NULL         | Vendor inventory quantity          |**

**| unit\_price           | NUMERIC(15,2) | NULL             | product unit price                 |**

**| upload\_time          | TIMESTAMP     | NOT NULL         | upload time stamp                  |  


6.3 Table: internal\_inventory**





**Purpose**





**Stores enterprise inventory records used for reconciliation.**



**Primary Key**



**inventory\_id


| Column Name              | Data Type    | Constraint       | Description                     |**

**| ------------------------ | ------------ | ---------------- | ------------------------------- |**

**| inventory\_id             | UUID         | PK, NOT NULL     | Internal inventory identifier   |**

**| sku                      | VARCHAR(100) | NOT NULL, UNIQUE | Product SKU                     |**

**| quantity                 | INTEGER      | NOT NULL         | Internal stock quantity         |**

**| warehouse                | VARCHAR(100) | NOT NULL         | Inventory location              |**

**| last\_updated             | TIMESTAMP    | NOT NULL         | Last inventory update timestamp |**



**Foreign Keys**

**None

6.4 Table: reconciliation\_result**



**Purpose**



**Stores reconciliation outcomes between vendor and internal inventory.**



**Primary Key**



**reconciliation\_id**



**Foreign Keys**



**batch\_id → batch\_execution(batch\_id)**

**vendor\_inventory\_id → vendor\_inventory(vendor\_inventory\_id)**

**inventory\_id → internal\_inventory(internal\_inventory\_id)

| Column Name                | Data Type   | Constraint   | Description                      |**

**| -------------------------- | ----------- | ------------ | -------------------------------- |**

**| reconciliation\_id          | UUID        | PK, NOT NULL | Reconciliation result identifier |**

**| batch\_id                   | UUID        | FK, NOT NULL | Associated reconciliation batch  |**

**| vendor\_inventory\_id        | UUID        | FK, NOT NULL | Vendor inventory record          |**

**| inventory\_id               | UUID        | FK, NOT NULL | Internal inventory record        |**

**| reconciliation\_status      | VARCHAR(20) | NOT NULL     | MATCHED or MISMATCHED            |**

**| quantity\_diffrence         | INTEGER     | NOT NULL     | Vendor inventory quantity        |**

**| remark                     | Text        |     NULL     | Reconciliation Remark            |**

**| reconciliation\_at          | Timestamp   | NOT NULL     | Reconciliation completion time   |


6.5 Table: discrepancy\_notification**



**Purpose**



**Stores discrepancy notification events published to SQS/SNS.**



**Primary Key**



**notification\_id**



**Foreign Keys**



**reconciliation\_id → reconciliation\_result(reconciliation\_id)**



**| Column Name                | Data Type    | Constraint   | Description                   |**

**| ------------------------- | ------------ | ------------ | ----------------------------- |**

**| notification\_id           | UUID         | PK, NOT NULL | Notification identifier       |**

**| reconciliation\_id         | UUID        | FK, NOT NULL | Related reconciliation result |**

**| notification\_type         | VARCHAR(50)  | NOT NULL     | Notification category         |**

**| notification\_status       | VARCHAR(30)  | NOT NULL     | SENT, FAILED, PENDING         |**

**| sent\_timestamp            | TIMESTAMP    | NOT NULL     | Notification publish time     |**


**7. Relationships

| Parent Table           | Child Table               | Relationship Type |**

**| ----------------------| ------------------------- | ----------------- |**

**| batch\_execution       | vendor\\\_inventory         | One-to-Many       |**

**| batch\_execution       | reconciliation\\\_result    | One-to-Many       |**

**| internal\_inventory    | reconciliation\\\_result    | One-to-Many       |**

**| reconciliation\_result | discrepancy\\\_notification | One-to-Many       |**




**8. Constraints**

**Primary Key Constraints**

**All major entities use UUID-based primary keys:**



**batch\_id**

**vendor\_inventory\_id**

**inventory\_id**

**reconciliation\_result\_id**

**notification\_id


9. Data base workflow

1. vendor uploads a csv file to s3.**

**2.**

**A new record is created in batch\_excecution**

**3. csv data is parse and stored in vendor\_inventory
4. records are compared with internal\_inventory.
5. Results are stored in reconciliation\_result
6. mismatches generate entries in discrepancy\_notification.
7. Users retrieve results using the RESTR APIs.

11. Assumptions**



**The following assumptions apply to this database design:**



* **Vendor inventory files are provided in CSV format.**
* **Each uploaded CSV file corresponds to a single reconciliation batch.**
* **SKU uniquely identifies a product across inventory systems.**
* **PostgreSQL is the authoritative relational database platform.**
* **UUID is used as the primary key for all major business entities.**
* **Internal inventory data is maintained independently of reconciliation processing.**
* **Notifications are generated only for mismatched inventory records.**
* **Batch execution records must be retained for audit and reporting purposes.**
* **Amazon S3 is the source system for vendor inventory files.**
* **Amazon SQS/SNS is used for discrepancy notification distribution.**
* **The application follows Java Spring Boot microservice architecture and Spring Data JPA persistence standards.**











































