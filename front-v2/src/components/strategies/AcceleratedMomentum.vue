<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-row justify="space-around">
    <v-col cols="3">
      <SelectStock tag="us" title="Ex Us Stock" v-model="strategyParameters.usStock"></SelectStock>
    </v-col>
    <v-col cols="3">
      <SelectStock tag="exus" title="Ex Us Stock" v-model="strategyParameters.exUsStock"></SelectStock>
    </v-col>
    <v-col cols="3">
      <SelectStock tag="bond" title="Bond" v-model="strategyParameters.bondStock"></SelectStock>
    </v-col>
  </v-row>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
import SelectStock from "@/components/strategies/accelerated-momentum/SelectStock";

export default {
  name: "AcceleratedMomentum",
  props: { value: Object }, // Mandatory : structure {valid: Boolean, settings: Object }
  data() {
    return {
      strategyParameters: {
        className:
          "com.couclock.portfolio.entity.strategies.AcceleratedMomentumParameters",
        usStock:
          this.value.settings && this.value.settings.usStock
            ? this.value.settings.usStock
            : null,
        exUsStock:
          this.value.settings && this.value.settings.exUsStock
            ? this.value.settings.exUsStock
            : null,
        bondStock:
          this.value.settings && this.value.settings.bondStock
            ? this.value.settings.bondStock
            : null
      }
    };
  },
  computed: {},
  methods: {
    checkSettings() {
      // eslint-disable-next-line
      console.log("checkSettings : ", this.strategyParameters);
      if (
        this.strategyParameters.usStock &&
        this.strategyParameters.exUsStock &&
        this.strategyParameters.bondStock
      ) {
        this.$emit("input", {
          valid: true,
          strategyParameters: this.strategyParameters
        });
      } else {
        this.$emit("input", {
          valid: false,
          strategyParameters: this.strategyParameters
        });
      }
    }
  },
  watch: {
    "strategyParameters.usStock": function() {
      this.checkSettings();
    },
    "strategyParameters.exUsStock": function() {
      this.checkSettings();
    },
    "strategyParameters.bondStock": function() {
      this.checkSettings();
    }
  },
  components: {
    SelectStock
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>