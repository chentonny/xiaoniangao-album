import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

const app = createApp(App)
const pinia = createPinia()

// 确保先使用Pinia，然后再使用其他插件
app.use(pinia)
app.use(ElementPlus)
app.use(router)

app.mount('#app')