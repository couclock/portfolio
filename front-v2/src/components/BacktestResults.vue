<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-row>
    <v-col cols="3" align="center">
      <v-chip color="primary" label outlined large>
        <strong>Fin du backtest :</strong>
        {{ backtest.endDate | formatDate}}
      </v-chip>
    </v-col>
    <v-col cols="3" align="center">
      <v-chip color="green" label large text-color="white">
        <strong>CAGR :&nbsp;</strong>
        {{ backtest.cagr * 100 | formatNb }} %
      </v-chip>
    </v-col>
    <v-col cols="3" align="center">
      <v-chip
        color="green"
        large
        label
        text-color="white"
      >Valeur estimée : {{ backtest.estimatedValue | formatNb }} €</v-chip>
    </v-col>
    <v-col cols="3" align="center">
      <v-chip color="primary" label outlined large>
        <strong>Montant réel :&nbsp;</strong>
        {{ backtest.currentMoney | formatNb}} €
      </v-chip>
    </v-col>
  </v-row>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import { format, parseISO } from "date-fns";

export default {
  name: "BacktestResults",
  props: { backtest: Object },
  data() {
    return {};
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