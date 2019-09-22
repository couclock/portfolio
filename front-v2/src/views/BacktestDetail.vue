<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-row>
    <v-col cols="12">
      <BacktestResults v-if="backtest" :backtest="backtest"></BacktestResults>

      <v-row>
        <v-col cols="4">
          <BacktestMetadata v-if="backtest" :backtest="backtest"></BacktestMetadata>
        </v-col>

        <v-col cols="8">
          <v-card>
            <v-card-title>Courbe</v-card-title>
            <v-card-text>I'm card text</v-card-text>
          </v-card>
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-col cols="6">
          <v-card>
            <v-card-title>Transactions</v-card-title>
            <v-card-text>I'm card text</v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import BacktestMetadata from "@/components/BacktestMetadata";
import BacktestResults from "@/components/BacktestResults";

export default {
  name: "backtestDetail",
  data() {
    return {
      backtest: null
    };
  },
  computed: {},
  methods: {},
  created() {
    this.axios
      .get("/backtests/" + this.$route.params.id)
      .then(response => {
        // eslint-disable-next-line
        console.log("ok : ", response);
        this.backtest = response.data;
      })
      .catch(err => {
        // eslint-disable-next-line
        console.log(err);
      })
      .finally(() => {});
  },
  components: {
    BacktestMetadata,
    BacktestResults
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>