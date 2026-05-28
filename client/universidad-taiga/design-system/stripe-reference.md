# Stripe — Style Reference

> Architectural blueprint on white marble.
> Fuente: https://styles.refero.design/style/48e5de76-05d5-4c4e-a269-c7c245b291ec

**Theme:** light

Stripe's design system evokes a digital command center on a clean canvas. It combines a serene white background with structured grid layouts and a single vibrant violet to highlight actions and key information. Subtle shadows provide soft elevation, preventing elements from feeling flat, while compact typography paired with highly descriptive gradients for hero sections and product showcases adds visual depth without clutter. The overall effect is one of quiet efficiency, where information is paramount, and interactions are clearly signposted.

## Tokens — Colors

| Name | Value | Token | Role |
|------|-------|-------|------|
| Midnight Ink | `#061b31` | `--color-midnight-ink` | Primary text, critical headings, icons, primary button text for ghost buttons |
| Slate Blue | `#50617a` | `--color-slate-blue` | Secondary text, muted links, subtle borders, descriptive captions |
| Ghost Gray | `#64748d` | `--color-ghost-gray` | Tertiary text, placeholder text, inactive states, subtle dividers |
| Platinum White | `#ffffff` | `--color-platinum-white` | Page backgrounds, card surfaces, primary button text against dark backgrounds |
| Porcelain White | `#f8fafd` | `--color-porcelain-white` | Secondary card surfaces, subtle background variations |
| Powder Blue | `#e5edf5` | `--color-powder-blue` | Background for secondary sections, light card backgrounds |
| Stone Gray | `#d8d6df` | `--color-stone-gray` | Horizontal rules, subtle borders, graphical elements |
| Deep Violet | `#533afd` | `--color-deep-violet` | Primary calls to action (buttons, links), active states, significant icons |
| Washed Violet | `#b9b9f9` | `--color-washed-violet` | Border for ghost buttons, subtle accents |
| Soft Violet | `#8087ff` | `--color-soft-violet` | Decorative icons, gradient highlights, sub-brand accents |
| Accent Green | `#81b81a` | `--color-accent-green` | Green outline accent for tags, dividers, and focused UI edges |
| Vibrant Orange | `#ff6118` | `--color-vibrant-orange` | Orange outline accent for tags, dividers, and focused UI edges |
| Sunburst Gradient | `linear-gradient(90deg, rgb(114, 50, 241) 3.13%, rgb(251, 118, 250) 50%, rgb(255, 207, 94))` | `--color-sunburst-gradient` | Decorative gradients in hero sections |
| Dreamy Gradient | `radial-gradient(circle, rgb(127, 125, 252), rgb(244, 75, 204) 33%, rgb(229, 237, 245) 66%)` | `--color-dreamy-gradient` | Abstract background graphics |
| Fuchsia Glow Gradient | `linear-gradient(0deg, rgb(255, 46, 222), rgb(210, 152, 255))` | `--color-fuchsia-glow-gradient` | Decorative illustration elements |

## Tokens — Typography

### Font

**sohne-var** — `--font-sohne-var`
- **Substitute:** `system-ui, sans-serif`
- **Weights:** 300, 400
- **Sizes:** 8px, 9px, 10px, 11px, 12px, 14px, 16px, 18px, 20px, 22px, 26px, 32px, 34px, 44px, 48px, 56px
- **Line height:** 0.80 to 1.50
- **Letter spacing:** -0.0300em at 56px, -0.0250em at 48px, -0.0090em at 18px
- **OpenType features:** `"ss01" on, "tnum"`
- **Role:** Weight 300 for large impactful headlines (understated authority). Weight 400 for body text.

### Type Scale

