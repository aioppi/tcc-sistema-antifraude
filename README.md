# 🛡️ Sistema Anti-Fraude com Machine Learning

Sistema de detecção de fraudes em transações financeiras digitais desenvolvido como Trabalho de Conclusão de Curso (TCC) do curso de Engenharia de Software.

## 📋 Sobre o Projeto

Sistema anti-fraude que analisa transações financeiras em tempo real, calculando score de risco (0-100%) e tomando decisões automáticas de aprovação, revisão ou bloqueio.

### 🎯 Objetivos

- Detectar transações fraudulentas automaticamente
- Reduzir falsos positivos
- Processar análises em tempo real (<200ms)
- Fornecer score de risco explicável

## 🚀 Tecnologias

- **Backend:** Java 17, Spring Boot 3.2
- **Banco de Dados:** H2 (desenvolvimento), PostgreSQL (produção)
- **Build:** Maven
- **Testes:** JUnit 5, Mockito
- **Versionamento:** Git

## 📦 Arquitetura
```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ HTTP/JSON
       ▼
┌─────────────────────────┐
│  TransacaoController    │
│  (API REST)             │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│  AnalisadorRiscoService │
│  (Lógica de Negócio)    │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│  TransacaoRepository    │
│  (Spring Data JPA)      │
└──────┬──────────────────┘
       │
       ▼
┌─────────────────────────┐
│  H2 Database            │
└─────────────────────────┘
```

## 🔌 Endpoints da API

### Status do Sistema
```bash
GET /api/transacoes/status
```

### Criar Transação
```bash
POST /api/transacoes
Content-Type: application/json

{
  "usuarioId": "user123",
  "valor": 1500.00,
  "tipo": "PIX"
}
```

### Listar Transações
```bash
GET /api/transacoes
```

### Estatísticas
```bash
GET /api/transacoes/estatisticas
```

## 🛠️ Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.6+

### Executar Localmente
```bash
# Clone o repositório
git clone https://github.com/SEU-USUARIO/tcc-sistema-antifraude.git
cd tcc-sistema-antifraude

# Execute
./mvnw spring-boot:run

# Acesse
http://localhost:8080/api/transacoes/status
```

### H2 Console

Acesse o console do banco de dados:
```
http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (vazio)
```

## 📊 Regras de Detecção (Versão Atual)

### Score de Risco

O sistema calcula um score de 0-100% baseado em:

1. **Valor da Transação (peso 40%)**
   - > R$ 5.000: +40 pontos
   - > R$ 2.000: +25 pontos
   - > R$ 1.000: +15 pontos

2. **Horário (peso 30%)**
   - 00h-05h: +30 pontos (madrugada)
   - 22h-23h: +20 pontos (noite)

3. **Tipo de Transação (peso 20%)**
   - PIX: +10 pontos

4. **Dia da Semana (peso 10%)**
   - Sábado/Domingo: +10 pontos

### Decisão Automática

- **0-39%:** ✅ APROVADO
- **40-69%:** ⚠️ REVISÃO MANUAL
- **70-100%:** ❌ BLOQUEADO

## 📈 Roadmap

- [x] Dia 1: API REST + Banco de Dados
- [ ] Dia 2: Lógica de Detecção de Fraude
- [ ] Dia 3: Spring Security + Validação
- [ ] Dia 4: Testes Automatizados
- [ ] Dia 5: Docker + Documentação
- [ ] Semana 2: Análise de Segurança (OWASP)
- [ ] Semana 3: Machine Learning Avançado
- [ ] Semana 4: Documento Final TCC

## 👨‍💻 Autor

**Alexandre Giacomoni Ioppi**
- Engenharia de Software - UNINTER
- LinkedIn: https://www.linkedin.com/in/alexandreioppi/
- GitHub: [@aioppi](https://github.com/aioppi)

## 📄 Licença

Este projeto é parte de um Trabalho de Conclusão de Curso e está disponível para fins educacionais.

---

⭐ Se este projeto te ajudou, considere dar uma estrela!
