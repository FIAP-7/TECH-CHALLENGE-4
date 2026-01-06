# feedback-ingestion-api

Função Lambda (Quarkus) exposta via AWS HttpApi responsável por receber avaliações dos usuários (feedbacks), persistir no DynamoDB e publicar uma mensagem na fila SQS para processamento assíncrono.

## Visão geral da funcionalidade
- Endpoint HTTP: `POST /avaliacao` (ver mapeamento em `template.yaml`).
- Corpo da requisição (JSON):
  - `descricao` (string, opcional)
  - `nota` (inteiro, opcional)
- A função gera um `feedbackId`, persiste o registro na tabela DynamoDB (`Feedbacks`) com status "PENDENTE" e data de envio, e publica uma mensagem na SQS contendo o `feedbackId`.
- Autenticação/Autorização: o HttpApi utiliza JWT do Amazon Cognito (Authorizer configurado em `template.yaml`). O `userId` (quando disponível via token) é associado ao feedback.

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

## Observações
- Certifique-se de configurar o Authorizer do Cognito e o cliente (`FeedbackUserPool` e `FeedbackUserPoolClient` em `template.yaml`) para proteção do endpoint.
- A URL da fila SQS é injetada via variável de ambiente; sem ela, o serviço registra um aviso e não publica a mensagem.
