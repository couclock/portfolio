<template>

  <md-card>
    <md-card-header>
      <div class="md-title">Add a portfolio</div>
    </md-card-header>
    <md-card-content class="md-layout md-gutter md-alignment-top-center">

      <div class="md-subheading error-line md-layout-item md-size-100 md-theme-default"
           v-if="errorMsg">{{ errorMsg }}</div>

      <!-- line : name -->
      <div class="md-layout-item md-size-100">
        <md-field>
          <label>New portfolio code</label>
          <md-input v-model="newPFCode"></md-input>
        </md-field>
      </div>

      <!-- line : startDate -->
      <div class="md-layout md-layout-item md-size-100">
        <div class="md-layout-item md-size-30">
          <md-field>
            <label>Start date</label>
            <md-input v-model="newStartDate"
                      type="date"></md-input>
          </md-field>
        </div>
      </div>

      <!-- 2nd line -->
      <div class="md-layout md-layout-item md-size-100">
        <div class="md-layout-item md-size-30">
          <md-radio v-model="newBucket"
                    value="us">US</md-radio>
          <md-radio v-model="newBucket"
                    value="exUs">ex-US</md-radio>
          <md-radio v-model="newBucket"
                    value="bond">bond</md-radio>
        </div>
        <div class="md-layout-item md-size-20">
          <md-field>
            <label for="newStock">Select stock</label>
            <md-select v-model="newStock"
                       md-dense
                       name="newStock"
                       id="newStock">
              <md-option v-for="(stock, index) in stocksPerBucket[newBucket]"
                         :key="index"
                         :value="stock">{{ stock}}</md-option>
            </md-select>
          </md-field>
        </div>
        <div class="md-layout-item md-size-5">
        </div>
        <div class="md-layout-item md-size-25">
          <md-field>
            <label>Percent</label>
            <md-input v-model="newPercent"
                      type="number"></md-input>
          </md-field>
        </div>
        <div class="md-layout-item md-size-5">
        </div>
        <div class="md-layout-item md-size-15">
          <md-button class="md-icon-button md-dense md-primary md-raised"
                     @click="addStockToPortfolio">
            <md-icon>add</md-icon>
          </md-button>
        </div>
      </div>

      <!-- 3rd line -->
      <div class="md-layout md-layout-item md-size-100">

        <md-table v-model="allParameters"
                  class="md-layout-item md-size-100"
                  v-if="allParameters.length > 0"
                  md-sort="bucket"
                  md-sort-order="asc"
                  md-card>

          <md-table-row slot="md-table-row"
                        slot-scope="{ item }">
            <md-table-cell md-label="Bucket"
                           md-sort-by="bucket">{{ item.bucket }}</md-table-cell>
            <md-table-cell md-label="Stock"
                           md-sort-by="stockCode">
              {{ item.stockCode }}
            </md-table-cell>
            <md-table-cell md-label="Percent"
                           md-numeric
                           md-sort-by="percent">{{ item.percent }}</md-table-cell>
            <md-table-cell md-label="Actions">

              <md-button class="md-icon-button md-dense md-accent md-raised"
                         @click="deleteStock(item)">
                <md-icon>delete</md-icon>
              </md-button>
            </md-table-cell>
          </md-table-row>
        </md-table>

      </div>

      <!-- 4th line -->
      <div class="md-layout md-layout-item md-size-100 md-alignment-top-center">
        <md-button class="md-raised"
                   @click="closeDialog">Cancel</md-button>
        <md-button class="md-raised md-primary"
                   @click="addPortfolio">Add portfolio</md-button>
      </div>

    </md-card-content>
  </md-card>
</template>

<script>
import { HTTP } from "@/http-constants";
import map from "lodash/map";
import filter from "lodash/filter";

export default {
  name: "PortfolioFormDialog",
  data() {
    return {
      // Add portfolio related vars
      stocksPerBucket: { us: [], exUs: [], bond: [] },
      newPFCode: undefined,
      newBucket: "us",
      newPercent: undefined,
      newStock: undefined,
      newStartDate: undefined,
      errorMsg: undefined,

      strategyParameters: {
        "@class":
          "com.couclock.portfolio.entity.strategies.AcceleratedMomentumStrategy",
        type: "ACCELERATED_MOMENTUM",
        usStocks: [],
        exUsStocks: [],
        bondStocks: []
      },
      allParameters: []
    };
  },
  methods: {
    closeDialog() {
      this.$emit("closeDialog");
    },
    loadStocksPerBucket() {
      HTTP.get("/stocks/by-tag/us").then(response => {
        this.stocksPerBucket["us"] = map(response.data, "code");
      });
      HTTP.get("/stocks/by-tag/exUs").then(response => {
        this.stocksPerBucket["exUs"] = map(response.data, "code");
      });
      HTTP.get("/stocks/by-tag/bond").then(response => {
        this.stocksPerBucket["bond"] = map(response.data, "code");
      });
    },
    deleteStock(portfolioLine) {
      this.allParameters = filter(this.allParameters, oneParam => {
        return oneParam.stockCode !== portfolioLine.stockCode;
      });
      this.strategyParameters[portfolioLine.bucket + "Stocks"] = filter(
        this.strategyParameters[portfolioLine.bucket + "Stocks"],
        oneParam => {
          return oneParam.stockCode !== portfolioLine.stockCode;
        }
      );
    },
    addStockToPortfolio() {
      if (!this.newStock || !this.newPercent) {
        console.error("missing data to add line");
        return;
      }

      this.strategyParameters[this.newBucket + "Stocks"].push({
        percent: this.newPercent / 100,
        stockCode: this.newStock
      });
      this.allParameters.push({
        bucket: this.newBucket,
        percent: this.newPercent,
        stockCode: this.newStock
      });
      this.newPercent = undefined;
      this.newStock = undefined;
      this.newBucket = "us";
    },
    addPortfolio() {
      HTTP.post("/portfolios", {
        code: this.newPFCode,
        startDate: this.newStartDate,
        strategyParameters: this.strategyParameters
      })
        .then(response => {
          this.$emit("portfolioAdded");
          this.$emit("closeDialog");
        })
        .catch(error => {
          console.error(error.response);
          this.errorMsg =
            error.response && error.response.data && error.response.data.message
              ? "ERROR : " + error.response.data.message
              : error;
        });
    }
  },
  created() {
    this.loadStocksPerBucket();
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
