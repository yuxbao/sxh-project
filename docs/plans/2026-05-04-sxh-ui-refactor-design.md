# sxh UI Refactor Design

Date: 2026-05-04

## Goal

Refactor both `sxh-front` and `sxh-admin` into a clean, flat, textured product interface inspired by Longbridge's fintech landing-page language. The result should feel trustworthy, modern, calm, and premium, while still fitting a knowledge community and AI tool platform.

## Visual Direction

Design theme: **Clean Lime FinTech Knowledge Platform**.

The visual system should use restrained financial-product clarity with a distinctive yellow-green accent. The accent should feel fresh and intelligent, not neon or playful.

Core traits:

- Clean white and cool-gray surfaces.
- Low-noise flat cards with thin borders.
- Subtle shadows used only for hierarchy.
- Yellow-green accent for active states, primary actions, metrics, and small highlights.
- Strong typographic hierarchy with readable body copy.
- Reduced decorative imagery and heavy gradients.
- Product-like navigation and professional admin density.

## Palette

Primary accent:

- Lime primary: `#9fda3a`
- Lime hover: `#8bc72f`
- Lime active: `#6fa91f`
- Lime soft: `rgba(159, 218, 58, 0.14)`

Neutral system:

- Ink: `#101411`
- Text: `#1d241f`
- Muted text: `#667065`
- Page background: `#f6f8f3`
- Surface: `#ffffff`
- Soft surface: `#fbfcf7`
- Border: `#e3eadb`

Dark mode:

- Background: `#101410`
- Surface: `#171d16`
- Soft surface: `#1e261b`
- Border: `rgba(197, 232, 130, 0.16)`
- Text: `#f4f7ee`
- Muted text: `#aeb9a8`

## Shared UI Rules

- Cards use 10-12px radius, 1px border, and restrained shadow.
- Buttons are flat pills or compact rounded rectangles depending on context.
- Avoid nested-card visual clutter.
- Lists use clear title, weak metadata, and consistent media aspect ratio.
- Navigation selected states use lime accent lines or soft lime backgrounds.
- Tables and filters in admin should read as professional tools, not marketing cards.
- Mobile layouts collapse sidebars and preserve text readability.

## `sxh-front` Scope

Priority surfaces:

- Global variables and common CSS.
- Header navigation.
- Home category navigation.
- Recommended article cards.
- Article list cards.
- Sidebar modules.
- Article detail container and comment surfaces.
- Footer tone where needed.

Expected result:

The user-facing site should feel like a modern knowledge and AI product, with more white space, less heavy shadow, and stronger article scanability.

## `sxh-admin` Scope

Priority surfaces:

- Global LESS variables and common styles.
- Login page.
- Main layout shell.
- Sidebar menu.
- Header and tabs.
- Dashboard cards.
- Search/filter panels.
- Table/list pages through shared overrides.

Expected result:

The admin should feel like a quiet fintech operations console: compact, readable, and consistent with the public site without becoming decorative.

## Implementation Strategy

Use a design-token-first approach:

1. Add or revise global tokens for lime accent, neutral surfaces, borders, shadows, and radius.
2. Refactor public-site shared CSS and high-impact components.
3. Refactor admin theme variables and layout CSS.
4. Apply common Element Plus and Ant Design overrides where useful.
5. Verify both builds and inspect the main views in-browser if a local server can run.

## Risks

- Existing styles are spread across many CSS, SCSS, and LESS files, so broad visual changes can have unintended overrides.
- `sxh-front` uses both Tailwind and legacy CSS, which may require careful specificity.
- `sxh-admin` uses Ant Design and custom LESS, so global overrides must avoid breaking tables and forms.

## Non-goals

- No route or API behavior changes.
- No backend changes.
- No complete component architecture rewrite in this pass.
- No exact copying of Longbridge assets or brand identity.
