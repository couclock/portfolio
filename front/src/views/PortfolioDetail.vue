<template>
  <div class="md-layout">

    <!-- Select line -->
    <div class="md-layout md-gutter md-layout-item md-alignment-center-center">
      <div class="md-layout-item md-size-20">

        <md-field v-if="currentPortfolio">
          <label for="portfolio">Portfolio</label>
          <md-select v-model="currentPFCode"
                     @md-selected="loadPortfolio"
                     name="portfolio"
                     md-dense
                     id="portfolio">
            <md-option v-for="pf in portfolios"
                       :key="pf.id"
                       :value="pf.code">{{ pf.code }}</md-option>
          </md-select>
        </md-field>
      </div>

    </div>

    <!-- Graph line -->
    <div class="md-layout md-gutter md-layout-item md-size-100">

      <div class="md-layout-item md-size-20">
        <div v-if="currentPortfolio">
          <div class="md-layout">
            Code : {{ currentPortfolio.code }}
          </div>
          <div class="md-layout">
            Last date : {{ currentPortfolio.endDate }}
          </div>
          <div class="md-layout">
            CAGR : {{ 100 * currentPortfolio.cagr | formatNb }} %
          </div>
          <div class="md-layout">
            UlcerIndex : {{ currentPortfolio.ulcerIndex | formatNb }} %
          </div>
          <div class="md-layout"
               v-for="stat in currentPortfolio.statistics"
               :key="stat.stockCode">
            {{ stat.stockCode }} : {{ 100 * stat.performance | formatNb }} % ({{ stat.dayCount }} days)
          </div>
          <div class="md-layout">
            {{ currentPortfolio.endStatus | pretty}}
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
        <md-table v-model="currentPortfolio.periods"
                  v-if="currentPortfolio && currentPortfolio.periods.length > 0"
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
      currentPFCode: undefined,
      currentPortfolio: undefined,
      portfolios: [],
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
    },
    pretty: function(value) {
      return JSON.stringify(value, null, 2);
    }
  },
  methods: {
    loadPortfolio(newPortfolio) {
      this.$router.push({
        name: "portfolioDetail",
        params: { code: newPortfolio }
      });
      this.currentPFCode = newPortfolio;
      this.currentPortfolio = find(this.portfolios, [
        "code",
        this.currentPFCode
      ]);
      this.updateGraph();
      this.loadEvents();
    },

    loadPortfolioList() {
      HTTP.get("/portfolios/").then(response => {
        this.portfolios = response.data;
        if (this.portfolios.length > 0) {
          this.currentPortfolio = find(this.portfolios, [
            "code",
            this.currentPFCode
          ]);
        }
      });
    },
    loadEvents() {
      HTTP.get("/portfolios/" + this.currentPFCode + "/events").then(
        response => {
          this.events = response.data;
        }
      );
    },
    updateStrategy(strategyCode) {
      HTTP.get("/portfolios/" + strategyCode).then(response => {
        this.currentPortfolio = response.data;
        this.currentPFCode = this.currentPortfolio.code;
      });
    },
    updateGraph() {
      HTTP.get("/portfolios/" + this.currentPFCode + "/history")
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
    this.currentPFCode = this.$route.params.code;
    this.loadPortfolioList();
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
