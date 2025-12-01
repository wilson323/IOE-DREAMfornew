{{/*
IOE-DREAM Helm Helper Templates
*/}}

{{/*
Expand the name of the chart.
*/}}
{{- define "ioedream.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "ioedream.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ioedream.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "ioedream.labels" -}}
helm.sh/chart: {{ include "ioedream.chart" . }}
{{ include "ioedream.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- if .Values.global.labels }}
{{ toYaml .Values.global.labels }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "ioedream.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ioedream.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Values.component }}
app.kubernetes.io/component: {{ .Values.component }}
{{- end }}
{{- if .Values.global.environment }}
app.kubernetes.io/environment: {{ .Values.global.environment }}
{{- end }}
{{- if .Values.global.project }}
app.kubernetes.io/project: {{ .Values.global.project }}
{{- end }}
{{- end }}

{{/*
Service labels
*/}}
{{- define "ioedream.serviceLabels" -}}
{{ include "ioedream.labels" . }}
app.kubernetes.io/component: service
{{- end }}

{{/*
Deployment labels
*/}}
{{- define "ioedream.deploymentLabels" -}}
{{ include "ioedream.labels" . }}
app.kubernetes.io/component: deployment
{{- end }}

{{/*
Pod labels
*/}}
{{- define "ioedream.podLabels" -}}
{{ include "ioedream.labels" . }}
app.kubernetes.io/component: pod
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "ioedream.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "ioedream.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the image name
*/}}
{{- define "ioedream.image" -}}
{{- $registry := .Values.global.imageRegistry | default "" }}
{{- $repository := .Values.image.repository }}
{{- $tag := .Values.image.tag | default .Chart.AppVersion }}
{{- if $registry }}
{{- printf "%s/%s:%s" $registry $repository $tag }}
{{- else }}
{{- printf "%s:%s" $repository $tag }}
{{- end }}
{{- end }}

{{/*
Generate the environment variables
*/}}
{{- define "ioedream.envVars" -}}
{{- range $key, $value := .Values.env }}
- name: {{ $key }}
  value: {{ $value | quote }}
{{- end }}
{{- if .Values.global.environment }}
- name: SPRING_PROFILES_ACTIVE
  value: {{ .Values.global.environment | quote }}
{{- end }}
- name: SPRING_APPLICATION_NAME
  value: {{ .Values.name | quote }}
- name: APP_VERSION
  value: {{ .Chart.AppVersion | quote }}
- name: TZ
  value: "Asia/Shanghai"
{{- end }}

{{/*
Generate the resources limits and requests
*/}}
{{- define "ioedream.resources" -}}
{{- if .Values.resources }}
resources:
  {{- if .Values.resources.requests }}
  requests:
    memory: {{ .Values.resources.requests.memory | default "256Mi" | quote }}
    cpu: {{ .Values.resources.requests.cpu | default "250m" | quote }}
  {{- end }}
  {{- if .Values.resources.limits }}
  limits:
    memory: {{ .Values.resources.limits.memory | default "1Gi" | quote }}
    cpu: {{ .Values.resources.limits.cpu | default "1000m" | quote }}
  {{- end }}
{{- else }}
resources:
  requests:
    memory: {{ .Values.global.resources.requests.memory | quote }}
    cpu: {{ .Values.global.resources.requests.cpu | quote }}
  limits:
    memory: {{ .Values.global.resources.limits.memory | quote }}
    cpu: {{ .Values.global.resources.limits.cpu | quote }}
{{- end }}
{{- end }}

{{/*
Generate the health check probes
*/}}
{{- define "ioedream.healthChecks" -}}
{{- if .Values.livenessProbe }}
livenessProbe:
  httpGet:
    path: {{ .Values.livenessProbe.httpGet.path | default "/actuator/health" }}
    port: {{ .Values.livenessProbe.httpGet.port | default "http" }}
  initialDelaySeconds: {{ .Values.livenessProbe.initialDelaySeconds | default 60 }}
  periodSeconds: {{ .Values.livenessProbe.periodSeconds | default 30 }}
  timeoutSeconds: {{ .Values.livenessProbe.timeoutSeconds | default 10 }}
  successThreshold: {{ .Values.livenessProbe.successThreshold | default 1 }}
  failureThreshold: {{ .Values.livenessProbe.failureThreshold | default 3 }}
{{- end }}
{{- if .Values.readinessProbe }}
readinessProbe:
  httpGet:
    path: {{ .Values.readinessProbe.httpGet.path | default "/actuator/health" }}
    port: {{ .Values.readinessProbe.httpGet.port | default "http" }}
  initialDelaySeconds: {{ .Values.readinessProbe.initialDelaySeconds | default 30 }}
  periodSeconds: {{ .Values.readinessProbe.periodSeconds | default 10 }}
  timeoutSeconds: {{ .Values.readinessProbe.timeoutSeconds | default 5 }}
  successThreshold: {{ .Values.readinessProbe.successThreshold | default 1 }}
  failureThreshold: {{ .Values.readinessProbe.failureThreshold | default 3 }}
{{- end }}
{{- end }}

{{/*
Generate the security context
*/}}
{{- define "ioedream.securityContext" -}}
{{- if .Values.securityContext }}
{{ toYaml .Values.securityContext }}
{{- else }}
{{- if .Values.global.securityContext }}
{{ toYaml .Values.global.securityContext }}
{{- else }}
runAsNonRoot: true
runAsUser: 1000
runAsGroup: 1000
fsGroup: 1000
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate the node selector, tolerations, and affinity
*/}}
{{- define "ioedream.scheduling" -}}
{{- if or .Values.nodeSelector .Values.tolerations .Values.affinity }}
{{- if .Values.nodeSelector }}
nodeSelector:
{{ toYaml .Values.nodeSelector | indent 2 }}
{{- end }}
{{- if .Values.tolerations }}
tolerations:
{{ toYaml .Values.tolerations | indent 2 }}
{{- end }}
{{- if .Values.affinity }}
affinity:
{{ toYaml .Values.affinity | indent 2 }}
{{- end }}
{{- else }}
{{- if .Values.global.nodeSelector }}
nodeSelector:
{{ toYaml .Values.global.nodeSelector | indent 2 }}
{{- end }}
{{- if .Values.global.tolerations }}
tolerations:
{{ toYaml .Values.global.tolerations | indent 2 }}
{{- end }}
{{- if .Values.global.affinity }}
affinity:
{{ toYaml .Values.global.affinity | indent 2 }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate the image pull secrets
*/}}
{{- define "ioedream.imagePullSecrets" -}}
{{- if .Values.global.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.global.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate the priority class name
*/}}
{{- define "ioedream.priorityClassName" -}}
{{- if .Values.priorityClassName }}
priorityClassName: {{ .Values.priorityClassName | quote }}
{{- end }}
{{- end }}

