# PROJECT KNOWLEDGE BASE

**Generated:** 2026-03-22
**Type:** Mobile UI Component Library (Weight Tracking App)

## OVERVIEW

Chinese weight tracking mobile app UI components using Tailwind CSS + Material Design 3. Each subdirectory contains standalone HTML demos with embedded Tailwind config.

## STRUCTURE

```
stitch_log_weight/
├── product_requirements_document.md   # PRD template (empty)
└── stitch_log_weight/
    ├── log_weight/     # Weight entry form (bento grid)
    ├── dashboard/      # Main dashboard (stats, charts)
    ├── trends/         # Trend analysis (SVG charts)
    ├── data_mgmt/     # Data management (backup, import/export)
    └── azure_horizon/ # Design system spec (DESIGN.md)
```

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Weight entry UI | `log_weight/code.html` |
| Dashboard layout | `dashboard/code.html` |
| Chart visualizations | `trends/code.html` |
| Data operations | `data_mgmt/code.html` |
| Design system | `azure_horizon/DESIGN.md` |

## CONVENTIONS

### Tailwind Config Pattern
- Dark mode via `class` strategy
- Colors use semantic naming (primary, surface, outline)
- Material Symbols Outlined for icons
- Inter/Public Sans fonts for Chinese-friendly typography
- Custom border radius scale (2xl, 3xl, full)

### Mobile-First Layout
- Max-width: `max-w-xl` centered
- Padding: `px-4` or `px-6`
- Bottom nav bar with glass blur effect
- Fixed FAB positioning (right-6, bottom-28)

### No 1px Dividers
- Use background color transitions for boundaries
- Surface container hierarchy (lowest, low, high, highest)
- Rounded cards (2xl-3xl radius) over border lines

## ANTI-PATTERNS (THIS PROJECT)

- **NO pure black (#000000)** — use `on-surface` (#1f1f1f) instead
- **NO 1px border lines** — use color transitions
- **NO hard edges** — min corner radius 0.5rem (sm)
- **NO oversized shadows** — use ambient blur (30-60px) if needed

## CODE MAP

| File | Lines | Purpose |
|------|-------|---------|
| log_weight/code.html | 148 | Weight input form |
| dashboard/code.html | 243 | Main stats dashboard |
| trends/code.html | 206 | Trend analysis charts |
| data_mgmt/code.html | 190 | Backup/import/export |
| azure_horizon/DESIGN.md | 103 | Design system spec |

## COMMANDS

None — pure static HTML demos, no build system.

## NOTES

- All HTML files use Tailwind CDN with forms plugin
- Language: Simplified Chinese (zh-CN)
- Icons: Google Material Symbols Outlined
- No JavaScript functionality — pure UI mockups
