<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

import DoorHeader from '@/components/auth/DoorHeader.vue'
import GgButton from '@/components/ui/GgButton.vue'

/** Spec §6: the club logo, a catchphrase, « Se connecter » and « Rejoindre le club » (D-099). */
const { t } = useI18n()
const route = useRoute()
</script>

<template>
  <div class="flex flex-1 flex-col bg-inserts">
    <DoorHeader :title="t('app.name')" :intro="t('welcome.tagline')" large />
    <div class="mt-auto flex flex-col gap-3 bg-bg/90 px-4 pt-6 pb-8">
      <GgButton :to="{ path: '/connexion', query: route.query }" size="lg" block>
        {{ t('welcome.signIn') }}
      </GgButton>
      <GgButton to="/inscription" variant="secondary" size="lg" block>
        {{ t('welcome.join') }}
      </GgButton>
      <RouterLink
        to="/tes-donnees"
        class="mt-2 inline-flex min-h-11 items-center justify-center text-sm text-ink-muted underline underline-offset-4"
      >
        {{ t('welcome.privacyLink') }}
      </RouterLink>
    </div>
  </div>
</template>
