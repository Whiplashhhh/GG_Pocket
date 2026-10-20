<script setup lang="ts">
import { useClipboard } from '@vueuse/core'
import { Ellipsis, Share, SquarePlus } from 'lucide-vue-next'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'

import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import { useInstallStore } from '@/stores/install'

/**
 * How to install the app from this very browser (spec §12, DECISIONS D-041): the native prompt,
 * the iOS Share steps, or how to leave an in-app browser.
 */
const emit = defineEmits<{ installed: [] }>()

/** The Share button, its "⋯" fallback, then "Sur l'écran d'accueil", drawn as on the iPhone. */
const IOS_STEPS = [
  { text: 'install.iosStep1', icons: [Share, Ellipsis] },
  { text: 'install.iosStep2', icons: [SquarePlus] },
  { text: 'install.iosStep3', icons: [] },
] as const

const { t } = useI18n()
const store = useInstallStore()
const { method, justInstalled } = storeToRefs(store)
const { copy, copied, isSupported: canCopy } = useClipboard({ copiedDuring: 2500 })
const appAddress = window.location.origin

async function install() {
  if (await store.install()) {
    emit('installed')
  }
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <FormAlert v-if="justInstalled" tone="success">{{ t('install.installed') }}</FormAlert>

    <p v-else-if="method === 'installed'">{{ t('install.alreadyInstalled') }}</p>

    <template v-else-if="method === 'prompt'">
      <p>{{ t('install.why') }}</p>
      <GgButton size="lg" block @click="install">{{ t('install.installButton') }}</GgButton>
    </template>

    <template v-else-if="method === 'ios'">
      <p>{{ t('install.why') }}</p>
      <p class="font-semibold">{{ t('install.iosIntro') }}</p>
      <ol class="flex flex-col gap-3">
        <li v-for="(step, index) in IOS_STEPS" :key="step.text" class="flex gap-3">
          <span
            class="inline-flex size-8 shrink-0 items-center justify-center rounded-full bg-ink font-bold text-bg"
            aria-hidden="true"
            >{{ index + 1 }}</span
          >
          <span class="flex-1">
            {{ t(step.text) }}
            <span
              v-if="step.icons.length"
              class="mt-1 flex gap-2 text-ink-muted"
              aria-hidden="true"
            >
              <component :is="icon" v-for="(icon, at) in step.icons" :key="at" :size="22" />
            </span>
          </span>
        </li>
      </ol>
      <p class="text-sm text-ink-muted">{{ t('install.iosAfter') }}</p>
    </template>

    <template v-else-if="method === 'in-app-browser'">
      <p class="font-semibold">{{ t('install.inAppTitle') }}</p>
      <p>{{ t('install.inAppBody') }}</p>
      <GgButton v-if="canCopy" variant="secondary" @click="copy(appAddress)">
        {{ copied ? t('common.copied') : t('install.copyLink') }}
      </GgButton>
    </template>

    <template v-else>
      <p>{{ t('install.why') }}</p>
      <p>{{ method === 'mac-safari' ? t('install.macSafari') : t('install.menu') }}</p>
    </template>
  </div>
</template>
