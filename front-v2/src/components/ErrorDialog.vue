<!-- Template ------------------------------------------------------------------------------------------->
<template>
  <v-dialog v-model="dialog" width="500">
    <v-card>
      <v-card-title class="headline red lighten-2" primary-title>Erreur</v-card-title>

      <v-card-text>
        <br />
        <pre>
        {{errorResponse.response.data}}
        </pre>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <div class="flex-grow-1"></div>
        <v-btn color="primary" text @click.stop="dialog = false">Fermer</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<!-- Script ------------------------------------------------------------------------------------------->
<script>
export default {
  name: "ErrorDialog",
  props: { errorResponse: Error, value: Boolean },

  computed: {
    dialog: {
      get() {
        return this.value;
      },
      set(value) {
        this.$emit("input", value);
      }
    }
  }
};
</script>

<!-- CSS ------------------------------------------------------------------------------------------->
<style scoped>
</style>