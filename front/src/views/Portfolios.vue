<template>
  <div class="md-layout md-gutter md-alignment-top-center">

    <div class="md-layout md-layout-item md-size-75 md-alignment-top-center">

      <md-table v-model="portfolioList"
                v-if="portfolioList.length > 0"
                md-sort="code"
                md-sort-order="asc"
                md-card>
        <md-table-toolbar>
          <h1 class="md-title">Portfolios</h1>
        </md-table-toolbar>

        <md-table-row slot="md-table-row"
                      slot-scope="{ item }">
          <md-table-cell md-label="ID"
                         md-numeric
                         md-sort-by="id">{{ item.id }}</md-table-cell>
          <md-table-cell md-label="Code"
                         md-sort-by="code">
            <router-link :to="{ name: 'portfolioDetail', params: { code: item.code }}">
              {{ item.code }}
            </router-link>
          </md-table-cell>
          <md-table-cell md-label="Start date"
                         md-sort-by="startDate">{{ item.startDate }}</md-table-cell>
          <md-table-cell md-label="End date"
                         md-sort-by="endDate">{{ item.endDate }}</md-table-cell>
          <md-table-cell md-label="Start money"
                         md-sort-by="startMoney">{{ item.startMoney | formatNb }} €</md-table-cell>
          <md-table-cell md-label="End money"
                         md-sort-by="endMoney">{{ item.endMoney | formatNb }} €</md-table-cell>

          <md-table-cell md-label="CAGR"
                         md-sort-by="cagr">{{ 100 * item.cagr | formatNb }} %</md-table-cell>
          <md-table-cell md-label="Ulcer"
                         md-sort-by="ulcerIndex">{{ item.ulcerIndex | formatNb }} %</md-table-cell>
          <md-table-cell md-label="Actions">
            <md-button class="md-icon-button md-dense md-raised"
                       @click="processStrategy(item.code)"
                       :disabled="actionsDisabled">
              <md-icon>autorenew</md-icon>
            </md-button>
            <md-button class="md-icon-button md-dense md-raised md-primary"
                       @click="resetPortfolioBacktest(item.code)"
                       :disabled="actionsDisabled">
              <md-icon>settings_backup_restore</md-icon>
            </md-button>
            <md-button class="md-icon-button md-dense md-accent md-raised"
                       @click="deletePortfolio(item.code)"
                       :disabled="actionsDisabled">
              <md-icon>delete</md-icon>
            </md-button>
          </md-table-cell>
        </md-table-row>
      </md-table>
    </div>
    <div class="md-layout md-layout-item md-size-100">

      <md-divider class="md-inset"></md-divider>
    </div>

    <!-- Add form line -->
    <div class="md-layout md-gutter md-layout-item md-alignment-top-center">
      <div class="md-layout-item md-size-50">

        <md-card>
          <md-card-header>
            <div class="md-title">Add a portfolio</div>
          </md-card-header>
          <md-card-content>

            <div class="md-layout">
              <md-field class="md-layout-item">
                <label>New portfolio code</label>
                <md-input v-model="newPFCode"></md-input>
              </md-field>
              <div class="md-layout-item md-size-5">
              </div>
              <md-field class="md-layout-item">
                <label for="usStock">
                  US stock
                </label>
                <md-select v-model="usStockCode"
                           md-dense
                           name="usStock"
                           id="usStock">
                  <md-option v-for="stock in stockList"
                             :key="stock.id"
                             :value="stock.code">
                    {{ stock.code }}
                  </md-option>
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
                         @click="addPortfolio">Add portfolio</md-button>
            </div>

          </md-card-content>
        </md-card>
      </div>
    </div>
    <!-- end Add form line -->

    <md-snackbar md-position="center"
                 :md-active.sync="showSnackbar"
                 md-persistent>
      <span>{{ snackbarMessage }}</span>
    </md-snackbar>

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
      newPFCode: undefined,
      usStockCode: undefined,
      exUsStockCode: undefined,
      bondStockCode: undefined,
      actionsDisabled: false,
      showSnackbar: false,
      snackbarMessage: ""
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
    addPortfolio() {
      console.log("addPortfolio");
      HTTP.post(
        "/strategies/" +
          this.newPFCode +
          "/" +
          this.usStockCode +
          "/" +
          this.exUsStockCode +
          "/" +
          this.bondStockCode
      ).then(response => {
        this.loadPortfolioList();
        this.newPFCode = undefined;
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
        this.actionsDisabled = false;
      });
    },
    processStrategy(currentPFCode) {
      this.actionsDisabled = true;
      HTTP.post("/strategies/" + currentPFCode + "/process").then(response => {
        this.snackbarMessage =
          "Your portfolio has been successfully processed ! ";
        this.showSnackbar = true;
        this.loadPortfolioList();
      });
    },
    resetPortfolioBacktest(currentPFCode) {
      this.actionsDisabled = true;
      HTTP.post("/strategies/" + currentPFCode + "/reset").then(response => {
        this.snackbarMessage = "Your portfolio has been successfully reset ! ";
        this.showSnackbar = true;
        this.loadPortfolioList();
      });
    },
    deletePortfolio(currentPFCode) {
      this.actionsDisabled = true;
      HTTP.delete("/strategies/" + currentPFCode).then(response => {
        this.snackbarMessage =
          "Your portfolio has been successfully deleted ! ";
        this.showSnackbar = true;
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
