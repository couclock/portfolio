import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'
import BacktestList from './views/BacktestList.vue'
import BacktestSettings from './views/BacktestSettings.vue'
import BacktestDetail from './views/BacktestDetail.vue'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/backtest-list',
      name: 'backtest-list',
      component: BacktestList
    },
    {
      path: '/backtest-settings/:id',
      name: 'backtest-settings-update',
      component: BacktestSettings
    },
    {
      path: '/backtest-settings',
      name: 'backtest-settings-new',
      component: BacktestSettings
    },
    {
      path: '/backtest-detail/:id',
      name: 'backtest-detail',
      component: BacktestDetail
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "about" */ './views/About.vue')
    }
  ]
})