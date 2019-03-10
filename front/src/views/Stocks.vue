<template>
  <div class="md-layout md-gutter md-alignment-top-center">

    <div class="md-layout md-layout-item md-size-100 md-gutter md-alignment-top-center">

      <div class="md-layout md-layout-item md-size-80">

        <md-table v-model="filteredStockList"
                  v-if="stockList.length > 0"
                  md-sort="code"
                  md-sort-order="asc"
                  md-card
                  ref="stockTable"
                  md-fixed-header
                  class="md-layout-item"
                  @md-selected="onStockSelect">
          <md-table-toolbar>
            <div class="md-toolbar-section-start">
              <h1 class="md-title">Stocks</h1>
            </div>
            <md-field md-clearable
                      class="md-toolbar-section-end">
              <md-input placeholder="Search by code ..."
                        v-model="search"
                        @input="searchOnTable" />
            </md-field>
          </md-table-toolbar>

          <md-table-toolbar slot="md-table-alternate-header"
                            slot-scope="{ count }">
            <div class="md-toolbar-section-start">{{ getAlternateLabel(count) }}</div>

            <div class="md-toolbar-section-end">

              <md-button class="md-icon-button md-raised"
                         title="Update history using Yahoo"
                         @click="updateStockHistoryMultiple_yahoo"
                         :disabled="actionsDisabled">
                <h2>Y</h2>
              </md-button>
              <md-button class="md-icon-button md-raised"
                         title="Update history using Boursorama"
                         @click="updateStockHistoryMultiple_bourso"
                         :disabled="actionsDisabled">
                <h2>B</h2>
              </md-button>

              <md-button class="md-icon-button md-raised"
                         title="Update stock indicators"
                         @click="updateStockIndicatorsMultiple"
                         :disabled="actionsDisabled">
                <md-icon>info</md-icon>
              </md-button>
              <md-button class="md-icon-button md-raised md-primary"
                         title="Update stock"
                         @click="stockToEdit = selectedStocks[0] ; showStockFormDialog = true"
                         :disabled="selectedStocks.length !== 1">
                <md-icon>edit</md-icon>
              </md-button>
              <md-button class="md-icon-button md-raised md-primary"
                         @click="resetStockHistoryMultiple"
                         :disabled="actionsDisabled">
                <md-icon>settings_backup_restore</md-icon>
              </md-button>
              <md-button class="md-icon-button md-accent md-raised"
                         @click="deleteStockMultiple"
                         :disabled="actionsDisabled">
                <md-icon>delete</md-icon>
              </md-button>

            </div>
          </md-table-toolbar>

          <md-table-row slot="md-table-row"
                        slot-scope="{ item }"
                        md-selectable="multiple"
                        md-auto-select>
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
            <md-table-cell md-label="Last history date">
              <span>{{ item.lastHistoryDate | formatDate }}</span>
            </md-table-cell>
            <md-table-cell md-label="Last indicator date"
                           :class="{ 'missing-indicator': item.lastIndicatorDate !== item.lastHistoryDate }">
              <span>{{ item.lastIndicatorDate | formatDate }}</span>
            </md-table-cell>

          </md-table-row>
        </md-table>
      </div>
    </div>

    <!-- Add form line -->
    <md-dialog :md-active.sync="showStockFormDialog">
      <stock-form-dialog :stockToEdit="stockToEdit"
                         @stockAdded="stockAddedEventHandler"
                         @closeDialog="showStockFormDialog = false"></stock-form-dialog>
    </md-dialog>
    <!-- end Add form line -->

    <md-button @click="stockToEdit = undefined ; showStockFormDialog = true"
               class="md-fab md-primary md-fab-bottom-right">
      <md-icon>add</md-icon>
    </md-button>

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
import map from "lodash/map";
import forEach from "lodash/forEach";
import filter from "lodash/filter";
import findIndex from "lodash/findIndex";
import remove from "lodash/remove";
import stockFormDialog from "@/components/StockFormDialog.vue";
import { format } from "date-fns";

