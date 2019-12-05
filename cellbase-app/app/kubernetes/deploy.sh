pass=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 13 ; echo '')

helm dependency update ./cellbase

helm upgrade cellbase  ./cellbase \
    --set deployMongo=true \
    --set mongodb.auth.enabled=true \
    --set mongodb.auth.mongodbRootPassword=$pass \
    --set mongodb.auth.key="$( openssl rand -base64 741)" \
    --install