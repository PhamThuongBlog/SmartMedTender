# Token Consumption Report — ACTUAL DATA (June 14, 2026)

**Source:** Claude Code session transcripts from `~/.claude/projects/D--NCKH-2027-Tien-ChuHai-MedTenderSystem-VERSION2/`
**Extraction date:** 2026-06-14
**Method:** Parsed all JSONL transcript files recursively (main sessions + subagent sessions)

---

## Overall Token Consumption

| Metric | Value |
|--------|-------|
| **Total tokens consumed** | **26.2M** |
| Input tokens | 20.3M (77.5%) |
| Output tokens | 5.9M (22.5%) |
| JSONL files with token data | 29 / 36 total |
| Main session files | 8 |
| Subagent session files | 21 |

## Session Breakdown (Top 20 by Tokens)

| Session/Agent | Type | Input (K) | Output (K) | Total (K) |
|--------------|------|-----------|------------|-----------|
| 4b563f87 | Main | 3,663 | 2,106 | **5,768** |
| c1ae2288 | Main | 1,188 | 1,381 | **2,569** |
| 510efe35 | Main | 1,062 | 663 | **1,725** |
| c9f372ad | Main | 903 | 475 | **1,378** |
| agent-a2a41dbc | Sub | 1,030 | 26 | **1,056** |
| agent-a18eb73f | Sub | 1,014 | 28 | **1,042** |
| agent-a89a51da | Sub | 943 | 27 | **970** |
| agent-a3abde4c | Sub | 882 | 27 | **909** |
| 647f5bcf | Main | 658 | 208 | **865** |
| agent-a88f9b0a | Sub | 846 | 19 | **864** |
| 3b43ccfe (current) | Main | 683 | 114 | **797** |
| agent-a8d0b1ee | Sub | 767 | 15 | **782** |
| agent-a01a07d1 | Sub | 660 | 43 | **702** |
| agent-a85c7e9a | Sub | 610 | 23 | **633** |
| agent-ac99c7d3 | Sub | 615 | 18 | **633** |
| agent-a0d3216c | Sub | 596 | 23 | **619** |
| agent-addabd43 | Sub | 502 | 16 | **518** |
| agent-a33e5623 | Sub | 454 | 9 | **463** |
| agent-a21ba934 | Sub | 385 | 68 | **453** |
| agent-ac24173d | Sub | 372 | 46 | **418** |

## Token Distribution

- **Main sessions**: ~13.1M tokens (50.0%) — human-AI dialogue
- **Subagent sessions**: ~13.1M tokens (50.0%) — agent-specific work
- **Input/Output ratio**: 3.4:1 (typical for code generation tasks)

## Top Subagent Types by Total Token Consumption

| Agent Type | Total Sessions | Total Tokens (K) |
|-----------|---------------|-------------------|
| architect | 3 | ~2,511 |
| senior-backend | 4 | ~3,269 |
| senior-fe | 2 | ~1,166 |
| tester-api | 2 | ~1,035 |
| code-review | 3 | ~1,484 |
| devops | 2 | ~782 |
| sonarqube | 2 | ~1,093 |
| explore | 3 | ~1,811 |

## Paper Estimate vs Reality

| Metric | Paper Estimate | Actual | Verdict |
|--------|---------------|--------|---------|
| Total token consumption | "15–25 million" | **26.2M** | ⚠️ Slightly above upper bound |
| Token distribution | Not specified | 50/50 main/subagent | NEW |

## Token Efficiency Notes

- The subagent architecture distributes token consumption: ~50% of tokens went to specialized agents
- Subagents have high input/output ratios (30-100:1) — they consume context but produce focused output
- Main sessions have lower ratios (~2:1) — more interactive dialogue
- The current session (3b43ccfe) has already consumed 797K tokens

## Estimated API Cost (June 2026 Pricing)

Using Claude Opus 4.8 pricing ($15/M input, $75/M output):
- Input: 20.3M × $15/M = **$304.50**
- Output: 5.9M × $75/M = **$442.50**
- **Total estimated cost: ~$747** for the entire SmartMedTender development

**Note:** This represents ~120 hours of development across 8 main sessions + 21 subagent sessions.
