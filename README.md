# 💬 Java RMI Chat Application

A modern, high-performance client-server chat application built with **JavaFX**, **Java RMI**, and **Hibernate**. This project features a completely custom-built premium "Glassmorphism" UI with buttery-smooth animations and real-time messaging capabilities.

---

## ✨ Features

*   **Premium UI/UX:** A stunning dark mode aesthetic utilizing glassmorphism (semi-transparent blurred backgrounds), scalable vector graphics (SVGs), and dynamic CSS styling.
*   **Smooth Animations:** Integrated with `AnimateFX` to provide buttery-smooth Framer Motion-style entry animations, UI wobble effects on errors, and custom floating "Toast" notifications for a highly responsive feel.
*   **Real-time Communication:** Built on robust **Java RMI** (Remote Method Invocation) for instant, low-latency, real-time message broadcasting between multiple clients.
*   **Dual Dashboards:** 
    *   **User Dashboard:** Users can join active chats, subscribe/unsubscribe from channels, and update their profile.
    *   **Admin Dashboard:** System administrators can easily create new chat rooms, force start/end chat sessions, and manage connected users.
*   **Persistent Storage:** Uses **Hibernate 6 ORM** and **MySQL** to securely manage and persist user profiles, encrypted passwords, chat metadata, and user subscriptions.
*   **Optimistic UI Updates:** Messages are instantly rendered on the screen the moment you press send, creating a snappy and lag-free chatting experience.

## 🛠️ Technology Stack

*   **Language:** Java 11+
*   **UI Framework:** JavaFX 11 (`javafx-controls`, `javafx-fxml`)
*   **Animations:** AnimateFX 1.2.1
*   **Communication:** Java RMI (Remote Method Invocation)
*   **ORM / Database:** Hibernate ORM 6.4.4 / MySQL 8
*   **Build Tool:** Maven

## 🚀 Getting Started

### Prerequisites
1. **Java Development Kit (JDK) 11** or higher.
2. **Maven** installed on your system.
3. **MySQL Server** running locally.

### Database Setup
1. Open MySQL and create a database named `chat_app`.
   ```sql
   CREATE DATABASE chat_app;
   ```
2. The application uses Hibernate's `update` auto-ddl. When you run the application for the first time, it will automatically generate all the necessary tables (`users`, `chats`, `chat_subscriptions`).
3. If necessary, update the MySQL credentials in `Chat Application/src/main/resources/META-INF/persistence.xml`:
   ```xml
   <property name="hibernate.connection.username" value="root"/>
   <property name="hibernate.connection.password" value="1234"/>
   ```

### Running the Application
1. Clone the repository and navigate to the `Chat Application` directory.
2. Build the project to resolve dependencies:
   ```bash
   mvn clean install
   ```
3. Run the `MainApplication` class.
   *   The RMI Server will automatically bootstrap on port `1099`.
   *   The JavaFX Client will launch immediately after.
4. **First User:** The very first user to register an account in the database will automatically be granted **System Administrator** privileges!

## 📸 Screenshots & Design
The UI completely avoids outdated Swing paradigms or pixelated raster images. All icons are drawn at runtime using native JavaFX `SVGPath` nodes and infused with dynamic `DropShadow` effects to ensure perfect rendering on 4K displays.

*Note: Add your project screenshots here!*

## 📝 License
This project is open-source and available under the MIT License.
