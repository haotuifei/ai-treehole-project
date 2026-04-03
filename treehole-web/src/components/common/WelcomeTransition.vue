<template>
  <Teleport to="body">
    <Transition name="overlay-fade">
      <div v-if="visible" class="unlock-overlay">
        <div class="spiral-scene">
          <div class="spiral-container">
            <div class="spiral-element spiral-a">
              <svg viewBox="0 0 200 200" class="spiral-svg">
                <path class="spiral-path" d="M100,100 
                  Q100,70 130,70 
                  Q160,70 160,100 
                  Q160,130 130,130 
                  Q100,130 100,100
                  Q100,60 140,60
                  Q180,60 180,100
                  Q180,140 140,140
                  Q100,140 100,100" />
              </svg>
            </div>
            <div class="spiral-element spiral-b">
              <svg viewBox="0 0 200 200" class="spiral-svg">
                <path class="spiral-path" d="M100,100 
                  Q100,130 70,130 
                  Q40,130 40,100 
                  Q40,70 70,70 
                  Q100,70 100,100
                  Q100,140 60,140
                  Q20,140 20,100
                  Q20,60 60,60
                  Q100,60 100,100" />
              </svg>
            </div>
          </div>

          <div class="core-glow"></div>

          <div class="energy-trails">
            <div v-for="i in 12" :key="i" class="trail" :style="getTrailStyle(i)"></div>
          </div>

          <div class="floating-orbs">
            <div v-for="i in 8" :key="i" class="orb" :style="getOrbStyle(i)"></div>
          </div>

          <div class="welcome-text">
            <div class="text-line title">{{ greeting }}</div>
            <div class="text-line subtitle">{{ message }}</div>
          </div>
        </div>

        <div class="reveal-curtain">
          <div class="curtain-left"></div>
          <div class="curtain-right"></div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  username: { type: String, default: '' }
})

const emit = defineEmits(['complete'])

const greetings = ['欢迎回来', '好久不见', '你来了']
const messages = ['你今天也很棒', '继续加油哦', '今天也要元气满满', '相信自己', '每一步都算数']

const greeting = ref('')
const message = ref('')
let timer = null

function getRandomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function getTrailStyle(index) {
  const angle = (index - 1) * 30
  const delay = (index - 1) * 0.04
  return {
    '--trail-angle': `${angle}deg`,
    '--trail-delay': `${delay}s`
  }
}

function getOrbStyle(index) {
  const angle = (index - 1) * 45
  const distance = 60 + (index % 3) * 30
  const delay = Math.random() * 0.3
  const size = 8 + Math.random() * 12
  return {
    '--orb-angle': `${angle}deg`,
    '--orb-distance': `${distance}px`,
    '--orb-delay': `${delay}s`,
    '--orb-size': `${size}px`
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    greeting.value = getRandomItem(greetings)
    message.value = getRandomItem(messages)
    timer = setTimeout(() => {
      emit('complete')
    }, 3000)
  } else {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }
})

onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
})
</script>

<style scoped>
.unlock-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: linear-gradient(135deg, #f6f4f1 0%, #e8f0e9 50%, #f6f4f1 100%);
  overflow: hidden;
}

