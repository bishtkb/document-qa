# Document QA Application

A Spring Boot application for document ingestion and Q&A functionality with authentication and role-based access control.

## Prerequisites

- Java 17 or higher
- PostgreSQL 14 or higher
- Redis 6 or higher
- RabbitMQ 3.8 or higher
- Gradle 7.6 or higher

# How to Run the Document QA Application

1. Clone the Repository
   git clone https://github.com/bishtkb/document-qa
   cd document-qa

2. Install Required Tools
   Ensure the following tools are installed on your system:
     Java 17+
     Gradle 7.6+
     PostgreSQL 14+
     Redis 6+
     RabbitMQ 3.8+

3. Set Up the PostgreSQL Database
  
4. Start Redis Server
   
5. Start RabbitMQ Server

6. Build the Application

7. Run the Application


## Setup Instructions

1. **Database Setup**
   ```bash
   # Create PostgreSQL database
   createdb documentqa
   
   # Configure database connection in application.yml
   # Update the following properties:
   # spring.datasource.url
   # spring.datasource.username
   # spring.datasource.password
   ```

2. **Redis Setup**
   ```bash
   # Install and start Redis
   sudo apt-get install redis-server
   sudo systemctl start redis-server
   
   # Configure Redis connection in application.yml if needed
   ```

3. **RabbitMQ Setup**
   ```bash
   # Install and start RabbitMQ
   sudo apt-get install rabbitmq-server
   sudo systemctl start rabbitmq-server
   
   # Configure RabbitMQ connection in application.yml if needed
   ```

4. **Build the Application**
   ```bash
   # Clone the repository
   git clone <repository-url>
   cd document-qa
   
   # Build the application
   ./gradlew build
   ```

5. **Run the Application**
   ```bash
   # Run the application
   ./gradlew bootRun
   ```

## API Documentation

Once the application is running, you can access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Authentication

1. **Register a new user**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "password123"}'
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:8080/api/auth/authenticate \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "password123"}'
   ```

3. **Use the JWT token**
   ```bash
   # Include the token in the Authorization header for protected endpoints
   curl -X GET http://localhost:8080/api/documents/user \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

## Document Management

1. **Upload a document**
   ```bash
   curl -X POST http://localhost:8080/api/documents/upload \
     -H "Authorization: Bearer <your-jwt-token>" \
     -F "file=@/path/to/document.pdf" \
     -F "title=Document Title" \
     -F "author=Author Name" \
     -F "documentType=PDF"
   ```

2. **Search documents**
   ```bash
   curl -X GET "http://localhost:8080/api/documents/search?keyword=searchterm&page=0&size=10" \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

3. **Get documents by author**
   ```bash
   curl -X GET "http://localhost:8080/api/documents/author/Author%20Name?page=0&size=10" \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── project/
│   │           └── documentqa/
│   │               ├── config/           # Configuration classes
│   │               ├── controller/       # REST controllers
│   │               ├── dto/             # Data Transfer Objects
│   │               ├── model/           # Entity classes
│   │               ├── repository/      # JPA repositories
│   │               ├── security/        # Security related classes
│   │               └── service/         # Business logic
│   └── resources/
│       └── application.yml             # Application configuration
└── test/                              # Test classes
```

## Features

- JWT-based authentication
- Role-based access control (Admin, Editor, Viewer)
- Document upload and storage
- Full-text search capabilities
- Document filtering by metadata
- Pagination and sorting
- Redis caching
- RabbitMQ for asynchronous processing
- Swagger API documentation

## Security Considerations

1. JWT tokens are used for authentication
2. Passwords are encrypted using BCrypt
3. Role-based access control for different operations
4. File upload size limits and type restrictions
5. Input validation and sanitization

## Performance Considerations

1. Redis caching for frequently accessed data
2. Asynchronous processing for document ingestion
3. Pagination for large result sets
4. Full-text search optimization
5. Batch processing for large document uploads

## Troubleshooting

1. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check database credentials in application.yml
   - Ensure database exists and is accessible

2. **Redis Connection Issues**
   - Verify Redis is running
   - Check Redis connection settings in application.yml

3. **RabbitMQ Connection Issues**
   - Verify RabbitMQ is running
   - Check RabbitMQ connection settings in application.yml

4. **File Upload Issues**
   - Check file size limits
   - Verify file type restrictions
   - Ensure upload directory has proper permissions
