# portfolio

## Build Backend docker image

```
mvn compile jib:dockerBuild -Pdocker
```

## Build Frontend docker image

```
pushd front && npm run build && popd
docker-compose build
```

## Run all

```
docker-compose up

```

OR

```
docker-compose create && docker-compose start

```
