#!/usr/bin/env python3
from __future__ import annotations

import html
import os
import re
import subprocess
import sys
from collections import defaultdict
from dataclasses import dataclass
from typing import Dict, List
from urllib.parse import parse_qsl, urlencode, urlsplit, urlunsplit

import boto3
import requests
from botocore.config import Config


USER_AGENT = (
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
)
DEFAULT_R2_ENDPOINT = "https://cca306661d9917b152c671ca2cb7fe55.r2.cloudflarestorage.com"
DEFAULT_R2_AK = "2dd8b3ca55c9f0ff2f2a69e0f12bfba3"
DEFAULT_R2_SK = "00c57ce9c8a1bbb580444326e9cde07a820f4db2f6d5b3ed2ca01638fcf85899"
DEFAULT_R2_BUCKET = "yuxbao-blog-storage"
DEFAULT_R2_HOST = "https://r2-storage.yux-bao.site/"
DEFAULT_R2_PREFIX = "yuxbao-sixianghui-oss/"
DEFAULT_R2_REGION = "auto"

DEFAULT_DB_HOST = "127.0.0.1"
DEFAULT_DB_PORT = "3306"
DEFAULT_DB_USER = "root"
DEFAULT_DB_PASSWORD = "Baoyu273511a"
DEFAULT_DB_NAME = "pai_coding"


@dataclass(frozen=True)
class CoverAsset:
    object_name: str
    photo_page: str


COVER_ASSETS: List[CoverAsset] = [
    CoverAsset("homepage/all-1.jpg", "https://images.unsplash.com/photo-1773332611522-06b86b48cbf1"),
    CoverAsset("homepage/all-2.jpg", "https://images.unsplash.com/photo-1451187580459-43490279c0fa"),
    CoverAsset("homepage/all-3.jpg", "https://images.unsplash.com/photo-1535957998253-26ae1ef29506"),
    CoverAsset("homepage/all-4.jpg", "https://images.unsplash.com/photo-1610116306796-6fea9f4fae38"),
    CoverAsset("homepage/backend-1.jpg", "https://images.unsplash.com/photo-1461749280684-dccba630e2f6"),
    CoverAsset("homepage/backend-2.jpg", "https://images.unsplash.com/photo-1542831371-29b0f74f9713"),
    CoverAsset("homepage/backend-3.jpg", "https://images.unsplash.com/photo-1531297484001-80022131f5a1"),
    CoverAsset("homepage/backend-4.jpg", "https://images.unsplash.com/photo-1497366811353-6870744d04b2"),
    CoverAsset("homepage/life-1.jpg", "https://images.unsplash.com/photo-1506126613408-eca07ce68773"),
    CoverAsset("homepage/life-2.jpg", "https://images.unsplash.com/photo-1598468872842-d39d85892daf"),
    CoverAsset("homepage/read-1.jpg", "https://images.unsplash.com/photo-1610116306796-6fea9f4fae38"),
    CoverAsset("homepage/read-2.jpg", "https://images.unsplash.com/photo-1604866830893-c13cafa515d5"),
    CoverAsset("homepage/ai-1.jpg", "https://images.unsplash.com/photo-1773332585754-f1436987743b"),
    CoverAsset("homepage/ai-2.jpg", "https://images.unsplash.com/photo-1697577418970-95d99b5a55cf"),
    CoverAsset("article/life-1.jpg", "https://images.unsplash.com/photo-1506126613408-eca07ce68773"),
    CoverAsset("article/life-2.jpg", "https://images.unsplash.com/photo-1598468872842-d39d85892daf"),
    CoverAsset("article/knowledge-1.jpg", "https://images.unsplash.com/photo-1610116306796-6fea9f4fae38"),
    CoverAsset("article/knowledge-2.jpg", "https://images.unsplash.com/photo-1604866830893-c13cafa515d5"),
    CoverAsset("article/tech-1.jpg", "https://images.unsplash.com/photo-1773332611522-06b86b48cbf1"),
    CoverAsset("article/tech-2.jpg", "https://images.unsplash.com/photo-1451187580459-43490279c0fa"),
    CoverAsset("article/backend-1.jpg", "https://images.unsplash.com/photo-1461749280684-dccba630e2f6"),
    CoverAsset("article/backend-2.jpg", "https://images.unsplash.com/photo-1542831371-29b0f74f9713"),
    CoverAsset("article/database-1.jpg", "https://images.unsplash.com/photo-1644088379091-d574269d422f"),
    CoverAsset("article/database-2.jpg", "https://images.unsplash.com/photo-1568952433726-3896e3881c65"),
    CoverAsset("article/interview-1.jpg", "https://images.unsplash.com/photo-1499951360447-b19be8fe80f5"),
    CoverAsset("article/interview-2.jpg", "https://images.unsplash.com/photo-1497366754035-f200968a6e72"),
    CoverAsset("article/ai-1.jpg", "https://images.unsplash.com/photo-1773332585754-f1436987743b"),
    CoverAsset("article/ai-2.jpg", "https://images.unsplash.com/photo-1697577418970-95d99b5a55cf"),
    CoverAsset("article/codelife-1.jpg", "https://images.unsplash.com/photo-1531141445733-14c2eb7d4c1f"),
    CoverAsset("article/codelife-2.jpg", "https://images.unsplash.com/photo-1517487881594-2787fef5ebf7"),
    CoverAsset("article/read-1.jpg", "https://images.unsplash.com/photo-1610116306796-6fea9f4fae38"),
    CoverAsset("article/read-2.jpg", "https://images.unsplash.com/photo-1604866830893-c13cafa515d5"),
]


