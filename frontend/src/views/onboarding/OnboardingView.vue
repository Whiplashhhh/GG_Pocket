<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { useCurrentMember } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useMyProfile, useUpdateProfile } from '@/api/me'
import { useSetFavoriteRoute } from '@/api/topo'
import InstallGuide from '@/components/install/InstallGuide.vue'
import ProfileDetailsFields from '@/components/profile/ProfileDetailsFields.vue'
import ProfilePhotoPicker from '@/components/profile/ProfilePhotoPicker.vue'
import RoutePicker from '@/components/topo/RoutePicker.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import { serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'
import { toForm, toUpdate, type ProfileDetails } from '@/lib/profile'
import { useInstallStore } from '@/stores/install'

/**
 * Right after registration (spec §6): photo, a few details, the favourite route (D-055), then
 * the invitation to install. Every step can be skipped.
 */
const STEPS = ['photo', 'infos', 'voie', 'installer'] as const
type Step = (typeof STEPS)[number]

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const member = useCurrentMember()
const profile = useMyProfile()
const update = useUpdateProfile()
const install = useInstallStore()
const setFavoriteRoute = useSetFavoriteRoute()

const step = computed(() => route.params.step as Step)
const position = computed(() => STEPS.indexOf(step.value) + 1)
const titleKey = computed(
  () =>
    ({
      photo: 'onboarding.photoTitle',
      infos: 'onboarding.infosTitle',
      voie: 'onboarding.favoriteTitle',
      installer: 'install.title',
    })[step.value],
)

const details = ref<ProfileDetails>({ clubArrivalYear: '', climbingSinceYear: '', bio: '' })
const errors = ref<FieldErrors<keyof ProfileDetails>>({})
const general = ref<Message | null>(null)
const favoriteRouteId = ref<string | undefined>(undefined)
watch(
  () => profile.data.value,
  (loaded) => {
    if (loaded) {
      favoriteRouteId.value ??= loaded.favoriteRouteId
      const form = toForm(loaded)
      details.value = {
        clubArrivalYear: form.clubArrivalYear,
        climbingSinceYear: form.climbingSinceYear,
        bio: form.bio,
      }
    }
  },
  { immediate: true },
)

function goTo(next: Step | 'done') {
  void router.push(next === 'done' ? '/actu' : `/premiers-pas/${next}`)
}

function saveDetails() {
  const loaded = profile.data.value
  if (loaded === undefined) {
    goTo('voie')
    return
  }
  general.value = null
  const result = toUpdate({ ...toForm(loaded), ...details.value }, new Date().getFullYear())
  if ('errors' in result) {
    errors.value = result.errors
    return
  }
  update.mutate(result.update, {
    onSuccess: () => {
      goTo('voie')
    },
    onError: (error) => {
      const placed = serverFieldErrors(error, [
        'clubArrivalYear',
        'climbingSinceYear',
        'bio',
      ] as const)
      errors.value = placed.fields
      general.value = placed.general
    },
  })
}

function saveFavoriteRoute() {
  const routeId = favoriteRouteId.value
  if (routeId === undefined || routeId === profile.data.value?.favoriteRouteId) {
    goTo('installer')
    return
  }
  setFavoriteRoute.mutate(routeId, {
    onSuccess: () => {
      goTo('installer')
    },
  })
}

/** Leaving the onboarding without installing counts as a first "Plus tard". */
function finish() {
  if (install.method !== 'installed' && !install.justInstalled) {
    install.postpone()
  }
  goTo('done')
}
</script>

<template>
  <div class="flex flex-1 flex-col px-4 pt-6 pb-8">
    <p class="text-sm font-semibold text-ink-muted">
      {{ t('onboarding.step', { current: position, total: STEPS.length }) }}
    </p>
    <div
      class="mt-2 flex gap-1"
      role="progressbar"
      :aria-valuenow="position"
      aria-valuemin="1"
      :aria-valuemax="STEPS.length"
      :aria-label="t('onboarding.step', { current: position, total: STEPS.length })"
    >
      <span
        v-for="index in STEPS.length"
        :key="index"
        class="h-1 flex-1 rounded-full"
        :class="index <= position ? 'bg-brand' : 'bg-surface-sunken'"
      />
    </div>
    <h1 class="mt-6 text-2xl font-title text-balance">{{ t(titleKey) }}</h1>

    <template v-if="step === 'photo'">
      <p class="mt-2 text-ink-muted">{{ t('onboarding.photoBody') }}</p>
      <div class="py-8"><ProfilePhotoPicker /></div>
      <div class="mt-auto flex flex-col gap-2">
        <GgButton v-if="member?.avatarUrl" size="lg" block @click="goTo('infos')">
          {{ t('onboarding.next') }}
        </GgButton>
        <GgButton variant="ghost" block @click="goTo('infos')">{{
          t('onboarding.later')
        }}</GgButton>
      </div>
    </template>

    <form
      v-else-if="step === 'infos'"
      class="flex flex-1 flex-col gap-5"
      novalidate
      @submit.prevent="saveDetails"
    >
      <p class="mt-2 text-ink-muted">{{ t('onboarding.infosBody') }}</p>
      <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>
      <ProfileDetailsFields
        v-model:club-arrival-year="details.clubArrivalYear"
        v-model:climbing-since-year="details.climbingSinceYear"
        v-model:bio="details.bio"
        :errors="errors"
      />
      <div class="mt-auto flex flex-col gap-2 pt-4">
        <GgButton type="submit" size="lg" block :disabled="update.isPending.value">
          {{ t('onboarding.next') }}
        </GgButton>
        <GgButton variant="ghost" block @click="goTo('voie')">
          {{ t('onboarding.later') }}
        </GgButton>
      </div>
    </form>

    <template v-else-if="step === 'voie'">
      <p class="mt-2 text-ink-muted">{{ t('onboarding.favoriteBody') }}</p>
      <div class="mt-5"><RoutePicker v-model="favoriteRouteId" /></div>
      <div v-if="setFavoriteRoute.isError.value" class="mt-4">
        <FormAlert>{{ t(errorMessageKey(setFavoriteRoute.error.value)) }}</FormAlert>
      </div>
      <div class="mt-auto flex flex-col gap-2 pt-8">
        <GgButton
          v-if="favoriteRouteId"
          size="lg"
          block
          :disabled="setFavoriteRoute.isPending.value"
          @click="saveFavoriteRoute"
        >
          {{ t('onboarding.favoriteSave') }}
        </GgButton>
        <GgButton variant="ghost" block @click="goTo('installer')">
          {{ t('onboarding.later') }}
        </GgButton>
      </div>
    </template>

    <template v-else>
      <div class="mt-4"><InstallGuide /></div>
      <div class="mt-auto flex flex-col gap-2 pt-8">
        <GgButton size="lg" block @click="finish">{{ t('onboarding.finish') }}</GgButton>
      </div>
    </template>
  </div>
</template>
