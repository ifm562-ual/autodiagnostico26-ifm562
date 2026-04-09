# 🏛️ **Hexagonal Architecture**

This project follows **Hexagonal Architecture** (also known as the **Ports and Adapters** pattern), which promotes a clean separation of concerns and high maintainability by isolating the core business logic from external systems and technologies.

## 🌟 **Key Concepts**

Hexagonal Architecture is built around three main parts:
1.  **Domain (Core)**: The pure business logic, entities, and services. It should have **zero** dependencies on external frameworks or databases.
2.  **Ports (Interfaces)**: These define *how* the outside world can interact with the domain (Driving Ports) and *how* the domain can interact with the outside world (Driven Ports).
3.  **Adapters (Implementation)**: Specific implementations of those ports, such as a **REST Controller** (Driving Adapter) or a **MongoDB Repository** (Driven Adapter).

---

## 🎨 **Visualizing the Architecture**

Below are the architectural diagrams illustrating the **PerrDido** structure.

### **The Architecture**
Now, if you were to explain this architecture to someone, houw would you describe it? I would describe it as the following:

<div align="center">
  <img src="./engineering/hexArch1.svg" alt="Core Architecture" width="800" />
</div>

### **Ports and Adapters Workflow**
This second diagram provides a more detailed look at the communication flow between the different layers of the application.

<div align="center">
  <img src="./engineering/hexArch2.svg" alt="Architecture Detail" width="800" />
</div>

---

## ✅ **Benefits**
*   **Independent of Frameworks**: We can change the web server or the database without touching a single line of business logic.
*   **Highly Testable**: The core logic can be tested in isolation using mocks for the ports.
*   **Maintainable**: Clear boundaries prevent the "big ball of mud" anti-pattern in large codebases.
