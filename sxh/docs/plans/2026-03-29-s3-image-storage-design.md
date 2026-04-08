# S3 Compatible Image Storage Design

## Background

The project already routes image upload through a single abstraction:

- `ImageRestController`
- `ImageService`
- `ImageUploader`

Current storage implementations are:

- `local`
- `ali`
- `rest`

The goal is to add a reusable `s3` storage type for S3-compatible providers and switch production image upload to the Cloudflare R2 bucket `yuxbao-blog-storage`.

## Requirements

- Keep existing upload APIs unchanged.
- Keep existing image save and external image transfer behavior unchanged.
- Add a reusable S3-compatible uploader instead of a Cloudflare-only implementation.
- Store credentials in YAML as requested.
- Return public image URLs using the bucket public host, not the S3 API endpoint.

## Chosen Approach

Add a new `S3OssWrapper` implementation of `ImageUploader` based on AWS SDK for Java v2 `S3Client`.

Reasons:

- It matches S3-compatible storage semantics directly.
- It works for Cloudflare R2 and can later support AWS S3, MinIO, and similar providers.
- It keeps changes isolated to the image storage integration layer.

## Configuration Design

Extend `image.oss` with S3-compatible fields:

- `type`
- `prefix`
- `endpoint`
- `ak`
- `sk`
- `bucket`
- `host`
- `region`
- `pathStyleAccess`

For the current production target:

- `type: s3`
- `endpoint: https://cca306661d9917b152c671ca2cb7fe55.r2.cloudflarestorage.com`
- `bucket: yuxbao-blog-storage`
- `host: https://r2-storage.yux-bao.site/`
- `region: auto`
- `pathStyleAccess: true`

## Upload Flow

1. Read the uploaded stream into bytes.
2. Compute MD5 to keep the current de-duplication naming behavior.
3. Build the object key as `prefix + md5 + .ext`.
4. Upload with `S3Client.putObject`.
5. Return the public URL as `host + objectKey`.

## Error Handling

- Preserve current behavior: uploader returns an empty string on upload failure.
- Keep external image transfer fallback logic unchanged in `ImageServiceImpl`.
- Preserve `uploadIgnore` behavior by checking whether the URL already starts with the configured public host.

## Scope

Included:

- Config model changes
- New `s3` uploader
- Dependency wiring
- Production image config switch

Not included:

- Multi-bucket routing
- Runtime provider switching beyond `image.oss.type`
- Signed URL support
- Bucket ACL or CORS management

## Verification Plan

- Compile the backend modules that include the new uploader.
- Check that Spring can bind the new config fields.
- Confirm that `prod/application-image.yml` now targets the Cloudflare R2 bucket.

## Rollback

Rollback only requires:

- switch `image.oss.type` back to `local` or `ali`
- restore previous `application-image.yml` values

No controller or service API rollback is needed.
