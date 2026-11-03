def main():
    event = read_github_event()

    pr = get_pull_request(event)
    files = get_changed_files(pr)
    context = collect_related_code(pr, files)

    findings = review_with_ai(
        changes=files,
        context=context,
        rules=REVIEW_RULES,
    )

    findings = validate_and_deduplicate(findings)

    # 代码已经更新，就丢弃这次旧版本的结果
    if get_latest_head_sha(pr) != pr["head"]["sha"]:
        return

    publish_review(
        pr=pr,
        commit_sha=pr["head"]["sha"],
        findings=findings,
    )