.spiral-scene {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spiral-container {
  position: relative;
  width: 200px;
  height: 200px;
}

.spiral-element {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  will-change: transform;
}

.spiral-a {
  animation: spiralUnwindA 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.spiral-b {
  animation: spiralUnwindB 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes spiralUnwindA {
  0% {
    transform: rotate(0deg) translate(0, 0) scale(1);
    opacity: 1;
  }
  50% {
    transform: rotate(180deg) translate(20px, -20px) scale(1.1);
    opacity: 1;
  }
  100% {
    transform: rotate(360deg) translate(150px, -150px) scale(0.8);
    opacity: 0;
  }
}

@keyframes spiralUnwindB {
  0% {
    transform: rotate(0deg) translate(0, 0) scale(1);
    opacity: 1;
  }
  50% {
    transform: rotate(-180deg) translate(-20px, 20px) scale(1.1);
    opacity: 1;
  }
  100% {
    transform: rotate(-360deg) translate(-150px, 150px) scale(0.8);
    opacity: 0;
  }
}

.spiral-svg {
  width: 100%;
  height: 100%;
}

.spiral-path {
  fill: none;
  stroke-width: 6;
  stroke-linecap: round;
  stroke-dasharray: 600;
  stroke-dashoffset: 0;
  filter: drop-shadow(0 0 15px currentColor) drop-shadow(0 0 30px currentColor) drop-shadow(0 0 50px currentColor);
}

.spiral-a .spiral-path {
  stroke: #00d9a5;
  animation: pathDraw 0.5s ease forwards;
}

.spiral-b .spiral-path {
  stroke: #ff6b6b;
  animation: pathDraw 0.5s ease forwards 0.05s;
}

@keyframes pathDraw {
  0% {
    stroke-dashoffset: 600;
    opacity: 0;
  }
  50% {
    stroke-dashoffset: 0;
    opacity: 1;
  }
  100% {
    stroke-dashoffset: 0;
    opacity: 1;
  }
}

.core-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle, #00d9a5 0%, rgba(0, 217, 165, 0.8) 30%, rgba(0, 217, 165, 0.4) 60%, transparent 80%);
  box-shadow: 0 0 80px rgba(0, 217, 165, 1), 0 0 160px rgba(0, 217, 165, 0.6), inset 0 0 40px rgba(255, 255, 255, 0.4);
  transform: translate(-50%, -50%) scale(0);
  animation: glowPulse 0.6s ease-out forwards 0.15s;
  will-change: transform, opacity;
}

@keyframes glowPulse {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(3);
    opacity: 0;
  }
}

.energy-trails {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.trail {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 6px;
  height: 90px;
  background: linear-gradient(to top, transparent, #00d9a5, rgba(0, 217, 165, 0.8));
  border-radius: 3px;
  box-shadow: 0 0 20px rgba(0, 217, 165, 1), 0 0 40px rgba(0, 217, 165, 0.6);
  transform-origin: bottom center;
  transform: translate(-50%, -100%) rotate(var(--trail-angle)) scaleY(0);
  opacity: 0;
  animation: trailShoot 0.4s ease-out forwards;
  animation-delay: var(--trail-delay);
  will-change: transform, opacity;
}

@keyframes trailShoot {
  0% {
    transform: translate(-50%, -100%) rotate(var(--trail-angle)) scaleY(0);
    opacity: 0;
  }
  40% {
    transform: translate(-50%, -100%) rotate(var(--trail-angle)) scaleY(1);
    opacity: 0.9;
  }
  100% {
    transform: translate(-50%, -100%) rotate(var(--trail-angle)) scaleY(1.5) translateY(-100px);
    opacity: 0;
  }
}

.floating-orbs {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.orb {
  position: absolute;
  width: var(--orb-size);
  height: var(--orb-size);
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #00d9a5, rgba(0, 217, 165, 0.8));
  box-shadow: 0 0 25px rgba(0, 217, 165, 1), 0 0 50px rgba(0, 217, 165, 0.6);
  transform: rotate(var(--orb-angle)) translateY(0);
  opacity: 0;
  animation: orbFloat 0.6s ease-out forwards;
  animation-delay: var(--orb-delay);
  will-change: transform, opacity;
}

@keyframes orbFloat {
  0% {
    transform: rotate(var(--orb-angle)) translateY(0) scale(0);
    opacity: 0;
  }
  30% {
    transform: rotate(var(--orb-angle)) translateY(calc(var(--orb-distance) * -0.5)) scale(1);
    opacity: 1;
  }
  100% {
    transform: rotate(var(--orb-angle)) translateY(calc(var(--orb-distance) * -1.5)) scale(0.5);
    opacity: 0;
  }
}

.welcome-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
}

.text-line {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
  animation: textReveal 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.title {
  font-size: 2.2rem;
  font-weight: 600;
  color: #006644;
  letter-spacing: 0.08em;
  text-shadow: 0 2px 20px rgba(0, 217, 165, 0.5), 0 0 40px rgba(0, 217, 165, 0.3), 2px 2px 4px rgba(0, 0, 0, 0.2);
  animation-delay: 0.3s;
}

.subtitle {
  font-size: 1.15rem;
  color: #008866;
  margin-top: 12px;
  font-weight: 500;
  text-shadow: 0 2px 15px rgba(0, 217, 165, 0.4), 0 0 30px rgba(0, 217, 165, 0.2);
  animation-delay: 0.5s;
}

@keyframes textReveal {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.reveal-curtain {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 20;
}

.curtain-left,
.curtain-right {
  position: absolute;
  top: 0;
  width: 55%;
  height: 100%;
  will-change: transform;
}

.curtain-left {
  left: 0;
  background: linear-gradient(90deg, #f6f4f1 0%, #e8f0e9 100%);
  transform: translateX(0);
  animation: curtainSlideLeft 0.7s cubic-bezier(0.6, 0, 0.9, 1) forwards;
  animation-delay: 1.5s;
}

.curtain-right {
  right: 0;
  background: linear-gradient(90deg, #e8f0e9 0%, #f6f4f1 100%);
  transform: translateX(0);
  animation: curtainSlideRight 0.7s cubic-bezier(0.6, 0, 0.9, 1) forwards;
  animation-delay: 1.5s;
}

@keyframes curtainSlideLeft {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}

@keyframes curtainSlideRight {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(100%);
  }
}

.overlay-fade-enter-active {
  transition: opacity 0.3s ease;
}

.overlay-fade-leave-active {
  transition: opacity 0.4s ease;
}

.overlay-fade-enter-from {
  opacity: 0;
}

.overlay-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .spiral-container {
    width: 150px;
    height: 150px;
  }

  .title {
    font-size: 1.8rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .core-glow {
    width: 60px;
    height: 60px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .spiral-a,
  .spiral-b,
  .spiral-path,
  .core-glow,
  .trail,
  .orb,
  .text-line,
  .curtain-left,
  .curtain-right {
    animation: none;
  }

  .text-line {
    opacity: 1;
    transform: none;
  }
}
</style>
