import { defineConfig } from "vitepress";
import { withMermaid } from "vitepress-plugin-mermaid";

export default withMermaid(defineConfig({
  title: "HAL Foundation",
  description: "Documentation for the HAL management console (halOP & halOS)",
  base: "/foundation/",

  ignoreDeadLinks: [/^http:\/\/localhost/],

  vite: {
    optimizeDeps: {
      include: ["mermaid"],
    },
  },

  themeConfig: {
    nav: [
      { text: "Features", link: "/features/overview" },
      { text: "Architecture", link: "/architecture/overview" },
      { text: "Editions", link: "/editions/halop" },
    ],

    sidebar: [
      {
        text: "Features",
        items: [
          { text: "Overview", link: "/features/overview" },
          { text: "Dashboard", link: "/features/dashboard" },
          { text: "Model Browser", link: "/features/model-browser" },
          {
            text: "Resource Management",
            link: "/features/resource-management",
          },
          { text: "Tasks", link: "/features/tasks" },
          { text: "Test Automation", link: "/features/test-automation" },
        ],
      },
      {
        text: "Architecture",
        items: [
          { text: "Overview", link: "/architecture/overview" },
          { text: "Dashboard", link: "/architecture/dashboard-architecture" },
          { text: "Task Framework", link: "/architecture/task-framework" },
          {
            text: "Resource Shell & SPI",
            link: "/architecture/resource-shell-spi",
          },
          {
            text: "Attribute Pipeline",
            link: "/architecture/attribute-pipeline",
          },
        ],
      },
      {
        text: "Editions",
        items: [
          { text: "halOP", link: "/editions/halop" },
          { text: "halOS", link: "/editions/halos" },
        ],
      },
      {
        text: "Development",
        items: [{ text: "Building", link: "/development/building" }],
      },
    ],

    socialLinks: [
      { icon: "github", link: "https://github.com/hal/foundation" },
    ],

    editLink: {
      pattern: "https://github.com/hal/foundation/edit/main/docs/:path",
    },

    search: {
      provider: "local",
    },

    footer: {
      message: "Documentation for the HAL management console (halOP & halOS)",
    },
  },
}));
