

{{ if eq .Chart.AppVersion "REPLACEME_OPENCGA_VERSION" }}
    {{ fail "Wrong Chart.AppVersion. Attempting to execute HELM from cellbase/cellbase-app/app/cloud/kubernetes"  }}
{{ end }}

{{ if .Values.mongodb.deploy.enabled }}
    {{ if ne .Values.mongodb.replicaSet .Values.mongodb.deploy.name}}
        {{ fail "Wrong Values.mongodb.replicaSet is diferent from Values.mongodb.deploy.name"  }}
    {{ end }}
{{ end }}