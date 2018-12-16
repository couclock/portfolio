<template>
  <div class="md-layout">

    <!-- Select line -->
    <div class="md-layout md-gutter md-layout-item md-alignment-center-center">
      <div class="md-layout-item md-size-20">

        <md-field v-if="currentStrategy">
          <label for="strategy">Strategy</label>
          <md-select v-model="currentStrategyCode"
                     @md-selected="loadStrategy"
                     name="strategy"
                     md-dense
                     id="strategy">
            <md-option v-for="strat in strategies"
                       :key="strat.id"
                       :value="strat.strategyCode">{{ strat.strategyCode }}</md-option>
          </md-select>
        </md-field>
      </div>

    </div>

    <!-- Graph line -->
    <div class="md-layout md-gutter md-layout-item md-size-100">

      <div class="md-layout-item md-size-20">
        <div v-if="currentStrategy">
          <div class="md-layout">
            Code : {{ currentStrategy.strategyCode }}
          </div>
          <div class="md-layout">
            Last date : {{ currentStrategy.endDate }}
          </div>
          <div class="md-layout">
            CAGR : {{ 100 * currentStrategy.cagr | formatNb }} %
          </div>
          <div class="md-layout">
            UlcerIndex : {{ currentStrategy.ulcerIndex | formatNb }} %
          </div>
          <div class="md-layout">
            <md-button class="md-layout-item md-raised md-primary"
                       @click="processStrategy">Process</md-button>
            <md-button class="md-layout-item md-raised"
                       @click="resetStrategy">Reset</md-button>
            <md-button class="md-layout-item md-raised md-accent"
                       @click="deleteStrategy">Delete</md-button>
          </div>
        </div>
      </div>

      <div class="md-layout-item md-size-80">
        <vue-c3 :handler="handler"></vue-c3>
      </div>
    </div>

    <!-- Action line -->
    <div class="md-layout md-gutter md-layout-item md-alignment-top-center">
      <div class="md-layout-item md-size-50">

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
                     @click="addStrategy">Add strategy</md-button>
        </div>

      </div>

    </div>
  </div>

</template>

<script>
// @ is an alias to /src
import HelloWorld from "@/components/HelloWorld.vue";
import Vue from "vue";
import VueC3 from "vue-c3";
import { HTTP } from "@/http-constants";

export default {
  name: "home",
  data() {
    return {
      handler: new Vue(),
      currentStrategyCode: undefined,
      currentStrategy: undefined,
      strategies: [],
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
    loadStrategy(newStrategy) {
      console.log("loadStrategy : ", newStrategy);
      this.updateStrategy(newStrategy);
      this.updateGraph(newStrategy);
    },
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
        this.loadStrategyList();
      });
    },
    processStrategy() {
      HTTP.post("/strategies/" + this.currentStrategyCode + "/process").then(
        response => {
          this.currentStrategy = response.data;
          this.updateGraph(this.currentStrategyCode);
        }
      );
    },
    resetStrategy() {
      HTTP.post("/strategies/" + this.currentStrategyCode + "/reset").then(
        response => {
          this.currentStrategy = response.data;
          this.updateGraph(this.currentStrategyCode);
        }
      );
    },
    deleteStrategy() {
      HTTP.delete("/strategies/" + this.currentStrategyCode).then(response => {
        this.loadStrategyList();
      });
    },
    loadStockList() {
      HTTP.get("/stocks/").then(response => {
        this.stockList = response.data;
      });
    },
    loadStrategyList() {
      HTTP.get("/strategies/").then(response => {
        this.strategies = response.data;
        if (this.strategies.length > 0) {
          this.currentStrategy = this.strategies[0];
          this.currentStrategyCode = this.currentStrategy.strategyCode;
          this.updateGraph(this.strategies[0].strategyCode);
        }
      });
    },
    updateStrategy(strategyCode) {
      HTTP.get("/strategies/" + strategyCode).then(response => {
        this.currentStrategy = response.data;
        this.currentStrategyCode = this.currentStrategy.strategyCode;
      });
    },
    updateGraph(strategyCode) {
      HTTP.get("/strategies/" + strategyCode + "/history")
        .then(response => {
          const options = {
            size: {
              height: 500,
              width: 1200
            },
            data: {
              json: response.data,
              keys: {
                x: "date",
                value: ["value"]
              }
            },
            axis: {
              x: {
                type: "timeseries",
                tick: {
                  count: 10,
                  format: "%Y-%m"
                }
              }
            },
            zoom: {
              enabled: true
            },
            point: {
              show: false
            }
          };
          this.handler.$emit("init", options);
        })
        .catch(response => {
          console.error("ERROR : ", response);
          this.error = true;
        });
    }
  },
  created() {
    this.loadStrategyList();
    this.loadStockList();
  },
  mounted() {},
  components: {
    HelloWorld,
    VueC3
  }
};
</script>

<style scoped>
</style>
