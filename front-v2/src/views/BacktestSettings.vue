<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-row>
    <v-col cols="12">
      <!-- Strategy selector ------------------------------------------------------------------------->
      <v-row justify="center">
        <v-col cols="8">
          <v-select
            outlined
            v-model="currentStrategy"
            :items="strategies"
            clearable
            label="Stratégie"
            @change="updateStrategyForm"
          ></v-select>
        </v-col>
      </v-row>
      <!-- Common settings ------------------------------------------------------------------------->
      <v-row justify="space-around">
        <v-col cols="4">
          <v-text-field label="Montant initial" type="number" v-model="startMoney"></v-text-field>
        </v-col>
        <v-col cols="4">
          <v-text-field label="Date de début" type="date" v-model="startDate"></v-text-field>
        </v-col>
      </v-row>

      <!-- Strategy specific settings ------------------------------------------------------------------------->
      <v-row justify="center">
        <v-col cols="12">
          <component :is="currentStrategySettings" v-model="settings"></component>
        </v-col>
      </v-row>

      <!-- Button ------------------------------------------------------------------------->
      <v-row justify="center">
        <v-col cols="4">
          <v-btn
            block
            :loading="isLoading"
            color="primary"
            :disabled="!isFormValid || isLoading"
            @click="saveBacktestSettings"
          >Lancer le backtest</v-btn>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import AcceleratedMomentum from "@/components/strategies/AcceleratedMomentum.vue";

export default {
  name: "backtestSettings",
  data() {
    return {
      isLoading: false,
      strategies: ["ACCELERATED_MOMENTUM"],
      currentStrategy: undefined,
      currentStrategySettings: undefined,
      settings: { valid: false },
      startMoney: 10000,
      startDate: "2016-01-01"
    };
  },
  computed: {
    isFormValid() {
      return this.settings.valid && this.startMoney && this.startDate;
    }
  },
  methods: {
    updateStrategyForm: function(selectedStrategy) {
      this.currentStrategy = selectedStrategy;
      if (selectedStrategy === "ACCELERATED_MOMENTUM") {
        this.currentStrategySettings = AcceleratedMomentum;
      } else {
        this.currentStrategySettings = undefined;
      }
    },
    saveBacktestSettings() {
      this.isLoading = true;
      this.axios
        .post("/backtests/", {
          id: this.$route.params.id,
          startMoney: this.startMoney,
          startDate: this.startDate,
          strategyCode: this.currentStrategy,
          strategyParameters: this.settings.strategyParameters
        })
        .then(response => {
          // eslint-disable-next-line
          console.log("ok : ", response);
        })
        .catch(err => {
          // eslint-disable-next-line
          console.log(err);
        })
        .finally(() => (this.isLoading = false));
    }
  },
  created() {
    if (this.$route.params.id) {
      this.axios
        .get("/backtests/" + this.$route.params.id)
        .then(response => {
          this.startMoney = response.data.startMoney;
          this.startDate = response.data.startDate;
          this.settings.valid = true;
          this.settings.strategyParameters = response.data.strategyParameters;
          this.updateStrategyForm(response.data.strategyCode);
        })
        .catch(err => {
          // eslint-disable-next-line
          console.log(err);
        })
        .finally(() => {});
    }
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>