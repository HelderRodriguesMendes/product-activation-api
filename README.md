# Product Activation API

API REST para registrar, processar e consultar solicitações de ativação de produtos, desenvolvida como desafio técnico Java Pleno/Sênior.

## Tecnologias

Java 21, Spring Boot 4, Spring MVC, Spring Data JPA, PostgreSQL 17, Flyway, Maven, JUnit/Mockito, Spring RestClient, WireMock, Docker e Docker Compose. Documentação HTTP com Swagger/OpenAPI.

## Como executar

**Pré-requisitos:** Docker e Docker Compose. Portas locais 8080, 5432 e 8089 disponíveis.

A partir da raiz do repositório:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

Swagger UI: http://localhost:8080/swagger-ui/index.html

OpenAPI: http://localhost:8080/v3/api-docs

Para encerrar:

```bash
docker compose down
```

O volume PostgreSQL é preservado por padrão. `docker compose down -v` **apaga os dados persistidos**.

### Execução local

Com Java 21 e Docker disponíveis, suba os serviços auxiliares e execute a aplicação pelo Maven Wrapper:

```bash
docker compose up -d postgres wiremock
./mvnw spring-boot:run
```

No Windows PowerShell, use `./mvnw.cmd` (ou `.\mvnw.cmd`). Não execute a API local e o container `api` simultaneamente na porta 8080.

### Configuração

| Variável | Finalidade | Valor para execução local |
|---|---|---|
| `DB_URL` | JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/product_activation_db` |
| `DB_USERNAME` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `postgres` (apenas desenvolvimento) |
| `PROVIDER_BASE_URL` | URL base do provedor | `http://localhost:8089` |

No Compose, os endereços internos são `postgres:5432` e `wiremock:8080`. O Flyway aplica a migração de criação da tabela `activation_requests`. As credenciais exemplificadas são **somente para ambiente local**, não para produção.

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| `POST` | `/api/v1/requests` | Cria e processa uma ativação; retorna `201` para nova solicitação e `200` para repetição idêntica |
| `GET` | `/api/v1/requests/{id}` | Consulta uma ativação por UUID; retorna `404` quando não encontrada |

Exemplo de criação:

```bash
curl -i -X POST http://localhost:8080/api/v1/requests \
  -H 'Content-Type: application/json' \
  -d '{"externalId":"EXT-001","customerId":"CUSTOMER-001","productCode":"PRODUCT-001"}'
```

O retorno inclui `id`, `externalId`, `customerId`, `productCode`, `status`, `createdAt` e `updatedAt` em UTC. Na criação, o cabeçalho `Location` aponta para a consulta por ID.

## Arquitetura e decisões técnicas

A implementação separa responsabilidades entre domínio (modelo e transições de status), aplicação (casos de uso e portas), persistência (adaptadores JPA) e integração HTTP com o provedor. DTOs HTTP não são expostos diretamente como entidades JPA.

### Estados

A solicitação nasce em `PENDING`, passa para `PROCESSING` e termina em `COMPLETED` ou `FAILED`. As transições são validadas no domínio. A implementação atual processa a ativação **sincronamente**: o POST aguarda a chamada ao provedor antes de responder. Isso simplifica o fluxo, mas aumenta a latência percebida pelo cliente quando o provedor fica lento.

### Idempotência e concorrência

`externalId` possui restrição `UNIQUE` no PostgreSQL. Para um `externalId` existente:

- Mesmo `customerId` e `productCode`: devolve a solicitação existente (`200 OK`), preservando ID e datas, sem chamar novamente o provedor.
- Conteúdo diferente: devolve `409 Conflict`.
- Requisições concorrentes: a verificação prévia é complementada pela restrição única no banco e pelo tratamento de colisão na persistência, evitando dois registros com o mesmo `externalId`. A inserção é isolada em uma transação própria, com sincronização via `flush`, para que a violação seja detectada na inserção.

A restrição única garante unicidade **dos registros**, mas não substitui um teste de concorrência ponta a ponta nem, sozinha, garante processamento externo exatamente uma vez. Para produção, acrescentaria teste concorrente com PostgreSQL real e protocolo de idempotência aceito pelo provedor.

### Integração com o provedor

O adaptador envia `POST /provider/v1/activate` usando Spring `RestClient`. O WireMock simula sucesso, erro 400, erro 500 e timeout. Há timeouts explícitos de conexão e leitura de aproximadamente 2 segundos. A chamada remota não deve ocorrer dentro de uma transação de banco mantida aberta durante toda a requisição.

| Resposta do provedor | Status local atual | Nova tentativa recomendada em produção | Motivo |
|---|---|---|---|
| 2xx | `COMPLETED` | Não | Operação confirmada |
| 400 | `FAILED` | Não, sem corrigir dados | Erro de requisição não transitório |
| 500 | `FAILED` | Sim, de forma controlada | Pode ser falha temporária |
| Timeout | `FAILED` | Somente após verificar o resultado / usar idempotência no provedor | O provedor pode ter concluído a operação apesar do timeout |
| 429 / 503 | Não há tratamento específico | Respeitar `Retry-After` | Diferencial ainda não implementado |

**Limitação atual:** não há agendador de retentativas automáticas nem reconciliação de solicitações interrompidas. Para produção, usaria outbox transacional e um worker que processe eventos pendentes, com tentativas limitadas, backoff exponencial e chave de idempotência encaminhada ao provedor. Isso cobre o intervalo crítico entre persistir a solicitação e chamar o serviço externo, além de permitir recuperação após falha da aplicação.

## Erros e validação

Os campos `externalId`, `customerId` e `productCode` são obrigatórios. A API trata erros de validação, conflito de idempotência e solicitação não encontrada com códigos HTTP apropriados. A resposta de erro segue o formato Problem Details (`application/problem+json`). Para produção, reforçaria limites de tamanho dos campos e do corpo HTTP, sanitização das mensagens e logs sem informações sensíveis.

