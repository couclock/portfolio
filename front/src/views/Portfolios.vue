<template>
  <div class="md-layout md-gutter md-alignment-top-center">

    <div class="md-layout md-layout-item md-size-50 md-alignment-top-center">

      <md-table v-model="portfolioList"
                v-if="portfolioList.length > 0"
                md-sort="strategyCode"
                md-sort-order="asc"
                md-card>
        <md-table-toolbar>
          <h1 class="md-title">Portfolios</h1>
        </md-table-toolbar>

        <md-table-row slot="md-table-row"
                      slot-scope="{ item }">
          <md-table-cell md-label="ID"
                         md-numeric>{{ item.id }}</md-table-cell>
          <md-table-cell md-label="Code"
                         md-sort-by="strategyCode">
            <router-link :to="{ name: 'portfolioDetail', params: { strategyCode: item.strategyCode }}">
              {{ item.strategyCode }}
            </router-link>
          </md-table-cell>
          <md-table-cell md-label="Start date"
                         md-sort-by="startDate">{{ item.startDate }}</md-table-cell>
          <md-table-cell md-label="End date"
                         md-sort-by="endDate">{{ item.endDate }}</md-table-cell>
          <md-table-cell md-label="CAGR"
                         md-sort-by="cagr">{{ 100 * item.cagr | formatNb }} %</md-table-cell>
          <md-table-cell md-label="Actions">
            <md-button class="md-icon-button md-dense md-raised"
                       @click="processStrategy(item.strategyCode)">
              <md-icon>autorenew</md-icon>
            </md-button>
            <md-button class="md-icon-button md-dense md-raised md-primary"
                       @click="resetStrategy(item.strategyCode)">
              <md-icon>settings_backup_restore</md-icon>
            </md-button>
            <md-button class="md-icon-button md-dense md-accent md-raised"
                       @click="deleteStrategy(item.strategyCode)">
              <md-icon>delete</md-icon>
            </md-button>
          </md-table-cell>
        </md-table-row>
      </md-table>
    </div>
    <div class="md-layout md-layout-item md-size-100">

      <md-divider class="md-inset"></md-divider>
    </div>

    <!-- Action line -->
    <div class="md-layout md-gutter md-layout-item md-alignment-top-center">
      <div class="md-layout-item md-size-50">

        <md-card>
          <md-card-header>
            <div class="md-title">Add a portfolio</div>
          </md-card-header>
          <md-card-content>

            <div class="md-layout">
              <md-field class="md-layout-item">
                <label>New strategy name</label>
                <md-input v-model="newStrategyName"></md-input>
              </md-field>
              <div class="md-layout-item md-size-5">
              </div>
              <md-field  class="md-layout-item">
                <label for="usStock">US stock</label>
                <md-select v-model="usStockCode"
                           md-dense
                           name="usStock"
                           id="usStock">
                  <md-option v-for="stock in stockList"
                             :key="stock.id"
                             :value="stock.code">{{ stock.code}}</md-option>
                </md-select>
              </md-field>

            </div>

            <div class="md-layout">

              <md-field  class="md-layout-item">
                <label for="exUsStock">Ex-US stock</label>
                <md-select v-model="exUsStockCode"
                           md-dense
                           name="exUsStock"
                           id="exUsStock">
                  <md-option v-for="stock in stockList"
                             :key="stock.id"
                             :value="stock.code">{{ stock.code}}</md-option>
                </md-select>
              </md-field>
              <div class="md-layout-item md-size-5">
              </div>
              <md-field  class="md-layout-item">
                <label for="bondStock">Bond stock</label>
                <md-select v-model="bondStockCode"
                           md-dense
                           name="bondStock"
                           id="bondStock">
                  <md-option v-for="stock in stockList"
                             :key="stock.id"
                             :value="stock.code">{{ stock.code}}</md-option>
                </md-select>
              </md-field>
            </div>
            <div class="md-layout md-alignment-top-center">
              <md-button class="md-raised md-primary"
                         @click="addStrategy">Add portfolio</md-button>
            </div>

          </md-card-content>
        </md-card>
      </div>

    </div>

  </div>

</template>

<script>
import { HTTP } from "@/http-constants";
import Vue from "vue";

export default {
  name: "portfolios",
  data() {
    return {
      portfolioList: [],

      // Add portfolio related vars
      stockList: [],
      newStrategyName: undefined,
      usStockCode: undefined,
      exUsStockCode: undefined,
      bondStockCode: undefined
    };
  },
  filters: {
    formatNb: function(value) {
      if (!value) {
        return "";
      }
      value = Math.round(value * 100) / 100;
      return value;
    }
  },
  methods: {
    addStrategy() {
      console.log("addStrategy");
      HTTP.post(
        "/strategies/" +
          this.newStrategyName +
          "/" +
          this.usStockCode +
          "/" +
          this.exUsStockCode +
          "/" +
          this.bondStockCode
      ).then(response => {
        this.loadPortfolioList();
        this.newStrategyName = undefined;
        this.usStockCode = undefined;
        this.exUsStockCode = undefined;
        this.bondStockCode = undefined;
      });
    },
    loadStockList() {
      HTTP.get("/stocks/").then(response => {
        this.stockList = response.data;
      });
    },
    loadPortfolioList() {
      HTTP.get("/strategies/").then(response => {
        this.portfolioList = response.data;
      });
    },
    processStrategy(currentStrategyCode) {
      HTTP.post("/strategies/" + currentStrategyCode + "/process").then(
        response => {
          this.loadPortfolioList();
        }
      );
    },
    resetStrategy(currentStrategyCode) {
      HTTP.post("/strategies/" + currentStrategyCode + "/reset").then(
        response => {
          this.loadPortfolioList();
        }
      );
    },
    deleteStrategy(currentStrategyCode) {
      HTTP.delete("/strategies/" + currentStrategyCode).then(response => {
        this.loadPortfolioList();
      });
    }
  },
  created() {
    this.loadPortfolioList();
    this.loadStockList();
  },
  mounted() {},
  components: {}
};
</script>

<style scoped>
</style>
