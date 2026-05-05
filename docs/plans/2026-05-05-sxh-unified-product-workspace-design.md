# SXH Unified Product Workspace UI Redesign

## Goal

Restructure the three primary SXH frontends into one coherent product family while preserving each surface's job:

- `sxh-front`: a warm, editorial knowledge community.
- `sxh-admin`: a focused operations cockpit for publishing and moderation.
- `sxh-rag/frontend`: an AI workspace for chat, retrieval, and knowledge operations.

The shared visual language remains warm amber, restrained surfaces, thin borders, low shadows, compact controls, and clear content hierarchy.

## Direction

Use a **Unified Product Workspace** model. The three apps should feel related, but not identical:

- Community pages prioritize reading, discovery, and quick entry into writing or AI help.
- Admin pages prioritize operational density, scanning, and repeated task execution.
- RAG pages prioritize context: knowledge sources, conversation flow, and assistant output.

## Community Frontend

Replace the old "top nav + article list + sidebar" feel with an editorial dashboard:

- Header becomes a command bar with brand, primary nav, theme control, write/login actions, and user status.
- Home gains a hero/workspace strip with community positioning, quick actions, and lightweight stats.
- Main area becomes a three-zone layout: featured/discovery lane, article stream, and right-side utility rail.
- Article list stays data-compatible, but cards receive more breathing room, stronger titles, and cleaner metadata.
- Mobile collapses to command bar, category rail, featured content, then article stream.

## Admin Frontend

Move from a template admin shell to an operations cockpit:

- Left sider becomes a warm rail with stronger logo area, grouped navigation, and softer active states.
- Header becomes a workbar: breadcrumb/context left, utility actions and user identity right.
- Content gets a wider canvas with page-level surface, consistent padding, and more deliberate section spacing.
- Statistics, home, article list, and editor screens inherit the cockpit background and card system.

## RAG Frontend

Turn the app into an AI workbench:

- Global layout keeps the existing Soybean Admin infrastructure but visually behaves like a workspace shell.
- Header becomes a pill workbar with search, return-to-community, theme, and user controls.
- Chat page becomes a three-panel workspace on desktop: context rail, conversation canvas, and source/action rail.
- Knowledge and history pages keep existing data flows but inherit the same surface, spacing, and control treatment.
- Mobile hides side rails and keeps the central chat/workflow clean.

## Implementation Notes

- Favor layout/CSS/component composition changes over API changes.
- Keep existing routes, stores, requests, and business interactions intact.
- Avoid touching backend files or unrelated documents.
- Use existing Vue, React, Element Plus, Ant Design, Naive UI, and UnoCSS patterns.
- Validate all four builds: `sxh-front`, `sxh-admin`, `sxh-rag/frontend`, and `sxh-rag/homepage` if homepage assets are affected.

