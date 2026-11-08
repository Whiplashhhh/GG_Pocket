<script setup lang="ts">
import { I18nT, useI18n } from 'vue-i18n'

import { useAuthState } from '@/api/auth'
import DoorHeader from '@/components/auth/DoorHeader.vue'

/**
 * Spec §14: what is collected, why, who sees it, how long, who is responsible, and the rights. The
 * club has no other privacy policy: this page is it (D-119).
 */
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
      <I18nT :keypath="body" tag="p" class="max-w-prose" scope="global">
        <template #email>
          <a
            :href="`mailto:${t('privacy.email')}`"
            class="font-semibold whitespace-nowrap underline underline-offset-4"
            >{{ t('privacy.email') }}</a
          >
        </template>
      </I18nT>
    </section>
  </article>
</template>
