<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import { useAuthState } from '@/api/auth'
import DoorHeader from '@/components/auth/DoorHeader.vue'

/** Spec §14: what is collected, why, who sees it, how long, who is responsible, and the rights. */
const { t } = useI18n()
const { data: auth } = useAuthState()

const SECTIONS = [
  ['privacy.whoTitle', 'privacy.whoBody'],
  ['privacy.whatTitle', 'privacy.whatBody'],
  ['privacy.whyTitle', 'privacy.whyBody'],
  ['privacy.whoSeesTitle', 'privacy.whoSeesBody'],
  ['privacy.minorsTitle', 'privacy.minorsBody'],
  ['privacy.howLongTitle', 'privacy.howLongBody'],
  ['privacy.cookiesTitle', 'privacy.cookiesBody'],
  ['privacy.rightsTitle', 'privacy.rightsBody'],
] as const
</script>

<template>
  <DoorHeader
    :title="t('privacy.title')"
    :intro="t('privacy.intro')"
    :back="auth?.authenticated ? '/profil' : '/accueil'"
    :back-label="t('privacy.back')"
  />
  <article class="flex flex-col pb-10">
    <section
      v-for="[title, body] in SECTIONS"
      :key="title"
      class="border-t border-rule px-4 py-5"
      :aria-labelledby="title"
    >
      <h2 :id="title" class="mb-2 text-lg font-title">{{ t(title) }}</h2>
      <p class="max-w-prose">{{ t(body) }}</p>
    </section>
  </article>
</template>