export default {
  name: "stocks",
  data() {
    return {
      stockList: [],
      filteredStockList: [],
      selectedStocks: [],
      search: undefined,

      lastHistory: {},
      actionsDisabled: false,
      showSnackbar: false,
      snackbarMessage: "",

      showStockFormDialog: false,
      stockToEdit: undefined
    };
  },
  filters: {
    formatDate: function(value) {
      if (!value) {
        return "";
      }
      value = format(value, "DD/MM/YYYY");
      return value;
    }
  },
  methods: {
    stockAddedEventHandler() {
      if (this.stockToEdit === undefined) {
        this.snackbarMessage = "Your stock has been successfully created ! ";
      } else {
        this.snackbarMessage = "Your stock has been successfully updated ! ";
      }
      this.showSnackbar = true;
      this.loadStockList();
    },
    searchOnTable() {
      if (this.search) {
        this.filteredStockList = filter(this.stockList, oneStock => {
          return oneStock.code
            .toString()
            .toLowerCase()
            .includes(this.search.toString().toLowerCase());
        });
      } else {
        this.filteredStockList = this.stockList;
      }
    },
    onStockSelect(items) {
      this.selectedStocks = items;
    },
    getAlternateLabel(count) {
      let plural = count > 1 ? "s" : "";
      return count + " stock" + plural + " selected";
    },
    loadStockList() {
      HTTP.get("/stocks/light/").then(response => {
        this.stockList = response.data;
        this.filteredStockList = this.stockList;
        this.actionsDisabled = false;
      });
    },
    /* ***************************************************************************
     * handleError
     * ***************************************************************************/
    handleError(response) {
      if (response.data && response.data.message) {
        this.snackbarMessage = "ERROR : " + response.data.message;
      } else {
        this.snackbarMessage = "ERROR : " + response;
      }
      this.showSnackbar = true;
      this.actionsDisabled = false;
    },

    updateStockHistoryMultiple_yahoo() {
      this.actionsDisabled = true;
      let stockIds = map(this.selectedStocks, oneStock => oneStock.id);

      HTTP.post("/stocks/update-history-yahoo", stockIds)
        .then(response => {
          forEach(response.data, oneStock => {
            let idx = findIndex(this.stockList, { id: oneStock.id });
            this.stockList.splice(idx, 1, oneStock);
          });
          this.snackbarMessage =
            "Your stock histories have been successfully updated ! ";
          this.showSnackbar = true;
          this.actionsDisabled = false;
          this.unselectAllStocks();
        })
        .catch(this.handleError);
    },
    updateStockIndicatorsMultiple() {
      this.actionsDisabled = true;
      let stockIds = map(this.selectedStocks, oneStock => oneStock.id);

      HTTP.post("/stocks/update-indicators", stockIds)
        .then(response => {
          forEach(response.data, oneStock => {
            let idx = findIndex(this.stockList, { id: oneStock.id });
            this.stockList.splice(idx, 1, oneStock);
          });
          this.snackbarMessage =
            "Your stock indicators have been successfully updated ! ";
          this.showSnackbar = true;
          this.actionsDisabled = false;
          this.unselectAllStocks();
        })
        .catch(this.handleError);
    },
    updateStockHistoryMultiple_bourso() {
      this.actionsDisabled = true;
      let stockIds = map(this.selectedStocks, oneStock => oneStock.id);

      HTTP.post("/stocks/update-history-bourso", stockIds)
        .then(response => {
          forEach(response.data, oneStock => {
            let idx = findIndex(this.stockList, { id: oneStock.id });
            this.stockList.splice(idx, 1, oneStock);
          });
          this.snackbarMessage =
            "Your stock histories have been successfully updated ! ";
          this.showSnackbar = true;
          this.actionsDisabled = false;
          this.unselectAllStocks();
        })
        .catch(this.handleError);
    },
    resetStockHistoryMultiple() {
      this.actionsDisabled = true;
      let stockIds = map(this.selectedStocks, oneStock => oneStock.id);

      HTTP.post("/stocks/reset-history", stockIds)
        .then(response => {
          forEach(response.data, oneStock => {
            let idx = findIndex(this.stockList, { id: oneStock.id });
            this.stockList.splice(idx, 1, oneStock);
          });
          this.snackbarMessage =
            "Your stock histories have been successfully reset ! ";
          this.showSnackbar = true;
          this.actionsDisabled = false;
          this.unselectAllStocks();
        })
        .catch(this.handleError);
    },
    /* ***************************************************************************
     * deleteStockMultiple
     * ***************************************************************************/
    deleteStockMultiple() {
      this.actionsDisabled = true;
      let stockIds = map(this.selectedStocks, oneStock => oneStock.id);

      HTTP.delete("/stocks", { data: stockIds })
        .then(response => {
          this.snackbarMessage =
            "Your stocks have been successfully deleted ! ";
          this.showSnackbar = true;

          forEach(this.selectedStocks, oneSelectedStock => {
            remove(this.stockList, { id: oneSelectedStock.id });
          });

          this.unselectAllStocks();
          this.actionsDisabled = false;
        })
        .catch(this.handleError);
    },

    unselectAllStocks() {
      forEach(this.selectedStocks, oneSelectedStock => {
        this.$refs.stockTable.manageItemSelection(oneSelectedStock);
      });
    }
  },
  created() {
    this.loadStockList();
  },
  components: {
    stockFormDialog
  }
};
</script>

<style scoped>
.stock-name {
  text-align: left;
}
td.missing-indicator {
  background-color: darksalmon;
}
</style>
