<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-row>
    <v-col cols="12">
      <BacktestResults v-if="backtest" :backtest="backtest"></BacktestResults>

      <v-row>
        <v-col cols="4">
          <BacktestMetadata v-if="backtest" :backtest="backtest"></BacktestMetadata>
        </v-col>

        <v-col cols="8">
          <v-card>
            <v-card-title>Courbe</v-card-title>
            <v-card-text>
              <highcharts :constructor-type="'stockChart'" :options="chartOptions"></highcharts>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-col cols="6">
          <BacktestTransactions v-if="backtest" :backtest="backtest"></BacktestTransactions>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import BacktestMetadata from "@/components/BacktestMetadata";
import BacktestResults from "@/components/BacktestResults";
import BacktestTransactions from "@/components/BacktestTransactions";

import { parseISO, getTime } from "date-fns";

import { Chart } from "highcharts-vue";

export default {
  name: "backtestDetail",
  data() {
    return {
      backtest: null,
      chartOptions: {
        series: [
          {
            name: "backtest",
            data: [],
            lineWidth: 2,
            color: "red"
          },
          {
            lineWidth: 1,
            data: []
          },
          {
            lineWidth: 1,
            data: []
          },
          {
            lineWidth: 1,
            data: []
          }
        ]
      }
    };
  },
  computed: {},
  methods: {
    getStockHistory(stockCode, startMoney, startDate, graphIdx) {
      this.axios
        .get(
          "/stocks/" +
            stockCode +
            "/graph-history/" +
            startMoney +
            "/" +
            startDate
        )
        .then(response => {
          this.chartOptions.series[graphIdx].name = stockCode;
          this.chartOptions.series[graphIdx].data = response.data;
        })
        .catch(err => {
          // eslint-disable-next-line
          console.log(err);
        })
        .finally(() => {});
    }
  },
  mounted() {
    this.axios
      .get("/backtests/" + this.$route.params.id)
      .then(response => {
        // eslint-disable-next-line
        console.log("ok : ", response);
        this.backtest = response.data;
        this.getStockHistory(
          this.backtest.strategyParameters.usStock,
          this.backtest.startMoney,
          this.backtest.startDate,
          1
        );
        this.getStockHistory(
          this.backtest.strategyParameters.exUsStock,
          this.backtest.startMoney,
          this.backtest.startDate,
          2
        );
        this.getStockHistory(
          this.backtest.strategyParameters.bondStock,
          this.backtest.startMoney,
          this.backtest.startDate,
          3
        );
      })
      .catch(err => {
        // eslint-disable-next-line
        console.log(err);
      })
      .finally(() => {});
    this.axios
      .get("/backtests/" + this.$route.params.id + "/history")
      .then(response => {
        // eslint-disable-next-line
        console.log("ok : ", response);
        this.chartOptions.series[0].data = response.data;
      })
      .catch(err => {
        // eslint-disable-next-line
        console.log(err);
      })
      .finally(() => {});
  },
  created() {},
  components: {
    BacktestMetadata,
    BacktestResults,
    BacktestTransactions,
    highcharts: Chart
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>