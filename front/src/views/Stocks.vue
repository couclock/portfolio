<template>
  <div class="md-layout md-gutter">

    <div class="md-layout md-gutter md-layout-item md-size-100"
         v-if="stockList"
         v-for="stock in stockList"
         :key="stock.id">
      <div class="md-layout-item md-size-100">

        {{ stock.code }}
        <span v-if="lastHistory[stock.code]">({{ lastHistory[stock.code].date }})</span>
      </div>

    </div>
    <div class="md-layout md-alignment-top-center">
      <md-button class="md-raised md-primary"
                 @click="updateStocks">Update stocks</md-button>
    </div>
    <!--
    <div class="md-layout md-gutter md-layout-item">
      {{ lastHistory }}
    </div>
-->
  </div>

</template>

<script>
import { HTTP } from "@/http-constants";
import Vue from "vue";

export default {
  name: "stocks",
  data() {
    return {
      stockList: [],
      lastHistory: {}
    };
  },

  methods: {
    loadStockList() {
      HTTP.get("/stocks/").then(response => {
        this.stockList = response.data;
        console.log("lastHistory : ", this.loadLastHistory());
      });
    },
    updateStocks() {
      HTTP.post("/stocks/update").then(() => {
        this.loadLastHistory();
      });
    },
    loadLastHistory() {
      for (var i = 0; i < this.stockList.length; i++) {
        let stockCode = this.stockList[i].code;
        HTTP.get("/stocks/" + stockCode + "/history/last").then(response => {
          Vue.set(this.lastHistory, stockCode, response.data);
        });
      }
      return this.lastHistory;
    }
  },
  created() {
    this.loadStockList();
  },
  mounted() {},
  components: {}
};
</script>

<style scoped>
</style>
