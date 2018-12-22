import Vue from 'vue';
import Router from 'vue-router';
import Stocks from '@/views/Stocks.vue';
import Portfolios from '@/views/Portfolios.vue';
import PortfolioDetail from '@/views/PortfolioDetail.vue';

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/stocks',
      name: 'stocks',
      component: Stocks
    },
    {
      path: '/portfolios',
      name: 'portfolios',
      component: Portfolios
    },
    {
      path: '/portfolios/:strategyCode',
      name: 'portfolioDetail',
      component: PortfolioDetail
    },

    { path: '*', redirect: '/portfolios' }
  ]
});
