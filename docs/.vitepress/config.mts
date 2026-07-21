import { defineConfig } from 'vitepress'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const docsDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')

const pageOrders: Record<string, string[]> = {
  'arcade-minigames': [
    'getting-started', 'basic-usage', 'advancements', 'chat', 'commands',
    'effects', 'events', 'players', 'recipes', 'resource_packs', 'scheduling',
    'serialization', 'settings', 'stats', 'teams', 'visuals', 'worlds'
  ]
}

function titleCase(value: string): string {
  return value.split(/[-_]/).map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

function moduleTitle(folder: string): string {
  const dir = path.join(docsDir, folder)
  const pages = fs.readdirSync(dir).filter((file) => file.endsWith('.md'))
  const preferred = pages.includes('getting-started.md') ? 'getting-started.md' : pages.sort()[0]
  if (preferred) {
    const heading = fs.readFileSync(path.join(dir, preferred), 'utf-8').match(/^#\s+(.+?)\s*$/m)
    if (heading) return heading[1]
  }
  return titleCase(folder.replace(/^arcade-/, ''))
}

function pages(folder: string): string[] {
  const order = pageOrders[folder] ?? []
  const rank = (page: string) => {
    const index = order.indexOf(page)
    if (index !== -1) return index
    if (page === 'getting-started') return -1
    return order.length + 1
  }
  return fs.readdirSync(path.join(docsDir, folder))
    .filter((file) => file.endsWith('.md'))
    .map((file) => file.replace(/\.md$/, ''))
    .sort((a, b) => rank(a) - rank(b) || a.localeCompare(b))
}

// Each module is a sidebar group whose title links to its getting-started page. The
// getting-started page is not repeated as a child; the remaining pages become items.
function sidebarGroup(folder: string) {
  const all = pages(folder)
  const landing = all.includes('getting-started') ? 'getting-started' : all[0]
  const items = all
    .filter((page) => page !== landing)
    .map((page) => ({ text: titleCase(page), link: `/${folder}/${page}` }))
  return {
    text: moduleTitle(folder),
    link: landing ? `/${folder}/${landing}` : undefined,
    collapsed: items.length > 0 ? true : undefined,
    items
  }
}

const modules = fs.readdirSync(docsDir)
  .filter((file) => file.startsWith('arcade-') && fs.statSync(path.join(docsDir, file)).isDirectory())
  .sort()

const sidebar = modules.map(sidebarGroup)

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
      { text: 'About', link: '/about' }
    ],

    sidebar,

    socialLinks: [
      { icon: 'github', link: 'https://github.com/CasualChampionships/arcade' }
    ]
  }
})
