<template>
  <div id="app">
    <b-container fluid id="router-view">
      <home-page />
    </b-container>
  </div>
</template>

<script>
import HomePage from '@/components/HomePage.vue'

export default {
  name: 'App',
  components: {
    HomePage
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */
  color: #2c3e50;
}

#router-view {
  margin-top: 25px;
  margin-bottom: 25px;
  width: 80%;
}

#user-bar {
  text-align: center;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
