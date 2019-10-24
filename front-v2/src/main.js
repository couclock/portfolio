import Vue from 'vue'
import './plugins/axios'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import router from './router'
import VueLodash from 'vue-lodash'
import Highcharts from 'highcharts'
import HighchartsVue from 'highcharts-vue'
import stockInit from 'highcharts/modules/stock'

Vue.config.productionTip = false

Vue.use(VueLodash, { name: 'lodash' }) // name is used to call lodash inside components

Vue.use(HighchartsVue) // activate highcharts plugin
stockInit(Highcharts)

new Vue({
  vuetify,
  router,
  render: h => h(App)
}).$mount('#app')
