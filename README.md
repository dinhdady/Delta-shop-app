# Delta Shop Application

## Overview
Delta Shop is a modern e‑commerce platform for sports equipment, providing a sleek Angular front‑end and a robust Spring Boot back‑end. It supports user authentication, product catalog, shopping cart, checkout with VNPay integration, order management, promotions, loyalty points, and an admin dashboard for full CRUD operations.

## Tech Stack
- **Front‑end:** Angular 17, TypeScript, SCSS, RxJS, Angular Router
- **Back‑end:** Spring Boot 3, Java 21, Spring Security (JWT), JPA/Hibernate, MySQL, Redis, Flyway
- **Payments:** VNPay integration
- **File Storage:** Cloudinary
- **Build & Deploy:** Maven, Docker, CI/CD (GitHub Actions)

## Key Features
- User registration, email verification, password reset
- Role‑based access (customer, admin, super‑admin)
- Product catalog with categories, brands, variants
- Shopping cart with real‑time price calculation
- Checkout flow with promo codes & loyalty points
- Order tracking and status history
- Admin dashboard for managing users, products, orders, promotions
- RESTful API documented with OpenAPI/Swagger
- Comprehensive unit and integration tests

## Getting Started
```bash
# Clone the repository
git clone <repo-url>

# Backend
cd delta-shop-app
./mvnw spring-boot:run   # runs on http://localhost:8080/api

# Frontend
cd delta-shop-app-ui
npm install
ng serve                 # runs on http://localhost:4200
```

## API Documentation
Visit `http://localhost:8080/api/swagger-ui.html` after starting the backend.

## Contributing
Please read the `CONTRIBUTING.md` for guidelines on how to submit pull requests.

## License
This project is licensed under the MIT License.
