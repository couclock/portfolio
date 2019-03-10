<template>
  <div class="md-layout md-gutter md-alignment-top-center">

    <div class="md-layout md-layout-item md-size-80 md-alignment-top-center">

      <md-table v-model="filteredPortfolioList"
                v-if="portfolioList.length > 0"
                md-sort="code"
                md-sort-order="asc"
                md-card
                ref="pfTable"
                md-fixed-header
                class="md-layout-item "
                @md-selected="onPortfolioSelect">
        <md-table-toolbar>
          <div class="md-toolbar-section-start">
            <h1 class="md-title">Portfolios</h1>
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
                       title="Update portfolio backtest"
                       @click="processPortfolioBacktestMultiple"
                       :disabled="actionsDisabled">
              <md-icon>autorenew</md-icon>
            </md-button>
            <md-button class="md-icon-button md-raised"
                       @click="optimizePortfolio"
                       title="Optimize portoflio (finding best ratio)"
                       :disabled="actionsDisabled || selectedPortfolios.length !== 1">
              <md-icon>trending_up</md-icon>
            </md-button>
            <md-button class="md-icon-button md-raised md-primary"
                       title="Update stock"
                       @click="portfolioToEdit = selectedPortfolios[0] ; showPortfolioFormDialog = true"
                       :disabled="actionsDisabled || selectedPortfolios.length !== 1">
              <md-icon>edit</md-icon>
            </md-button>
            <md-button class="md-icon-button md-raised md-primary"
                       @click="resetPortfolioBacktestMultiple"
                       :disabled="actionsDisabled">
              <md-icon>settings_backup_restore</md-icon>
            </md-button>
            <md-button class="md-icon-button md-accent md-raised"
                       @click="deletePortfolioMultiple"
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
            <router-link :to="{ name: 'portfolioDetail', params: { id: item.id }}">
              {{ item.code }}
            </router-link>
          </md-table-cell>
          <md-table-cell md-label="Start date"
                         md-sort-by="startDate">{{ item.startDate | formatDate }}</md-table-cell>
          <md-table-cell md-label="End date"
                         md-sort-by="endDate">{{ item.endDate | formatDate }}</md-table-cell>
          <md-table-cell md-label="Start money"
                         md-sort-by="startMoney">{{ item.startMoney | formatNb }} €</md-table-cell>
          <md-table-cell md-label="End money"
                         md-sort-by="endMoney">{{ item.endMoney | formatNb }} €</md-table-cell>

          <md-table-cell md-label="CAGR"
                         md-sort-by="cagr">
            <span v-if="item.cagr">{{ 100 * item.cagr | formatNb }} %</span>
          </md-table-cell>
          <md-table-cell md-label="Ulcer"
                         md-sort-by="ulcerIndex">
            <span v-if="item.ulcerIndex">{{ item.ulcerIndex | formatNb }} %</span>
          </md-table-cell>
          <md-table-cell md-label="Optim">
            <span v-if="item.strategyParameters.ema6MonthsProtectionRatio && item.strategyParameters.ema6MonthsProtectionRatio > 0">
              <md-icon :title="item.strategyParameters.ema6MonthsProtectionRatio">trending_up</md-icon>
            </span>
          </md-table-cell>

        </md-table-row>
      </md-table>
    </div>
    <div class="md-layout md-layout-item md-size-100">

      <md-divider class="md-inset"></md-divider>
    </div>

    <!-- Add form line -->
    <md-dialog :md-active.sync="showPortfolioFormDialog">
      <portfolio-form-dialog :portfolioToEdit="portfolioToEdit"
                             @portfolioAdded="portfolioAddedEventHandler"
                             @closeDialog="showPortfolioFormDialog = false"></portfolio-form-dialog>

    </md-dialog>
    <!-- end Add form line -->

    <md-button @click="portfolioToEdit = undefined; showPortfolioFormDialog = true"
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
import findIndex from "lodash/findIndex";
import remove from "lodash/remove";
import map from "lodash/map";
import forEach from "lodash/forEach";
import filter from "lodash/filter";
import { format } from "date-fns";

import { HTTP } from "@/http-constants";
import Vue from "vue";
import portfolioFormDialog from "@/components/PortfolioFormDialog.vue";

