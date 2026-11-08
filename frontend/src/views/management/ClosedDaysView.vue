<script setup lang="ts">
import { CalendarOff, ChevronDown, LockOpen } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCloseDays, useClosedDays, useReopenDays } from '@/api/closedDays'
import { errorMessageKey } from '@/api/errors'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { clubToday } from '@/lib/age'
import {
  CLOSED_DAY_LABEL_MAX,
  closedPeriods,
  closureSettings,
  emptyClosureForm,
  splitPeriods,
  type ClosedPeriod,
  type ClosureForm,
} from '@/lib/closedDays'
import { formatDate } from '@/lib/format'
import { focusFirstInvalid, type FieldErrors, type Message } from '@/lib/forms'

import ManagementTitle from './ManagementTitle.vue'

/**
 * D-116, board and admin: the days the gym is closed. A free session day closed here is not
 * waited for by the "Mois complet" badge, and every planning shows it.
 */
const { t } = useI18n()
const today = clubToday()
const closedDays = useClosedDays()
const close = useCloseDays()
const reopen = useReopenDays()

const periods = computed(() => splitPeriods(closedPeriods(closedDays.data.value ?? []), today))

const form = ref<ClosureForm>(emptyClosureForm())
const errors = ref<FieldErrors<keyof ClosureForm>>({})
const notice = ref<string | null>(null)
const failure = ref<unknown>(null)

function message(error: Message | undefined): string | undefined {
  return error && t(error.key, error.params ?? {})
}

function describe(from: string, to: string): string {
  return from === to
    ? t('closedDays.single', { date: formatDate(from) })
    : t('closedDays.range', { from: formatDate(from), to: formatDate(to) })
}

function submit() {
  const result = closureSettings(form.value)
  if ('errors' in result) {
    errors.value = result.errors
    focusFirstInvalid()
    return
  }
  errors.value = {}
  const { from, to } = result.settings
  close.mutate(result.settings, {
    onSuccess: () => {
      failure.value = null
      notice.value = t('closedDays.closed', { period: describe(from, to ?? from) })
      form.value = emptyClosureForm()
    },
    onError: (error) => {
      notice.value = null
      failure.value = error
    },
  })
}

const reopening = ref<ClosedPeriod | null>(null)
const confirmOpen = ref(false)
function askReopen(period: ClosedPeriod) {
  reopening.value = period
  confirmOpen.value = true
}
function confirmReopen() {
  const period = reopening.value
  if (period === null) return
  reopen.mutate(
    { from: period.from, to: period.to },
    {
      onSuccess: () => {
        failure.value = null
        notice.value = t('closedDays.reopened', { period: describe(period.from, period.to) })
      },
      onError: (error) => {
        notice.value = null
        failure.value = error
      },
    },
  )
}
</script>

