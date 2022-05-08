import Vue from 'vue'
import { BootstrapVue, IconsPlugin, CardPlugin } from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

import App from './App.vue'

Vue.use(BootstrapVue)
Vue.use(IconsPlugin)
Vue.use(CardPlugin)

Vue.config.productionTip = false

new Vue({
  render: h => h(App),
}).$mount('#app')
