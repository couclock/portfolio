<template>

  <div class="md-layout">

    <md-card class="md-layout-item ">
      <md-card-header>
        <div class="md-title">Add a stock</div>
      </md-card-header>

      <md-card-content>
        <div class="md-layout md-gutter md-alignment-top-center">

          <div class="md-subheading error-line md-layout-item md-size-100 md-theme-default"
               v-if="errorMsg">{{ errorMsg }}</div>

          <div class="md-layout-item md-size-100 md-layout">
            <md-field class="md-layout-item">
              <label>New stock code</label>
              <md-input v-model="newStockCode"></md-input>
            </md-field>
            <div class="md-layout-item md-size-5">
            </div>
            <md-chips class="md-layout-item"
                      v-model="newStockTags"
                      md-placeholder="Add tag..."></md-chips>

          </div>

          <div class="md-layout md-alignment-top-center">
            <md-button class="md-raised"
                       @click="closeDialog">Cancel</md-button>
            <md-button class="md-raised md-primary"
                       @click="addStock">Add stock</md-button>
          </div>
        </div>
      </md-card-content>
    </md-card>
  </div>

</template>

<script>
import { HTTP } from "@/http-constants";
import map from "lodash/map";
import filter from "lodash/filter";

export default {
  name: "StockFormDialog",
  data() {
    return {
      newStockCode: undefined,
      newStockTags: [],
      errorMsg: undefined
    };
  },

  methods: {
    closeDialog() {
      this.$emit("closeDialog");
    },
    addStock() {
      HTTP.post("/stocks", {
        code: this.newStockCode,
        tags: this.newStockTags
      })
        .then(() => {
          this.$emit("stockAdded");
          this.$emit("closeDialog");
        })
        .catch(error => {
          console.error(error.response);
          this.errorMsg =
            error.response && error.response.data && error.response.data.message
              ? "ERROR : " + error.response.data.message
              : error;
        });
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss" >
@import "~vue-material/dist/theme/engine.scss";
@include md-register-theme("default");

div.error-line {
  background-color: md-theme(accent);
  color: white;
}
</style>
