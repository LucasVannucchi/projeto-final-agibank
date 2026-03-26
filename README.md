# AgiEmprestV2 — Plataforma de Processamento de Empréstimos

> **Monorepo · Java 21 · Spring Boot 3 · Kafka · Redis · MongoDB · Camunda BPM 7**

---

## Sumário

1. [Visão Geral da Arquitetura](#arquitetura)
2. [Estrutura do Monorepo](#estrutura)
3. [Fluxo Completo](#fluxo)
4. [Pré-Requisitos](#pre-requisitos)
5. [Como Subir o Ambiente](#como-subir)
6. [Como Testar](#como-testar)
7. [Referência das APIs](#apis)
8. [Tópicos Kafka](#kafka)
9. [Camunda Cockpit](#camunda)
10. [Variáveis de Ambiente](#variaveis)

---

## Arquitetura

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  POST /loans                                                                 │
│  (Cliente)                                                                   │
└────────────────────────┬─────────────────────────────────────────────────────┘
                         │
                         ▼
            ┌────────────────────────┐
            │     loan-service       │  :8080
            │  Spring Boot + Redis   │
            └───────┬────────────────┘
                    │ Cache miss?
                    │ GET /analysis/{userId}
                    ▼
         ┌──────────────────────────┐
         │  credit-analysis-service │  :8081
         │  (score + limit)         │
         └──────────────────────────┘
                    │ Publica evento
                    ▼
            ┌───────────────┐
            │  Kafka topic  │  loan-requested
            └───────┬───────┘
                    │ Consome
                    ▼
         ┌──────────────────────────────────────┐
         │         contract-service             │  :8082
         │  Camunda BPM + MongoDB + Kafka       │
         │                                      │
         │  ┌─────────────────────────────────┐ │
         │  │  BPMN: loan-approval-process    │ │
         │  │                                 │ │
         │  │  Check Contract ──► Score GW    │ │
         │  │       │               │         │ │
         │  │  Reject (dup)   [≥400] [<400]   │ │
         │  │                   │      │      │ │
         │  │             Auto-Aprv  Manager  │ │
         │  │                   │    UserTask │ │
         │  │             Create Contract     │ │
         │  │                   │             │ │
         │  │          loan-approved/-rejected│ │
         │  └─────────────────────────────────┘ │
         └──────────────┬───────────────────────┘
                        │  Publica eventos
              ┌─────────┴──────────┐
              ▼                    ▼
      loan-approved          loan-rejected
              │                    │
              └─────────┬──────────┘
                        ▼
         ┌──────────────────────────────┐
         │     notification-service     │  :8083
         │  SSE (Server-Sent Events)    │
         └──────────────────────────────┘
                        │
                        ▼
              GET /notifications/subscribe/{userId}
              (Cliente recebe push em tempo real)
```

---

## Estrutura

```
agiEmprestV2/
├── loan-service/                # Recebe solicitação, valida limite, publica evento
│   └── src/main/java/.../
│       ├── controller/LoanController.java
│       ├── service/LoanService.java
│       ├── client/CreditAnalysisClient.java
│       ├── dto/
│       ├── event/LoanRequestedEvent.java
│       ├── config/
│       └── exception/
│
├── credit-analysis-service/     # Retorna score e limite de crédito
│   └── src/main/java/.../
│       ├── controller/CreditAnalysisController.java
│       ├── service/CreditAnalysisService.java
│       └── dto/CreditAnalysisResponse.java
│
├── contract-service/            # Orquestra BPMN, persiste contratos, publica resultado
│   └── src/main/java/.../
│       ├── Application.java
│       ├── delegate/            # Java Delegates do Camunda
│       │   ├── CheckExistingContractDelegate.java
│       │   ├── CheckScoreDelegate.java
│       │   ├── CreateContractDelegate.java
│       │   ├── PublishApprovedDelegate.java
│       │   └── PublishRejectedDelegate.java
│       ├── kafka/
│       │   ├── LoanRequestedConsumer.java
│       │   └── LoanEventProducer.java
│       ├── model/Contract.java
│       ├── repository/ContractRepository.java
│       ├── service/ContractService.java
│       ├── controller/ContractController.java
│       └── event/
│   └── src/main/resources/
│       └── loan-approval.bpmn   # Processo BPMN completo
│
├── notification-service/        # Consome aprovações/rejeições → SSE
│   └── src/main/java/.../
│       ├── controller/NotificationController.java
│       ├── listener/LoanEventListener.java
│       ├── service/NotificationService.java
│       └── event/
│
└── docker-compose/
    └── docker-compose.yml
```

---

## Fluxo

```
1. POST /loans  →  loan-service
2. loan-service consulta Redis: user:{userId}:limit
3. Cache miss?  →  GET /analysis/{userId}  →  credit-analysis-service
4. Salva score+limit no Redis (TTL 30 min)
5. Valida amount ≤ limit  (→ 422 se inválido)
6. Publica evento `loan-requested` no Kafka  →  HTTP 202
7. contract-service consome `loan-requested`
8. Inicia processo BPMN `loan-approval-process`
   a. CheckExistingContractDelegate  → contractExists = true/false
   b. ContractExistsGW
      - true  → PublishRejectedDelegate → `loan-rejected`
      - false → CheckScoreDelegate  (scoreCategory)
   c. ScoreGW (score ≥ 400?)
      - ≥ 400 → CreateContractDelegate → MongoDB → PublishApprovedDelegate
      - < 400 → UserTask "Manager Decision" (Camunda Tasklist/Cockpit)
         - approved=true  → CreateContractDelegate → PublishApprovedDelegate
         - approved=false → PublishRejectedDelegate
9. notification-service consome `loan-approved` ou `loan-rejected`
10. Envia push via SSE ao cliente conectado em /notifications/subscribe/{userId}
```

---

## Pré-Requisitos

- **Docker** + **Docker Compose** ≥ v2
- (Opcional) Java 21 + Gradle/Maven para rodar localmente

---

## Como Subir

```bash
docker compose up --build
```

Aguarde todos os health checks passarem (~60 s).

### Verificar serviços

| Serviço              | URL                                |
|----------------------|------------------------------------|
| loan-service         | http://localhost:8080              |
| credit-analysis      | http://localhost:8081              |
| contract-service     | http://localhost:8082              |
| notification-service | http://localhost:8083              |
| Camunda Cockpit      | http://localhost:8082/camunda      |
| Kafka UI             | http://localhost:9000              |

> **Camunda login:** `demo` / `demo`

---

## Como Testar

### 1. Assinar notificações SSE (abrir em terminal separado)

```bash
curl -N http://localhost:8083/notifications/subscribe/123
```

### 2. Solicitar um empréstimo (score ≥ 400 → aprovação automática)

```bash
curl -s -X POST http://localhost:8080/loans \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123",
    "name": "João Silva",
    "email": "joao@email.com",
    "amount": 5000,
    "installments": 12
  }'
```

**Resposta esperada (HTTP 202):**
```json
{
  "loanId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "Loan request accepted and is being processed."
}
```

O terminal SSE receberá em segundos:
```
event: loan-approved
data: ✅ Parabéns João Silva! Seu empréstimo de R$ 5000,00 em 12 parcelas foi APROVADO. ContractId: ...
```

### 3. Testar score < 400 (decisão de gestor)

Use um userId cujo hash produza score < 400. No algoritmo atual:
- `userId = "low"` → score ≈ 318

```bash
curl -s -X POST http://localhost:8080/loans \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "low",
    "name": "Maria Test",
    "email": "maria@email.com",
    "amount": 1000,
    "installments": 6
  }'
```

Acesse o **Camunda Tasklist** em `http://localhost:8082/camunda/app/tasklist`  
→ Faça login com `demo/demo`  
→ Localize a tarefa **"Manager Decision"**  
→ Preencha `approved = true/false` e complete a tarefa.

### 4. Verificar contrato salvo no MongoDB

```bash
docker exec -it agiemprest-mongodb mongosh agiemprestv2 --eval "db.contracts.find().pretty()"
```

### 5. Testar limite insuficiente

```bash
curl -s -X POST http://localhost:8080/loans \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123",
    "name": "João Silva",
    "email": "joao@email.com",
    "amount": 999999,
    "installments": 12
  }'
```

**Resposta (HTTP 422):**
```json
{"error": "Requested amount 999999.00 exceeds credit limit ...", "code": "INSUFFICIENT_LIMIT"}
```

---

## APIs

### loan-service `:8080`

| Método | Path    | Descrição                        |
|--------|---------|----------------------------------|
| POST   | /loans  | Solicita um empréstimo           |

### credit-analysis-service `:8081`

| Método | Path                 | Descrição                        |
|--------|----------------------|----------------------------------|
| GET    | /analysis/{userId}   | Retorna score e limite de crédito|

### contract-service `:8082`

| Método | Path                    | Descrição                        |
|--------|-------------------------|----------------------------------|
| GET    | /contracts/user/{userId}| Busca contrato ativo do usuário  |

### notification-service `:8083`

| Método | Path                              | Descrição                   |
|--------|-----------------------------------|-----------------------------|
| GET    | /notifications/subscribe/{userId} | Stream SSE de notificações  |

---

## Tópicos Kafka

| Tópico          | Produzido por      | Consumido por         |
|-----------------|--------------------|-----------------------|
| loan-requested  | loan-service       | contract-service      |
| loan-approved   | contract-service   | notification-service  |
| loan-rejected   | contract-service   | notification-service  |

---

## Camunda

O processo **`loan-approval-process`** contém:

- **CheckExistingContract** — ServiceTask → `CheckExistingContractDelegate`
- **ContractExistsGW** — ExclusiveGateway (contractExists = true/false)
- **CheckScore** — ServiceTask → `CheckScoreDelegate`
- **ScoreGW** — ExclusiveGateway (score ≥ 400 / score < 400)
- **CreateContract** — ServiceTask → `CreateContractDelegate` (salva no MongoDB)
- **ManagerDecision** — UserTask (candidateGroups: managers)
- **PublishApproved** — ServiceTask → `PublishApprovedDelegate` → Kafka
- **PublishRejected** — ServiceTask → `PublishRejectedDelegate` → Kafka

---

## Variáveis de Ambiente

| Variável               | Padrão                              | Serviço           |
|------------------------|-------------------------------------|-------------------|
| REDIS_HOST             | localhost                           | loan-service      |
| REDIS_PORT             | 6379                                | loan-service      |
| KAFKA_BOOTSTRAP_SERVERS| localhost:9092                      | todos             |
| CREDIT_ANALYSIS_URL    | http://localhost:8081               | loan-service      |
| MONGODB_URI            | mongodb://localhost:27017/agiemprestv2 | contract-service |
| SERVER_PORT            | (ver cada serviço)                  | todos             |
