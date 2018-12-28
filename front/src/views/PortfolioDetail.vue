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
          <div class="md-layout"
               v-for="stat in currentStrategy.statistics"
               :key="stat.stockCode">
            {{ stat.stockCode }} : {{ 100 * stat.performance | formatNb }} % ({{ stat.dayCount }} days)
          </div>

        </div>
      </div>

      <div class="md-layout-item md-size-80">
        <vue-c3 :handler="handler"></vue-c3>
      </div>
    </div>

    <!-- Periods & Events -->
    <div class="md-layout md-gutter md-layout-item md-size-100 md-alignment-top-center">

      <!-- Periods -->
      <div class="md-layout md-gutter md-layout-item md-size-60">
        <md-table v-model="currentStrategy.periods"
                  v-if="currentStrategy && currentStrategy.periods.length > 0"
                  md-sort="startDate"
                  md-sort-order="desc"
                  md-card
                  class="md-layout-item md-size-100">
          <md-table-toolbar>
            <h1 class="md-title">Periods</h1>
          </md-table-toolbar>

          <md-table-row slot="md-table-row"
                        slot-scope="{ item }">

            <md-table-cell md-label="Stock"
                           md-sort-by="stockCode">
              {{ item.stockCode }}
            </md-table-cell>
            <md-table-cell md-label="Buy date"
                           md-sort-by="startDate">
              {{ item.startDate }}
            </md-table-cell>
            <md-table-cell md-label="Buy price"
                           md-sort-by="buyPrice"
                           md-numeric>
              {{ item.buyPrice | formatNb }} €
            </md-table-cell>
            <md-table-cell md-label="Sell date"
                           md-sort-by="endDate">
              {{ item.endDate }}
            </md-table-cell>
            <md-table-cell md-label="Sell price"
                           md-sort-by="sellPrice"
                           md-numeric>
              {{ item.sellPrice | formatNb }} €
            </md-table-cell>
            <md-table-cell md-label="Count"
                           md-sort-by="count"
                           md-numeric>
              {{ item.count }}
            </md-table-cell>
            <md-table-cell md-label="Duration"
                           md-sort-by="duration"
                           md-numeric>
              {{ item.duration }}
            </md-table-cell>
            <md-table-cell md-label="Perf"
                           md-sort-by="perf"
                           md-numeric
                           :class="{'green-perf': item.perf > 0, 'red-perf': item.perf < 0}">
              {{ 100 * item.perf | formatNb }} %
            </md-table-cell>

          </md-table-row>
        </md-table>
      </div>
      <!-- end Periods -->

      <!-- Events -->
      <div class="md-layout md-gutter md-layout-item md-size-40">
        <md-table v-model="events"
                  v-if="events.length > 0"
                  md-sort="date"
                  md-sort-order="asc"
                  md-card
                  class="md-layout-item md-size-100">
          <md-table-toolbar>
            <h1 class="md-title">Events</h1>
          </md-table-toolbar>

          <md-table-row slot="md-table-row"
                        slot-scope="{ item }">
            <md-table-cell md-label="ID"
                           md-numeric>{{ item.id }}</md-table-cell>
            <md-table-cell md-label="Date"
                           md-sort-by="date">
              {{ item.date }}
            </md-table-cell>
            <md-table-cell md-label="Type"
                           md-sort-by="type">{{ item.type }}</md-table-cell>
            <md-table-cell md-label="Stocks">{{ item.count }} {{ item.stockCode }}</md-table-cell>
          </md-table-row>
        </md-table>
      </div>
      <!-- end Events -->
    </div>
    <!-- endPeriods & Events -->

  </div>

</template>

<script>
import Vue from "vue";
import find from "lodash/find";
import VueC3 from "vue-c3";
import { HTTP } from "@/http-constants";
import router from "vue-router";

export default {
  name: "home",
  data() {
    return {
      handler: new Vue(),
      currentStrategyCode: undefined,
      currentStrategy: undefined,
      strategies: [],
      events: []
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
      this.$router.push({
        name: "portfolioDetail",
        params: { strategyCode: newStrategy }
      });
      this.currentStrategyCode = newStrategy;
      this.currentStrategy = find(this.strategies, [
        "strategyCode",
        this.currentStrategyCode
      ]);
      this.updateGraph();
      this.loadEvents();
    },

    loadStrategyList() {
      HTTP.get("/strategies/").then(response => {
        this.strategies = response.data;
        if (this.strategies.length > 0) {
          this.currentStrategy = find(this.strategies, [
            "strategyCode",
            this.currentStrategyCode
          ]);
        }
      });
    },
    loadEvents() {
      HTTP.get("/strategies/" + this.currentStrategyCode + "/events").then(
        response => {
          this.events = response.data;
        }
      );
    },
    updateStrategy(strategyCode) {
      HTTP.get("/strategies/" + strategyCode).then(response => {
        this.currentStrategy = response.data;
        this.currentStrategyCode = this.currentStrategy.strategyCode;
      });
    },
    updateGraph() {
      HTTP.get("/strategies/" + this.currentStrategyCode + "/history")
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
              },
              names: {
                value: "Encours"
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
            },
            legend: {
              show: false
            },
            tooltip: {
              format: {
                title: function(d) {
                  return d.toLocaleDateString();
                },
                value: function(value, ratio, id) {
                  return Math.round(value * 100) / 100 + " €";
                }
                //            value: d3.format(',') // apply this format to both y and y2
              }
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
    this.currentStrategyCode = this.$route.params.strategyCode;
    this.loadStrategyList();
    this.updateGraph();
    this.loadEvents();
  },
  mounted() {},
  components: {
    VueC3
  }
};
</script>

<style scoped>
.green-perf {
  font-weight: bold;
  color: green;
}
.red-perf {
  font-weight: bold;
  color: red;
}
</style>