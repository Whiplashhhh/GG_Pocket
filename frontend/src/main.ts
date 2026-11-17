import './styles/main.css'

import { VueQueryPlugin } from '@tanstack/vue-query'
import { createPinia } from 'pinia'
import { createApp } from 'vue'

import { queryClient } from './api/queryClient'
import App from './App.vue'
import { i18n } from './i18n'
import { pageToOpen } from './lib/pushWorker'
import { router } from './router'
import { listenForInstallPrompt } from './stores/install'

// Before anything else: the browser may offer installation right away.
listenForInstallPrompt()

// A tapped notification, the app already open: the service worker asks it to show the page.
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.addEventListener('message', (event: MessageEvent) => {
    const page = pageToOpen(event.data)
    if (page !== undefined) void router.push(page)
  })
}

createApp(App)
  .use(createPinia())
  .use(i18n)
  .use(router)
  .use(VueQueryPlugin, { queryClient })
  .mount('#app')
