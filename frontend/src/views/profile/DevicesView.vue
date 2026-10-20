<script setup lang="ts">
import { Laptop, Smartphone, Tablet } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useCloseDevice, useCloseOtherDevices, useMyDevices, type Device } from '@/api/me'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { formatDate, formatRelative } from '@/lib/format'

/** Spec §6 "Mes appareils": open sessions, close one or all the others. */
const { t } = useI18n()
const devices = useMyDevices()
const closeOne = useCloseDevice()
const closeOthers = useCloseOtherDevices()
const confirmingOthers = ref(false)

const others = computed(() => devices.data.value?.filter((device) => !device.current) ?? [])
const actionError = computed(() => closeOne.error.value ?? closeOthers.error.value)

function label(device: Device): string {
  return t('devices.label', {
    browser: t(`devices.browsers.${device.browser}`),
    platform: t(`devices.platforms.${device.platform}`),
  })
}

function icon(device: Device) {
  if (device.platform === 'IPHONE' || device.platform === 'ANDROID') return Smartphone
  if (device.platform === 'IPAD') return Tablet
  return Laptop
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('devices.title') }}
  </SubPageHeader>
  <p class="px-4 pt-2 pb-4 text-ink-muted">{{ t('devices.intro') }}</p>

  <div v-if="devices.isPending.value" class="flex flex-col gap-3 px-4" aria-hidden="true">
    <SkeletonBlock class="h-16 w-full" />
    <SkeletonBlock class="h-16 w-full" />
  </div>
  <div v-else-if="devices.isError.value" class="flex flex-col items-start gap-3 px-4">
    <FormAlert>{{ t(errorMessageKey(devices.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="devices.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <template v-else-if="devices.data.value">
    <FormAlert v-if="actionError" class="mx-4 mb-3">{{
      t(errorMessageKey(actionError))
    }}</FormAlert>
    <ul class="border-t border-rule">
      <li
        v-for="device in devices.data.value"
        :key="device.id"
        class="flex items-center gap-3 border-b border-rule px-4 py-3"
      >
        <component
          :is="icon(device)"
          :size="28"
          class="shrink-0 text-ink-muted"
          aria-hidden="true"
        />
        <div class="flex min-w-0 flex-1 flex-col">
          <p class="font-semibold">
            {{ label(device) }}
            <span
              v-if="device.current"
              class="ml-1 rounded-control bg-olive px-1.5 py-px text-micro font-semibold text-on-olive"
              >{{ t('devices.current') }}</span
            >
          </p>
          <p class="text-sm text-ink-muted">
            {{ t('devices.signedIn', { date: formatDate(device.signedInAt) }) }} ·
            {{ t('devices.lastActive', { when: formatRelative(device.lastActiveAt) }) }}
          </p>
        </div>
        <GgButton
          v-if="!device.current"
          variant="ghost"
          :disabled="closeOne.isPending.value"
          :aria-label="t('devices.closeNamed', { device: label(device) })"
          @click="closeOne.mutate(device.id)"
        >
          {{ t('devices.close') }}
        </GgButton>
      </li>
    </ul>
    <div class="px-4 py-6">
      <p v-if="others.length === 0" class="text-ink-muted">{{ t('devices.onlyThis') }}</p>
      <GgButton
        v-else
        variant="secondary"
        :disabled="closeOthers.isPending.value"
        @click="confirmingOthers = true"
      >
        {{ t('devices.closeOthers') }}
      </GgButton>
    </div>
  </template>

  <ConfirmDialog
    v-model:open="confirmingOthers"
    :title="t('devices.closeOthersConfirmTitle')"
    :body="t('devices.closeOthersConfirmBody')"
    :confirm-label="t('devices.closeOthers')"
    @confirm="closeOthers.mutate()"
  />
</template>
