#!/bin/bash

set -e

function printUsage() {
  echo ""
  echo "Deploy required Helm charts for a fully working Cellbase installation"
  echo " - mongodb-operator"
  echo " - cellbase-nginx"
  echo " - cellbase"
  echo ""
  echo "Usage:   $(basename $0) --context <context> [options]"
  echo ""
  echo "Options:"
  echo "   * -c     --context             STRING     Kubernetes context"
  echo "     -n     --namespace           STRING     Kubernetes namespace"
  echo "   * -f     --values              FILE       Helm values file"
  echo "     -o     --outdir              DIRECTORY  Output directory where to write the generated manifests. Default: \$PWD"
  echo "            --name-suffix         STRING     Helm deployment name suffix. e.g. '-test' : cellbase-nginx-test, cellbase-test..."
  echo "            --what                STRING     What to deploy. [cellbase-nginx, cellbase, all]. Default: all"
  echo "            --cellbase-conf-dir   DIRECTORY  Cellbase configuration folder. Default: build/conf/ "
  echo "            --keep-tmp-files      FLAG       Do not remove any temporary file generated in the outdir"
  echo "            --dry-run             FLAG       Simulate an installation."
  echo "     -h     --help                FLAG       Print this help"
  echo "            --verbose             FLAG       Verbose mode. Print debugging messages about the progress."
  echo ""
}

function requiredParam() {
  key=$1
  value=$2
  if [ -z "${value}" ]; then
    echo "Missing param $key"
    printUsage
    exit 1
  fi
}

function requiredFile() {
  key=$1
  file=$2
  if [ ! -f "${file}" ]; then
    echo "Missing file ${key} : '${file}' : No such file"
    printUsage
    exit 1
  fi
}

function requiredDirectory() {
  key=$1
  dir=$2
  if [ ! -d "${dir}" ]; then
    echo "Missing directory ${key} : '${dir}' : No such directory"
    printUsage
    exit 1
  fi
}

#K8S_CONTEXT
#K8S_NAMESPACE
#HELM_VALUES_FILE
WHAT=ALL
DRY_RUN=false
#NAME_SUFFIX
HELM_OPTS="${HELM_OPTS} --debug "
OUTPUT_DIR="$(pwd)"
#CELLBASE_CONF_DIR
KEEP_TMP_FILES=false

while [[ $# -gt 0 ]]; do
  key="$1"
  value="$2"
  case $key in
  -h | --help)
    printUsage
    exit 0
    ;;
  -c | --context)
    K8S_CONTEXT="$value"
    shift # past argument
    shift # past value
    ;;
  -n | --namespace)
    K8S_NAMESPACE="$value"
    shift # past argument
    shift # past value
    ;;
  -f | --values)
    HELM_VALUES_FILE="$value"
    shift # past argument
    shift # past value
    ;;
  -o | --outdir)
    OUTPUT_DIR="$value"
    shift # past argument
    shift # past value
    ;;
  --what)
    WHAT="${value^^}" # Upper case
    shift             # past argument
    shift             # past value
    ;;
  --name-suffix)
    NAME_SUFFIX="${value}"
    shift # past argument
    shift # past value
    ;;
  --cellbase-conf-dir)
    CELLBASE_CONF_DIR="${value}"
    shift # past argument
    shift # past value
    ;;
  --keep-tmp-files)
    KEEP_TMP_FILES=true
    shift # past argument
    ;;
  --verbose)
    set -x
    shift # past argument
    ;;
  --dry-run)
    DRY_RUN=true
    HELM_OPTS="${HELM_OPTS} --dry-run "
    shift # past argument
    ;;
  *) # unknown option
    echo "Unknown option $key"
    printUsage
    exit 1
    ;;
  esac
done

K8S_NAMESPACE="${K8S_NAMESPACE:-$K8S_CONTEXT}"
requiredParam "--context" "${K8S_CONTEXT}"
requiredParam "--values" "${HELM_VALUES_FILE}"
requiredDirectory "--outdir" "${OUTPUT_DIR}"

OUTPUT_DIR=$(realpath "${OUTPUT_DIR}")
if [ -n "${CELLBASE_CONF_DIR}" ]; then
  requiredDirectory "--cellbase-conf-dir" "${CELLBASE_CONF_DIR}"
  CELLBASE_CONF_DIR=$(realpath "${CELLBASE_CONF_DIR}")
else
  CELLBASE_CONF_DIR=$(realpath "$(dirname "$0")/../../conf")
fi

for f in $(echo "${HELM_VALUES_FILE}" | tr "," "\n"); do
  requiredFile "--values" "${f}"
  if [ -z "$REAL_HELM_VALUES_FILE" ]; then
    REAL_HELM_VALUES_FILE=$(realpath "${f}")
  else
    REAL_HELM_VALUES_FILE="${REAL_HELM_VALUES_FILE},$(realpath "${f}")"
  fi
