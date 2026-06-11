# BTP-IS AlertService

Spring Boot background application that consolidated BTP MPL Messages for alert recipients and error event on a predefined schedule.

## Prerequisites

- Java 17
- Maven 3.9+
- Docker
- kubectl (for Kubernetes deployment)

## Run locally

```bash
mvn spring-boot:run
```

Set environment variables (or export from a local `.env`) for BTP IS API (CPI) and Event Mesh credentials. See [Environment variables](#environment-variables).

## Run locally with sample data

The `sample` profile uses `sample_input.json` for MPL data, but still calls live CPI Partner Directory APIs for Pid and ErrorEvent resolution. Export CPI OAuth credentials first (see `.env.example`), then:

```bash
export CPI_TOKEN_URL='..'
export CPI_CLIENT_ID='..'
export CPI_CLIENT_SECRET='..'
export EM_BASE_URL='..'
export EM_TOPIC='..'
export EM_PUBLISH_PATH='/messagingrest/v1/topics/{topic}/messages'
export EM_TOKEN_URL='..'
export EM_CLIENT_ID='..'
export EM_CLIENT_SECRET='..'
mvn spring-boot:run -Dspring-boot.run.profiles=sample
```

Or use the helper script (loads `.env` + custom truststore for CPI SSL):

```bash
cp .env.example .env   # fill in CPI_CLIENT_ID and CPI_CLIENT_SECRET
./run-sample.sh
```

The app runs in the foreground with no HTTP server. It executes the alert job on the configured cron schedule (default: every 15 minutes).

## Environment variables

Note: look for "required" variables to config as it's required.


| Variable                          | Description                                    | Default                                     |
| --------------------------------- | ---------------------------------------------- | ------------------------------------------- |
| `ALERT_SCHEDULE_CRON`             | Cron expression for the alert job              | `0 */15 * * * *`                            |
| `CPI_BASE_URL`                    | CPI tenant base URL                            | (required)                                  |
| `CPI_GLOBAL_PID`                  | Partner Directory PID for global config        | `Global_Alert`                              |
| `CPI_SENDER_INTERFACE_SCHEME_HEX` | Hex scheme for sender interface lookup         | `53656e646572...`                           |
| `CPI_TOKEN_URL`                   | CPI OAuth token URL                            | (required)                                  |
| `CPI_CLIENT_ID`                   | CPI OAuth client ID                            | (required)                                  |
| `CPI_CLIENT_SECRET`               | CPI OAuth client secret                        | (required)                                  |
| `CPI_MPL_FILTER`                  | OData `$filter` for MPL query                  | `Status eq 'ESCALATED'`                     |
| `CPI_MPL_SELECT`                  | OData `$select` fields                         | see `application.yml`                       |
| `CPI_MPL_LOOKBACK_MINUTES`        | First-run watermark lookback                   | `15`                                        |
| `CPI_MPL_WATERMARK_PARAMETER_ID`  | Partner Directory watermark param ID           | `AlertWatermark`                            |
| `EM_BASE_URL`                     | Event Mesh base URL                            | (required)                                  |
| `EM_TOPIC`                        | Event Mesh topic (URL-encoded if needed)       | (required)                                  |
| `EM_PUBLISH_PATH`                 | Event Mesh publish path template               | `/messagingrest/v1/topics/{topic}/messages` |
| `EM_FAIL_ON_ERROR`                | Fail job when Event Mesh publish fails         | `true`                                      |
| `EM_TOKEN_URL`                    | Event Mesh OAuth token URL                     | (required)                                  |
| `EM_CLIENT_ID`                    | Event Mesh OAuth client ID                     | (required)                                  |
| `EM_CLIENT_SECRET`                | Event Mesh OAuth client secret                 | (required)                                  |
| `LOG_LEVEL_ALERT_SERVICE`         | Log level for `com.quachtd.btp.is.alertservice`        | `INFO`                                      |
| `LOG_LEVEL_SPRING_WEB_CLIENT`     | Log level for `org.springframework.web.client` | `INFO`                                      |
| `JAVA_OPTS`                       | JVM options (truststore for CPI SSL)           | —                                           |
| `TRUSTSTORE_PASSWORD`             | Truststore password (used by `docker-run.sh`)  | `changeit`                                  |


Copy `.env.example` to `.env` and fill in values. Use single quotes for values containing `!` or `$`.

## Build JAR

```bash
mvn clean package
```

## Build container image

For a Apple Silicon. if you see `no match for platform in manifest`, use --platform linux/amd64:

```bash
docker build -t <dockerhub-user>/btp-is-alert-service:<tag> .
```

Or manually (note: `amd64`, not `adm64`):

```bash
docker build -t <dockerhub-user>/btp-is-alert-service:<tag> . --platform linux/amd64
```

## Run container locally

Prepare truststore (if CPI SSL requires custom CAs):

```bash
./certs/import-certs.sh
```

Copy and edit environment file:

```bash
cp .env.example .env
# edit .env — set DOCKER_IMAGE, CPI_*, EM_*, etc.
```

Run:

```bash
docker run --rm \
  --env-file .env \
  -e JAVA_OPTS="-Djavax.net.ssl.trustStore=/app/certs/custom-cacerts.jks -Djavax.net.ssl.trustStorePassword=changeit" \
  -v "$PWD/certs/custom-cacerts.jks:/app/certs/custom-cacerts.jks:ro" \
  <dockerhub-user>/btp-is-alert-service:latest
```

## Push to Docker Hub

```bash
docker login

docker build -t <dockerhub-user>/btp-is-alert-service:<tag> . --platform linux/adm64
docker tag <dockerhub-user>/btp-is-alert-service:<tag> <dockerhub-user>/btp-is-alert-service:latest

docker push <dockerhub-user>/btp-is-alert-service:<tag>
docker push <dockerhub-user>/btp-is-alert-service:latest
```

Replace `<dockerhub-user>` and `<tag>` with your Docker Hub username and image tag.

## Deploy to Kubernetes

Apply in order:

1. **Prepare**: Prepare the info to access BTP tenant

```bash
# create namespace
kubectl create namespace dev-service
# copy your deployment files from template
cp k8s/secret.yaml.example k8s/secret.yaml
cp k8s/configmap.yaml.example k8s/configmap.yaml
```

1. **Secrets** — OAuth credentials and truststore:

```bash
kubectl apply -f k8s/secret.yaml

kubectl create secret generic btp-is-alert-service-truststore \
  --from-file=custom-cacerts.jks=./certs/custom-cacerts.jks
```

1. **Deployment** — set the image in [k8s/deployment.yaml](k8s/deployment.yaml), then apply:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl get pods -l app=btp-is-alert-service
```

For local clusters (minikube/kind) using a local image instead of Docker Hub:

```bash
# minikube
minikube image load <dockerhub-user>/btp-is-alert-service:latest

# kind
kind load docker-image <dockerhub-user>/btp-is-alert-service:latest
```

## INPUT

Query ESCLATED messages from BTP Integration Suite MPL

## OUTPUT

Consolicated json payload of alert recipient and error type (in this concept, I called it ErrorEvent). This payload will be sent to the configured Event Mesh Topic/Queue.

With this output data, you have a fully custom solutions for alert:

- Use Alert Notification service: subscribed to this Event Mesh queue in Integration Suite and map this payload to the desired structure payload of Alert Notification for any alert action. Such as Email alert, Teams alert... 
- SMTP (Email) adapter: subscribed to this Event Mesh queue in Integration Suite, and build the expect email content.
- Webhook..

Output payload sample from the job:

```bash
{
    "entries": [
        {
            "recipient": "rep1",
            "type": "SINGLE",
            "errorNumber": 2,
            "value": "IF1_ErrorEvent1"
        },
        {
            "recipient": "BTPSupport",
            "type": "GROUP",
            "errorNumber": 6,
            "value": [
                "IF1",
                "IF2"
            ]
        },
        {
            "recipient": "rep2",
            "type": "SINGLE",
            "errorNumber": 1,
            "value": "IF1_ErrorEvent2"
        },
        {
            "recipient": "rep3",
            "type": "SINGLE",
            "errorNumber": 1,
            "value": "IF1_ErrorEvent3"
        },
        {
            "recipient": "rep4",
            "type": "SINGLE",
            "errorNumber": 2,
            "value": "IF2_ErrorEvent1"
        }
    ]
}
```

## Local Test

```bash
mvn test
```

