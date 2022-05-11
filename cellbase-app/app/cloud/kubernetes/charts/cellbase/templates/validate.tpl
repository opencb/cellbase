

{{ if eq .Chart.AppVersion "REPLACEME_OPENCGA_VERSION" }}
    {{ fail "Wrong Chart.AppVersion. Attempting to execute HELM from cellbase/cellbase-app/app/cloud/kubernetes"  }}
{{ end }}

{{ if and (not .Values.mongodb.deploy.enabled) (eq .Values.mongodb.replicaSet "") }}
    {{ fail "Make sure you have completed Values.mongodb.replicaSet if Values.mongodb.deploy.enable is false"  }}
{{ end }}