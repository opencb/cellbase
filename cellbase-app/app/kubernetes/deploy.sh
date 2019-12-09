# Note: this is only good for first time install as password and key change each time!

pass=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 13 ; echo '')
key=$( openssl rand -base64 741)
deployment_name="cellbase"

helm dependency update ./cellbase

helm upgrade $deployment_name  ./cellbase \
    --set deployMongo=true \
    --set mongodb.replicaSet.enabled=true \
    --set mongodb.auth.enabled=true \
    --set mongodb.mongodbRootPassword="$pass" \
    --set settings.CELLBASE_DATABASES_MONGODB_HOST="$deployment_name-mongodb" \
    --set mongodb.auth.key="$key" \
    --install