CATEGORY_COVER_OBJECTS: Dict[str, List[str]] = {
    "后端": ["article/backend-1.jpg", "article/backend-2.jpg"],
    "人工智能": ["article/ai-1.jpg", "article/ai-2.jpg"],
    "生活": ["article/life-1.jpg", "article/life-2.jpg"],
    "知识": ["article/knowledge-1.jpg", "article/knowledge-2.jpg"],
    "科技": ["article/tech-1.jpg", "article/tech-2.jpg"],
    "数据库": ["article/database-1.jpg", "article/database-2.jpg"],
    "面试八股": ["article/interview-1.jpg", "article/interview-2.jpg"],
    "代码人生": ["article/codelife-1.jpg", "article/codelife-2.jpg"],
    "阅读": ["article/read-1.jpg", "article/read-2.jpg"],
}

FALLBACK_CATEGORY = ["article/tech-1.jpg", "article/tech-2.jpg"]


def env(name: str, default: str) -> str:
    return os.environ.get(name, default).strip()


def sql_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace("'", "\\'")


def build_mysql_base_command() -> List[str]:
    host = env("SXH_DB_HOST", DEFAULT_DB_HOST)
    port = env("SXH_DB_PORT", DEFAULT_DB_PORT)
    user = env("SXH_DB_USER", DEFAULT_DB_USER)
    database = env("SXH_DB_NAME", DEFAULT_DB_NAME)
    return ["mysql", f"-h{host}", f"-P{port}", f"-u{user}", "-N", "-B", "-D", database]


