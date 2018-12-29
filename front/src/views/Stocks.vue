<template>
  <div class="md-layout md-gutter md-alignment-top-center">

    <div class="md-layout md-layout-item md-size-100 md-gutter md-alignment-top-center">

      <div class="md-layout md-layout-item md-size-80">

        <md-table v-model="stockList"
                  v-if="stockList.length > 0"
                  md-sort="code"
                  md-sort-order="asc"
                  md-card
                  class="md-layout-item md-size-100">
          <md-table-toolbar>
            <h1 class="md-title">Stocks</h1>
          </md-table-toolbar>

          <md-table-row slot="md-table-row"
                        slot-scope="{ item }">
            <md-table-cell md-label="Code"
                           md-sort-by="code">
              {{ item.code }}
            </md-table-cell>
            <md-table-cell md-label="Name"
                           md-sort-by="name"
                           class="stock-name">
              {{ item.name }}
            </md-table-cell>
            <md-table-cell md-label="Tags">
              <md-chip v-for="(tag, index) in item.tags"
                       :key="index">
                {{ tag }}
              </md-chip>
            </md-table-cell>
            <md-table-cell md-label="Last date">
              <span v-if="lastHistory[item.code]">{{ lastHistory[item.code].date }}</span>
            </md-table-cell>
            <md-table-cell md-label="Actions">
              <md-button class="md-icon-button md-dense md-raised"
                         @click="updateStock_yahoo(item.code)"
                         :disabled="actionsDisabled"
                         title="Update history (yahoo)">
                <md-icon>autorenew</md-icon>
              </md-button>
              <md-button class="md-icon-button md-dense md-raised"
                         @click="updateStock_bourso(item.code)"
                         :disabled="actionsDisabled"
                         title="Update history (bourso)">
                <md-icon>autorenew</md-icon>
              </md-button>
              <md-button class="md-icon-button md-dense md-raised md-primary"
                         @click="resetStockHistory(item.code)"
                         :disabled="actionsDisabled"
                         title="Reset history">
                <md-icon>settings_backup_restore</md-icon>
              </md-button>
              <md-button class="md-icon-button md-dense md-accent md-raised"
                         @click="deleteStock(item.code)"
                         :disabled="actionsDisabled"
                         title="Delete stock">
                <md-icon>delete</md-icon>
              </md-button>
            </md-table-cell>
          </md-table-row>
        </md-table>
      </div>
    </div>

    <!-- Divider -->
    <div class="md-layout md-layout-item md-size-100">
      <md-divider class="md-inset"></md-divider>
    </div>
    <!-- end Divider -->

    <!-- Add form line -->
    <div class="md-layout md-gutter md-layout-item md-alignment-top-center">
      <div class="md-layout-item md-size-50">

        <md-card>
          <md-card-header>
            <div class="md-title">Add a stock</div>
          </md-card-header>
          <md-card-content>

            <div class="md-layout md-gutter">
              <md-field class="md-layout-item">
                <label>New stock code</label>
                <md-input v-model="newStockCode"></md-input>
              </md-field>
              <div class="md-layout-item md-size-5">
              </div>
              <md-chips class="md-layout-item"
                        v-model="newStockTags"
                        md-placeholder="Add tag..."></md-chips>

            </div>

            <div class="md-layout md-alignment-top-center">
              <md-button class="md-raised md-primary"
                         @click="addStock">Add stock</md-button>
            </div>

          </md-card-content>
        </md-card>
      </div>
    </div>
    <!-- end Add form line -->

    <!--
    <div class="md-layout  md-layout-item  md-alignment-top-center">
      <md-button class="md-raised md-primary"
                 @click="updateStocks">Update stocks</md-button>
    </div>
  -->
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
  name: "stocks",
  data() {
    return {
      stockList: [],
      lastHistory: {},
      actionsDisabled: false,
      showSnackbar: false,
      snackbarMessage: "",

      newStockCode: undefined,
      newStockTags: []
    };
  },

  methods: {
    loadStockList() {
      HTTP.get("/stocks/").then(response => {
        this.stockList = response.data;
        this.actionsDisabled = false;
        this.loadLastHistory();
      });
    },
    updateStocks() {
      HTTP.post("/stocks/update").then(() => {
        this.loadLastHistory();
      });
    },
    addStock() {
      HTTP.post("/stocks", {
        code: this.newStockCode,
        tags: this.newStockTags
      })
        .then(() => {
          this.snackbarMessage = "Your stock has been successfully added ! ";
          this.showSnackbar = true;
          this.loadStockList();
          this.newStockCode = undefined;
          this.newStockTags = [];
        })
        .catch(response => {
          console.error("ERROR : ", response);
          this.snackbarMessage = "ERROR : " + response;
          this.showSnackbar = true;
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
    },
    updateStock_yahoo(stockCode) {
      this.actionsDisabled = true;
      HTTP.post("/stocks/" + stockCode + "/update").then(response => {
        this.snackbarMessage =
          "Your stock history has been successfully updated ! ";
        this.showSnackbar = true;
        this.loadStockList();
      });
    },
    updateStock_bourso(stockCode) {
      this.actionsDisabled = true;
      HTTP.post("/stocks/" + stockCode + "/update_bourso").then(response => {
        this.snackbarMessage =
          "Your stock history has been successfully updated ! ";
        this.showSnackbar = true;
        this.loadStockList();
      });
    },

    resetStockHistory(stockCode) {
      this.actionsDisabled = true;
      HTTP.post("/stocks/" + stockCode + "/reset").then(response => {
        this.snackbarMessage =
          "Your stock history has been successfully reset ! ";
        this.showSnackbar = true;
        this.loadStockList();
      });
    },
    deleteStock(stockCode) {
      this.actionsDisabled = true;
      HTTP.delete("/stocks/" + stockCode).then(response => {
        this.snackbarMessage = "Your stock has been successfully deleted ! ";
        this.showSnackbar = true;
        this.loadStockList();
      });
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
.stock-name {
  text-align: left;
}
</style>
