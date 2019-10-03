<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-card>
    <v-card-title>Les transactions</v-card-title>
    <v-card-text>
      <v-simple-table>
        <template v-slot:default>
          <thead>
            <tr>
              <th class="text-left">ETF</th>
              <th class="text-center" colspan="2">Achat</th>
              <th class="text-center" colspan="2">Vente</th>
              <th class="text-center">Variation</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in orderedTx" :key="item.buyDate">
              <td>{{ item.stock.code }}</td>
              <td>{{ item.buyDate | formatDate}}</td>
              <td class="text-right">{{ item.buyValue | formatNb}} €</td>
              <td>{{ item.sellDate | formatDate}}</td>
              <td class="text-right">
                <span v-if="item.sellValue">{{ item.sellValue | formatNb}} €</span>
              </td>
              <td class="text-right">
                <span v-if="item.sellValue">
                  <v-chip
                    class="ma-2"
                    color="red"
                    text-color="white"
                    v-if="item.buyValue >= item.sellValue"
                  >{{ item | getVariation | formatNb}} %</v-chip>
                  <v-chip
                    class="ma-2"
                    color="green"
                    text-color="white"
                    v-if="item.buyValue < item.sellValue"
                  >{{ item | getVariation | formatNb}} %</v-chip>
                </span>
              </td>
            </tr>
          </tbody>
        </template>
      </v-simple-table>
    </v-card-text>
  </v-card>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import { format, parseISO } from "date-fns";

export default {
  name: "BacktestTransactions",
  props: { backtest: Object },
  data() {
    return {};
  },
  computed: {
    orderedTx: function() {
      return this.lodash.orderBy(this.backtest.transactions, "buyDate", "desc");
    }
  },
  filters: {
    formatDate: function(value) {
      if (!value) {
        return "";
      }
      value = format(parseISO(value), "dd/MM/yyyy");
      return value;
    },
    getVariation: function(item) {
      let res = (item.sellValue - item.buyValue) / item.buyValue;
      return res * 100;
    },
    formatNb: function(value) {
      if (!value) {
        return "";
      }
      value = Math.round(value * 100) / 100;
      return value;
    }
  },
  methods: {}
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>