## Segurança — proposta para produção

A autenticação/autorização OAuth 2.0/JWT **não foi implementada** neste desafio. Para comunicação entre sistemas, adotaria **OAuth 2.0 Client Credentials**, com um provedor de identidade externo e a API configurada como **Spring Security OAuth2 Resource Server**.

A API validaria assinatura do JWT via chaves públicas do JWKS do emissor, `iss`, `aud`, `exp` e, quando aplicável, `nbf`. Aplicaria autorização por escopos, por exemplo `activation:write` no POST e `activation:read` no GET. Tokens ausentes/inválidos retornariam `401`, e escopo insuficiente retornaria `403`.

Também restringiria acesso aos registros pelo consumidor autorizado (identidade/tenant extraído do token), evitando acesso indevido a solicitações de outros clientes. Credenciais do provedor externo seriam mantidas em um gerenciador de segredos, com rotação e privilégios mínimos; jamais no repositório ou nos logs. Em produção, usaria TLS, limitação de taxa e limites de payload.

## Testes

Há testes unitários do domínio e do caso de uso (incluindo repetição idêntica, conflito e transições), além de testes do adaptador HTTP para sucesso, 400, 500 e timeout. Os testes do adaptador HTTP dependem do WireMock acessível na porta 8089; por isso, inicie-o antes de executar a suíte completa:

```bash
docker compose up -d postgres wiremock
./mvnw test
```

No PowerShell:

```powershell
docker compose up -d postgres wiremock
.\mvnw.cmd test
```

A compilação da imagem Docker usa `-DskipTests` para não depender de serviços externos durante o build; execute os testes separadamente. Foram verificados manualmente via Swagger os cenários de criação `201`, repetição `200`, conflito `409`, consulta existente `200` e inexistente `404`. Testes de concorrência real, de controllers e de execução a partir de ambiente completamente limpo ainda podem ser ampliados.

## Cenário de análise de produção (item 10)

**Sintomas:** tempo médio da API de 300 ms para 2,5 s; CPU em 45%; memória em 60%; consultas ao banco de 50 ms para 1,2 s; provedor externo de 400 ms para 3 s.

**O que investigar primeiro:** identificar quando a degradação começou, quais endpoints foram afetados e a contribuição de cada dependência para o tempo total. Verificar taxa de requisições, erros, saturação de pools de conexões/threads e alterações recentes. CPU e memória moderadas não descartam gargalos de I/O, locks ou espera por rede.

**Como isolar a causa:** instrumentar o tempo de cada etapa (controller, consulta/inserção PostgreSQL e HTTP ao provedor). No banco, consultar `pg_stat_statements`, planos `EXPLAIN ANALYZE`, locks, índices e conexões. Na integração, medir latência por código HTTP, timeouts e número de chamadas. Comparar percentis p50/p95/p99, não apenas médias.

**Logs e métricas:** logs estruturados com `traceId`/`correlationId`, `externalId` com cuidado para não expor dados sensíveis, duração de consultas e chamadas, taxa de erro, pool Hikari, latência do provedor, filas e número de ativações por status. Em produção, integraria Micrometer/Prometheus e tracing distribuído.

**Melhorias possíveis:** otimizar consultas e índices conforme plano real, reduzir contenção, ajustar pools com base em medições, definir timeouts e circuit breaker, aplicar backoff para falhas transitórias e desacoplar o processamento do provedor por fila/outbox quando a latência justificar.

**Hipótese de causa comum:** lentidão do provedor mantém requisições abertas, aumenta a concorrência e a pressão sobre recursos; se houver transações/consultas acopladas, o banco também degrada. Confirmaria com traces ponta a ponta e métricas correlacionadas no tempo.

**Como validar a correção:** reproduzir a carga antes/depois, acompanhar p95/p99, erros e throughput, verificar ausência de duplicidades, executar rollout gradual e monitorar regressões.

## Execução em produção

Publicaria a imagem em um registry e executaria o serviço em plataforma de containers, com PostgreSQL gerenciado, segredos fora da imagem, rede privada para o banco, TLS, réplicas e políticas de recursos. A migração Flyway seria controlada no deploy. Para várias instâncias, manteria a unicidade no banco e evoluiria o processamento para outbox/worker com claim atômico, evitando ativação duplicada. Adicionaria health checks, métricas, logs correlacionados e pipeline CI/CD (build, testes, análise, imagem e deploy gradual).

## Pontos pendentes e melhorias futuras

- `GET /api/v1/requests` com paginação e filtros por `status` e `customerId`, acompanhado de índices e testes.
- Testes concorrentes reais com banco e cenários de falha entre commit e chamada ao provedor.
- Política de retry/reconciliação e idempotência de ponta a ponta com o provedor.
- Tratamento de 429/503 com `Retry-After`, circuit breaker e configuração externa dos timeouts.
- Spring Security OAuth2 Resource Server, autorização por escopos e isolamento por consumidor.
- Observabilidade (Actuator, Micrometer, Prometheus, tracing), limites de requisição e pipeline de CI/CD.

## Utilização de Inteligência Artificial

Durante o desenvolvimento do desafio, utilizei o ChatGPT como ferramenta de apoio pontual, principalmente para:

- Auxiliar na organização dos commits e na padronização de suas mensagens.
- Consultar exemplos de configuração do WireMock para simular respostas do provedor externo.
- Esclarecer dúvidas técnicas e revisar possíveis cenários de teste.
- Apoiar a organização da documentação do projeto.

A ferramenta foi utilizada como recurso complementar de consulta e revisão, com as sugestões analisadas e validadas durante o desenvolvimento.