<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-card>
    <v-card-title>
      Paramètres du backtest &nbsp;&nbsp;
      <v-icon
        class="mr-2"
        @click="$router.push({name:'backtest-settings-update', params:{id:backtest.id}})"
      >mdi-settings</v-icon>
    </v-card-title>
    <v-card-text>
      <v-row>
        <v-col cols="6">Stratégie</v-col>
        <v-col cols="6">{{ backtest.strategyCode}}</v-col>
        <v-col cols="6">Début du backtest</v-col>
        <v-col cols="6">{{ backtest.startDate | formatDate}}</v-col>
        <v-col cols="6">Montant de départ</v-col>
        <v-col cols="6">{{ backtest.startMoney}} €</v-col>
        <v-col cols="6">ETF US</v-col>
        <v-col cols="6">{{ backtest.strategyParameters.usStock}}</v-col>
        <v-col cols="6">ETF hors-US</v-col>
        <v-col cols="6">{{ backtest.strategyParameters.exUsStock}}</v-col>
        <v-col cols="6">ETF Obligations</v-col>
        <v-col cols="6">{{ backtest.strategyParameters.bondStock}}</v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import { format, parseISO } from "date-fns";

export default {
  name: "BacktestMetadata",
  props: { backtest: Object },
  data() {
    return {};
  },
  filters: {
    formatDate: function(value) {
      if (!value) {
        return "";
      }
      value = format(parseISO(value), "dd/MM/yyyy");
      return value;
    }
  },
  computed: {},
  methods: {}
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>