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
                     id="strategy">
            <md-option v-for="strat in strategies"
                       :key="strat.id"
                       :value="strat.strategyCode">{{ strat.strategyCode }}</md-option>
          </md-select>
        </md-field>
      </div>

    </div>

    <!-- First line -->
    <div class="md-layout md-gutter md-layout-item md-size-100">

      <div class="md-layout-item md-size-20">
        <div v-if="currentStrategy">
          <div class="md-layout">
            Code : {{ currentStrategy.strategyCode }}
          </div>
          <div class="md-layout">
            CAGR : {{ 100 * currentStrategy.cagr | formatNb }} %
          </div>
          <div class="md-layout">
            UlcerIndex : {{ currentStrategy.ulcerIndex | formatNb }} %
          </div>
        </div>
      </div>

      <div class="md-layout-item md-size-80">
        <vue-c3 :handler="handler"></vue-c3>
      </div>
    </div>

    <!-- Second line -->
    <div class="md-layout md-gutter md-layout-item">
      <div class="md-layout-item">

        <md-button class="md-raised md-primary"
                   @click="loadAnotherOne">Primary</md-button>
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
      strategies: []
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
    },
    loadAnotherOne() {
      this.updateStrategy("GEM");

      this.updateGraph("GEM");
    },
    updateStrategy(strategyCode) {
      HTTP.get("/strategies/" + strategyCode).then(response => {
        this.currentStrategy = response.data;
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
    HTTP.get("/strategies/").then(response => {
      this.strategies = response.data;
      if (this.strategies.length > 0) {
        this.currentStrategy = this.strategies[0];
        this.currentStrategyCode = this.currentStrategy.strategyCode;
        this.updateGraph(this.strategies[0].strategyCode);
      }
    });
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
