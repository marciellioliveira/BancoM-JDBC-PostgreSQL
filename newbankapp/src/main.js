import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap';  // Para garantir que os componentes JS do Bootstrap funcionem

createApp(App).use(router).mount('#app')