# 🛡️ Sistema Anti-Fraude com Machine Learning

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-TCC-blue)](LICENSE)

Sistema inteligente de detecção de fraudes em transações financeiras desenvolvido como Trabalho de Conclusão de Curso (TCC) - Engenharia de Software UNINTER.

Sobre o Projeto

Sistema anti-fraude que analisa transações financeiras em **tempo real**, calculando **score de risco (0-100%)** e tomando **decisões automáticas** de aprovação, revisão ou bloqueio.

Objetivos

- ✅ Detectar transações fraudulentas automaticamente
- ✅ Reduzir falsos positivos através de análise multicritério
- ✅ Processar análises em tempo real (<200ms)
- ✅ Fornecer score de risco explicável e auditável
- ✅ Proteger contra valores negativos e dados inválidos

Tecnologias

Backend
- **Java 17** - Linguagem principal
- **Spring Boot 3.2.0** - Framework web
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM (Object-Relational Mapping)
- **Bean Validation** - Validação de dados

Banco de Dados
- **H2 Database** (desenvolvimento) - Banco em memória
- **PostgreSQL** (produção) - Banco relacional

Ferramentas
- **Maven** - Gerenciamento de dependências
- **Git** - Versionamento
- **Docker** (futuro) - Containerização

Arquitetura
```
┌─────────────────────────────────────────────────┐
│                   CLIENTE                       │
│            (Postman / Frontend)                 │
└──────────────────┬──────────────────────────────┘
                   │ HTTP/JSON
                   ▼
┌─────────────────────────────────────────────────┐
│          CONTROLLER LAYER                       │
│      (Recebe requisições HTTP)                  │
│    - TransacaoController                        │
│    - Validação de entrada (@Valid)              │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│           SERVICE LAYER                         │
│      (Lógica de negócio)                        │
│    - AnalisadorRiscoService                     │
│    - Cálculo de score (0-100%)                  │
│    - Decisão automática                         │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│         REPOSITORY LAYER                        │
│      (Acesso ao banco)                          │
│    - TransacaoRepository                        │
│    - Spring Data JPA                            │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│           DATABASE                              │
│      (Persistência)                             │
│    - H2 / PostgreSQL                            │
└─────────────────────────────────────────────────┘
```

Endpoints da API

1. Status do Sistema
```bash
GET /api/transacoes/status
```
**Resposta:**
```
Sistema Anti-Fraude Operacional - TCC 2025
```

---

2. Criar Transação (com análise automática)
```bash
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
  "dataHora": "2026-01-08T19:30:00",
  "status": "APROVADO",
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
  "status": "BLOQUEADO",
  "scoreRisco": 75.0
}
```

---

3. Listar Todas as Transações
```bash
GET /api/transacoes
```

---

4. Estatísticas
```bash
GET /api/transacoes/estatisticas
```

**Resposta:**
```json
{
  "total": 10,
  "bloqueadas": 2,
  "aprovadas": 6,
  "emRevisao": 2,
  "valorTotal": "R$ 45.000,00",
  "valorBloqueado": "R$ 27.000,00",
  "scoreMedia": "48,5%",
  "taxaBloqueio": "20,0%"
}
```

---

### 5. Explicar Decisão
```bash
GET /api/transacoes/{id}/explicacao
```

**Resposta:**
```json
{
  "transacaoId": 1,
  "status": "BLOQUEADO",
  "scoreRisco": 75.0,
  "explicacao": "Score: 75.0%\nMotivos:\n- Valor: R$ 15000.0 (+50.0%)\n- Tipo: PIX (+25.0%)\n"
}
```

Regras de Detecção

### Score de Risco (0-100%)

O sistema calcula pontuação baseada em **4 critérios**:

#### 1️⃣ **Valor da Transação (peso 0-50 pontos)**
| Valor | Pontos | Risco |
|-------|--------|-------|
| > R$ 15.000 | +50 | Muito Alto |
| > R$ 10.000 | +45 | Alto |
| > R$ 5.000 | +40 | Médio-Alto |
| > R$ 2.000 | +30 | Médio |
| > R$ 1.000 | +20 | Baixo-Médio |
| > R$ 500 | +10 | Baixo |
| < R$ 500 | 0 | Normal |

#### 2️⃣ **Horário (peso 0-30 pontos)**
| Horário | Pontos | Motivo |
|---------|--------|--------|
| 00h-06h | +30 | Madrugada (vítima dormindo) |
| 22h-23h | +20 | Noite (suporte reduzido) |
| 06h-09h | +5 | Manhã cedo |
| 20h-22h | +10 | Noite inicial |
| 09h-17h | 0 | Horário comercial (seguro) |
| 17h-20h | 0 | Pico legítimo (saída trabalho) |

