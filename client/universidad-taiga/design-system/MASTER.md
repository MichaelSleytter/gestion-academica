# Stripe — Style Reference
> Architectural blueprint on white marble.

**Theme:** light

Stripe's design system evokes a digital command center on a clean canvas. It combines a serene white background with sharp, high-contrast typography and meticulously placed UI elements. The aesthetic is professional, clean, and data-driven, prioritizing clarity and ease of navigation.

## Color Palette

Based on `slate`, `gray`, and `neutral` from Tailwind CSS, with a focus on high contrast and accessibility.

| Name | Hex | Usage |
| :--- | :--- | :--- |
| `primary` | `#4F46E5` | (Indigo 600) Key actions, links, focus states |
| `primary-hover` | `#4338CA` | (Indigo 700) Hover state for primary actions |
| `secondary` | `#64748B` | (Slate 500) Secondary text, icons, borders |
| `secondary-hover`| `#475569` | (Slate 600) Hover for secondary elements |
| `background` | `#FFFFFF` | Main page background |
| `surface` | `#F8FAFC` | (Slate 50) Cards, sidebars, distinct sections |
| `border` | `#E2E8F0` | (Slate 200) Borders, dividers |
| `text-strong` | `#0F172A` | (Slate 900) Headlines, primary text |
| `text-default` | `#334155` | (Slate 700) Body copy, standard text |
| `text-muted` | `#64748B` | (Slate 500) Helper text, disabled states, watermarks |
| `success` | `#16A34A` | (Green 600) Success notifications, confirmations |
| `warning` | `#CA8A04` | (Yellow 600) Warnings, pending states |
| `danger` | `#DC2626` | (Red 600) Errors, destructive actions |

## Typography

- **Font Family:** `Inter`, a clean sans-serif font.
- **Headings (h1, h2, h3):** `font-semibold`, `text-strong`.
- **Body:** `font-normal`, `text-default`.
- **Links:** `font-medium`, `text-primary`.

| Element | Font Size (rem) | Font Weight | Letter Spacing |
| :--- | :--- | :--- | :--- |
| `h1` | 2.25rem (36px) | 600 | -0.025em |
| `h2` | 1.875rem (30px) | 600 | -0.025em |
| `h3` | 1.5rem (24px) | 600 | -0.025em |
| `h4` | 1.25rem (20px)| 600 | -0.025em |
| `p (body)` | 1rem (16px) | 400 | normal |
| `small` | 0.875rem (14px)| 400 | normal |

## Spacing & Sizing

Follows a 4-point grid system (1 unit = 0.25rem = 4px).

| Token | Value (rem) | Value (px) | Usage |
| :--- | :--- | :--- | :--- |
| `space-1` | 0.25 | 4 | Micro-spacing, inside components |
| `space-2` | 0.5 | 8 | Gaps between icons and text |
| `space-3` | 0.75 | 12 | Small component padding |
| `space-4` | 1.0 | 16 | Standard padding/margin (p-4, m-4) |
| `space-6` | 1.5 | 24 | Medium padding/margin |
| `space-8` | 2.0 | 32 | Large padding/margin |
| `space-12`| 3.0 | 48 | Section spacing |
| `space-16`| 4.0 | 64 | Page-level spacing |

## UI Elements

### Borders & Corner Radius

- **Standard Radius:** `0.375rem` (6px) - `rounded-md`. Used for cards, inputs, buttons.
- **Large Radius:** `0.5rem` (8px) - `rounded-lg`. For larger containers or modals.
- **Full Radius:** `9999px` - `rounded-full`. For pills, avatars.
- **Border Width:** `1px` (`border`).
- **Focus Ring:** `2px` solid border using `ring-2`, `ring-offset-2`, `ring-primary`.

### Shadows

Subtle shadows for depth.

- **`shadow-sm`**: `0 1px 2px 0 rgb(0 0 0 / 0.05)`. For small, interactive elements.
- **`shadow`**: `0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)`. Default for cards.
- **`shadow-md`**: `0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)`. For elevated elements.
- **`shadow-lg`**: `0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)`. For modals or popovers.
