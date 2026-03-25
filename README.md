# 🛡️ Sistema Anti-Fraude — TCC Engenharia de Software

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.14-blue)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-TCC-blue)](/LICENSE)

Sistema de detecção de fraudes em transações financeiras desenvolvido como Trabalho de Conclusão de Curso (TCC) — Engenharia de Software UNINTER.

Combina um motor de regras baseado em score com um modelo de Machine Learning (Random Forest) treinado em 284.807 transações reais.

---

## 📋 Sobre o Projeto

O sistema analisa transações financeiras em tempo real, calculando um **score de risco (0-100%)** e tomando decisões automáticas de aprovação, revisão ou bloqueio.

A decisão final usa uma abordagem híbrida:
- **Score < 40 ou ≥ 70**: decisão direta pelas regras (rápido, sem overhead)
- **Score 40–69 (zona de revisão)**: consulta o modelo ML para refinar a decisão, combinando score de regras (60%) com probabilidade do modelo (40%)

### Objetivos

- ✅ Detectar transações fraudulentas automaticamente
- ✅ Reduzir falsos positivos através de análise multicritério
- ✅ Processar análises em tempo real (<200ms)
- ✅ Fornecer score de risco explicável e auditável
- ✅ Integrar modelo de ML como camada complementar às regras

---

## 🚀 Tecnologias

### Backend (Java)
- **Java 17** — linguagem principal
- **Spring Boot 3.2.0** — framework web
- **Spring Data JPA** — persistência de dados
- **Hibernate** — ORM
- **Bean Validation** — validação de entrada
- **RestTemplate** — cliente HTTP para integração com o serviço ML

### Machine Learning (Python)
- **Python 3.14** — linguagem do serviço ML
- **scikit-learn** — treinamento do modelo Random Forest
- **Flask** — API REST do serviço ML
- **pandas / numpy** — manipulação do dataset
- **joblib** — serialização do modelo treinado

### Banco de Dados
- **H2** (desenvolvimento) — banco em memória
- **PostgreSQL** (produção) — banco relacional

### Ferramentas
- **Maven** — gerenciamento de dependências
- **Docker** — containerização
- **Git** — versionamento

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────┐
│               CLIENTE                       │
│         (Postman / Frontend)                │
└──────────────────┬──────────────────────────┘
                   │ HTTP/JSON
                   ▼
┌─────────────────────────────────────────────┐
│         CONTROLLER LAYER                    │
│       TransacaoController                   │
│       Validação de entrada (@Valid)         │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│          SERVICE LAYER                      │
│       AnalisadorRiscoService                │
│       Score por regras (0-100%)             │
│       Decisão: APROVADA / REVISAO /         │
│               BLOQUEADA                     │
│                    │                        │
│            score 40-69?                     │
│                    │                        │
│                    ▼                        │
│           MlFraudeClient                   │
│           (chama serviço ML)                │
└──────────────────┬──────────────────────────┘
                   │                    │
                   ▼                    ▼
┌──────────────────────┐  ┌─────────────────────────┐
│   REPOSITORY LAYER   │  │   ML SERVICE (Python)   │
│  TransacaoRepository │  │   Flask API :5001       │
│  Spring Data JPA     │  │   Random Forest         │
└──────────┬───────────┘  │   284k transações       │
           │              │   F1=0.81 (fraudes)     │
           ▼              └─────────────────────────┘
┌──────────────────────┐
│      DATABASE        │
│   H2 / PostgreSQL    │
└──────────────────────┘
```

---

## 📁 Estrutura do Projeto

```
tcc-sistema-antifraude/
├── src/
│   ├── main/java/com/tcc/antifraude_seguro/
│   │   ├── controller/      TransacaoController.java
│   │   ├── dto/             ErrorResponse.java
│   │   ├── exception/       GlobalExceptionHandler.java
│   │   ├── model/           Transacao.java
│   │   ├── repository/      TransacaoRepository.java
│   │   └── service/
│   │       ├── AnalisadorRiscoService.java
│   │       └── MlFraudeClient.java
│   ├── ml-service/          ← serviço Python/ML
│   │   ├── app.py           API Flask
│   │   ├── train_model.py   treinamento do modelo
│   │   └── requirements.txt dependências Python
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

> **Nota:** O arquivo `creditcard.csv` (143MB) e o modelo treinado `fraud_model.pkl` não estão no repositório por limite de tamanho do GitHub. Para gerar o modelo, siga as instruções abaixo.

---

## 🔌 Endpoints da API

### 1. Status do Sistema
```
GET /api/transacoes/status
```
```
Sistema Anti-Fraude Operacional - TCC 2025
```

