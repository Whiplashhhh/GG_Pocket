<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useReact } from '@/api/feed'
import type { Session } from '@/api/sessions'
import ToggleChip from '@/components/ui/ToggleChip.vue'
import { nextReaction, REACTION_EMOJI, type ReactionKind } from '@/lib/reactions'

/**
 * The four reactions of a session (spec §8.2). On someone else's session, four toggles: one
 * reaction per person, changed or taken back with a tap. On mine, only what the others gave.
 */
const props = defineProps<{ session: Session; mine: boolean }>()

const { t } = useI18n()
const react = useReact(() => props.session)

const counts = computed(() => props.session.reactions.counts)
const given = computed(() => counts.value.filter((entry) => entry.count > 0))

function tap(kind: ReactionKind) {
  react.mutate(nextReaction(props.session.reactions.mine, kind))
}
</script>

<template>
  <div v-if="!mine" class="flex flex-col gap-1">
    <ul class="flex flex-wrap gap-2" :aria-label="t('reactions.groupLabel')">
      <li v-for="entry in counts" :key="entry.kind">
        <ToggleChip
          :pressed="session.reactions.mine === entry.kind"
          :aria-label="
            t('reactions.give', { name: t(`reactions.names.${entry.kind}`), count: entry.count })
          "
          @toggle="tap(entry.kind)"
        >
          <span aria-hidden="true" class="text-lg leading-none">{{
            REACTION_EMOJI[entry.kind]
          }}</span>
          <span v-if="entry.count > 0" aria-hidden="true" class="tabular-nums">{{
            entry.count
          }}</span>
        </ToggleChip>
      </li>
    </ul>
    <p v-if="react.isError.value" role="alert" class="text-sm font-semibold text-danger">
      {{ t('reactions.failed') }} {{ t(errorMessageKey(react.error.value)) }}
    </p>
  </div>
  <p v-else-if="given.length > 0" class="flex flex-wrap gap-3">
    <span
      v-for="entry in given"
      :key="entry.kind"
      class="inline-flex items-center gap-1 font-semibold"
    >
      <span aria-hidden="true" class="text-lg leading-none">{{ REACTION_EMOJI[entry.kind] }}</span>
      <span class="tabular-nums" aria-hidden="true">{{ entry.count }}</span>
      <span class="sr-only">{{
        t('reactions.received', { name: t(`reactions.names.${entry.kind}`), count: entry.count })
      }}</span>
    </span>
  </p>
</template>
