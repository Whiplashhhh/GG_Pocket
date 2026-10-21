import { nextTick } from 'vue'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { authStateQuery } from '@/api/auth'
import { queryClient } from '@/api/queryClient'
import { i18n } from '@/i18n'
import type { Role } from '@/lib/route'
import { reloadOnStaleChunk } from '@/lib/stale-chunk'
import ActuView from '@/views/ActuView.vue'

import { decideNavigation } from './guard'

declare module 'vue-router' {
  interface RouteMeta {
    /** i18n key of the page title (browser tab). */
    titleKey?: Parameters<typeof i18n.global.t>[0]
    /** Shows the floating "Nouvelle séance" button. */
    fab?: boolean
    /** Reachable without being signed in. */
    public?: boolean
    /** Signed-in members are sent to the app instead (welcome, sign-in, registration). */
    guestOnly?: boolean
    /** "bare": no header nor tab bar (welcome, sign-in, onboarding…). */
    layout?: 'app' | 'bare'
    /** Wider content area, for management tables on large screens. */
    wide?: boolean
    /** Lowest role allowed (spec §5); the server checks it again on every call. */
    minRole?: Role
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
    path: '/seance/nouvelle',
    component: () => import('@/views/NewSessionView.vue'),
    meta: { titleKey: 'newSession.fab' },
  },

  // Profile and settings
  {
    path: '/profil',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: { titleKey: 'profile.title' },
  },
  {
    path: '/profil/modifier',
    component: () => import('@/views/profile/ProfileEditView.vue'),
    meta: { titleKey: 'profileEdit.title' },
  },
  {
    path: '/profil/mot-de-passe',
    component: () => import('@/views/profile/PasswordView.vue'),
    meta: { titleKey: 'password.title' },
  },
  {
    path: '/profil/appareils',
    component: () => import('@/views/profile/DevicesView.vue'),
    meta: { titleKey: 'devices.title' },
  },
  {
    path: '/profil/installer',
    component: () => import('@/views/profile/InstallView.vue'),
    meta: { titleKey: 'install.title' },
  },

  // Doors of the app, without header nor tabs
  {
    path: '/accueil',
    component: () => import('@/views/auth/WelcomeView.vue'),
    meta: { titleKey: 'welcome.title', public: true, guestOnly: true, layout: 'bare' },
  },
  {
    path: '/connexion',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { titleKey: 'login.title', public: true, guestOnly: true, layout: 'bare' },
  },
  {
    path: '/inscription',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { titleKey: 'register.title', public: true, guestOnly: true, layout: 'bare' },
  },
  {
    path: '/nouveau-mot-de-passe',
    component: () => import('@/views/auth/ResetPasswordView.vue'),
    meta: { titleKey: 'reset.title', public: true, layout: 'bare' },
  },
  {
    path: '/tes-donnees',
    component: () => import('@/views/auth/PrivacyView.vue'),
    meta: { titleKey: 'privacy.title', public: true, layout: 'bare' },
  },
  {
    path: '/premiers-pas/:step(photo|infos|installer)',
    component: () => import('@/views/onboarding/OnboardingView.vue'),
    meta: { titleKey: 'welcome.title', layout: 'bare' },
  },

  // Management space (spec §8.10, §8.11)
  {
    path: '/gestion',
    component: () => import('@/views/management/ManagementLayout.vue'),
    meta: { titleKey: 'management.title', minRole: 'SETTER', wide: true },
    children: [
      {
        path: '',
        component: () => import('@/views/management/ManagementHomeView.vue'),
        meta: { titleKey: 'management.title', minRole: 'SETTER', wide: true },
      },
      {
        path: 'voies',
        component: () => import('@/views/management/RoutesPlaceholderView.vue'),
        meta: { titleKey: 'management.routes', minRole: 'SETTER', wide: true },
      },
      {
        path: 'membres',
        component: () => import('@/views/management/MembersView.vue'),
        meta: { titleKey: 'members.title', minRole: 'BOARD', wide: true },
      },
      {
        path: 'membres/:id',
        component: () => import('@/views/management/MemberDetailView.vue'),
        meta: { titleKey: 'members.title', minRole: 'BOARD', wide: true },
      },
      {
        path: 'journal',
        component: () => import('@/views/management/AuditView.vue'),
        meta: { titleKey: 'audit.title', minRole: 'BOARD', wide: true },
      },
      {
        path: 'codes',
        component: () => import('@/views/management/RegistrationCodesView.vue'),
        meta: { titleKey: 'codes.title', minRole: 'ADMIN', wide: true },
      },
    ],
  },

  ...(import.meta.env.DEV
    ? [
        {
          path: '/dev/galerie',
          component: () => import('@/views/dev/GalleryView.vue'),
          meta: { titleKey: 'gallery.title', public: true },
        } satisfies RouteRecordRaw,
      ]
    : []),
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { titleKey: 'notFound.title', public: true },
  },
]

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: (_to, _from, savedPosition) => savedPosition ?? { top: 0 },
})

router.beforeEach(async (to) => {
  // Whatever is cached is good enough: sign-ins, sign-outs and 401s keep it up to date.
  // Unreachable or failing server: navigate anyway, the app shell shows what went wrong.
  const state = await queryClient
    .query({ ...authStateQuery, staleTime: 'static' })
    .catch(() => 'unknown' as const)
  return decideNavigation(to, state)
})

// A screen's code went missing with a release (installed app left open): load the page afresh.
router.onError((error, to) => {
  reloadOnStaleChunk(error, to.fullPath)
})

router.afterEach((to, from) => {
  const appName = i18n.global.t('app.name')
  document.title = to.meta.titleKey ? `${i18n.global.t(to.meta.titleKey)} · ${appName}` : appName

  // After an in-app navigation, move focus to the new content so screen readers announce it.
  // Not on the first load: the browser already starts at the top of the page.
  if (from.matched.length > 0 && to.path !== from.path) {
    void nextTick(() => document.getElementById('contenu')?.focus({ preventScroll: true }))
  }
})
