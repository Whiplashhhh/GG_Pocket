import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import ActuView from '@/views/ActuView.vue'
import { i18n } from '@/i18n'

declare module 'vue-router' {
  interface RouteMeta {
    /** i18n key of the page title (browser tab). */
    titleKey?: Parameters<typeof i18n.global.t>[0]
    /** Shows the floating "Nouvelle séance" button. */
    fab?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/actu' },
  { path: '/actu', component: ActuView, meta: { titleKey: 'nav.actu', fab: true } },
  {
    path: '/topo',
    component: () => import('@/views/TopoView.vue'),
    meta: { titleKey: 'nav.topo' },
  },
  {
    path: '/a-faire',
    component: () => import('@/views/TodoView.vue'),
    meta: { titleKey: 'nav.todo' },
  },
  {
    path: '/planning',
    component: () => import('@/views/PlanningView.vue'),
    meta: { titleKey: 'nav.planning', fab: true },
  },
  {
    path: '/podium',
    component: () => import('@/views/PodiumView.vue'),
    meta: { titleKey: 'nav.podium' },
  },
  {
    path: '/profil',
    component: () => import('@/views/ProfileView.vue'),
    meta: { titleKey: 'profile.title' },
  },
  {
    path: '/seance/nouvelle',
    component: () => import('@/views/NewSessionView.vue'),
    meta: { titleKey: 'newSession.fab' },
  },
  ...(import.meta.env.DEV
    ? [
        {
          path: '/dev/galerie',
          component: () => import('@/views/dev/GalleryView.vue'),
          meta: { titleKey: 'gallery.title' },
        } satisfies RouteRecordRaw,
      ]
    : []),
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { titleKey: 'notFound.title' },
  },
]

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: (_to, _from, savedPosition) => savedPosition ?? { top: 0 },
})

router.afterEach((to) => {
  const appName = i18n.global.t('app.name')
  document.title = to.meta.titleKey ? `${i18n.global.t(to.meta.titleKey)} · ${appName}` : appName
})
