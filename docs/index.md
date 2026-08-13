---
layout: home

hero:
  name: HAL Foundation
  tagline: Next-generation management console for WildFly and JBoss EAP
  actions:
    - theme: brand
      text: Features
      link: /features/overview
    - theme: alt
      text: Architecture
      link: /architecture/overview
    - theme: alt
      text: GitHub
      link: https://github.com/hal/foundation
---

<div class="hero-screenshot">
  <img class="light-only" src="/foundation/media/dashboard.png" alt="HAL Dashboard">
  <img class="dark-only" src="/foundation/media/dashboard-dark.png" alt="HAL Dashboard">
</div>

<style>
.hero-screenshot {
  max-width: 1152px;
  margin: 0 auto;
}

.hero-screenshot img {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.dark .hero-screenshot img {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.hero-screenshot .dark-only {
  display: none;
}

.dark .hero-screenshot .light-only {
  display: none;
}

.dark .hero-screenshot .dark-only {
  display: block;
}
</style>
