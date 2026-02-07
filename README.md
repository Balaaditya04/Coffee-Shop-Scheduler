# Coffee Shop Barista Dilemma - Solution

A simple, efficient order scheduling system using a Dynamic Priority Queue.

## Project Structure

```
HCL Hackathon/
├── coffee-shop-backend/     # Java Spring Boot API
│   └── src/main/java/com/coffeeshop/scheduler/
│       ├── model/           # Order, Barista classes
│       ├── service/         # SchedulerService (core logic)
│       └── controller/      # REST API endpoints
│
├── coffee-shop-frontend/    # React Dashboard
│   └── src/
│       ├── App.js           # Main component
│       └── index.css        # Styling
│
└── coffee_shop_solution.md  # Algorithm documentation
```

## Quick Start

### Backend (Spring Boot)

```bash
cd coffee-shop-backend
./mvnw spring-boot:run
```
Runs on: `http://localhost:8080`

### Frontend (React)

```bash
cd coffee-shop-frontend
npm install
npm start
```
Runs on: `http://localhost:3000`

## Simple Priority Formula

```
Priority = (WaitTime × 5) + (10 - PrepTime) + EmergencyBoost + SkipBonus
```

| Factor | Rule |
|--------|------|
| Wait Time | `waitMinutes × 5` |
| Prep Time | `10 - prepMinutes` (simpler = higher) |
| Emergency | `+50` if wait > 8 min |
| Skip Bonus | `+10` per skip after 3 |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create new order |
| GET | `/api/orders` | Get queue (sorted by priority) |
| GET | `/api/baristas` | Get all baristas |
| POST | `/api/baristas/{id}/complete` | Complete current order |
| GET | `/api/stats` | Get system statistics |

## Key Features

✅ No customer waits > 10 minutes (emergency boost at 8 min)  
✅ Orders are not split (one barista per order)  
✅ Workload balancing (skip overloaded baristas)  
✅ Fairness tracking (skip count compensation)  
✅ Real-time dashboard with live updates
