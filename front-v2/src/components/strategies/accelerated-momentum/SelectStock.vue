<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-autocomplete
    v-model="model"
    :items="items"
    :loading="isLoading"
    :search-input.sync="search"
    hide-no-data
    hide-selected
    item-text="label"
    item-value="code"
    :label="title"
    clearable
    placeholder="Start typing to Search"
    prepend-icon="mdi-database-search"
    @change="updateSelectedStock"
  ></v-autocomplete>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
export default {
  name: "SelectStock",
  props: { title: String, tag: String, value: String },
  data: () => ({
    isLoading: false,
    entries: [],
    search: null,
    model: null
  }),
  computed: {
    items() {
      return this.entries.map(entry => {
        return Object.assign({}, entry, {
          label: entry.code + " - " + entry.name
        });
      });
    }
  },
  methods: {
    updateSelectedStock: function(selectedStock) {
      // eslint-disable-next-line
      console.log("updateSelectedStock : ", selectedStock);
      this.$emit("input", selectedStock);
    }
  },
  watch: {
    search() {
      // Items have already been loaded
      if (this.items.length > 0) return;

      // Items have already been requested
      if (this.isLoading) return;

      this.isLoading = true;

      // Lazily load input items
      this.axios
        .get("/stocks/by-tag/" + this.tag)
        .then(response => {
          this.entries = response.data;
          this.loading = false;
        })
        .catch(err => {
          // eslint-disable-next-line
          console.log(err);
        })
        .finally(() => (this.isLoading = false));
    }
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>