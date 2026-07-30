&#x20;                                                               **API Specification Document**

&#x20;                                                         **Vendor Inventory Reconciliation Service (VIRS)


1. Project Name**

**Vendor Inventory Reconciliation Service (VIRS)

2. Purpose of the API**

**The Vendor Inventory Reconciliation Service (VIRS) provides REST-based APIs to reconcile inventory data received from external vendors against the enterprise inventory repository. The service enables reconciliation execution, result retrieval, batch re-processing, and operational health monitoring.**

**The service is implemented as a Java Spring Boot microservice and supports both synchronous retrieval and asynchronous processing through messaging integration.

3. API Standards

| Standard                | Specification                                |**

**| ----------------------- | -------------------------------------------- |**

**| Architecture Style      | REST APIs                                    |**

**| Transport Protocol      | HTTPS                                        |**

**| Payload Formats         | JSON, CSV                                    |**

**| API Definition          | OpenAPI/swagger                              |**                                         

**| Stateless Communication | Yes                                          |**

**| Authentication          | Aws secret manager                           |**

**| Messaging Integration   | Amazon SQS / SNS                             |**

**| Backend Database        | PostgreSQL                                   |**



**REST API Design Principles**



* Resource-oriented URI design
* Proper use of HTTP methods
* Stateless request processing
* Standard HTTP status codes
* Consistent error handling
* Version-controlled APIs
* OpenAPI-compliant documentation



**4. Base URL

https://localhost:8080/virs/api/v1

production

https://virs.company.com/api/v1


5. API Versioning Strategy**
* **URI Versioning**
* **The service uses URI-based versioning.**


**Ex:
/api/v1

Future
/api/v2/reconciliation

why URI Versioning:
- Easy to understand
- Easy to test
- Backward compatible
- common enterprise practices

6. Content Negotiation**

**The service supports multiple response formats using the Accept header.

Supported Content Types

| Format | Header Value     |**

**| ------ | ---------------- |**

**| JSON   | application/json |**

**| CSV    | text/csv         |**


**Response:

{**

**"batchId": 1001,**

**"status: SUCCESS"**

**}


csv response:

Accept : text/csv

Ex:

BactchId,status
1001,SUCCESS


Default response format:
application/json

7. Authentication

Local Development

Future**


**AWS secret Manager
OAuth2/jwt


8. Error Response Format:

{**



**"timestamp": "2026-07-28T10:30:15Z",3  "status": 400,4  "error": "Bad Request",5  "message": "Csv file is Empty.",6  "path": "/api/v1/reconciliation/start",**



**}

9. Rest Apis

API 1**




**Start Reconciliation

/api/v1/reconciliation/start

purpose
Starts inventory reconciliation

Request
{
"FileName": vendor\_inventory.csv
}

Success response

202 Accept


{**

**"batchId": 1001,**

**"status": "PROCESSING",
"message": "Reconciliation started"**

**}


Error
-400 Bad request
-500 Internal Server Error


API 2

Get reconciliation Result

GET
/api/v1/reconciliation/{batchId}**



**Resopnse

{**

**"batchId": 1001,**

**"status": "COMPLETED",
"processRecords": 1500,
"matchRecords": 1450,
"mismatchRecords": 50**

**}

Status code

200 ok
404 not found


API 3

Get all Reconcilliation Results

Get 
/api/v1/reconciliation/results

Resopnse

\[{**

**{**

**"batchId": 1001,**

**"status: SUCCESS"**

**},{**

**{**

**"batchId": 1002,**

**"status": "FAILED"**

**}**





**}]


Supports 

Accept : application/json
Accept : text/csv

API 4

Retrigger Batch

Post
/api/v1/reconciliation/retrigger/{batchId}


Purpose
Reprocess an existing batch
This API is idempotent
if batch already processs
(No duplicate execution occurs)


Resopnse

{**

**"batchId": 1001,**

**"status": "REPROCESSING"**

**}


Status: 202 Accepted

API 5

Get Batch STatus

GET : /api/v1/{bactchId}

Resonse
{**

**"batchId": 1001,**

**"status": "COMPLETED",
"startTime": "10.20",
"endTime":"10:30"**

**}**



**API 6

Health check

Get 

/actuator/health

Resopnse
{**



**"status":"UP"**

**}


10. Swagger / OpenAPI Documentation**

**Swagger UI

https://localhost:8080/swagger-ui/index.html

OpenAPI Specification

/v3/api-docs

11. API Flow

Client**

&#x20;  **|**

&#x20;  **v
Post/reconciliation
	|**

**Spring Boot REST Controller**

&#x20;  **|**

&#x20;  **v**

**Service Layer**

&#x20;  **|**

&#x20;  **v
Validation
    |**



**csv parser
    |
Reconciliation engine
        |
	|**

**Repository Layer**

&#x20;  **|**

&#x20;  **v**

**PostgreSQL**

&#x20;  **|**

&#x20;  **v**

**SQS / SNS Messaging**

&#x20;  **|**

&#x20;  **v**

**Response Returned to Client


Reconciliation Workflow
1. Client sends reconciliation request.**

**2. Controller validates incoming request.**

**3. Service initiates reconciliation process.**

**4. Repository persists reconciliation metadata in PostgreSQL.**

**5. Service publishes processing event to SQS/SNS.**

**6. Background processor performs reconciliation.**

**7. Results are stored in PostgreSQL.**

**8. Client retrieves reconciliation results using GET APIs.

12.Request Header

Content-Type: application/json
Accept: application/json


Csv Export

Accept: text/csv**

































































