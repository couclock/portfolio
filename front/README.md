# front

## Project setup

```
npm install
```

### Compiles and hot-reloads for development

```
npm run serve
```

Consider .env.development file

### Compiles and minifies for production

```
npm run build
```

Consider .env.production file

### Dependencies

Embedded Vue Material is not working right with table selection.
TODO :

```
cd front
git clone https://github.com/vuematerial/vue-material.git
git checkout dev
yarn build
cp -r dist/* ../node-modules/vue-material/
```
