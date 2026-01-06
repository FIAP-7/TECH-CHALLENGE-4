# feedback-report-generator

Função Lambda responsável por gerar semanalmente um relatório em PDF com os feedbacks armazenados no DynamoDB, salvar o arquivo no S3 e enviar um e-mail ao administrador com o link temporário para download.

## Visão geral da funcionalidade
- Agenda: acionada por EventBridge (Schedule) conforme definido em `template.yaml` (padrão `rate(1 day)`).
- Consulta a tabela DynamoDB (`Feedbacks`) para obter os registros de feedback.
- Gera um PDF contendo o resumo/relatório dos feedbacks.
- Salva o PDF no bucket S3 configurado.
- Gera uma URL pré-assinada com validade controlada e envia e-mail via Amazon SES ao administrador contendo o link para download.

## Stack e integrações
- Linguagem/Framework: Quarkus (Java) empacotado como imagem de contêiner para AWS Lambda.
- AWS EventBridge: executa a função em intervalos definidos (scheduler).
- Amazon DynamoDB: origem dos dados (feedbacks).
- Amazon S3: armazenamento dos PDFs gerados.
- Amazon SES: envio do e-mail com o link do relatório.
- Amazon CloudWatch Logs: observabilidade (logs).

## Variáveis de ambiente principais
Definidas em `template.yaml` e/ou `src/main/resources/application.properties`:
- FEEDBACK_DYNAMODB_TABLE_NAME: nome da tabela DynamoDB (padrão: `Feedbacks`).
- PDF_BUCKET: bucket S3 para salvar os relatórios (padrão: `relatorios-avaliacao-bucket`).
- PDF_DIAS_URL_VALIDA: dias de validade do link pré-assinado (padrão: `7`).
- EMAIL_ADMIN_ADDRESS: e-mail de destino (admin) para receber o link.
- EMAIL_SOURCE_ADDRESS: e-mail de origem (verificado no SES) para envio.
- QUARKUS_LOG_LEVEL: nível de log (ex.: `INFO`).

## Execução e deploy
- Empacotamento: o projeto é construído com Maven e implantado via AWS SAM como imagem de contêiner (ver `template.yaml`).
- Durante o deploy, a função recebe permissões para ler o DynamoDB, escrever/ler no S3 e enviar e-mails pelo SES, além de escrever logs no CloudWatch.

## Estrutura do código (alto nível)
- `domain` e `application/usecase`: regras para consulta e agregação dos feedbacks e geração do relatório.
- `infrastructure/dynamo`: acesso ao DynamoDB.
- `infrastructure/pdf`: geração do PDF.
- `infrastructure/storage`: gravação no S3 e criação do link pré-assinado.
- `infrastructure/email`: envio de e-mail via SES.

## Observações
- Garanta que o remetente (EMAIL_SOURCE_ADDRESS) esteja verificado no SES e que a conta/Região não esteja limitada ao sandbox ou, se estiver, que os destinatários também estejam verificados.
- Ajuste o agendamento em `ReportSchedule` no `template.yaml` caso necessite outra periodicidade.
