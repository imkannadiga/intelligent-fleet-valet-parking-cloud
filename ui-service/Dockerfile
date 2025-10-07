# syntax=docker/dockerfile:1
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Environment config for OTel
ENV PYTHONUNBUFFERED=1
ENV OTEL_SERVICE_NAME="ui-service"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://otel-collector-agent.monitoring.svc.cluster.local:4317"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="otlp"
ENV OTEL_RESOURCE_ATTRIBUTES="service.namespace=valetparking"

# Run with OTel auto-instrumentation
CMD ["opentelemetry-instrument", "python", "valet_tester.py"]
