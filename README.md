# portfolio

## Build Backend docker image
```
mvn compile jib:dockerBuild -Pdocker
```

## Build Frontend docker image
```
pushd front && npm build && popd
docker-compose build
```

## Run all
```
docker-compose up

```