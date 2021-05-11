{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "cellbase.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "cellbase.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "cellbase.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "cellbase.labels" -}}
helm.sh/chart: {{ include "cellbase.chart" . }}
{{ include "cellbase.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "cellbase.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cellbase.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "cellbase.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
    {{ default (include "cellbase.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}


{{- define "configMap.name" -}}
{{ include "cellbase.fullname" . }}-conf
{{- end -}}

{{- define "cellbase.secretName" -}}
{{- if .Values.cellbase.secretName -}}
{{ .Values.cellbase.secretName }}
{{- else -}}
{{ include "cellbase.fullname" . }}-secret
{{- end -}}
{{- end -}}

{{- define "clusterRoleBinding.name" -}}
{{ include "cellbase.fullname" . }}-fabric8-rbac
{{- end -}}




{{- define "mongodbHosts" -}}
{{- if .Values.mongodb.deploy.enabled -}}
    {{- $name := .Values.mongodb.deploy.name -}}
    {{- $namespace := .Release.Namespace -}}
    {{- range $i := until ( int .Values.mongodb.deploy.replicas ) }}
        {{- if ne $i 0 -}}
          ,
        {{- end -}}
        {{- cat $name "-" $i "." $name "-svc." $namespace ":27017" | replace " " "" -}}
    {{- end -}}
{{- else -}}
    {{- .Values.mongodb.external.hosts }}
{{- end -}}
{{- end -}}




{{- define "pvResources" -}}
pv-{{ include "cellbase.fullname" . }}-conf
{{- end -}}


{{- define "pvcStorageClassName" -}}
{{- if .Values.azureStorageAccount.enabled -}}
azurefile
{{- end -}}
{{- end -}}

{{- define "pvcResources" -}}
pvc-{{ include "cellbase.fullname" . }}-conf
{{- end -}}