#### 3️⃣ **Tipo de Transação (peso 0-25 pontos)**
| Tipo | Pontos | Motivo |
|------|--------|--------|
| PIX | +25 | Instantâneo, difícil reverter |
| TED | +15 | Rápido, rastreável |
| CARTAO | +12 | Pode ter chargeback |
| DOC | +8 | Mais lento |
| BOLETO | +5 | Mais rastreável |

#### 4️⃣ **Dia da Semana (peso 0-10 pontos)**
| Dia | Pontos | Motivo |
|-----|--------|--------|
| Sábado/Domingo | +10 | Suporte reduzido |
| Sexta-feira | +5 | Véspera de feriado |
| Seg-Qui | 0 | Dias úteis normais |

### Decisão Automática
```
Score 0-39%   → ✅ APROVADO    (transação legítima)
Score 40-69%  → ⚠️  REVISÃO     (análise humana)
Score 70-100% → ❌ BLOQUEADO   (fraude detectada)
```

✅ Validações Implementadas

O sistema **bloqueia automaticamente**:

| Campo | Validação | Exemplo Inválido | Mensagem |
|-------|-----------|------------------|----------|
| `usuarioId` | Não vazio, 3-50 chars | `"ab"` | "Usuário ID deve ter entre 3 e 50 caracteres" |
| `valor` | Positivo, max 1M | `-100` ou `0` | "Valor deve ser positivo" |
| `tipo` | Enum específico | `"TRANSFERENCIA"` | "Tipo deve ser: PIX, TED, DOC, BOLETO ou CARTAO" |

**Exemplo de erro:**
```json
{
  "timestamp": "2026-01-08T19:30:00",
  "status": 400,
  "error": "Erro de validação",
  "messages": [
    "usuarioId: Usuário ID deve ter entre 3 e 50 caracteres",
    "valor: Valor deve ser positivo",
    "tipo: Tipo deve ser: PIX, TED, DOC, BOLETO ou CARTAO"
  ],
  "path": "/api/transacoes"
}
```

Como Executar

### Pré-requisitos

- **Java 17+** instalado
- **Maven 3.6+** (ou usar o wrapper incluído)
- **Git** para clonar o repositório

### Passo a Passo
```bash
# 1. Clone o repositório
git clone https://github.com/aioppi/tcc-sistema-antifraude.git
cd tcc-sistema-antifraude

# 2. Execute a aplicação
./mvnw spring-boot:run

# 3. Acesse
http://localhost:8080/api/transacoes/status
```

### H2 Console (Interface do Banco)

Acesse o console web do banco de dados:
```
URL: http://localhost:8080/h2-console

Configuração:
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (deixe vazio)
```

**Query de exemplo:**
```sql
SELECT * FROM TRANSACOES ORDER BY SCORE_RISCO DESC;
```

Testes

### Teste 1: Transação Normal (Aprovada)
```bash
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": "maria_silva",
    "valor": 150.00,
    "tipo": "PIX"
  }'
```
**Resultado:** Score ~25%, Status: APROVADO

---

Teste 2: Transação Suspeita (Revisão)
```bash
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": "joao_santos",
    "valor": 3000.00,
    "tipo": "PIX"
  }'
```
**Resultado:** Score ~55%, Status: REVISAO

---

Teste 3: Fraude Detectada (Bloqueada)
```bash
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": "fraudador",
    "valor": 15000.00,
    "tipo": "PIX"
  }'
```
**Resultado:** Score ~75%, Status: BLOQUEADO

---

Teste 4: Validação (Erro)
```bash
curl -X POST http://localhost:8080/api/transacoes \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": "ab",
    "valor": -100,
    "tipo": "INVALIDO"
  }'
```
**Resultado:** HTTP 400 com lista de erros

Autor

**Alexandre Giacomoni Ioppi**
-  Engenharia de Software - UNINTER
-  Graduação: Setembro 2026
-  Liverpool, Inglaterra
-  ioppiengineer@gmail.com
-  [LinkedIn](https://linkedin.com/in/ioppialexandre)
-  [@aioppi](https://github.com/aioppi)

Licença

Este projeto é parte de um Trabalho de Conclusão de Curso (TCC) e está disponível para fins **educacionais**.

---

Estatísticas do Projeto

![GitHub last commit](https://img.shields.io/github/last-commit/aioppi/tcc-sistema-antifraude)
![GitHub commit activity](https://img.shields.io/github/commit-activity/w/aioppi/tcc-sistema-antifraude)

---

**Se este projeto te ajudou, considere dar uma estrela!**

---

Suporte

Encontrou algum problema? Abra uma [issue](https://github.com/aioppi/tcc-sistema-antifraude/issues)!
