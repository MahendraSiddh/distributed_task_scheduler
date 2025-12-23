# 🚀 Distributed Task Orchestrator

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green?style=for-the-badge&logo=spring)
![React](https://img.shields.io/badge/React-18.2.0-blue?style=for-the-badge&logo=react)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.0-red?style=for-the-badge&logo=redis)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange?style=for-the-badge&logo=rabbitmq)

**A production-ready distributed task management system with real-time updates, priority-based scheduling, and intelligent load balancing.**

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Demo](#-demo)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

---

## 🎯 Overview

The **Distributed Task Orchestrator** is an enterprise-grade task management system designed to handle high-throughput workloads across distributed worker nodes. It features intelligent task prioritization, fair load distribution, real-time WebSocket notifications, and comprehensive monitoring capabilities.

### Key Highlights

- 🔄 **Real-time Task Assignment** via WebSocket with sub-second latency
- 📊 **Priority-based Queue Management** (P1-P5 scheduling)
- ⚖️ **Fair Load Balancing** across employee nodes
- 🔒 **Distributed Locking** using Redis (Redisson) for idempotency
- 🔁 **Automatic Retry Mechanism** with configurable attempts
- 📈 **Comprehensive Analytics** and performance metrics
- 🎭 **Role-based Access Control** (Admin & Employee)
- 💬 **Task Comments & Audit Trail** for accountability

---

## ✨ Features

### For Administrators
- ✅ Create tasks with descriptions and priority levels (1-5)
- ✅ Monitor all employees and their workload in real-time
- ✅ View task priority queue and assignment status
- ✅ Access comprehensive analytics dashboard
- ✅ Receive instant notifications on task completion/failure
- ✅ Review employee performance metrics

### For Employees
- ✅ Automatic task assignment based on workload
- ✅ Real-time task notifications via WebSocket
- ✅ Progress tracking with live updates
- ✅ Task completion/failure reporting with mandatory comments
- ✅ Personal statistics and performance tracking
- ✅ Task history with detailed audit trail

### System Features
- ✅ **Distributed Locking**: Prevents duplicate task processing
- ✅ **State Machine**: Manages task lifecycle (Pending → Running → Completed/Failed)
- ✅ **Priority Queue**: Ensures critical tasks are processed first
- ✅ **Fair Distribution Algorithm**: Balances workload across employees
- ✅ **Auto-scaling Ready**: Supports dynamic worker pool sizing
- ✅ **Fault Tolerance**: Automatic recovery from worker failures
- ✅ **WebSocket Fallback**: Automatic polling if WebSocket unavailable

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Layer (React)                        │
│  ┌──────────────────┐              ┌──────────────────┐        │
│  │ Admin Dashboard  │              │Employee Dashboard│        │
│  └────────┬─────────┘              └────────┬─────────┘        │
└───────────┼────────────────────────────────┼──────────────────┘
            │                                 │
            └────────────┬────────────────────┘
                         │ REST API + WebSocket
┌────────────────────────▼─────────────────────────────────────────┐
│                  Application Layer (Spring Boot)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │  │  Services    │  │   Scheduled  │          │
│  │              │  │              │  │  Assignment  │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼──────────────────┘
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼──────────────────┐
│                    Infrastructure Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │  MySQL   │  │  Redis   │  │ RabbitMQ │  │WebSocket │        │
│  │  (Data)  │  │  (Lock)  │  │ (Queue)  │  │  (RT)    │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
└──────────────────────────────────────────────────────────────────┘
```

### Task Flow Diagram

```
Admin Creates Task
      ↓
Priority Queue (Sorted by Priority & Time)
      ↓
Auto-Assignment Service (Every 5s)
      ↓
Distributed Lock Acquisition (Redis)
      ↓
Employee Assignment (Fair Distribution)
      ↓
WebSocket Notification to Employee
      ↓
Employee Works on Task (Progress Updates)
      ↓
Complete/Fail with Comments
      ↓
Release Lock → Mark Employee Idle
      ↓
Assign Next Task Automatically
```

---

## 🛠 Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring State Machine** - Task lifecycle management
- **Spring WebSocket** - Real-time communication
- **MySQL 8.0** - Relational database
- **Redis 7.0** - Distributed locking & caching
- **Redisson** - Redis client with distributed primitives
- **RabbitMQ 3.12** - Message queue
- **Lombok** - Boilerplate code reduction
- **Maven** - Dependency management

### Frontend
- **React 18.2.0** - UI framework
- **Vite** - Build tool
- **Lucide React** - Icon library
- **Native WebSocket API** - Real-time updates
- **Fetch API** - HTTP requests

### DevOps
- **Docker & Docker Compose** - Containerization
- **Git** - Version control

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17+**
  ```bash
  java -version
  # Should show: java version "17.x.x" or higher
  ```

- **Apache Maven 3.6+**
  ```bash
  mvn -version
  # Should show: Apache Maven 3.6.x or higher
  ```

- **Node.js 16+ & npm**
  ```bash
  node -v  # Should show: v16.x.x or higher
  npm -v   # Should show: 8.x.x or higher
  ```

- **Docker & Docker Compose**
  ```bash
  docker --version          # Should show: Docker version 20.x.x or higher
  docker-compose --version  # Should show: docker-compose version 1.29.x or higher
  ```

- **Git**
  ```bash
  git --version
  ```

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/distributed-task-orchestrator.git
cd distributed-task-orchestrator
```

### 2. Start Infrastructure Services

```bash
# Start MySQL, Redis, and RabbitMQ using Docker Compose
docker-compose up -d

# Verify services are running
docker-compose ps

# Expected output:
# NAME                    STATUS    PORTS
# task-orchestrator-mysql    Up    0.0.0.0:3306->3306/tcp
# task-orchestrator-redis    Up    0.0.0.0:6379->6379/tcp
# task-orchestrator-rabbitmq Up    0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
```

### 3. Configure Backend

```bash
cd backend

# Update src/main/resources/application.yml with your settings (if needed)
# Default settings should work with Docker Compose
```

### 4. Run Backend

```bash
# Build and run Spring Boot application
mvn clean install
mvn spring-boot:run

# Backend will start on http://localhost:8080
# You should see: "Started TaskOrchestratorApplication in X.XXX seconds"
```

### 5. Configure Frontend

```bash
cd ../frontend

# Install dependencies
npm install
```

### 6. Run Frontend

```bash
# Start React development server
npm run dev

# Frontend will start on http://localhost:3000
```

### 7. Access the Application

- **Frontend**: http://localhost:3000
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Backend API**: http://localhost:8080/api

### 8. Create Test Accounts

**Option A: Via UI**
1. Open http://localhost:3000
2. Click "Register"
3. Create an admin account and employee accounts

**Option B: Via API**
```bash
# Register Admin
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "fullName": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN"
  }'

# Register Employee
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "employee1",
    "password": "emp123",
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "EMPLOYEE"
  }'
```

---

## ⚙️ Configuration

### Backend Configuration (`backend/src/main/resources/application.yml`)

```yaml
spring:
  application:
    name: task-orchestrator
    
  datasource:
    url: jdbc:mysql://localhost:3306/task_orchestrator?createDatabaseIfNotExist=true
    username: orchestrator
    password: orchestrator123
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false  # Set to true for debugging
    
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    
  redis:
    host: localhost
    port: 6379
    
server:
  port: 8080

logging:
  level:
    com.orchestrator: INFO  # Change to DEBUG for detailed logs
```

### Frontend Configuration (`frontend/src/App.jsx`)

```javascript
const API_BASE_URL = 'http://localhost:8080/api';  // Change for production
const WS_URL = 'ws://localhost:8080/ws';            // Change for production
```

### Docker Compose Configuration (`docker-compose.yml`)

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: task-orchestrator-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: task_orchestrator
      MYSQL_USER: orchestrator
      MYSQL_PASSWORD: orchestrator123
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    container_name: task-orchestrator-redis
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: task-orchestrator-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI

volumes:
  mysql-data:
```

---

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login user |
| GET | `/api/auth/user/{userId}` | Get user details |

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/tasks` | Create new task |
| GET | `/api/admin/tasks` | Get all tasks |
| GET | `/api/admin/tasks/statistics` | Get task statistics |
| GET | `/api/admin/employees` | Get all employees |
| POST | `/api/admin/tasks/{taskId}/assign/{employeeId}` | Manually assign task |

### Employee Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employee/tasks/dashboard` | Get dashboard data |
| GET | `/api/employee/tasks/current` | Get current task |
| POST | `/api/employee/tasks/get-next` | Request next task |
| POST | `/api/employee/tasks/{taskId}/complete` | Complete task |
| POST | `/api/employee/tasks/{taskId}/fail` | Mark task as failed |
| POST | `/api/employee/tasks/{taskId}/progress` | Update progress |
| GET | `/api/employee/tasks/history` | Get task history |

### WebSocket Topics

| Topic | Description |
|-------|-------------|
| `/topic/admin/tasks` | Admin broadcasts (all task events) |
| `/topic/admin/statistics` | Statistics updates |
| `/user/{userId}/queue/tasks` | User-specific notifications |

### Example API Requests

**Create Task (Admin)**
```bash
curl -X POST http://localhost:8080/api/admin/tasks \
  -H "Content-Type: application/json" \
  -H "User-Id: 1" \
  -d '{
    "name": "Fix critical bug in payment service",
    "description": "The payment gateway is returning 500 errors",
    "priority": 1
  }'
```

**Complete Task (Employee)**
```bash
curl -X POST http://localhost:8080/api/employee/tasks/task-123/complete \
  -H "Content-Type: application/json" \
  -H "User-Id: 2" \
  -d '{
    "message": "Bug fixed. Deployed patch v1.2.3"
  }'
```

---

## 🧪 Testing

### Run Backend Tests

```bash
cd backend
mvn test
```

### Run Frontend Tests

```bash
cd frontend
npm test
```

### Manual Testing Checklist

- [ ] Admin can create tasks with different priorities
- [ ] Tasks are automatically assigned to idle employees
- [ ] Employees receive real-time notifications
- [ ] Task progress updates in real-time
- [ ] Employees can complete/fail tasks with comments
- [ ] Comments are visible to admin
- [ ] Failed tasks can be retried
- [ ] Worker crashes are handled gracefully
- [ ] WebSocket reconnects automatically
- [ ] Fair distribution algorithm works correctly

### Load Testing

```bash
# Using Apache Bench
ab -n 1000 -c 10 -H "User-Id: 1" http://localhost:8080/api/admin/tasks/statistics
```

---

## 🚢 Deployment

### Production Deployment Checklist

- [ ] Update `application.yml` with production database credentials
- [ ] Set `spring.jpa.hibernate.ddl-auto` to `validate`
- [ ] Configure HTTPS/SSL certificates
- [ ] Set up reverse proxy (Nginx/Apache)
- [ ] Configure CORS for production domain
- [ ] Enable production logging
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure backup strategy for MySQL
- [ ] Set up Redis persistence
- [ ] Configure RabbitMQ clustering
- [ ] Update WebSocket URL in frontend
- [ ] Build optimized frontend: `npm run build`
- [ ] Set up CI/CD pipeline

### Docker Production Build

```bash
# Build backend
cd backend
mvn clean package -DskipTests
docker build -t task-orchestrator-backend .

# Build frontend
cd ../frontend
npm run build
docker build -t task-orchestrator-frontend .

# Run with Docker Compose
docker-compose -f docker-compose.prod.yml up -d
```

---

## 🐛 Troubleshooting

### Common Issues

**Issue: Backend won't start - Connection refused to MySQL**
```bash
# Solution: Ensure Docker containers are running
docker-compose ps
docker-compose logs mysql
```

**Issue: Lombok compilation errors**
```bash
# Solution: Enable annotation processing in IDE
# IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
mvn clean install -U
```

**Issue: WebSocket not connecting**
```bash
# Solution: Check CORS configuration and WebSocket URL
# Verify in browser console: ws://localhost:8080/ws
```

**Issue: Tasks not auto-assigning**
```bash
# Solution: Check if @EnableScheduling is present in main class
# Verify in logs: "Auto-assigned task X to employee Y"
```

**Issue: Frontend won't connect to backend**
```bash
# Solution: Verify API_BASE_URL in frontend code
# Check if backend is running: curl http://localhost:8080/api/auth/user/1
```

### Debug Mode

Enable debug logging:
```yaml
# application.yml
logging:
  level:
    com.orchestrator: DEBUG
    org.springframework.statemachine: DEBUG
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style Guidelines

- Follow Java coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features
- Update documentation as needed

---

## 📞 Contact & Support

- **Email**: mahisiddh36@gmail.com
- **LinkedIn**: [Mahendra Nath](https://www.linkedin.com/in/mahendra-nath/)
- **GitHub Issues**: [Report a bug](https://github.com/MahendraSiddh/distributed-task-orchestrator/issues)

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

Made with ❤️ by Mahendra Nath

</div>
