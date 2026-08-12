#!/usr/bin/env python3
"""Validate the BIL-facing subset of a Blockbench .bbmodel/.ajmodel file."""

from __future__ import annotations

import argparse
import base64
import binascii
import json
from pathlib import Path
import re
import sys


PNG_PREFIX = "data:image/png;base64,"
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
MODEL_ID = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_./-]+$")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Validate a Blockbench model for SemionTD's pinned BIL loader."
    )
    parser.add_argument("model", type=Path, help="Path to a .bbmodel or .ajmodel file")
    parser.add_argument(
        "--model-id",
        help="Expected Minecraft resource ID, for example semion-td:tower/penguin",
    )
    parser.add_argument(
        "--require-animation",
        action="append",
        default=[],
        metavar="NAME",
        help="Animation name that must exist; repeat for multiple names",
    )
    return parser.parse_args()


def load_model(path: Path) -> dict:
    if path.suffix not in {".bbmodel", ".ajmodel"}:
        raise ValueError("model must use the .bbmodel or .ajmodel extension")
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ValueError(f"model does not exist: {path}") from exception
    except json.JSONDecodeError as exception:
        raise ValueError(
            f"invalid JSON at line {exception.lineno}, column {exception.colno}: {exception.msg}"
        ) from exception
    if not isinstance(value, dict):
        raise ValueError("model root must be a JSON object")
    return value


def validate_model_id(path: Path, model_id: str | None, errors: list[str]) -> None:
    if model_id is None:
        return
    if not MODEL_ID.fullmatch(model_id):
        errors.append(f"invalid lowercase Minecraft resource ID: {model_id}")
        return
    namespace, resource_path = model_id.split(":", 1)
    expected = Path("model") / namespace / f"{resource_path}{path.suffix}"
    normalized = path.as_posix()
    if not normalized.endswith(expected.as_posix()):
        errors.append(f"path must end with {expected.as_posix()} for model ID {model_id}")


def validate_structure(model: dict, errors: list[str]) -> None:
    elements = model.get("elements")
    outliner = model.get("outliner")
    textures = model.get("textures")
    if not isinstance(elements, list) or not elements:
        errors.append("elements must be a non-empty array")
    if not isinstance(outliner, list) or not outliner:
        errors.append("outliner must be a non-empty array")
    elif not any(
        isinstance(node, dict) and node.get("export", True) and node.get("children")
        for node in outliner
    ):
        errors.append("outliner must contain an exported group with children")
    if not isinstance(textures, list) or not textures:
        errors.append("textures must be a non-empty array")


def validate_textures(model: dict, errors: list[str]) -> int:
    textures = model.get("textures")
    if not isinstance(textures, list):
        return 0
    valid = 0
    for index, texture in enumerate(textures):
        if not isinstance(texture, dict):
            errors.append(f"textures[{index}] must be an object")
            continue
        name = texture.get("name") or f"textures[{index}]"
        source = texture.get("source")
        if not isinstance(source, str) or not source.startswith(PNG_PREFIX):
            errors.append(f"texture {name!r} must embed PNG data in source")
            continue
        try:
            decoded = base64.b64decode(source[len(PNG_PREFIX) :], validate=True)
        except (binascii.Error, ValueError):
            errors.append(f"texture {name!r} contains invalid base64 data")
            continue
        if not decoded.startswith(PNG_SIGNATURE):
            errors.append(f"texture {name!r} does not decode to a PNG")
            continue
        valid += 1
    return valid


def animation_names(model: dict, errors: list[str]) -> list[str]:
    animations = model.get("animations", [])
    if not isinstance(animations, list):
        errors.append("animations must be an array when present")
        return []
    names: list[str] = []
    for index, animation in enumerate(animations):
        name = animation.get("name") if isinstance(animation, dict) else None
        if not isinstance(name, str) or not name.strip():
            errors.append(f"animations[{index}] must have a non-blank name")
            continue
        names.append(name)
    duplicates = sorted({name for name in names if names.count(name) > 1})
    if duplicates:
        errors.append(f"duplicate animation names: {', '.join(duplicates)}")
    return names


def main() -> int:
    args = parse_args()
    try:
        model = load_model(args.model)
    except (OSError, UnicodeError, ValueError) as exception:
        print(f"ERROR: {exception}", file=sys.stderr)
        return 1

    errors: list[str] = []
    validate_model_id(args.model, args.model_id, errors)
    validate_structure(model, errors)
    valid_textures = validate_textures(model, errors)
    animations = animation_names(model, errors)
    missing = sorted(set(args.require_animation) - set(animations))
    if missing:
        errors.append(f"missing required animations: {', '.join(missing)}")

    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    model_format = model.get("meta", {}).get("format_version", "unknown")
    print(
        f"OK: {args.model} | format={model_format} | "
        f"elements={len(model['elements'])} | textures={valid_textures} | "
        f"animations={','.join(animations) or '-'}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
