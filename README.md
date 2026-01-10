# Tech Challenge 4 — Sistema de Feedback (Landing Page)

Bem-vindo(a)! Este repositório contém o sistema de coleta, processamento e geração de relatórios de feedbacks, construído sobre a AWS como desafio da Fase 4 da pós graduação Arquitetura e Desenvolvimento Java da FIAP.

. Aqui você encontrará:
- Uma API de ingestão para receber avaliações (com autenticação Cognito e RBAC por grupos)
- Um processador assíncrono que lê mensagens da SQS e envia alertas por e-mail (SES)
- Um gerador de relatórios que cria PDFs e integra com serviços AWS
- Documentação centralizada na pasta `docs/`

Se é sua primeira visita, comece por “Visão Geral” e depois explore os módulos (Lambdas) e seus READMEs específicos.

## Visão Geral do Projeto
O fluxo principal é:
1. Usuário autenticado envia um feedback pela API (apenas grupo "Alunos" autorizado)
2. A API persiste o item no DynamoDB e envia o ID para a fila SQS
3. O processador assíncrono lê a fila, avalia regras e notifica via SES quando necessário
4. O gerador de relatórios consulta dados e produz PDFs para análise/consumo

Para uma visão detalhada da arquitetura, critérios de aceitação e decisões, consulte a pasta:
- docs/ (documentação geral)
  - docs/arquitetura-geral-sistema.md

## Módulos (Lambdas) e Documentos
- feedback-ingestion-api — API HTTP para receber avaliações (Quarkus + AWS Lambda HTTP API)
  - README: feedback-ingestion-api/README.md
- feedback-notification-processor — Processador SQS → DynamoDB → SES, com regra de criticidade (ex.: nota <= 6)
  - README: feedback-notification-processor/README.md
- feedback-report-generator — Geração de relatórios (PDF), integração com DynamoDB/Storage/E-mail
  - README: feedback-report-generator/README.md

## Tecnologias Utilizadas
- Linguagem/Framework: Java 21 com Quarkus
- AWS: Lambda, API Gateway (HTTP API), Cognito (JWT/Grupos), DynamoDB, SQS, SNS, SES, CloudWatch, S3, EventBridge, IAM, X-Ray
- Build/Empacotamento: Maven, AWS SAM, Docker
- Observabilidade: CloudWatch Logs (JSON Logging opcional)

## Estrutura do Projeto
- feedback-ingestion-api
  - src/main/java/com/fiap/techchallenge/ingestion/... (REST, Service, DTO, Model)
  - README.md
- feedback-notification-processor
  - src/main/java/com/fiap/techchallenge/notification/... (SQS, Service, SES)
  - README.md
- feedback-report-generator
  - src/main/java/com/fiap/techchallenge/... (domain, application, infrastructure/pdf|dynamo|email|storage)
  - README.md
- docs
  - arquitetura-geral-sistema.md
- .github
  - workflows
    - deploy.yml
- template.yaml (Infra como Código — recursos AWS e integrações)
- samconfig.toml (configurações de deploy)

## Como Navegar
- Quer entender a arquitetura? Abra docs/arquitetura-geral-sistema.md
- Quer subir os componentes? Veja template.yaml e os READMEs dos módulos para build/run/deploy
- Quer conhecer as regras de acesso (RBAC) da API? Veja feedback-ingestion-api/README.md

## Padrões e Convenções
- Implementado totalmente em ambiente de cloud, no modelo serveless
- Deploy 100% automatizado via Github Actions
- Segurança na borda com Authorizer do API Gateway (Cognito) e verificação de grupo (RBAC) na Lambda de ingestão
- Comunicação assíncrona via SQS para desacoplamento e resiliência
- Logs estruturados e mínimos privilégios de IAM

## 📚 Créditos

Projeto desenvolvido para o **Tech Challenge FIAP** como parte da entrega da fase 4.

Autores:
- [@FMTSL - Felipe Matos](https://github.com/FMTSL)
- [@gustavoleite - Gustavo Leite](https://github.com/gustavoleite)
- [@JefHerc - Jeferson Matos](https://github.com/JefHerc)
- [@kellycps - Kelly](https://github.com/kellycps)
- [@MichaelPBarroso - Michael Barroso](https://github.com/MichaelPBarroso)