---

### 2. Criar Transação
```
POST /api/transacoes
Content-Type: application/json

{
  "usuarioId": "user123",
  "valor": 1500.00,
  "tipo": "PIX"
}
```

**Resposta (Aprovada):**
```json
{
  "id": 1,
  "usuarioId": "user123",
  "valor": 1500.0,
  "tipo": "PIX",
  "dataHora": "2026-03-25T22:30:00",
  "status": "APROVADA",
  "scoreRisco": 35.0
}
```

**Resposta (Bloqueada):**
```json
{
  "id": 2,
  "usuarioId": "fraudador",
  "valor": 15000.0,
  "tipo": "PIX",
  "status": "BLOQUEADA",
  "scoreRisco": 75.0
}
```

---

### 3. Listar Transações
```
GET /api/transacoes
```

### 4. Estatísticas
```
GET /api/transacoes/estatisticas
```

### 5. Explicar Decisão
```
GET /api/transacoes/{id}/explicacao
```

---

## 📊 Regras de Detecção

### Score de Risco (0–100%)

**1️⃣ Valor da Transação (0–50 pontos)**

| Valor | Pontos |
|---|---|
| > R$ 15.000 | +50 |
| > R$ 10.000 | +45 |
| > R$ 5.000 | +40 |
| > R$ 2.000 | +30 |
| ≥ R$ 1.000 | +20 |
| ≥ R$ 500 | +10 |
| < R$ 500 | 0 |

**2️⃣ Horário (0–30 pontos)**

| Horário | Pontos |
|---|---|
| 00h–06h (madrugada) | +30 |
| 22h–23h (noite) | +20 |
| 20h–22h | +10 |
| 06h–09h (manhã cedo) | +5 |
| 09h–20h (comercial) | 0 |

**3️⃣ Tipo de Transação (0–25 pontos)**

| Tipo | Pontos |
|---|---|
| PIX | +25 |
| TED | +15 |
| CARTAO | +12 |
| DOC | +8 |
| BOLETO | +5 |

**4️⃣ Dia da Semana (0–10 pontos)**

| Dia | Pontos |
|---|---|
| Sábado / Domingo | +10 |
| Sexta-feira | +5 |
| Seg–Qui | 0 |

### Decisão Automática

```
Score  0–39%  → ✅ APROVADA
Score 40–69%  → ⚠️  REVISAO  → consulta modelo ML
Score 70–100% → ❌ BLOQUEADA
```

---

## 🤖 Modelo de Machine Learning

- **Algoritmo:** Random Forest Classifier
- **Dataset:** [Credit Card Fraud Detection — Kaggle](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud)
- **Transações:** 284.807 (492 fraudes — 0,17%)
- **Métricas no conjunto de teste:**

| Métrica | Legítima | Fraude |
|---|---|---|
| Precision | 1.00 | 0.81 |
| Recall | 1.00 | 0.81 |
| F1-Score | 1.00 | 0.81 |
| Accuracy | 1.00 | — |

O modelo é consultado apenas na zona de revisão (score 40–69%), combinando sua probabilidade com o score de regras para produzir a decisão final.

---

## 🛠️ Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+ (ou usar o wrapper)
- Python 3.10+
- Dataset `creditcard.csv` do [Kaggle](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud)

### 1. Treinar o modelo ML

```bash
cd src/ml-service
pip install -r requirements.txt
python3 train_model.py
```

### 2. Subir o serviço ML (Flask)

```bash
python3 app.py
# Rodando em http://localhost:5001
```

### 3. Subir o backend Java

```bash
# Na raiz do projeto
./mvnw spring-boot:run
# Rodando em http://localhost:8080
```

### H2 Console

```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (vazio)
```

---

## 🧪 Testes

```bash
# Transação aprovada (score baixo)
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": "maria_silva", "valor": 150.00, "tipo": "BOLETO"}'

# Zona de revisão — aciona modelo ML
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": "joao_santos", "valor": 3000.00, "tipo": "PIX"}'

# Bloqueada diretamente pelas regras
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": "fraudador", "valor": 15000.00, "tipo": "PIX"}'
```

---

## 👨‍💻 Autor

**Alexandre Giacomoni Ioppi**

- 📚 Engenharia de Software — UNINTER (conclusão: setembro 2026)
- 🌍 Liverpool, Inglaterra
- 📧 ioppiengineer@gmail.com
- 💼 [LinkedIn](https://linkedin.com/in/ioppialexandre)
- 🐙 [@aioppi](https://github.com/aioppi)

---

## 📄 Licença

Projeto desenvolvido para fins educacionais como TCC de Engenharia de Software.