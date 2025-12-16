# Transaction Gateway 🛡️

Microsserviço de entrada (Entrypoint) do ecossistema de Antifraude. Responsável por ingerir transações, validar segurança/idempotência e publicar eventos no Kafka.

**Arquitetura:** Hexagonal (Ports & Adapters)
**Stack:** Java 21, Quarkus, Kafka, Redis, Prometheus.

## 🚀 Quick Start

### Pré-requisitos
* Java 21+
* Docker (para Kafka/Redis via DevServices)

### Rodar Localmente (Dev Mode)
```bash
./mvnw quarkus:dev
````

A API estará em: `http://localhost:8080`

### Rodar Testes

Executa unitários e integração (com Kafka em memória e Testcontainers).

```bash
./mvnw clean verify
```

## ⚙️ Variáveis de Ambiente

| Variável | Padrão | Descrição |
| :--- | :--- | :--- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço do Kafka |
| `REDIS_HOST` | `localhost:6379` | Endereço do Redis |
| `ANTIFRAUD_SECURITY_API_KEY`| `minha-chave-secreta` | Chave para header `Authorization` |

## 🔌 API Reference

### Criar Transação

`POST /v1/transactions/{transactionId}`

**Headers Obrigatórios:**

* `Authorization`: `minha-chave-secreta`
* `Content-Type`: `application/json`

**Header Opcional:**
* `X-Correlation-ID`: `ULID`

**Exemplo de Payload:**

Nota: O campo amount deve ser enviado como Inteiro (centavos). Ex: R$ 1.500,50 = 150050.

```json
{
  "amount": 150050,
  "currency": "BRL",
  "user_id": "user-123",
  "timestamp": "2025-12-14T10:00:00Z",
  "device_fingerprint": "device-hash-abc",
  "merchant_id": "1234",
  "location": { 
      "latitude": -23.5, 
      "longitude": -46.6 
    }
}
```

**Exemplo CURL:**
```bash
curl -i -X POST http://localhost:8080/v1/transactions/$(uuidgen) \
  -H "Authorization: minha-chave-secreta" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: 01KCJE5ECSJPYS27651HY53JWP" \
  -d '{
    "amount": 150050,
    "currency": "BRL",
    "user_id": "user-123",
    "timestamp": "2025-12-13T19:00:00Z",
    "device_fingerprint": "device-hash-abc",
    "merchant_id": "1234",
    "location": {
        "latitude": -23.5505,
        "longitude": -46.6333
    }
  }'
```

**Status Codes:**

* `202`: Aceito (Processamento Assíncrono).
* `409`: Conflito (Idempotência/Duplicado).
* `401`: Não autorizado.
* `400`: Erro de validação.

## 📊 Observabilidade

* **Métricas (Prometheus):** `http://localhost:8080/q/metrics`
* **Health Check:** `http://localhost:8080/q/health`

## 🏗️ Estrutura do Projeto

* `domain`: Regras de negócio, Entidades e Interfaces.
* `application`: Controllers REST e Filtros.
* `infrastructure`: Implementação de Kafka e Redis.