{{/*
Generate extra environment variables
*/}}
{{- define "ioedream.extraEnv" -}}
{{- if .Values.extraEnv }}
{{- range .Values.extraEnv }}
- name: {{ .name }}
{{- if .value }}
  value: {{ .value | quote }}
{{- else if .valueFrom }}
  valueFrom:
{{ toYaml .valueFrom | indent 4 }}
{{- end }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate extra volumes
*/}}
{{- define "ioedream.extraVolumes" -}}
{{- if .Values.extraVolumes }}
{{- range .Values.extraVolumes }}
- name: {{ .name }}
{{ toYaml . | indent 2 }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate extra volume mounts
*/}}
{{- define "ioedream.extraVolumeMounts" -}}
{{- if .Values.extraVolumeMounts }}
{{- range .Values.extraVolumeMounts }}
- name: {{ .name }}
  mountPath: {{ .mountPath }}
{{- if .readOnly }}
  readOnly: {{ .readOnly }}
{{- end }}
{{- if .subPath }}
  subPath: {{ .subPath }}
{{- end }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Generate the common annotations
*/}}
{{- define "ioedream.annotations" -}}
{{- if .Values.global.annotations }}
{{ toYaml .Values.global.annotations }}
{{- end }}
{{- if .Values.commonAnnotations }}
{{ toYaml .Values.commonAnnotations }}
{{- end }}
{{- if .Values.annotations }}
{{ toYaml .Values.annotations }}
{{- end }}
{{- end }}

