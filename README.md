# 🧭 PortableMama

PortableMama is an **AI-powered  safety and advisory platform** built with **Spring Boot**. It aggregates **real-time open data** (ski, weather, traffic) and enhances it using **LLM-based analysis** to provide smart, contextual recommendations for users.

---

## 🚀 Features

* 🌨️ **Ski Advisory**: Snow conditions, slope data, safety warnings, and AI-generated recommendations
* 🌦️ **Weather Risk Analysis**: Real-time weather data with alerts and outfit/travel advice
* 🚦 **Traffic Monitoring**: Critical road events detection and categorization
* 🧠 **AI Integration**: Uses OpenAI (via LangChain4j) for intelligent analysis
* 🧩 **Modular Architecture**: Easily extensible AI components

---

## 🏗️ Architecture Overview

PortableMama follows a **component-based AI architecture**:

* Each domain (ski, weather, traffic) is implemented as an independent **AI Component**
* A **generic AI controller** dynamically routes requests
* A shared **LLM service layer** handles all AI interactions

```
Client
  │
  ▼
Controller (/api/ai)
  │
  ├── SkiService
  ├── WeatherService
  ├── TrafficService
  │
  ▼
OpenAIService (LLM)
```


## 🧩 Components

* **SkiService / SkiController** – Ski area data + AI ski advice
* **WeatherService / WeatherController** – Weather risks and recommendations
* **TrafficService** – Critical traffic event analysis
* **AdviceService** – Combines multiple components into one AI response
* **Controller** – Generic AI dispatcher

---

## 🌐 External APIs Used

* **OpenDataHub Tourism API**

  * Ski Areas
  * Weather Realtime
  * Weather Measuring Points
  * Traffic Announcements

* **OpenAI API**

---
---

## ▶️ Running the Project

### Prerequisites

* Java 17+
* Maven
* Internet connection (for APIs)

### Run

```bash
mvn spring-boot:run
```

---

## 🔗 Example Endpoints

### Generic AI Endpoint

```http
GET /api/ai?component=weather&latitude=46.5&longitude=11.3
```

### Ski AI Endpoint

```http
GET /api/ski/ai?latitude=46.5&longitude=11.3
```

### Weather AI Endpoint

```http
GET /api/weather/ai?latitude=46.5&longitude=11.3
```

---

## 🎓 Academic Context

This project was developed as part of an **academic software engineering / AI systems course**, focusing on:

* Requirements engineering
* AI-assisted decision systems
* Modular backend architecture
* Responsible use of LLMs

---

## 👩‍💻 Authors

* **Meriem Bchini**

---

## 📜 License

This project is for **educational purposes**.

---

✨ *PortableMama – Smart guidance for safer journeys* ✨