<template>
  <ManagementTitle>{{ t('closedDays.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('closedDays.intro') }}</p>
  <div class="flex flex-col gap-3 px-4 pb-4 lg:px-0" aria-live="polite">
    <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
    <FormAlert v-if="failure">{{ t(errorMessageKey(failure)) }}</FormAlert>
  </div>

  <div
    class="flex flex-col gap-10 px-4 pb-10 lg:grid lg:grid-cols-[minmax(0,2fr)_minmax(0,3fr)] lg:items-start lg:px-0"
  >
    <section aria-labelledby="fermer" class="flex flex-col gap-4">
      <h2 id="fermer" class="text-lg font-title">{{ t('closedDays.formTitle') }}</h2>
      <form class="flex flex-col gap-4" novalidate @submit.prevent="submit">
        <div class="grid grid-cols-1 gap-4 min-[420px]:grid-cols-2">
          <TextField
            v-model="form.from"
            type="date"
            :label="t('closedDays.from')"
            min="2000-01-01"
            :error="message(errors.from)"
          />
          <TextField
            v-model="form.to"
            type="date"
            :label="t('closedDays.to')"
            :min="form.from || '2000-01-01'"
            :hint="t('closedDays.toHint')"
            :error="message(errors.to)"
            optional
          />
        </div>
        <TextField
          v-model="form.label"
          :label="t('closedDays.label')"
          :hint="t('closedDays.labelHint')"
          :placeholder="t('closedDays.labelPlaceholder')"
          :maxlength="CLOSED_DAY_LABEL_MAX"
          :error="message(errors.label)"
          optional
        />
        <GgButton type="submit" class="self-start" :disabled="close.isPending.value">
          <CalendarOff :size="20" aria-hidden="true" />
          {{ t('closedDays.submit') }}
        </GgButton>
      </form>
    </section>

    <section aria-labelledby="a-venir" class="flex flex-col gap-2">
      <h2 id="a-venir" class="text-lg font-title">{{ t('closedDays.upcoming') }}</h2>
      <div v-if="closedDays.isPending.value" class="flex flex-col gap-2" aria-hidden="true">
        <SkeletonBlock v-for="index in 3" :key="index" class="h-14 w-full" />
      </div>
      <div v-else-if="closedDays.isError.value" class="flex flex-col items-start gap-3">
        <FormAlert>{{ t(errorMessageKey(closedDays.error.value)) }}</FormAlert>
        <GgButton variant="secondary" @click="closedDays.refetch()">{{
          t('common.retry')
        }}</GgButton>
      </div>
      <template v-else>
        <p v-if="periods.upcoming.length === 0" class="text-ink-muted">
          {{ t('closedDays.empty') }}
        </p>
        <ul v-else class="border-t border-rule">
          <li
            v-for="period in periods.upcoming"
            :key="period.from"
            class="flex items-center gap-3 border-b border-rule py-2"
          >
            <div class="min-w-0 flex-1">
              <p class="font-semibold first-letter:uppercase">
                {{ describe(period.from, period.to) }}
              </p>
              <p class="text-sm text-ink-muted">
                <span :class="{ italic: !period.label }">{{
                  period.label ?? t('closedDays.noLabel')
                }}</span>
                <span aria-hidden="true"> · </span>
                <span class="tabular-nums">{{ t('closedDays.days', period.days) }}</span>
              </p>
            </div>
            <GgIconButton
              :label="t('closedDays.reopen', { period: describe(period.from, period.to) })"
              @click="askReopen(period)"
            >
              <LockOpen :size="20" aria-hidden="true" />
            </GgIconButton>
          </li>
        </ul>

        <details v-if="periods.past.length > 0" class="group mt-4">
          <summary
            class="inline-flex min-h-11 cursor-pointer items-center gap-1 font-semibold text-ink hover:underline"
          >
            {{ t('closedDays.past', { n: periods.past.length }) }}
            <ChevronDown
              :size="18"
              class="transition-transform duration-150 group-open:rotate-180 motion-reduce:transition-none"
              aria-hidden="true"
            />
          </summary>
          <ul class="mt-1 border-t border-rule">
            <li
              v-for="period in periods.past"
              :key="period.from"
              class="flex items-center gap-3 border-b border-rule py-2 text-ink-muted"
            >
              <div class="min-w-0 flex-1">
                <p class="first-letter:uppercase">{{ describe(period.from, period.to) }}</p>
                <p class="text-sm">
                  {{ period.label ?? t('closedDays.noLabel') }}
                  <span aria-hidden="true"> · </span>
                  <span class="tabular-nums">{{ t('closedDays.days', period.days) }}</span>
                </p>
              </div>
              <GgIconButton
                :label="t('closedDays.reopen', { period: describe(period.from, period.to) })"
                @click="askReopen(period)"
              >
                <LockOpen :size="20" aria-hidden="true" />
              </GgIconButton>
            </li>
          </ul>
        </details>
      </template>
    </section>
  </div>

  <ConfirmDialog
    v-model:open="confirmOpen"
    :title="t('closedDays.reopenTitle')"
    :body="
      reopening
        ? t('closedDays.reopenBody', { period: describe(reopening.from, reopening.to) })
        : ''
    "
    :confirm-label="t('closedDays.reopenConfirm')"
    @confirm="confirmReopen"
  />
</template>