done

HELM_VALUES_FILE="${REAL_HELM_VALUES_FILE}"

# Don't move the PWD until we found out the realpath. It could be a relative path.
cd "$(dirname "$0")"

function configureContext() {
  kubectl config use-context "$K8S_CONTEXT"

  # Create a namespace for cellbase
  if ! kubectl get namespace "${K8S_NAMESPACE}" --context "$K8S_CONTEXT"; then
    kubectl create namespace "${K8S_NAMESPACE}" --context "$K8S_CONTEXT"
  fi

  kubectl config set-context "${K8S_CONTEXT}" --namespace="${K8S_NAMESPACE}"
}

function deployNginx() {
  # Use Helm to deploy an NGINX ingress controller
  ## Deploy in the same namespace
  ## https://docs.nginx.com/nginx-ingress-controller/installation/installation-with-helm/

#  helm repo add stable https://kubernetes-charts.storage.googleapis.com/
#  helm repo add nginx-stable https://helm.nginx.com/stable
  helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
  helm repo update

  NAME="cellbase-nginx${NAME_SUFFIX}"
  echo "# Deploy NGINX ${NAME}"
  helm upgrade ${NAME} ingress-nginx/ingress-nginx \
    --kube-context "${K8S_CONTEXT}" --namespace "${K8S_NAMESPACE}"  \
    -f charts/nginx/values.yaml \
    --values "${HELM_VALUES_FILE}" \
    --install --wait --timeout 10m ${HELM_OPTS}
}

function deployMongodbOperator() {
  NAME="mongodb-operator${NAME_SUFFIX}"
  DATE=$(date "+%Y%m%d%H%M%S")
  ./charts/mongodb-operator/fetch-mongodb-operator-files.sh

  helm upgrade "${NAME}" charts/mongodb-operator \
    --values "${HELM_VALUES_FILE}" \
    --install --wait --kube-context "${K8S_CONTEXT}" -n "${K8S_NAMESPACE}" --timeout 10m ${HELM_OPTS}
  if [ $DRY_RUN == "false" ]; then
    helm get manifest "${NAME}" --kube-context "${K8S_CONTEXT}" -n "${K8S_NAMESPACE}" >"${OUTPUT_DIR}/helm-${NAME}-manifest-${DATE}.yaml"
  fi
}

function deployCellbase() {
  DATE=$(date "+%Y%m%d%H%M%S")
  if [[ -n "$CELLBASE_CONF_DIR" ]]; then
    NAME="cellbase${NAME_SUFFIX}"
    echo "# Deploy Cellbase ${NAME}"
    CELLBASE_CHART="${OUTPUT_DIR:?}/${NAME}_${DATE}_tmp/"
    if [ $KEEP_TMP_FILES == "false" ]; then
      trap 'rm -rf "${CELLBASE_CHART:?}"' EXIT
    fi

    mkdir "$CELLBASE_CHART"
    mkdir "$CELLBASE_CHART"/conf
    cp -r charts/cellbase/* "${CELLBASE_CHART:?}"
    cp -r "${CELLBASE_CONF_DIR:?}"/*.yml "$CELLBASE_CHART"/conf
    cp -r "${CELLBASE_CONF_DIR:?}"/log4j2.*.xml "$CELLBASE_CHART"/conf
  else
    CELLBASE_CHART=charts/cellbase/
  fi

  helm upgrade "${NAME}" "${CELLBASE_CHART}" \
    --values "${HELM_VALUES_FILE}" \
    --set "kubeContext=${K8S_CONTEXT}" \
    --install --wait --kube-context "${K8S_CONTEXT}" -n "${K8S_NAMESPACE}" --timeout 10m ${HELM_OPTS}
  if [ $DRY_RUN == "false" ]; then
    helm get manifest "${NAME}" --kube-context "${K8S_CONTEXT}" -n "${K8S_NAMESPACE}" >"${OUTPUT_DIR}/helm-${NAME}-manifest-${DATE}.yaml"
  fi
}

echo "# Deploy kubernetes"
echo "# Configuring context $K8S_CONTEXT"
configureContext

if [[ "$WHAT" == "NGINX" || "$WHAT" == "ALL" ]]; then
  deployNginx
fi

if [[ "$WHAT" == "MONGODBOPERATOR" || "$WHAT" == "ALL" ]]; then
  deployMongodbOperator
fi

if [[ "$WHAT" == "CELLBASE" || "$WHAT" == "ALL" ]]; then
  deployCellbase
fi
