<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import TextField from '@/components/ui/TextField.vue'
import type { FieldErrors } from '@/lib/forms'
import type { ProfileDetails } from '@/lib/profile'

/** The optional details of a profile, shared by the onboarding and the profile editor. */
defineProps<{ errors: FieldErrors<keyof ProfileDetails> }>()
const clubArrivalYear = defineModel<string>('clubArrivalYear', { required: true })
const climbingSinceYear = defineModel<string>('climbingSinceYear', { required: true })
const bio = defineModel<string>('bio', { required: true })
const { t } = useI18n()
</script>

<template>
  <div class="grid grid-cols-2 gap-4">
    <TextField
      v-model="clubArrivalYear"
      :label="t('profileFields.clubArrivalYear')"
      :placeholder="t('profileFields.yearPlaceholder')"
      inputmode="numeric"
      :maxlength="4"
      :error="errors.clubArrivalYear && t(errors.clubArrivalYear.key)"
      optional
    />
    <TextField
      v-model="climbingSinceYear"
      :label="t('profileFields.climbingSinceYear')"
      :placeholder="t('profileFields.yearPlaceholder')"
      inputmode="numeric"
      :maxlength="4"
      :error="errors.climbingSinceYear && t(errors.climbingSinceYear.key)"
      optional
    />
  </div>
  <TextField
    v-model="bio"
    :label="t('profileFields.bio')"
    :hint="t('profileFields.bioHint')"
    :maxlength="160"
    :error="errors.bio && t(errors.bio.key)"
    optional
  />
</template>