def run_mysql(sql: str) -> str:
    command = build_mysql_base_command() + ["-e", sql]
    result = subprocess.run(
        command,
        env={**os.environ, "MYSQL_PWD": env("SXH_DB_PASSWORD", DEFAULT_DB_PASSWORD)},
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout.strip()


def parse_unsplash_image_url(photo_page: str) -> str:
    parts = urlsplit(html.unescape(photo_page))
    query = dict(parse_qsl(parts.query, keep_blank_values=True))
    query.update(
        {
            "w": "1600",
            "h": "900",
            "fit": "crop",
            "auto": "format",
            "fm": "jpg",
            "q": "82",
        }
    )
    return urlunsplit((parts.scheme, parts.netloc, parts.path, urlencode(query), ""))


def build_s3_client():
    endpoint = env("SXH_R2_ENDPOINT", DEFAULT_R2_ENDPOINT)
    ak = env("SXH_R2_AK", DEFAULT_R2_AK)
    sk = env("SXH_R2_SK", DEFAULT_R2_SK)
    region = env("SXH_R2_REGION", DEFAULT_R2_REGION)
    return boto3.client(
        "s3",
        endpoint_url=endpoint,
        aws_access_key_id=ak,
        aws_secret_access_key=sk,
        region_name=region,
        config=Config(signature_version="s3v4", s3={"addressing_style": "path"}),
    )


def upload_cover_assets() -> Dict[str, str]:
    bucket = env("SXH_R2_BUCKET", DEFAULT_R2_BUCKET)
    host = env("SXH_R2_HOST", DEFAULT_R2_HOST).rstrip("/")
    prefix = env("SXH_R2_PREFIX", DEFAULT_R2_PREFIX).strip("/")
    s3_client = build_s3_client()
    uploaded_urls: Dict[str, str] = {}
    page_cache: Dict[str, bytes] = {}

    for asset in COVER_ASSETS:
        if asset.photo_page not in page_cache:
            image_url = parse_unsplash_image_url(asset.photo_page)
            image_response = requests.get(image_url, headers={"User-Agent": USER_AGENT}, timeout=60)
            image_response.raise_for_status()
            page_cache[asset.photo_page] = image_response.content

        object_key = f"{prefix}/images/cover/{asset.object_name}"
        s3_client.put_object(
            Bucket=bucket,
            Key=object_key,
            Body=page_cache[asset.photo_page],
            ContentType="image/jpeg",
            CacheControl="public, max-age=31536000, immutable",
        )
        uploaded_urls[asset.object_name] = f"{host}/{object_key}"
        print(f"uploaded {asset.object_name} -> {uploaded_urls[asset.object_name]}")

    return uploaded_urls


def update_article_covers(uploaded_urls: Dict[str, str]) -> None:
    rows = run_mysql(
        """
        SELECT a.id, c.category_name, a.title
        FROM article a
        JOIN category c ON c.id = a.category_id
        WHERE a.deleted = 0
          AND a.status = 1
          AND (a.picture = '' OR a.picture IS NULL OR a.picture LIKE '%xuyifei-oss%')
        ORDER BY c.id, a.id;
        """
    )
    if not rows:
        print("no article cover needs update")
        return

    category_index: Dict[str, int] = defaultdict(int)
    statements: List[str] = []
    updated = 0
    for line in rows.splitlines():
        article_id, category_name, title = line.split("\t", 2)
        object_names = CATEGORY_COVER_OBJECTS.get(category_name, FALLBACK_CATEGORY)
        position = category_index[category_name] % len(object_names)
        object_name = object_names[position]
        category_index[category_name] += 1
        picture = uploaded_urls[object_name]
        statements.append(f"UPDATE article SET picture = '{sql_escape(picture)}' WHERE id = {article_id};")
        updated += 1
        print(f"set article {article_id} [{category_name}] {title} -> {picture}")

    run_mysql("\n".join(statements))
    print(f"updated {updated} articles")


def verify_result() -> None:
    output = run_mysql(
        """
        SELECT
          COUNT(*) AS total_online,
          SUM(CASE WHEN picture = '' OR picture IS NULL THEN 1 ELSE 0 END) AS empty_picture,
          SUM(CASE WHEN picture LIKE '%xuyifei-oss%' THEN 1 ELSE 0 END) AS xuyifei_picture,
          SUM(CASE WHEN picture LIKE '%r2-storage.yux-bao.site/%' THEN 1 ELSE 0 END) AS r2_picture
        FROM article
        WHERE deleted = 0 AND status = 1;
        """
    )
    print("verification:", output)


def main() -> int:
    try:
        uploaded_urls = upload_cover_assets()
        update_article_covers(uploaded_urls)
        verify_result()
        return 0
    except Exception as exc:  # noqa: BLE001
        print(f"refresh article covers failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
