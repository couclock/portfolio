<template>
  <div class="home">
    <img alt="Vue logo" src="../assets/logo.png" />
    <v-btn
      :loading="loading"
      :disabled="loading"
      large
      block
      color="primary"
      @click="updateStocks"
    >Update stocks</v-btn>
  </div>
</template>

<script>
export default {
  name: "home",
  data() {
    return {
      loading: false
    };
  },
  methods: {
    updateStocks() {
      this.loading = true;
      this.axios
        .post("/stocks/refresh")
        .then(response => {
          // eslint-disable-next-line
          console.log("ok : ", response);
        })
        .catch(err => {
          // eslint-disable-next-line
          console.log(err);
        })
        .finally(() => {
          this.loading = false;
        });
    }
  },
  components: {}
};
</script>
