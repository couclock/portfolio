<template>
  <div class="home">
    <div>
      <vue-c3 :handler="handler"></vue-c3>
    </div>

  </div>
</template>

<script>
// @ is an alias to /src
import HelloWorld from "@/components/HelloWorld.vue";
import Vue from "vue";
import VueC3 from "vue-c3";
import { HTTP } from "@/http-constants";

export default {
  name: "home",
  data() {
    return {
      handler: new Vue()
    };
  },
  created() {
    HTTP.get("/strategies/GEM/history")
      .then(response => {
        const options = {
          size: {
            height: 500,
            width: 1500
          },
          data: {
            json: response.data,
            keys: {
              x: "date",
              value: ["value"]
            }
          },
          axis: {
            x: {
              type: "timeseries",
              tick: {
                count: 10,
                format: "%Y-%m"
              }
            }
          },
          zoom: {
            enabled: true
          },
          point: {
            show: false
          }
        };
        this.handler.$emit("init", options);
      })
      .catch(response => {
        console.error("ERROR : ", response);
        this.error = true;
      });
  },
  mounted() {},
  components: {
    HelloWorld,
    VueC3
  }
};
</script>