{{/*
Generate the common labels
*/}}
{{- define "ioedream.commonLabels" -}}
{{- if .Values.global.labels }}
{{ toYaml .Values.global.labels }}
{{- end }}
{{- if .Values.commonLabels }}
{{ toYaml .Values.commonLabels }}
{{- end }}
{{- end }}

{{/*
Check if the service is enabled
*/}}
{{- define "ioedream.serviceEnabled" -}}
{{- if hasKey . "enabled" }}
{{- .enabled }}
{{- else }}
{{- true }}
{{- end }}
{{- end }}

{{/*
Generate the service ports
*/}}
{{- define "ioedream.servicePorts" -}}
{{- range $name, $port := .Values.ports }}
- name: {{ $name }}
  port: {{ $port }}
  targetPort: {{ $port }}
  protocol: TCP
{{- end }}
{{- end }}

{{/*
Generate the container ports
*/}}
{{- define "ioedream.containerPorts" -}}
{{- range $name, $port := .Values.ports }}
- name: {{ $name }}
  containerPort: {{ $port }}
  protocol: TCP
{{- end }}
{{- end }}

{{/*
Generate the database connection URL
*/}}
{{- define "ioedream.databaseURL" -}}
{{- $host := .Values.mysql.primary.service.host | default "mysql" }}
{{- $port := .Values.mysql.primary.service.ports.mysql | default 3306 }}
{{- $database := .Values.mysql.auth.database | default "ioedream_prod" }}
jdbc:mysql://{{ $host }}:{{ $port }}/{{ $database }}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
{{- end }}

{{/*
Generate the Redis connection URL
*/}}
{{- define "ioedream.redisURL" -}}
{{- $host := .Values.redis.master.service.host | default "redis" }}
{{- $port := .Values.redis.master.service.ports.redis | default 6379 }}
{{- $password := .Values.redis.auth.password }}
redis://{{ if $password }}:{{ $password }}@{{ end }}{{ $host }}:{{ $port }}
{{- end }}

{{/*
Generate the Nacos server address
*/}}
{{- define "ioedream.nacosServerAddr" -}}
{{- $host := .Values.nacos.service.host | default "nacos" }}
{{- $port := .Values.nacos.service.ports.server | default 8848 }}
{{ $host }}:{{ $port }}
{{- end }}

{{/*
Generate the gateway ingress rules
*/}}
{{- define "ioedream.gatewayIngressRules" -}}
{{- range $host, $paths := .Values.ingress.hosts }}
- host: {{ $host }}
  http:
    paths:
    {{- range $paths }}
    - path: {{ .path }}
      pathType: {{ .pathType | default "Prefix" }}
      backend:
        service:
          name: {{ .service }}
          port:
            number: {{ .port }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
Generate the autoscaling configuration
*/}}
{{- define "ioedream.autoscaling" -}}
{{- if .Values.autoscaling.enabled }}
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {{ include "ioedream.fullname" . }}-hpa
  namespace: {{ .Release.Namespace }}
  labels:
    {{- include "ioedream.labels" . | nindent 4 }}
    app.kubernetes.io/component: hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {{ include "ioedream.fullname" . }}
  minReplicas: {{ .Values.autoscaling.minReplicas | default 2 }}
  maxReplicas: {{ .Values.autoscaling.maxReplicas | default 10 }}
  metrics:
    {{- if .Values.autoscaling.targetCPUUtilizationPercentage }}
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: {{ .Values.autoscaling.targetCPUUtilizationPercentage }}
    {{- end }}
    {{- if .Values.autoscaling.targetMemoryUtilizationPercentage }}
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: {{ .Values.autoscaling.targetMemoryUtilizationPercentage }}
    {{- end }}
  {{- if .Values.autoscaling.behavior }}
  behavior:
{{ toYaml .Values.autoscaling.behavior | indent 4 }}
  {{- end }}
{{- end }}
{{- end }}