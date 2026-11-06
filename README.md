# CloudForge 🚀
### Lightweight Cloud Infrastructure & Container Orchestration Platform

CloudForge is a container orchestration platform designed to simplify the deployment, intelligent scheduling, monitoring, and autonomous fault recovery of containerized microservices across server clusters.

Built with **Java 21, Spring Boot 4.1.1, Docker Java Client 3.6.0, and React.js**.

---

## ✨ Key Features

- **Real Docker Engine Integration**: Connects directly to the local or remote Docker daemon via Unix sockets (`/var/run/docker.sock` or `~/.docker/run/docker.sock`) or TCP. Pulls images, sets CPU/RAM resource limits, forwards ports, streams logs, and captures container stats.
- **Intelligent Workload Scheduling**:
  - `LEAST_ALLOCATED` (Spread algorithm - balances cluster load based on CPU & Memory headroom)
  - `BIN_PACKING` (Consolidates workloads into fewest nodes to conserve energy)
  - `ROUND_ROBIN` (Even distribution across ready servers)
- **Autonomous Self-Healing & Fault Recovery**: Background watchdog automatically detects crashed containers (exit code 137, SIGKILL, process crashes) and restarts or migrates them according to restart policy.
- **Node Cordon & Evacuation**: Drain computing nodes with zero downtime; CloudForge automatically migrates active containers to alternative healthy nodes.
- **Chaos Engineering Lab**: Trigger simulated container crashes and node outages directly from the UI to witness self-healing in real time.
- **Live React Dashboard**: Single-page developer console with real-time cluster telemetry, SVG resource utilization graphs, live container log streaming, and scale controls.
- **Zero-Dependency Running**: Seamless dual-mode engine! If Docker Desktop is closed, CloudForge operates in high-fidelity simulation mode so tests and presentations never fail. Starting Docker Desktop auto-switches to real Docker containers!
- **Persistent Storage**: Atomic file-backed persistence store (`data/`) surviving server restarts, plus full PostgreSQL 16 schema (`infrastructure/schema.sql`).

---

## 🏗️ Architecture

```
                    ┌────────────────────────────────────────┐
                    │      React.js Single Page Dashboard    │
                    │      (Overview, Fleet, Nodes, Chaos)   │
                    └───────────────────┬────────────────────┘
                                        │ REST / JSON (HTTP)
                                        ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                    CloudForge Spring Boot Control Plane                    │
│                                                                            │
│  ┌───────────────────────┐  ┌──────────────────────┐  ┌─────────────────┐  │
│  │  ApplicationService   │  │     NodeService      │  │ ContainerService│  │
│  └───────────┬───────────┘  └──────────┬───────────┘  └────────┬────────┘  │
│              │                         │                       │           │
│              ▼                         ▼                       ▼           │
│  ┌───────────────────────┐  ┌──────────────────────┐  ┌─────────────────┐  │
│  │  IntelligentScheduler │  │ SelfHealingWatchdog  │  │ MetricsService  │  │
│  │(Least-Alloc, Bin-Pack)│  │ (Crash auto-restart) │  │(3s ring buffer) │  │
│  └───────────┬───────────┘  └──────────┬───────────┘  └────────┬────────┘  │
│              └─────────────────────────┼───────────────────────┘           │
│                                        ▼                                   │
│                        ┌───────────────────────────────┐                   │
│                        │       DockerService Adapter   │                   │
│                        │ (Dual-Mode Real Docker / Sim) │                   │
│                        └───────────────┬───────────────┘                   │
└────────────────────────────────────────┼───────────────────────────────────┘
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
   ┌───────────────────────────┐                   ┌───────────────────────────┐
   │    Real Docker Daemon     │                   │  Simulated Runtime Engine │
   │ (/var/run/docker.sock or  │                   │  (Graceful fallback when  │
   │ ~/.docker/run/docker.sock)│                   │   Docker daemon is down)  │
   └───────────────────────────┘                   └───────────────────────────┘
```

---

## 🚀 Quick Start (1 Command)

### Run CloudForge Control Plane & Web Dashboard
```bash
./scripts/start-all.sh
```
Or directly with Maven:
```bash
cd backend/cloudforge-backend
./mvnw spring-boot:run
```

Open your browser at:
👉 **[http://localhost:8080](http://localhost:8080)**

The React dashboard is embedded directly into Spring Boot. No separate Node.js server is needed!

---

## 🛠️ Frontend Development (Optional)

If you wish to edit the React frontend with Vite's Hot Module Replacement (HMR):
```bash
./scripts/dev-frontend.sh
# Opens Vite on http://localhost:5173 with API proxy to port 8080
```
To compile frontend changes back into Spring Boot:
```bash
cd frontend && npm run build
```

---

## 💥 Running Chaos Engineering Demo

In a separate terminal, test the complete deployment, scale, crash, and self-healing lifecycle via the CLI demo script:
```bash
./scripts/demo-chaos.sh
```

---

## 🧪 Running Unit & Integration Tests

All 12 backend unit and integration tests compile and run in under 2 seconds:
```bash
cd backend/cloudforge-backend
./mvnw test
```

---

## 📋 Technology Stack

| Component | Technology | Version | Purpose |
|---|---|---|---|
| **Runtime** | Java | 21 (LTS) | Control plane core runtime |
| **Framework** | Spring Boot | 4.1.1 | REST APIs, dependency injection, async scheduling |
| **Container Engine** | Docker Java Client | 3.6.0 | Connects Java backend to Docker daemon |
| **HTTP Transport** | Apache HttpClient 5 | 5.4.x | Unix socket / TCP HTTP transport |
| **Frontend UI** | React.js | 18.3 | Interactive dashboard & real-time telemetry |
| **Icons & Style** | Lucide React | 1.16 | Modern dark cloud console styling |
| **Build Tools** | Maven & Vite | 3.9+ / 5.4+ | Backend & frontend build automation |
| **Database** | PostgreSQL | 16 | Relational schema in `infrastructure/` |

---

## 📚 Documentation

- [System Architecture Guide](docs/ARCHITECTURE.md) — Deep-dive into internal modules, state machine, and scheduling scores.
- [Placement Interview Guide](docs/INTERVIEW_GUIDE.md) — Common interview questions, technical trade-offs, and sample answers.
- [PostgreSQL Schema](infrastructure/schema.sql) — Production relational database schema.
