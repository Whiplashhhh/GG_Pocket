<script setup lang="ts">
import { useClipboard } from '@vueuse/core'
import { Pencil, Plus } from 'lucide-vue-next'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import {
  useCreateRegistrationCode,
  useRegistrationCodes,
  useUpdateRegistrationCode,
  type RegistrationCode,
} from '@/api/admin'
import { errorMessageKey } from '@/api/errors'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { codeSettings, toCodeForm, type CodeForm } from '@/lib/codes'
import { formatDate } from '@/lib/format'
import { focusFirstInvalid, type FieldErrors } from '@/lib/forms'

import ManagementTitle from './ManagementTitle.vue'

/** Spec §6 and §8.11: the club codes that open registration, admin only. */
const { t } = useI18n()
const codes = useRegistrationCodes()
const create = useCreateRegistrationCode()
const update = useUpdateRegistrationCode()
const { copy, isSupported: canCopy } = useClipboard()

const editing = ref<RegistrationCode | 'new' | null>(null)
const open = ref(false)
const form = ref<CodeForm>(toCodeForm(undefined))
const errors = ref<FieldErrors<keyof CodeForm>>({})
const notice = ref<string | null>(null)
const failure = ref<unknown>(null)

function startCreate() {
  editing.value = 'new'
  form.value = toCodeForm(undefined)
  errors.value = {}
  open.value = true
}

function startEdit(code: RegistrationCode) {
  editing.value = code
  form.value = toCodeForm(code)
  errors.value = {}
  open.value = true
}

function save() {
  const result = codeSettings(form.value)
  if ('errors' in result) {
    errors.value = result.errors
    focusFirstInvalid()
    return
  }
  const target = editing.value
  const onError = (error: unknown) => {
    failure.value = error
    open.value = false
  }
  if (target === 'new') {
    create.mutate(result.settings, {
      onSuccess: (created) => {
        failure.value = null
        notice.value = t('codes.created', { code: created.code })
        open.value = false
      },
      onError,
    })
  } else if (target !== null) {
    update.mutate(
      { id: target.id, settings: result.settings },
      {
        onSuccess: () => {
          failure.value = null
          notice.value = t('codes.updated')
          open.value = false
        },
        onError,
      },
    )
  }
}

/** The copy is announced by the notice area, read out by screen readers too. */
async function copyAndSay(text: string) {
  await copy(text)
  failure.value = null
  notice.value = t('codes.copiedNotice')
}

function signUpLink(code: RegistrationCode): string {
  return `${window.location.origin}/inscription?code=${encodeURIComponent(code.code)}`
}

function uses(code: RegistrationCode): string {
  return code.maxUses === undefined
    ? t('codes.uses', code.useCount)
    : t('codes.usesOf', { count: code.useCount, max: code.maxUses })
}
</script>

<template>
  <ManagementTitle>{{ t('codes.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('codes.intro') }}</p>
  <div class="flex flex-col gap-3 px-4 pb-4 lg:px-0" aria-live="polite">
    <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
    <FormAlert v-if="failure">{{ t(errorMessageKey(failure)) }}</FormAlert>
    <GgButton class="self-start" @click="startCreate">
      <Plus :size="20" aria-hidden="true" />
      {{ t('codes.create') }}
    </GgButton>
  </div>

  <div v-if="codes.isPending.value" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <SkeletonBlock v-for="index in 2" :key="index" class="h-24 w-full" />
  </div>
  <div v-else-if="codes.isError.value" class="px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(codes.error.value)) }}</FormAlert>
  </div>
  <EmptyState v-else-if="(codes.data.value ?? []).length === 0" :title="t('codes.empty')" body="" />

  <ul v-else class="border-t border-rule">
    <li
      v-for="code in codes.data.value"
      :key="code.id"
      class="flex flex-col gap-3 border-b border-rule px-4 py-4 lg:flex-row lg:items-center lg:gap-6 lg:px-0"
    >
      <div class="flex min-w-0 flex-1 items-start justify-between gap-3">
        <div class="flex min-w-0 flex-col">
          <p
            class="text-3xl font-cord tracking-wide"
            :class="{ 'text-ink-muted line-through': code.state !== 'USABLE' }"
          >
            {{ code.code }}
          </p>
          <p class="font-semibold">{{ code.label }}</p>
          <p class="text-sm text-ink-muted">
            {{ uses(code) }} ·
            {{
              code.expiresAt
                ? t('codes.expires', { date: formatDate(code.expiresAt) })
                : t('codes.noExpiry')
            }}
          </p>
        </div>
        <div class="flex shrink-0 items-center gap-1">
          <span
            class="rounded-control border px-1.5 py-px text-micro font-semibold"
            :class="
              code.state === 'USABLE' ? 'border-success text-success' : 'border-rule text-ink-muted'
            "
            >{{ t(`codes.states.${code.state}`) }}</span
          >
          <GgIconButton :label="t('codes.editNamed', { code: code.code })" @click="startEdit(code)">
            <Pencil :size="20" aria-hidden="true" />
          </GgIconButton>
        </div>
      </div>
      <div v-if="canCopy && code.state === 'USABLE'" class="flex flex-wrap gap-2">
        <GgButton
          variant="secondary"
          :aria-label="t('codes.copyCodeNamed', { code: code.code })"
          @click="copyAndSay(code.code)"
        >
          {{ t('codes.copyCode') }}
        </GgButton>
        <GgButton variant="secondary" @click="copyAndSay(signUpLink(code))">
          {{ t('codes.copyLink') }}
        </GgButton>
      </div>
    </li>
  </ul>

  <GgDialog
    v-model:open="open"
    :title="
      editing === 'new' || editing === null
        ? t('codes.createTitle')
        : t('codes.editTitle', { code: editing.code })
    "
  >
    <form class="flex flex-col gap-5" novalidate @submit.prevent="save">
      <TextField
        v-model="form.label"
        :label="t('codes.label')"
        :hint="t('codes.labelHint')"
        :maxlength="80"
        :error="errors.label && t(errors.label.key)"
      />
      <TextField
        v-model="form.expiresOn"
        type="date"
        :label="t('codes.expiresAt')"
        :hint="t('codes.expiresAtHint')"
        :error="errors.expiresOn && t(errors.expiresOn.key)"
        optional
      />
      <TextField
        v-model="form.maxUses"
        :label="t('codes.maxUses')"
        :hint="t('codes.maxUsesHint')"
        inputmode="numeric"
        :maxlength="5"
        :error="errors.maxUses && t(errors.maxUses.key)"
        optional
      />
      <CheckboxField v-model="form.active">{{ t('codes.active') }}</CheckboxField>
      <div class="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <GgButton variant="secondary" @click="open = false">{{ t('common.cancel') }}</GgButton>
        <GgButton type="submit" :disabled="create.isPending.value || update.isPending.value">
          {{ t('common.save') }}
        </GgButton>
      </div>
    </form>
  </GgDialog>
</template>
