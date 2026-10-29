<script setup lang="ts">
import { Check, UserX } from 'lucide-vue-next'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useDirectory, type MemberCard } from '@/api/social'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { belayerChoices } from '@/lib/belayers'

/**
 * Who held the rope on a route (D-101): anyone in the club, my friends first, or nobody said.
 * One sheet for the whole session sheet; the line being changed says who is ticked.
 */
const props = defineProps<{ selectedId: string | null }>()
const emit = defineEmits<{ pick: [member: MemberCard | null] }>()
const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const directory = useDirectory()
const search = ref('')
const choices = computed(() => belayerChoices(directory.data.value?.members ?? [], search.value))
const searching = computed(() => search.value.trim() !== '')
const found = computed(() => choices.value.friends.length + choices.value.others.length)
const groups = computed(() =>
  [
    { key: 'friends', title: t('belayerPicker.friends'), members: choices.value.friends },
    { key: 'others', title: t('belayerPicker.others'), members: choices.value.others },
  ].filter((group) => group.members.length > 0),
)

watch(open, (value) => {
  if (value) search.value = ''
})

function pick(member: MemberCard | null) {
  emit('pick', member)
  open.value = false
}

const row =
  'flex min-h-14 w-full items-center gap-3 rounded-control px-2 py-2 text-left hover:bg-surface-sunken'
</script>

<template>
  <GgDialog
    v-model:open="open"
    :title="t('belayerPicker.title')"
    :description="t('belayerPicker.description')"
    :initial-focus="['[aria-pressed=\'true\']', '[data-nobody]']"
  >
    <div class="flex flex-col gap-3">
      <TextField
        v-model="search"
        type="search"
        inputmode="search"
        autocomplete="off"
        :label="t('belayerPicker.search')"
        optional
        :show-optional-mark="false"
      />
      <p v-if="searching" class="sr-only" role="status">
        {{ t('belayerPicker.results', found) }}
      </p>
      <button
        type="button"
        :class="row"
        :aria-pressed="props.selectedId === null"
        data-nobody
        @click="pick(null)"
      >
        <span
          class="inline-flex size-10 shrink-0 items-center justify-center rounded-full bg-surface-sunken"
          aria-hidden="true"
        >
          <UserX :size="20" />
        </span>
        <span class="flex-1 font-semibold">{{ t('belayerPicker.nobody') }}</span>
        <Check v-if="props.selectedId === null" :size="20" aria-hidden="true" />
      </button>

      <div v-if="directory.isPending.value" class="flex flex-col gap-2" aria-hidden="true">
        <SkeletonBlock v-for="n in 4" :key="n" class="h-12 w-full" />
      </div>
      <div v-else-if="directory.isError.value" class="flex flex-col items-start gap-2">
        <FormAlert>{{ t(errorMessageKey(directory.error.value)) }}</FormAlert>
        <GgButton variant="secondary" @click="directory.refetch()">{{
          t('common.retry')
        }}</GgButton>
      </div>
      <p v-else-if="found === 0" class="text-ink-muted">{{ t('belayerPicker.noMatch') }}</p>
      <section
        v-for="group in groups"
        :key="group.key"
        class="flex flex-col gap-1"
        :aria-label="group.title"
      >
        <h3 class="font-semibold text-ink-muted">{{ group.title }}</h3>
        <ul class="flex flex-col">
          <li v-for="member in group.members" :key="member.id">
            <button
              type="button"
              :class="row"
              :aria-pressed="props.selectedId === member.id"
              @click="pick(member)"
            >
              <UserAvatar
                :first-name="member.firstName"
                :last-name="member.lastName"
                :seed="member.nickname"
                :src="member.avatarUrl"
                :size="40"
              />
              <span class="flex min-w-0 flex-1 flex-col">
                <span class="truncate font-semibold"
                  >{{ member.firstName }} {{ member.lastName }}</span
                >
                <span class="truncate text-sm text-ink-muted">@{{ member.nickname }}</span>
              </span>
              <Check v-if="props.selectedId === member.id" :size="20" aria-hidden="true" />
            </button>
          </li>
        </ul>
      </section>
    </div>
  </GgDialog>
</template>
