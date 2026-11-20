<script setup lang="ts">
import { BellRing } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { usePushSettings, useSavePushPreferences, type PushPreferences } from '@/api/push'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { usePushDevice } from '@/composables/usePushDevice'
import { isIos } from '@/lib/install'
import { pushSupport } from '@/lib/push'
import { isStandalone } from '@/stores/install'

/**
 * Notifications on this phone (D-128, D-130): turned on device by device, then chosen kind by
 * kind. An iPhone needs the installed app first; a « no » given to the browser is undone there.
 */
const { t } = useI18n()
const device = usePushDevice()
const settings = usePushSettings(() => device.endpoint.value)
const save = useSavePushPreferences()
const busy = ref(false)
const failure = ref<unknown>(null)
const refused = ref(false)

const support = computed(() =>
  pushSupport({
    hasPushApi: 'serviceWorker' in navigator && 'PushManager' in window,
    permission: device.permission.value,
    ios: isIos(navigator.userAgent, navigator.maxTouchPoints),
    standalone: isStandalone(),
    serverKey: settings.data.value?.publicKey,
  }),
)
const on = computed(
  () =>
    device.endpoint.value !== undefined &&
    settings.data.value?.thisDevice === true &&
    device.permission.value === 'granted',
)

const KINDS = ['comments', 'replies', 'friendRequests', 'reactions', 'friendSessions'] as const

function toggle(kind: (typeof KINDS)[number], value: boolean) {
  const current = settings.data.value?.preferences
  if (current === undefined) return
  const next: PushPreferences = { ...current, [kind]: value }
  save.mutate(next)
}

async function turnOn() {
  const key = settings.data.value?.publicKey
  if (key === undefined) return
  busy.value = true
  failure.value = null
  refused.value = false
  try {
    refused.value = !(await device.enable(key))
    await settings.refetch()
  } catch (error) {
    failure.value = error
  } finally {
    busy.value = false
  }
}

async function turnOff() {
  busy.value = true
  failure.value = null
  try {
    await device.disable()
    await settings.refetch()
  } catch (error) {
    failure.value = error
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('push.title') }}
  </SubPageHeader>
  <p class="px-4 pt-2 pb-4 text-ink-muted">{{ t('push.intro') }}</p>

  <div v-if="settings.isPending.value || !device.loaded.value" class="px-4" aria-hidden="true">
    <SkeletonBlock class="h-24 w-full" />
  </div>
  <div v-else-if="settings.isError.value" class="flex flex-col items-start gap-3 px-4">
    <FormAlert>{{ t(errorMessageKey(settings.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="settings.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <template v-else>
    <section class="mx-4 rounded-paper border border-rule bg-surface p-4" aria-live="polite">
      <p v-if="support === 'unsupported'">{{ t('push.unsupported') }}</p>
      <template v-else-if="support === 'needsInstall'">
        <p>{{ t('push.needsInstall') }}</p>
        <GgButton class="mt-3" variant="secondary" to="/profil/installer">{{
          t('push.install')
        }}</GgButton>
      </template>
      <p v-else-if="support === 'denied'">{{ t('push.denied') }}</p>
      <template v-else-if="on">
        <p class="flex items-center gap-2 font-semibold">
          <BellRing :size="20" aria-hidden="true" />{{ t('push.on') }}
        </p>
        <GgButton class="mt-3" variant="secondary" :disabled="busy" @click="turnOff">{{
          t('push.turnOff')
        }}</GgButton>
      </template>
      <template v-else>
        <p>{{ t('push.off') }}</p>
        <GgButton class="mt-3" :disabled="busy" @click="turnOn">{{ t('push.turnOn') }}</GgButton>
      </template>
      <p v-if="refused" class="mt-3 text-sm font-semibold text-danger" role="alert">
        {{ t('push.refused') }}
      </p>
      <FormAlert v-if="failure" class="mt-3">{{ t('push.failed') }}</FormAlert>
    </section>

    <section
      v-if="settings.data.value?.publicKey !== undefined"
      class="px-4 pt-6"
      aria-labelledby="push-kinds"
    >
      <h2 id="push-kinds" class="text-lg font-title">{{ t('push.kindsTitle') }}</h2>
      <p class="pb-2 text-sm text-ink-muted">{{ t('push.kindsHint') }}</p>
      <CheckboxField
        v-for="kind in KINDS"
        :key="kind"
        :model-value="settings.data.value?.preferences[kind] ?? false"
        @update:model-value="(value: boolean) => toggle(kind, value)"
        >{{ t(`push.kinds.${kind}`) }}</CheckboxField
      >
      <FormAlert v-if="save.error.value" class="mt-2">{{
        t(errorMessageKey(save.error.value))
      }}</FormAlert>
    </section>
  </template>
</template>
