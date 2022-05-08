<template>
  <div class="home">
    <b-card-group columns>
      <b-card v-for="img in imglist" :key="img"
        :img-src="'/uploads/' + img"
        img-alt="Image"
        img-top
      >
      </b-card>
    </b-card-group>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: "HomePage",
  data() {
    return {
      imglist: []
    }
  },
  mounted() {
    axios({
      method: 'GET',
      url: '/api/image',
      responseType: 'json'
    })
      .then(res => {
        this.imglist = res.data.imglist
      })
  }
};
</script>

<style scoped>

</style>