export default {
  name: "portfolios",
  data() {
    return {
      portfolioList: [],
      filteredPortfolioList: [],
      selectedPortfolios: [],
      search: undefined,
      actionsDisabled: false,

      showSnackbar: false,
      snackbarMessage: "",
      showPortfolioFormDialog: false,
      portfolioToEdit: undefined
    };
  },
  filters: {
    formatNb: function(value) {
      if (!value) {
        return "";
      }
      value = Math.round(value * 100) / 100;
      return value;
    },
    formatDate: function(value) {
      if (!value) {
        return "";
      }
      value = format(value, "DD/MM/YYYY");
      return value;
    }
  },
  methods: {
    searchOnTable() {
      if (this.search) {
        this.filteredPortfolioList = filter(
          this.portfolioList,
          onePortfolio => {
            return onePortfolio.code
              .toString()
              .toLowerCase()
              .includes(this.search.toString().toLowerCase());
          }
        );
      } else {
        this.filteredPortfolioList = this.portfolioList;
      }
    },
    test() {
      forEach(this.selectedPortfolios, oneSelectedPortfolio => {
        this.$refs.pfTable.manageItemSelection(oneSelectedPortfolio);
      });
    },
    onPortfolioSelect(items) {
      this.selectedPortfolios = items; //map(items, oneItem => oneItem.id);
    },
    getAlternateLabel(count) {
      let plural = count > 1 ? "s" : "";
      return count + " portfolio" + plural + " selected";
    },
    portfolioAddedEventHandler() {
      if (this.portfolioToEdit === undefined) {
        this.snackbarMessage =
          "Your portfolio has been successfully created ! ";
      } else {
        this.snackbarMessage =
          "Your portfolio has been successfully updated ! ";
      }
      this.showSnackbar = true;
      this.loadPortfolioList();
    },
    loadPortfolioList() {
      HTTP.get("/portfolios/").then(response => {
        this.portfolioList = response.data;
        this.filteredPortfolioList = this.portfolioList;
        this.actionsDisabled = false;
      });
    },
    optimizePortfolio() {
      this.actionsDisabled = true;
      HTTP.get(
        "/portfolios/" + this.selectedPortfolios[0].id + "/optimize"
      ).then(response => {
        let idx = findIndex(this.portfolioList, { id: response.data.id });
        this.portfolioList.splice(idx, 1, response.data);

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
    processPortfolioBacktestMultiple() {
      this.actionsDisabled = true;
      let portfolioIds = map(
        this.selectedPortfolios,
        onePortfolio => onePortfolio.id
      );

      HTTP.post("/portfolios/process-backtest", portfolioIds)
        .then(response => {
          forEach(response.data, onePortfolio => {
            let idx = findIndex(this.portfolioList, { id: onePortfolio.id });
            this.portfolioList.splice(idx, 1, onePortfolio);
          });
          this.snackbarMessage =
            "Your portfolio has been successfully processed ! ";
          this.showSnackbar = true;
          this.actionsDisabled = false;
          this.unselectAllPortfolios();
        })
        .catch(this.handleError);
    },

    resetPortfolioBacktestMultiple() {
      this.actionsDisabled = true;
      let portfolioIds = map(
        this.selectedPortfolios,
        onePortfolio => onePortfolio.id
      );

      HTTP.post("/portfolios/reset-backtest", portfolioIds)
        .then(response => {
          forEach(response.data, onePortfolio => {
            let idx = findIndex(this.portfolioList, { id: onePortfolio.id });
            this.portfolioList.splice(idx, 1, onePortfolio);
          });
          this.snackbarMessage =
            "Your portfolios have been successfully reset ! ";
          this.showSnackbar = true;
          this.actionsDisabled = false;
          this.unselectAllPortfolios();
        })
        .catch(this.handleError);
    },
    deletePortfolioMultiple() {
      this.actionsDisabled = true;
      let portfolioIds = map(
        this.selectedPortfolios,
        onePortfolio => onePortfolio.id
      );
      HTTP.delete("/portfolios", { data: portfolioIds }).then(response => {
        this.snackbarMessage =
          "Your portfolios have been successfully deleted ! ";
        this.showSnackbar = true;
        forEach(this.selectedPortfolios, oneSelectedPortfolio => {
          remove(this.portfolioList, { id: oneSelectedPortfolio.id });
        });
        this.unselectAllPortfolios();
        this.actionsDisabled = false;
      });
    },
    unselectAllPortfolios() {
      forEach(this.selectedPortfolios, oneSelectedPortfolio => {
        this.$refs.pfTable.manageItemSelection(oneSelectedPortfolio);
      });
    }
  },
  created() {
    this.loadPortfolioList();
  },
  components: {
    portfolioFormDialog
  }
};
</script>

<style scoped>
</style>
