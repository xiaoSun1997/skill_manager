<template>
  <canvas ref="canvasRef" class="particle-bg"></canvas>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

const canvasRef = ref(null)
let animationId = null
let resizeObserver = null
let started = false

onMounted(async () => {
  await nextTick()
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  // ── Zone-based character sets ──
  const CHARS_A = 'ΞΨΩΔΠΣΦΘΛΓ≡≈∞∏∑√∫∂∇∈∉⊕⊗⊞⊟⊠⊡' +
    '启元道极虚灵玄幽穹冥乾坤震离坎兑炁爻'
  const CHARS_B = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%&{}[]<>/|!'
  const CHARS_C = 'abcdefghijklmnopqrstuvwxyz0123456789+-=_.:;,'

  const FONT_SIZE = 14
  const BASE_BRIGHTNESS = { r: 180, g: 180, b: 190 }

  const ZONE_A_MAX = 0.30
  const ZONE_B_MAX = 0.58

  function personalityForZone(zoneRatio) {
    if (zoneRatio < ZONE_A_MAX) {
      const r = Math.random() * 100
      if (r < 20) return 'stable'
      if (r < 55) return 'slow'
      if (r < 85) return 'jitter'
      return 'glitch'
    } else if (zoneRatio < ZONE_B_MAX) {
      const r = Math.random() * 100
      if (r < 55) return 'stable'
      if (r < 80) return 'slow'
      if (r < 95) return 'jitter'
      return 'glitch'
    } else {
      const r = Math.random() * 100
      if (r < 90) return 'stable'
      return 'slow'
    }
  }

  function charForZone(zoneRatio) {
    const pool = zoneRatio < ZONE_A_MAX ? CHARS_A
      : zoneRatio < ZONE_B_MAX ? CHARS_B
      : CHARS_C
    return pool[Math.floor(Math.random() * pool.length)]
  }

  const PERSONALITY = {
    stable: { min: 8000,  max: 12000, alphaMul: 0.55, colorMul: 0.75 },
    slow:   { min: 4000,  max: 7000,  alphaMul: 0.75, colorMul: 0.85 },
    jitter: { min: 800,   max: 2500,  alphaMul: 1.0,  colorMul: 1.0 },
    glitch: { min: 120,   max: 500,   alphaMul: 1.2,  colorMul: 1.35 }
  }

  function randomInterval(p) {
    return p.min + Math.random() * (p.max - p.min)
  }

  // ── Canvas state (mutated by initSize) ──
  let width = 0
  let height = 0
  let cols = 0
  let rows = 0
  let grid = []
  let cx = 0
  let cy = 0
  let maxDist = 0
  let posX = []
  let posY = []

  function getZoneRatio(i) {
    const dx = posX[i] - cx
    const dy = posY[i] - cy
    return Math.sqrt(dx * dx + dy * dy) / (maxDist || 1)
  }

  function rebuildPositions() {
    cx = width / 2
    cy = height / 2
    maxDist = Math.sqrt(cx * cx + cy * cy)
    for (let i = 0; i < grid.length; i++) {
      posX[i] = (i % cols) * FONT_SIZE + FONT_SIZE / 2
      posY[i] = Math.floor(i / cols) * FONT_SIZE
    }
  }

  function rebuildGrid() {
    cols = Math.floor(width / FONT_SIZE)
    rows = Math.floor(height / FONT_SIZE)
    const now = performance.now()
    posX = new Array(cols * rows)
    posY = new Array(cols * rows)
    rebuildPositions()
    grid = new Array(cols * rows).fill(0).map((_, i) => {
      const zr = getZoneRatio(i)
      const pers = personalityForZone(zr)
      const p = PERSONALITY[pers]
      return {
        char: charForZone(zr),
        nextUpdate: now + randomInterval(p),
        personality: pers
      }
    })
  }

  // ── Size init using getBoundingClientRect + DPR ──
  function initSize() {
    const dpr = window.devicePixelRatio || 1
    const rect = canvas.getBoundingClientRect()
    const w = Math.floor(rect.width)
    const h = Math.floor(rect.height)
    if (w === 0 || h === 0) return false
    width = w
    height = h
    canvas.width = w * dpr
    canvas.height = h * dpr
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    return true
  }

  // ── Start guard: only fire once layout gives real size ──
  function tryStart() {
    if (started) return
    if (!initSize()) return
    rebuildGrid()
    started = true
    animationId = requestAnimationFrame(animate)
  }

  // FPS control
  const FPS = 30
  const INTERVAL = 1000 / FPS
  let lastFrameTime = 0

  ctx.textBaseline = 'top'
  ctx.textAlign = 'center'

  function animate(time) {
    if (time - lastFrameTime < INTERVAL) {
      animationId = requestAnimationFrame(animate)
      return
    }
    lastFrameTime = time

    ctx.clearRect(0, 0, width, height)
    ctx.font = `${FONT_SIZE}px "Courier New", monospace`

    for (let i = 0; i < grid.length; i++) {
      const cell = grid[i]
      if (time > cell.nextUpdate) {
        const zr = getZoneRatio(i)
        cell.char = charForZone(zr)
        const p = PERSONALITY[cell.personality]
        cell.nextUpdate = time + randomInterval(p)
      }

      const px = posX[i]
      const py = posY[i]
      const dx = px - cx
      const dy = py - cy
      const dist = Math.sqrt(dx * dx + dy * dy)
      let alpha = Math.pow(Math.max(0, 1 - dist / maxDist), 2.0)

      const p = PERSONALITY[cell.personality]
      alpha *= p.alphaMul * 1.2
      const cm = p.colorMul
      const r = Math.min(255, Math.round(BASE_BRIGHTNESS.r * cm))
      const g = Math.min(255, Math.round(BASE_BRIGHTNESS.g * cm))
      const b = Math.min(255, Math.round(BASE_BRIGHTNESS.b * cm))

      const zr = dist / maxDist
      let rf = r, gf = g, bf = b
      if (zr > ZONE_B_MAX) {
        const edgeT = (zr - ZONE_B_MAX) / (1 - ZONE_B_MAX)
        rf = Math.round(r * (1 - edgeT * 0.3))
        gf = Math.round(g * (1 - edgeT * 0.25))
        bf = Math.round(b * (1 - edgeT * 0.1))
        alpha *= (1 - edgeT * 0.15)
      }

      ctx.fillStyle = `rgba(${rf},${gf},${bf},${alpha.toFixed(3)})`
      ctx.fillText(cell.char, px, py)
    }

    animationId = requestAnimationFrame(animate)
  }

  // ── ResizeObserver: wraps rAF to read committed layout size ──
  let resizePending = false
  resizeObserver = new ResizeObserver(() => {
    if (resizePending) return
    resizePending = true
    requestAnimationFrame(() => {
      resizePending = false
      if (!started) {
        tryStart()
      } else {
        // Already running → resize canvas on-the-fly
        if (initSize()) rebuildGrid()
      }
    })
  })
  resizeObserver.observe(canvas)

  // ── Double rAF: fallback if ResizeObserver hasn't fired yet ──
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      tryStart()
    })
  })

  // ── window.resize: additional fallback for browser-triggered viewport changes ──
  function handleWindowResize() {
    if (!started) {
      tryStart()
    } else {
      requestAnimationFrame(() => {
        if (initSize()) rebuildGrid()
      })
    }
  }
  window.addEventListener('resize', handleWindowResize)
})

onBeforeUnmount(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (resizeObserver) resizeObserver.disconnect()
  window.removeEventListener('resize', handleWindowResize)
})
</script>

<style scoped>
.particle-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}
</style>
