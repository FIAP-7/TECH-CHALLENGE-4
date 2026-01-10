# feedback-ingestion-api

Função Lambda (Quarkus) exposta via AWS HttpApi responsável por receber avaliações dos usuários (feedbacks), persistir no DynamoDB e publicar uma mensagem na fila SQS para processamento assíncrono.

## Visão geral da funcionalidade
- Endpoint HTTP: `POST /avaliacao` (ver mapeamento em `template.yaml`).
- Corpo da requisição (JSON):
  - `descricao` (string, opcional)
  - `nota` (inteiro, opcional)
- A função gera um `feedbackId`, persiste o registro na tabela DynamoDB (`Feedbacks`) com status "PENDENTE" e data de envio, e publica uma mensagem na SQS contendo o `feedbackId`.
- Autenticação/Autorização: o HttpApi utiliza JWT do Amazon Cognito (Authorizer configurado em `template.yaml`).

## Stack e integrações
- Linguagem/Framework: Quarkus (Java) como imagem de contêiner para AWS Lambda.
- AWS HttpApi (API Gateway v2): expõe o endpoint `/avaliacao` (método POST).
- Amazon DynamoDB: armazenamento dos feedbacks.
- Amazon SQS: publicação de mensagens para processamento posterior.
- Amazon CloudWatch Logs: observabilidade.

## Variáveis de ambiente principais
Definidas em `template.yaml` e/ou `src/main/resources/application.properties`:
- FEEDBACK_DYNAMODB_TABLE_NAME: nome da tabela DynamoDB (padrão: `Feedbacks`).
- FEEDBACK_SUBMITTED_QUEUE_URL: URL da fila SQS principal para envio da mensagem de novo feedback.
- QUARKUS_LOG_LEVEL: nível de log (ex.: `INFO`).

## Detalhes de implementação
- Recurso REST: `com.fiap.techchallenge.ingestion.rest.FeedbackResource` (rota `/avaliacao`).
- Serviço: `com.fiap.techchallenge.ingestion.service.FeedbackService` realiza a persistência no DynamoDB (PutItem) e o envio da mensagem na SQS (`{"feedbackId":"<id>"}`).
- Configuração: `src/main/resources/application.properties` define chaves `feedback.dynamodb.table-name` e `feedback.sqs.queue-url`. Observação: `quarkus.http.enabled=false` (execução como Lambda, sem servidor HTTP local embutido).

## Execução e deploy
- Empacotamento: Maven; deploy via AWS SAM como imagem de contêiner (ver `template.yaml` — recurso `FeedbackIngestionFunction`).
- Permissões: a função possui permissões para `dynamodb:PutItem`, `sqs:SendMessage` e logs no CloudWatch.

## Governança de Acesso via Grupos do Cognito (RBAC)
Esta API implementa um controle de acesso baseado em papéis (RBAC) usando grupos do Amazon Cognito. Apenas usuários pertencentes ao grupo "Alunos" podem enviar avaliações via `POST /avaliacao`.

### Como a validação foi implementada (na Lambda)
- A validação ocorre dentro da Lambda (camada de aplicação), reaproveitando o JWT já validado pelo Authorizer do API Gateway. Não há chamadas extras ao Cognito, preservando baixa latência.
- O recurso `FeedbackResource` extrai o token JWT do cabeçalho `Authorization` (formato `Bearer <token>`), decodifica a parte do payload e inspeciona as claims.
- Claims lidas para RBAC:
  - Preferencialmente `cognito:groups` (array padrão do Cognito)
  - Fallback para `groups` (alguns ambientes/integrações podem popular apenas esta claim)
- Regra de autorização: o grupo requerido é exatamente `Alunos`.
- Em caso de erro ao ler/parsear o token ou ausências das claims esperadas, a requisição é tratada como não autorizada para este recurso.

### Comportamentos esperados
- Usuário com token válido e pertencente ao grupo "Alunos":
  - Resposta: `201 Created`
  - Corpo: objeto `Feedback` criado (inclui `feedbackId`, `status` = `PENDENTE`, `dataEnvio` etc.)
- Usuário com token válido mas sem o grupo "Alunos" (ex.: "Administradores" ou sem grupo):
  - Resposta: `403 Forbidden`
  - Corpo (JSON): `{ "message": "Acesso negado: seu perfil não tem permissão para enviar avaliações." }`
- Requisição sem cabeçalho `Authorization` ou token malformado/ilegível:
  - Na prática, o API Gateway deve barrar antes (401), mas, se chegar à Lambda e falhar a leitura, a regra interna resulta em negação e a API retorna `403`.

### Como testar
- Obtenha um JWT a partir do Cognito para um usuário membro do grupo `Alunos`.
- Envie uma requisição HTTP com o cabeçalho `Authorization: Bearer <JWT>`.

Exemplos (curl):
- Caso permitido (grupo Alunos):
```
curl -i \
  -H "Authorization: Bearer $JWT_ALUNO" \
  -H "Content-Type: application/json" \
  -d '{"descricao":"ótimo serviço","nota":10}' \
  https://<api-id>.execute-api.<region>.amazonaws.com/avaliacao
```
Resposta esperada: `201 Created`.

- Caso negado (outro grupo / sem grupo):
```
curl -i \
  -H "Authorization: Bearer $JWT_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"descricao":"teste","nota":5}' \
  https://<api-id>.execute-api.<region>.amazonaws.com/avaliacao
```
Resposta esperada: `403 Forbidden` e corpo com a mensagem de acesso negado.

### Observabilidade e troubleshooting
- Em `CloudWatch Logs`, procure por entradas do `FeedbackService` e por eventuais warnings. A negativa por RBAC não loga detalhes sensíveis do token.
- Possíveis causas para 403 inesperado:
  - Usuário não está no grupo `Alunos` no User Pool.
  - Token sem a claim `cognito:groups` e `groups` não presente.
  - Token expirado/malformado (em geral barrado pelo Authorizer).
- Segurança: a Lambda não confia em dados de rede externos; apenas lê as claims já validadas pelo Authorizer. Não são feitas chamadas adicionais ao Cognito.