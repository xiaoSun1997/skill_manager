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

  // ── Six character sets: CN 40%, others 60% evenly split ──
  const CHARS_CN = '启元道极虚灵玄幽穹冥乾坤震离坎兑炁爻'
  const CHARS_GREEK = 'ΞΨΩΔΠΣΦΘΛΓ≡≈∞∏∑√∫∂∇∈∉⊕⊗⊞⊟⊠⊡'
  const CHARS_UPPER = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const CHARS_LOWER = 'abcdefghijklmnopqrstuvwxyz'
  const CHARS_NUM = '0123456789'
  const CHARS_SYMBOL = '@#$%&{}[]<>/|!+-=_.:;,'

  const FONT_SIZE = 16
  const LINE_HEIGHT = 1.35

  const ZONE_A_MAX = 0.30

  function personalityForZone(zoneRatio) {
    // ~99% change every 5-20s, ~1% change every 2-5s (vivid pop-outs)
    // All non-vivid characters have same brightness — no static circle boundary
    if (Math.random() < 0.01) return 'vivid'
    return zoneRatio < ZONE_A_MAX ? 'mid' : 'dim'
  }

  function randomChar() {
    // Pure random: each update can change type freely; 40% Chinese
    const r = Math.random()
    let pool, type
    if (r < 0.40) { pool = CHARS_CN; type = 'cn' }
    else if (r < 0.52) { pool = CHARS_GREEK; type = 'gk' }
    else if (r < 0.64) { pool = CHARS_UPPER; type = 'up' }
    else if (r < 0.76) { pool = CHARS_LOWER; type = 'lo' }
    else if (r < 0.88) { pool = CHARS_NUM; type = 'num' }
    else { pool = CHARS_SYMBOL; type = 'sym' }
    return { char: pool[Math.floor(Math.random() * pool.length)], type }
  }

  const PERSONALITY = {
    dim:  { min: 6000,  max: 10000, alphaMul: 0.70, colorMul: 0.85 },
    mid:  { min: 4000,  max: 8000,  alphaMul: 0.70, colorMul: 0.85 },
    vivid:{ min: 2000,  max: 5000,  alphaMul: 0.95, colorMul: 1.10 }
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
      posY[i] = Math.floor(i / cols) * FONT_SIZE * LINE_HEIGHT
    }
  }

  function rebuildGrid() {
    cols = Math.floor(width / FONT_SIZE)
    rows = Math.floor(height / (FONT_SIZE * LINE_HEIGHT))
    const now = performance.now()
    posX = new Array(cols * rows)
    posY = new Array(cols * rows)
    rebuildPositions()
    grid = new Array(cols * rows).fill(0).map((_, i) => {
      const zr = getZoneRatio(i)
      const pers = personalityForZone(zr)
      const p = PERSONALITY[pers]
      const { char, type } = randomChar()
      return {
        char,
        type,
        nextUpdate: now + Math.random() * 2000, // quick initial stagger
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

    // ── Eye gaze effect: sway left/right + pulse focus radius (clearly visible) ──
    const GAZE_SPEED = 0.0005
    const GAZE_AMPLITUDE = 0.30
    const PULSE_SPEED = 0.0007
    const PULSE_MIN = 0.35
    const PULSE_MAX = 1.15

    const gazeOffset = Math.sin(time * GAZE_SPEED) * width * GAZE_AMPLITUDE
    const focusScale = PULSE_MIN + (PULSE_MAX - PULSE_MIN) * (0.5 + 0.5 * Math.sin(time * PULSE_SPEED))

    const dynamicCx = cx + gazeOffset
    const dynamicMaxDist = maxDist * focusScale

    ctx.clearRect(0, 0, width, height)
    ctx.font = `${FONT_SIZE}px "Cascadia Code", "Fira Code", "SF Mono", "Consolas", "Courier New", monospace`

    for (let i = 0; i < grid.length; i++) {
      const cell = grid[i]
      if (time > cell.nextUpdate) {
        const zr = getZoneRatio(i)
        const { char, type } = randomChar()
        cell.char = char
        cell.type = type
        const p = PERSONALITY[cell.personality]
        cell.nextUpdate = time + randomInterval(p)
      }

      const px = posX[i]
      const py = posY[i]

      // Use dynamic gaze center for brightness (eye-like movement)
      const gdx = px - dynamicCx
      const gdy = py - cy
      const gazeDist = Math.sqrt(gdx * gdx + gdy * gdy)
      let alpha = Math.pow(Math.max(0, 1 - gazeDist / dynamicMaxDist), 1.5)

      const p = PERSONALITY[cell.personality]
      alpha *= p.alphaMul * 1.1
      // All characters use dark gold (no type-based split)
      const GOLD = { r: 170, g: 135, b: 45 }
      const baseR = GOLD.r
      const baseG = GOLD.g
      const baseB = GOLD.b

      const cm = p.colorMul
      const r = Math.min(255, Math.round(baseR * cm))
      const g = Math.min(255, Math.round(baseG * cm))
      const b = Math.min(255, Math.round(baseB * cm))
      let rf = r, gf = g, bf = b

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
