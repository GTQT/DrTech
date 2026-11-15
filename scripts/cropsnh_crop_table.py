#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Statically extract the CropsNH crop table (no game launch) into markdown.

Source: E:\\模组开发\\资料库\\CropsNH\\src\\main\\java\\com\\gtnewhorizon\\cropsnh
Output: docs/cropsnh-crop-table.md (written by caller)
"""
import os
import re
import sys
import json

ROOT = r"E:\模组开发\资料库\CropsNH\src\main\java\com\gtnewhorizon\cropsnh"
CROPS = os.path.join(ROOT, "crops")
LOADER = os.path.join(ROOT, "loaders", "CropLoader.java")

VOLTAGE = {
    "ULV": 0, "LV": 1, "MV": 2, "HV": 3, "EV": 4, "IV": 5, "LuV": 6,
    "ZPM": 7, "UV": 8, "UHV": 9, "UEV": 10, "UIV": 11, "UMV": 12,
    "UXV": 13, "MAX": 14,
}
VOLTAGE_NAME = {v: k for k, v in VOLTAGE.items()}

CATEGORY = [
    ("crops/abstracts", None),
    ("crops/food/vanilla", "食物·原版"),
    ("crops/food/bop", "食物·BoP"),
    ("crops/food/natura", "食物·Natura"),
    ("crops/food", "食物"),
    ("crops/material", "材料"),
    ("crops/mobs", "怪物"),
    ("crops/oreBerries", "矿石莓"),
    ("crops/stoneilies/etfuturum", "石百合·EtFuturum"),
    ("crops/stoneilies/modern", "石百合·Modern"),
    ("crops/stoneilies", "石百合"),
    ("crops/thaumcraft", "神秘时代"),
    ("crops/twilightforest", "暮色森林"),
    ("crops/vanilla/flowers", "原版·花"),
    ("crops/vanilla/mushrooms", "原版·蘑菇"),
    ("crops/vanilla", "原版"),
    ("crops/witchery", "巫术"),
    ("crops/TiC", "匠魂"),
    ("crops/biomesoplenty", "BoP"),
    ("crops/natura/nether/berry", "Natura·地狱莓"),
    ("crops/natura/nether/glowshroom", "Natura·地狱菇"),
    ("crops/natura/nether", "Natura·地狱"),
    ("crops/natura", "Natura"),
]


def read(path):
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        return f.read()


def strip_comments(src):
    src = re.sub(r"/\*.*?\*/", "", src, flags=re.S)
    src = re.sub(r"//[^\n]*", "", src)
    return src


def matching_paren(text, start):
    """start points at '('; return index of matching ')' or -1."""
    depth = 0
    i = start
    in_str = False
    in_char = False
    while i < len(text):
        c = text[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == '"':
                in_str = False
        elif in_char:
            if c == "\\":
                i += 2
                continue
            if c == "'":
                in_char = False
        else:
            if c == '"':
                in_str = True
            elif c == "'":
                in_char = True
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    return -1


def split_top(text):
    """split on top-level commas"""
    out, depth, cur = [], 0, []
    in_str = False
    i = 0
    while i < len(text):
        c = text[i]
        if in_str:
            cur.append(c)
            if c == "\\":
                cur.append(text[i + 1])
                i += 2
                continue
            if c == '"':
                in_str = False
        else:
            if c == '"':
                in_str = True
                cur.append(c)
            elif c in "([{":
                depth += 1
                cur.append(c)
            elif c in ")]}":
                depth -= 1
                cur.append(c)
            elif c == "," and depth == 0:
                out.append("".join(cur).strip())
                cur = []
            else:
                cur.append(c)
        i += 1
    if cur:
        out.append("".join(cur).strip())
    return out


class Cls:
    def __init__(self, path):
        self.path = path
        self.src = read(path)
        self.clean = strip_comments(self.src)
        self.name = self._find(r"\bclass\s+(\w+)")
        self.parent = self._find(r"\bclass\s+\w+\s+extends\s+(\w+)")
        self.pkg = self._find(r"\bpackage\s+([\w.]+)")
        self.consts = {}
        for m in re.finditer(r"(?:static\s+final|final\s+static)\s+int\s+(\w+)\s*=\s*([\w.+\-*/ ]+);", self.clean):
            self.consts[m.group(1)] = m.group(2).strip()

    def _find(self, pat):
        m = re.search(pat, self.clean)
        return m.group(1) if m else None

    def method_body(self, name):
        """return body of first method with this name (any return type)"""
        for m in re.finditer(r"\b" + re.escape(name) + r"\s*\([^;{]*\)\s*\{", self.clean):
            brace = self.clean.index("{", m.end() - 1)
            depth = 0
            i = brace
            while i < len(self.clean):
                if self.clean[i] == "{":
                    depth += 1
                elif self.clean[i] == "}":
                    depth -= 1
                    if depth == 0:
                        return self.clean[brace + 1:i]
                i += 1
        return None

    def has_method(self, name):
        return re.search(r"\b" + re.escape(name) + r"\s*\([^;{]*\)\s*\{", self.clean) is not None


def load_classes():
    classes = {}
    for dirpath, _, files in os.walk(CROPS):
        for fn in files:
            if not fn.endswith(".java"):
                continue
            c = Cls(os.path.join(dirpath, fn))
            if c.name:
                classes[c.name] = c
    # base classes outside crops/
    for rel in ["api/CropCard.java"]:
        c = Cls(os.path.join(ROOT, rel))
        if c.name and c.name not in classes:
            classes[c.name] = c
    return classes


def eval_expr(expr, cls, classes, cache, seen):
    if expr is None:
        return None
    expr = expr.strip().rstrip(";").strip()
    expr = expr.replace("this.", "")
    m = re.fullmatch(r"-?\d+", expr)
    if m:
        return int(expr)
    m = re.fullmatch(r"VoltageIndex\.(\w+)", expr)
    if m:
        return VOLTAGE.get(m.group(1))
    if expr in ("getTier()", "this.getTier()"):
        return resolve_int(cls, "getTier", classes, cache, seen, default=1)
    m = re.fullmatch(r"(\d+)\s*\*\s*getTier\(\)", expr)
    if m:
        t = resolve_int(cls, "getTier", classes, cache, seen, default=1)
        return int(m.group(1)) * t if t is not None else None
    m = re.fullmatch(r"getTier\(\)\s*\*\s*(\d+)", expr)
    if m:
        t = resolve_int(cls, "getTier", classes, cache, seen, default=1)
        return t * int(m.group(1)) if t is not None else None
    if expr in cls.consts:
        return eval_expr(cls.consts[expr], cls, classes, cache, seen)
    # simple arithmetic with constants
    if re.fullmatch(r"[\w.+\-*/ ()]+", expr):
        e = expr
        for k, v in list(cls.consts.items()) + list(classes.get("NHCropCard", ).consts.items() if False else []):
            e = re.sub(r"\b" + re.escape(k) + r"\b", str(v), e)
        try:
            return int(eval(e, {"__builtins__": {}}, {}))
        except Exception:
            return None
    return None


class NoFile:
    consts = {}


def resolve_raw(cls, method, classes, depth=0):
    """Find the raw return expression for method starting at cls and walking up."""
    if cls is None or depth > 8:
        return None, None
    body = cls.method_body(method)
    if body is not None:
        m = re.search(r"return\s+([^;]+);", body)
        if m:
            return m.group(1).strip(), cls
        return None, cls
    parent = classes.get(cls.parent)
    return resolve_raw(parent, method, classes, depth + 1)


def resolve_int(cls, method, classes, cache, seen=None, default=None):
    key = (cls.name, method)
    if key in cache:
        return cache[key]
    if seen is None:
        seen = set()
    if key in seen:
        return default
    seen.add(key)
    expr, owner = resolve_raw(cls, method, classes)
    if expr is None:
        cache[key] = default
        return default
    val = eval_expr(expr, owner, classes, cache, seen)
    if val is None:
        val = default
    cache[key] = val
    return val


def resolve_ref(cls, method, classes, prefix):
    expr, owner = resolve_raw(cls, method, classes)
    if not expr:
        return None
    m = re.search(re.escape(prefix) + r"\.(\w+)", expr)
    return m.group(1) if m else None


def chain_of(cls, classes):
    out, seen = [], set()
    while cls is not None and cls.name not in seen:
        out.append(cls)
        seen.add(cls.name)
        cls = classes.get(cls.parent)
    return out


def chain_own(cls, classes):
    """chain without the API base helpers (their bodies contain helper calls)."""
    return [c for c in chain_of(cls, classes) if c.name not in ("CropCard", "NHCropCard")]


def category_for(path):
    rel = os.path.relpath(path, os.path.dirname(CROPS)).replace("\\", "/")
    low = rel.lower()
    for prefix, label in CATEGORY:
        if low.startswith(prefix.lower() + "/"):
            return label, prefix
    if rel in ("crops/CropWeed.java","crops/CropMigrator.java"):
        return "特殊", rel
    return None, rel



def matching_brace(text, start):
    depth = 0
    i = start
    in_str = False
    while i < len(text):
        c = text[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == '"':
                in_str = False
        else:
            if c == '"':
                in_str = True
            elif c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    return -1


def mod_gates(text):
    """Return [(start,end,mod)] regions gated by ModUtils.<mod>.isModLoaded()."""
    gates = []
    for m in re.finditer(r"if\s*\(\s*ModUtils\.(\w+)\.isModLoaded\(\)\s*\)\s*\{", text):
        b = text.index("{", m.end() - 1)
        e = matching_brace(text, b)
        if e > 0:
            gates.append((m.start(), e, m.group(1)))
    # early-return guards: if (!ModUtils.X.isModLoaded()) return;
    for m in re.finditer(r"if\s*\(\s*!\s*ModUtils\.(\w+)\.isModLoaded\(\)\s*\)\s*return\s*;", text):
        # scope = rest of enclosing method (find next "private static void" after m)
        nxt = re.search(r"private\s+static\s+void\s+\w+", text[m.end():])
        e = m.end() + (nxt.start() if nxt else len(text) - m.end())
        gates.append((m.start(), e, m.group(1)))
    return gates


def gates_for(pos, gates):
    return sorted({g[2] for g in gates if g[0] <= pos <= g[1]})


def parse_registrations(text, gates=None):
    regs = []
    for m in re.finditer(r"CropRegistry\.instance\.register\s*\(", text):
        start = m.end() - 1
        end = matching_paren(text, start)
        if end < 0:
            continue
        stmt = text[start:end + 1]
        vm = re.search(r"CropsNHCrops\.(\w+)\s*=\s*", stmt)
        var = vm.group(1) if vm else None
        nm = re.search(r"new\s+(\w+)\s*\(", stmt)
        if not nm:
            continue
        ctor = nm.group(1)
        cstart = stmt.index("(", nm.end() - 1)
        cend = matching_paren(stmt, cstart)
        args = split_top(stmt[cstart + 1:cend])
        chain = stmt[cend + 1:]
        mods = gates_for(m.start(), gates or [])
        regs.append({
            "var": var,
            "class": ctor,
            "args": args,
            "chain": chain,
            "raw": stmt,
            "mods": mods,
        })
    return regs


def drop_calls(text):
    out = []
    for m in re.finditer(r"\.addDrop\s*\(", text):
        s = text.index("(", m.end() - 1)
        e = matching_paren(text, s)
        if e > 0:
            out.append(text[s + 1:e])
    return out


def main():
    classes = load_classes()
    cache = {}
    text = read(LOADER)
    clean_loader = strip_comments(text)
    gates = mod_gates(clean_loader)
    regs = parse_registrations(clean_loader, gates)

    rows = []
    for idx, r in enumerate(regs):
        cls = classes.get(r["class"])
        if cls is None:
            continue
        cat, rel = category_for(cls.path)
        if cat is None:
            if cls.name in ("CropBonsai",) or r["var"] and "Bonsai" in r["var"]:
                cat, rel = "盆景树", "crops/abstracts"
            else:
                continue
        crop_id = None
        for a in r["args"]:
            m = re.fullmatch(r'"([^"]+)"', a.strip())
            if m:
                crop_id = m.group(1)
                break
        if crop_id is None:
            m = re.search(r'super\(\s*"([^"]+)"', cls.clean)
            crop_id = m.group(1) if m else (r["var"] or cls.name)

        is_bonsai = False
        p = cls
        while p:
            if p.name in ("CropBonsai", "CropSlimyBonsai"):
                is_bonsai = True
                break
            p = classes.get(p.parent)
        tier = resolve_int(cls, "getTier", classes, cache, default=1)
        if is_bonsai and len(r["args"]) > 4:
            try:
                tier = int(r["args"][4])
            except Exception:
                pass
        duration = resolve_int(cls, "getGrowthDuration", classes, cache)
        if duration is None:
            duration = 600 * (tier or 1)
        min_stage = resolve_int(cls, "getMinGrowthStage", classes, cache, default=1)
        max_stage = resolve_int(cls, "getMaxGrowthStage", classes, cache, default=4)
        render = resolve_ref(cls, "getRenderShape", classes, "PlantRenderShape") or "Hash"
        soil = resolve_ref(cls, "getSoilTypes", classes, "CropsNHSoilTypes") or "farmland"
        machine_tier = resolve_int(cls, "getMachineBreedingRecipeTier", classes, cache, default=1)
        min_bed = resolve_int(cls, "getMinSeedBedTier", classes, cache, default=-1)
        entity_dmg = None
        for c in chain_own(cls, classes):
            b = c.method_body("getEntityDamage")
            if b is None:
                continue
            m = re.search(r"return\s+([\d.]+)F?", b)
            if m:
                entity_dmg = float(m.group(1))
            break
        subsoil = re.findall(r"addSubSoilRequirement\(\s*CropsNHSubSoilTypes\.(\w+)", cls.clean)
        liked = []
        m = re.search(r"addLikedBiomes\(([^)]*)\)", r["chain"])
        if m:
            liked = re.findall(r"BiomeDictionary\.Type\.(\w+)", m.group(1))
        req_txt = []
        for c in chain_own(cls, classes):
            for nm2, args2 in re.findall(r"addGrowthRequirement\(\s*new\s+(\w+)\s*\(([^()]*)\)", c.clean):
                args2 = re.sub(r"\s+", " ", args2.strip())
                req_txt.append(nm2.replace("GrowthRequirement", "") + ("(" + args2 + ")" if args2 else ""))
        req_txt = list(dict.fromkeys(req_txt))
        # inherited constructor drops + registration drops
        all_drops = []
        for c in chain_own(cls, classes):
            all_drops += drop_calls(c.clean)
        all_drops += drop_calls(r["chain"])
        alt = sum(len(re.findall(r"\.addAlternateSeed", c.clean)) for c in chain_own(cls, classes)) + len(re.findall(r"\.addAlternateSeed", r["chain"]))
        catn = sum(len(re.findall(r"\.addDuplicationCatalyst", c.clean)) for c in chain_own(cls, classes)) + len(re.findall(r"\.addDuplicationCatalyst", r["chain"]))
        hide = False
        for c in chain_own(cls, classes):
            b3 = c.method_body("hideFromNEI")
            if b3 is not None:
                hide = "return true" in b3
                break
        spreads_body = None
        for c in chain_own(cls, classes):
            b2 = c.method_body("spreadsWeeds")
            if b2 is not None:
                spreads_body = b2
                break
        notes = []
        if req_txt:
            notes.append("需求:" + "/".join(dict.fromkeys(req_txt)))
        if liked:
            notes.append("喜好:" + "/".join(liked))
        if entity_dmg:
            notes.append("伤害%.1f" % entity_dmg)
        if alt:
            notes.append("替代种子×%d" % alt)
        if catn:
            notes.append("催化剂×%d" % catn)
        if hide:
            notes.append("NEI隐藏")
        if spreads_body and "return true" in spreads_body:
            notes.append("必长杂草")
        rows.append({
            "id": crop_id, "class": cls.name, "cat": cat, "rel": rel,
            "tier": tier, "dur": duration, "min_stage": min_stage, "max_stage": max_stage,
            "render": render, "soil": soil, "subsoil": subsoil,
            "machine": machine_tier, "minbed": min_bed,
            "drops": len(all_drops), "notes": "; ".join(notes),
            "mods": r["mods"],
            "order": idx,
        })

    with open("build/crop_rows.json","w",encoding="utf-8") as f:
        json.dump(rows,f,ensure_ascii=False,indent=1)
    print("rows:",len(rows))


if __name__ == "__main__":
    main()
