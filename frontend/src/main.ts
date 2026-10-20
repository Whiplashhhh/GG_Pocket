import './styles/main.css'

import { VueQueryPlugin } from '@tanstack/vue-query'
import { createPinia } from 'pinia'
import { createApp } from 'vue'

import { queryClient } from './api/queryClient'
import App from './App.vue'
import { i18n } from './i18n'
import { router } from './router'
import { listenForInstallPrompt } from './stores/install'

// Before anything else: the browser may offer installation right away.
listenForInstallPrompt()

createApp(App)
  .use(createPinia())
  .use(i18n)
  .use(router)
  .use(VueQueryPlugin, { queryClient })
  .mount('#app')
