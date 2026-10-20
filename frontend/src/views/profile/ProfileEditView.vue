<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useMyProfile, useUpdateProfile } from '@/api/me'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import ProfileDetailsFields from '@/components/profile/ProfileDetailsFields.vue'
import ProfilePhotoPicker from '@/components/profile/ProfilePhotoPicker.vue'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { clubToday, needsParentalConsent } from '@/lib/age'
import {
  EMAIL_PATTERN,
  focusFirstInvalid,
  NICKNAME_PATTERN,
  serverFieldErrors,
  type FieldErrors,
  type Message,
} from '@/lib/forms'
import { toForm, toUpdate, type ProfileForm } from '@/lib/profile'

const FIELDS = [
  'nickname',
  'firstName',
  'lastName',
  'email',
  'birthDate',
  'parentalConsent',
  'clubArrivalYear',
  'climbingSinceYear',
  'bio',
] as const

const { t } = useI18n()
const router = useRouter()
const profile = useMyProfile()
const update = useUpdateProfile()

const form = ref<ProfileForm | null>(null)
const errors = ref<FieldErrors<keyof ProfileForm>>({})
const general = ref<Message | null>(null)
const today = clubToday()

watch(
  () => profile.data.value,
  (loaded) => {
    if (loaded && form.value === null) {
      form.value = toForm(loaded)
    }
  },
  { immediate: true },
)

/** Under 15 without a consent on record: the box must be ticked (spec §6). */
const consentNeeded = computed(
  () =>
    form.value !== null &&
    needsParentalConsent(form.value.birthDate, today) &&
    profile.data.value?.parentalConsent !== true,
)

function message(field: keyof ProfileForm): string | undefined {
  const error = errors.value[field]
  return error && t(error.key, error.params ?? {})
}

function submit() {
  const value = form.value
  if (value === null) {
    return
  }
  general.value = null
  const found: FieldErrors<keyof ProfileForm> = {}
  if (value.firstName.trim() === '') found.firstName = { key: 'validation.required' }
  if (value.lastName.trim() === '') found.lastName = { key: 'validation.required' }
  if (!NICKNAME_PATTERN.test(value.nickname.trim())) found.nickname = { key: 'validation.nickname' }
  if (value.email.trim() !== '' && !EMAIL_PATTERN.test(value.email.trim()))
    found.email = { key: 'validation.email' }
  if (value.birthDate !== '' && value.birthDate > today)
    found.birthDate = { key: 'validation.dateInFuture' }
  if (consentNeeded.value && !value.parentalConsent)
    found.parentalConsent = { key: 'validation.parentalConsent' }
  const result = toUpdate(value, new Date().getFullYear())
  errors.value = { ...found, ...('errors' in result ? result.errors : {}) }
  if (Object.keys(errors.value).length > 0 || 'errors' in result) {
    focusFirstInvalid()
    return
  }
  update.mutate(result.update, {
    onSuccess: () => void router.push('/profil'),
    onError: (error) => {
      const placed = serverFieldErrors(error, FIELDS)
      errors.value = placed.fields
      general.value = placed.general
      focusFirstInvalid()
    },
  })
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('profileEdit.title') }}
  </SubPageHeader>

  <div v-if="profile.isPending.value" class="flex flex-col gap-4 px-4 py-6" aria-hidden="true">
    <SkeletonBlock class="mx-auto size-32 rounded-full" />
    <SkeletonBlock class="h-12 w-full" />
    <SkeletonBlock class="h-12 w-full" />
  </div>
  <div v-else-if="profile.isError.value" class="px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(profile.error.value)) }}</FormAlert>
  </div>

  <template v-else-if="form">
    <section class="px-4 py-6" aria-labelledby="photo-title">
      <h2 id="photo-title" class="sr-only">{{ t('profileEdit.photo') }}</h2>
      <ProfilePhotoPicker />
    </section>

    <form class="flex flex-col gap-5 px-4 pb-10" novalidate @submit.prevent="submit">
      <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>

      <h2 class="border-t border-rule pt-5 text-lg font-title">{{ t('profileEdit.identity') }}</h2>
      <div class="grid grid-cols-1 gap-5 min-[26rem]:grid-cols-2">
        <TextField
          v-model="form.firstName"
          :label="t('register.firstName')"
          autocomplete="given-name"
          :maxlength="50"
          :error="message('firstName')"
        />
        <TextField
          v-model="form.lastName"
          :label="t('register.lastName')"
          autocomplete="family-name"
          :maxlength="50"
          :error="message('lastName')"
        />
      </div>
      <TextField
        v-model="form.nickname"
        :label="t('register.nickname')"
        :hint="t('register.nicknameHint')"
        autocomplete="username"
        :maxlength="20"
        :error="message('nickname')"
        verbatim
      />
      <TextField
        v-model="form.email"
        type="email"
        :label="t('register.email')"
        :hint="t('register.emailHint')"
        autocomplete="email"
        inputmode="email"
        :maxlength="254"
        :error="message('email')"
        optional
        verbatim
      />
      <TextField
        v-model="form.birthDate"
        type="date"
        :label="t('register.birthDate')"
        :hint="t('register.birthDateHint')"
        autocomplete="bday"
        min="1900-01-01"
        :max="today"
        :error="message('birthDate')"
        optional
      />
      <CheckboxField
        v-if="consentNeeded"
        v-model="form.parentalConsent"
        :hint="t('register.parentalConsentHint')"
        :error="message('parentalConsent')"
      >
        {{ t('register.parentalConsent') }}
      </CheckboxField>

      <h2 class="border-t border-rule pt-5 text-lg font-title">{{ t('profileEdit.about') }}</h2>
      <ProfileDetailsFields
        v-model:club-arrival-year="form.clubArrivalYear"
        v-model:climbing-since-year="form.climbingSinceYear"
        v-model:bio="form.bio"
        :errors="errors"
      />

      <h2 class="border-t border-rule pt-5 text-lg font-title">
        {{ t('profileEdit.visibility') }}
      </h2>
      <CheckboxField v-model="form.showAge">{{ t('profileFields.showAge') }}</CheckboxField>
      <CheckboxField
        v-model="form.appearsInRankings"
        :hint="t('profileFields.appearsInRankingsHint')"
      >
        {{ t('profileFields.appearsInRankings') }}
      </CheckboxField>

      <GgButton type="submit" size="lg" block :disabled="update.isPending.value">
        {{ t('common.save') }}
      </GgButton>
    </form>
  </template>
</template>
