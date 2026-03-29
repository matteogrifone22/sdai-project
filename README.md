# Emergency Department Multi-Agent System (ED-MAS)

**ED-MAS** is a Multi-Agent System (MAS) designed to simulate the complex dynamics of a hospital Emergency Department. Developed using the **JADE** framework and **JavaFX**, the system models the entire patient lifecycle—from arrival (via ambulance or walk-in) to triage, medical examination, and potential hospitalization or discharge.

The project highlights the use of **Shared Artifacts** and advanced concurrency control to manage common resources in a distributed environment.


## Key Features

* **Autonomous Agent Logic:** Implementation of specialized agents (Patient, Doctor, Nurse, Triage, and Ambulance) with distinct FIPA-compliant behaviors.
* **Hybrid Architecture:** Transition from a pure agent-based model to an **Agent-Artifact** model for robust resource management.
* **Thread-Safe Operations:** Use of `java.util.concurrent.locks.ReentrantReadWriteLock` to prevent race conditions in the Priority Queue and Bed Management.
* **Real-Time Dashboard:** A comprehensive JavaFX UI featuring:
    * Live Event Logs and manual patient spawning.
    * Queue status and Doctor availability tracking.
    * Dynamic charts for Triage distribution and performance metrics (Discharge Times).
    * Visual monitoring of Bed occupancy and Ambulance fleet status.

## Technical Stack

* **Language:** Java 21
* **Build Tool:** Apache Maven
* **Middleware:** JADE (Java Agent DEvelopment Framework) v4.5.0
* **GUI:** JavaFX v21.0.2
---

## Prerequisites

Before running the simulation, ensure you have the following installed:
1.  **Java Development Kit (JDK) 21** or higher.
2.  **Apache Maven**.
3.  **JADE Library:** The project expects the `jade.jar` file to be located in the `/lib` directory of the project root (as specified in the `pom.xml`).

---

## Build and Installation

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/YourUsername/sdai-project.git](https://github.com/YourUsername/sdai-project.git)
    cd sdai-project
    ```

2.  **Verify Local Dependencies:**
    Ensure the `lib/` folder contains the necessary `jade.jar` file.

3.  **Compile and Package:**
    Use Maven to resolve dependencies and create the executable "fat-jar":
    ```bash
    mvn clean package
    ```
    This will generate a file named `sdai-project.jar` inside the `target/` directory.

---

## Running the Simulation

### Option 1: Using Maven (Recommended)
Launch the simulation directly using the executive plugin:
```bash
mvn clean compile exec:java
```

### Option 2: Running the JAR File
Launch the standalone executable:
```bash
java -jar target/sdai-project.jar
```

---

## Project Structure

```text
.
├── lib/                    # Local JADE dependencies
├── src/
│   └── main/
│       └── java/
│           └── it/unige/dibris/mas/
│               ├── agents/      # Agent logic (Doctor, Patient, etc.)
│               ├── behaviours/  # JADE Behaviours (Cyclic, Ticker, etc.)
│               ├── gui/         # JavaFX Controllers and Views
│               ├── ontology/    # Data structures (TriageColor, BedInfo)
│               └── Main.java    # Entry point
├── pom.xml                 # Maven configuration
└── README.md               # Project documentation
```

## Metrics
* **Total Lines of Code:** 2883
* **Architecture:** Multi-Agent System with Shared Artifacts.
* **Concurrency Model:** Reentrant Read-Write Locking (Readers-Writer policy).
