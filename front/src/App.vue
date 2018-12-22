<template>
  <div id="app">
    <md-toolbar class="md-primary"
                md-elevation="1">
      <h3 class="md-title"
          style="flex: 1">Portfolio management</h3>
      <router-link to="/">
        <md-button>PF</md-button>
      </router-link>
      <router-link to="/portfolios">
        <md-button>Portfolios</md-button>
      </router-link>
      <router-link to="/stocks">
        <md-button>Stocks</md-button>
      </router-link>
    </md-toolbar>

    <router-view />
  </div>
</template>

<style lang="scss">
#app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
#nav {
  padding: 30px;
  a {
    font-weight: bold;
    color: #2c3e50;
    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
</style>
