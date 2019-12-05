helm dependency update ./cellbase

pass=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 13 ; echo '')

helm upgrade cellbase-sharded  ./cellbase \
    --set deployMongoSharded=true \
    --set settings.CELLBASE_DATABASES_MONGODB_HOST=cellbase-mongo-mongodb-sharded.default.svc.cluster.local \
    --set mongodb.auth.adminUser=admin \
    --set mongodb.auth.adminPassword=$pass \
    --install