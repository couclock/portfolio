<template>
  <v-row justify="center">
    <v-col cols="8">
      <v-data-table
        :loading="loading"
        loading-text="Loading... Please wait"
        :headers="headers"
        :items="backtestList"
        :items-per-page="10"
        class="elevation-1"
      >
        <template v-slot:item.action="{ item }">
          <v-icon
            small
            class="mr-2"
            @click="$router.push({name:'backtest-detail', params:{id:item.id}})"
          >mdi-eye</v-icon>
          <v-icon
            small
            class="mr-2"
            @click="$router.push({name:'backtest-settings-update', params:{id:item.id}})"
          >mdi-pencil</v-icon>
          <v-icon small class="mr-2">mdi-content-copy</v-icon>
          <v-icon small color="error" @click="deleteBacktest(item.id)">mdi-delete</v-icon>
        </template>
      </v-data-table>
    </v-col>
    <v-btn
      class="fab-button"
      color="pink"
      dark
      absolute
      bottom
      right
      fab
      @click="$router.push('backtest-settings')"
    >
      <v-icon>mdi-plus</v-icon>
    </v-btn>
  </v-row>
</template>

<script>
export default {
  name: "backtestList",
  data() {
    return {
      loading: true,
      headers: [
        {
          text: "Name",
          align: "left",
          sortable: true,
          value: "name"
        },
        { text: "Strategy", value: "strategyCode" },
        { text: "End date", value: "endDate" },
        { text: "CAGR", value: "cagr" },
        { text: "Action", value: "action", sortable: false }
      ],
      backtestList: []
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
    loadBacktestList() {
      this.loading = true;
      this.axios.get("/backtests/").then(response => {
        this.backtestList = response.data;
        this.loading = false;
      });
    },
    deleteBacktest(backtestId) {
      this.axios
        .delete("/backtests/" + backtestId)
        .then(response => {
          // eslint-disable-next-line
          console.log("ok deleted : ", response);
          this.loadBacktestList();
        })
        .catch(err => {
          // eslint-disable-next-line
          console.log(err);
        });
    }
  },
  created() {
    this.loadBacktestList();
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
.v-btn--fab.v-size--default.v-btn--absolute.v-btn--bottom.fab-button {
  bottom: 15px;
}
</style>