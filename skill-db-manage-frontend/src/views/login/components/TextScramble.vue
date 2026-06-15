<template>
  <span ref="elRef" class="text-typewriter" :style="style">{{ displayText }}</span>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'

const props = defineProps({
  text: { type: String, required: true },
  typeSpeed: { type: Number, default: 80 },
  startDelay: { type: Number, default: 0 },
  fontSize: { type: String, default: '' },
  color: { type: String, default: '' }
})

const elRef = ref(null)
const displayText = ref('')
let timer = null

const style = computed(() => ({
  ...(props.fontSize ? { fontSize: props.fontSize } : {}),
  ...(props.color ? { color: props.color } : {})
}))

onMounted(() => {
  if (!elRef.value || !props.text) return

  setTimeout(() => {
    let index = 0
    function typeNext() {
      if (index >= props.text.length) return
      displayText.value += props.text[index]
      index++
      timer = setTimeout(typeNext, props.typeSpeed)
    }
    typeNext()
  }, props.startDelay)
})

onBeforeUnmount(() => {
  if (timer) clearTimeout(timer)
})
</script>

<style scoped>
.text-typewriter {
  display: inline;
  white-space: pre-wrap;
}
</style>