| Role | Size | Line Height | Letter Spacing | Token |
|------|------|-------------|----------------|-------|
| caption | 11px | 1.45 | 0.03px | `--text-caption` |
| body | 14px | 1.4 | 0.003px | `--text-body` |
| subheading | 18px | 1.25 | -0.009px | `--text-subheading` |
| heading-sm | 22px | 1.2 | -0.01px | `--text-heading-sm` |
| heading | 32px | 1.15 | -0.02px | `--text-heading` |
| heading-lg | 44px | 1.1 | -0.025px | `--text-heading-lg` |
| display | 56px | 1.07 | -0.03px | `--text-display` |

## Tokens — Spacing & Shapes

**Base unit:** 4px
**Density:** comfortable

| Name | Value | Token |
|------|-------|-------|
| 4 | 4px | `--spacing-4` |
| 8 | 8px | `--spacing-8` |
| 12 | 12px | `--spacing-12` |
| 16 | 16px | `--spacing-16` |
| 20 | 20px | `--spacing-20` |
| 24 | 24px | `--spacing-24` |
| 28 | 28px | `--spacing-28` |
| 32 | 32px | `--spacing-32` |
| 36 | 36px | `--spacing-36` |
| 40 | 40px | `--spacing-40` |
| 48 | 48px | `--spacing-48` |
| 60 | 60px | `--spacing-60` |
| 64 | 64px | `--spacing-64` |
| 80 | 80px | `--spacing-80` |
| 96 | 96px | `--spacing-96` |

### Border Radius

| Element | Value |
|---------|-------|
| tags | 4px |
| cards | 6px |
| images | 4px |
| inputs | 4px |
| buttons | 4px |

### Shadows

| Name | Value | Token |
|------|-------|-------|
| xl | `rgba(0, 0, 0, 0.2) 0px 0px 32px 8px` | `--shadow-xl` |
| xl-2 | `rgba(50, 50, 93, 0.12) 0px 16px 32px 0px` | `--shadow-xl-2` |
| xl-3 | `rgba(23, 23, 23, 0.08) 0px 15px 35px 0px` | `--shadow-xl-3` |
| sm | `rgba(23, 23, 23, 0.06) 0px 3px 6px 0px` | `--shadow-sm` |

### Layout Tokens

- **Section gap:** 64px
- **Card padding:** 12px
- **Element gap:** 8px

## Components

### Primary Filled Button
Background: Deep Violet (#533afd), Text: Platinum White (#ffffff), 4px radius, Padding: 15.5px vertical, 24px horizontal.

### Ghost Button
Background: transparent, Text: Midnight Ink (#061b31), No border, Padding: 12px vertical, 0px horizontal.

### Outlined Button
Background: transparent, Text: Deep Violet (#533afd), Border: Washed Violet (#b9b9f9), 4px radius, Padding: 14.5px vertical, 24px horizontal.

### Default Card
Background: Powder Blue (#e5edf5), No border, 6px radius, Padding: 12px.

### Feature Card
Background: Porcelain White (#f8fafd), No border, 6px radius, Box Shadow: xl. Padding: 12px.

### Primary Navigation Link
Text: Midnight Ink (#061b31), No underline on hover, Padding: 0px.

## Surfaces

| Level | Name | Value | Purpose |
|-------|------|-------|---------|
| 0 | Platinum White | `#ffffff` | Primary page background and base canvas |
| 1 | Porcelain White | `#f8fafd` | Secondary surfaces, light card backgrounds |
| 2 | Powder Blue | `#e5edf5` | Backgrounds for alternating sections |

## Do's and Don'ts

### Do
- Use Platinum White as default page background
- Apply Deep Violet specifically for primary interactive elements
- Use weight 300 for all display and large heading typography
- Keep card surfaces subtle, using Powder Blue or Porcelain White with 6px radius
- Utilize negative letter-spacing for large text
- Maintain elementGap of 8px for logical grouping

### Don't
- Don't use saturated colors for large areas or text
- Avoid sharp shadows; prefer soft, diffused shadows
- Don't introduce multiple font families
- Don't use border radii other than 4px and 6px
- Avoid high-contrast bold headlines; prefer lighter weights
