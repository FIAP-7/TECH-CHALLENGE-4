# feedback-notification-processor

Função Lambda (Quarkus) acionada por mensagens da fila SQS. Para cada feedback enviado pela API de ingestão, a função consulta os detalhes no DynamoDB e, quando aplicável, envia uma notificação por e-mail via Amazon SES (por exemplo, para feedbacks críticos com nota baixa).

## Visão geral da funcionalidade
- Disparo: evento SQS (ver `template.yaml`, recurso `NotificationProcessorFunction`).
- Para cada mensagem, o corpo esperado contém `{"feedbackId":"<id>"}`.
- A função busca o item correspondente no DynamoDB (`Feedbacks`).
- Caso o feedback atenda ao critério de criticidade (ex.: nota menor ou igual a 2), envia um e-mail de alerta ao administrador.
- Mensagens que falham podem ser redirecionadas para uma DLQ (fila de dead-letter) configurada no stack.

## Stack e integrações
- Linguagem/Framework: Quarkus (Java) como imagem de contêiner para AWS Lambda.
- Amazon SQS: evento de disparo (consumo da fila principal).
- Amazon DynamoDB: consulta dos detalhes do feedback.
- Amazon SES: envio de e-mails de notificação.
- Amazon CloudWatch Logs: observabilidade.

## Variáveis de ambiente principais
Definidas em `template.yaml` e/ou `src/main/resources/application.properties`:
- feedback.dynamodb.table-name: nome da tabela DynamoDB (padrão: `Feedbacks`).
- email.admin.address: e-mail do destinatário administrador.
- email.source.address: e-mail de origem (verificado no SES) utilizado no envio.
- quarkus.lambda.handler: nome do handler (`notificationProcessor`).
- QUARKUS_LOG_LEVEL: nível de log (ex.: `INFO`).

## Detalhes de implementação
- Handler: `com.fiap.techchallenge.notification.NotificationProcessorFunction` (implementa `RequestHandler<SQSEvent, Void>`), anotado com `@Named("notificationProcessor")`.
- Repositório DynamoDB: `com.fiap.techchallenge.notification.repository.FeedbackRepository` (consulta item pelo `FeedbackID`).
- Serviço de E-mail: `com.fiap.techchallenge.notification.service.EmailService` (envia e-mail via SES).
- Mensagens com parsing inválido, ausência de `feedbackId` ou falhas de comunicação com AWS geram logs de erro; falhas lançadas durante o processamento permitem o reenvio/retenção pela SQS e roteamento para DLQ, quando configurado.

## Execução e deploy
- Empacotamento: Maven; deploy via AWS SAM como imagem de contêiner (ver `template.yaml` — recurso `NotificationProcessorFunction`).
- Permissões: a função recebe permissões para leitura no DynamoDB, recebimento/remoção de mensagens da SQS e envio de e-mails via SES, além de logs.

## Observações
- Garanta que `email.source.address` esteja verificado no SES e que a conta/Região permita envio para o destinatário (fora do sandbox ou destinatário verificado).
- Ajuste o limite de criticidade (nota) diretamente no código, se necessário (`NOTA_CRITICA_LIMITE`).
