import Vue from 'vue'
import './plugins/axios'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import router from './router'
import VueLodash from 'vue-lodash'

Vue.config.productionTip = false

const options = { name: 'lodash' } // customize the way you want to call it
Vue.use(VueLodash, options) // options is optional

new Vue({
  vuetify,
  router,
  render: h => h(App)
}).$mount('#app')
