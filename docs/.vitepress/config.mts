import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Arcade",
  description: "Docs for the Arcade api",
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: '/logo.png' }]
  ],
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config

    logo: '/logo.png',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'About', link: '/about' },
      { text: 'Getting Started', link: '/arcade-minigames/getting-started' }
    ],

    sidebar: [
      {
        text: 'Boundaries',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-boundaries/getting-started' },
          { text: 'Usage', link: '/arcade-boundaries/usage' }
        ]
      },
      {
        text: 'Commands',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-commands/getting-started' },
          { text: 'Usage', link: '/arcade-commands/usage' }
        ]
      },
      {
        text: 'Datagen',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-datagen/getting-started' }
        ]
      },
      {
        text: 'Dimensions',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-dimensions/getting-started' },
          { text: 'Basic Usage', link: '/arcade-dimensions/basic-usage' },
          { text: 'Advanced Usage', link: '/arcade-dimensions/advanced-usage' }
        ]
      },
      {
        text: 'Events',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-events-server/getting-started' },
          { text: 'Basic Usage', link: '/arcade-events-server/basic-usage' },
          { text: 'Advanced Usage', link: '/arcade-events-server/advanced-usage' }
        ]
      },
      {
        text: 'Extensions',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-extensions/getting-started' },
          { text: 'Usage', link: '/arcade-extensions/usage' }
        ]
      },
      {
        text: 'Guis',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-guis/getting-started' }
        ]
      },
      {
        text: 'Items',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-items/getting-started' }
        ]
      },
      {
        text: 'Minigames',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-minigames/getting-started' },
          { text: 'Basic Usage', link: '/arcade-minigames/basic-usage' },
          { text: 'Advancements', link: '/arcade-minigames/advancements' },
          { text: 'Chat', link: '/arcade-minigames/chat' },
          { text: 'Commands', link: '/arcade-minigames/commands' },
          { text: 'Effects', link: '/arcade-minigames/effects' },
          { text: 'Events', link: '/arcade-minigames/events' },
          { text: 'Players', link: '/arcade-minigames/players' },
          { text: 'Recipes', link: '/arcade-minigames/recipes' },
          { text: 'Resource Packs', link: '/arcade-minigames/resource_packs' },
          { text: 'Scheduling', link: '/arcade-minigames/scheduling' },
          { text: 'Serialization', link: '/arcade-minigames/serialization' },
          { text: 'Settings', link: '/arcade-minigames/settings' },
          { text: 'Stats', link: '/arcade-minigames/stats' },
          { text: 'Teams', link: '/arcade-minigames/teams' },
          { text: 'Visuals', link: '/arcade-minigames/visuals' },
          { text: 'Worlds', link: '/arcade-minigames/worlds' }
        ]
      },
      {
        text: 'Nametags',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-nametags/getting-started' }
        ]
      },
      {
        text: 'NPCs',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-npcs/getting-started' }
        ]
      },
      {
        text: 'Replay',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-replay/getting-started' }
        ]
      },
      {
        text: 'Resource Packs',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-resource-pack/getting-started' }
        ]
      },
      {
        text: 'Resource Pack Hosting',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-resource-pack-host/getting-started' }
        ]
      },
      {
        text: 'Scheduling',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-scheduler/getting-started' },
          { text: 'Usage', link: '/arcade-scheduler/usage' }
        ]
      },
      {
        text: 'Utilities',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-utils/getting-started' }
        ]
      },
      {
        text: 'Virtual Entities',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-virtual-entities/getting-started' },
          { text: 'Usage', link: '/arcade-virtual-entities/usage' }
        ]
      },
      {
        text: 'Visuals',
        collapsed: true,
        items: [
          { text: 'Getting Started', link: '/arcade-visuals/getting-started' },
          { text: 'Usage', link: '/arcade-visuals/usage' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/CasualChampionships/arcade' }
    ]
  }
})
