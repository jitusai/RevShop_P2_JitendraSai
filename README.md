# RevShop P2 - Full-Stack E-Commerce Application

RevShop is a comprehensive, monolithic e-commerce platform designed for both buyers and sellers. It features a responsive web interface, role-based access control, and a full-featured REST API.

## Core Features

### As a Buyer
- **Registration & Login**: Secure account management with personalized profiles.
- **Product Discovery**: Browse by category, search by keywords, and view detailed product information.
- **Shopping Cart**: Add products, update quantities, and seamless removal.
- **Checkout & Payment**: Secure checkout with simulated payment methods (COD, Credit/Debit Card).
- **Order Tracking**: View order history, status updates, and receive in-app notifications.
- **Engagement**: Rate and review products, and save items as favorites.

### As a Seller
- **Inventory Management**: Add, update, and delete products. Manage stock levels and MRP/Discounted pricing.
- **Order Monitoring**: View all incoming orders for your products with detailed buyer information.
- **Low Stock Alerts**: Automatically receive notifications when inventory hits a predefined threshold.
- **Product Insights**: View customer reviews and ratings for your inventory.

## Technology Stack
- **Backend**: Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA.
- **Frontend**: Thymeleaf, HTML5, CSS3.
- **Database**: Oracle Database.
- **Security**: Role-based access control (RBAC), JWT for REST API security.
- **Architecture**: Monolithic with layered service-oriented design.

## How to Run
1. **Prerequisites**: Ensure you have JDK 21 and Maven installed.
2. **Database Setup**: Configure your Oracle Database connection in `src/main/resources/application.properties`.
3. **Execution**: Run the application using `./mvnw spring-boot:run`.
4. **Access**: Open `http://localhost:8080` in your browser.

## Documentation
- [Entity Relationship Diagram (ERD)](file:///C:/Users/prasanna/.gemini/antigravity/brain/07bdd359-f550-439c-8246-b39090b912a1/ERD.md)
- [Application Architecture](file:///C:/Users/prasanna/.gemini/antigravity/brain/07bdd359-f550-439c-8246-b39090b912a1/Architecture.md)
- [Testing Artifacts](file:///C:/Users/prasanna/.gemini/antigravity/brain/07bdd359-f550-439c-8246-b39090b912a1/Testing_Artifacts.md)

## Development
This project was completed by addressing missing layers (REST), fixing mapper exceptions, and implementing core notification and alert triggers.
